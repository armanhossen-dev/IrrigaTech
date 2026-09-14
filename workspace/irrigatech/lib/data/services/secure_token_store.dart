import 'package:flutter_secure_storage/flutter_secure_storage.dart';

import '../../core/constants/app_constants.dart';

class SecureTokenStore {
  SecureTokenStore({FlutterSecureStorage? storage})
      : _storage = storage ?? const FlutterSecureStorage();

  final FlutterSecureStorage _storage;

  Future<void> saveToken(String token) =>
      _storage.write(key: AppConstants.secureBlynkToken, value: token);

  Future<String?> readToken() =>
      _storage.read(key: AppConstants.secureBlynkToken);

  Future<void> clearToken() =>
      _storage.delete(key: AppConstants.secureBlynkToken);
}
