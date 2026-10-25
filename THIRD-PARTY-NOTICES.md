# 原生分支依赖与来源

原生分支移除了 Flutter、原 APK/SO/DEX、模型、词典和原界面素材。直接依赖由 `android/gradle/libs.versions.toml` 以及各模块 Gradle 文件固定声明。

主要组件为 Kotlin / Coroutines / Serialization、AndroidX Compose / Material 3 / Lifecycle / Navigation / Room / Paging / DataStore / WorkManager / Core / DocumentFile，以及 Coil 的 Compose 模块（未添加网络模块）。测试依赖仅进入检查程序。第三方组件按各自随发布物提供的许可分发，本次没有联网重新审计全部传递依赖许可。

采集规则来源：参考分支 73 版的 15 组静态恢复处理器，按既有页面流程转为 Kotlin。`android/capture/rule-transcription-inventory.json` 保留源文件 SHA-256、方法和分支清单；这个清单是可追溯记录，不是运行行为验证或来源权利证明。自有应用架构、界面、数据库与业务实现不复用原界面资源或二进制。原仓库 LICENSE 保留，不能据此推断参考规则来源自动获得相同许可。

AndroidX UI 路径计算及 DataStore 共享计数包含其官方原生库（`libandroidx.graphics.path.so`、`libdatastore_shared_counter.so`），与原 APK 的 SO 无关。CI 对 DataStore 的 APK 文件和固定版本依赖 AAR 做逐 ABI 哈希比对并保存来源记录；不执行下载内容来检查来源。
