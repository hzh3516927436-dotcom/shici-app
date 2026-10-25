package com.zhonghua.shici.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.zhonghua.shici.ScreenTopBar
import com.zhonghua.shici.data.Poet
import com.zhonghua.shici.data.Repo
import com.zhonghua.shici.ui.PoemCard
import com.zhonghua.shici.ui.SealBadge
import com.zhonghua.shici.ui.theme.Dai
import com.zhonghua.shici.ui.theme.DaiMist
import com.zhonghua.shici.ui.theme.Gold
import com.zhonghua.shici.ui.theme.GoldMist
import com.zhonghua.shici.ui.theme.Ink
import com.zhonghua.shici.ui.theme.InkSoft
import com.zhonghua.shici.ui.theme.Line
import com.zhonghua.shici.ui.theme.Paper
import com.zhonghua.shici.ui.theme.PaperCard
import com.zhonghua.shici.ui.theme.PaperDeep

@Composable
fun PoetsScreen(onOpenPoet: (String) -> Unit) {
    val groups = remember(Repo.poetVersion, Repo.poemVersion) {
        Repo.dynastyOrder.map { d ->
            d to Repo.poets.values
                .filter { it.dynasty == d }
                .sortedByDescending { Repo.poemsOf(it.name).size }
        }.filter { it.second.isNotEmpty() }
    }
    LazyColumn(Modifier.fillMaxSize().background(Paper)) {
        item {
            Column(Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 8.dp)) {
                Text("诗人长廊", style = MaterialTheme.typography.displaySmall, color = Ink)
                Spacer(Modifier.height(6.dp))
                Text("百代才人 · 各领风骚", style = MaterialTheme.typography.bodySmall)
            }
        }
        groups.forEach { g ->
            item(key = "header_${g.first}") {
                Row(
                    Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(g.first, style = MaterialTheme.typography.titleMedium, color = Ink)
                    Spacer(Modifier.width(8.dp))
                    Box(
                        Modifier
                            .weight(1f)
                            .height(1.dp)
                            .background(Line)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("${g.second.size} 家", style = MaterialTheme.typography.labelMedium)
                }
            }
            items(g.second, key = { it.name }) { poet ->
                val works = Repo.poemsOf(poet.name)
                Surface(
                    onClick = { onOpenPoet(poet.name) },
                    shape = RoundedCornerShape(8.dp),
                    color = PaperCard,
                    border = BorderStroke(1.dp, Line),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Row(
                        Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SealBadge(poet.name, size = 42.dp, fontSize = 18)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(poet.name, style = MaterialTheme.typography.titleMedium)
                                if (poet.honor.isNotBlank()) {
                                    Spacer(Modifier.width(6.dp))
                                    Text(poet.honor, style = MaterialTheme.typography.labelMedium, color = Gold)
                                }
                            }
                            Spacer(Modifier.height(2.dp))
                            Text(
                                "${if (poet.years.isBlank()) "" else poet.years + " · "}${works.size} 首",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Text("›", style = MaterialTheme.typography.titleMedium, color = InkSoft)
                    }
                }
            }
        }
        item { Spacer(Modifier.height(40.dp)) }
    }
}

@Composable
fun PoetDetailScreen(
    name: String,
    onOpenPoem: (String) -> Unit,
    onBack: () -> Unit
) {
    val poet = remember { Repo.poets[name] ?: Poet(name, "", "", "", "") }
    val works = remember { Repo.poemsOf(name) }
    // 懒加载列表：名家可达数千首，必须用 LazyColumn 逐条按需组装（Column+forEach 会一次全部组装导致 ANR）
    LazyColumn(Modifier.fillMaxSize().background(Paper)) {
        item {
            ScreenTopBar(name, onBack)
        }
        item {
            Column(
                Modifier.padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SealBadge(name, size = 68.dp, fontSize = 30)
                Spacer(Modifier.height(12.dp))
                Text(name, style = MaterialTheme.typography.displaySmall, textAlign = TextAlign.Center)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (poet.honor.isNotBlank()) {
                        Surface(shape = RoundedCornerShape(50), color = GoldMist) {
                            Text(
                                poet.honor,
                                color = Gold,
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                    }
                    if (poet.dynasty.isNotBlank()) {
                        Surface(shape = RoundedCornerShape(50), color = DaiMist) {
                            Text(
                                poet.dynasty,
                                color = Dai,
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                    }
                    if (poet.years.isNotBlank()) {
                        Text(poet.years, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
        if (poet.bio.isNotBlank()) {
            item {
                Spacer(Modifier.height(14.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = PaperDeep,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Text(
                        poet.bio,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Justify,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
        item {
            com.zhonghua.shici.ui.SectionTitle("收录作品", trailing = "${works.size} 首")
        }
        items(works, key = { it.id }) { p ->
            PoemCard(p) { onOpenPoem(p.id) }
        }
        item { Spacer(Modifier.height(40.dp)) }
    }
}
