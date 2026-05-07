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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
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
import kotlin.getValue

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
            setContent { CompetitionDetailsScreen(viewModel, onMatchClick = { matchId ->
                val bundle = Bundle().apply {
                    putInt("matchId", matchId)
                }
                findNavController().navigate(R.id.matchDetailsFragment, bundle)
            }) }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CompetitionDetailsScreen(viewModel: CompetitionDetailsViewModel, onMatchClick: (Int) -> Unit) {
    val standing by viewModel.standings.observeAsState()
    val scheduled by viewModel.scheduledMatchesByMatchday.observeAsState()
    val finished by viewModel.finishedMatchesByMatchday.observeAsState()
    val hasError by viewModel.error.observeAsState(false)
    val isFetching by viewModel.isFetching.observeAsState(false)
    val context = LocalContext.current
    val tabs = listOf("Przyszłe mecze", "Tabela", "Wyniki")

    var selectedTabIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(hasError) {
        if (hasError) {
            Toast.makeText(
                context,
                "Wystąpił błąd podczas pobierania danych.",
                Toast.LENGTH_LONG
            ).show()
        }
    }


    if (isFetching) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center

        ) {
            CircularProgressIndicator(
                modifier = Modifier.width(64.dp),
                color = MaterialTheme.colorScheme.secondary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        }
    } else {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF595959))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val flagModel = if (standing?.area?.flag.isNullOrBlank()) {
                        "https://publicdomainvectors.org/photos/Anonymous_Flag_of_the_United_Nations.png"
                    } else {
                        standing?.area?.flag
                    }
                    CrestAsyncImage(
                        flagModel.toString(),
                        standing?.area?.name ?: "Unknown",
                        50
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = standing?.area?.name ?: "Unknown",
                        color = Color.White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.bodyMedium
                    )
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
                    Spacer(modifier = Modifier.width(12.dp))
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
            TabRow(
                selectedTabIndex = selectedTabIndex
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        modifier = Modifier.background(Color(0xFF595959)),
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text=title,
                                color = Color.White
                            )
                        }
                    )
                }
            }
            Box(modifier = Modifier.fillMaxSize()) {
                when (selectedTabIndex) {
                    0 -> ScheduledMatchesContent(scheduled ?: emptyMap(), onMatchClick)
                    1 -> CompetitionTableContent(standing ?: StandingResponse())
                    2 -> ResultsContent(finished ?: emptyMap(), onMatchClick)
                }
            }
        }
    }

}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ScheduledMatchesContent(scheduled: Map<Int?, List<MatchEntry>?>, onMatchClick: (Int) -> Unit) {
    MatchesList(scheduled, onMatchClick)
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ResultsContent(finished: Map<Int?, List<MatchEntry>?>, onMatchClick: (Int) -> Unit) {
    MatchesList(finished, onMatchClick)
}

@Composable
fun CompetitionTableContent(standing: StandingResponse) {
    StandingList(standing.standings)
}

@Composable
@RequiresApi(Build.VERSION_CODES.O)
fun MatchesList(finishedMatches: Map<Int?, List<MatchEntry>?>, onMatchClick: (Int) -> Unit) {
    LazyColumn {
        finishedMatches.forEach { (matchday, matches) ->
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(51.dp)
                        .background(Color(0xFF595959)),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Text(
                        text = when(matchday){
                            100 -> "Finał"
                            99 -> "Półfinał"
                            98 -> "Ćwierćfinał"
                            else -> "Kolejka ${matchday}"
                        },
                        color = Color.White,
                        modifier = Modifier.padding(start = 10.dp)
                    )
                }
            }
            if (matches != null) {
                items(matches) { match ->
                    MatchItem(match, onMatchClick)
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MatchItem(match: MatchEntry, onMatchClick: (Int) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clickable{match.id?.let { id -> onMatchClick(id) }},
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF8F8E8E)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CrestAsyncImage(match.homeTeam.crest ?: "", match.homeTeam.name ?: "Unknown", 35)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = match.homeTeam.shortName ?: "",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1
                )
            }
            Column(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .width(80.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    val homeScore = match.score?.fullTime?.home
                    val awayScore = match.score?.fullTime?.away

                    if (homeScore != null && awayScore != null) {
                        Text(
                            text = "$homeScore : $awayScore",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium
                        )
                    } else {
                        Text(
                            text = "vs",
                            color = Color.White.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                if (match.utcDate != null) {
                    val parsedDate = OffsetDateTime.parse(match.utcDate)
                    val date = parsedDate.format(DateTimeFormatter.ofPattern("dd.MM"))
                    val hour = parsedDate.format(DateTimeFormatter.ofPattern("HH:mm"))

                    Text(
                        text = hour,
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(
                        text = date,
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = match.awayTeam.shortName ?: "",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.width(8.dp))
                CrestAsyncImage(match.awayTeam.crest ?: "", match.awayTeam.name ?: "Unknown", 35)
            }
        }
    }
}

@Composable
fun StandingList(groups: List<Standing>) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.wrapContentWidth()
            ) {
                Text(
                    text = "#",
                    color = Color.White,
                    modifier = Modifier.width(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Drużyna",
                    color = Color.White,
                    modifier = Modifier.width(50.dp)
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.wrapContentWidth()
            ) {
                Text(
                    text = "M",
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "B",
                    color = Color.White,
                    modifier = Modifier.width(45.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "P",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
        LazyColumn {
            groups.forEach { groupEntry ->
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF444444))
                            .padding(vertical = 8.dp, horizontal = 16.dp)
                    ) {
                        Text(
                            text = groupEntry.group ?: "Tabela",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
                items(groupEntry.table) { tableEntry ->
                    StandingItem(tableEntry)
                }
            }
        }
    }
}

@Composable
fun StandingItem(tableEntry: Table) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF8F8E8E)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.wrapContentWidth()
            ) {
                Text(
                    text = tableEntry.position.toString(),
                    color = Color.White,
                    modifier = Modifier.width(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                CrestAsyncImage(tableEntry.team.crest ?: "", tableEntry.team.name ?: "Unknown", 50)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = tableEntry.team.shortName ?: "",
                    color = Color.White
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.wrapContentWidth()
            ) {
                Text(
                    text = tableEntry.playedGames.toString(),
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "${tableEntry.goalsFor}:${tableEntry.goalsAgainst}",
                    color = Color.White,
                    modifier = Modifier.width(45.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "${tableEntry.points}",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
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