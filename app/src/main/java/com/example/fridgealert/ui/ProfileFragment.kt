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
        displayUserInfo()

        binding.btnLogout.setOnClickListener {
            // Implement Google Sign-out here for a clean exit (optional but recommended)
            auth.signOut()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        // Ensure NotificationHelper is defined or replace with standard notification code
        binding.btnTestNotification.setOnClickListener {
            // NotificationHelper.showNotification(
            //     requireContext(),
            //     id = Random().nextInt(),
            //     title = "ทดสอบการแจ้งเตือน",
            //     message = "นี่คือการแจ้งเตือนทดสอบจาก ProfileFragment"
            // )
            Toast.makeText(requireContext(), "ส่งการแจ้งเตือนแล้ว", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Handles displaying user information by prioritizing Firebase Auth (for Google SSO)
     * and falling back to Firestore (for custom registration).
     */
    private fun displayUserInfo() {
        val user = auth.currentUser
        val email = user?.email
        val authDisplayName = user?.displayName

        if (email != null) {
            // 1. Always display email from Firebase Auth, which is reliable
            binding.profileEmail.text = "อีเมล: $email"

            // 2. Check Firebase Auth Display Name first (for Google/Facebook SSO)
            if (!authDisplayName.isNullOrBlank()) {
                binding.profileName.text = "ชื่อ: $authDisplayName"
                // No need to query Firestore if the name is available from Auth
                return
            }

            // 3. FALLBACK: Query Firestore for custom users (e.g., Email/Password with custom 'username')
            db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        val doc = documents.documents[0]
                        val username = doc.getString("username") ?: "N/A"
                        // Only update name, email is already set above
                        binding.profileName.text = "ชื่อ: $username"
                    } else {
                        // User exists in Auth but not in your custom Firestore 'users' collection
                        binding.profileName.text = "ชื่อ: ไม่พบข้อมูล (SSO User)"
                    }
                }
                .addOnFailureListener { e ->
                    // Set a friendly error message
                    binding.profileName.text = "ชื่อ: เกิดข้อผิดพลาดในการโหลด"
                    Toast.makeText(requireContext(), "Error loading profile: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            // No user logged in
            binding.profileName.text = "ไม่พบผู้ใช้"
            binding.profileEmail.text = ""
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}