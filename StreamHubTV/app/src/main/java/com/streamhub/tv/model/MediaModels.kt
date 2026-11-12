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
import com.streamhub.tv.model.AppLanguage
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
    val heroItem = state.forYou.firstOrNull() ?: state.latest.firstOrNull() ?: state.youtubeTrending.firstOrNull()

    Surface(
        modifier = Modifier.fillMaxSize(),
        colors = SurfaceDefaults.colors(containerColor = Color(0xFF090D16))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
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
                                text = "${state.selectedPlatform?.displayName ?: "All OTT"} • ${state.selectedLanguage.label}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF60A5FA)
                            )
                        }
                        Button(onClick = { viewModel
                            
