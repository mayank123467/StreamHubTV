package com.streamhub.tv.data

import android.content.Context
import com.streamhub.tv.BuildConfig
import com.streamhub.tv.model.AppLanguage
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

    suspend fun fetchLatestOtt(platform: OttPlatform? = null, language: AppLanguage = AppLanguage.ALL): List<MediaItem> {
        if (platform == OttPlatform.YOUTUBE || platform == OttPlatform.JIO_TV) return emptyList()
        if (BuildConfig.TMDB_API_KEY.isBlank()) return sampleItems(platform)
        val providers = platform?.tmdbProviderId?.toString() ?: "119|122|232|237"
        return try {
            val original = NetworkModule.tmdb.discoverMovies(
                apiKey = BuildConfig.TMDB_API_KEY,
                providers = providers,
                sortBy = "primary_release_date.desc",
                language = language.tmdbLocale,
                originalLanguage = language.isoCode
            ).results

            val localizedDubbed = if (language != AppLanguage.ALL) {
                NetworkModule.tmdb.discoverMovies(
                    apiKey = BuildConfig.TMDB_API_KEY,
                    providers = providers,
                    sortBy = "popularity.desc",
                    minVotes = 50,
                    language = language.tmdbLocale,
                    originalLanguage = null
                ).results
            } else emptyList()

            (original + localizedDubbed)
                .distinctBy { it.id }
                .mapIndexed { idx, dto -> dto.toMediaItem(idx, platform) }
        } catch (_: Exception) {
            sampleItems(platform)
        }
    }

    suspend fun fetchMostRatedOtt(platform: OttPlatform? = null, language: AppLanguage = AppLanguage.ALL): List<MediaItem> {
        if (platform == OttPlatform.YOUTUBE || platform == OttPlatform.JIO_TV) return emptyList()
        if (BuildConfig.TMDB_API_KEY.isBlank()) return sampleItems(platform).sortedByDescending { it.rating }
        val providers = platform?.tmdbProviderId?.toString() ?: "119|122|232|237"
        return try {
            val movies = NetworkModule.tmdb.discoverMovies(
                apiKey = BuildConfig.TMDB_API_KEY,
                providers = providers,
                sortBy = "vote_average.desc",
                minVotes = 50,
                language = language.tmdbLocale,
                originalLanguage = language.isoCode
            ).results

            val tv = NetworkModule.tmdb.discoverTv(
                apiKey = BuildConfig.TMDB_API_KEY,
                providers = providers,
                sortBy = "vote_average.desc",
                minVotes = 20,
                language = language.tmdbLocale,
                originalLanguage = language.isoCode
            ).results

            (movies + tv)
                .distinctBy { it.id }
                .mapIndexed { idx, dto -> dto.toMediaItem(idx, platform) }
                .sortedByDescending { it.rating }
        } catch (_: Exception) {
            sampleItems(platform)
        }
    }

    suspend fun fetchYouTubeTrending(language: AppLanguage = AppLanguage.ALL): List<MediaItem> {
        if (BuildConfig.YOUTUBE_API_KEY.isBlank()) return emptyList()
        return try {
            if (language == AppLanguage.ALL || language.isoCode == null) {
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
            } else {
                val resp = NetworkModule.youtube.searchByLanguage(
                    relevanceLanguage = language.isoCode,
                    query = "${language.label} trending movies shows",
                    apiKey = BuildConfig.YOUTUBE_API_KEY
                )
                resp.items.mapNotNull { v ->
                    val vid = v.id.videoId ?: return@mapNotNull null
                    MediaItem(
                        id = "yt_$vid",
                        title = v.snippet.title,
                        overview = v.snippet.description,
                        imageUrl = v.snippet.thumbnails.high?.url ?: v.snippet.thumbnails.medium?.url.orEmpty(),
                        rating = 0.0,
                        releaseDate = v.snippet.publishedAt.take(10),
                        platform = OttPlatform.YOUTUBE,
                        deepLinkUrl = "https://www.youtube.com/watch?v=$vid"
                    )
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun TmdbMovieDto.toMediaItem(index: Int, forcedPlatform: OttPlatform?): MediaItem {
        val ottPool = listOf(
            OttPlatform.JIO_HOTSTAR,
            OttPlatform.PRIME_VIDEO,
            OttPlatform.ZEE5,
            OttPlatform.SONY_LIV
        )
        val platform = forcedPlatform ?: ottPool[index % ottPool.size]
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

    private fun sampleItems(platform: OttPlatform?): List<MediaItem> {
        val all = listOf(
            MediaItem("s1", "JioHotstar Catalog", "Check internet or Private DNS dns.google.", "", 8.5, "2026", OttPlatform.JIO_HOTSTAR, OttPlatform.JIO_HOTSTAR.fallbackWebUrl, listOf(28, 53)),
            MediaItem("s2", "Prime Video Catalog", "Open Prime Video directly.", "", 8.2, "2026", OttPlatform.PRIME_VIDEO, OttPlatform.PRIME_VIDEO.fallbackWebUrl, listOf(18, 35)),
            MediaItem("s3", "ZEE5 Catalog", "Open ZEE5 directly.", "", 7.9, "2026", OttPlatform.ZEE5, OttPlatform.ZEE5.fallbackWebUrl, listOf(35, 10751)),
            MediaItem("s4", "SonyLIV Catalog", "Open SonyLIV directly.", "", 8.4, "2026", OttPlatform.SONY_LIV, OttPlatform.SONY_LIV.fallbackWebUrl, listOf(53, 878))
        )
        return if (platform == null) all else all.filter { it.platform == platform }
    }
}
