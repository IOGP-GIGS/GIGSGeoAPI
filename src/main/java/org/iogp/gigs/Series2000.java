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

import java.text.Normalizer;
import java.util.Collection;
import org.opengis.util.GenericName;
import org.opengis.util.FactoryException;
import org.opengis.referencing.AuthorityFactory;
import org.opengis.referencing.IdentifiedObject;
import org.opengis.referencing.ReferenceIdentifier;
import org.opengis.referencing.operation.SingleOperation;
import org.iogp.gigs.internal.geoapi.Configuration;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Base class for tests of EPSG definitions (2000 series).
 * Those tests verify the correctness of geodetic parameters that are delivered with the software.
 * The geodetic objects are created using one or many {@link AuthorityFactory} sub-types.
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
     * {@code true} if the tested factories support {@linkplain IdentifiedObject#getIdentifiers() identifiers}.
     * If {@code true} (the default), then the test methods will ensure that the identified objects created by
     * the factories declare the authority code used for fetching the object.
     * If {@code false}, then the identifiers are ignored.
     */
    protected boolean isStandardIdentifierSupported;

    /**
     * {@code true} if the tested factories support standard {@linkplain IdentifiedObject#getName() names}.
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
     * {@code true} if the tested factories support {@linkplain SingleOperation#getOperationVersion()
     * operation version}. If {@code true} (the default), then the test methods will ensure that the
     * operations created by the factories declare the expected operation version.
     * If {@code false}, then the operation version is ignored.
     *
     * <p>This flag is significant only for tests on coordinate transformations.</p>
     */
    protected boolean isOperationVersionSupported;

    /**
     * Creates a new test.
     */
    Series2000() {
        @SuppressWarnings("unchecked")
        final boolean[] isEnabled = getEnabledFlags(
                Configuration.Key.isStandardIdentifierSupported,
                Configuration.Key.isStandardNameSupported,
                Configuration.Key.isStandardAliasSupported,
                Configuration.Key.isDependencyIdentificationSupported,
                Configuration.Key.isDeprecatedObjectCreationSupported,
                Configuration.Key.isOperationVersionSupported);
        isStandardIdentifierSupported       = isEnabled[0];
        isStandardNameSupported             = isEnabled[1];
        isStandardAliasSupported            = isEnabled[2];
        isDependencyIdentificationSupported = isEnabled[3];
        isDeprecatedObjectCreationSupported = isEnabled[4];
        isOperationVersionSupported         = isEnabled[5];
    }

    /**
     * Modifies the configuration for testing a dependency of the object tested by the given test.
     * This is used for testing for example the ellipsoid in a datum.
     *
     * @param  parent  the test from which to inherit the configuration.
     */
    final void configureAsDependency(final Series2000<?> parent) {
        isDeprecatedObjectCreationSupported &= parent.isDeprecatedObjectCreationSupported;
        isDependencyIdentificationSupported &= parent.isDependencyIdentificationSupported;
        isStandardIdentifierSupported       &= parent.isStandardIdentifierSupported & isDependencyIdentificationSupported;
        isStandardNameSupported              = false;   // Leave that test to dedicated test classes.
        isStandardAliasSupported             = false;
    }

    /**
     * Returns information about the configuration of the test which has been run.
     * This method returns a map containing:
     *
     * <ul>
     *   <li>All the following values associated to the {@link Configuration.Key} of the same name:
     *     <ul>
     *       <li>{@link #isStandardIdentifierSupported}</li>
     *       <li>{@link #isStandardNameSupported}</li>
     *       <li>{@link #isStandardAliasSupported}</li>
     *       <li>{@link #isDependencyIdentificationSupported}</li>
     *       <li>{@link #isDeprecatedObjectCreationSupported}</li>
     *       <li>{@link #isOperationVersionSupported} (only in transformation tests)</li>
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
        assertNull(op.put(Configuration.Key.isStandardIdentifierSupported,       isStandardIdentifierSupported));
        assertNull(op.put(Configuration.Key.isStandardNameSupported,             isStandardNameSupported));
        assertNull(op.put(Configuration.Key.isStandardAliasSupported,            isStandardAliasSupported));
        assertNull(op.put(Configuration.Key.isDependencyIdentificationSupported, isDependencyIdentificationSupported));
        assertNull(op.put(Configuration.Key.isDeprecatedObjectCreationSupported, isDeprecatedObjectCreationSupported));
        // isOperationVersionSupported not added here. Should be added only by subclasses that use it.
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
     * The current implementation returns {@code object.getName().getCode()} converted to
     * ASCII characters on a best effort basis (e.g. "é" is replaced by "e"),
     * or {@code null} if the given object, its name or its code is null.
     *
     * <p>Subclasses can override this method when testing an {@link AuthorityFactory} implementation
     * which is known to use slightly different name than the one used in the EPSG database, or if the
     * implementation stores the EPSG name as an {@linkplain IdentifiedObject#getAlias() alias} instead
     * of as the {@linkplain IdentifiedObject#getName() primary name}.</p>
     *
     * <div class="note"><b>Example:</b> if an implementation replaces all spaces by underscores,
     * then a subclass testing that implementation could override this method as below:
     *
     * {@snippet lang="java" :
     *     @Override
     *     protected String getVerifiableName(IdentifiedObject object) {
     *         return super.getVerifiableName().replace(' ', '_');
     *     }
     * }
     * </div>
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
        return toASCII(getName(object));
    }

    /**
     * Returns {@code true} if the given text contains only ASCII character.
     * This quick check allows us to avoid the costly {@link #toASCII(String)}
     * conversion in the common case where the text is already in ASCII.
     *
     * @param  text  the text to verify.
     * @return {@code true} if the given text contains only ASCII characters.
     */
    private static boolean isASCII(final String text) {
        final int length = text.length();
        for (int i=0; i<length; i++) {
            if (text.charAt(i) >= 128) {
                return false;
            }
        }
        return true;
    }

    /**
     * Replaces some Unicode characters by ASCII characters on a "best effort basis".
     * For example the “ é ” character is replaced by  “ e ” (without accent),
     * the  “ ″ ” symbol for minutes of angle is replaced by straight double quotes “ " ”,
     * and combined characters like ㎏, ㎎, ㎝, ㎞, ㎢, ㎦, ㎖, ㎧, ㎩, ㎐, <i>etc.</i> are replaced
     * by the corresponding sequences of characters.
     *
     * @param  buffer  the text to scan for Unicode characters to replace by ASCII characters, or {@code null}.
     * @return the text with only ASCII characters (best effort), or {@code null} if the given text was null.
     */
    static String toASCII(final String text) {
        if (text == null || isASCII(text)) {
            return text;
        }
        final StringBuilder buffer = new StringBuilder(Normalizer.normalize(text, Normalizer.Form.NFD));
        int i = buffer.length();
        while (i > 0) {
            final int c = Character.codePointBefore(buffer, i);
            final int n = Character.charCount(c);
            i -= n;                                 // After this line, `i` is the index of character `c`.
            final char r;                           // The character replacement.
            switch (Character.getType(c)) {
                case Character.FORMAT:
                case Character.CONTROL:                   // Character.isIdentifierIgnorable
                case Character.NON_SPACING_MARK:          buffer.delete(i, i + n); continue;
                case Character.PARAGRAPH_SEPARATOR:       // Fall through
                case Character.LINE_SEPARATOR:            r = '\n'; break;
                case Character.SPACE_SEPARATOR:           r = ' '; break;
                case Character.INITIAL_QUOTE_PUNCTUATION: r = (c == '‘') ? '\'' : '"'; break;
                case Character.FINAL_QUOTE_PUNCTUATION:   r = (c == '’') ? '\'' : '"'; break;
                case Character.OTHER_PUNCTUATION:
                case Character.MATH_SYMBOL: {
                    switch (c) {
                        case '⋅': r = '*';  break;
                        case '∕': r = '/';  break;
                        case '′': r = '\''; break;
                        case '″': r = '"';  break;
                        default:  continue;
                    }
                    break;
                }
                default: continue;
            }
            if (n == 2) {
                buffer.deleteCharAt(i + 1);         // Remove the low surrogate of a surrogate pair.
            }
            // Nothing special to do about codepoint here, because `r` is in the basic plane
            buffer.setCharAt(i, r);
        }
        return buffer.toString();
    }

    /**
     * Ensures that the given collection contains exactly one identifier in the EPSG code space
     * and that identifier has the given code value (ignoring case).
     * Identifiers in other code spaces are ignored.
     *
     * @param  expected  the expected identifier code (usually {@link #code}).
     * @param  object    the object from which to test identifiers.
     * @param  message   the Java expression used for getting the object. This is shown if the assertion fails.
     */
    final void assertIdentifierEquals(final int expected, final IdentifiedObject object, final String message) {
        final Collection<? extends ReferenceIdentifier> identifiers = object.getIdentifiers();
        assertNotNull(identifiers, () -> message + ".getIdentifiers()");
        if (isStandardIdentifierSupported) {
            final Configuration.Key<Boolean> previous = configurationTip;
            if (previous != Configuration.Key.isDependencyIdentificationSupported) {
                configurationTip = Configuration.Key.isStandardIdentifierSupported;
            }
            int found = 0;
            for (final ReferenceIdentifier id : identifiers) {
                if (EPSG.equalsIgnoreCase(id.getCodeSpace().trim())) {
                    found++;
                    try {
                        assertEquals(expected, Integer.parseInt(id.getCode()), message);
                    } catch (NumberFormatException e) {
                        fail(message + ".getIdentifiers().get(…).getCode(): expected " + expected +
                                " but got a non-numerical value: " + e);
                    }
                }
            }
            assertEquals(1, found, () -> message + ".getIdentifiers(*): occurrence of " + EPSG + ':' + expected);
            configurationTip = previous;
        }
    }

    /**
     * Ensures that the name of the given object is equal to the value of the expected name.
     * The comparison ignores case and non-spacing marks (e.g. "é" is compared as "e").
     * The way non-spacing marks are ignored can be modified if user overrides {@link #getVerifiableName(IdentifiedObject)}.
     *
     * @param  full      {@code true} for a full match, or {@code false} if the name only needs to start with expected value.
     * @param  expected  the expected name (usually {@link #name}).
     * @param  object    the object for which to verify the name.
     * @param  message   the Java expression used for getting the object. This is shown if the assertion fails.
     */
    final void assertNameEquals(final boolean full, final String expected, final IdentifiedObject object, final String message) {
        if (isStandardNameSupported) {
            final Configuration.Key<Boolean> previous = configurationTip;
            if (previous != Configuration.Key.isDependencyIdentificationSupported) {
                configurationTip = Configuration.Key.isStandardNameSupported;
            }
            final String actual = getVerifiableName(object);
            int length = expected.length();
            if (full) length = StrictMath.max(length, actual.length());
            if (actual == null || !actual.regionMatches(true, 0, expected, 0, length)) {
                assertEquals(expected, getName(object), message + ".getName()");
            }
            configurationTip = previous;
        }
    }

    /**
     * Ensures that the aliases of the given object contains all expected aliases.
     * This method verifies that the given collection contains at least the expected aliases.
     * However the collection may contain additional aliases, which will be ignored.
     *
     * @param  expected  the expected aliases (usually {@link #aliases}).
     * @param  object    the object for which to verify the aliases.
     * @param  message   the Java expression used for getting the object. This is shown if the assertion fails.
     */
    final void assertAliasesEqual(final String[] expected, final IdentifiedObject object, final String message) {
        if (isStandardAliasSupported) {
            final Configuration.Key<Boolean> previous = configurationTip;
            if (previous != Configuration.Key.isDependencyIdentificationSupported) {
                configurationTip = Configuration.Key.isStandardAliasSupported;
            }
            final Collection<GenericName> actual = object.getAlias();
            assertNotNull(actual, () -> message + ".getAlias()");
next:       for (final String search : expected) {
                for (final GenericName alias : actual) {
                    final String tip = alias.tip().toString();
                    if (search.equalsIgnoreCase(toASCII(tip))) {
                        continue next;
                    }
                }
                fail(message + ".getAlias(): alias not found: " + search);
            }
            configurationTip = previous;
        }
    }
}
