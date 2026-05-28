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
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import com.example.pitchside.data.LeagueScorerDao
import com.example.pitchside.data.LeagueTableDao
import com.example.pitchside.data.MatchDao
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
        viewModel.fetchData()
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
    val leagueInfo by viewModel.leagueInfo.observeAsState()
    val standing by viewModel.standingsByGroups.observeAsState()
    val scheduled by viewModel.scheduledMatchesByMatchday.observeAsState()
    val finished by viewModel.finishedMatchesByMatchday.observeAsState()
    val scorers by viewModel.scorers.observeAsState()
    val hasError by viewModel.error.observeAsState(false)
    val isFetching by viewModel.isFetching.observeAsState(false)
    val context = LocalContext.current
    val tabs = listOf("Przyszłe mecze", "Tabela", "Strzelcy", "Wyniki")

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
            modifier = Modifier.fillMaxSize().background(Color.White),
            contentAlignment = Alignment.Center

        ) {
            CircularProgressIndicator(
                modifier = Modifier.width(64.dp),
                color = Color(0xFFD4AF37),
                trackColor = Color(0xFF111111),
            )
        }
    } else {
        Column(
            modifier = Modifier.fillMaxSize().background(Color.White),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF111111))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val flagModel = if (leagueInfo?.flaga_kraju.isNullOrBlank()) {
                        "https://publicdomainvectors.org/photos/Anonymous_Flag_of_the_United_Nations.png"
                    } else {
                        leagueInfo?.flaga_kraju
                    }
                    CrestAsyncImage(
                        flagModel.toString(),
                        leagueInfo?.kraj ?: "Unknown",
                        50
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = leagueInfo?.kraj ?: "Unknown",
                        color = Color.White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    thickness = 1.dp,
                    color = Color.DarkGray
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CrestAsyncImage(
                        leagueInfo?.emblemat_ligi ?: "",
                        leagueInfo?.nazwa_ligi ?: "Unknown",
                        50
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = leagueInfo?.nazwa_ligi ?: "Unknown",
                        color = Color(0xFFD4AF37),
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
                                tint = if (isFavorite) Color(0xFFD4AF37) else Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    thickness = 1.dp,
                    color = Color.DarkGray
                )
            }
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color(0xFF111111),
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = Color(0xFFD4AF37)
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        modifier = Modifier.background(Color(0xFF111111)),
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                color = if (selectedTabIndex == index) Color(0xFFD4AF37) else Color.White
                            )
                        }
                    )
                }
            }
            Box(modifier = Modifier.fillMaxSize()) {
                when (selectedTabIndex) {
                    0 -> ScheduledMatchesContent(scheduled ?: emptyMap(), onMatchClick)
                    1 -> CompetitionTableContent(standing ?: emptyMap())
                    2 -> TopScorersList(scorers ?: emptyList())
                    3 -> ResultsContent(finished ?: emptyMap(), onMatchClick)
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ScheduledMatchesContent(scheduled: Map<Int?, List<MatchDao.MatchWithTeams>?>, onMatchClick: (Int) -> Unit) {
    MatchesList(scheduled, onMatchClick)
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ResultsContent(finished: Map<Int?, List<MatchDao.MatchWithTeams>?>, onMatchClick: (Int) -> Unit) {
    MatchesList(finished, onMatchClick)
}

@Composable
fun CompetitionTableContent(standing: Map<String, List<LeagueTableDao.LeagueTableWithTeam>>) {
    StandingList(standing)
}

@Composable
@RequiresApi(Build.VERSION_CODES.O)
fun MatchesList(finishedMatches: Map<Int?, List<MatchDao.MatchWithTeams>?>, onMatchClick: (Int) -> Unit) {
    LazyColumn {
        finishedMatches.forEach { (matchday, matches) ->
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(51.dp)
                        .background(Color.White),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Text(
                        text = when(matchday){
                            100 -> "Finał"
                            99 -> "Półfinał"
                            98 -> "Ćwierćfinał"
                            else -> "Kolejka $matchday"
                        },
                        color = Color(0xFF111111),
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
fun MatchItem(match: MatchDao.MatchWithTeams, onMatchClick: (Int) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clickable { onMatchClick(match.matchId) },
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF111111)
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
                CrestAsyncImage(match.homeTeamCrest ?: "", match.homeTeamName ?: "Unknown", 35)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = match.homeTeamName ?: "",
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
                    val homeScore = match.homeTeamScore
                    val awayScore = match.awayTeamScore

                    if (homeScore != null && awayScore != null) {
                        Text(
                            text = "$homeScore : $awayScore",
                            color = Color(0xFFD4AF37),
                            style = MaterialTheme.typography.titleMedium
                        )
                    } else {
                        Text(
                            text = "vs",
                            color = Color(0xFFD4AF37).copy(alpha = 0.7f),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                if (match.startDate != null) {
                    val parsedDate = OffsetDateTime.parse(match.startDate)
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
                    text = match.awayTeamName ?: "",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.width(8.dp))
                CrestAsyncImage(match.awayTeamCrest ?: "", match.awayTeamName ?: "Unknown", 35)
            }
        }
    }
}

@Composable
fun StandingList(standing: Map<String, List<LeagueTableDao.LeagueTableWithTeam>>) {
    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
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
                    .padding(start = 10.dp)
            ) {
                Text(
                    text = "#",
                    color = Color(0xFF111111),
                    modifier = Modifier.width(24.dp)
                )
                Spacer(modifier = Modifier.width(18.dp))
                Text(
                    text = "Drużyna",
                    color = Color(0xFF111111),
                    modifier = Modifier.width(60.dp)
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.wrapContentWidth()
                    .padding(end = 15.dp)
            ) {
                Text(
                    text = "M",
                    color = Color(0xFF111111)
                )
                Spacer(modifier = Modifier.width(32.dp))
                Text(
                    text = "B",
                    color = Color(0xFF111111),
                )
                Spacer(modifier = Modifier.width(45.dp))
                Text(
                    text = "P",
                    color = Color(0xFF111111),
                )
            }
        }
        LazyColumn {
            standing.forEach { groupEntry ->
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(vertical = 8.dp, horizontal = 16.dp)
                    ) {
                        Text(
                            text = groupEntry.key,
                            color = Color(0xFF111111),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
                items(groupEntry.value) { tableEntry ->
                    StandingItem(tableEntry)
                }
            }
        }
    }
}

@Composable
fun StandingItem(tableEntry: LeagueTableDao.LeagueTableWithTeam) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF111111)
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
                CrestAsyncImage(tableEntry.teamEmblem ?: "", tableEntry.teamName ?: "Unknown", 50)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = tableEntry.teamName ?: "",
                    color = Color.White
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.wrapContentWidth()
            ) {
                Text(
                    text = tableEntry.playedMatches.toString(),
                    color = Color(0xFFD4AF37)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "${tableEntry.goalsFor}:${tableEntry.goalsAgainst}",
                    color = Color(0xFFD4AF37),
                    modifier = Modifier.width(45.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "${tableEntry.points}",
                    color = Color(0xFFD4AF37),
                )
            }
        }
    }
}

@Composable
fun TopScorersList(scorers: List<LeagueScorerDao.LeagueScorerWithTeam>) {
    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
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
                    .padding(start = 10.dp)
            ) {
                Text(
                    text = "#",
                    color = Color(0xFF111111),
                    modifier = Modifier.width(24.dp)
                )
                Spacer(modifier = Modifier.width(18.dp))
                Text(
                    text = "Zawodnik",
                    color = Color(0xFF111111),
                    modifier = Modifier.width(60.dp)
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.wrapContentWidth()
                    .padding(end = 13.dp)
            ) {
                Text(
                    text = "B",
                    color = Color(0xFF111111)
                )
                Spacer(modifier = Modifier.width(20.dp))
                Text(
                    text = "A",
                    color = Color(0xFF111111),
                )
            }
        }
        LazyColumn {
            itemsIndexed(scorers) { index, scorerEntry ->
                TopScorerItem(scorerEntry, position = index + 1)
            }
        }
    }
}

@Composable
fun TopScorerItem(scorerEntry: LeagueScorerDao.LeagueScorerWithTeam, position: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF111111)
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
                    text = "$position.",
                    color = Color.White,
                    modifier = Modifier.width(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                CrestAsyncImage(scorerEntry.teamEmblem ?: "", scorerEntry.teamName ?: "Unknown", 50)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = scorerEntry.playerName,
                    color = Color.White
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.wrapContentWidth()
            ) {
                Text(
                    text = scorerEntry.goals.toString(),
                    color = Color(0xFFD4AF37)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = scorerEntry.assists.toString(),
                    color = Color(0xFFD4AF37),
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