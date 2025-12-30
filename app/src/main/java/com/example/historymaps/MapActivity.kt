package com.example.historymaps

import android.animation.ValueAnimator
import android.graphics.Color
import android.graphics.Path
import android.os.Bundle
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import java.text.SimpleDateFormat
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI
import java.util.*

class MapActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private lateinit var dateSeekBar: SeekBar
    private lateinit var dateTextView: TextView
    private lateinit var btnPrev: Button
    private lateinit var btnNext: Button

    private val markers = mutableListOf<Marker>()
    private var currentFrontLine: Polygon? = null
    private var animator: ValueAnimator? = null

    private var currentDateIndex = 0

    // Плавные линии фронта с использованием кривых
    private val frontLineData = listOf(
        // 9 апреля 1940 - Начало вторжения (береговая линия)
        createSmoothFrontLine(
            coastalPoints = listOf(
                GeoPoint(58.1465, 8.0181),   // Ставангер - высадка
                GeoPoint(60.3913, 5.3221),   // Берген - высадка
                GeoPoint(63.4305, 10.3951),  // Тронхейм - высадка
                GeoPoint(68.4384, 17.4273)   // Нарвик - высадка
            ),
            depthPercent = 0.0,  // Только на побережье
            curveIntensity = 0.3  // Слабая кривизна
        ),

        // 14 апреля 1940 - Продвижение вглубь (25-50 км от берега)
        createSmoothFrontLine(
            coastalPoints = listOf(
                GeoPoint(58.5, 8.5),    // Юг - продвижение
                GeoPoint(60.8, 6.5),    // Берген - продвижение
                GeoPoint(62.5, 10.0),   // Центр - высадка союзников
                GeoPoint(64.5, 12.0),   // Намсус - плацдарм
                GeoPoint(66.5, 15.0),   // Север - бои
                GeoPoint(68.0, 16.5)    // Подходы к Нарвику
            ),
            depthPercent = 0.15,  // 15% продвижение вглубь
            curveIntensity = 0.4  // Средняя кривизна
        ),

        // 30 апреля 1940 - Установление контроля (50-100 км от берега)
        createSmoothFrontLine(
            coastalPoints = listOf(
                GeoPoint(59.0, 9.0),    // Юг - контроль
                GeoPoint(61.5, 7.5),    // Запад - контроль
                GeoPoint(63.5, 11.0),   // Центр - контроль
                GeoPoint(65.5, 13.5),   // Север - продвижение
                GeoPoint(67.5, 15.5),   // Север - бои
                GeoPoint(68.3, 16.2)    // Нарвик - окружение
            ),
            depthPercent = 0.35,  // 35% продвижение вглубь
            curveIntensity = 0.5  // Сильная кривизна
        ),

        // 10 июня 1940 - Полный контроль (фронт проходит по горам)
        createSmoothFrontLine(
            coastalPoints = listOf(
                GeoPoint(59.5, 10.0),   // Восточная граница
                GeoPoint(61.0, 9.0),    // Горные районы
                GeoPoint(63.0, 12.0),   // Центральные горы
                GeoPoint(65.0, 14.0),   // Северные плато
                GeoPoint(67.0, 16.0),   // Арктический круг
                GeoPoint(68.5, 17.5)    // Граница со Швецией
            ),
            depthPercent = 0.6,   // 60% - линия по горам
            curveIntensity = 0.6  // Максимальная кривизна (горный рельеф)
        )
    )

    // Описания дат
    private val dateDescriptions = listOf(
        "9 апреля 1940 - Высадка немецких войск в ключевых портах",
        "14 апреля 1940 - Продвижение вглубь страны, высадка союзников",
        "30 апреля 1940 - Установление контроля, эвакуация союзников",
        "10 июня 1940 - Полный контроль Германии над Норвегией"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        Configuration.getInstance().userAgentValue = packageName
        Configuration.getInstance().load(this, getPreferences(MODE_PRIVATE))

        // Инициализация всех элементов
        mapView = findViewById(R.id.mapView)
        dateTextView = findViewById(R.id.dateTextView)
        dateSeekBar = findViewById(R.id.dateSeekBar)
        btnPrev = findViewById(R.id.btnPrev)
        btnNext = findViewById(R.id.btnNext)

        setupMap()
        setupSeekBar()
        setupButtons()

        // Начальное состояние
        updateDate(0, animate = false)
    }

    /**
     * Создает плавную линию фронта с изгибами
     * @param coastalPoints основные точки на побережье
     * @param depthPercent насколько глубоко линия уходит в страну (0.0-1.0)
     * @param curveIntensity интенсивность изгибов (0.0-1.0)
     */
    private fun createSmoothFrontLine(
        coastalPoints: List<GeoPoint>,
        depthPercent: Double,
        curveIntensity: Double
    ): List<GeoPoint> {
        val smoothedPoints = mutableListOf<GeoPoint>()

        // Если это береговая линия (depthPercent = 0)
        if (depthPercent == 0.0) {
            // Просто возвращаем точки побережья с небольшой сглаживающей кривой
            return createBezierCurve(coastalPoints, tension = 0.3)
        }

        // Для линий внутри страны: сдвигаем точки от побережья вглубь
        for (i in coastalPoints.indices) {
            val coastalPoint = coastalPoints[i]

            // Направление вглубь страны (примерно на восток/северо-восток)
            val directionAngle = when (i) {
                0 -> 80.0  // Юг - больше на восток
                1 -> 70.0  // Запад - северо-восток
                2 -> 60.0  // Центр - северо-восток
                3 -> 50.0  // Север - север
                4 -> 40.0  // Крайний север
                else -> 60.0
            }

            // Расстояние продвижения вглубь (в градусах)
            val distance = depthPercent * when (i) {
                0 -> 1.5  // Юг - больше пространства
                1 -> 1.2  // Запад - горы
                2 -> 1.0  // Центр
                3 -> 0.8  // Север
                4 -> 0.6  // Крайний север
                else -> 1.0
            }

            // Вычисляем новую точку
            val radians = Math.toRadians(directionAngle)
            val latOffset = distance * cos(radians) * 0.8
            val lonOffset = distance * sin(radians)

            val inlandPoint = GeoPoint(
                coastalPoint.latitude + latOffset,
                coastalPoint.longitude + lonOffset
            )

            // Добавляем случайные изгибы для естественности
            val finalPoint = if (curveIntensity > 0.3 && i > 0 && i < coastalPoints.size - 1) {
                addNaturalCurve(inlandPoint, i, curveIntensity)
            } else {
                inlandPoint
            }

            smoothedPoints.add(finalPoint)
        }

        // Создаем плавную кривую Безье через все точки
        return createBezierCurve(smoothedPoints, tension = 0.5 * curveIntensity)
    }

    /**
     * Создает кривую Безье через набор точек
     */
    private fun createBezierCurve(points: List<GeoPoint>, tension: Double): List<GeoPoint> {
        if (points.size < 2) return points

        val result = mutableListOf<GeoPoint>()
        val n = points.size - 1

        // Добавляем первую точку
        result.add(points[0])

        // Генерируем промежуточные точки кривой
        val segments = 20 // Количество сегментов между точками

        for (i in 0 until n) {
            val p0 = if (i > 0) points[i - 1] else points[i]
            val p1 = points[i]
            val p2 = points[i + 1]
            val p3 = if (i < n - 1) points[i + 2] else points[i + 1]

            for (t in 1..segments) {
                val t1 = t.toDouble() / segments
                val lat = catmullRom(p0.latitude, p1.latitude, p2.latitude, p3.latitude, t1, tension)
                val lon = catmullRom(p0.longitude, p1.longitude, p2.longitude, p3.longitude, t1, tension)
                result.add(GeoPoint(lat, lon))
            }
        }

        // Добавляем последнюю точку
        result.add(points.last())

        return result
    }

    /**
     * Интерполяция Catmull-Rom для плавных кривых
     */
    private fun catmullRom(
        p0: Double, p1: Double, p2: Double, p3: Double,
        t: Double, tension: Double
    ): Double {
        val t2 = t * t
        val t3 = t2 * t

        return 0.5 * (
                (2 * p1) +
                        (-p0 + p2) * t +
                        (2 * p0 - 5 * p1 + 4 * p2 - p3) * t2 +
                        (-p0 + 3 * p1 - 3 * p2 + p3) * t3
                )
    }

    /**
     * Добавляет естественные изгибы к точке
     */
    private fun addNaturalCurve(point: GeoPoint, index: Int, intensity: Double): GeoPoint {
        // Создаем псевдо-случайные, но воспроизводимые изгибы
        val seed = (point.latitude * 100 + point.longitude * 100 + index).toLong()
        val random = Random(seed)

        // Амплитуда изгибов зависит от интенсивности
        val amplitude = intensity * 0.3

        val latOffset = (random.nextDouble() - 0.5) * amplitude
        val lonOffset = (random.nextDouble() - 0.5) * amplitude

        return GeoPoint(point.latitude + latOffset, point.longitude + lonOffset)
    }

    private fun setupMap() {
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)

        val norwayPoint = GeoPoint(63.0, 10.0)
        mapView.controller.setZoom(5.5)
        mapView.controller.setCenter(norwayPoint)
    }

    private fun setupSeekBar() {
        dateSeekBar.max = frontLineData.size - 1

        dateSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    updateDate(progress, animate = true)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                animator?.cancel()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }

    private fun setupButtons() {
        btnPrev.setOnClickListener {
            if (currentDateIndex > 0) {
                updateDate(currentDateIndex - 1, animate = true)
                dateSeekBar.progress = currentDateIndex - 1
            }
        }

        btnNext.setOnClickListener {
            if (currentDateIndex < frontLineData.size - 1) {
                updateDate(currentDateIndex + 1, animate = true)
                dateSeekBar.progress = currentDateIndex + 1
            }
        }
    }

    private fun updateDate(newDateIndex: Int, animate: Boolean = true) {
        if (newDateIndex < dateDescriptions.size) {
            dateTextView.text = dateDescriptions[newDateIndex]
        }

        if (newDateIndex == currentDateIndex) return

        if (!animate || currentFrontLine == null) {
            currentDateIndex = newDateIndex
            drawFrontLine(currentDateIndex)
            updateMarkers(currentDateIndex)
            return
        }

        animateFrontLineTransition(newDateIndex)
        currentDateIndex = newDateIndex
    }

    private fun animateFrontLineTransition(newDateIndex: Int) {
        val currentPoints = frontLineData[currentDateIndex]
        val targetPoints = frontLineData[newDateIndex]

        animator?.cancel()

        // Для анимации используем промежуточные точки
        val animatedPoints = currentPoints.map { GeoPoint(it.latitude, it.longitude) }

        val animatedPolygon = Polygon().apply {
            points = animatedPoints
            fillColor = Color.argb(80, 255, 140, 0)
            strokeColor = Color.argb(220, 255, 140, 0)
            strokeWidth = 6.0f
        }

        currentFrontLine?.let { mapView.overlays.remove(it) }
        mapView.overlays.add(animatedPolygon)

        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 1200
            interpolator = android.view.animation.AccelerateDecelerateInterpolator()

            addUpdateListener { animation ->
                val fraction = animation.animatedValue as Float

                // Плавная интерполяция каждой точки
                for (i in animatedPoints.indices) {
                    val currentIdx = i.coerceAtMost(currentPoints.size - 1)
                    val targetIdx = i.coerceAtMost(targetPoints.size - 1)

                    if (currentIdx < currentPoints.size && targetIdx < targetPoints.size) {
                        val start = currentPoints[currentIdx]
                        val end = targetPoints[targetIdx]

                        // Кубическая интерполяция для плавности
                        val easeFraction = fraction * fraction * (3 - 2 * fraction)

                        animatedPoints[i].latitude = start.latitude + (end.latitude - start.latitude) * easeFraction
                        animatedPoints[i].longitude = start.longitude + (end.longitude - start.longitude) * easeFraction
                    }
                }

                animatedPolygon.points = animatedPoints

                // Плавное изменение цвета
                val red = 255
                val green = (140 * (1 - fraction)).toInt()
                val blue = 0
                animatedPolygon.strokeColor = Color.argb(220, red, green, blue)
                animatedPolygon.fillColor = Color.argb(80, red, green, blue)

                mapView.invalidate()
            }

            addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    mapView.overlays.remove(animatedPolygon)
                    drawFrontLine(newDateIndex)
                    updateMarkers(newDateIndex)
                }
            })

            start()
        }
    }

    private fun drawFrontLine(dateIndex: Int) {
        currentFrontLine?.let { mapView.overlays.remove(it) }

        val points = frontLineData.getOrNull(dateIndex) ?: return

        val polygon = Polygon().apply {
            this.points = points
            fillColor = Color.argb(60, 220, 20, 60) // Полупрозрачный
            strokeColor = Color.argb(220, 220, 20, 60) // Темно-красный
            strokeWidth = 6.0f
        }

        mapView.overlays.add(polygon)
        currentFrontLine = polygon
        mapView.invalidate()
    }

    private fun updateMarkers(dateIndex: Int) {
        markers.forEach { mapView.overlays.remove(it) }
        markers.clear()

        val markerData = getHistoricalMarkers(dateIndex)

        markerData.forEach { marker ->
            val mapMarker = Marker(mapView).apply {
                position = GeoPoint(marker.latitude, marker.longitude)
                title = marker.title
                snippet = marker.description
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

                when (marker.type) {
                    "battle" -> icon = resources.getDrawable(android.R.drawable.ic_menu_compass, theme)
                    "landing" -> icon = resources.getDrawable(android.R.drawable.ic_menu_directions, theme)
                    "capture" -> icon = resources.getDrawable(android.R.drawable.ic_menu_edit, theme)
                    else -> icon = resources.getDrawable(android.R.drawable.ic_menu_mylocation, theme)
                }
            }

            mapView.overlays.add(mapMarker)
            markers.add(mapMarker)
        }

        mapView.invalidate()
    }

    private fun getHistoricalMarkers(dateIndex: Int): List<HistoricalMarker> {
        return when (dateIndex) {
            0 -> listOf(
                HistoricalMarker("Ставангер", "Высадка 9 апреля", 58.1465, 8.0181, "landing"),
                HistoricalMarker("Берген", "Высадка 9 апреля", 60.3913, 5.3221, "landing"),
                HistoricalMarker("Тронхейм", "Высадка 9 апреля", 63.4305, 10.3951, "landing"),
                HistoricalMarker("Нарвик", "Высадка 9 апреля", 68.4384, 17.4273, "landing"),
                HistoricalMarker("Осло", "Захват столицы", 59.9139, 10.7522, "capture")
            )
            1 -> listOf(
                HistoricalMarker("Намсус", "Высадка союзников", 64.4667, 11.5000, "landing"),
                HistoricalMarker("Ондалснес", "Высадка союзников", 62.5678, 7.3333, "landing"),
                HistoricalMarker("Лиллехаммер", "Битва за перевал", 61.1145, 10.4662, "battle"),
                HistoricalMarker("Фёрде", "Немецкое продвижение", 61.4508, 5.8578, "capture")
            )
            2 -> listOf(
                HistoricalMarker("Молде", "Временная столица", 62.7372, 7.1608, "government"),
                HistoricalMarker("Тронхейм", "Основная база", 63.4305, 10.3951, "base"),
                HistoricalMarker("Намсус", "Эвакуирован", 64.4667, 11.5000, "withdrawal"),
                HistoricalMarker("Мушёэн", "Захвачен", 65.8360, 13.1932, "capture")
            )
            3 -> listOf(
                HistoricalMarker("Осло", "Капитуляция", 59.9139, 10.7522, "surrender"),
                HistoricalMarker("Тромсё", "Последнее сопротивление", 69.6496, 18.9553, "battle"),
                HistoricalMarker("Нарвик", "Эвакуация завершена", 68.4384, 17.4273, "withdrawal"),
                HistoricalMarker("Будё", "Крайняя точка", 67.2804, 14.4050, "front")
            )
            else -> emptyList()
        }
    }

    override fun onDestroy() {
        animator?.cancel()
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }
}

// Класс для исторических маркеров
data class HistoricalMarker(
    val title: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val type: String
)