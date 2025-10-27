package com.example.gestionpisoscompartidos.ui.piso.gestionUsuarios

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import com.example.gestionpisoscompartidos.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class GestionUsuariosPiso : Fragment() {
    private val viewModel: GestionUsuariosPisoViewModel by viewModels()
    private lateinit var miembrosAdapter: MiembrosPisoAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var buttonInviteQr: Button

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

        recyclerView = view.findViewById(R.id.recycler_view_members)
        buttonInviteQr = view.findViewById(R.id.button_invite_qr_main)

        miembrosAdapter =
            MiembrosPisoAdapter { miembro ->
                mostrarDialogoDeConfirmacion(miembro)
            }
        recyclerView.adapter = miembrosAdapter

        // Observa los miembros del ViewModel
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.miembros.collect { miembrosList ->
                    miembrosAdapter.submitList(miembrosList)
                }
            }
        }

        buttonInviteQr.setOnClickListener {
            val currentPisoId = "piso123"
            Toast.makeText(requireContext(), "Navegar a generar QR para piso: $currentPisoId", Toast.LENGTH_SHORT).show()
        }
    }

    private fun mostrarDialogoDeConfirmacion(miembro: MiembroPiso) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Confirmar eliminación")
            .setMessage("¿Estás seguro de que quieres eliminar a ${miembro.nombre} del piso?")
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }.setPositiveButton("Eliminar") { dialog, _ ->
                viewModel.removeMiembro(miembro.id)
            }.show()
    }
}
