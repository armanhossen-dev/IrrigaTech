for flutter:

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

for android studio

in mac terminal:
keytool -list -v \
-keystore ~/.android/debug.keystore \
-alias androiddebugkey \
-storepass android \
-keypass android



in AS terminal:

ls -la gradle/wrapper

cat gradle/wrapper/gradle-wrapper.properties

ls -la ~/.gradle/wrapper/dists/


If Gradle 8.9 is NOT there - in mac terminal

brew install gradle

gradle --version


cd ~/Documents/GitHub/IrrigaTech/irrigatech-android

/Users/armanhossenripon/Documents/GitHub/IrrigaTech/workspace/irrigatech-android


export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home

in AS -terminal
gradle wrapper --gradle-version 8.9

ls -la gradle/wrapper

in mac terminal

cd /Users/armanhossenripon/Documents/GitHub/IrrigaTech/workspace/irrigatech-android

export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home

java -version

gradle wrapper --gradle-version 8.9


wcid
342878600607-ls66raosmoer47lcahloc4fd52sc9ff6.apps.googleusercontent.com

feed
deployment id
AKfycbzkUWJ30TIOy9qCQZ-15ofQ85ktZnbA6cEV3bDYQ8zY2sQhmOS4w2Gc83XVZTHSVwvf

Web app
https://script.google.com/macros/s/AKfycbzkUWJ30TIOy9qCQZ-15ofQ85ktZnbA6cEV3bDYQ8zY2sQhmOS4w2Gc83XVZTHSVwvf/exec
