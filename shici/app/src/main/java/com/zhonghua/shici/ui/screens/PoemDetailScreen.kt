package com.zhonghua.shici.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.zhonghua.shici.ScreenTopBar
import com.zhonghua.shici.data.Repo
import com.zhonghua.shici.ui.EmptyView
import com.zhonghua.shici.ui.SectionTitle
import com.zhonghua.shici.ui.TagChip
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PoemDetailScreen(
    id: String,
    onOpenBrowse: (Int, String?) -> Unit,
    onOpenPoet: (String) -> Unit,
    onBack: () -> Unit
) {
    val ctx = LocalContext.current
    val poem = remember { Repo.byId[id] }
    if (poem == null) {
        Column(Modifier.fillMaxSize().background(Paper)) {
            ScreenTopBar("诗词", onBack)
            EmptyView("未找到这首诗词")
        }
        return
    }
    var fav by remember(poem.id) { mutableStateOf(Repo.isFav(ctx, poem.id)) }

    Column(
        Modifier
            .fillMaxSize()
            .background(Paper)
            .verticalScroll(rememberScrollState())
    ) {
        ScreenTopBar(poem.title, onBack) {
            IconButton(onClick = { fav = Repo.toggleFav(ctx, poem.id) }) {
                Icon(
                    if (fav) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "收藏",
                    tint = if (fav) Cinnabar else InkSoft
                )
            }
        }
        Column(Modifier.padding(horizontal = 20.dp)) {
            // ------- 题头 -------
            Column(
                Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(poem.dynasty, style = MaterialTheme.typography.bodySmall, color = Cinnabar)
                Spacer(Modifier.height(4.dp))
                Text(
                    poem.title,
                    style = MaterialTheme.typography.displaySmall,
                    textAlign = TextAlign.Center,
                    color = Ink
                )
                poem.tune?.let {
                    Spacer(Modifier.height(4.dp))
                    Text("词牌 · $it", style = MaterialTheme.typography.labelMedium, color = Gold)
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "〔${poem.dynasty}〕${poem.author}",
                    style = MaterialTheme.typography.titleSmall,
                    color = InkSoft,
                    modifier = Modifier.clickable { onOpenPoet(poem.author) }
                )
                poem.textbook?.let {
                    Spacer(Modifier.height(8.dp))
                    Surface(shape = RoundedCornerShape(50), color = GoldMist) {
                        Text(
                            it,
                            color = Gold,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            // ------- 正文（长文如序赋散文用较小字号） -------
            val longForm = poem.body.length > 300
            val bodyStyle = if (longForm) MaterialTheme.typography.bodyMedium
            else MaterialTheme.typography.bodyLarge
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = PaperCard,
                border = BorderStroke(1.dp, Line),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    poem.bodyLines.forEach { line ->
                        if (line.isBlank()) {
                            Spacer(Modifier.height(18.dp))
                        } else {
                            Text(
                                line,
                                style = bodyStyle,
                                textAlign = if (longForm) TextAlign.Start else TextAlign.Center,
                                color = Ink
                            )
                        }
                    }
                }
            }
            // ------- 名句 -------
            if (poem.famous.isNotEmpty()) {
                Spacer(Modifier.height(14.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = CinnabarMist,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        poem.famous.forEach {
                            Text(
                                "「$it」",
                                style = MaterialTheme.typography.titleMedium,
                                color = CinnabarDeep,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
            // ------- 白话译文 -------
            if (poem.translation.isNotBlank()) {
                SectionTitle("白话译文")
                Column(Modifier.padding(horizontal = 16.dp)) {
                    poem.translation.split("\n").forEach {
                        if (it.isBlank()) Spacer(Modifier.height(6.dp))
                        else Text(it, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            // ------- 注释 -------
            if (poem.notes.isNotEmpty()) {
                SectionTitle("注释")
                Column(Modifier.padding(horizontal = 16.dp)) {
                    poem.notes.forEach { n ->
                        Row(Modifier.padding(vertical = 3.dp)) {
                            Text(
                                "${n.w}　",
                                style = MaterialTheme.typography.titleSmall,
                                color = Dai
                            )
                            Text(n.n, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
            // ------- 创作背景 -------
            if (poem.background.isNotBlank()) {
                SectionTitle("创作背景")
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = PaperCard,
                    border = BorderStroke(1.dp, Line),
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Row(Modifier.height(IntrinsicSize.Min)) {
                        Box(
                            Modifier
                                .width(4.dp)
                                .fillMaxHeight()
                                .background(Cinnabar)
                        )
                        Text(
                            poem.background,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Justify,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
            // ------- 所属分类 -------
            SectionTitle("所属分类")
            FlowRow(
                Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TagChip(poem.dynasty) { onOpenBrowse(0, poem.dynasty) }
                TagChip(poem.author) { onOpenPoet(poem.author) }
                TagChip(poem.form) { onOpenBrowse(2, poem.form) }
                poem.tune?.let { TagChip(it) }
                poem.themes.forEach { t ->
                    TagChip(t) { onOpenBrowse(1, t) }
                }
                poem.textbook?.let { TagChip(it) { onOpenBrowse(3, it) } }
            }
            Spacer(Modifier.height(50.dp))
        }
    }
}
