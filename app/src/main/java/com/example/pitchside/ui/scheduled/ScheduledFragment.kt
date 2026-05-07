package com.example.pitchside.ui.scheduled

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
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
                ScheduledScreen(viewModel)
            }
        }
    }
}

@Composable
fun ScheduledScreen(viewModel: ScheduledViewModel) {
    val matches by viewModel.scheduled.observeAsState(emptyList())
    val favoriteIds by viewModel.favoriteIds.observeAsState(emptySet()) // DODANO
    val hasError by viewModel.error.observeAsState(false)
    val isFetching by viewModel.isFetching.observeAsState(true)

    ScheduledScreenContent(
        matches = matches,
        favoriteIds = favoriteIds, // DODANO
        hasError = hasError,
        isFetching = isFetching,
        onFavoriteToggle = { viewModel.toggleFavorite(it) } // DODANO
    )
}

@Composable
fun ScheduledScreenContent(
    matches: List<MatchEntry>,
    favoriteIds: Set<Int>, // DODANO
    hasError: Boolean,
    isFetching: Boolean,
    onFavoriteToggle: (MatchEntry) -> Unit // DODANO
) {
    val context = LocalContext.current
    LaunchedEffect(hasError) {
        if (hasError) {
            Toast.makeText(context, "Wystąpił błąd podczas pobierania danych.", Toast.LENGTH_LONG).show()
        }
    }
    Column(modifier = Modifier.fillMaxSize()) {
        ScheduledHeader()
        if (isFetching) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    modifier = Modifier.width(64.dp),
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        } else {
            MatchesList(matches, favoriteIds, onFavoriteToggle) // ZAKTUALIZOWANO
        }
    }
}

@Composable
fun ScheduledHeader() {
    Box(
        modifier = Modifier.fillMaxWidth().height(51.dp).background(Color(0xFF595959)),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(text = "Zaplanowane mecze", color = Color.White, modifier = Modifier.padding(start = 10.dp))
    }
}

@Composable
fun MatchesList(
    matches: List<MatchEntry>,
    favoriteIds: Set<Int>, // DODANO
    onFavoriteToggle: (MatchEntry) -> Unit // DODANO
) {
    LazyColumn {
        items(matches) { match ->
            // Sprawdzamy czy ID meczu jest w zestawie ulubionych
            val isFavorite = match.id?.let { favoriteIds.contains(it) } ?: false
            MatchItem(match, isFavorite, onFavoriteToggle)
        }
    }
}

@Composable
fun MatchItem(
    match: MatchEntry,
    isFavorite: Boolean, // DODANO
    onFavoriteToggle: (MatchEntry) -> Unit // DODANO
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF8F8E8E))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TeamCrestImage(match.homeTeam.crest ?: "", match.homeTeam.name ?: "Unknown")
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = match.homeTeam.shortName ?: "", color = Color.White)
            }

            Text(text = "-", color = Color.White)

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = match.awayTeam.shortName ?: "", color = Color.White)
                Spacer(modifier = Modifier.width(12.dp))
                TeamCrestImage(match.awayTeam.crest ?: "", match.awayTeam.name ?: "Unknown")
            }

            // DODANO: Przycisk gwiazdki
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
        modifier = Modifier.size(50.dp)
    )
}