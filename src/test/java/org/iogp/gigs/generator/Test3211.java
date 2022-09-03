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

import java.util.Map;
import java.util.OptionalInt;
import java.io.IOException;


/**
 * Code generator for {@link org.iogp.gigs.Test3211}. This generator needs to be executed only if the GIGS data changed.
 * The code is sent to the standard output; maintainers need to copy-and-paste the relevant methods to the test class,
 * but be aware that the original code may contain manual changes that need to be preserved.
 *
 * @author  Michael Arneson (INT)
 * @author  Martin Desruisseaux (Geomatys)
 * @version 1.0
 * @since   1.0
 */
public final class Test3211 extends TestMethodGenerator {
    /**
     * Launcher.
     *
     * @param  args  ignored.
     * @throws IOException if an error occurred while reading the test data.
     */
    public static void main(String[] args) throws IOException {
        new Test3211().run();
    }

    /**
     * Mapping from the transform method name specified in the GIGS testing file
     * to method name used in EPSG database.
     */
    private static final Map<String,String> GIGS_TO_EPSG_METHOD_NAME = Map.of(
        "Vertical offset", "Vertical Offset");

    /**
     * Mapping from GIGS CRS codes to EPSG codes.
     */
    private CodeMapper crsCodes;

    /**
     * Creates a new test methods generator.
     */
    private Test3211() {
    }

    /**
     * Generates the code.
     *
     * @throws IOException if an error occurred while reading the test data.
     */
    private void run() throws IOException {
        // GIGS CRS codes to themselves.
        crsCodes = new CodeMapper("GIGS_user_3210_VerticalCRS.txt", 0, false);
        final DataParser data = new DataParser(Series.USER_DEFINED, "GIGS_user_3211_VertTfm.txt",
                Integer.class,      // [ 0]: GIGS Transformation Code
                Integer.class,      // [ 1]: GIGS Source CRS Code (see GIGS Test Procedure 3210)
                String .class,      // [ 2]: GIGS Source CRS Name
                Integer.class,      // [ 3]: GIGS Target CRS Code (see GIGS Test Procedure 3210)
                String .class,      // [ 4]: GIGS Target CRS Name
                String .class,      // [ 5]: GIGS Transformation Version
                String .class,      // [ 6]: EPSG Transformation Method Name
                String .class,      // [ 7]: Parameter 1 Name
                Double .class,      // [ 8]: Parameter 1 Value
                String .class,      // [ 9]: Parameter 1 Unit
                String .class,      // [10]: Parameter 2 Name
                Double .class,      // [11]: Parameter 2 Value
                String .class,      // [12]: Parameter 2 Unit
                String .class,      // [13]: Parameter 3 Name
                Double .class,      // [14]: Parameter 3 Value
                String .class,      // [15]: Parameter 3 Unit
                String .class,      // [16]: Parameter 4 Name
                Double .class,      // [17]: Parameter 4 Value
                String .class,      // [18]: Parameter 4 Unit
                String .class,      // [19]: Parameter 5 Name
                Double .class,      // [20]: Parameter 5 Value
                String .class,      // [21]: Parameter 5 Unit
                Integer.class,      // [22]: Equivalent EPSG Transformation Code
                String .class,      // [23]: Equivalent EPSG Transformation Name
                String .class);     // [24]: GIGS Remarks

        while (data.next()) {
            final int         code       = data.getInt        ( 0);
            final int         sourceCRS  = data.getInt        ( 1);
            final int         targetCRS  = data.getInt        ( 3);
            final String      methodName = data.getString     ( 6);
            final Parameter[] parameters = data.getParameters ( 7, 5, 0);
            final OptionalInt codeEPSG   = data.getIntOptional(22);
            final String      nameEPSG   = data.getString     (23);
            final String      remarks    = data.getString     (24);
            final String      name       = data.getString(2) + " to " + data.getString(4);
            /*
             * Write javadoc.
             */
            out.append('\n');
            indent(1); out.append("/**\n");
            indent(1); out.append(" * Tests “").append(name).append("” transformation from the factory.\n");
            indent(1); out.append(" *\n");
            printJavadocKeyValues("GIGS transformation code", code,
                                  "EPSG Transformation Method", methodName,
                                  "EPSG equivalence", codeAndName(codeEPSG, nameEPSG));
            printJavadocParameters("Transformation parameters", parameters);
            printRemarks(remarks);
            printJavadocThrows("if an error occurred while creating the transformation from the properties.");
            /*
             * Write test method.
             */
            printTestMethodSignature(GIGS, code, name);
            printCallToSetCodeAndName(code, name);
            writeCreateCRS("createSourceCRS", sourceCRS);
            writeCreateCRS("createTargetCRS", targetCRS);
            /*
             * Write definitions of operation parameters.
             */
            printParameterDefinitions(GIGS_TO_EPSG_METHOD_NAME.getOrDefault(methodName, methodName), parameters);
            indent(2); out.append("verifyTransformation();\n");
            indent(1); out.append('}');
            saveTestMethod();
        }
        flushAllMethods();
    }

    /**
     * Write a "create CRS" statement (either from GIGS or EPSG).
     */
    private void writeCreateCRS(final String method, final int code) {
        indent(2);
        out.append(method).append('(');
        if (crsCodes.optional(code).isPresent()) {
            out.append("Test3210::GIGS_");
        }
        out.append(code).append(");\n");
    }
}
