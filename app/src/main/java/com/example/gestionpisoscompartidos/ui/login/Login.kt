package com.example.gestionpisoscompartidos.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.gestionpisoscompartidos.data.repository.repositories.RepositoryLogin
import com.example.gestionpisoscompartidos.databinding.FragmentLoginBinding
import com.example.gestionpisoscompartidos.data.remote.NetworkModule
import com.example.gestionpisoscompartidos.model.LoginResponse

/**
 * Fragmento de la pantalla de inicio de sesión.
 */
class Login : Fragment() {
    // Usar View Binding para acceder a los elementos del layout
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    // Inicializar el ViewModel (asumiendo que tienes una ViewModelFactory
    // o un sistema de inyección como Hilt/Koin configurado).
    // Si no usas DI, tendrás que inicializar el repositorio y el ViewModel manualmente.
    // Ejemplo manual (simplificado, no recomendado en apps grandes):
    private val viewModel: LoginViewModel by viewModels {
        // 1. Obtener la API del módulo de red
        val apiService = NetworkModule.loginApiService

        // 2. Crear el Repositorio con la API
        val repository = RepositoryLogin(apiService)

        // 3. Devolver la fábrica con el Repositorio
        LoginViewModelFactory(repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        setupListeners()
        setupObservers()
    }

    private fun setupListeners() {
        binding.btnIniciar.setOnClickListener {
            val email = binding.etUsuario.text.toString()
            val password = binding.etContrasena.text.toString()
            viewModel.login(email, password)
        }

        // Listener para el botón de registro
        binding.tvRegistrate.setOnClickListener {
            // TODO: Navegar a la pantalla de registro
            Toast.makeText(context, "Ir a Registro", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupObservers() {
        // Observar el estado de carga
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // Muestra/oculta un ProgressBar, si lo tienes en tu layout
            binding.btnIniciar.isEnabled = !isLoading
            // Puedes añadir un ProgressBar en el layout y controlarlo aquí:
            // binding.progressBar.isVisible = isLoading
        }

        // Observar los errores
        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            if (errorMessage != null) {
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                // Limpiar el error si es necesario
                // viewModel.clearError()
            }
        }

        // Observar el resultado del login
        viewModel.loginResult.observe(viewLifecycleOwner) { response ->
            if (response != null) {
                handleLoginSuccess(response)
                viewModel.clearLoginResult() // Limpiar el LiveData tras su uso
            }
        }
    }

    /**
     * Maneja la respuesta exitosa del login.
     */
    private fun handleLoginSuccess(response: LoginResponse) {
        Toast.makeText(context, "¡Bienvenido, ${response.user.nombre}!", Toast.LENGTH_SHORT).show()

        // 1. Guardar el token de autenticación (SharedPreferences/DataStore)
        // SharedPreferencesManager.saveToken(response.authToken)

        // 2. Manejar la lista de pisos
        if (response.flats.isNotEmpty()) {
            // TODO: Navegar a una pantalla de selección de piso
            // O si solo tiene uno, seleccionarlo automáticamente e iniciar el servicio MQTT
            // startMqttService(response.flats.first().id)
        } else {
            Toast.makeText(context, "No tienes pisos asignados.", Toast.LENGTH_LONG).show()
            // TODO: Navegar a una pantalla para crear/unirse a un piso
        }

        // TODO: Navegar a la pantalla principal (lista de tareas)
        // findNavController().navigate(R.id.action_loginFragment_to_tareasFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
