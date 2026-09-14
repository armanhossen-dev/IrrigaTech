import 'package:flutter/material.dart';
import 'package:lottie/lottie.dart';

class WeatherLottie extends StatelessWidget {
  const WeatherLottie({super.key, required this.raining});

  final bool raining;

  @override
  Widget build(BuildContext context) {
    return SizedBox(
      width: 56,
      height: 56,
      child: Lottie.asset(
        'assets/lottie/water_drop.json',
        fit: BoxFit.contain,
        errorBuilder: (context, error, stack) {
          return Icon(
            raining ? Icons.umbrella : Icons.wb_sunny_outlined,
            size: 36,
          );
        },
      ),
    );
  }
}
