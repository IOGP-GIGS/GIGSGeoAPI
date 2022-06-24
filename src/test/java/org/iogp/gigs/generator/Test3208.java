package org.iogp.gigs.generator;

import org.iogp.gigs.internal.geoapi.Units;

import javax.measure.Unit;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.OptionalInt;

/**
 * Code generator for {@link org.iogp.gigs.Test3208}. This generator needs to be executed only if the GIGS data changed.
 * The code is sent to the standard output; maintainer need to copy-and-paste the relevant methods to the test class,
 * but be aware that the original code may contain manual changes that need to be preserved.
 *
 * @author  Michael Arneson (INT)
 * @version 1.0
 * @since   1.0
 */
public class Test3208 extends TestMethodGenerator {
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
            int    code                     = data.getInt         ( 0);
            String name                     = data.getString      ( 1);
            int    sourceCRSCode            = data.getInt         ( 2);
            int    targetCRSCode            = data.getInt         ( 4);
            String methodName               = data.getString      ( 7);

            String parameter1Name           = data.getString      ( 8);
            String parameter1Value          = data.getString      ( 9);
            String parameter1Unit           = data.getString      (10);
            String parameter1ValueInDegrees = data.getString      (11);
            String parameter2Name           = data.getString      (12);
            String parameter2Value          = data.getString      (13);
            String parameter2Unit           = data.getString      (14);
            String parameter3Name           = data.getString      (15);
            String parameter3Value          = data.getString      (16);
            String parameter3Unit           = data.getString      (17);
            String parameter4Name           = data.getString      (18);
            String parameter4Value          = data.getString      (19);
            String parameter4Unit           = data.getString      (20);
            String parameter5Name           = data.getString      (21);
            String parameter5Value          = data.getString      (22);
            String parameter5Unit           = data.getString      (23);
            String parameter6Name           = data.getString      (24);
            String parameter6Value          = data.getString      (25);
            String parameter6Unit           = data.getString      (26);
            String parameter7Name           = data.getString      (27);
            String parameter7Value          = data.getString      (28);
            String parameter7Unit           = data.getString      (29);
            String parameter8Name           = data.getString      (30);
            String parameter8Value          = data.getString      (31);
            String parameter8Unit           = data.getString      (32);
            String parameter9Name           = data.getString      (33);
            String parameter9Value          = data.getString      (34);
            String parameter9Unit           = data.getString      (35);
            String parameter10Name          = data.getString      (36);
            String parameter10Value         = data.getString      (37);
            String parameter10Unit          = data.getString      (38);
            OptionalInt codeEPSG            = data.getIntOptional (39);
            String nameEPSG                 = data.getString      (40);
            String remarks                  = data.getString      (41);

            /*
             * Write javadoc.
             */
            out.append('\n');
            indent(1); out.append("/**\n");
            indent(1); out.append(" * Tests “").append(name).append("” ")
                    .append(" transformation from the factory.\n");
            indent(1); out.append(" *\n");
            final var descriptions = new ArrayList<>();
            descriptions.addAll(Arrays.asList("GIGS transformation code", code,
                    "GIGS transformation name", name,
                    "EPSG Transformation Method", methodName));
            if (codeEPSG.isPresent()) {
                descriptions.addAll(Arrays.asList("EPSG equivalence", codeAndName(codeEPSG.getAsInt(), nameEPSG)));
            }
            printJavadocKeyValues(descriptions.toArray());
            printParameterTableHeader("Transformation parameters");
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
            printParameterTableFooter();
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
            printCRSReference("createSourceCRS", sourceCRSCode);
            printCRSReference("createTargetCRS", targetCRSCode);

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

    private void printJavadocParameterString(String parameterName, String parameterValue, String parameterUnit, String... parameterValueAsDec) {
        if (parameterName == null || parameterName.equals("NULL")) {
            return;
        }
        if (parameterUnit != null && parameterUnit.equals("sexagesimal DMS") && parameterValueAsDec != null && parameterValueAsDec.length > 0) {
            printParameterTableRow(parameterName, parameterValueAsDec[0], "degree");
            return;
        }
        if (parameterUnit != null && parameterUnit.equals("NULL")) {
            parameterUnit = null;
        }
        printParameterTableRow(parameterName, parameterValue, parameterUnit);
    }

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

    private void printTransformationMethodName(String transformationMethodName) {
        indent(2);out.append("methodName = ");
        switch(transformationMethodName) {
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
                out.append("\"").append(transformationMethodName).append("\";\n");
        }
    }

    private void printCRSReference(String functionName, int code) {
        indent(2);out.append(functionName).append("(");
        if (code > 64000 && code < 65000) {
            out.append("Test3205Geog2DCRS::GIGS_").append(code);
        } else {
            out.append(code);
        }
        out.append(");\n");
    }

}
