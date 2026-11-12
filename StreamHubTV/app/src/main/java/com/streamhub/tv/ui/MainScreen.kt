package com.streamhub.tv.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.*
import coil.compose.AsyncImage
import com.streamhub.tv.model.GenreCatalog
import com.streamhub.tv.model.MediaItem
import com.streamhub.tv.model.OttPlatform
import com.streamhub.tv.util.AppLauncher

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun MainScreen(viewModel: StreamHubViewModel) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    Surface(
        modifier = Modifier.fillMaxSize(),
        colors = SurfaceDefaults.colors(containerColor = Color(0xFF0F1117))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 36.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "StreamHub TV",
                                style = MaterialTheme.typography.headlineLarge,
                                color = Color.White
                            )
                            Text(
                                text = if (state.hasApiKeys) "ZEE5 • SonyLIV • Prime • Hotstar • YouTube • JioTV"
                                else "Demo Mode — Add TMDB & YouTube API keys in GitHub Secrets",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF9AA0A6)
                            )
                        }
                        Button(onClick = { viewModel.toggleInterestPicker(!state.showInterestPicker) }) {
                            Text(if (state.showInterestPicker) "Done" else "My Interests")
                        }
                    }
                }

                if (state.showInterestPicker) {
                    item {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(GenreCatalog.genres) { (id, name) ->
                                val selected = id in state.selectedGenres
                                Button(
                                    onClick = { viewModel.toggleGenre(id) },
                                    colors = ButtonDefaults.colors(
                                        containerColor = if (selected) Color(0xFF2563EB) else Color(0xFF232733)
                                    )
                                ) {
                                    Text(if (selected) "✓ $name" else name, color = Color.White)
                                }
                            }
                        }
                    }
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Quick Launch Apps", style = MaterialTheme.typography.titleMedium, color = Color.White)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                            items(OttPlatform.entries) { platform ->
                                Button(
                                    onClick = { AppLauncher.openPlatform(context, platform) },
                                    colors = ButtonDefaults.colors(containerColor = Color(0xFF1E2433))
                                ) {
                                    Text(platform.displayName, color = Color.White)
                                }
                            }
                        }
                    }
                }

                if (state.forYou.isNotEmpty()) {
                    item { ContentRow("My Interested Content", state.forYou) { viewModel.selectItem(it) } }
                }
                if (state.latest.isNotEmpty()) {
                    item { ContentRow("Latest Releases", state.latest) { viewModel.selectItem(it) } }
                }
                if (state.mostRated.isNotEmpty()) {
                    item { ContentRow("Most Rated", state.mostRated) { viewModel.selectItem(it) } }
                }
                if (state.youtubeTrending.isNotEmpty()) {
                    item { ContentRow("Trending on YouTube", state.youtubeTrending) { viewModel.selectItem(it) } }
                }
            }

            state.selectedItem?.let { item ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xCC000000)),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier.width(540.dp).padding(24.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = SurfaceDefaults.colors(containerColor = Color(0xFF1A1E29))
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(item.title, style = MaterialTheme.typography.headlineSmall, color = Color.White)
                            Text(
                                "${item.platform.displayName}  •  ★ ${if (item.rating > 0) String.format("%.1f", item.rating) else "Trending"}  •  ${item.releaseDate}",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color(0xFF60A5FA)
                            )
                            Text(
                                item.overview.ifBlank { "Open in ${item.platform.displayName} on your TV." },
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFD1D5DB),
                                maxLines = 4,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Button(onClick = {
                                    AppLauncher.openPlatform(context, item.platform, item.deepLinkUrl)
                                }) {
                                    Text("Open in ${item.platform.displayName}")
                                }
                                Button(
                                    onClick = { viewModel.selectItem(null) },
                                    colors = ButtonDefaults.colors(containerColor = Color(0xFF374151))
                                ) {
                                    Text("Close", color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun ContentRow(
    title: String,
    items: List<MediaItem>,
    onSelect: (MediaItem) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = Color.White)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            items(items, key = { it.id }) { item ->
                Card(
                    onClick = { onSelect(item) },
                    modifier = Modifier.width(170.dp).height(240.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF1C202B))
                    ) {
                        if (item.imageUrl.isNotBlank()) {
                            AsyncImage(
                                model = item.imageUrl,
                                contentDescription = item.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(165.dp)
                                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(165.dp)
                                    .background(Color(0xFF283044)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(item.platform.displayName, color = Color(0xFF93C5FD))
                            }
                        }
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = item.platform.displayName,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF9AA0A6)
                            )
                        }
                    }
                }
            }
        }
    }
}
