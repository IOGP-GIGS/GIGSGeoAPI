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
import java.util.Set;
import java.util.OptionalInt;
import java.io.IOException;


/**
 * Code generator for {@link org.iogp.gigs.Test3208}. This generator needs to be executed only if the GIGS data changed.
 * The code is sent to the standard output; maintainers need to copy-and-paste the relevant methods to the test class,
 * but be aware that the original code may contain manual changes that need to be preserved.
 *
 * @author  Michael Arneson (INT)
 * @author  Martin Desruisseaux (Geomatys)
 * @version 1.0
 * @since   1.0
 */
public final class Test3208 extends TestMethodGenerator {
    /**
     * Launcher.
     *
     * @param  args  ignored.
     * @throws IOException if an error occurred while reading the test data.
     */
    public static void main(String[] args) throws IOException {
        new Test3208().run();
    }

    /**
     * Mapping from the transform method name specified in the GIGS testing file
     * to method name used in EPSG database.
     *
     * @see <a href="https://github.com/IOGP-GIGS/GIGSTestDataset/issues/5">Ambiguous operation method names</a>
     */
    private static final Map<String,String> GIGS_TO_EPSG_METHOD_NAME = Map.of(
        "Geocentric translations",                        "Geocentric translations (geog2D domain)",
        "Position Vector 7-param. transformation",        "Position Vector transformation (geog2D domain)",
        "Coordinate Frame rotation",                      "Coordinate Frame rotation (geog2D domain)",
        "Molodensky-Badekas 10-parameter transformation", "Molodensky-Badekas (PV geog2D domain)");

    /**
     * Operation methods that do not require ellipsoid axis lengths.
     */
    private static final Set<String> NO_AXIS_LENGTHS = Set.of("NTv2", "NADCON", "Longitude rotation");

    /**
     * Creates a new test methods generator.
     */
    private Test3208() {
    }

    /**
     * Generates the code.
     *
     * @throws IOException if an error occurred while reading the test data.
     */
    private void run() throws IOException {
        final DataParser data = new DataParser(Series.USER_DEFINED, "GIGS_user_3208_CoordTfm.txt",
                Integer.class,      // [ 0]: GIGS Transformation Code
                String .class,      // [ 1]: GIGS Transformation Name
                Integer.class,      // [ 2]: GIGS Source CRS Code (see GIGS Test Procedure 3205)
                String .class,      // [ 3]: GIGS Source CRS Name
                Integer.class,      // [ 4]: GIGS Target CRS Code (see GIGS Test Procedure 3205)
                String .class,      // [ 5]: GIGS Target CRS Name
                Integer.class,      // [ 6]: GIGS Transformation Variant
                String .class,      // [ 7]: EPSG Transformation Method Name
                String .class,      // [ 8]: Parameter 1 Name
                String .class,      // [ 9]: Parameter 1 Value. May be a filename.
                String .class,      // [10]: Parameter 1 Unit
                Double .class,      // [11]: Parameter 1 Value in decimal degrees
                String .class,      // [12]: Parameter 2 Name
                String .class,      // [13]: Parameter 2 Value. May be a filename.
                String .class,      // [14]: Parameter 2 Unit
                String .class,      // [15]: Parameter 3 Name
                Double .class,      // [16]: Parameter 3 Value
                String .class,      // [17]: Parameter 3 Unit
                String .class,      // [18]: Parameter 4 Name
                Double .class,      // [19]: Parameter 4 Value
                String .class,      // [20]: Parameter 4 Unit
                String .class,      // [21]: Parameter 5 Name
                Double .class,      // [22]: Parameter 5 Value
                String .class,      // [23]: Parameter 5 Unit
                String .class,      // [24]: Parameter 6 Name
                Double .class,      // [25]: Parameter 6 Value
                String .class,      // [26]: Parameter 6 Unit
                String .class,      // [27]: Parameter 7 Name
                Double .class,      // [28]: Parameter 7 Value
                String .class,      // [29]: Parameter 7 Unit
                String .class,      // [30]: Parameter 8 Name
                Double .class,      // [31]: Parameter 8 Value
                String .class,      // [32]: Parameter 8 Unit
                String .class,      // [33]: Parameter 9 Name
                Double .class,      // [34]: Parameter 9 Value
                String .class,      // [35]: Parameter 9 Unit
                String .class,      // [36]: Parameter 10 Name
                Double .class,      // [37]: Parameter 10 Value
                String .class,      // [38]: Parameter 10 Unit
                Integer.class,      // [39]: Equivalent EPSG Transformation Code
                String .class,      // [40]: Equivalent EPSG Transformation Name
                String .class);     // [41]: GIGS Remarks
        /*
         * Note: we ommit source/target CRS names because they are repeated in the transformation method name.
         */
        while (data.next()) {
            final int         code       = data.getInt        ( 0);
            final String      name       = data.getString     ( 1);
            final int         sourceCRS  = data.getInt        ( 2);
            final int         targetCRS  = data.getInt        ( 4);
            final String      methodName = data.getString     ( 7);
            final Parameter[] parameters = data.getParameters ( 8, 10, 1);
            final OptionalInt codeEPSG   = data.getIntOptional(39);
            final String      nameEPSG   = data.getString     (40);
            final String      remarks    = data.getString     (41);
            /*
             * Write javadoc.
             */
            out.append('\n');
            indent(1); out.append("/**\n");
            indent(1); out.append(" * Tests “").append(name).append("” transformation from the factory.\n");
            indent(1); out.append(" *\n");
            printJavadocKeyValues("GIGS transformation code", code,
                                  "GIGS transformation name", name,
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
             * Some (not all) methods requires semi-major and semi-minor axis lengths.
             */
            indent(2);
            out.append("createDefaultParameters(\"")
               .append(GIGS_TO_EPSG_METHOD_NAME.getOrDefault(methodName, methodName))
               .append("\");\n");
            printParameterDefinitions(parameters);
            if (!NO_AXIS_LENGTHS.contains(methodName)) {
                indent(2);
                out.append("setEllipsoidAxisLengths();\n");
            }
            indent(2); out.append("verifyTransformation();\n");
            indent(1); out.append('}');
            saveTestMethod();
        }
        flushAllMethods();
    }

    /**
     * Writes a "create CRS" statement from GIGS code. According the header in GIGS test file,
     * codes in "Source CRS" and "Target CRS" columns are GIGS codes. But the following EPSG
     * codes are exceptions to this rule:
     *
     * <blockquote>4326</blockquote>
     *
     * Those exceptions are handled in a special way because, for example,
     * there is two GIGS methods is series 3205 for EPSG:4326.
     */
    private void writeCreateCRS(final String method, int code) {
        final int gigs;
        switch (code) {
            case 4326: gigs = 64003; break;
            default:   gigs = 0; break;
        }
        indent(2);
        out.append(method).append('(');
        if (gigs != 0) {
            out.append(code).append(", ");      // EPSG code.
            code = gigs;
        }
        out.append("Test3205::GIGS_").append(code).append(");\n");
    }
}
