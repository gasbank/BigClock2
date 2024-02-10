package top.plusalpha.bigclock2

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.util.Locale

class SpeakerService : Service(), TextToSpeech.OnInitListener {
    companion object {
        const val EXTRA_MESSAGE = "message"
        const val CHANNEL_ID = "1"
    }

    private lateinit var tts: TextToSpeech
    private lateinit var handler: Handler
    var isInit: Boolean = false

    private var message: String? = null

    override fun onCreate() {
        super.onCreate()

        Log.i(LOG_TAG, "onUpdate")
        tts = TextToSpeech(applicationContext, this)
        Log.i(LOG_TAG, "TTS: $tts")

        handler = Handler(mainLooper)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "SpeakerService",
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = "Notification channel for foreground service notification"

            val notificationManager = NotificationManagerCompat.from(this);
            notificationManager.createNotificationChannel(channel)
        }

        val notificationBuilder = NotificationCompat.Builder(
            this, CHANNEL_ID
        )
            //.setSmallIcon(R.drawable.btn_radio)
            .setContentTitle("TTS Service for Big Clock 2")
            //.setContentText(this.getString(R.string.ok))
            .setOngoing(true)
        startForeground(1, notificationBuilder.build())
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onInit(initResult: Int) {
        if (initResult == TextToSpeech.SUCCESS) {

            val result = tts.setLanguage(Locale.KOREA)

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(LOG_TAG, "TTS set language failed (missing or not supported)")
            } else {
                Log.i(LOG_TAG, "TTS successfully init! :)")
                isInit = true

                speakOut(message)
                message = null
            }

        } else {
            Log.e(LOG_TAG, "TTS init failed with result $initResult")
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        handler.removeCallbacksAndMessages(null)


        message = intent!!.getStringExtra(EXTRA_MESSAGE)

        if (isInit) {
            speakOut(message)
            message = null
        }

        handler.postDelayed({ stopSelf() }, (15 * 1000).toLong())

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        tts.stop()
        tts.shutdown()
        super.onDestroy()
    }

    private fun speakOut(message: String?) {
        if (message != null) {
            tts.setSpeechRate(0.5f)
            tts.speak(message, TextToSpeech.QUEUE_FLUSH, null, null)
            Log.d(LOG_TAG, "TTS: Speaking: $message")
        } else {
            Log.d(LOG_TAG, "TTS: Empty message...")
        }
    }
}