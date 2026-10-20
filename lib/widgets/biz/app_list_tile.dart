import 'package:flutter/material.dart';

/// Standard settings row with Material focus, semantics and tap targets.
class AppListTile extends StatelessWidget {
  final IconData leading;
  final Widget? leadingWidget;
  final String title;
  final String? subtitle;
  final VoidCallback? onTap;
  final bool enabled;
  final Widget? trailing;

  const AppListTile({
    super.key,
    required this.leading,
    this.leadingWidget,
    required this.title,
    this.subtitle,
    this.onTap,
    this.enabled = true,
    this.trailing,
  });

  @override
  Widget build(BuildContext context) => ListTile(
        contentPadding: EdgeInsets.zero,
        leading: leadingWidget ?? Icon(leading),
        title: Text(title),
        subtitle: subtitle == null ? null : Text(subtitle!),
        enabled: enabled,
        onTap: onTap,
        trailing: trailing ??
            (enabled && onTap != null ? const Icon(Icons.chevron_right) : null),
      );
}
