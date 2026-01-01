package com.example.historymaps

import android.content.Intent
import android.os.Bundle
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.historymaps.MapActivity
import com.example.historymaps.adapters.OperationAdapter
import com.example.historymaps.models.MilitaryOperation
import com.example.historymaps.model.FrontModel
import com.example.historymaps.data.FrontScenario
import org.osmdroid.views.overlay.Polygon
class MainActivity : AppCompatActivity() {
    class MainActivity : AppCompatActivity() {

        private lateinit var frontModel: FrontModel
        private var frontPolygon: Polygon? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        slider.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                updateFront(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        frontModel = FrontModel(FrontScenario.narvikLanding)

        val slider = findViewById<SeekBar>(R.id.slider)


        // Список доступных операций
        val operations = listOf(
            MilitaryOperation(
                id = 1,
                title = "Операция «Весеннее упражнение»",
                subtitle = "Захват Норвегии Германией",
                period = "9 апреля - 10 июня 1940",
                description = "Немецкое вторжение в Норвегию и Данию",
                iconRes = android.R.drawable.ic_dialog_map  // Используем системную иконку
            )
            // Можно добавить другие операции позже
        )

        val recyclerView: RecyclerView = findViewById(R.id.operationsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = OperationAdapter(operations) { operation ->
            // Переход к карте при выборе операции
            val intent = Intent(this, MapActivity::class.java)
            intent.putExtra("operation_id", operation.id)
            intent.putExtra("operation_title", operation.title)
            startActivity(intent)
        }
    }
        private fun updateFront(day: Int) {
            val points = frontModel.frontForDay(day)

            frontPolygon?.let {
                map.overlays.remove(it)
            }

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

