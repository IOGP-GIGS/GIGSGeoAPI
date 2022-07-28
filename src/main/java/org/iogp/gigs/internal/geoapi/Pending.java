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

import org.opengis.referencing.operation.CoordinateOperationFactory;
import org.opengis.referencing.operation.MathTransformFactory;
import org.opengis.referencing.operation.OperationMethod;
import org.opengis.referencing.operation.SingleOperation;
import org.opengis.util.NoSuchIdentifierException;


/**
 * Placeholder for methods that are missing in GeoAPI 3.0 but may be added in GeoAPI 3.1.
 * This class centralizes in a single place the methods that could be in GeoAPI,
 * but that we have to simulate in the meantime.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @version 1.0
 * @since   1.0
 */
public final class Pending {
    /**
     * Do not allow instantiation of this class.
     */
    private Pending() {
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
}
