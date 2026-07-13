package com.toywake.ui.bind

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
fun BindScreen(
    onBack: () -> Unit,
    onBound: (Int) -> Unit,
    vm: BindViewModel = viewModel(),
) {
    val name by vm.name.collectAsState()
    val toyType by vm.toyType.collectAsState()
    val trait by vm.trait.collectAsState()
    val childSetting by vm.childSetting.collectAsState()
    val saving by vm.saving.collectAsState()
    val error by vm.error.collectAsState()
    val canSubmit by vm.canSubmit.collectAsState()
    val boundToyId by vm.boundToyId.collectAsState()

    // 绑定成功 -> 开始游戏
    LaunchedEffect(boundToyId) {
        boundToyId?.let { onBound(it) }
    }

    var typeExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("绑定玩具") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("标签：${vm.tagUidLabel()}", style = MaterialTheme.typography.bodySmall)
            OutlinedTextField(
                value = name,
                onValueChange = { if (it.length <= 20) vm.name.value = it },
                label = { Text("玩具名字 *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            ExposedDropdownMenuBox(
                expanded = typeExpanded,
                onExpandedChange = { typeExpanded = it },
            ) {
                OutlinedTextField(
                    value = toyType,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("玩具类型 *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(typeExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                )
                DropdownMenu(
                    expanded = typeExpanded,
                    onDismissRequest = { typeExpanded = false },
                ) {
                    TOY_TYPES.forEach { t ->
                        DropdownMenuItem(text = { Text(t) }, onClick = {
                            vm.toyType.value = t
                            typeExpanded = false
                        })
                    }
                }
            }
            OutlinedTextField(
                value = trait,
                onValueChange = { if (it.length <= 20) vm.trait.value = it },
                label = { Text("简单特点（选填）") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = childSetting,
                onValueChange = { if (it.length <= 50) vm.childSetting.value = it },
                label = { Text("孩子设定（选填）") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 2,
            )
            Button(
                onClick = { vm.submit() },
                enabled = canSubmit,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (saving) "保存中……" else "保存并叫醒")
            }
            error?.let {
                Text("保存失败：$it", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
