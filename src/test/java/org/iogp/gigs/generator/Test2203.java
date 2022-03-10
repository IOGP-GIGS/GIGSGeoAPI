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
 * Code generator for {@link Test2203}. This generator needs to be executed only if the GIGS data changed.
 * The code is sent to the standard output; maintainer need to copy-and-paste the relevant methods to the
 * test class, but be aware that the original code may contain manual changes that need to be preserved.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @version 1.0
 * @since   1.0
 */
public final class Test2203 extends TestMethodGenerator {
    /**
     * Launcher.
     *
     * @param  args  ignored.
     * @throws IOException if an error occurred while reading the test data.
     */
    public static void main(String[] args) throws IOException {
        new Test2203().run();
    }

    /**
     * Generates the code.
     *
     * @throws IOException if an error occurred while reading the test data.
     */
    private void run() throws IOException {
        final DataParser data = new DataParser(Series.PREDEFINED, "GIGS_lib_2203_PrimeMeridian.txt",
                Integer.class,      // [0]: EPSG Prime Meridian Code
                String .class,      // [1]: EPSG Prime Meridian Name
                String .class,      // [2]: Alias(es)
                String .class,      // [3]: Longitude from Greenwich (sexagesimal)
                String .class,      // [4]: Unit Name
                Double .class,      // [5]: Longitude from Greenwich (decimal degrees)
                String .class,      // [6]: EPSG Usage Extent
                String .class);     // [7]: GIGS Remarks

         while (data.next()) {
            final int      code      = data.getInt    (0);
            final String   name      = data.getString (1);
            final String[] aliases   = data.getStrings(2);
            final String   lonString = data.getString (3);
            final String   unit      = data.getString (4);
            final double   longitude = data.getDouble (5);
            final String   extent    = data.getString (6);
            final String   remarks   = data.getString (7);

            out.append('\n');
            indent(1); out.append("/**\n");
            indent(1); out.append(" * Tests “").append(name).append("” prime meridian creation from the factory.\n");
            indent(1); out.append(" *\n");
            printJavadocKeyValues("EPSG prime meridian code", code,
                                  "EPSG prime meridian name", name,
                                  "Alias(es) given by EPSG", aliases,
                                  "Greenwich longitude", reformatSexagesimalAngle(lonString),
                                  "Angular unit", unit,
                                  "Specific usage / Remarks", remarks,
                                  "EPSG Usage Extent", extent);
            printRemarks(remarks);
            printJavadocThrows("if an error occurred while creating the prime meridian from the EPSG code.");
            printTestMethodSignature(code, name);
            printFieldAssignments("code",               code,
                                  "name",               name,
                                  "aliases",            aliases,
                                  "greenwichLongitude", longitude);
            indent(2); out.append("verifyPrimeMeridian();\n");
            indent(1); out.append('}');
            saveTestMethod();
        }
        printAllMethods();
    }

    /**
     * Replaces the prime and double-prime quotes of the given angle by their Unicode symbol.
     */
    private static String reformatSexagesimalAngle(final String angle) {
        final StringBuilder buffer = new StringBuilder(angle);
        for (int i=buffer.length(); --i>=0;) {
            switch (buffer.charAt(i)) {
                case '\'': buffer.setCharAt(i, '′'); break;
                case '"' : buffer.setCharAt(i, '″'); break;
                case '\\': buffer.deleteCharAt(i); break;
            }
        }
        return buffer.toString();
    }
}
