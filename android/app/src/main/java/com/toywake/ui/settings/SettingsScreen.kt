package com.toywake.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(vm: SettingsViewModel = viewModel()) {
    val baseUrl by vm.baseUrl.collectAsState()
    val connection by vm.connection.collectAsState()
    var urlInput by remember { mutableStateOf(baseUrl) }

    // 后端地址变化时同步输入框
    LaunchedEffect(baseUrl) { urlInput = baseUrl }

    Scaffold(
        topBar = { TopAppBar(title = { Text("设置") }) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("后端地址", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = urlInput,
                onValueChange = { urlInput = it },
                singleLine = true,
                placeholder = { Text("http://192.168.1.100:8000") },
                modifier = Modifier.fillMaxWidth(),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { vm.updateBaseUrl(urlInput) }) { Text("保存") }
                OutlinedButton(onClick = { vm.testConnection() }) { Text("测试连接") }
            }
            Spacer(Modifier.height(8.dp))
            ConnectionStatus(connection)
        }
    }
}

@Composable
private fun ConnectionStatus(state: ConnectionState) {
    when (state) {
        ConnectionState.Idle -> Text("未测试连接。")
        ConnectionState.Testing -> Text("正在连接……")
        is ConnectionState.Connected ->
            Text(
                "服务可用（AI 模式：${state.aiMode}）",
                color = MaterialTheme.colorScheme.primary,
            )
        is ConnectionState.Failed ->
            Text(
                "连接失败：${state.message}",
                color = MaterialTheme.colorScheme.error,
            )
    }
}
