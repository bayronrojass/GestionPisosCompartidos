package com.example.gestionpisoscompartidos.ui.item

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.example.gestionpisoscompartidos.databinding.FragmentItemBinding
import android.app.AlertDialog
import android.widget.EditText
import android.widget.LinearLayout

class Item : Fragment() {
    private var _binding: FragmentItemBinding? = null
    private val binding get() = _binding!!

    private val args: ItemArgs by navArgs()
    private val viewModel: ItemViewModel by viewModels {
        ItemViewModelFactory(args.listaId)
    }
    private lateinit var itemsAdapter: ItemsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentItemBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("ItemFragment", "Recibido listaId: ${args.listaId}")

        binding.headerTitle.text = args.casaNombre
        binding.sectionTitle.text = args.listaNombre

        setupRecyclerView()
        setupViewModelObservers()
        setupListeners()
    }

    private fun setupRecyclerView() {
        itemsAdapter =
            ItemsAdapter(
                emptyList(),
                onCompletadoClick = { item ->
                    Log.d("ItemFragment", "Click completado en: ${item.nombre}")
                    // TODO: Llamar a viewModel.toggleItemCompletado(item)
                    Toast.makeText(context, "TODO: Marcar ${item.nombre}", Toast.LENGTH_SHORT).show()
                },
                onBorrarClick = { item ->
                    Log.d("ItemFragment", "Click borrar en: ${item.nombre}")
                    // TODO: Llamar a viewModel.deleteItem(item)
                    Toast.makeText(context, "TODO: Borrar ${item.nombre}", Toast.LENGTH_SHORT).show()
                },
            )
        binding.recyclerViewItems.adapter = itemsAdapter // Usa el ID correcto del RecyclerView
    }

    private fun setupViewModelObservers() {
        viewModel.items.observe(viewLifecycleOwner) { listaItems ->
            itemsAdapter.updateData(listaItems)
            if (viewModel.isLoading.value == false) {
                binding.recyclerViewItems.isVisible = listaItems.isNotEmpty()
                binding.tvMensajeVacioItems.isVisible = listaItems.isEmpty()
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBarItems.isVisible = isLoading
            if (isLoading) {
                binding.recyclerViewItems.isVisible = false
                binding.tvMensajeVacioItems.isVisible = false
            } else {
                val items = viewModel.items.value
                if (items != null) {
                    binding.recyclerViewItems.isVisible = items.isNotEmpty()
                    binding.tvMensajeVacioItems.isVisible = items.isEmpty()
                } else {
                    binding.recyclerViewItems.isVisible = false
                    binding.tvMensajeVacioItems.isVisible = true
                }
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMsg ->
            if (errorMsg != null) {
                Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                binding.progressBarItems.isVisible = false
                binding.recyclerViewItems.isVisible = false
                binding.tvMensajeVacioItems.isVisible = true
            }
        }
    }

    private fun setupListeners() {
        binding.fabAdd.setOnClickListener {
            mostrarDialogoCrearItem()
        }
    }

    private fun mostrarDialogoCrearItem() {
        val context = requireContext()

        val linearLayout =
            LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(48, 48, 48, 48)
            }

        val nombreInput =
            EditText(context).apply {
                hint = "Nombre del item"
                maxLines = 1
            }

        val descripcionInput =
            EditText(context).apply {
                hint = "Descripción (opcional)"
                maxLines = 3
            }

        linearLayout.addView(nombreInput)
        linearLayout.addView(descripcionInput)

        AlertDialog
            .Builder(context)
            .setTitle("Añadir Nuevo Item")
            .setView(linearLayout)
            .setPositiveButton("Añadir") { dialog, _ ->
                val nombre = nombreInput.text.toString()
                val descripcion = descripcionInput.text.toString().ifBlank { null }

                if (nombre.isNotBlank()) {
                    viewModel.crearElemento(nombre, descripcion)
                } else {
                    Toast.makeText(context, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }.setNegativeButton("Cancelar") { dialog, _ ->
                dialog.cancel()
            }.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
