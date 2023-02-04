/*
 * GIGS - Geospatial Integrity of Geoscience Software
 * https://gigs.iogp.org/
 *
 * Copyright (C) 2022-2023 International Association of Oil and Gas Producers.
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
 * Implements the Geospatial Integrity of Geoscience Software tests.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @author  Alexis Manin (Geomatys)
 * @author  Johann Sorel (Geomatys)
 * @author  Michael Arneson (INT)
 * @version 1.0
 * @since   1.0
 */
module org.iogp.gigs {
    /*
     * Modules needed by GIGS for its internal, but not exposed to users.
     */
    requires java.prefs;
    requires java.logging;
    requires java.desktop;
    requires org.junit.platform.engine;
    requires org.junit.platform.launcher;
    /*
     * API that users of GIGS tests will need to handle,
     * both from external projects and defined by GIGS.
     */
    requires transitive org.junit.jupiter.api;
    requires transitive org.opengis.geoapi;
    exports org.iogp.gigs.runner;
    exports org.iogp.gigs;
    opens   org.iogp.gigs;          // Grants reflective access (needed by JUnit).
    /*
     * Services providers defined by implementations to test.
     */
    uses org.opengis.referencing.datum.DatumFactory;
    uses org.opengis.referencing.datum.DatumAuthorityFactory;
    uses org.opengis.referencing.cs.CSFactory;
    uses org.opengis.referencing.cs.CSAuthorityFactory;
    uses org.opengis.referencing.crs.CRSFactory;
    uses org.opengis.referencing.crs.CRSAuthorityFactory;
    uses org.opengis.referencing.operation.CoordinateOperationFactory;
}
