package com.example.pitchside.ui.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
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
import com.example.pitchside.ui.home.CrestAsyncImage
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
    val scheduled by viewModel.scheduledMatchesByMatchday.observeAsState()
    val finished by viewModel.finishedMatchesByMatchday.observeAsState()
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
                    CrestAsyncImage(
                        standing?.area?.flag ?: "",
                        standing?.area?.name ?: "Unknown",
                        30
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = standing?.area?.name ?: "Unknown",
                        color = Color.White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
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
                }
            }
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
                    2 -> ResultsContent(finished ?: emptyMap())
                }
            }
        }
    }

}

@Composable
fun ScheduledMatchesContent(scheduled: Map<Int?, List<MatchEntry>?>){
    MatchesList(scheduled)
}

@Composable
fun ResultsContent(finished: Map<Int?, List<MatchEntry>?>){
    MatchesList(finished)
}

@Composable
fun CompetitionTableContent(standing: StandingResponse){
    StandingList(standing.standings[0].table)
}


@Composable
fun MatchesList(finishedMatches: Map<Int?, List<MatchEntry>?>){
    LazyColumn {
        finishedMatches.forEach { (matchday,matches) ->
            item{
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .height(51.dp)
                        .background(Color(0xFF595959)),
                    contentAlignment = Alignment.CenterStart,
                ){
                    Text(
                        text = "Kolejka $matchday",
                        color = Color.White,
                        modifier = Modifier.padding(start = 10.dp)
                    )
                }
            }
            if (matches != null) {
                items(matches) { match ->
                    MatchItem(match)
                }
            }
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
                CrestAsyncImage(match.homeTeam.crest ?: "", match.homeTeam.name ?: "Unknown",50)
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
                CrestAsyncImage(match.awayTeam.crest ?: "", match.awayTeam.name ?: "Unknown",50)
            }
        }
    }
}

@Composable
fun StandingList(table: List<Table>){
    Column(modifier = Modifier.fillMaxSize()){
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ){
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.wrapContentWidth()
            ){
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
            ){
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
            items(table) {tableEntry ->
                StandingItem(tableEntry)
            }
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.wrapContentWidth()
            ){
                Text(
                    text = tableEntry.position.toString(),
                    color = Color.White,
                    modifier = Modifier.width(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                CrestAsyncImage(tableEntry.team.crest ?: "", tableEntry.team.name ?: "Unknown",50)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = tableEntry.team.shortName ?: "",
                    color = Color.White
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.wrapContentWidth()
            ){
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
fun CrestAsyncImage(url: String, name: String, size: Int){
    AsyncImage(
        model = url,
        contentDescription = name,
        modifier = Modifier.size(size.dp)
    )
}
