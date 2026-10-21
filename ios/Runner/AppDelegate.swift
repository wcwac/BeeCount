import Flutter
import UIKit
import UserNotifications

@main
@objc class AppDelegate: FlutterAppDelegate, FlutterImplicitEngineDelegate {
  override func application(
    _ application: UIApplication,
    didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
  ) -> Bool {
    excludeLocalDataFromBackup()

    // 设置通知中心代理
    if #available(iOS 10.0, *) {
      UNUserNotificationCenter.current().delegate = self
    }

    return super.application(application, didFinishLaunchingWithOptions: launchOptions)
  }

  // UIScene creates the storyboard's Flutter engine after application launch.
  // Register plugins and channels against that engine without accessing a window.
  func didInitializeImplicitFlutterEngine(_ engineBridge: FlutterImplicitEngineBridge) {
    GeneratedPluginRegistrant.register(with: engineBridge.pluginRegistry)

    let loggerChannel = FlutterMethodChannel(
      name: "com.beecount.logger",
      binaryMessenger: engineBridge.applicationRegistrar.messenger()
    )
    LoggerPlugin.setup(channel: loggerChannel)

    // 测试日志
    LoggerPlugin.info(tag: "AppDelegate", message: "日志系统已初始化")
  }

  func handleSceneBecameActive() {
    excludeLocalDataFromBackup()
    clearNotificationBadge()
  }

  /// Offline bookkeeping data must not enter automatic iCloud device backups.
  private func excludeLocalDataFromBackup() {
    let manager = FileManager.default
    var directories = manager.urls(for: .documentDirectory, in: .userDomainMask)
    directories += manager.urls(for: .libraryDirectory, in: .userDomainMask)
    if let group = manager.containerURL(
      forSecurityApplicationGroupIdentifier: "group.com.tntlikely.beecount"
    ) {
      directories.append(group)
    }
    for var directory in directories {
      do {
        var values = URLResourceValues()
        values.isExcludedFromBackup = true
        try directory.setResourceValues(values)
      } catch {
        NSLog("Unable to exclude local data from backup: %@", error.localizedDescription)
      }
    }
  }

  private func clearNotificationBadge() {
    if #available(iOS 16.0, *) {
      UNUserNotificationCenter.current().setBadgeCount(0) { error in
        if let error = error {
          LoggerPlugin.info(tag: "AppDelegate", message: "清除通知角标失败: \(error.localizedDescription)")
        }
      }
    } else {
      UIApplication.shared.applicationIconBadgeNumber = 0
    }
  }

  // 前台显示通知
  @available(iOS 10.0, *)
  override func userNotificationCenter(
    _ center: UNUserNotificationCenter,
    willPresent notification: UNNotification,
    withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void
  ) {
    // 前台保留横幅和声音，避免正在使用 App 时重新留下未读角标。
    if #available(iOS 14.0, *) {
      completionHandler([.banner, .sound])
    } else {
      completionHandler([.alert, .sound])
    }
  }

  // 处理通知点击
  @available(iOS 10.0, *)
  override func userNotificationCenter(
    _ center: UNUserNotificationCenter,
    didReceive response: UNNotificationResponse,
    withCompletionHandler completionHandler: @escaping () -> Void
  ) {
    clearNotificationBadge()
    completionHandler()
  }
}

// Keep the class in this compiled source file so the scene is included in every
// Runner configuration without modifying Xcode's source-file build phases.
class SceneDelegate: FlutterSceneDelegate {
  override func sceneDidBecomeActive(_ scene: UIScene) {
    super.sceneDidBecomeActive(scene)
    (UIApplication.shared.delegate as? AppDelegate)?.handleSceneBecameActive()
  }
}
