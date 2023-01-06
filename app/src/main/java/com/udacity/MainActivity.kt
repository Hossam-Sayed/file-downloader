package com.udacity

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*


class MainActivity : AppCompatActivity() {
    private val NOTIFICATION_ID = 0

    private var downloadID: Long = 0

    private lateinit var notificationManager: NotificationManager
    private lateinit var pendingIntent: PendingIntent
    private lateinit var fileName: String
    lateinit var downloadStatus: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        createChannel()

        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        custom_button.setOnClickListener {

            notificationManager = ContextCompat.getSystemService(
                application,
                NotificationManager::class.java
            ) as NotificationManager

            when (radioGroup.checkedRadioButtonId) {
                R.id.radio_glide -> {
                    fileName = resources.getString(R.string.glide_radio_btn_text)
                    download()
                }
                R.id.radio_repo -> {
                    fileName = resources.getString(R.string.repo_radio_btn_text)
                    download()
                }
                R.id.radio_retrofit -> {
                    fileName = resources.getString(R.string.retrofit_radio_btn_text)
                    download()
                }
                else -> Toast.makeText(
                    applicationContext,
                    "Please select the file to download",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel =
                NotificationChannel(
                    CHANNEL_ID,
                    "Download File",
                    NotificationManager.IMPORTANCE_HIGH
                )
                    .apply { setShowBadge(false) }

            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)
            notificationChannel.description = getString(R.string.notification_description)
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)

            if (id == downloadID) {

                val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
                val query = DownloadManager.Query().setFilterById(downloadID)
                val cursor = downloadManager.query(query)
                if (cursor.moveToFirst()) {

                    val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                    if (status == DownloadManager.STATUS_SUCCESSFUL) {
                        downloadStatus = "Success"
                        notificationManager.sendNotification(applicationContext)
                    } else {
                        downloadStatus = "Fail"
                        notificationManager.sendNotification(applicationContext)
                    }
                }
            }
        }
    }

    private fun download() {
        val request =
            DownloadManager.Request(Uri.parse(URL))
                .setTitle(getString(R.string.app_name))
                .setDescription(getString(R.string.app_description))
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager

        downloadID =
            downloadManager.enqueue(request)// enqueue puts the download request in the queue.
    }

    companion object {
        private const val URL =
            "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"
        private const val CHANNEL_ID = "channelId"
    }

    private fun NotificationManager.sendNotification(applicationContext: Context) {

        val contentIntent = Intent(applicationContext, DetailActivity::class.java)
        contentIntent.putExtra("fileName", fileName)
        contentIntent.putExtra("status", downloadStatus)
        pendingIntent = PendingIntent.getActivity(
            applicationContext,
            NOTIFICATION_ID,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(
            applicationContext,
            CHANNEL_ID
        )
            .setSmallIcon(R.drawable.ic_assistant_black_24dp)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_description))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .addAction(
                R.drawable.ic_assistant_black_24dp,
                getString(R.string.download_details),
                pendingIntent
            )

        notify(NOTIFICATION_ID, builder.build())
    }

}
