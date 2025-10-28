package com.example.gestionpisoscompartidos.ui.listas

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.gestionpisoscompartidos.databinding.ItemListaBinding
import com.example.gestionpisoscompartidos.model.Lista

class ListasAdapter(
    private var listas: List<Lista>,
    private val onItemClick: (Lista) -> Unit,
) : RecyclerView.Adapter<ListasAdapter.ListaViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ListaViewHolder {
        val binding = ItemListaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ListaViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ListaViewHolder,
        position: Int,
    ) {
        holder.bind(listas[position])
    }

    override fun getItemCount(): Int = listas.size

    fun updateData(newListas: List<Lista>) {
        listas = newListas
        notifyDataSetChanged()
    }

    inner class ListaViewHolder(
        private val binding: ItemListaBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(lista: Lista) {
            binding.tvNombreLista.text = lista.nombre
            binding.tvDescripcionLista.text = lista.descripcion ?: ""

            binding.root.setOnClickListener {
                onItemClick(lista)
            }
        }
    }
}
