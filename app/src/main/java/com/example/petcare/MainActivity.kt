package com.example.petcare

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var dbHelper: PetDatabaseHelper
    private lateinit var rvPets: RecyclerView
    private lateinit var petAdapter: PetAdapter
    private lateinit var progressBarPets: ProgressBar
    private lateinit var bottomNavigation: BottomNavigationView

    private lateinit var homeDashboard: View
    private lateinit var tvTotalPets: TextView
    private lateinit var tvSection: TextView
    private lateinit var tvSubtitle: TextView
    private lateinit var searchPet: SearchView
    private lateinit var btnAddPet: Button
    private lateinit var tvEmptyPets: TextView

    private var currentQuery = ""
    private var isPetsScreen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbHelper = PetDatabaseHelper(this)

        // Inisialisasi komponen
        val btnGoPets = findViewById<Button>(R.id.btnGoPets)

        rvPets = findViewById(R.id.rvPets)
        progressBarPets = findViewById(R.id.progressBarPets)
        bottomNavigation = findViewById(R.id.bottomNavigation)

        homeDashboard = findViewById(R.id.homeDashboard)
        tvTotalPets = findViewById(R.id.tvTotalPets)
        tvSection = findViewById(R.id.tvSection)
        tvSubtitle = findViewById(R.id.tvSubtitle)
        searchPet = findViewById(R.id.searchPet)
        btnAddPet = findViewById(R.id.btnAddPet)
        tvEmptyPets = findViewById(R.id.tvEmptyPets)

        // RecyclerView
        rvPets.layoutManager = LinearLayoutManager(this)

        petAdapter = PetAdapter(emptyList()) { pet ->
            showPetOptions(pet)
        }

        rvPets.adapter = petAdapter

        // Logout

        // Pengaturan SearchView
        val searchEditText = searchPet.findViewById<EditText>(
            androidx.appcompat.R.id.search_src_text
        )

        searchEditText.imeOptions =
            searchEditText.imeOptions or
                    android.view.inputmethod.EditorInfo.IME_FLAG_NO_EXTRACT_UI

        searchPet.setOnQueryTextListener(
            object : SearchView.OnQueryTextListener {

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

        // Tambah hewan
        btnAddPet.setOnClickListener {
            startActivity(
                Intent(this, AddPetActivity::class.java)
            )
        }

        // Tombol dari dashboard menuju halaman Hewan
        btnGoPets.setOnClickListener {
            bottomNavigation.selectedItemId = R.id.nav_pets
        }

        // Bottom Navigation
        bottomNavigation.setOnItemSelectedListener { item ->

            when (item.itemId) {

                R.id.nav_home -> {
                    showHomeScreen()
                    true
                }

                R.id.nav_pets -> {
                    showPetsScreen()
                    true
                }
                R.id.nav_schedule -> {
                    startActivity(
                        Intent(this, CareScheduleActivity::class.java)
                    )
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

        // Tampilan awal sesuai menu yang dipilih
        val openTab = intent.getIntExtra("OPEN_TAB", R.id.nav_home)

        if (openTab == R.id.nav_pets) {
            bottomNavigation.selectedItemId = R.id.nav_pets
            showPetsScreen()
        } else {
            bottomNavigation.selectedItemId = R.id.nav_home
            showHomeScreen()
        }
    }

    // Halaman Beranda
    private fun showHomeScreen() {
        isPetsScreen = false

        homeDashboard.visibility = View.VISIBLE

        tvSection.visibility = View.GONE
        tvSubtitle.visibility = View.GONE
        searchPet.visibility = View.GONE
        progressBarPets.visibility = View.GONE
        rvPets.visibility = View.GONE
        tvEmptyPets.visibility = View.GONE
        btnAddPet.visibility = View.GONE

        currentQuery = ""
        searchPet.setQuery("", false)

        updatePetCount()
    }

    // Halaman pengelolaan Hewan
    private fun showPetsScreen() {
        isPetsScreen = true

        homeDashboard.visibility = View.GONE

        tvSection.visibility = View.VISIBLE
        tvSubtitle.visibility = View.VISIBLE
        searchPet.visibility = View.VISIBLE
        btnAddPet.visibility = View.VISIBLE

        displayPets(currentQuery)
        rvPets.smoothScrollToPosition(0)
    }

    // Menghitung jumlah hewan
    private fun updatePetCount() {
        try {
            val totalPets = dbHelper.getAllPets().size
            tvTotalPets.text = "$totalPets Hewan"
        } catch (e: Exception) {
            android.util.Log.e(
                "PetCare",
                "Gagal menghitung jumlah hewan",
                e
            )
            tvTotalPets.text = "Data tidak tersedia"
        }
    }

    override fun onResume() {
        super.onResume()

        updatePetCount()

        if (isPetsScreen) {
            displayPets(currentQuery)
        }
    }

    // Menampilkan daftar hewan
    private fun displayPets(query: String = currentQuery) {

        if (!isPetsScreen) return

        try {
            progressBarPets.visibility = View.VISIBLE
            rvPets.visibility = View.GONE
            tvEmptyPets.visibility = View.GONE

            val allPets = dbHelper.getAllPets()

            val pets = allPets.filter { pet ->
                pet.name.contains(query, ignoreCase = true) ||
                        pet.type.contains(query, ignoreCase = true) ||
                        pet.breed.contains(query, ignoreCase = true)
            }

            petAdapter.updateData(pets)

            if (pets.isEmpty()) {
                tvEmptyPets.text = getString(R.string.empty_pets)
                tvEmptyPets.visibility = View.VISIBLE
                rvPets.visibility = View.GONE
            } else {
                tvEmptyPets.visibility = View.GONE
                rvPets.visibility = View.VISIBLE
            }

        } catch (e: Exception) {

            android.util.Log.e(
                "PetCare",
                "Gagal memuat data hewan",
                e
            )

            tvEmptyPets.text = getString(R.string.load_pets_error)
            tvEmptyPets.visibility = View.VISIBLE
            rvPets.visibility = View.GONE

        } finally {
            progressBarPets.visibility = View.GONE
        }
    }

    // Menu pilihan hewan
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

    // Edit data hewan
    private fun editPet(pet: Pet) {

        val view = layoutInflater.inflate(
            R.layout.activity_add_pet,
            null
        )

        view.findViewById<TextView>(R.id.tvFormTitle).text =
            getString(R.string.edit_pet)

        view.findViewById<TextView>(R.id.tvFormSubtitle).text =
            getString(R.string.edit_pet_subtitle)

        view.findViewById<Button>(R.id.btnSavePet).visibility =
            View.GONE

        val name = view.findViewById<EditText>(R.id.etPetName)
        val type = view.findViewById<EditText>(R.id.etPetType)
        val breed = view.findViewById<EditText>(R.id.etPetBreed)
        val age = view.findViewById<EditText>(R.id.etPetAge)
        val weight = view.findViewById<EditText>(R.id.etPetWeight)

        name.setText(pet.name)
        type.setText(pet.type)
        breed.setText(pet.breed)
        age.setText(pet.age.toString())
        weight.setText(pet.weight.toString())

        val dialog = android.app.AlertDialog.Builder(this)
            .setTitle(getString(R.string.edit_dialog_title))
            .setView(view)
            .setPositiveButton(getString(R.string.save), null)
            .setNegativeButton(getString(R.string.cancel), null)
            .create()

        dialog.setOnShowListener {

            dialog.getButton(
                android.app.AlertDialog.BUTTON_POSITIVE
            ).setOnClickListener {

                val newName = name.text.toString().trim()
                val newType = type.text.toString().trim()
                val newBreed = breed.text.toString().trim()
                val newAge = age.text.toString().toDoubleOrNull()
                val newWeight = weight.text.toString().toDoubleOrNull()

                if (
                    newName.isEmpty() ||
                    newType.isEmpty() ||
                    newBreed.isEmpty() ||
                    newAge == null ||
                    newWeight == null ||
                    newAge < 0 ||
                    newWeight <= 0
                ) {
                    Toast.makeText(
                        this,
                        getString(R.string.check_data),
                        Toast.LENGTH_SHORT
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

                dialog.dismiss()
                displayPets()
                updatePetCount()

                Toast.makeText(
                    this,
                    "Data hewan berhasil diperbarui!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        dialog.show()
    }

    // Konfirmasi penghapusan
    private fun confirmDeletePet(pet: Pet) {

        android.app.AlertDialog.Builder(this)
            .setTitle(getString(R.string.delete_pet_title))
            .setMessage(
                getString(R.string.delete_pet_message, pet.name)
            )
            .setPositiveButton(getString(R.string.delete)) { _, _ ->

                dbHelper.deletePet(pet.id)

                displayPets()
                updatePetCount()

                Toast.makeText(
                    this,
                    getString(R.string.delete_success, pet.name),
                    Toast.LENGTH_SHORT
                ).show()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        dbHelper.close()
    }
}
