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

import org.opengis.metadata.citation.Citation;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.OperationMethod;
import org.opengis.referencing.operation.Transformation;
import org.opengis.util.FactoryException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

public class DefaultTransformationFactory implements TransformationFactory {
    private final Class defaultTransformationClass;

    public DefaultTransformationFactory(ClassLoader loader) throws ClassNotFoundException {
        this.defaultTransformationClass = loader.loadClass("org.apache.sis.referencing.operation.DefaultTransformation");
    }

    @Override
    public Citation getVendor() {
        return null;
    }


    @Override
    public Transformation createTransformation(Map<String,Object> properties, CoordinateReferenceSystem sourceCRS, CoordinateReferenceSystem targetCRS,
                                               OperationMethod method, MathTransform transform) throws FactoryException {
        try {
            Constructor<?> constructor = defaultTransformationClass.getConstructor(Map.class, CoordinateReferenceSystem.class, CoordinateReferenceSystem.class,
                    CoordinateReferenceSystem.class, OperationMethod.class, MathTransform.class);
            return (Transformation) constructor.newInstance(properties, sourceCRS, targetCRS, null, method, transform);
        } catch(Exception ex) {
            throw new FactoryException("Unable to create transformation", ex);
        }
    }
}
