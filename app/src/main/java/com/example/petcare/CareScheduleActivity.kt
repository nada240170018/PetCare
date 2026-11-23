package com.example.petcare

import android.Manifest
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import android.content.res.Configuration

class CareScheduleActivity : AppCompatActivity() {

    private lateinit var db: PetDatabaseHelper
    private lateinit var scheduleContainer: LinearLayout

    private val green = Color.rgb(66, 145, 119)
    private val darkGreen = Color.rgb(42, 105, 85)
    private val mint = Color.rgb(225, 244, 235)
    private val cream = Color.rgb(255, 249, 242)
    private val pink = Color.rgb(255, 235, 235)
    private val peach = Color.rgb(255, 241, 222)
    private val lavender = Color.rgb(241, 237, 255)
    private val dark = Color.rgb(55, 69, 61)
    private val gray = Color.rgb(128, 139, 132)
    private val white = Color.WHITE

    private val channelId = "petcare_reminders"
    private val notificationRequestCode = 200

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    private fun shape(color: Int, radius: Int = 18): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radius).toFloat()
        }
    }

    private fun makeText(
        value: String,
        size: Float,
        color: Int,
        bold: Boolean = false
    ): TextView {
        return TextView(this).apply {
            text = value
            textSize = size
            setTextColor(color)
            if (bold) typeface = Typeface.DEFAULT_BOLD
        }
    }

    private fun makeAction(
        value: String,
        bg: Int,
        fg: Int,
        action: () -> Unit
    ): TextView {
        return TextView(this).apply {
            text = value
            textSize = 12f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(fg)
            gravity = Gravity.CENTER
            background = shape(bg, 14)
            setPadding(dp(8), dp(10), dp(8), dp(10))
            isClickable = true
            isFocusable = true
            setOnClickListener { action() }
        }
    }

    private fun addGap(parent: LinearLayout, height: Int) {
        parent.addView(
            View(this),
            LinearLayout.LayoutParams(1, dp(height))
        )
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        db = PetDatabaseHelper(this)

        val isLandscape =
            resources.configuration.orientation ==
                    Configuration.ORIENTATION_LANDSCAPE

        window.statusBarColor = darkGreen
        window.navigationBarColor = cream

        // ROOT
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(cream)
        }

        // HEADER
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(
                dp(18),
                dp(if (isLandscape) 7 else 15),
                dp(18),
                dp(if (isLandscape) 7 else 15)
            )
            background = shape(green, 0)
        }

        val back = makeText("‹", 34f, white, true).apply {
            gravity = Gravity.CENTER
            setOnClickListener { finish() }
        }

        header.addView(
            back,
            LinearLayout.LayoutParams(dp(42), dp(44))
        )

        val headerText = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        headerText.addView(
            makeText("Jadwal Perawatan 🐾", 19f, white, true)
        )

        addGap(headerText, 2)

        headerText.addView(
            makeText(
                "Rawat si kecil dengan penuh cinta ♡",
                11f,
                white
            )
        )

        header.addView(
            headerText,
            LinearLayout.LayoutParams(0, -2, 1f)
        )

        // MAIN CONTENT
        val content = LinearLayout(this).apply {
            orientation = if (isLandscape) {
                LinearLayout.HORIZONTAL
            } else {
                LinearLayout.VERTICAL
            }

            setPadding(
                dp(if (isLandscape) 14 else 18),
                dp(if (isLandscape) 12 else 18),
                dp(if (isLandscape) 14 else 18),
                dp(10)
            )
        }

        // LEFT PANEL
        val introPanel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = if (isLandscape) {
                Gravity.CENTER_VERTICAL
            } else {
                Gravity.TOP
            }
        }

        introPanel.addView(
            makeText("Halo, Pet Lover! ♡", 20f, dark, true)
        )

        addGap(introPanel, 6)

        introPanel.addView(
            makeText(
                "Jangan lupa jadwalkan perawatan sahabat kecilmu.",
                13f,
                gray
            )
        )

        addGap(introPanel, if (isLandscape) 16 else 18)

        val addButton = makeAction(
            "＋  Tambah Jadwal Perawatan",
            green,
            white
        ) {
            showScheduleDialog(null)
        }.apply {
            textSize = 13f
        }

        introPanel.addView(
            addButton,
            LinearLayout.LayoutParams(
                -1,
                dp(if (isLandscape) 48 else 52)
            )
        )

        if (isLandscape) {
            addGap(introPanel, 14)

            introPanel.addView(
                makeText(
                    "♡ Catat jadwal vaksin, mandi, grooming, dan kesehatan hewan kesayanganmu.",
                    12f,
                    gray
                )
            )
        }

        // RIGHT PANEL
        val listPanel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(
                if (isLandscape) white else cream
            )

            if (isLandscape) {
                setPadding(dp(12), dp(12), dp(12), dp(8))
                background = shape(white, 18)
            }
        }

        val listTitle = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        listTitle.addView(
            makeText("Daftar Jadwal ♡", 17f, dark, true),
            LinearLayout.LayoutParams(0, -2, 1f)
        )

        listTitle.addView(
            makeText("PAW CARE", 9f, green, true).apply {
                background = shape(mint, 20)
                setPadding(dp(9), dp(5), dp(9), dp(5))
            }
        )

        listPanel.addView(listTitle)
        addGap(listPanel, 10)

        scheduleContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        val scrollView = ScrollView(this).apply {
            isFillViewport = true
            clipToPadding = false
            overScrollMode = View.OVER_SCROLL_NEVER
            setPadding(0, 0, 0, dp(8))
            addView(scheduleContainer)
        }

        listPanel.addView(
            scrollView,
            LinearLayout.LayoutParams(-1, 0, 1f)
        )

        // RESPONSIVE LAYOUT
        if (isLandscape) {
            content.addView(
                introPanel,
                LinearLayout.LayoutParams(0, -1, 0.38f).apply {
                    marginEnd = dp(14)
                }
            )

            content.addView(
                listPanel,
                LinearLayout.LayoutParams(0, -1, 0.62f)
            )
        } else {
            content.addView(
                introPanel,
                LinearLayout.LayoutParams(-1, -2)
            )

            addGap(content, 22)

            content.addView(
                listPanel,
                LinearLayout.LayoutParams(-1, 0, 1f)
            )
        }

        root.addView(header)

        root.addView(
            content,
            LinearLayout.LayoutParams(-1, 0, 1f)
        )

        setContentView(root)

        // SYSTEM BAR PADDING
        val originalTopPadding = header.paddingTop

        ViewCompat.setOnApplyWindowInsetsListener(root) { _, insets ->
            val statusBarHeight =
                insets.getInsets(WindowInsetsCompat.Type.statusBars()).top

            header.setPadding(
                header.paddingLeft,
                originalTopPadding + statusBarHeight,
                header.paddingRight,
                header.paddingBottom
            )

            insets
        }

        ViewCompat.requestApplyInsets(root)

        requestNotificationPermission()
        loadSchedules()
    }


    // PERMISSION NOTIFICATION
    private fun requestNotificationPermission() {
        if (
            Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                notificationRequestCode
            )
        }
    }

    // CONVERT DATE AND TIME TO TIMESTAMP
    private fun getScheduleTimestamp(
        date: String,
        time: String
    ): Long? {
        return try {
            val formatter = SimpleDateFormat(
                "yyyy-MM-dd HH:mm",
                Locale.US
            ).apply {
                isLenient = false
            }

            formatter.parse("$date $time")?.time
        } catch (_: Exception) {
            null
        }
    }

    // CREATE OR UPDATE REMINDER
    private fun scheduleReminder(
        scheduleId: Int,
        title: String,
        petName: String,
        date: String,
        time: String
    ) {
        cancelReminder(scheduleId)

        val triggerTime = getScheduleTimestamp(date, time) ?: return

        // Jangan pasang alarm untuk jadwal yang sudah lewat.
        if (triggerTime <= System.currentTimeMillis()) return

        val alarmManager =
            getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(this, CareReminderReceiver::class.java).apply {
            putExtra("scheduleId", scheduleId)
            putExtra("title", title)
            putExtra("petName", petName)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            scheduleId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            pendingIntent
        )
    }

    // CANCEL REMINDER
    private fun cancelReminder(scheduleId: Int) {
        val alarmManager =
            getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(this, CareReminderReceiver::class.java)

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            scheduleId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    // LOAD SCHEDULES

    private fun loadSchedules() {
        scheduleContainer.removeAllViews()

        val isLandscape =
            resources.configuration.orientation ==
                    Configuration.ORIENTATION_LANDSCAPE

        val schedules = db.getAllSchedules()

        // PASTIKAN PENGINGAT TETAP AKTIF
        schedules.forEach { schedule ->
            if (!schedule.isCompleted) {
                scheduleReminder(
                    schedule.id,
                    schedule.title,
                    schedule.petName,
                    schedule.scheduleDate,
                    schedule.scheduleTime
                )
            } else {
                cancelReminder(schedule.id)
            }
        }

        // EMPTY STATE
        if (schedules.isEmpty()) {
            val emptyCard = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setPadding(
                    dp(18),
                    dp(if (isLandscape) 18 else 35),
                    dp(18),
                    dp(if (isLandscape) 18 else 35)
                )
                background = shape(white, 22)
                elevation = dp(2).toFloat()
            }

            emptyCard.addView(
                makeText("🐶 🐱", 34f, green).apply {
                    gravity = Gravity.CENTER
                }
            )

            addGap(emptyCard, 8)

            emptyCard.addView(
                makeText(
                    "Belum Ada Jadwal",
                    17f,
                    dark,
                    true
                ).apply {
                    gravity = Gravity.CENTER
                }
            )

            addGap(emptyCard, 5)

            emptyCard.addView(
                makeText(
                    "Yuk, buat jadwal pertama untuk hewan kesayanganmu!",
                    12f,
                    gray
                ).apply {
                    gravity = Gravity.CENTER
                }
            )

            val params = LinearLayout.LayoutParams(-1, -2)
            params.bottomMargin = dp(10)
            scheduleContainer.addView(emptyCard, params)

            return
        }

        schedules.forEach { schedule ->

            // CARD
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL

                setPadding(
                    dp(if (isLandscape) 12 else 16),
                    dp(if (isLandscape) 11 else 16),
                    dp(if (isLandscape) 12 else 16),
                    dp(if (isLandscape) 11 else 16)
                )

                background = shape(white, 19)
                elevation = dp(2).toFloat()
            }

            // TITLE AND STATUS
            val titleRow = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }

            val title = makeText(
                "🐾  ${schedule.title}",
                if (isLandscape) 14f else 16f,
                darkGreen,
                true
            ).apply {
                maxLines = 2
            }

            titleRow.addView(
                title,
                LinearLayout.LayoutParams(0, -2, 1f)
            )

            val statusLabel = makeText(
                if (schedule.isCompleted) "Selesai ✓" else "Aktif ♡",
                10f,
                if (schedule.isCompleted) {
                    darkGreen
                } else {
                    Color.rgb(166, 119, 48)
                },
                true
            ).apply {
                background = shape(
                    if (schedule.isCompleted) mint else peach,
                    18
                )
                setPadding(dp(8), dp(5), dp(8), dp(5))
            }

            titleRow.addView(statusLabel)
            card.addView(titleRow)

            addGap(card, if (isLandscape) 7 else 12)

            // PET NAME
            val petBox = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                background = shape(mint, 12)
                setPadding(
                    dp(10),
                    dp(if (isLandscape) 6 else 10),
                    dp(10),
                    dp(if (isLandscape) 6 else 10)
                )
            }

            petBox.addView(makeText("♡", 15f, green, true))

            petBox.addView(
                makeText(
                    "  ${schedule.petName}",
                    13f,
                    dark,
                    true
                )
            )

            card.addView(petBox)

            addGap(card, if (isLandscape) 7 else 10)

            // DATE AND TIME
            val dateTimeRow = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }

            val dateBox = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                background = shape(cream, 11)
                setPadding(dp(9), dp(7), dp(9), dp(7))
            }

            dateBox.addView(
                makeText("📅 TANGGAL", 9f, gray, true)
            )

            addGap(dateBox, 3)

            dateBox.addView(
                makeText(
                    schedule.scheduleDate,
                    12f,
                    dark,
                    true
                )
            )

            val timeBox = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                background = shape(lavender, 11)
                setPadding(dp(9), dp(7), dp(9), dp(7))
            }

            timeBox.addView(
                makeText("🕒 JAM", 9f, gray, true)
            )

            addGap(timeBox, 3)

            timeBox.addView(
                makeText(
                    schedule.scheduleTime,
                    12f,
                    dark,
                    true
                )
            )

            dateTimeRow.addView(
                dateBox,
                LinearLayout.LayoutParams(0, -2, 1f).apply {
                    marginEnd = dp(5)
                }
            )

            dateTimeRow.addView(
                timeBox,
                LinearLayout.LayoutParams(0, -2, 1f).apply {
                    marginStart = dp(5)
                }
            )

            card.addView(dateTimeRow)

            // NOTES
            if (!schedule.notes.isNullOrBlank()) {
                addGap(card, 7)

                card.addView(
                    makeText(
                        "📝  ${schedule.notes}",
                        11f,
                        gray
                    )
                )
            }

            addGap(card, if (isLandscape) 9 else 13)

            // ACTION BUTTONS
            val buttons = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
            }

            fun addCardButton(
                label: String,
                bg: Int,
                fg: Int,
                action: () -> Unit
            ) {
                val button = makeAction(label, bg, fg, action).apply {
                    textSize = if (isLandscape) 10f else 12f
                    setPadding(
                        dp(4),
                        dp(if (isLandscape) 7 else 10),
                        dp(4),
                        dp(if (isLandscape) 7 else 10)
                    )
                }

                val params = LinearLayout.LayoutParams(
                    0,
                    dp(if (isLandscape) 36 else 42),
                    1f
                )

                params.setMargins(dp(3), 0, dp(3), 0)
                buttons.addView(button, params)
            }

            addCardButton("✎ Edit", mint, darkGreen) {
                showScheduleDialog(schedule)
            }

            addCardButton(
                if (schedule.isCompleted) "↻ Aktifkan" else "✓ Selesai",
                green,
                white
            ) {
                val newStatus = !schedule.isCompleted

                db.updateScheduleStatus(schedule.id, newStatus)

                if (newStatus) {
                    cancelReminder(schedule.id)
                } else {
                    scheduleReminder(
                        schedule.id,
                        schedule.title,
                        schedule.petName,
                        schedule.scheduleDate,
                        schedule.scheduleTime
                    )
                }

                loadSchedules()
            }

            addCardButton(
                "Hapus",
                pink,
                Color.rgb(190, 75, 75)
            ) {
                AlertDialog.Builder(this)
                    .setTitle("Hapus Jadwal? ♡")
                    .setMessage(
                        "Yakin ingin menghapus ${schedule.title}?"
                    )
                    .setPositiveButton("Hapus") { _, _ ->
                        cancelReminder(schedule.id)
                        db.deleteSchedule(schedule.id)
                        loadSchedules()

                        Toast.makeText(
                            this,
                            "Jadwal berhasil dihapus.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .setNegativeButton("Batal", null)
                    .show()
            }

            card.addView(buttons)

            val params = LinearLayout.LayoutParams(-1, -2)
            params.bottomMargin = dp(10)

            scheduleContainer.addView(card, params)
        }
    }


    // ADD OR EDIT SCHEDULE
    private fun showScheduleDialog(schedule: CareSchedule?) {

        val pets = db.getAllPets()

        if (pets.isEmpty()) {
            Toast.makeText(
                this,
                "Tambahkan data hewan terlebih dahulu.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val form = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(8), dp(20), dp(12))
        }

        fun addLabel(value: String) {
            form.addView(
                makeText(value, 13f, darkGreen, true).apply {
                    setPadding(0, dp(13), 0, dp(6))
                }
            )
        }

        fun addInput(hintText: String): EditText {
            return EditText(this).apply {
                hint = hintText
                textSize = 14f
                setTextColor(dark)
                setHintTextColor(gray)
                setPadding(dp(12), dp(10), dp(12), dp(10))
                background = shape(cream, 13)
            }
        }

        // PET
        addLabel("🐾 Pilih Hewan")

        val petSpinner = Spinner(this)
        petSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            pets.map { it.name }
        )

        form.addView(
            petSpinner,
            LinearLayout.LayoutParams(-1, dp(48))
        )

        // CARE TYPE
        val types = listOf(
            "Vaksinasi",
            "Mandi",
            "Grooming",
            "Pemberian Obat",
            "Pemeriksaan Kesehatan",
            "Perawatan Lainnya"
        )

        addLabel("♡ Jenis Perawatan")

        val typeSpinner = Spinner(this)
        typeSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            types
        )

        form.addView(
            typeSpinner,
            LinearLayout.LayoutParams(-1, dp(48))
        )

        // TITLE
        addLabel("✏ Nama Jadwal")

        val titleInput = addInput("Contoh: Vaksin rabies")
        titleInput.setSingleLine(true)

        form.addView(
            titleInput,
            LinearLayout.LayoutParams(-1, dp(50))
        )

        val calendar = Calendar.getInstance()

        var selectedDate = schedule?.scheduleDate
            ?: String.format(
                Locale.US,
                "%04d-%02d-%02d",
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH)
            )

        var selectedTime = schedule?.scheduleTime
            ?: String.format(
                Locale.US,
                "%02d:%02d",
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE)
            )

        // DATE
        addLabel("📅 Tanggal Perawatan")

        val dateButton = makeAction(
            "Pilih Tanggal  •  $selectedDate",
            mint,
            darkGreen
        ) {}

        dateButton.setOnClickListener {
            val parts = selectedDate.split("-")
            val year = parts.getOrNull(0)?.toIntOrNull()
                ?: calendar.get(Calendar.YEAR)
            val month = (parts.getOrNull(1)?.toIntOrNull() ?: 1) - 1
            val day = parts.getOrNull(2)?.toIntOrNull()
                ?: calendar.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    selectedDate = String.format(
                        Locale.US,
                        "%04d-%02d-%02d",
                        selectedYear,
                        selectedMonth + 1,
                        selectedDay
                    )
                    dateButton.text = "Pilih Tanggal  •  $selectedDate"
                },
                year,
                month,
                day
            ).show()
        }

        form.addView(
            dateButton,
            LinearLayout.LayoutParams(-1, dp(47))
        )

        // TIME
        addLabel("🕒 Jam Perawatan")

        val timeButton = makeAction(
            "Pilih Jam  •  $selectedTime",
            lavender,
            darkGreen
        ) {}

        timeButton.setOnClickListener {
            val parts = selectedTime.split(":")
            val hour = parts.getOrNull(0)?.toIntOrNull()
                ?: calendar.get(Calendar.HOUR_OF_DAY)
            val minute = parts.getOrNull(1)?.toIntOrNull()
                ?: calendar.get(Calendar.MINUTE)

            TimePickerDialog(
                this,
                { _, selectedHour, selectedMinute ->
                    selectedTime = String.format(
                        Locale.US,
                        "%02d:%02d",
                        selectedHour,
                        selectedMinute
                    )
                    timeButton.text = "Pilih Jam  •  $selectedTime"
                },
                hour,
                minute,
                true
            ).show()
        }

        form.addView(
            timeButton,
            LinearLayout.LayoutParams(-1, dp(47))
        )

        // NOTES
        addLabel("📝 Catatan (Opsional)")

        val notesInput = addInput("Contoh: Bawa buku vaksin")
        notesInput.minLines = 2
        notesInput.maxLines = 3
        notesInput.gravity = Gravity.TOP

        form.addView(
            notesInput,
            LinearLayout.LayoutParams(-1, dp(80))
        )

        // LOAD EXISTING DATA
        if (schedule != null) {
            val petIndex = pets.indexOfFirst {
                it.id == schedule.petId
            }

            if (petIndex >= 0) petSpinner.setSelection(petIndex)

            val typeIndex = types.indexOf(schedule.title)
            if (typeIndex >= 0) typeSpinner.setSelection(typeIndex)

            titleInput.setText(schedule.title)
            notesInput.setText(schedule.notes ?: "")
        }

        val scrollForm = ScrollView(this).apply {
            addView(form)
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle(
                if (schedule == null) "♡ Tambah Jadwal"
                else "♡ Edit Jadwal"
            )
            .setView(scrollForm)
            .setPositiveButton("Simpan", null)
            .setNegativeButton("Batal", null)
            .create()

        dialog.setOnShowListener {

            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(darkGreen)

            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                .setTextColor(gray)

            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener {

                    val title = titleInput.text.toString().trim()
                        .ifEmpty {
                            typeSpinner.selectedItem.toString()
                        }

                    val petId = pets[
                        petSpinner.selectedItemPosition
                    ].id

                    val petName = pets[
                        petSpinner.selectedItemPosition
                    ].name

                    val notes = notesInput.text.toString().trim()

                    // VALIDATE DATE AND TIME
                    val triggerTime = getScheduleTimestamp(
                        selectedDate,
                        selectedTime
                    )

                    if (triggerTime == null) {
                        Toast.makeText(
                            this,
                            "Tanggal atau jam tidak valid.",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@setOnClickListener
                    }

                    if (triggerTime <= System.currentTimeMillis()) {
                        Toast.makeText(
                            this,
                            "Pilih tanggal dan jam yang akan datang.",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@setOnClickListener
                    }

                    val result = if (schedule == null) {
                        db.insertSchedule(
                            petId,
                            title,
                            selectedDate,
                            selectedTime,
                            notes.ifEmpty { null }
                        )
                    } else {
                        db.updateSchedule(
                            schedule.id,
                            petId,
                            title,
                            selectedDate,
                            selectedTime,
                            notes.ifEmpty { null }
                        ).toLong()
                    }

                    if (result > 0) {

                        val savedId = if (schedule == null) {
                            result.toInt()
                        } else {
                            schedule.id
                        }

                        scheduleReminder(
                            savedId,
                            title,
                            petName,
                            selectedDate,
                            selectedTime
                        )

                        dialog.dismiss()
                        loadSchedules()

                        Toast.makeText(
                            this,
                            "Jadwal berhasil disimpan ♡",
                            Toast.LENGTH_SHORT
                        ).show()

                    } else {
                        Toast.makeText(
                            this,
                            "Jadwal gagal disimpan.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }

        dialog.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        db.close()
    }
}
