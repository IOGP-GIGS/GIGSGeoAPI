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

import java.util.Collection;
import org.opengis.util.GenericName;
import org.opengis.util.FactoryException;
import org.opengis.referencing.AuthorityFactory;
import org.opengis.referencing.IdentifiedObject;
import org.opengis.referencing.ReferenceIdentifier;
import org.iogp.gigs.internal.geoapi.Configuration;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Base class for tests of EPSG definitions (2000 series).
 * Those tests verify the correctness of geodetic parameters that are delivered with the software.
 * The comparison to be taken as truth is the EPSG Dataset.
 *
 * @param  <T>  the type of objects to test.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @version 1.0
 * @since   1.0
 */
public abstract class Series2000<T> extends IntegrityTest {
    /**
     * The value to give to the {@link #aliases} field for meaning "no alias".
     */
    private static final String[] NONE = new String[0];

    /**
     * The EPSG code of the {@code T} instance to test.
     * This field is set by all test methods before to create and verify the {@code T} instance.
     * This code can be compared to the identifiers returned by {@link IdentifiedObject#getIdentifiers()}.
     */
    public int code;

    /**
     * The name of the {@code T} instance to test, as used in the EPSG dataset.
     * This field is set by all test methods before to create and verify the {@code T} instance.
     * This name will be compared to the value returned by {@link IdentifiedObject#getName()},
     * unless {@link #isStandardNameSupported} is {@code false}.
     */
    public String name;

    /**
     * The expected aliases of the {@code T} instance to test, or an empty array if none.
     * This field is set by all test methods before to create and verify the {@code T} instance.
     * Those aliases will be compared to the values returned by {@link IdentifiedObject#getAlias()},
     * unless {@link #isStandardAliasSupported} is {@code false}.
     */
    public String[] aliases = NONE;

    /**
     * {@code true} if the tested factories support {@linkplain IdentifiedObject#getName() name}.
     * If {@code true} (the default), then the test methods will ensure that the identified objects
     * created by the factories declare the same name than the GIGS tests.
     * If {@code false}, then the names are ignored.
     */
    protected boolean isStandardNameSupported;

    /**
     * {@code true} if the tested factories support {@linkplain IdentifiedObject#getAlias() aliases}.
     * If {@code true} (the default), then the test methods will ensure that the identified objects
     * created by the factories declare at least all the aliases enumerated in the GIGS tests -
     * additional aliases, if any, are ignored. If {@code false}, then the aliases are ignored.
     */
    protected boolean isStandardAliasSupported;

    /**
     * {@code true} if the {@link IdentifiedObject} instances created indirectly by the factories
     * are expected to have correct identification information.
     * For example when testing a {@link org.opengis.referencing.crs.CoordinateReferenceSystem} (CRS) object,
     * the CRS authority code will be verified unconditionally but the authority codes of associated objects
     * ({@link org.opengis.referencing.datum.GeodeticDatum} or {@link org.opengis.referencing.cs.CoordinateSystem})
     * will be verified only if this flag is {@code true}.
     */
    protected boolean isDependencyIdentificationSupported;

    /**
     * {@code true} if the factory support creation of deprecated objects.
     */
    protected boolean isDeprecatedObjectCreationSupported;

    /**
     * {@code true} if the tested object is particularly important to E&amp;P industry.
     * This field is set at the beginning of test methods.
     */
    boolean important;

    /**
     * Creates a new test.
     */
    Series2000() {
        @SuppressWarnings("unchecked")
        final boolean[] isEnabled = getEnabledFlags(
                Configuration.Key.isStandardNameSupported,
                Configuration.Key.isStandardAliasSupported,
                Configuration.Key.isDependencyIdentificationSupported,
                Configuration.Key.isDeprecatedObjectCreationSupported);
        isStandardNameSupported             = isEnabled[0];
        isStandardAliasSupported            = isEnabled[1];
        isDependencyIdentificationSupported = isEnabled[2];
        isDeprecatedObjectCreationSupported = isEnabled[3];
    }

    /**
     * Modifies the configuration for testing a dependency of the object tested by the given test.
     * This is used for testing for example the ellipsoid in a datum.
     *
     * @param  parent  the test from which to inherit the configuration.
     */
    final void configureAsDependency(final Series2000 parent) {
        isDeprecatedObjectCreationSupported &= parent.isDeprecatedObjectCreationSupported;
        isDependencyIdentificationSupported &= parent.isDependencyIdentificationSupported;
        isStandardNameSupported             &= parent.isStandardNameSupported  & isDependencyIdentificationSupported;
        isStandardAliasSupported            &= parent.isStandardAliasSupported & isDependencyIdentificationSupported;
    }

    /**
     * Returns information about the configuration of the test which has been run.
     * This method returns a map containing:
     *
     * <ul>
     *   <li>All the following values associated to the {@link org.opengis.test.Configuration.Key} of the same name:
     *     <ul>
     *       <li>{@link #isStandardNameSupported}</li>
     *       <li>{@link #isStandardAliasSupported}</li>
     *       <li>{@link #isDependencyIdentificationSupported}</li>
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
        assertNull(op.put(Configuration.Key.isStandardNameSupported,             isStandardNameSupported));
        assertNull(op.put(Configuration.Key.isStandardAliasSupported,            isStandardAliasSupported));
        assertNull(op.put(Configuration.Key.isDependencyIdentificationSupported, isDependencyIdentificationSupported));
        assertNull(op.put(Configuration.Key.isDeprecatedObjectCreationSupported, isDeprecatedObjectCreationSupported));
        return op;
    }

    /**
     * Returns the instance to be tested. When this method is invoked for the first time,
     * it creates the instance to test by invoking a {@code createXXX(String)} method on the
     * user-specified {@link AuthorityFactory} with the current {@link #code} value in argument.
     * The created object is then cached and returned in subsequent invocations of this method.
     *
     * <p>Usually, each test method creates exactly one object. But a few (relatively rare) tests may create
     * more than one object. In such case, the instance returned by this method may vary.</p>
     *
     * @return the instance to test.
     * @throws FactoryException if an error occurred while creating the identified object.
     */
    public abstract T getIdentifiedObject() throws FactoryException;

    /**
     * Returns a name of the given object that can be compared against the expected name.
     * The default implementation returns {@code object.getName().getCode()} or {@code null}
     * if the given object, its name or its code is null.
     *
     * <p>Subclasses can override this method when testing an {@link AuthorityFactory} implementation
     * which is known to use slightly different name than the one used in the EPSG database, or if the
     * implementation stores the EPSG name as an {@linkplain IdentifiedObject#getAlias() alias} instead
     * of as the {@linkplain IdentifiedObject#getName() primary name}.</p>
     *
     * <div class="note"><b>Example:</b> if an implementation replaces all spaces by underscores,
     * then a subclass testing that implementation could override this method as below:
     *
     * <pre> &#64;Override
     * protected String getVerifiableName(IdentifiedObject object) {
     *    return super.getVerifiableName().replace(' ', '_');
     * }</pre></div>
     *
     * Note that if the object names are too different for being compared, then subclasses can also
     * disable name comparisons by setting {@link #isStandardNameSupported} to {@code false}.
     *
     * @param  object  the object from which to get a name than can be verified against the expected name.
     * @return the name of the given object, eventually modified in order to match the expected name.
     *
     * @see #isStandardNameSupported
     * @see #isStandardAliasSupported
     */
    protected String getVerifiableName(final IdentifiedObject object) {
        return getName(object);
    }

    /**
     * Compares the given generic names with the given set of expected aliases.
     * This method verifies that the given collection contains at least the expected aliases.
     * However the collection may contain additional aliases, which will be ignored.
     *
     * @param  message   the prefix of the message to show in case of failure.
     * @param  expected  the expected aliases.
     * @param  aliases   the actual aliases.
     */
    static void assertContainsAll(final String message, final String[] expected,
            final Collection<GenericName> aliases)
    {
        assertNotNull(aliases, message);
next:   for (final String search : expected) {
            for (final GenericName alias : aliases) {
                final String tip = alias.tip().toString();
                if (search.equalsIgnoreCase(tip)) {
                    continue next;
                }
            }
            fail(message + ": alias not found: " + search);
        }
    }

    /**
     * Ensures that the given collection contains at least one identifier having the given
     * codespace (ignoring case) and the given code value.
     *
     * @param  message      the message to show in case of failure.
     * @param  codespace    the code space of identifiers to search.
     * @param  expected     the expected identifier code.
     * @param  identifiers  the actual identifiers.
     */
    static void assertContainsCode(final String message, final String codespace, final int expected,
            final Collection<? extends ReferenceIdentifier> identifiers)
    {
        assertNotNull(identifiers, message);
        int found = 0;
        for (final ReferenceIdentifier id : identifiers) {
            if (codespace.equalsIgnoreCase(id.getCodeSpace().trim())) {
                found++;
                try {
                    assertEquals(expected, Integer.parseInt(id.getCode()), message);
                } catch (NumberFormatException e) {
                    fail(message + ".getCode(): expected " + expected +
                            " but got a non-numerical value: " + e);
                }
            }
        }
        assertEquals(1, found, () -> message + ": occurrence of " + codespace + ':' + expected);
    }
}
