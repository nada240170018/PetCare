package com.example.petcare

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.widget.Spinner
class AddPetActivity : AppCompatActivity() {

    private lateinit var dbHelper: PetDatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_pet)

        dbHelper = PetDatabaseHelper(this)

        val etPetName = findViewById<EditText>(R.id.etPetName)
        val etPetType = findViewById<EditText>(R.id.etPetType)
        val etPetBreed = findViewById<EditText>(R.id.etPetBreed)
        val etPetAge = findViewById<EditText>(R.id.etPetAge)
        val spAgeUnit = findViewById<Spinner>(R.id.spAgeUnit)
        val etPetWeight = findViewById<EditText>(R.id.etPetWeight)

        val btnSavePet = findViewById<Button>(R.id.btnSavePet)

        btnSavePet.setOnClickListener {
            val name = etPetName.text.toString().trim()
            val type = etPetType.text.toString().trim()
            val breed = etPetBreed.text.toString().trim()
            val ageText = etPetAge.text.toString().trim()
            val weightText = etPetWeight.text.toString().trim()

            if (name.isEmpty() || type.isEmpty() ||
                breed.isEmpty() || ageText.isEmpty() ||
                weightText.isEmpty()
            ) {
                Toast.makeText(
                    this,
                    getString(R.string.all_fields_required),
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val enteredAge = ageText.toDoubleOrNull()
            val weight = weightText.toDoubleOrNull()

            if (enteredAge == null || weight == null ||
                enteredAge < 0 || weight <= 0
            ) {
                Toast.makeText(
                    this,
                    getString(R.string.invalid_pet_data),
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

// Konversi umur ke satuan tahun sebelum disimpan
            val age = if (
                spAgeUnit.selectedItem.toString() == getString(R.string.age_unit_months)
            ) {
                enteredAge / 12.0
            } else {
                enteredAge
            }

            val result = dbHelper.insertPet(
                name, type, breed, age, weight
            )

            if (result != -1L) {
                Toast.makeText(
                    this,
                    getString(R.string.save_success),
                    Toast.LENGTH_SHORT
                ).show()

                finish()
            } else {
                Toast.makeText(
                    this,
                    getString(R.string.save_failed),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onDestroy() {
        dbHelper.close()
        super.onDestroy()
    }
}
