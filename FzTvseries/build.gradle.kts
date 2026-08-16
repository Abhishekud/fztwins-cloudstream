// Bump this by 1 every time you change the provider so users get the update.
version = 2

cloudstream {
    language = "en"

    description = "FzTvseries — TV series & shows as direct, data-friendly MP4 downloads (per-episode, multiple quality tiers). Working with fztvseries.live. Twin site of FzMovies."
    authors = listOf("FzTwins")

    /**
     * Status int as one of the following:
     * 0: Down
     * 1: Ok
     * 2: Slow
     * 3: Beta only
     */
    status = 1 // Ok — verified working on fztvseries.live

    tvTypes = listOf("TvSeries")

    iconUrl = "https://www.google.com/s2/favicons?domain=fztvseries.live&sz=%size%"
}
