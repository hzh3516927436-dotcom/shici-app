package com.zhonghua.shici.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.zhonghua.shici.ScreenTopBar
import com.zhonghua.shici.data.Repo
import com.zhonghua.shici.data.UserStore
import com.zhonghua.shici.ui.SectionTitle
import com.zhonghua.shici.ui.theme.Cinnabar
import com.zhonghua.shici.ui.theme.Dai
import com.zhonghua.shici.ui.theme.Ink
import com.zhonghua.shici.ui.theme.Line
import com.zhonghua.shici.ui.theme.Paper
import com.zhonghua.shici.ui.theme.PaperCard

/** 我的添加：表单 + 管理列表 */
@Composable
fun UserAddScreen(onOpenPoem: (String) -> Unit, onBack: () -> Unit) {
    val ctx = LocalContext.current
    var mode by remember { mutableStateOf(0) } // 0=诗词/文章 1=好句
    var title by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var dynasty by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var line by remember { mutableStateOf("") }
    var ap by remember { mutableStateOf("") }
    var savedTick by remember { mutableStateOf(0) }
    var toast by remember { mutableStateOf("") }

    Column(
        Modifier
            .fillMaxSize()
            .background(Paper)
            .imePadding()
    ) {
        ScreenTopBar("我的添加", onBack)
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            // 类型切换
            Row(Modifier.padding(vertical = 8.dp)) {
                TabChip("诗词 / 文章", mode == 0) { mode = 0; line = ""; ap = "" }
                Spacer(Modifier.width(8.dp))
                TabChip("好句", mode == 1) { mode = 1; title = ""; author = ""; dynasty = ""; body = ""; note = "" }
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = PaperCard,
                border = BorderStroke(1.dp, Line),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(14.dp)) {
                    if (mode == 0) {
                        OutlinedTextField(value = title, onValueChange = { title = it },
                            label = { Text("标题 *") }, singleLine = true,
                            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp),
                            colors = tfColors())
                        Spacer(Modifier.height(8.dp))
                        Row {
                            OutlinedTextField(value = author, onValueChange = { author = it },
                                label = { Text("作者") }, singleLine = true,
                                modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp),
                                colors = tfColors())
                            Spacer(Modifier.width(8.dp))
                            OutlinedTextField(value = dynasty, onValueChange = { dynasty = it },
                                label = { Text("朝代/类别") }, singleLine = true,
                                modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp),
                                colors = tfColors())
                        }
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(value = body, onValueChange = { body = it },
                            label = { Text("正文 *（每行一句）") },
                            minLines = 4, maxLines = 12,
                            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp),
                            colors = tfColors())
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(value = note, onValueChange = { note = it },
                            label = { Text("笔记 / 背景（可选）") },
                            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp),
                            colors = tfColors())
                    } else {
                        OutlinedTextField(value = line, onValueChange = { line = it },
                            label = { Text("好句 *") },
                            minLines = 2, maxLines = 4,
                            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp),
                            colors = tfColors())
                        Spacer(Modifier.height(8.dp))
                        Row {
                            OutlinedTextField(value = title, onValueChange = { title = it },
                                label = { Text("出处（书名/篇名）") }, singleLine = true,
                                modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp),
                                colors = tfColors())
                            Spacer(Modifier.width(8.dp))
                            OutlinedTextField(value = author, onValueChange = { author = it },
                                label = { Text("作者") }, singleLine = true,
                                modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp),
                                colors = tfColors())
                        }
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(value = ap, onValueChange = { ap = it },
                            label = { Text("赏析 / 感想（可选）") },
                            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp),
                            colors = tfColors())
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (toast.isNotBlank()) {
                            Text(toast, color = Cinnabar, style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.weight(1f))
                        } else {
                            Text("添加后立即可被搜索、收藏",
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.weight(1f))
                        }
                        OutlinedButton(onClick = {
                            if (mode == 0) {
                                if (title.isBlank() || body.isBlank()) {
                                    toast = "请填写标题与正文"; return@OutlinedButton
                                }
                                UserStore.addPoem(ctx, title, author, dynasty, "", body, note)
                                title = ""; author = ""; dynasty = ""; body = ""; note = ""
                            } else {
                                if (line.isBlank()) {
                                    toast = "请填写好句内容"; return@OutlinedButton
                                }
                                UserStore.addQuote(ctx, line, title, author, ap)
                                line = ""; title = ""; author = ""; ap = ""
                            }
                            UserStore.syncIntoRepo()
                            savedTick++
                            toast = "已保存 ✓"
                        }) {
                            Icon(Icons.Filled.Done, contentDescription = null, tint = Cinnabar)
                            Spacer(Modifier.width(4.dp))
                            Text("保存", color = Cinnabar)
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))

            // 管理列表
            SectionTitle("我添加的诗词文章 (${UserStore.poems.size})")
            if (UserStore.poems.isEmpty() && savedTick >= 0) {
                Text("暂无", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(horizontal = 16.dp))
            }
            UserStore.poems.forEach { p ->
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = PaperCard,
                    border = BorderStroke(1.dp, Line),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Row(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(p.title, style = MaterialTheme.typography.titleSmall, color = Ink)
                            Text(p.meta, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                        }
                        TextButton(onClick = { onOpenPoem(p.id) }) { Text("查看", color = Dai) }
                        IconButton(onClick = {
                            UserStore.removePoem(ctx, p.id)
                            Repo.removeById(p.id)
                            savedTick++
                        }) {
                            Icon(Icons.Filled.Delete, contentDescription = "删除", tint = Cinnabar)
                        }
                    }
                }
            }

            SectionTitle("我添加的好句 (${UserStore.quotes.size})")
            UserStore.quotes.forEach { q ->
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = PaperCard,
                    border = BorderStroke(1.dp, Line),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Row(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("「${q.line}」", style = MaterialTheme.typography.bodyMedium, color = Ink, maxLines = 2)
                            Text("——《${q.title}》 ${q.author}", style = MaterialTheme.typography.bodySmall, maxLines = 1)
                        }
                        IconButton(onClick = {
                            UserStore.removeQuote(ctx, q.id)
                            Repo.removeAppreciation(q.id)
                            savedTick++
                        }) {
                            Icon(Icons.Filled.Delete, contentDescription = "删除", tint = Cinnabar)
                        }
                    }
                }
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun TabChip(text: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = if (selected) Cinnabar else PaperCard,
        border = if (selected) null else BorderStroke(1.dp, Line)
    ) {
        Text(
            text,
            color = if (selected) Paper else Ink,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun tfColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Cinnabar,
    unfocusedBorderColor = Line,
    focusedContainerColor = Paper,
    unfocusedContainerColor = Paper,
    cursorColor = Cinnabar
)
