/*
 *    GeoAPI - Java interfaces for OGC/ISO standards
 *    http://www.geoapi.org
 *
 *    Copyright (C) 2011-2021 Open Geospatial Consortium, Inc.
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
package org.iogp.gigs;

import javax.measure.Unit;
import javax.measure.quantity.Angle;
import javax.measure.quantity.Length;
import org.opengis.util.Factory;
import org.opengis.metadata.Identifier;
import org.opengis.referencing.IdentifiedObject;
import org.opengis.referencing.cs.AxisDirection;
import org.opengis.referencing.cs.CoordinateSystem;
import org.opengis.referencing.cs.CoordinateSystemAxis;
import org.opengis.referencing.datum.Ellipsoid;
import org.opengis.referencing.datum.PrimeMeridian;
import org.opengis.test.ValidatorContainer;
import org.opengis.test.Validators;
import org.opengis.test.Units;
import org.junit.AssumptionViolatedException;

import static org.junit.Assert.*;


/**
 * Base class of all GIGS tests.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @version 1.0
 * @since   1.0
 */
strictfp abstract class TestCase extends org.opengis.test.TestCase {
    /**
     * The keyword for unrestricted value in {@link String} arguments.
     */
    private static final String UNRESTRICTED = "##unrestricted";

    /**
     * Relative tolerance factor from GIGS documentation.
     * This tolerance threshold is typically multiplied
     * by the magnitude of the value being compared.
     */
    static final double TOLERANCE = 1E-10;

    /**
     * Absolute angular tolerance from GIGS documentation.
     * This tolerance threshold is <strong>not</strong>
     * multiplied by the value being compared.
     */
    static final double ANGULAR_TOLERANCE = 1E-7;

    /**
     * The list of tests that are enabled.
     * This is a static field for now but will become configurable in a future version.
     */
    private static final Configuration config = new Configuration();

    /**
     * Provider of units of measurement (degree, metre, second, <i>etc</i>), never {@code null}.
     * The {@link Units#degree()}, {@link Units#metre() metre()} and other methods shall return
     * {@link javax.measure.Unit} instances compatible with the units created by the {@link Factory}
     * instances to be tested. Those {@code Unit<?>} instances depend on the Unit of Measurement (JSR-373)
     * implementation used by the factories.
     */
    final Units units;

    /**
     * Should be part of {@link #units}, but missing as of GeoAPI 3.0.1.
     */
    static final Unit<Length> footSurveyUS, foot;

    /**
     * Should be part of {@link #units}, but missing as of GeoAPI 3.0.1.
     */
    static final Unit<Angle> grad;
    static {
        final Units units = Units.getDefault();
        footSurveyUS = units.metre().multiply(12 / 39.37);
        foot         = units.metre().multiply(0.3048);
        grad         = units.radian().multiply(Math.PI / 200);
    }

    /**
     * The set of {@link Validator} instances to use for verifying objects conformance (never {@code null}).
     */
    final ValidatorContainer validators;

    /**
     * A tip set by subclasses during the execution of some optional tests.
     * In case of optional test failure, if this field is non-null, then a message will be logged at the
     * {@link java.util.logging.Level#INFO} for giving some tips to the developer about how he can disable the test.
     *
     * <p><b>Example</b></p>
     * <blockquote><pre>&#64;Test
     *public void myTest() {
     *    if (isDerivativeSupported) {
     *        configurationTip = Configuration.Key.isDerivativeSupported;
     *        // Do some tests the require support of math transform derivatives.
     *    }
     *    configurationTip = null;
     *}</pre></blockquote>
     */
    transient Configuration.Key<Boolean> configurationTip;

    /**
     * Creates a new test.
     */
    TestCase() {
        units = Units.getDefault();
        validators = Validators.DEFAULT;
    }

    /**
     * Returns booleans indicating whether the given operations are enabled.
     *
     * @param  properties  the key for which the flags are wanted.
     * @return an array of the same length than {@code properties} in which each element at index
     *         <var>i</var> indicates whether the {@code properties[i]} test should be enabled.
     */
    @SafeVarargs
    final boolean[] getEnabledFlags(final Configuration.Key<Boolean>... properties) {
        final boolean[] isEnabled = new boolean[properties.length];
        for (int i=0; i<properties.length; i++) {
            final Boolean value = config.get(properties[i]);
            isEnabled[i] = (value == null) || value;
        }
        return isEnabled;
    }

    /**
     * Invoked when the implementation does not support one of the codes defined in the GIGS test suite.
     * This method has a behavior equivalent to a call to {@code assumeTrue(false)}, which will cause
     * the test to terminate with the "ignored" status.
     *
     * @param  type  the GeoAPI interface of the tested object.
     * @param  code  the EPSG code or name of the tested object.
     */
    final void unsupportedCode(final Class<?> type, final Object code) {
        final StringBuilder buffer = new StringBuilder(50).append(type.getSimpleName()).append('[');
        final boolean quote = !(code instanceof Number);
        if (quote) buffer.append('"');
        buffer.append(code);
        if (quote) buffer.append('"');
        buffer.append("] not supported.");
        throw new AssumptionViolatedException(buffer.toString());
    }

    /**
     * Returns the name of the given object, or {@code null} if none.
     */
    static String getName(final IdentifiedObject object) {
        if (object != null) {
            final Identifier name = object.getName();
            if (name != null) {
                return name.getCode();
            }
        }
        return null;
    }

    /**
     * Returns the given message, or an empty string if the message is null.
     */
    private static String nonNull(final String message) {
        return (message != null) ? message.trim().concat(" ") : "";
    }

    /**
     * Verifies if we expected a null value, then returns {@code true} if the value is null as expected.
     */
    private static boolean isNull(final String message, final Object expected, final Object actual) {
        final boolean isNull = (actual == null);
        if (isNull != (expected == null)) {
            fail(concat(message, isNull ? "Value is null." : "Expected null."));
        }
        return isNull;
    }

    /**
     * Returns {@code true} if the given codepoint is an unicode identifier start or part.
     */
    private static boolean isUnicodeIdentifier(final int codepoint, final boolean part) {
        return part ? Character.isUnicodeIdentifierPart (codepoint)
                    : Character.isUnicodeIdentifierStart(codepoint);
    }

    /**
     * Returns the concatenation of the given message with the given extension.
     * This method returns the given extension if the message is null or empty.
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
     * Asserts that all axes in the given coordinate system are pointing toward the given
     * directions, in the same order.
     *
     * @param message   header of the exception message in case of failure, or {@code null} if none.
     * @param cs        the coordinate system to test.
     * @param expected  the expected axis directions.
     */
    static void assertAxisDirectionsEqual(String message,
            final CoordinateSystem cs, final AxisDirection... expected)
    {
        assertEquals(concat(message, "Wrong coordinate system dimension."), expected.length, cs.getDimension());
        message = concat(message, "Wrong axis direction.");
        for (int i=0; i<expected.length; i++) {
            assertEquals(message, expected[i], cs.getAxis(i).getDirection());
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
    private static void assertUnicodeIdentifierEquals(final String message,
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
     * Compares the name, axis lengths and inverse flattening factor of the given ellipsoid against the expected values.
     * This method allows for some flexibilities:
     *
     * <ul>
     *   <li>{@link Ellipsoid#getName()} allows for the same flexibilities than the one documented in
     *       {@link #verifyIdentification verifyIdentification(…)}.</li>
     *   <li>{@link Ellipsoid#getSemiMajorAxis()} does not need to use the unit of measurement given
     *       by the {@code axisUnit} argument. Unit conversion will be applied as needed.</li>
     * </ul>
     *
     * The tolerance thresholds are 0.5 unit of the last digits of the values found in the EPSG database:
     * <ul>
     *   <li>3 decimal digits for {@code semiMajor} values in metres.</li>
     *   <li>9 decimal digits for {@code inverseFlattening} values.</li>
     * </ul>
     *
     * If the given {@code ellipsoid} is {@code null}, then this method does nothing.
     * Deciding if {@code null} datum are allowed or not is {@link org.opengis.test.Validator}'s job.
     *
     * @param ellipsoid          the ellipsoid to verify, or {@code null} if none.
     * @param name               the expected name (ignoring code space), or {@code null} if unrestricted.
     * @param semiMajor          the expected semi-major axis length, in units given by the {@code axisUnit} argument.
     * @param inverseFlattening  the expected inverse flattening factor.
     * @param axisUnit           the unit of the {@code semiMajor} argument (not necessarily the actual unit of the ellipsoid).
     */
    final void verifyFlattenedSphere(final Ellipsoid ellipsoid, final String name,
            final double semiMajor, final double inverseFlattening, final Unit<Length> axisUnit)
    {
        if (ellipsoid != null) {
            if (name != null) {
                assertUnicodeIdentifierEquals("Ellipsoid.getName().getCode()", name, getName(ellipsoid), true);
            }
            final Unit<Length> actualUnit = ellipsoid.getAxisUnit();
            assertNotNull("Ellipsoid.getAxisUnit()", actualUnit);
            assertEquals("Ellipsoid.getSemiMajorAxis()", semiMajor,
                    actualUnit.getConverterTo(axisUnit).convert(ellipsoid.getSemiMajorAxis()),
                    units.metre().getConverterTo(axisUnit).convert(5E-4));
            assertEquals("Ellipsoid.getInverseFlattening()", inverseFlattening, ellipsoid.getInverseFlattening(), 5E-10);
        }
    }

    /**
     * Compares the name and Greenwich longitude of the given prime meridian against the expected values.
     * This method allows for some flexibilities:
     *
     * <ul>
     *   <li>{@link PrimeMeridian#getName()} allows for the same flexibilities than the one documented in
     *       {@link #verifyIdentification verifyIdentification(…)}.</li>
     *   <li>{@link PrimeMeridian#getGreenwichLongitude()} does not need to use the unit of measurement given
     *       by the {@code angularUnit} argument. Unit conversion will be applied as needed.</li>
     * </ul>
     *
     * The tolerance threshold is 0.5 unit of the last digit of the values found in the EPSG database:
     * <ul>
     *   <li>7 decimal digits for {@code greenwichLongitude} values in degrees.</li>
     * </ul>
     *
     * If the given {@code primeMeridian} is {@code null}, then this method does nothing.
     * Deciding if {@code null} prime meridians are allowed or not is {@link org.opengis.test.Validator}'s job.
     *
     * @param primeMeridian       the prime meridian to verify, or {@code null} if none.
     * @param name                the expected name (ignoring code space), or {@code null} if unrestricted.
     * @param greenwichLongitude  the expected Greenwich longitude, in units given by the {@code angularUnit} argument.
     * @param angularUnit         the unit of the {@code greenwichLongitude} argument (not necessarily the actual unit of the prime meridian).
     */
    final void verifyPrimeMeridian(final PrimeMeridian primeMeridian, final String name,
            final double greenwichLongitude, final Unit<Angle> angularUnit)
    {
        if (primeMeridian != null) {
            if (name != null) {
                assertUnicodeIdentifierEquals("PrimeMeridian.getName().getCode()", name, getName(primeMeridian), true);
            }
            final Unit<Angle> actualUnit = primeMeridian.getAngularUnit();
            assertNotNull("PrimeMeridian.getAngularUnit()", actualUnit);
            assertEquals("PrimeMeridian.getGreenwichLongitude()", greenwichLongitude,
                    actualUnit.getConverterTo(angularUnit).convert(primeMeridian.getGreenwichLongitude()),
                    units.degree().getConverterTo(angularUnit).convert(5E-8));
        }
    }

    /**
     * Compares the type, axis units and directions of the given coordinate system against the expected values.
     * This method does not verify the coordinate system name because it is usually not significant.
     * This method does not verify axis names neither because the names specified by ISO 19111 and ISO 19162 differ.
     *
     * <p>If the given {@code cs} is {@code null}, then this method does nothing.
     * Deciding if {@code null} coordinate systems are allowed or not is {@link org.opengis.test.Validator}'s job.</p>
     *
     * @param  cs          the coordinate system to verify, or {@code null} if none.
     * @param  type        the expected coordinate system type.
     * @param  directions  the expected axis directions. The length of this array determines the expected {@code cs} dimension.
     * @param  axisUnits   the expected axis units. If the array length is less than the {@code cs} dimension,
     *                     then the last unit is repeated for all remaining dimensions.
     *                     If the array length is greater, than extra units are ignored.
     */
    final void verifyCoordinateSystem(final CoordinateSystem cs, final Class<? extends CoordinateSystem> type,
            final AxisDirection[] directions, final Unit<?>... axisUnits)
    {
        if (cs != null) {
            assertEquals("CoordinateSystem.getDimension()", directions.length, cs.getDimension());
            for (int i=0; i<directions.length; i++) {
                final CoordinateSystemAxis axis = cs.getAxis(i);
                assertNotNull("CoordinateSystem.getAxis(*)", axis);
                assertEquals ("CoordinateSystem.getAxis(*).getDirection()", directions[i], axis.getDirection());
                assertEquals ("CoordinateSystem.getAxis(*).getUnit()", axisUnits[Math.min(i, axisUnits.length-1)], axis.getUnit());
            }
        }
    }

    /**
     * Compares the name and identifier of the given {@code object} against the expected values.
     * This method allows for some flexibilities:
     *
     * <ul>
     *   <li>For {@link IdentifiedObject#getName()}:
     *     <ul>
     *       <li>Only the value returned by {@link Identifier#getCode()} is verified.
     *           The code space, authority and version are ignored.</li>
     *       <li>Only the characters that are valid for Unicode identifiers are compared (ignoring case), as documented in
     *           {@link org.opengis.test.Assert#assertUnicodeIdentifierEquals Assert.assertUnicodeIdentifierEquals(…)}.</li>
     *     </ul>
     *   </li>
     *   <li>For {@link IdentifiedObject#getIdentifiers()}:
     *     <ul>
     *       <li>Only the value returned by {@link Identifier#getCode()} is verified.
     *           The code space, authority and version are ignored.</li>
     *       <li>The identifiers collection can contain more identifiers than the expected one,
     *           and the expected identifier does not need to be first.</li>
     *       <li>The comparison is case-insensitive.</li>
     *     </ul>
     *   </li>
     * </ul>
     *
     * If the given {@code object} is {@code null}, then this method does nothing.
     * Deciding if {@code null} objects are allowed or not is {@link org.opengis.test.Validator}'s job.
     *
     * @param object      the object to verify, or {@code null} if none.
     * @param name        the expected name (ignoring code space), or {@code null} if unrestricted.
     * @param identifier  the expected identifier code (ignoring code space), or {@code null} if unrestricted.
     */
    final void verifyIdentification(final IdentifiedObject object, final String name, final String identifier) {
        if (object != null) {
            if (name != null) {
                assertUnicodeIdentifierEquals("getName().getCode()", name, getName(object), true);
            }
            if (identifier != null) {
                for (final Identifier id : object.getIdentifiers()) {
                    assertNotNull("getName().getIdentifiers()", id);
                    if (identifier.equalsIgnoreCase(id.getCode())) {
                        return;
                    }
                }
                fail("getName().getIdentifiers(): element “" + identifier + "” not found.");
            }
        }
    }
}
