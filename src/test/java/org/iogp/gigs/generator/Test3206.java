package org.iogp.gigs.generator;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.OptionalInt;

/**
 * Code generator for {@link org.iogp.gigs.Test3206}. This generator needs to be executed only if the GIGS data changed.
 * The code is sent to the standard output; maintainer need to copy-and-paste the relevant methods to the test class,
 * but be aware that the original code may contain manual changes that need to be preserved.
 *
 * @author  Michael Arneson (INT)
 * @version 1.0
 * @since   1.0
 */
public class Test3206 extends TestMethodGenerator {

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
            int    code                     = data.getInt           ( 0);
            String name                     = data.getString        ( 1);
            String conversionName           = data.getString        ( 2);

            String parameter1Name           = data.getString        (3);
            Double parameter1Value          = data.getDouble        (4);
            String parameter1Unit           = data.getString        (5);
            Double parameter1ValueInDegrees = data.getDouble        (6);

            String parameter2Name           = data.getString        (7);
            Double parameter2Value          = data.getDouble        (8);
            String parameter2Unit           = data.getString        (9);
            Double parameter2ValueInDegrees = data.getDouble        (10);

            String parameter3Name           = data.getString        (11);
            Double parameter3Value          = data.getDouble        (12);
            String parameter3Unit           = data.getString        (13);
            Double parameter3ValueInDegrees = data.getDouble        (14);

            String parameter4Name           = data.getString        (15);
            Double parameter4Value          = data.getDouble        (16);
            String parameter4Unit           = data.getString        (17);
            Double parameter4ValueInDegrees = data.getDouble        (18);

            String parameter5Name           = data.getString        (19);
            Double parameter5Value          = data.getDouble        (20);
            String parameter5Unit           = data.getString        (21);

            String parameter6Name           = data.getString        (22);
            Double parameter6Value          = data.getDouble        (23);
            String parameter6Unit           = data.getString        (24);

            String parameter7Name           = data.getString        (25);
            Double parameter7Value          = data.getDouble        (26);
            String parameter7Unit           = data.getString        (27);

            OptionalInt codeEPSG            = data.getIntOptional   (28);
            String nameEPSG                 = data.getString        (29);
            String remarks                  = data.getString        (30);

            /*
             * Write javadoc.
             */
            out.append('\n');
            indent(1); out.append("/**\n");
            indent(1); out.append(" * Tests “").append(name).append("” ")
                    .append(" conversion from the factory.\n");
            indent(1); out.append(" *\n");
            final var descriptions = new ArrayList<>();
            descriptions.addAll(Arrays.asList("GIGS conversion code", code,
                    "GIGS conversion name", name));
            if (codeEPSG.isPresent()) {
                descriptions.addAll(Arrays.asList("EPSG equivalence", codeAndName(codeEPSG.getAsInt(), nameEPSG)));
            }

            printJavadocKeyValues(descriptions.toArray());
            printParameterTableHeader("Conversion parameters");
            printJavadocParameterString(parameter1Name, parameter1Value, parameter1Unit, parameter1ValueInDegrees);
            printJavadocParameterString(parameter2Name, parameter2Value, parameter2Unit, parameter2ValueInDegrees);
            printJavadocParameterString(parameter3Name, parameter3Value, parameter3Unit, parameter3ValueInDegrees);
            printJavadocParameterString(parameter4Name, parameter4Value, parameter4Unit, parameter4ValueInDegrees);
            printJavadocParameterString(parameter4Name, parameter4Value, parameter4Unit, parameter4ValueInDegrees);
            printJavadocParameterString(parameter5Name, parameter5Value, parameter5Unit);
            printJavadocParameterString(parameter6Name, parameter6Value, parameter6Unit);
            printJavadocParameterString(parameter7Name, parameter7Value, parameter7Unit);
            printParameterTableFooter();
            printRemarks(remarks);
            printJavadocThrows("if an error occurred while creating the conversion from the properties.");

            printTestMethodSignature(GIGS, code, name);
            printCallToSetCodeAndName(code, name);
            printFieldAssignments("methodName", conversionName);
            indent(2);out.append("createDefaultParameters();\n");
            printUnits(parameter1Unit, parameter2Unit, parameter3Unit, parameter4Unit, parameter5Unit, parameter6Unit, parameter7Unit);


            printParameterString(parameter1Name, parameter1Value, parameter1Unit, parameter1ValueInDegrees);
            printParameterString(parameter2Name, parameter2Value, parameter2Unit, parameter2ValueInDegrees);
            printParameterString(parameter3Name, parameter3Value, parameter3Unit, parameter3ValueInDegrees);
            printParameterString(parameter4Name, parameter4Value, parameter4Unit, parameter4ValueInDegrees);
            printParameterString(parameter4Name, parameter4Value, parameter4Unit, parameter4ValueInDegrees);
            printParameterString(parameter5Name, parameter5Value, parameter5Unit);
            printParameterString(parameter6Name, parameter6Value, parameter6Unit);
            printParameterString(parameter7Name, parameter7Value, parameter7Unit);
            indent(2);out.append("verifyConversion();\n");
            indent(1); out.append('}');
            saveTestMethod();
        }
        flushAllMethods();
    }

    private void printParameterString(String parameterName, Double parameterValue, String parameterUnit, Double... parameterValueAsDec) {
        if (parameterName == null || parameterName.equals("NULL")) {
            return;
        }
        indent(2);out.append("definition.parameter(\"").append(parameterName).append("\")");
        if (parameterUnit.equals("US survey foot")) {
            parameterUnit = "footSurveyUS";
        } else if (parameterUnit.equals("Unity")) {
            parameterUnit = "unity";
        }

        if (parameterUnit != null && parameterUnit.equals("sexagesimal DMS") && parameterValueAsDec != null) {
            out.append(".setValue(").append(parameterValueAsDec[0]).append(",degree);\n");
            return;
        }
        out.append(".setValue(").append(parameterValue);
        if (parameterUnit != null) {
            out.append(", ").append(parameterUnit);
        }
        out.append(");\n");
    }

    private void printJavadocParameterString(String parameterName, Double parameterValue, String parameterUnit, Double... parameterValueAsDec) {
        if (parameterName == null || parameterName.equals("NULL")) {
            return;
        }
        if (parameterUnit != null && parameterUnit.equals("sexagesimal DMS") && parameterValueAsDec != null && parameterValueAsDec.length > 0) {
            printParameterTableRow(parameterName, String.valueOf(parameterValueAsDec[0]), "degree");
            return;
        }
        printParameterTableRow(parameterName, String.valueOf(parameterValue), parameterUnit);
    }

    private void printUnits(final String... parameterUnits) {
        List<String> parameterUnitList = List.of(parameterUnits);
        if (parameterUnitList.contains("degree") || parameterUnitList.contains("sexagesimal DMS")) {
            indent(2);out.append("final Unit<Angle> degree = units.degree();\n");
        }
        if (parameterUnitList.contains("grad")) {
            indent(2);out.append("final Unit<Angle> grad = units.grad();\n");
        }
        if (parameterUnitList.contains("metre")) {
            indent(2);out.append("final Unit<Length> metre = units.metre();\n");
        }
        if (parameterUnitList.contains("Unity")) {
            indent(2);out.append("final Unit<Dimensionless> unity = units.one();\n");
        }
        if (parameterUnitList.contains("US survey foot")) {
            indent(2);out.append("final Unit<Length> footSurveyUS = units.footSurveyUS();\n");
        }
        if (parameterUnitList.contains("foot")) {
            indent(2);out.append("final Unit<Length> foot = units.foot();\n");
        }
    }
}
