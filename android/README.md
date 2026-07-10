# ToyWake Android

Kotlin + Jetpack Compose 单 Activity 应用。家长操作，儿童只接触现实玩具。

## 实际版本（已验证可构建）

| 组件 | 版本 |
|---|---|
| Android Gradle Plugin | 8.13.2 |
| Gradle | 9.0.0 |
| Kotlin | 2.1.20 |
| Compose BOM | 2024.12.01 |
| compileSdk / targetSdk / minSdk | 35 / 35 / 26 |
| JDK | 17 |

> 说明：2026 年中最新 AndroidX（core-ktx 1.19、lifecycle 2.11 等）要求 AGP 9.1+ 并涉及
> AGP 9.0 内置 Kotlin 迁移。为保证 Demo 稳定可复现，采用成熟的 AGP 8.x + 显式 Kotlin
> 插件栈 + 2024 年底 AndroidX 稳定版。详见 `docs/DECISIONS.md` D-007。

## 构建

```bash
# 确保 local.properties 指向 SDK（首次需配置）
# sdk.dir=C:\\Users\\ASUS\\AppData\\Local\\Android\\Sdk

cd android
./gradlew :app:assembleDebug          # 构建 Debug APK
./gradlew :app:testDebugUnitTest      # 运行单元测试
```

APK 产物：`app/build/outputs/apk/debug/app-debug.apk`

## 运行后端联调

1. 启动后端（见 `../backend/README.md`），监听 `0.0.0.0:8000`
2. 手机与开发电脑同一局域网
3. App 设置页填入后端地址，如 `http://192.168.1.100:8000`
4. 点击「测试连接」，成功会显示 AI 模式

## 模块结构

```
app/src/main/java/com/toywake/
├─ MainActivity.kt              # 单 Activity
├─ navigation/AppNavHost.kt     # 导航
├─ data/remote/                 # Retrofit API + DTO + ApiClient
├─ data/preferences/            # DataStore + UrlUtil
├─ data/repository/             # ToyWakeRepository
└─ ui/
   ├─ theme/                    # Compose 主题
   └─ settings/                 # 设置页 + ViewModel（连接测试）
```

## 测试

- `UrlUtilTest`：Base URL 校验与规整（8 项）
- 后续阶段补充 Tag UID 格式化、NFC 去重、ViewModel 状态流转等纯逻辑测试。
