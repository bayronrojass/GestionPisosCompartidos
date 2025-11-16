package com.tuapp.utils

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.fragment.app.Fragment

class ImagePicker(
    private val fragment: Fragment,
    private val onImagePicked: (bitmap: Bitmap?, uri: Uri?) -> Unit,
) {
    private lateinit var pickImageLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher: ActivityResultLauncher<String>

    fun init() {
        permissionLauncher =
            fragment.registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
                if (granted) {
                    openGallery()
                } else {
                    Toast.makeText(fragment.requireContext(), "Permiso denegado", Toast.LENGTH_SHORT).show()
                }
            }

        pickImageLauncher =
            fragment.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val uri = result.data?.data
                    if (uri != null) {
                        val bitmap =
                            try {
                                if (Build.VERSION.SDK_INT >= 28) {
                                    val source = ImageDecoder.createSource(fragment.requireContext().contentResolver, uri)
                                    ImageDecoder.decodeBitmap(source)
                                } else {
                                    MediaStore.Images.Media.getBitmap(fragment.requireContext().contentResolver, uri)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                null
                            }
                        onImagePicked(bitmap, uri)
                    }
                }
            }
    }

    fun pickPhoto() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.READ_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(
                    fragment.requireContext(),
                    permission,
                ) != PermissionChecker.PERMISSION_GRANTED
            ) {
                permissionLauncher.launch(permission)
                return
            }
        }
        openGallery()
    }

    private fun openGallery() {
        val intent =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Intent(MediaStore.ACTION_PICK_IMAGES).apply { type = "image/*" }
            } else {
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            }
        pickImageLauncher.launch(intent)
    }
}
