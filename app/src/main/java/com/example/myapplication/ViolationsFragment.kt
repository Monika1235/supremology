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
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class ViolationsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var violationAdapter: ViolationAdapter
    private val violationsList = mutableListOf<SpeedViolation>()

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
