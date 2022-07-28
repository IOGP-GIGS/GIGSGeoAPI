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
import java.io.IOException;
import java.util.HashMap;
import org.opengis.referencing.datum.Ellipsoid;
import org.opengis.referencing.datum.PrimeMeridian;

import static org.junit.jupiter.api.Assertions.assertNull;


/**
 * Code generator for {@link org.iogp.gigs.Test2204}. This generator needs to be executed only if the GIGS data changed.
 * The code is sent to the standard output; maintainer need to copy-and-paste the relevant methods to the test class,
 * but be aware that the original code may contain manual changes that need to be preserved.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @version 1.0
 * @since   1.0
 */
public final class Test2204 extends TestMethodGenerator {
    /**
     * Launcher.
     *
     * @param  args  ignored.
     * @throws IOException if an error occurred while reading the test data.
     */
    public static void main(String[] args) throws IOException {
        new Test2204().run();
    }

    /**
     * Creates a new test methods generator.
     */
    private Test2204() {
    }

    /**
     * Loads (object name, EPSG code) mapping from the ellipsoids or prime meridians test file.
     * Keys and values are the content of the second and first columns respectively.
     * The file is assumed a member of {@link Series#PREDEFINED}.
     *
     * @param  type of object for which to load the dependency map.
     * @return (object name, EPSG code) inferred from the two first columns in the given file.
     * @throws IOException if an error occurred while reading the test data.
     */
    static Map<String,Integer> loadDependencies(final Class<?> type) throws IOException {
        final String file;
        if (type == Ellipsoid.class) {
            file = "GIGS_lib_2202_Ellipsoid.txt";
        } else if (type == PrimeMeridian.class) {
            file = "GIGS_lib_2203_PrimeMeridian.txt";
        } else {
            throw new IllegalArgumentException(String.valueOf(type));
        }
        final DataParser data = new DataParser(Series.PREDEFINED, file, Integer.class, String.class);
        final Map<String,Integer> dependencies = new HashMap<>();
        while (data.next()) {
            assertNull(dependencies.put(data.getString(1), data.getInt(0)));
        }
        return dependencies;
    }

    /**
     * Generates the code.
     *
     * @throws IOException if an error occurred while reading the test data.
     */
    private void run() throws IOException {
        final Map<String,Integer> ellipsoids     = loadDependencies(Ellipsoid.class);
        final Map<String,Integer> primeMeridians = loadDependencies(PrimeMeridian.class);
        final DataParser data = new DataParser(Series.PREDEFINED, "GIGS_lib_2204_GeodeticDatum.txt",
                Integer.class,      // [0]: EPSG Datum Code
                String .class,      // [1]: EPSG Datum Name
                String .class,      // [2]: Alias(es)
                String .class,      // [3]: Ellipsoid Name
                String .class,      // [4]: Prime Meridian Name
                String .class,      // [5]: EPSG Usage Extent
                String .class);     // [6]: GIGS Remarks

        while (data.next()) {
            final int      code              = data.getInt    (0);
            final String   name              = data.getString (1);
            final String[] aliases           = data.getStrings(2);
            final String   ellipsoidName     = data.getString (3);
            final String   primeMeridianName = data.getString (4);
            final String   extent            = data.getString (5);
            final String   remarks           = data.getString (6);

            out.append('\n');
            indent(1); out.append("/**\n");
            indent(1); out.append(" * Tests “").append(name).append("” geodetic datum creation from the factory.\n");
            indent(1); out.append(" *\n");
            printJavadocKeyValues("EPSG datum code", code,
                                  "EPSG datum name", name,
                                  "Alias(es) given by EPSG", aliases,
                                  "Ellipsoid name", ellipsoidName,
                                  "Prime meridian name", primeMeridianName,
                                  "EPSG Usage Extent", extent);
            printRemarks(remarks);
            printJavadocThrows("if an error occurred while creating the datum from the EPSG code.");
            printTestMethodSignature(EPSG, code, name);
            printFieldAssignments("code",              code,
                                  "name",              name,
                                  "aliases",           aliases,
                                  "ellipsoidName",     ellipsoidName,
                                  "primeMeridianName", primeMeridianName);
            indent(2); out.append("verifyDatum();\n");
            printCallToDependencyTest("ellipsoidTest", ellipsoids.get(ellipsoidName));
            printCallToDependencyTest("primeMeridianTest", primeMeridians.get(primeMeridianName));
            indent(1); out.append('}');
            saveTestMethod();
        }
        flushAllMethods();
    }
}
