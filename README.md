# GIGSGeoAPI
Repository for integrating GIGS reference data with OGC GeoAPI.
This repository provides an executable form of Geospatial Integrity of Geoscience Software (GIGS) tests.
GIGS tests are published by the [International Association of Oil & Gas Producers](https://www.iogp.org) (IOGP).
This module makes those tests available in a form that can be executed by various implementations,
through the [OGC GeoAPI 3.0.1](https://www.geoapi.org/) standard interfaces.

## Warnings
* This is a work in progress. Only a subset of GIGS tests are currently available.
* This is a partial implementation of GIGS version 1. Upgrade to GIGS version 2 is planed but not yet done.
* Testing framework is currently JUnit 4. Upgrade to JUnit 5 will be done in the future.

## Programming languages
The tests are implemented in the Java programming language but are executable in the following languages:

* Any Java library implementing OGC GeoAPI 3.0.1 interfaces.
* Any C/C++ library providing Java Native Interface (JNI) bridge to GeoAPI 3.0.1.
* Python library through the GeoAPI-Python bridge _(experimental in GeoAPI 4 branch)_.

### Implementations
Known implementations capable to run GIGS tests are:

* [Apache Spatial Information System (SIS)](https://sis.apache.org/)
* [JNI binding for PROJ](https://github.com/Kortforsyningen/PROJ-JNI)
