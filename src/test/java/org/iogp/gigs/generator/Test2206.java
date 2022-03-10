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
 * Code generator for {@link Test2206}. This generator needs to be executed only if the GIGS data changed.
 * The code is sent to the standard output; maintainer need to copy-and-paste the relevant methods to the
 * test class, but be aware that the original code may contain manual changes that need to be preserved.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @version 1.0
 * @since   1.0
 */
public final class Test2206 extends TestMethodGenerator {
    /**
     * Launcher.
     *
     * @param  args  ignored.
     * @throws IOException if an error occurred while reading the test data.
     */
    public static void main(String[] args) throws IOException {
        new Test2206().run();
    }

    /**
     * Creates a new generator.
     */
    private Test2206() {
    }

    /**
     * Generates the code.
     *
     * @throws IOException if an error occurred while reading the test data.
     */
    private void run() throws IOException {
        final DataParser data = new DataParser(Series.PREDEFINED, "GIGS_lib_2206_Conversion.txt",
                Integer.class,      // [0]: EPSG Conversion Code
                String .class,      // [1]: EPSG Conversion Name
                String .class,      // [2]: Alias(es)
                String .class,      // [3]: Conversion Method
                String .class,      // [4]: EPSG Usage Extent
                String .class);     // [5]: GIGS Remarks

        data.regroup(0, 3, 1, "\\s+zone\\s+\\w+", "\\s+CM\\s+\\w+");

        while (data.next()) {
            final int[]    codes      = data.getInts   (0);
            final String   name       = data.getString (1);
            final String[] aliases    = data.getStrings(2);
            final String   methodName = data.getString (3);
            final String   extent     = data.getString (4);
            final String   remarks    = data.getString (5);

            out.append('\n');
            indent(1); out.append("/**\n");
            indent(1); out.append(" * Tests “").append(name).append("” coordinate operation creation from the factory.\n");
            indent(1); out.append(" *\n");
            printJavadocKeyValues("EPSG coordinate operation codes", codes,
                                  "EPSG coordinate operation name", name,
                                  "Alias(es) given by EPSG", aliases,
                                  "Coordinate operation method", methodName,
                                  "Specific usage / Remarks", remarks,
                                  "EPSG Usage Extent", extent);
            printRemarks(remarks);
            printJavadocThrows("if an error occurred while creating the coordinate operation from the EPSG code.");
            printTestMethodSignature(codes.length == 1 ? codes[0] : -1, name);
            printFieldAssignments("name",       name,
                                  "aliases",    aliases,
                                  "methodName", methodName);
            printCallsToMethod("createAndVerifyProjection", codes);
            indent(1); out.append('}');
            saveTestMethod();
        }
        printAllMethods();
    }
}
