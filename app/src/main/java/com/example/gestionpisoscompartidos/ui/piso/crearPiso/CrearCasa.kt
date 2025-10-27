package com.example.gestionpisoscompartidos.ui.piso.crearPiso

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.gestionpisoscompartidos.R
import kotlinx.coroutines.launch
import com.example.gestionpisoscompartidos.ui.piso.crearCasa.CrearCasaViewModel

class CrearCasa : Fragment() {
    private val viewModel: CrearCasaViewModel by viewModels()
    private lateinit var editTextName: EditText
    private lateinit var descriptionTextName: EditText
    private lateinit var createFlatButton: Button

    private lateinit var selectImageButton: Button
    private lateinit var selectedImage: ImageView

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
            pickPhoto()
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
                    Toast.makeText(requireContext(), "Flat created", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(requireContext(), "Error creating flat", Toast.LENGTH_LONG).show()
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

    private fun pickPhoto() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val intent = Intent(MediaStore.ACTION_PICK_IMAGES)
            intent.type = "image/*"
            startActivityForResult(intent, 2)
        } else {
            val permission = android.Manifest.permission.READ_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(requireContext(), permission)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(permission), 1)
            } else {
                openGallery()
            }
        }
    }

    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent, 2)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery()
            } else {
                android.widget.Toast
                    .makeText(
                        requireContext(),
                        "Se necesita permiso para acceder a la galería",
                        android.widget.Toast.LENGTH_SHORT,
                    ).show()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
    ) {
        if (requestCode == 2 && resultCode == Activity.RESULT_OK && data != null) {
            pickedPhoto = data.data
            if (pickedPhoto != null) {
                try {
                    if (Build.VERSION.SDK_INT >= 28) {
                        val source = ImageDecoder.createSource(requireContext().contentResolver, pickedPhoto!!)
                        pickedBitMap = ImageDecoder.decodeBitmap(source)
                    } else {
                        pickedBitMap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, pickedPhoto!!)
                    }
                    selectedImage.setImageBitmap(pickedBitMap)
                } catch (e: Exception) {
                    e.printStackTrace()
                    android.widget.Toast
                        .makeText(requireContext(), "Error loading image", android.widget.Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
        updateButtonState(editTextName.text.trim().toString(), pickedPhoto)
        super.onActivityResult(requestCode, resultCode, data)
    }
}
