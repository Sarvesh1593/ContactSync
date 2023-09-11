package com.mack.contactsync

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.mack.contactsync.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding
    private lateinit var number : String
    private lateinit var currentUser : FirebaseUser
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        number = intent.getStringExtra("phone_number").toString()

        binding.submitBtn.setOnClickListener {
            val name = binding.edtName.text.toString()
            val email = binding.edtEmail.text.toString()

            if(name.isNotEmpty() && email.isNotEmpty()){
                saveUserData(name,email,number)
                startActivity(Intent(this,ContactListActivity::class.java))
            }else{
                Toast.makeText(this,"Please Enter name and email",Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun saveUserData(name: String, email: String, number: String) {
        currentUser = FirebaseAuth.getInstance().currentUser!!
        val uid = currentUser.uid

        val db=FirebaseFirestore.getInstance()
        val user = hashMapOf(
            "uid" to uid,
            "name" to name,
            "email" to email,
            "Number" to number
        )
        val userCollection = db.collection("users")

        userCollection.document(uid)
            .set(user)
            .addOnSuccessListener {
                Toast.makeText(this,"User Data Save Successfully",Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Log.d("exception",it.toString())
            }
    }
}