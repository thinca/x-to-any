package io.github.thinca.xtoany

object PostLink {
    private val composePaths = setOf(
        "/intent/tweet",
        "/intent/post",
        "/share",
        "/compose/tweet",
        "/compose/post",
    )

    fun extractText(scheme: String?, host: String?, path: String?, param: (String) -> String?): String? =
        when (scheme?.lowercase()) {
            "twitter" -> if (host == "post") param("message").orEmpty() else null
            "http", "https" -> extractWebText(path?.trimEnd('/').orEmpty(), param)
            else -> null
        }

    private fun extractWebText(path: String, param: (String) -> String?): String? = when {
        path in composePaths -> composeText(
            text = param("text").orEmpty(),
            url = param("url").orEmpty(),
            hashtags = param("hashtags").orEmpty(),
        )
        path == "/home" -> param("status")
        else -> null
    }

    private fun composeText(text: String, url: String, hashtags: String): String {
        val tags = hashtags.split(',').map { it.trim() }.filter { it.isNotEmpty() }.map { "#$it" }
        val urlPart = url.takeIf { it.isNotBlank() && it !in text }
        return (listOf(text) + tags + listOfNotNull(urlPart))
            .filter { it.isNotBlank() }
            .joinToString(" ")
    }
}
