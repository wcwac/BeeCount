# 完整本地数据库备份

在“我的 → 数据管理”选择“备份全部账务数据”，使用系统分享面板将 JSON 文件保存到自己选择的位置。文件最大 100 MB。

备份包含数据库中的全部普通数据表，包括账本、账户、分类、流水、标签、预算、周期账单、附件元数据，以及报销、借贷、分期、房贷、理财的计划、事件与关联流水，以及采集确认关联和流水删除后的防重复标记。记录 ID 和数据库原始时间值保持不变。旧版数据库中的历史数据也会保留。

备份不包含应用偏好、应用锁设置、云端或 AI 凭据、原生采集收件箱中的待确认记录及采集开关/丢失提示计数，也不包含图片、附件及自定义图标文件。图片等文件请同时使用现有的“导出附件”功能另行保存。JSON 内容未经加密，应存放在自己信任的位置。

恢复时选择“从完整备份恢复”。程序先检查格式、表结构和财务引用，再要求确认覆盖。**恢复会替换全部本地账务数据，不是合并。请先备份当前数据。** 恢复失败会回滚整个数据库事务，现有账务数据保持原状；应用偏好和附件文件始终不由该操作更改。

导出使用备份格式版本 1、数据库结构版本 35；恢复支持结构版本 34 和 35，旧版 34 备份恢复时采集确认关联表为空。CSV/YAML 是其他导入导出格式，不能作为完整数据库 JSON 恢复。恢复数据库后，再通过“导入附件”恢复对应图片文件。数据库索引与财务保护触发器保留，历史全文检索缓存由恢复后的记录重建。

# Full local database backup

Choose **My → Data management → Back up all financial data**, then save the JSON using the system share sheet. The maximum file size is 100 MB.

The backup includes all ordinary database tables, including ledgers, accounts, transactions, categories, tags, budgets, recurring bills, attachment metadata, finance plans, events and linked transactions, plus confirmed capture links and markers that prevent deleted transactions from being recreated by a capture retry. IDs and raw database timestamps are preserved.

Preferences, app-lock settings, cloud/AI credentials, pending native capture drafts, capture settings/loss counters and attachment/image files are excluded. Export attachment files separately. JSON backups are not encrypted; keep them in a trusted location.

Restoring replaces all financial data rather than merging it. Back up the current database before confirming. Validation precedes replacement, and a failed database restore rolls back the complete transaction. Restore the matching attachment archive separately afterwards.

Exports use backup format version 1 and database schema version 35. Restoring supports schema versions 34 and 35; legacy version 34 backups restore with an empty capture-link table. CSV and YAML files are not full database backups.
