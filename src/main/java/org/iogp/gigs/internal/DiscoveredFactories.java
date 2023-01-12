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
     * The name of the desired authority for authority factories. This is usually "EPSG".
     *
     * @todo Value is fixed for GIGS tests, but may become configurable in a future version.
     */
    private static final String AUTHORITY = "EPSG";

    /**
     * Creates a set of factories discovered using the specified service loader.
     */
    DiscoveredFactories(final ClassLoader loader) {
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
     * Finds the first factory of the specified type.
     * Authority factories are filtered in order to find an instance for the desired {@linkplain #AUTHORITY}.
     *
     * @param  <T>     compile-time value of the {@code type} argument.
     * @param  loader  the loader to use for discovering factories.
     * @param  type    GeoAPI interface of the desired factory.
     * @return factory for the specified interface, or {@code null} if none.
     */
    private static <T extends Factory> T find(final ClassLoader loader, final Class<T> type) {
        for (final Factory factory : ServiceLoader.load(type, loader)) {
            if (factory instanceof AuthorityFactory) {
                if (!useAuthority(((AuthorityFactory) factory).getAuthority())) {
                    continue;
                }
            }
            return type.cast(factory);
        }
        return null;
    }

    /**
     * Returns {@code true} if this test suite can use a factory for the specified authority.
     * If the authority is not specified, this method conservatively assumes {@code true}.
     *
     * @param  citation  citation of the authority of a factory. May be {@code null}.
     * @return whether the given citation is for the desired authority.
     */
    private static boolean useAuthority(final Citation citation) {
        if (citation == null || useAuthority(citation.getTitle())) {
            return true;
        }
        boolean hasNoOtherTitle = true;
        for (final InternationalString title : citation.getAlternateTitles()) {
            if (useAuthority(title)) return true;
            hasNoOtherTitle = false;
        }
        return hasNoOtherTitle;
    }

    /**
     * Returns {@code true} if the given title is the name of the desired authority.
     *
     * @param  title  the title to check, or {@code null}.
     * @return {@code true} if the given title is for the desired authority.
     */
    private static boolean useAuthority(final InternationalString title) {
        return (title != null) && title.toString().contains(AUTHORITY);
    }
}
