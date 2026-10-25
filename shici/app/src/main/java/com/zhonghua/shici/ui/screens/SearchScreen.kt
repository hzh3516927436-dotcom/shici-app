package com.zhonghua.shici.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zhonghua.shici.ScreenTopBar
import com.zhonghua.shici.data.Repo
import com.zhonghua.shici.ui.EmptyView
import com.zhonghua.shici.ui.PoemCard
import com.zhonghua.shici.ui.SectionTitle
import com.zhonghua.shici.ui.TagChip
import com.zhonghua.shici.ui.theme.Cinnabar
import com.zhonghua.shici.ui.theme.Ink
import com.zhonghua.shici.ui.theme.InkSoft
import com.zhonghua.shici.ui.theme.Line
import com.zhonghua.shici.ui.theme.Paper
import com.zhonghua.shici.ui.theme.PaperCard

@Composable
fun SearchScreen(
    onOpenPoem: (String) -> Unit,
    onOpenPoet: (String) -> Unit,
    onOpenQuote: (String) -> Unit,
    onBack: () -> Unit
) {
    val ctx = androidx.compose.ui.platform.LocalContext.current
    var query by remember { mutableStateOf("") }
    // 输入防抖：6.7 万件全库扫描，停顿 250ms 后才执行
    var debounced by remember { mutableStateOf("") }
    androidx.compose.runtime.LaunchedEffect(query) {
        kotlinx.coroutines.delay(250)
        debounced = query.trim()
    }
    val history = remember { androidx.compose.runtime.mutableStateOf(Repo.searchHistory(ctx)) }
    val results = remember(debounced, Repo.poemVersion) { if (debounced.isNotEmpty()) Repo.search(debounced) else emptyList() }
    val quoteHits = remember(debounced, Repo.poemVersion) { if (debounced.isNotEmpty()) Repo.searchQuotes(debounced) else emptyList() }
    val poetsHit = remember(debounced, Repo.poetVersion) { if (debounced.isEmpty()) emptyList() else Repo.poets.keys.filter { it.contains(debounced) } }

    fun recordAndOpenPoem(id: String) { Repo.addSearchHistory(ctx, debounced); history.value = Repo.searchHistory(ctx); onOpenPoem(id) }
    fun recordAndOpenQuote(id: String) { Repo.addSearchHistory(ctx, debounced); history.value = Repo.searchHistory(ctx); onOpenQuote(id) }
    fun recordAndOpenPoet(name: String) { Repo.addSearchHistory(ctx, debounced); history.value = Repo.searchHistory(ctx); onOpenPoet(name) }

    Column(Modifier.fillMaxSize().background(Paper)) {
        ScreenTopBar("搜索诗词", onBack)
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("诗名 / 诗人 / 诗句 / 名句") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = InkSoft) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { query = "" }) {
                        Icon(Icons.Filled.Close, contentDescription = "清空", tint = InkSoft)
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(50),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Cinnabar,
                unfocusedBorderColor = Line,
                focusedContainerColor = PaperCard,
                unfocusedContainerColor = PaperCard,
                cursorColor = Cinnabar
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )
        if (query.isEmpty()) {
            // 搜索历史
            if (history.value.isNotEmpty()) {
                Row(
                    Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier
                            .width(4.dp)
                            .height(18.dp)
                            .background(Cinnabar, RoundedCornerShape(2.dp))
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("搜索历史", style = MaterialTheme.typography.titleMedium, color = Ink, modifier = Modifier.weight(1f))
                    Text(
                        "清空",
                        style = MaterialTheme.typography.labelMedium,
                        color = Cinnabar,
                        modifier = Modifier.clickable {
                            Repo.clearSearchHistory(ctx)
                            history.value = emptyList()
                        }
                    )
                }
                history.value.forEach { h ->
                    Surface(
                        onClick = { query = h },
                        shape = RoundedCornerShape(8.dp),
                        color = PaperCard,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Line),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 3.dp)
                    ) {
                        Text(
                            h,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Ink,
                            maxLines = 1,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                        )
                    }
                }
            }
            EmptyView("寻诗觅句", "输入诗名、诗人、诗句或名句关键词")
        } else {
            if (poetsHit.isNotEmpty()) {
                SectionTitle("相关诗人", trailing = "${poetsHit.size} 位")
                LazyRow(
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp)
                ) {
                    items(poetsHit) { p ->
                        TagChip(p, onClick = { recordAndOpenPoet(p) })
                    }
                }
            }
            // 结果分类标签：全部 / 诗词文章 / 好句
            var resultTab by remember { mutableStateOf(0) }
            Row(Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                SearchTab("全部 ${results.size + quoteHits.size}", resultTab == 0) { resultTab = 0 }
                Spacer(Modifier.width(8.dp))
                SearchTab("诗词文章 ${results.size}", resultTab == 1) { resultTab = 1 }
                Spacer(Modifier.width(8.dp))
                SearchTab("好句 ${quoteHits.size}", resultTab == 2) { resultTab = 2 }
            }
            LazyColumn {
                val showPoems = resultTab == 0 || resultTab == 1
                val showQuotes = resultTab == 0 || resultTab == 2
                if (showPoems) {
                    items(results, key = { it.id }) { p ->
                        PoemCard(p) { recordAndOpenPoem(p.id) }
                    }
                }
                if (showQuotes && quoteHits.isNotEmpty()) {
                    item {
                        SectionTitle(
                            "相关好句",
                            trailing = if (resultTab == 2) "${quoteHits.size} 条" else "前 ${quoteHits.size.coerceAtMost(6)} 条 · 更多见名句页"
                        )
                    }
                    items(
                        if (resultTab == 2) quoteHits else quoteHits.take(6),
                        key = { it.id }
                    ) { q ->
                        Surface(
                            onClick = { recordAndOpenQuote(q.id) },
                            shape = RoundedCornerShape(8.dp),
                            color = PaperCard,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Line),
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
                        ) {
                            Column(Modifier.padding(14.dp)) {
                                Text("「${q.line}」", style = MaterialTheme.typography.titleSmall, color = Ink, maxLines = 3)
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "——《${q.title}》 ${if (q.dynasty.isBlank()) "" else q.dynasty + " · "}${q.author}",
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
                if (!showPoems && !showQuotes || (showPoems && results.isEmpty() && (!showQuotes || quoteHits.isEmpty()))) {
                    item { EmptyView("没有匹配的内容", "换个关键词试试") }
                }
                item { Spacer(Modifier.height(40.dp)) }
            }
        }
    }
}

/** 搜索结果分类标签 */
@Composable
private fun SearchTab(text: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = if (selected) Cinnabar else PaperCard,
        border = if (selected) null else androidx.compose.foundation.BorderStroke(1.dp, Line)
    ) {
        Text(
            text,
            color = if (selected) com.zhonghua.shici.ui.theme.White else Ink,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
        )
    }
}

@Composable
fun FavoritesScreen(onOpenPoem: (String) -> Unit) {
    val ctx = androidx.compose.ui.platform.LocalContext.current
    val favs = remember(Repo.poemVersion) { Repo.favoritePoems(ctx) }
    val quoteFavs = remember(Repo.poemVersion) { Repo.favoriteQuotes(ctx) }
    Column(Modifier.fillMaxSize().background(Paper)) {
        Column(Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 8.dp)) {
            Text("我的收藏", style = MaterialTheme.typography.displaySmall, color = Ink)
            Spacer(Modifier.height(6.dp))
            Text("心头所好 · 常读常新", style = MaterialTheme.typography.bodySmall)
        }
        if (favs.isEmpty() && quoteFavs.isEmpty()) {
            EmptyView("书签犹空", "在诗词页或佳句卡点心形，收录心爱之作")
        } else {
            if (quoteFavs.isNotEmpty()) {
                SectionTitle("收藏的佳句", trailing = "${quoteFavs.size} 条")
                LazyColumn {
                    items(quoteFavs, key = { it.id }) { q ->
                        Surface(
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                            color = PaperCard,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Line),
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f).padding(start = 14.dp, top = 12.dp, bottom = 12.dp)) {
                                    Text("「${q.line}」", style = MaterialTheme.typography.titleSmall, color = Ink)
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        "——《${q.title}》 ${if (q.dynasty.isBlank()) "" else q.dynasty + " · "}${q.author}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                QuoteFavHeart(q.id)
                            }
                        }
                    }
                    item { Spacer(Modifier.height(8.dp)) }
                }
            }
            Text(
                "收藏的诗词 ${favs.size} 首",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            LazyColumn {
                items(favs, key = { it.id }) { p ->
                    PoemCard(p) { onOpenPoem(p.id) }
                }
                item { Spacer(Modifier.height(40.dp)) }
            }
        }
    }
}

@Composable
fun FamousScreen(onOpenQuote: (String) -> Unit, onOpenPoem: (String) -> Unit) {
    val lines = remember(Repo.poemVersion) { Repo.appreciations.toList() }
    Column(Modifier.fillMaxSize().background(Paper)) {
        Column(Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 8.dp)) {
            Text("名句集锦", style = MaterialTheme.typography.displaySmall, color = Ink)
            Spacer(Modifier.height(6.dp))
            Text("千古绝唱 · 当代佳句 · 一览风华", style = MaterialTheme.typography.bodySmall)
        }
        LazyColumn {
            items(lines, key = { it.id }) { q ->
                androidx.compose.material3.Surface(
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                    color = PaperCard,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Line),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Column(Modifier.padding(start = 18.dp, end = 6.dp, top = 18.dp, bottom = 14.dp)) {
                        Row {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    "「${q.line}」",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Ink
                                )
                            }
                            QuoteFavHeart(q.id)
                        }
                        if (q.appreciation.isNotBlank()) {
                            Spacer(Modifier.height(6.dp))
                            Text(
                                q.appreciation,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 2
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Text(
                                "——《${q.title}》 ${if (q.dynasty.isBlank()) "" else q.dynasty + " · "}${q.author}",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.End
                            )
                            if (q.poemId.isNotBlank()) {
                                Text(
                                    "查看全篇 ›",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Cinnabar,
                                    modifier = Modifier
                                        .clickable { onOpenPoem(q.poemId) }
                                        .padding(start = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(40.dp)) }
        }
    }
}

/** 佳句收藏心形（名句页/收藏页共用） */
@Composable
fun QuoteFavHeart(id: String) {
    val ctx = androidx.compose.ui.platform.LocalContext.current
    var fav by remember(id) { mutableStateOf(Repo.isQuoteFav(ctx, id)) }
    androidx.compose.material3.IconButton(onClick = { fav = Repo.toggleQuoteFav(ctx, id) }) {
        androidx.compose.material3.Icon(
            if (fav) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
            contentDescription = "收藏佳句",
            tint = if (fav) Cinnabar else InkSoft
        )
    }
}
