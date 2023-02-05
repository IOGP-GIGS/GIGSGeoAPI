# GIGS runner through GeoAPI
Repository for integrating GIGS reference data with OGC GeoAPI.
This repository provides an executable form of [Geospatial Integrity of Geoscience Software](https://gigs.iogp.org/) (GIGS) tests.
GIGS tests are published by the [International Association of Oil & Gas Producers](https://www.iogp.org) (IOGP) Geomatics Committee.
This module makes those tests available in a form that can be executed by various implementations,
through the [OGC GeoAPI 3.0.2](https://www.geoapi.org/) standard interfaces.


## Installation
This project requires Java 11 or later and [Maven](https://maven.apache.org/) build system.
For installation, clone this repository and build as below:

```shell
git clone https://github.com/IOGP-GIGS/GIGSGeoAPI.git
cd GIGSGeoAPI
mvn install
```


## Execution
The tests can be executed using a graphical application,
or integrated in the tests of a library using [JUnit 5](https://junit.org/).
For launching the graphical application, execute the following command in the
`GIGSGeoAPI` directory and select the JAR file of the application to test:

```shell
mvn exec:java
```

For integrating GIGS tests with the JUnit tests of an application, see:

* [Online javadoc](https://iogp-gigs.github.io/GIGSGeoAPI/).


## Programming languages
The tests are implemented in the Java programming language but are executable in the following languages:

* Any Java library implementing OGC GeoAPI 3.0.2 interfaces.
* Any C/C++ library providing Java Native Interface (JNI) bridge to GeoAPI 3.0.2.
* Python library through the GeoAPI-Python bridge _(experimental in GeoAPI 4 branch)_.


### Implementations
Known implementations capable to run GIGS tests are:

* [Apache Spatial Information System (SIS)](https://sis.apache.org/)
* [JNI binding for PROJ](https://github.com/Kortforsyningen/PROJ-JNI)
