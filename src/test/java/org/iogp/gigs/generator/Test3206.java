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
 * Code generator for {@link org.iogp.gigs.Test3206}. This generator needs to be executed only if the GIGS data changed.
 * The code is sent to the standard output; maintainers need to copy-and-paste the relevant methods to the test class,
 * but be aware that the original code may contain manual changes that need to be preserved.
 *
 * @author  Michael Arneson (INT)
 * @author  Martin Desruisseaux (Geomatys)
 * @version 1.0
 * @since   1.0
 */
public final class Test3206 extends TestMethodGenerator {
    /**
     * Launcher.
     *
     * @param  args  ignored.
     * @throws IOException if an error occurred while reading the test data.
     */
    public static void main(String[] args) throws IOException {
        new Test3206().run();
    }

    /**
     * Creates a new test methods generator.
     */
    private Test3206() {
    }

    /**
     * Generates the code.
     *
     * @throws IOException if an error occurred while reading the test data.
     */
    private void run() throws IOException {
        final DataParser data = new DataParser(Series.USER_DEFINED, "GIGS_user_3206_Conversion.txt",
                Integer.class,      // [ 0]: GIGS Conversion Code
                String .class,      // [ 1]: GIGS Conversion Name
                String .class,      // [ 2]: Conversion Method Name (a)
                String .class,      // [ 3]: Parameter 1 Name
                Double .class,      // [ 4]: Parameter 1 Value
                String .class,      // [ 5]: Parameter 1 Unit
                Double .class,      // [ 6]: Parameter 1 Value in decimal degrees
                String .class,      // [ 7]: Parameter 2 Name
                Double .class,      // [ 8]: Parameter 2 Value
                String .class,      // [ 9]: Parameter 2 Unit
                Double .class,      // [10]: Parameter 2 Value in decimal degrees
                String .class,      // [11]: Parameter 3 Name
                Double .class,      // [12]: Parameter 3 Value
                String .class,      // [13]: Parameter 3 Unit
                Double .class,      // [14]: Parameter 3 Value in decimal degrees
                String .class,      // [15]: Parameter 4 Name
                Double .class,      // [16]: Parameter 4 Value
                String .class,      // [17]: Parameter 4 Unit
                Double .class,      // [18]: Parameter 4 Value in decimal degrees
                String .class,      // [19]: Parameter 5 Name
                Double .class,      // [20]: Parameter 5 Value
                String .class,      // [21]: Parameter 5 Unit
                String .class,      // [22]: Parameter 6 Name
                Double .class,      // [23]: Parameter 6 Value
                String .class,      // [24]: Parameter 6 Unit
                String .class,      // [25]: Parameter 7 Name
                Double .class,      // [26]: Parameter 7 Value
                String .class,      // [27]: Parameter 7 Unit
                Integer.class,      // [28]: Equivalent EPSG Conversion Code
                String .class,      // [29]: Equivalent EPSG Conversion Name
                String .class);     // [30]: GIGS Remarks

        while (data.next()) {
            final int         code       = data.getInt        ( 0);
            final String      name       = data.getString     ( 1);
            final String      conversion = data.getString     ( 2);
            final Parameter[] parameters = data.getParameters ( 3, 7, 4);
            final OptionalInt codeEPSG   = data.getIntOptional(28);
            final String      nameEPSG   = data.getString     (29);
            final String      remarks    = data.getString     (30);
            /*
             * Write javadoc.
             */
            out.append('\n');
            indent(1); out.append("/**\n");
            indent(1); out.append(" * Tests “").append(name).append("” conversion from the factory.\n");
            indent(1); out.append(" *\n");
            printJavadocKeyValues("GIGS conversion code", code,
                                  "GIGS conversion name", name,
                                  "EPSG equivalence", codeAndName(codeEPSG, nameEPSG));
            printJavadocParameters("Conversion parameters", parameters);
            printRemarks(remarks);
            printJavadocThrows("if an error occurred while creating the conversion from the properties.");
            /*
             * Write test method.
             */
            printTestMethodSignature(GIGS, code, name);
            printCallToSetCodeAndName(code, name);
            printParameterDefinitions(conversion, parameters);
            indent(2); out.append("verifyConversion();\n");
            indent(1); out.append('}');
            saveTestMethod();
        }
        flushAllMethods();
    }
}
