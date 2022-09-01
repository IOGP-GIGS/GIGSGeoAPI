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

import javax.measure.Unit;
import org.opengis.referencing.crs.CRSFactory;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.cs.CSFactory;
import org.opengis.referencing.cs.CSAuthorityFactory;
import org.opengis.referencing.datum.Datum;
import org.opengis.referencing.datum.Ellipsoid;
import org.opengis.referencing.datum.PrimeMeridian;
import org.opengis.referencing.datum.DatumFactory;
import org.opengis.referencing.datum.DatumAuthorityFactory;
import org.opengis.referencing.operation.CoordinateOperation;
import org.opengis.referencing.operation.CoordinateOperationFactory;
import org.opengis.referencing.operation.CoordinateOperationAuthorityFactory;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.MathTransformFactory;


/**
 * A container for various factory implementations.
 * This is used as a replacement for long list of arguments in constructors when a test may require
 * many factories for different kinds of objects (datum, coordinate system, operations, <i>etc</i>).
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @version 1.0
 * @since   1.0
 */
public class Factories {
    /**
     * Factory to use for building {@link CoordinateReferenceSystem} instances from authority codes,
     * or {@code null} if none.
     */
    protected CRSAuthorityFactory crsAuthorityFactory;

    /**
     * Factory to use for building {@link CoordinateReferenceSystem} instances, or {@code null} if none.
     */
    protected CRSFactory crsFactory;

    /**
     * The factory to use for creating coordinate system instances from authority codes, or {@code null} if none.
     * May also be used for fetching {@link Unit} instances.
     */
    protected CSAuthorityFactory csAuthorityFactory;

    /**
     * The factory to use for creating coordinate system instances, or {@code null} if none.
     */
    protected CSFactory csFactory;

    /**
     * Factory to use for building {@link Datum} instances from authority codes, or {@code null} if none.
     * May also be used for building {@link Ellipsoid} and {@link PrimeMeridian} components.
     */
    protected DatumAuthorityFactory datumAuthorityFactory;

    /**
     * Factory to use for building {@link Datum} instances, or {@code null} if none.
     * May also be used for building {@link Ellipsoid} and {@link PrimeMeridian} components.
     */
    protected DatumFactory datumFactory;

    /**
     * Factory to use for building {@link CoordinateOperation} instances from authority codes,
     * or {@code null} if none.
     */
    protected CoordinateOperationAuthorityFactory copAuthorityFactory;

    /**
     * Factory to use for building {@link CoordinateOperation} instances, or {@code null} if none.
     */
    protected CoordinateOperationFactory copFactory;

    /**
     * The factory to use for fetching operation methods and building {@link MathTransform} instances,
     * or {@code null} if none.
     */
    protected MathTransformFactory mtFactory;

    /**
     * Creates an initially empty set of factories.
     */
    protected Factories() {
    }
}
