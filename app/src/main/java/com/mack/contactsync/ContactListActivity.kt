package com.mack.contactsync

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.content.Intent
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mack.contactsync.databinding.ActivityContactListBinding

class ContactListActivity : AppCompatActivity() {
    private lateinit var binding : ActivityContactListBinding
    private lateinit var listView: ListView
    private lateinit var contactsAdapter: ArrayAdapter<String>
    private val contactsList = ArrayList<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContactListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        listView = binding.contactListView
        contactsAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, contactsList)
        listView.adapter = contactsAdapter

        retrieveContacts()

        binding.logoutBtn.setOnClickListener {
            val auth = FirebaseAuth.getInstance()
            auth.signOut()
            startActivity(Intent(this,LoginActivity::class.java))
        }
    }


    private fun retrieveContacts() {
        val db = FirebaseFirestore.getInstance()
        val user = FirebaseAuth.getInstance().currentUser

        if (user != null) {
            val userCollection = db.collection("users")

            userCollection.get()
                .addOnSuccessListener { querySnapshot ->
                    // Clear existing data
                    contactsList.clear()

                    for (document in querySnapshot) {
                        val name = document.getString("name")
                        val phoneNumber = document.getString("Number")

                        if (name != null && phoneNumber != null) {
                            val contactInfo = "$name\n$phoneNumber"
                            contactsList.add(contactInfo)
                        }
                    }

                    // Notify the adapter that the data has changed
                    contactsAdapter.notifyDataSetChanged()
                }
                .addOnFailureListener { exception ->
                    // Handle any errors
                    Toast.makeText(this, "Error retrieving contacts: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

}