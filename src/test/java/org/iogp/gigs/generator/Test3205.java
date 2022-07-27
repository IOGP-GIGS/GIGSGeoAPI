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
import java.util.Map;
import org.opengis.referencing.datum.DatumFactory;
import org.opengis.referencing.datum.DatumAuthorityFactory;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Code generator for {@link org.iogp.gigs.Test3205}. This generator needs to be executed only if the GIGS data changed.
 * The code is sent to the standard output; maintainer need to copy-and-paste the relevant methods to the test class,
 * but be aware that the original code may contain manual changes that need to be preserved.
 *
 * @author  Michael Arneson (INT)
 * @author  Martin Desruisseaux (Geomatys)
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
     * Creates a new test generator.
     */
    public Test3205() {
        libraryFactoryType = DatumAuthorityFactory.class;
        userFactoryType    = DatumFactory.class;
    }

    /**
     * Loads (GIGS code, EPSG code) mapping for datum.
     *
     * @return (GIGS code, EPSG code) of datum.
     * @throws IOException if an error occurred while reading the test data.
     */
    private static Map<Integer,Integer> loadDependencies() throws IOException {
        final DataParser data = new DataParser(Series.USER_DEFINED, "GIGS_user_3204_GeodeticDatum.txt",
                Integer.class, null, null, null, null, null, null, String.class);
        final Map<Integer,Integer> dependencies = new HashMap<>();
        while (data.next()) {
            final int[] codes = data.getInts(7);
            if (codes.length == 1) {
                assertNull(dependencies.put(data.getInt(0), codes[0]));
            }
        }
        return dependencies;
    }

    /**
     * Generates the code.
     *
     * @throws IOException if an error occurred while reading the test data.
     */
    private void run() throws IOException {
        // EPSG definitions
        final Map<Integer,Integer> datumsEPSG = loadDependencies();

        // GIGS definitions
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
            final int              code         = data.getInt    (0);
            final DefinitionSource source       = data.getSource (1);
            final String           name         = data.getString (2);
            final String           geodeticType = data.getString (3);
            final int              datumCode    = data.getInt    (4);
            final int              csCode       = data.getInt    (5);
            final int[]            codeEPSG     = data.getInts   (6);
            final String[]         nameEPSG     = data.getStrings(7);
            final String           remarks      = data.getString (9);

            final boolean isGeocentric;
            if (geodeticType.startsWith("Geographic")) {
                isGeocentric = false;
            } else if (geodeticType.equals("Geocentric")) {
                isGeocentric = true;
            } else {
                throw new AssertionError(geodeticType);
            }
            /*
             * TODO: "User Early-Bound" is not yet supported because we do not have the needed API in GeoAPI 3.0.
             */
            if (source == DefinitionSource.USER_EARLY_BOUND) {
                addUnsupportedTest(3205, code, name, "No method in GeoAPI for early-binding.");
                continue;
            }
            out.append('\n');
            indent(1); out.append("/**\n");
            indent(1); out.append(" * Tests “").append(name).append("” ").append(geodeticType).append(" CRS from the factory.\n");
            indent(1); out.append(" *\n");
            final var descriptions = new ArrayList<>(20);
            descriptions.addAll(Arrays.asList("GIGS CRS code", code,
                                              "GIGS CRS name", replaceAsciiPrimeByUnicode(name)));
            addCodesAndNames(descriptions, codeEPSG, nameEPSG);
            descriptions.addAll(Arrays.asList("Datum definition source", source,
                                              "Coordinate System code", csCode,
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
            indent(2); out.append("createDatum(");
            if (source == DefinitionSource.LIBRARY) {
                out.append(toEPSG(datumsEPSG, datumCode)).append(", ");
            }
            out.append("Test3204::GIGS_").append(datumCode).append(");\n");
            indent(2); out.append("csCode = ").append(csCode).append(";\n");
            if (isGeocentric) {
                indent(2); out.append("isGeocentric = true;\n");
            }
            indent(2); out.append(isGeocentric ? "verifyGeocentricCRS" : "verifyGeographicCRS").append("();\n");
            indent(1); out.append('}');
            saveTestMethod();
        }
        flushAllMethods();
    }

    /**
     * Returns the EPSG code associated to the given GIGS code in the map, making sure it is not null.
     *
     * @param  codes  the map from which to get the code.
     * @param  code   GIGS code of the object for which to get the EPSG code.
     * @return EPSG code for the named object.
     */
    private static int toEPSG(final Map<Integer,Integer> codes, final int code) {
        Integer epsg = codes.get(code);
        assertNotNull(epsg);
        return epsg;
    }
}
