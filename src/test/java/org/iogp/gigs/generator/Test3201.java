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

/**
 * Code generator for {@link org.iogp.gigs.Test3201}. This generator needs to be executed only if the GIGS data changed.
 * The code is sent to the standard output; maintainer need to copy-and-paste the relevant methods to the test class,
 * but be aware that the original code may contain manual changes that need to be preserved.
 *
 * @author  Michael Arneson (INT)
 * @version 1.0
 * @since   1.0
 */
public class Test3201 extends TestMethodGenerator {
    /**
     * Launcher.
     *
     * @param  args  ignored.
     * @throws IOException if an error occurred while reading the test data.
     */
    public static void main(String[] args) throws IOException {
        new Test3201().run();
    }

    /**
     * Generates the code.
     *
     * @throws IOException if an error occurred while reading the test data.
     */
    private void run() throws IOException {
        final DataParser data = new DataParser(Series.USER_DEFINED, "GIGS_user_3201_Unit.txt",
                Integer.class,      // [ 0]: GIGS Unit of Measure Code
                String .class,      // [ 1]: Unit Type
                String .class,      // [ 2]: GIGS Unit of Measure Name
                Double .class,      // [ 3]: Base Units per Unit
                String .class,      // [ 4]: Base Units per Unit Description
                Integer.class,      // [ 5]: Equivalent EPSG Unit of Measure Code
                String .class,      // [ 6]: Equivalent EPSG Unit of Measure Name
                String .class);     // [ 7]: GIGS Remarks

        while (data.next()) {
            final int code = data.getInt(0);
            final String name = data.getString(2);
            final String unitType = data.getString(1);
            final double unitToBaseUnit = data.getDouble(3);
            final int codeEPSG = data.getInt(5);
            final String nameEPSG = data.getString(6);
            final String remarks = data.getString(7);

            /*
             * Write javadoc.
             */
            out.append('\n');
            indent(1); out.append("/**\n");
            indent(1); out.append(" * Tests “").append(name).append("” unit creation from the factory.\n");
            indent(1); out.append(" *\n");
            printJavadocKeyValues("GIGS unit", code,
                    "GIGS unit name", name,
                    "EPSG equivalence", codeAndName(codeEPSG, nameEPSG),
                    "Unit type", unitType,
                    "Base Units per Unit", unitToBaseUnit);
            printRemarks(remarks);
            printJavadocThrows("if an error occurred while creating the unit from the properties.");
            printJavadocSee(2201, EPSG + '_' + codeEPSG);
            /*
             * Write test method.
             */
            printTestMethodSignature(GIGS, code, name);
            printCallToSetCodeAndName(code, name);
            String quantityType = unitType;
            if (unitType.equals("Linear")) {
                quantityType = "Length";
            } else if (unitType.equals("Scale")) {
                quantityType = "Dimensionless";
            }
            indent(2); out.append("unitToBase = ").append(unitToBaseUnit).append(";\n");
            indent(2); out.append("baseUnit   = units.system.getUnit(").append(quantityType).append(".class);\n");
            indent(2); out.append("verifyLinearConversions(createConverter());\n");
            indent(1); out.append('}');
            saveTestMethod();
        }
        flushAllMethods();
    }
}
