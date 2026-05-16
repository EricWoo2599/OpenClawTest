package com.smarttrip.app.util

import com.smarttrip.app.model.Destination

object PlaceNameParser {

    fun parse(text: String): List<Destination> {
        if (text.isBlank()) return emptyList()

        val trimmedText = text.trim()

        val arrowResult = tryParseArrowFormat(trimmedText)
        if (arrowResult.isNotEmpty()) return arrowResult

        val numberedResult = tryParseNumberedFormat(trimmedText)
        if (numberedResult.isNotEmpty()) return numberedResult

        val naturalResult = tryParseNaturalLanguage(trimmedText)
        if (naturalResult.isNotEmpty()) return naturalResult

        return tryParseDelimiters(trimmedText)
    }

    private fun tryParseArrowFormat(text: String): List<Destination> {
        val arrowPatterns = listOf("→", "->", "➔", "➜", "→")
        for (pattern in arrowPatterns) {
            if (text.contains(pattern)) {
                return text.split(pattern)
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                    .map { Destination(name = it) }
            }
        }
        return emptyList()
    }

    private fun tryParseNumberedFormat(text: String): List<Destination> {
        val patterns = listOf(
            Regex("""^\d+[.、]\s*(.+)$""", RegexOption.MULTILINE),
            Regex("""\n\d+[.、]\s*(.+)$""")
        )

        for (pattern in patterns) {
            val matches = pattern.findAll(text).toList()
            if (matches.isNotEmpty()) {
                return matches.map { match ->
                    Destination(name = match.groupValues[1].trim())
                }
            }
        }
        return emptyList()
    }

    private fun tryParseNaturalLanguage(text: String): List<Destination> {
        val fromPattern = Regex("""从\s*(.+?)\s*(?:出发|开始)""")
        val passPattern = Regex("""经过\s*(.+?)(?:，|,|$)""")
        val toPattern = Regex("""到达\s*(.+?)(?:，|,|$)""")

        val destinations = mutableListOf<Destination>()

        fromPattern.find(text)?.let { match ->
            destinations.add(Destination(name = match.groupValues[1].trim()))
        }

        passPattern.findAll(text).forEach { match ->
            destinations.add(Destination(name = match.groupValues[1].trim()))
        }

        toPattern.find(text)?.let { match ->
            destinations.add(Destination(name = match.groupValues[1].trim()))
        }

        return destinations
    }

    private fun tryParseDelimiters(text: String): List<Destination> {
        val delimiters = listOf(",", "，", "\n", ";")

        for (delimiter in delimiters) {
            if (text.contains(delimiter)) {
                val parts = text.split(delimiter)
                    .map { it.trim() }
                    .filter { it.isNotBlank() }

                if (parts.size >= 2) {
                    return parts.map { Destination(name = it) }
                }
            }
        }

        return emptyList()
    }
}
