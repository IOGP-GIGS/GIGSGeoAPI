/*
 * GIGS - Geospatial Integrity of Geoscience Software
 * https://gigs.iogp.org/
 *
 * Copyright (C) 2022 International Association of Oil and Gas Producers.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

/**
 * Copy of the {@code module-info.java} file provided in main source,
 * modified with additional dependencies needed for testing purposes only.
 */
module GIGSTests {
    /*
     * Part copied from main code.
     */
    requires java.prefs;
    requires java.logging;
    requires java.desktop;
    requires org.junit.platform.engine;
    requires org.junit.platform.launcher;

    requires transitive org.junit.jupiter.api;
    requires transitive org.opengis.geoapi;
//  exports org.iogp.gigs.runner;               // Avoid a NullPointerException because no runner package in tests.
    exports org.iogp.gigs;
    opens   org.iogp.gigs;

    uses org.opengis.referencing.datum.DatumFactory;
    uses org.opengis.referencing.datum.DatumAuthorityFactory;
    uses org.opengis.referencing.cs.CSFactory;
    uses org.opengis.referencing.cs.CSAuthorityFactory;
    uses org.opengis.referencing.crs.CRSFactory;
    uses org.opengis.referencing.crs.CRSAuthorityFactory;
    uses org.opengis.referencing.operation.CoordinateOperationFactory;
    /*
     * Part needed for tests only.
     */
    requires tech.uom.seshat;
    exports org.iogp.gigs.generator;
}
