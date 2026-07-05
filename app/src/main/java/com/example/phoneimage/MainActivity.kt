@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.ui.ExperimentalComposeUiApi::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
)

package com.example.phoneimage

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.speech.RecognizerIntent
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items as lazyRowItems
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Hub
import androidx.compose.material.icons.outlined.ImageSearch
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.SubcomposeAsyncImage
import com.example.phoneimage.ui.theme.PhoneImageTheme
import com.example.phoneimage.ui.theme.StudioColors
import java.io.File
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

private const val DEFAULT_BASE_URL = "https://image.yydsapi.uno"
private const val PREFS = "phoneimage_prefs"
private const val HISTORY_DIR = "history"
private const val HISTORY_INDEX = "history.json"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 内容延伸到系统栏后面，实现沉浸式 edge-to-edge
        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            PhoneImageTheme {
                ImageStudioApp()
            }
        }
    }
}

private data class ApiChannel(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "默认渠道",
    val baseUrl: String = DEFAULT_BASE_URL,
    val apiKey: String = "",
    val models: List<String> = listOf("gpt-image-2"),
)

private data class StudioSettings(
    val channels: List<ApiChannel> = listOf(ApiChannel()),
    val activeChannelId: String = "",
    val defaultImageCount: Int = 1,
    val defaultSize: String = "auto",
    val defaultQuality: String = "auto",
    val defaultFormat: String = "jpeg",
    val defaultVoice: String = "Alloy",
    val defaultAudioFormat: String = "MP3",
    val defaultAudioSpeed: String = "1",
    val defaultAudioPrompt: String = "",
    val systemPrompt: String = "",
)

private data class GenerationParams(
    val model: String = "",
    val size: String = "auto",
    val quality: String = "auto",
    val format: String = "jpeg",
    val count: Int = 1,
)

internal enum class GenerationMode(val label: String) {
    TextToImage("文生图"),
    ImageToImage("图生图"),
}

internal enum class TaskStatus(val label: String) {
    Preparing("准备中"),
    Uploading("提交中"),
    Generating("生成中"),
    Saving("保存中"),
    Completed("已完成"),
    Failed("失败"),
}

internal data class GenerationTask(
    val id: Long,
    val prompt: String,
    val mode: GenerationMode,
    val model: String,
    val startedAt: String,
    val status: TaskStatus,
    val message: String,
    val progress: Float,
)

private data class PromptTemplate(
    val title: String,
    val category: String,
    val prompt: String,
)

private data class ReferenceImage(
    val uri: Uri,
    val name: String = "参考图",
)

internal data class ImageResult(
    val id: Long,
    val prompt: String,
    val model: String,
    val size: String,
    val format: String,
    val createdAt: String,
    val imageUrl: String? = null,
    val base64: String? = null,
    val mimeType: String = "image/jpeg",
    val starred: Boolean = false,
    val localPath: String? = null,
)

private enum class StudioTab(val label: String, val icon: ImageVector) {
    Workbench("工作台", Icons.Outlined.AutoAwesome),
    Gallery("画廊", Icons.Outlined.GridView),
}

private val promptTemplates = listOf(
    PromptTemplate(
        title = "高级人像",
        category = "人像",
        prompt = "一张高级质感的人像摄影，柔和自然光，干净背景，面部细节清晰，电影级色彩，真实皮肤质感，85mm 镜头，浅景深",
    ),
    PromptTemplate(
        title = "电商主图",
        category = "商品",
        prompt = "高端电商产品主图，主体居中，干净纯色背景，柔和阴影，商业摄影布光，细节锐利，质感高级，适合手机端展示",
    ),
    PromptTemplate(
        title = "国风插画",
        category = "插画",
        prompt = "精致国风插画，东方美学构图，层次丰富，细腻笔触，柔和色彩，留白克制，画面高级，适合作为手机壁纸",
    ),
    PromptTemplate(
        title = "电影场景",
        category = "场景",
        prompt = "电影感场景概念图，戏剧化光影，真实环境细节，宽银幕构图，氛围强烈，色彩克制，超清画质",
    ),
    PromptTemplate(
        title = "社媒海报",
        category = "设计",
        prompt = "现代社交媒体视觉海报，强主体构图，高级排版感，留出文字区域，配色克制，视觉冲击力强，适合手机屏幕",
    ),
    PromptTemplate(
        title = "图生图精修",
        category = "图生图",
        prompt = "基于参考图进行高级精修，保持主体特征和构图，优化光影、质感、细节与背景，使画面更干净、更真实、更有商业摄影质感",
    ),
    PromptTemplate(
        title = "角色设定",
        category = "角色",
        prompt = "完整角色设定图，正面半身像，独特服装设计，清晰轮廓，精致面部，统一色彩方案，概念设计风格，高完成度",
    ),
    PromptTemplate(
        title = "建筑空间",
        category = "空间",
        prompt = "现代高级室内空间摄影，自然光进入房间，材质真实，线条干净，空间通透，杂物极少，建筑杂志风格",
    ),
)

/* ---------------------------------------------------------------------------
 *  根据尺寸字符串（如 1024x1536 / 1536x1024）推导真实宽高比，
 *  保证竖图、横图、方图都按正确比例显示，不再被拉伸或裁切变形。
 * ------------------------------------------------------------------------- */
private fun aspectRatioFor(size: String): Float {
    val parts = size.split("x", "×", "*")
    val w = parts.getOrNull(0)?.trim()?.toFloatOrNull()
    val h = parts.getOrNull(1)?.trim()?.toFloatOrNull()
    return if (w != null && h != null && w > 0f && h > 0f) w / h else 1f
}

/* ---------------------------------------------------------------------------
 *  持久化：把 baseUrl / apiKey 存进 SharedPreferences，重启自动读回
 * ------------------------------------------------------------------------- */
private fun loadSettings(context: Context): StudioSettings {
    val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    val channels = runCatching {
        val raw = prefs.getString("channels_json", null) ?: return@runCatching emptyList()
        val array = JSONArray(raw)
        buildList {
            for (index in 0 until array.length()) {
                val item = array.optJSONObject(index) ?: continue
                val models = item.optJSONArray("models")
                add(
                    ApiChannel(
                        id = item.optString("id").ifBlank { UUID.randomUUID().toString() },
                        name = item.optString("name", "默认渠道"),
                        baseUrl = item.optString("baseUrl", DEFAULT_BASE_URL).trimEnd('/'),
                        apiKey = item.optString("apiKey"),
                        models = buildList {
                            if (models != null) {
                                for (modelIndex in 0 until models.length()) {
                                    val model = models.optString(modelIndex).trim()
                                    if (model.isNotBlank()) add(model)
                                }
                            }
                        }.ifEmpty { listOf("gpt-image-2") },
                    )
                )
            }
        }
    }.getOrDefault(emptyList())

    val migratedChannels = channels.ifEmpty {
        listOf(
            ApiChannel(
                name = "默认渠道",
                baseUrl = prefs.getString("base_url", DEFAULT_BASE_URL)?.trimEnd('/') ?: DEFAULT_BASE_URL,
                apiKey = prefs.getString("api_key", "") ?: "",
            )
        )
    }
    return StudioSettings(
        channels = migratedChannels,
        activeChannelId = prefs.getString("active_channel_id", "")?.takeIf { id -> migratedChannels.any { it.id == id } }
            ?: migratedChannels.first().id,
        defaultImageCount = prefs.getInt("default_image_count", 1).coerceIn(1, 4),
        defaultSize = prefs.getString("default_size", "auto") ?: "auto",
        defaultQuality = prefs.getString("default_quality", "auto") ?: "auto",
        defaultFormat = prefs.getString("default_format", "jpeg") ?: "jpeg",
        defaultVoice = prefs.getString("default_voice", "Alloy") ?: "Alloy",
        defaultAudioFormat = prefs.getString("default_audio_format", "MP3") ?: "MP3",
        defaultAudioSpeed = prefs.getString("default_audio_speed", "1") ?: "1",
        defaultAudioPrompt = prefs.getString("default_audio_prompt", "") ?: "",
        systemPrompt = prefs.getString("system_prompt", "") ?: "",
    )
}

private fun saveSettings(context: Context, settings: StudioSettings) {
    val channels = JSONArray().apply {
        settings.channels.forEach { channel ->
            put(
                JSONObject()
                    .put("id", channel.id)
                    .put("name", channel.name)
                    .put("baseUrl", channel.baseUrl.trimEnd('/'))
                    .put("apiKey", channel.apiKey)
                    .put("models", JSONArray(channel.models))
            )
        }
    }
    val active = settings.channels.firstOrNull { it.id == settings.activeChannelId } ?: settings.channels.firstOrNull()
    context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
        .putString("channels_json", channels.toString())
        .putString("active_channel_id", active?.id.orEmpty())
        .putString("base_url", active?.baseUrl ?: DEFAULT_BASE_URL)
        .putString("api_key", active?.apiKey.orEmpty())
        .putInt("default_image_count", settings.defaultImageCount.coerceIn(1, 4))
        .putString("default_size", settings.defaultSize)
        .putString("default_quality", settings.defaultQuality)
        .putString("default_format", settings.defaultFormat)
        .putString("default_voice", settings.defaultVoice)
        .putString("default_audio_format", settings.defaultAudioFormat)
        .putString("default_audio_speed", settings.defaultAudioSpeed)
        .putString("default_audio_prompt", settings.defaultAudioPrompt)
        .putString("system_prompt", settings.systemPrompt)
        .apply()
}

private fun StudioSettings.activeChannel(): ApiChannel {
    return channels.firstOrNull { it.id == activeChannelId } ?: channels.firstOrNull() ?: ApiChannel()
}

private fun historyDir(context: Context): File {
    return File(context.filesDir, HISTORY_DIR).apply { mkdirs() }
}

private fun historyIndexFile(context: Context): File {
    return File(historyDir(context), HISTORY_INDEX)
}

private suspend fun loadHistory(context: Context): List<ImageResult> = withContext(Dispatchers.IO) {
    runCatching {
        val index = historyIndexFile(context)
        if (!index.exists()) return@withContext emptyList()
        val array = JSONArray(index.readText())
        buildList {
            for (i in 0 until array.length()) {
                val json = array.optJSONObject(i) ?: continue
                val localPath = json.optString("localPath").takeIf { it.isNotBlank() }
                if (localPath != null && !File(localPath).exists()) continue
                add(
                    ImageResult(
                        id = json.optLong("id"),
                        prompt = json.optString("prompt"),
                        model = json.optString("model"),
                        size = json.optString("size", "auto"),
                        format = json.optString("format", "jpeg"),
                        createdAt = json.optString("createdAt"),
                        imageUrl = json.optString("imageUrl").takeIf { it.isNotBlank() },
                        mimeType = json.optString("mimeType", "image/jpeg"),
                        starred = json.optBoolean("starred", false),
                        localPath = localPath,
                    )
                )
            }
        }
    }.getOrDefault(emptyList())
}

private suspend fun saveHistory(context: Context, items: List<ImageResult>) = withContext(Dispatchers.IO) {
    val array = JSONArray()
    items.take(120).forEach { item ->
        array.put(
            JSONObject()
                .put("id", item.id)
                .put("prompt", item.prompt)
                .put("model", item.model)
                .put("size", item.size)
                .put("format", item.format)
                .put("createdAt", item.createdAt)
                .put("imageUrl", item.imageUrl.orEmpty())
                .put("mimeType", item.mimeType)
                .put("starred", item.starred)
                .put("localPath", item.localPath.orEmpty())
        )
    }
    historyIndexFile(context).writeText(array.toString())
}

private suspend fun persistGeneratedImages(context: Context, items: List<ImageResult>): List<ImageResult> = withContext(Dispatchers.IO) {
    items.map { item ->
        runCatching {
            val bytes = imageResultBytes(context, item).getOrThrow()
            val ext = item.mimeType.substringAfter('/', item.format).ifBlank { "jpg" }
            val file = File(historyDir(context), "${item.id}.$ext")
            file.writeBytes(bytes)
            item.copy(localPath = file.absolutePath, base64 = null)
        }.getOrElse { item }
    }
}

private suspend fun saveImageToGallery(context: Context, item: ImageResult): Result<Uri> = withContext(Dispatchers.IO) {
    runCatching {
        val bytes = imageResultBytes(context, item).getOrThrow()
        val ext = item.mimeType.substringAfter('/', "jpeg")
        val name = "PhoneImage_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.$ext"
        val resolver = context.contentResolver
        val values = android.content.ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, name)
            put(MediaStore.Images.Media.MIME_TYPE, item.mimeType)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/PhoneImage")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            ?: error("无法创建相册文件")
        resolver.openOutputStream(uri)?.use { it.write(bytes) } ?: error("无法写入图片")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.clear()
            values.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
        }
        uri
    }
}

private suspend fun imageResultToCacheUri(context: Context, item: ImageResult): Result<Uri> = withContext(Dispatchers.IO) {
    runCatching {
        val bytes = imageResultBytes(context, item).getOrThrow()
        val file = File(context.cacheDir, "reference_${UUID.randomUUID()}.jpg")
        file.writeBytes(bytes)
        Uri.fromFile(file)
    }
}

private suspend fun shareImage(context: Context, item: ImageResult): Result<Unit> = withContext(Dispatchers.IO) {
    runCatching {
        val bytes = imageResultBytes(context, item).getOrThrow()
        val ext = item.mimeType.substringAfter('/', item.format).ifBlank { "jpg" }
        val dir = File(context.cacheDir, "share").apply { mkdirs() }
        val file = File(dir, "PhoneImage_${item.id}.$ext")
        file.writeBytes(bytes)
        val uri = androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file,
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = item.mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, item.prompt)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "分享图片").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}

private suspend fun imageResultBytes(context: Context, item: ImageResult): Result<ByteArray> = withContext(Dispatchers.IO) {
    runCatching {
        when {
            item.localPath != null && File(item.localPath).exists() -> File(item.localPath).readBytes()
            item.base64 != null -> Base64.decode(item.base64, Base64.DEFAULT)
            item.imageUrl != null -> URL(item.imageUrl).openStream().use { it.readBytes() }
            else -> error("图片数据为空")
        }
    }
}

@Composable
private fun ImageStudioApp() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val gallery = remember { mutableStateListOf<ImageResult>() }
    var settings by remember { mutableStateOf(loadSettings(context)) }
    var prompt by remember { mutableStateOf("") }
    var params by remember {
        mutableStateOf(
            GenerationParams(
                model = settings.activeChannel().models.firstOrNull().orEmpty().ifBlank { "gpt-image-2" },
                count = settings.defaultImageCount,
                size = settings.defaultSize,
                quality = settings.defaultQuality,
                format = settings.defaultFormat,
            )
        )
    }
    var models by remember { mutableStateOf(settings.activeChannel().models.ifEmpty { listOf("gpt-image-2") }) }
    var query by remember { mutableStateOf("") }
    var generationMode by remember { mutableStateOf(GenerationMode.TextToImage) }
    val referenceImages = remember { mutableStateListOf<ReferenceImage>() }
    var onlyStarred by remember { mutableStateOf(false) }
    var activeTab by remember { mutableStateOf(StudioTab.Workbench) }
    var isGenerating by remember { mutableStateOf(false) }
    var isLoadingModels by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var showParams by remember { mutableStateOf(false) }
    var showModelPicker by remember { mutableStateOf(false) }
    var showPromptLibrary by remember { mutableStateOf(false) }
    var selectedImage by remember { mutableStateOf<ImageResult?>(null) }
    var currentTask by remember { mutableStateOf<GenerationTask?>(null) }
    // 中央生图窗口当前展示的图（最新生成的，或用户从历史里点选的）
    var stageImage by remember { mutableStateOf<ImageResult?>(null) }
    val keyboard = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        gallery.clear()
        gallery.addAll(loadHistory(context))
    }

    fun persistGallery() {
        scope.launch { saveHistory(context, gallery.toList()) }
    }

    fun toggleStar(item: ImageResult) {
        val index = gallery.indexOfFirst { it.id == item.id }
        if (index >= 0) {
            val updated = gallery[index].copy(starred = !gallery[index].starred)
            gallery[index] = updated
            if (selectedImage?.id == updated.id) selectedImage = updated
            persistGallery()
        }
    }

    var pendingSaveItem by remember { mutableStateOf<ImageResult?>(null) }

    fun performSaveToAlbum(item: ImageResult) {
        scope.launch {
            saveImageToGallery(context, item)
                .onSuccess { snackbarHostState.showSnackbar("已保存到相册") }
                .onFailure { snackbarHostState.showSnackbar(it.message ?: "保存失败") }
        }
    }

    val storagePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        val item = pendingSaveItem
        pendingSaveItem = null
        if (granted) {
            item?.let { performSaveToAlbum(it) }
        } else {
            scope.launch { snackbarHostState.showSnackbar("需要存储权限才能保存到相册") }
        }
    }

    fun saveToAlbum(item: ImageResult) {
        // Android 10（API 29）以下写入公共相册需要运行时申请存储权限，否则会直接抛异常
        val needsLegacyPermission = Build.VERSION.SDK_INT <= Build.VERSION_CODES.P
        if (needsLegacyPermission &&
            androidx.core.content.ContextCompat.checkSelfPermission(
                context, Manifest.permission.WRITE_EXTERNAL_STORAGE,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            pendingSaveItem = item
            storagePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } else {
            performSaveToAlbum(item)
        }
    }

    fun shareResult(item: ImageResult) {
        scope.launch {
            shareImage(context, item)
                .onFailure { snackbarHostState.showSnackbar(it.message ?: "分享失败") }
        }
    }

    fun useAsReference(item: ImageResult) {
        scope.launch {
            val result = imageResultToCacheUri(context, item)
            result
                .onSuccess { uri ->
                    if (referenceImages.size >= 6) {
                        snackbarHostState.showSnackbar("最多支持 6 张参考图")
                    } else {
                        referenceImages.add(ReferenceImage(uri = uri, name = "生成图 ${referenceImages.size + 1}"))
                        generationMode = GenerationMode.ImageToImage
                        selectedImage = null
                        snackbarHostState.showSnackbar("已加入图生图参考图")
                    }
                }
                .onFailure {
                    snackbarHostState.showSnackbar(it.message ?: "无法设为参考图")
                }
        }
    }

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { result ->
            val spoken = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()
                ?.trim()
                .orEmpty()
            if (spoken.isNotBlank()) {
                prompt = if (prompt.isBlank()) spoken else "$prompt，$spoken"
            }
        }
    )
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 6),
        onResult = { uris ->
            if (uris.isNotEmpty()) {
                referenceImages.clear()
                referenceImages.addAll(uris.mapIndexed { index, uri -> ReferenceImage(uri = uri, name = "参考图 ${index + 1}") })
                generationMode = GenerationMode.ImageToImage
            }
        }
    )

    fun refreshModels() {
        val channel = settings.activeChannel()
        if (channel.apiKey.isBlank()) {
            scope.launch { snackbarHostState.showSnackbar("先在设置里填写 API Key") }
            return
        }
        isLoadingModels = true
        scope.launch {
            val result = ImageApi.listModels(channel.baseUrl, channel.apiKey)
            isLoadingModels = false
            result
                .onSuccess { remoteModels ->
                    models = remoteModels.ifEmpty { models }
                    settings = settings.copy(
                        channels = settings.channels.map {
                            if (it.id == channel.id) it.copy(models = models) else it
                        }
                    )
                    saveSettings(context, settings)
                    if (params.model !in models) {
                        params = params.copy(model = models.firstOrNull().orEmpty())
                    }
                    snackbarHostState.showSnackbar("模型列表已刷新")
                }
                .onFailure { snackbarHostState.showSnackbar(it.message ?: "模型列表加载失败") }
        }
    }

    fun refreshAllModels() {
        isLoadingModels = true
        scope.launch {
            var nextSettings = settings
            nextSettings.channels.forEach { channel ->
                if (channel.apiKey.isNotBlank()) {
                    ImageApi.listModels(channel.baseUrl, channel.apiKey)
                        .onSuccess { remoteModels ->
                            if (remoteModels.isNotEmpty()) {
                                nextSettings = nextSettings.copy(
                                    channels = nextSettings.channels.map {
                                        if (it.id == channel.id) it.copy(models = remoteModels) else it
                                    }
                                )
                            }
                        }
                }
            }
            settings = nextSettings
            models = settings.activeChannel().models.ifEmpty { models }
            if (params.model !in models) params = params.copy(model = models.firstOrNull().orEmpty())
            saveSettings(context, settings)
            isLoadingModels = false
            snackbarHostState.showSnackbar("模型列表已更新")
        }
    }

    fun generate() {
        val cleanPrompt = prompt.trim()
        if (cleanPrompt.isBlank()) {
            scope.launch { snackbarHostState.showSnackbar("先写一点提示词") }
            return
        }
        val channel = settings.activeChannel()
        if (channel.apiKey.isBlank()) {
            showSettings = true
            scope.launch { snackbarHostState.showSnackbar("需要先填写 API Key") }
            return
        }
        if (params.model.isBlank()) {
            scope.launch { snackbarHostState.showSnackbar("请选择模型") }
            return
        }
        if (generationMode == GenerationMode.ImageToImage && referenceImages.isEmpty()) {
            scope.launch { snackbarHostState.showSnackbar("请先选择参考图") }
            return
        }

        keyboard?.hide()
        isGenerating = true
        val taskId = System.currentTimeMillis()
        currentTask = GenerationTask(
            id = taskId,
            prompt = cleanPrompt,
            mode = generationMode,
            model = params.model,
            startedAt = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
            status = TaskStatus.Preparing,
            message = "正在准备请求",
            progress = 0.12f,
        )
        scope.launch {
            currentTask = currentTask?.copy(status = TaskStatus.Uploading, message = "正在提交到中转站", progress = 0.32f)
            val result = if (generationMode == GenerationMode.ImageToImage && referenceImages.isNotEmpty()) {
                currentTask = currentTask?.copy(status = TaskStatus.Generating, message = "参考图已加入，正在生成", progress = 0.58f)
                ImageApi.edit(context, channel.baseUrl, channel.apiKey, cleanPrompt, params, referenceImages.toList())
            } else {
                currentTask = currentTask?.copy(status = TaskStatus.Generating, message = "模型正在生成图片", progress = 0.58f)
                ImageApi.generate(channel.baseUrl, channel.apiKey, cleanPrompt, params)
            }
            isGenerating = false
            result
                .onSuccess { images ->
                    currentTask = currentTask?.copy(status = TaskStatus.Saving, message = "正在写入本地历史", progress = 0.86f)
                    val createdAt = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                    val rawItems = images.mapIndexed { index, image ->
                        ImageResult(
                            id = System.currentTimeMillis() + index,
                            prompt = cleanPrompt,
                            model = params.model,
                            size = params.size,
                            format = params.format,
                            createdAt = createdAt,
                            imageUrl = image.url,
                            base64 = image.base64,
                            mimeType = image.mimeType,
                        )
                    }
                    val newItems = persistGeneratedImages(context, rawItems)
                    gallery.addAll(0, newItems)
                    saveHistory(context, gallery.toList())
                    prompt = ""
                    // 把最新一张推到中央生图窗口
                    stageImage = newItems.firstOrNull()
                    currentTask = currentTask?.copy(status = TaskStatus.Completed, message = "已生成 ${newItems.size} 张图片", progress = 1f)
                    snackbarHostState.showSnackbar("已生成 ${newItems.size} 张图片")
                }
                .onFailure {
                    val message = it.message ?: "生成失败"
                    currentTask = currentTask?.copy(status = TaskStatus.Failed, message = message, progress = 1f)
                    snackbarHostState.showSnackbar(message)
                }
        }
    }

    val filteredGallery = remember(gallery.toList(), query, onlyStarred) {
        gallery.filter { item ->
            val matchesQuery = query.isBlank() ||
                item.prompt.contains(query, ignoreCase = true) ||
                item.model.contains(query, ignoreCase = true)
            val matchesStar = !onlyStarred || item.starred
            matchesQuery && matchesStar
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = StudioColors.Background,
        bottomBar = {
            if (activeTab == StudioTab.Workbench) {
                ComposerBar(
                    prompt = prompt,
                    params = params,
                    mode = generationMode,
                    referenceImages = referenceImages.toList(),
                    isGenerating = isGenerating,
                    onOpenPromptLibrary = { showPromptLibrary = true },
                    onPromptChange = { prompt = it },
                    onModeChange = { generationMode = it },
                    onPickReference = {
                        imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    onClearReferences = { referenceImages.clear() },
                    onVoiceInput = {
                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.CHINESE.toLanguageTag())
                            putExtra(RecognizerIntent.EXTRA_PROMPT, "请描述你想生成的画面")
                        }
                        runCatching { speechLauncher.launch(intent) }
                            .onFailure {
                                scope.launch { snackbarHostState.showSnackbar("当前设备不可用语音输入") }
                            }
                    },
                    onOpenParams = { showParams = true },
                    onGenerate = ::generate,
                )
            }
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(StudioColors.Background)
        ) {
            // 顶部氛围光晕
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(StudioColors.GlowGradient)
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = padding.calculateBottomPadding())
            ) {
                StudioHeader(
                    params = params,
                    isLoadingModels = isLoadingModels,
                    onModelClick = { showModelPicker = true },
                    onRefreshModels = ::refreshModels,
                    onSettingsClick = { showSettings = true },
                )
                Spacer(Modifier.height(6.dp))
                SegmentTabs(active = activeTab, onSelect = { activeTab = it })
                Spacer(Modifier.height(4.dp))

                Crossfade(
                    targetState = activeTab,
                    animationSpec = tween(260),
                    label = "tabContent",
                    modifier = Modifier.weight(1f),
                ) { tab ->
                    when (tab) {
                        StudioTab.Workbench -> {
                            Column(modifier = Modifier.fillMaxSize()) {
                                GenerationStage(
                                    isGenerating = isGenerating,
                                    task = currentTask,
                                    image = stageImage,
                                    params = params,
                                    historyCount = gallery.size,
                                    onOpen = { stageImage?.let { selectedImage = it } },
                                    onToggleStar = { stageImage?.let { toggleStar(it) } },
                                    onSave = { stageImage?.let { saveToAlbum(it) } },
                                    onShare = { stageImage?.let { shareResult(it) } },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                        StudioTab.Gallery -> {
                            Column(modifier = Modifier.fillMaxSize()) {
                                SearchAndFilter(
                                    query = query,
                                    onQueryChange = { query = it },
                                    onlyStarred = onlyStarred,
                                    onToggleStarred = {
                                        if (!onlyStarred && gallery.none { it.starred }) {
                                            scope.launch { snackbarHostState.showSnackbar("还没有收藏作品") }
                                        } else {
                                            onlyStarred = !onlyStarred
                                        }
                                    },
                                    count = filteredGallery.size,
                                )
                                GallerySection(
                                    results = filteredGallery,
                                    onOpen = { selectedImage = it },
                                    onToggleStar = ::toggleStar,
                                    onSave = ::saveToAlbum,
                                    onShare = ::shareResult,
                                    onUseAsReference = ::useAsReference,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSettings) {
        SettingsPageV2(
            settings = settings,
            params = params,
            isLoadingModels = isLoadingModels,
            onClose = { showSettings = false },
            onSavePreferences = { nextSettings, nextParams ->
                settings = nextSettings
                params = nextParams
                saveSettings(context, nextSettings)
            },
            onSaveChannels = { next ->
                settings = next
                saveSettings(context, next)
                models = next.activeChannel().models.ifEmpty { models }
                if (params.model !in models) {
                    params = params.copy(model = models.firstOrNull().orEmpty())
                }
                refreshAllModels()
            },
        )
    }

    if (showModelPicker) {
        ModelPickerDialog(
            settings = settings,
            isLoadingModels = isLoadingModels,
            currentModel = params.model,
            onSelectModel = { channelId, model ->
                val nextSettings = settings.copy(activeChannelId = channelId)
                settings = nextSettings
                models = nextSettings.activeChannel().models.ifEmpty { models }
                saveSettings(context, nextSettings)
                params = params.copy(model = model)
                showModelPicker = false
            },
            onClose = { showModelPicker = false },
        )
    }

    if (showParams) {
        ParamsDialog(
            params = params,
            onParamsChange = { params = it },
            onClose = { showParams = false },
        )
    }

    if (showPromptLibrary) {
        PromptLibraryDialog(
            templates = promptTemplates,
            onDismiss = { showPromptLibrary = false },
            onUseTemplate = { template ->
                prompt = template.prompt
                if (template.category == "图生图") generationMode = GenerationMode.ImageToImage
                showPromptLibrary = false
            },
        )
    }

    selectedImage?.let { item ->
        ImageDetailDialog(
            item = item,
            onDismiss = { selectedImage = null },
            onToggleStar = { toggleStar(item) },
            onSave = { saveToAlbum(item) },
            onShare = { shareResult(item) },
            onUseAsReference = { useAsReference(item) },
        )
    }
}

/* ------------------------------- Header ---------------------------------- */

@Composable
private fun StudioHeader(
    params: GenerationParams,
    isLoadingModels: Boolean,
    onModelClick: () -> Unit,
    onRefreshModels: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 当前模型：左侧主角，一枚干净的胶囊
        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(18.dp))
                .background(StudioColors.Surface)
                .border(1.dp, StudioColors.Border, RoundedCornerShape(18.dp))
                .bounceClick(onClick = onModelClick)
                .padding(start = 14.dp, end = 10.dp, top = 9.dp, bottom = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val dotAlpha by rememberInfiniteTransition(label = "onlineDot").animateFloat(
                initialValue = 0.35f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1100, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "onlineDotAlpha",
            )
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(StudioColors.Success.copy(alpha = dotAlpha))
            )
            Spacer(Modifier.width(9.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "当前模型",
                    color = StudioColors.TextMuted,
                    fontSize = 9.sp,
                    maxLines = 1,
                )
                Text(
                    text = params.model.ifBlank { "未选择模型" },
                    color = StudioColors.TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.width(6.dp))
            Icon(
                Icons.Outlined.ExpandMore,
                contentDescription = null,
                tint = StudioColors.TextMuted,
                modifier = Modifier.size(18.dp),
            )
        }
        GlassIconButton(
            icon = Icons.Outlined.Refresh,
            contentDescription = "刷新模型",
            loading = isLoadingModels,
            size = 42.dp,
            onClick = onRefreshModels,
        )
        GlassIconButton(
            icon = Icons.Outlined.Settings,
            contentDescription = "设置",
            size = 42.dp,
            onClick = onSettingsClick,
        )
    }
}

@Composable
private fun GlassIconButton(
    icon: ImageVector,
    contentDescription: String,
    loading: Boolean = false,
    size: androidx.compose.ui.unit.Dp = 38.dp,
    subtle: Boolean = false,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(12.dp))
            .background(if (subtle) Color.Transparent else StudioColors.SurfaceHigh)
            .then(
                if (subtle) Modifier else Modifier.border(1.dp, StudioColors.Border, RoundedCornerShape(12.dp))
            )
            .bounceClick(enabled = !loading, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = StudioColors.Violet,
            )
        } else {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = StudioColors.TextSecondary,
                modifier = Modifier.size(19.dp)
            )
        }
    }
}

/* -------------------------------- Tabs ----------------------------------- */

/**
 * 可点击元素的统一「按压回弹」手感：按下时轻微缩小，抬起回弹。
 * 无水波纹（indication=null），配合暗色高级风更干净。
 */
@Composable
internal fun Modifier.bounceClick(
    enabled: Boolean = true,
    pressedScale: Float = 0.93f,
    onClick: () -> Unit,
): Modifier {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled) pressedScale else 1f,
        animationSpec = tween(120),
        label = "bounce",
    )
    return this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(
            interactionSource = interaction,
            indication = null,
            enabled = enabled,
            onClick = onClick,
        )
}


@Composable
private fun SegmentTabs(active: StudioTab, onSelect: (StudioTab) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(StudioColors.Surface.copy(alpha = 0.70f))
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        StudioTab.values().forEach { tab ->
            val selected = tab == active
            val bg by animateColorAsState(
                targetValue = if (selected) StudioColors.SurfaceGlass.copy(alpha = 0.72f) else Color.Transparent,
                animationSpec = tween(200),
                label = "tabBg",
            )
            val fg by animateColorAsState(
                targetValue = if (selected) StudioColors.TextPrimary else StudioColors.TextMuted,
                animationSpec = tween(200),
                label = "tabFg",
            )
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(11.dp))
                    .background(bg)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) { onSelect(tab) }
                    .padding(vertical = 7.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(tab.icon, contentDescription = null, modifier = Modifier.size(17.dp), tint = fg)
                Spacer(Modifier.width(6.dp))
                Text(tab.label, fontWeight = FontWeight.SemiBold, color = fg, fontSize = 13.sp)
            }
        }
    }
}

/* ------------------------- Search + filters ------------------------------ */

@Composable
private fun SearchAndFilter(
    query: String,
    onQueryChange: (String) -> Unit,
    onlyStarred: Boolean,
    onToggleStarred: () -> Unit,
    count: Int,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "作品",
                    fontSize = 19.sp,
                    color = StudioColors.TextPrimary,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "$count 张",
                    color = StudioColors.TextMuted,
                    fontSize = 11.sp,
                )
            }
            val starBg by animateColorAsState(
                if (onlyStarred) StudioColors.Violet.copy(alpha = 0.18f) else StudioColors.SurfaceHigh,
                label = "starBg",
            )
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(starBg)
                    .border(
                        1.dp,
                        if (onlyStarred) StudioColors.Violet else StudioColors.Border,
                        RoundedCornerShape(12.dp)
                    )
                    .clickable(onClick = onToggleStarred),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (onlyStarred) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    contentDescription = "只看收藏",
                    tint = if (onlyStarred) StudioColors.Star else StudioColors.TextSecondary,
                    modifier = Modifier.size(19.dp)
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        StudioInputField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = "搜索提示词、模型...",
            leadingIcon = Icons.Outlined.Search,
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
        )
        Spacer(Modifier.height(8.dp))
    }
}

/* ------------------------------ Gallery ---------------------------------- */

@Composable
private fun GallerySection(
    results: List<ImageResult>,
    onOpen: (ImageResult) -> Unit,
    onToggleStar: (ImageResult) -> Unit,
    onSave: (ImageResult) -> Unit,
    onShare: (ImageResult) -> Unit,
    onUseAsReference: (ImageResult) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (results.isEmpty()) {
        EmptyGallery(modifier = modifier.fillMaxWidth())
    } else {
        // 按屏幕宽度自适应列数：普通手机 2 列，大屏/折叠屏 3 列，平板 4 列
        val screenWidthDp = LocalConfiguration.current.screenWidthDp
        val columns = when {
            screenWidthDp >= 840 -> 4
            screenWidthDp >= 600 -> 3
            else -> 2
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            modifier = modifier.fillMaxWidth(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 2.dp, bottom = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(results, key = { it.id }) { item ->
                GalleryCard(
                    item = item,
                    onOpen = { onOpen(item) },
                    onToggleStar = { onToggleStar(item) },
                    onSave = { onSave(item) },
                    onShare = { onShare(item) },
                    onUseAsReference = { onUseAsReference(item) },
                )
            }
        }
    }
}

@Composable
private fun EmptyGallery(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 22.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 18.dp, horizontal = 22.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(StudioColors.SurfaceHigh),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.ImageSearch,
                    contentDescription = null,
                    modifier = Modifier.size(30.dp),
                    tint = StudioColors.TextSecondary
                )
            }
            Spacer(Modifier.height(14.dp))
            Text(
                "还没有作品",
                fontWeight = FontWeight.Bold,
                color = StudioColors.TextPrimary,
                fontSize = 17.sp,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "在下方输入提示词，生成的图片会\n出现在这里",
                color = StudioColors.TextMuted,
                fontSize = 13.sp,
                lineHeight = 20.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }
    }
}

@Composable
private fun GenerationStage(
    isGenerating: Boolean,
    task: GenerationTask?,
    image: ImageResult?,
    params: GenerationParams,
    historyCount: Int,
    onOpen: () -> Unit,
    onToggleStar: () -> Unit,
    onSave: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(StudioColors.Surface)
            .border(1.dp, StudioColors.Border, RoundedCornerShape(22.dp))
            .padding(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "PREVIEW",
                    color = StudioColors.Violet,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "生成画布",
                    color = StudioColors.TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = task?.message ?: "${params.size} · ${params.quality} · ${params.format.uppercase()} · $historyCount items",
                    color = StudioColors.TextMuted,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            StatusPill(
                text = task?.status?.label ?: if (image == null) "空闲" else "Ready",
                active = isGenerating,
            )
            Spacer(Modifier.width(8.dp))
            if (image != null) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(StudioColors.SurfaceHigh)
                        .bounceClick(onClick = onToggleStar),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = if (image.starred) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = "收藏",
                        tint = if (image.starred) StudioColors.Star else StudioColors.TextSecondary,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(18.dp))
                .background(StudioColors.PlaceholderGradient)
                .border(1.dp, StudioColors.Border, RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center,
        ) {
            DottedPattern(
                modifier = Modifier.fillMaxSize(),
                color = StudioColors.Violet.copy(alpha = 0.22f),
            )
            // 生图时的流光氛围：铺满画布的旋转极光 + 斜向扫光
            if (isGenerating) {
                FlowingLightBackground(modifier = Modifier.fillMaxSize())
            }
            when {
                image != null -> {
                    ResultImage(
                        item = image,
                        modifier = Modifier
                            .fillMaxSize()
                            .bounceClick(pressedScale = 0.99f, onClick = onOpen),
                    )
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(12.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(StudioColors.Surface.copy(alpha = 0.92f))
                            .border(1.dp, StudioColors.Border, RoundedCornerShape(16.dp))
                            .padding(6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        StageAction("保存", Icons.Outlined.Download, onSave)
                        StageAction("分享", Icons.Outlined.Share, onShare)
                        StageAction("查看", Icons.Outlined.ImageSearch, onOpen)
                    }
                }
                isGenerating -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        AuroraLoadingOrb()
                        Spacer(Modifier.height(20.dp))
                        Text(
                            text = task?.status?.label ?: "生成中",
                            color = StudioColors.TextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = task?.prompt ?: "模型正在处理你的画面",
                            color = StudioColors.TextMuted,
                            fontSize = 12.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 28.dp),
                        )
                        Spacer(Modifier.height(14.dp))
                        ShimmerProgressBar(
                            modifier = Modifier
                                .fillMaxWidth(0.5f)
                                .padding(horizontal = 8.dp),
                        )
                    }
                }
                else -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(StudioColors.Surface.copy(alpha = 0.82f))
                                .border(1.dp, StudioColors.Border, RoundedCornerShape(24.dp)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AutoAwesome,
                                contentDescription = null,
                                tint = StudioColors.Violet,
                                modifier = Modifier.size(34.dp),
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "等待生成",
                            color = StudioColors.TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "文生图、图生图和多参考图都会在这里预览",
                            color = StudioColors.TextMuted,
                            fontSize = 12.sp,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusPill(text: String, active: Boolean) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(13.dp))
            .background(if (active) StudioColors.Violet.copy(alpha = 0.14f) else StudioColors.SurfaceHigh)
            .border(
                1.dp,
                if (active) StudioColors.Violet.copy(alpha = 0.45f) else StudioColors.Border,
                RoundedCornerShape(13.dp),
            )
            .padding(horizontal = 9.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(if (active) StudioColors.Violet else StudioColors.TextMuted)
        )
        Text(text, color = StudioColors.TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
    }
}

/**
 * 生图时铺满画布的「流光」氛围背景。
 * 由两层构成：
 *  1. 缓慢旋转的极光光斑（径向渐变团），营造柔和呼吸感；
 *  2. 反复扫过的斜向高光带（sweep），带来高级的「流光」质感。
 * 全部用 Canvas 绘制，兼容所有 API，不依赖 blur。
 */
@Composable
private fun FlowingLightBackground(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "flowingLight")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(animation = tween(9000, easing = LinearEasing)),
        label = "flowRotation",
    )
    val sweep by transition.animateFloat(
        initialValue = -0.35f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "flowSweep",
    )

    val glowA = StudioColors.Violet.copy(alpha = 0.18f)
    val glowB = StudioColors.Cyan.copy(alpha = 0.16f)

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // —— 层 1：两团缓慢旋转的极光光斑 ——
        rotate(rotation, pivot = Offset(w / 2f, h / 2f)) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(glowA, Color.Transparent),
                    center = Offset(w * 0.30f, h * 0.32f),
                    radius = w * 0.62f,
                ),
                radius = w * 0.62f,
                center = Offset(w * 0.30f, h * 0.32f),
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(glowB, Color.Transparent),
                    center = Offset(w * 0.74f, h * 0.70f),
                    radius = w * 0.58f,
                ),
                radius = w * 0.58f,
                center = Offset(w * 0.74f, h * 0.70f),
            )
        }

        // —— 层 2：斜向扫过的高光带 ——
        val bandCenter = sweep * (w + h) * 0.75f
        val bandWidth = w * 0.55f
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.Transparent,
                    Color.White.copy(alpha = 0.10f),
                    StudioColors.Violet.copy(alpha = 0.20f),
                    Color.White.copy(alpha = 0.10f),
                    Color.Transparent,
                ),
                start = Offset(bandCenter - bandWidth, 0f),
                end = Offset(bandCenter + bandWidth, h),
            ),
            size = size,
        )
    }
}

/**
 * 极光加载球：外圈旋转的流光光环 + 呼吸的核心 + 柔和光晕。
 */
@Composable
private fun AuroraLoadingOrb() {
    val transition = rememberInfiniteTransition(label = "auroraOrb")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(animation = tween(1400, easing = LinearEasing)),
        label = "auroraRotation",
    )
    val counterRotation by transition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(animation = tween(2600, easing = LinearEasing)),
        label = "auroraCounter",
    )
    val pulse by transition.animateFloat(
        initialValue = 0.86f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "auroraPulse",
    )

    Box(modifier = Modifier.size(84.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 5.dp.toPx()
            val inset = stroke / 2f + 2.dp.toPx()
            val arcSize = Size(size.width - inset * 2, size.height - inset * 2)
            val topLeft = Offset(inset, inset)

            // 柔和外光晕
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(StudioColors.Violet.copy(alpha = 0.22f), Color.Transparent),
                    center = Offset(size.width / 2f, size.height / 2f),
                    radius = size.minDimension / 2f,
                ),
                radius = size.minDimension / 2f,
            )
            // 顺时针主光环
            rotate(rotation) {
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            Color.Transparent,
                            StudioColors.Cyan,
                            StudioColors.Violet,
                            Color.Transparent,
                        ),
                    ),
                    startAngle = 0f,
                    sweepAngle = 300f,
                    useCenter = false,
                    style = Stroke(width = stroke, cap = StrokeCap.Round),
                    size = arcSize,
                    topLeft = topLeft,
                )
            }
            // 逆时针内环细线
            val innerInset = inset + 9.dp.toPx()
            rotate(counterRotation) {
                drawArc(
                    color = StudioColors.Violet.copy(alpha = 0.45f),
                    startAngle = 40f,
                    sweepAngle = 120f,
                    useCenter = false,
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round),
                    size = Size(size.width - innerInset * 2, size.height - innerInset * 2),
                    topLeft = Offset(innerInset, innerInset),
                )
            }
        }
        // 呼吸的核心
        Box(
            modifier = Modifier
                .size(30.dp)
                .graphicsLayer {
                    scaleX = pulse
                    scaleY = pulse
                }
                .clip(RoundedCornerShape(12.dp))
                .background(StudioColors.BrandGradient)
                .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Outlined.AutoAwesome,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

/**
 * 生成进度条：一段高光在轨道上反复流动的「跑马灯」式流光。
 */
@Composable
private fun ShimmerProgressBar(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "shimmerBar")
    val offset by transition.animateFloat(
        initialValue = -0.4f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmerOffset",
    )
    Canvas(
        modifier = modifier
            .height(4.dp)
            .clip(RoundedCornerShape(2.dp)),
    ) {
        drawRect(color = StudioColors.SurfaceGlass)
        val bandWidth = size.width * 0.4f
        val center = offset * size.width
        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color.Transparent,
                    StudioColors.Cyan,
                    StudioColors.Violet,
                    Color.Transparent,
                ),
                startX = center - bandWidth,
                endX = center + bandWidth,
            ),
        )
    }
}

@Composable
private fun StageAction(label: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .height(36.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(StudioColors.SurfaceHigh)
            .bounceClick(onClick = onClick)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Icon(icon, contentDescription = label, tint = StudioColors.TextPrimary, modifier = Modifier.size(15.dp))
        Text(label, color = StudioColors.TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun HistoryStrip(
    results: List<ImageResult>,
    activeId: Long?,
    onlyStarred: Boolean,
    onToggleStarred: () -> Unit,
    onSelect: (ImageResult) -> Unit,
    onOpen: (ImageResult) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp)
            .padding(bottom = 10.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(StudioColors.Surface)
            .border(1.dp, StudioColors.Border, RoundedCornerShape(22.dp))
            .padding(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("使用记录", color = StudioColors.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("${results.size} 张作品", color = StudioColors.TextMuted, fontSize = 11.sp)
            }
            Box(
                modifier = Modifier
                    .height(34.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (onlyStarred) StudioColors.Violet.copy(alpha = 0.15f) else StudioColors.SurfaceHigh)
                    .border(
                        1.dp,
                        if (onlyStarred) StudioColors.Violet else StudioColors.Border,
                        RoundedCornerShape(14.dp),
                    )
                    .bounceClick(onClick = onToggleStarred)
                    .padding(horizontal = 11.dp),
                contentAlignment = Alignment.Center,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    Icon(
                        imageVector = if (onlyStarred) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = "只看收藏",
                        tint = if (onlyStarred) StudioColors.Star else StudioColors.TextSecondary,
                        modifier = Modifier.size(16.dp),
                    )
                    Text("收藏", color = StudioColors.TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        if (results.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(82.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(StudioColors.SurfaceHigh),
                contentAlignment = Alignment.Center,
            ) {
                Text("暂无历史，生成后会出现在这里", color = StudioColors.TextMuted, fontSize = 12.sp)
            }
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(end = 2.dp),
            ) {
                lazyRowItems(results, key = { it.id }) { item ->
                    HistoryThumb(
                        item = item,
                        selected = item.id == activeId,
                        onSelect = { onSelect(item) },
                        onOpen = { onOpen(item) },
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryThumb(
    item: ImageResult,
    selected: Boolean,
    onSelect: () -> Unit,
    onOpen: () -> Unit,
) {
    Column(
        modifier = Modifier
            .width(86.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) StudioColors.Violet.copy(alpha = 0.12f) else StudioColors.SurfaceHigh)
            .border(
                1.dp,
                if (selected) StudioColors.Violet.copy(alpha = 0.70f) else StudioColors.Border,
                RoundedCornerShape(16.dp),
            )
            .bounceClick(pressedScale = 0.97f, onClick = onSelect)
            .padding(6.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(12.dp))
                .bounceClick(pressedScale = 0.98f, onClick = onOpen),
        ) {
            ResultImage(item = item, modifier = Modifier.fillMaxSize())
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = item.prompt,
            color = StudioColors.TextSecondary,
            fontSize = 10.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun DottedPattern(
    modifier: Modifier = Modifier,
    color: Color,
) {
    Canvas(modifier = modifier) {
        val spacing = 18.dp.toPx()
        val radius = 1.25.dp.toPx()
        var y = spacing
        while (y < size.height) {
            var x = spacing
            while (x < size.width) {
                drawCircle(color = color, radius = radius, center = Offset(x, y))
                x += spacing
            }
            y += spacing
        }
    }
}

@Composable
private fun TaskStatusCard(
    task: GenerationTask,
    onDismiss: () -> Unit,
) {
    val animatedProgress by animateFloatAsState(
        targetValue = task.progress.coerceIn(0.03f, 1f),
        animationSpec = tween(500),
        label = "taskProgress",
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp)
            .padding(bottom = 10.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(StudioColors.Surface.copy(alpha = 0.88f))
            .border(1.dp, StudioColors.Border.copy(alpha = 0.70f), RoundedCornerShape(18.dp))
            .padding(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(
                        if (task.status == TaskStatus.Failed) StudioColors.Danger.copy(alpha = 0.18f) else StudioColors.Violet.copy(alpha = 0.16f)
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (task.status in listOf(TaskStatus.Preparing, TaskStatus.Uploading, TaskStatus.Generating, TaskStatus.Saving)) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = StudioColors.Violet,
                    )
                } else {
                    Icon(
                        imageVector = if (task.status == TaskStatus.Failed) Icons.Outlined.Close else Icons.Outlined.AutoAwesome,
                        contentDescription = null,
                        tint = if (task.status == TaskStatus.Failed) StudioColors.Danger else StudioColors.Violet,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${task.status.label} · ${task.mode.label}",
                    color = StudioColors.TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = task.message,
                    color = StudioColors.TextMuted,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (task.status == TaskStatus.Completed || task.status == TaskStatus.Failed) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(onClick = onDismiss),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Outlined.Close, contentDescription = "关闭任务状态", tint = StudioColors.TextMuted, modifier = Modifier.size(17.dp))
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(StudioColors.SurfaceHigh)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(
                        if (task.status == TaskStatus.Failed) {
                            Brush.linearGradient(listOf(StudioColors.Danger, StudioColors.Danger))
                        } else {
                            StudioColors.BrandGradient
                        }
                    )
            )
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = task.model,
                color = StudioColors.TextSecondary,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = task.startedAt,
                color = StudioColors.TextMuted,
                fontSize = 11.sp,
            )
        }
    }
}

@Composable
private fun GalleryCard(
    item: ImageResult,
    onOpen: () -> Unit,
    onToggleStar: () -> Unit,
    onSave: () -> Unit,
    onShare: () -> Unit,
    onUseAsReference: () -> Unit,
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(StudioColors.Surface.copy(alpha = 0.86f))
            .border(1.dp, StudioColors.Border.copy(alpha = 0.62f), RoundedCornerShape(18.dp))
            .bounceClick(pressedScale = 0.985f, onClick = onOpen)
    ) {
        Box {
            ResultImage(
                item = item,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(aspectRatioFor(item.size))
                    .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
            )
            // 尺寸角标
            if (item.size != "auto") {
                Text(
                    text = item.size,
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.5f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            // 收藏按钮浮在右上角
            val starScale by animateFloatAsState(
                targetValue = if (item.starred) 1.18f else 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow,
                ),
                label = "starScale",
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(32.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.Black.copy(alpha = 0.4f))
                    .bounceClick(onClick = onToggleStar),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (item.starred) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    contentDescription = "收藏",
                    tint = if (item.starred) StudioColors.Star else Color.White,
                    modifier = Modifier
                        .size(18.dp)
                        .graphicsLayer {
                            scaleX = starScale
                            scaleY = starScale
                        }
                )
            }
        }
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = item.prompt,
                color = StudioColors.TextPrimary,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                MetaPill(item.model)
                Spacer(Modifier.weight(1f))
                Text(
                    item.createdAt,
                    color = StudioColors.TextMuted,
                    fontSize = 11.sp,
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MiniAction("保存", Icons.Outlined.Download, onSave)
                MiniAction("分享", Icons.Outlined.Share, onShare)
                MiniAction("参考", Icons.Outlined.ImageSearch, onUseAsReference)
            }
        }
    }
}

@Composable
private fun MiniAction(label: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(9.dp))
            .background(StudioColors.SurfaceHigh)
            .bounceClick(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(icon, contentDescription = label, tint = StudioColors.TextSecondary, modifier = Modifier.size(13.dp))
        Text(label, color = StudioColors.TextSecondary, fontSize = 11.sp)
    }
}

/**
 * 加载中的 shimmer 骨架屏：一道柔和高光在暗色底上循环扫过，
 * 比单调的转圈更有「内容正在到来」的高级感。
 */
@Composable
private fun ShimmerPlaceholder(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmerProgress",
    )
    val sweep = 700f
    val x = progress * (sweep * 2f) - sweep
    val brush = Brush.linearGradient(
        colors = listOf(
            StudioColors.SurfaceHigh,
            StudioColors.SurfaceGlass,
            StudioColors.SurfaceHigh,
        ),
        start = Offset(x, 0f),
        end = Offset(x + sweep, sweep),
    )
    Box(modifier = modifier.fillMaxSize().background(brush))
}

@Composable
internal fun ResultImage(item: ImageResult, modifier: Modifier = Modifier) {
    val model = remember(item.localPath, item.imageUrl, item.base64) {
        when {
            item.localPath != null && File(item.localPath).exists() -> File(item.localPath)
            item.imageUrl != null -> item.imageUrl
            item.base64 != null -> runCatching {
                java.nio.ByteBuffer.wrap(Base64.decode(item.base64, Base64.DEFAULT))
            }.getOrNull()
            else -> null
        }
    }
    Box(
        modifier = modifier.background(StudioColors.PlaceholderGradient),
        contentAlignment = Alignment.Center
    ) {
        if (model == null) {
            ImagePlaceholderText("等待图片数据")
        } else {
            SubcomposeAsyncImage(
                model = model,
                contentDescription = item.prompt,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                loading = { ShimmerPlaceholder() },
                error = { ImagePlaceholderText("图片加载失败") },
            )
        }
    }
}

@Composable
private fun ImagePlaceholderText(text: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(16.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.AutoAwesome,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.85f),
            modifier = Modifier.size(34.dp)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = text,
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 11.sp,
        )
    }
}

@Composable
private fun ImageDetailDialog(
    item: ImageResult,
    onDismiss: () -> Unit,
    onToggleStar: () -> Unit,
    onSave: () -> Unit,
    onShare: () -> Unit,
    onUseAsReference: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(StudioColors.Surface)
                .border(1.dp, StudioColors.Border, RoundedCornerShape(24.dp))
                .verticalScroll(rememberScrollState())
                .padding(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(aspectRatioFor(item.size))
                    .clip(RoundedCornerShape(18.dp))
            ) {
                ResultImage(item = item, modifier = Modifier.fillMaxSize())
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                        .size(36.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black.copy(alpha = 0.45f))
                        .clickable(onClick = onDismiss),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.Close, contentDescription = "关闭", tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MetaPill(item.model)
                MetaPill(item.size)
                MetaPill(item.format.uppercase())
                Spacer(Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .background(StudioColors.SurfaceHigh)
                        .clickable(onClick = onToggleStar),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (item.starred) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = "收藏",
                        tint = if (item.starred) StudioColors.Star else StudioColors.TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = item.prompt,
                color = StudioColors.TextPrimary,
                fontSize = 14.sp,
                lineHeight = 21.sp,
                fontWeight = FontWeight.Medium,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "生成于 ${item.createdAt}",
                color = StudioColors.TextMuted,
                fontSize = 12.sp,
            )
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                DetailAction("保存", Icons.Outlined.Download, onSave, Modifier.weight(1f))
                DetailAction("分享", Icons.Outlined.Share, onShare, Modifier.weight(1f))
                DetailAction("参考", Icons.Outlined.ImageSearch, onUseAsReference, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun DetailAction(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .height(42.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(StudioColors.SurfaceHigh)
            .bounceClick(onClick = onClick)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(icon, contentDescription = label, tint = StudioColors.TextPrimary, modifier = Modifier.size(17.dp))
        Spacer(Modifier.width(5.dp))
        Text(label, color = StudioColors.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun PromptLibraryDialog(
    templates: List<PromptTemplate>,
    onDismiss: () -> Unit,
    onUseTemplate: (PromptTemplate) -> Unit,
) {
    var selectedCategory by remember { mutableStateOf("全部") }
    var promptQuery by remember { mutableStateOf("") }
    val categories = remember(templates) { listOf("全部") + templates.map { it.category }.distinct() }
    val visibleTemplates = remember(selectedCategory, promptQuery, templates) {
        templates.filter { template ->
            val query = promptQuery.trim()
            val matchesCategory = selectedCategory == "全部" || template.category == selectedCategory
            val matchesQuery = query.isBlank() ||
                template.title.contains(query, ignoreCase = true) ||
                template.category.contains(query, ignoreCase = true) ||
                template.prompt.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(26.dp))
                .background(StudioColors.Surface)
                .border(1.dp, StudioColors.Border, RoundedCornerShape(26.dp))
                .padding(20.dp)
        ) {
            // —— 顶部：关闭按钮靠右，标题区居中 ——
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                GlassIconButton(
                    icon = Icons.Outlined.Close,
                    contentDescription = "关闭",
                    subtle = true,
                    onClick = onDismiss,
                )
            }
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "提示词中心",
                    color = StudioColors.TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "共 ${templates.size} 条提示词，按标题、分类快速查找灵感。",
                    color = StudioColors.TextMuted,
                    fontSize = 13.sp,
                )
            }
            Spacer(Modifier.height(18.dp))
            StudioInputField(
                value = promptQuery,
                onValueChange = { promptQuery = it },
                placeholder = "按标题查询",
                leadingIcon = Icons.Outlined.Search,
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
            )
            Spacer(Modifier.height(14.dp))
            // —— 分类筛选行：左侧“分类”标签 + 横向 chip ——
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "分类",
                    color = StudioColors.TextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 8.dp),
                )
                Spacer(Modifier.width(12.dp))
                androidx.compose.foundation.layout.FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    categories.forEach { category ->
                        val selected = category == selectedCategory
                        Text(
                            text = category,
                            color = if (selected) StudioColors.Background else StudioColors.TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                            modifier = Modifier
                                .padding(bottom = 8.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selected) StudioColors.Violet else StudioColors.SurfaceHigh)
                                .border(
                                    1.dp,
                                    if (selected) StudioColors.Violet else StudioColors.Border,
                                    RoundedCornerShape(10.dp)
                                )
                                .bounceClick(pressedScale = 0.95f) { selectedCategory = category }
                                .padding(horizontal = 12.dp, vertical = 7.dp),
                        )
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 260.dp, max = 500.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (visibleTemplates.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 36.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "没有匹配的提示词",
                            color = StudioColors.TextMuted,
                            fontSize = 13.sp,
                        )
                    }
                } else {
                    visibleTemplates.forEach { template ->
                        PromptTemplateRow(
                            template = template,
                            onClick = { onUseTemplate(template) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PromptTemplateRow(
    template: PromptTemplate,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(StudioColors.SurfaceHigh.copy(alpha = 0.55f))
            .border(1.dp, StudioColors.Border, RoundedCornerShape(16.dp))
            .bounceClick(pressedScale = 0.98f, onClick = onClick)
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = template.title,
                color = StudioColors.TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = template.category,
                color = StudioColors.Violet,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(StudioColors.Violet.copy(alpha = 0.14f))
                    .padding(horizontal = 9.dp, vertical = 4.dp),
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = template.prompt,
            color = StudioColors.TextSecondary,
            fontSize = 12.sp,
            lineHeight = 18.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Spacer(Modifier.weight(1f))
            Text(
                text = "使用",
                color = StudioColors.Violet,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.width(3.dp))
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = StudioColors.Violet,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

@Composable
private fun MetaPill(text: String) {
    Text(
        text = text,
        color = StudioColors.TextSecondary,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(StudioColors.SurfaceHigh)
            .border(1.dp, StudioColors.Border, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
}

/* ------------------------------ Agent tab -------------------------------- */

@Composable
private fun AgentPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(StudioColors.Surface)
                .border(1.dp, StudioColors.Border, RoundedCornerShape(24.dp))
                .padding(vertical = 48.dp, horizontal = 24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(StudioColors.BrandGradient),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.SmartToy,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = Color.White
                )
            }
            Spacer(Modifier.height(20.dp))
            Text("Agent 即将上线", fontWeight = FontWeight.Bold, color = StudioColors.TextPrimary, fontSize = 18.sp)
            Spacer(Modifier.height(6.dp))
            Text(
                "多轮对话式生图与自动优化提示词，\n正在路上",
                color = StudioColors.TextMuted,
                fontSize = 13.sp,
                lineHeight = 20.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }
    }
}

/* ------------------------------ Inputs ----------------------------------- */

@Composable
private fun StudioInputField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = singleLine,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        textStyle = TextStyle(
            color = StudioColors.TextPrimary,
            fontSize = 14.sp,
            lineHeight = 20.sp,
        ),
        modifier = modifier
            .heightIn(max = if (singleLine) 46.dp else 96.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(StudioColors.SurfaceHigh)
            .border(1.dp, StudioColors.Border.copy(alpha = 0.72f), RoundedCornerShape(15.dp)),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (leadingIcon != null) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        tint = StudioColors.TextMuted,
                        modifier = Modifier.size(19.dp),
                    )
                    Spacer(Modifier.width(10.dp))
                }
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    if (value.isEmpty()) {
                        Text(
                            placeholder,
                            color = StudioColors.TextMuted,
                            fontSize = 14.sp,
                            maxLines = if (singleLine) 1 else 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 20.sp,
                        )
                    }
                    innerTextField()
                }
            }
        }
    )
}

/* ----------------------------- Composer ---------------------------------- */

@Composable
private fun ComposerBar(
    prompt: String,
    params: GenerationParams,
    mode: GenerationMode,
    referenceImages: List<ReferenceImage>,
    isGenerating: Boolean,
    onOpenPromptLibrary: () -> Unit,
    onPromptChange: (String) -> Unit,
    onModeChange: (GenerationMode) -> Unit,
    onPickReference: () -> Unit,
    onClearReferences: () -> Unit,
    onVoiceInput: () -> Unit,
    onOpenParams: () -> Unit,
    onGenerate: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(Color.Transparent, StudioColors.Background, StudioColors.Background)
                )
            )
            .imePadding()
            .padding(horizontal = 12.dp)
            .padding(top = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(StudioColors.Surface)
                .border(1.dp, StudioColors.Border.copy(alpha = 0.72f), RoundedCornerShape(20.dp))
                .padding(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ModePill(
                    mode = GenerationMode.TextToImage,
                    selected = mode == GenerationMode.TextToImage,
                    onClick = { onModeChange(GenerationMode.TextToImage) },
                    modifier = Modifier.weight(1f),
                )
                ModePill(
                    mode = GenerationMode.ImageToImage,
                    selected = mode == GenerationMode.ImageToImage,
                    onClick = { onModeChange(GenerationMode.ImageToImage) },
                    modifier = Modifier.weight(1f),
                )
            }
            if (mode == GenerationMode.ImageToImage) {
                Spacer(Modifier.height(8.dp))
                ReferenceStrip(
                    referenceImages = referenceImages,
                    onPickReference = onPickReference,
                    onClearReferences = onClearReferences,
                )
            }
            Spacer(Modifier.height(10.dp))
            // 输入框：独占一行，给足书写空间
            StudioInputField(
                value = prompt,
                onValueChange = onPromptChange,
                placeholder = "描述你想生成的画面...",
                singleLine = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onGenerate() }),
            )
            Spacer(Modifier.height(10.dp))
            // 操作行：左侧工具，右侧主生成按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ComposerTool(
                    icon = Icons.Outlined.Tune,
                    label = params.size,
                    contentDescription = "参数",
                    onClick = onOpenParams,
                )
                ComposerTool(
                    icon = Icons.Outlined.AutoAwesome,
                    contentDescription = "灵感库",
                    onClick = onOpenPromptLibrary,
                )
                ComposerTool(
                    icon = Icons.Outlined.Mic,
                    contentDescription = "语音输入",
                    onClick = onVoiceInput,
                )
                Spacer(Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (isGenerating) Brush.linearGradient(
                                listOf(StudioColors.BorderStrong, StudioColors.BorderStrong)
                            ) else StudioColors.BrandGradient
                        )
                        .bounceClick(enabled = !isGenerating) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onGenerate()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            Icons.Outlined.ArrowForward,
                            contentDescription = "生成",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
            }
        }
        Spacer(
            Modifier
                .windowInsetsPadding(WindowInsets.navigationBars)
                .height(2.dp)
        )
    }
}

/**
 * 输入区左侧的轻量工具按钮。可只显示图标，也可在图标后带一段短标签
 * （例如把当前尺寸直接摊在参数入口上，省去单独的「参数」文字链接）。
 */
@Composable
private fun ComposerTool(
    icon: ImageVector,
    contentDescription: String,
    label: String? = null,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(StudioColors.SurfaceHigh)
            .bounceClick(onClick = onClick)
            .padding(horizontal = if (label != null) 12.dp else 0.dp)
            .then(if (label == null) Modifier.size(44.dp) else Modifier.height(44.dp)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(
            icon,
            contentDescription = contentDescription,
            tint = StudioColors.TextSecondary,
            modifier = Modifier.size(20.dp),
        )
        if (label != null) {
            Spacer(Modifier.width(6.dp))
            Text(
                label,
                color = StudioColors.TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun ModePill(
    mode: GenerationMode,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) StudioColors.Violet.copy(alpha = 0.13f) else StudioColors.SurfaceHigh.copy(alpha = 0.80f))
            .border(
                1.dp,
                if (selected) StudioColors.Violet.copy(alpha = 0.72f) else StudioColors.Border.copy(alpha = 0.60f),
                RoundedCornerShape(12.dp)
            )
            .bounceClick(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(
            if (mode == GenerationMode.TextToImage) Icons.Outlined.AutoAwesome else Icons.Outlined.ImageSearch,
            contentDescription = null,
            tint = if (selected) StudioColors.TextPrimary else StudioColors.TextMuted,
            modifier = Modifier.size(15.dp),
        )
        Spacer(Modifier.width(5.dp))
        Text(
            mode.label,
            color = if (selected) StudioColors.TextPrimary else StudioColors.TextMuted,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun ReferenceStrip(
    referenceImages: List<ReferenceImage>,
    onPickReference: () -> Unit,
    onClearReferences: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(StudioColors.SurfaceHigh.copy(alpha = 0.82f))
            .clickable(onClick = onPickReference)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Outlined.PhotoLibrary, contentDescription = null, tint = StudioColors.Violet, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        val label = if (referenceImages.isEmpty()) {
            "从相册选择参考图（可多选）"
        } else {
            val names = referenceImages.take(3).joinToString("、") { it.name }
            if (referenceImages.size > 3) "$names 等 ${referenceImages.size} 张" else "$names · 共 ${referenceImages.size} 张"
        }
        Text(
            label,
            color = if (referenceImages.isEmpty()) StudioColors.TextMuted else StudioColors.TextPrimary,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        if (referenceImages.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onClearReferences),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Outlined.Close, contentDescription = "清除参考图", tint = StudioColors.TextMuted, modifier = Modifier.size(15.dp))
            }
        }
    }
}

/* ------------------------------ Dialogs ---------------------------------- */

@Composable
private fun ModelPickerDialog(
    settings: StudioSettings,
    isLoadingModels: Boolean,
    currentModel: String,
    onSelectModel: (String, String) -> Unit,
    onClose: () -> Unit,
) {
    val choices = settings.channels.flatMap { channel ->
        channel.models.ifEmpty { listOf("gpt-image-2") }.map { model -> channel to model }
    }
    val activeChannel = settings.activeChannel()
    Dialog(onDismissRequest = onClose) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(StudioColors.Surface)
                .border(1.dp, StudioColors.Border, RoundedCornerShape(24.dp))
                .verticalScroll(rememberScrollState())
                .padding(22.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "模型列表",
                    style = MaterialTheme.typography.headlineSmall,
                    color = StudioColors.TextPrimary,
                )
                Spacer(Modifier.weight(1f))
                if (isLoadingModels) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = StudioColors.Violet,
                        strokeWidth = 2.dp,
                    )
                    Spacer(Modifier.width(8.dp))
                }
                GlassIconButton(
                    icon = Icons.Outlined.Close,
                    contentDescription = "关闭",
                    onClick = onClose,
                )
            }
            Spacer(Modifier.height(20.dp))
            Text("当前模型", fontWeight = FontWeight.SemiBold, color = StudioColors.TextSecondary, fontSize = 13.sp)
            Spacer(Modifier.height(10.dp))
            choices.forEach { (channel, model) ->
                val selected = channel.id == activeChannel.id && model == currentModel
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(if (selected) StudioColors.Violet.copy(alpha = 0.16f) else StudioColors.SurfaceHigh)
                        .border(
                            1.dp,
                            if (selected) StudioColors.Violet else StudioColors.Border,
                            RoundedCornerShape(13.dp)
                        )
                        .clickable { onSelectModel(channel.id, model) }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(model, color = StudioColors.TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text(channel.name, color = StudioColors.TextMuted, fontSize = 11.sp)
                    }
                    if (selected) Text("当前", color = StudioColors.Violet, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ParamsDialog(
    params: GenerationParams,
    onParamsChange: (GenerationParams) -> Unit,
    onClose: () -> Unit,
) {
    Dialog(onDismissRequest = onClose) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(StudioColors.Surface)
                .border(1.dp, StudioColors.Border, RoundedCornerShape(24.dp))
                .verticalScroll(rememberScrollState())
                .padding(22.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "生成参数",
                    style = MaterialTheme.typography.headlineSmall,
                    color = StudioColors.TextPrimary,
                )
                Spacer(Modifier.weight(1f))
                /*
                GlassIconButton(
                    icon = Icons.Outlined.Refresh,
                    contentDescription = "刷新模型",
                    loading = isLoadingModels,
                    onClick = onRefreshModels,
                )
                Spacer(Modifier.width(8.dp))
                */
                GlassIconButton(
                    icon = Icons.Outlined.Close,
                    contentDescription = "关闭",
                    onClick = onClose,
                )
            }
            Spacer(Modifier.height(20.dp))

            /*
            OptionGroup(
                title = "模型",
                options = models,
                selected = params.model,
                onSelect = { onParamsChange(params.copy(model = it)) },
            )
            */
            OptionGroup(
                title = "尺寸",
                options = listOf("auto", "1024x1024", "1024x1536", "1536x1024"),
                selected = params.size,
                onSelect = { onParamsChange(params.copy(size = it)) },
            )
            OptionGroup(
                title = "质量",
                options = listOf("auto", "low", "medium", "high"),
                selected = params.quality,
                onSelect = { onParamsChange(params.copy(quality = it)) },
            )
            OptionGroup(
                title = "格式",
                options = listOf("jpeg", "png", "webp"),
                selected = params.format,
                onSelect = { onParamsChange(params.copy(format = it)) },
            )
            OptionGroup(
                title = "数量",
                options = listOf("1", "2", "4"),
                selected = params.count.toString(),
                onSelect = { onParamsChange(params.copy(count = it.toInt())) },
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onClose,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(15.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = StudioColors.Violet,
                    contentColor = StudioColors.Background,
                ),
            ) {
                Text("完成", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}

@Composable
private fun OptionGroup(
    title: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
) {
    Text(
        title,
        fontWeight = FontWeight.SemiBold,
        color = StudioColors.TextSecondary,
        fontSize = 13.sp,
    )
    Spacer(Modifier.height(10.dp))
    androidx.compose.foundation.layout.FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        options.forEach { option ->
            val isSel = selected == option
            Box(
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSel) StudioColors.Violet.copy(alpha = 0.16f) else StudioColors.SurfaceHigh)
                    .border(
                        1.dp,
                        if (isSel) StudioColors.Violet else StudioColors.Border,
                        RoundedCornerShape(12.dp)
                    )
                    .bounceClick(pressedScale = 0.95f) { onSelect(option) }
                    .padding(horizontal = 14.dp, vertical = 9.dp),
            ) {
                Text(
                    option,
                    color = if (isSel) StudioColors.TextPrimary else StudioColors.TextSecondary,
                    fontWeight = if (isSel) FontWeight.SemiBold else FontWeight.Normal,
                    fontSize = 13.sp,
                )
            }
        }
    }
    Spacer(Modifier.height(20.dp))
}

@Composable
private fun SettingsCardHeader(
    icon: ImageVector,
    title: String,
    subtitle: String,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(StudioColors.Violet.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = StudioColors.Violet,
                modifier = Modifier.size(18.dp),
            )
        }
        Spacer(Modifier.width(11.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = StudioColors.TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = StudioColors.TextMuted, fontSize = 12.sp, lineHeight = 16.sp)
        }
    }
}

@Composable
private fun SettingsEntry(
    icon: ImageVector,
    title: String,
    subtitle: String,
    meta: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(StudioColors.SurfaceHigh.copy(alpha = 0.62f))
            .border(1.dp, StudioColors.Border, RoundedCornerShape(18.dp))
            .bounceClick(pressedScale = 0.98f, onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(StudioColors.Violet.copy(alpha = 0.14f))
                .border(1.dp, StudioColors.Violet.copy(alpha = 0.28f), RoundedCornerShape(13.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = StudioColors.Violet,
                modifier = Modifier.size(21.dp),
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = StudioColors.TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(5.dp))
            Text(subtitle, color = StudioColors.TextMuted, fontSize = 12.sp, lineHeight = 17.sp)
        }
        Spacer(Modifier.width(10.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text(meta, color = StudioColors.Violet, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = StudioColors.TextMuted,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun SettingsPageV2(
    settings: StudioSettings,
    params: GenerationParams,
    isLoadingModels: Boolean,
    onClose: () -> Unit,
    onSavePreferences: (StudioSettings, GenerationParams) -> Unit,
    onSaveChannels: (StudioSettings) -> Unit,
) {
    fun normalizedSettings(source: StudioSettings): StudioSettings {
        val channels = source.channels.ifEmpty { listOf(ApiChannel(baseUrl = DEFAULT_BASE_URL)) }
            .mapIndexed { index, channel ->
                channel.copy(
                    name = channel.name.ifBlank { if (index == 0) "默认渠道" else "渠道 ${index + 1}" },
                    baseUrl = channel.baseUrl.ifBlank { DEFAULT_BASE_URL }.trim().trimEnd('/'),
                    apiKey = channel.apiKey.trim(),
                    models = channel.models.filter { it.isNotBlank() }.distinct().ifEmpty { listOf("gpt-image-2") },
                )
            }
        val activeId = channels.firstOrNull { it.id == source.activeChannelId }?.id ?: channels.first().id
        return source.copy(channels = channels, activeChannelId = activeId)
    }

    var section by remember { mutableStateOf("home") }
    var draftSettings by remember(settings) { mutableStateOf(normalizedSettings(settings)) }
    var draftParams by remember(params, settings) {
        mutableStateOf(
            params.copy(
                count = settings.defaultImageCount,
                size = settings.defaultSize,
                quality = settings.defaultQuality,
                format = settings.defaultFormat,
            )
        )
    }

    fun updateChannel(channel: ApiChannel) {
        draftSettings = draftSettings.copy(
            channels = draftSettings.channels.map { if (it.id == channel.id) channel else it }
        )
    }

    fun removeChannel(channel: ApiChannel) {
        if (draftSettings.channels.size <= 1) return
        val channels = draftSettings.channels.filterNot { it.id == channel.id }
        val activeId = if (draftSettings.activeChannelId == channel.id) channels.first().id else draftSettings.activeChannelId
        draftSettings = draftSettings.copy(channels = channels, activeChannelId = activeId)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(StudioColors.Background)
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(20.dp))
                .background(StudioColors.Surface)
                .border(1.dp, StudioColors.Border, RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                if (section != "home") {
                    TextButton(onClick = { section = "home" }) {
                        Text("返回", color = StudioColors.TextSecondary)
                    }
                    Spacer(Modifier.width(4.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = when (section) {
                            "channels" -> "渠道设置"
                            "preferences" -> "生成偏好"
                            else -> "设置"
                        },
                        color = StudioColors.TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = when (section) {
                            "channels" -> "填写 URL 和 Key，确定后自动拉取全部模型"
                            "preferences" -> "默认生图参数、音频指令和系统提示词"
                            else -> "选择要调整的设置项"
                        },
                        color = StudioColors.TextMuted,
                        fontSize = 12.sp,
                    )
                }
                GlassIconButton(icon = Icons.Outlined.Close, contentDescription = "关闭", onClick = onClose)
            }
            Spacer(Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                when (section) {
                    "home" -> {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            val activeChannel = draftSettings.channels.firstOrNull { it.id == draftSettings.activeChannelId }
                                ?: draftSettings.channels.firstOrNull()
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(StudioColors.GlowGradient)
                                    .border(1.dp, StudioColors.Border, RoundedCornerShape(18.dp))
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (activeChannel?.apiKey?.isNotBlank() == true) StudioColors.Success else StudioColors.Warning)
                                )
                                Spacer(Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "当前渠道 · ${activeChannel?.name.orEmpty().ifBlank { "默认渠道" }}",
                                        color = StudioColors.TextPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                    Text(
                                        if (activeChannel?.apiKey?.isNotBlank() == true) "已配置 Key · ${activeChannel.models.size} 个模型" else "尚未填写 API Key",
                                        color = StudioColors.TextMuted,
                                        fontSize = 12.sp,
                                    )
                                }
                            }
                            SettingsEntry(
                                icon = Icons.Outlined.Hub,
                                title = "渠道设置",
                                subtitle = "默认渠道已使用你的中转站地址，只需填写 Key。也可以新增多个渠道。",
                                meta = "${draftSettings.channels.size} 个渠道",
                                onClick = { section = "channels" },
                            )
                            SettingsEntry(
                                icon = Icons.Outlined.Tune,
                                title = "生成偏好",
                                subtitle = "设置默认生图张数、尺寸、质量、格式和提示词。",
                                meta = "${draftParams.count} 张",
                                onClick = { section = "preferences" },
                            )
                        }
                    }
                    "channels" -> {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("渠道", color = StudioColors.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Text("填好 Key，确定后自动拉取模型。", color = StudioColors.TextMuted, fontSize = 12.sp)
                            }
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(11.dp))
                                    .background(StudioColors.Violet)
                                    .bounceClick(pressedScale = 0.96f) {
                                        val channel = ApiChannel(
                                            name = "渠道 ${draftSettings.channels.size + 1}",
                                            baseUrl = DEFAULT_BASE_URL,
                                        )
                                        draftSettings = draftSettings.copy(
                                            channels = draftSettings.channels + channel,
                                            activeChannelId = channel.id,
                                        )
                                    }
                                    .padding(horizontal = 13.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Add,
                                    contentDescription = null,
                                    tint = StudioColors.Background,
                                    modifier = Modifier.size(17.dp),
                                )
                                Spacer(Modifier.width(5.dp))
                                Text("新增", color = StudioColors.Background, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        draftSettings.channels.forEachIndexed { index, channel ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp)
                                    .clip(RoundedCornerShape(17.dp))
                                    .background(StudioColors.SurfaceHigh.copy(alpha = 0.48f))
                                    .border(
                                        1.dp,
                                        if (channel.id == draftSettings.activeChannelId) StudioColors.Violet else StudioColors.Border,
                                        RoundedCornerShape(17.dp)
                                    )
                                    .padding(12.dp)
                            ) {
                                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    val isActive = channel.id == draftSettings.activeChannelId
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (isActive) StudioColors.Success else StudioColors.BorderStrong)
                                    )
                                    Spacer(Modifier.width(9.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            channel.name.ifBlank { if (index == 0) "默认渠道" else "渠道 ${index + 1}" },
                                            color = StudioColors.TextPrimary,
                                            fontWeight = FontWeight.Bold,
                                        )
                                        Text(
                                            if (isActive) "当前渠道" else "备用渠道",
                                            color = StudioColors.TextMuted,
                                            fontSize = 12.sp,
                                        )
                                    }
                                    if (isActive) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(StudioColors.Violet.copy(alpha = 0.16f))
                                                .padding(horizontal = 10.dp, vertical = 5.dp)
                                        ) {
                                            Text("当前", color = StudioColors.Violet, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                        }
                                    } else {
                                        TextButton(onClick = { draftSettings = draftSettings.copy(activeChannelId = channel.id) }) {
                                            Text("设为当前", color = StudioColors.Violet)
                                        }
                                    }
                                    TextButton(onClick = { removeChannel(channel) }, enabled = draftSettings.channels.size > 1) {
                                        Text("删除", color = if (draftSettings.channels.size > 1) StudioColors.Danger else StudioColors.TextMuted)
                                    }
                                }
                                Spacer(Modifier.height(10.dp))
                                OutlinedTextField(
                                    value = channel.name,
                                    onValueChange = { updateChannel(channel.copy(name = it)) },
                                    label = { Text("渠道名称") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(14.dp),
                                    colors = studioFieldColors(),
                                    modifier = Modifier.fillMaxWidth(),
                                )
                                Spacer(Modifier.height(10.dp))
                                OutlinedTextField(
                                    value = channel.baseUrl,
                                    onValueChange = { updateChannel(channel.copy(baseUrl = it.trim().trimEnd('/'))) },
                                    label = { Text("Base URL") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(14.dp),
                                    colors = studioFieldColors(),
                                    modifier = Modifier.fillMaxWidth(),
                                )
                                Spacer(Modifier.height(10.dp))
                                OutlinedTextField(
                                    value = channel.apiKey,
                                    onValueChange = { updateChannel(channel.copy(apiKey = it.trim())) },
                                    label = { Text("API Key") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(14.dp),
                                    colors = studioFieldColors(),
                                    modifier = Modifier.fillMaxWidth(),
                                )
                                Spacer(Modifier.height(8.dp))
                                val uriHandler = LocalUriHandler.current
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        "没有密钥？",
                                        color = StudioColors.TextMuted,
                                        fontSize = 12.sp,
                                    )
                                    Text(
                                        "去获取",
                                        color = StudioColors.Violet,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .bounceClick(pressedScale = 0.94f) {
                                                runCatching { uriHandler.openUri("https://image.yydsapi.uno/") }
                                            }
                                            .padding(horizontal = 4.dp, vertical = 2.dp),
                                    )
                                    Icon(
                                        imageVector = Icons.Outlined.ChevronRight,
                                        contentDescription = null,
                                        tint = StudioColors.Violet,
                                        modifier = Modifier.size(14.dp),
                                    )
                                }
                            }
                        }
                    }
                    "preferences" -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(17.dp))
                                .background(StudioColors.SurfaceHigh.copy(alpha = 0.48f))
                                .border(1.dp, StudioColors.Border, RoundedCornerShape(17.dp))
                                .padding(14.dp)
                        ) {
                            SettingsCardHeader(
                                icon = Icons.Outlined.Tune,
                                title = "生图参数",
                                subtitle = "每次生图默认套用，可在工作台临时调整。",
                            )
                            Spacer(Modifier.height(14.dp))
                            OptionGroup(
                                title = "默认生图张数",
                                options = listOf("1", "2", "3", "4"),
                                selected = draftParams.count.toString(),
                                onSelect = { draftParams = draftParams.copy(count = it.toInt()) },
                            )
                            OptionGroup(
                                title = "默认尺寸",
                                options = listOf("auto", "1024x1024", "1024x1536", "1536x1024"),
                                selected = draftParams.size,
                                onSelect = { draftParams = draftParams.copy(size = it) },
                            )
                            OptionGroup(
                                title = "默认质量",
                                options = listOf("auto", "low", "medium", "high"),
                                selected = draftParams.quality,
                                onSelect = { draftParams = draftParams.copy(quality = it) },
                            )
                            OptionGroup(
                                title = "默认格式",
                                options = listOf("jpeg", "png", "webp"),
                                selected = draftParams.format,
                                onSelect = { draftParams = draftParams.copy(format = it) },
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(17.dp))
                                .background(StudioColors.SurfaceHigh.copy(alpha = 0.48f))
                                .border(1.dp, StudioColors.Border, RoundedCornerShape(17.dp))
                                .padding(14.dp)
                        ) {
                            SettingsCardHeader(
                                icon = Icons.Outlined.AutoAwesome,
                                title = "提示词",
                                subtitle = "音频指令与系统提示词会作为全局默认注入。",
                            )
                            Spacer(Modifier.height(14.dp))
                            OutlinedTextField(
                                value = draftSettings.defaultAudioPrompt,
                                onValueChange = { draftSettings = draftSettings.copy(defaultAudioPrompt = it) },
                                label = { Text("默认音频指令") },
                                maxLines = 3,
                                shape = RoundedCornerShape(14.dp),
                                colors = studioFieldColors(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 74.dp),
                            )
                            Spacer(Modifier.height(10.dp))
                            OutlinedTextField(
                                value = draftSettings.systemPrompt,
                                onValueChange = { draftSettings = draftSettings.copy(systemPrompt = it) },
                                label = { Text("系统提示词") },
                                maxLines = 6,
                                shape = RoundedCornerShape(14.dp),
                                colors = studioFieldColors(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 120.dp),
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                if (section == "home") {
                    Button(
                        onClick = onClose,
                        modifier = Modifier.height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = StudioColors.SurfaceHigh, contentColor = StudioColors.TextPrimary),
                    ) {
                        Text("完成")
                    }
                } else {
                    TextButton(onClick = { section = "home" }) {
                        Text("取消", color = StudioColors.TextSecondary)
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (section == "channels") {
                                val next = normalizedSettings(draftSettings)
                                draftSettings = next
                                onSaveChannels(next)
                                section = "home"
                            } else {
                                val nextSettings = draftSettings.copy(
                                    defaultImageCount = draftParams.count,
                                    defaultSize = draftParams.size,
                                    defaultQuality = draftParams.quality,
                                    defaultFormat = draftParams.format,
                                )
                                draftSettings = nextSettings
                                onSavePreferences(nextSettings, draftParams)
                                section = "home"
                            }
                        },
                        enabled = !isLoadingModels,
                        modifier = Modifier.height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = StudioColors.Violet, contentColor = StudioColors.Background),
                    ) {
                        Text(if (isLoadingModels && section == "channels") "拉取中" else "确定")
                    }
                }
            }
        }
    }
}

@Composable
private fun studioFieldColors() = TextFieldDefaults.outlinedTextFieldColors(
    containerColor = StudioColors.SurfaceHigh,
    focusedBorderColor = StudioColors.Violet,
    unfocusedBorderColor = StudioColors.Border,
    cursorColor = StudioColors.Violet,
    textColor = StudioColors.TextPrimary,
    focusedLabelColor = StudioColors.Violet,
    unfocusedLabelColor = StudioColors.TextMuted,
)

/* ------------------------------- Network --------------------------------- */

private data class GeneratedImage(
    val url: String? = null,
    val base64: String? = null,
    val mimeType: String = "image/jpeg",
)

private object ImageApi {
    suspend fun listModels(baseUrl: String, key: String): Result<List<String>> = withContext(Dispatchers.IO) {
        runCatching {
            val connection = openConnection("$baseUrl/v1/models", key, "GET")
            val response = readResponse(connection)
            val json = JSONObject(response)
            val data = json.optJSONArray("data") ?: return@runCatching emptyList()
            buildList {
                for (index in 0 until data.length()) {
                    val item = data.optJSONObject(index)
                    val id = item?.optString("id").orEmpty()
                    if (id.isNotBlank()) add(id)
                }
            }.distinct()
        }
    }

    suspend fun generate(
        baseUrl: String,
        key: String,
        prompt: String,
        params: GenerationParams,
    ): Result<List<GeneratedImage>> = withContext(Dispatchers.IO) {
        runCatching {
            val body = JSONObject()
                .put("model", params.model)
                .put("prompt", prompt)
                .put("n", params.count)
                .put("size", params.size)
                .put("quality", params.quality)
                .put("response_format", "b64_json")
                .put("output_format", params.format)

            val connection = openConnection("$baseUrl/v1/images/generations", key, "POST")
            connection.doOutput = true
            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(body.toString())
            }
            val response = readResponse(connection)
            val json = JSONObject(response)
            val data = json.optJSONArray("data") ?: error("接口未返回 data")
            buildList {
                for (index in 0 until data.length()) {
                    val item = data.optJSONObject(index) ?: continue
                    val rawBase64 = item.optString("b64_json").takeIf { it.isNotBlank() }
                    val url = item.optString("url").takeIf { it.isNotBlank() }
                    add(GeneratedImage(url = url, base64 = rawBase64, mimeType = "image/${params.format}"))
                }
            }.ifEmpty { error("没有解析到图片") }
        }
    }

    suspend fun edit(
        context: Context,
        baseUrl: String,
        key: String,
        prompt: String,
        params: GenerationParams,
        references: List<ReferenceImage>,
    ): Result<List<GeneratedImage>> = withContext(Dispatchers.IO) {
        runCatching {
            if (references.isEmpty()) error("请先选择参考图")
            val boundary = "PhoneImageBoundary${UUID.randomUUID().toString().replace("-", "")}"
            val connection = openConnection("$baseUrl/v1/images/edits", key, "POST")
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
            connection.outputStream.use { output ->
                fun writeText(name: String, value: String) {
                    output.write("--$boundary\r\n".toByteArray())
                    output.write("Content-Disposition: form-data; name=\"$name\"\r\n\r\n".toByteArray())
                    output.write(value.toByteArray())
                    output.write("\r\n".toByteArray())
                }

                writeText("model", params.model)
                writeText("prompt", prompt)
                writeText("n", params.count.toString())
                writeText("size", params.size)
                writeText("quality", params.quality)
                writeText("response_format", "b64_json")
                writeText("output_format", params.format)

                references.forEachIndexed { index, reference ->
                    val imageBytes = if (reference.uri.scheme == "file") {
                        java.io.File(reference.uri.path ?: error("无法读取参考图")).readBytes()
                    } else {
                        context.contentResolver.openInputStream(reference.uri)?.use { it.readBytes() }
                            ?: error("无法读取参考图")
                    }
                    val mimeType = context.contentResolver.getType(reference.uri) ?: "image/png"
                    output.write("--$boundary\r\n".toByteArray())
                    output.write("Content-Disposition: form-data; name=\"image\"; filename=\"reference_${index + 1}.png\"\r\n".toByteArray())
                    output.write("Content-Type: $mimeType\r\n\r\n".toByteArray())
                    output.write(imageBytes)
                    output.write("\r\n".toByteArray())
                }
                output.write("--$boundary--\r\n".toByteArray())
            }
            val response = readResponse(connection)
            val json = JSONObject(response)
            val data = json.optJSONArray("data") ?: error("接口未返回 data")
            buildList {
                for (index in 0 until data.length()) {
                    val item = data.optJSONObject(index) ?: continue
                    val rawBase64 = item.optString("b64_json").takeIf { it.isNotBlank() }
                    val url = item.optString("url").takeIf { it.isNotBlank() }
                    add(GeneratedImage(url = url, base64 = rawBase64, mimeType = "image/${params.format}"))
                }
            }.ifEmpty { error("没有解析到图片") }
        }
    }

    private fun openConnection(url: String, key: String, method: String): HttpURLConnection {
        return (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 30_000
            readTimeout = 120_000
            setRequestProperty("Authorization", "Bearer $key")
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Accept", "application/json")
        }
    }

    private fun readResponse(connection: HttpURLConnection): String {
        val stream = if (connection.responseCode in 200..299) {
            connection.inputStream
        } else {
            connection.errorStream ?: connection.inputStream
        }
        val text = BufferedReader(InputStreamReader(stream)).use { it.readText() }
        if (connection.responseCode !in 200..299) {
            val message = runCatching { JSONObject(text).optJSONObject("error")?.optString("message") }
                .getOrNull()
                ?.takeIf { it.isNotBlank() }
                ?: text.take(180)
            error("HTTP ${connection.responseCode}: $message")
        }
        return text
    }
}
