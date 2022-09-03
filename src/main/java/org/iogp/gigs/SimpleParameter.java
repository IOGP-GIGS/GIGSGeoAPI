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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.Set;
import javax.measure.Unit;
import javax.measure.IncommensurableException;
import org.opengis.parameter.InvalidParameterValueException;
import org.opengis.parameter.InvalidParameterTypeException;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterValue;


/**
 * A simple parameter value implementation for GIGS tests creating coordinate operation.
 * In order to keep this class simple, this parameter value is also its own descriptor.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @version 1.0
 * @since   1.0
 */
final class SimpleParameter extends SimpleIdentifiedObject
        implements ParameterValue<Object>, ParameterDescriptor<Object>
{
    /**
     * The parameter value.
     */
    private final Object value;

    /**
     * The unit of measurement, or {@code null} if none.
     */
    private final Unit<?> unit;

    /**
     * Creates a new parameter of the given name and value without unit.
     *
     * @param  name   the parameter name.
     * @param  value  the parameter value.
     */
    SimpleParameter(final String name, final String value) {
        super(name);
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
    SimpleParameter(final String name, final double value, final Unit<?> unit) {
        super(name);
        this.value = value;
        this.unit  = unit;
    }

    /**
     * Returns the descriptor of the parameter value. Since this simple class implements both the
     * {@linkplain ParameterValue value} and the {@linkplain ParameterDescriptor descriptor} interfaces,
     * this method returns {@code this}.
     *
     * @return {@code this} descriptor.
     */
    @Override
    public ParameterDescriptor<Object> getDescriptor() {
        return this;
    }

    /**
     * Returns the class of the value.
     * We use raw types because we did not bother to parameterized this class.
     * It would be bad practice in a public class, but because instances of this
     * class are created only by GIGS code in contexts where users will only see
     * {@code ParameterValue<?>}, it is okay here.
     */
    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public Class getValueClass() {
        return value.getClass();
    }

    @Override
    public int getMinimumOccurs() {
        return 1;
    }

    @Override
    public int getMaximumOccurs() {
        return 1;
    }

    @Override
    public Comparable<Object> getMinimumValue() {
        return null;
    }

    @Override
    public Comparable<Object> getMaximumValue() {
        return null;
    }

    @Override
    public Set<Object> getValidValues() {
        return null;
    }

    @Override
    public Object getDefaultValue() {
        return null;
    }

    /**
     * Returns the unit of measurement.
     *
     * @return the unit of measurement, or {@code null} if unknown.
     */
    @Override
    public Unit<?> getUnit() {
        return unit;
    }

    /**
     * Returns the parameter {@linkplain #value} as an object.
     */
    @Override
    public Object getValue() {
        return value;
    }

    /**
     * Returns the numeric value represented by this parameter.
     */
    @Override
    public double doubleValue() {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        } else {
            throw new InvalidParameterTypeException("Not a number", name);
        }
    }

    /**
     * Returns the numeric value of the operation parameter in the specified unit of measure.
     * This convenience method applies unit conversion on the fly as needed.
     */
    @Override
    public double doubleValue(final Unit<?> target) {
        if (unit == null) {
            throw new IllegalStateException("No unit for parameter " + name + '.');
        } else try {
            return unit.getConverterToAny(target).convert(doubleValue());
        } catch (IncommensurableException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Returns the integer value of an operation parameter, usually used for a count.
     */
    @Override
    public int intValue() {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        } else {
            throw new InvalidParameterTypeException("Not a number", name);
        }
    }

    /**
     * Returns the boolean value of an operation parameter.
     */
    @Override
    public boolean booleanValue() {
        if (value instanceof Boolean) {
            return (Boolean) value;
        } else {
            throw new InvalidParameterTypeException("Not a boolean", name);
        }
    }

    /**
     * Returns the string representation of an operation parameter value.
     */
    @Override
    public String stringValue() {
        return value.toString();
    }

    /**
     * Returns an ordered sequence of numeric values in the specified unit of measure.
     */
    @Override
    public double[] doubleValueList(final Unit<?> unit) throws IllegalArgumentException, IllegalStateException {
        throw new InvalidParameterTypeException("This parameter is not for arrays.", name);
    }

    /**
     * Returns an ordered sequence numeric values of an operation parameter list, where each value
     * has the same associated {@linkplain Unit unit of measure}.
     */
    @Override
    public double[] doubleValueList() {
        throw new InvalidParameterTypeException("This parameter is not for arrays.", name);
    }

    /**
     * Returns an ordered sequence integer values of an operation parameter list.
     */
    @Override
    public int[] intValueList() {
        throw new InvalidParameterTypeException("This parameter is not for arrays.", name);
    }

    /**
     * Returns the parameter value as an URI.
     */
    @Override
    public URI valueFile() {
        URISyntaxException cause = null;
        if (value instanceof URI) try {
            return new URI((String) value);
        } catch (URISyntaxException e) {
            cause = e;
        }
        InvalidParameterTypeException ex = new InvalidParameterTypeException("This parameter is not for files.", name);
        if (cause != null) ex.initCause(cause);
        throw ex;
    }

    /**
     * Message for the exception to be thrown when a parameter setter is invoked.
     */
    static final String IMMUTABLE = "This parameter implementation is immutable.";

    /**
     * Unsupported operation because this parameter implementation is immutable.
     */
    @Override
    public void setValue(final double value, final Unit<?> unit) {
        throw new UnsupportedOperationException(IMMUTABLE);
    }

    /**
     * Unsupported operation because this parameter implementation is immutable.
     */
    @Override
    public void setValue(final double value) throws InvalidParameterValueException {
        throw new UnsupportedOperationException(IMMUTABLE);
    }

    /**
     * Unsupported operation because this parameter implementation is immutable.
     */
    @Override
    public void setValue(final double[] values, final Unit<?> unit) throws InvalidParameterValueException {
        throw new UnsupportedOperationException(IMMUTABLE);
    }

    /**
     * Unsupported operation because this parameter implementation is immutable.
     */
    @Override
    public void setValue(final int value) throws InvalidParameterValueException {
        throw new UnsupportedOperationException(IMMUTABLE);
    }

    /**
     * Unsupported operation because this parameter implementation is immutable.
     */
    @Override
    public void setValue(final boolean value) throws InvalidParameterValueException {
        throw new UnsupportedOperationException(IMMUTABLE);
    }

    /**
     * Unsupported operation because this parameter implementation is immutable.
     */
    @Override
    public void setValue(final Object value) throws InvalidParameterValueException {
        throw new UnsupportedOperationException(IMMUTABLE);
    }

    /**
     * Unsupported operation because this parameter implementation is immutable.
     */
    @Override
    public SimpleParameter createValue() {
        throw new UnsupportedOperationException(IMMUTABLE);
    }

    /**
     * Not needed because this parameter implementation is immutable.
     */
    @Override
    @SuppressWarnings("CloneDoesntCallSuperClone")
    public ParameterValue<Object> clone() {
        return this;
    }

    /**
     * Compares the given object with this parameter for equality.
     */
    @Override
    public boolean equals(final Object object) {
        if (object instanceof SimpleParameter) {
            final SimpleParameter other = (SimpleParameter) object;
            return name.equals(other.name) &&
                   value.equals(other.value) &&
                   Objects.equals(unit, other.unit);
        }
        return false;
    }

    /**
     * Returns a hash code value for this parameter.
     */
    @Override
    public int hashCode() {
        return ~name.hashCode() + 31 * value.hashCode();
    }

    /**
     * Returns the string representation of this parameter value.
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
