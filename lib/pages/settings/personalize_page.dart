import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../l10n/app_localizations.dart';
import '../../providers/theme_providers.dart';

class PersonalizePage extends ConsumerWidget {
  const PersonalizePage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final primary = ref.watch(primaryColorProvider);
    final l10n = AppLocalizations.of(context);
    final options = <_ThemeOption>[
      _ThemeOption(l10n.personalizeThemeHoney, const Color(0xFFF8C91C)),
      _ThemeOption(l10n.personalizeThemeOrange, const Color(0xFFFF7043)),
      _ThemeOption(l10n.personalizeThemeGreen, const Color(0xFF26A69A)),
      _ThemeOption(l10n.personalizeThemePurple, const Color(0xFF7E57C2)),
      _ThemeOption(l10n.personalizeThemePink, const Color(0xFFE91E63)),
      _ThemeOption(l10n.personalizeThemeBlue, const Color(0xFF2196F3)),
      _ThemeOption(l10n.personalizeThemeMint, const Color(0xFF80CBC4)),
      _ThemeOption(l10n.personalizeThemeSand, const Color(0xFFFFCC80)),
      _ThemeOption(l10n.personalizeThemeLavender, const Color(0xFFB39DDB)),
      _ThemeOption(l10n.personalizeThemeSky, const Color(0xFF90CAF9)),
      // 新增注意色系
      _ThemeOption(l10n.personalizeThemeWarmOrange, const Color(0xFFFF8A65)),
      _ThemeOption(l10n.personalizeThemeMintGreen, const Color(0xFF4DB6AC)),
      _ThemeOption(l10n.personalizeThemeRoseGold, const Color(0xFFAD7A99)),
      _ThemeOption(l10n.personalizeThemeDeepBlue, const Color(0xFF1565C0)),
      _ThemeOption(l10n.personalizeThemeMapleRed, const Color(0xFFD32F2F)),
      _ThemeOption(l10n.personalizeThemeEmerald, const Color(0xFF388E3C)),
      _ThemeOption(l10n.personalizeThemeLavenderPurple, const Color(0xFF9575CD)),
      _ThemeOption(l10n.personalizeThemeAmber, const Color(0xFFFFA726)),
      _ThemeOption(l10n.personalizeThemeRouge, const Color(0xFFC2185B)),
      _ThemeOption(l10n.personalizeThemeIndigo, const Color(0xFF3F51B5)),
      _ThemeOption(l10n.personalizeThemeOlive, const Color(0xFF689F38)),
      _ThemeOption(l10n.personalizeThemeCoral, const Color(0xFFFF8A80)),
      _ThemeOption(l10n.personalizeThemeDarkGreen, const Color(0xFF2E7D32)),
      _ThemeOption(l10n.personalizeThemeViolet, const Color(0xFF673AB7)),
      _ThemeOption(l10n.personalizeThemeSunset, const Color(0xFFFF5722)),
      _ThemeOption(l10n.personalizeThemePeacock, const Color(0xFF00ACC1)),
      _ThemeOption(l10n.personalizeThemeLime, Colors.lime),
    ];

    return Scaffold(
      appBar: AppBar(title: Text(l10n.personalizeTitle)),
      body: ListView(
        children: [
          for (final option in options)
            RadioListTile<Color>(
              value: option.color,
              groupValue: primary,
              title: Text(option.name),
              secondary: Icon(Icons.circle, color: option.color),
              onChanged: (color) {
                if (color != null) {
                  ref.read(primaryColorProvider.notifier).state = color;
                }
              },
            ),
          ListTile(
            leading: const Icon(Icons.colorize),
            title: Text(l10n.personalizeCustomTitle),
            trailing: const Icon(Icons.chevron_right),
            onTap: () => showDialog<void>(
              context: context,
              builder: (dialogContext) => AlertDialog(
                title: Text(l10n.personalizeCustomTitle),
                content: SingleChildScrollView(
                  child: _ColorPicker(
                    initialColor: primary,
                    onColorSelected: (color) {
                      ref.read(primaryColorProvider.notifier).state = color;
                      Navigator.of(dialogContext).pop();
                    },
                  ),
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }
}

class _ThemeOption {
  final String name;
  final Color color;
  const _ThemeOption(this.name, this.color);
}

class _ColorPicker extends StatefulWidget {
  final Color initialColor;
  final ValueChanged<Color> onColorSelected;
  const _ColorPicker({required this.initialColor, required this.onColorSelected});

  @override
  State<_ColorPicker> createState() => _ColorPickerState();
}

class _ColorPickerState extends State<_ColorPicker> {
  late HSVColor _color = HSVColor.fromColor(widget.initialColor);

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context);
    final color = _color.toColor();
    final hex = (color.value & 0xFFFFFF).toRadixString(16).padLeft(6, '0').toUpperCase();
    return SizedBox(
      width: 320,
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          ListTile(
            leading: Icon(Icons.circle, color: color),
            title: Text('#$hex'),
          ),
          Text(l10n.personalizeHue(_color.hue.round())),
          Slider(
            value: _color.hue,
            max: 360,
            onChanged: (value) => setState(() => _color = _color.withHue(value)),
          ),
          Text(l10n.personalizeSaturation((_color.saturation * 100).round())),
          Slider(
            value: _color.saturation,
            onChanged: (value) =>
                setState(() => _color = _color.withSaturation(value)),
          ),
          Text(l10n.personalizeBrightness((_color.value * 100).round())),
          Slider(
            value: _color.value,
            onChanged: (value) => setState(() => _color = _color.withValue(value)),
          ),
          FilledButton(
            onPressed: () => widget.onColorSelected(color),
            child: Text(l10n.personalizeSelectColor),
          ),
        ],
      ),
    );
  }
}
