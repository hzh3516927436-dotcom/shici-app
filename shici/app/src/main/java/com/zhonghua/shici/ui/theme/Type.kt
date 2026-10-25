package com.zhonghua.shici.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val Serif = FontFamily.Serif

val ShiciTypography = Typography(
    displayLarge = TextStyle(fontFamily = Serif, fontWeight = FontWeight.Bold, fontSize = 38.sp, lineHeight = 50.sp, color = Ink),
    displaySmall = TextStyle(fontFamily = Serif, fontWeight = FontWeight.Bold, fontSize = 26.sp, lineHeight = 36.sp, color = Ink),
    headlineMedium = TextStyle(fontFamily = Serif, fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 32.sp, color = Ink),
    headlineSmall = TextStyle(fontFamily = Serif, fontWeight = FontWeight.SemiBold, fontSize = 19.sp, lineHeight = 28.sp, color = Ink),
    titleLarge = TextStyle(fontFamily = Serif, fontWeight = FontWeight.Bold, fontSize = 20.sp, lineHeight = 30.sp, color = Ink),
    titleMedium = TextStyle(fontFamily = Serif, fontWeight = FontWeight.SemiBold, fontSize = 17.sp, lineHeight = 26.sp, color = Ink),
    titleSmall = TextStyle(fontFamily = Serif, fontWeight = FontWeight.Medium, fontSize = 15.sp, lineHeight = 24.sp, color = Ink),
    bodyLarge = TextStyle(fontFamily = Serif, fontWeight = FontWeight.Normal, fontSize = 18.sp, lineHeight = 38.sp, color = Ink),
    bodyMedium = TextStyle(fontFamily = Serif, fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 28.sp, color = Ink),
    bodySmall = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 18.sp, color = InkSoft),
    labelLarge = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp, color = Ink),
    labelMedium = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 18.sp, color = InkSoft),
    labelSmall = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 16.sp, color = InkSoft)
)
