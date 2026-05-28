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
import androidx.navigation.fragment.findNavController
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.example.pitchside.R
import com.example.pitchside.data.MatchDao
import com.example.pitchside.managers.SessionManager
import com.example.pitchside.ui.home.CrestAsyncImage

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

    ScheduledScreenContent(
        matches = matches,
        favoriteIds = favoriteIds,
        hasError = hasError,
        onMatchClick = onMatchClick,
        onFavoriteToggle = { viewModel.toggleFavorite(it) }
    )
}

@Composable
fun ScheduledScreenContent(
    matches: List<MatchDao.MatchWithTeams>,
    favoriteIds: Set<Int>,
    hasError: Boolean,
    onMatchClick: (Int) -> Unit,
    onFavoriteToggle: (MatchDao.MatchWithTeams) -> Unit
) {
    val context = LocalContext.current
    LaunchedEffect(hasError) {
        if (hasError) {
            Toast.makeText(context, "Wystąpił błąd podczas pobierania danych.", Toast.LENGTH_LONG).show()
        }
    }
    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        ScheduledHeader()
        MatchesList(matches, favoriteIds, onMatchClick, onFavoriteToggle)
    }
}

@Composable
fun ScheduledHeader() {
    Box(
        modifier = Modifier.fillMaxWidth().height(51.dp).background(Color(0xFF111111)),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = "Zaplanowane mecze",
            color = Color(0xFFD4AF37),
            modifier = Modifier.padding(start = 16.dp),
            fontWeight = FontWeight.Bold
        )
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
        items(matches) { match ->
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
                onMatchClick(match.matchId)
            },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111111))
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
                        contentDescription = "Ulubione",
                        tint = if (isFavorite) Color(0xFFD4AF37) else Color.White
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
        modifier = Modifier.size(30.dp)
    )
}