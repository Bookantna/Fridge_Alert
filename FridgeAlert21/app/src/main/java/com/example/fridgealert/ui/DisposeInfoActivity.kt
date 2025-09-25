package com.example.fridgealert.ui

import android.content.DialogInterface
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.fridgealert.databinding.ActivityDisposeInfoBinding
import com.google.firebase.firestore.FirebaseFirestore

class DisposeInfoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDisposeInfoBinding
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDisposeInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()

        val name = intent.getStringExtra("item_name") ?: ""
        val category = intent.getStringExtra("item_category") ?: "Other"

        binding.disposeTitle.text = "สิ่งที่ควรทำเมื่อ $category หมดอายุ"

        val info = when (category) {
            "Fruits" -> """ การนำไปใช้ประโยชน์:
- หมักทำปุ๋ยอินทรีย์ ปุ๋ยน้ำหมักชีวภาพ เพิ่มความสมบูรณ์ให้ดิน
- หมักทำเอนไซม์ล้างจาน/น้ำหมักเอนกประสงค์ ใช้ทำความสะอาดได้
- ใช้เลี้ยงสัตว์บางชนิด เช่น ไก่ เป็ด สุกร
⚠️ ข้อเสียหากทิ้ง:
- หากสะสมจำนวนมาก จะเกิดกลิ่นเหม็นแรง ดึงดูดแมลงวัน
- เพิ่มปริมาณขยะเปียกที่ย่อยสลายช้า ก่อให้เกิดก๊าซเรือนกระจก
- เชื้อโรคและเชื้อราสะสม ทำให้สุขอนามัยรอบข้างแย่ลง"""

            "Vegetables" -> """ การนำไปใช้ประโยชน์:
- หมักทำปุ๋ยหมักหรือ EM สำหรับปรับปรุงดิน
- ใช้เป็นอาหารสัตว์ เช่น กระต่าย เป็ด ไก่
- นำไปผลิตก๊าซชีวภาพได้
⚠️ ข้อเสียหากทิ้ง:
- เกิดเชื้อราและกลิ่นเน่าเหม็น
- สะสมก๊าซมีเทนและก๊าซเรือนกระจก
- เป็นแหล่งเพาะแมลงและแบคทีเรียที่เป็นอันตราย"""

            "Dairy" -> """ การนำไปใช้ประโยชน์:
- หมักทำปุ๋ยน้ำหมักจุลินทรีย์ ใช้เร่งการย่อยสลายอินทรีย์วัตถุ
- ใช้เป็นส่วนผสมในการทำจุลินทรีย์ดินหรือ EM
⚠️ ข้อเสียหากทิ้ง:
- นมและผลิตภัณฑ์นมบูดง่าย → เกิดแบคทีเรียอันตราย (E.coli, Salmonella, Listeria)
- กลิ่นแรงมาก ทำให้พื้นที่สกปรก
- เสี่ยงดึงดูดแมลงวันและสัตว์พาหะ เช่น หนู"""

            "Meat" -> """ การนำไปใช้ประโยชน์:
- สามารถนำไปผลิตก๊าซชีวภาพในระบบปิด
- ใช้ทำปุ๋ยหมักได้เฉพาะระบบที่ควบคุมอุณหภูมิ (composting แบบพิเศษ)
⚠️ ข้อเสียหากทิ้ง:
- เน่าเสียเร็ว เกิดกลิ่นรุนแรงมาก
- ดึงดูดแมลงวัน หนู และสัตว์จรจัด
- เสี่ยงปนเปื้อนเชื้อโรคที่อันตราย เช่น Salmonella, E.coli, Campylobacter
- ไม่ควรหมักในปุ๋ยหมักทั่วไป เพราะเสี่ยงแพร่เชื้อ"""

            "Snacks" -> """ การนำไปใช้ประโยชน์:
- รีไซเคิลบรรจุภัณฑ์ เช่น ซองพลาสติก กล่อง กระดาษ
- ขนมบางชนิด (ขนมปังกรอบ) สามารถบดเป็นอาหารสัตว์ได้
⚠️ ข้อเสียหากทิ้ง:
- ขยะพลาสติกจำนวนมากจากบรรจุภัณฑ์ → ย่อยสลายยาก
- หากเก็บไม่ถูกวิธี อาจเป็นแหล่งสะสมแมลงและสัตว์ฟันแทะ
- ขยะสะสมทำให้เกิดปัญหามลพิษสิ่งแวดล้อม"""

            "Drinks" -> """ การนำไปใช้ประโยชน์:
- รีไซเคิลขวดพลาสติก/แก้ว/อะลูมิเนียม
- นำเครื่องดื่มที่มีน้ำตาลหมักเป็นปุ๋ยน้ำชีวภาพได้
- ใช้ผลิตก๊าซชีวภาพได้ในระบบปิด
⚠️ ข้อเสียหากทิ้ง:
- น้ำเสียจากการเน่า บูด ส่งกลิ่นแรง
- บรรจุภัณฑ์จำนวนมากเพิ่มปัญหาขยะ
- หากไม่แยกรีไซเคิล ทำให้เกิดภาระการกำจัดสูง"""

            else -> """ การนำไปใช้ประโยชน์:
- แยกเป็นขยะอินทรีย์เพื่อทำปุ๋ย
- รีไซเคิลบรรจุภัณฑ์หรือวัสดุที่ยังใช้ได้
⚠️ ข้อเสียหากทิ้ง:
- หากไม่คัดแยก จะทำให้ปริมาณขยะรวมเพิ่มขึ้น
- เกิดมลพิษ กลิ่นเหม็น และปัญหาสิ่งแวดล้อมในระยะยาว"""
        }
        binding.disposeInfo.text = info

        // ปุ่มลบทิ้ง
        binding.btnDeleteItem.setOnClickListener {
            if (name.isEmpty()) {
                Toast.makeText(this, "ไม่พบชื่อไอเทม", Toast.LENGTH_SHORT).show(); return@setOnClickListener
            }
            AlertDialog.Builder(this)
                .setTitle("ยืนยันการลบทิ้ง")
                .setMessage("คุณแน่ใจหรือไม่ว่าจะลบ $name ?")
                .setPositiveButton("ลบ") { _: DialogInterface, _: Int ->
                    deleteByName(name)
                }
                .setNegativeButton("ยกเลิก", null)
                .show()
        }
    }

    private fun deleteByName(name: String) {
        firestore.collection("items")
            .whereEqualTo("name", name)
            .get()
            .addOnSuccessListener { docs ->
                for (doc in docs) firestore.collection("items").document(doc.id).delete()
                Toast.makeText(this, "ลบ $name เรียบร้อย", Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "ลบไม่สำเร็จ", Toast.LENGTH_SHORT).show()
            }
    }
}
