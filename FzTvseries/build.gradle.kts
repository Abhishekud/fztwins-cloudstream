// Bump this by 1 every time you change the provider so users get the update.
version = 1

cloudstream {
    language = "en"

    description = "FzTvseries — TV series & shows as direct, data-friendly MP4 downloads (per-episode, multiple quality tiers). Serves mobiletvshows.site / tvseries.in. Twin site of FzMovies."
    authors = listOf("FzTwins")

    /**
     * Status int as one of the following:
     * 0: Down
     * 1: Ok
     * 2: Slow
     * 3: Beta only
     */
    status = 3 // Beta — selectors need verification against the live site (see README).

    tvTypes = listOf("TvSeries")

    iconUrl = "https://www.google.com/s2/favicons?domain=mobiletvshows.site&sz=%size%"
}
