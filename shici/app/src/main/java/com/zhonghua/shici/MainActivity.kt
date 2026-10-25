package com.zhonghua.shici

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.zhonghua.shici.data.Repo
import com.zhonghua.shici.data.UserStore
import com.zhonghua.shici.ui.SealBadge
import com.zhonghua.shici.ui.screens.BrowseScreen
import com.zhonghua.shici.ui.screens.FamousScreen
import com.zhonghua.shici.ui.screens.FavoritesScreen
import com.zhonghua.shici.ui.screens.HomeScreen
import com.zhonghua.shici.ui.screens.PoemDetailScreen
import com.zhonghua.shici.ui.screens.PoetDetailScreen
import com.zhonghua.shici.ui.screens.PoetsScreen
import com.zhonghua.shici.ui.screens.QuoteDetailScreen
import com.zhonghua.shici.ui.screens.SearchScreen
import com.zhonghua.shici.ui.screens.UserAddScreen
import com.zhonghua.shici.ui.theme.Cinnabar
import com.zhonghua.shici.ui.theme.CinnabarMist
import com.zhonghua.shici.ui.theme.Ink
import com.zhonghua.shici.ui.theme.InkSoft
import com.zhonghua.shici.ui.theme.Paper
import com.zhonghua.shici.ui.theme.ShiciTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed class Dest {
    data object Home : Dest()
    data class Browse(val dim: Int = 0, val value: String? = null) : Dest()
    data object Poets : Dest()
    data class PoetDetail(val name: String) : Dest()
    data class Poem(val id: String) : Dest()
    data object Search : Dest()
    data object Favs : Dest()
    data object Famous : Dest()
    data object UserAdd : Dest()
    data class QuoteDetail(val id: String) : Dest()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ShiciTheme {
                ShiciApp()
            }
        }
    }
}

private fun isTopLevel(d: Dest): Boolean =
    d is Dest.Home || d is Dest.Browse || d is Dest.Poets || d is Dest.Famous || d is Dest.Favs

@Composable
fun ShiciApp() {
    val ctx = LocalContext.current
    var loaded by remember { mutableStateOf(Repo.ready) }
    LaunchedEffect(Unit) {
        if (!loaded) {
            withContext(Dispatchers.IO) { Repo.init(ctx.applicationContext) }
            loaded = true
        }
        // 全集语料在后台静默并入，界面通过响应式列表自动刷新
        withContext(Dispatchers.IO) { Repo.initBulk(ctx.applicationContext) }
        // 用户自添加内容并入（可搜索/可收藏/进每日佳句池）
        withContext(Dispatchers.IO) {
            UserStore.init(ctx.applicationContext)
            UserStore.syncIntoRepo()
        }
    }
    if (!loaded) {
        LoadingScreen()
        return
    }

    val stack = remember { mutableStateListOf<Dest>(Dest.Home) }
    val current = stack.last()

    fun open(d: Dest, reset: Boolean = false) {
        if (reset) {
            stack.clear()
            stack.add(d)
        } else {
            stack.add(d)
        }
    }

    BackHandler(enabled = stack.size > 1) { stack.removeAt(stack.lastIndex) }

    Scaffold(
        containerColor = Paper,
        bottomBar = {
            if (isTopLevel(current)) {
                ShiciBottomBar(current) { d -> open(d, reset = true) }
            }
        }
    ) { padding ->
        AnimatedContent(
            targetState = current,
            transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(160)) },
            label = "nav",
            modifier = Modifier.padding(padding)
        ) { dest ->
            when (dest) {
                is Dest.Home -> HomeScreen(
                    onOpenPoem = { open(Dest.Poem(it)) },
                    onSearch = { open(Dest.Search) },
                    onBrowse = { dim, v -> open(Dest.Browse(dim, v)) },
                    onPoets = { open(Dest.Poets) },
                    onFamous = { open(Dest.Famous) },
                    onFavs = { open(Dest.Favs) },
                    onUserAdd = { open(Dest.UserAdd) }
                )
                is Dest.Browse -> BrowseScreen(
                    initialDim = dest.dim,
                    initialValue = dest.value,
                    onOpenPoem = { open(Dest.Poem(it)) }
                )
                is Dest.Poets -> PoetsScreen(
                    onOpenPoet = { open(Dest.PoetDetail(it)) }
                )
                is Dest.PoetDetail -> PoetDetailScreen(
                    name = dest.name,
                    onOpenPoem = { open(Dest.Poem(it)) },
                    onBack = { if (stack.size > 1) stack.removeAt(stack.lastIndex) }
                )
                is Dest.Poem -> PoemDetailScreen(
                    id = dest.id,
                    onOpenBrowse = { dim, v -> open(Dest.Browse(dim, v)) },
                    onOpenPoet = { open(Dest.PoetDetail(it)) },
                    onBack = { if (stack.size > 1) stack.removeAt(stack.lastIndex) }
                )
                is Dest.Search -> SearchScreen(
                    onOpenPoem = { open(Dest.Poem(it)) },
                    onOpenPoet = { open(Dest.PoetDetail(it)) },
                    onOpenQuote = { open(Dest.QuoteDetail(it)) },
                    onBack = { if (stack.size > 1) stack.removeAt(stack.lastIndex) }
                )
                is Dest.Favs -> FavoritesScreen(
                    onOpenPoem = { open(Dest.Poem(it)) }
                )
                is Dest.Famous -> FamousScreen(
                    onOpenQuote = { open(Dest.QuoteDetail(it)) },
                    onOpenPoem = { open(Dest.Poem(it)) }
                )
                is Dest.UserAdd -> UserAddScreen(
                    onOpenPoem = { open(Dest.Poem(it)) },
                    onBack = { if (stack.size > 1) stack.removeAt(stack.lastIndex) }
                )
                is Dest.QuoteDetail -> QuoteDetailScreen(
                    id = dest.id,
                    onOpenPoem = { open(Dest.Poem(it)) },
                    onBack = { if (stack.size > 1) stack.removeAt(stack.lastIndex) }
                )
            }
        }
    }
}

private data class TabItem(val char: String, val label: String, val dest: Dest)

@Composable
private fun ShiciBottomBar(current: Dest, onSelect: (Dest) -> Unit) {
    val items = listOf(
        TabItem("詩", "首页", Dest.Home),
        TabItem("類", "分类", Dest.Browse()),
        TabItem("家", "诗人", Dest.Poets),
        TabItem("句", "名句", Dest.Famous),
        TabItem("藏", "收藏", Dest.Favs)
    )
    NavigationBar(containerColor = Paper, tonalElevation = 0.dp) {
        items.forEach { item ->
            val selected = sameTab(current, item.dest)
            NavigationBarItem(
                selected = selected,
                onClick = { onSelect(item.dest) },
                icon = {
                    Text(
                        item.char,
                        fontFamily = MaterialTheme.typography.titleMedium.fontFamily,
                        color = if (selected) Cinnabar else InkSoft
                    )
                },
                label = { Text(item.label, style = MaterialTheme.typography.labelMedium, color = if (selected) Cinnabar else InkSoft, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = CinnabarMist,
                    selectedIconColor = Cinnabar,
                    selectedTextColor = Cinnabar,
                    unselectedIconColor = InkSoft,
                    unselectedTextColor = InkSoft
                )
            )
        }
    }
}

private fun sameTab(current: Dest, tab: Dest): Boolean = when (tab) {
    is Dest.Home -> current is Dest.Home
    is Dest.Browse -> current is Dest.Browse
    is Dest.Poets -> current is Dest.Poets
    is Dest.Famous -> current is Dest.Famous
    is Dest.Favs -> current is Dest.Favs
    else -> false
}

@Composable
fun LoadingScreen() {
    Box(Modifier.fillMaxSize().background(Paper), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            SealBadge("诗", size = 64.dp, fontSize = 28)
            Spacer(Modifier.height(14.dp))
            Text("诗词典藏", style = MaterialTheme.typography.headlineMedium, color = Ink)
            Spacer(Modifier.height(4.dp))
            Text("正在展卷……", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun ScreenTopBar(title: String, onBack: () -> Unit, actions: @Composable () -> Unit = {}) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回", tint = Ink)
        }
        Text(
            title,
            style = MaterialTheme.typography.titleLarge,
            color = Ink,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        actions()
    }
}
