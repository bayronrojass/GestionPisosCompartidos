package com.example.gestionpisoscompartidos.ui.tareas

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gestionpisoscompartidos.databinding.FragmentTareasBinding
import com.example.gestionpisoscompartidos.model.Tarea

class Tareas : Fragment() {
    private var _binding: FragmentTareasBinding? = null
    private val binding get() = _binding!!

    private val args: TareasArgs by navArgs()
    private val viewModel: TareasViewModel by viewModels {
        TareasViewModelFactory(args.casaId)
    }
    private lateinit var tareasAdapter: TareasAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentTareasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        binding.headerTitle.text = args.casaNombre

        setupRecyclerView()
        setupListeners()
        setupObservers()
    }

    private fun setupRecyclerView() {
        tareasAdapter =
            TareasAdapter(
                emptyList(),
                onItemClick = { tarea ->
                    AlertDialog
                        .Builder(requireContext())
                        .setTitle(tarea.nombre)
                        .setMessage(tarea.descripcion ?: "Sin descripción.")
                        .setPositiveButton("Cerrar", null)
                        .show()
                },
                onCompleteClick = { tarea ->
                    viewModel.toggleCompletado(tarea)
                },
                onDeleteClick = { tarea ->
                    AlertDialog
                        .Builder(requireContext())
                        .setTitle("Borrar Tarea")
                        .setMessage("¿Seguro que quieres borrar '${tarea.nombre}'?")
                        .setPositiveButton("Borrar") { _, _ -> viewModel.borrarTarea(tarea) }
                        .setNegativeButton("Cancelar", null)
                        .show()
                },
                onEditClick = { tarea ->
                    mostrarDialogoEditarTarea(tarea)
                },
            )
        binding.recyclerViewTasks.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = tareasAdapter
        }
    }

    private fun setupListeners() {
        binding.fabAddTask.setOnClickListener {
            mostrarDialogoCrearTarea()
        }
    }

    private fun setupObservers() {
        viewModel.tareas.observe(viewLifecycleOwner) { tareas ->
            tareasAdapter.updateData(tareas)
            binding.tvMensajeVacioTareas.isVisible = tareas.isEmpty()
            binding.recyclerViewTasks.isVisible = tareas.isNotEmpty()
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBarTareas.isVisible = isLoading
            if (isLoading) {
                binding.recyclerViewTasks.isVisible = false
                binding.tvMensajeVacioTareas.isVisible = false
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun mostrarDialogoCrearTarea() {
        val context = requireContext()
        val linearLayout =
            LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(48, 48, 48, 48)
            }
        val nombreInput = EditText(context).apply { hint = "Nombre de la tarea" }
        val descripcionInput = EditText(context).apply { hint = "Descripción (opcional)" }

        linearLayout.addView(nombreInput)
        linearLayout.addView(descripcionInput)

        AlertDialog
            .Builder(context)
            .setTitle("Nueva Tarea")
            .setView(linearLayout)
            .setPositiveButton("Crear") { _, _ ->
                val nombre = nombreInput.text.toString()
                val descripcion = descripcionInput.text.toString().ifBlank { null }
                if (nombre.isNotBlank()) {
                    viewModel.crearTarea(nombre, descripcion, null)
                } else {
                    Toast.makeText(context, "El nombre es obligatorio", Toast.LENGTH_SHORT).show()
                }
            }.setNegativeButton("Cancelar", null)
            .show()
    }

    private fun mostrarDialogoEditarTarea(tarea: Tarea) {
        // TODO: Este diálogo debe ser más complejo para incluir
        // nombre, descripción, y un Spinner/Dropdown para asignar miembro.
        // Por ahora, solo edita nombre y descripción.

        val context = requireContext()
        val linearLayout =
            LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(48, 48, 48, 48)
            }
        val nombreInput =
            EditText(context).apply {
                hint = "Nombre de la tarea"
                setText(tarea.nombre)
            }
        val descripcionInput =
            EditText(context).apply {
                hint = "Descripción (opcional)"
                setText(tarea.descripcion)
            }
        // Aquí faltaría el Spinner para 'asignadoAId'

        linearLayout.addView(nombreInput)
        linearLayout.addView(descripcionInput)

        AlertDialog
            .Builder(context)
            .setTitle("Editar Tarea")
            .setView(linearLayout)
            .setPositiveButton("Guardar") { _, _ ->
                val nombre = nombreInput.text.toString()
                val descripcion = descripcionInput.text.toString().ifBlank { null }
                if (nombre.isNotBlank()) {
                    // (null para asignadoAId por ahora)
                    viewModel.editarTarea(tarea, nombre, descripcion, null)
                } else {
                    Toast.makeText(context, "El nombre es obligatorio", Toast.LENGTH_SHORT).show()
                }
            }.setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
