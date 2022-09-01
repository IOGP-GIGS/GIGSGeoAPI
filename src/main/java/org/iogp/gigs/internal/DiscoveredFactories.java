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
package org.iogp.gigs.internal;

import java.util.ServiceLoader;
import org.opengis.util.Factory;
import org.opengis.util.InternationalString;
import org.opengis.metadata.citation.Citation;
import org.opengis.referencing.AuthorityFactory;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.CRSFactory;
import org.opengis.referencing.cs.CSAuthorityFactory;
import org.opengis.referencing.cs.CSFactory;
import org.opengis.referencing.datum.DatumAuthorityFactory;
import org.opengis.referencing.datum.DatumFactory;
import org.opengis.referencing.operation.CoordinateOperationAuthorityFactory;
import org.opengis.referencing.operation.CoordinateOperationFactory;
import org.opengis.referencing.operation.MathTransformFactory;
import org.iogp.gigs.Factories;


/**
 * Factories that are automatically discovered using {@link ServiceLoader}.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @version 1.0
 * @since   1.0
 */
final class DiscoveredFactories extends Factories {
    /**
     * Creates an initially empty set of factories.
     */
    DiscoveredFactories() {
    }

    /**
     * Restores this object to its initial state.
     */
    final void clear() {
        crsAuthorityFactory   = null;
        crsFactory            = null;
        csAuthorityFactory    = null;
        csFactory             = null;
        datumAuthorityFactory = null;
        datumFactory          = null;
        copAuthorityFactory   = null;
        copFactory            = null;
        mtFactory             = null;
    }

    /**
     * Initializes this object to all discovered factories.
     *
     * @param  loader  the loader to use for discovering factories.
     */
    final void initialize(final ClassLoader loader) {
        crsAuthorityFactory   = find(loader, CRSAuthorityFactory.class);
        crsFactory            = find(loader, CRSFactory.class);
        csAuthorityFactory    = find(loader, CSAuthorityFactory.class);
        csFactory             = find(loader, CSFactory.class);
        datumAuthorityFactory = find(loader, DatumAuthorityFactory.class);
        datumFactory          = find(loader, DatumFactory.class);
        copAuthorityFactory   = find(loader, CoordinateOperationAuthorityFactory.class);
        copFactory            = find(loader, CoordinateOperationFactory.class);
        mtFactory             = find(loader, MathTransformFactory.class);
    }

    /**
     * Returns the factory to test for the specified type.
     *
     * @param  type  GeoAPI interface of the desired factory.
     * @return factory for the specified interface, or {@code null} if none.
     */
    final Object get(final Class<?> type) {
        if (type == Factories.class)                           return this;
        if (type == CRSAuthorityFactory.class)                 return crsAuthorityFactory;
        if (type == CRSFactory.class)                          return crsFactory;
        if (type == CSAuthorityFactory.class)                  return csAuthorityFactory;
        if (type == CSFactory.class)                           return csFactory;
        if (type == DatumAuthorityFactory.class)               return datumAuthorityFactory;
        if (type == DatumFactory.class)                        return datumFactory;
        if (type == CoordinateOperationAuthorityFactory.class) return copAuthorityFactory;
        if (type == CoordinateOperationFactory.class)          return copFactory;
        if (type == MathTransformFactory.class)                return mtFactory;
        return null;
    }

    /**
     * Finds the first factory of the specified type.
     * If an authority factory is for a name space other than EPSG, skips that factory.
     *
     * @param  loader  the loader to use for discovering factories.
     * @param  type    GeoAPI interface of the desired factory.
     * @return factory for the specified interface, or {@code null} if none.
     */
    private static <T extends Factory> T find(final ClassLoader loader, final Class<T> type) {
        for (final Factory factory : ServiceLoader.load(type, loader)) {
            if (factory instanceof AuthorityFactory) {
                if (isNotEPSG(((AuthorityFactory) factory).getAuthority())) {
                    continue;
                }
            }
            return type.cast(factory);
        }
        return null;
    }

    /**
     * Tests whether the given citation is for an authority other than EPSG.
     * If not specified, conservatively assumes {@code false}.
     *
     * @param  citation  the citation to test.
     * @return whether the given citation is for EPSG authority.
     */
    private static boolean isNotEPSG(final Citation citation) {
        if (citation == null || isEPSG(citation.getTitle())) {
            return false;
        }
        boolean hasOtherTitle = false;
        for (final InternationalString title : citation.getAlternateTitles()) {
            if (isEPSG(title)) return false;
            hasOtherTitle = true;
        }
        return hasOtherTitle;
    }

    /**
     * Returns {@code true} if the given title is "EPSG".
     *
     * @param  title  the title to check, or {@code null}.
     * @return {@code true} if the given title is "EPSG".
     */
    private static boolean isEPSG(final InternationalString title) {
        return (title != null) && title.toString().contains("EPSG");
    }
}
