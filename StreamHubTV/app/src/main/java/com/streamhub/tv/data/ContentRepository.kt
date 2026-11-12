package com.streamhub.tv.data

import android.content.Context
import com.streamhub.tv.BuildConfig
import com.streamhub.tv.model.MediaItem
import com.streamhub.tv.model.OttPlatform
import com.streamhub.tv.network.NetworkModule
import com.streamhub.tv.network.TmdbMovieDto

class ContentRepository(context: Context) {
    private val prefs = context.getSharedPreferences("streamhub_prefs", Context.MODE_PRIVATE)

    fun getSelectedGenres(): Set<Int> {
        val saved = prefs.getStringSet("genres", null)
        return saved?.mapNotNull { it.toIntOrNull() }?.toSet() ?: setOf(28, 35, 53)
    }

    fun toggleGenre(genreId: Int): Set<Int> {
        val current = getSelectedGenres().toMutableSet()
        if (!current.add(genreId)) current.remove(genreId)
        prefs.edit().putStringSet("genres", current.map { it.toString() }.toSet()).apply()
        return current
    }

    fun hasApiKeys(): Boolean =
        BuildConfig.TMDB_API_KEY.isNotBlank() || BuildConfig.YOUTUBE_API_KEY.isNotBlank()

    suspend fun fetchLatestOtt(): List<MediaItem> {
        if (BuildConfig.TMDB_API_KEY.isBlank()) return sampleItems()
        return try {
            val resp = NetworkModule.tmdb.discoverMovies(
                apiKey = BuildConfig.TMDB_API_KEY,
                sortBy = "primary_release_date.desc"
            )
            resp.results.mapIndexed { idx, dto -> dto.toMediaItem(idx) }
        } catch (_: Exception) {
            sampleItems()
        }
    }

    suspend fun fetchMostRatedOtt(): List<MediaItem> {
        if (BuildConfig.TMDB_API_KEY.isBlank()) return sampleItems().sortedByDescending { it.rating }
        return try {
            val resp = NetworkModule.tmdb.discoverMovies(
                apiKey = BuildConfig.TMDB_API_KEY,
                sortBy = "vote_average.desc",
                minVotes = 100
            )
            val tvResp = NetworkModule.tmdb.discoverTv(
                apiKey = BuildConfig.TMDB_API_KEY,
                sortBy = "vote_average.desc"
            )
            (resp.results + tvResp.results)
                .mapIndexed { idx, dto -> dto.toMediaItem(idx) }
                .sortedByDescending { it.rating }
        } catch (_: Exception) {
            sampleItems()
        }
    }

    suspend fun fetchYouTubeTrending(): List<MediaItem> {
        if (BuildConfig.YOUTUBE_API_KEY.isBlank()) return emptyList()
        return try {
            val resp = NetworkModule.youtube.getTrending(apiKey = BuildConfig.YOUTUBE_API_KEY)
            resp.items.map { v ->
                MediaItem(
                    id = "yt_${v.id}",
                    title = v.snippet.title,
                    overview = v.snippet.description,
                    imageUrl = v.snippet.thumbnails.high?.url ?: v.snippet.thumbnails.medium?.url.orEmpty(),
                    rating = 0.0,
                    releaseDate = v.snippet.publishedAt.take(10),
                    platform = OttPlatform.YOUTUBE,
                    deepLinkUrl = "https://www.youtube.com/watch?v=${v.id}"
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun TmdbMovieDto.toMediaItem(index: Int): MediaItem {
        val ottPool = listOf(
            OttPlatform.JIO_HOTSTAR,
            OttPlatform.PRIME_VIDEO,
            OttPlatform.ZEE5,
            OttPlatform.SONY_LIV
        )
        val platform = ottPool[index % ottPool.size]
        val poster = poster_path ?: backdrop_path
        val fullImg = if (poster != null) "https://image.tmdb.org/t/p/w500$poster" else ""
        return MediaItem(
            id = "tmdb_$id",
            title = title ?: name ?: "Untitled",
            overview = overview.orEmpty(),
            imageUrl = fullImg,
            rating = vote_average,
            releaseDate = release_date ?: first_air_date ?: "",
            platform = platform,
            deepLinkUrl = "https://www.themoviedb.org/movie/$id",
            genreIds = genre_ids.orEmpty()
        )
    }

    private fun sampleItems(): List<MediaItem> = listOf(
        MediaItem(
            id = "s1",
            title = "Add TMDB API Key to Load Live Catalog",
            overview = "Follow the setup guide to connect TMDB & YouTube API keys in GitHub Secrets.",
            imageUrl = "",
            rating = 8.5,
            releaseDate = "2026",
            platform = OttPlatform.JIO_HOTSTAR,
            deepLinkUrl = OttPlatform.JIO_HOTSTAR.fallbackWebUrl,
            genreIds = listOf(28, 53)
        ),
        MediaItem(
            id = "s2",
            title = "Prime Video Quick Launch",
            overview = "Opens Prime Video directly on your Android TV.",
            imageUrl = "",
            rating = 8.2,
            releaseDate = "2026",
            platform = OttPlatform.PRIME_VIDEO,
            deepLinkUrl = OttPlatform.PRIME_VIDEO.fallbackWebUrl,
            genreIds = listOf(18, 35)
        ),
        MediaItem(
            id = "s3",
            title = "ZEE5 Quick Launch",
            overview = "Opens ZEE5 directly on your Android TV.",
            imageUrl = "",
            rating = 7.9,
            releaseDate = "2026",
            platform = OttPlatform.ZEE5,
            deepLinkUrl = OttPlatform.ZEE5.fallbackWebUrl,
            genreIds = listOf(35, 10751)
        ),
        MediaItem(
            id = "s4",
            title = "SonyLIV Quick Launch",
            overview = "Opens SonyLIV directly on your Android TV.",
            imageUrl = "",
            rating = 8.4,
            releaseDate = "2026",
            platform = OttPlatform.SONY_LIV,
            deepLinkUrl = OttPlatform.SONY_LIV.fallbackWebUrl,
            genreIds = listOf(53, 878)
        )
    )
}
