package org.iogp.gigs.generator;

import java.io.IOException;

/**
 * Code generator for {@link org.iogp.gigs.Test3212}. This generator needs to be executed only if the GIGS data changed.
 * The code is sent to the standard output; maintainer need to copy-and-paste the relevant methods to the test class,
 * but be aware that the original code may contain manual changes that need to be preserved.
 *
 * @author  Michael Arneson (INT)
 * @version 1.0
 * @since   1.0
 */
public class Test3212 extends TestMethodGenerator {

    /**
     * Launcher.
     *
     * @param  args  ignored.
     * @throws IOException if an error occurred while reading the test data.
     */
    public static void main(String[] args) throws IOException {
        new Test3212().run();
    }

    /**
     * Generates the code.
     *
     * @throws IOException if an error occurred while reading the test data.
     */
    private void run() throws IOException {
        final DataParser data = new DataParser(Series.USER_DEFINED, "GIGS_user_3212_ConcatTfm.txt",
                Integer.class,      // [ 0]: GIGS Transformation Code
                Integer.class,      // [ 1]: GIGS Source CRS Code (see GIGS Test Procedure 3205)
                String .class,      // [ 2]: GIGS Source CRS Name
                Integer.class,      // [ 3]: GIGS Target CRS Code (see GIGS Test Procedure 3205)
                String .class,      // [ 4]: GIGS Target CRS Name
                String .class,      // [ 5]: GIGS Transformation Version
                Integer.class,      // [ 6]: Step 1 GIGS Transformation Code (see GIGS Test Procedure 3208)
                String .class,      // [ 7]: Step 1 GIGS Transformation Name
                Integer.class,      // [ 8]: Step 2 GIGS Transformation Code (see GIGS Test Procedure 3208)
                String .class);     // [ 9]: Step 2 GIGS Transformation Name

        while (data.next()) {
            final int    code               = data.getInt    (0);
            final int    step1TransformCode = data.getInt    (6);
            final String step1TransformName = data.getString (7);
            final int    step2TransformCode = data.getInt    (8);
            final String transform2Name     = data.getString (9);

            /*
             * Write javadoc.
             */
            final String name = "GIGS_" + String.valueOf(code);
            out.append('\n');
            indent(1);
            out.append("/**\n");
            indent(1);
            out.append(" * Tests “").append(name).append("” ")
                    .append(" transformation from the factory.\n");
            indent(1);
            out.append(" *\n");
            printJavadocKeyValues("GIGS transformation code", code,
                    "Step 1 GIGS Transform Code", step1TransformCode,
                    "Step 1 GIGS Transform Name", step1TransformName,
                    "Step 2 GIGS Transform Code", step2TransformCode,
                    "Step 2 GIGS Transform Name", transform2Name);
            printJavadocThrows("if an error occurred while creating the transformation from the properties.");

            /*
             * Write test method.
             */
            printTestMethodSignature(GIGS, code, name);
            printCallToSetCodeAndName(code, name);
            indent(2); out.append("createStep1Transformation(Test3208::GIGS_").append(step1TransformCode).append(");\n");
            indent(2); out.append("createStep2Transformation(Test3208::GIGS_").append(step2TransformCode).append(");\n");
            indent(2); out.append("verifyTransformation();\n");
            indent(1); out.append('}');
            saveTestMethod();
        }
        flushAllMethods();
    }
}
