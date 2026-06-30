import 'package:flutter/material.dart';

abstract final class AppTheme {
  static const _accent = Color(0xFFFF6B35);

  static ThemeData get dark {
    const scheme = ColorScheme.dark(
      primary: _accent,
      surface: Color(0xFF0D1117),
      onSurface: Color(0xFFE8EAED),
    );
    return ThemeData(
      useMaterial3: true,
      colorScheme: scheme,
      scaffoldBackgroundColor: scheme.surface,
      appBarTheme: const AppBarTheme(centerTitle: false),
      filledButtonTheme: FilledButtonThemeData(
        style: FilledButton.styleFrom(backgroundColor: _accent, foregroundColor: Colors.white),
      ),
    );
  }
}
