package com.example.myapplication

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import androidx.core.app.NotificationCompat
import com.example.myapplication.databinding.ActivityMainBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    private var speedViolationListener: ListenerRegistration?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Speed Violation Monitor"
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        binding.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        val auth = Firebase.auth
        val userID = auth.currentUser?.uid
        if(userID!=null) {
            listenForSpeedViolation(userID)
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    override fun onDestroy(){
        super.onDestroy()
        stopListenerForSpeedViolation()
    }
    private fun stopListenerForSpeedViolation() {
        speedViolationListener?.remove()  // This will stop the listener
    }
    private fun listenForSpeedViolation(userID: String) {
        val database = FirebaseFirestore.getInstance()
        val violationData = database.collection("violations").whereEqualTo("userID", userID)
        violationData.addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                Log.e("Firestore", "Error fetching data ${exception.message}")
                return@addSnapshotListener
            }
            if(snapshot != null){
                for (document in snapshot.documents){
                    val violation = document.toObject(SpeedViolation::class.java)
                    violation?.let{Log.e("Firestore","violation found!!")
                        notifyUser(violation, applicationContext)
                    }
                }
            }else{
                    Log.e("Firestore","No violations found")
                }
        }
    }

    private fun notifyUser(violation: SpeedViolation, context: Context){
        if(violation.status == "pending"){
            val title = "Speed violation detected"
            val text = "You have violated the speed limit ${violation.limit}!! current speed ${violation.speed}"
            val channelId = "speed violation_channel"
            val channel = NotificationChannel(channelId,"speed violations", NotificationManager.IMPORTANCE_HIGH).
            apply{ description = "notifications for speed violations" }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            val notification = NotificationCompat.Builder(context, channelId).setSmallIcon(android.R.drawable.ic_dialog_alert).
            setContentTitle(title).setContentText(text).setPriority(NotificationCompat.PRIORITY_HIGH).setAutoCancel(true)
                .build()
            val notifyManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notifyManager.notify(0,notification)
        }
    }
}