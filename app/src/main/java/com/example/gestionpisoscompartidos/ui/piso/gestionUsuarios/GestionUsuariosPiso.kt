// com.example.gestionpisoscompartidos.ui.piso.gestionUsuarios.GestionUsuariosPiso.kt
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
// Asegúrate de que tu NavGraph y Safe Args estén configurados correctamente
// import com.example.gestionpisoscompartidos.ui.piso.gestionUsuarios.GestionUsuariosPisoDirections
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

        // Inicializa el adaptador
        miembrosAdapter =
            MiembrosPisoAdapter { miembro ->
                // Acción al hacer clic en eliminar miembro
                Toast.makeText(requireContext(), "Eliminar ${miembro.nombre}", Toast.LENGTH_SHORT).show()
                viewModel.removeMiembro(miembro.id)
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

        // Listener para el botón de invitar QR
        buttonInviteQr.setOnClickListener {
            // Aquí necesitarás el ID del piso actual.
            // Si este fragmento siempre se muestra dentro de un piso ya creado,
            // puedes pasarlo como argumento al fragmento.
            // Por ejemplo, si el ID del piso es "piso123"
            val currentPisoId = "piso123" // <-- ¡IMPORTANTE! Reemplaza esto con el ID real del piso
            // Asegúrate de que la acción exista en tu nav_graph.xml
            // y que CodigoQR reciba un argumento "pisoId"
            // val action = GestionUsuariosPisoDirections.actionGestionUsuariosPisoToCodigoQR(currentPisoId)
            // findNavController().navigate(action)
            Toast.makeText(requireContext(), "Navegar a generar QR para piso: $currentPisoId", Toast.LENGTH_SHORT).show()
        }
    }
}
