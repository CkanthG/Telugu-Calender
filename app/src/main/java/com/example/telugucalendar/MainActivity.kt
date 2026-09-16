package com.example.telugucalendar

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.telugucalendar.reminder.NotificationHelper
import com.example.telugucalendar.ui.CalendarScreen
import com.example.telugucalendar.ui.DayDetailScreen
import com.example.telugucalendar.ui.theme.TeluguCalendarTheme
import java.util.Calendar

class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* no-op either way */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        NotificationHelper.createChannel(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            TeluguCalendarTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    var selectedDay by remember { mutableStateOf<Calendar?>(null) }

    val day = selectedDay
    if (day == null) {
        CalendarScreen(onDayClick = { selectedDay = it })
    } else {
        DayDetailScreen(dayCal = day, onBack = { selectedDay = null })
    }
}
