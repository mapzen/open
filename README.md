# Mapzen

Start where you are

## Setup notes

The following dependencies must be installed locally prior to building/running the project.

### Install VTM

Compile and install the VectorTileMap dependency in the local .m2 repository.

<pre><code>$ git clone --recursive https://github.com/opensciencemap/vtm.git
$ cd vtm && ./gradlew clean install
</pre></code>

## Build project

Clone project and import `build.gradle` in project root into Android Studio.

-or-

Build via command line using the Gradle wrapper.

<pre><code>$ git clone --recursive https://github.com:mapzen/android.git mapzen
$ cd mapzen
$ ./gradlew clean installDebug
</pre></code>
