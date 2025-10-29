package com.example.gestionpisoscompartidos.ui.piso.listaPisos

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
import com.example.gestionpisoscompartidos.databinding.FragmentListaCasasBinding

class ListaCasas : Fragment() {
    private var _binding: FragmentListaCasasBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ListaCasasViewModel by viewModels()
    private val args: ListaCasasArgs by navArgs()
    private lateinit var casasAdapter: CasasAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentListaCasasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupViewModel()
        setupListeners()
    }

    private fun setupRecyclerView() {
        casasAdapter =
            CasasAdapter(emptyList()) { casaSeleccionada ->
                // Acción al hacer clic en una casa
                Toast.makeText(context, "Has seleccionado: ${casaSeleccionada.nombre}", Toast.LENGTH_SHORT).show()
                Log.d("ListaCasasFragment", "Navegando a listas de casaId: ${casaSeleccionada.id}")
                val action =
                    ListaCasasDirections.actionListaCasasFragmentToListaDeListasFragment(
                        casaSeleccionada.id,
                        casaSeleccionada.nombre,
                    )
                findNavController().navigate(action)
            }
        binding.recyclerViewCasas.adapter = casasAdapter
    }

    private fun setupViewModel() {
        // Pasa la lista recibida como argumento al ViewModel
        // Convertimos el Array<Casa> de Safe Args a List<Casa>
        viewModel.setCasas(args.casas.toList())

        // Observa la lista de casas
        viewModel.casas.observe(viewLifecycleOwner) { listaCasas ->
            casasAdapter.updateData(listaCasas)
            Log.d("ListaCasasFragment", "Lista de casas actualizada en el adaptador: ${listaCasas.size} elementos")
        }

        // Observa si se debe mostrar el mensaje de lista vacía
        viewModel.mostrarMensajeVacio.observe(viewLifecycleOwner) { mostrar ->
            binding.tvMensajeVacio.isVisible = mostrar
            binding.recyclerViewCasas.isVisible = !mostrar
            Log.d("ListaCasasFragment", "Mostrar mensaje vacío: $mostrar")
        }
    }

    private fun setupListeners() {
        binding.fabCrearCasa.setOnClickListener {
            // Navegar a la pantalla de crear casa
            val action = ListaCasasDirections.actionListaCasasFragmentToCrearPisoFragment()
            findNavController().navigate(action)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Limpiar binding para evitar fugas de memoria
    }
}
