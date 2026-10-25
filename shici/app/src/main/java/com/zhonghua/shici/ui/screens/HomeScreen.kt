package com.zhonghua.shici.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zhonghua.shici.data.Repo
import com.zhonghua.shici.ui.SectionTitle
import com.zhonghua.shici.ui.SealBadge
import com.zhonghua.shici.ui.TagChip
import com.zhonghua.shici.ui.VerticalChars
import com.zhonghua.shici.ui.theme.Cinnabar
import com.zhonghua.shici.ui.theme.CinnabarDeep
import com.zhonghua.shici.ui.theme.CinnabarMist
import com.zhonghua.shici.ui.theme.Dai
import com.zhonghua.shici.ui.theme.Gold
import com.zhonghua.shici.ui.theme.GoldMist
import com.zhonghua.shici.ui.theme.Ink
import com.zhonghua.shici.ui.theme.InkSoft
import com.zhonghua.shici.ui.theme.Line
import com.zhonghua.shici.ui.theme.Paper
import com.zhonghua.shici.ui.theme.PaperCard

@Composable
fun HomeScreen(
    onOpenPoem: (String) -> Unit,
    onSearch: () -> Unit,
    onBrowse: (Int, String?) -> Unit,
    onPoets: () -> Unit,
    onFamous: () -> Unit,
    onFavs: () -> Unit,
    onUserAdd: () -> Unit
) {
    val ctx = androidx.compose.ui.platform.LocalContext.current
    var daily by remember { mutableStateOf(Repo.dailyPoem(ctx)) }
    var favState by remember(daily?.id) { mutableStateOf(daily?.let { Repo.isFav(ctx, it.id) } ?: false) }
    var appre by remember { mutableStateOf(Repo.dailyAppreciation()) }
    var quoteFav by remember(appre?.id) { mutableStateOf(appre?.let { Repo.isQuoteFav(ctx, it.id) } ?: false) }
    val dynasties = remember(Repo.poemVersion) { Repo.countsByDynasty() }
    val themeCount = remember(Repo.poemVersion) { Repo.allThemes().size }
    val formCount = remember(Repo.poemVersion) { Repo.allForms().size }
    val poetCount = remember(Repo.poetVersion) { Repo.poets.size }

    LazyColumn(Modifier.fillMaxSize().background(Paper)) {
        // ------- 顶部题字 -------
        item {
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(PaperCard, Paper)))
            ) {
                Text(
                    "詩",
                    fontSize = 150.sp,
                    color = Ink.copy(alpha = 0.05f),
                    fontFamily = MaterialTheme.typography.titleMedium.fontFamily,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 4.dp, end = 12.dp)
                )
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, top = 26.dp, bottom = 18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("诗词典藏", style = MaterialTheme.typography.displaySmall, color = Ink)
                        Spacer(Modifier.height(6.dp))
                        Text("中华上下五千年 · 名家名篇", style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.height(10.dp))
                        Surface(shape = RoundedCornerShape(50), color = CinnabarMist) {
                            Text(
                                "今日 · ${Repo.todayText()}",
                                style = MaterialTheme.typography.labelMedium,
                                color = CinnabarDeep,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Spacer(Modifier.width(14.dp))
                    VerticalChars("翰墨千秋", Gold, 13)
                }
            }
        }
        // ------- 搜索条 + 添加 -------
        item {
            Row(Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = PaperCard,
                    border = BorderStroke(1.dp, Line),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSearch() }
                ) {
                    Row(
                        Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Search, contentDescription = "搜索", tint = InkSoft)
                        Spacer(Modifier.width(10.dp))
                        Text("搜索诗名、诗人、名句……", style = MaterialTheme.typography.bodySmall)
                    }
                }
                Spacer(Modifier.width(10.dp))
                Surface(
                    onClick = onUserAdd,
                    shape = RoundedCornerShape(50),
                    color = Cinnabar
                ) {
                    Row(
                        Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("＋ 添加", color = com.zhonghua.shici.ui.theme.White, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
        // ------- 今日荐读 -------
        item {
            SectionTitle("今日荐读")
            daily?.let { d ->
                Surface(
                    onClick = { onOpenPoem(d.id) },
                    shape = RoundedCornerShape(8.dp),
                    color = PaperCard,
                    border = BorderStroke(1.dp, Line),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Row(Modifier.height(IntrinsicSize.Min)) {
                        Box(
                            Modifier
                                .width(4.dp)
                                .fillMaxHeight()
                                .background(Cinnabar)
                        )
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 18.dp, vertical = 20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(d.title, style = MaterialTheme.typography.headlineMedium, color = Ink)
                            Spacer(Modifier.height(4.dp))
                            Text(d.meta, style = MaterialTheme.typography.bodySmall)
                            Spacer(Modifier.height(14.dp))
                            val lines = d.bodyLines.filter { it.isNotBlank() }
                            lines.take(6).forEach {
                                Text(it, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
                            }
                            if (lines.size > 6) {
                                Spacer(Modifier.height(4.dp))
                                Text("……", style = MaterialTheme.typography.bodyMedium, color = InkSoft)
                            }
                            Spacer(Modifier.height(14.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                TextButton(onClick = {
                                    Repo.reshuffleDaily(ctx)?.let {
                                        daily = it
                                        favState = Repo.isFav(ctx, it.id)
                                    }
                                }) {
                                    Text("换一首", color = Cinnabar, style = MaterialTheme.typography.labelLarge)
                                }
                                IconButton(onClick = {
                                    favState = Repo.toggleFav(ctx, d.id)
                                }) {
                                    Icon(
                                        if (favState) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                        contentDescription = "收藏",
                                        tint = if (favState) Cinnabar else InkSoft
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        // ------- 每日佳句赏析 -------
        item {
            SectionTitle("每日佳句赏析")
            appre?.let { q ->
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = com.zhonghua.shici.ui.theme.PaperDeep,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("「${q.line}」", style = MaterialTheme.typography.titleMedium, color = Dai)
                        if (q.appreciation.isNotBlank()) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                q.appreciation,
                                style = MaterialTheme.typography.bodySmall,
                                color = Ink
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("——《${q.title}》", style = MaterialTheme.typography.labelMedium)
                                Text(
                                    if (q.dynasty.isBlank()) q.author else "${q.dynasty} · ${q.author}",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                            TextButton(onClick = { appre = Repo.randomAppreciation(q.id) }) {
                                Text("换一句", color = Dai, style = MaterialTheme.typography.labelLarge)
                            }
                            IconButton(onClick = { quoteFav = Repo.toggleQuoteFav(ctx, q.id) }) {
                                Icon(
                                    if (quoteFav) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                    contentDescription = "收藏佳句",
                                    tint = if (quoteFav) Cinnabar else InkSoft
                                )
                            }
                            if (q.poemId.isNotBlank()) {
                                TextButton(onClick = { onOpenPoem(q.poemId) }) {
                                    Text("查看全篇 →", color = Cinnabar, style = MaterialTheme.typography.labelLarge)
                                }
                            }
                        }
                    }
                }
            }
        }
        // ------- 分类宫格 -------
        item {
            SectionTitle("分类浏览")
            Column(Modifier.padding(horizontal = 16.dp)) {
                Row {
                    EntryCard("朝", "朝代", "${dynasties.size} 朝", Modifier.weight(1f)) { onBrowse(0, null) }
                    Spacer(Modifier.width(10.dp))
                    EntryCard("材", "题材", "$themeCount 类", Modifier.weight(1f)) { onBrowse(1, null) }
                }
                Spacer(Modifier.height(10.dp))
                Row {
                    EntryCard("体", "体裁", "$formCount 体", Modifier.weight(1f)) { onBrowse(2, null) }
                    Spacer(Modifier.width(10.dp))
                    EntryCard("家", "诗人", "$poetCount 家", Modifier.weight(1f)) { onPoets() }
                }
                Spacer(Modifier.height(10.dp))
                Row {
                    EntryCard("句", "名句", "撷句成锦", Modifier.weight(1f)) { onFamous() }
                    Spacer(Modifier.width(10.dp))
                    EntryCard("藏", "收藏", "心头所好", Modifier.weight(1f)) { onFavs() }
                }
            }
        }
        // ------- 朝代长卷 -------
        item {
            SectionTitle("朝代长卷")
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                items(dynasties) { d ->
                    TagChip("${d.first} · ${d.second}", onClick = { onBrowse(0, d.first) })
                }
            }
        }
        // ------- 尾注 -------
        item {
            Box(Modifier.fillMaxWidth().padding(top = 28.dp, bottom = 30.dp)) {
                Text(
                    "—— 卷帙浩繁 · 撷英拾萃 ——",
                    style = MaterialTheme.typography.bodySmall,
                    color = InkSoft,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun EntryCard(char: String, label: String, sub: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = PaperCard,
        border = BorderStroke(1.dp, Line),
        modifier = modifier.clickable { onClick() }
    ) {
        Row(
            Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(40.dp)
                    .background(CinnabarMist, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    char,
                    color = CinnabarDeep,
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = MaterialTheme.typography.titleMedium.fontFamily
                )
            }
            Spacer(Modifier.width(10.dp))
            Column {
                Text(label, style = MaterialTheme.typography.titleSmall)
                Text(sub, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
