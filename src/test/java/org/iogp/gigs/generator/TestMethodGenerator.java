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

import java.util.Map;
import java.util.HashMap;
import javax.measure.Unit;
import javax.measure.quantity.Angle;
import javax.measure.quantity.Length;
import javax.measure.quantity.Dimensionless;
import org.iogp.gigs.internal.geoapi.Units;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Base class of test code generators. Those generators need to be executed only if the GIGS data changed.
 * The code is sent to the standard output; maintainer need to copy-and-paste the relevant methods to the
 * test class, but be aware that the original code may contain manual changes that need to be preserved.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @version 1.0
 * @since   1.0
 */
public abstract class TestMethodGenerator {
    /**
     * Minimum number of calls to a method before we replace the calls by a loop.
     */
    private static final int CALL_IN_LOOP_THRESHOLD = 4;

    /**
     * Provider of unit implementations.
     */
    static final Units units = Units.getInstance();

    /**
     * The {@value} unit name, which is handled specially.
     */
    static final String SEXAGESIMAL_DEGREE = "sexagesimal degree";

    /**
     * The value to give to the {@link org.iogp.gigs.Series2000#aliases} field for meaning "no alias".
     */
    static final String[] NO_ALIAS = new String[0];

    /**
     * Where to write the generated code.
     * The Unix line separator ({@code '\n'}) should be used when writing content in this buffer.
     * It will be replaced by the platform-specific line separator by the {@link #print()} method.
     *
     * @see #print()
     */
    final StringBuilder out;

    /**
     * Creates a new test generator.
     */
    protected TestMethodGenerator() {
        out = new StringBuilder(8000);
    }

    /**
     * Retrieves the unit of the given name.
     *
     * @param  name  the unit name.
     * @return the unit for the given name, or {@code null} if unknown.
     */
    protected final Unit<?> parseUnit(final String name) {
        Unit<?> unit = parseLinearUnit(name);
        if (unit == null) {
            unit = parseAngularUnit(name);
            if (unit == null) {
                unit = parseScaleUnit(name);
            }
        }
        return unit;
    }

    /**
     * Returns the linear unit (compatible with metres) of the given name.
     *
     * @param  name  the unit name.
     * @return the linear unit for the given name, or {@code null} if unknown.
     */
    protected static Unit<Length> parseLinearUnit(final String name) {
        if (name.equalsIgnoreCase("metre"))          return units.metre();
        if (name.equalsIgnoreCase("kilometre"))      return units.kilometre();
        if (name.equalsIgnoreCase("US survey foot")) return units.footSurveyUS();
        if (name.equalsIgnoreCase("ft(US)"))         return units.footSurveyUS();
        if (name.equalsIgnoreCase("foot"))           return units.foot();
        return null;
    }

    /**
     * Retrieves the angular unit (compatible with degrees) of the given name.
     *
     * @param  name  the unit name.
     * @return the angular unit for the given name, or {@code null} if unknown.
     */
    protected static Unit<Angle> parseAngularUnit(final String name) {
        if (name.equalsIgnoreCase("degree"))      return units.degree();
        if (name.equalsIgnoreCase("grad"))        return units.grad();
        if (name.equalsIgnoreCase("arc-second"))  return units.arcSecond();
        if (name.equalsIgnoreCase("microradian")) return units.microradian();
        return null;
    }

    /**
     * Retrieves the scale unit (dimensionless) of the given name.
     *
     * @param  name  the unit name.
     * @return the scale unit for the given name, or {@code null} if unknown.
     */
    protected static Unit<Dimensionless> parseScaleUnit(final String name) {
        if (name.equalsIgnoreCase("unity"))             return units.one();
        if (name.equalsIgnoreCase("parts per million")) return units.ppm();
        return null;
    }

    /**
     * The programmatic names of above units.
     */
    private static final Map<Unit<?>,String> UNIT_NAMES;
    static {
        final Map<Unit<?>,String> m = new HashMap<>();
        assertNull(m.put(units.one(),          "units.one()"));
        assertNull(m.put(units.metre(),        "units.metre()"));
        assertNull(m.put(units.kilometre(),    "units.kilometre()"));
        assertNull(m.put(units.radian(),       "units.radian()"));
        assertNull(m.put(units.microradian(),  "units.microradian()"));
        assertNull(m.put(units.grad(),         "units.grad()"));
        assertNull(m.put(units.degree(),       "units.degree()"));
        assertNull(m.put(units.arcSecond(),    "units.arcSecond()"));
        assertNull(m.put(units.foot(),         "units.foot()"));
        assertNull(m.put(units.footSurveyUS(), "units.footSurveyUS()"));
        assertNull(m.put(units.ppm(),          "units.ppm()"));
        UNIT_NAMES = m;
    }

    /**
     * Prints the margin at the beginning of a new line.
     */
    final void indent(int n) {
        do out.append("    ");
        while (--n != 0);
    }

    /**
     * Returns {@code true} if the given value should be skipped from javadoc.
     */
    private static boolean omitFromJavadoc(final Object value) {
        if (value instanceof Double)   return ((Double) value).isNaN();
        if (value instanceof Object[]) return ((Object[]) value).length == 0;
        return false;
    }

    /**
     * Prints a sequence of key-values as a list in javadoc.
     * Boolean value are treated especially: the key is printed only if the value is {@code true}.
     *
     * @param pairs  key-value pairs.
     */
    final void printJavadocKeyValues(final Object... pairs) {
        assertTrue((pairs.length & 1) == 0, "Array length shall be even");
        indent(1); out.append(" * <ul>\n");
        for (int i=0; i<pairs.length; i += 2) {
            final Object value = pairs[i+1];
            if (value != null && !omitFromJavadoc(value)) {
                if (value instanceof Boolean) {
                    if ((Boolean) value) {
                        indent(1);
                        out.append(" *   <li>").append(pairs[i]).append("</li>\n");
                    }
                } else {
                    indent(1);
                    out.append(" *   <li>").append(pairs[i]).append(": <b>");
                    if (value instanceof Object[]) {
                        String separator = "";
                        for (final Object e : (Object[]) value) {
                            out.append(separator).append(e);
                            separator = "</b>, <b>";
                        }
                    } else if (value instanceof int[]) {
                        String separator = "";
                        final int length = ((int[]) value).length;
                        final int stopAt = StrictMath.min(length, 10);
                        for (int j=0; j<stopAt; j++) {
                            out.append(separator).append(((int[]) value)[j]);
                            separator = "</b>, <b>";
                        }
                        if (stopAt != length) {
                            out.append("</b>, <i>…").append(length - stopAt).append(" more</i></li>\n");
                            continue;                   // Because we already wrote the closing </li>.
                        }
                    } else if (value instanceof Double) {
                        final double asDouble = (Double) value;
                        final int asInteger = (int) asDouble;
                        if (asDouble == asInteger) {
                            out.append(asInteger);
                        } else {
                            out.append(asDouble);
                        }
                    } else {
                        out.append(value);
                    }
                    out.append("</b></li>\n");
                }
            }
        }
        indent(1); out.append(" * </ul>\n");
    }

    /**
     * Formats code and name on the same line, for inclusion in the list of argument given to
     * {@link #printJavadocKeyValues(Object[])}.
     */
    static String codeAndName(final int code, final String name) {
        return code + " – " + name;
    }

    /**
     * Formats codes and names on the same line, for inclusion in the list of argument given to
     * {@link #printJavadocKeyValues(Object[])}.
     */
    static String codeAndName(final int[] code, final String[] name) {
        final StringBuilder buffer = new StringBuilder();
        for (int i=0; i<code.length; i++) {
            if (buffer.length() != 0) {
                buffer.append("</b>, <b>");
            }
            buffer.append(code[i]).append(" – ").append(name[i]);
        }
        return (buffer.length() != 0) ? buffer.toString() : null;
    }

    /**
     * Formats a value followed by its unit of measurement. If the given alternative value is different
     * but not NaN, then it will also be formatted. This is used for inclusion in the list of argument
     * given to {@link #printJavadocKeyValues(Object[])}.
     */
    static String quantityAndAlternative(final Object value, final String unit, final double altValue, final String altUnit) {
        final StringBuilder buffer = new StringBuilder().append(value).append(' ').append(unit);
        if (!value.equals(altValue) && !Double.isNaN(altValue)) {
            buffer.append(" (").append(altValue).append(' ').append(altUnit).append(')');
        }
        return buffer.toString();
    }

    /**
     * Replaces repetition of ASCII {@code '} character by the Unicode single, double or triple prime character.
     */
    static String replaceAsciiPrimeByUnicode(String text) {
        if (text.endsWith("'''")) {
            text = text.substring(0, text.length() - 3) + '‴';
        } else if (text.endsWith("''")) {
            text = text.substring(0, text.length() - 2) + '″';
        } else if (text.endsWith("'")) {
            text = text.substring(0, text.length() - 1) + '′';
        }
        return text;
    }

    /**
     * Prints the first lines for the table of parameters in Javadoc.
     *
     * @param caption The table caption (e.g. "Conversion parameters").
     */
    final void printParameterTableHeader(final String caption) {
        indent(1); out.append(" * <table class=\"ogc\">\n");
        indent(1); out.append(" *   <caption>").append(caption).append("</caption>\n");
        indent(1); out.append(" *   <tr><th>Parameter name</th><th>Value</th></tr>\n");
    }

    /**
     * Prints a parameter name, value and units in Javadoc.
     */
    final void printParameterTableRow(final String name, final String value, String unit) {
        indent(1);
        out.append(" *   <tr><td>").append(name).append("</td><td>").append(value);
        if (unit != null && !unit.equals("unity") && !unit.equals(SEXAGESIMAL_DEGREE)) {
            if (unit.equals("degree")) {
                out.append('°');
            } else {
                if (StrictMath.abs(Double.valueOf(value)) > 1) {
                    unit += 's';
                }
                out.append(' ').append(unit);
            }
        }
        out.append("</td></tr>\n");
    }

    /**
     * Prints the last lines for the table of parameters in Javadoc.
     */
    final void printParameterTableFooter() {
        indent(1); out.append(" * </table>\n");
    }

    /**
     * Prints the given remarks if they are not null.
     * The remarks may be separated on many lines.
     *
     * @param  remarks  the remarks, or {@code null} or empty if none.
     */
    final void printRemarks(final String remarks) {
        if (remarks != null && !remarks.isEmpty()) {
            indent(1); out.append(" *\n");
            indent(1); out.append(" * Remarks: ");
            int start = 0, end;
            while ((end = remarks.indexOf("; ", start)) >= 0) {
                out.append(Character.toUpperCase(remarks.charAt(start)))
                   .append(remarks, start+1, end).append(".\n");
                indent(1); out.append(" * ");
                start = end + 2;
            }
            out.append(Character.toUpperCase(remarks.charAt(start)))
               .append(remarks, start+1, remarks.length());
            if (!remarks.endsWith(".")) {
                out.append('.');
            }
            out.append('\n');
        }
    }

    /**
     * Prints the javadoc {@code throws FactoryException} followed by the given explanatory text.
     */
    final void printJavadocThrows(final String condition) {
        indent(1); out.append(" *\n");
        indent(1); out.append(" * @throws FactoryException ").append(condition).append('\n');
    }

    /**
     * Prints a "see" annotation if the given {@code method} is non-null.
     *
     * @param classe The class, or {@code null} for the current class.
     * @param method The method, or {@code null} if unknown.
     */
    final void printJavadocSee(final String classe, final String method) {
        if (method != null) {
            indent(1); out.append(" *\n");
            indent(1); out.append(" * @see ");
            if (classe != null) {
                out.append(classe);
            }
            out.append('#').append(method).append("()\n");
        }
    }

    /**
     * Closes the javadoc comment block, then prints the test method signature.
     * The signature includes the {@code throws FactoryException} declaration.
     *
     * @param nameToMethod  a map of test method names to use for the given {@code name}.
     *        If this map does not contain an entry for the given {@code name}, then this method
     *        will generate a new name by trimming illegal characters from the given {@code name}.
     * @param name  the name to use for generating a method name.
     *              Spaces will be replaced by camel-cases.
     */
    final void printTestMethodSignature(final Map<String,String> nameToMethod, final String name) {
        indent(1); out.append(" */\n");
        indent(1); out.append("@Test\n");
        indent(1); out.append("@DisplayName(\"").append(name).append("\")\n");
        indent(1); out.append("public void ");
        final String predefined = nameToMethod.get(name);
        if (predefined != null) {
            out.append(predefined);
        } else {
            out.append("test");
            boolean toUpperCase = true;
            for (int i=0; i<name.length(); i++) {
                char c = name.charAt(i);
                if (Character.isJavaIdentifierPart(c)) {
                    if (toUpperCase) {
                        toUpperCase = false;
                        c = Character.toUpperCase(c);
                    }
                    out.append(c);
                } else {
                    if (c == '(' || c == ')') {
                        if (i+1 < name.length()) {
                            out.append('_');
                            toUpperCase = false;
                        }
                    } else {
                        toUpperCase = true;
                    }
                    /*
                     * For name like “Clarke's foot”, skip also the "s" after the single quote.
                     * The result will be “ClarkeFoot”.
                     */
                    if (c == '\'' && i+1 < name.length() && name.charAt(i+1) == 's') {
                        i++;
                    }
                }
            }
        }
        out.append("() throws FactoryException {\n");
    }

    /**
     * Prints a sequence of key-values as assignments in the method body.
     * Boolean value are treated especially: the assignment is printed only if the value is {@code true}.
     *
     * @param pairs  key-value pairs.
     */
    final void printFieldAssignments(final Object... pairs) {
        assertTrue((pairs.length & 1) == 0, "Array length shall be even");
        int length = 0;
        for (int i=0; i<pairs.length; i += 2) {
            length = StrictMath.max(length, ((String) pairs[i]).length());
        }
        for (int i=0; i<pairs.length; i += 2) {
            final String name  = (String) pairs[i];
            final Object value = pairs[i+1];
            if (!(value instanceof Boolean) || (Boolean) value) {
                indent(2);
                out.append(name);
                for (int j = length - name.length(); --j >= 0;) {
                    out.append(' ');
                }
                out.append(" = ");
                if (value instanceof Unit<?>) {
                    printProgrammaticName((Unit<?>) value);
                } else if (value instanceof String[]) {
                    if (((String[]) value).length == 0) {
                        out.append("NONE");
                    } else {
                        String separator = "new String[] {\"";
                        for (final String e : (String[]) value) {
                            out.append(separator).append(e);
                            separator = "\", \"";
                        }
                        out.append("\"}");
                    }
                } else if (value instanceof Double && ((Double) value).isNaN()) {
                    out.append("Double.NaN");
                } else {
                    final boolean quote = (value instanceof CharSequence);
                    if (quote) {
                        out.append('"');
                    }
                    out.append(value);
                    if (quote) {
                        out.append('"');
                    }
                }
                out.append(";\n");
            }
        }
    }

    /**
     * Prints the programmatic name of the given unit.
     *
     * @param unit  the unit for which to print the programmatic name.
     */
    final void printProgrammaticName(final Unit<?> unit) {
        final String name = UNIT_NAMES.get(unit);
        assertNotNull(unit.toString(), name);
        out.append(name);
    }

    /**
     * Prints a sequence of calls to the given method, each call using a different argument value.
     * If this method detects a sequence of at least {@value #CALL_IN_LOOP_THRESHOLD} consecutive
     * values, then this method will invoke the given method in a loop.
     *
     * @param method  the name of the method to call (without arguments).
     * @param codes   the arguments to give to the method.
     */
    final void printCallsToMethod(final String method, final int[] codes) {
        for (int i=0; i<codes.length; i++) {
            if (i+CALL_IN_LOOP_THRESHOLD <= codes.length) {
                final int delta = codes[i+1] - codes[i];
                if (delta >= 1) {
                    int upper = i + 2;  // Exclusive
                    while (upper < codes.length) {
                        if (codes[upper] - codes[upper - 1] != delta) {
                            break;
                        }
                        upper++;
                    }
                    if (upper - i >= CALL_IN_LOOP_THRESHOLD) {
                        indent(2);
                        out.append("for (int code = ").append(codes[i])
                           .append("; code <= ").append(codes[upper - 1])
                           .append("; ");
                        if (delta == 1) {
                            out.append("code++");
                        } else {
                            out.append("code += ").append(delta);
                        }
                        out.append(") {    // Loop over ").append(upper - i).append(" codes\n");
                        indent(3);
                        out.append(method).append("(code);\n");
                        indent(2);
                        out.append("}\n");
                        i = upper - 1;                      // Skip the sequence.
                        continue;
                    }
                }
            }
            indent(2);
            out.append(method).append('(').append(codes[i]).append(");\n");
        }
    }

    /**
     * Prints a call to the {@link UserObjectFactoryTestCase#setCodeAndName(String, int)} method.
     */
    final void printCallToSetCodeAndName(final int code, final String name) {
        indent(2);
        out.append("setCodeAndName(").append(code).append(", \"").append(name).append("\");\n");
    }

    /**
     * Prints the {@link #out} content to the standard output stream
     * and clears the buffer for next entry.
     */
    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    final void print() {
        final String lineSeparator = System.lineSeparator();
        if (!lineSeparator.equals("\n")) {
            int i = 0;
            while ((i = out.indexOf("\n", i)) >= 0) {
                out.replace(i, i+1, lineSeparator);
                i += lineSeparator.length();
            }
        }
        System.out.println(out.toString());
        out.setLength(0);
    }
}
