# 第三方组件声明 / Third-Party Notices

本仓库源码**不包含（vendor）任何第三方库的源代码**。所有第三方依赖由 `pubspec.yaml` / `pubspec.lock` 声明，在构建时经 pub 从官方源获取，各自按其原始开源协议授权，与本项目的 [LICENSE](LICENSE)（双许可）相互独立。使用、分发或基于本项目二次开发时，请一并遵守下列组件各自的协议。

当前直接生产依赖清单与 `pubspec.yaml` 对齐（2026-10-22）：36 个 hosted 包及 2 个 Flutter SDK 包。既有组件协议分类沿用原核验记录；新增 `flutter_markdown_plus 1.0.12` 已核对其正式发布物的 BSD-3-Clause LICENSE。本次版本迁移不代表对所有新版及传递依赖完成了新的逐项协议审计，分发时以锁定发布物的 LICENSE 为准。

The direct production list below matches `pubspec.yaml` as of 2026-10-22: 36 hosted packages and two Flutter SDK packages. Existing license classifications are retained from the earlier review; the newly listed `flutter_markdown_plus 1.0.12` was checked against its published BSD-3-Clause LICENSE. This migration is not a fresh audit of every updated or transitive package; the locked distributions' licenses prevail.

## 直接依赖 / Direct dependencies

**Flutter SDK / flutter_localizations** — BSD-3-Clause

**MIT（15）**

archive · country_flags · csv · drift · excel · file_picker · fl_chart · flutter_image_compress · flutter_list_view · flutter_riverpod · flutter_svg · gbk_codec · reorderable_grid_view · uuid · yaml

**BSD-3-Clause（16）**

collection · crypto · flutter_local_notifications · flutter_markdown_plus · gal · home_widget · image_cropper · intl · local_auth · package_info_plus · path · path_provider · qr_flutter · share_plus · shared_preferences · visibility_detector

**Apache-2.0（4）**
app_links · decimal · image_picker · table_calendar

**BSD-2-Clause（1）**
timezone

---

如发现本清单与上游实际协议不符，欢迎提 Issue 指正。
