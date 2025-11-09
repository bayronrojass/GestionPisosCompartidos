package com.example.gestionpisoscompartidos.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.gestionpisoscompartidos.databinding.FragmentHome2Binding

class Home2 : Fragment() {
    private var _binding: FragmentHome2Binding? = null
    private val binding get() = _binding!!
    private val args: Home2Args by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentHome2Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        // --- CÓDIGO ACTUALIZADO ---
        // (He renombrado tus actions para que coincidan con 'Home2Directions')

        binding.btnVerListas.setOnClickListener {
            val action = Home2Directions.actionHome2ToListasFragment(args.casaId, args.casaNombre)
            findNavController().navigate(action)
        }

        binding.btnVerTareas.setOnClickListener {
            val action =
                Home2Directions.actionHome2ToTareasFragment(
                    args.casaId,
                    args.casaNombre,
                )
            findNavController().navigate(action)
        }

        binding.btnGestionPiso.setOnClickListener {
            val action = Home2Directions.actionHome2ToGestionUsuariosPisoFragment(args.casaId)
            findNavController().navigate(action)
        }
        /*
        binding.btnGestionInvitaciones.setOnClickListener {
            // Este fragmento (por ahora) no necesita argumentos
            val action = Home2Directions.actionHome2ToInvitacionesFragment()
            findNavController().navigate(action)
        }
         */
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
