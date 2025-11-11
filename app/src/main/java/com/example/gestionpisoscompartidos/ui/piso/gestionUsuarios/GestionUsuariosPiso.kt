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
import android.graphics.Bitmap
import android.graphics.Color
import android.widget.ImageView
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

class GestionUsuariosPiso : Fragment() {
    // Prepara la Factory
    private lateinit var viewModelFactory: GestionUsuariosPisoViewModelFactory

    private val viewModel: GestionUsuariosPisoViewModel by viewModels { viewModelFactory }

    // Declaración de Vistas
    private lateinit var miembrosAdapter: MiembrosPisoAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var buttonInviteQr: Button
    private lateinit var buttonInviteEmail: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val context = requireContext().applicationContext
        val sessionManager = SessionManager(context)

        val pisoRepository = RepositoryCasa(NetworkModule.casaApiService)
        val invitacionRepository = RepositoryInvitacion(NetworkModule.invitacionApiService)

        viewModelFactory =
            GestionUsuariosPisoViewModelFactory(
                pisoRepository,
                invitacionRepository,
                sessionManager,
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

        android.util.Log.d("GestionPiso", "ID del piso cargado: $pisoId")

        if (pisoId == 0L) {
            Toast.makeText(requireContext(), "Error FATAL: ID de piso no encontrado", Toast.LENGTH_LONG).show()
        } else {
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
        buttonInviteEmail = view.findViewById(R.id.button_invite_email)

        miembrosAdapter =
            MiembrosPisoAdapter { miembro ->
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
            val pisoId = arguments?.getLong("PISO_ID") ?: 0L
            if (pisoId == 0L) {
                Toast.makeText(requireContext(), "Error: No se ha cargado el ID del piso", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val qrData = "{\"action\":\"join_casa\", \"casaId\":$pisoId}"

            try {
                val qrBitmap = generarQrBitmap(qrData)
                mostrarQrEnDialogo(qrBitmap)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error al generar QR", Toast.LENGTH_SHORT).show()
            }
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

    private fun generarQrBitmap(data: String): Bitmap {
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, 512, 512) // 512x512 píxeles
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bmp.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }
        return bmp
    }

    private fun mostrarQrEnDialogo(bitmap: Bitmap) {
        val imageView = ImageView(requireContext())
        imageView.setImageBitmap(bitmap)
        imageView.setPadding(40, 40, 40, 40)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Invitar con QR")
            .setMessage("Pídele a tu amigo que escanee este código.")
            .setView(imageView)
            .setPositiveButton("Cerrar", null)
            .show()
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
