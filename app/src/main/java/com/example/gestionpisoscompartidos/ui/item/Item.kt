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
import androidx.navigation.fragment.navArgs // Importa navArgs
import com.example.gestionpisoscompartidos.databinding.FragmentItemBinding // Importa el binding

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
            // TODO: Mostrar diálogo o navegar a pantalla para añadir item
            Toast.makeText(context, "TODO: Añadir item", Toast.LENGTH_SHORT).show()
            // Por ejemplo, mostrar un AlertDialog con un EditText
            // o navegar a un AddItemFragment
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
