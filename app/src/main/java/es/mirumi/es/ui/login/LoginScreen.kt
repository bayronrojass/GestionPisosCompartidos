package es.mirumi.es.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.mirumi.es.utils.BiometricHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    isLoading: Boolean = false,
    savedEmail: String? = null, // El email guardado en la sesion
    hasSavedToken: Boolean = false, // Para saber si podemos usar huella
    onLoginClick: (String, String) -> Unit = { _, _ -> },
    onBiometricSuccess: () -> Unit = {}, // Llamar cuando la huella es correcta
    onRegisterClick: () -> Unit = {},
    onForgotPasswordClick: () -> Unit = {},
) {
    var username by remember { mutableStateOf(savedEmail ?: "") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val canUseBiometrics = remember { BiometricHelper.isBiometricAvailable(context) }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "MiRumi",
            color = Color(0xFF0D47A1),
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        Text(
            text = "Inicia sesión para continuar",
            color = Color(0xFF1565C0),
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        )

        Spacer(modifier = Modifier.height(24.dp))

        Surface(
            modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Usuario",
                    color = Color(0xFF0D47A1),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 6.dp),
                )

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    placeholder = { Text("Introduce tu usuario") },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    singleLine = true,
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Contraseña",
                    color = Color(0xFF0D47A1),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 6.dp),
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
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

                Text(
                    text = "Recuperar Contraseña",
                    color = Color(0xFF1976D2),
                    fontSize = 13.sp,
                    modifier = Modifier.clickable(onClick = onForgotPasswordClick).padding(top = 10.dp),
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Fila con el botón de Iniciar Sesión y el de Biometría (si está disponible)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Button(
                        onClick = { onLoginClick(username, password) },
                        modifier = Modifier.weight(1f).height(52.dp),
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Text("Iniciar sesión", color = Color.White)
                        }
                    }

                    // Botón de huella (Solo se muestra si hay un token guardado y el móvil tiene lector)
                    if (canUseBiometrics && hasSavedToken) {
                        Box(
                            modifier =
                                Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE3F2FD))
                                    .clickable {
                                        BiometricHelper.showBiometricPrompt(
                                            context = context,
                                            onSuccess = { onBiometricSuccess() },
                                            onError = { error -> println("Error biometría: $error") },
                                        )
                                    },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fingerprint,
                                contentDescription = "Iniciar con huella",
                                tint = Color(0xFF1976D2),
                                modifier = Modifier.size(32.dp),
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
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
