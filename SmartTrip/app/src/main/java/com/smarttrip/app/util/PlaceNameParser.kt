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
        val destinations = mutableListOf<Destination>()
        
        // 模式1：从...出发，经过...，到达...
        val fromPattern = Regex("""从\s*(.+?)\s*(?:出发|开始)""")
        val passPattern = Regex("""经过\s*(.+?)(?:，|,|$)""")
        val toPattern = Regex("""到达\s*(.+?)(?:，|,|$)""")

        fromPattern.find(text)?.let { match ->
            destinations.add(Destination(name = match.groupValues[1].trim()))
        }

        passPattern.findAll(text).forEach { match ->
            destinations.add(Destination(name = match.groupValues[1].trim()))
        }

        toPattern.find(text)?.let { match ->
            destinations.add(Destination(name = match.groupValues[1].trim()))
        }
        
        if (destinations.isNotEmpty()) {
            return destinations
        }
        
        // 模式2：使用"到"、"再到"、"然后到"等关键字拆分
        val parts = mutableListOf<String>()
        var current = text.trim()
        
        // 先处理"再到"和"然后到"，防止被先拆分为"到"
        val thenToPattern = Regex("""\s*(?:再到|然后到|接着到)\s*""")
        val toPattern2 = Regex("""\s*(?:到|至)\s*""")
        
        var result = thenToPattern.split(current)
        result = result.flatMap { toPattern2.split(it) }.toList()
        
        parts.addAll(result.filter { it.isNotBlank() })
        
        if (parts.size >= 1) {
            return parts.map { Destination(name = it.trim()) }
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
