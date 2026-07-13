package com.toywake.ui.play

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.toywake.tts.ToyWakeTtsManager

private enum class ContextAction { NEXT, END }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayScreen(
    onBack: () -> Unit,
    onNavigateScan: () -> Unit,
    vm: PlayViewModel = viewModel(),
) {
    val context = LocalContext.current
    val tts = remember { ToyWakeTtsManager(context) }

    val uiState by vm.uiState.collectAsState()
    val error by vm.error.collectAsState()
    val finished by vm.finished.collectAsState()

    var pendingAction by remember { mutableStateOf<ContextAction?>(null) }
    var contextInput by remember { mutableStateOf("") }

    // TTS 生命周期：离开页面时 shutdown
    DisposableEffect(tts) {
        onDispose { tts.shutdown() }
    }

    // 收到语音事件 -> 按顺序播放
    LaunchedEffect(tts) {
        vm.speech.collect { utterances ->
            utterances.forEachIndexed { i, text ->
                tts.speak(text, flush = (i == 0))
            }
        }
    }

    // 游戏结束并保存/跳过后返回扫描页
    LaunchedEffect(finished) {
        if (finished) onNavigateScan()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("共玩") },
                navigationIcon = {
                    IconButton(onClick = {
                        tts.stop()
                        onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            when (val s = uiState) {
                PlayUiState.Loading -> Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CircularProgressIndicator()
                    Text("正在叫醒……", modifier = Modifier.padding(start = 12.dp))
                }

                is PlayUiState.Playing -> PlayingContent(
                    state = s,
                    onNext = { pendingAction = ContextAction.NEXT; contextInput = "" },
                    onEnd = { pendingAction = ContextAction.END; contextInput = "" },
                    onReplay = {
                        val phrases = buildList {
                            s.wakePhrase?.let { add(it) }
                            add(s.spark.child_speech)
                        }
                        phrases.forEachIndexed { i, t -> tts.speak(t, flush = (i == 0)) }
                    },
                )

                is PlayUiState.Ended -> EndedContent(
                    state = s,
                    onSave = { vm.saveMemory(it) },
                    onSkip = { vm.skipMemory() },
                )

                is PlayUiState.Error -> Text(
                    "出错了：${s.message}\n请检查后端连接。",
                    color = MaterialTheme.colorScheme.error,
                )
            }
            error?.let {
                Text("提示：$it", color = MaterialTheme.colorScheme.error)
            }
        }
    }

    // 家长上下文输入对话框
    if (pendingAction != null) {
        AlertDialog(
            onDismissRequest = { pendingAction = null },
            title = { Text("孩子刚才说了什么，或做了什么？") },
            text = {
                OutlinedTextField(
                    value = contextInput,
                    onValueChange = { if (it.length <= 100) contextInput = it },
                    placeholder = { Text("例如：孩子说要去救小兔。") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val ctx = contextInput.ifBlank { null }
                    when (pendingAction) {
                        ContextAction.NEXT -> vm.next(ctx)
                        ContextAction.END -> vm.end(ctx)
                        null -> Unit
                    }
                    pendingAction = null
                }) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { pendingAction = null }) { Text("取消") }
            },
        )
    }
}

@Composable
private fun PlayingContent(
    state: PlayUiState.Playing,
    onNext: () -> Unit,
    onEnd: () -> Unit,
    onReplay: () -> Unit,
) {
    state.wakePhrase?.let {
        Text(it, style = MaterialTheme.typography.headlineSmall)
    }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(state.spark.child_speech, style = MaterialTheme.typography.titleMedium)
            Text(
                "给家长：${state.spark.parent_hint}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary,
            )
            Text("来源：${labelOf(state.spark.source)}", style = MaterialTheme.typography.labelSmall)
        }
    }
    OutlinedButton(onClick = onReplay, enabled = !state.requesting) { Text("再听一次") }
    Button(onClick = onNext, enabled = !state.requesting, modifier = Modifier.fillMaxWidth()) {
        Text(if (state.requesting) "正在想……" else "再来一个点子")
    }
    OutlinedButton(onClick = onEnd, enabled = !state.requesting, modifier = Modifier.fillMaxWidth()) {
        Text("准备结束")
    }
}

@Composable
private fun EndedContent(
    state: PlayUiState.Ended,
    onSave: (String) -> Unit,
    onSkip: () -> Unit,
) {
    Text(state.endingSpeech, style = MaterialTheme.typography.headlineSmall)
    Text("今天发生了什么？", style = MaterialTheme.typography.titleMedium)
    var memory by remember(state) { mutableStateOf(state.memoryCandidate ?: "") }
    OutlinedTextField(
        value = memory,
        onValueChange = { if (it.length <= 50) memory = it },
        modifier = Modifier.fillMaxWidth(),
        maxLines = 2,
    )
    Button(
        onClick = { onSave(memory) },
        enabled = memory.isNotBlank(),
        modifier = Modifier.fillMaxWidth(),
    ) { Text("保存这条记忆") }
    OutlinedButton(onClick = onSkip, modifier = Modifier.fillMaxWidth()) { Text("不保存") }
}

private fun labelOf(source: String): String = when (source) {
    "ai" -> "AI 生成"
    "fixed" -> "固定内容"
    "mock" -> "离线模拟"
    else -> source
}
