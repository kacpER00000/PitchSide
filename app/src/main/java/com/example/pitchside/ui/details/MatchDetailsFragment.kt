package com.example.pitchside.ui.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.pitchside.data.Resource
import com.example.pitchside.managers.SessionManager

class MatchDetailsFragment : Fragment() {
    private val viewModel: MatchDetailsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val matchId = arguments?.getInt("matchId") ?: -1
        return ComposeView(requireContext()).apply {
            setContent {
                val match = viewModel.match.observeAsState(initial = Resource.Loading).value
                val isFavorite by viewModel.isFavorite.observeAsState(false)
                val context = LocalContext.current

                LaunchedEffect(matchId) {
                    if (matchId != -1) {
                        viewModel.setMatchId(matchId)
                    }
                }

                LaunchedEffect(match) {
                    if (match is Resource.Error) {
                        Toast.makeText(
                            context,
                            "Wystapil blad podczas pobierania danych.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                Box(
                    modifier = Modifier.fillMaxSize().background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    when (match) {
                        is Resource.Loading -> {
                            CircularProgressIndicator(color = Color(0xFFD4AF37))
                        }

                        is Resource.Error -> {
                            Text(text = "Nie udalo sie pobrac danych meczu.", color = Color(0xFF111111))
                        }

                        is Resource.Success -> {
                            val m = match.data
                            Column(
                                modifier = Modifier.fillMaxSize().padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Spacer(modifier = Modifier.width(48.dp))
                                    Text(
                                        text = m.leagueName ?: "",
                                        color = Color(0xFF111111),
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.weight(1f),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )

                                    if (SessionManager.isLoggedIn()) {
                                        IconButton(onClick = { viewModel.toggleFavorite() }) {
                                            Icon(
                                                imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                                contentDescription = "Ulubiony mecz",
                                                tint = if (isFavorite) Color(0xFFD4AF37) else Color(0xFF111111),
                                                modifier = Modifier.size(32.dp)
                                            )
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.width(48.dp))
                                    }
                                }

                                Spacer(modifier = Modifier.height(48.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TeamDetailItem(m.homeTeamCrest ?: "", m.homeTeamName ?: m.homeTeamName ?: "")

                                    val homeScore = m.homeTeamScore
                                    val awayScore = m.awayTeamScore

                                    val displayText = if (homeScore != null && awayScore != null) {
                                        "$homeScore : $awayScore"
                                    } else {
                                        "VS"
                                    }

                                    Text(
                                        text = displayText,
                                        color = Color(0xFFD4AF37),
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.Bold
                                    )

                                    TeamDetailItem(m.awayTeamCrest ?: "", m.awayTeamName ?: m.awayTeamName ?: "")
                                }

                                Spacer(modifier = Modifier.height(48.dp))

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF111111))
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        DetailRow("Data", m.startDate?.take(10) ?: "-")

                                        val displayStatus = when(m.status) {
                                            "TIMED" -> "Zaplanowany"
                                            "SCHEDULED" -> "Zaplanowany"
                                            "FINISHED" -> "Zakończony"
                                            "IN_PLAY" -> "W trakcie"
                                            else -> m.status
                                        }
                                        DetailRow("Status", displayStatus)

                                        DetailRow("Sędzia", m.referee ?: "Brak danych")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TeamDetailItem(crest: String, name: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        CrestAsyncImage(crest, "", 60)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = name,
            color = Color(0xFF111111),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.LightGray)
        Text(value, color = Color.White, fontWeight = FontWeight.Medium)
    }
}
