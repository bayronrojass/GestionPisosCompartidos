package es.mirumi.es.ui.home

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import es.mirumi.es.R
import es.mirumi.es.data.SessionManager
import es.mirumi.es.data.remote.NetworkModule
import es.mirumi.es.data.repository.repositories.RepositoryLogin
import es.mirumi.es.model.Casa
import es.mirumi.es.model.responses.LoginResponse
import es.mirumi.es.ui.login.LoginUiState
import es.mirumi.es.ui.login.LoginViewModel
import es.mirumi.es.ui.login.LoginViewModelFactory
import es.mirumi.es.ui.navigation.Route
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.builtins.ArraySerializer
import kotlinx.serialization.json.Json

@Composable
fun PrincipalInicioSesin(
    navController: NavController,
    sessionManager: SessionManager,
    modifier: Modifier = Modifier,
) {
    val viewModel: LoginViewModel =
        viewModel(
            factory =
                LoginViewModelFactory(
                    RepositoryLogin(NetworkModule.loginApiService, LocalContext.current),
                ),
        )

    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is LoginUiState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                viewModel.resetState() // Limpiamos para no volver a mostrarlo al rotar la pantalla
            }
            is LoginUiState.Success -> {
                handleLoginSuccess(
                    context = context,
                    response = state.response,
                    sessionManager = sessionManager,
                    navController = navController,
                )
                viewModel.resetState()
            }
            else -> {} // Idle o Loading, no hacemos nada especial aquí
        }
    }

    // Interfaz de usuario
    LoginScreenUI(
        modifier = modifier,
        username = username,
        password = password,
        passwordVisible = passwordVisible,
        isLoading = uiState is LoginUiState.Loading, // Extraemos isLoading del estado
        onUsernameChange = { username = it },
        onPasswordChange = { password = it },
        onPasswordVisibilityToggle = { passwordVisible = !passwordVisible },
        onLoginClick = { viewModel.login(username.trim(), password.trim()) },
        onRegisterClick = {
            Toast.makeText(context, "Ir a Registro", Toast.LENGTH_SHORT).show()
        },
        onForgotPasswordClick = {
            Toast.makeText(context, "Recuperar contraseña", Toast.LENGTH_SHORT).show()
        },
        onSocialLogin = { provider ->
            Toast.makeText(context, "Iniciar sesión con $provider", Toast.LENGTH_SHORT).show()
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreenUI(
    modifier: Modifier = Modifier,
    username: String,
    password: String,
    passwordVisible: Boolean,
    isLoading: Boolean,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordVisibilityToggle: () -> Unit,
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onSocialLogin: (String) -> Unit,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .background(color = Color(0xfff8f8f8)),
    ) {
        Box(
            modifier =
                Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(x = 27.dp, y = 4.dp)
                    .requiredWidth(width = 343.dp)
                    .requiredHeight(height = 40.dp),
        )
        Box(
            modifier =
                Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(x = 0.dp, y = 80.dp)
                    .requiredWidth(width = 390.dp)
                    .requiredHeight(height = 885.dp)
                    .clip(shape = RoundedCornerShape(40.dp))
                    .background(color = Color(0xfff8f8f8)),
        )
        Box(
            modifier =
                Modifier
                    .align(alignment = Alignment.TopCenter)
                    .offset(x = 0.dp, y = 100.dp)
                    .requiredWidth(width = 50.dp)
                    .requiredHeight(height = 10.dp)
                    .clip(shape = RoundedCornerShape(20.dp))
                    .background(color = Color(0xff6c6c6c)),
        )
        Image(
            painter = painterResource(id = R.drawable.ilustracioniniciosesion),
            contentDescription = "ilustracion inicio sesin",
            contentScale = ContentScale.Crop,
            modifier =
                Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(x = 142.dp, y = 153.dp)
                    .requiredWidth(width = 108.dp)
                    .requiredHeight(height = 153.dp),
        )
        Text(
            text = "¡Hola, de nuevo!",
            color = Color.Black,
            textAlign = TextAlign.Center,
            lineHeight = 1.33.em,
            style = MaterialTheme.typography.displaySmall,
            modifier =
                Modifier
                    .align(alignment = Alignment.TopCenter)
                    .offset(x = 1.dp, y = 342.dp)
                    .requiredWidth(width = 350.dp),
        )

        // Campo de usuario
        Column(
            modifier =
                Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(x = 20.dp, y = 430.dp)
                    .requiredWidth(width = 350.dp),
        ) {
            OutlinedTextField(
                value = username,
                onValueChange = onUsernameChange,
                modifier = Modifier.fillMaxWidth().height(60.dp),
                placeholder = { Text("Correo Electrónico", color = Color(0xff6c6c6c)) },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                singleLine = true,
                shape = RoundedCornerShape(15.dp),
            )
        }

        // Campo de contraseña
        Column(
            modifier =
                Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(x = 20.dp, y = 500.dp)
                    .requiredWidth(width = 350.dp),
        ) {
            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                modifier = Modifier.fillMaxWidth().height(60.dp),
                placeholder = { Text("Contraseña", color = Color(0xff6c6c6c)) },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                trailingIcon = {
                    IconButton(onClick = onPasswordVisibilityToggle) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle password",
                            tint = Color(0xff6c6c6c),
                        )
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(15.dp),
            )
        }

        // Botón de inicio de sesión
        Button(
            onClick = onLoginClick,
            modifier =
                Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(x = 20.dp, y = 580.dp)
                    .requiredWidth(width = 350.dp)
                    .height(52.dp),
            enabled = !isLoading && username.isNotEmpty() && password.isNotEmpty(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xff581327)),
            shape = RoundedCornerShape(15.dp),
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.requiredSize(24.dp))
            } else {
                Text("Iniciar sesión", color = Color.White, fontSize = 16.sp)
            }
        }

        // Enlace de recuperar contraseña
        Text(
            text = "¿Has olvidado tu contraseña?",
            color = Color(0xff581327),
            textAlign = TextAlign.Center,
            style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Medium),
            modifier =
                Modifier
                    .align(alignment = Alignment.TopCenter)
                    .offset(x = 1.dp, y = 640.dp)
                    .clickable(onClick = onForgotPasswordClick)
                    .requiredWidth(width = 212.dp)
                    .requiredHeight(height = 20.dp),
        )

        Text(
            text = "o",
            color = Color(0xff6c6c6c),
            textAlign = TextAlign.Center,
            style = TextStyle(fontSize = 16.sp),
            modifier = Modifier.align(alignment = Alignment.TopCenter).offset(x = 0.dp, y = 680.dp),
        )
        Image(
            painter = painterResource(id = R.drawable.line3),
            contentDescription = "Line 3",
            modifier =
                Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(x = 220.dp, y = 690.5.dp)
                    .requiredWidth(width = 160.dp)
                    .border(border = BorderStroke(1.dp, Color(0xff6c6c6c))),
        )
        Image(
            painter = painterResource(id = R.drawable.line4),
            contentDescription = "Line 4",
            modifier =
                Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(x = 20.dp, y = 690.5.dp)
                    .requiredWidth(width = 160.dp)
                    .border(border = BorderStroke(1.dp, Color(0xff6c6c6c))),
        )

        // Botones de login social
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
            modifier =
                Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(x = 245.dp, y = 730.dp)
                    .requiredWidth(width = 60.dp)
                    .requiredHeight(height = 50.dp)
                    .clip(shape = RoundedCornerShape(15.dp))
                    .background(color = Color.White)
                    .border(border = BorderStroke(1.dp, Color(0xff6c6c6c)), shape = RoundedCornerShape(15.dp))
                    .clickable { onSocialLogin("facebook") },
        ) {
            Image(
                painter = painterResource(id = R.drawable.facebook),
                contentDescription = "facebook",
                modifier = Modifier.requiredSize(size = 28.dp),
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
            modifier =
                Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(x = 165.dp, y = 730.dp)
                    .requiredWidth(width = 60.dp)
                    .requiredHeight(height = 50.dp)
                    .clip(shape = RoundedCornerShape(15.dp))
                    .background(color = Color.White)
                    .border(border = BorderStroke(1.dp, Color(0xff6c6c6c)), shape = RoundedCornerShape(15.dp))
                    .clickable { onSocialLogin("google") },
        ) {
            Image(
                painter = painterResource(id = R.drawable.google),
                contentDescription = "google",
                modifier = Modifier.requiredSize(size = 24.dp),
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
            modifier =
                Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(x = 85.dp, y = 730.dp)
                    .requiredWidth(width = 60.dp)
                    .requiredHeight(height = 50.dp)
                    .clip(shape = RoundedCornerShape(15.dp))
                    .background(color = Color.White)
                    .border(border = BorderStroke(1.dp, Color(0xff6c6c6c)), shape = RoundedCornerShape(15.dp))
                    .padding(start = 10.dp, end = 10.dp, top = 10.dp, bottom = 12.dp)
                    .clickable { onSocialLogin("apple") },
        ) {
            Image(
                painter = painterResource(id = R.drawable.apple),
                contentDescription = "apple",
                colorFilter = ColorFilter.tint(Color.Black),
                modifier = Modifier.requiredSize(size = 30.dp),
            )
        }

        // Enlace de registro
        Text(
            textAlign = TextAlign.Center,
            text =
                buildAnnotatedString {
                    withStyle(style = SpanStyle(color = Color(0xff6c6c6c), fontSize = 15.sp, fontWeight = FontWeight.Medium)) {
                        append("¿No tienes cuenta todavía?")
                    }
                    withStyle(
                        style = SpanStyle(color = Color(0xff581327), fontSize = 15.sp, fontWeight = FontWeight.Medium),
                    ) { append(" ") }
                    withStyle(style = SpanStyle(color = Color(0xff581327), fontSize = 15.sp)) { append("Regístrate") }
                },
            modifier =
                Modifier
                    .align(alignment = Alignment.TopCenter)
                    .offset(x = 1.5.dp, y = 800.dp)
                    .clickable(onClick = onRegisterClick)
                    .requiredWidth(width = 273.dp)
                    .requiredHeight(height = 19.dp),
        )
    }
}

@OptIn(ExperimentalSerializationApi::class)
private fun handleLoginSuccess(
    context: Context,
    response: LoginResponse,
    sessionManager: SessionManager,
    navController: NavController,
) {
    Toast.makeText(context, "¡Bienvenido, ${response.user.nombre}!", Toast.LENGTH_SHORT).show()

    sessionManager.saveAuthData(
        token = response.authToken,
        userId = response.user.id,
        email = response.user.correo,
    )

    val casasList =
        response.flats.map { casaDto ->
            Casa(
                id = casaDto.id,
                nombre = casaDto.nombre,
                descripcion = casaDto.descripcion,
                rutaImagen = null,
                fechaCreacion = casaDto.fechaCreacion,
            )
        }

    // 3. Lógica directa de navegación a la Casa Activa
    if (casasList.isNotEmpty()) {
        val casaGuardadaId = sessionManager.getCasaActivaId()

        var casaActiva = casasList.find { it.id == casaGuardadaId }

        if (casaActiva == null) {
            casaActiva = casasList[0]
            sessionManager.saveCasaActivaId(casaActiva.id)
        }

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
