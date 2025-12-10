package es.mirumi.es.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// LoginScreen.kt
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    isLoading: Boolean = false,
    onLoginClick: (String, String) -> Unit = { _, _ -> },
    onRegisterClick: () -> Unit = {},
    onForgotPasswordClick: () -> Unit = {},
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Título de la app
        Text(
            text = "MiRumi",
            color = Color(0xFF0D47A1),
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        // Subtítulo
        Text(
            text = "Inicia sesión para continuar",
            color = Color(0xFF1565C0),
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Card de login
        Surface(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Campo de usuario
                Text(
                    text = "Usuario",
                    color = Color(0xFF0D47A1),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 6.dp),
                )

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                    placeholder = { Text("Introduce tu usuario") },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    singleLine = true,
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Campo de contraseña
                Text(
                    text = "Contraseña",
                    color = Color(0xFF0D47A1),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 6.dp),
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                    placeholder = { Text("Introduce tu contraseña") },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle password",
                            )
                        }
                    },
                    singleLine = true,
                )

                // Enlace de recuperar contraseña
                Text(
                    text = "Recuperar Contraseña",
                    color = Color(0xFF1976D2),
                    fontSize = 13.sp,
                    modifier =
                        Modifier
                            .clickable(onClick = onForgotPasswordClick)
                            .padding(top = 10.dp),
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Botón de iniciar sesión
                Button(
                    onClick = { onLoginClick(username, password) },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("Iniciar sesión", color = Color.White)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Sección de registro
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.Center,
        ) {
            Text("¿No tienes cuenta?", color = Color(0xFF424242), fontSize = 14.sp)
            Text(
                "  ¡Regístrate hoy!",
                color = Color(0xFF1976D2),
                fontSize = 14.sp,
                modifier = Modifier.clickable(onClick = onRegisterClick),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    MaterialTheme {
        LoginScreen(
            isLoading = false,
            onLoginClick = { user, pass ->
                println("Login: $user, $pass")
            },
            onRegisterClick = {
                println("Registrarse")
            },
            onForgotPasswordClick = {
                println("Olvidé contraseña")
            },
        )
    }
}
