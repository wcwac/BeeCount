# BeeCount 原生离线版

Android 8.0+ 的独立 Kotlin 应用。Compose + Material 3 界面、Room 新数据库；默认从空账本开始，可与 73 版对照安装。此分支是 `feat/kotlin-compose-offline`，不兼容 Flutter/一木数据库。

- 账单、资产、统计、更多四栏；分类标签、预算目标、附件、周期与投资日志。
- 22 个包名、15 组采集规则；先保存候选，再显示确认浮窗。自动采集和自动保存默认关闭。
- 单一事务写账入口、整数最小货币单位、全局账户、可重试操作回执。
- 自有 CSV 和 ZIP 数据保护、自动备份、系统认证应用锁、桌面快捷入口。
- 无网络权限，无 AI/分类模型、会员、广告或远程配置。

实现与验证边界见 [实施清单](docs/NATIVE_IMPLEMENTATION.md)，架构见 [设计说明](docs/NATIVE_DESIGN.md)。

当前预发行版：[native-v0.1.0-81](https://github.com/wcwac/BeeCount/releases/tag/native-v0.1.0-81) · [下载 APK（约 2.11 MB）](https://github.com/wcwac/BeeCount/releases/download/native-v0.1.0-81/native-v0.1.0-81.apk)。新包名与 73 版并行安装，默认关闭自动采集与自动保存；真实支付应用兼容性仍需分别验证。

## 构建与交付

只通过 GitHub Actions 的 `release.yml` 手动验证。本地不安装工具、不编译、不执行测试。流水线一次完成 Kotlin 编译、关键业务检查、lint、设备采集、界面截图、Release 启动检查；全部通过后发布同一个签名 APK。版本标签独立使用 `native-v...`。

开发模块：`android/domain`、`android/data`、`android/capture`、`android/ui`、`android/app`。`captureFixture` 只提供云端合成支付页面，不包含在应用中。

旧分支 `feat/yimu-reference-rebuild` 和 [73 版](https://github.com/wcwac/BeeCount/releases/tag/yimu-reference-v0.1.0-73) 保留用于行为对照。
