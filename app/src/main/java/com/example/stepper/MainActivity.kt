package com.example.stepper

import android.content.Context
import android.graphics.Paint
import android.graphics.Path
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stepper.ui.theme.StepperTheme

class MainActivity : ComponentActivity(), SensorEventListener {

    private var running = false // Give the running status
    private var totalSteps = 0F // Count total steps
    private var previousTotalSteps = 0F // Count previous total steps
    private lateinit var stepsTaken: MutableState<Float>
    private var sensorManager: SensorManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StepperTheme {
                sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
                stepsTaken = remember { mutableStateOf(0F) }
                loadPreviousData()

                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CounterUI()
                }
            }
        }
    }

    @Composable
    @OptIn(ExperimentalFoundationApi::class)
    private fun CounterUI() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Green.copy(0.35F)),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.verticalGradient(
                            listOf(
                                Color.Blue,
                                Color.Red.copy(0.75F),
                                Color.Yellow.copy(0.8F)
                            )
                        )
                    )
                    .combinedClickable(
                        onClick = {},
                        onLongClick = {
                            stepsTaken.value = 0F
                            saveData() // Save the previous data
                        }
                    )
            ) {
                Text(
                    text = "${stepsTaken.value.toInt()}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center),
                    color = Color.White,
                    fontSize = 100.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                CurvedText(
                    modifier = Modifier.padding(start = 9.dp),
                    text = "Long press to reset."
                )
            }
        }
    }

    @Composable
    private fun CurvedText(modifier: Modifier = Modifier, text: String) {
        Canvas(modifier = modifier) {
            drawIntoCanvas { canvas ->
                val textPadding = 35.dp.toPx()
                val arcHeight = 290.dp.toPx()
                val arcWidth = 280.dp.toPx()

                val path = Path().apply {
                    addArc(0f, textPadding, arcWidth, arcHeight, 180f, -180f)
                }

                canvas.nativeCanvas.drawTextOnPath(
                    text,
                    path,
                    0f, 0f,
                    Paint().apply {
                        textSize = 20.sp.toPx()
                        textAlign = Paint.Align.CENTER
                        //color = resources.getColor(R.color.white)
                    }
                )
            }
        }
    }

    private fun saveData() {
        val sharedPrefs = getSharedPreferences("savedPref", Context.MODE_PRIVATE)
        val editor = sharedPrefs.edit()
        editor.putFloat("prevSteps", previousTotalSteps)
        editor.apply()
    }

    private fun loadPreviousData() { // Retrieve old saved data
        val sharedPrefs = getSharedPreferences("savedPref", Context.MODE_PRIVATE)
        val savedNumber = sharedPrefs.getFloat("prevSteps", 0F)
        previousTotalSteps = savedNumber
    }

    override fun onResume() {
        super.onResume()
        running = true
        val stepSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) // 19

        if (stepSensor == null) {
            Toast.makeText(this, "No sensor detected on this device", Toast.LENGTH_SHORT).show()
        } else {
            sensorManager?.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (running) {
            totalSteps = event!!.values[0]
            stepsTaken.value = totalSteps - previousTotalSteps // current steps
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Toast.makeText(this, "Accuracy Changed $accuracy", Toast.LENGTH_SHORT).show()
    }
}
