package es.mirumi.es.ui.piso.gestionUsuarios

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
import coil.load
import coil.transform.CircleCropTransformation
import es.mirumi.es.R

class MiembrosPisoAdapter(
    private val onRemoveClick: (MiembroPiso) -> Unit,
) : ListAdapter<MiembroPiso, MiembrosPisoAdapter.MiembroViewHolder>(MiembroDiffCallback()) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): MiembroViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_miembro_piso, parent, false)
        return MiembroViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: MiembroViewHolder,
        position: Int,
    ) {
        val miembro = getItem(position)
        holder.bind(miembro)
    }

    inner class MiembroViewHolder(
        itemView: View,
    ) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.member_name)
        private val adminTag: TextView = itemView.findViewById(R.id.member_role_admin)
        private val youTag: TextView = itemView.findViewById(R.id.member_role_you)
        private val removeButton: ImageView = itemView.findViewById(R.id.member_remove_button)
        private val colorIndicator: View = itemView.findViewById(R.id.member_color_indicator)

        // Las dos vistas del "Sándwich"
        private val profileImageView: ImageView = itemView.findViewById(R.id.member_profile_image)
        private val profileLetter: TextView = itemView.findViewById(R.id.member_profile_letter)

        fun bind(miembro: MiembroPiso) {
            nameTextView.text = miembro.nombre
            adminTag.visibility = if (miembro.esAdmin) View.VISIBLE else View.GONE
            youTag.visibility = if (miembro.esTu) View.VISIBLE else View.GONE

            profileLetter.text = miembro.nombre
                .getOrNull(0)
                ?.toString()
                ?.uppercase() ?: "?"

            // 2. Configuramos el color del circulito indicador pequeño
            try {
                val drawable = colorIndicator.background as GradientDrawable
                drawable.setColor(ContextCompat.getColor(itemView.context, miembro.colorIndicator))
            } catch (e: Exception) {
                e.printStackTrace()
            }

            val tieneFotoValida =
                !miembro.fotoUrl.isNullOrEmpty() &&
                    miembro.fotoUrl != "null" &&
                    miembro.fotoUrl!!.startsWith("http")

            if (tieneFotoValida) {
                profileImageView.visibility = View.VISIBLE
                profileImageView.load("${miembro.fotoUrl}?v=${System.currentTimeMillis()}") {
                    crossfade(true)
                    transformations(CircleCropTransformation())
                    listener(onError = { _, _ -> profileImageView.visibility = View.GONE })
                }
            } else {
                profileImageView.visibility = View.GONE
            }

            removeButton.setOnClickListener { onRemoveClick(miembro) }
            removeButton.visibility = if (miembro.esTu) View.GONE else View.VISIBLE
        }
    }
}

// El DiffUtil compara el Modelo de UI
class MiembroDiffCallback : DiffUtil.ItemCallback<MiembroPiso>() {
    override fun areItemsTheSame(
        oldItem: MiembroPiso,
        newItem: MiembroPiso,
    ): Boolean = oldItem.id == newItem.id

    override fun areContentsTheSame(
        oldItem: MiembroPiso,
        newItem: MiembroPiso,
    ): Boolean = oldItem == newItem
}
