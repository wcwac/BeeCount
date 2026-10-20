import 'package:quick_actions/quick_actions.dart';
import 'app_link_service.dart';

/// Clears AI shortcuts left by an earlier online installation.
class QuickActionsService {
  static const actionImage = 'action_image';
  static const actionCamera = 'action_camera';
  static const actionVoice = 'action_voice';
  static const actionAiChat = 'action_ai_chat';
  final QuickActions _quickActions = const QuickActions();
  void Function(AppLinkAction action)? onNavigate;

  void initialize() { clearShortcutItems(); }
  void processPendingAction() {}

  Future<void> clearShortcutItems() async {
    try {
      await _quickActions.clearShortcutItems();
    } catch (_) {
      // Some platforms do not implement app icon shortcuts.
    }
  }
}
