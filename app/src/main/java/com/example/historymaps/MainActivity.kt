package com.example.historymaps

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.historymaps.MapActivity
import com.example.historymaps.adapters.OperationAdapter
import com.example.historymaps.models.MilitaryOperation

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
}