import 'package:flutter_riverpod/flutter_riverpod.dart';

/// Legacy automation routes cannot start recognition or read incoming files.
class AutoBillingService {
  AutoBillingService(ProviderContainer container);

  Future<int?> processScreenshot(String imagePath, {
    String? recognitionImagePath,
    bool showNotification = true,
  }) async => null;

  Future<int?> processText(String text, {bool showNotification = true}) async => null;

  void dispose() {}
}
