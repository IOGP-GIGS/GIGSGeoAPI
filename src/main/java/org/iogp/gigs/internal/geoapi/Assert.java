/*
 *    GeoAPI - Java interfaces for OGC/ISO standards
 *    http://www.geoapi.org
 *
 *    Copyright (C) 2008-2021 Open Geospatial Consortium, Inc.
 *    All Rights Reserved. http://www.opengeospatial.org/ogc/legal
 *
 *    Permission to use, copy, and modify this software and its documentation, with
 *    or without modification, for any purpose and without fee or royalty is hereby
 *    granted, provided that you include the following on ALL copies of the software
 *    and documentation or portions thereof, including modifications, that you make:
 *
 *    1. The full text of this NOTICE in a location viewable to users of the
 *       redistributed or derivative work.
 *    2. Notice of any changes or modifications to the OGC files, including the
 *       date changes were made.
 *
 *    THIS SOFTWARE AND DOCUMENTATION IS PROVIDED "AS IS," AND COPYRIGHT HOLDERS MAKE
 *    NO REPRESENTATIONS OR WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 *    TO, WARRANTIES OF MERCHANTABILITY OR FITNESS FOR ANY PARTICULAR PURPOSE OR THAT
 *    THE USE OF THE SOFTWARE OR DOCUMENTATION WILL NOT INFRINGE ANY THIRD PARTY
 *    PATENTS, COPYRIGHTS, TRADEMARKS OR OTHER RIGHTS.
 *
 *    COPYRIGHT HOLDERS WILL NOT BE LIABLE FOR ANY DIRECT, INDIRECT, SPECIAL OR
 *    CONSEQUENTIAL DAMAGES ARISING OUT OF ANY USE OF THE SOFTWARE OR DOCUMENTATION.
 *
 *    The name and trademarks of copyright holders may NOT be used in advertising or
 *    publicity pertaining to the software without specific, written prior permission.
 *    Title to copyright in this software and any associated documentation will at all
 *    times remain with copyright holders.
 */
package org.iogp.gigs.internal.geoapi;

import java.util.Collection;
import org.opengis.util.InternationalString;
import org.opengis.metadata.citation.Citation;
import org.opengis.referencing.ReferenceIdentifier;
import org.opengis.referencing.operation.Matrix;
import org.opengis.referencing.cs.AxisDirection;
import org.opengis.referencing.cs.CoordinateSystem;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Extension to JUnit assertion methods.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @version 1.0
 * @since   1.0
 */
public final strictfp class Assert {
    /**
     * The keyword for unrestricted value in {@link String} arguments.
     */
    private static final String UNRESTRICTED = "##unrestricted";

    /**
     * Do not allow instantiation of this class.
     */
    private Assert() {
    }

    /**
     * Returns the given message followed by a space,
     * or an empty string if the message is null.
     *
     * @param  message  the message, or {@code null}.
     * @return a non-null message.
     */
    private static String nonNull(final String message) {
        return (message != null) ? message.trim().concat(" ") : "";
    }

    /**
     * Returns the concatenation of the given message with the given extension.
     * This method returns the given extension if the message is null or empty.
     *
     * <p>Invoking this method is equivalent to invoking {@code nonNull(message) + ext},
     * but avoid the creation of temporary objects in the common case where the message
     * is null.</p>
     *
     * @param  message  the message, or {@code null}.
     * @param  ext      the extension to append after the message.
     * @return the concatenated string.
     */
    private static String concat(String message, final String ext) {
        if (message == null || (message = message.trim()).isEmpty()) {
            return ext;
        }
        return message + ' ' + ext;
    }

    /**
     * Verifies if we expected a null value, then returns {@code true} if the value is null as expected.
     *
     * @param  message   the message to show in case of test failure, or {@code null}.
     * @param  expected  the expected value (only its existence is checked).
     * @param  actual    the actual value (only its existence is checked).
     * @return whether the actual value is null.
     */
    private static boolean isNull(final String message, final Object expected, final Object actual) {
        final boolean isNull = (actual == null);
        if (isNull != (expected == null)) {
            fail(concat(message, isNull ? "Value is null." : "Expected null."));
        }
        return isNull;
    }

    /**
     * Asserts that the given integer value is positive, including zero.
     *
     * @param message  header of the exception message in case of failure, or {@code null} if none.
     * @param value    the value to test.
     */
    public static void assertPositive(final String message, final int value) {
        if (value < 0) {
            fail(nonNull(message) + "Value is " + value + '.');
        }
    }

    /**
     * Asserts that the given integer value is strictly positive, excluding zero.
     *
     * @param message  header of the exception message in case of failure, or {@code null} if none.
     * @param value    the value to test.
     */
    public static void assertStrictlyPositive(final String message, final int value) {
        if (value <= 0) {
            fail(nonNull(message) + "Value is " + value + '.');
        }
    }

    /**
     * Asserts that the given minimum and maximum values make a valid range. More specifically
     * asserts that if both values are non-null, then the minimum value is not greater than the
     * maximum value.
     *
     * @param <T>      the type of values being compared.
     * @param message  header of the exception message in case of failure, or {@code null} if none.
     * @param minimum  the lower bound of the range to test, or {@code null} if unbounded.
     * @param maximum  the upper bound of the range to test, or {@code null} if unbounded.
     */
    @SuppressWarnings("unchecked")
    public static <T> void assertValidRange(final String message, final Comparable<T> minimum, final Comparable<T> maximum) {
        if (minimum != null && maximum != null) {
            if (minimum.compareTo((T) maximum) > 0) {
                fail(nonNull(message) + "Range found is [" + minimum + " ... " + maximum + "].");
            }
        }
    }

    /**
     * Asserts that the given minimum is smaller or equals to the given maximum.
     *
     * @param message  header of the exception message in case of failure, or {@code null} if none.
     * @param minimum  the lower bound of the range to test.
     * @param maximum  the upper bound of the range to test.
     */
    public static void assertValidRange(final String message, final int minimum, final int maximum) {
        if (minimum > maximum) {
            fail(nonNull(message) + "Range found is [" + minimum + " ... " + maximum + "].");
        }
    }

    /**
     * Asserts that the given minimum is smaller or equals to the given maximum.
     * If one bound is or both bounds are {@linkplain Double#NaN NaN}, then the test fails.
     *
     * @param message  header of the exception message in case of failure, or {@code null} if none.
     * @param minimum  the lower bound of the range to test.
     * @param maximum  the upper bound of the range to test.
     */
    public static void assertValidRange(final String message, final double minimum, final double maximum) {
        if (!(minimum <= maximum)) { // Use '!' for catching NaN.
            fail(nonNull(message) + "Range found is [" + minimum + " ... " + maximum + "].");
        }
    }

    /**
     * Asserts that the given value is inside the given range. This method does <strong>not</strong>
     * test the validity of the given [{@code minimum} … {@code maximum}] range.
     *
     * @param <T>      the type of values being compared.
     * @param message  header of the exception message in case of failure, or {@code null} if none.
     * @param minimum  the lower bound of the range (inclusive), or {@code null} if unbounded.
     * @param maximum  the upper bound of the range (inclusive), or {@code null} if unbounded.
     * @param value    the value to test, or {@code null} (which is a failure).
     */
    public static <T> void assertBetween(final String message, final Comparable<T> minimum, final Comparable<T> maximum, T value) {
        if (minimum != null) {
            if (minimum.compareTo(value) > 0) {
                fail(nonNull(message) + "Value " + value + " is less than " + minimum + '.');
            }
        }
        if (maximum != null) {
            if (maximum.compareTo(value) < 0) {
                fail(nonNull(message) + "Value " + value + " is greater than " + maximum + '.');
            }
        }
    }

    /**
     * Asserts that the given value is inside the given range. This method does <strong>not</strong>
     * test the validity of the given [{@code minimum} … {@code maximum}] range.
     *
     * @param message  header of the exception message in case of failure, or {@code null} if none.
     * @param minimum  the lower bound of the range, inclusive.
     * @param maximum  the upper bound of the range, inclusive.
     * @param value    the value to test.
     */
    public static void assertBetween(final String message, final int minimum, final int maximum, final int value) {
        if (value < minimum) {
            fail(nonNull(message) + "Value " + value + " is less than " + minimum + '.');
        }
        if (value > maximum) {
            fail(nonNull(message) + "Value " + value + " is greater than " + maximum + '.');
        }
    }

    /**
     * Asserts that the given value is inside the given range. If the given {@code value} is
     * {@linkplain Double#NaN NaN}, then this test passes silently. This method does <strong>not</strong>
     * test the validity of the given [{@code minimum} … {@code maximum}] range.
     *
     * @param message  header of the exception message in case of failure, or {@code null} if none.
     * @param minimum  the lower bound of the range, inclusive.
     * @param maximum  the upper bound of the range, inclusive.
     * @param value    the value to test.
     */
    public static void assertBetween(final String message, final double minimum, final double maximum, final double value) {
        if (value < minimum) {
            fail(nonNull(message) + "Value " + value + " is less than " + minimum + '.');
        }
        if (value > maximum) {
            fail(nonNull(message) + "Value " + value + " is greater than " + maximum + '.');
        }
    }

    /**
     * Asserts that the given value is contained in the given collection. If the given collection
     * is null, then this test passes silently (a null collection is considered as "unknown", not
     * empty). If the given value is null, then the test passes only if the given collection
     * contains the null element.
     *
     * @param message     header of the exception message in case of failure, or {@code null} if none.
     * @param collection  the collection where to look for inclusion, or {@code null} if unrestricted.
     * @param value       the value to test for inclusion.
     */
    public static void assertContains(final String message, final Collection<?> collection, final Object value) {
        if (collection != null) {
            if (!collection.contains(value)) {
                fail(nonNull(message) + "Looked for value \"" + value + "\" in a collection of " +
                        collection.size() + "elements.");
            }
        }
    }

    /**
     * Asserts that the title or an alternate title of the given citation is equal to the given string.
     * This method is typically used for testing if a citation stands for the OGC, OGP or EPSG authority
     * for instance. Such abbreviations are often declared as {@linkplain Citation#getAlternateTitles()
     * alternate titles} rather than the main {@linkplain Citation#getTitle() title}, but this method
     * tests both for safety.
     *
     * @param message   header of the exception message in case of failure, or {@code null} if none.
     * @param expected  the expected title or alternate title.
     * @param actual    the citation to test.
     */
    public static void assertAnyTitleEquals(final String message, final String expected, final Citation actual) {
        if (isNull(message, expected, actual)) {
            return;
        }
        InternationalString title = actual.getTitle();
        if (title != null && expected.equals(title.toString())) {
            return;
        }
        for (final InternationalString t : actual.getAlternateTitles()) {
            if (expected.equals(t.toString())) {
                return;
            }
        }
        fail(concat(message, '"' + expected + "\" not found in title or alternate titles."));
    }

    /**
     * Asserts that the given identifier is equals to the given authority, code space, version and code.
     * If any of the above-cited properties is {@code ""##unrestricted"}, then it will not be verified.
     * This flexibility is useful in the common case where a test accepts any {@code version} value.
     *
     * @param message    header of the exception message in case of failure, or {@code null} if none.
     * @param authority  the expected authority title or alternate title (may be {@code null}), or {@code "##unrestricted"}.
     * @param codeSpace  the expected code space (may be {@code null}), or {@code "##unrestricted"}.
     * @param version    the expected version    (may be {@code null}), or {@code "##unrestricted"}.
     * @param code       the expected code value (may be {@code null}), or {@code "##unrestricted"}.
     * @param actual     the identifier to test.
     */
    public static void assertIdentifierEquals(final String message, final String authority, final String codeSpace,
            final String version, final String code, final ReferenceIdentifier actual)
    {
        if (actual == null) {
            fail(concat(message, "Identifier is null"));
        } else {
            if (!UNRESTRICTED.equals(authority)) assertAnyTitleEquals(message,                     authority, actual.getAuthority());
            if (!UNRESTRICTED.equals(codeSpace)) assertEquals(concat(message, "Wrong code space"), codeSpace, actual.getCodeSpace());
            if (!UNRESTRICTED.equals(version))   assertEquals(concat(message, "Wrong version"),    version,   actual.getVersion());
            if (!UNRESTRICTED.equals(code))      assertEquals(concat(message, "Wrong code"),       code,      actual.getCode());
        }
    }

    /**
     * Asserts that the character sequences are equal, ignoring any characters that are not valid for Unicode identifiers.
     * First, this method locates the {@linkplain Character#isUnicodeIdentifierStart(int) Unicode identifier start}
     * in each sequences, ignoring any other characters before them. Then, starting from the identifier starts, this
     * method compares only the {@linkplain Character#isUnicodeIdentifierPart(int) Unicode identifier parts} until
     * the end of character sequences.
     *
     * <p><b>Examples:</b> {@code "WGS 84"} and {@code "WGS84"} as equal according this method.</p>
     *
     * @param message     header of the exception message in case of failure, or {@code null} if none.
     * @param expected    the expected character sequence (may be {@code null}), or {@code "##unrestricted"}.
     * @param actual      the character sequence to compare, or {@code null}.
     * @param ignoreCase  {@code true} for ignoring case.
     */
    public static void assertUnicodeIdentifierEquals(final String message,
            final CharSequence expected, final CharSequence actual, final boolean ignoreCase)
    {
        if (UNRESTRICTED.equals(expected) || isNull(message, expected, actual)) {
            return;
        }
        final int expLength = expected.length();
        final int valLength = actual.length();
        int       expOffset = 0;
        int       valOffset = 0;
        boolean   expPart   = false;
        boolean   valPart   = false;
        while (expOffset < expLength) {
            int expCode = Character.codePointAt(expected, expOffset);
            if (isUnicodeIdentifier(expCode, expPart)) {
                expPart = true;
                int valCode;
                do {
                    if (valOffset >= valLength) {
                        fail(nonNull(message) + "Expected \"" + expected + "\" but got \"" + actual + "\". "
                                + "Missing part: \"" + expected.subSequence(expOffset, expLength) + "\".");
                        return;
                    }
                    valCode    = Character.codePointAt(actual, valOffset);
                    valOffset += Character.charCount(valCode);
                } while (!isUnicodeIdentifier(valCode, valPart));
                valPart = true;
                if (ignoreCase) {
                    expCode = Character.toLowerCase(expCode);
                    valCode = Character.toLowerCase(valCode);
                }
                if (valCode != expCode) {
                    fail(nonNull(message) + "Expected \"" + expected + "\" but got \"" + actual + "\".");
                    return;
                }
            }
            expOffset += Character.charCount(expCode);
        }
        while (valOffset < valLength) {
            final int valCode = Character.codePointAt(actual, valOffset);
            if (isUnicodeIdentifier(valCode, valPart)) {
                fail(nonNull(message) + "Expected \"" + expected + "\", but found it with a unexpected "
                        + "trailing string: \"" + actual.subSequence(valOffset, valLength) + "\".");
            }
            valOffset += Character.charCount(valCode);
        }
    }

    /**
     * Returns {@code true} if the given codepoint is an unicode identifier start or part.
     *
     * @param  codepoint  the code point to test.
     * @param  part       {@code false} for identifier start, or {@code true} for identifier part.
     * @return whether the given code point is a Unicode identifier start or part.
     */
    private static boolean isUnicodeIdentifier(final int codepoint, final boolean part) {
        return part ? Character.isUnicodeIdentifierPart (codepoint)
                    : Character.isUnicodeIdentifierStart(codepoint);
    }

    /**
     * Asserts that all axes in the given coordinate system are pointing toward the given directions,
     * in the same order.
     *
     * @param message   header of the exception message in case of failure, or {@code null} if none.
     * @param cs        the coordinate system to test.
     * @param expected  the expected axis directions.
     */
    public static void assertAxisDirectionsEqual(String message,
            final CoordinateSystem cs, final AxisDirection... expected)
    {
        assertEquals(expected.length, cs.getDimension(), concat(message, "Wrong coordinate system dimension."));
        message = concat(message, "Wrong axis direction.");
        for (int i=0; i<expected.length; i++) {
            assertEquals(expected[i], cs.getAxis(i).getDirection(), message);
        }
    }

    /**
     * Asserts that the given matrix is equals to the expected one, up to the given tolerance value.
     *
     * @param message    header of the exception message in case of failure, or {@code null} if none.
     * @param expected   the expected matrix, which may be {@code null}.
     * @param actual     the matrix to compare, or {@code null}.
     * @param tolerance  the tolerance threshold.
     */
    public static void assertMatrixEquals(final String message, final Matrix expected, final Matrix actual, final double tolerance) {
        if (isNull(message, expected, actual)) {
            return;
        }
        final int numRow = actual.getNumRow();
        final int numCol = actual.getNumCol();
        assertEquals(expected.getNumRow(), numRow, "numRow");
        assertEquals(expected.getNumCol(), numCol, "numCol");
        for (int j=0; j<numRow; j++) {
            for (int i=0; i<numCol; i++) {
                final double e = expected.getElement(j,i);
                final double a = actual.getElement(j,i);
                if (!(StrictMath.abs(e - a) <= tolerance) && Double.doubleToLongBits(a) != Double.doubleToLongBits(e)) {
                    fail(nonNull(message) + "Matrix.getElement(" + j + ", " + i + "): expected " + e + " but got " + a);
                }
            }
        }
    }
}
