package es.mirumi.es.ui.login

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import es.mirumi.es.data.SessionManager
import es.mirumi.es.data.remote.NetworkModule
import es.mirumi.es.data.repository.repositories.RepositoryLogin
import es.mirumi.es.model.Casa
import es.mirumi.es.model.responses.LoginResponse
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.builtins.ArraySerializer
import kotlinx.serialization.json.Json

@Composable
fun LoginDestination(
    navController: NavController,
    sessionManager: SessionManager,
) {
    val viewModel: LoginViewModel =
        viewModel(
            factory =
                LoginViewModelFactory(
                    RepositoryLogin(NetworkModule.loginApiService, LocalContext.current),
                ),
        )

    val isLoading by viewModel.isLoading.observeAsState(false)
    val errorMessage by viewModel.error.observeAsState()
    val loginResult by viewModel.loginResult.observeAsState()

    val context = LocalContext.current

    LaunchedEffect(errorMessage) {
        errorMessage?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(loginResult) {
        loginResult?.let { response ->
            handleLoginSuccess(
                context = context,
                response = response,
                sessionManager = sessionManager,
                navController = navController,
            )
            viewModel.clearLoginResult()
        }
    }

    LoginScreen(
        isLoading = isLoading,
        onLoginClick = { email, password ->
            viewModel.login(email.trim(), password.trim())
        },
        onRegisterClick = {
            Toast.makeText(context, "Ir a Registro", Toast.LENGTH_SHORT).show()
        },
        onForgotPasswordClick = {
            Toast.makeText(context, "Recuperar contraseña", Toast.LENGTH_SHORT).show()
        },
    )
}

@OptIn(ExperimentalSerializationApi::class)
private fun handleLoginSuccess(
    context: Context,
    response: LoginResponse,
    sessionManager: SessionManager,
    navController: NavController,
) {
    Toast
        .makeText(
            context,
            "¡Bienvenido, ${response.user.nombre}!",
            Toast.LENGTH_SHORT,
        ).show()

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

    val casasJson =
        Json.encodeToString(ArraySerializer(Casa.serializer()), casasList.toTypedArray())
    val casaId = 1L
    val casaNombre = "Temp"
    navController.navigate("lista_casas?casas=$casasJson") {
        popUpTo("login") { inclusive = true }
    }
}
