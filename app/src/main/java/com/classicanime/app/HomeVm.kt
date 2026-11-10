package com.classicanime.app
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class HomeVm : ViewModel() {
    val decades = listOf(Triple("Golden 70s", 19700000, 19800000), Triple("Golden 80s", 19800000, 19900000), Triple("Cult 90s", 19900000, 20000000), Triple("Y2K Era", 20000000, 20100000))
    val order = listOf("Trending now", "Airing now", "Top rated") + decades.map { it.first }
    val genres = listOf("Action", "Adventure", "Comedy", "Drama", "Fantasy", "Horror", "Mecha", "Romance", "Sci-Fi", "Slice of Life", "Sports", "Supernatural")
    val rows = mutableStateMapOf<String, List<Media>>()
    val videos = mutableStateListOf<Video>()
    val saved = mutableStateListOf<Media>()
    var results by mutableStateOf<List<Media>>(emptyList())
    var error by mutableStateOf<String?>(null)

    private suspend fun fetch(v: Map<String, Any>) = Api.svc.query(mapOf("query" to Api.Q, "variables" to v)).data?.Page?.media.orEmpty()
    private fun load(name: String, v: Map<String, Any>) {
        viewModelScope.launch { try { rows[name] = fetch(v) } catch (e: Exception) { error = "Couldn't load titles. Check your connection." } }
    }
    init {
        viewModelScope.launch { videos.addAll(try { Api.svc.catalog(Catalog.URL) } catch (e: Exception) { Catalog.demo }) }
        load("Trending now", mapOf("sort" to listOf("TRENDING_DESC")))
        load("Airing now", mapOf("sort" to listOf("POPULARITY_DESC"), "status" to "RELEASING"))
        load("Top rated", mapOf("sort" to listOf("SCORE_DESC")))
        decades.forEach { (n, f, t) -> load(n, mapOf("sort" to listOf("POPULARITY_DESC"), "from" to f, "to" to t)) }
    }
    fun search(q: String, genre: String?) {
        viewModelScope.launch {
            try {
                val v = mutableMapOf<String, Any>("sort" to listOf(if (q.isBlank()) "POPULARITY_DESC" else "SEARCH_MATCH"))
                if (q.isNotBlank()) v["search"] = q
                if (genre != null) v["genre"] = genre
                results = fetch(v)
            } catch (e: Exception) { error = "Search failed. Check your connection." }
        }
    }
    fun isSaved(m: Media) = saved.any { it.id == m.id }
    fun toggle(m: Media) { if (isSaved(m)) saved.removeAll { it.id == m.id } else saved.add(m) }
}
