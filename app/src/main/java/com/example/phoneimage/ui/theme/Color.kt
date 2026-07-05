package com.example.phoneimage.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * 统一设计令牌（Design Tokens）——「浅色编辑风 · 简约高级」。
 *
 * 参考 mintpop 风格：暖白纸张底 + 大留白 + 柔和实色卡片 + 点阵纹理 + 翠绿点缀。
 * 设计原则：
 *  1. 大面积交给温暖的中性纸色，安静、透气、有质感。
 *  2. 品牌翠绿只用于点睛（主按钮、激活态、在线点、进度）。
 *  3. 收藏用一枚琥珀金，是唯一暖色语义。
 *
 * 说明：Violet / Cyan / Indigo 等历史命名保留以兼容引用，实际含义见右侧注释。
 */
object StudioColors {
    // —— 中性纸底：四层递进（由浅到更浅/更实）——————————————
    val Background = Color(0xFFF4F2EC)   // 暖白纸张主背景
    val Surface = Color(0xFFFFFFFF)      // 卡片 / 弹窗 / 生图窗口
    val SurfaceHigh = Color(0xFFEDEBE3)  // 输入框 / 小控件 / 点阵底
    val SurfaceGlass = Color(0xFFE4E1D6) // 激活 / 浮起元素

    val Border = Color(0xFFE5E2D8)       // 常规描边（很淡）
    val BorderStrong = Color(0xFFD3CFC2) // 强调描边 / 禁用态

    // —— 文本：三级对比 ————————————————————————————————
    val TextPrimary = Color(0xFF1A1B1D)   // 主文本，近黑
    val TextSecondary = Color(0xFF5C5E63) // 次级文本 / 图标
    val TextMuted = Color(0xFF97968F)     // 辅助 / 占位文本

    // —— 品牌翠绿：唯一主色，只用于点睛 ————————————————
    val Violet = Color(0xFF10A45A)  // 主色（按钮、激活、强调）
    val Indigo = Color(0xFF0A8347)  // 主色深阶（渐变收尾 / 按压）
    val Cyan = Color(0xFF6FCB97)    // 柔和浅阶
    val Blue = Color(0xFF10A45A)    // 收敛为主色
    val Pink = Color(0xFF6FCB97)    // 柔和浅阶别名

    // —— 语义色 ————————————————————————————————————————
    val Success = Color(0xFF10A45A) // 在线 / 成功
    val Warning = Color(0xFFDE9A2B) // 警示
    val Danger = Color(0xFFE14B4B)  // 失败 / 危险
    val Star = Color(0xFFE3A11B)    // 收藏，唯一暖金

    // —— 渐变 ————————————————————————————————————————
    val BrandGradient = Brush.linearGradient(
        colors = listOf(Color(0xFF16B364), Color(0xFF0A8347))
    )

    // 生图窗口空闲/占位底：温暖纸色 + 极淡绿意
    val PlaceholderGradient = Brush.linearGradient(
        colors = listOf(Color(0xFFF1EFE8), Color(0xFFEDECE4), Color(0xFFEDF1EA))
    )

    // 顶部氛围辉光：极淡品牌绿渐隐到背景
    val GlowGradient = Brush.verticalGradient(
        colors = listOf(Color(0x1410A45A), Color(0x00F4F2EC))
    )
}

// Material3 需要的默认色（主题里用得到）
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)
