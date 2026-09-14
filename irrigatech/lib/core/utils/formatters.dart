import 'package:intl/intl.dart';

import '../constants/app_constants.dart';

class Formatters {
  static final DateFormat time = DateFormat('HH:mm');
  static final DateFormat dateTime = DateFormat('dd MMM, HH:mm');
  static final DateFormat day = DateFormat('EEE d MMM');

  static String temperature(double celsius, {required bool useCelsius}) {
    if (celsius == AppConstants.temperatureErrorSentinel) {
      return 'Sensor Error';
    }
    if (useCelsius) {
      return '${celsius.toStringAsFixed(1)}°C';
    }
    final f = celsius * 9 / 5 + 32;
    return '${f.toStringAsFixed(1)}°F';
  }

  static String percent(double value) => '${value.toStringAsFixed(0)}%';

  static String voltage(double volts) => '${volts.toStringAsFixed(1)}V';

  static String motor(bool on) => on ? 'ON 🟢' : 'OFF 🔴';

  static String rain(bool raining) => raining ? 'Raining' : 'None';

  static String tank(bool full) => full ? 'Full 🛑' : 'Empty 💧';
}
