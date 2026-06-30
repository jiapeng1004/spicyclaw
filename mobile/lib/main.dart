import 'package:flutter/material.dart';

import 'api/spicyclaw_api.dart';
import 'config/app_config.dart';
import 'models/user.dart';
import 'screens/home_shell.dart';
import 'screens/login_screen.dart';
import 'theme/app_theme.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  runApp(const SpicyclawApp());
}

class SpicyclawApp extends StatefulWidget {
  const SpicyclawApp({super.key});

  @override
  State<SpicyclawApp> createState() => _SpicyclawAppState();
}

class _SpicyclawAppState extends State<SpicyclawApp> {
  SpicyclawApi? _api;
  User? _user;
  var _checking = true;
  String? _bootError;

  @override
  void initState() {
    super.initState();
    _boot();
  }

  Future<void> _boot() async {
    try {
      final api = await SpicyclawApi.create();
      User? user;
      try {
        user = await api.guard(api.me);
      } catch (_) {
        user = null;
      }
      if (mounted) {
        setState(() {
          _api = api;
          _user = user;
          _checking = false;
        });
      }
    } catch (e) {
      if (mounted) {
        setState(() {
          _bootError = e.toString();
          _checking = false;
        });
      }
    }
  }

  Future<void> _logout() async {
    await _api?.logout();
    setState(() => _user = null);
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'SpicyClaw',
      theme: AppTheme.dark,
      home: Builder(
        builder: (context) {
          if (_checking) {
            return const Scaffold(body: Center(child: CircularProgressIndicator()));
          }
          if (_bootError != null) {
            return Scaffold(
              body: Center(
                child: Padding(
                  padding: const EdgeInsets.all(24),
                  child: Text('无法连接后端 ${AppConfig.apiBaseUrl}\n$_bootError'),
                ),
              ),
            );
          }
          final api = _api!;
          if (_user == null) {
            return LoginScreen(
              api: api,
              onLoggedIn: (user) => setState(() => _user = user),
            );
          }
          return HomeShell(api: api, user: _user!, onLogout: _logout);
        },
      ),
    );
  }
}
