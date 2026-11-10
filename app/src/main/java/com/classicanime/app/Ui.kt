package com.classicanime.app
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import kotlinx.coroutines.delay
import androidx.compose.ui.platform.LocalContext
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.ui.viewinterop.AndroidView

val Bg = Color(0xFF0A0714); val Gold = Color(0xFFFF6FA5); val Violet = Color(0xFF8B5CF6)
val Ink = Color(0xFFF2F0EA); val Muted = Color(0xFFB8B6C0)

fun Modifier.glass(r: Dp = 24.dp) = this.clip(RoundedCornerShape(r))
    .background(Brush.linearGradient(listOf(Color(0x33FFFFFF), Color(0x0DFFFFFF))))
    .border(1.dp, Brush.linearGradient(listOf(Color(0x66FFFFFF), Color(0x0FFFFFFF))), RoundedCornerShape(r))

fun String?.plain() = (this ?: "").replace(Regex("<[^>]*>"), "").trim()

@Composable fun App(vm: HomeVm = viewModel()) {
    var tab by remember { mutableIntStateOf(0) }
    var sel by remember { mutableStateOf<Media?>(null) }
    var playing by remember { mutableStateOf<Video?>(null) }
    Box(Modifier.fillMaxSize().background(Bg).drawBehind {
        drawCircle(Brush.radialGradient(listOf(Color(0x66FF4F9A), Color.Transparent), Offset(size.width * 0.9f, size.height * 0.1f), 700f), 700f, Offset(size.width * 0.9f, size.height * 0.1f))
        drawCircle(Brush.radialGradient(listOf(Color(0x668B5CF6), Color.Transparent), Offset(0f, size.height * 0.7f), 800f), 800f, Offset(0f, size.height * 0.7f))
    }) {
        when (tab) { 0 -> Home(vm, { sel = it }, { playing = it }); 1 -> SearchTab(vm) { sel = it }; else -> Library(vm) { sel = it } }
        NavBar(tab, Modifier.align(Alignment.BottomCenter)) { tab = it }
        sel?.let { Detail(it, vm) { sel = null } }
        playing?.let { VideoPlayer(it) { playing = null } }
    }
}

@Composable fun Home(vm: HomeVm, open: (Media) -> Unit, play: (Video) -> Unit) {
    val hero = vm.rows["Trending now"]?.firstOrNull()
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 120.dp)) {
        item {
            Box(Modifier.fillMaxWidth().height(420.dp)) {
                AsyncImage(hero?.bannerImage ?: hero?.coverImage?.large, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Bg), startY = 300f)))
                Text("アニメ", color = Color(0x22FFFFFF), fontSize = 64.sp, fontWeight = FontWeight.Black, modifier = Modifier.align(Alignment.TopEnd).statusBarsPadding().padding(20.dp))
                if (hero != null) Column(Modifier.align(Alignment.BottomStart).padding(20.dp).fillMaxWidth().glass().padding(18.dp)) {
                    Text("TRENDING NOW · 話題", color = Gold, fontSize = 11.sp, letterSpacing = 2.sp)
                    Text(hero.title?.romaji ?: "", color = Ink, fontSize = 28.sp, fontFamily = FontFamily.Serif, fontWeight = FontWeight.SemiBold)
                    Text("${hero.startDate?.year ?: ""} · ${hero.format ?: ""}", color = Muted, fontSize = 13.sp)
                    Spacer(Modifier.height(12.dp))
                    GoldButton("View") { open(hero) }
                }
            }
        }
        if (vm.videos.isNotEmpty()) item {
            Text("Free to stream", color = Ink, fontSize = 20.sp, fontFamily = FontFamily.Serif, modifier = Modifier.padding(20.dp, 20.dp, 20.dp, 12.dp))
            LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) { items(vm.videos) { VideoCard(it) { play(it) } } }
        }
        vm.error?.let { item { Text(it, color = Muted, modifier = Modifier.padding(20.dp)) } }
        vm.order.forEach { name ->
            item {
                Text(name, color = Ink, fontSize = 20.sp, fontFamily = FontFamily.Serif, modifier = Modifier.padding(20.dp, 20.dp, 20.dp, 12.dp))
                LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    items(vm.rows[name].orEmpty()) { Poster(it) { open(it) } }
                }
            }
        }
    }
}

@Composable fun Library(vm: HomeVm, open: (Media) -> Unit) {
    Column(Modifier.fillMaxSize().statusBarsPadding().padding(20.dp)) {
        Text("My Watchlist", color = Ink, fontSize = 28.sp, fontFamily = FontFamily.Serif)
        if (vm.saved.isEmpty()) Text("Save classics from their detail page.", color = Muted, modifier = Modifier.padding(top = 12.dp))
        LazyRow(Modifier.padding(top = 20.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) { items(vm.saved) { Poster(it) { open(it) } } }
    }
}

@Composable fun Poster(m: Media, onClick: () -> Unit) {
    Column(Modifier.width(120.dp).clickable(onClick = onClick)) {
        Box {
            AsyncImage(m.coverImage?.large, m.title?.romaji, Modifier.size(120.dp, 172.dp).clip(RoundedCornerShape(16.dp)).border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(16.dp)), contentScale = ContentScale.Crop)
            m.averageScore?.let { Text("★ ${it / 10.0}", color = Ink, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.align(Alignment.TopEnd).padding(6.dp).glass(10.dp).padding(6.dp, 3.dp)) }
        }
        Text(m.title?.romaji ?: "", color = Ink, fontSize = 13.sp, maxLines = 2, modifier = Modifier.padding(top = 8.dp))
        Text("${m.startDate?.year ?: ""}", color = Muted, fontSize = 12.sp)
    }
}

@Composable fun GoldButton(label: String, onClick: () -> Unit) {
    Box(Modifier.height(48.dp).clip(RoundedCornerShape(24.dp)).background(Gold).clickable(onClick = onClick).padding(horizontal = 28.dp), Alignment.Center) {
        Text(label, color = Bg, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
    }
}

@Composable fun NavBar(tab: Int, modifier: Modifier, onTab: (Int) -> Unit) {
    Row(modifier.navigationBarsPadding().padding(20.dp).fillMaxWidth().height(64.dp).glass(32.dp), Arrangement.SpaceEvenly, Alignment.CenterVertically) {
        listOf("Home" to "ホーム", "Search" to "検索", "Library" to "保存").forEachIndexed { i, (l, k) ->
            Column(Modifier.clickable { onTab(i) }.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(l, color = if (tab == i) Gold else Muted, fontWeight = FontWeight.Medium)
                Text(k, color = if (tab == i) Gold else Muted, fontSize = 10.sp)
            }
        }
    }
}

@Composable fun Detail(m: Media, vm: HomeVm, close: () -> Unit) {
    BackHandler(onBack = close)
    var web by remember { mutableStateOf<String?>(null) }
    Box(Modifier.fillMaxSize().background(Bg)) {
        AsyncImage(m.coverImage?.large, null, Modifier.fillMaxWidth().height(380.dp).blur(40.dp), contentScale = ContentScale.Crop)
        Box(Modifier.fillMaxWidth().height(380.dp).background(Brush.verticalGradient(listOf(Color(0x440B0B10), Bg))))
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).statusBarsPadding().padding(20.dp).navigationBarsPadding()) {
            Text("‹ Back", color = Ink, modifier = Modifier.glass(22.dp).clickable(onClick = close).padding(16.dp, 10.dp))
            Row(Modifier.padding(top = 24.dp), verticalAlignment = Alignment.Bottom) {
                AsyncImage(m.coverImage?.large, null, Modifier.size(130.dp, 188.dp).clip(RoundedCornerShape(16.dp)).border(1.dp, Color(0x44FFFFFF), RoundedCornerShape(16.dp)), contentScale = ContentScale.Crop)
                Column(Modifier.padding(start = 16.dp)) {
                    Text(m.title?.romaji ?: "", color = Ink, fontSize = 26.sp, fontFamily = FontFamily.Serif, fontWeight = FontWeight.SemiBold)
                    Text(listOfNotNull(m.startDate?.year, m.format, m.episodes?.let { "$it eps" }, m.averageScore?.let { "★ ${it / 10.0}" }).joinToString(" · "), color = Muted, fontSize = 13.sp)
                }
            }
            Row(Modifier.padding(top = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                m.genres.orEmpty().take(3).forEach { Text(it, color = Ink, fontSize = 12.sp, modifier = Modifier.glass(14.dp).padding(12.dp, 6.dp)) }
            }
            Text(m.description.plain(), color = Muted, fontSize = 14.sp, lineHeight = 22.sp, modifier = Modifier.padding(top = 16.dp).fillMaxWidth().glass(20.dp).padding(16.dp))
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                GoldButton(if (vm.isSaved(m)) "Saved ✓" else "+ Watchlist") { vm.toggle(m) }
                m.trailer?.takeIf { it.site.equals("youtube", true) && it.id != null }?.let { t -> GoldButton("▶ Trailer") { web = "https://www.youtube.com/embed/${t.id}" } }
            }
            Text("Where to watch", color = Ink, fontSize = 20.sp, fontFamily = FontFamily.Serif, modifier = Modifier.padding(top = 24.dp, bottom = 12.dp))
            val links = m.externalLinks.orEmpty().filter { it.type == "STREAMING" && it.url != null }
            if (links.isEmpty()) Text("No streaming link listed yet.", color = Muted)
            links.forEach { l -> Text("${l.site} ›", color = Gold, modifier = Modifier.padding(bottom = 10.dp).fillMaxWidth().glass(18.dp).clickable { web = l.url }.padding(16.dp)) }
            Spacer(Modifier.height(24.dp))
        }
        web?.let { Player(it) { web = null } }
    }
}

@Composable fun Player(url: String, close: () -> Unit) {
    BackHandler(onBack = close)
    Column(Modifier.fillMaxSize().background(Bg).statusBarsPadding()) {
        Text("‹ Close", color = Ink, modifier = Modifier.padding(16.dp).glass(22.dp).clickable(onClick = close).padding(16.dp, 10.dp))
        AndroidView(factory = { c -> WebView(c).apply {
            settings.javaScriptEnabled = true; settings.domStorageEnabled = true
            settings.mediaPlaybackRequiresUserGesture = false
            webViewClient = WebViewClient(); webChromeClient = WebChromeClient(); loadUrl(url)
        } }, modifier = Modifier.fillMaxSize())
    }
}

@Composable fun VideoCard(v: Video, onClick: () -> Unit) {
    Column(Modifier.width(160.dp).clickable(onClick = onClick)) {
        Box(Modifier.size(160.dp, 100.dp).clip(RoundedCornerShape(16.dp)).background(Brush.linearGradient(listOf(Color(0xFF4A1E36), Color(0xFF1B1A3A)))).border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(16.dp)), Alignment.Center) {
            if (v.poster != null) AsyncImage(v.poster, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            Text("▶", color = Gold, fontSize = 28.sp)
        }
        Text(v.title, color = Ink, fontSize = 13.sp, maxLines = 2, modifier = Modifier.padding(top = 8.dp))
        Text(v.license ?: "", color = Muted, fontSize = 11.sp, maxLines = 1)
    }
}

@Composable fun VideoPlayer(v: Video, close: () -> Unit) {
    BackHandler(onBack = close)
    val ctx = LocalContext.current
    val player = remember { ExoPlayer.Builder(ctx).build().apply { setMediaItem(MediaItem.fromUri(v.url)); prepare(); playWhenReady = true } }
    DisposableEffect(Unit) { onDispose { player.release() } }
    Column(Modifier.fillMaxSize().background(Bg).statusBarsPadding()) {
        Text("‹ Close", color = Ink, modifier = Modifier.padding(16.dp).glass(22.dp).clickable(onClick = close).padding(16.dp, 10.dp))
        AndroidView(factory = { PlayerView(it).apply { this.player = player } }, modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f))
        Column(Modifier.padding(20.dp)) {
            Text(v.title, color = Ink, fontSize = 24.sp, fontFamily = FontFamily.Serif, fontWeight = FontWeight.SemiBold)
            Text("${v.year ?: ""} · ${v.license ?: ""}", color = Muted, fontSize = 13.sp, modifier = Modifier.padding(top = 6.dp))
            Text(v.description ?: "", color = Muted, fontSize = 14.sp, lineHeight = 22.sp, modifier = Modifier.padding(top = 14.dp).fillMaxWidth().glass(20.dp).padding(16.dp))
        }
    }
}

@Composable fun SearchTab(vm: HomeVm, open: (Media) -> Unit) {
    var q by remember { mutableStateOf("") }
    var genre by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(q, genre) { delay(400); vm.search(q, genre) }
    Column(Modifier.fillMaxSize().statusBarsPadding().padding(top = 20.dp)) {
        Text("Search · 検索", color = Ink, fontSize = 28.sp, fontFamily = FontFamily.Serif, modifier = Modifier.padding(horizontal = 20.dp))
        BasicTextField(q, { q = it }, singleLine = true, textStyle = TextStyle(color = Ink, fontSize = 16.sp), cursorBrush = SolidColor(Gold),
            modifier = Modifier.padding(20.dp).fillMaxWidth().glass(26.dp).padding(18.dp, 14.dp),
            decorationBox = { inner -> if (q.isEmpty()) Text("Search anime…", color = Muted); inner() })
        LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(vm.genres) { g ->
                Text(g, color = if (genre == g) Bg else Ink, fontSize = 13.sp, modifier = Modifier.clip(RoundedCornerShape(16.dp))
                    .background(if (genre == g) Gold else Color(0x22FFFFFF)).clickable { genre = if (genre == g) null else g }.padding(14.dp, 8.dp))
            }
        }
        LazyVerticalGrid(GridCells.Fixed(3), contentPadding = PaddingValues(20.dp, 20.dp, 20.dp, 120.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(vm.results) { Poster(it) { open(it) } }
        }
    }
}
