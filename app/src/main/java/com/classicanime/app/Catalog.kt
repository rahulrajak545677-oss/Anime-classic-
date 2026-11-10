package com.classicanime.app

data class Video(val id: String, val title: String, val year: Int?, val poster: String?, val url: String, val description: String?, val license: String?)

object Catalog {
    // Edit catalog.json in your GitHub repo to add legally hosted videos (MP4 or HLS .m3u8 links).
    const val URL = "https://raw.githubusercontent.com/rahulrajak545677-oss/Anime/main/catalog.json"
    val demo = listOf(
        Video("bbb-hls", "Big Buck Bunny (HLS demo)", 2008, null, "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8", "Open Blender short used to test the player.", "CC-BY Blender Foundation"),
        Video("bbb-mp4", "Big Buck Bunny (MP4 demo)", 2008, null, "https://download.blender.org/peach/bigbuckbunny_movies/BigBuckBunny_320x180.mp4", "Open Blender short used to test the player.", "CC-BY Blender Foundation")
    )
}
