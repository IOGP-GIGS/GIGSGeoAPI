package org.iogp.gigs.generator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.OptionalInt;

/**
 * Code generator for {@link org.iogp.gigs.Test3209}. This generator needs to be executed only if the GIGS data changed.
 * The code is sent to the standard output; maintainer need to copy-and-paste the relevant methods to the test class,
 * but be aware that the original code may contain manual changes that need to be preserved.
 *
 * @author  Michael Arneson (INT)
 * @version 1.0
 * @since   1.0
 */
public class Test3209 extends TestMethodGenerator {
    /**
     * Launcher.
     *
     * @param  args  ignored.
     * @throws IOException if an error occurred while reading the test data.
     */
    public static void main(String[] args) throws IOException {
        new Test3209().run();
    }

    /**
     * Generates the code.
     *
     * @throws IOException if an error occurred while reading the test data.
     */
    private void run() throws IOException {
        final DataParser data = new DataParser(Series.USER_DEFINED, "GIGS_user_3209_VerticalDatum.txt",
                Integer.class,      // [ 0]: GIGS Vertical Datum Code
                String .class,      // [ 1]: GIGS Vertical Datum Name
                String .class,      // [ 2]: Datum Origin (see associated entity in EPSG Dataset
                Integer.class,      // [ 3]: Equivalent EPSG Datum Code
                String .class,      // [ 4]: Equivalent EPSG Datum Name
                String .class);     // [ 5]: GIGS Remarks

        while (data.next()) {
            final int         code     = data.getInt         ( 0);
            final String      name     = data.getString      ( 1);
            final String      origin   = data.getString      ( 2);
            final OptionalInt codeEPSG = data.getIntOptional ( 3);
            final String      nameEPSG = data.getString      ( 4);
            final String      remarks  = data.getString      ( 5);

            /*
             * Write javadoc.
             */
            out.append('\n');
            indent(1); out.append("/**\n");
            indent(1); out.append(" * Tests “").append(name).append("” ")
                    .append(" vertical datum from the factory.\n");
            indent(1); out.append(" *\n");
            final var descriptions = new ArrayList<>();
            descriptions.addAll(Arrays.asList("GIGS datum code", code,
                    "GIGS datum name", name,
                    "Datum Origin", origin));
            if (codeEPSG.isPresent()) {
                descriptions.addAll(Arrays.asList("EPSG equivalence", codeAndName(codeEPSG.getAsInt(), nameEPSG)));
            }
            printJavadocKeyValues(descriptions.toArray());
            printRemarks(remarks);
            printJavadocThrows("if an error occurred while creating the datum from the properties.");

            /*
             * Write test method.
             */
            printTestMethodSignature(GIGS, code, name);
            printCallToSetCodeAndName(code, name);
            indent(2); out.append("properties.put(Datum.ANCHOR_POINT_KEY, \"").append(origin).append("\");\n");
            indent(2); out.append("datumType = VerticalDatumType.GEOIDAL;\n");
            indent(2); out.append("verifyVerticalDatum();\n");
            indent(1); out.append('}');
            saveTestMethod();
        }
        flushAllMethods();
    }
}
