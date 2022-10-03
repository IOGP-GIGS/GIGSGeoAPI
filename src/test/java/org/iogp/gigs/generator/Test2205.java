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
 * Code generator for {@link org.iogp.gigs.Test2205}. This generator needs to be executed only if the GIGS data changed.
 * The code is sent to the standard output; maintainers need to copy-and-paste the relevant methods to the test class,
 * but be aware that the original code may contain manual changes that need to be preserved.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @version 1.0
 * @since   1.0
 */
public final class Test2205 extends TestMethodGenerator {
    /**
     * Launcher.
     *
     * @param  args  ignored.
     * @throws IOException if an error occurred while reading the test data.
     */
    public static void main(String[] args) throws IOException {
        new Test2205().run();
    }

    /**
     * Creates a new test methods generator.
     */
    private Test2205() {
    }

    /**
     * Generates the code.
     *
     * @throws IOException if an error occurred while reading the test data.
     */
    private void run() throws IOException {
        final DataParser data = new DataParser(Series.PREDEFINED, "GIGS_lib_2205_GeodeticCRS.txt",
                Integer.class,      // [0]: EPSG Geodetic CRS Code
                String .class,      // [1]: Geodetic CRS Type.
                String .class,      // [2]: EPSG Geodetic CRS Name
                String .class,      // [3]: Alias(es)
                Integer.class,      // [4]: Associated Geodetic Datum
                String .class,      // [5]: EPSG Usage Extent
                String .class);     // [6]: GIGS Remarks

        while (data.next()) {
            final int             code    = data.getInt    (0);
            final GeodeticCrsType type    = data.getCrsType(1);
            final String          name    = data.getString (2);
            final String[]        aliases = data.getStrings(3);
            final int             datum   = data.getInt    (4);
            final String          extent  = data.getString (5);
            final String          remarks = data.getString (6);

            out.append('\n');
            indent(1); out.append("/**\n");
            indent(1); out.append(" * Tests “").append(name).append("” geodetic CRS creation from the factory.\n");
            indent(1); out.append(" *\n");
            printJavadocKeyValues("EPSG CRS code", code,
                                  "EPSG CRS name", name,
                                  "Alias(es) given by EPSG", aliases,
                                  "CRS type", type.label,
                                  "EPSG datum code", datum,
                                  "EPSG Usage Extent", extent);
            printRemarks(remarks);
            printJavadocThrows("if an error occurred while creating the CRS from the EPSG code.");
            printTestMethodSignature(EPSG, code, name);
            printFieldAssignments("code",         code,
                                  "name",         name,
                                  "aliases",      aliases,
                                  "datumCode",    datum,
                                  "isGeocentric", type == GeodeticCrsType.GEOCENTRIC);
            indent(2); out.append("verifyGeodeticCRS(").append(type.name()).append(");\n");
            printCallToDependencyTest("datumTest", datum);
            indent(1); out.append('}');
            saveTestMethod();
        }
        flushAllMethods();
    }
}
