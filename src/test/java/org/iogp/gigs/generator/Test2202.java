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
package org.iogp.gigs.generator;

import java.io.IOException;


/**
 * Code generator for {@link org.iogp.gigs.Test2202}. This generator needs to be executed only if the GIGS data changed.
 * The code is sent to the standard output; maintainers need to copy-and-paste the relevant methods to the test class,
 * but be aware that the original code may contain manual changes that need to be preserved.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @version 1.0
 * @since   1.0
 */
public final class Test2202 extends TestMethodGenerator {
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
     * Creates a new test methods generator.
     */
    private Test2202() {
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
            printTestMethodSignature(EPSG, code, name);
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
            saveTestMethod();
        }
        flushAllMethods();
    }
}
