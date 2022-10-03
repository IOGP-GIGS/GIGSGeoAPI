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

import static org.junit.jupiter.api.Assertions.assertNotNull;


/**
 * Code generator for {@link org.iogp.gigs.Test3203}. This generator needs to be executed only if the GIGS data changed.
 * The code is sent to the standard output; maintainers need to copy-and-paste the relevant methods to the test class,
 * but be aware that the original code may contain manual changes that need to be preserved.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @version 1.0
 * @since   1.0
 */
public final class Test3203 extends TestMethodGenerator {
    /**
     * Launcher.
     *
     * @param  args  ignored.
     * @throws IOException if an error occurred while reading the test data.
     */
    public static void main(String[] args) throws IOException {
        new Test3203().run();
    }

    /**
     * Creates a new test methods generator.
     */
    private Test3203() {
    }

    /**
     * Generates the code.
     *
     * @throws IOException if an error occurred while reading the test data.
     */
    private void run() throws IOException {
        final DataParser data = new DataParser(Series.USER_DEFINED, "GIGS_user_3203_PrimeMeridian.txt",
                Integer.class,      // [0]: GIGS Prime Meridian Code
                String .class,      // [1]: GIGS Prime Meridian Name
                Double .class,      // [2]: Longitude from Greenwich
                String .class,      // [3]: Unit Name
                Double .class,      // [4]: Longitude from Greenwich in decimal degrees
                Integer.class,      // [5]: Equivalent EPSG Prime Meridian Code
                String .class,      // [6]: Equivalent EPSG Prime Meridian Name
                String .class);     // [7]: GIGS Remarks

        while (data.next()) {
            final int     code               = data.getInt    ( 0);
            final String  name               = data.getString ( 1);
            final String  unitName           = data.getString ( 3);
            final double  greenwichLongitude = data.getDouble ( 2);
            final double  longitudeInDegrees = data.getDouble ( 4);
            final int     codeEPSG           = data.getInt    ( 5);
            final String  nameEPSG           = data.getString ( 6);
            final String  remarks            = data.getString ( 7);
            Object unit = SexagesimalUnit.parse(unitName);
            if (unit == null) {
                unit = parseAngularUnit(unitName);
                assertNotNull(unit, unitName);
            }
            /*
             * Write javadoc.
             */
            out.append('\n');
            indent(1); out.append("/**\n");
            indent(1); out.append(" * Tests “").append(name).append("” prime meridian creation from the factory.\n");
            indent(1); out.append(" *\n");
            printJavadocKeyValues("GIGS prime meridian code", code,
                                  "GIGS prime meridian name", name,
                                  "EPSG equivalence", codeAndName(codeEPSG, nameEPSG),
                                  "Greenwich longitude", quantityAndAlternative(greenwichLongitude, unitName, longitudeInDegrees, "°"));
            printRemarks(remarks);
            printJavadocThrows("if an error occurred while creating the prime meridian from the properties.");
            printJavadocSee(2203, EPSG + '_' + codeEPSG);
            /*
             * Write test method.
             */
            printTestMethodSignature(GIGS, code, name);
            printCallToSetCodeAndName(code, name);
            printFieldAssignments("longitudeInDegrees", longitudeInDegrees,
                                  "greenwichLongitude", greenwichLongitude,
                                  "angularUnit",        unit);

            indent(2); out.append("verifyPrimeMeridian();\n");
            indent(1); out.append('}');
            saveTestMethod();
        }
        flushAllMethods();
    }
}
