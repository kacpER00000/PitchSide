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
}

@Composable
fun HomeScreen(viewModel: HomeViewModel, onLeagueClick: (String) -> Unit, onMatchClick: (Int) -> Unit) {
    val matches by viewModel.scheduled.observeAsState(emptyList())
    val competitions by viewModel.competitions.observeAsState(emptyList())
    val favoriteIds by viewModel.favoriteIds.observeAsState(emptySet())
    val favoriteLeagueIds by viewModel.favoriteLeagueIds.observeAsState(emptySet())
    val hasError by viewModel.error.observeAsState(false)
    val isFetching by viewModel.isFetching.observeAsState(false)

    HomeScreenContent(
        matches = matches,
        competitions = competitions,
        favoriteIds = favoriteIds,
        favoriteLeagueIds = favoriteLeagueIds,
        hasError = hasError,
        isFetching = isFetching,
        onLeagueClick = onLeagueClick,
        onMatchClick = onMatchClick,
        onFavoriteToggle = { match -> viewModel.toggleFavorite(match) },
        onLeagueFavoriteToggle = { competition -> viewModel.toggleFavoriteLeague(competition) }
    )
}

// IZOLOWANA WYSZUKIWARKA: Z naprawionym "Fake Dropdownem", który rozwiązuje lag renderowania!
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
            delay(200) // Wystarczy 200ms dla idealnej płynności
            filteredCompetitions = withContext(Dispatchers.Default) {
                competitions.filter { it.nazwa_ligi?.contains(query, ignoreCase = true) == true }
            }
            dropdownExpanded = filteredCompetitions.isNotEmpty()
        }
    }

    // Dodano zIndex(1f), aby podpowiedzi z "Fake Dropdownu" wyświetlały się na wierzchu, zasłaniając inne elementy pod spodem
    Box(modifier = Modifier.fillMaxWidth().zIndex(1f)) {
        OutlinedTextField(
            value = query,
            onValueChange = { newValue ->
                query = newValue
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            placeholder = { Text(text = "Wyszukaj ligę...") },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Search, contentDescription = "Ikona szukania")
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = {
                        query = ""
                        dropdownExpanded = false
                    }) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Wyczyść")
                    }
                }
            },
            singleLine = true,
            maxLines = 1,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF595959),
                unfocusedBorderColor = Color.LightGray
            )
        )

        // TO JEST NOWY MAGICZNY FIX: Zwykła, lekka Karta zamiast ciężkiego DropdownMenu!
        if (dropdownExpanded && filteredCompetitions.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 64.dp), // To opuszcza kartę DOKŁADNIE pod linię pola tekstowego
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
                            Text(text = league.nazwa_ligi ?: "Nieznana", color = Color.Black)
                        }
                        Divider(color = Color.LightGray, thickness = 0.5.dp) // Elegancka linia oddzielająca
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
    hasError: Boolean,
    isFetching: Boolean,
    onLeagueClick: (String) -> Unit,
    onMatchClick: (Int) -> Unit,
    onFavoriteToggle: (MatchDao.MatchWithTeams) -> Unit,
    onLeagueFavoriteToggle: (League) -> Unit
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
        modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {

        SearchBarWithDropdown(
            competitions = competitions,
            onLeagueClick = onLeagueClick
        )

        if (isFetching) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary)
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
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Ligi", color = Color.White)
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
                    CompetitionsList(competitions, onLeagueClick, favoriteLeagueIds, onLeagueFavoriteToggle)
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
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Zaplanowane mecze", color = Color.White)
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
                    MatchesList(matches, favoriteIds, onMatchClick, onFavoriteToggle)
                }
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
        colors = CardDefaults.cardColors(containerColor = Color(0xFF8F8E8E))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CrestAsyncImage(competition.emblemat_ligi ?: "", competition.nazwa_ligi ?: "Unknown")
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = competition.nazwa_ligi ?: "", color = Color.White)
            }

            if (SessionManager.isLoggedIn()) {
                IconButton(onClick = { onFavoriteToggle(competition) }) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = "Ulubione",
                        tint = if (isFavorite) Color.Yellow else Color.White
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
        colors = CardDefaults.cardColors(containerColor = Color(0xFF8F8E8E))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                CrestAsyncImage(match.homeTeamCrest ?: "", match.homeTeamName ?: "Unknown")
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = match.homeTeamName ?: "", color = Color.White)
            }
            Text(text = "-", color = Color.White, modifier = Modifier.padding(horizontal = 4.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.End) {
                Text(text = match.awayTeamName ?: "", color = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                CrestAsyncImage(match.awayTeamCrest ?: "", match.awayTeamName ?: "Unknown")
            }

            if (SessionManager.isLoggedIn()) {
                IconButton(onClick = { onFavoriteToggle(match) }) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = "Ulubione",
                        tint = if (isFavorite) Color.Yellow else Color.White
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