package com.example.myapplication

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * RecyclerView adapter for displaying a list of speed violations.
 *
 * This adapter binds `SpeedViolation` objects to views defined in `item_violation.xml`,
 * allowing them to be displayed inside a RecyclerView.
 *
 * @property violations The list of speed violation entries to be displayed.
 */
class ViolationAdapter(private val violations: List<SpeedViolation>): RecyclerView.Adapter<ViolationAdapter.ViolationViewHolder>() {
    /**
     * Inflates the item view layout and creates a ViewHolder instance.
     *
     * @param parent The parent ViewGroup in which the new view will be added.
     * @param viewType The view type of the new View.
     * @return A new [ViolationViewHolder] containing the inflated layout.
     */
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViolationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_violation, parent, false)
        return ViolationViewHolder(view)
    }

    /**
     * Binds a violation item to the given ViewHolder.
     *
     * @param holder The ViewHolder that should be updated.
     * @param position The position of the item in the data list.
     */
    override fun onBindViewHolder(holder: ViolationViewHolder, position: Int) {
        val violation = violations[position]
        holder.bind(violation)
    }

    /**
     * Returns the total number of items in the violations list.
     */
    override fun getItemCount(): Int {
        return violations.size
    }
    
    /**
     * ViewHolder that represents each violation row in the RecyclerView.
     *
     * @param itemView The view that represents a single violation item.
     */
    class ViolationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val limitText: TextView = itemView.findViewById(R.id.violation_limit)
        private val speedText: TextView = itemView.findViewById(R.id.violation_speed)

        /**
         * Binds violation data to the view components.
         *
         * @param violation The speed violation model containing limit and speed details.
         */
        @SuppressLint("SetTextI18n")
        fun bind(violation: SpeedViolation) {
            limitText.text = "Limit: ${violation.limit} km/h"
            speedText.text = "Speed: ${violation.speed} km/h"
        }
    }
}
