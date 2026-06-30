class ChatSession {
  const ChatSession({
    required this.id,
    required this.title,
    required this.agentName,
    this.modelRef,
    required this.createdAt,
    required this.updatedAt,
  });

  final String id;
  final String title;
  final String agentName;
  final String? modelRef;
  final String createdAt;
  final String updatedAt;

  factory ChatSession.fromJson(Map<String, dynamic> json) => ChatSession(
        id: json['id'] as String,
        title: json['title'] as String,
        agentName: json['agentName'] as String,
        modelRef: json['modelRef'] as String?,
        createdAt: json['createdAt'] as String,
        updatedAt: json['updatedAt'] as String,
      );
}

class ChatMessage {
  const ChatMessage({
    required this.id,
    required this.sessionId,
    required this.role,
    required this.content,
    required this.createdAt,
  });

  final String id;
  final String sessionId;
  final String role;
  final String content;
  final String createdAt;

  factory ChatMessage.fromJson(Map<String, dynamic> json) => ChatMessage(
        id: json['id'] as String,
        sessionId: json['sessionId'] as String,
        role: json['role'] as String,
        content: json['content'] as String,
        createdAt: json['createdAt'] as String,
      );
}

class LlmModel {
  const LlmModel({
    required this.slug,
    required this.displayName,
    required this.isDefault,
    required this.enabled,
  });

  final String slug;
  final String displayName;
  final bool isDefault;
  final bool enabled;

  factory LlmModel.fromJson(Map<String, dynamic> json) => LlmModel(
        slug: json['slug'] as String,
        displayName: json['displayName'] as String,
        isDefault: json['isDefault'] as bool? ?? false,
        enabled: json['enabled'] as bool? ?? true,
      );
}
