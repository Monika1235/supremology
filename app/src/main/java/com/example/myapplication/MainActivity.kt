package com.example.myapplication
import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

/**
 * Main activity for the application.
 *
 * This activity sets up location updates using the fused location provider to monitor the device's
 * current speed and detect speed violations. When a violation is detected the violation data is
 * recorded into the Firebase Realtime Database together with the user's associated rental company.
 */
class MainActivity : AppCompatActivity() {
    private lateinit var fusedLocationClient : FusedLocationProviderClient
    private lateinit var locationCallback : LocationCallback
    private val database = Firebase.database
    private val auth = Firebase.auth
    private val userID = auth.currentUser?.uid

    /**
     * Called when the activity is starting.
     *
     * Sets up the fused location provider, the location request and callback, attaches the view
     * and requests location permissions if they are not already granted.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down
     * then this Bundle contains the data it most recently supplied.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L).
            setMaxUpdates(1).build() // Optional: Limit number of location updates

        setContentView(R.layout.activity_main)

        locationCallback = object:LocationCallback(){
            override fun onLocationResult(locationResult : LocationResult){
                super.onLocationResult(locationResult)
                locationResult.locations.forEach{
                        location-> checkForSpeedViolation(location)
                }
            }
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // You can access location, request location updates
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        } else {
            // Request permission
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }

    }

    /**
     * Fetches the rental company's display name for the provided rentalCompanyID and invokes the callback with it.
     *
     * This reads the "rental_companies/{rentalCompanyID}/rentalCompanyId" node from Firebase and returns the
     * resolved company name (if available) via the provided callback.
     *
     * @param rentalCompanyID The ID of the rental company to look up.
     * @param callback A function that will be invoked with the rental company name once it is retrieved.
     */
    fun fetchRentalCompanyName(rentalCompanyID:String, callback: (String) -> Unit) {
        val rentalCompanyRef = database.getReference("rental_companies").child(rentalCompanyID)
        rentalCompanyRef.child("rentalCompanyId").addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val companyName = snapshot.getValue(String::class.java)
                if (companyName !=  null){ callback(companyName)}
                else{ Toast.makeText(this@MainActivity, "Company not found", Toast.LENGTH_SHORT).show()}
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            } })
    }
    private fun getCompanyID(userID:String, callback: (String) -> Unit) {
        val userRef = database.getReference("users").child(userID)

        userRef.child("rentalCompanyId")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val rentalCompanyID = snapshot.getValue(String::class.java)
                    if (rentalCompanyID != null) {
                        fetchRentalCompanyName(rentalCompanyID, callback)
                    } else {
                        Toast.makeText(this@MainActivity, "Company ID not found", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                } })
    }

    private fun logViolationSpeedData(userID:String, currentSpeed: Float, speedLimit:Float) {

        getCompanyID(userID){
                CompanyName ->
                val violationData = hashMapOf(
                    "speed" to currentSpeed,
                    "limit" to speedLimit,
                    "Company Name" to CompanyName
                )

                database.getReference("violations").child(userID).setValue(violationData)
                    .addOnSuccessListener { println("violation data logged successfully") }
                    .addOnFailureListener { println("violation data failure logged") }
            }
    }

    private fun checkForSpeedViolation(location: android.location.Location) {
        val currentSpeed = location.speed * 3.6f // Speed in km/h
        val speedLimit = 50f
        //val userID = auth.currentUser?.uid
        if (userID != null && currentSpeed > speedLimit) {
            Toast.makeText(this, "Speed Violation! You are over speeding", Toast.LENGTH_LONG).show()
            logViolationSpeedData(userID, currentSpeed, speedLimit)
        }
    }

    /**
     * Called when the activity is no longer visible to the user.
     *
     * Stops location updates to avoid continuing to receive location callbacks when the activity is stopped.
     */
    override fun onStop() {
        super.onStop()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}