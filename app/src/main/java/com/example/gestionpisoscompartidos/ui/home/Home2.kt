package com.example.gestionpisoscompartidos.ui.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

        binding.btnVerListas.setOnClickListener {
            // Pasa el array de casas que recibió del login
            val action = Home2Directions.actionHomeFragmentToListaCasasFragment(args.casas)
            findNavController().navigate(action)
        }

        binding.btnVerTareas.setOnClickListener {
            // Asumimos que operamos sobre la *primera* casa.
            // Idealmente, deberías navegar a 'listaCasasFragment' y que ESE fragment
            // devuelva el ID de la casa seleccionada para navegar a Tareas.
            // Por simplicidad, usamos la primera casa del array.
            if (args.casas.isNotEmpty()) {
                val casaId = args.casas[0].id
                val casaNombre = args.casas[0].nombre
                val action = Home2Directions.actionHomeFragmentToTareasFragment(casaId, casaNombre)
                findNavController().navigate(action)
            } else {
                // TODO: Navegar a "Crear Casa" si no hay casas
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
