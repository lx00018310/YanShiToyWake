package com.toywake.ui.scan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.toywake.nfc.NfcReaderManager
import com.toywake.nfc.NfcState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(
    onNavigateBind: (String) -> Unit,
    onNavigatePlay: (Int) -> Unit,
    onNavigateSettings: () -> Unit,
    vm: ScanViewModel = viewModel(),
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val manager = remember { NfcReaderManager(context as android.app.Activity) }
    val state by vm.state.collectAsState()
    val outcome by vm.outcome.collectAsState()
    val error by vm.error.collectAsState()

    // 初始化 NFC 状态
    LaunchedEffect(Unit) { vm.setNfcState(manager.initialState) }

    // Reader Mode 生命周期：进入页面开启，退出关闭
    DisposableEffect(lifecycleOwner, manager) {
        manager.onTagScanned = { uid -> vm.onTagUid(uid) }
        manager.enable()
        onDispose { manager.disable() }
    }

    // 扫描结果 -> 路由
    LaunchedEffect(outcome) {
        when (val o = outcome) {
            is ScanOutcome.New -> {
                vm.clearOutcome()
                onNavigateBind(o.tagUid)
            }
            is ScanOutcome.Known -> {
                val toyId = o.toy.id
                vm.clearOutcome()
                onNavigatePlay(toyId)
            }
            null -> Unit
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ToyWake") },
                actions = {
                    IconButton(onClick = onNavigateSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "设置")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(statusText(state), style = MaterialTheme.typography.headlineSmall)
            if (state == NfcState.RESOLVING) {
                Text("正在叫醒……", style = MaterialTheme.typography.bodyLarge)
            }
            error?.let {
                Text(
                    "连接失败：$it\n请在设置中检查后端地址。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

private fun statusText(state: NfcState): String = when (state) {
    NfcState.READY -> "请用手机轻触玩具"
    NfcState.TAG_DETECTED -> "检测到玩具"
    NfcState.RESOLVING -> "正在叫醒……"
    NfcState.UNAVAILABLE -> "该手机不支持 NFC"
    NfcState.DISABLED -> "NFC 未开启，请在系统设置中打开"
    NfcState.ERROR -> "出错了，请重试"
}
