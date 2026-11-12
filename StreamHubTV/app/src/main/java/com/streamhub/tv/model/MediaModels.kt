package com.streamhub.tv.model

enum class OttPlatform(
    val displayName: String,
    val packageName: String,
    val mobilePackageName: String,
    val tmdbProviderId: Int? = null,
    val fallbackWebUrl: String
) {
    JIO_HOTSTAR("JioHotstar", "in.startv.hotstar", "in.startv.hotstar", 122, "https://www.hotstar.com"),
    PRIME_VIDEO("Prime Video", "com.amazon.amazonvideo.livingroom", "com.amazon.avod.thirdpartyclient", 119, "https://www.primevideo.com"),
    ZEE5("ZEE5", "com.graymatrix.did", "com.graymatrix.did", 232, "https://www.zee5.com"),
    SONY_LIV("SonyLIV", "com.sonyliv", "com.sonyliv", 237, "https://www.sonyliv.com"),
    YOUTUBE("YouTube", "com.google.android.youtube.tv", "com.google.android.youtube", null, "https://www.youtube.com"),
    JIO_TV("JioTV", "com.jio.jioplay.tv", "com.jio.media.jiotv", null, "https://jiotv.com")
}

data class MediaItem(
    val id: String,
    val title: String,
    val overview: String,
    val imageUrl: String,
    val rating: Double,
    val releaseDate: String,
    val platform: OttPlatform,
    val deepLinkUrl: String,
    val genreIds: List<Int> = emptyList()
)

object GenreCatalog {
    val genres = listOf(
        28 to "Action",
        35 to "Comedy",
        18 to "Drama",
        53 to "Thriller",
        10749 to "Romance",
        878 to "Sci-Fi",
        10751 to "Family",
        27 to "Horror"
    )
}
