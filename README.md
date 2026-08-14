# FzTwins — CloudStream extensions for FzMovies & FzTvseries

Two CloudStream providers for the "FzTwins" family of sites, which serve **direct,
data-friendly MP4 downloads** in multiple quality/size tiers — ideal for people on
limited data.

| Provider      | Type      | Live domains |
|---------------|-----------|--------------|
| **FzMovies**   | Movies    | `fzmovies.net`, `fzmovies.live`, `fzmovies.host` |
| **FzTvseries** | TV series | `mobiletvshows.site`, `tvseries.in` |

> **Status: Beta.** The download *chain* (search → detail → download page →
> direct MP4) is modelled on how these sites actually work, but the exact CSS
> selectors and the browse/search endpoints must be confirmed against the live
> HTML before this is rock-solid. See **[Maintenance](#maintenance)**. Nothing
> here streams via HLS — each result is a real downloadable file, which is the
> whole point for low-data users.

---

## Install in CloudStream

CloudStream installs extensions from a **repository** (a `repo.json` + a `builds`
branch of compiled `.cs3` files). Two ways to use this:

### Option A — build it yourself with GitHub Actions (recommended)

1. Create a new GitHub repo (e.g. `fztwins-cloudstream`) and push this project to it.
2. In `build.gradle.kts`, change the `setRepo(... ?: "https://github.com/Abhishekud/fztwins-cloudstream")`
   fallback to your repo.
3. Push to `main`. The workflow in `.github/workflows/build.yml` compiles the
   plugins and force-pushes `FzMovies.cs3`, `FzTvseries.cs3` and `plugins.json`
   to a `builds` branch.
4. In the CloudStream app: **Settings → Extensions → Add repository**, and paste
   the raw `repo.json` URL (see `repo.json` in this project — update the URLs to
   your username first).

### Option B — sideload a single `.cs3`

After a successful build, grab a `.cs3` from the `builds` branch and load it in
CloudStream via **Settings → Extensions → (＋) → local file**.

---

## Build locally

Requires **JDK 11** and the Android SDK. The Gradle wrapper is pinned to Gradle
7.4 (required by Android Gradle Plugin 7.0.4).

```bash
./gradlew make            # compile both providers into */build/*.cs3
./gradlew makePluginsJson # generate build/plugins.json
```

The first run downloads the CloudStream stubs and Android deps from
`jitpack.io` / Google Maven, so you need network access.

---

## Project layout

```
build.gradle.kts            root config (versions, deps, author, repo)
settings.gradle.kts         auto-includes every module folder
gradle/ gradlew gradlew.bat Gradle 7.4 wrapper
.github/workflows/build.yml CI that builds .cs3 files to the `builds` branch
FzMovies/
  build.gradle.kts          plugin metadata (version, tvTypes, icon, status)
  src/main/AndroidManifest.xml
  src/main/kotlin/com/fztwins/FzMovies.kt        the scraper
  src/main/kotlin/com/fztwins/FzMoviesPlugin.kt  registers the provider
FzTvseries/                 same shape, TV-series scraper
```

Each provider implements the standard `MainAPI` surface: `getMainPage`,
`search`, `load`, and `loadLinks`.

### How the scrape works

Both sites share the same download chain, which is the reliable part:

1. **search** → `csearch.php?searchname=<query>` → result cards in `div.mainbox`.
2. **load** → detail (movie) or series page. For TV, episode anchors are parsed
   into a season/episode list.
3. **loadLinks** → quality/size options in `ul.moviesfiles` → each links to a
   download page whose `a#downloadlink` leads to the final page → the direct MP4
   URL sits in `input[name=download1]` (and siblings). Each becomes an
   `ExtractorLink` of type `VIDEO`.

---

## Maintenance

These sites change domains and tweak markup often. When something breaks:

- **Site "Down" / moved:** change `MAIN_URL` at the top of the provider's
  `companion object` to a live mirror, then bump `version` in that module's
  `build.gradle.kts`.
- **Search or browse returns nothing:** the `csearch.php` params or the
  `browse.php?g=...` categories are the first things to verify. Open the site in
  a desktop browser, run a search, and check the real query string and the
  container class (currently assumed to be `div.mainbox`).
- **No playable links:** re-check, in order, `ul.moviesfiles` (the options),
  `a#downloadlink` (the redirect), and `input[name^=download]` (the final URL).
  All selector names are declared as constants at the top of each provider, so
  they're the only things you should need to touch.
- Always **bump `version`** after any change so existing users get the update.

---

## Disclaimer

This is an unofficial, educational scraper. It hosts no content and is not
affiliated with or endorsed by the operators of any of these sites. You are
responsible for complying with the laws and terms of service that apply to you.
Prefer legal, licensed sources where available.
