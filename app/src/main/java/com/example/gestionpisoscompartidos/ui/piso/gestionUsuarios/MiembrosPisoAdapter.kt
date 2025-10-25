package com.example.gestionpisoscompartidos.ui.piso.gestionUsuarios

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.gestionpisoscompartidos.R


class MiembrosPisoAdapter(
    private val onRemoveClick: (MiembroPisoUI) -> Unit,
) : ListAdapter<MiembroPisoUI, MiembrosPisoAdapter.MiembroViewHolder>(MiembroDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MiembroViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_miembro_piso, parent, false)
        return MiembroViewHolder(view)
    }

    override fun onBindViewHolder(holder: MiembroViewHolder, position: Int) {
        val miembro = getItem(position)
        holder.bind(miembro)
    }

    inner class MiembroViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.member_name)
        private val adminTag: TextView = itemView.findViewById(R.id.member_role_admin)
        private val youTag: TextView = itemView.findViewById(R.id.member_role_you)
        private val removeButton: ImageView = itemView.findViewById(R.id.member_remove_button)
        private val colorIndicator: View = itemView.findViewById(R.id.member_color_indicator)

        fun bind(miembro: MiembroPisoUI) {
            nameTextView.text = miembro.nombre
            adminTag.visibility = if (miembro.esAdmin) View.VISIBLE else View.GONE
            youTag.visibility = if (miembro.esTu) View.VISIBLE else View.GONE

            try {
                val drawable = colorIndicator.background as GradientDrawable
                drawable.setColor(ContextCompat.getColor(itemView.context, miembro.colorIndicator))
            } catch (e: Exception) {
                e.printStackTrace()
            }

            removeButton.setOnClickListener { onRemoveClick(miembro) }

            // No puedes eliminarte a ti mismo
            removeButton.visibility = if (miembro.esTu) View.GONE else View.VISIBLE
        }
    }
}

// 3. El DiffUtil compara el Modelo de UI (usa el ID: Long)
class MiembroDiffCallback : DiffUtil.ItemCallback<MiembroPisoUI>() {
    override fun areItemsTheSame(oldItem: MiembroPisoUI, newItem: MiembroPisoUI): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: MiembroPisoUI, newItem: MiembroPisoUI): Boolean {
        return oldItem == newItem
    }
}