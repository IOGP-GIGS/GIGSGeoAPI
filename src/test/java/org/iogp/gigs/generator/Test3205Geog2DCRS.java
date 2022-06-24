package org.iogp.gigs.generator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Code generator for {@link org.iogp.gigs.Test3205Geog2DCRS}. This generator needs to be executed only if the GIGS data changed.
 * The code is sent to the standard output; maintainer need to copy-and-paste the relevant methods to the test class,
 * but be aware that the original code may contain manual changes that need to be preserved.
 *
 * @author  Michael Arneson (INT)
 * @version 1.0
 * @since   1.0
 */
public class Test3205Geog2DCRS extends TestMethodGenerator {
    /**
     * Launcher.
     *
     * @param  args  ignored.
     * @throws IOException if an error occurred while reading the test data.
     */
    public static void main(String[] args) throws IOException {
        new Test3205Geog2DCRS().run();
    }


    /**
     * Generates the code.
     *
     * @throws IOException if an error occurred while reading the test data.
     */
    private void run() throws IOException {
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
            int    code             = data.getInt           ( 0);
            String name             = data.getString        ( 2);
            String geodeticType     = data.getString        ( 3);
            int    datumCode        = data.getInt           ( 4);
            int    csCode           = data.getInt           ( 5);
            String remarks          = data.getString        (9);

            if (!geodeticType.equals("Geographic 2D")) {
                continue;
            }

            out.append('\n');
            indent(1); out.append("/**\n");
            indent(1); out.append(" * Tests “").append(name).append("” ")
                    .append(" geographic 2D CRS from the factory.\n");
            indent(1); out.append(" *\n");
            final var descriptions = new ArrayList<>(20);
            descriptions.addAll(Arrays.asList("GIGS geographic 2D CRS code", code,
                    "GIGS geographic 2D CRS name", replaceAsciiPrimeByUnicode(name)));
            descriptions.addAll(Arrays.asList("Coordinate System code", csCode,
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
            indent(2);
            out.append("createDatum(Test3204::GIGS_")
                    .append(datumCode)
                    .append(");\n");
            indent(2);
            out.append("csCode=")
                    .append(csCode)
                    .append(";\n");
            indent(2);
            out.append("verifyGeographicCRS();\n");
            indent(1); out.append('}');
            saveTestMethod();
        }
        flushAllMethods();
    }

}
