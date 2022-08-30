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

import javax.measure.Unit;
import java.io.IOException;
import java.util.HashSet;
import java.util.OptionalInt;
import java.util.Set;


/**
 * Code generator for {@link org.iogp.gigs.Test3208}. This generator needs to be executed only if the GIGS data changed.
 * The code is sent to the standard output; maintainer need to copy-and-paste the relevant methods to the test class,
 * but be aware that the original code may contain manual changes that need to be preserved.
 *
 * @author  Michael Arneson (INT)
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
        // GIGS CRS codes
        final Set<Integer> crsCodes = loadDependencies("GIGS_user_3205_GeodeticCRS.txt");

        final DataParser data = new DataParser(Series.USER_DEFINED, "GIGS_user_3208_CoordTfm.txt",
                Integer.class,      // [ 0]: GIGS Transformation Code
                String .class,      // [ 1]: GIGS Transformation Name
                Integer.class,      // [ 2]: GIGS Source CRS Code (see GIGS Test Procedure 3205)
                String .class,      // [ 3]: GIGS Source CRS Name
                Integer.class,      // [ 4]: GIGS Target CRS Code (see GIGS Test Procedure 3205)
                String .class,      // [ 5]: GIGS Target CRS Name
                String .class,      // [ 6]: GIGS Transformation Variant
                String .class,      // [ 7]: EPSG Transformation Method Name
                String .class,      // [ 8]: Parameter 1 Name
                String .class,      // [ 9]: Parameter 1 Value
                String .class,      // [10]: Parameter 1 Unit
                String .class,      // [11]: Parameter 1 Value in decimal degrees
                String .class,      // [12]: Parameter 2 Name
                String .class,      // [13]: Parameter 2 Value
                String .class,      // [14]: Parameter 2 Unit
                String .class,      // [15]: Parameter 3 Name
                String .class,      // [16]: Parameter 3 Value
                String .class,      // [17]: Parameter 3 Unit
                String .class,      // [18]: Parameter 4 Name
                String .class,      // [19]: Parameter 4 Value
                String .class,      // [20]: Parameter 4 Unit
                String .class,      // [21]: Parameter 5 Name
                String .class,      // [22]: Parameter 5 Value
                String .class,      // [23]: Parameter 5 Unit
                String .class,      // [24]: Parameter 6 Name
                String .class,      // [25]: Parameter 6 Value
                String .class,      // [26]: Parameter 6 Unit
                String .class,      // [27]: Parameter 7 Name
                String .class,      // [28]: Parameter 7 Value
                String .class,      // [29]: Parameter 7 Unit
                String .class,      // [30]: Parameter 8 Name
                String .class,      // [31]: Parameter 8 Value
                String .class,      // [32]: Parameter 8 Unit
                String .class,      // [33]: Parameter 9 Name
                String .class,      // [34]: Parameter 9 Value
                String .class,      // [35]: Parameter 9 Unit
                String .class,      // [36]: Parameter 9 Name
                String .class,      // [37]: Parameter 9 Value
                String .class,      // [38]: Parameter 9 Unit
                Integer.class,      // [39]: Equivalent EPSG Transformation Code
                String .class,      // [40]: Equivalent EPSG Transformation Name
                String .class);     // [41]: GIGS Remarks

        while (data.next()) {
            final int    code                     = data.getInt         ( 0);
            final String name                     = data.getString      ( 1);
            final int    sourceCRSCode            = data.getInt         ( 2);
            final int    targetCRSCode            = data.getInt         ( 4);
            final String methodName               = data.getString      ( 7);

            final String parameter1Name           = data.getString      ( 8);
            final String parameter1Value          = data.getString      ( 9);
            final String parameter1Unit           = data.getString      (10);
            final String parameter1ValueInDegrees = data.getString      (11);
            final String parameter2Name           = data.getString      (12);
            final String parameter2Value          = data.getString      (13);
            final String parameter2Unit           = data.getString      (14);
            final String parameter3Name           = data.getString      (15);
            final String parameter3Value          = data.getString      (16);
            final String parameter3Unit           = data.getString      (17);
            final String parameter4Name           = data.getString      (18);
            final String parameter4Value          = data.getString      (19);
            final String parameter4Unit           = data.getString      (20);
            final String parameter5Name           = data.getString      (21);
            final String parameter5Value          = data.getString      (22);
            final String parameter5Unit           = data.getString      (23);
            final String parameter6Name           = data.getString      (24);
            final String parameter6Value          = data.getString      (25);
            final String parameter6Unit           = data.getString      (26);
            final String parameter7Name           = data.getString      (27);
            final String parameter7Value          = data.getString      (28);
            final String parameter7Unit           = data.getString      (29);
            final String parameter8Name           = data.getString      (30);
            final String parameter8Value          = data.getString      (31);
            final String parameter8Unit           = data.getString      (32);
            final String parameter9Name           = data.getString      (33);
            final String parameter9Value          = data.getString      (34);
            final String parameter9Unit           = data.getString      (35);
            final String parameter10Name          = data.getString      (36);
            final String parameter10Value         = data.getString      (37);
            final String parameter10Unit          = data.getString      (38);
            final OptionalInt codeEPSG            = data.getIntOptional (39);
            final String nameEPSG                 = data.getString      (40);
            final String remarks                  = data.getString      (41);
            /*
             * Write javadoc.
             */
            out.append('\n');
            indent(1); out.append("/**\n");
            indent(1); out.append(" * Tests “").append(name).append("” ")
                          .append(" transformation from the factory.\n");
            indent(1); out.append(" *\n");
            printJavadocKeyValues("GIGS transformation code", code,
                                  "GIGS transformation name", name,
                                  "EPSG Transformation Method", methodName,
                                  "EPSG equivalence", codeAndName(codeEPSG, nameEPSG));
            printJavadocParameterHeader("Transformation parameters");
            printJavadocParameterString(parameter1Name, parameter1Value, parameter1Unit, parameter1ValueInDegrees);
            printJavadocParameterString(parameter2Name, parameter2Value, parameter2Unit);
            printJavadocParameterString(parameter3Name, parameter3Value, parameter3Unit);
            printJavadocParameterString(parameter4Name, parameter4Value, parameter4Unit);
            printJavadocParameterString(parameter5Name, parameter5Value, parameter5Unit);
            printJavadocParameterString(parameter6Name, parameter6Value, parameter6Unit);
            printJavadocParameterString(parameter7Name, parameter7Value, parameter7Unit);
            printJavadocParameterString(parameter8Name, parameter8Value, parameter8Unit);
            printJavadocParameterString(parameter9Name, parameter9Value, parameter9Unit);
            printJavadocParameterString(parameter10Name, parameter10Value, parameter10Unit);
            printJavadocTableFooter();
            printRemarks(remarks);
            printJavadocThrows("if an error occurred while creating the transformation from the properties.");

            /*
             * Write test method.
             */
            printTestMethodSignature(GIGS, code, name);
            printCallToSetCodeAndName(code, name);
            indent(2); out.append("properties.put(CoordinateOperation.OPERATION_VERSION_KEY, \"GIGS Transformation\");\n");
            printTransformationMethodName(methodName);
            indent(2); out.append("createDefaultParameters();\n");
            //specify the source crs (either from gigs or epsg)
            if (crsCodes.contains(sourceCRSCode)) {
                indent(2);
                out.append("createSourceCRS(Test3205::GIGS_")
                        .append(sourceCRSCode).append(");\n");
            } else {
                indent(2);
                out.append("createSourceCRS(")
                        .append(sourceCRSCode).append(");\n");
            }
            //specify the target crs (either from gigs or epsg)
            if (crsCodes.contains(targetCRSCode)) {
                indent(2);
                out.append("createTargetCRS(Test3205::GIGS_")
                        .append(targetCRSCode).append(");\n");
            } else {
                indent(2);
                out.append("createTargetCRS(")
                        .append(targetCRSCode).append(");\n");
            }

            printParameterString(parameter1Name, parameter1Value, parameter1Unit, parameter1ValueInDegrees);
            printParameterString(parameter2Name, parameter2Value, parameter2Unit);
            printParameterString(parameter3Name, parameter3Value, parameter3Unit);
            printParameterString(parameter4Name, parameter4Value, parameter4Unit);
            printParameterString(parameter5Name, parameter5Value, parameter5Unit);
            printParameterString(parameter6Name, parameter6Value, parameter6Unit);
            printParameterString(parameter7Name, parameter7Value, parameter7Unit);
            printParameterString(parameter8Name, parameter8Value, parameter8Unit);
            printParameterString(parameter9Name, parameter9Value, parameter9Unit);
            printParameterString(parameter10Name, parameter10Value, parameter10Unit);

            if (!methodName.equalsIgnoreCase("ntv2") && !methodName.equalsIgnoreCase("nadcon")
                    && !methodName.equalsIgnoreCase("Longitude rotation")) {
                indent(2);
                out.append("Ellipsoid sourceEllipsoid = sourceCRS.getDatum().getEllipsoid();\n");
                indent(2);
                out.append("parameterValueGroup.parameter(\"src_semi_major\").setValue(sourceEllipsoid.getSemiMajorAxis(), sourceEllipsoid.getAxisUnit());\n");
                indent(2);
                out.append("parameterValueGroup.parameter(\"src_semi_minor\").setValue(sourceEllipsoid.getSemiMinorAxis(), sourceEllipsoid.getAxisUnit());\n");
                indent(2);
                out.append("Ellipsoid targetEllipsoid = targetCRS.getDatum().getEllipsoid();\n");
                indent(2);
                out.append("parameterValueGroup.parameter(\"tgt_semi_major\").setValue(targetEllipsoid.getSemiMajorAxis(), targetEllipsoid.getAxisUnit());\n");
                indent(2);
                out.append("parameterValueGroup.parameter(\"tgt_semi_minor\").setValue(targetEllipsoid.getSemiMinorAxis(), targetEllipsoid.getAxisUnit());\n");
            }
            indent(2); out.append("verifyTransformation();\n");
            indent(1); out.append('}');
            saveTestMethod();
        }
        flushAllMethods();
    }

    /**
     * Prints the parameter entry (name, value, unit) for javadoc function header.
     *
     * @param parameterName   the parameter name.
     * @param parameterValue  the parameter value.
     * @param parameterUnit   the unit.
     * @param parameterValueAsDec the parameter value in decimal degrees if available.
     */
    private void printJavadocParameterString(String parameterName, String parameterValue, String parameterUnit, String... parameterValueAsDec) {
        if (parameterName == null || parameterName.equals("NULL")) {
            return;
        }
        if (parameterUnit != null && parameterUnit.equals("sexagesimal DMS") && parameterValueAsDec != null && parameterValueAsDec.length > 0) {
            printJavadocParameterRow(parameterName, Double.valueOf(parameterValueAsDec[0]), "degree", Double.NaN);
            return;
        }
        if (parameterUnit != null && parameterUnit.equals("NULL")) {
            parameterUnit = null;
        }
        printJavadocParameterRow(parameterName, Double.valueOf(parameterValue), parameterUnit, Double.NaN);
    }

    /**
     * Prints the programmatic line that adds a parameter to a parameter group.
     *
     * @param parameterName   the parameter name.
     * @param parameterValue  the parameter value.
     * @param parameterUnit   the unit.
     * @param parameterValueAsDec the parameter value in decimal degrees if available.
     */
    private void printParameterString(String parameterName, String parameterValue, String parameterUnit, String... parameterValueAsDec) {
        if (parameterName == null || parameterName.equals("NULL")) {
            return;
        }
        indent(2);out.append("parameterValueGroup.parameter(\"").append(parameterName).append("\")");
        if (parameterUnit != null && parameterUnit.equals("sexagesimal DMS") && parameterValueAsDec != null) {
            parameterUnit = "degree";
            parameterValue = parameterValueAsDec[0];
        }
        try {
            Double.parseDouble(parameterValue);
        } catch(Exception ex) {
            //parameter is not a number
            parameterValue = "\"" + parameterValue + "\"";
        }
        Unit<?> unit = parseUnit(parameterUnit);
        out.append(".setValue(").append(parameterValue);
        if (unit != null) {
            out.append(", ");
            printProgrammaticName(unit);
        }
        out.append(");\n");
    }

    /**
     * Prints the programmatic line that specifies the trajectory method. The transform method name
     * specified in the GIGS testing file is converted to method name used for GeoAPI.
     *
     * @param gigsTransformationMethodName  the trajectory method as specified in the GIGS testing file.
     */
    private void printTransformationMethodName(String gigsTransformationMethodName) {
        indent(2);out.append("methodName = ");
        switch(gigsTransformationMethodName) {
            case "Geocentric translations":
                out.append("\"Geocentric translations (geog2D domain)\";\n");
                break;
            case "Position Vector 7-param. transformation":
                out.append("\"Position Vector transformation (geog2D domain)\";\n");
                break;
            case "Coordinate Frame rotation":
                out.append("\"Coordinate Frame rotation (geog2D domain)\";\n");
                break;
            case "Molodensky-Badekas 10-parameter transformation":
                out.append("\"Molodensky\";\n");
                break;
            default:
                out.append("\"").append(gigsTransformationMethodName).append("\";\n");
        }
    }

    /**
     * Loads set of GIGS codes from the given file.
     * Keys are the content of the first column.
     *
     * @param  file  the file to load.
     * @return GIGS codes inferred from the first columns in the given file.
     * @throws IOException if an error occurred while reading the test data.
     */
    private static Set<Integer> loadDependencies(final String file) throws IOException {
        final DataParser data = new DataParser(Series.USER_DEFINED, file, Integer.class);
        final Set<Integer> dependencies = new HashSet<>();
        while (data.next()) {
            dependencies.add(data.getInt(0));
        }
        return dependencies;
    }
}
