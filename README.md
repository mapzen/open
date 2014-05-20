# Mapzen
[![Circle CI Build Status](https://circleci.com/gh/mapzen/mapzen-android-demo.png?circle-token=cfd8a71bc5d58302f87abaec91a89a0ffd871d1e)][1]

This project is a simple mobile mapping application built using the Mapzen SDKs and other open source projects. 


## Prerequisites

* [Java 7](http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html)
* [Maven 3.1+](http://maven.apache.org/download.cgi)
* [Android SDK](http://developer.android.com/sdk/index.html) (Platform-tools, Build-tools 19.0.3, API 19)
* `JAVA_HOME`, `M2_HOME`, `ANDROID_HOME`, and `ANDROID_NDK_HOME` environment variables

## Mapzen Android Demo

Install and run Mapzen demo application.

```bash
$ git clone https://github.com/mapzen/mapzen-android-demo.git
$ cd mapzen-android-demo
$ ./install-dependencies.sh
$ mvn clean install
$ mvn android:deploy android:run
```

## Contributions
We appreciate pull requests. Please run <code>$ mvn clean verify</code>
and make sure it runs cleanly as it runs all of our tests and code quality tools 
we have configured. If you cannot make it run cleanly please let us know in the
comments of your pull requests and we will help. Thanks!

[1]: https://circleci.com/gh/mapzen/mapzen-android-demo
