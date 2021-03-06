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
package org.iogp.gigs;

import java.util.Map;
import java.util.HashMap;
import org.opengis.metadata.Identifier;
import org.opengis.util.FactoryException;
import org.opengis.referencing.ObjectFactory;
import org.opengis.referencing.IdentifiedObject;
import org.iogp.gigs.internal.geoapi.Configuration;

import static org.junit.jupiter.api.Assertions.*;


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
public abstract class Series3000<T> extends IntegrityTest {
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
     * Returns information about the configuration of the test which has been run.
     * This method returns a map containing:
     *
     * <ul>
     *   <li>All the following values associated to the {@link org.opengis.test.Configuration.Key} of the same name:
     *     <ul>
     *       <li>{@link #isFactoryPreservingUserValues}</li>
     *       <li>The factories used by the test (provided by subclasses)</li>
     *     </ul>
     *   </li>
     * </ul>
     *
     * @return the configuration of the test being run.
     */
    @Override
    Configuration configuration() {
        final Configuration op = super.configuration();
        assertNull(op.put(Configuration.Key.isFactoryPreservingUserValues, isFactoryPreservingUserValues));
        return op;
    }

    /**
     * Copies the configuration from the given test class. This method is invoked when a test depends on other tests,
     * in which case the tests need to be run with the same configuration in order to get data.
     *
     * @param  sous  the test class from which to copy the configuration.
     */
    final void copyConfigurationFrom(final Series3000<?> source) {
        isFactoryPreservingUserValues = source.isFactoryPreservingUserValues;
        skipIdentificationCheck |= source.skipIdentificationCheck;
        skipTests = false;
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
        assertNull(properties.put(IdentifiedObject.NAME_KEY, name), IdentifiedObject.NAME_KEY);
        assertNull(properties.put(IdentifiedObject.IDENTIFIERS_KEY, new SimpleIdentifier(code)), IdentifiedObject.IDENTIFIERS_KEY);
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
