package com.example.pitchside.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.pitchside.R

class LoginFragment : Fragment() {

    private val viewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                LoginScreen(
                    viewModel = viewModel,
                    onNavigateToHome = {
                        findNavController().navigate(R.id.navigation_home)
                    }
                )
            }
        }
    }
}

@Composable
fun LoginScreen(viewModel: LoginViewModel, onNavigateToHome: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var haslo by remember { mutableStateOf("") }
    var nazwa by remember { mutableStateOf("") }
    var isRegisterMode by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val success by viewModel.isSuccess.observeAsState(false)
    val errorMsg by viewModel.error.observeAsState(null)

    LaunchedEffect(success, errorMsg) {
        if (success) {
            val message = if (isRegisterMode) "Rejestracja udana!" else "Zalogowano pomyślnie!"
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            onNavigateToHome()
        }
        errorMsg?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (isRegisterMode) "Stwórz konto" else "Witaj z powrotem",
            style = MaterialTheme.typography.headlineMedium,
            color = Color(0xFFD4AF37)
        )

        Spacer(modifier = Modifier.height(20.dp))

        CustomTextField(
            value = nazwa,
            onValueChange = { nazwa = it },
            label = if (isRegisterMode) "Nazwa użytkownika" else "Nazwa użytkownika"
        )

        if (isRegisterMode) {
            CustomTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email"
            )
        }

        CustomTextField(
            value = haslo,
            onValueChange = { haslo = it },
            label = "Hasło",
            isPassword = true
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                if (isRegisterMode) {
                    viewModel.zarejestruj(email, haslo, nazwa)
                } else {
                    viewModel.zaloguj(nazwa, haslo)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFD4AF37),
                contentColor = Color(0xFF111111)
            )
        ) {
            Text(if (isRegisterMode) "Zarejestruj" else "Zaloguj")
        }

        TextButton(onClick = { isRegisterMode = !isRegisterMode }) {
            Text(
                text = if (isRegisterMode) "Masz już konto? Zaloguj się" else "Nie masz konta? Zarejestruj się",
                color = Color(0xFF111111)
            )
        }
    }
}

@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isPassword: Boolean = false
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(
            text = label,
            color = Color(0xFF111111),
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = null,
            placeholder = { Text(text = label, color = Color(0xFF8F8E8E)) },
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = Color(0xFF111111),
                unfocusedContainerColor = Color(0xFF111111),
                focusedBorderColor = Color(0xFFD4AF37),
                unfocusedBorderColor = Color(0xFF111111),
                cursorColor = Color(0xFFD4AF37)
            ),
            singleLine = true
        )
    }
}