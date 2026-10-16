package com.example.diwali.ui.adapter

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.diwali.R
import com.example.diwali.model.CrackerItem
import com.example.diwali.model.CrackerLevelConfig

class LevelNodeAdapter(
    private val cracker: CrackerItem,
    private val unlockedLevel: Int,
    private val onNodeClick: (Int) -> Unit
) : RecyclerView.Adapter<LevelNodeAdapter.NodeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NodeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_level_node, parent, false)
        return NodeViewHolder(view)
    }

    override fun onBindViewHolder(holder: NodeViewHolder, position: Int) {
        val level = position + 1
        holder.bind(cracker, level, unlockedLevel, onNodeClick)
    }

    override fun getItemCount(): Int = CrackerLevelConfig.TOTAL_LEVELS_PER_CRACKER

    class NodeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // STRICTLY using findViewById (No ViewBinding)
        private val rootLayout: View = itemView.findViewById(R.id.llNodeRoot)
        private val tvNumber: TextView = itemView.findViewById(R.id.tvNodeLevelNumber)
        private val tvSubtext: TextView = itemView.findViewById(R.id.tvNodeSubtext)
        private val ivLock: ImageView = itemView.findViewById(R.id.ivNodeLock)

        fun bind(cracker: CrackerItem, level: Int, unlockedLevel: Int, onClick: (Int) -> Unit) {
            tvNumber.text = level.toString()

            when {
                // 1. Current Active Stage (Vibrant Orange with Gold glow)
                level == unlockedLevel -> {
                    ivLock.visibility = View.GONE
                    tvSubtext.visibility = View.VISIBLE
                    tvSubtext.text = "PLAY"
                    tvSubtext.setTextColor(Color.WHITE)
                    tvNumber.setTextColor(Color.WHITE)

                    val bg = GradientDrawable().apply {
                        cornerRadius = 24f
                        setColor(Color.parseColor("#FFFF6D00"))
                        setStroke(4, Color.parseColor("#FFFFD54F"))
                    }
                    rootLayout.background = bg
                    rootLayout.setOnClickListener { onClick(level) }
                }

                // 2. Already Completed Stage (Forest Emerald Green)
                level < unlockedLevel -> {
                    ivLock.visibility = View.GONE
                    tvSubtext.visibility = View.VISIBLE
                    tvSubtext.text = "•••\nPLAY"
                    tvSubtext.setTextColor(Color.parseColor("#E0FFFFFF"))
                    tvNumber.setTextColor(Color.WHITE)

                    val bg = GradientDrawable().apply {
                        cornerRadius = 22f
                        setColor(Color.parseColor("#FF1B5E20"))
                        setStroke(2, Color.parseColor("#8000E676"))
                    }
                    rootLayout.background = bg
                    rootLayout.setOnClickListener { onClick(level) }
                }

                // 3. Next Unlockable Stage (Royal Blue with Gold Unlock Cost)
                level == unlockedLevel + 1 -> {
                    ivLock.visibility = View.GONE
                    tvSubtext.visibility = View.VISIBLE
                    val cost = CrackerLevelConfig.getLevelUnlockCost(cracker.rewardCoins, level)
                    tvSubtext.text = "● $cost\nUNLOCK"
                    tvSubtext.setTextColor(Color.parseColor("#FFFFD54F"))
                    tvNumber.setTextColor(Color.parseColor("#FFFFD54F"))

                    val bg = GradientDrawable().apply {
                        cornerRadius = 22f
                        setColor(Color.parseColor("#FF1A237E"))
                        setStroke(3, Color.parseColor("#FFFFD54F"))
                    }
                    rootLayout.background = bg
                    rootLayout.setOnClickListener { onClick(level) }
                }

                // 4. Distant Locked Stages
                else -> {
                    tvSubtext.visibility = View.GONE
                    ivLock.visibility = View.VISIBLE
                    tvNumber.setTextColor(Color.parseColor("#55FFFFFF"))

                    val bg = GradientDrawable().apply {
                        cornerRadius = 20f
                        setColor(Color.parseColor("#44100B24"))
                        setStroke(1, Color.parseColor("#22FFFFFF"))
                    }
                    rootLayout.background = bg
                    rootLayout.setOnClickListener(null)
                }
            }
        }
    }
}
