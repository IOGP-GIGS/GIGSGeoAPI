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
import javax.measure.Unit;


/**
 * Code generator for {@link Test2201}. This generator needs to be executed only if the GIGS data changed.
 * The code is sent to the standard output; maintainer needs to copy-and-paste the relevant methods to the
 * test class, but be aware that the original code may contain manual changes that need to be preserved.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @version 1.0
 * @since   1.0
 */
public final class Test2201 extends TestMethodGenerator {
    /**
     * Launcher.
     *
     * @param  args  ignored.
     * @throws IOException if an error occurred while reading the test data.
     */
    public static void main(String[] args) throws IOException {
        new Test2201().run();
    }

    /**
     * Creates a new generator.
     */
    private Test2201() {
    }

    /**
     * Generates the code.
     *
     * @throws IOException if an error occurred while reading the test data.
     */
    private void run() throws IOException {
        final DataParser data = new DataParser(Series.PREDEFINED, "GIGS_lib_2201_Unit.txt",
                Integer.class,      // [0]: EPSG Unit of Measure Code
                String .class,      // [1]: Unit Type
                String .class,      // [2]: EPSG Unit of Measure Name
                String .class,      // [3]: Alias(es)
                Double .class,      // [4]: Base units per unit
                String .class,      // [5]: Base units per unit description
                String .class,      // [6]: EPSG Usage Extent
                String .class);     // [7]: GIGS Remarks

        while (data.next()) {
            final int      code        = data.getInt    (0);
            final String   type        = data.getString (1);
            final String   name        = data.getString (2);
            final String[] aliases     = data.getStrings(3);
            final double   unitToBase  = data.getDouble (4);
            final String   description = data.getString (5);
            final String   extent      = data.getString (6);
            final String   remarks     = data.getString (7);
            final Unit<?> base;
            if      (type.equalsIgnoreCase("Linear")) base = units.metre();
            else if (type.equalsIgnoreCase("Angle" )) base = units.radian();
            else if (type.equalsIgnoreCase("Scale" )) base = units.one();
            else throw new IOException("Unknown type: " + type);

            out.append('\n');
            indent(1); out.append("/**\n");
            indent(1); out.append(" * Tests “").append(name).append("” unit creation from the factory.\n");
            indent(1); out.append(" *\n");
            printJavadocKeyValues("EPSG UoM code", code,
                                  "Type", type,
                                  "Name of Units used in EPSG dataset", name,
                                  "Alias(es) given by EPSG", aliases,
                                  "Base units per unit", unitToBase,
                                  "Base units per unit description", description,
                                  "EPSG Usage Extent", extent);
            printRemarks(remarks);
            printJavadocThrows("if an error occurred while creating the unit from the EPSG code.");
            printTestMethodSignature(code, name);
            printFieldAssignments("code",       code,
                                  "name",       name,
                                  "aliases",    aliases,
                                  "unitToBase", unitToBase);
            indent(2); out.append("baseUnit   = ");
            printProgrammaticName(base);
            out.append(";\n");
            indent(2); out.append("verifyLinearConversions(createConverter());\n");
            indent(1); out.append('}');
            saveTestMethod();
        }
        flushAllMethods();
    }
}
