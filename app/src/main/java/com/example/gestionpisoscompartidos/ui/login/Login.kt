package com.example.gestionpisoscompartidos.ui.login

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.gestionpisoscompartidos.data.remote.NetworkModule
import com.example.gestionpisoscompartidos.data.repository.repositories.RepositoryLogin
import com.example.gestionpisoscompartidos.databinding.FragmentLoginBinding
import com.example.gestionpisoscompartidos.model.LoginResponse
import com.example.gestionpisoscompartidos.data.SessionManager
import com.example.gestionpisoscompartidos.model.Casa

class Login : Fragment() {
    companion object {
        fun newInstance() = Login()
    }

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var sessionManager: SessionManager

    private val viewModel: LoginViewModel by viewModels {
        val apiService = NetworkModule.loginApiService
        val repository = RepositoryLogin(apiService)
        LoginViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        binding.btnIniciar.setOnClickListener {
            val email =
                binding.etUsuario.text
                    .toString()
                    .trim()
            val password =
                binding.etContrasena.text
                    .toString()
                    .trim()
            viewModel.login(email, password)
        }

        binding.tvRegistrate.setOnClickListener {
            // TODO: Navegar a la pantalla de registro
            Toast.makeText(context, "Ir a Registro", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.btnIniciar.isEnabled = !isLoading
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            if (errorMessage != null) {
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            }
        }

        viewModel.loginResult.observe(viewLifecycleOwner) { response ->
            if (response != null) {
                handleLoginSuccess(response)
                viewModel.clearLoginResult()
            }
        }
    }

    private fun handleLoginSuccess(response: LoginResponse) {
        Toast.makeText(context, "¡Bienvenido, ${response.user.nombre}!", Toast.LENGTH_SHORT).show()

        sessionManager.saveAuthData(
            token = response.authToken,
            userId = response.user.id,
            email = response.user.correo,
        )

        if (response.flats.isNotEmpty()) {
            Log.d("LoginSuccess", "Pisos encontrados: ${response.flats.size}")

            val casasList: List<Casa> =
                response.flats.map { casaDto ->
                    Casa(
                        id = casaDto.id,
                        nombre = casaDto.nombre,
                        descripcion = casaDto.descripcion,
                        rutaImagen = null,
                        fechaCreacion = casaDto.fechaCreacion,
                    )
                }

            val casasArray = casasList.toTypedArray()

            val action = LoginDirections.actionLoginFragmentToListaCasasFragment(casasArray)
            findNavController().navigate(action)
        } else {
            Log.d("LoginSuccess", "La lista 'flats' está vacía.")
            Toast.makeText(context, "No tienes pisos asignados.", Toast.LENGTH_LONG).show()

            val emptyCasasArray = arrayOf<Casa>()

            val action = LoginDirections.actionLoginFragmentToListaCasasFragment(emptyCasasArray)
            findNavController().navigate(action)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
