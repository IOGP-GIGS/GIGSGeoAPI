package org.iogp.gigs.generator;

import org.iogp.gigs.Test3206;
import org.opengis.referencing.cs.AxisDirection;

import java.io.IOException;
import java.lang.module.FindException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.OptionalInt;

/**
 * Code generator for {@link org.iogp.gigs.Test3207}. This generator needs to be executed only if the GIGS data changed.
 * The code is sent to the standard output; maintainer need to copy-and-paste the relevant methods to the test class,
 * but be aware that the original code may contain manual changes that need to be preserved.
 *
 * @author  Michael Arneson (INT)
 * @version 1.0
 * @since   1.0
 */
public class Test3207 extends TestMethodGenerator {
    /**
     * Launcher.
     *
     * @param  args  ignored.
     * @throws IOException if an error occurred while reading the test data.
     */
    public static void main(String[] args) throws IOException {
        new Test3207().run();
    }

    /**
     * Generates the code.
     *
     * @throws IOException if an error occurred while reading the test data.
     */
    private void run() throws IOException {
        final DataParser data = new DataParser(Series.USER_DEFINED, "GIGS_user_3207_ProjectedCRS.txt",
                Integer.class,      // [ 0]: GIGS Projected CRS Code
                String .class,      // [ 1]: GIGS Projected CRS Definition Source
                String .class,      // [ 2]: GIGS Projected CRS Name
                Integer.class,      // [ 3]: Base CRS Code (see GIGS Test Procedure 3205)
                String .class,      // [ 4]: Base CRS Name (see GIGS Test Procedure 3205)
                Integer.class,      // [ 5]: Conversion Code (see GIGS Test Procedure 3206)
                String .class,      // [ 6]: Conversion Name (see GIGS Test Procedure 3206)
                Integer.class,      // [ 7]: EPSG Coordinate System Code
                String .class,      // [ 8]: Coordinate System Axis 1 Name
                String .class,      // [ 9]: Coordinate System Axis 1 Abbreviation
                String .class,      // [10]: Coordinate System Axis 1 Orientation
                String .class,      // [11]: Coordinate System Axis 1 Unit
                String .class,      // [12]: Coordinate System Axis 2 Name
                String .class,      // [13]: Coordinate System Axis 2 Abbreviation
                String .class,      // [14]: Coordinate System Axis 2 Orientation
                String .class,      // [15]: Coordinate System Axis 2 Unit
                Integer.class,      // [16]: Equivalent EPSG CRS Code
                String .class,      // [17]: Equivalent EPSG CRS Name
                String .class);     // [18]: GIGS Remarks
        while (data.next()) {

            final int         code              = data.getInt(0);
            final String      name              = data.getString(2);
            final int         baseCRSCode       = data.getInt(3);
            final int         conversionCode    = data.getInt(5);
            final int         csCode            = data.getInt(7);
            final String      axis1Name         = data.getString(8);
            final String      axis1Abbreviation = data.getString(9);
            final String      axis1Orientation  = data.getString(10);
            final String      axis1Unit         = data.getString(11);
            final String      axis2Name         = data.getString(12);
            final String      axis2Abbreviation = data.getString(13);
            final String      axis2Orientation  = data.getString(14);
            final String      axis2Unit         = data.getString(15);
            final OptionalInt optionalCodeEPSG  = data.getIntOptional(16);
            final String      nameEPSG          = data.getString(17);
            final String      remarks           = data.getString(18);

            out.append('\n');
            out.append('\n');
            indent(1);
            out.append("/**\n");
            indent(1);
            out.append(" * Tests “").append(name).append("” projected CRS creation from the factory.\n");
            indent(1);
            out.append(" *\n");
            final var descriptions = new ArrayList<>(20);
            descriptions.addAll(Arrays.asList("GIGS projected CRS code", code,
                    "GIGS projectedCRS name", replaceAsciiPrimeByUnicode(name)));
            if (optionalCodeEPSG.isPresent()) {
                descriptions.addAll(Arrays.asList("EPSG equivalence", codeAndName(optionalCodeEPSG.getAsInt(), nameEPSG)));
            }
            descriptions.addAll(Arrays.asList("GIGS base CRS code", baseCRSCode,
                    "GIGS conversion code", conversionCode,
                    "EPSG coordinate system code", csCode,
                    "Axis 1 name", axis1Name,
                    "Axis 1 abbreviation", axis1Abbreviation,
                    "Axis 1 orientation", axis1Orientation,
                    "Axis 1 unit", axis1Unit,
                    "Axis 2 name", axis2Name,
                    "Axis 2 abbreviation", axis2Abbreviation,
                    "Axis 2 orientation", axis2Orientation,
                    "Axis 2 unit", axis2Unit
            ));
            printJavadocKeyValues(descriptions.toArray());
            printRemarks(remarks);
            printJavadocThrows("if an error occurred while creating the projected CRS from the properties.");
            if (optionalCodeEPSG.isPresent()) {
                printJavadocSee(2207, EPSG + '_' + optionalCodeEPSG.getAsInt());
            }
            /*
             * Write test method.
             */
            printTestMethodSignature(GIGS, code, name);
            printCallToSetCodeAndName(code, name);

            indent(2); out.append("createBaseCRS(Test3205Geog2DCRSTemp::GIGS_").append(baseCRSCode).append(");\n");
            indent(2); out.append("createConversion(Test3206::GIGS_").append(conversionCode).append(");\n");
            indent(2); out.append("CoordinateSystemAxis axis1 = createCoordinateSystemAxis(\"").append(axis1Name).append("\", \"")
                    .append(axis1Abbreviation).append("\", ").append(getAxisDirection(axis1Orientation)).append(", ")
                    .append(getAxisUnit(axis1Unit)).append(");\n");
            indent(2); out.append("CoordinateSystemAxis axis2 = createCoordinateSystemAxis(\"").append(axis2Name).append("\", \"")
                    .append(axis2Abbreviation).append("\", ").append(getAxisDirection(axis2Orientation)).append(", ")
                    .append(getAxisUnit(axis2Unit)).append(");\n");
            indent(2); out.append("createAndVerifyCartesianCS(").append(csCode).append(", axis1, axis2);\n");
            indent(1); out.append('}');
            saveTestMethod();
        }
        flushAllMethods();
    }

    private String getAxisUnit(String axisUnit) {
        if (axisUnit.equals("US survey foot")) {
            return "units.footSurveyUS()";
        }
        return "units." + axisUnit + "()";
    }

    private String getAxisDirection(String axisOrientation) {
        switch (axisOrientation) {
            case "north":
                return "AxisDirection.NORTH";
            case "east":
                return "AxisDirection.EAST";
            case "west":
                return "AxisDirection.WEST";
            case "south":
                return "AxisDirection.SOUTH";
            default:
                throw new IllegalArgumentException("Invalid axis orientation, " + axisOrientation);
        }
    }


}
