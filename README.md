# Mapzen
[![Circle CI Build Status](https://circleci.com/gh/mapzen/open.png?circle-token=cfd8a71bc5d58302f87abaec91a89a0ffd871d1e)][1]

This project is a simple mobile mapping application built using the Mapzen SDKs and other open source projects. 


## Prerequisites

* [Java 7](http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html)
* [Maven 3.1+](http://maven.apache.org/download.cgi)
* [Android SDK](http://developer.android.com/sdk/index.html) (Platform-tools, Support Repository, Build-tools 19.0.3, API 19)
* `JAVA_HOME`, `M2_HOME`, and `ANDROID_HOME` environment variables

## Mapzen Android Demo

Install and run Mapzen demo application.

```bash
$ git clone https://github.com/mapzen/open.git
$ cd open
$ ./scripts/install-dependencies.sh
$ mvn clean install
$ mvn android:deploy android:run
```

**Note to OS X users:** You can install Maven using [Homebrew](http://brew.sh/) but you may receive an `Error: Could not find or load main class ...`. To fix this, set your `M2_HOME` to `/usr/local/Cellar/maven/[version]/libexec` (note the added 'libexec'). 

**Note to Ubuntu users:** Additional packages must be installed to run `aapt` on Ubuntu 64-bit installations. Please see http://stackoverflow.com/questions/19523502/androids-aapt-not-running-on-64-bit-ubuntu-13-10-no-ia32-libs-how-can-i-fix for more information.

## Contributions
We appreciate pull requests. Please run <code>$ mvn clean verify</code>
and make sure it runs cleanly as it runs all of our tests and code quality tools 
we have configured. If you cannot make it run cleanly please let us know in the
comments of your pull requests and we will help. Thanks!

[1]: https://circleci.com/gh/mapzen/open
