package com.example.gestionpisoscompartidos.ui.piso.crearPiso

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.gestionpisoscompartidos.R
import com.example.gestionpisoscompartidos.ui.piso.crearCasa.CrearCasaViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearCasaScreen(viewModel: CrearCasaViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val launcher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent(),
        ) { uri: Uri? ->
            viewModel.updateImagenUri(uri)
        }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear Nuevo Piso") },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF6200EE),
                        titleContentColor = Color.White,
                    ),
            )
        },
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
        ) {
            Text(
                "¡Crea un nuevo piso!",
                color = Color(0xFF3F51B5),
                fontSize = 28.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(24.dp))

            Text(
                "Nombre",
                color = Color(0xFF3F51B5),
                fontSize = 24.sp,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            OutlinedTextField(
                value = uiState.nombre,
                onValueChange = { viewModel.updateNombre(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Nombre del piso") },
                isError = uiState.nombre.isBlank(),
                singleLine = true,
            )

            if (uiState.nombre.isBlank()) {
                Text(
                    "Introduzca un nombre para el piso",
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp),
                )
            }

            Spacer(Modifier.height(16.dp))

            // Campo Descripción
            Text(
                "Descripción",
                color = Color(0xFF3F51B5),
                fontSize = 24.sp,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            OutlinedTextField(
                value = uiState.descripcion,
                onValueChange = { viewModel.updateDescripcion(it) },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                placeholder = { Text("Descripción del piso") },
            )

            Spacer(Modifier.height(16.dp))

            Text(
                "Foto",
                color = Color(0xFF3F51B5),
                fontSize = 24.sp,
                modifier = Modifier.padding(bottom = 8.dp),
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 8.dp),
            ) {
                Button(
                    onClick = { launcher.launch("image/*") },
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color.Black,
                        ),
                    elevation = ButtonDefaults.buttonElevation(4.dp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_insert_photo),
                        contentDescription = "Seleccionar foto",
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Seleccionar Foto", color = Color.Black)
                }

                Spacer(Modifier.width(16.dp))

                uiState.imagenUri?.let { uri ->
                    AsyncImage(
                        model = uri,
                        contentDescription = "Imagen seleccionada",
                        modifier =
                            Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop,
                    )
                } ?: run {
                    Icon(
                        painter = painterResource(R.drawable.ic_insert_photo),
                        contentDescription = "Sin imagen",
                        modifier =
                            Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(8.dp)),
                        tint = Color.Gray,
                    )
                }
            }

            if (uiState.imagenUri == null) {
                Text(
                    "Seleccione una imagen para el piso",
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp),
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(
                "Invitar Miembros",
                color = Color(0xFF3F51B5),
                fontSize = 24.sp,
                textAlign = TextAlign.Center,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
            )

            Button(
                onClick = {
                },
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black,
                    ),
                elevation = ButtonDefaults.buttonElevation(4.dp),
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp, vertical = 4.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_qr_code),
                    contentDescription = "Invitar usando QR",
                    tint = Color.Black,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(12.dp))
                Text("Invitar usando QR", color = Color.Black)
            }

            Button(
                onClick = {
                },
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black,
                    ),
                elevation = ButtonDefaults.buttonElevation(4.dp),
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp, vertical = 4.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_email),
                    contentDescription = "Invitar por Email",
                    tint = Color.Black,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(12.dp))
                Text("Invitar por Email", color = Color.Black)
            }

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = {
                    scope.launch {
                        val success = viewModel.crearCasa()
                        if (success) {
                            Toast.makeText(context, "Casa creada con éxito", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Error al crear un piso", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                enabled = uiState.isButtonEnabled,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = if (uiState.isButtonEnabled) Color(0xFF6200EE) else Color.Gray,
                        contentColor = Color.White,
                    ),
                elevation = ButtonDefaults.buttonElevation(8.dp),
            ) {
                Text(
                    "Crear Piso",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            if (!uiState.isButtonEnabled) {
                Text(
                    "Complete todos los campos requeridos para crear el piso",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CrearCasaScreenPreview() {
    CrearCasaScreen()
}

@Preview(showBackground = true)
@Composable
fun CrearCasaScreenWithDataPreview() {
    CrearCasaScreen()
}
