package com.mack.contactsync

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Intent
import android.database.Cursor
import android.provider.ContactsContract
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.mack.contactsync.databinding.ActivityContactListBinding

class ContactListActivity : AppCompatActivity() {
    private val CONTACTS_PERMISSION_REQUEST = 101
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
            startActivity(Intent(this,PhoneActivity::class.java))
        }

    }

    @SuppressLint("Range")
    private fun retrieveContacts() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            val contentResolver: ContentResolver = contentResolver
            val cursor: Cursor? = contentResolver.query(
                ContactsContract.Contacts.CONTENT_URI,
                null,
                null,
                null,
                null
            )

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    val contactNameColumnIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                    val contactName = cursor.getString(contactNameColumnIndex)

                    val contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))
                    val phoneCursor: Cursor? = contentResolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        arrayOf(contactId),
                        null
                    )

                    if (phoneCursor != null && phoneCursor.moveToFirst()) {
                        val phoneNumberColumnIndex = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                        val phoneNumber = phoneCursor.getString(phoneNumberColumnIndex)

                        // Combine the contact name and phone number with a line break
                        val contactInfo = "$contactName\n$phoneNumber"
                        contactsList.add(contactInfo)
                    } else {
                        // If there's no phone number for the contact, just add the name
                        contactsList.add(contactName)
                    }

                    phoneCursor?.close()
                } while (cursor.moveToNext())

                cursor.close()
                contactsAdapter.notifyDataSetChanged()
            }
        } else {
            // Permission is not granted, request it from the user
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CONTACTS), CONTACTS_PERMISSION_REQUEST)
        }
    }
}