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

/**
 * **MainActivity**
 *
 * This activity acts as the central controller for the app.  
 * It:
 * - Sets up navigation and toolbar
 * - Listens for speed violations for the logged-in user
 * - Shows system notifications when violations occur
 * - Manages Firestore listeners to prevent memory leaks
 */
class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private var speedViolationListener: ListenerRegistration?=null

    /**
     * Called when the activity is created.
     *
     * Responsibilities:
     * - Inflate layout
     * - Set up navigation and toolbar
     * - Attach listener for speed violations if user is authenticated
     */
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

    /**
     * Inflate the activity menu.
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    /**
     * Handle toolbar menu selections.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Handles “navigate up” button actions for the navigation component.
     */
    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    /**
     * Lifecycle callback.
     *
     * Removes Firestore listener to prevent memory leaks.
     */
    override fun onDestroy(){
        super.onDestroy()
        stopListenerForSpeedViolation()
    }

    /**
     * Removes the active Firestore listener if it exists.
     *
     * Prevents:
     * - Memory leaks
     * - Listeners running after activity is gone
     */
    private fun stopListenerForSpeedViolation() {
        speedViolationListener?.remove() 
    }

    /**
     * Attaches a Firestore real-time listener for speed violations.
     *
     * @param userID The authenticated user's unique ID.
     *
     * Behavior:
     * - Listens continuously for changes in the "violations" collection
     * - Filters by current user
     * - Triggers notifications for new pending violations
     */
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

    /**
     * Shows a push notification for the user when a speed violation occurs.
     *
     * @param violation The violation data retrieved from Firestore.
     * @param context The context used for accessing system services.
     *
     * Notification is shown only when:
     * - `violation.status == "pending"`
     * - A valid NotificationChannel exists (Android 8+)
     */
    private fun notifyUser(violation: SpeedViolation, context: Context){
        if(violation.status == "pending"){
            val title = "Speed violation detected"
            val text = "You have violated the speed limit ${violation.limit}!! current speed ${violation.speed}"
            val channelId = "speed_violation_channel"
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
