package com.example.historymaps

import android.graphics.Color
import android.os.Bundle
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.example.historymaps.data.FrontScenario
import com.example.historymaps.model.FrontModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polygon

class MapActivity : AppCompatActivity() {

    private lateinit var map: MapView
    private lateinit var slider: SeekBar
    private lateinit var frontModel: FrontModel
    private var frontPolygon: Polygon? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        map = findViewById(R.id.map)
        slider = findViewById(R.id.slider)

        map.setMultiTouchControls(true)
        map.controller.setZoom(6.0)
        map.controller.setCenter(GeoPoint(68.4, 17.4)) // Нарвик

        frontModel = FrontModel(FrontScenario.narvikLanding)
        slider.max = frontModel.daysCount

        slider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                updateFront(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        updateFront(0)
    }

    private fun updateFront(day: Int) {
        val points = frontModel.frontForDay(day)

        frontPolygon?.let { map.overlays.remove(it) }

        frontPolygon = Polygon().apply {
            this.points = points
            fillColor = Color.argb(60, 200, 0, 0)
            strokeColor = Color.RED
            strokeWidth = 3f
        }

        map.overlays.add(frontPolygon)
        map.invalidate()
    }
}

