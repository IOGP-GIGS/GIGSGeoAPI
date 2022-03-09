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

import org.opengis.referencing.cs.CSFactory;
import org.opengis.referencing.datum.DatumFactory;
import org.iogp.gigs.internal.geoapi.ValidatorContainer;
import org.iogp.gigs.internal.geoapi.PseudoEpsgFactory;
import org.iogp.gigs.internal.geoapi.Units;


/**
 * Provides data for geodetic objects defined by the EPSG dataset but not present in the GIGS files.
 * This class is used when a test case needs some dependencies, and those dependencies are expected
 * to be defined in the EPSG dataset instead of in a previous test case.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @version 1.0
 * @since   1.0
 */
final class EPSGMock extends PseudoEpsgFactory {
    /**
     * Creates a new EPSG pseudo-factory which will use the given factories for creating coordinate system instances.
     *
     * @param  units         provider of pre-defined {@code Unit} instances.
     * @param  datumFactory  factory for creating {@code Datum} instances.
     * @param  csFactory     factory for creating {@code CoordinateSystem} instances.
     * @param  validators    the set of validators to use for verifying objects conformance.
     */
    EPSGMock(final Units              units,
             final DatumFactory       datumFactory,
             final CSFactory          csFactory,
             final ValidatorContainer validators)
    {
        super(units, datumFactory, csFactory, null, null, null, validators);
    }

    /**
     * Returns the coordinate system factory specified at construction time.
     */
    CSFactory getCSFactory() {
        return csFactory;
    }
}
