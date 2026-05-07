package com.example.pitchside.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.pitchside.R
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.example.pitchside.api.responses.CompetitionResponse
import com.example.pitchside.api.responses.MatchEntry

class HomeFragment : Fragment() {

    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                HomeScreen(
                    viewModel = viewModel,
                    onLeagueClick = { competitionCode: String ->
                        val bundle = Bundle().apply {
                            putString("competitionCode", competitionCode)
                        }
                        findNavController().navigate(R.id.competitionDetailsFragment, bundle)
                    },
                    onMatchClick = { matchId: Int ->
                        val bundle = Bundle().apply {
                            putInt("matchId", matchId)
                        }
                        findNavController().navigate(R.id.matchDetailsFragment, bundle)
                    }
                )
            }
        }
    }
}

@Composable
fun HomeScreen(viewModel: HomeViewModel, onLeagueClick: (String) -> Unit, onMatchClick: (Int) -> Unit) {
    val matches by viewModel.scheduled.observeAsState(emptyList())
    val competitions by viewModel.competitions.observeAsState(emptyList())
    val hasError by viewModel.error.observeAsState(false)
    val isFetching by viewModel.isFetching.observeAsState(false)
    HomeScreenContent(matches, competitions, hasError, isFetching, onLeagueClick, onMatchClick)
}

@Composable
fun HomeScreenContent(
    matches: List<MatchEntry>,
    competitions: List<CompetitionResponse>,
    hasError: Boolean,
    isFetching: Boolean,
    onLeagueClick: (String) -> Unit,
    onMatchClick: (Int) -> Unit
) {
    val context = LocalContext.current
    var competitionsExpanded by remember { mutableStateOf(false) }
    var matchesExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(hasError) {
        if (hasError) {
            Toast.makeText(context, "Wystąpił błąd podczas pobierania danych.", Toast.LENGTH_LONG).show()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        if (isFetching) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    modifier = Modifier.width(64.dp),
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(51.dp)
                    .background(Color(0xFF595959))
                    .clickable { competitionsExpanded = !competitionsExpanded },
                contentAlignment = Alignment.CenterStart,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Ligi", color = Color.White, modifier = Modifier.padding(start = 10.dp))
                    Icon(
                        painter = painterResource(
                            if (competitionsExpanded) R.drawable.outline_arrow_circle_up_24
                            else R.drawable.outline_arrow_circle_down_24
                        ),
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            }

            if (competitionsExpanded) {
                Box(modifier = Modifier.heightIn(max = 300.dp)) {
                    CompetitionsList(competitions, onLeagueClick)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(51.dp)
                    .background(Color(0xFF595959))
                    .clickable { matchesExpanded = !matchesExpanded },
                contentAlignment = Alignment.CenterStart,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(text = "Zaplanowane mecze", color = Color.White, modifier = Modifier.padding(start = 10.dp))
                    Icon(
                        painter = painterResource(
                            if (matchesExpanded) R.drawable.outline_arrow_circle_up_24
                            else R.drawable.outline_arrow_circle_down_24
                        ),
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            }

            if (matchesExpanded) {
                Box(modifier = Modifier.heightIn(max = 400.dp)) {
                    MatchesList(matches, onMatchClick)
                }
            }
        }
    }
}

@Composable
fun CompetitionsList(competitions: List<CompetitionResponse>, onLeagueClick: (String) -> Unit) {
    LazyColumn {
        items(competitions) { competition ->
            CompetitionItem(competition, onLeagueClick)
        }
    }
}

@Composable
fun CompetitionItem(competition: CompetitionResponse, onLeagueClick: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { competition.code?.let { onLeagueClick(it) } },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF8F8E8E))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CrestAsyncImage(competition.emblem ?: "", competition.name ?: "Unknown")
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = competition.name ?: "", color = Color.White)
        }
    }
}

@Composable
fun CrestAsyncImage(url: String, competitionsName: String) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(url)
            .decoderFactory(SvgDecoder.Factory())
            .crossfade(true)
            .build(),
        contentDescription = competitionsName,
        modifier = Modifier.size(50.dp)
    )
}

@Composable
fun MatchesList(matches: List<MatchEntry>, onMatchClick: (Int) -> Unit) {
    LazyColumn {
        items(matches) { match ->
            MatchItem(match, onMatchClick)
        }
    }
}

@Composable
fun MatchItem(match: MatchEntry, onMatchClick: (Int) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                // Bezpieczne wywołanie: wykonaj tylko jeśli id != null
                match.id?.let { id -> onMatchClick(id) }
            },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF8F8E8E))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CrestAsyncImage(match.homeTeam.crest ?: "", match.homeTeam.name ?: "Unknown")
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = match.homeTeam.shortName ?: "", color = Color.White)
            }
            Text(text = "-", color = Color.White)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = match.awayTeam.shortName ?: "", color = Color.White)
                Spacer(modifier = Modifier.width(12.dp))
                CrestAsyncImage(match.awayTeam.crest ?: "", match.awayTeam.name ?: "Unknown")
            }
        }
    }
}