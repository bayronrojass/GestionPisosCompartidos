package es.mirumi.es.ui.home

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import es.mirumi.es.utils.BiometricHelper
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
            factory = LoginViewModelFactory(RepositoryLogin(NetworkModule.loginApiService, LocalContext.current)),
        )

    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Cargamos el email guardado si existe para facilitar el login
    var username by remember { mutableStateOf(sessionManager.fetchUserEmail() ?: "") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberBiometric by remember { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is LoginUiState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }
            is LoginUiState.Success -> {
                if (rememberBiometric) {
                    sessionManager.setBiometricEnabled(true)
                }

                handleLoginSuccess(
                    context = context,
                    response = state.response,
                    sessionManager = sessionManager,
                    navController = navController,
                )
                viewModel.resetState()
            }
            else -> {}
        }
    }

    LoginScreenUI(
        modifier = modifier,
        username = username,
        password = password,
        passwordVisible = passwordVisible,
        isLoading = uiState is LoginUiState.Loading,
        rememberBiometric = rememberBiometric,
        sessionManager = sessionManager,
        onUsernameChange = { username = it },
        onPasswordChange = { password = it },
        onPasswordVisibilityToggle = { passwordVisible = !passwordVisible },
        onRememberBiometricChange = { rememberBiometric = it },
        onLoginClick = { viewModel.login(username.trim(), password.trim()) },
        onBiometricSuccess = {
            val casaId = sessionManager.getCasaActivaId()
            val casaNombre = sessionManager.getCasaActivaNombre()

            if (casaId != -1L) {
                navController.navigate(Route.Home.createRoute(casaId, casaNombre)) {
                    popUpTo(0) { inclusive = true }
                }
            } else {
                navController.navigate(Route.ListaCasas.createRoute("[]")) {
                    popUpTo(0) { inclusive = true }
                }
            }
        },
        onRegisterClick = { Toast.makeText(context, "Ir a Registro", Toast.LENGTH_SHORT).show() },
        onForgotPasswordClick = { Toast.makeText(context, "Recuperar contraseña", Toast.LENGTH_SHORT).show() },
        onSocialLogin = { provider -> Toast.makeText(context, "Iniciar sesión con $provider", Toast.LENGTH_SHORT).show() },
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
    rememberBiometric: Boolean,
    sessionManager: SessionManager,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordVisibilityToggle: () -> Unit,
    onRememberBiometricChange: (Boolean) -> Unit,
    onLoginClick: () -> Unit,
    onBiometricSuccess: () -> Unit,
    onRegisterClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onSocialLogin: (String) -> Unit,
) {
    val context = LocalContext.current
    val canUseBiometrics = remember { BiometricHelper.isBiometricAvailable(context) }
    val isBiometricEnabled = sessionManager.isBiometricEnabled() && sessionManager.hasSavedToken()

    Box(
        modifier = modifier.fillMaxWidth().fillMaxHeight().background(color = Color(0xfff8f8f8)),
    ) {
        Box(
            modifier =
                Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 27.dp, y = 4.dp)
                    .requiredWidth(343.dp)
                    .requiredHeight(40.dp),
        )
        Box(
            modifier =
                Modifier
                    .align(
                        Alignment.TopStart,
                    ).offset(
                        x = 0.dp,
                        y = 80.dp,
                    ).requiredWidth(390.dp)
                    .requiredHeight(885.dp)
                    .clip(RoundedCornerShape(40.dp))
                    .background(Color(0xfff8f8f8)),
        )
        Box(
            modifier =
                Modifier
                    .align(
                        Alignment.TopCenter,
                    ).offset(
                        x = 0.dp,
                        y = 100.dp,
                    ).requiredWidth(50.dp)
                    .requiredHeight(10.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xff6c6c6c)),
        )
        Image(
            painter = painterResource(id = R.drawable.ilustracioniniciosesion),
            contentDescription = "ilustracion",
            contentScale = ContentScale.Crop,
            modifier =
                Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 142.dp, y = 153.dp)
                    .requiredWidth(108.dp)
                    .requiredHeight(153.dp),
        )

        Text(
            text = "¡Hola, de nuevo!",
            color = Color.Black,
            textAlign = TextAlign.Center,
            lineHeight = 1.33.em,
            style = MaterialTheme.typography.displaySmall,
            modifier = Modifier.align(Alignment.TopCenter).offset(x = 1.dp, y = 342.dp).requiredWidth(350.dp),
        )

        // CAMPO USUARIO
        Column(modifier = Modifier.align(Alignment.TopStart).offset(x = 20.dp, y = 410.dp).requiredWidth(350.dp)) {
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

        // CAMPO CONTRASEÑA
        Column(modifier = Modifier.align(Alignment.TopStart).offset(x = 20.dp, y = 480.dp).requiredWidth(350.dp)) {
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

            // CHECKBOX PARA RECORDAR HUELLA
            if (canUseBiometrics && !isBiometricEnabled) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp),
                ) {
                    Checkbox(
                        checked = rememberBiometric,
                        onCheckedChange = onRememberBiometricChange,
                        colors = CheckboxDefaults.colors(checkedColor = Color(0xff581327)),
                    )
                    Text("Usar huella la próxima vez", fontSize = 14.sp, color = Color.DarkGray)
                }
            }
        }

        Row(
            modifier = Modifier.align(Alignment.TopStart).offset(x = 20.dp, y = 585.dp).requiredWidth(350.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Button(
                onClick = onLoginClick,
                modifier = Modifier.weight(1f).height(52.dp),
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

            // BOTÓN DE HUELLA
            if (canUseBiometrics && isBiometricEnabled) {
                Box(
                    modifier =
                        Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(15.dp))
                            .background(Color(0xFFE8D5DC))
                            .clickable {
                                BiometricHelper.showBiometricPrompt(
                                    context = context,
                                    onSuccess = { onBiometricSuccess() },
                                    onError = { error -> Toast.makeText(context, error, Toast.LENGTH_SHORT).show() },
                                )
                            },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = "Huella",
                        tint = Color(0xff581327),
                        modifier = Modifier.size(32.dp),
                    )
                }
            }
        }

        // [TUS ENLACES INFERIORES INTACTOS]
        Text(
            text = "¿Has olvidado tu contraseña?",
            color = Color(0xff581327),
            textAlign = TextAlign.Center,
            style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Medium),
            modifier =
                Modifier
                    .align(
                        Alignment.TopCenter,
                    ).offset(x = 1.dp, y = 650.dp)
                    .clickable(onClick = onForgotPasswordClick)
                    .requiredWidth(212.dp),
        )
        Text(
            text = "o",
            color = Color(0xff6c6c6c),
            textAlign = TextAlign.Center,
            style = TextStyle(fontSize = 16.sp),
            modifier = Modifier.align(Alignment.TopCenter).offset(x = 0.dp, y = 680.dp),
        )
        Image(
            painter = painterResource(id = R.drawable.line3),
            contentDescription = "Line 3",
            modifier =
                Modifier
                    .align(
                        Alignment.TopStart,
                    ).offset(x = 220.dp, y = 690.5.dp)
                    .requiredWidth(160.dp)
                    .border(BorderStroke(1.dp, Color(0xff6c6c6c))),
        )
        Image(
            painter = painterResource(id = R.drawable.line4),
            contentDescription = "Line 4",
            modifier =
                Modifier
                    .align(
                        Alignment.TopStart,
                    ).offset(x = 20.dp, y = 690.5.dp)
                    .requiredWidth(160.dp)
                    .border(BorderStroke(1.dp, Color(0xff6c6c6c))),
        )

        Row(
            horizontalArrangement =
                Arrangement.spacedBy(
                    10.dp,
                    Alignment.CenterHorizontally,
                ),
            verticalAlignment = Alignment.CenterVertically,
            modifier =
                Modifier
                    .align(
                        Alignment.TopStart,
                    ).offset(
                        x = 245.dp,
                        y = 730.dp,
                    ).requiredSize(
                        60.dp,
                        50.dp,
                    ).clip(
                        RoundedCornerShape(15.dp),
                    ).background(Color.White)
                    .border(BorderStroke(1.dp, Color(0xff6c6c6c)), RoundedCornerShape(15.dp))
                    .clickable {
                        onSocialLogin("facebook")
                    },
        ) {
            Image(
                painter = painterResource(id = R.drawable.facebook),
                contentDescription = "facebook",
                modifier = Modifier.requiredSize(28.dp),
            )
        }
        Row(
            horizontalArrangement =
                Arrangement.spacedBy(
                    10.dp,
                    Alignment.CenterHorizontally,
                ),
            verticalAlignment = Alignment.CenterVertically,
            modifier =
                Modifier
                    .align(
                        Alignment.TopStart,
                    ).offset(
                        x = 165.dp,
                        y = 730.dp,
                    ).requiredSize(
                        60.dp,
                        50.dp,
                    ).clip(
                        RoundedCornerShape(15.dp),
                    ).background(Color.White)
                    .border(BorderStroke(1.dp, Color(0xff6c6c6c)), RoundedCornerShape(15.dp))
                    .clickable {
                        onSocialLogin("google")
                    },
        ) {
            Image(
                painter = painterResource(id = R.drawable.google),
                contentDescription = "google",
                modifier = Modifier.requiredSize(24.dp),
            )
        }
        Row(
            horizontalArrangement =
                Arrangement.spacedBy(
                    10.dp,
                    Alignment.CenterHorizontally,
                ),
            verticalAlignment = Alignment.CenterVertically,
            modifier =
                Modifier
                    .align(
                        Alignment.TopStart,
                    ).offset(
                        x = 85.dp,
                        y = 730.dp,
                    ).requiredSize(
                        60.dp,
                        50.dp,
                    ).clip(
                        RoundedCornerShape(15.dp),
                    ).background(
                        Color.White,
                    ).border(
                        BorderStroke(1.dp, Color(0xff6c6c6c)),
                        RoundedCornerShape(15.dp),
                    ).padding(start = 10.dp, end = 10.dp, top = 10.dp, bottom = 12.dp)
                    .clickable {
                        onSocialLogin("apple")
                    },
        ) {
            Image(
                painter = painterResource(id = R.drawable.apple),
                contentDescription = "apple",
                colorFilter = ColorFilter.tint(Color.Black),
                modifier = Modifier.requiredSize(30.dp),
            )
        }

        Text(
            textAlign = TextAlign.Center,
            text =
                buildAnnotatedString {
                    withStyle(
                        SpanStyle(color = Color(0xff6c6c6c), fontSize = 15.sp, fontWeight = FontWeight.Medium),
                    ) { append("¿No tienes cuenta todavía?") }
                    withStyle(SpanStyle(color = Color(0xff581327), fontSize = 15.sp, fontWeight = FontWeight.Medium)) { append(" ") }
                    withStyle(SpanStyle(color = Color(0xff581327), fontSize = 15.sp)) { append("Regístrate") }
                },
            modifier =
                Modifier
                    .align(
                        Alignment.TopCenter,
                    ).offset(x = 1.5.dp, y = 800.dp)
                    .clickable(onClick = onRegisterClick)
                    .requiredWidth(273.dp)
                    .requiredHeight(19.dp),
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

    if (casasList.isNotEmpty()) {
        val casaGuardadaId = sessionManager.getCasaActivaId()
        var casaActiva = casasList.find { it.id == casaGuardadaId }

        if (casaActiva == null) {
            casaActiva = casasList[0]
        }

        // ¡NUEVO! Guardamos el ID y el Nombre para que la biometria funcione luego
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
