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
import java.util.OptionalInt;


/**
 * Code generator for {@link org.iogp.gigs.Test3210}. This generator needs to be executed only if the GIGS data changed.
 * The code is sent to the standard output; maintainers need to copy-and-paste the relevant methods to the test class,
 * but be aware that the original code may contain manual changes that need to be preserved.
 *
 * @author  Michael Arneson (INT)
 * @version 1.0
 * @since   1.0
 */
public final class Test3210 extends TestMethodGenerator {
    /**
     * Launcher.
     *
     * @param  args  ignored.
     * @throws IOException if an error occurred while reading the test data.
     */
    public static void main(String[] args) throws IOException {
        new Test3210().run();
    }

    /**
     * Creates a new test methods generator.
     */
    private Test3210() {
    }

    /**
     * Generates the code.
     *
     * @throws IOException if an error occurred while reading the test data.
     */
    private void run() throws IOException {
        final DataParser data = new DataParser(Series.USER_DEFINED, "GIGS_user_3210_VerticalCRS.txt",
                Integer.class,      // [ 0]: GIGS Vertical CRS Code
                String .class,      // [ 1]: GIGS Vertical CRS Name
                Integer.class,      // [ 2]: GIGS Vertical Datum Code (see GIGS Test Procedure 3209)
                Integer.class,      // [ 3]: EPSG Coordinate System Code
                String .class,      // [ 4]: Coordinate System Axis 1 Name
                String .class,      // [ 5]: Coordinate System Axis 1 Abbreviation
                String .class,      // [ 6]: Coordinate System Axis 1 Orientation
                String .class,      // [ 7]: Coordinate System Axis 1 Unit
                Integer.class,      // [ 8]: EPSG Coordinate System Code
                String .class,      // [ 9]: Equivalent EPSG Datum Name
                String .class,      // [10]: Early Binding Transformation Code (see GIGS Test Procedure 3208)
                String .class);     // [11]: GIGS Remarks

        while (data.next()) {
            final int         code             = data.getInt        ( 0);
            final String      name             = data.getString     ( 1);
            final int         datumCode        = data.getInt        ( 2);
            final int         csCode           = data.getInt        ( 3);
            final String      axisName         = data.getString     ( 4);
            final String      axisAbbreviation = data.getString     ( 5);
            final String      axisOrientation  = data.getString     ( 6);
            final String      axisUnit         = data.getString     ( 7);
            final OptionalInt codeEPSG         = data.getIntOptional( 8);
            final String      nameEPSG         = data.getString     ( 9);
            final String      remarks          = data.getString     (11);
            /*
             * Write javadoc.
             */
            out.append('\n');
            indent(1); out.append("/**\n");
            indent(1); out.append(" * Tests “").append(name).append("” vertical CRS creation from the factory.\n");
            indent(1); out.append(" *\n");
            printJavadocKeyValues("GIGS vertical CRS code", code,
                                  "GIGS vertical name", replaceAsciiPrimeByUnicode(name),
                                  "EPSG equivalence", codeAndName(codeEPSG, nameEPSG),
                                  "EPSG coordinate system code", csCode);
            printJavadocAxisHeader();
            printJavadocAxisRow(axisName, axisAbbreviation, axisOrientation, axisUnit);
            printJavadocTableFooter();
            printRemarks(remarks);
            printJavadocThrows("if an error occurred while creating the vertical CRS from the properties.");
            /*
             * Write test method.
             */
            printTestMethodSignature(GIGS, code, name);
            printCallToSetCodeAndName(code, name);
            indent(2); out.append("createDatum(Test3209::GIGS_").append(datumCode).append(");\n");
            indent(2); out.append("createVerticalCS(").append(csCode).append(");\n");
            printVerifyAxis(-1, axisName, axisAbbreviation, axisOrientation, axisUnit);
            indent(2); out.append("verifyVerticalCRS();\n");
            indent(1); out.append('}');
            saveTestMethod();
        }
        flushAllMethods();
    }
}
