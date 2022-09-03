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

import java.util.Set;
import java.util.Collection;
import java.util.Collections;
import org.opengis.util.GenericName;
import org.opengis.util.InternationalString;
import org.opengis.metadata.Identifier;
import org.opengis.metadata.citation.Citation;
import org.opengis.referencing.IdentifiedObject;
import org.opengis.referencing.ReferenceIdentifier;


/**
 * An {@code IdentifiedObject} abstract base class which contain only a {@linkplain #getName() name}.
 * All other {@code IdentifiedObject} attributes are {@code null} or empty collections.
 *
 * <p>Since the {@linkplain #getName() name} is the only identifier contained by this class,
 * {@code SimpleIdentifiedObject} implements directly the {@link Identifier} interface.</p>
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @version 1.0
 * @since   1.0
 */
class SimpleIdentifiedObject implements IdentifiedObject, ReferenceIdentifier {
    /**
     * Alphanumeric value identifying an instance in the authority name space.
     *
     * @see #getCode()
     */
    final String name;

    /**
     * Creates a new object of the given name.
     *
     * @param  name  the name of the new object.
     */
    SimpleIdentifiedObject(final String name) {
        this.name = name;
    }

    /**
     * Returns the name of this identified object, which is represented directly by {@code this}
     * implementation class. This is the only {@link IdentifiedObject} method in this class
     * returning a non-null and non-empty value.
     */
    @Override
    public ReferenceIdentifier getName() {
        return this;
    }

    /**
     * Returns the aliases (there is none).
     */
    @Override
    public Collection<GenericName> getAlias() {
        return Collections.emptySet();
    }

    /**
     * Returns the identifiers (there is none).
     */
    @Override
    public Set<ReferenceIdentifier> getIdentifiers() {
        return Collections.emptySet();
    }

    /**
     * Returns the person or party responsible for maintenance of the namespace.
     */
    @Override
    public Citation getAuthority() {
        return null;
    }

    /**
     * Returns the identifier or namespace in which the code is valid.
     */
    @Override
    public String getCodeSpace() {
        return null;
    }

    /**
     * Returns the name given at construction time.
     */
    @Override
    public String getCode() {
        return name;
    }

    /**
     * Returns the version (there is none).
     */
    @Override
    public String getVersion() {
        return null;
    }

    /**
     * Returns the remarks (there is none).
     */
    @Override
    public InternationalString getRemarks() {
        return null;
    }

    /**
     * Returns a <cite>Well-Known Text</cite> (WKT) for this object. The default implementation
     * throws unconditionally the exception since we do not support WKT formatting.
     */
    @Override
    public String toWKT() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns a string representation of this identified object.
     */
    @Override
    public String toString() {
        return getClass().getSimpleName() + "[\"" + name + "\"]";
    }
}
