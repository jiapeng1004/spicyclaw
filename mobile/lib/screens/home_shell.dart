import 'package:flutter/material.dart';

import '../api/spicyclaw_api.dart';
import '../models/chat_models.dart';
import '../models/user.dart';
import 'chat_screen.dart';
import 'skills_screen.dart';

class HomeShell extends StatefulWidget {
  const HomeShell({super.key, required this.api, required this.user, required this.onLogout});

  final SpicyclawApi api;
  final User user;
  final VoidCallback onLogout;

  @override
  State<HomeShell> createState() => _HomeShellState();
}

class _HomeShellState extends State<HomeShell> {
  var _tab = 0;
  List<ChatSession> _sessions = [];
  List<LlmModel> _models = [];
  String? _activeSessionId;
  String? _selectedModelSlug;
  var _loading = true;

  @override
  void initState() {
    super.initState();
    _bootstrap();
  }

  Future<void> _bootstrap() async {
    setState(() => _loading = true);
    try {
      final results = await Future.wait([
        widget.api.guard(widget.api.listSessions),
        widget.api.guard(widget.api.listModels),
      ]);
      _sessions = results[0] as List<ChatSession>;
      _models = results[1] as List<LlmModel>;
      _selectedModelSlug ??= _models.where((m) => m.enabled && m.isDefault).map((m) => m.slug).firstOrNull ??
          _models.where((m) => m.enabled).map((m) => m.slug).firstOrNull;
      _activeSessionId ??= _sessions.isNotEmpty ? _sessions.first.id : null;
      if (_activeSessionId == null) {
        final created = await widget.api.guard(
          () => widget.api.createSession(modelSlug: _selectedModelSlug),
        );
        _sessions = [created, ..._sessions];
        _activeSessionId = created.id;
      }
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  Future<void> _createSession() async {
    final session = await widget.api.guard(
      () => widget.api.createSession(modelSlug: _selectedModelSlug),
    );
    setState(() {
      _sessions = [session, ..._sessions];
      _activeSessionId = session.id;
      _tab = 0;
    });
  }

  Future<void> _deleteSession(String id) async {
    await widget.api.guard(() => widget.api.deleteSession(id));
    setState(() {
      _sessions = _sessions.where((s) => s.id != id).toList();
      if (_activeSessionId == id) {
        _activeSessionId = _sessions.isNotEmpty ? _sessions.first.id : null;
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    if (_loading) {
      return const Scaffold(body: Center(child: CircularProgressIndicator()));
    }

    return Scaffold(
      appBar: AppBar(
        title: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text('SpicyClaw'),
            Text(
              widget.user.displayName,
              style: Theme.of(context).textTheme.labelSmall,
            ),
          ],
        ),
        actions: [
          IconButton(onPressed: widget.onLogout, icon: const Icon(Icons.logout)),
        ],
      ),
      body: IndexedStack(
        index: _tab,
        children: [
          _activeSessionId == null
              ? const Center(child: Text('暂无会话'))
              : ChatScreen(
                  key: ValueKey(_activeSessionId),
                  api: widget.api,
                  sessionId: _activeSessionId!,
                  sessions: _sessions,
                  models: _models,
                  selectedModelSlug: _selectedModelSlug,
                  onSelectSession: (id) => setState(() => _activeSessionId = id),
                  onCreateSession: _createSession,
                  onDeleteSession: _deleteSession,
                  onModelChanged: (slug) => setState(() => _selectedModelSlug = slug),
                ),
          SkillsScreen(api: widget.api),
        ],
      ),
      bottomNavigationBar: NavigationBar(
        selectedIndex: _tab,
        onDestinationSelected: (i) => setState(() => _tab = i),
        destinations: const [
          NavigationDestination(icon: Icon(Icons.chat_bubble_outline), label: '对话'),
          NavigationDestination(icon: Icon(Icons.extension_outlined), label: '技能'),
        ],
      ),
    );
  }
}

extension<T> on Iterable<T> {
  T? get firstOrNull => isEmpty ? null : first;
}
