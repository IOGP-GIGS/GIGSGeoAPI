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

import java.util.Map;
import java.util.HashMap;
import org.opengis.metadata.Identifier;
import org.opengis.util.FactoryException;
import org.opengis.referencing.ObjectFactory;
import org.opengis.referencing.IdentifiedObject;
import org.iogp.gigs.internal.geoapi.Configuration;

import static org.junit.Assert.*;


/**
 * Base class for tests of new CRS definitions (3000 series).
 * The test procedures in this series evaluate the software’s capabilities
 * for adding user-defined CRS and transformation definitions.
 *
 * @param  <T>  the type of objects to test.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @version 1.0
 * @since   1.0
 */
public abstract class Series3000<T> extends TestCase {
    /**
     * The properties to be given in argument to a {@code ObjectFactory.createXXX(String)} method.
     * This map contains at least the given entries:
     *
     * <ul>
     *   <li>A {@link String} value associated to the {@value org.opengis.referencing.IdentifiedObject#NAME_KEY} key.</li>
     *   <li>An {@link Identifier} value associated to the {@value org.opengis.referencing.IdentifiedObject#IDENTIFIERS_KEY} key.</li>
     * </ul>
     *
     * This map is populated by all test methods before to create and verify the {@code T} instance.
     */
    public final Map<String,Object> properties;

    /**
     * Whether the objects created by the tested {@link ObjectFactory} use the specified values <i>as-is</i>.
     * This flag should be set to {@code false} if the factory performs any of the following operations:
     *
     * <ul>
     *   <li>Convert numerical values from user-provided linear units to metres.</li>
     *   <li>Convert numerical values from user-provided angular units to degrees.</li>
     *   <li>Change ellipsoid second defining parameter
     *       (e.g. from <i>semi-major axis length</i> to an equivalent <i>inverse flattening factor</i>).</li>
     *   <li>Change map projection parameters
     *       (e.g. from <i>standard parallel</i> to an equivalent <i>scale factor</i>).</li>
     *   <li>Any other change that preserve numeric equivalence.</li>
     * </ul>
     *
     * If the factory does not perform any of the above conversions, then this flag can be {@code true}.
     */
    protected boolean isFactoryPreservingUserValues;

    /**
     * If {@code true}, initialize the data but do not run the test.
     */
    boolean skipTests;

    /**
     * Creates a new test.
     */
    Series3000() {
        properties = new HashMap<>(4);
        @SuppressWarnings("unchecked")
        final boolean[] isEnabled = getEnabledFlags(
                Configuration.Key.isFactoryPreservingUserValues);
        isFactoryPreservingUserValues = isEnabled[0];
    }

    /**
     * Copies the configuration to the given test cases. This method is invoked when a test depends on other tests,
     * in which case the other tests need to be run with the same configuration in order to get data.
     *
     * @param  destinations  the test cases to configure.
     */
    final void copyConfigurationTo(final Series3000<?>... destinations) {
        for (final Series3000<?> destination : destinations) {
            destination.isFactoryPreservingUserValues = isFactoryPreservingUserValues;
        }
    }

    /**
     * Creates a map containing the given name and code, to be given to object factories.
     *
     * @param  code  the GIGS (not EPSG) code of the object to create.
     * @param  name  the name of the object to create.
     * @return properties to be given to the {@code create(…)} method.
     */
    static Map<String,Object> properties(final int code, final String name) {
        final Map<String,Object> properties = new HashMap<>(4);
        assertNull(properties.put(IdentifiedObject.IDENTIFIERS_KEY, new SimpleIdentifier(code)));
        assertNull(properties.put(IdentifiedObject.NAME_KEY, name));
        return properties;
    }

    /**
     * Sets the GIGS code name in the {@link #properties} map.
     *
     * @param  code  the GIGS (not EPSG) code of the object to create.
     * @param  name  the name of the object to create.
     */
    final void setCodeAndName(final int code, final String name) {
        assertNull(IdentifiedObject.NAME_KEY,        properties.put(IdentifiedObject.NAME_KEY, name));
        assertNull(IdentifiedObject.IDENTIFIERS_KEY, properties.put(IdentifiedObject.IDENTIFIERS_KEY, new SimpleIdentifier(code)));
    }

    /**
     * Returns the code stored in the {@link #properties} map, or {@code null} if none.
     *
     * @return the code declared in properties map.
     */
    final String getCode() {
        final Identifier identifier = (Identifier) properties.get(IdentifiedObject.IDENTIFIERS_KEY);
        return (identifier != null) ? identifier.getCode() : null;
    }

    /**
     * Returns the name stored in the {@link #properties} map, or {@code null} if none.
     *
     * @return the name declared in properties map.
     */
    final String getName() {
        return (String) properties.get(IdentifiedObject.NAME_KEY);
    }

    /**
     * Returns the instance to be tested. When this method is invoked for the first time, it creates the instance
     * to test by invoking a {@code createXXX(String)} method from the user-specified {@link ObjectFactory} with
     * the current {@link #properties} in argument. The created object is then cached and returned in subsequent
     * invocations of this method.
     *
     * @return the instance to test.
     * @throws FactoryException if an error occurred while creating the identified object.
     */
    public abstract T getIdentifiedObject() throws FactoryException;
}
