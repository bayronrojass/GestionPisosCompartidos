package com.example.gestionpisoscompartidos.ui.listas

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs

// import androidx.navigation.NavController

class Listas : Fragment() {
    private val args: ListasArgs by navArgs()

    private val viewModel: ListasViewModel by viewModels {
        ListasViewModelFactory(args.casaId)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View =
        ComposeView(requireContext()).apply {
            setContent {
                // Aquí la UI (ListaScreen) solo avisa de que hubo un clic
                ListaScreen(
                    viewModel = viewModel,
                    onNavigateToItem = { listaId, listaNombre ->

                        // --- LÓGICA DE NAVEGACIÓN (Igual que en Login) ---
                        Log.d("Navegación", "Intentando navegar a lista: $listaNombre")

                        try {
                            // 1. Definimos la acción (nombre generado por el nav_graph.xml)
                            val action =
                                ListasDirections.actionListaDeListasFragmentToItemFragment(
                                    listaId = listaId,
                                    listaNombre = listaNombre,
                                    casaNombre = args.casaNombre,
                                )

                            // 2. EJECUTAMOS LA NAVEGACIÓN
                            // Usamos 'this@Listas' para asegurarnos de usar el controlador del FRAGMENTO.
                            // Si usas solo 'findNavController()', intenta usar el de la vista (ComposeView)
                            // que aún no está conectada y falla silenciosamente.
                            this@Listas.findNavController().navigate(action)
                        } catch (e: Exception) {
                            Log.e("Navegación", "Error crítico al navegar: ${e.message}")
                            e.printStackTrace()
                        }
                    },
                )
            }
        }
}
