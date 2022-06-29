package org.iogp.gigs.generator;

import javax.measure.Unit;
import java.io.IOException;
import java.util.*;

/**
 * Code generator for {@link org.iogp.gigs.Test3211}. This generator needs to be executed only if the GIGS data changed.
 * The code is sent to the standard output; maintainer need to copy-and-paste the relevant methods to the test class,
 * but be aware that the original code may contain manual changes that need to be preserved.
 *
 * @author  Michael Arneson (INT)
 * @version 1.0
 * @since   1.0
 */
public class Test3211 extends TestMethodGenerator {
    /**
     * Launcher.
     *
     * @param  args  ignored.
     * @throws IOException if an error occurred while reading the test data.
     */
    public static void main(String[] args) throws IOException {
        new Test3211().run();
    }

    /**
     * Generates the code.
     *
     * @throws IOException if an error occurred while reading the test data.
     */
    private void run() throws IOException {
        // GIGS crs codes
        final Set<Integer> crsCodes = loadDependencies("GIGS_user_3210_VerticalCRS.txt");

        final DataParser data = new DataParser(Series.USER_DEFINED, "GIGS_user_3211_VertTfm.txt",
                Integer.class,      // [ 0]: GIGS Transformation Code
                Integer.class,      // [ 1]: GIGS Source CRS Code (see GIGS Test Procedure 3210)
                String .class,      // [ 2]: GIGS Source CRS Name
                Integer.class,      // [ 3]: GIGS Target CRS Code (see GIGS Test Procedure 3210)
                String .class,      // [ 4]: GIGS Target CRS Name
                String .class,      // [ 5]: GIGS Transformation Version
                String .class,      // [ 6]: EPSG Transformation Method Name
                String .class,      // [ 7]: Parameter 1 Name
                String .class,      // [ 8]: Parameter 1 Value
                String .class,      // [ 9]: Parameter 1 Unit
                String .class,      // [10]: Parameter 2 Name
                String .class,      // [11]: Parameter 2 Value
                String .class,      // [12]: Parameter 2 Unit
                String .class,      // [13]: Parameter 3 Name
                String .class,      // [14]: Parameter 3 Value
                String .class,      // [15]: Parameter 3 Unit
                String .class,      // [16]: Parameter 4 Name
                String .class,      // [17]: Parameter 4 Value
                String .class,      // [18]: Parameter 4 Unit
                String .class,      // [19]: Parameter 5 Name
                String .class,      // [20]: Parameter 5 Value
                String .class,      // [21]: Parameter 5 Unit
                Integer.class,      // [22]: Equivalent EPSG Transformation Code
                String .class,      // [23]: Equivalent EPSG Transformation Name
                String .class);     // [24]: GIGS Remarks

        while (data.next()) {
            final int code = data.getInt(0);
            final int sourceCRSCode = data.getInt(1);
            final int targetCRSCode = data.getInt(3);
            final String methodName = data.getString(6);

            final String parameter1Name = data.getString(7);
            final String parameter1Value = data.getString(8);
            final String parameter1Unit = data.getString(9);
            final String parameter2Name = data.getString(10);
            final String parameter2Value = data.getString(11);
            final String parameter2Unit = data.getString(12);
            final String parameter3Name = data.getString(13);
            final String parameter3Value = data.getString(14);
            final String parameter3Unit = data.getString(15);
            final String parameter4Name = data.getString(16);
            final String parameter4Value = data.getString(17);
            final String parameter4Unit = data.getString(18);
            final String parameter5Name = data.getString(19);
            final String parameter5Value = data.getString(20);
            final String parameter5Unit = data.getString(21);
            final OptionalInt codeEPSG = data.getIntOptional(22);
            final String nameEPSG = data.getString(23);
            final String remarks = data.getString(24);

            /*
             * Write javadoc.
             */
            String name = "GIGS_" + String.valueOf(code);
            out.append('\n');
            indent(1);
            out.append("/**\n");
            indent(1);
            out.append(" * Tests “").append(name).append("” ")
                    .append(" transformation from the factory.\n");
            indent(1);
            out.append(" *\n");
            final var descriptions = new ArrayList<>();
            descriptions.addAll(Arrays.asList("GIGS transformation code", code,
                    "EPSG Transformation Method", methodName));
            if (codeEPSG.isPresent()) {
                descriptions.addAll(Arrays.asList("EPSG equivalence", codeAndName(codeEPSG.getAsInt(), nameEPSG)));
            }
            printJavadocKeyValues(descriptions.toArray());
            printParameterTableHeader("Transformation parameters");
            printJavadocParameterString(parameter1Name, parameter1Value, parameter1Unit);
            printJavadocParameterString(parameter2Name, parameter2Value, parameter2Unit);
            printJavadocParameterString(parameter3Name, parameter3Value, parameter3Unit);
            printJavadocParameterString(parameter4Name, parameter4Value, parameter4Unit);
            printJavadocParameterString(parameter5Name, parameter5Value, parameter5Unit);
            printParameterTableFooter();
            printRemarks(remarks);
            printJavadocThrows("if an error occurred while creating the transformation from the properties.");

            /*
             * Write test method.
             */
            printTestMethodSignature(GIGS, code, name);
            printCallToSetCodeAndName(code, name);
            indent(2); out.append("properties.put(CoordinateOperation.OPERATION_VERSION_KEY, \"GIGS Transformation\");\n");
            indent(2); out.append("methodName = \"").append(methodName).append("\";\n");
            indent(2); out.append("createDefaultParameters();\n");
            //specify the source crs (either from gigs or epsg)
            if (crsCodes.contains(sourceCRSCode)) {
                indent(2);
                out.append("createSourceCRS(Test3210::GIGS_")
                        .append(sourceCRSCode).append(");\n");
            } else {
                indent(2);
                out.append("createSourceCRS(")
                        .append(sourceCRSCode).append(");\n");
            }
            //specify the target crs (either from gigs or epsg)
            if (crsCodes.contains(targetCRSCode)) {
                indent(2);
                out.append("createTargetCRS(Test3210::GIGS_")
                        .append(targetCRSCode).append(");\n");
            } else {
                indent(2);
                out.append("createTargetCRS(")
                        .append(targetCRSCode).append(");\n");
            }
            printParameterString(parameter1Name, parameter1Value, parameter1Unit);
            printParameterString(parameter2Name, parameter2Value, parameter2Unit);
            printParameterString(parameter3Name, parameter3Value, parameter3Unit);
            printParameterString(parameter4Name, parameter4Value, parameter4Unit);
            printParameterString(parameter5Name, parameter5Value, parameter5Unit);
            indent(2); out.append("verifyTransformation();\n");
            indent(1); out.append('}');
            saveTestMethod();
        }
        flushAllMethods();
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

    /**
     * Prints the transform parameter entry (name, value, unit) for javadoc.
     * @param parameterName the parameter name.
     * @param parameterValue the parameter value.
     * @param parameterUnit the unit.
     */
    private void printJavadocParameterString(String parameterName, String parameterValue, String parameterUnit) {
        if (parameterName == null || parameterName.equals("NULL")) {
            return;
        }
        if (parameterUnit != null && parameterUnit.equals("NULL")) {
            parameterUnit = null;
        }
        printParameterTableRow(parameterName, parameterValue, parameterUnit);
    }

    /**
     * Prints the programmatic line that adds a parameter to a parameter group.
     * @param parameterName the parameter name.
     * @param parameterValue the parameter value.
     * @param parameterUnit the unit..
     */
    private void printParameterString(String parameterName, String parameterValue, String parameterUnit) {
        if (parameterName == null || parameterName.equals("NULL")) {
            return;
        }
        indent(2);out.append("parameterValueGroup.parameter(\"").append(parameterName).append("\")");
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

}
