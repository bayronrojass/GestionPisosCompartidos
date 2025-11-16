package com.example.gestionpisoscompartidos.ui.piso.crearPiso

import android.net.Uri
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var imagenUri by remember { mutableStateOf<Uri?>(null) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val launcher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent(),
        ) { uri: Uri? ->
            imagenUri = uri
        }

    val isButtonEnabled = nombre.isNotBlank() && imagenUri != null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Casas") },
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

            // Campo Nombre
            Text("Nombre", color = Color(0xFF3F51B5), fontSize = 24.sp)
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Nombre del piso") },
//                colors = TextFieldDefaults.outlinedTextFieldColors(
//                    focusedBorderColor = Color(0xFF3F51B5),
//                    unfocusedBorderColor = Color.Gray
//                )
            )

            Spacer(Modifier.height(16.dp))

            // Campo Descripción
            Text("Descripción", color = Color(0xFF3F51B5), fontSize = 24.sp)
            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                placeholder = { Text("Descripción") },
//                colors = TextFieldDefaults.outlinedTextFieldColors(
//                    focusedBorderColor = Color(0xFF3F51B5),
//                    unfocusedBorderColor = Color.Gray
//                )
            )

            Spacer(Modifier.height(16.dp))

            // Selección de imagen
            Text("Foto", color = Color(0xFF3F51B5), fontSize = 24.sp)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 16.dp),
            ) {
                Button(
                    onClick = { launcher.launch("image/*") },
//                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.White),
                    elevation = ButtonDefaults.buttonElevation(4.dp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_insert_photo),
                        contentDescription = null,
                        tint = Color.Black,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Seleccionar Foto", color = Color.Black)
                }

                Spacer(Modifier.width(16.dp))

                imagenUri?.let { uri ->
                    AsyncImage(
                        model = uri,
                        contentDescription = null,
                        modifier =
                            Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop,
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Sección miembros
            Text(
                "Miembros",
                color = Color(0xFF3F51B5),
                fontSize = 24.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )

            Button(
                onClick = { /* QR */ },
//                colors = ButtonDefaults.buttonColors(backgroundColor = Color.White),
                elevation = ButtonDefaults.buttonElevation(4.dp),
                modifier = Modifier.align(Alignment.CenterHorizontally),
            ) {
                Text("Invitar usando QR", color = Color.Black)
                Spacer(Modifier.width(8.dp))
                Icon(
                    painter = painterResource(R.drawable.ic_qr_code),
                    contentDescription = null,
                    tint = Color.Black,
                )
            }

            Button(
                onClick = { /* Email */ },
//                colors = ButtonDefaults.buttonColors(backgroundColor = Color.White),
                elevation = ButtonDefaults.buttonElevation(4.dp),
                modifier = Modifier.align(Alignment.CenterHorizontally),
            ) {
                Text("Invitar por Email", color = Color.Black)
                Spacer(Modifier.width(8.dp))
                Icon(
                    painter = painterResource(R.drawable.ic_email),
                    contentDescription = null,
                    tint = Color.Black,
                )
            }

            Spacer(Modifier.height(32.dp))

            // Botón crear
            Button(
                onClick = {
                    // Llamar al ViewModel
                    scope.launch {
                        viewModel.CrearCasa(nombre, descripcion, imagenUri)
                    }
                },
                enabled = isButtonEnabled,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                //                colors = ButtonDefaults.buttonColors(
//                    backgroundColor = if (isButtonEnabled) Color(0xFF6A5ACD) else Color.Gray
//                ),
                elevation = ButtonDefaults.buttonElevation(8.dp),
            ) {
                Text("Crear Piso", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Preview
@Composable
fun CrearCasaScreenPreview() {
    CrearCasaScreen()
}
