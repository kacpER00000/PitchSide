package com.example.pitchside.ui.scheduled

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.pitchside.api.responses.MatchEntry
import com.example.pitchside.api.responses.TeamResponse
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import coil.decode.SvgDecoder
import coil.request.ImageRequest

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
fun ScheduledScreen(viewModel: ScheduledViewModel){
    val matches by viewModel.scheduled.observeAsState(emptyList())
    val hasError by viewModel.error.observeAsState(false)
    val isFetching by viewModel.isFetching.observeAsState(true)
    ScheduledScreenContent(matches, hasError, isFetching)
}

@Composable
fun ScheduledScreenContent(matches: List<MatchEntry>, hasError: Boolean, isFetching: Boolean){
    val context = LocalContext.current
    LaunchedEffect(hasError) {
        if(hasError){
            Toast.makeText(
                context,
                "Wystąpił błąd podczas pobierania danych.",
                Toast.LENGTH_LONG
            ).show()
        }
    }
    Column(modifier = Modifier.fillMaxSize()) {
        ScheduledHeader()
        if(isFetching){
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
            MatchesList(matches)
        }
    }
}

@Composable
fun ScheduledHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(51.dp)
            .background(Color(0xFF595959)),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = "Zaplanowane mecze",
            color = Color.White,
            modifier = Modifier.padding(start = 10.dp)
        )
    }
}

@Composable
fun MatchesList(matches: List<MatchEntry>){
    LazyColumn {
        items(matches){ match ->
            MatchItem(match)
        }
    }
}

@Composable
fun MatchItem(match: MatchEntry) {
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                TeamCrestImage(match.homeTeam.crest ?: "", match.homeTeam.name ?: "Unknown")
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = match.homeTeam.shortName ?: "", color = Color.White)
            }

            Text(text = "-", color = Color.White)

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = match.awayTeam.shortName ?: "", color = Color.White)
                Spacer(modifier = Modifier.width(12.dp))
                TeamCrestImage(match.awayTeam.crest ?: "", match.awayTeam.name ?: "Unknown")
            }
        }
    }
}

@Composable
fun TeamCrestImage(
    url: String,
    teamName: String
) {
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
