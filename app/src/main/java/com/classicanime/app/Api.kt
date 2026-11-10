package com.classicanime.app
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Url

data class Title(val romaji: String?)
data class Cover(val large: String?)
data class SDate(val year: Int?)
data class Trailer(val id: String?, val site: String?)
data class Link(val site: String?, val url: String?, val type: String?)
data class Media(
    val id: Int, val title: Title?, val coverImage: Cover?, val bannerImage: String?,
    val description: String?, val startDate: SDate?, val genres: List<String>?,
    val format: String?, val externalLinks: List<Link>?, val trailer: Trailer?,
    val averageScore: Int?, val episodes: Int?, val status: String?
)
data class MediaPage(val media: List<Media>?)
data class PageData(val Page: MediaPage?)
data class Resp(val data: PageData?)

interface AniList {
    @GET suspend fun catalog(@Url url: String): List<Video>
    @POST("/") suspend fun query(@Body body: Map<String, @JvmSuppressWildcards Any>): Resp }

object Api {
    val svc: AniList = Retrofit.Builder().baseUrl("https://graphql.anilist.co/")
        .addConverterFactory(GsonConverterFactory.create()).build().create(AniList::class.java)
    const val F = "id title{romaji} coverImage{large} bannerImage description(asHtml:false) startDate{year} genres format externalLinks{site url type} trailer{id site} averageScore episodes status"
    const val Q = "query(\$sort:[MediaSort],\$status:MediaStatus,\$search:String,\$genre:String,\$from:FuzzyDateInt,\$to:FuzzyDateInt){Page(page:1,perPage:24){media(type:ANIME,isAdult:false,sort:\$sort,status:\$status,search:\$search,genre:\$genre,startDate_greater:\$from,startDate_lesser:\$to,format_in:[TV,MOVIE]){" + F + "}}}"
}
