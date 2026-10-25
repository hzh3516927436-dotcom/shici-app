package com.zhonghua.shici.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zhonghua.shici.data.Repo
import com.zhonghua.shici.ui.PoemCard
import com.zhonghua.shici.ui.TagChip
import com.zhonghua.shici.ui.theme.Cinnabar
import com.zhonghua.shici.ui.theme.InkSoft
import com.zhonghua.shici.ui.theme.Line
import com.zhonghua.shici.ui.theme.Paper

@Composable
fun BrowseScreen(
    initialDim: Int,
    initialValue: String?,
    onOpenPoem: (String) -> Unit
) {
    var dim by remember { mutableStateOf(initialDim.coerceIn(0, 3)) }
    var value by remember { mutableStateOf(initialValue) }
    val tabs = listOf("朝代", "题材", "体裁", "教材")

    val values: List<Pair<String, Int>> = remember(dim, Repo.poemVersion) {
        when (dim) {
            0 -> Repo.countsByDynasty()
            1 -> Repo.allThemes()
            2 -> Repo.allForms()
            else -> Repo.textbookValues
                .map { it to Repo.poems.count { p -> p.textbook == it } }
                .filter { it.second > 0 }
        }
    }

    val filtered = remember(dim, value, Repo.poemVersion) {
        Repo.poems.filter { p ->
            value == null || when (dim) {
                0 -> p.dynasty == value
                1 -> p.themes.contains(value)
                2 -> p.form == value
                else -> p.textbook == value
            }
        }
    }

    Column(Modifier.fillMaxSize().background(Paper)) {
        TabRow(
            selectedTabIndex = dim,
            containerColor = Paper
        ) {
            tabs.forEachIndexed { i, t ->
                Tab(
                    selected = dim == i,
                    onClick = {
                        dim = i
                        value = null
                    },
                    text = { Text(t, style = MaterialTheme.typography.titleSmall) },
                    selectedContentColor = Cinnabar,
                    unselectedContentColor = InkSoft
                )
            }
        }
        LazyRow(
            Modifier.padding(vertical = 10.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            item {
                TagChip(
                    "全部 ${Repo.poems.size}",
                    selected = value == null,
                    onClick = { value = null }
                )
            }
            items(values) { v ->
                TagChip(
                    "${v.first} ${v.second}",
                    selected = value == v.first,
                    onClick = { value = if (value == v.first) null else v.first }
                )
            }
        }
        Box(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Line)
        )
        Text(
            "共 ${filtered.size} 首",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        LazyColumn {
            items(filtered, key = { it.id }) { p ->
                PoemCard(p) { onOpenPoem(p.id) }
            }
            item { Spacer(Modifier.height(40.dp)) }
        }
    }
}
