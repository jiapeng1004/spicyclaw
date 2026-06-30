import 'package:flutter/material.dart';

import '../api/spicyclaw_api.dart';
import '../models/chat_models.dart';

class ChatScreen extends StatefulWidget {
  const ChatScreen({
    super.key,
    required this.api,
    required this.sessionId,
    required this.sessions,
    required this.models,
    required this.selectedModelSlug,
    required this.onSelectSession,
    required this.onCreateSession,
    required this.onDeleteSession,
    required this.onModelChanged,
  });

  final SpicyclawApi api;
  final String sessionId;
  final List<ChatSession> sessions;
  final List<LlmModel> models;
  final String? selectedModelSlug;
  final ValueChanged<String> onSelectSession;
  final Future<void> Function() onCreateSession;
  final Future<void> Function(String id) onDeleteSession;
  final ValueChanged<String?> onModelChanged;

  @override
  State<ChatScreen> createState() => _ChatScreenState();
}

class _ChatScreenState extends State<ChatScreen> {
  final _input = TextEditingController();
  List<ChatMessage> _messages = [];
  var _streaming = false;
  var _streamText = '';
  var _loading = true;

  @override
  void initState() {
    super.initState();
    _loadMessages();
  }

  @override
  void didUpdateWidget(covariant ChatScreen oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (oldWidget.sessionId != widget.sessionId) {
      _loadMessages();
    }
  }

  @override
  void dispose() {
    _input.dispose();
    super.dispose();
  }

  Future<void> _loadMessages() async {
    setState(() => _loading = true);
    try {
      final messages = await widget.api.guard(
        () => widget.api.listMessages(widget.sessionId),
      );
      if (mounted) setState(() => _messages = messages);
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  Future<void> _send() async {
    final text = _input.text.trim();
    if (text.isEmpty || _streaming) return;
    _input.clear();
    setState(() {
      _streaming = true;
      _streamText = '';
    });
    try {
      await for (final chunk in widget.api.streamMessage(widget.sessionId, text)) {
        if (chunk.event == 'delta' || chunk.event == 'error') {
          setState(() => _streamText += chunk.data);
        }
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(e.toString())));
      }
    } finally {
      if (mounted) {
        setState(() {
          _streaming = false;
          _streamText = '';
        });
        await _loadMessages();
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        SizedBox(
          width: 280,
          child: Column(
            children: [
              if (widget.models.isNotEmpty)
                Padding(
                  padding: const EdgeInsets.all(12),
                  child: DropdownButton<String>(
                    isExpanded: true,
                    value: widget.selectedModelSlug,
                    hint: const Text('新对话模型'),
                    items: widget.models
                        .where((m) => m.enabled)
                        .map((m) => DropdownMenuItem(
                              value: m.slug,
                              child: Text('${m.displayName}${m.isDefault ? '（默认）' : ''}'),
                            ))
                        .toList(),
                    onChanged: widget.onModelChanged,
                  ),
                ),
              Padding(
                padding: const EdgeInsets.symmetric(horizontal: 12),
                child: FilledButton.icon(
                  onPressed: widget.onCreateSession,
                  icon: const Icon(Icons.add),
                  label: const Text('新对话'),
                ),
              ),
              Expanded(
                child: ListView.builder(
                  itemCount: widget.sessions.length,
                  itemBuilder: (context, index) {
                    final s = widget.sessions[index];
                    final active = s.id == widget.sessionId;
                    return ListTile(
                      selected: active,
                      title: Text(s.title, maxLines: 1, overflow: TextOverflow.ellipsis),
                      subtitle: s.modelRef == null
                          ? null
                          : Text(s.modelRef!, maxLines: 1, overflow: TextOverflow.ellipsis),
                      onTap: () => widget.onSelectSession(s.id),
                      trailing: IconButton(
                        icon: const Icon(Icons.close, size: 18),
                        onPressed: () => widget.onDeleteSession(s.id),
                      ),
                    );
                  },
                ),
              ),
            ],
          ),
        ),
        const VerticalDivider(width: 1),
        Expanded(
          child: Column(
            children: [
              Expanded(
                child: _loading
                    ? const Center(child: CircularProgressIndicator())
                    : ListView.builder(
                        padding: const EdgeInsets.all(16),
                        itemCount: _messages.length + (_streaming ? 1 : 0),
                        itemBuilder: (context, index) {
                          if (_streaming && index == _messages.length) {
                            return _Bubble(role: 'assistant', text: _streamText.isEmpty ? '思考中…' : _streamText);
                          }
                          final m = _messages[index];
                          return _Bubble(role: m.role, text: m.content);
                        },
                      ),
              ),
              Padding(
                padding: const EdgeInsets.all(12),
                child: Row(
                  children: [
                    Expanded(
                      child: TextField(
                        controller: _input,
                        minLines: 1,
                        maxLines: 4,
                        decoration: const InputDecoration(hintText: '输入任务…'),
                        onSubmitted: (_) => _send(),
                      ),
                    ),
                    const SizedBox(width: 8),
                    FilledButton(
                      onPressed: _streaming ? null : _send,
                      child: Text(_streaming ? '…' : '发送'),
                    ),
                  ],
                ),
              ),
            ],
          ),
        ),
      ],
    );
  }
}

class _Bubble extends StatelessWidget {
  const _Bubble({required this.role, required this.text});

  final String role;
  final String text;

  @override
  Widget build(BuildContext context) {
    final isUser = role == 'user';
    return Align(
      alignment: isUser ? Alignment.centerRight : Alignment.centerLeft,
      child: Container(
        margin: const EdgeInsets.only(bottom: 12),
        padding: const EdgeInsets.all(12),
        constraints: const BoxConstraints(maxWidth: 640),
        decoration: BoxDecoration(
          color: isUser
              ? Theme.of(context).colorScheme.surfaceContainerHighest
              : Theme.of(context).colorScheme.surfaceContainer,
          borderRadius: BorderRadius.circular(12),
          border: isUser ? null : Border(left: BorderSide(color: Theme.of(context).colorScheme.primary, width: 3)),
        ),
        child: Text(text),
      ),
    );
  }
}
