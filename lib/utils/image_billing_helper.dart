import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

/// Compatibility entry points; local attachments use AttachmentService instead.
class ImageBillingHelper {
  static Future<void> pickImageForBilling(BuildContext context, WidgetRef ref) async {}
  static Future<void> openCameraForBilling(BuildContext context, WidgetRef ref) async {}
}
