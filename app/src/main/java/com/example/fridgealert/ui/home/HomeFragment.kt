package com.example.fridgealert.ui.home

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fridgealert.data.item
import com.example.fridgealert.databinding.FragmentHomeBinding
import com.example.fridgealert.ui.AddItemActivity
import com.example.fridgealert.ui.ItemDetailActivity
import com.example.fridgealert.ui.NotificationHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private val allItems = mutableListOf<item>()
    private val filteredItems = mutableListOf<item>()

    private val adapter: HomeAdapter by lazy {
        HomeAdapter(filteredItems) { selectedItem ->
            val intent = Intent(requireContext(), ItemDetailActivity::class.java).apply {
                putExtra("item_name", selectedItem.name)
                putExtra("item_category", selectedItem.category)
                putExtra("item_expiry", selectedItem.expDate)
                putExtra("item_quantity", selectedItem.quantity)
            }
            detailLauncher.launch(intent)
        }
    }

    private lateinit var addItemLauncher: ActivityResultLauncher<Intent>
    private lateinit var detailLauncher: ActivityResultLauncher<Intent>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        // 🔹 เตรียม filter categories
        val categories = listOf("All", "Fruits", "Vegetables", "Dairy", "Meat", "Snacks", "Drinks", "Other")
        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.categoryFilter.adapter = spinnerAdapter

        binding.categoryFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                filterData(binding.searchBar.text.toString())
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        binding.searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterData(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        fetchItemsFromFirestore()

        addItemLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) fetchItemsFromFirestore()
        }

        detailLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) fetchItemsFromFirestore()
        }

        binding.additemButton.setOnClickListener {
            val intent = Intent(requireContext(), AddItemActivity::class.java)
            addItemLauncher.launch(intent)
        }
    }

    private fun fetchItemsFromFirestore() {
        val uid = auth.currentUser?.uid ?: return

        firestore.collection("items")
            .whereEqualTo("userId", uid)
            .get()
            .addOnSuccessListener { result ->
                allItems.clear()
                for (document in result) {
                    val it = document.toObject(item::class.java)
                    allItems.add(it)
                }
                filterData(binding.searchBar.text.toString())

                // ✅ หลังจากโหลดเสร็จ → เช็คแจ้งเตือน
                checkAndNotify(allItems)
            }
            .addOnFailureListener { e ->
                Log.e("HomeFragment", "Error fetching data", e)
                Toast.makeText(requireContext(), "Failed to fetch data: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun filterData(query: String) {
        val selectedCategory = binding.categoryFilter.selectedItem?.toString() ?: "All"

        filteredItems.clear()
        filteredItems.addAll(
            allItems.filter { item ->
                val name = item.name ?: ""
                val matchCategory = (selectedCategory == "All" || item.category == selectedCategory)
                val matchName = name.contains(query, ignoreCase = true)
                matchCategory && matchName
            }
        )
        adapter.notifyDataSetChanged()
    }

    // ✅ เช็ควันหมดอายุ และสร้าง Notification
    private fun checkAndNotify(items: List<item>) {
        val today = Calendar.getInstance().time
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        for (it in items) {
            val expDate = try { sdf.parse(it.expDate ?: "") } catch (e: Exception) { null }
            if (expDate != null) {
                val diff = (expDate.time - today.time) / (1000 * 60 * 60 * 24)
                when {
                    diff == 1L -> {
                        NotificationHelper.showNotification(
                            requireContext(),
                            it.hashCode(),
                            "ใกล้หมดอายุ: ${it.name}",
                            "เหลืออีก 1 วัน รีบใช้ก่อนหมดอายุนะ!"
                        )
                    }
                    diff <= 0L -> {
                        NotificationHelper.showNotification(
                            requireContext(),
                            it.hashCode(),
                            "หมดอายุแล้ว: ${it.name}",
                            "ควรนำไปทิ้งหรือจัดการให้เรียบร้อย"
                        )
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
