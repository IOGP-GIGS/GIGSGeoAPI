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
package org.iogp.gigs.internal.sis;

import java.util.Map;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.opengis.util.FactoryException;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.OperationMethod;
import org.opengis.referencing.operation.Transformation;
import org.iogp.gigs.internal.geoapi.Pending;

import static org.junit.jupiter.api.Assertions.assertNull;


/**
 * Accessor to API specific to Apache SIS project.
 * This is a placeholder for methods that are missing in GeoAPI 3.0 but may be added in GeoAPI 3.1.
 *
 * @author  Michael Arneson (INT)
 * @author  Martin Desruisseaux (Geomatys)
 * @version 1.0
 * @since   1.0
 */
public final class DefaultTransformationFactory extends Pending {
    /**
     * Prefix in package name for identifying this implementation.
     */
    public static final String PACKAGE_PREFIX = "org.apache.sis.";

    /**
     * Accessor to the public {@code DefaultTransformation} constructor of Apache SIS.
     */
    private final Constructor<? extends Transformation> defaultTransformationConstructor;

    /**
     * Creates a new accessor to Apache SIS.
     *
     * @param  loader  the class loader that loaded the implementation.
     * @throws ReflectiveOperationException if the class or constructor is not found.
     */
    public DefaultTransformationFactory(final ClassLoader loader) throws ReflectiveOperationException {
        super(loader);
        defaultTransformationConstructor = loader.loadClass("org.apache.sis.referencing.operation.DefaultTransformation")
                .asSubclass(Transformation.class)
                .getConstructor(Map.class,                            // properties
                                CoordinateReferenceSystem.class,      // sourceCRS
                                CoordinateReferenceSystem.class,      // targetCRS
                                CoordinateReferenceSystem.class,      // interpolationCRS
                                OperationMethod.class,                // method
                                MathTransform.class);                 // transform
    }

    /**
     * Creates a coordinate transformation from the given properties.
     * This method delegates to Apache SIS public API using reflection.
     */
    @Override
    public Transformation createTransformation(final Map<String,Object> properties,
                                               final CoordinateReferenceSystem sourceCRS,
                                               final CoordinateReferenceSystem targetCRS,
                                               final OperationMethod method,
                                               final ParameterValueGroup parameters,
                                               final MathTransform transform) throws FactoryException
    {
        assertNull(properties.put("parameters", parameters));       // SIS-specific property.
        try {
            return defaultTransformationConstructor.newInstance(properties, sourceCRS, targetCRS, null, method, transform);
        } catch (InvocationTargetException ex) {
            final Throwable cause = ex.getCause();
            if (cause instanceof FactoryException) {    // Propagate as if we invoked the implementation API directly.
                throw (FactoryException) cause;
            }
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            throw new FactoryException("Unable to create transformation", cause);
        } catch (ReflectiveOperationException ex) {
            throw new FactoryException("Unable to create transformation", ex);
        }
    }
}
