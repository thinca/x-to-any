package io.github.thinca.xtoany

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PostLinkTest {
    private fun extract(scheme: String, host: String, path: String, vararg params: Pair<String, String>): String? {
        val map = params.toMap()
        return PostLink.extractText(scheme, host, path) { map[it] }
    }

    @Test
    fun intentTweetCombinesTextHashtagsAndUrl() {
        val text = extract(
            "https", "twitter.com", "/intent/tweet",
            "text" to "Hello",
            "url" to "https://example.com/",
            "hashtags" to "foo, bar",
            "via" to "someone",
        )
        assertEquals("Hello #foo #bar https://example.com/", text)
    }

    @Test
    fun intentPostOnXDomain() {
        assertEquals("Hi", extract("https", "x.com", "/intent/post", "text" to "Hi"))
    }

    @Test
    fun trailingSlashIsIgnored() {
        assertEquals("Hi", extract("https", "x.com", "/intent/tweet/", "text" to "Hi"))
    }

    @Test
    fun urlAlreadyInTextIsNotDuplicated() {
        val text = extract(
            "https", "x.com", "/intent/tweet",
            "text" to "Read https://example.com/ now",
            "url" to "https://example.com/",
        )
        assertEquals("Read https://example.com/ now", text)
    }

    @Test
    fun legacyShareLink() {
        assertEquals("Hi https://example.com/", extract("https", "twitter.com", "/share", "text" to "Hi", "url" to "https://example.com/"))
    }

    @Test
    fun legacyHomeStatus() {
        assertEquals("Hi", extract("https", "twitter.com", "/home", "status" to "Hi"))
    }

    @Test
    fun homeWithoutStatusIsNotPostLink() {
        assertNull(extract("https", "x.com", "/home"))
    }

    @Test
    fun twitterSchemePost() {
        assertEquals("Hi", extract("twitter", "post", "", "message" to "Hi"))
    }

    @Test
    fun emptyIntentYieldsEmptyText() {
        assertEquals("", extract("https", "x.com", "/intent/post"))
    }

    @Test
    fun ordinaryLinkIsNotPostLink() {
        assertNull(extract("https", "x.com", "/someone/status/123"))
    }
}
