package com.streamhub.tv.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.*
import coil.compose.AsyncImage
import com.streamhub.tv.model.GenreCatalog
import com.streamhub.tv.model.MediaItem
import com.streamhub.tv.model.OttPlatform
import com.streamhub.tv.util.AppLauncher

private fun platformAccent(platform: OttPlatform): Color = when (platform) {
    OttPlatform.JIO_HOTSTAR -> Color(0xFF0F4C81)
    OttPlatform.PRIME_VIDEO -> Color(0xFF0078B8)
    OttPlatform.ZEE5 -> Color(0xFF7B2CBF)
    OttPlatform.SONY_LIV -> Color(0xFFEA580C)
    OttPlatform.YOUTUBE -> Color(0xFFDC2626)
    OttPlatform.JIO_TV -> Color(0xFFE11D48)
}

private fun platformShortTag(platform: OttPlatform): String = when (platform) {
    OttPlatform.JIO_HOTSTAR -> "JH"
    OttPlatform.PRIME_VIDEO -> "PV"
    OttPlatform.ZEE5 -> "Z5"
    OttPlatform.SONY_LIV -> "SL"
    OttPlatform.YOUTUBE -> "YT"
    OttPlatform.JIO_TV -> "JT"
}

private fun platformSubtitle(platform: OttPlatform): String = when (platform) {
    OttPlatform.JIO_HOTSTAR -> "Sports & Specials"
    OttPlatform.PRIME_VIDEO -> "Originals & Movies"
    OttPlatform.ZEE5 -> "Hindi & Regional"
    OttPlatform.SONY_LIV -> "Shows & Thrillers"
    OttPlatform.YOUTUBE -> "Trending & Videos"
    OttPlatform.JIO_TV -> "Live TV Channels"
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun MainScreen(viewModel: StreamHubViewModel) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    Surface(
        modifier = Modifier.fillMaxSize(),
        colors = SurfaceDefaults.colors(containerColor = Color(0xFF0B0F19))
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "StreamHub TV",
                                style = MaterialTheme.typography.headlineLarge,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = state.selectedPlatform?.let { "Showing: ${it.displayName}" }
                                    ?: "All OTT Platforms",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF94A3B8)
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
                                        containerColor = if (selected) Color(0xFF2563EB) else Color(0xFF1E293B)
                                    )
                                ) {
                                    Text(if (selected) "✓ $name" else name, color = Color.White)
                                }
                            }
                        }
                    }
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Filter by OTT App",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )
                            state.selectedPlatform?.let { selected ->
                                Button(
                                    onClick = { AppLauncher.openPlatform(context, selected) },
                                    colors = ButtonDefaults.colors(containerColor = platformAccent(selected))
                                ) {
                                    Text("Open ${selected.displayName} App", color = Color.White)
                                }
                            }
                        }

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                            item {
                                val isAll = state.selectedPlatform == null
                                Card(
                                    onClick = { viewModel.selectPlatform(null) },
                                    modifier = Modifier
                                        .width(120.dp)
                                        .height(86.dp)
                                        .then(
                                            if (isAll) Modifier.border(2.dp, Color.White, RoundedCornerShape(12.dp))
                                            else Modifier
                                        )
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color(0xFF1E293B)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "All OTT",
                                            style = MaterialTheme.typography.titleSmall,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            items(OttPlatform.entries) { platform ->
                                val accent = platformAccent(platform)
                                val isSelected = state.selectedPlatform == platform
                                Card(
                                    onClick = { viewModel.selectPlatform(platform) },
                                    modifier = Modifier
                                        .width(190.dp)
                                        .height(86.dp)
                                        .then(
                                            if (isSelected) Modifier.border(2.dp, Color.White, RoundedCornerShape(12.dp))
                                            else Modifier
                                        )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.horizontalGradient(
                                                    listOf(accent.copy(alpha = 0.85f), Color(0xFF1E293B))
                                                )
                                            )
                                            .padding(horizontal = 14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .clip(CircleShape)
                                                .background(Color.White.copy(alpha = 0.18f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = platformShortTag(platform),
                                                style = MaterialTheme.typography.titleSmall,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Column {
                                            Text(
                                                text = platform.displayName,
                                                style = MaterialTheme.typography.titleSmall,
                                                color = Color.White,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                text = platformSubtitle(platform),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color(0xFFE2E8F0),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (state.selectedPlatform == OttPlatform.JIO_TV) {
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = SurfaceDefaults.colors(containerColor = Color(0xFF161D2E))
                        ) {
                            Row(
                                modifier = Modifier.padding(20.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("JioTV Live Channels", style = MaterialTheme.typography.titleMedium, color = Color.White)
                                    Text("Live TV channels stream directly inside the JioTV app.", style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8))
                                }
                                Button(
                                    onClick = { AppLauncher.openPlatform(context, OttPlatform.JIO_TV) },
                                    colors = ButtonDefaults.colors(containerColor = platformAccent(OttPlatform.JIO_TV))
                                ) {
                                    Text("Launch JioTV", color = Color.White)
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
                val accent = platformAccent(item.platform)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xCC000000)),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier.width(560.dp).padding(24.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = SurfaceDefaults.colors(containerColor = Color(0xFF161D2E))
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
                                item.overview.ifBlank { "Open in ${item.platform.displayName} on your device." },
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFD1D5DB),
                                maxLines = 4,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Button(
                                    onClick = { AppLauncher.openPlatform(context, item.platform, item.deepLinkUrl) },
                                    colors = ButtonDefaults.colors(containerColor = accent)
                                ) {
                                    Text("Watch on ${item.platform.displayName}", color = Color.White)
                                }
                                Button(
                                    onClick = { viewModel.selectItem(null) },
                                    colors = ButtonDefaults.colors(containerColor = Color(0xFF334155))
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
                val accent = platformAccent(item.platform)
                Card(
                    onClick = { onSelect(item) },
                    modifier = Modifier.width(176.dp).height(252.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF161D2E))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(175.dp)
                        ) {
                            if (item.imageUrl.isNotBlank()) {
                                AsyncImage(
                                    model = item.imageUrl,
                                    contentDescription = item.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(accent.copy(alpha = 0.8f), Color(0xFF1E293B))
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = platformShortTag(item.platform),
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            if (item.rating > 0) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(6.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xCC0F172A))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "★ ${String.format("%.1f", item.rating)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFFFACC15)
                                    )
                                }
                            }

                         
