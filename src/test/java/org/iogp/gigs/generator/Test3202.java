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
import javax.measure.quantity.Length;


/**
 * Code generator for {@link org.iogp.gigs.Test3202}. This generator needs to be executed only if the GIGS data changed.
 * The code is sent to the standard output; maintainers need to copy-and-paste the relevant methods to the test class,
 * but be aware that the original code may contain manual changes that need to be preserved.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @version 1.0
 * @since   1.0
 */
public final class Test3202 extends TestMethodGenerator {
    /**
     * Launcher.
     *
     * @param  args  ignored.
     * @throws IOException if an error occurred while reading the test data.
     */
    public static void main(String[] args) throws IOException {
        new Test3202().run();
    }

    /**
     * Creates a new test methods generator.
     */
    private Test3202() {
    }

    /**
     * Generates the code.
     *
     * @throws IOException if an error occurred while reading the test data.
     */
    private void run() throws IOException {
        final DataParser data = new DataParser(Series.USER_DEFINED, "GIGS_user_3202_Ellipsoid.txt",
                Integer.class,      // [ 0]: GIGS Ellipsoid Code
                String .class,      // [ 1]: GIGS Ellipsoid Name
                Double .class,      // [ 2]: Semi-major axis (a)
                String .class,      // [ 3]: Unit Name
                Double .class,      // [ 4]: Second Defining Parameter: Inverse flattening (1/f
                Double .class,      // [ 5]: Second Defining Parameter: Semi-minor axis (b)
                Boolean.class,      // [ 6]: Is spherical?
                Double .class,      // [ 7]: Unit Conversion Factor
                Double .class,      // [ 8]: Semi-major axis (a) in metres
                Integer.class,      // [ 9]: Equivalent EPSG Ellipsoid Code
                String .class,      // [10]: Equivalent EPSG Ellipsoid Name
                String .class);     // [11]: GIGS Remarks

        while (data.next()) {
            int     code              = data.getInt    ( 0);
            String  name              = data.getString ( 1);
            int     codeEPSG          = data.getInt    ( 9);
            String  nameEPSG          = data.getString (10);
            String  unitName          = data.getString ( 3);
            double  toMetres          = data.getDouble ( 7);
            double  semiMajorInMetres = data.getDouble ( 8);
            double  semiMajorAxis     = data.getDouble ( 2);
            double  semiMinorAxis     = data.getDouble ( 5);
            double  inverseFlattening = data.getDouble ( 4);
            boolean isSphere          = data.getBoolean( 6);
            String  remarks           = data.getString (11);
            boolean isIvfDefinitive   = inverseFlattening > 0;
            /*
             * GIGS test file use 0 for "not specified". We need to replace those values by NaN
             * for allowing code generation to skip Javadoc or Java code for those values.
             */
            if (semiMajorInMetres <= 0) {
                semiMajorInMetres = Double.NaN;
            }
            if (toMetres <= 0) {
                toMetres = Double.NaN;
            }
            /*
             * The GIGS test file provides only one of `semiMinorAxis` or `inverseFlattening` values.
             * The other value is left to 0. Compute the missing value for allowing tests to compare
             * the value computed by the implementation.
             */
            if (!(semiMinorAxis > 0)) {                                 // Use `!` for catching NaN.
                if (!isIvfDefinitive) {
                    semiMinorAxis     = semiMajorAxis;
                    inverseFlattening = Double.POSITIVE_INFINITY;
                } else {
                    semiMinorAxis = semiMajorAxis - semiMajorAxis/inverseFlattening;
                }
            } else if (!isIvfDefinitive) {
                inverseFlattening = semiMajorAxis / (semiMajorAxis - semiMinorAxis);
            }
            /*
             * Count how many significant digits there is in the axis length.
             * If the semi-major axis length was computed by above code instead
             * of provided by GIGS data, round to the same amount of digits.
             */
            double axisTolerance = 10, scaled;
            while ((scaled = semiMajorAxis * axisTolerance) != Math.rint(scaled)) {
                axisTolerance *= 10;
            }
            if (isIvfDefinitive) {
                semiMinorAxis = Math.rint(semiMinorAxis * axisTolerance) / axisTolerance;
            }
            axisTolerance = 0.5 / axisTolerance;
            /*
             * Write javadoc.
             */
            out.append('\n');
            indent(1); out.append("/**\n");
            indent(1); out.append(" * Tests “").append(name).append("” ")
                    .append(isSphere ? "sphere" : isIvfDefinitive ? "flattened sphere" : "ellipsoid")
                    .append(" creation from the factory.\n");
            indent(1); out.append(" *\n");
            printJavadocKeyValues("GIGS ellipsoid code", code,
                                  "GIGS ellipsoid name", name,
                                  "EPSG equivalence", codeAndName(codeEPSG, nameEPSG),
                                  "Semi-major axis (<var>a</var>)", quantityAndAlternative(semiMajorAxis, unitName, semiMajorInMetres, "metres"),
                                  "Semi-minor axis (<var>b</var>)", quantityAndAlternative(semiMinorAxis, unitName, semiMinorAxis*toMetres, "metres"),
                                  "Inverse flattening (1/<var>f</var>)", inverseFlattening);
            printRemarks(remarks);
            printJavadocThrows("if an error occurred while creating the ellipsoid from the properties.");
            printJavadocSee(2202, EPSG + '_' + codeEPSG);
            /*
             * Write test method.
             */
            final Unit<Length> axisUnit = parseLinearUnit(unitName);
            printTestMethodSignature(GIGS, code, name);
            printCallToSetCodeAndName(code, name);
            printFieldAssignments("semiMajorInMetres", semiMajorInMetres,
                                  "semiMajorAxis",     semiMajorAxis,
                                  "semiMinorAxis",     semiMinorAxis,
                                  "axisUnit",          axisUnit,
                                  "axisTolerance",     axisTolerance,
                                  "inverseFlattening", inverseFlattening,
                                  "isIvfDefinitive",   isIvfDefinitive,
                                  "isSphere",          isSphere);

            indent(2); out.append("verifyEllipsoid();\n");
            indent(1); out.append('}');
            saveTestMethod();
        }
        flushAllMethods();
    }
}
