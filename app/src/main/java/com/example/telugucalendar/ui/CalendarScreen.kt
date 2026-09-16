package com.example.telugucalendar.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.telugucalendar.panchangam.PanchangamCalculator
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(onDayClick: (Calendar) -> Unit) {
    var monthCal by remember {
        mutableStateOf(Calendar.getInstance().apply { set(Calendar.DAY_OF_MONTH, 1) })
    }

    val monthFormat = remember { SimpleDateFormat("MMMM yyyy", Locale.ENGLISH) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("తెలుగు క్యాలెండర్") })
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {

            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    val c = monthCal.clone() as Calendar
                    c.add(Calendar.MONTH, -1)
                    monthCal = c
                }) { Icon(Icons.Filled.ArrowBack, contentDescription = "Previous month") }

                Text(
                    text = monthFormat.format(monthCal.time),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                IconButton(onClick = {
                    val c = monthCal.clone() as Calendar
                    c.add(Calendar.MONTH, 1)
                    monthCal = c
                }) { Icon(Icons.Filled.ArrowForward, contentDescription = "Next month") }
            }

            val weekDayLabels = listOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa")
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
                weekDayLabels.forEach {
                    Text(
                        text = it,
                        modifier = Modifier.weight(1f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            val firstDayOfWeek = (monthCal.clone() as Calendar).apply {
                set(Calendar.DAY_OF_MONTH, 1)
            }.get(Calendar.DAY_OF_WEEK) - 1 // 0 = Sunday

            val daysInMonth = (monthCal.clone() as Calendar).getActualMaximum(Calendar.DAY_OF_MONTH)

            val cells = remember(monthCal) {
                val list = mutableListOf<Calendar?>()
                repeat(firstDayOfWeek) { list.add(null) }
                for (d in 1..daysInMonth) {
                    val c = monthCal.clone() as Calendar
                    c.set(Calendar.DAY_OF_MONTH, d)
                    list.add(c)
                }
                list
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier.fillMaxSize().padding(4.dp)
            ) {
                items(cells) { dayCal ->
                    if (dayCal == null) {
                        Box(modifier = Modifier.aspectRatio(0.8f))
                    } else {
                        DayCell(dayCal, onClick = { onDayClick(dayCal) })
                    }
                }
            }
        }
    }
}

private fun isToday(dayCal: Calendar): Boolean {
    val today = Calendar.getInstance()
    return dayCal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
            dayCal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
}

@Composable
private fun DayCell(dayCal: Calendar, onClick: () -> Unit) {
    val panchangam = remember(dayCal.timeInMillis) { PanchangamCalculator.calculate(dayCal) }
    val today = isToday(dayCal)
    val bgColor = when {
        today -> MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
        panchangam.isAmavasya -> Color(0xFFD7CCC8)
        panchangam.isPournami -> Color(0xFFFFECB3)
        else -> MaterialTheme.colorScheme.surface
    }
    val borderModifier = if (today) {
        Modifier.border(2.dp, MaterialTheme.colorScheme.primary)
    } else {
        Modifier
    }

    Column(
        modifier = Modifier
            .aspectRatio(0.8f)
            .padding(2.dp)
            .background(bgColor)
            .then(borderModifier)
            .clickable { onClick() }
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = dayCal.get(Calendar.DAY_OF_MONTH).toString(),
            fontWeight = FontWeight.Bold,
            color = if (today) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = panchangam.tithiName,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1
        )
        Text(
            text = panchangam.nakshatraName,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            color = MaterialTheme.colorScheme.primary
        )
        if (panchangam.isAdhikaMasa) {
            Text(
                text = "అధిక",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Red,
                maxLines = 1
            )
        }
    }
}
