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
