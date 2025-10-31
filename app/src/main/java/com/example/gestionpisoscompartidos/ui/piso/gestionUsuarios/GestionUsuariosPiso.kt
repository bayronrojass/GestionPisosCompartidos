package com.example.gestionpisoscompartidos.ui.piso.gestionUsuarios

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gestionpisoscompartidos.R
import com.example.gestionpisoscompartidos.data.SessionManager
import com.example.gestionpisoscompartidos.data.remote.NetworkModule
import com.example.gestionpisoscompartidos.data.repository.repositories.RepositoryCasa
import com.example.gestionpisoscompartidos.data.repository.repositories.RepositoryInvitacion
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class GestionUsuariosPiso : Fragment() {

    // Prepara la Factory
    private lateinit var viewModelFactory: GestionUsuariosPisoViewModelFactory

    // ¡USA LA FACTORY! Esto evita el crash.
    private val viewModel: GestionUsuariosPisoViewModel by viewModels { viewModelFactory }

    // Declaración de Vistas
    private lateinit var miembrosAdapter: MiembrosPisoAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var buttonInviteQr: Button
    private lateinit var buttonInviteEmail: Button // Botón añadido

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializa tus dependencias (idealmente con Hilt/Koin)
        val context = requireContext().applicationContext
        val sessionManager = SessionManager(context)

        // Asumo que tienes un objeto 'RetrofitClient' que expone las APIs
        // y un 'RepositoryCasa'
        val pisoRepository = RepositoryCasa(NetworkModule.casaApiService)
        val invitacionRepository = RepositoryInvitacion(NetworkModule.invitacionApiService)

        // Crea la factory con las dependencias
        viewModelFactory = GestionUsuariosPisoViewModelFactory(
            pisoRepository,
            invitacionRepository,
            sessionManager
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? = inflater.inflate(R.layout.fragment_gestion_usuarios_piso, container, false)

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Vincula las Vistas
        setupViews(view)

        // 2. Carga los datos
        // Asume que el ID del piso se pasa en los argumentos del Fragment
        val pisoId = arguments?.getLong("PISO_ID") ?: 0L
        if (pisoId == 0L) {
            Toast.makeText(requireContext(), "Error: ID de piso no encontrado", Toast.LENGTH_LONG).show()
        } else {
            // ¡Llama al ViewModel para que cargue los miembros!
            viewModel.loadData(pisoId)
        }

        // 3. Configura los Listeners
        setupListeners()

        // 4. Inicia los observadores
        setupObservers()
    }

    private fun setupViews(view: View) {
        recyclerView = view.findViewById(R.id.recycler_view_members)
        buttonInviteQr = view.findViewById(R.id.button_invite_qr_main)
        // No hat boton de invitacion por email
        buttonInviteEmail = view.findViewById(R.id.button_invite_qr_main)

        miembrosAdapter = MiembrosPisoAdapter { miembro ->
            mostrarDialogoDeConfirmacion(miembro)
        }
        recyclerView.adapter = miembrosAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setupListeners() {
        buttonInviteEmail.setOnClickListener {
            mostrarDialogoInvitarEmail()
        }

        buttonInviteQr.setOnClickListener {
            val currentPisoId = "piso123" // TODO: Usar el pisoId real
            Toast.makeText(requireContext(), "Navegar a generar QR para piso: $currentPisoId", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observa la lista de miembros
                launch {
                    viewModel.miembros.collect { miembrosList ->
                        miembrosAdapter.submitList(miembrosList)
                    }
                }

                // Observa los resultados de las acciones (invitar, eliminar)
                launch {
                    viewModel.accionResult.collect { resultado ->
                        resultado?.let {
                            Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                            viewModel.clearAccionResult() // Limpia el mensaje
                        }
                    }
                }
            }
        }
    }

    /**
     * Muestra el diálogo para invitar por email.
     */
    private fun mostrarDialogoInvitarEmail() {
        val input = EditText(requireContext())
        input.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        input.hint = "email@ejemplo.com"
        input.setPadding(60, 40, 60, 40) // Padding simple

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Invitar por Email")
            .setView(input) // Añade el EditText al diálogo
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Enviar") { dialog, _ ->
                val email = input.text.toString()
                if (email.isNotBlank()) {
                    viewModel.enviarInvitacion(email)
                } else {
                    Toast.makeText(requireContext(), "El email no puede estar vacío", Toast.LENGTH_SHORT).show()
                }
            }.show()
    }

    private fun mostrarDialogoDeConfirmacion(miembro: MiembroPiso) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Confirmar eliminación")
            .setMessage("¿Estás seguro de que quieres eliminar a ${miembro.nombre} del piso?")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Eliminar") { dialog, _ ->
                viewModel.removeMiembro(miembro.id)
            }.show()
    }
}