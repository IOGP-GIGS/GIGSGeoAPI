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

import java.util.BitSet;
import java.util.Objects;
import java.util.Collection;
import java.util.logging.Logger;
import org.opengis.annotation.Obligation;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Base class of all GeoAPI validators. Validators can be configured on a case-by-case basis by
 * changing the values of non-final public fields. If the same configuration needs to be applied
 * on all validators, then {@link ValidatorContainer#all} provides a convenient way to make such
 * change in a loop.
 *
 * <p>Configurations available in this class and some subclasses are:</p>
 * <ul>
 *   <li>{@link #requireMandatoryAttributes} - controls whether unexpected null values can be tolerated.</li>
 *   <li>{@link #enforceForbiddenAttributes} - controls whether unexpected non-null values can be tolerated.</li>
 *   <li>{@link CRSValidator#enforceStandardNames} - controls whether axis
 *       names shall be restricted to ISO standards.</li>
 * </ul>
 *
 * <p>Once the configuration is finished, all validators provided in GeoAPI are thread-safe
 * provided that their configuration is not modified.</p>
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @version 1.0
 * @since   1.0
 */
public abstract class Validator {
    /**
     * The default tolerance value for comparisons of floating point numbers in validators.
     * The current value is {@value}. This value is relatively large because some implementations
     * may store their values as {@code float} numbers instead than {@code double}.
     *
     * <p>Note that {@link TestCase} subclasses use smaller tolerance thresholds, typically centimetric.
     * Test cases are stricter then validators because the tests control the objects they create,
     * while validators need to work reasonably well for arbitrary objects.</p>
     *
     * @see GeometryValidator#tolerance
     */
    public static final double DEFAULT_TOLERANCE = 1E-6;

    /**
     * The validators to use for every validations not defined in the concrete subclass.
     * For example if {@link CRSValidator} needs to validate
     * a datum, it will use the {@link DatumValidator} instance
     * defined in this container.
     *
     * <p>The container may contain this validator instance. For example if this validator
     * is an instance of {@link CRSValidator}, then the
     * {@link ValidatorContainer#crs} field may be set to {@code this}. Doing so ensure
     * that the proper {@code validate(…)} methods will be invoked in case of callback.</p>
     *
     * <p><b>Tip:</b> if the other validators are not expected to callback the {@code validate}
     * methods defined in this {@code Validator} instance (for example a datum has no reason
     * to validate a CRS), then it is safe to set this field to {@link ValidatorContainer#DEFAULT}.</p>
     */
    protected final ValidatorContainer container;

    /**
     * The logger for reporting non-fatal warnings.
     * This logger is determined by the package name given at construction time.
     */
    protected final Logger logger;

    /**
     * {@code true} if mandatory attributes are required to be non-null, or {@code false}
     * for tolerating null values. ISO specifications flags some attributes as mandatory,
     * while some other are optional. Optional attributes are allowed to be null at any time,
     * but mandatory attributes shall never be null - in theory. However implementers may
     * choose to returns {@code null} on a temporary basis while they are developing their
     * library. If this field is set to {@code false}, then missing mandatory attributes
     * will be logged as warnings instead than causing a failure.
     *
     * <p>The default value is {@code true}.</p>
     *
     * @see #mandatory(String, Object)
     */
    public boolean requireMandatoryAttributes = true;

    /**
     * {@code true} if forbidden attributes are required to be null, or {@code false} for
     * tolerating non-null values. In ISO specifications, some attributes are declared as
     * optional in parent class and specialized in subclasses, either as mandatory or as
     * forbidden. If this field is set to {@code false}, then forbidden attributes will
     * be logged as warnings instead than causing a failure.
     *
     * <p>The default value is {@code true}.</p>
     *
     * @see #forbidden(String, Object)
     */
    public boolean enforceForbiddenAttributes = true;

    /**
     * Creates a new validator instance.
     *
     * @param container    the set of validators to use for validating other kinds of objects
     *                     (see {@linkplain #container field javadoc}).
     * @param packageName  the name of the package containing the classes to be validated.
     */
    protected Validator(final ValidatorContainer container, final String packageName) {
        Objects.requireNonNull(container, "ValidatorContainer shall not be null.");
        this.container = container;
        this.logger = Logger.getLogger(packageName);
    }

    /**
     * Returns {@code true} if the given object is an empty collection.
     *
     * @param  value  the object to test, or {@code null}.
     * @return {@code true} if the given object is a non-null empty collection.
     */
    private static boolean isEmptyCollection(final Object value) {
        return (value instanceof Collection<?>) && ((Collection<?>) value).isEmpty();
    }

    /**
     * Invoked when the existence of a mandatory attribute needs to be verified.
     * If the given value is {@code null} or is an {@linkplain Collection#isEmpty()
     * empty collection}, then there is a choice:
     *
     * <ul>
     *   <li>If {@link #requireMandatoryAttributes} is {@code true} (which is the default),
     *       then the test fails with the given message.</li>
     *   <li>Otherwise, the message is logged as a warning and the test continues.</li>
     * </ul>
     *
     * Subclasses can override this method if they want more control.
     *
     * @param message  the message to send in case of failure.
     * @param value    the value to test for non-nullity.
     *
     * @see #requireMandatoryAttributes
     * @see Obligation#MANDATORY
     */
    protected void mandatory(final String message, final Object value) {
        if (requireMandatoryAttributes) {
            assertNotNull(value, message);
            assertFalse(isEmptyCollection(value), message);
        } else if (value == null || isEmptyCollection(value)) {
            WarningMessage.log(logger, message, true);
        }
    }

    /**
     * Invoked when the existence of a forbidden attribute needs to be checked.
     * If the given value is non-null and is not an {@linkplain Collection#isEmpty()
     * empty collection}, then there is a choice:
     *
     * <ul>
     *   <li>If {@link #enforceForbiddenAttributes} is {@code true} (which is the default),
     *       then the test fails with the given message.</li>
     *   <li>Otherwise, the message is logged as a warning and the test continues.</li>
     * </ul>
     *
     * Subclasses can override this method if they want more control.
     *
     * @param message  the message to send in case of failure.
     * @param value    the value to test for nullity.
     *
     * @see #enforceForbiddenAttributes
     * @see Obligation#FORBIDDEN
     */
    protected void forbidden(final String message, final Object value) {
        if (enforceForbiddenAttributes) {
            if (value instanceof Collection<?>) {
                assertTrue(((Collection<?>) value).isEmpty(), message);
            } else {
                assertNull(value, message);
            }
        } else if (value != null && !isEmptyCollection(value)) {
            WarningMessage.log(logger, message, false);
        }
    }

    /**
     * Delegates to {@link #mandatory(String, Object)} or {@link #forbidden(String, Object)}
     * depending on a condition.
     *
     * @param message    the message to send in case of failure.
     * @param value      the value to test for (non)-nullity.
     * @param condition  {@code true} if the given value is mandatory, or {@code false} if it is forbidden.
     *
     * @see Obligation#CONDITIONAL
     */
    protected void conditional(final String message, final Object value, final boolean condition) {
        if (condition) {
            mandatory(message, value);
        } else {
            forbidden(message, value);
        }
    }

    /**
     * Ensures that the elements in the given collection are compliant with the {@link Object}
     * {@code equals(Object)} and {@code hashCode()} contract. This method ensures that the
     * {@code equals(Object)} methods implement <cite>reflexive</cite>, <cite>symmetric</cite>
     * and <cite>transitive</cite> relations. It also ensures that if {@code A.equals(B)}, then
     * {@code A.hashCode() == B.hashCode()}.
     *
     * <p>If the given collection is null, then this method does nothing.
     * If the given collection contains null elements, then those elements are ignored.</p>
     *
     * <p>This method does not invoke any other {@code validate} method on collection elements.
     * It is caller responsibility to validates elements according their types.</p>
     *
     * @param collection  the collection of elements to validate, or {@code null}.
     */
    protected void validate(final Collection<?> collection) {
        if (collection == null) {
            return;
        }
        /*
         * Get an array with null elements omitted.
         */
        int count = 0;
        final Object[] elements = collection.toArray();
        for (final Object element : elements) {
            if (element != null) {
                elements[count++] = element;
            }
        }
        /*
         * Store the hash code before to do any comparison
         * in order to detect unexpected changes.
         */
        final int[] hashCodes = new int[count];
        for (int i=0; i<count; i++) {
            hashCodes[i] = elements[i].hashCode();
        }
        /*
         * Marks every objects that are equal.
         */
        final BitSet[] equalMasks = new BitSet[count];
        for (int i=0; i<count; i++) {
            final Object toCompare = elements  [i];
            final int    hashCode  = hashCodes [i];
            final BitSet equalMask = equalMasks[i] = new BitSet(count);
            for (int j=0; j<count; j++) {
                final Object candidate = elements[j];
                if (toCompare.equals(candidate)) {
                    assertEquals(hashCode, candidate.hashCode(), "Inconsistent hash codes.");
                    equalMask.set(j);
                }
            }
            assertFalse(toCompare.equals(null), "equals(null):");
        }
        /*
         * Now compare the sets of objects marked as equal.
         */
        for (int i=0; i<count; i++) {
            final BitSet equalMask = equalMasks[i];
            assertTrue(equalMask.get(i), "equals(this) shall be reflexive.");
            for (int j=0; (j = equalMask.nextSetBit(j)) >= 0; j++) {
                assertEquals(equalMask, equalMasks[j], "A.equals(B) shall be symmetric and transitive.");
            }
            assertEquals(hashCodes[i], elements[i].hashCode(), "The hash code value has changed.");
        }
    }
}
