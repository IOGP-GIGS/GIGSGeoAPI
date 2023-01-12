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
import java.util.Optional;
import java.util.function.Function;
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
 * <p>Implementations can create a {@code Factories} subclass and initialize all fields in their constructor.</p>
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
     * Maps each property type to an accessor for getting the property value.
     */
    private static final Map<Class<?>, Function<Factories,?>> ACCESSORS = Map.ofEntries(
            entry(Factories.class,                           Function.identity()),
            entry(CRSAuthorityFactory.class,                 (f) -> f.crsAuthorityFactory),
            entry(CRSFactory.class,                          (f) -> f.crsFactory),
            entry(CSAuthorityFactory.class,                  (f) -> f.csAuthorityFactory),
            entry(CSFactory.class,                           (f) -> f.csFactory),
            entry(DatumAuthorityFactory.class,               (f) -> f.datumAuthorityFactory),
            entry(DatumFactory.class,                        (f) -> f.datumFactory),
            entry(CoordinateOperationAuthorityFactory.class, (f) -> f.copAuthorityFactory),
            entry(CoordinateOperationFactory.class,          (f) -> f.copFactory),
            entry(MathTransformFactory.class,                (f) -> f.mtFactory));

    /**
     * Creates an entry for populating the {@link #ACCESSORS} map.
     * This method delegates to {@code Map.entry(…)} with only a type safety added in method signature.
     *
     * @param  <T>       compile-type value of {@code type}.
     * @param  type      key of the map entry to create.
     * @param  accessor  value of the map entry to create.
     * @return entry to put in the {@link #ACCESSORS} map.
     */
    private static <T> Map.Entry<Class<?>, Function<Factories,?>> entry(final Class<T> type, final Function<Factories,T> accessor) {
        return Map.entry(type, accessor);
    }

    /**
     * Creates an initially empty set of factories.
     */
    protected Factories() {
    }

    /**
     * Returns the factory to test for the specified type.
     * This method returns the value of one of the type defined in this class.
     * The field is identified by the value type.
     * For example {@code get(CRSFactory.class)} returns {@link #crsFactory}.
     *
     * @param  <T>   compile-time value of the {@code type} argument.
     * @param  type  GeoAPI interface of the desired factory.
     * @return factory for the specified interface.
     */
    public final <T> Optional<T> get(final Class<T> type) {
        final Function<Factories, ?> accessor = ACCESSORS.get(type);
        if (accessor != null) {
            return Optional.ofNullable(type.cast(accessor.apply(this)));
        }
        return Optional.empty();
    }

    /**
     * Returns {@code true} if the given type is supported by the {@code get(…)} method.
     * Note that a {@code true} value does not guarantee that {@code get(type)} will return
     * a non-empty value, because the type may be supported with no value has been specified.
     *
     * @param  type  GeoAPI interface of a factory.
     * @return whether the given type is supported.
     */
    public static boolean isSupported(final Class<?> type) {
        return ACCESSORS.containsKey(type);
    }
}
