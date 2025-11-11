package com.example.gestionpisoscompartidos.ui.invitaciones

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gestionpisoscompartidos.data.SessionManager
import com.example.gestionpisoscompartidos.data.remote.NetworkModule
import com.example.gestionpisoscompartidos.data.repository.repositories.RepositoryInvitacion
import com.example.gestionpisoscompartidos.databinding.FragmentInvitacionesBinding // Importa tu ViewBinding
import kotlinx.coroutines.launch

class InvitacionesFragment : Fragment() {
    private var _binding: FragmentInvitacionesBinding? = null
    private val binding get() = _binding!!

    // Prepara la Factory para inyectar las dependencias
    private lateinit var viewModelFactory: InvitacionesViewModelFactory
    private val viewModel: InvitacionesViewModel by viewModels { viewModelFactory }

    private lateinit var invitacionesAdapter: InvitacionesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializa las dependencias
        val context = requireContext().applicationContext
        val sessionManager = SessionManager(context)
        val repository = RepositoryInvitacion(NetworkModule.invitacionApiService)

        // Crea la Factory
        viewModelFactory = InvitacionesViewModelFactory(repository, sessionManager)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentInvitacionesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()

        // Carga inicial de datos
        viewModel.fetchMisInvitaciones()
    }

    private fun setupRecyclerView() {
        invitacionesAdapter =
            InvitacionesAdapter(
                onAcceptClick = { invitacion ->
                    // Delega la lógica al ViewModel
                    viewModel.aceptarInvitacion(invitacion.id)
                },
                onRejectClick = { invitacion ->
                    // Delega la lógica al ViewModel
                    viewModel.rechazarInvitacion(invitacion.id)
                },
            )
        binding.recyclerViewInvitaciones.apply {
            adapter = invitacionesAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observa la lista de invitaciones
                launch {
                    viewModel.invitaciones.collect { listaInvitaciones ->
                        invitacionesAdapter.submitList(listaInvitaciones)
                        // Muestra un mensaje si la lista está vacía
                        binding.tvMensajeVacio.isVisible = listaInvitaciones.isEmpty()
                        binding.recyclerViewInvitaciones.isVisible = listaInvitaciones.isNotEmpty()
                    }
                }

                // Observa el estado de carga
                launch {
                    viewModel.isLoading.collect { isLoading ->
                        binding.progressBar.isVisible = isLoading
                        // Oculta la lista y el mensaje de vacío mientras carga
                        if (isLoading) {
                            binding.recyclerViewInvitaciones.isVisible = false
                            binding.tvMensajeVacio.isVisible = false
                        }
                    }
                }

                // Observa los errores
                launch {
                    viewModel.error.collect { errorMsg ->
                        if (errorMsg != null) {
                            Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show()
                            binding.tvMensajeVacio.isVisible = true // Muestra mensaje de vacío si hay error
                            viewModel.clearError() // Limpia el error para que no se repita
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
