# Mapzen

Start where you are

## Setup notes

The following dependencies must be installed locally prior to building/running the project.

### Install VTM

Compile and install the VectorTileMap depenency in the local .m2 repository.

<pre><code>$ git clone	git@github.com:mapzen/vtm.git
$ cd vtm
$ git submodule init
$ git submodule update
$ ./gradlew clean install
</pre></code>

## Build project

Clone project and import `build.gradle` in project root into Android Studio.

-or-

Build via command line using the Gradle wrapper.

<pre><code>$ git clone git@github.com:mapzen/android.git mapzen
$ cd mapzen
$ ./gradlew clean installDebug
</pre></code>
