package com.example.pitchside.ui.details

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.example.pitchside.R
import com.example.pitchside.api.responses.MatchEntry
import com.example.pitchside.api.responses.Standing
import com.example.pitchside.api.responses.StandingResponse
import com.example.pitchside.api.responses.Table
import com.example.pitchside.managers.SessionManager
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

class CompetitionDetailsFragment : Fragment() {
    private val viewModel: CompetitionDetailsViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val competitionCode = arguments?.getString("competitionCode") ?: "Unknown"
        viewModel.setCompetitionCode(competitionCode)
        viewModel.fetchAllData()
        return ComposeView(requireContext()).apply {
            setContent {
                CompetitionDetailsScreen(
                    viewModel = viewModel,
                    onMatchClick = { matchId ->
                        val bundle = Bundle().apply { putInt("matchId", matchId) }
                        findNavController().navigate(R.id.matchDetailsFragment, bundle)
                    }
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CompetitionDetailsScreen(viewModel: CompetitionDetailsViewModel, onMatchClick: (Int) -> Unit) {
    val standing by viewModel.standings.observeAsState()
    val scheduled by viewModel.scheduledMatchesByMatchday.observeAsState()
    val finished by viewModel.finishedMatchesByMatchday.observeAsState()
    val isFetching by viewModel.isFetching.observeAsState(false)
    val tabs = listOf("Przyszłe mecze", "Tabela", "Wyniki")
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    if (isFetching) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary)
        }
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            // Nagłówek ligi
            Column(modifier = Modifier.fillMaxWidth().background(Color(0xFF595959)).padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CrestAsyncImage(standing?.area?.flag ?: "", "", 30)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = standing?.area?.name ?: "", color = Color.White.copy(alpha = 0.7f))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CrestAsyncImage(standing?.competition?.emblem ?: "", "", 50)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = standing?.competition?.name ?: "", color = Color.White, style = MaterialTheme.typography.headlineSmall)
                }
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    thickness = 1.dp,
                    color = Color.Gray
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CrestAsyncImage(
                        standing?.competition?.emblem ?: "",
                        standing?.competition?.name ?: "Unknown",
                        50
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = standing?.competition?.name ?: "Unknown",
                        color = Color.White,
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.weight(1f)
                    )

                    // DODANO: Gwiazdka ulubionych (tylko dla zalogowanych)
                    if (SessionManager.isLoggedIn()) {
                        val isFavorite by viewModel.isFavorite.observeAsState(false)
                        IconButton(onClick = { viewModel.toggleFavorite() }) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                contentDescription = "Ulubiona liga",
                                tint = if (isFavorite) Color.Yellow else Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    thickness = 1.dp,
                    color = Color.Gray
                )
            }

            TabRow(selectedTabIndex = selectedTabIndex, containerColor = Color(0xFF595959)) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(text = title, color = Color.White) }
                    )
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when (selectedTabIndex) {
                    0 -> MatchesList(scheduled ?: emptyMap(), onMatchClick)
                    1 -> StandingList(standing?.standings ?: emptyList())
                    2 -> MatchesList(finished ?: emptyMap(), onMatchClick)
                }
            }
        }
    }
}

@Composable
@RequiresApi(Build.VERSION_CODES.O)
fun MatchesList(matches: Map<Int?, List<MatchEntry>?>, onMatchClick: (Int) -> Unit) {
    LazyColumn {
        matches.forEach { (day, list) ->
            item {
                Box(modifier = Modifier.fillMaxWidth().background(Color(0xFF444444)).padding(8.dp)) {
                    Text(text = "Kolejka $day", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
            list?.let {
                items(it) { match -> MatchItem(match, onMatchClick) }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MatchItem(match: MatchEntry, onMatchClick: (Int) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp).clickable { match.id?.let { onMatchClick(it) } },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF8F8E8E))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Home Team
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                CrestAsyncImage(match.homeTeam.crest ?: "", "", 30)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = match.homeTeam.shortName ?: "", color = Color.White, maxLines = 1)
            }

            // Score/VS
            Column(modifier = Modifier.width(60.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                val h = match.score?.fullTime?.home
                val a = match.score?.fullTime?.away
                Text(
                    text = if (h != null) "$h : $a" else " vs ",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }

            // Away Team
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End) {
                Text(text = match.awayTeam.shortName ?: "", color = Color.White, maxLines = 1, textAlign = TextAlign.End)
                Spacer(modifier = Modifier.width(8.dp))
                CrestAsyncImage(match.awayTeam.crest ?: "", "", 30)
            }
        }
    }
}

@Composable
fun StandingList(standings: List<Standing>) {
    LazyColumn {
        standings.forEach { standing ->
            item {
                Box(modifier = Modifier.fillMaxWidth().background(Color(0xFF444444)).padding(8.dp)) {
                    Text(text = standing.group ?: "Tabela", color = Color.White)
                }
            }
            items(standing.table) { StandingItem(it) }
        }
    }
}

@Composable
fun StandingItem(entry: Table) {
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 2.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF8F8E8E))) {
        Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(text = "${entry.position}.", color = Color.White, modifier = Modifier.width(25.dp))
            CrestAsyncImage(entry.team.crest ?: "", "", 24)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = entry.team.shortName ?: "", color = Color.White, modifier = Modifier.weight(1f))
            Text(text = "${entry.playedGames}m", color = Color.White, modifier = Modifier.width(40.dp))
            Text(text = "${entry.points}pkt", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun CrestAsyncImage(url: String, name: String, size: Int) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(url)
            .decoderFactory(SvgDecoder.Factory())
            .crossfade(true)
            .build(),
        contentDescription = name,
        modifier = Modifier.size(size.dp)
    )
}