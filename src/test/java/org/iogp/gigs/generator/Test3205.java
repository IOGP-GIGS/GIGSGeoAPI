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
import java.util.ArrayList;
import java.util.Arrays;


/**
 * Code generator for {@link org.iogp.gigs.Test3205}. This generator needs to be executed only if the GIGS data changed.
 * The code is sent to the standard output; maintainer need to copy-and-paste the relevant methods to the test class,
 * but be aware that the original code may contain manual changes that need to be preserved.
 *
 * @author  Michael Arneson (INT)
 * @version 1.0
 * @since   1.0
 */
public class Test3205 extends TestMethodGenerator {
    /**
     * Launcher.
     *
     * @param  args  ignored.
     * @throws IOException if an error occurred while reading the test data.
     */
    public static void main(String[] args) throws IOException {
        new Test3205().run();
    }

    /**
     * Generates the code.
     *
     * @throws IOException if an error occurred while reading the test data.
     */
    private void run() throws IOException {
        final DataParser data = new DataParser(Series.USER_DEFINED, "GIGS_user_3205_GeodeticCRS.txt",
                Integer.class,      // [ 0]: GIGS Geodetic CRS Code
                String .class,      // [ 1]: GIGS Geodetic CRS Definition Source
                String .class,      // [ 2]: GIGS Geodetic CRS Name
                String .class,      // [ 3]: Geodetic CRS Type
                Integer.class,      // [ 4]: GIGS Datum Code (see GIGS Test Procedure 3204)
                Integer.class,      // [ 5]: EPSG Coordinate System Code
                String .class,      // [ 6]: Equivalent EPSG CRS Code(s)
                String .class,      // [ 7]: Equivalent EPSG CRS Name(s)
                Integer.class,      // [ 8]: Early-binding Transformation Code (see GIGS Test Procedure 3208 or 2208)
                String .class);     // [ 9]: GIGS Remarks
        while (data.next()) {
            final int    code             = data.getInt           ( 0);
            final String name             = data.getString        ( 2);
            final String geodeticType     = data.getString        ( 3);
            final int    datumCode        = data.getInt           ( 4);
            final int    csCode           = data.getInt           ( 5);
            final String remarks          = data.getString        (9);

            if (!geodeticType.equals("Geographic 2D")) {
                continue;
            }

            out.append('\n');
            indent(1); out.append("/**\n");
            indent(1); out.append(" * Tests “").append(name).append("” ")
                    .append(" geographic 2D CRS from the factory.\n");
            indent(1); out.append(" *\n");
            final var descriptions = new ArrayList<>(20);
            descriptions.addAll(Arrays.asList("GIGS geographic 2D CRS code", code,
                    "GIGS geographic 2D CRS name", replaceAsciiPrimeByUnicode(name)));
            descriptions.addAll(Arrays.asList("Coordinate System code", csCode,
                    "Geodetic CRS Type", geodeticType,
                    "GIGS Datum code", datumCode));
            printJavadocKeyValues(descriptions.toArray());
            printRemarks(remarks);
            printJavadocThrows("if an error occurred while creating the CRS from the properties.");
            /*
             * Write test method.
             */
            printTestMethodSignature(GIGS, code, name);
            printCallToSetCodeAndName(code, name);
            indent(2); out.append("createDatum(Test3204::GIGS_").append(datumCode).append(");\n");
            indent(2); out.append("csCode=").append(csCode).append(";\n");
            indent(2);
            out.append("verifyGeographicCRS();\n");
            indent(1); out.append('}');
            saveTestMethod();
        }
        flushAllMethods();
    }
}
