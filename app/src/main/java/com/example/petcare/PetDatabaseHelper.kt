package com.example.petcare

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.google.firebase.auth.FirebaseAuth

data class Pet(
    val id: Int,
    val name: String,
    val type: String,
    val breed: String,
    val age: Double,
    val weight: Double,
    val photoUri: String? = null
)

data class CareSchedule(
    val id: Int,
    val petId: Int,
    val petName: String,
    val title: String,
    val scheduleDate: String,
    val scheduleTime: String,
    val notes: String?,
    val isCompleted: Boolean
)

class PetDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "petcare.db"
        private const val DATABASE_VERSION = 4

        private const val TABLE_PETS = "pets"
        private const val COL_ID = "id"
        private const val COL_NAME = "name"
        private const val COL_TYPE = "type"
        private const val COL_BREED = "breed"
        private const val COL_AGE = "age"
        private const val COL_WEIGHT = "weight"
        private const val COL_PHOTO_URI = "photo_uri"
        private const val COL_USER_UID = "user_uid"

        private const val TABLE_SCHEDULES = "care_schedules"
    }

    private fun createScheduleTable(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS $TABLE_SCHEDULES (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                pet_id INTEGER NOT NULL,
                title TEXT NOT NULL,
                schedule_date TEXT NOT NULL,
                schedule_time TEXT NOT NULL,
                notes TEXT,
                user_uid TEXT NOT NULL,
                is_completed INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent())
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createPetTable = """
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

        db.execSQL(createPetTable)
        createScheduleTable(db)
    }

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
            val currentUid = FirebaseAuth.getInstance().currentUser?.uid

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

        if (oldVersion < 4) {
            createScheduleTable(db)
        }
    }

    private fun getCurrentUid(): String? {
        return FirebaseAuth.getInstance().currentUser?.uid
    }

    // ==================== DATA HEWAN ====================

    fun insertPet(
        name: String,
        type: String,
        breed: String,
        age: Double,
        weight: Double
    ): Long {
        val currentUid = getCurrentUid() ?: return -1L

        return try {
            val values = ContentValues().apply {
                put(COL_NAME, name)
                put(COL_TYPE, type)
                put(COL_BREED, breed)
                put(COL_AGE, age)
                put(COL_WEIGHT, weight)
                put(COL_USER_UID, currentUid)
            }

            writableDatabase.insert(TABLE_PETS, null, values)
        } catch (e: Exception) {
            android.util.Log.e("PetCare", "Gagal menyimpan data hewan", e)
            -1L
        }
    }

    fun getAllPets(): List<Pet> {
        val petList = mutableListOf<Pet>()
        val currentUid = getCurrentUid() ?: return emptyList()

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
                        id = it.getInt(it.getColumnIndexOrThrow(COL_ID)),
                        name = it.getString(it.getColumnIndexOrThrow(COL_NAME)),
                        type = it.getString(it.getColumnIndexOrThrow(COL_TYPE)),
                        breed = it.getString(it.getColumnIndexOrThrow(COL_BREED)),
                        age = it.getDouble(it.getColumnIndexOrThrow(COL_AGE)),
                        weight = it.getDouble(it.getColumnIndexOrThrow(COL_WEIGHT)),
                        photoUri = it.getString(it.getColumnIndexOrThrow(COL_PHOTO_URI))
                    )
                )
            }
        }

        return petList
    }

    fun updatePet(
        id: Int,
        name: String,
        type: String,
        breed: String,
        age: Double,
        weight: Double
    ): Int {
        val currentUid = getCurrentUid() ?: return 0

        val values = ContentValues().apply {
            put(COL_NAME, name)
            put(COL_TYPE, type)
            put(COL_BREED, breed)
            put(COL_AGE, age)
            put(COL_WEIGHT, weight)
        }

        return writableDatabase.update(
            TABLE_PETS,
            values,
            "$COL_ID = ? AND $COL_USER_UID = ?",
            arrayOf(id.toString(), currentUid)
        )
    }

    fun updatePetPhoto(id: Int, photoUri: String): Int {
        val currentUid = getCurrentUid() ?: return 0

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

    fun deletePet(id: Int): Int {
        val currentUid = getCurrentUid() ?: return 0

        return writableDatabase.delete(
            TABLE_PETS,
            "$COL_ID = ? AND $COL_USER_UID = ?",
            arrayOf(id.toString(), currentUid)
        )
    }

    // ==================== JADWAL PERAWATAN ====================

    fun insertSchedule(
        petId: Int,
        title: String,
        scheduleDate: String,
        scheduleTime: String,
        notes: String?
    ): Long {
        val currentUid = getCurrentUid() ?: return -1L

        // Pastikan hewan benar-benar milik akun yang sedang login.
        val petCursor = readableDatabase.query(
            TABLE_PETS,
            arrayOf(COL_ID),
            "$COL_ID = ? AND $COL_USER_UID = ?",
            arrayOf(petId.toString(), currentUid),
            null,
            null,
            null
        )

        val petExists = petCursor.use { it.moveToFirst() }
        if (!petExists) return -1L

        val values = ContentValues().apply {
            put("pet_id", petId)
            put("title", title)
            put("schedule_date", scheduleDate)
            put("schedule_time", scheduleTime)
            put("notes", notes)
            put("user_uid", currentUid)
            put("is_completed", 0)
        }

        return writableDatabase.insert(TABLE_SCHEDULES, null, values)
    }

    fun getAllSchedules(): List<CareSchedule> {
        val schedules = mutableListOf<CareSchedule>()
        val currentUid = getCurrentUid() ?: return emptyList()

        val cursor = readableDatabase.rawQuery(
            """
            SELECT s.id, s.pet_id, p.name AS pet_name,
                   s.title, s.schedule_date, s.schedule_time,
                   s.notes, s.is_completed
            FROM $TABLE_SCHEDULES s
            INNER JOIN $TABLE_PETS p ON s.pet_id = p.$COL_ID
            WHERE s.user_uid = ? AND p.$COL_USER_UID = ?
            ORDER BY s.schedule_date ASC, s.schedule_time ASC
            """.trimIndent(),
            arrayOf(currentUid, currentUid)
        )

        cursor.use {
            while (it.moveToNext()) {
                schedules.add(
                    CareSchedule(
                        id = it.getInt(it.getColumnIndexOrThrow("id")),
                        petId = it.getInt(it.getColumnIndexOrThrow("pet_id")),
                        petName = it.getString(it.getColumnIndexOrThrow("pet_name")),
                        title = it.getString(it.getColumnIndexOrThrow("title")),
                        scheduleDate = it.getString(it.getColumnIndexOrThrow("schedule_date")),
                        scheduleTime = it.getString(it.getColumnIndexOrThrow("schedule_time")),
                        notes = it.getString(it.getColumnIndexOrThrow("notes")),
                        isCompleted = it.getInt(it.getColumnIndexOrThrow("is_completed")) == 1
                    )
                )
            }
        }

        return schedules
    }

    fun updateSchedule(
        id: Int,
        petId: Int,
        title: String,
        scheduleDate: String,
        scheduleTime: String,
        notes: String?
    ): Int {
        val currentUid = getCurrentUid() ?: return 0

        val values = ContentValues().apply {
            put("pet_id", petId)
            put("title", title)
            put("schedule_date", scheduleDate)
            put("schedule_time", scheduleTime)
            put("notes", notes)
        }

        return writableDatabase.update(
            TABLE_SCHEDULES,
            values,
            "id = ? AND user_uid = ?",
            arrayOf(id.toString(), currentUid)
        )
    }

    fun updateScheduleStatus(id: Int, isCompleted: Boolean): Int {
        val currentUid = getCurrentUid() ?: return 0

        val values = ContentValues().apply {
            put("is_completed", if (isCompleted) 1 else 0)
        }

        return writableDatabase.update(
            TABLE_SCHEDULES,
            values,
            "id = ? AND user_uid = ?",
            arrayOf(id.toString(), currentUid)
        )
    }

    fun deleteSchedule(id: Int): Int {
        val currentUid = getCurrentUid() ?: return 0

        return writableDatabase.delete(
            TABLE_SCHEDULES,
            "id = ? AND user_uid = ?",
            arrayOf(id.toString(), currentUid)
        )
    }
}