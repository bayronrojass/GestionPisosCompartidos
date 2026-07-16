package es.mirumi.es.ui.registro

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import es.mirumi.es.data.SessionManager
import es.mirumi.es.data.remote.NetworkModule
import es.mirumi.es.data.repository.repositories.RepositoryRegistro
import es.mirumi.es.model.Casa
import es.mirumi.es.model.responses.LoginResponse
import es.mirumi.es.ui.navigation.Route
import es.mirumi.es.ui.theme.Burgundy
import es.mirumi.es.ui.theme.ButtonShape
import es.mirumi.es.ui.theme.Fondo
import es.mirumi.es.ui.theme.InputShape
import es.mirumi.es.ui.theme.LilaPrimary
import es.mirumi.es.ui.theme.TextoGris
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.builtins.ArraySerializer
import kotlinx.serialization.json.Json

@Composable
fun RegistroScreen(
    navController: NavController,
    sessionManager: SessionManager,
    modifier: Modifier = Modifier,
) {
    val viewModel: RegistroViewModel =
        viewModel(
            factory =
                RegistroViewModelFactory(
                    RepositoryRegistro(NetworkModule.registroApiService, LocalContext.current),
                ),
        )

    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var nombre by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var contrasenaConfirm by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmVisible by remember { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is RegistroUiState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }
            is RegistroUiState.Success -> {
                handleRegistroSuccess(state.response, sessionManager, navController)
                viewModel.resetState()
            }
            else -> {}
        }
    }

    RegistroScreenUI(
        modifier = modifier,
        nombre = nombre,
        correo = correo,
        contrasena = contrasena,
        contrasenaConfirm = contrasenaConfirm,
        passwordVisible = passwordVisible,
        confirmVisible = confirmVisible,
        isLoading = uiState is RegistroUiState.Loading,
        onNombreChange = { nombre = it },
        onCorreoChange = { correo = it },
        onContrasenaChange = { contrasena = it },
        onContrasenaConfirmChange = { contrasenaConfirm = it },
        onPasswordVisibilityToggle = { passwordVisible = !passwordVisible },
        onConfirmVisibilityToggle = { confirmVisible = !confirmVisible },
        onRegisterClick = { viewModel.register(nombre, correo, contrasena, contrasenaConfirm) },
        onBackToLoginClick = {
            // Try pop first (normal case: entered via login → registro). If the back stack is
            // empty (e.g. process death restored us directly into Registro, or an internal
            // popUpTo cleared the stack), fall back to an explicit navigate so the arrow is
            // never a dead button.
            if (!navController.popBackStack()) {
                navController.navigate(Route.InicioSesion.route) {
                    popUpTo(Route.Registro.route) { inclusive = true }
                }
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroScreenUI(
    modifier: Modifier = Modifier,
    nombre: String,
    correo: String,
    contrasena: String,
    contrasenaConfirm: String,
    passwordVisible: Boolean,
    confirmVisible: Boolean,
    isLoading: Boolean,
    onNombreChange: (String) -> Unit,
    onCorreoChange: (String) -> Unit,
    onContrasenaChange: (String) -> Unit,
    onContrasenaConfirmChange: (String) -> Unit,
    onPasswordVisibilityToggle: () -> Unit,
    onConfirmVisibilityToggle: () -> Unit,
    onRegisterClick: () -> Unit,
    onBackToLoginClick: () -> Unit,
) {
    // `focusedFieldColors` — brand focus state applied uniformly to every field so the whole
    // form reads as a single family. `LilaPrimary` on focus, subtle `TextoGris` when idle,
    // Burgundy for the placeholder cursor.
    val focusedFieldColors =
        OutlinedTextFieldDefaults.colors(
            focusedBorderColor = LilaPrimary,
            unfocusedBorderColor = TextoGris.copy(alpha = 0.4f),
            cursorColor = Burgundy,
            focusedLabelColor = LilaPrimary,
        )

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(Fondo)
                .statusBarsPadding(),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            IconButton(
                onClick = onBackToLoginClick,
                modifier = Modifier.size(48.dp),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver a inicio de sesión",
                    tint = Burgundy,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Crea tu cuenta",
                color = Color.Black,
                style = MaterialTheme.typography.displaySmall,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Únete a MiRumi y organiza tu piso compartido",
                color = TextoGris,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(40.dp))

            OutlinedTextField(
                value = nombre,
                onValueChange = onNombreChange,
                modifier = Modifier.fillMaxWidth().height(60.dp),
                placeholder = { Text("Nombre", color = TextoGris) },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                singleLine = true,
                shape = InputShape,
                colors = focusedFieldColors,
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = correo,
                onValueChange = onCorreoChange,
                modifier = Modifier.fillMaxWidth().height(60.dp),
                placeholder = { Text("Correo electrónico", color = TextoGris) },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                singleLine = true,
                shape = InputShape,
                colors = focusedFieldColors,
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = contrasena,
                onValueChange = onContrasenaChange,
                modifier = Modifier.fillMaxWidth().height(60.dp),
                placeholder = { Text("Contraseña", color = TextoGris) },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                trailingIcon = {
                    IconButton(onClick = onPasswordVisibilityToggle) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle password",
                            tint = TextoGris,
                        )
                    }
                },
                singleLine = true,
                shape = InputShape,
                colors = focusedFieldColors,
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = contrasenaConfirm,
                onValueChange = onContrasenaConfirmChange,
                modifier = Modifier.fillMaxWidth().height(60.dp),
                placeholder = { Text("Repite la contraseña", color = TextoGris) },
                visualTransformation = if (confirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                trailingIcon = {
                    IconButton(onClick = onConfirmVisibilityToggle) {
                        Icon(
                            imageVector = if (confirmVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle password",
                            tint = TextoGris,
                        )
                    }
                },
                singleLine = true,
                shape = InputShape,
                colors = focusedFieldColors,
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onRegisterClick,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled =
                    !isLoading &&
                        nombre.isNotBlank() &&
                        correo.isNotBlank() &&
                        contrasena.isNotBlank() &&
                        contrasenaConfirm.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Burgundy),
                shape = ButtonShape,
                // Subtle drop shadow on the primary CTA — reinforces its hierarchy over the
                // rest of the form (which is border-only).
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp, pressedElevation = 2.dp),
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.requiredSize(24.dp),
                    )
                } else {
                    Text("Crear cuenta", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                textAlign = TextAlign.Center,
                text =
                    buildAnnotatedString {
                        withStyle(SpanStyle(color = TextoGris, fontSize = 15.sp, fontWeight = FontWeight.Medium)) {
                            append("¿Ya tienes cuenta?")
                        }
                        withStyle(SpanStyle(color = Burgundy, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)) {
                            append("  Inicia sesión")
                        }
                    },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onBackToLoginClick)
                        .padding(vertical = 12.dp),
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalSerializationApi::class)
private fun handleRegistroSuccess(
    response: LoginResponse,
    sessionManager: SessionManager,
    navController: NavController,
) {
    // Auto-login: the register endpoint returns the same LoginResponse shape as /login.
    // RepositoryRegistro already persisted the JWT via NetworkModule.sessionManager — this
    // handler just performs the same routing decision as the login path.
    val casasList =
        response.flats.map { dto ->
            Casa(
                id = dto.id,
                nombre = dto.nombre,
                descripcion = dto.descripcion,
                rutaImagen = null,
                fechaCreacion = dto.fechaCreacion,
            )
        }

    if (casasList.isNotEmpty()) {
        val casaActiva = casasList[0]
        sessionManager.saveCasaActiva(casaActiva.id, casaActiva.nombre)
        navController.navigate(Route.Home.createRoute(casaActiva.id, casaActiva.nombre)) {
            popUpTo(0) { inclusive = true }
        }
    } else {
        val casasJson = Json.encodeToString(ArraySerializer(Casa.serializer()), casasList.toTypedArray())
        navController.navigate(Route.ListaCasas.createRoute(casasJson)) {
            popUpTo(0) { inclusive = true }
        }
    }
}
