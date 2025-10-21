package com.example.gestionpisoscompartidos.ui.piso.crearPiso

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.gestionpisoscompartidos.R
import kotlinx.coroutines.launch

class CrearPiso : Fragment() {
    private val viewModel: CrearPisoViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val view = inflater.inflate(R.layout.fragment_crear_piso, container, false)

        // Referencias a los elementos del layout
        val nombreInput = view.findViewById<EditText>(R.id.edit_text_name)
        val descripcionInput = view.findViewById<EditText>(R.id.edit_text_description)
        // val botonCrear = view.findViewById<Button>(R.id.button_crear_piso)
        val tvResultado = TextView(requireContext()) // O añade un TextView en XML si prefieres

        // Acción al pulsar el botón "Crear Piso"

        /*
        botonCrear.setOnClickListener {
            val nombre = nombreInput.text.toString().trim()
            val descripcion = descripcionInput.text.toString().trim()

            if (nombre.isEmpty()) {
                nombreInput.error = "Introduce un nombre"
                return@setOnClickListener
            }

            viewModel.crearPiso(nombre, descripcion, direccion = null)
        }
         */

        // Observa el resultado
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.estado.collect { estado ->
                tvResultado.text = estado
            }
        }

        return view
    }
}
