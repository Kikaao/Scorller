package com.scorller.data.linkpreview

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

private const val TAG = "LinkPreviewFetcher"
private const val MAX_IMAGES = 60
private const val MIN_IMAGE_WIDTH = 400

// Airbnb's og:image tags only cover a few "share preview" photos; the full gallery
// sits as plain image URLs in the page's own JSON state, so we scan the raw HTML
// for its photo CDN instead of running any JavaScript.
private val MUSCACHE_IMAGE_REGEX = Regex("""https://[\w.-]*muscache\.com/im/pictures/[^"'\\\s)]+""")
private val IM_WIDTH_REGEX = Regex("""[?&]im_w=(\d+)""")

// That same CDN also serves avatars, badges and brand assets under these paths or
// at tiny resolutions - none of which are listing photos.
private val EXCLUDED_URL_KEYWORDS = listOf(
    "user/", "users/", "avatar", "profile_pic", "profile-pic", "badge",
    "icon", "logo", "emoji", "favicon", "pictures/user",
    "platformassets", "award", "verified", "superhost", "qualitysignal",
    "trust_and_safety", "trust-and-safety"
)

data class LinkPreview(
    val title: String?,
    val description: String?,
    val imageUrls: List<String>
) {
    val imageUrl: String? get() = imageUrls.firstOrNull()
}

/**
 * Best-effort fetch of a link's title/description/photos via a plain HTML GET
 * (no JS). Reads Open Graph tags for the generic case, plus a scan for Airbnb's
 * image CDN to pull its full gallery rather than just the preview images a page
 * publishes. Uses an unbounded body size and a generous timeout since listing
 * pages can be large - Jsoup's 1MB/default-timeout defaults are too tight for them.
 */
class LinkPreviewFetcher {

    suspend fun fetch(url: String): LinkPreview? = withContext(Dispatchers.IO) {
        runCatching {
            val doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 Chrome/120.0 Mobile Safari/537.36")
                .header("Accept-Language", "en-US,en;q=0.9")
                .timeout(20000)
                .maxBodySize(0)
                .followRedirects(true)
                .get()

            fun metaContents(property: String): List<String> =
                doc.select("meta[property=$property]").mapNotNull { it.attr("content").ifBlank { null } }

            val title = metaContents("og:title").firstOrNull() ?: doc.title().ifBlank { null }
            val description = metaContents("og:description").firstOrNull()
                ?: doc.select("meta[name=description]").attr("content").ifBlank { null }

            val ogImages = metaContents("og:image") + metaContents("og:image:secure_url") + metaContents("og:image:url")
            val cdnImages = MUSCACHE_IMAGE_REGEX.findAll(doc.html()).map { it.value }.filter(::isLikelyListingPhoto).toList()
            val images = dedupeByBaseUrl(ogImages + cdnImages).take(MAX_IMAGES)

            Log.d(TAG, "Fetched $url: title=${title != null}, ${images.size} photos")

            if (title == null && description == null && images.isEmpty()) null
            else LinkPreview(title, description, images)
        }.onFailure { e -> Log.e(TAG, "Failed to fetch preview for $url", e) }.getOrNull()
    }

    private fun isLikelyListingPhoto(url: String): Boolean {
        if (EXCLUDED_URL_KEYWORDS.any { url.lowercase().contains(it) }) return false
        val width = widthOf(url) ?: return true
        return width >= MIN_IMAGE_WIDTH
    }

    private fun widthOf(url: String): Int? = IM_WIDTH_REGEX.find(url)?.groupValues?.get(1)?.toIntOrNull()

    /** Airbnb repeats each photo at several sizes; keep the original, else the widest variant. */
    private fun dedupeByBaseUrl(urls: List<String>): List<String> {
        val best = LinkedHashMap<String, String>()
        for (url in urls) {
            val base = url.substringBefore('?')
            val current = best[base]
            if (current == null || isBetterVariant(url, current)) best[base] = url
        }
        return best.values.toList()
    }

    private fun isBetterVariant(candidate: String, current: String): Boolean {
        if (!candidate.contains('?')) return true
        if (!current.contains('?')) return false
        return (widthOf(candidate) ?: 0) > (widthOf(current) ?: 0)
    }
}
