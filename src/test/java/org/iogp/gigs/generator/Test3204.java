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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import static org.junit.jupiter.api.Assertions.*;


/**
 * Code generator for {@link org.iogp.gigs.Test3204}. This generator needs to be executed only if the GIGS data changed.
 * The code is sent to the standard output; maintainer need to copy-and-paste the relevant methods to the test class,
 * but be aware that the original code may contain manual changes that need to be preserved.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @version 1.0
 * @since   1.0
 */
public final class Test3204 extends TestMethodGenerator {
    /**
     * Launcher.
     *
     * @param  args  ignored.
     * @throws IOException if an error occurred while reading the test data.
     */
    public static void main(String[] args) throws IOException {
        new Test3204().run();
    }

    /**
     * Generates the code.
     *
     * @throws IOException if an error occurred while reading the test data.
     */
    private void run() throws IOException {
        // EPSG definitions
        final Map<String,Integer> ellipsoidsEPSG     = Test2204.loadDependencies("GIGS_lib_2202_Ellipsoid.txt");
        final Map<String,Integer> primeMeridiansEPSG = Test2204.loadDependencies("GIGS_lib_2203_PrimeMeridian.txt");

        // GIGS definitions
        final Map<String,Integer> ellipsoids     = loadDependencies("GIGS_user_3202_Ellipsoid.txt");
        final Map<String,Integer> primeMeridians = loadDependencies("GIGS_user_3203_PrimeMeridian.txt");
        final DataParser data = new DataParser(Series.USER_DEFINED, "GIGS_user_3204_GeodeticDatum.txt",
                Integer.class,    // [0]: GIGS Datum Code
                String .class,    // [1]: Datum Definition Source
                String .class,    // [2]: GIGS Datum Name
                String .class,    // [3]: GIGS/EPSG Ellipsoid Name (see GIGS Test Procedure 3202 or 2202)
                String .class,    // [4]: GIGS/EPSG Prime Meridian Name (see GIGS Test Procedure 3203 or 2203)
                String .class,    // [5]: GIGS Datum Origin
                Integer.class,    // [6]: Early-binding Transformation Code (see GIGS Test Procedure 3208 or 2208)
                String .class,    // [7]: Equivalent EPSG Datum Code
                String .class,    // [8]: Equivalent EPSG Datum Name
                String .class);   // [9]: GIGS Remarks

        while (data.next()) {
            final int         code              = data.getInt   (0);
            final String      name              = data.getString(2);
            final String      source            = data.getString(1);
            final String      ellipsoidName     = data.getString(3);
            final String      primeMeridianName = data.getString(4);
            final String      origin            = data.getString(5);
            final OptionalInt transformCode     = data.getIntOptional(6);
            final int[]       codeEPSG          = data.getInts   (7);
            final String[]    nameEPSG          = data.getStrings(8);
            final String      remarks           = data.getString (9);
            /*
             * Identify whether the components will be built from EPSG codes or user-supplied definitions.
             *
             * TODO: "User Early-Bound" is not yet supported because we do not have the needed API in GeoAPI 3.0.
             */
            final boolean librarySource = "Library".equalsIgnoreCase(source);
            if (!librarySource) {
                if ("User Early-Bound".equalsIgnoreCase(source)) {
                    continue;       // TODO: not yet supported.
                } else if (!"User".equalsIgnoreCase(source)) {
                    fail(source);
                }
            }
            /*
             * Write javadoc.
             */
            out.append('\n');
            indent(1); out.append("/**\n");
            indent(1); out.append(" * Tests “").append(name).append("” geodetic datum creation from the factory.\n");
            indent(1); out.append(" *\n");
            final var descriptions = new ArrayList<>(20);
            descriptions.addAll(Arrays.asList("GIGS datum code", code,
                                              "GIGS datum name", replaceAsciiPrimeByUnicode(name)));
            addCodesAndNames(descriptions, codeEPSG, nameEPSG);
            descriptions.addAll(Arrays.asList("Datum definition source", source,
                                              "Ellipsoid name", ellipsoidName,
                                              "Prime meridian name", primeMeridianName,
                                              "Datum origin", origin));
            printJavadocKeyValues(descriptions.toArray());
            printRemarks(remarks);
            printJavadocThrows("if an error occurred while creating the datum from the properties.");
            if (codeEPSG.length == 1) {
                printJavadocSee(2204, EPSG + '_' + codeEPSG[0]);
            }
            /*
             * Write test method.
             */
            printTestMethodSignature(GIGS, code, name);
            printCallToSetCodeAndName(code, name);
            if (origin != null) {
                indent(2);
                out.append("setOrigin(\"").append(origin).append("\");\n");
            }
            if (librarySource) {
                indent(2);
                out.append("createEllipsoid(")
                   .append(getCodeForName(ellipsoidsEPSG, ellipsoidName)).append(");\n");

                indent(2);
                out.append("createPrimeMeridian(")
                   .append(getCodeForName(primeMeridiansEPSG, primeMeridianName)).append(");\n");
            } else {
                indent(2);
                out.append("createEllipsoid(Test3202::GIGS_")
                   .append(getCodeForName(ellipsoids, ellipsoidName)).append(");\n");

                indent(2);
                out.append("createPrimeMeridian(Test3203::GIGS_")
                   .append(getCodeForName(primeMeridians, primeMeridianName)).append(");\n");
            }
            indent(2); out.append("verifyDatum();\n");
            indent(1); out.append('}');
            saveTestMethod();
        }
        flushAllMethods();
    }

    /**
     * Loads (object name, EPSG code) mapping from the given file.
     * Keys and values are the content of the second and first columns respectively.
     *
     * @param  file  the file to load.
     * @return (object name, EPSG code) inferred from the two first columns in the given file.
     * @throws IOException if an error occurred while reading the test data.
     */
    private static Map<String,Integer> loadDependencies(final String file) throws IOException {
        final DataParser data = new DataParser(Series.USER_DEFINED, file, Integer.class, String.class);
        final Map<String,Integer> dependencies = new HashMap<>();
        while (data.next()) {
            assertNull(dependencies.put(data.getString(1), data.getInt(0)));
        }
        return dependencies;
    }

    /**
     * Returns the code associated to the given name in the map, making sure it is not null.
     *
     * @param  codes  the map from which to get the code.
     * @param  name   name of the object for which to get the code.
     * @return EPSG or GIGS code for the named object.
     */
    private static int getCodeForName(final Map<String,Integer> codes, final String name) {
        Integer code = codes.get(name);
        assertNotNull(code, name);
        return code;
    }

    /**
     * Adds "EPSG equivalence" pairs for an arbitrary amount of EPSG equivalences.
     *
     * @param  addTo  where to add EPSG equivalence" pairs.
     * @param  codes  equivalent EPSG codes.
     * @param  names  equivalent EPSG names.
     */
    private static void addCodesAndNames(final List<Object> addTo, final int[] codes, final String[] names) {
        for (int i=0; i < codes.length; i++) {
            String label = "EPSG equivalence";
            if (codes.length != 1) {
                label = label + " (" + (i+1) + " of " + codes.length + ')';
            }
            addTo.add(label);
            addTo.add(codeAndName(codes[i], names[i]));
        }
    }
}
