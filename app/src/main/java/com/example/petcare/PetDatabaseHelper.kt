package com.example.petcare

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

data class Pet(
    val id: Int,
    val name: String,
    val type: String,
    val breed: String,
    val age: Double,
    val weight: Double,
    val photoUri: String? = null
)

class PetDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "petcare.db"
        private const val DATABASE_VERSION = 3

        private const val TABLE_PETS = "pets"
        private const val COL_ID = "id"
        private const val COL_NAME = "name"
        private const val COL_TYPE = "type"
        private const val COL_BREED = "breed"
        private const val COL_AGE = "age"
        private const val COL_WEIGHT = "weight"
        private const val COL_PHOTO_URI = "photo_uri"
        private const val COL_USER_UID = "user_uid"
    }

    // Membuat tabel database
    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_PETS (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_NAME TEXT NOT NULL,
                $COL_TYPE TEXT NOT NULL,
                $COL_BREED TEXT NOT NULL,
                $COL_AGE REAL NOT NULL,
                $COL_WEIGHT REAL NOT NULL,
                $COL_PHOTO_URI TEXT,
                $COL_USER_UID TEXT NOT NULL
            )
        """.trimIndent()

        db.execSQL(createTable)
    }

    // Memperbarui struktur database tanpa menghapus data lama
    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) {
        if (oldVersion < 2) {
            db.execSQL(
                "ALTER TABLE $TABLE_PETS ADD COLUMN $COL_PHOTO_URI TEXT"
            )
        }

        if (oldVersion < 3) {
            val currentUid = com.google.firebase.auth.FirebaseAuth
                .getInstance()
                .currentUser?.uid

            db.execSQL(
                "ALTER TABLE $TABLE_PETS ADD COLUMN $COL_USER_UID TEXT"
            )

            if (currentUid != null) {
                db.execSQL(
                    "UPDATE $TABLE_PETS SET $COL_USER_UID = ? WHERE $COL_USER_UID IS NULL",
                    arrayOf(currentUid)
                )
            }
        }
    }

    // Menambahkan data hewan
    fun insertPet(
        name: String,
        type: String,
        breed: String,
        age: Double,
        weight: Double
    ): Long {
        val currentUid = com.google.firebase.auth.FirebaseAuth
            .getInstance()
            .currentUser?.uid ?: return -1L
        val values = ContentValues().apply {
            put(COL_NAME, name)
            put(COL_TYPE, type)
            put(COL_BREED, breed)
            put(COL_AGE, age)
            put(COL_WEIGHT, weight)
            put(COL_USER_UID, currentUid)
        }

        return writableDatabase.insert(TABLE_PETS, null, values)
    }

    // Mengambil seluruh data hewan termasuk foto
    fun getAllPets(): List<Pet> {

        val petList = mutableListOf<Pet>()

        val currentUid = com.google.firebase.auth.FirebaseAuth
            .getInstance()
            .currentUser?.uid ?: return emptyList()

        val cursor = readableDatabase.query(
            TABLE_PETS,
            null,
            "$COL_USER_UID = ?",
            arrayOf(currentUid),
            null,
            null,
            "$COL_ID DESC"
        )

        cursor.use {
            while (it.moveToNext()) {

                petList.add(
                    Pet(
                        id = it.getInt(
                            it.getColumnIndexOrThrow(COL_ID)
                        ),
                        name = it.getString(
                            it.getColumnIndexOrThrow(COL_NAME)
                        ),
                        type = it.getString(
                            it.getColumnIndexOrThrow(COL_TYPE)
                        ),
                        breed = it.getString(
                            it.getColumnIndexOrThrow(COL_BREED)
                        ),
                        age = it.getDouble(
                            it.getColumnIndexOrThrow(COL_AGE)
                        ),
                        weight = it.getDouble(
                            it.getColumnIndexOrThrow(COL_WEIGHT)
                        ),
                        photoUri = it.getString(
                            it.getColumnIndexOrThrow(COL_PHOTO_URI)
                        )
                    )
                )
            }
        }

        return petList
    }

    // Memperbarui data hewan
    fun updatePet(
        id: Int,
        name: String,
        type: String,
        breed: String,
        age: Double,
        weight: Double
    ): Int {

        val values = ContentValues().apply {
            put(COL_NAME, name)
            put(COL_TYPE, type)
            put(COL_BREED, breed)
            put(COL_AGE, age)
            put(COL_WEIGHT, weight)
        }

        val currentUid = com.google.firebase.auth.FirebaseAuth
            .getInstance()
            .currentUser?.uid ?: return 0

        return writableDatabase.update(
            TABLE_PETS,
            values,
            "$COL_ID = ? AND $COL_USER_UID = ?",
            arrayOf(id.toString(), currentUid)
        )
    }

    // Menyimpan atau mengganti foto hewan
    fun updatePetPhoto(id: Int, photoUri: String): Int {
        val currentUid = com.google.firebase.auth.FirebaseAuth
            .getInstance()
            .currentUser?.uid ?: return 0

        val values = ContentValues().apply {
            put(COL_PHOTO_URI, photoUri)
        }

        return writableDatabase.update(
            TABLE_PETS,
            values,
            "$COL_ID = ? AND $COL_USER_UID = ?",
            arrayOf(id.toString(), currentUid)
        )
    }

    // Menghapus data hewan
    fun deletePet(id: Int): Int {
        val currentUid = com.google.firebase.auth.FirebaseAuth
            .getInstance()
            .currentUser?.uid ?: return 0

        return writableDatabase.delete(
            TABLE_PETS,
            "$COL_ID = ? AND $COL_USER_UID = ?",
            arrayOf(id.toString(), currentUid)
        )
    }
}
