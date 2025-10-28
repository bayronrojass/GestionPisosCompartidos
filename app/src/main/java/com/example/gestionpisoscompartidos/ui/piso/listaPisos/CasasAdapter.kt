package com.example.gestionpisoscompartidos.ui.piso.listaPisos

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.gestionpisoscompartidos.databinding.ItemCasaBinding // Importa el binding generado
import com.example.gestionpisoscompartidos.model.Casa // Importa tu modelo Casa

class CasasAdapter(
    private var casas: List<Casa>,
    private val onItemClick: (Casa) -> Unit, // Lambda para manejar clics en un item
) : RecyclerView.Adapter<CasasAdapter.CasaViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): CasaViewHolder {
        val binding = ItemCasaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CasaViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: CasaViewHolder,
        position: Int,
    ) {
        holder.bind(casas[position])
    }

    override fun getItemCount(): Int = casas.size

    // Función para actualizar la lista desde el Fragment/ViewModel
    fun updateData(newCasas: List<Casa>) {
        casas = newCasas
        notifyDataSetChanged() // Notifica al RecyclerView que los datos cambiaron
    }

    inner class CasaViewHolder(
        private val binding: ItemCasaBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(casa: Casa) {
            binding.tvNombreCasa.text = casa.nombre
            binding.tvDescripcionCasa.text = casa.descripcion ?: "Sin descripción" // Muestra descripción o texto alternativo

            // Configura el clic listener para toda la fila
            binding.root.setOnClickListener {
                onItemClick(casa)
            }
        }
    }
}
