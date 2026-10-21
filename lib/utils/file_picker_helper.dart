import 'dart:typed_data';

import 'package:file_picker/file_picker.dart';
import 'package:flutter/services.dart';

import '../services/system/logger_service.dart';

/// 文件选择器辅助工具类
///
/// 提供对 FilePicker 的封装，处理部分 Android 设备不支持扩展名过滤的问题。
class FilePickerHelper {
  FilePickerHelper._();

  /// Read a picked file with a byte cap even when its provider omits or
  /// understates the length. Unknown lengths are streamed instead of eagerly
  /// loading the whole file to determine its size.
  static Future<Uint8List> readBytes(
    PlatformFile file, {
    required int maxBytes,
  }) async {
    if (maxBytes < 0) throw ArgumentError.value(maxBytes, 'maxBytes');
    final knownLength = file.lengthSync();
    if (knownLength != null && knownLength > maxBytes) {
      throw const FormatException('文件超过大小限制 / File exceeds size limit');
    }
    final buffer = BytesBuilder(copy: false);
    await for (final chunk in file.readAsByteStream()) {
      if (buffer.length + chunk.length > maxBytes) {
        throw const FormatException('文件超过大小限制 / File exceeds size limit');
      }
      buffer.add(chunk);
    }
    return buffer.takeBytes();
  }

  /// 选择指定扩展名的文件
  ///
  /// [allowedExtensions] - 允许的文件扩展名列表（不含点号），如 ['yml', 'yaml']
  /// [validateExtension] - 是否在选择后验证扩展名（默认 true）
  ///
  /// 部分 Android 设备不支持扩展名过滤，会抛出 PlatformException。
  /// 此方法会自动 fallback 到选择任意文件，然后手动验证扩展名。
  ///
  /// 如果用户取消选择，返回 null。
  /// 如果选择的文件扩展名不匹配且 [validateExtension] 为 true，抛出 [FileExtensionException]。
  static Future<PlatformFile?> pickFileWithExtensions({
    required List<String> allowedExtensions,
    bool validateExtension = true,
  }) async {
    PlatformFile? result;

    try {
      // 尝试使用扩展名过滤
      result = await FilePicker.pickFile(
        type: FileType.custom,
        allowedExtensions: allowedExtensions,
      );
    } on PlatformException catch (e) {
      // 设备不支持扩展名过滤，fallback 到选择任意文件
      logger.warning('FilePickerHelper', '设备不支持扩展名过滤，fallback 到选择任意文件: $e');
      result = await FilePicker.pickFile(type: FileType.any);
    }

    // 用户取消选择
    if (result == null) {
      return null;
    }

    // 验证扩展名
    if (validateExtension) {
      final fileName = result.name.toLowerCase();
      final hasValidExtension = allowedExtensions.any(
        (ext) => fileName.endsWith('.${ext.toLowerCase()}'),
      );

      if (!hasValidExtension) {
        throw FileExtensionException(
          expectedExtensions: allowedExtensions,
          actualPath: result.path ?? result.name,
        );
      }
    }

    return result;
  }

  /// 选择 YAML 配置文件 (.yml, .yaml)
  static Future<PlatformFile?> pickYamlFile() async {
    return pickFileWithExtensions(
      allowedExtensions: ['yml', 'yaml'],
      validateExtension: true,
    );
  }

  /// 选择压缩归档文件 (.gz, .tar, .tar.gz)
  static Future<PlatformFile?> pickArchiveFile() async {
    return pickFileWithExtensions(
      allowedExtensions: ['gz', 'tar'],
      validateExtension: true,
    );
  }
}

/// 文件扩展名不匹配异常
class FileExtensionException implements Exception {
  final List<String> expectedExtensions;
  final String actualPath;

  FileExtensionException({
    required this.expectedExtensions,
    required this.actualPath,
  });

  @override
  String toString() {
    final extensionsText = expectedExtensions.map((e) => '.$e').join(', ');
    return '请选择以下格式的文件：$extensionsText';
  }
}
