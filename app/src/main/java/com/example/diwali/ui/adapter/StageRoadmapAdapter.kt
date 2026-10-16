package com.example.diwali.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.compose.ui.platform.ComposeView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.diwali.R
import com.example.diwali.model.CrackerItem
import com.example.diwali.ui.components.CrackerArtView

class StageRoadmapAdapter(
    private var crackers: List<CrackerItem>,
    private val getLevelForCracker: (String) -> Int,
    private val onLevelSelected: (CrackerItem, Int) -> Unit
) : RecyclerView.Adapter<StageRoadmapAdapter.StageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_stage_card, parent, false)
        return StageViewHolder(view)
    }

    override fun onBindViewHolder(holder: StageViewHolder, position: Int) {
        val cracker = crackers[position]
        val currentLevel = getLevelForCracker(cracker.id)
        holder.bind(cracker, currentLevel, onLevelSelected)
    }

    override fun getItemCount(): Int = crackers.size

    class StageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // STRICTLY using findViewById (No ViewBinding)
        private val composeIcon: ComposeView = itemView.findViewById(R.id.composeCardCrackerIcon)
        private val tvCrackerName: TextView = itemView.findViewById(R.id.tvCardCrackerName)
        private val tvCategory: TextView = itemView.findViewById(R.id.tvCardCategory)
        private val tvStagePill: TextView = itemView.findViewById(R.id.tvCardStagePill)
        private val tvActiveLevelTitle: TextView = itemView.findViewById(R.id.tvActiveLevelTitle)
        private val tvActiveLevelDesc: TextView = itemView.findViewById(R.id.tvActiveLevelDesc)
        private val rvGrid: RecyclerView = itemView.findViewById(R.id.rvLevelNodesGrid)

        fun bind(cracker: CrackerItem, currentLevel: Int, onSelect: (CrackerItem, Int) -> Unit) {
            tvCrackerName.text = cracker.name
            tvCategory.text = "${cracker.category} • Stage $currentLevel/25"
            tvStagePill.text = "Stage $currentLevel/25"

            composeIcon.setContent {
                CrackerArtView(type = cracker.type)
            }

            // Description info for active level
            if (cracker.type == com.example.diwali.model.CrackerType.SUTLI_BOMB) {
                val detail = com.example.diwali.model.SutliBombLevelConfig.getDetail(currentLevel)
                tvActiveLevelTitle.text = "Level $currentLevel: ${detail.name}"
                tvActiveLevelDesc.text = "${detail.phaseName} • ${detail.description}"
            } else {
                tvActiveLevelTitle.text = "${cracker.name} • Stage $currentLevel"
                tvActiveLevelDesc.text = "Blasting stage $currentLevel with heightened flash, crackle, and rewards!"
            }

            // Setup 4-column level grid
            rvGrid.layoutManager = GridLayoutManager(itemView.context, 4)
            rvGrid.adapter = LevelNodeAdapter(cracker, currentLevel) { selectedLevel ->
                onSelect(cracker, selectedLevel)
            }
        }
    }
}
