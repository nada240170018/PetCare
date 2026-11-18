package com.example.petcare

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.net.Uri
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import android.view.inputmethod.EditorInfo
import android.widget.EditText
class MainActivity : AppCompatActivity() {

    private lateinit var dbHelper: PetDatabaseHelper
    private lateinit var rvPets: RecyclerView
    private lateinit var petAdapter: PetAdapter
    private lateinit var bottomNavigation:
            com.google.android.material.bottomnavigation.BottomNavigationView

    private var isOpeningProfile = false
    private var currentQuery = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbHelper = PetDatabaseHelper(this)
        val btnLogout = findViewById<Button>(R.id.btnLogout)

        btnLogout.setOnClickListener {
            android.app.AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Apakah kamu yakin ingin keluar dari akun PetCare?")
                .setPositiveButton("Ya, Keluar") { _, _ ->
                    FirebaseAuth.getInstance().signOut()

                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TASK

                    startActivity(intent)
                    finish()
                }
                .setNegativeButton("Batal", null)
                .show()
        }

        rvPets = findViewById(R.id.rvPets)
        rvPets.layoutManager = LinearLayoutManager(this)

        petAdapter = PetAdapter(emptyList()) { pet ->
            showPetOptions(pet)
        }

        rvPets.adapter = petAdapter

        val btnAddPet = findViewById<Button>(R.id.btnAddPet)
        val searchPet = findViewById<androidx.appcompat.widget.SearchView>(
            R.id.searchPet
        )
        val searchEditText = searchPet.findViewById<EditText>(
            androidx.appcompat.R.id.search_src_text
        )

        searchEditText.imeOptions =
            searchEditText.imeOptions or EditorInfo.IME_FLAG_NO_EXTRACT_UI

        searchPet.setOnQueryTextListener(
            object : androidx.appcompat.widget.SearchView.OnQueryTextListener {

                override fun onQueryTextSubmit(query: String?): Boolean {
                    currentQuery = query.orEmpty()
                    displayPets(currentQuery)
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    currentQuery = newText.orEmpty()
                    displayPets(currentQuery)
                    return true
                }
            }
        )

        btnAddPet.setOnClickListener {
            val intent = Intent(this, AddPetActivity::class.java)
            startActivity(intent)
        }
        bottomNavigation = findViewById(R.id.bottomNavigation)

        bottomNavigation.selectedItemId = R.id.nav_home

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {

                R.id.nav_home -> {
                    displayPets("")
                    true
                }

                R.id.nav_pets -> {
                    displayPets(currentQuery)
                    rvPets.smoothScrollToPosition(0)
                    true
                }

                R.id.nav_profile -> {
                    startActivity(
                        Intent(this, ProfileActivity::class.java)
                    )
                    true
                }

                else -> false
            }
        }
    }


    override fun onResume() {
        super.onResume()
        displayPets()
    }

    private fun displayPets(query: String = currentQuery) {
        val allPets = dbHelper.getAllPets()

        val pets = allPets.filter {
            it.name.contains(query, ignoreCase = true) ||
                    it.type.contains(query, ignoreCase = true) ||
                    it.breed.contains(query, ignoreCase = true)
        }

        petAdapter.updateData(pets)

        val tvEmptyPets = findViewById<TextView>(R.id.tvEmptyPets)

        if (pets.isEmpty()) {
            tvEmptyPets.visibility = View.VISIBLE
        } else {
            tvEmptyPets.visibility = View.GONE
        }
    }
    private fun showPetOptions(pet: Pet) {
        val options = arrayOf(
            "Lihat Detail",
            "Edit Hewan",
            "Hapus Hewan"
        )

        android.app.AlertDialog.Builder(this)
            .setTitle("Kelola ${pet.name} 🐾")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        val intent = Intent(
                            this,
                            PetDetailActivity::class.java
                        )
                        intent.putExtra("PET_ID", pet.id)
                        startActivity(intent)
                    }

                    1 -> editPet(pet)

                    2 -> confirmDeletePet(pet)
                }
            }
            .show()
    }

    private fun editPet(pet: Pet) {
        val view = layoutInflater.inflate(
            R.layout.activity_add_pet,
            null
        )

        // Ubah judul dan deskripsi khusus mode Edit
        view.findViewById<TextView>(R.id.tvFormTitle).text =
            getString(R.string.edit_pet)

        view.findViewById<TextView>(R.id.tvFormSubtitle).text =
            getString(R.string.edit_pet_subtitle)

        // Sembunyikan tombol Simpan dari formulir
        view.findViewById<Button>(R.id.btnSavePet).visibility =
            View.GONE

        val name = view.findViewById<android.widget.EditText>(R.id.etPetName)
        val type = view.findViewById<android.widget.EditText>(R.id.etPetType)
        val breed = view.findViewById<android.widget.EditText>(R.id.etPetBreed)
        val age = view.findViewById<android.widget.EditText>(R.id.etPetAge)
        val weight = view.findViewById<android.widget.EditText>(R.id.etPetWeight)

        name.setText(pet.name)
        type.setText(pet.type)
        breed.setText(pet.breed)
        age.setText(pet.age.toString())
        weight.setText(pet.weight.toString())

        android.app.AlertDialog.Builder(this)
            .setTitle(getString(R.string.edit_dialog_title))
            .setView(view)
            .setPositiveButton(getString(R.string.save), null)
            .setNegativeButton(getString(R.string.cancel), null)
            .create()
            .apply {
                setOnShowListener {
                    getButton(android.app.AlertDialog.BUTTON_POSITIVE)
                        .setOnClickListener {
                            val newName = name.text.toString().trim()
                            val newType = type.text.toString().trim()
                            val newBreed = breed.text.toString().trim()
                            val newAge = age.text.toString().toDoubleOrNull()
                            val newWeight = weight.text.toString().toDoubleOrNull()

                            if (newName.isEmpty() || newType.isEmpty() ||
                                newBreed.isEmpty() || newAge == null ||
                                newWeight == null || newAge < 0 ||
                                newWeight <= 0
                            ) {
                                android.widget.Toast.makeText(
                                    this@MainActivity,
                                    getString(R.string.check_data),
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                                return@setOnClickListener
                            }

                            dbHelper.updatePet(
                                pet.id,
                                newName,
                                newType,
                                newBreed,
                                newAge,
                                newWeight
                            )

                            displayPets()
                            dismiss()
                        }
                }
                show()
            }
    }

    private fun confirmDeletePet(pet: Pet) {
        android.app.AlertDialog.Builder(this)
            .setTitle(getString(R.string.delete_pet_title))
            .setMessage(getString(R.string.delete_pet_message, pet.name))
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                dbHelper.deletePet(pet.id)
                displayPets()

                android.widget.Toast.makeText(
                    this,
                    getString(R.string.delete_success, pet.name),
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }
}
