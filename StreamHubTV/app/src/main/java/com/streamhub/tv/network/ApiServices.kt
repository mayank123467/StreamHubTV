package com.streamhub.tv.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

data class TmdbResponse(val results: List<TmdbMovieDto> = emptyList())

data class TmdbMovieDto(
    val id: Int,
    val title: String? = null,
    val name: String? = null,
    val overview: String? = null,
    val poster_path: String? = null,
    val backdrop_path: String? = null,
    val vote_average: Double = 0.0,
    val release_date: String? = null,
    val first_air_date: String? = null,
    val genre_ids: List<Int>? = null
)

interface TmdbApi {
    @GET("3/discover/movie")
    suspend fun discoverMovies(
        @Query("api_key") apiKey: String,
        @Query("watch_region") region: String = "IN",
        @Query("with_watch_providers") providers: String = "119|122|232|237",
        @Query("sort_by") sortBy: String = "primary_release_date.desc",
        @Query("vote_count.gte") minVotes: Int = 5
    ): TmdbResponse

    @GET("3/discover/tv")
    suspend fun discoverTv(
        @Query("api_key") apiKey: String,
        @Query("watch_region") region: String = "IN",
        @Query("with_watch_providers") providers: String = "119|122|232|237",
        @Query("sort_by") sortBy: String = "vote_average.desc",
        @Query("vote_count.gte") minVotes: Int = 30
    ): TmdbResponse
}

data class YouTubeResponse(val items: List<YouTubeVideoDto> = emptyList())
data class YouTubeVideoDto(val id: String, val snippet: YouTubeSnippet)
data class YouTubeSnippet(
    val title: String,
    val description: String,
    val publishedAt: String,
    val thumbnails: YouTubeThumbnails
)
data class YouTubeThumbnails(val high: YouTubeThumb? = null, val medium: YouTubeThumb? = null)
data class YouTubeThumb(val url: String)

interface YouTubeApi {
    @GET("youtube/v3/videos")
    suspend fun getTrending(
        @Query("part") part: String = "snippet",
        @Query("chart") chart: String = "mostPopular",
        @Query("regionCode") regionCode: String = "IN",
        @Query("maxResults") maxResults: Int = 15,
        @Query("key") apiKey: String
    ): YouTubeResponse
}

object NetworkModule {
    val tmdb: TmdbApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.themoviedb.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TmdbApi::class.java)
    }

    val youtube: YouTubeApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://www.googleapis.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(YouTubeApi::class.java)
    }
}
