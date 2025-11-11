package com.example.gestionpisoscompartidos.ui.piso.listaPisos

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gestionpisoscompartidos.data.SessionManager
import com.example.gestionpisoscompartidos.data.remote.NetworkModule
import com.example.gestionpisoscompartidos.data.repository.repositories.RepositoryCasa
import com.example.gestionpisoscompartidos.databinding.FragmentListaCasasBinding
import com.example.gestionpisoscompartidos.model.JoinCasaRequest
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.coroutines.launch
import org.json.JSONObject

class ListaCasas : Fragment() {
    private var _binding: FragmentListaCasasBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ListaCasasViewModel by viewModels()
    private val args: ListaCasasArgs by navArgs()
    private lateinit var casasAdapter: CasasAdapter

    // --- DEPENDENCIAS AÑADIDAS ---
    private lateinit var sessionManager: SessionManager
    private lateinit var repositoryCasa: RepositoryCasa

    // Prepara el launcher para el resultado del escáner
    private val qrScannerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val intentResult = IntentIntegrator.parseActivityResult(result.resultCode, result.data)
            if (intentResult.contents != null) {
                handleQrResult(intentResult.contents)
            } else {
                Toast.makeText(context, "Escaneo cancelado", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inicializa las dependencias
        sessionManager = SessionManager(requireContext().applicationContext)
        repositoryCasa = RepositoryCasa(NetworkModule.casaApiService)
    }

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
                Log.d("ListaCasasFragment", "Navegando a DashBoard de casaId: ${casaSeleccionada.id}")
                val action =
                    ListaCasasDirections.actionListaCasasFragmentToCasaDashboardFragment(
                        casaSeleccionada.id,
                        casaSeleccionada.nombre,
                    )
                findNavController().navigate(action)
            }
        binding.recyclerViewCasas.adapter = casasAdapter
        binding.recyclerViewCasas.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setupViewModel() {
        viewModel.setCasas(args.casas.toList())

        viewModel.casas.observe(viewLifecycleOwner) { listaCasas ->
            casasAdapter.updateData(listaCasas)
            Log.d("ListaCasasFragment", "Lista de casas actualizada en el adaptador: ${listaCasas.size} elementos")
        }

        viewModel.mostrarMensajeVacio.observe(viewLifecycleOwner) { mostrar ->
            binding.tvMensajeVacio.isVisible = mostrar
            binding.recyclerViewCasas.isVisible = !mostrar
            Log.d("ListaCasasFragment", "Mostrar mensaje vacío: $mostrar")
        }
    }

    private fun setupListeners() {
        binding.fabCrearCasa.setOnClickListener {
            val action = ListaCasasDirections.actionListaCasasFragmentToCrearPisoFragment()
            findNavController().navigate(action)
        }

        // --- LISTENERS NUEVOS AÑADIDOS ---

        binding.btnVerInvitaciones.setOnClickListener {
            val action = ListaCasasDirections.actionListaCasasFragmentToInvitacionesFragment()
            findNavController().navigate(action)
        }

        binding.btnEscanearQr.setOnClickListener {
            iniciarEscanerQr()
        }
    }

    // --- FUNCIONES NUEVAS AÑADIDAS ---

    // Inicia la actividad del escáner (ZXing)
    private fun iniciarEscanerQr() {
        // (Recuerda: Ya añadiste el permiso de CÁMARA al Manifest)
        val integrator = IntentIntegrator.forSupportFragment(this)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
        integrator.setPrompt("Escanea el QR del piso")
        integrator.setBeepEnabled(true)
        qrScannerLauncher.launch(integrator.createScanIntent())
    }

    // Maneja el resultado del QR
    private fun handleQrResult(qrData: String) {
        try {
            // Parsea el JSON que generó el Anfitrión
            val json = JSONObject(qrData)
            val action = json.optString("action")

            if (action == "join_casa") {
                val casaId = json.getLong("casaId")

                val miId = sessionManager.fetchCurrentUserId()
                if (miId == -1L) {
                    Toast.makeText(context, "Error: Inicia sesión antes de unirte", Toast.LENGTH_LONG).show()
                    return
                }

                // Llama al backend (endpoint inseguro /join)
                lifecycleScope.launch {
                    try {
                        val token = sessionManager.fetchAuthToken() ?: ""
                        val request = JoinCasaRequest(usuarioId = miId)

                        val response = repositoryCasa.joinCasa(token, casaId, request)

                        if (response.isSuccessful) {
                            Toast.makeText(context, "¡Te has unido al piso!", Toast.LENGTH_LONG).show()
                            // TODO: Recargar la lista de pisos
                        } else {
                            Toast.makeText(context, "Error al unirse: ${response.errorBody()?.string()}", Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error de red: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                Toast.makeText(context, "QR no válido", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error al leer el QR", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
