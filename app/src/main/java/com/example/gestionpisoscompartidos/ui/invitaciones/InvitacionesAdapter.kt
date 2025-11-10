package com.example.gestionpisoscompartidos.ui.invitaciones

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.gestionpisoscompartidos.databinding.ItemInvitacionBinding // 1. Importa el ViewBinding de tu item
import com.example.gestionpisoscompartidos.model.InvitacionResponse

class InvitacionesAdapter(
    private val onAcceptClick: (InvitacionResponse) -> Unit,
    private val onRejectClick: (InvitacionResponse) -> Unit,
) : ListAdapter<InvitacionResponse, InvitacionesAdapter.InvitacionViewHolder>(InvitacionDiffCallback()) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): InvitacionViewHolder {
        val binding =
            ItemInvitacionBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            )
        return InvitacionViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: InvitacionViewHolder,
        position: Int,
    ) {
        val invitacion = getItem(position)
        holder.bind(invitacion)
    }

    inner class InvitacionViewHolder(
        private val binding: ItemInvitacionBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(invitacion: InvitacionResponse) {
            binding.tvCasaNombre.text = "Invitación a: ${invitacion.casaNombre}"
            binding.tvRemitenteNombre.text = "De: ${invitacion.remitenteNombre}"

            binding.btnAceptar.setOnClickListener {
                onAcceptClick(invitacion)
            }
            binding.btnRechazar.setOnClickListener {
                onRejectClick(invitacion)
            }
        }
    }

    /**
     * DiffUtil para que el ListAdapter sepa qué items han cambiado,
     * mejorando el rendimiento y permitiendo animaciones.
     */
    class InvitacionDiffCallback : DiffUtil.ItemCallback<InvitacionResponse>() {
        override fun areItemsTheSame(
            oldItem: InvitacionResponse,
            newItem: InvitacionResponse,
        ): Boolean = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: InvitacionResponse,
            newItem: InvitacionResponse,
        ): Boolean = oldItem == newItem
    }
}
