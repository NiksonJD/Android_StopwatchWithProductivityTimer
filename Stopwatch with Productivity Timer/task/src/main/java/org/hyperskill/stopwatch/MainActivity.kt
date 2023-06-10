package org.hyperskill.stopwatch

import android.app.AlertDialog
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.InputType
import android.text.method.DigitsKeyListener
import android.view.View
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import org.hyperskill.stopwatch.databinding.ActivityMainBinding
import kotlin.random.Random

const val CHANNEL_ID = "org.hyperskill"

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        var (isThreadRunning, elapsedTime, handler) = Triple(true, 0, Handler())
        val (black, red) = 0xFF000000.toInt() to 0xFFFF0000.toInt()
        var (added, upperLimit, timerColor) = Triple(false, -1, black)
        val bar = binding.progressBar

        val timerRunnable = object : Runnable {
            override fun run() {
                val formattedTime = String.format("%02d:%02d", elapsedTime / 60, elapsedTime % 60)
                binding.textView.text = formattedTime.also { elapsedTime++ }
                timerColor = if (upperLimit in 1 until elapsedTime)
                    red.also { if (!added) { added = true; addNF(this@MainActivity) }} else black
                binding.textView.setTextColor(timerColor)
                bar.indeterminateTintList = ColorStateList.valueOf(Random.nextInt(0xFFFFFF + 1))
                if (isThreadRunning) handler.postDelayed(this, 1000)
            }
        }

        binding.startButton.setOnClickListener {
            if (elapsedTime == 0) {
                isThreadRunning = true.also { handler.post(timerRunnable) }
                bar.visibility = View.VISIBLE
                binding.settingsButton.isEnabled = false
                createChannel(this)
            }
        }

        binding.resetButton.setOnClickListener {
            isThreadRunning = false.also { handler.removeCallbacks(timerRunnable) }
            binding.textView.text = "00:00"
            elapsedTime = 0
            bar.visibility = View.GONE
            binding.settingsButton.isEnabled = true
            binding.textView.setTextColor(black)
        }

        binding.settingsButton.setOnClickListener {
            val editText = EditText(this).apply {
                id = R.id.upperLimitEditText
                inputType = InputType.TYPE_CLASS_NUMBER
                keyListener = DigitsKeyListener.getInstance("0123456789")
            }
            AlertDialog.Builder(this)
                .setTitle("Set upper limit in seconds")
                .setView(editText)
                .setPositiveButton("OK") { _, _ ->
                    val inputText = editText.text.toString()
                    if (inputText.isNotBlank()) upperLimit = inputText.toInt()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun createChannel(context: Context) {
    val name = "Time exceeded"
    val importance = NotificationManager.IMPORTANCE_HIGH
    val channel = NotificationChannel(CHANNEL_ID, name, importance)
    val notificationManager = context.getSystemService(NotificationManager::class.java)
    notificationManager.createNotificationChannel(channel)
}

@RequiresApi(Build.VERSION_CODES.M)
fun addNF(context: Context) {
    val notificationFI = Notification.FLAG_INSISTENT
    val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentTitle("Warning")
        .setContentText("Time exceeded")
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setOnlyAlertOnce(true)
    val notificationManager = context.getSystemService(NotificationManager::class.java)
    notificationManager.notify(393939, notificationBuilder.build().apply { flags += notificationFI })
}