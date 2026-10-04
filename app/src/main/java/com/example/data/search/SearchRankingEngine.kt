package com.example.data.search

import com.example.data.model.WebSource
import java.net.URI

object SearchRankingEngine {

    private val GOV_DOMAINS = listOf(".gov", ".mil", ".gov.uk", ".gov.au", ".europa.eu", "nasa.gov", "nih.gov", "noaa.gov")
    private val EDU_DOMAINS = listOf(".edu", ".ac.uk", ".edu.au", "mit.edu", "stanford.edu", "harvard.edu", "ox.ac.uk", "cam.ac.uk", "caltech.edu")
    private val SCIENTIFIC_DOMAINS = listOf(
        "nature.com", "sciencemag.org", "science.org", "arxiv.org", "cern.ch", "esa.int",
        "scientificamerican.com", "newscientist.com", "phys.org", "cell.com", "thelancet.com",
        "pnas.org", "plos.org", "frontiersin.org", "ieee.org", "acm.org"
    )
    private val OFFICIAL_DOCS_DOMAINS = listOf(
        "docs.python.org", "developer.android.com", "developer.mozilla.org", "w3.org",
        "kotlinlang.org", "rust-lang.org", "docs.oracle.com", "learn.microsoft.com",
        "kubernetes.io", "github.com", "ietf.org"
    )
    private val REPUTABLE_EDU_DOMAINS = listOf(
        "britannica.com", "khanacademy.org", "wikipedia.org", "coursera.org",
        "edx.org", "nationalgeographic.com", "smithsonianmag.com"
    )
    private val MAJOR_NEWS_DOMAINS = listOf(
        "reuters.com", "apnews.com", "bbc.com", "bbc.co.uk", "nytimes.com",
        "theguardian.com", "bloomberg.com", "ft.com", "technologyreview.com", "arstechnica.com"
    )

    fun categorizeDomain(url: String): Pair<String, Int> {
        val host = try {
            val uri = URI(url)
            uri.host?.lowercase() ?: url.lowercase()
        } catch (e: Exception) {
            url.lowercase()
        }

        return when {
            GOV_DOMAINS.any { host.endsWith(it) || host.contains(it) } ->
                Pair("Government Organization", 98)

            EDU_DOMAINS.any { host.endsWith(it) || host.contains(it) } ->
                Pair("University / Academic", 95)

            SCIENTIFIC_DOMAINS.any { host.contains(it) } ->
                Pair("Scientific Research Institution", 94)

            OFFICIAL_DOCS_DOMAINS.any { host.contains(it) } ->
                Pair("Official Technical Documentation", 92)

            REPUTABLE_EDU_DOMAINS.any { host.contains(it) } ->
                Pair("Reputable Educational Hub", 88)

            MAJOR_NEWS_DOMAINS.any { host.contains(it) } ->
                Pair("Verified Major Publication", 84)

            host.endsWith(".org") ->
                Pair("Research / Public Organization", 80)

            else ->
                Pair("Web Knowledge Source", 75)
        }
    }

    fun rankAndFilterSources(sources: List<WebSource>): List<WebSource> {
        return sources
            .distinctBy { it.url.trimEnd('/') }
            .map { source ->
                val (category, score) = categorizeDomain(source.url)
                source.copy(
                    domainCategory = category,
                    credibilityScore = score
                )
            }
            .sortedWith(
                compareByDescending<WebSource> { it.credibilityScore }
                    .thenByDescending { it.date ?: "" }
            )
    }

    fun isQueryTimeSensitive(query: String): Boolean {
        val keywords = listOf(
            "latest", "today", "current", "recent", "new", "2026", "2025",
            "this week", "this month", "now", "upcoming", "breakthrough", "update", "discoveries"
        )
        val lower = query.lowercase()
        return keywords.any { lower.contains(it) }
    }
}
