package com.example.myapplication

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ViolationAdapter(private val violations: List<SpeedViolation>): RecyclerView.Adapter<ViolationAdapter.ViolationViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViolationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_violation, parent, false)
        return ViolationViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViolationViewHolder, position: Int) {
        val violation = violations[position]
        holder.bind(violation)
    }

    override fun getItemCount(): Int {
        return violations.size
    }
    class ViolationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val limitText: TextView = itemView.findViewById(R.id.violation_limit)
        private val speedText: TextView = itemView.findViewById(R.id.violation_speed)

        @SuppressLint("SetTextI18n")
        fun bind(violation: SpeedViolation) {
            limitText.text = "Limit: ${violation.limit} km/h"
            speedText.text = "Speed: ${violation.speed} km/h"
        }
    }
}