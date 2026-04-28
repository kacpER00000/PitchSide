package com.example.pitchside.ui.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import coil.compose.AsyncImage
import com.example.pitchside.api.responses.MatchEntry
import com.example.pitchside.api.responses.StandingResponse
import com.example.pitchside.api.responses.Table
import kotlin.getValue

class CompetitionDetailsFragment : Fragment() {
    private val viewModel: CompetitionDetailsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val competitionCode = arguments?.getString("competitionCode") ?: "Unknown"
        viewModel.setCompetitionCode(competitionCode)
        viewModel.fetchAllData()
        return ComposeView(requireContext()).apply{
            setContent { CompetitionDetailsScreen(viewModel) }
        }
    }
}

@Composable
fun CompetitionDetailsScreen(viewModel: CompetitionDetailsViewModel){
    val standing by viewModel.standings.observeAsState()
    val scheduled by viewModel.scheduled.observeAsState()
    val finished by viewModel.finished.observeAsState()
    val hasError by viewModel.error.observeAsState(false)
    val isFetching by viewModel.isFetching.observeAsState(false)
    val context = LocalContext.current
    val tabs = listOf("Przyszłe mecze", "Tabela", "Wyniki")

    var selectedTabIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(hasError) {
        if(hasError){
            Toast.makeText(
                context,
                "Wystąpił błąd podczas pobierania danych.",
                Toast.LENGTH_LONG
            ).show()
        }
    }


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
        Column(modifier = Modifier.fillMaxSize(),
        ) {
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }
            Box(modifier = Modifier.fillMaxSize()) {
                when (selectedTabIndex) {
                    0 -> ScheduledMatchesContent(scheduled!!)
                    1 -> CompetitionTableContent(standing!!)
                    2 -> ResultsContent(finished!!)
                }
            }
        }
    }

}

@Composable
fun ScheduledMatchesContent(scheduled: List<MatchEntry>){
    MatchesList(scheduled)
}

@Composable
fun ResultsContent(finished: List<MatchEntry>){
    MatchesList(finished)
}

@Composable
fun CompetitionTableContent(standing: StandingResponse){
    StandingList(standing.standings[0].table)
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

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if(match.score?.fullTime?.home != null){
                    Text(text = match.score.fullTime.home.toString(), color = Color.White)
                }
                Text(text = "-", color = Color.White)
                if(match.score?.fullTime?.away != null){
                    Text(text = match.score.fullTime.away.toString(), color = Color.White)
                }
            }

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
        model = url,
        contentDescription = teamName,
        modifier = Modifier.size(50.dp)
    )
}

@Composable
fun StandingList(table: List<Table>){
    LazyColumn {
        items(table) {tableEntry ->
            StandingItem(tableEntry)
        }
    }
}

@Composable
fun StandingItem(tableEntry: Table){
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF8F8E8E)
        )
    ){
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ){
            Text(
                text = tableEntry.position.toString(),
                color = Color.White
            )
            Spacer(modifier = Modifier.width(5.dp))
            TeamCrestImage(tableEntry.team.crest ?: " ", tableEntry.team.name ?: "Unknown")
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = tableEntry.team.shortName ?: "",
                color = Color.White
            )
        }
    }
}