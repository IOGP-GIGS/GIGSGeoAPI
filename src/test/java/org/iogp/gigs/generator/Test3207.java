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
import java.util.*;


/**
 * Code generator for {@link org.iogp.gigs.Test3207}. This generator needs to be executed only if the GIGS data changed.
 * The code is sent to the standard output; maintainer need to copy-and-paste the relevant methods to the test class,
 * but be aware that the original code may contain manual changes that need to be preserved.
 *
 * @author  Michael Arneson (INT)
 * @version 1.0
 * @since   1.0
 */
public final class Test3207 extends TestMethodGenerator {
    /**
     * Launcher.
     *
     * @param  args  ignored.
     * @throws IOException if an error occurred while reading the test data.
     */
    public static void main(String[] args) throws IOException {
        new Test3207().run();
    }

    /**
     * Creates a new test methods generator.
     */
    private Test3207() {
    }

    /**
     * Generates the code.
     *
     * @throws IOException if an error occurred while reading the test data.
     */
    private void run() throws IOException {
        //use corrected file for now, unit issue https://github.com/IOGP-GIGS/GIGSTestDataset/issues/2 is fixed
        final DataParser data = new DataParser(Series.USER_DEFINED, "GIGS_user_3207_ProjectedCRS.txt",
                Integer.class,      // [ 0]: GIGS Projected CRS Code
                String .class,      // [ 1]: GIGS Projected CRS Definition Source
                String .class,      // [ 2]: GIGS Projected CRS Name
                Integer.class,      // [ 3]: Base CRS Code (see GIGS Test Procedure 3205)
                String .class,      // [ 4]: Base CRS Name (see GIGS Test Procedure 3205)
                Integer.class,      // [ 5]: Conversion Code (see GIGS Test Procedure 3206)
                String .class,      // [ 6]: Conversion Name (see GIGS Test Procedure 3206)
                Integer.class,      // [ 7]: EPSG Coordinate System Code
                String .class,      // [ 8]: Coordinate System Axis 1 Name
                String .class,      // [ 9]: Coordinate System Axis 1 Abbreviation
                String .class,      // [10]: Coordinate System Axis 1 Orientation
                String .class,      // [11]: Coordinate System Axis 1 Unit
                String .class,      // [12]: Coordinate System Axis 2 Name
                String .class,      // [13]: Coordinate System Axis 2 Abbreviation
                String .class,      // [14]: Coordinate System Axis 2 Orientation
                String .class,      // [15]: Coordinate System Axis 2 Unit
                Integer.class,      // [16]: Equivalent EPSG CRS Code
                String .class,      // [17]: Equivalent EPSG CRS Name
                String .class);     // [18]: GIGS Remarks

        while (data.next()) {
            final int              code              = data.getInt        ( 0);
            final DefinitionSource source            = data.getSource     ( 1);
            final String           name              = data.getString     ( 2);
            final int              baseCRSCode       = data.getInt        ( 3);
            final int              conversionCode    = data.getInt        ( 5);
            final int              csCode            = data.getInt        ( 7);
            final String           axis1Name         = data.getString     ( 8);
            final String           axis1Abbreviation = data.getString     ( 9);
            final String           axis1Orientation  = data.getString     (10);
            final String           axis1Unit         = data.getString     (11);
            final String           axis2Name         = data.getString     (12);
            final String           axis2Abbreviation = data.getString     (13);
            final String           axis2Orientation  = data.getString     (14);
            final String           axis2Unit         = data.getString     (15);
            final OptionalInt      codeEPSG          = data.getIntOptional(16);
            final String           nameEPSG          = data.getString     (17);
            final String           remarks           = data.getString     (18);
            /*
             * Write javadoc.
             */
            out.append('\n');
            indent(1); out.append("/**\n");
            indent(1); out.append(" * Tests “").append(name).append("” projected CRS creation from the factory.\n");
            indent(1); out.append(" *\n");
            final var descriptions = new ArrayList<>(20);
            descriptions.addAll(Arrays.asList("GIGS projected CRS code", code,
                    "GIGS projectedCRS name", replaceAsciiPrimeByUnicode(name)));
            if (codeEPSG.isPresent()) {
                descriptions.addAll(Arrays.asList("EPSG equivalence", codeAndName(codeEPSG.getAsInt(), nameEPSG)));
            }
            descriptions.addAll(Arrays.asList("GIGS base CRS code", baseCRSCode,
                    "GIGS conversion code", conversionCode,
                    "Conversion definition source", source,
                    "EPSG coordinate system code", csCode
            ));
            printJavadocKeyValues(descriptions.toArray());
            printJavadocAxisHeader();
            printJavadocAxisRow(axis1Name, axis1Abbreviation, axis1Orientation, axis1Unit);
            printJavadocAxisRow(axis2Name, axis2Abbreviation, axis2Orientation, axis2Unit);
            printJavadocTableFooter();
            printRemarks(remarks);
            printJavadocThrows("if an error occurred while creating the projected CRS from the properties.");
            /*
             * Write test method.
             */
            printTestMethodSignature(GIGS, code, name);
            printCallToSetCodeAndName(code, name);

            indent(2); out.append("createBaseCRS(Test3205::GIGS_").append(baseCRSCode).append(");\n");
            indent(2); out.append("createConversion(");
            switch (source) {
                case LIBRARY: break;
                case USER:    out.append("Test3206::GIGS_"); break;
                default:      throw new AssertionError(source);
            }
            out.append(conversionCode).append(");\n");
            printAxis("axis1", axis1Name, axis1Abbreviation, axis1Orientation, axis1Unit);
            printAxis("axis2", axis2Name, axis2Abbreviation, axis2Orientation, axis2Unit);
            indent(2); out.append("createCartesianCS(").append(csCode).append(", axis1, axis2);\n");
            indent(2); out.append("verifyProjectedCRS();\n");
            indent(1); out.append('}');
            saveTestMethod();
        }
        flushAllMethods();
    }
}
