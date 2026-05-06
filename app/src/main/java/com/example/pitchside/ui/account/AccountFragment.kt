package com.example.pitchside.ui.account

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
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.pitchside.R
import com.example.pitchside.managers.SessionManager
import com.example.pitchside.ui.login.CustomTextField // Importujemy Twoje pole tekstowe

class AccountFragment : Fragment() {

    // Inicjalizacja ViewModelu dla tego ekranu
    private val viewModel: AccountViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_account, container, false)

        val composeView = view.findViewById<ComposeView>(R.id.compose_view_account)

        composeView.apply {
            setContent {
                AccountScreen(
                    viewModel = viewModel,
                    onLogout = {
                        findNavController().navigate(R.id.navigation_home)
                        requireActivity().invalidateOptionsMenu()
                    }
                )
            }
        }
        return view
    }

    @Composable
    fun AccountScreen(viewModel: AccountViewModel, onLogout: () -> Unit) {
        val currentUser = SessionManager.loggedInUser
        // Stan dla pola edycji nazwy
        var nowaNazwa by remember { mutableStateOf(currentUser?.nazwa_uzytkownika ?: "") }

        val context = LocalContext.current
        val success by viewModel.updateSuccess.observeAsState(false)
        val errorMsg by viewModel.error.observeAsState(null)

        // Reakcja na sukces aktualizacji
        LaunchedEffect(success) {
            if (success) {
                Toast.makeText(context, "Dane zaktualizowane!", Toast.LENGTH_SHORT).show()
                viewModel.updateSuccess.value = false
            }
        }

        // Reakcja na błąd
        LaunchedEffect(errorMsg) {
            errorMsg?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                viewModel.error.value = null
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Ustawienia Konta",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Pole do zmiany nazwy
            CustomTextField(
                value = nowaNazwa,
                onValueChange = { nowaNazwa = it },
                label = "Nazwa użytkownika"
            )

            // Przycisk zapisu
            Button(
                onClick = {
                    if (nowaNazwa.isNotBlank()) {
                        viewModel.zmienNazwe(nowaNazwa)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8F8E8E))
            ) {
                Text("Zapisz zmiany")
            }

            Spacer(modifier = Modifier.weight(1f))

            // Przycisk wylogowania (na dole ekranu)
            Button(
                onClick = {
                    SessionManager.loggedInUser = null
                    onLogout()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB03030)) // Ciemnoczerwony
            ) {
                Text("Wyloguj się", color = Color.White)
            }
        }
    }
}