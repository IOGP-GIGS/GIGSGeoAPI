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

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.OptionalInt;
import java.util.TreeMap;
import javax.measure.Unit;
import javax.measure.quantity.Angle;
import javax.measure.quantity.Length;
import javax.measure.quantity.Dimensionless;
import org.opengis.referencing.ObjectFactory;
import org.opengis.referencing.AuthorityFactory;
import org.iogp.gigs.internal.geoapi.Units;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Base class of test code generators. Those generators need to be executed only if the GIGS data changed.
 * The code is sent to the standard output; maintainer need to copy-and-paste the relevant methods to the
 * test class, but be aware that the original code may contain manual changes that need to be preserved.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @author  Michael Arneson (INT)
 * @version 1.0
 * @since   1.0
 */
public abstract class TestMethodGenerator {
    /**
     * Minimum number of calls to a method before we replace the calls by a loop.
     */
    private static final int CALL_IN_LOOP_THRESHOLD = 4;

    /**
     * Frequently-used authority name.
     */
    static final String EPSG = "EPSG", GIGS = "GIGS";

    /**
     * Provider of unit implementations.
     */
    static final Units units = Units.getInstance();

    /**
     * Where to write the generated code.
     * The Unix line separator ({@code '\n'}) should be used when writing content in this buffer.
     * It will be replaced by the platform-specific line separator by the {@link #print()} method.
     *
     * @see #print()
     */
    final StringBuilder out;

    /**
     * The generated test methods, sorted in order defined by the keys.
     */
    private final Map<String,String> methods;

    /**
     * Display name of the test method, possibly modified for sorting purpose.
     * This is used for adding entries in the {@link #methods} map.
     */
    private String methodSortKey;

    /**
     * List of GIGS tests that we could not translate as JUnit tests.
     * Each list of element is a row in the table to format.
     *
     * @see #addUnsupportedTest(int, int, String, String)
     */
    private final List<String[]> unsupportedTests;

    /**
     * The type of factory for definitions provided by the library, or {@code null} if unknown.
     * This field can be set by sub-class constructors if applicable. It is used for writing
     * Javadoc with a more informative text about column such as "Datum Definition Source".
     */
    protected Class<? extends AuthorityFactory> libraryFactoryType;

    /**
     * The type of factory for definitions supplied by the user, or {@code null} if unknown.
     * This field can be set by sub-class constructors if applicable. It is used for writing
     * Javadoc with a more informative text about column such as "Datum Definition Source".
     */
    protected Class<? extends ObjectFactory> userFactoryType;

    /**
     * Creates a new test generator.
     */
    protected TestMethodGenerator() {
        out = new StringBuilder(8000);
        methods = new TreeMap<>();
        unsupportedTests = new ArrayList<>();
    }

    /**
     * Retrieves the unit of the given name.
     *
     * @param  name  the unit name.
     * @return the unit for the given name, or {@code null} if unknown.
     */
    protected static Unit<?> parseUnit(final String name) {
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
        if (name.equalsIgnoreCase("ftUS"))           return units.footSurveyUS();
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
     *
     * @see #printProgrammaticName(Unit)
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
     *
     * @param  n  indentation level (usually 1 or 2).
     */
    final void indent(int n) {
        do out.append("    ");
        while (--n != 0);
    }

    /**
     * Returns {@code true} if the given value should be skipped from javadoc.
     *
     * @param  value  the value to potentially write in javadoc.
     * @return whether the given value should be omitted from javadoc.
     */
    private static boolean isOmittedFromJavadoc(final Object value) {
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
            if (value != null && !isOmittedFromJavadoc(value)) {
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
                        if (length <= 10) {
                            for (int j=0; j<length; j++) {
                                out.append(separator).append(((int[]) value)[j]);
                                separator = "</b>, <b>";
                            }
                        } else {
                            out.append("various");
                        }
                    } else if (value instanceof Number) {
                        append(((Number) value).doubleValue());
                    } else {
                        out.append(value.toString().replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;"));
                    }
                    out.append("</b>");
                    if (value instanceof DefinitionSource) {
                        ((DefinitionSource) value).appendFactoryInformation(out, libraryFactoryType, userFactoryType);
                    }
                    out.append("</li>\n");
                }
            }
        }
        indent(1); out.append(" * </ul>\n");
    }

    /**
     * Appends the given value as an integer if possible, or as a floating point otherwise.
     * The intent is to avoid scientific notation for values around 1E+7, which are frequent
     * with ellipsoidal axis lengths.
     *
     * @param  value  the value to append.
     */
    private void append(final double value) {
        final long asInteger = Math.round(value);
        if (asInteger == value) {
            out.append(asInteger);
        } else {
            out.append(value);
            trimFractionalPart();
        }
    }

    /**
     * Trims the fractional part of the last formatted number, provided that it doesn't change the value.
     * If the buffer ends with a {@code '.'} character followed by a sequence of {@code '0'} characters,
     * then those characters are removed. Otherwise this method does nothing.
     * This is a <cite>"all or nothing"</cite> method: either the fractional part is completely removed,
     * or either it is left unchanged.
     */
    @SuppressWarnings("fallthrough")
    private void trimFractionalPart() {
        for (int i=out.length(); i > 0;) {
            switch (out.charAt(--i)) {               // No need to use Unicode code points here.
                case '0': continue;
                case '.': out.setLength(i);          // Fall through
                default : return;
            }
        }
    }

    /**
     * Formats code and name on the same line, for inclusion in the list of argument given to
     * {@link #printJavadocKeyValues(Object[])}.
     *
     * @param  code  the authority code to format.
     * @param  name  the name to format.
     * @return the (authority, code) tuple.
     */
    static String codeAndName(final int code, final String name) {
        return code + " – " + name;
    }

    /**
     * Returns the code and name on the same line if the code is present.
     *
     * @param  code  the authority code to format.
     * @param  name  the name to format.
     * @return the (authority, code) tuple, or {@code null} if the EPSG code is absent.
     */
    static String codeAndName(final OptionalInt code, final String name) {
        return code.isPresent() ? codeAndName(code.getAsInt(), name) : null;
    }

    /**
     * Adds "EPSG equivalence" pairs for an arbitrary amount of EPSG equivalences.
     *
     * @param  addTo  where to add EPSG equivalence" pairs.
     * @param  codes  equivalent EPSG codes.
     * @param  names  equivalent EPSG names.
     */
    static void addCodesAndNames(final List<Object> addTo, final int[] codes, final String[] names) {
        for (int i=0; i < codes.length; i++) {
            String label = "EPSG equivalence";
            if (codes.length != 1) {
                label = label + " (" + (i+1) + " of " + codes.length + ')';
            }
            addTo.add(label);
            addTo.add(codeAndName(codes[i], names[i]));
        }
    }

    /**
     * Formats a value followed by its unit of measurement. If the given alternative value is different
     * but not NaN, then it will also be formatted. This is used for inclusion in the list of argument
     * given to {@link #printJavadocKeyValues(Object[])}.
     *
     * @param  value     the numerical value.
     * @param  unit      unit of measurement associated to the value.
     * @param  altValue  another value to write if different than the main value.
     * @param  altUnit   unit of measurement associated to the alternative value.
     * @return string representation of the quantities.
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
     *
     * @param  text  the text with ASCII prime symbols.
     * @return the text with Unicode prime symbols.
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
     * Prints a Java identified inferred from a sentence.
     * The sentence can contain spaces and punctuations.
     *
     * @param  name    the name as a sentence, possibly with spaces and punctuations.
     */
    private void printJavaIdentifier(final String name) {
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

    /**
     * Prints the first lines for the table of axes in Javadoc.
     */
    final void printJavadocAxisHeader() {
        indent(1); out.append(" * <table class=\"ogc\">\n");
        indent(1); out.append(" *   <caption>Coordinate system axes</caption>\n");
        indent(1); out.append(" *   <tr><th>Name</th><th>Abbreviation</th><th>Orientation</th><th>Unit</th></tr>\n");
    }

    /**
     * Prints the first lines for the table of parameters in Javadoc.
     *
     * @param  caption  the table caption (e.g. "Conversion parameters").
     */
    final void printJavadocParameterHeader(final String caption) {
        indent(1); out.append(" * <table class=\"ogc\">\n");
        indent(1); out.append(" *   <caption>").append(caption).append("</caption>\n");
        indent(1); out.append(" *   <tr><th>Parameter name</th><th>Value</th></tr>\n");
    }

    /**
     * Prints an axis name, abbreviation, orientation and units in Javadoc.
     *
     * @param  name          the axis name.
     * @param  abbreviation  the axis abbreviation.
     * @param  orientation   the axis orientation.
     * @param  unit          unit of measurement associated to the axis.
     *
     * @see #printParameterString(String, double, String, double)
     */
    final void printJavadocAxisRow(final String name, final String abbreviation, final String orientation, final String unit) {
        indent(1);
        out.append(" *   <tr><td>").append(name)
               .append("</td><td>").append(abbreviation)
               .append("</td><td>").append(orientation)
               .append("</td><td>").append(unit)
               .append("</td></tr>\n");
    }

    /**
     * Prints a parameter name, value and units in Javadoc.
     *
     * @param  name     the parameter name.
     * @param  value    value to assign to the parameter, or {@link Double#NaN} if none.
     * @param  unit     unit of measurement associated to the value.
     * @param  degrees  the value in decimal degrees, or {@link Double#NaN} if none or not applicable.
     *
     * @see #printParameterString(String, double, String, double)
     */
    final void printJavadocParameterRow(final String name, final double value, final String unit, final double degrees) {
        if (!Double.isNaN(value)) {
            indent(1);
            out.append(" *   <tr><td>").append(name).append("</td><td>");
            if (unit == null || unit.equals("unity")) {
                append(value);
            } else if (unit.equals("degree")) {
                append(value);
                out.append('°');
            } else {
                final SexagesimalUnit su = SexagesimalUnit.parse(unit);
                if (su != null) {
                    su.format(value, out);
                } else {
                    append(value);
                    out.append(' ').append(unit);
                    if (Math.abs(value) > 1) {
                        out.append('s');
                    }
                }
            }
            if (!Double.isNaN(degrees)) {
                out.append(" (").append(degrees);
                trimFractionalPart();
                out.append("°)");
            }
            out.append("</td></tr>\n");
        }
    }

    /**
     * Prints the last lines for the table of axes or parameters in Javadoc.
     */
    final void printJavadocTableFooter() {
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
     *
     * @param  condition  text saying when the exception is thrown.
     */
    final void printJavadocThrows(final String condition) {
        indent(1); out.append(" *\n");
        indent(1); out.append(" * @throws FactoryException ").append(condition).append('\n');
    }

    /**
     * Prints a "see" annotation if the given {@code method} is non-null.
     *
     * @param classe the number class, or 0 for the current class.
     * @param method the method name, or {@code null} if unknown.
     */
    final void printJavadocSee(final int classe, final String method) {
        if (method != null) {
            indent(1); out.append(" *\n");
            indent(1); out.append(" * @see ");
            if (classe != 0) {
                out.append("Test").append(classe);
            }
            out.append('#').append(method).append("()\n");
        }
    }

    /**
     * Closes the javadoc comment block, then prints the test method signature.
     * The signature includes the {@code throws FactoryException} declaration.
     *
     * @param authority  {@link #EPSG} or {@link #GIGS}.
     * @param code       the EPSG or GIGS code to use in method signature, or -1 for using the name.
     * @param name       the name to use for generating a method name. Used for sorting.
     */
    final void printTestMethodSignature(final String authority, final int code, final String name) {
        indent(1); out.append(" */\n");
        indent(1); out.append("@Test\n");
        indent(1); out.append("@DisplayName(\"").append(replaceAsciiPrimeByUnicode(name)).append("\")\n");
        indent(1); out.append("public void ");
        if (code >= 0) {
            out.append(authority).append('_').append(code);
        } else {
            out.append("various");
            printJavaIdentifier(name);
        }
        out.append("() throws FactoryException {\n");
        final StringBuilder buffer = new StringBuilder(name.length());
        for (int c, i=0; i<name.length(); i += Character.charCount(c)) {
            c = name.codePointAt(i);
            if (Character.isLetterOrDigit(c) || !Character.isSpaceChar(c)) {
                buffer.appendCodePoint(Character.toLowerCase(c));
            }
        }
        methodSortKey = buffer.toString();
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
            length = Math.max(length, ((CharSequence) pairs[i]).length());
        }
        for (int i=0; i<pairs.length; i += 2) {
            final Object value = pairs[i+1];
            if (accept(value)) {
                indent(2);
                if (value instanceof SexagesimalUnit) {
                    out.append("setSexagesimalUnit(")
                       .append(((SexagesimalUnit) value).code)
                       .append(')');
                } else {
                    final CharSequence name  = (CharSequence) pairs[i];
                    out.append(name);
                    for (int j = length - name.length(); --j >= 0;) {
                        out.append(' ');
                    }
                    out.append(" = ");
                    if (value instanceof Unit<?>) {
                        printProgrammaticName((Unit<?>) value);
                    } else if (value instanceof String[]) {
                        String separator = "new String[] {\"";
                        for (final String e : (String[]) value) {
                            out.append(separator).append(e);
                            separator = "\", \"";
                        }
                        out.append("\"}");
                    } else if (value instanceof Double) {
                        final double v = (Double) value;
                        if (Double.isNaN(v)) out.append("Double.NaN");
                        else if (v == Double.POSITIVE_INFINITY) out.append("Double.POSITIVE_INFINITY");
                        else if (v == Double.NEGATIVE_INFINITY) out.append("Double.NEGATIVE_INFINITY");
                        else out.append(v);
                    } else if (value instanceof CharSequence) {
                        out.append('"').append(value).append('"');
                    } else {
                        out.append(value);
                    }
                }
                out.append(";\n");
            }
        }
    }

    /**
     * Returns {@code true} if the given value should be included in the code.
     * This method should return {@code false} when the given value is equal
     * to a default value.
     *
     * @param  value  the value to test.
     * @return whether the given value should be added to the code.
     */
    private static boolean accept(final Object value) {
        if (value == null) return false;
        if (value instanceof Boolean)  return (Boolean) value;
        if (value instanceof String[]) return ((String[]) value).length != 0;
        return true;
    }

    /**
     * Prints a statement like "{@code CoordinateSystemAxis axis1 = createCoordinateSystemAxis(…)}".
     *
     * @param  variable      name of the variable.
     * @param  name          axis name.
     * @param  abbreviation  axis abbreviation.
     * @param  orientation   axis orientation.
     * @param  unit          axis units of measurement.
     */
    final void printAxis(final String variable, final String name, final String abbreviation, final String orientation, final String unit) {
        Unit<?> parsedUnit = parseUnit(unit);
        indent(2);
        out.append("CoordinateSystemAxis ").append(variable)
           .append(" = createCoordinateSystemAxis(\"")
           .append(name).append("\", \"")
           .append(abbreviation).append("\", ")
           .append("AxisDirection.").append(orientation.toUpperCase(Locale.US)).append(", ");
        printProgrammaticName(parsedUnit);
        out.append(");\n");
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
     * Prints a call to a test method from another test file.
     * This is used for testing dependencies, for example ellipsoid in a geodetic datum.
     *
     * @param test  name of the method to call for obtaining an instance of the dependency test class.
     * @param code  EPSG code which determine the test method to call, or {@code null} if not found.
     */
    final void printCallToDependencyTest(final String test, final Integer code) {
        if (code != null) {
            indent(2);
            out.append(test).append("().EPSG_").append(code).append("();\n");
        }
    }

    /**
     * Prints a call to the {@link org.iogp.gigs.Series3000#setCodeAndName(int, String)} method.
     *
     * @param code  authority code.
     * @param name  name of the identified object.
     */
    final void printCallToSetCodeAndName(final int code, final String name) {
        indent(2);
        out.append("setCodeAndName(").append(code).append(", \"").append(name).append("\");\n");
    }

    /**
     * Prints the programmatic line that adds a parameter to a parameter group.
     *
     * @param  name     the parameter name.
     * @param  value    the parameter value, or {@link Double#NaN} if none.
     * @param  unit     the parameter unit of measurement.
     * @param  degrees  the value in decimal degrees, or {@link Double#NaN} if none or not applicable.
     *
     * @see #printJavadocParameterRow(String, double, String, double)
     */
    final void printParameterString(final String name, double value, final String unit, final double degrees) {
        if (!Double.isNaN(value)) {
            indent(2); out.append("definition.parameter(\"").append(name).append("\")");
            Unit<?> parsedUnit;
            if (!Double.isNaN(degrees)) {
                value = degrees;
                parsedUnit = units.degree();
            } else {
                parsedUnit = parseUnit(unit);
                if (parsedUnit == null) {
                    value = SexagesimalUnit.parse(unit).toDecimalDegrees(value);
                    parsedUnit = units.degree();
                }
            }
            out.append(".setValue(").append(value);
            if (unit != null) {
                out.append(", ");
                printProgrammaticName(parsedUnit);
            }
            out.append(");\n");
        }
    }

    /**
     * Saves the {@link #out} content and clears the buffer for next entry.
     * Methods will be sorted by test method names.
     */
    final void saveTestMethod() {
        final String lineSeparator = System.lineSeparator();
        if (!lineSeparator.equals("\n")) {
            int i = 0;
            while ((i = out.indexOf("\n", i)) >= 0) {
                out.replace(i, i+1, lineSeparator);
                i += lineSeparator.length();
            }
        }
        int    count = 0;
        String key   = methodSortKey;
        String value = out.toString();
        while (methods.putIfAbsent(key, value) != null) {
            key = methodSortKey + (++count);
        }
        out.setLength(0);
    }

    /**
     * Prints all saved methods to the standard output stream.
     */
    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    final void flushAllMethods() {
        methods.values().forEach(System.out::println);
        if (!unsupportedTests.isEmpty()) {
            System.out.println();
            System.out.println("Unsupported tests");
            final int[] lengths = new int[4];
            unsupportedTests.forEach((row) -> {
                for (int i=0; i<row.length; i++) {
                    final int length = row[i].length();
                    if (length > lengths[i]) {
                        lengths[i] = length;
                    }
                }
            });
            unsupportedTests.forEach((row) -> {
                System.out.print("| ");
                System.out.print(row[0]);
                for (int i=1; i<row.length; i++) {
                    final int length = row[i-1].length();
                    System.out.print(" ".repeat(lengths[i-1] - length));
                    System.out.print(" | ");
                    System.out.print(row[i]);
                }
                System.out.println();
            });
        }
    }

    /**
     * Declares that a test has been skipped because it can not be represented as a JUnit test.
     *
     * @param  series  the test series (e.g. 3204).
     * @param  test    code of the GIGS object which can not be tested.
     * @param  name    name of the GIGS object which can not be tested.
     * @param  reason  reason why the test has been skipped.
     */
    final void addUnsupportedTest(final int series, final int test, final String name, final String reason) {
        if (unsupportedTests.isEmpty()) {
            unsupportedTests.add(new String[] {"Series", "Test", "GIGS object name", "Reason for skipping"});
            unsupportedTests.add(new String[] {"----", "----", "----", "----"});
        }
        unsupportedTests.add(new String[] {String.valueOf(series), String.valueOf(test), name, reason});
    }
}
