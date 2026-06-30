import 'package:flutter/material.dart';

import '../api/spicyclaw_api.dart';
import '../models/skill_models.dart';

class SkillsScreen extends StatefulWidget {
  const SkillsScreen({super.key, required this.api});

  final SpicyclawApi api;

  @override
  State<SkillsScreen> createState() => _SkillsScreenState();
}

class _SkillsScreenState extends State<SkillsScreen> {
  List<Skill> _skills = [];
  var _loading = true;

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    setState(() => _loading = true);
    try {
      final skills = await widget.api.guard(widget.api.listSkills);
      if (mounted) setState(() => _skills = skills);
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    if (_loading) {
      return const Center(child: CircularProgressIndicator());
    }
    if (_skills.isEmpty) {
      return Center(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            const Text('暂无技能'),
            const SizedBox(height: 12),
            OutlinedButton(onPressed: _load, child: const Text('刷新')),
          ],
        ),
      );
    }
    return RefreshIndicator(
      onRefresh: _load,
      child: ListView.separated(
        padding: const EdgeInsets.all(16),
        itemCount: _skills.length,
        separatorBuilder: (context, index) => const SizedBox(height: 8),
        itemBuilder: (context, index) {
          final s = _skills[index];
          return Card(
            child: ListTile(
              title: Text(s.name),
              subtitle: Text('${s.slug}\n${s.description}', maxLines: 3, overflow: TextOverflow.ellipsis),
              trailing: Chip(
                label: Text(s.enabled ? '启用' : '禁用'),
                backgroundColor: s.enabled ? Colors.green.withValues(alpha: 0.15) : null,
              ),
            ),
          );
        },
      ),
    );
  }
}
