import 'dart:convert';
import 'dart:io';

import 'package:agentcore/agentcore.dart';
import 'package:dio/dio.dart';
import 'package:flutter/foundation.dart' show visibleForTesting;
import 'legacy_provider_error.dart' show OpenAIException;

import 'ai_provider_config.dart';

/// Offline-only implementation. No client is created, no file is read, and no
/// stored API key can enable an upload, including capability validation/probes.
class AIProviderFactory {
  AIProviderFactory._();
  static const offlineMessage = '离线版已禁用 AI、图片识别和语音上传。';

  static Future<String> chat(String prompt, {
    String? systemPrompt,
    double temperature = 0.7,
    String? logTag,
  }) async => throw AIException(offlineMessage);

  static Stream<Map<String, dynamic>> chatWithToolsStream({
    required List<Map<String, dynamic>> messages,
    required List<Map<String, dynamic>> tools,
    String? logTag,
  }) async* { throw AIException(offlineMessage); }

  static Stream<Map<String, dynamic>> chatWithToolsStreamForConfig({
    required AIServiceProviderConfig config,
    required List<Map<String, dynamic>> messages,
    required List<Map<String, dynamic>> tools,
    String? logTag,
    Dio? client,
  }) async* { throw AIException(offlineMessage); }

  static Future<String> vision(File image, String prompt, {String? logTag}) async =>
      throw AIException(offlineMessage);

  static Future<String> speechToText(File audio, {String? logTag}) async =>
      throw AIException(offlineMessage);

  static Future<(bool success, String? error)> validateConfig({String? logTag}) async =>
      (false, offlineMessage);

  static Future<(bool success, String? error)> validateProvider(
    AIServiceProviderConfig config, {String? logTag}
  ) async => (false, offlineMessage);

  static Future<(bool success, String? error)> validateTextCapability(
    AIServiceProviderConfig config, {
    String? logTag,
    void Function(AgentModelCapabilities capabilities)? onCapabilities,
    Dio? client,
  }) async => (false, offlineMessage);

  static Future<AgentModelCapabilities> probeAgentCapabilities(
    AIServiceProviderConfig config, {
    bool probeStreaming = false,
    String? logTag,
    Dio? client,
  }) async => AgentModelCapabilities(
    text: AgentCapabilitySupport.unsupported,
    nativeToolCalls: AgentCapabilitySupport.unsupported,
    detail: offlineMessage,
  );

  static Future<(bool success, String? error)> validateVisionCapability(
    AIServiceProviderConfig config, {String? logTag}
  ) async => (false, offlineMessage);

  static Future<(bool success, String? error)> validateSpeechCapability(
    AIServiceProviderConfig config, {String? logTag}
  ) async => (false, offlineMessage);

  static String userFacingError(
    Object error, {
    String prefix = '验证失败',
  }) {
    final String detail;
    if (error is AIException) {
      detail = error.message;
    } else if (error is OpenAIException) {
      detail = error.userMessage;
    } else if (error is DioException) {
      detail = OpenAIException.fromDioException(error).userMessage;
    } else {
      detail = '服务商返回了无法识别的响应，请检查模型名称和接口兼容性';
    }
    return '$prefix：$detail';
  }

  /// Classifies only explicit provider signals; a generic occurrence of the
  /// word "tool" is not enough to label a model as incompatible.
  @visibleForTesting
  static AIExceptionCode classifyProviderError(
    int? statusCode,
    Object? responseData, {
    bool toolRequest = false,
  }) {
    final normalized = responseData is String
        ? responseData.toLowerCase()
        : jsonEncode(responseData).toLowerCase();
    if (statusCode == 401 || statusCode == 403) {
      return AIExceptionCode.unauthorized;
    }
    if (statusCode == 429) return AIExceptionCode.rateLimited;
    const nativeToolSignals = <String>[
      'does not support tools',
      'tools are not supported',
      'tool calls are not supported',
      'function calling is not supported',
      'function_call is not supported',
      'unsupported parameter: tools',
      'unsupported field: tools',
      'unknown field: tools',
      'unrecognized request argument supplied: tools',
      '不支持原生工具',
      '不支持工具调用',
      '不支持 function calling',
    ];
    if (nativeToolSignals.any(normalized.contains)) {
      return AIExceptionCode.nativeToolsUnsupported;
    }
    const streamingSignals = <String>[
      'stream is not supported',
      'streaming is not supported',
      'unsupported parameter: stream',
      '不支持流式',
    ];
    if (streamingSignals.any(normalized.contains)) {
      return AIExceptionCode.streamingUnsupported;
    }
    if (toolRequest &&
        (statusCode == 404 || statusCode == 405) &&
        (normalized.contains('function calling') ||
            normalized.contains('tool calls'))) {
      return AIExceptionCode.nativeToolsUnsupported;
    }
    return AIExceptionCode.unknown;
  }

  static const _requiredChatKeys = {'model', 'messages', 'stream'};
  static String? rejectedChatParam(
    Map<String, dynamic> payload,
    int? statusCode,
    String errorBody,
  ) {
    if (statusCode == null || statusCode < 400) return null;
    final low = errorBody.toLowerCase();
    for (final key in payload.keys) {
      if (!_requiredChatKeys.contains(key) && low.contains(key.toLowerCase())) {
        return key;
      }
    }
    return null;
  }

}

enum AIExceptionCode {
  unknown,
  nativeToolsUnsupported,
  streamingUnsupported,
  invalidResponse,
  unauthorized,
  rateLimited,
}

/// AI 异常
class AIException implements Exception {
  final String message;
  final AIExceptionCode code;

  AIException(this.message, {this.code = AIExceptionCode.unknown});

  @override
  String toString() => message;
}
