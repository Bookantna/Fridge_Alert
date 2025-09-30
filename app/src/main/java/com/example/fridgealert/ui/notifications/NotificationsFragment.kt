package com.example.fridgealert.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fridgealert.databinding.FragmentNotificationsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    private val notifications = mutableListOf<NotificationItem>()
    private lateinit var adapter: NotificationAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = NotificationAdapter(notifications)
        binding.notificationRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.notificationRecycler.adapter = adapter

        fetchNotifications()
    }

    private fun fetchNotifications() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("notifications")
            .whereEqualTo("userId", uid) // 🔹 เอามาเฉพาะ user นี้
            .get()
            .addOnSuccessListener { result ->
                notifications.clear()
                for (doc in result) {
                    val title = doc.getString("title") ?: ""
                    val message = doc.getString("message") ?: ""
                    val timestamp = doc.getDate("timestamp") ?: java.util.Date()
                    notifications.add(NotificationItem(title, message, timestamp))
                }

                // 🔹 sort ฝั่ง client เอง (ใหม่สุดอยู่บน)
                notifications.sortByDescending { it.timestamp }

                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
