```bash
flutter clean

flutter pub get

flutter build apk --release

# 17-21 range openjdk is needed for apk
brew install openjdk@17 
/usr/libexec/java_home -V 

flutter config --jdk-dir="$(/usr/libexec/java_home -v17)"
brew reinstall openjdk@17

/usr/libexec/java_home -v17
```