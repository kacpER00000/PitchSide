package com.example.pitchside.ui.scheduled

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
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
import androidx.navigation.fragment.findNavController
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.example.pitchside.R
import com.example.pitchside.api.responses.MatchEntry
import com.example.pitchside.managers.SessionManager

class ScheduledFragment : Fragment() {

    private val viewModel: ScheduledViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                ScheduledScreen(
                    viewModel = viewModel,
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
fun ScheduledScreen(viewModel: ScheduledViewModel, onMatchClick: (Int) -> Unit) {
    val matches by viewModel.scheduled.observeAsState(emptyList())
    val favoriteIds by viewModel.favoriteIds.observeAsState(emptySet())
    val hasError by viewModel.error.observeAsState(false)
    val isFetching by viewModel.isFetching.observeAsState(true)

    ScheduledScreenContent(
        matches = matches,
        favoriteIds = favoriteIds,
        hasError = hasError,
        isFetching = isFetching,
        onMatchClick = onMatchClick,
        onFavoriteToggle = { viewModel.toggleFavorite(it) }
    )
}

@Composable
fun ScheduledScreenContent(
    matches: List<MatchEntry>,
    favoriteIds: Set<Int>,
    hasError: Boolean,
    isFetching: Boolean,
    onMatchClick: (Int) -> Unit,
    onFavoriteToggle: (MatchEntry) -> Unit
) {
    val context = LocalContext.current
    LaunchedEffect(hasError) {
        if (hasError) {
            Toast.makeText(context, "Wystąpił błąd podczas pobierania danych.", Toast.LENGTH_LONG).show()
        }
    }
    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF121212))) {
        ScheduledHeader()
        if (isFetching) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary)
            }
        } else {
            MatchesList(matches, favoriteIds, onMatchClick, onFavoriteToggle)
        }
    }
}

@Composable
fun ScheduledHeader() {
    Box(
        modifier = Modifier.fillMaxWidth().height(51.dp).background(Color(0xFF595959)),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = "Zaplanowane mecze",
            color = Color.White,
            modifier = Modifier.padding(start = 16.dp),
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun MatchesList(
    matches: List<MatchEntry>,
    favoriteIds: Set<Int>,
    onMatchClick: (Int) -> Unit,
    onFavoriteToggle: (MatchEntry) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(matches) { match ->
            val isFavorite = match.id?.let { favoriteIds.contains(it) } ?: false
            MatchItem(match, isFavorite, onMatchClick, onFavoriteToggle)
        }
    }
}

@Composable
fun MatchItem(
    match: MatchEntry,
    isFavorite: Boolean,
    onMatchClick: (Int) -> Unit,
    onFavoriteToggle: (MatchEntry) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clickable { match.id?.let { onMatchClick(it) } }, // Twoja nawigacja
        colors = CardDefaults.cardColors(containerColor = Color(0xFF8F8E8E))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Home Team
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                TeamCrestImage(match.homeTeam.crest ?: "", match.homeTeam.name ?: "Unknown")
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = match.homeTeam.shortName ?: "", color = Color.White, fontSize = 14.sp)
            }

            Text(text = "-", color = Color.White, modifier = Modifier.padding(horizontal = 8.dp))

            // Away Team
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.End) {
                Text(text = match.awayTeam.shortName ?: "", color = Color.White, fontSize = 14.sp)
                Spacer(modifier = Modifier.width(12.dp))
                TeamCrestImage(match.awayTeam.crest ?: "", match.awayTeam.name ?: "Unknown")
            }

            // Przycisk gwiazdki od kolegów
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
fun TeamCrestImage(url: String, teamName: String) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(url)
            .decoderFactory(SvgDecoder.Factory())
            .crossfade(true)
            .build(),
        contentDescription = teamName,
        modifier = Modifier.size(30.dp) // Zmniejszyłem trochę, żeby gwiazdka się mieściła
    )
}