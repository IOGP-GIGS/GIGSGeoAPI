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
package org.iogp.gigs.internal.geoapi;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.iogp.gigs.internal.sis.DefaultTransformationFactory;
import org.opengis.util.FactoryException;
import org.opengis.util.NoSuchIdentifierException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.CoordinateOperationFactory;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.MathTransformFactory;
import org.opengis.referencing.operation.OperationMethod;
import org.opengis.referencing.operation.SingleOperation;
import org.opengis.referencing.operation.Transformation;

import static org.junit.jupiter.api.Assumptions.assumeTrue;


/**
 * Placeholder for methods that are missing in GeoAPI 3.0 but may be added in GeoAPI 3.1.
 * This class centralizes in a single place the methods that could be in GeoAPI,
 * but that we have to simulate in the meantime.
 *
 * <p>Implementation-specific subclasses may exist.</p>
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @author  Michael Arneson (INT)
 * @version 1.0
 * @since   1.0
 */
public class Pending {
    /**
     * The (usually unique) instance, created when first needed.
     */
    private static Pending instance;

    /**
     * The class loader of the library for which to provide access to implementation-specific API.
     * This is the {@code loader} argument given to the constructor.
     */
    private final ClassLoader loader;

    /**
     * Creates implementation-specific accessor to pending methods.
     *
     * @param  loader  class loader of the library for which to provide access to implementation-specific API.
     */
    public Pending(final ClassLoader loader) {
        this.loader = loader;
    }

    /**
     * Returns an accessor to pending API for the given factory.
     *
     * @param  factory  the factory for which to get an accessor to pending API.
     * @return accessor to pending API for the given factory.
     */
    public static synchronized Pending getInstance(final CoordinateOperationFactory factory) {
        final Class<? extends CoordinateOperationFactory> c = factory.getClass();
        final ClassLoader loader = c.getClassLoader();
        if (instance == null || instance.loader != loader) {
            final String pn = c.getPackageName();
            try {
                if (pn.startsWith(DefaultTransformationFactory.PACKAGE_PREFIX)) {
                    instance = new DefaultTransformationFactory(loader);
                } else {
                    instance = new Pending(loader);
                }
            } catch (ReflectiveOperationException e) {
                instance = new Pending(loader);
                Logger.getLogger("org.opengis.referencing.operation")
                      .log(Level.WARNING, "Can not find the implementation-specific API.", e);
            }
        }
        return instance;
    }

    /**
     * Returns the operation method of the given name. This is a temporary method until
     * {@code getOperationMethod(…)} is added directly in the {@link CoordinateOperationFactory} interface.
     *
     * @param  mtFactory   the factory to use for fetching operation methods.
     * @param  methodName  name of the operation method to fetch.
     * @return operation method for the given name.
     * @throws NoSuchIdentifierException if the given name is not recognized.
     */
    public static OperationMethod getOperationMethod(final MathTransformFactory mtFactory, final String methodName)
            throws NoSuchIdentifierException
    {
        for (final OperationMethod candidate : mtFactory.getAvailableMethods(SingleOperation.class)) {
            if (methodName.equalsIgnoreCase(candidate.getName().getCode())) {
                return candidate;
            }
        }
        throw new NoSuchIdentifierException("Operation method \"" + methodName + "\" not found.", methodName);
    }

    /**
     * Creates a coordinate transformation from the given properties.
     * This is a temporary method until a similar method is added in
     * the {@link CoordinateOperationFactory} interface.
     *
     * @param  properties  the properties to be given to the identified object.
     * @param  sourceCRS   the source CRS.
     * @param  targetCRS   the target CRS.
     * @param  method      the coordinate operation method (mandatory in all cases).
     * @param  transform   transform from positions in the source CRS to positions in the target CRS.
     * @return a transformation from {@code sourceCRS} to {@code targetCRS} using the given transform implementation.
     * @throws FactoryException if the operation creation failed.
     */
    public Transformation createTransformation(Map<String,Object> properties,
                                               CoordinateReferenceSystem sourceCRS,
                                               CoordinateReferenceSystem targetCRS,
                                               OperationMethod method,
                                               MathTransform transform) throws FactoryException
    {
        assumeTrue(false, "Factory method is not available.");
        throw new FactoryException("Factory method is not available.");
    }
}
