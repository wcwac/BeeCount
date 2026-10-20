import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_markdown_plus/flutter_markdown_plus.dart';

import '../../l10n/app_localizations.dart';
import '../../styles/tokens.dart';
import 'primary_header.dart';

/// Bundled documentation only. Remote images and external links are disabled.
class LocalDocumentPage extends StatefulWidget {
  const LocalDocumentPage({
    super.key,
    required this.title,
    required this.assetPath,
  });

  final String title;
  final String assetPath;

  @override
  State<LocalDocumentPage> createState() => _LocalDocumentPageState();
}

class _LocalDocumentPageState extends State<LocalDocumentPage> {
  late Future<String> _document;

  @override
  void initState() {
    super.initState();
    _document = rootBundle.loadString(widget.assetPath);
  }

  @override
  void didUpdateWidget(covariant LocalDocumentPage oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (oldWidget.assetPath != widget.assetPath) {
      _document = rootBundle.loadString(widget.assetPath);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: BeeTokens.scaffoldBackground(context),
      body: Column(
        children: [
          PrimaryHeader(title: widget.title, showBack: true, compact: true),
          Expanded(
            child: FutureBuilder<String>(
              future: _document,
              builder: (context, snapshot) {
                if (snapshot.hasError) {
                  return Center(
                    child: Text(AppLocalizations.of(context).helpCenterLoadFailed),
                  );
                }
                if (!snapshot.hasData) {
                  return const Center(child: CircularProgressIndicator());
                }
                return Markdown(
                  data: snapshot.data!,
                  selectable: true,
                  imageBuilder: (uri, title, alt) => const SizedBox.shrink(),
                );
              },
            ),
          ),
        ],
      ),
    );
  }
}
