package org.iogp.gigs.internal.sis;

import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.OperationMethod;
import org.opengis.referencing.operation.Transformation;
import org.opengis.util.Factory;
import org.opengis.util.FactoryException;

import java.util.Map;

public interface TransformationFactory extends Factory {

    public Transformation createTransformation(final Map<String,Object> properties,
                                               final CoordinateReferenceSystem sourceCRS,
                                               final CoordinateReferenceSystem targetCRS,
                                               final OperationMethod method,
                                               final MathTransform transform) throws FactoryException;
}
