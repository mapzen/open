# Mapzen

Start where you are

## Setup notes

The following dependencies must be installed locally prior to building/running the project.

### Install VTM

Compile and install the VectorTileMap dependency in the local .m2 repository.

<pre><code>$ git clone --recursive https://github.com/opensciencemap/vtm.git
$ echo "ndk.dir=/path/to/ndk" >> vtm-android/local.properties
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

## Contributions
We appreciate pull requests. Please run 
<pre><code>$ ./gradlew</pre></code>
twice (the very first run might fail due to an error we have not yet figured out) 
and make sure it runs cleanly as it runs all of our tests and code quality tools 
we have configured. If you cannot make it run cleanly please let us know in the
comments of your pull requests and we will help.

