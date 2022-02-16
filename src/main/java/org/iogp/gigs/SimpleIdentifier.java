/*
 *    GeoAPI - Java interfaces for OGC/ISO standards
 *    http://www.geoapi.org
 *
 *    Copyright (C) 2012-2021 Open Geospatial Consortium, Inc.
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
     */
    @Override
    public Citation getAuthority() {
        return null;
    }

    /**
     * Returns the code given at construction time.
     */
    @Override
    public String getCode() {
        return String.valueOf(code);
    }

    /**
     * Returns the code space, which is fixed to {@code "GIGS"}.
     */
    @Override
    public String getCodeSpace() {
        return "GIGS";
    }

    /**
     * Version identifier for the namespace, as specified by the code authority.
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
