class AppUser {
  const AppUser({
    required this.uid,
    this.displayName,
    this.email,
    this.photoUrl,
    this.linkedDeviceToken,
    this.fcmToken,
  });

  final String uid;
  final String? displayName;
  final String? email;
  final String? photoUrl;
  final String? linkedDeviceToken;
  final String? fcmToken;

  Map<String, dynamic> toFirestore() => {
        'displayName': displayName,
        'email': email,
        'photoUrl': photoUrl,
        'linkedDeviceToken': linkedDeviceToken,
        'fcmToken': fcmToken,
        'updatedAt': DateTime.now().toIso8601String(),
      };
}
