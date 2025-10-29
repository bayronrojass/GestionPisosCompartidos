package com.example.gestionpisoscompartidos.ui.listas

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.gestionpisoscompartidos.databinding.FragmentListasBinding

class Listas : Fragment() {
    private var _binding: FragmentListasBinding? = null
    private val binding get() = _binding!!

    private val args: ListasArgs by navArgs() // Para recibir casaId
    private val viewModel: ListasViewModel by viewModels {
        ListasViewModelFactory(args.casaId) // Pasa el casaId a la Factory
    }
    private lateinit var listasAdapter: ListasAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentListasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("ListaDeListasFragment", "Recibido casaId: ${args.casaId}") // Verifica ID

        setupRecyclerView()
        setupViewModelObservers()
        setupListeners()
    }

    private fun setupRecyclerView() {
        listasAdapter =
            ListasAdapter(emptyList()) { listaSeleccionada ->
                // Navegar a ItemFragment pasando el ID de la lista
                Log.d("ListasFragment", "Navegando a ItemFragment con listaId: ${listaSeleccionada.id}")
                val action =
                    ListasDirections.actionListaDeListasFragmentToItemFragment(
                        listaSeleccionada.id,
                        listaSeleccionada.nombre,
                        args.casaNombre,
                    )
                findNavController().navigate(action)
            }
        binding.recyclerViewListas.adapter = listasAdapter
    }

    private fun setupViewModelObservers() {
        // Observa la lista de casas
        viewModel.listas.observe(viewLifecycleOwner) { listaListas ->
            listasAdapter.updateData(listaListas)
            Log.d("ListaDeListasFragment", "Datos del adaptador actualizados: ${listaListas.size} listas")

            // Si NO estamos cargando, actualiza la visibilidad
            if (viewModel.isLoading.value == false) {
                binding.recyclerViewListas.isVisible = listaListas.isNotEmpty()
                binding.tvMensajeVacioListas.isVisible = listaListas.isEmpty()
            }
        }

        // Observa el estado de carga
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBarListas.isVisible = isLoading

            if (isLoading) {
                // Oculta todo lo demás mientras carga
                binding.recyclerViewListas.isVisible = false
                binding.tvMensajeVacioListas.isVisible = false
            } else {
                // CUANDO TERMINA de cargar, decidimos qué mostrar
                // Re-evaluamos la lista que (probablemente) ya tenemos
                val listas = viewModel.listas.value
                if (listas != null) {
                    binding.recyclerViewListas.isVisible = listas.isNotEmpty()
                    binding.tvMensajeVacioListas.isVisible = listas.isEmpty()
                } else {
                    // Si las listas son nulas (ej. error antes de cargar)
                    binding.recyclerViewListas.isVisible = false
                    binding.tvMensajeVacioListas.isVisible = true
                }
            }
            Log.d("ListaDeListasFragment", "isLoading: $isLoading")
        }

        // Observa los errores
        viewModel.error.observe(viewLifecycleOwner) { errorMsg ->
            if (errorMsg != null) {
                Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                Log.e("ListaDeListasFragment", "Error: $errorMsg")

                // Si hay un error, fuerza la vista de mensaje vacío
                binding.progressBarListas.isVisible = false
                binding.recyclerViewListas.isVisible = false
                binding.tvMensajeVacioListas.isVisible = true
            }
        }

        // El observador 'mostrarMensajeVacio' ya no existe
    }

    private fun setupListeners() {
        binding.fabCrearLista.setOnClickListener {
            // TODO: Navegar a una futura pantalla CrearListaFragment
            Toast.makeText(context, "TODO: Ir a crear lista", Toast.LENGTH_SHORT).show()
            // val action = ListaDeListasFragmentDirections.actionListaDeListasFragmentToCrearListaFragment(args.casaId)
            // findNavController().navigate(action)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
