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
import com.example.gestionpisoscompartidos.data.SessionManager

/**
 * Fragmento de la pantalla de inicio de sesión.
 */
class Login : Fragment() {
    companion object {
        fun newInstance() = Login()
    }

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var sessionManager: SessionManager

    // Inicialización del ViewModel (tu código está bien)
    private val viewModel: LoginViewModel by viewModels {
        val apiService = NetworkModule.loginApiService
        val repository = RepositoryLogin(apiService)
        LoginViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Usamos applicationContext para que sea seguro
        sessionManager = SessionManager(requireContext().applicationContext)
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
        // ... (tu código de listeners está bien)
    }

    private fun setupObservers() {
        // ... (tu código de observers está bien)
    }

    /**
     * Maneja la respuesta exitosa del login.
     */
    private fun handleLoginSuccess(response: LoginResponse) {
        Toast.makeText(context, "¡Bienvenido, ${response.user.nombre}!", Toast.LENGTH_SHORT).show()

       sessionManager.saveAuthData(
            token = response.authToken,
            userId = response.user.id,
            email = response.user.correo
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