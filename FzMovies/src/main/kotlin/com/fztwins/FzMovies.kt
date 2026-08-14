package com.fztwins

import com.lagradost.cloudstream3.HomePageResponse
import com.lagradost.cloudstream3.LoadResponse
import com.lagradost.cloudstream3.MainAPI
import com.lagradost.cloudstream3.MainPageRequest
import com.lagradost.cloudstream3.SearchResponse
import com.lagradost.cloudstream3.SubtitleFile
import com.lagradost.cloudstream3.TvType
import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream3.fixUrlNull
import com.lagradost.cloudstream3.mainPageOf
import com.lagradost.cloudstream3.newHomePageResponse
import com.lagradost.cloudstream3.newMovieLoadResponse
import com.lagradost.cloudstream3.newMovieSearchResponse
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.ExtractorLinkType
import com.lagradost.cloudstream3.utils.getQualityFromName
import com.lagradost.cloudstream3.utils.newExtractorLink
import org.jsoup.nodes.Element

class FzMovies : MainAPI() {

    // ---------------------------------------------------------------------
    // Site config. FzMovies rotates domains often — if the site goes "Down",
    // just change MAIN_URL to a live mirror (fzmovies.net / fzmovies.live /
    // fzmovies.host) and bump `version` in build.gradle.kts.
    // ---------------------------------------------------------------------
    companion object {
        private const val MAIN_URL = "https://fzmovies.net"

        // Endpoints
        private const val SEARCH_PATH = "/csearch.php"
        private const val SEARCH_FIELD = "searchname"

        // Selectors (verify against live HTML — see README "Maintenance").
        private const val RESULT_BOX = "div.mainbox"
        private const val DOWNLOAD_OPTIONS = "ul.moviesfiles a[href]"
        private const val MEDIAINFO_MARKER = "mediainfo.php"
        private const val DOWNLOAD_LINK_ID = "a#downloadlink"
        private const val FINAL_INPUT = "input[name^=download]"

        private val YEAR_REGEX = Regex("(19|20)\\d{2}")
    }

    override var mainUrl = MAIN_URL
    override var name = "FzMovies"
    override val hasMainPage = true
    override var lang = "en"
    override val supportedTypes = setOf(TvType.Movie)

    // request.data holds a browse URL ending in the page-id query so we can append the page number.
    override val mainPage = mainPageOf(
        "$MAIN_URL/browse.php?g=Hollywood&pageID=" to "Hollywood",
        "$MAIN_URL/browse.php?g=Bollywood&pageID=" to "Bollywood",
    )

    private fun Element.toSearchResponse(): SearchResponse? {
        val anchor = this.selectFirst("a") ?: return null
        val href = anchor.attr("href")

        if (href.isBlank()) return null

        val image = this.selectFirst("img")
        val title = image?.attr("alt")?.ifBlank { anchor.text() } ?: anchor.text()

        if (title.isBlank()) return null

        val poster = image?.attr("src")

        return newMovieSearchResponse(title.trim(), fixUrl(href), TvType.Movie) {
            this.posterUrl = fixUrlNull(poster)
        }
    }

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val document = app.get(request.data + page).document
        val home = document.select(RESULT_BOX).mapNotNull { it.toSearchResponse() }

        return newHomePageResponse(request.name, home)
    }

    override suspend fun search(query: String): List<SearchResponse> {
        // FzMovies search is a simple GET form: csearch.php?searchname=<query>
        val url = "$mainUrl$SEARCH_PATH?$SEARCH_FIELD=${query.trim().replace(" ", "+")}"
        val document = app.get(url).document

        return document.select(RESULT_BOX).mapNotNull { it.toSearchResponse() }
    }

    override suspend fun load(url: String): LoadResponse {
        val document = app.get(url).document

        val title = document.selectFirst("meta[property=og:title]")?.attr("content")
            ?: document.selectFirst("h3")?.text()
            ?: name

        val poster = document.selectFirst("meta[property=og:image]")?.attr("content")
            ?: document.selectFirst("img.imgcls")?.attr("src")

        val plot = document.selectFirst("meta[property=og:description]")?.attr("content")
            ?: document.selectFirst("meta[name=description]")?.attr("content")

        val year = YEAR_REGEX.find(document.selectFirst("span.moviesizetop")?.text() ?: title)
            ?.value
            ?.toIntOrNull()

        // dataUrl (4th arg) is passed straight to loadLinks below.
        return newMovieLoadResponse(title.trim(), url, TvType.Movie, url) {
            this.posterUrl = fixUrlNull(poster)
            this.plot = plot
            this.year = year
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit,
    ): Boolean {
        val document = app.get(data).document

        // Each quality/size option is an <a> inside ul.moviesfiles (skip the "media info" link).
        val options = document.select(DOWNLOAD_OPTIONS)
            .filterNot { it.attr("href").contains(MEDIAINFO_MARKER, ignoreCase = true) }
            .map { fixUrl(it.attr("href")) to it.text().trim() }
            .distinctBy { it.first }

        var found = false

        options.forEach { (optionUrl, label) ->
            if (resolveDownload(optionUrl, label, callback)) {
                found = true
            }
        }

        return found
    }

    // Walks: download-option page -> #downloadlink page -> input[name=download*] direct MP4.
    private suspend fun resolveDownload(
        optionUrl: String,
        label: String,
        callback: (ExtractorLink) -> Unit,
    ): Boolean {
        val optionDoc = app.get(optionUrl, referer = mainUrl).document
        val downloadPage = optionDoc.selectFirst(DOWNLOAD_LINK_ID)?.attr("href") ?: return false

        val finalDoc = app.get(fixUrl(downloadPage), referer = optionUrl).document
        val directLinks = finalDoc.select(FINAL_INPUT)
            .mapNotNull { it.attr("value").ifBlank { null } }
            .ifEmpty {
                finalDoc.select("a[href*=.mp4]").mapNotNull { it.attr("href").ifBlank { null } }
            }
            .distinct()

        if (directLinks.isEmpty()) return false

        val qualityLabel = label.ifBlank { "FzMovies" }

        directLinks.forEach { link ->
            callback.invoke(
                newExtractorLink(
                    source = name,
                    name = "$name $qualityLabel",
                    url = link,
                    type = ExtractorLinkType.VIDEO,
                ) {
                    this.referer = mainUrl
                    this.quality = getQualityFromName(qualityLabel)
                }
            )
        }

        return true
    }
}
