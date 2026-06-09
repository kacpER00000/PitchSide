package com.example.pitchside.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.example.pitchside.R
import com.example.pitchside.data.League
import com.example.pitchside.data.MatchDao
import com.example.pitchside.data.Resource
import com.example.pitchside.managers.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

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
                    onLeagueClick = { competitionCode ->
                        val bundle = Bundle().apply {
                            putString("competitionCode", competitionCode)
                        }
                        findNavController().navigate(R.id.competitionDetailsFragment, bundle)
                    },
                    onMatchClick = { matchId ->
                        val bundle = Bundle().apply {
                            putInt("matchId", matchId)
                        }
                        findNavController().navigate(R.id.matchDetailsFragment, bundle)
                    }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        requireActivity().findViewById<androidx.appcompat.widget.Toolbar>(R.id.top_toolbar)
            ?.setTitleTextColor(android.graphics.Color.parseColor("#D4AF37"))
    }

    override fun onPause() {
        super.onPause()
        requireActivity().findViewById<androidx.appcompat.widget.Toolbar>(R.id.top_toolbar)
            ?.setTitleTextColor(android.graphics.Color.WHITE)
    }
}

@Composable
fun HomeScreen(viewModel: HomeViewModel, onLeagueClick: (String) -> Unit, onMatchClick: (Int) -> Unit) {
    val matches = viewModel.scheduled.observeAsState(initial = Resource.Loading).value
    val competitions = viewModel.competitions.observeAsState(initial = Resource.Loading).value
    val favoriteIds by viewModel.favoriteIds.observeAsState(emptySet())
    val favoriteLeagueIds by viewModel.favoriteLeagueIds.observeAsState(emptySet())
    val isFetching by viewModel.isFetching.observeAsState(false)
    val hasError by viewModel.error.observeAsState(false)


    when {
        isFetching || matches is Resource.Loading || competitions is Resource.Loading -> {
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
        }

        hasError || matches is Resource.Error || competitions is Resource.Error -> {
            Toast.makeText(LocalContext.current, "An error occurred while fetching data.", Toast.LENGTH_LONG).show()
        }

        matches is Resource.Success && competitions is Resource.Success -> {
            HomeScreenContent(
                matches = matches.data,
                competitions = competitions.data,
                favoriteIds = favoriteIds,
                favoriteLeagueIds = favoriteLeagueIds,
                onLeagueClick = onLeagueClick,
                onMatchClick = onMatchClick,
                onFavoriteToggle = { match -> viewModel.toggleFavorite(match) },
                onLeagueFavoriteToggle = { competition -> viewModel.toggleFavoriteLeague(competition) }
            )
        }
    }


}

@Composable
fun SearchBarWithDropdown(
    competitions: List<League>,
    onLeagueClick: (String) -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }
    var dropdownExpanded by remember { mutableStateOf(false) }
    var filteredCompetitions by remember { mutableStateOf(emptyList<League>()) }

    LaunchedEffect(query, competitions) {
        if (query.isBlank()) {
            filteredCompetitions = emptyList()
            dropdownExpanded = false
        } else {
            delay(200)
            filteredCompetitions = withContext(Dispatchers.Default) {
                competitions.filter { it.nazwa_ligi?.contains(query, ignoreCase = true) == true }
            }
            dropdownExpanded = filteredCompetitions.isNotEmpty()
        }
    }

    Box(modifier = Modifier.fillMaxWidth().zIndex(1f)) {
        OutlinedTextField(
            value = query,
            onValueChange = { newValue ->
                query = newValue
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            placeholder = { Text(text = "Search for a league...") },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Search, contentDescription = "Search icon", tint = Color(0xFF111111))
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = {
                        query = ""
                        dropdownExpanded = false
                    }) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", tint = Color(0xFF111111))
                    }
                }
            },
            singleLine = true,
            maxLines = 1,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFD4AF37),
                unfocusedBorderColor = Color(0xFF111111),
                cursorColor = Color(0xFFD4AF37)
            )
        )

        if (dropdownExpanded && filteredCompetitions.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 64.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column {
                    filteredCompetitions.take(5).forEach { league ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    query = ""
                                    dropdownExpanded = false
                                    league.kod_ligi?.let { onLeagueClick(it) }
                                }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(league.emblemat_ligi)
                                    .decoderFactory(SvgDecoder.Factory())
                                    .crossfade(true)
                                    .build(),
                                contentDescription = null,
                                modifier = Modifier.size(30.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = league.nazwa_ligi ?: "Unknown", color = Color.Black)
                        }
                        Divider(color = Color.LightGray, thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}

@Composable
fun HomeScreenContent(
    matches: List<MatchDao.MatchWithTeams>,
    competitions: List<League>,
    favoriteIds: Set<Int>,
    favoriteLeagueIds: Set<Int>,
    onLeagueClick: (String) -> Unit,
    onMatchClick: (Int) -> Unit,
    onFavoriteToggle: (MatchDao.MatchWithTeams) -> Unit,
    onLeagueFavoriteToggle: (League) -> Unit
) {
    var competitionsExpanded by remember { mutableStateOf(false) }
    var matchesExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        SearchBarWithDropdown(
            competitions = competitions,
            onLeagueClick = onLeagueClick
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(51.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF111111))
                .clickable { competitionsExpanded = !competitionsExpanded },
            contentAlignment = Alignment.CenterStart,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Leagues", color = Color(0xFFD4AF37))
                Icon(
                    painter = painterResource(
                        if (competitionsExpanded) R.drawable.outline_arrow_circle_up_24
                        else R.drawable.outline_arrow_circle_down_24
                    ),
                    contentDescription = null,
                    tint = Color(0xFFD4AF37)
                )
            }
        }

        if (competitionsExpanded) {
            Box(modifier = Modifier.heightIn(max = 300.dp)) {
                CompetitionsList(competitions, onLeagueClick, favoriteLeagueIds, onLeagueFavoriteToggle)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(51.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF111111))
                .clickable { matchesExpanded = !matchesExpanded },
            contentAlignment = Alignment.CenterStart,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Scheduled matches", color = Color(0xFFD4AF37))
                Icon(
                    painter = painterResource(
                        if (matchesExpanded) R.drawable.outline_arrow_circle_up_24
                        else R.drawable.outline_arrow_circle_down_24
                    ),
                    contentDescription = null,
                    tint = Color(0xFFD4AF37)
                )
            }
        }
        if (matchesExpanded) {
            Box(modifier = Modifier.heightIn(max = 400.dp)) {
                MatchesList(matches, favoriteIds, onMatchClick, onFavoriteToggle)
            }
        }
    }
}

@Composable
fun CompetitionsList(
    competitions: List<League>,
    onLeagueClick: (String) -> Unit,
    favoriteLeagueIds: Set<Int>,
    onLeagueFavoriteToggle: (League) -> Unit
) {
    LazyColumn {
        items(
            items = competitions,
            key = { competition -> competition.liga_id }
        ) { competition ->
            CompetitionItem(
                competition,
                onLeagueClick,
                favoriteLeagueIds.contains(competition.liga_id),
                onLeagueFavoriteToggle
            )
        }
    }
}

@Composable
fun CompetitionItem(
    competition: League,
    onLeagueClick: (String) -> Unit,
    isFavorite: Boolean,
    onFavoriteToggle: (League) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { competition.kod_ligi?.let { onLeagueClick(it) } },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111111)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CrestAsyncImage(competition.emblemat_ligi ?: "", competition.nazwa_ligi ?: "Unknown")
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = competition.nazwa_ligi ?: "", color = Color(0xFFD4AF37))
            }

            if (SessionManager.isLoggedIn()) {
                IconButton(onClick = { onFavoriteToggle(competition) }) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = "Favorites",
                        tint = if (isFavorite) Color(0xFFD4AF37) else Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun MatchesList(
    matches: List<MatchDao.MatchWithTeams>,
    favoriteIds: Set<Int>,
    onMatchClick: (Int) -> Unit,
    onFavoriteToggle: (MatchDao.MatchWithTeams) -> Unit
) {
    LazyColumn {
        items(
            items = matches,
            key = { match -> match.matchId ?: match.hashCode() }
        ) { match ->
            val isFav = match.matchId?.let { favoriteIds.contains(it) } ?: false
            MatchItem(match, isFav, onMatchClick, onFavoriteToggle)
        }
    }
}

@Composable
fun MatchItem(
    match: MatchDao.MatchWithTeams,
    isFavorite: Boolean,
    onMatchClick: (Int) -> Unit,
    onFavoriteToggle: (MatchDao.MatchWithTeams) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                match.matchId?.let { id -> onMatchClick(id) }
            },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111111)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                CrestAsyncImage(match.homeTeamCrest ?: "", match.homeTeamName ?: "Unknown")
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = match.homeTeamName ?: "", color = Color(0xFFD4AF37))
            }
            Text(text = "-", color = Color(0xFFD4AF37), modifier = Modifier.padding(horizontal = 4.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.End) {
                Text(text = match.awayTeamName ?: "", color = Color(0xFFD4AF37))
                Spacer(modifier = Modifier.width(8.dp))
                CrestAsyncImage(match.awayTeamCrest ?: "", match.awayTeamName ?: "Unknown")
            }

            if (SessionManager.isLoggedIn()) {
                IconButton(onClick = { onFavoriteToggle(match) }) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = "Favorites",
                        tint = if (isFavorite) Color(0xFFD4AF37) else Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun CrestAsyncImage(url: String, contentDesc: String) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(url)
            .decoderFactory(SvgDecoder.Factory())
            .crossfade(true)
            .build(),
        contentDescription = contentDesc,
        modifier = Modifier.size(50.dp)
    )
}
