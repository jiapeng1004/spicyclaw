import 'dart:convert';

import 'package:cookie_jar/cookie_jar.dart';
import 'package:dio/dio.dart';
import 'package:dio_cookie_manager/dio_cookie_manager.dart';
import 'package:path_provider/path_provider.dart';

import '../config/app_config.dart';
import '../models/chat_models.dart';
import '../models/skill_models.dart';
import '../models/user.dart';

class SpicyclawApi {
  SpicyclawApi._(this._dio);

  static SpicyclawApi? _instance;

  static Future<SpicyclawApi> create() async {
    if (_instance != null) return _instance!;
    final dir = await getApplicationDocumentsDirectory();
    final jar = PersistCookieJar(storage: FileStorage('${dir.path}/cookies'));
    final dio = Dio(BaseOptions(
      baseUrl: AppConfig.apiBaseUrl,
      connectTimeout: const Duration(seconds: 15),
      receiveTimeout: const Duration(minutes: 5),
      headers: {'Content-Type': 'application/json'},
    ));
    dio.interceptors.add(CookieManager(jar));
    _instance = SpicyclawApi._(dio);
    return _instance!;
  }

  final Dio _dio;

  Future<User> login(String username, String password) async {
    final res = await _dio.post<Map<String, dynamic>>(
      '/auth/login',
      data: {'username': username, 'password': password},
    );
    return User.fromJson(res.data!);
  }

  Future<User> me() async {
    final res = await _dio.get<Map<String, dynamic>>('/auth/me');
    return User.fromJson(res.data!);
  }

  Future<void> logout() async {
    await _dio.post<void>('/auth/logout');
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
