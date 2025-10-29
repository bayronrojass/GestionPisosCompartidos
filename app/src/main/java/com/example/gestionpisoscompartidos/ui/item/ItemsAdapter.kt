package com.example.gestionpisoscompartidos.ui.item

import android.graphics.Paint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.gestionpisoscompartidos.R
import com.example.gestionpisoscompartidos.databinding.ItemElementoBinding
import com.example.gestionpisoscompartidos.model.Elemento

class ItemsAdapter(
    private var items: List<Elemento>,
    private val onCompletadoClick: (Elemento) -> Unit,
    private val onBorrarClick: (Elemento) -> Unit,
) : RecyclerView.Adapter<ItemsAdapter.ItemViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ItemViewHolder {
        val binding = ItemElementoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ItemViewHolder,
        position: Int,
    ) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<Elemento>) {
        items = newItems
        notifyDataSetChanged()
        Log.d("ItemsAdapter", "Actualizados ${items.size} items")
    }

    inner class ItemViewHolder(
        private val binding: ItemElementoBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(elemento: Elemento) {
            binding.tvNombreElemento.text = elemento.nombre

            // Estilo visual basado en si está completado
            val context = binding.root.context
            if (elemento.completado) {
                binding.root.setBackgroundResource(R.drawable.bg_task_completed) // Fondo gris
                binding.tvNombreElemento.paintFlags = binding.tvNombreElemento.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG // Tachar texto
                binding.tvNombreElemento.setTextColor(ContextCompat.getColor(context, R.color.black_alpha_60))
                binding.btnCompletado.setImageResource(R.drawable.ic_check_circle) // Icono check
                binding.btnCompletado.setColorFilter(ContextCompat.getColor(context, R.color.black_alpha_60))
                binding.btnBorrarElemento.setColorFilter(ContextCompat.getColor(context, R.color.black_alpha_60))
            } else {
                binding.root.setBackgroundResource(R.drawable.bg_task_pending) // Fondo morado
                binding.tvNombreElemento.paintFlags = binding.tvNombreElemento.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv() // Quitar tachado
                binding.tvNombreElemento.setTextColor(ContextCompat.getColor(context, R.color.white))
                binding.btnCompletado.setImageResource(R.drawable.ic_circle_outline) // Icono círculo
                binding.btnCompletado.setColorFilter(ContextCompat.getColor(context, R.color.white))
                binding.btnBorrarElemento.setColorFilter(ContextCompat.getColor(context, R.color.white))
            }

            // Listeners
            binding.btnCompletado.setOnClickListener {
                onCompletadoClick(elemento)
            }
            binding.btnBorrarElemento.setOnClickListener {
                onBorrarClick(elemento)
            }
        }
    }
}
