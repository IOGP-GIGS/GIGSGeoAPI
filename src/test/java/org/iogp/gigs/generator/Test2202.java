/*
 *    GeoAPI - Java interfaces for OGC/ISO standards
 *    http://www.geoapi.org
 *
 *    Copyright (C) 2011-2021 Open Geospatial Consortium, Inc.
 *    All Rights Reserved. http://www.opengeospatial.org/ogc/legal
 *
 *    Permission to use, copy, and modify this software and its documentation, with
 *    or without modification, for any purpose and without fee or royalty is hereby
 *    granted, provided that you include the following on ALL copies of the software
 *    and documentation or portions thereof, including modifications, that you make:
 *
 *    1. The full text of this NOTICE in a location viewable to users of the
 *       redistributed or derivative work.
 *    2. Notice of any changes or modifications to the OGC files, including the
 *       date changes were made.
 *
 *    THIS SOFTWARE AND DOCUMENTATION IS PROVIDED "AS IS," AND COPYRIGHT HOLDERS MAKE
 *    NO REPRESENTATIONS OR WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 *    TO, WARRANTIES OF MERCHANTABILITY OR FITNESS FOR ANY PARTICULAR PURPOSE OR THAT
 *    THE USE OF THE SOFTWARE OR DOCUMENTATION WILL NOT INFRINGE ANY THIRD PARTY
 *    PATENTS, COPYRIGHTS, TRADEMARKS OR OTHER RIGHTS.
 *
 *    COPYRIGHT HOLDERS WILL NOT BE LIABLE FOR ANY DIRECT, INDIRECT, SPECIAL OR
 *    CONSEQUENTIAL DAMAGES ARISING OUT OF ANY USE OF THE SOFTWARE OR DOCUMENTATION.
 *
 *    The name and trademarks of copyright holders may NOT be used in advertising or
 *    publicity pertaining to the software without specific, written prior permission.
 *    Title to copyright in this software and any associated documentation will at all
 *    times remain with copyright holders.
 */
package org.iogp.gigs.generator;

import java.util.Map;
import java.io.IOException;


/**
 * Code generator for {@link Test2202}. This generator needs to be executed only if the GIGS data changed.
 * The code is sent to the standard output; maintainer need to copy-and-paste the relevant methods to the
 * test class, but be aware that the original code may contain manual changes that need to be preserved.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @version 1.0
 * @since   1.0
 */
public final class Test2202 extends TestMethodGenerator {
    /**
     * The mapping from a few GIGS test names to method names.
     * We put an entry in this map only when a name different
     * than the automatically generated name is desired.
     */
    private final Map<String,String> METHOD_NAMES = map(
            "Airy Modified 1849",             "testAiryModified",
            "Bessel 1841",                    "testBessel",
            "Clarke 1880 (Benoit)",           "testClarkeBenoit",
            "Clarke 1880 (IGN)",              "testClarkeIGN",
            "Clarke 1880 (RGS)",              "testClarkeRGS",
            "Clarke 1880 (Arc)",              "testClarkeArc",
            "Clarke 1880 (SGA 1922)",         "testClarkeSGA",
            "Clarke 1866 Authalic Sphere",    "testClarkeAuthalicSphere",
            "Everest (1830 Definition)",      "testEverest1830",
            "Everest 1830 (1937 Adjustment)", "testEverest1937",
            "Everest 1830 (1962 Definition)", "testEverest1962",
            "Everest 1830 (1967 Definition)", "testEverest1967",
            "Everest 1830 (1975 Definition)", "testEverest1975",
            "Everest 1830 Modified",          "testEverestModified",
            "Everest 1830 (RSO 1969)",        "testEverestRSO",
            "Helmert 1906",                   "testHelmert",
            "Indonesian National Spheroid",   "testIndonesianNational",
            "Danish 1876",                    "testDanish",
            "Plessis 1817",                   "testPlessis",
            "Struve 1860",                    "testStruve");

    /**
     * Launcher.
     *
     * @param  args  ignored.
     * @throws IOException if an error occurred while reading the test data.
     */
    public static void main(String[] args) throws IOException {
        new Test2202().run();
    }

    /**
     * Generates the code.
     *
     * @throws IOException if an error occurred while reading the test data.
     */
    private void run() throws IOException {
        final DataParser data = new DataParser(Series.PREDEFINED, "GIGS_lib_2202_Ellipsoid.txt",
                Integer.class,      // [ 0]: EPSG Ellipsoid Code
                String .class,      // [ 1]: EPSG Ellipsoid Name
                String .class,      // [ 2]: Alias(es)
                Double .class,      // [ 3]: Semi-major axis (a)
                String .class,      // [ 4]: Unit Name
                Double .class,      // [ 5]: Unit Conversion Factor
                Double .class,      // [ 6]: Semi-major axis (a) in metres
                Double .class,      // [ 7]: Second defining parameter: Inverse flattening (1/f)
                Double .class,      // [ 8]: Second defining parameter: Semi-minor axis (b)
                Boolean.class,      // [ 9]: Spherical
                String .class,      // [10]: EPSG Usage Extent
                String .class);     // [11]: GIGS Remarks

        while (data.next()) {
            final int      code              = data.getInt    ( 0);
            final String   name              = data.getString ( 1);
            final String[] aliases           = data.getStrings( 2);
            final String   axisUnit          = data.getString ( 4);
            final double   toMetres          = data.getDouble ( 5);
            final double   semiMajorInMetres = data.getDouble ( 6);
            final double   semiMajorAxis     = data.getDouble ( 3);
            final double   semiMinorAxis     = data.getDouble ( 8);
            final double   inverseFlattening = data.getDouble ( 7);
            final boolean  isSphere          = data.getBoolean( 9);
            final String   extent            = data.getString (10);
            final String   remarks           = data.getString (11);

            out.append('\n');
            indent(1); out.append("/**\n");
            indent(1); out.append(" * Tests “").append(name).append("” ")
                    .append(isSphere ? "spheroid" : "ellipsoid")
                    .append(" creation from the factory.\n");
            indent(1); out.append(" *\n");
            printJavadocKeyValues("EPSG ellipsoid code", code,
                                  "EPSG ellipsoid name", name,
                                  "Alias(es) given by EPSG", aliases,
                                  "Semi-major axis (<var>a</var>)", semiMajorAxis + " " + axisUnit,
                                  "Semi-minor axis (<var>b</var>)", Double.isNaN(semiMinorAxis) ? semiMinorAxis : semiMinorAxis + " " + axisUnit,
                                  "Inverse flattening (1/<var>f</var>)", inverseFlattening,
                                  "EPSG Usage Extent", extent);
            printRemarks(remarks);
            printJavadocThrows("if an error occurred while creating the ellipsoid from the EPSG code.");
            printTestMethodSignature(METHOD_NAMES, name);
            printFieldAssignments("code",              code,
                                  "name",              name,
                                  "aliases",           aliases,
                                  "toMetres",          Double.isNaN(toMetres) ? 1 : toMetres,
                                  "semiMajorInMetres", Double.isNaN(semiMajorInMetres) ? semiMajorAxis : semiMajorInMetres,
                                  "semiMajorAxis",     semiMajorAxis,
                                  "semiMinorAxis",     semiMinorAxis,
                                  "inverseFlattening", inverseFlattening,
                                  "isSphere",          isSphere);
            indent(2); out.append("verifyEllipsoid();\n");
            indent(1); out.append('}');
            print();
        }
    }
}
