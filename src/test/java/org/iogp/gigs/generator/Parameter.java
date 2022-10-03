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
package org.iogp.gigs.generator;

import static org.junit.jupiter.api.Assertions.assertNotNull;


/**
 * Description of an operation parameter as a (name, value, unit) tuple.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @version 1.0
 * @since   1.0
 */
public final class Parameter {
    /**
     * The parameter name.
     */
    public String name;

    /**
     * The parameter value, or {@link Double#NaN} if the parameter is not numerical.
     */
    public double value;

    /**
     * The parameter value in degrees, or {@link Double#NaN} if not applicable.
     */
    public double valueInDegrees;

    /**
     * The parameter value as a string.
     */
    public String valueAsString;

    /**
     * The parameter unit of measurement, or {@code null} if none.
     */
    public String unit;

    /**
     * Creates a new parameter description.
     *
     * @param data               the parser from which to read data.
     * @param column             column of the parameter name.
     * @param valueObject        the parameter value as an object.
     * @param hasDecimalDegrees  whether a "decimal degrees" column is present.
     */
    Parameter(final DataParser data, final int column, final Object valueObject, final boolean hasDecimalDegrees) {
        name = data.getString(column);
        if (valueObject != null) {
            valueAsString = valueObject.toString();
            if (valueObject instanceof Number) {
                value = ((Number) valueObject).doubleValue();
            } else try {
                value = Double.valueOf(valueAsString);
            } catch (NumberFormatException e) {
                value = Double.NaN;
            }
        }
        unit = data.getString(column + 2);
        valueInDegrees = hasDecimalDegrees ? data.getDouble(column + 3) : Double.NaN;
    }

    /**
     * Returns {@code true} if this parameter contains no value.
     */
    final boolean isEmpty() {
        if (name == null && valueAsString == null && unit == null && Double.isNaN(valueInDegrees)) {
            return true;
        }
        assertNotNull(name, "name");
        assertNotNull(valueAsString, "value");
        return false;
    }

    /**
     * Returns a string representation for debugging purposes.
     *
     * @return a string representation of this parameter.
     */
    @Override
    public String toString() {
        final StringBuilder buffer = new StringBuilder(30)
                .append('"').append(name).append("\" = ").append(valueAsString);
        if (unit != null) {
            buffer.append(' ').append(unit);
        }
        if (!Double.isNaN(valueInDegrees)) {
            buffer.append(" = ").append(valueInDegrees).append('°');
        }
        return buffer.toString();
    }
}
