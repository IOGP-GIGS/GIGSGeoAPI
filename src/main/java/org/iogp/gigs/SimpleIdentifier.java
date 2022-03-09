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

import org.opengis.metadata.Identifier;
import org.opengis.metadata.citation.Citation;
import org.opengis.referencing.ReferenceIdentifier;


/**
 * A simple implementation of {@link Identifier}, used for GIGS testing purpose only.
 *
 * @author  Alexis Manin (Geomatys)
 * @author  Martin Desruisseaux (Geomatys)
 * @version 1.0
 * @since   1.0
 */
final class SimpleIdentifier implements ReferenceIdentifier {
    /**
     * The "code" part of the identifier, as a numerical value.
     * This is the value to be returned by {@link #getCode()}.
     */
    private final int code;

    /**
     * Creates a new identifier for the "GIGS" namespace and the given code.
     *
     * @param  code  the "code" part of the identifier, as a numerical value.
     */
    SimpleIdentifier(final int code) {
        this.code = code;
    }

    /**
     * Person or party responsible for maintenance of the namespace.
     *
     * @return {@code null} in current implementation.
     */
    @Override
    public Citation getAuthority() {
        return null;
    }

    /**
     * Returns the code given at construction time.
     *
     * @return string representation of the code given at construction time.
     */
    @Override
    public String getCode() {
        return String.valueOf(code);
    }

    /**
     * Returns the code space, which is fixed to {@code "GIGS"}.
     *
     * @return {@code "GIGS"}.
     */
    @Override
    public String getCodeSpace() {
        return "GIGS";
    }

    /**
     * Version identifier for the namespace, as specified by the code authority.
     *
     * @return {@code null} in current implementation.
     */
    @Override
    public String getVersion() {
        return null;
    }

    /**
     * Returns a string representation of this identifier.
     */
    @Override
    public String toString() {
        return "GIGS:" + code;
    }

    /**
     * Returns an arbitrary hash code value for this identifier.
     * Current implementation does not use the codespace, since
     * tested EPSG and GIGS codes do not overlap.
     */
    @Override
    public int hashCode() {
        return code ^ 237674218;
    }

    /**
     * Compares this identifier with the given object for equality.
     */
    @Override
    public boolean equals(final Object obj) {
        return (obj instanceof SimpleIdentifier) && code == ((SimpleIdentifier) obj).code;
    }
}
