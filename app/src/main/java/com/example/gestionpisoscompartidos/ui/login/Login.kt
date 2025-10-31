package com.example.gestionpisoscompartidos.ui.login

import android.os.Bundle
import android.util.Log
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
import androidx.navigation.fragment.findNavController
// 1. IMPORTA TU SESSION MANAGER
import com.example.gestionpisoscompartidos.data.SessionManager3

/**
 * Fragmento de la pantalla de inicio de sesión.
 */
class Login : Fragment() {
    companion object {
        fun newInstance() = Login()
    }

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var sessionManager: SessionManager3

    // Inicialización del ViewModel (tu código está bien)
    private val viewModel: LoginViewModel by viewModels {
        val apiService = NetworkModule.loginApiService
        val repository = RepositoryLogin(apiService)
        LoginViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Usamos applicationContext para que sea seguro
        sessionManager = SessionManager3(requireContext().applicationContext)
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

        sessionManager.saveAuthData(
            token = response.authToken,
            userId = response.user.id,
            email = response.user.correo,
        )

        // 5. Ahora, maneja la navegación
        if (response.flats.isNotEmpty()) {
            // El usuario tiene pisos, vamos a la lista de selección
            Log.d("LoginSuccess", "Pisos encontrados: ${response.flats.size}")

            val casasArray = response.flats.toTypedArray()
            val action = LoginDirections.actionLoginFragmentToListaCasasFragment(casasArray)
            findNavController().navigate(action)
        } else {
            // El usuario no tiene pisos, vamos a la pantalla de "Crear o Unirse"
            Log.d("LoginSuccess", "La lista 'flats' está vacía.")
            Toast.makeText(context, "No tienes pisos asignados.", Toast.LENGTH_LONG).show()

            // TODO: Cambia esto por la navegación real a tu pantalla de "Crear/Unirse"
            // val action = LoginDirections.actionLoginFragmentToUnirsePisoFragment()
            // findNavController().navigate(action)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
