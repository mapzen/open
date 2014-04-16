# Mapzen

This project is a simple mobile mapping application built using the Mapzen SDKs and other open source projects. 


## Setup notes

The following dependencies must be installed locally prior to building the project.

### Maven on Ubuntu
As of Ubuntu 14.04 the version of maven in the repositories are not up to date enough. Maven 3.1+ is required. You can either build Maven from source at http://maven.apache.org/ or use the PPA available at https://launchpad.net/~natecarlson/+archive/maven3

#### Maven with the PPA
```bash
$ sudo apt-get remove maven
$ sudo add-apt-repository https://launchpad.net/~natecarlson/+archive/maven3
$ sudo apt-get update
$ sudo apt-get install maven3
$ sudo ln -s /usr/bin/mvn3 /usr/bin/mvn
```

If you are running Ubuntu Saucy or Trusty, after running add-apt-repository you will need to change the source to Precise. You can either do this by running Software & Updates and updating the entry in the Other Software tab changing the Distribution property, or by editing /etc/apt/sources.list.d/natecarlson-maven3-saucy.list and changing saucy or trusty to precise.

### Install Android SDK and Support Library

```bash
$ git clone https://github.com/mapzen/maven-android-sdk-deployer.git
$ cd maven-android-sdk-deployer
$ mvn install -P 4.4
$ mvn install -P 4.3
$ mvn install -fextras/compatibility-v4/pom.xml
```

See [Maven Android SDK Deployer](https://github.com/mosabua/maven-android-sdk-deployer) project for more information.

### Install VectorTileMap Library

```bash
$ git clone --recursive https://github.com/mapzen/vtm.git
$ cd vtm && ./gradlew clean install
```

### Install VectorTileMap Native Libs

```bash
$ git clone https://github.com/mapzen/vtm-native-libs.git
$ cd vtm-native-libs && ./install-dependencies.sh
```

### Install Samsung Accessories

```bash
$ mvn install:install-file \
    -Dpackaging=jar \
    -Dfile=<path to>/accessory-v1.0.9.jar \
    -DgroupId=com.samsung \
    -DartifactId=samsung-accessory \
    -Dversion=1.0.9
```

## Mapzen Android Demo

Install and run Mapzen demo application.

```bash
$ git clone https://github.com/mapzen/mapzen-android-demo.git
$ cd mapzen-android-demo
$ mvn clean install
$ mvn android:deploy android:run
```

## Contributions
We appreciate pull requests. Please run <code>$ mvn clean verify</code>
and make sure it runs cleanly as it runs all of our tests and code quality tools 
we have configured. If you cannot make it run cleanly please let us know in the
comments of your pull requests and we will help. Thanks!
