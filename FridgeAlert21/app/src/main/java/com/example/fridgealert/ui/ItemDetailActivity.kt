package com.example.fridgealert.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.fridgealert.databinding.ActivityItemDetailBinding
import com.google.firebase.firestore.FirebaseFirestore

class ItemDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityItemDetailBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var disposeLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityItemDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // ถ้าไม่อยากมี ActionBar: supportActionBar?.hide()

        firestore = FirebaseFirestore.getInstance()

        // รับค่า
        val name = intent.getStringExtra("item_name") ?: ""
        val category = intent.getStringExtra("item_category") ?: ""
        val expiry = intent.getStringExtra("item_expiry") ?: ""
        val quantity = intent.getIntExtra("item_quantity", 0)

        // bind UI
        binding.detailName.text = name
        binding.detailCategory.text = category
        binding.detailExpiry.text = expiry
        binding.detailQuantity.text = quantity.toString()
        binding.btniBack.setOnClickListener {
            finish()
        }


        disposeLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                setResult(RESULT_OK)
                finish()
            }
        }


        binding.btnDispose.setOnClickListener {
            val intent = Intent(this, DisposeInfoActivity::class.java).apply {
                putExtra("item_name", name)
                putExtra("item_category", category)
            }
            disposeLauncher.launch(intent)
        }


        binding.btnCook.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("ยืนยันการนำไปประกอบอาหาร")
                .setMessage("คุณแน่ใจหรือไม่ว่าจะใช้ $name ไปประกอบอาหาร?")
                .setPositiveButton("ใช่") { _, _ ->
                    deleteItemByName(name)
                }
                .setNegativeButton("ยกเลิก", null)
                .show()
        }
    }

    private fun deleteItemByName(name: String) {
        if (name.isEmpty()) {
            Toast.makeText(this, "ไม่พบชื่อไอเทม", Toast.LENGTH_SHORT).show(); return
        }
        FirebaseFirestore.getInstance().collection("items")
            .whereEqualTo("name", name)
            .get()
            .addOnSuccessListener { docs ->
                for (d in docs) FirebaseFirestore.getInstance().collection("items").document(d.id).delete()
                Toast.makeText(this, "ลบ $name เรียบร้อย", Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK) // ✅ ให้ HomeFragment รีเฟรช
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "ลบไม่สำเร็จ", Toast.LENGTH_SHORT).show()
            }
    }
}
