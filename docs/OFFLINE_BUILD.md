# 离线版构建与发布

此分支维护 `wcwac/BeeCount` 的离线修改版。原作者为 sunxiao（TNT-Likely），
原始版权、许可证和作者信息继续保留；修改版源码公开，商业使用遵循原许可证。

## 当前状态

- 已禁用联网功能，移除 Supabase、WebDAV、S3、iCloud 后端包及 AI 服务商包。
- 保留本地记账、统计、导入导出和备份，以及部分供旧代码引用的禁用接口。
- Android 使用独立应用 ID `io.github.wcwac.beecount.offline`。
  Kotlin namespace 保持原值，以兼容现有原生类和桌面组件类名。
- 与原版数据目录隔离。迁移需要先从原版导出，再导入离线版；请先验证备份。
- 当前只完成静态检查。尚未执行依赖解析、分析器、测试、构建或真机验证。
- 锁定文件尚未重新解析；首次获准构建后，必须检查解析差异并将新的
  `pubspec.lock` 提交到分支。CI 会将实际使用的锁定文件随 APK 一起保存。

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

此命令会交互询问密码和证书信息，不要把密码放在命令行中。生成和上传
Secrets 需另行完成；仓库准备过程没有替你执行。

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
flutter test --no-pub test/offline \
  test/ai/providers/ai_provider_factory_test.dart \
  test/pages/ai/ai_chat_page_lifecycle_test.dart \
  test/services/exchange_rate_service_test.dart \
  test/utils/website_urls_test.dart
# 在 packages/flutter_cloud_sync 目录：
flutter pub get
flutter test --no-pub test/auth_privacy_test.dart
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
