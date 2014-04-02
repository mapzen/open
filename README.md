# Mapzen

Start where you are

## Setup notes

The following dependencies must be installed locally prior to building/running the project.

### Install Android SDK, Support Library, and Google Play Services

<pre><code>$ git clone https://github.com/mapzen/maven-android-sdk-deployer.git
$ cd maven-android-sdk-deployer
$ mvn install -P 4.4 # Used for building production application
$ mvn install -P 4.3 # Used for unit tests (Robolectric)
$ mvn install -fextras/compatibility-v4/pom.xml
</pre></code>

See [Maven Android SDK Deployer](https://github.com/mosabua/maven-android-sdk-deployer) project for more information.

### Install GeoJson
<pre><code>$ git clone https://github.com/mapzen/simple-geojson.git geojson
$ cd geojson && ./gradlew clean install
</pre></code>

### Install OnTheRoad
<pre><code>$ git clone https://github.com/mapzen/OnTheRoad.git
$ cd OnTheRoad && ./gradlew clean install
</pre></code>

### Install VectorTileMap Library

<pre><code>$ git clone --recursive https://github.com/mapzen/vtm.git
$ cd vtm
$ echo "ndk.dir=/path/to/ndk" >> vtm-android/local.properties
$ ./gradlew clean install
</pre></code>

### Install VectorTileMap Native Libs

<pre><code>$ git clone https://github.com/mapzen/vtm-native-libs.git
$ cd vtm-native-libs && ./install-dependencies.sh
</pre></code>

### Install Speakerbox

<pre><code>$ git clone https://github.com/mapzen/speakerbox.git
$ cd speakerbox && mvn clean install
</pre></code>

### Install Samsung Accessories

<pre><code>
$ mvn install:install-file -Dpackaging=jar -Dfile=<path to>/accessory-v1.0.9.jar -DgroupId=com.samsung -DartifactId=samsung-accessory -Dversion=1.0.9
</pre></code>

## Build Mapzen Application

<pre><code>$ git clone https://github.com/mapzen/android.git mapzen-android
$ cd mapzen-android
$ mvn clean install
$ mvn android:deploy android:run
</pre></code>

## Contributions
We appreciate pull requests. Please run <code>$ mvn clean verify</code>
and make sure it runs cleanly as it runs all of our tests and code quality tools 
we have configured. If you cannot make it run cleanly please let us know in the
comments of your pull requests and we will help. Thanks!
