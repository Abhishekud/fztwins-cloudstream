// Bump this by 1 every time you change the provider so users get the update.
version = 2

cloudstream {
    language = "en"
    // All of these properties are optional, you can safely remove them.

    description = "FzMovies — Bollywood & Hollywood movies as direct, data-friendly MP4 downloads (multiple quality tiers). Twin site of FzTvseries. Working with fzmovies.live"
    authors = listOf("FzTwins")

    /**
     * Status int as one of the following:
     * 0: Down
     * 1: Ok
     * 2: Slow
     * 3: Beta only
     */
    status = 1 // Ok — verified working on fzmovies.live

    // List of video source types. Users can filter for extensions in these categories.
    // Possible values: "AnimeMovie", "TvSeries", "Cartoon", "Anime", "OVA", "Movie",
    // "Torrent", "Documentary", "AsianDrama", "Live", "NSFW", "Others"
    tvTypes = listOf("Movie")

    iconUrl = "https://www.google.com/s2/favicons?domain=fzmovies.live&sz=%size%"
}
