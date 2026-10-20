import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:image_picker/image_picker.dart';

import '../ai/core/prompt_builder.dart';
import '../ai/providers/ai_provider_config.dart';
import '../ai/providers/ai_provider_manager.dart';
import '../l10n/app_localizations.dart';
import '../providers.dart';
import '../providers/ai_chat_providers.dart';
import '../services/ai/billing_image_service.dart';
import '../services/attachment_service.dart';
import '../services/billing/post_processor.dart';
import '../services/data/tag_seed_service.dart';
import '../widgets/ui/ui.dart';

/// 图片记账入口(相册/相机)。瘦身后:UI 流程 + 兜底,业务调 [AiBookkeeper]。
class ImageBillingHelper {
  /// 从相册选择图片并自动记账
  static Future<void> pickImageForBilling(
    BuildContext context,
    WidgetRef ref,
  ) =>
      _processImageBilling(context, ref, ImageSource.gallery);

  /// 打开相机拍照并自动记账
  static Future<void> openCameraForBilling(
    BuildContext context,
    WidgetRef ref,
  ) =>
      _processImageBilling(context, ref, ImageSource.camera);

  static Future<void> _processImageBilling(
    BuildContext context,
    WidgetRef ref,
    ImageSource source,
  ) async {
    return;
  }
}
