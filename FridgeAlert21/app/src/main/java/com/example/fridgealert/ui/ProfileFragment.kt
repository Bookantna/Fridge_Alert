package com.example.fridgealert.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.fridgealert.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Random

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        val user = auth.currentUser
        val email = user?.email

        if (email != null) {

            db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        val doc = documents.documents[0]
                        val username = doc.getString("username") ?: "Unknown"
                        val emailText = doc.getString("email") ?: "N/A"

                        binding.profileName.text = "ชื่อ: $username"
                        binding.profileEmail.text = "อีเมล: $emailText"
                    } else {
                        binding.profileName.text = "ไม่พบข้อมูลผู้ใช้"
                        binding.profileEmail.text = ""
                    }
                }
                .addOnFailureListener { e ->
                    binding.profileName.text = "เกิดข้อผิดพลาด"
                    binding.profileEmail.text = e.message
                }
        } else {
            binding.profileName.text = "ไม่พบข้อมูลผู้ใช้"
            binding.profileEmail.text = ""
        }

        binding.btnLogout.setOnClickListener {
            auth.signOut()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)

        }
        binding.btnTestNotification.setOnClickListener {
            NotificationHelper.showNotification(
                requireContext(),
                id = Random().nextInt(),
                title = "ทดสอบการแจ้งเตือน",
                message = "นี่คือการแจ้งเตือนทดสอบจาก ProfileFragment"
            )
            Toast.makeText(requireContext(), "ส่งการแจ้งเตือนแล้ว", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
