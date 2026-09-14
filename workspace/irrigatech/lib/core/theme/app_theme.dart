import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';

class AppColors {
  static const Color teal = Color(0xFF2F6F68);
  static const Color emerald = Color(0xFF3E7A6C);
  static const Color deepTeal = Color(0xFF1F4A46);
  static const Color mist = Color(0xFFE8EEED);
  static const Color rain = Color(0xFF4A7188);
  static const Color danger = Color(0xFFB42318);
  static const Color warning = Color(0xFFB54708);
  static const Color success = Color(0xFF027A48);
  static const Color tankFull = Color(0xFFB42318);
  static const Color info = Color(0xFF3B6D8A);
  static const Color glassLight = Color(0xFFFFFFFF);
  static const Color glassDark = Color(0xFF1C2422);
  static const Color surfaceLight = Color(0xFFF5F6F6);
  static const Color surfaceDark = Color(0xFF111614);
  static const Color borderLight = Color(0xFFE4E7E7);
  static const Color borderDark = Color(0xFF2C3533);
}

class AppSpace {
  static const double xs = 4;
  static const double sm = 8;
  static const double md = 12;
  static const double lg = 16;
  static const double xl = 24;
  static const double radius = 12;
  static const double icon = 20;
  static const double iconLg = 24;
}

class AppTheme {
  static ThemeData light() {
    final scheme = ColorScheme.fromSeed(
      seedColor: AppColors.teal,
      brightness: Brightness.light,
    ).copyWith(
      primary: AppColors.teal,
      error: AppColors.danger,
      surface: AppColors.glassLight,
    );
    return _base(scheme, Brightness.light);
  }

  static ThemeData dark() {
    final scheme = ColorScheme.fromSeed(
      seedColor: AppColors.teal,
      brightness: Brightness.dark,
    ).copyWith(
      primary: const Color(0xFF7AA8A2),
      error: const Color(0xFFF97066),
      surface: AppColors.glassDark,
    );
    return _base(scheme, Brightness.dark);
  }

  static ThemeData _base(ColorScheme scheme, Brightness brightness) {
    final isDark = brightness == Brightness.dark;
    final textTheme = GoogleFonts.interTextTheme(
      isDark ? ThemeData.dark().textTheme : ThemeData.light().textTheme,
    ).copyWith(
      titleLarge: GoogleFonts.inter(
        fontWeight: FontWeight.w600,
        fontSize: 20,
        letterSpacing: -0.2,
      ),
      titleMedium: GoogleFonts.inter(
        fontWeight: FontWeight.w600,
        fontSize: 16,
        letterSpacing: -0.1,
      ),
      titleSmall: GoogleFonts.inter(
        fontWeight: FontWeight.w600,
        fontSize: 13,
        letterSpacing: 0.1,
      ),
      bodyLarge: GoogleFonts.inter(fontSize: 15, height: 1.45),
      bodyMedium: GoogleFonts.inter(fontSize: 14, height: 1.45),
      bodySmall: GoogleFonts.inter(
        fontSize: 12,
        height: 1.4,
        color: scheme.onSurface.withOpacity(0.64),
      ),
      labelSmall: GoogleFonts.inter(
        fontWeight: FontWeight.w500,
        fontSize: 11,
        letterSpacing: 0.2,
      ),
    );
    return ThemeData(
      useMaterial3: true,
      colorScheme: scheme,
      textTheme: textTheme,
      scaffoldBackgroundColor:
          isDark ? AppColors.surfaceDark : AppColors.surfaceLight,
      dividerColor: isDark ? AppColors.borderDark : AppColors.borderLight,
      appBarTheme: AppBarTheme(
        centerTitle: false,
        elevation: 0,
        scrolledUnderElevation: 0.5,
        backgroundColor:
            isDark ? AppColors.surfaceDark : AppColors.surfaceLight,
        foregroundColor: scheme.onSurface,
        titleTextStyle: textTheme.titleMedium?.copyWith(
          color: scheme.onSurface,
        ),
      ),
      cardTheme: CardThemeData(
        elevation: 0,
        color: isDark ? AppColors.glassDark : AppColors.glassLight,
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(AppSpace.radius),
          side: BorderSide(
            color: isDark ? AppColors.borderDark : AppColors.borderLight,
          ),
        ),
      ),
      inputDecorationTheme: InputDecorationTheme(
        filled: true,
        fillColor: isDark ? const Color(0xFF161D1C) : const Color(0xFFF0F2F2),
        contentPadding: const EdgeInsets.symmetric(horizontal: 14, vertical: 14),
        border: OutlineInputBorder(
          borderRadius: BorderRadius.circular(AppSpace.radius),
          borderSide: BorderSide.none,
        ),
      ),
      filledButtonTheme: FilledButtonThemeData(
        style: FilledButton.styleFrom(
          minimumSize: const Size.fromHeight(48),
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(AppSpace.radius),
          ),
        ),
      ),
      navigationBarTheme: NavigationBarThemeData(
        height: 64,
        elevation: 0,
        backgroundColor: isDark ? AppColors.glassDark : AppColors.glassLight,
        indicatorColor: scheme.primary.withOpacity(0.12),
        labelTextStyle: WidgetStatePropertyAll(
          textTheme.labelSmall?.copyWith(fontWeight: FontWeight.w600),
        ),
      ),
    );
  }
}
