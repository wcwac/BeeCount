# 离线版构建与发布

此分支维护 `wcwac/BeeCount` 的离线修改版。原作者为 sunxiao（TNT-Likely），
原始版权、许可证和作者信息继续保留；修改版源码公开，商业使用遵循原许可证。

## 当前状态

- 已移除全部云同步后端和同步引擎、AI/语音/截图识别、在线更新及联网汇率代码；
  Android 原生截图监听、分享识别及 APK 安装通道也已删除。
- 保留本地记账、统计、手动汇率、附件、提醒、桌面组件和文件导入导出。
  历史数据库表和迁移保留，以读取旧备份；旧共享账本镜像仅用于本地数据兼容。
- 界面使用标准 Material 底部导航与独立记账按钮；移除装饰皮肤、功能红点、
  推广入口和失效引导。账户编辑和首页提醒规则已提取到独立控制器。
- 配置和附件导出通过系统分享面板交由用户选择保存位置，不硬写公共 Download 目录。
- Android 使用独立应用 ID `io.github.wcwac.beecount.offline`。
  Kotlin namespace 保持原值，以兼容现有原生类和桌面组件类名。
- 与原版数据目录隔离。迁移需要先从原版导出，再导入离线版；请先验证备份。
- 本轮重构已通过源码分析（警告和提示暂不阻断）、437 项测试、签名 APK 编译
  及权限检查：[Actions #5](https://github.com/wcwac/BeeCount/actions/runs/39457643240)，
  对应源码提交 `1a5d71b`。1 项手动预览图片生成测试按原配置跳过。
  真机验证和 iOS 构建尚未完成。
- 相对上游，直接运行依赖从 64 项减至 40 项，本地子包从 10 个减至 0 个。
  根锁文件包数从 254 减至 184，SQLite 原生库、图片附件和本地通知等依赖保留。
  已核对 CI 实际生成的锁定文件和本地化代码与提交内容一致。
- 当前没有 Android 无障碍自动记账服务；周期记账和小组件手动入口继续保留。

## 手动触发

唯一保留的 workflow 是 `.github/workflows/release.yml`，名称为
**Offline Android**。普通 push、PR 和 tag 都不会触发它。
原有在线 AI 评测、Pullfrog 代理和 Issue 评论机器人已移除。

GitHub 的手动触发要求 workflow 文件存在于默认分支。首次设置可将
`offline` 设为仓库默认分支，或将修改合并到自己的 `main`；不要误运行
原版 `main` 上旧的 Release 流程。参见
[GitHub workflow_dispatch 文档](https://docs.github.com/en/actions/reference/workflows-and-actions/events-that-trigger-workflows#workflow_dispatch)。

获得仓库代码和第三方工具的执行授权后，在 Actions → Offline Android →
Run workflow 选择离线分支，并填写：

| 输入 | 默认值 | 含义 |
| --- | --- | --- |
| `version` | `0.1.0` | 三段数字版本号 |
| `sign_apk` | false | 使用自己的固定签名密钥 |
| `create_release` | false | 发布 GitHub 预发行版，要求启用签名 |

未签名运行用于首次编译验证，附件标有 `UNSIGNED-`，不能直接安装。
签名运行输出可安装的 APK。发布运行会创建
`offline-v<version>-<run_number>` 标签和预发行版，不覆盖已有 Release。
版本代码使用递增的 Actions run number；同一运行的重试使用相同版本代码。

## 签名配置

在仓库 Settings → Secrets and variables → Actions 中配置：

| Secret | 内容 |
| --- | --- |
| `ANDROID_KEYSTORE_BASE64` | 自己的 keystore 文件的单行 Base64 |
| `ANDROID_KEYSTORE_PASSWORD` | keystore 密码 |
| `ANDROID_KEY_ALIAS` | 签名密钥别名 |
| `ANDROID_KEY_PASSWORD` | 签名密钥密码 |

密钥必须长期保留并安全备份，后续版本使用同一个密钥。不要提交密钥或密码，
不要将 SSH 私钥用作 Android 签名密钥。此流程不会临时生成替代签名密钥。

若尚无 Android 签名密钥，可以在明确允许执行本机 JDK `keytool` 后，
在仓库外创建一个：

```sh
keytool -genkeypair -v -keystore beecount-offline.jks \
  -alias beecount-offline -keyalg RSA -keysize 3072 -validity 10000
```

此命令会交互询问密码和证书信息，不要把密码放在命令行中。此仓库已配置
固定签名和对应 Secrets；上述命令仅供新部署参考，不要重新生成密钥替换现有签名。

## 执行范围与验证

执行位置为 GitHub 托管的 Ubuntu runner。流程下载 Flutter 3.27.3、
Java 17、Android SDK/NDK 和 Pub/Gradle 依赖；使用已固定提交的
`actions/checkout`、`actions/setup-java`、`actions/upload-artifact`、
`actions/download-artifact` 和 `subosito/flutter-action`。
Gradle 通过仓库 wrapper 下载并执行。来源和版本写在 workflow 与
`android/gradle/wrapper/gradle-wrapper.properties`。

主要执行命令：

```sh
flutter pub get
flutter analyze --no-pub --no-fatal-infos --no-fatal-warnings
flutter test --no-pub
# 在仓库根目录，版本和构建号由 workflow 提供：
flutter build apk --release --flavor prod --no-pub \
  --build-name=<version> --build-number=<run_number>
```

构建后检查每个 APK：禁止 INTERNET、ACCESS_NETWORK_STATE、RECORD_AUDIO、
REQUEST_INSTALL_PACKAGES 权限；确认独立包名、`allowBackup=false`，
签名构建还验证 APK 签名。产物包含 APK、实际依赖锁定文件、许可证、
构建提交信息及 SHA-256 校验值。

执行构建需要联网下载工具和依赖，与成品应用的离线运行约束不同。
运行会消耗 Actions 配额，构建产物上传到 GitHub；只有选择
`create_release` 时才发布预发行版。CI 成功仍需真机检查本地记账、
备份恢复、桌面组件和网络行为。


## 0.2.0 验证记录

[Actions 构建 10](https://github.com/wcwac/BeeCount/actions/runs/39464878776)
从提交 `e47d808a01920aabf619752ed868dbf81b29f356` 构建，版本 `0.2.0`，
Android versionCode `10`。工作流格式化和本地化生成后的源码作为
`finance-sources-10` 保存，仓库随后同步该源码；同步不改变业务逻辑。

- 静态检查通过（旧代码的 warning/info 不作为失败条件）。
- 完整测试 476 项通过，1 项原有手动小组件预览生成测试跳过。
- 新测试覆盖报销、借贷、理财成本和盈亏、分期房贷、历史净资产、
  数据库升级、完整备份恢复/回滚、欢迎页四步完成/失败重试/窄屏大字号。
- 生成 ARM64、ARMv7、x86_64 和通用 APK，固定签名通过验证。
- 所有 APK 均确认没有 INTERNET、ACCESS_NETWORK_STATE、RECORD_AUDIO、
  REQUEST_INSTALL_PACKAGES 权限，独立应用 ID 和关闭系统备份的设置正确。
- 没有运行本机 Flutter、JDK、项目测试或构建；执行发生在已授权的 Actions。
- 尚未完成 Android 真机复测，iOS 未编译验证。

这版同时修复欢迎页从五页精简到四页后仍显示“下一步”的问题：末页按实际页数
显示“完成”；数据库初始化成功后才保存完成标记，失败可重试。
