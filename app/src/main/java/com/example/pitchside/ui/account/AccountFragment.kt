package com.example.pitchside.ui.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.background
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
import com.example.pitchside.ui.login.CustomTextField

class AccountFragment : Fragment() {

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
        var nowaNazwa by remember { mutableStateOf(currentUser?.nazwa_uzytkownika ?: "") }

        val context = LocalContext.current
        val success by viewModel.updateSuccess.observeAsState(false)
        val errorMsg by viewModel.error.observeAsState(null)

        LaunchedEffect(success) {
            if (success) {
                Toast.makeText(context, "Account updated!", Toast.LENGTH_SHORT).show()
                viewModel.updateSuccess.value = false
            }
        }

        LaunchedEffect(errorMsg) {
            errorMsg?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                viewModel.error.value = null
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Spacer(modifier = Modifier.height(24.dp))

            CustomTextField(
                value = nowaNazwa,
                onValueChange = { nowaNazwa = it },
                label = "Username"
            )

            Button(
                onClick = {
                    if (nowaNazwa.isNotBlank()) {
                        viewModel.zmienNazwe(nowaNazwa)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD4AF37),
                    contentColor = Color(0xFF111111)
                )
            ) {
                Text("Save changes")
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    SessionManager.loggedInUser = null
                    onLogout()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB03030))
            ) {
                Text("Log out", color = Color.White)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
