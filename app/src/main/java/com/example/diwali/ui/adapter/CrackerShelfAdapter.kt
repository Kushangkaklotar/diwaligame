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

class CrackerShelfAdapter(
    private var crackers: List<CrackerItem>,
    private var selectedCrackerId: String,
    private val onCrackerClick: (CrackerItem) -> Unit
) : RecyclerView.Adapter<CrackerShelfAdapter.ShelfViewHolder>() {

    fun updateData(newCrackers: List<CrackerItem>, newSelectedId: String) {
        this.crackers = newCrackers
        this.selectedCrackerId = newSelectedId
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShelfViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cracker_shelf, parent, false)
        return ShelfViewHolder(view)
    }

    override fun onBindViewHolder(holder: ShelfViewHolder, position: Int) {
        val cracker = crackers[position]
        holder.bind(cracker, cracker.id == selectedCrackerId, onCrackerClick)
    }

    override fun getItemCount(): Int = crackers.size

    class ShelfViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // STRICTLY using findViewById (No ViewBinding)
        private val rootLayout: View = itemView.findViewById(R.id.clShelfCardRoot)
        private val tvCategoryName: TextView = itemView.findViewById(R.id.tvCategoryName)
        private val composeIcon: ComposeView = itemView.findViewById(R.id.composeShelfCrackerIcon)
        private val tvCrackerName: TextView = itemView.findViewById(R.id.tvShelfCrackerName)
        private val tvShelfBadge: TextView = itemView.findViewById(R.id.tvShelfBadge)

        fun bind(cracker: CrackerItem, isSelected: Boolean, onClick: (CrackerItem) -> Unit) {
            tvCategoryName.text = cracker.category.uppercase()
            tvCrackerName.text = cracker.name

            // Animated vector cracker art inside ComposeView
            composeIcon.setContent {
                CrackerArtView(type = cracker.type)
            }

            // Selection state styling
            if (isSelected) {
                tvShelfBadge.text = "SELECTED"
                tvShelfBadge.setTextColor(Color.parseColor("#FFFF6D00"))
                tvShelfBadge.setBackgroundResource(R.drawable.bg_pill_dark)

                val selectedDrawable = GradientDrawable().apply {
                    cornerRadius = 32f
                    setColor(Color.parseColor("#E6221545"))
                    setStroke(5, Color.parseColor("#FFFFD54F"))
                }
                rootLayout.background = selectedDrawable
            } else {
                tvShelfBadge.text = "BLAST ▶"
                tvShelfBadge.setTextColor(Color.parseColor("#FF00E676"))
                tvShelfBadge.setBackgroundResource(R.drawable.bg_pill_dark)

                val normalDrawable = GradientDrawable().apply {
                    cornerRadius = 28f
                    setColor(Color.parseColor("#E6151030"))
                    setStroke(2, Color.parseColor("#40FFD54F"))
                }
                rootLayout.background = normalDrawable
            }

            rootLayout.setOnClickListener {
                onClick(cracker)
            }
        }
    }
}
