package com.example.gestionpisoscompartidos.ui.piso.crearPiso

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.gestionpisoscompartidos.R
import com.example.gestionpisoscompartidos.ui.piso.crearCasa.CrearCasaViewModel
import com.tuapp.utils.ImagePicker
import kotlinx.coroutines.launch

class CrearCasa : Fragment() {
    private val viewModel: CrearCasaViewModel by viewModels()
    private lateinit var editTextName: EditText
    private lateinit var descriptionTextName: EditText
    private lateinit var createFlatButton: Button

    private lateinit var selectImageButton: Button
    private lateinit var selectedImage: ImageView
    private lateinit var imagePicker: ImagePicker

    var pickedPhoto: Uri? = null
    var pickedBitMap: Bitmap? = null

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        editTextName = view.findViewById<EditText>(R.id.edit_text_name)
        createFlatButton = view.findViewById<Button>(R.id.button_create_flat)
        selectImageButton = view.findViewById<Button>(R.id.button_select_image)
        selectedImage = view.findViewById<ImageView>(R.id.image_selected)
        descriptionTextName = view.findViewById<EditText>(R.id.edit_text_description)

        selectedImage.scaleType = ImageView.ScaleType.CENTER_CROP
        selectedImage.adjustViewBounds = true

        updateButtonState(editTextName.text?.toString() ?: "", pickedPhoto)

        imagePicker =
            ImagePicker(this) { bitmap, uri ->
                if (bitmap != null) selectedImage.setImageBitmap(bitmap)
                updateButtonState(editTextName.text.trim().toString(), uri)
            }

        imagePicker.init()

        editTextName.addTextChangedListener(
            object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    val apartmentName = s?.toString() ?: ""
                    updateButtonState(apartmentName, pickedPhoto)
                    showError(apartmentName)
                }

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int,
                ) {}

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int,
                ) {}
            },
        )

        selectImageButton.setOnClickListener {
            imagePicker.pickPhoto()
        }

        createFlatButton.setOnClickListener {
            lifecycleScope.launch {
                val success =
                    viewModel.CrearCasa(
                        editTextName.text.toString(),
                        descriptionTextName.text.toString(),
                        pickedPhoto,
                    )
                if (success) {
                    Toast.makeText(requireContext(), "Piso creado", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(requireContext(), "Error creando piso", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    companion object {
        fun newInstance() = CrearCasa()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = inflater.inflate(R.layout.fragment_crear_piso, container, false)

    private fun updateButtonState(
        apartmentName: String,
        photoUri: Uri?,
    ) {
        val isEnabled = viewModel.buttonConditions(apartmentName)
        createFlatButton.isEnabled = isEnabled
        createFlatButton.isClickable = isEnabled

        if (isEnabled) {
            createFlatButton.setBackgroundColor(resources.getColor(R.color.purple_500))
        } else {
            createFlatButton.setBackgroundColor(resources.getColor(R.color.black))
        }
    }

    private fun showError(apartmentName: String) {
        if (viewModel.nameNull(apartmentName)) {
            editTextName.error = "Introduzca un nombre para el piso"
        } else {
            editTextName.error = null
        }
    }
}
