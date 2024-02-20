package com.madhav.travel_calculator

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.ToggleButton
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.ViewModel


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val miles = findViewById<ToggleButton>(R.id.units_button)
        val list_type = findViewById<ToggleButton>(R.id.list_type_button)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar2)
        val next_stop_text = findViewById<TextView>(R.id.next_stop_textview)
        val distance_travelled = findViewById<TextView>(R.id.distance_travelled_textview)
        val distance_left = findViewById<TextView>(R.id.distance_remaining_textview)
        val next_button = findViewById<Button>(R.id.next_stop_)

        // Set up the Compose UI initially
        val composeView = findViewById<ComposeView>(R.id.compose_view)
        composeView.setContent {
            calculator_app(
                miles,
                list_type,
                progressBar,
                next_stop_text,
                distance_travelled,
                distance_left,
                next_button,
                composeView
            )
        }
    }
}

@Composable
fun calculator_app(
    miles: ToggleButton,
    list_type: ToggleButton,
    progressBar: ProgressBar,
    next_stop_text: TextView,
    distance_travelled: TextView,
    distance_left: TextView,
    next_button: Button,
    composeView: ComposeView
) {
    var milesState by remember { mutableStateOf(false) }
    var listTypeState by remember { mutableStateOf(false) }
    var currentIndex by remember { mutableStateOf(-1) }
    var distanceTravelled by remember { mutableStateOf(0) }

    // Set the state of miles and list type based on the toggle buttons
    miles.setOnCheckedChangeListener { _, isChecked ->
        milesState = isChecked
    }

    list_type.setOnCheckedChangeListener { _, isChecked ->
        listTypeState = isChecked
    }

    val stop = CreateStopsList()
    val totalDistance = stop.last().distance

    // Choose between lazy or normal list based on list type state
    if (listTypeState) {
        Column {
            Spacer(modifier = Modifier.weight(0.68f))
            Box(modifier = Modifier.weight(1f)) {
                LazyList(milesState)
            }
            Spacer(modifier = Modifier.height(80.dp))
        }
    } else {
        Column{
            Spacer(modifier = Modifier.weight(0.68f))
            Box(modifier = Modifier.weight(1f)) {
                NormalList(milesState)
            }
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    if(milesState) {
        distance_travelled.text = KilometersToMiles(distanceTravelled).toString()
        distance_left.text = KilometersToMiles(CalculateRemainingDistance(stop, distanceTravelled)).toString()
    } else {
        distance_travelled.text = distanceTravelled.toString()
        distance_left.text = CalculateRemainingDistance(stop, distanceTravelled).toString()
    }

    // Button click listener to update distance traveled and remaining
    next_button.setOnClickListener {
        currentIndex++
        if (currentIndex < stop.size) {
            val currentStop = stop[currentIndex]
            distanceTravelled = currentStop.distance
            val progressPercentage = (distanceTravelled.toFloat() / totalDistance.toFloat()) * 100
            progressBar.progress = progressPercentage.toInt()
            if(milesState) {
                distance_travelled.text = KilometersToMiles(distanceTravelled).toString()
                distance_left.text = KilometersToMiles(CalculateRemainingDistance(stop, distanceTravelled)).toString()
            } else {
                distance_travelled.text = distanceTravelled.toString()
                distance_left.text = CalculateRemainingDistance(stop, distanceTravelled).toString()
            }
            next_stop_text.text = currentStop.name
        } else {
            currentIndex = -1
        }
    }
}


fun CalculateRemainingDistance(stop: MutableList<Stop>, distanceTravelled: Int): Int {
    val totalDistance = stop[stop.size - 1].distance
    return totalDistance - distanceTravelled
}

@Preview(heightDp = 400)
@Composable
fun PreviewItem() {
    MaterialTheme {
        Column {
            // Add Spacer to push the list to the bottom
            Spacer(modifier = Modifier.weight(0.2f))
            Box(modifier = Modifier.weight(1f)) {
                LazyList(true)
            }
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

//    LazyList()
}

@Composable
fun LazyList(miles: Boolean = false) {
    val lazyListState = rememberLazyListState() // Remember the lazy list state
    LazyColumn(state = lazyListState) { // Pass the lazy list state to LazyColumn
        items(CreateStopsList()) { stop ->
            if (miles) {
                StopsList(stop.name, KilometersToMiles(stop.distance), "miles")
            } else {
                StopsList(stop.name, stop.distance, stop.unit)
            }
        }
    }
}

@Composable
fun NormalList(miles: Boolean = false) {
    val scrollState = rememberScrollState() // Remember the scroll state
    Column(
        modifier = Modifier
            .verticalScroll(scrollState) // Pass the scroll state to verticalScroll
            .fillMaxWidth()
    ) {
        if (miles) {
            CreateStopsList().forEach {
                StopsList(it.name, KilometersToMiles(it.distance), "miles")
            }
        } else {
            CreateStopsList().forEach {
                StopsList(it.name, it.distance, it.unit)
            }
        }
    }
}


@Composable
fun StopsList(stop: String, int: Int, unit: String) {
    Card(
        elevation = 8.dp,
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .fillMaxWidth()
            .shadow(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text(
                    "\t$stop",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(12.dp)
                )
            }
            Column {
                Text("$int $unit", fontSize = 20.sp, modifier = Modifier.padding(12.dp))
            }
        }
    }
}


data class Stop(val name: String, var distance: Int, var unit: String)

fun CreateStopsList(): MutableList<Stop> {
    val list = mutableListOf<Stop>()
    list.add(Stop("Stop 1", 7, "km"))
    list.add(Stop("Stop 2", 12, "km"))
    list.add(Stop("Stop 3", 17, "km"))
    list.add(Stop("Stop 4", 20, "km"))
    list.add(Stop("Stop 5", 29, "km"))
    list.add(Stop("Stop 6", 31, "km"))
    list.add(Stop("Stop 7", 35, "km"))
    list.add(Stop("Stop 8", 41, "km"))
    list.add(Stop("Stop 9", 49, "km"))
    list.add(Stop("Stop 10", 59, "km"))
    list.add(Stop("Stop 11", 70, "km"))
    list.add(Stop("Stop 12", 83, "km"))
    list.add(Stop("Stop 13", 90, "km"))
    list.add(Stop("Stop 14", 100, "km"))
    list.add(Stop("Stop 15", 110, "km"))
    list.add(Stop("Stop 16", 120, "km"))
    list.add(Stop("Stop 17", 130, "km"))

    return list
}

fun KilometersToMiles(km: Int): Int {
    return (km * 0.62).toInt()
}