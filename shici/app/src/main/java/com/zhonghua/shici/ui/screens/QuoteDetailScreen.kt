package com.zhonghua.shici.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.zhonghua.shici.ui.theme.Cinnabar
import com.zhonghua.shici.ui.theme.Dai
import com.zhonghua.shici.ui.theme.Gold
import com.zhonghua.shici.ui.theme.Ink
import com.zhonghua.shici.ui.theme.InkSoft
import com.zhonghua.shici.ui.theme.Line
import com.zhonghua.shici.ui.theme.Paper
import com.zhonghua.shici.ui.theme.PaperCard

/** 好句详情：佳句本体 + 出处 + 赏析 + 收藏 + 查看原篇 */
@Composable
fun QuoteDetailScreen(id: String, onOpenPoem: (String) -> Unit, onBack: () -> Unit) {
    val ctx = LocalContext.current
    val quote = remember(id) { Repo.findAppreciation(id) }
    Column(Modifier.fillMaxSize().background(Paper)) {
        if (quote == null) {
            ScreenTopBar("好句", onBack)
            EmptyView("未找到这条好句")
            return@Column
        }
        var fav by remember(id) { mutableStateOf(Repo.isQuoteFav(ctx, id)) }
        ScreenTopBar("好句", onBack)
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Column(
                Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (quote.dynasty.isNotBlank()) {
                    Text(quote.dynasty, style = MaterialTheme.typography.bodySmall, color = Cinnabar)
                    Spacer(Modifier.height(4.dp))
                }
                Text(
                    "「${quote.line}」",
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                    color = Ink
                )
                Spacer(Modifier.height(10.dp))
                Text("——《${quote.title}》", style = MaterialTheme.typography.titleSmall, color = Ink)
                Text(quote.author, style = MaterialTheme.typography.bodySmall, color = InkSoft)
            }
            Spacer(Modifier.height(18.dp))
            if (quote.appreciation.isNotBlank()) {
                SectionTitle("赏析")
                Surface(
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                    color = PaperCard,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Line),
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Text(
                        quote.appreciation,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                Spacer(Modifier.height(14.dp))
            }
            if (quote.poemId.isNotBlank()) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    OutlinedButton(onClick = { onOpenPoem(quote.poemId) }) {
                        Text("查看原篇全文 →", color = Dai)
                    }
                }
                Spacer(Modifier.height(14.dp))
            }
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (fav) "已收录在心 · 再点取消" else "点此收录在心",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (fav) Gold else InkSoft
                )
                Spacer(Modifier.width(10.dp))
                IconButton(onClick = { fav = Repo.toggleQuoteFav(ctx, id) }) {
                    Icon(
                        if (fav) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "收藏佳句",
                        tint = if (fav) Cinnabar else InkSoft
                    )
                }
            }
            Spacer(Modifier.height(50.dp))
        }
    }
}
