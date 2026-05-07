package com.example.pitchside.ui.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.pitchside.managers.SessionManager
import com.example.pitchside.ui.home.CrestAsyncImage

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
                val match by viewModel.match.observeAsState()
                val isFetching by viewModel.isFetching.observeAsState(false)
                val isFavorite by viewModel.isFavorite.observeAsState(false) // DODANO

                LaunchedEffect(matchId) {
                    if (matchId != -1) {
                        viewModel.fetchMatchDetails(matchId)
                    }
                }

                Box(
                    modifier = Modifier.fillMaxSize().background(Color(0xFF121212)),
                    contentAlignment = Alignment.Center
                ) {
                    if (isFetching) {
                        CircularProgressIndicator(color = Color.White)
                    } else if (match != null) {
                        match?.let { m ->
                            Column(
                                modifier = Modifier.fillMaxSize().padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Nagłówek z Ligą i Gwiazdką
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Pusty spacer dla wycentrowania tekstu ligi (opcjonalnie)
                                    Spacer(modifier = Modifier.width(48.dp))

                                    Text(
                                        text = m.competition?.name ?: "",
                                        color = Color.LightGray,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.weight(1f),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )

                                    // DODANO: Gwiazdka ulubionych
                                    if (SessionManager.isLoggedIn()) {
                                        IconButton(onClick = { viewModel.toggleFavorite() }) {
                                            Icon(
                                                imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                                contentDescription = "Ulubiony mecz",
                                                tint = if (isFavorite) Color.Yellow else Color.White,
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
                                    TeamDetailItem(m.homeTeam.crest ?: "", m.homeTeam.shortName ?: m.homeTeam.name ?: "")

                                    val homeScore = m.score?.fullTime?.home
                                    val awayScore = m.score?.fullTime?.away

                                    val displayText = if (homeScore != null && awayScore != null) {
                                        "$homeScore : $awayScore"
                                    } else {
                                        "VS"
                                    }

                                    Text(
                                        text = displayText,
                                        color = Color.White,
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.Bold
                                    )

                                    TeamDetailItem(m.awayTeam.crest ?: "", m.awayTeam.shortName ?: m.awayTeam.name ?: "")
                                }

                                Spacer(modifier = Modifier.height(48.dp))

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF222222))
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        DetailRow("Data", m.utcDate?.take(10) ?: "-")

                                        val displayStatus = when(m.status) {
                                            "TIMED" -> "Zaplanowany"
                                            "SCHEDULED" -> "Zaplanowany"
                                            "FINISHED" -> "Zakończony"
                                            "IN_PLAY" -> "W trakcie"
                                            else -> m.status ?: "Nieznany"
                                        }
                                        DetailRow("Status", displayStatus)

                                        DetailRow("Sędzia", m.referees?.firstOrNull()?.name ?: "Brak danych")
                                    }
                                }
                            }
                        }
                    } else {
                        Text("Nie udało się pobrać danych meczu.", color = Color.White)
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
            color = Color.White,
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
        Text(label, color = Color.Gray)
        Text(value, color = Color.White, fontWeight = FontWeight.Medium)
    }
}