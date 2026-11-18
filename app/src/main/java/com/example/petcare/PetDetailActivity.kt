package com.example.petcare

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class PetDetailActivity : AppCompatActivity() {

    private lateinit var dbHelper: PetDatabaseHelper
    private var petId: Int = -1

    private val pilihFoto =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->

            if (uri != null) {
                try {
                    contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (e: SecurityException) {
                    // Menangani jika izin akses permanen tidak tersedia
                }

                dbHelper.updatePetPhoto(petId, uri.toString())

                val foto = findViewById<ImageView>(R.id.ivPetPhoto)
                val emoji = findViewById<TextView>(R.id.tvPetEmoji)

                foto.setImageURI(uri)
                foto.visibility = View.VISIBLE
                emoji.visibility = View.GONE
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pet_detail)

        dbHelper = PetDatabaseHelper(this)

        petId = intent.getIntExtra("PET_ID", -1)

        val tvEmoji = findViewById<TextView>(R.id.tvPetEmoji)
        val tvName = findViewById<TextView>(R.id.tvDetailName)
        val tvType = findViewById<TextView>(R.id.tvDetailType)
        val tvBreed = findViewById<TextView>(R.id.tvDetailBreed)
        val tvAge = findViewById<TextView>(R.id.tvDetailAge)
        val tvWeight = findViewById<TextView>(R.id.tvDetailWeight)
        val foto = findViewById<ImageView>(R.id.ivPetPhoto)

        val pet = dbHelper.getAllPets().find {
            it.id == petId
        }

        if (pet != null) {
            tvEmoji.text = when (pet.type.lowercase()) {
                "kucing" -> "🐱"
                "anjing" -> "🐶"
                "kelinci" -> "🐰"
                "burung" -> "🐦"
                "ikan" -> "🐠"
                "hamster" -> "🐹"
                else -> "🐾"
            }

            tvName.text = pet.name
            tvType.text = pet.type
            tvBreed.text = pet.breed
            tvAge.text = "${pet.age} tahun"
            tvWeight.text = "${pet.weight} kg"

            if (!pet.photoUri.isNullOrEmpty()) {
                try {
                    foto.setImageURI(Uri.parse(pet.photoUri))
                    foto.visibility = View.VISIBLE
                    tvEmoji.visibility = View.GONE
                } catch (e: Exception) {
                    foto.visibility = View.GONE
                    tvEmoji.visibility = View.VISIBLE
                }
            }
        } else {
            finish()
            return
        }

        findViewById<Button>(R.id.btnChoosePhoto).setOnClickListener {
            pilihFoto.launch(arrayOf("image/*"))
        }
    }

    override fun onDestroy() {
        dbHelper.close()
        super.onDestroy()
    }
}
