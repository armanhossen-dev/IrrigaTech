import 'package:fl_chart/fl_chart.dart';
import 'package:flutter/material.dart';

class Sparkline extends StatelessWidget {
  const Sparkline({
    super.key,
    required this.values,
    required this.color,
  });

  final List<double> values;
  final Color color;

  @override
  Widget build(BuildContext context) {
    if (values.length < 2) {
      return const SizedBox(height: 28);
    }
    final spots = <FlSpot>[
      for (var i = 0; i < values.length; i++) FlSpot(i.toDouble(), values[i]),
    ];
    return SizedBox(
      height: 28,
      child: LineChart(
        LineChartData(
          minX: 0,
          maxX: (values.length - 1).toDouble(),
          gridData: const FlGridData(show: false),
          titlesData: const FlTitlesData(show: false),
          borderData: FlBorderData(show: false),
          lineTouchData: const LineTouchData(enabled: false),
          lineBarsData: [
            LineChartBarData(
              spots: spots,
              isCurved: true,
              color: color,
              barWidth: 2,
              dotData: const FlDotData(show: false),
              belowBarData: BarAreaData(
                show: true,
                color: color.withOpacity(0.18),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
