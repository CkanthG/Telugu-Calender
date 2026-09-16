package com.example.telugucalendar.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.telugucalendar.panchangam.PanchangamCalculator
import com.example.telugucalendar.reminder.AlarmScheduler
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayDetailScreen(dayCal: Calendar, onBack: () -> Unit) {
    val panchangam = remember(dayCal.timeInMillis) { PanchangamCalculator.calculate(dayCal) }
    val dateFormat = remember { SimpleDateFormat("EEEE, d MMMM yyyy", Locale.ENGLISH) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(dateFormat.format(dayCal.time)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            DetailRow("వారం (Day)", panchangam.varaName)
            DetailRow("తిథి (Tithi)", "${panchangam.tithiName} (${panchangam.pakshaName})")
            DetailRow("నక్షత్రం (Nakshatra)", panchangam.nakshatraName)
            DetailRow("రాశి (Moon Rashi)", panchangam.rashiName)
            DetailRow("యోగం (Yoga)", panchangam.yogaName)
            DetailRow("మాసం (Masam)", panchangam.masaName + if (panchangam.isAdhikaMasa) " (అధిక మాసం)" else "")

            if (panchangam.isAmavasya) {
                SpecialBanner("అమావాస్య — Amavasya")
            }
            if (panchangam.isPournami) {
                SpecialBanner("పౌర్ణమి — Pournami")
            }

            Spacer(modifier = Modifier.height(16.dp))
            ReminderSection(dayCal)

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "గమనిక: ఈ లెక్కలు సుమారు ఖగోళ గణన ఆధారంగా ఉన్నాయి. ఖచ్చితమైన సమయాల కోసం ప్రామాణిక పంచాంగంతో సరిచూసుకోండి.\n" +
                        "(Note: these figures are from an approximate astronomical calculation. Please cross-check exact timings against a trusted published panchangam.)",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun ReminderSection(dayCal: Calendar) {
    val context = LocalContext.current
    var reminderText by remember { mutableStateOf("") }
    var scheduledLabel by remember { mutableStateOf<String?>(null) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "రిమైండర్ (Reminder)",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = reminderText,
                onValueChange = { reminderText = it },
                label = { Text("Reminder note (optional)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    val picked = dayCal.clone() as Calendar

                    // Step 1: pick the time of day.
                    val timePicker = TimePickerDialog(
                        context,
                        { _, hourOfDay, minute ->
                            picked.set(Calendar.HOUR_OF_DAY, hourOfDay)
                            picked.set(Calendar.MINUTE, minute)
                            picked.set(Calendar.SECOND, 0)

                            if (picked.timeInMillis <= System.currentTimeMillis()) {
                                Toast.makeText(
                                    context,
                                    "Please choose a future time.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@TimePickerDialog
                            }

                            val requestCode = picked.timeInMillis.toInt()
                            val title = "Telugu Calendar Reminder"
                            val message = reminderText.ifBlank {
                                "Reminder for ${SimpleDateFormat("d MMM yyyy, h:mm a", Locale.ENGLISH).format(picked.time)}"
                            }

                            AlarmScheduler.schedule(
                                context = context,
                                requestCode = requestCode,
                                triggerAtMillis = picked.timeInMillis,
                                title = title,
                                message = message
                            )

                            scheduledLabel = SimpleDateFormat(
                                "d MMM yyyy, h:mm a", Locale.ENGLISH
                            ).format(picked.time)

                            if (AlarmScheduler.needsExactAlarmPermission(context)) {
                                Toast.makeText(
                                    context,
                                    "Reminder set, but for exact-time alarms please allow " +
                                        "\"Alarms & reminders\" for this app in system settings.",
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                Toast.makeText(context, "Reminder set!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        dayCal.get(Calendar.HOUR_OF_DAY),
                        dayCal.get(Calendar.MINUTE),
                        false
                    )

                    // Step 2: pick the date first, then show the time picker above.
                    val datePicker = DatePickerDialog(
                        context,
                        { _, year, month, dayOfMonth ->
                            picked.set(Calendar.YEAR, year)
                            picked.set(Calendar.MONTH, month)
                            picked.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                            timePicker.show()
                        },
                        dayCal.get(Calendar.YEAR),
                        dayCal.get(Calendar.MONTH),
                        dayCal.get(Calendar.DAY_OF_MONTH)
                    )
                    datePicker.show()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.Notifications, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Set Reminder / Alarm")
            }

            scheduledLabel?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Reminder scheduled for $it",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontWeight = FontWeight.SemiBold)
        Text(text = value)
    }
    Divider()
}

@Composable
private fun SpecialBanner(text: String) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(12.dp),
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium
        )
    }
}
