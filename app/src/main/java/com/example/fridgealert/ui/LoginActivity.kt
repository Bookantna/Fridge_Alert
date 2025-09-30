package com.example.fridgealert.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fridgealert.MainActivity
import com.example.fridgealert.R // Ensure this is imported for resources like strings
import com.example.fridgealert.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    // Add Google Sign-In variables
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 9001 // Request code for Google Sign-In

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // 1. Configure Google Sign-In to request the user's ID and email,
        // and crucially, the ID token for Firebase Authentication.
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            // Use the Web client ID here.
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        // Build a GoogleSignInClient with the options specified by gso.
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // ปุ่ม Login (Email/Password) - Existing Logic
        binding.loginLogin.setOnClickListener {
            val email = binding.loginUsername.text.toString().trim()
            val password = binding.loginPassword.text.toString().trim()

            Log.d("LoginActivity", "Attempting to login with email: [$email]")

            if (email.isNotEmpty() && password.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }

        // ปุ่ม Register - Existing Logic
        binding.loginRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // 2. Add a new button listener for Google Sign-In
        // Assume you have a button like 'login_google' in your layout
        binding.loginGoogle.setOnClickListener { // Replace 'loginGoogle' with your actual button ID
            signInWithGoogle()
        }
    }

    // Function to start the Google Sign-In flow
    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    // 3. Handle the result from the Google Sign-In Intent
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // ... (rest of the successful login code)
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                val statusCode = e.statusCode // Get the specific error code
                Log.w("LoginActivity", "Google sign in failed. Status Code: $statusCode", e)
                Toast.makeText(this, "Google Sign In Failed. Code: $statusCode", Toast.LENGTH_LONG).show()
            }
        }
    }

    // 4. Authenticate with Firebase using the Google ID Token
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        Log.d("LoginActivity", "Attempting Firebase auth with Google ID Token.")

        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, navigate to Main Activity
                    Log.d("LoginActivity", "Firebase Google Sign-In successful! User: ${auth.currentUser?.email}")
                    Toast.makeText(this, "Google Login successful", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    // If sign in fails, display a message to the user.
                    val errorMessage = task.exception?.message ?: "Unknown Firebase error."
                    Log.e("LoginActivity", "Firebase Google Sign-In failed: $errorMessage", task.exception)

                    // Check for the common account link error
                    if (errorMessage.contains("already been linked to a different credential")) {
                        Toast.makeText(this, "Email already registered. Try Email/Password login.", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this, "Firebase Login failed: $errorMessage", Toast.LENGTH_LONG).show()
                    }
                }
            }
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            Log.d("LoginActivity", "User already logged in: ${currentUser.email}")
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}