package com.example.gestionpisoscompartidos.ui.tareas

import android.graphics.Paint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.gestionpisoscompartidos.R
import com.example.gestionpisoscompartidos.databinding.ItemTaskPendingBinding
import com.example.gestionpisoscompartidos.model.Tarea
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

class TareasAdapter(
    private var tareas: List<Tarea>,
    private val onItemClick: (Tarea) -> Unit,
    private val onCompleteClick: (Tarea) -> Unit,
    private val onDeleteClick: (Tarea) -> Unit,
    private val onEditClick: (Tarea) -> Unit,
) : RecyclerView.Adapter<TareasAdapter.TareaViewHolder>() {
    override fun getItemViewType(position: Int): Int = if (tareas[position].completado) VIEW_TYPE_COMPLETED else VIEW_TYPE_PENDING

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): TareaViewHolder {
        val binding = ItemTaskPendingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TareaViewHolder(binding)
    }

    override fun getItemCount(): Int = tareas.size

    override fun onBindViewHolder(
        holder: TareaViewHolder,
        position: Int,
    ) {
        holder.bind(tareas[position])
    }

    fun updateData(newTareas: List<Tarea>) {
        tareas = newTareas
        notifyDataSetChanged()
    }

    inner class TareaViewHolder(
        private val binding: ItemTaskPendingBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(tarea: Tarea) {
            binding.taskTitle.text = tarea.nombre
            // Asigna el nombre del usuario o "Sin asignar"
            binding.taskAssignee.text = tarea.asignadoA?.nombre ?: "Sin asignar"

            val context = binding.root.context
            if (tarea.completado) {
                binding.root.setBackgroundResource(R.drawable.bg_task_completed)
                binding.taskTitle.paintFlags = binding.taskTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                binding.taskTitle.setTextColor(ContextCompat.getColor(context, R.color.black_alpha_60))

                val asignado = tarea.asignadoA?.nombre ?: "Alguien"
                var fechaFormateada = ""

                try {
                    val fechaCompletada = LocalDateTime.parse(LocalDateTime.now().toString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME)

                    val formatter =
                        DateTimeFormatter
                            .ofLocalizedDateTime(FormatStyle.MEDIUM) // Formato corto (ej: 6/11/25 18:30)
                            .withLocale(Locale.getDefault()) // Usa el idioma/región del teléfono

                    fechaFormateada = " a las ${fechaCompletada.format(formatter)}"
                } catch (e: Exception) {
                    Log.e("TareasAdapter", "No se pudo parsear fechaFin: ${tarea.fechaFin}", e)
                }
                binding.taskAssignee.text = "Completada por $asignado$fechaFormateada"
                binding.taskAssignee.setTextColor(ContextCompat.getColor(context, R.color.black_alpha_60))
                // ------------------------------------------

                binding.taskStatusIcon.setImageResource(R.drawable.ic_check_circle)
                binding.taskStatusIcon.setColorFilter(ContextCompat.getColor(context, R.color.black_alpha_60))
                binding.taskDeleteButton.setColorFilter(ContextCompat.getColor(context, R.color.black_alpha_60))
                binding.taskEditButton.setColorFilter(ContextCompat.getColor(context, R.color.black_alpha_60))
            } else {
                binding.root.setBackgroundResource(R.drawable.bg_task_pending)
                binding.taskTitle.paintFlags = binding.taskTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                binding.taskTitle.setTextColor(ContextCompat.getColor(context, R.color.white))

                binding.taskAssignee.text = tarea.asignadoA?.nombre ?: "Sin asignar"
                binding.taskAssignee.setTextColor(ContextCompat.getColor(context, R.color.white))

                binding.taskStatusIcon.setImageResource(R.drawable.ic_circle_outline)
                binding.taskStatusIcon.setColorFilter(ContextCompat.getColor(context, R.color.white))
                binding.taskDeleteButton.setColorFilter(ContextCompat.getColor(context, R.color.white))
                binding.taskEditButton.setColorFilter(ContextCompat.getColor(context, R.color.white))
            }

            // Listeners
            binding.taskStatusIcon.setOnClickListener { onCompleteClick(tarea) }
            binding.taskDeleteButton.setOnClickListener { onDeleteClick(tarea) }
            binding.root.setOnClickListener { onItemClick(tarea) }
            binding.taskEditButton.setOnClickListener { onEditClick(tarea) }
        }
    }

    companion object {
        private const val VIEW_TYPE_PENDING = 0
        private const val VIEW_TYPE_COMPLETED = 1
    }
}
