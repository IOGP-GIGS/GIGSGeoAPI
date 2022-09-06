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

import java.util.Objects;
import javax.measure.Unit;
import org.opengis.parameter.ParameterNotFoundException;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.parameter.ParameterValue;
import org.opengis.referencing.operation.SingleOperation;
import org.opentest4j.AssertionFailedError;

import static org.junit.jupiter.api.Assertions.*;


/**
 * An immutable tuple for parameter name, value and unit of measurement.
 * This class provides a small subset of {@link ParameterDescriptor} and
 * {@link ParameterValue} functionalities combined in a single class for simplicity.
 * This class serves two purposes:
 *
 * <ol>
 *   <li>Assign parameter values to the {@link ParameterValueGroup}
 *       instance supplied by the implementation to test.</li>
 *   <li>Verifies the parameter values declared by the {@link SingleOperation}
 *       instance created by the implementation to test.</li>
 * </ol>
 *
 * This class exists because the GIGS tests need to perform those two steps.
 * For normal use, this class would not be needed.
 * Users should instead initialize parameters with a pattern like below:
 *
 * {@snippet lang="java" :
 *     ParameterValueGroup pg = mtFactory.getDefaultParameters("Oblique Stereographic");
 *     pg.parameter("Latitude of natural origin")    .setValue(52.15616056, units.degree());
 *     pg.parameter("Longitude of natural origin")   .setValue(5.387638889, units.degree());
 *     pg.parameter("Scale factor at natural origin").setValue(0.9999079,   units.one());
 *     pg.parameter("False easting")                 .setValue(155000,      units.metre());
 *     pg.parameter("False northing")                .setValue(463000,      units.metre());
 * }
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @version 1.0
 * @since   1.0
 */
public class Parameter {
    /**
     * The parameter name.
     *
     * @see ParameterDescriptor#getName()
     */
    public final String name;

    /**
     * The parameter value.
     * The type can be {@link String} or {@link Double}.
     *
     * @see ParameterValue#getValue()
     */
    private final Object value;

    /**
     * The unit of measurement, or {@code null} if none.
     * If non-null, then {@link #value} shall be an instance of {@link Double}.
     *
     * @see ParameterValue#getUnit()
     */
    private final Unit<?> unit;

    /**
     * Creates a new parameter of the given name and value without unit.
     *
     * @param  name   the parameter name.
     * @param  value  the parameter value.
     */
    public Parameter(final String name, final String value) {
        this.name  = name;
        this.value = value;
        this.unit  = null;
    }

    /**
     * Creates a new parameter of the given name, value and units.
     *
     * @param  name   the parameter name.
     * @param  value  the parameter value.
     * @param  unit   the unit of measurement, or {@code null} if none.
     */
    public Parameter(final String name, final double value, final Unit<?> unit) {
        this.name  = name;
        this.value = value;
        this.unit  = unit;
    }

    /**
     * Returns the error message to return if the parameter was not found.
     * This method is defined in case that the implementation does not said which parameter was not found.
     * Users would normally not need to define such method for using the {@link ParameterValueGroup} API.
     */
    private String parameterNotFound() {
        return "A parameter named \"" + name + "\" was required but not found.";
    }

    /**
     * Locates the parameter named {@link #name} in the specified group and sets its value.
     * This is a convenience method for GIGS tests; users normally do not need this method.
     * The recommended pattern for setting a parameter value is simply like below:
     *
     * {@snippet lang="java" :
     *     group.parameter("Latitude of 1st standard parallel").setValue(-18, units.degree());
     * }
     *
     * The code in this method is more complicated, but this is for the purpose of GIGS tests only.
     *
     * @param  destination  the parameter group where to copy the parameter value.
     * @throws AssertionFailedError if this parameter is not found in the specified group.
     */
    public void setValueInto(final ParameterValueGroup destination) {
        final ParameterValue<?> parameter;
        try {
            parameter = destination.parameter(name);
        } catch (ParameterNotFoundException e) {
            throw new AssertionFailedError(parameterNotFound(), e);
        }
        if (unit != null) {
            parameter.setValue((Double) value, unit);
        } else {
            parameter.setValue(value);
        }
    }

    /**
     * Verifies that the parameter named {@link #name} exists and has the expected value.
     * Unit conversion are applied if needed.
     *
     * @param  parameters  the group of parameters to verify.
     * @throws AssertionFailedError if this parameter is not found in the specified group
     *         or does not have the expected value.
     */
    public void verify(final ParameterValueGroup parameters) {
        final ParameterValue<?> parameter;
        try {
            parameter = parameters.parameter(name);
        } catch (ParameterNotFoundException e) {
            throw new AssertionFailedError(parameterNotFound(), e);
        }
        if (unit != null) {
            final double expected = (Double) value;
            final double actual = parameter.doubleValue(unit);
            assertEquals(expected, actual, StrictMath.abs(expected * IntegrityTest.TOLERANCE), name);
        } else {
            assertEquals(value, parameter.getValue(), name);
        }
    }

    /**
     * Compares the given object with this parameter for equality.
     *
     * @param  object  the object to compare with this parameter.
     * @return whether the two objects are equal.
     */
    @Override
    public boolean equals(final Object object) {
        if (object != null && object.getClass() == getClass()) {
            final Parameter other = (Parameter) object;
            return name .equals(other.name)  &&
                   value.equals(other.value) &&
                   Objects.equals(unit, other.unit);
        }
        return false;
    }

    /**
     * Returns a hash code value for this parameter.
     *
     * @return a hash code value for this parameter.
     */
    @Override
    public int hashCode() {
        return ~name.hashCode() + 31 * value.hashCode();
    }

    /**
     * Returns a string representation of this parameter value.
     *
     * @return a string representation of this parameter value.
     */
    @Override
    public String toString() {
        final StringBuilder buffer = new StringBuilder(30).append(getClass().getSimpleName())
                .append("[\"").append(name).append("\" = ").append(value);
        if (unit != null) {
            buffer.append(' ').append(unit);
        }
        return buffer.append(']').toString();
    }
}
