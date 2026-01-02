package com.example.historymaps

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.historymaps.data.FrontScenario
import com.example.historymaps.model.FrontModel
import kotlinx.coroutines.*
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon

class MapActivity : AppCompatActivity() {

    private lateinit var map: MapView
    private lateinit var slider: SeekBar
    private lateinit var dateTextView: TextView
    private lateinit var btnPrev: Button
    private lateinit var btnNext: Button

    private lateinit var frontModel: FrontModel
    private var frontPolygon: Polygon? = null
    private val cityMarkers = mutableListOf<Marker>()

    private val scope = CoroutineScope(Dispatchers.Main)

    companion object {
        private const val TAG = "MapActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "=== НАЧАЛО onCreate ===")

        // Показываем экран загрузки мгновенно
        setContentView(R.layout.activity_map_simple)

        val loadingText = findViewById<TextView>(R.id.loadingText)
        val progressBar = findViewById<android.widget.ProgressBar>(R.id.progressBar)

        // Загружаем тяжелые операции в фоне
        scope.launch {
            try {
                loadingText.text = "Инициализация карты..."

                // Шаг 1: Инициализация OSMDroid в фоне
                withContext(Dispatchers.IO) {
                    Log.d(TAG, "Инициализация OSMDroid в фоне...")
                    Configuration.getInstance().load(
                        applicationContext,
                        getSharedPreferences("osmdroid", MODE_PRIVATE)
                    )
                }

                loadingText.text = "Загрузка данных операции..."

                // Шаг 2: Загрузка данных в фоне
                withContext(Dispatchers.IO) {
                    frontModel = FrontModel(FrontScenario.weserubungNorway)
                }

                // Шаг 3: В UI-потоке переключаем на основной макет
                withContext(Dispatchers.Main) {
                    Log.d(TAG, "Переход на основной макет...")
                    setContentView(R.layout.activity_map)

                    // Инициализация View
                    initViews()
                    setupMap()
                    setupListeners()
                    setupCities()

                    // Показываем начальное состояние
                    slider.max = frontModel.daysCount - 1
                    updateFront(0)

                    Log.d(TAG, "=== УСПЕШНО ЗАГРУЖЕНО ===")
                }

            } catch (e: Exception) {
                Log.e(TAG, "Ошибка загрузки: ${e.message}", e)
                showErrorScreen("Ошибка загрузки карты: ${e.message}")
            }
        }
    }

    private fun initViews() {
        map = findViewById(R.id.map)
        slider = findViewById(R.id.slider)
        dateTextView = findViewById(R.id.dateTextView)
        btnPrev = findViewById(R.id.btnPrev)
        btnNext = findViewById(R.id.btnNext)
    }

    private fun setupMap() {
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)

        // Начальная позиция (центр Норвегии)
        map.controller.setZoom(5.0)
        map.controller.setCenter(GeoPoint(63.0, 10.0))

        map.minZoomLevel = 3.5
        map.maxZoomLevel = 12.0
    }

    private fun setupListeners() {
        slider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    updateFront(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        btnPrev.setOnClickListener {
            if (slider.progress > 0) {
                slider.progress = slider.progress - 1
                updateFront(slider.progress)
            }
        }

        btnNext.setOnClickListener {
            if (slider.progress < slider.max) {
                slider.progress = slider.progress + 1
                updateFront(slider.progress)
            }
        }
    }

    private fun setupCities() {
        val cities = listOf(
            City("Осло", GeoPoint(59.91, 10.75), "Столица Норвегии"),
            City("Берген", GeoPoint(60.39, 5.32), "Крупный порт"),
            City("Тронхейм", GeoPoint(63.43, 10.39), "Историческая столица"),
            City("Ставангер", GeoPoint(58.97, 5.73), "Нефтяная столица"),
            City("Нарвик", GeoPoint(68.44, 17.43), "Железорудный порт"),
            City("Копенгаген", GeoPoint(55.6761, 12.5683), "Столица Дании")
        )

        for (city in cities) {
            val marker = Marker(map).apply {
                position = city.location
                title = city.name
                snippet = city.description
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

                icon = ContextCompat.getDrawable(
                    this@MapActivity,
                    if (city.name == "Нарвик")
                        android.R.drawable.ic_menu_mylocation
                    else
                        android.R.drawable.ic_menu_myplaces
                )
            }
            map.overlays.add(marker)
            cityMarkers.add(marker)
        }
    }

    private fun updateFront(day: Int) {
        Log.d(TAG, "Updating front for day $day")

        try {
            val points = frontModel.frontForDay(day)

            // Удаляем старый полигон
            frontPolygon?.let {
                map.overlays.remove(it)
                frontPolygon = null
            }

            if (points.isNotEmpty()) {
                frontPolygon = Polygon().apply {
                    this.points = points
                    fillColor = Color.argb(80, 220, 50, 50)
                    strokeColor = Color.rgb(180, 0, 0)
                    strokeWidth = 4f
                }

                map.overlays.add(frontPolygon!!)

                // Автоматический зум на линию фронта
                zoomToFrontLine(points)
            }

            // Обновляем текст даты
            dateTextView.text = frontModel.dateForDay(day)

            // Обновляем карту
            map.invalidate()

        } catch (e: Exception) {
            Log.e(TAG, "Error updating front: ${e.message}", e)
            dateTextView.text = "Ошибка: ${e.message}"
        }
    }

    private fun zoomToFrontLine(points: List<GeoPoint>) {
        if (points.size < 2) return

        try {
            var minLat = 90.0
            var maxLat = -90.0
            var minLon = 180.0
            var maxLon = -180.0

            for (point in points) {
                minLat = minOf(minLat, point.latitude)
                maxLat = maxOf(maxLat, point.latitude)
                minLon = minOf(minLon, point.longitude)
                maxLon = maxOf(maxLon, point.longitude)
            }

            val latSpan = maxLat - minLat
            val lonSpan = maxLon - minLon
            val padding = 0.1

            val boundingBox = org.osmdroid.util.BoundingBox(
                maxLat + latSpan * padding,
                maxLon + lonSpan * padding,
                minLat - latSpan * padding,
                minLon - lonSpan * padding
            )

            map.zoomToBoundingBox(boundingBox, true, 100)

        } catch (e: Exception) {
            Log.e(TAG, "Error in zoomToFrontLine: ${e.message}")
        }
    }

    private fun showErrorScreen(errorMessage: String) {
        val textView = TextView(this).apply {
            text = "Ошибка загрузки карты:\n$errorMessage\n\nНажмите для возврата"
            textSize = 16f
            gravity = Gravity.CENTER
            setPadding(32, 32, 32, 32)
            setTextColor(Color.RED)
        }

        textView.setOnClickListener {
            finish()
        }

        setContentView(textView)
    }

    data class City(
        val name: String,
        val location: GeoPoint,
        val description: String = ""
    )

    override fun onResume() {
        super.onResume()
        map.onResume()
        Log.d(TAG, "MapActivity.onResume()")
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
        Log.d(TAG, "MapActivity.onPause()")
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
        cityMarkers.clear()
        Log.d(TAG, "MapActivity.onDestroy()")
    }
}