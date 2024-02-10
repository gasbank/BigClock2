package top.plusalpha.bigclock2

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.RemoteViews
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

const val LOG_TAG = "BigClock2"

/**
 * Implementation of App Widget functionality.
 */
class BigClockWidget : AppWidgetProvider() {
    companion object { const val MY_ON_CLICK = "top.plusalpha.myOnClickTag" }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        Log.i(LOG_TAG, "onUpdate")

        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            Log.i(LOG_TAG, "App Widget ID: $appWidgetId")
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)

        Log.i(LOG_TAG, "onReceive() called; intent.action=${intent?.action}")

        if (intent != null && MY_ON_CLICK == intent.action) {
            Log.i(LOG_TAG, "MY_ON_CLICK")

            val speechIntent = Intent(
                context,
                SpeakerService::class.java
            )

            val sdf = SimpleDateFormat("M월 d일 a h시 m분", Locale.KOREA)
            val currentTime = sdf.format(Date())

            speechIntent.putExtra(
                SpeakerService.EXTRA_MESSAGE,
                currentTime
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context!!.startForegroundService(speechIntent)
            } else {
                context!!.startService(speechIntent)
            }
        }
    }

    private fun getPendingSelfIntent(context: Context?, appWidgetId: Int, action: String?): PendingIntent? {
        val intent = Intent(context, javaClass)
        intent.setAction(action)
        //intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        return PendingIntent.getBroadcast(context, appWidgetId, intent, PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        //val widgetText = context.getString(R.string.appwidget_text)
        val views = RemoteViews(context.packageName, R.layout.big_clock_widget)

        //views.setTextViewText(R.id.appwidget_text, widgetText)
        views.setOnClickPendingIntent(R.id.appwidget_text, getPendingSelfIntent(context, appWidgetId, MY_ON_CLICK));

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}

