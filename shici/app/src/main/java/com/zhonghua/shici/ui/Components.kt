package com.zhonghua.shici.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zhonghua.shici.data.Poem
import com.zhonghua.shici.ui.theme.Cinnabar
import com.zhonghua.shici.ui.theme.CinnabarMist
import com.zhonghua.shici.ui.theme.Dai
import com.zhonghua.shici.ui.theme.DaiMist
import com.zhonghua.shici.ui.theme.Gold
import com.zhonghua.shici.ui.theme.GoldMist
import com.zhonghua.shici.ui.theme.Ink
import com.zhonghua.shici.ui.theme.InkSoft
import com.zhonghua.shici.ui.theme.Line
import com.zhonghua.shici.ui.theme.PaperCard
import com.zhonghua.shici.ui.theme.White

/** 朱砂印章：以姓氏入印 */
@Composable
fun SealBadge(name: String, size: Dp = 30.dp, fontSize: Int = 14) {
    Box(
        modifier = Modifier
            .size(size)
            .background(Cinnabar, RoundedCornerShape(4.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name.take(1),
            color = White,
            fontSize = fontSize.sp,
            lineHeight = fontSize.sp, // 行高锁定为字号，防止系统字体把字顶出红框
            fontWeight = FontWeight.Bold,
            fontFamily = MaterialTheme.typography.titleMedium.fontFamily,
            textAlign = TextAlign.Center,
            style = LocalTextStyle.current.copy(
                platformStyle = androidx.compose.ui.text.PlatformTextStyle(includeFontPadding = false)
            ),
            maxLines = 1,
            modifier = Modifier.padding(0.dp)
        )
    }
}

/** 分区标题：朱砂竖条 + 宋体标题 */
@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier, trailing: String = "") {
    Row(modifier = modifier.padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .width(4.dp)
                .height(18.dp)
                .background(Cinnabar, RoundedCornerShape(2.dp))
        )
        Spacer(Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.titleMedium, color = Ink)
        Spacer(Modifier.weight(1f))
        if (trailing.isNotBlank()) {
            Text(trailing, style = MaterialTheme.typography.labelMedium, color = InkSoft)
        }
    }
}

/** 标签小片 */
@Composable
fun TagChip(
    text: String,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val container = when {
        selected -> Cinnabar
        text.contains("必背") -> GoldMist
        else -> PaperCard
    }
    val content = when {
        selected -> White
        text.contains("必背") -> Gold
        else -> Dai
    }
    Surface(
        onClick = onClick ?: {},
        enabled = onClick != null,
        shape = RoundedCornerShape(50),
        color = container,
        border = if (selected) null else BorderStroke(1.dp, Line),
        modifier = Modifier.padding(end = 8.dp)
    ) {
        Text(
            text,
            color = content,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

/** 诗词卡片 */
@Composable
fun PoemCard(poem: Poem, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = PaperCard,
        border = BorderStroke(1.dp, Line),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(poem.title, style = MaterialTheme.typography.titleMedium, color = Ink)
                    Spacer(Modifier.height(2.dp))
                    Text(poem.meta, style = MaterialTheme.typography.bodySmall)
                }
                SealBadge(poem.author)
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = poem.bodyLines.filter { it.isNotBlank() }.take(2).joinToString("　"),
                style = MaterialTheme.typography.bodySmall,
                color = InkSoft,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(10.dp))
            Row {
                poem.themes.take(3).forEach {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = DaiMist,
                        modifier = Modifier.padding(end = 6.dp)
                    ) {
                        Text(
                            it,
                            color = Dai,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
                poem.textbook?.let {
                    Surface(shape = RoundedCornerShape(50), color = GoldMist, modifier = Modifier.padding(end = 6.dp)) {
                        Text(
                            it,
                            color = Gold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }
    }
}

/** 空状态 */
@Composable
fun EmptyView(title: String, hint: String = "") {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("卷", color = CinnabarMist, fontSize = 64.sp, fontFamily = MaterialTheme.typography.titleMedium.fontFamily, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, color = Ink)
        if (hint.isNotBlank()) {
            Spacer(Modifier.height(4.dp))
            Text(hint, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
        }
    }
}

/** 竖排装饰文字 */
@Composable
fun VerticalChars(text: String, color: Color = Gold, fontSize: Int = 13) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        text.forEach {
            Text(it.toString(), color = color, fontSize = fontSize.sp, fontFamily = MaterialTheme.typography.titleMedium.fontFamily)
        }
    }
}
