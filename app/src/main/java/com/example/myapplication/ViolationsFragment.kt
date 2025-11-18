package com.example.myapplication

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

/**
 * Fragment responsible for displaying all speed violations logged for the current user.
 *
 * This fragment listens to Firestore's `violations` collection and shows all documents
 * that match the current user's ID. Data is displayed using a RecyclerView backed by
 * [ViolationAdapter].
 *
 * Responsibilities:
 * - Fetching the authenticated user's ID.
 * - Listening for Firestore updates in real-time.
 * - Updating the RecyclerView when new violations are found.
 */
class ViolationsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var violationAdapter: ViolationAdapter
    private val violationsList = mutableListOf<SpeedViolation>()

    /**
     * Called when the view hierarchy of this fragment has been created.
     *
     * Initializes RecyclerView, fetches the user ID from FirebaseAuth, and
     * starts listening for changes in Firestore's `violations` collection.
     *
     * @param view The root View returned by [onCreateView].
     * @param savedInstanceState Previously saved instance state.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerViewViolations)
        violationAdapter = ViolationAdapter(violationsList)
        recyclerView.adapter = violationAdapter

        val auth = Firebase.auth
        val userID = auth.currentUser?.uid

        userID?.let {
            val database = FirebaseFirestore.getInstance()
            database.collection("violations")
                .whereEqualTo("userID", userID)
                .addSnapshotListener { snapshot, exception ->
                    if (exception != null) {
                        Log.e("Firestore", "Error fetching data ${exception.message}")
                        return@addSnapshotListener
                    }
                    snapshot?.let { documents ->
                        violationsList.clear()
                        for (document in documents) {
                            val violation = document.toObject(SpeedViolation::class.java)
                            violationsList.add(violation)
                        }
                        violationAdapter.notifyDataSetChanged()
                    }
                }
        }
    }
}
