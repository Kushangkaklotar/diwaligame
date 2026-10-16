package com.example.diwali.ui.adapter

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.compose.ui.platform.ComposeView
import androidx.recyclerview.widget.RecyclerView
import com.example.diwali.R
import com.example.diwali.model.CrackerItem
import com.example.diwali.ui.components.CrackerArtView

class CrackerRailAdapter(
    private var crackers: List<CrackerItem>,
    private var selectedIndex: Int,
    private val getLevelForCracker: (String) -> Int,
    private val onItemClick: (Int) -> Unit
) : RecyclerView.Adapter<CrackerRailAdapter.RailViewHolder>() {

    fun updateSelection(newIndex: Int) {
        val oldIndex = this.selectedIndex
        this.selectedIndex = newIndex
        notifyItemChanged(oldIndex)
        notifyItemChanged(newIndex)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RailViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cracker_rail, parent, false)
        return RailViewHolder(view)
    }

    override fun onBindViewHolder(holder: RailViewHolder, position: Int) {
        val cracker = crackers[position]
        val currentLevel = getLevelForCracker(cracker.id)
        holder.bind(cracker, position == selectedIndex, currentLevel) {
            onItemClick(position)
        }
    }

    override fun getItemCount(): Int = crackers.size

    class RailViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // STRICTLY using findViewById (No ViewBinding)
        private val rootLayout: View = itemView.findViewById(R.id.llRailItemRoot)
        private val composeIcon: ComposeView = itemView.findViewById(R.id.composeRailCrackerIcon)
        private val tvCrackerName: TextView = itemView.findViewById(R.id.tvRailCrackerName)
        private val tvLevelBadge: TextView = itemView.findViewById(R.id.tvRailLevelBadge)

        fun bind(cracker: CrackerItem, isSelected: Boolean, level: Int, onClick: () -> Unit) {
            tvCrackerName.text = cracker.name
            tvLevelBadge.text = "L$level/25"

            composeIcon.setContent {
                CrackerArtView(type = cracker.type)
            }

            if (isSelected) {
                val selectedBg = GradientDrawable().apply {
                    cornerRadius = 24f
                    setColor(Color.parseColor("#E6FF6D00"))
                    setStroke(3, Color.parseColor("#FFFFD54F"))
                }
                rootLayout.background = selectedBg
                tvCrackerName.setTextColor(Color.WHITE)
            } else {
                val normalBg = GradientDrawable().apply {
                    cornerRadius = 20f
                    setColor(Color.parseColor("#80151030"))
                    setStroke(1, Color.parseColor("#40FFD54F"))
                }
                rootLayout.background = normalBg
                tvCrackerName.setTextColor(Color.parseColor("#CCFFFFFF"))
            }

            rootLayout.setOnClickListener {
                onClick()
            }
        }
    }
}
