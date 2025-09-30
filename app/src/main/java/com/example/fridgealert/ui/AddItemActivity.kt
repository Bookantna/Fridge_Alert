package com.example.fridgealert.ui

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fridgealert.databinding.ActivityAddItemBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class AddItemActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddItemBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = ActivityAddItemBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // category
        val categories = listOf("Fruits", "Vegetables", "Dairy", "Meat", "Snacks", "Drinks", "Other")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.additemCategorySpinner.adapter = adapter

        // เลือกวันหมดอายุ
        binding.additemExpdate.setOnClickListener {
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePicker = DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    val selectedDate = Calendar.getInstance()
                    selectedDate.set(selectedYear, selectedMonth, selectedDay)
                    val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    binding.additemExpdate.setText(format.format(selectedDate.time))
                },
                year, month, day
            )
            datePicker.show()
        }


        binding.additemSave.setOnClickListener {
            val name = binding.additemName.text.toString().trim()
            val category = binding.additemCategorySpinner.selectedItem.toString()
            val expired = binding.additemExpdate.text.toString().trim()
            val quantity = binding.additemQuantity.text.toString().toIntOrNull() ?: 0
            val uid = auth.currentUser?.uid

            if (name.isNotEmpty() && category.isNotEmpty() && expired.isNotEmpty() && uid != null) {
                val newItem = hashMapOf(
                    "name" to name,
                    "category" to category,
                    "expDate" to expired,
                    "quantity" to quantity,
                    "userId" to uid
                )
                saveItemToFirestore(newItem)
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveItemToFirestore(newItem: Map<String, Any>) {
        firestore.collection("items")
            .add(newItem)
            .addOnSuccessListener {
                Toast.makeText(this, "Item added successfully", Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to add item", Toast.LENGTH_SHORT).show()
            }
    }
}
