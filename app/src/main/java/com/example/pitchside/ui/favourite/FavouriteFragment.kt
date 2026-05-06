package com.example.pitchside.ui.favourite

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.example.pitchside.R
import com.example.pitchside.data.Favorite
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.fragment.findNavController


class FavouriteFragment : Fragment() {
    private val viewModel: FavouriteViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_favourite, container, false)
        val composeView = view.findViewById<ComposeView>(R.id.compose_view_favourite)

        composeView.apply {
            setContent {
                FavouriteScreen(
                    viewModel = viewModel,
                    onLeagueClick = { leagueCode ->
                        // NAWIGACJA DO SZCZEGÓŁÓW LIGI
                        val bundle = Bundle().apply {
                            putString("competitionCode", leagueCode)
                        }
                        findNavController().navigate(R.id.competitionDetailsFragment, bundle)
                    }
                )
            }
        }
        return view
    }
}

@Composable
fun FavouriteScreen(viewModel: FavouriteViewModel, onLeagueClick: (String) -> Unit) {
    val favoriteMatches by viewModel.favoriteMatches.collectAsState()

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF121212)).padding(16.dp)) {
        Text(text = "Twoje Ulubione", color = Color.White, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(bottom = 16.dp))

        if (favoriteMatches.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Brak ulubionych.", color = Color.Gray)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(favoriteMatches) { favorite ->
                    FavoriteItem(
                        favorite = favorite,
                        onDelete = { viewModel.usunUlubione(favorite) },
                        onItemClick = {
                            // Reagujemy na kliknięcie tylko jeśli to liga i mamy jej kod
                            if (favorite.typ_obiektu == "LIGA") {
                                favorite.kod_ligi?.let { onLeagueClick(it) }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun FavoriteItem(favorite: Favorite, onDelete: () -> Unit, onItemClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick() }, // CAŁA KARTA JEST KLIKALNA
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (favorite.typ_obiektu == "MECZ") {
                // POPRAWIONY UKŁAD MECZU (Myślnik na środku)
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.Start) {
                        CrestAsyncImageSmall(favorite.herb_gospodarza ?: "", "")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = favorite.skrot_gospodarza ?: "", color = Color.White, maxLines = 1)
                    }

                    Text(
                        text = "-",
                        color = Color.White,
                        modifier = Modifier.width(30.dp), // Stała szerokość dla myślnika
                        textAlign = TextAlign.Center
                    )

                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.End) {
                        Text(text = favorite.skrot_goscia ?: "", color = Color.White, maxLines = 1)
                        Spacer(modifier = Modifier.width(8.dp))
                        CrestAsyncImageSmall(favorite.herb_goscia ?: "", "")
                    }
                }
            } else {
                // UKŁAD LIGI
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    CrestAsyncImageSmall(favorite.emblem_ligi ?: "", "")
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = favorite.nazwa_ligi ?: "Liga", color = Color.White)
                        Text(text = "Liga", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            IconButton(onClick = onDelete) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = Color(0xFFCF6679))
            }
        }
    }
}
@Composable
fun CrestAsyncImageSmall(url: String, desc: String) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(url)
            .decoderFactory(SvgDecoder.Factory())
            .crossfade(true)
            .build(),
        contentDescription = desc,
        modifier = Modifier.size(30.dp)
    )
}