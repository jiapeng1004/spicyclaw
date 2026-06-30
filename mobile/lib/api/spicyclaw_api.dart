import 'dart:convert';

import 'package:dio/dio.dart';
import 'package:shared_preferences/shared_preferences.dart';

import '../config/app_config.dart';
import '../models/chat_models.dart';
import '../models/skill_models.dart';
import '../models/user.dart';

class SpicyclawApi {
  SpicyclawApi._(this._dio, this._prefs);

  static const _tokenKey = 'spicyclaw-access-token';
  static SpicyclawApi? _instance;

  static Future<SpicyclawApi> create() async {
    if (_instance != null) return _instance!;
    final prefs = await SharedPreferences.getInstance();
    final dio = Dio(BaseOptions(
      baseUrl: AppConfig.apiBaseUrl,
      connectTimeout: const Duration(seconds: 15),
      receiveTimeout: const Duration(minutes: 5),
      headers: {'Content-Type': 'application/json'},
    ));
    final api = SpicyclawApi._(dio, prefs);
    dio.interceptors.add(InterceptorsWrapper(
      onRequest: (options, handler) {
        final token = api._readToken();
        if (token != null && token.isNotEmpty) {
          options.headers['Authorization'] = 'Bearer $token';
        }
        handler.next(options);
      },
      onError: (error, handler) {
        if (error.response?.statusCode == 401) {
          api._clearToken();
        }
        handler.next(error);
      },
    ));
    _instance = api;
    return api;
  }

  final Dio _dio;
  final SharedPreferences _prefs;

  String? _readToken() => _prefs.getString(_tokenKey);

  Future<void> _saveToken(String token) => _prefs.setString(_tokenKey, token);

  Future<void> _clearToken() => _prefs.remove(_tokenKey);

  Future<User> login(String username, String password) async {
    final res = await _dio.post<Map<String, dynamic>>(
      '/auth/login',
      data: {'username': username, 'password': password},
    );
    final data = res.data!;
    final token = data['accessToken'] as String?;
    if (token == null || token.isEmpty) {
      throw Exception('登录响应缺少 accessToken');
    }
    await _saveToken(token);
    return User.fromJson(data);
  }

  Future<User> me() async {
    final res = await _dio.get<Map<String, dynamic>>('/auth/me');
    return User.fromJson(res.data!);
  }

  Future<void> logout() async {
    try {
      await _dio.post<void>('/auth/logout');
    } finally {
      await _clearToken();
    }
  }

  Future<List<ChatSession>> listSessions() async {
    final res = await _dio.get<List<dynamic>>('/chat/sessions');
    return res.data!.map((e) => ChatSession.fromJson(e as Map<String, dynamic>)).toList();
  }

  Future<ChatSession> createSession({String? modelSlug}) async {
    final res = await _dio.post<Map<String, dynamic>>(
      '/chat/sessions',
      data: modelSlug == null ? {} : {'modelSlug': modelSlug},
    );
    return ChatSession.fromJson(res.data!);
  }

  Future<void> deleteSession(String id) async {
    await _dio.delete<void>('/chat/sessions/$id');
  }

  Future<List<ChatMessage>> listMessages(String sessionId) async {
    final res = await _dio.get<List<dynamic>>('/chat/sessions/$sessionId/messages');
    return res.data!.map((e) => ChatMessage.fromJson(e as Map<String, dynamic>)).toList();
  }

  Stream<SseChunk> streamMessage(String sessionId, String content) async* {
    final response = await _dio.post<ResponseBody>(
      '/chat/sessions/$sessionId/stream',
      data: {'content': content},
      options: Options(responseType: ResponseType.stream, headers: {'Accept': 'text/event-stream'}),
    );
    final stream = response.data!.stream.map(utf8.decode);
    var buffer = '';
    await for (final chunk in stream) {
      buffer += chunk;
      final parts = buffer.split('\n\n');
      buffer = parts.removeLast();
      for (final part in parts) {
        var event = 'message';
        var data = '';
        for (final line in part.split('\n')) {
          if (line.startsWith('event:')) event = line.substring(6).trim();
          if (line.startsWith('data:')) data += line.substring(5).trim();
        }
        if (data.isNotEmpty) yield SseChunk(event: event, data: data);
      }
    }
  }

  Future<List<Skill>> listSkills() async {
    final res = await _dio.get<List<dynamic>>('/skills');
    return res.data!.map((e) => Skill.fromJson(e as Map<String, dynamic>)).toList();
  }

  Future<List<LlmModel>> listModels() async {
    final res = await _dio.get<List<dynamic>>('/models');
    return res.data!.map((e) => LlmModel.fromJson(e as Map<String, dynamic>)).toList();
  }

  String _errorMessage(DioException e) {
    final data = e.response?.data;
    if (data is Map && data['detail'] != null) return data['detail'].toString();
    return e.message ?? '请求失败';
  }

  Future<T> guard<T>(Future<T> Function() fn) async {
    try {
      return await fn();
    } on DioException catch (e) {
      throw Exception(_errorMessage(e));
    }
  }
}

class SseChunk {
  const SseChunk({required this.event, required this.data});
  final String event;
  final String data;
}
