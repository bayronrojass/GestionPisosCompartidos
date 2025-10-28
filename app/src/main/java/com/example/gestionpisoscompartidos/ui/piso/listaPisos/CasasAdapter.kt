package com.example.gestionpisoscompartidos.ui.piso.listaPisos

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.gestionpisoscompartidos.databinding.ItemCasaBinding
import com.example.gestionpisoscompartidos.model.Casa
import android.util.Log
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

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

            try {
                // Intenta parsear la fecha/hora
                val fecha = LocalDateTime.parse(casa.fechaCreacion)
                // Formatea a un estilo más legible (ej: "28 oct. 2025")
                val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                binding.tvFechaCreacionCasa.text = "Creada el: ${fecha.format(formatter)}"
            } catch (e: Exception) {
                // Si el parseo falla, muestra el string original o un mensaje de error
                binding.tvFechaCreacionCasa.text = "Fecha: ${casa.fechaCreacion}"
                Log.e("CasasAdapter", "Error al parsear fecha: ${casa.fechaCreacion}", e)
            }

            // Configura el clic listener para toda la fila
            binding.root.setOnClickListener {
                onItemClick(casa)
            }
        }
    }
}
