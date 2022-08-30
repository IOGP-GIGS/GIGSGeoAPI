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


/**
 * Enumeration values for sexagesimal units, which are handled specially.
 * There is no {@link DataParser} method returning instances of this type,
 * which is why this enumeration is not public.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @version 1.0
 *
 * @see TestMethodGenerator#parseAngularUnit(String)
 *
 * @since 1.0
 */
enum SexagesimalUnit {
    /**
     * Sexagesimal degree.
     */
    DEGREE(9110, "sexagesimal degree"),

    /**
     * Sexagesimal DMS (EPSG::9110).
     */
    DMS(9110, "sexagesimal DMS");

    /**
     * The EPSG code for the unit.
     */
    final int code;

    /**
     * The unit name in data files.
     */
    final String text;

    /**
     * Creates a new enumeration value.
     *
     * @param code the EPSG code for the unit.
     * @param text the unit name in data files.
     */
    private SexagesimalUnit(final int code, final String text) {
        this.text = text;
        this.code = code;
    }

    /**
     * Retrieves the angular unit (compatible with degrees) of the given name.
     *
     * @param  text  the unit name.
     * @return the angular unit for the given name, or {@code null} if unrecognized.
     */
    static SexagesimalUnit parse(final String text) {
        for (final SexagesimalUnit c : values()) {
            if (c.text.equalsIgnoreCase(text)) {
                return c;
            }
        }
        return null;
    }

    /**
     * Formats the given value as DD°MM′SS″.
     *
     * @param  value  the value to format.
     * @param  out    where to append the value.
     */
    void format(final double value, final StringBuilder out) {
        int s = out.length();
        out.append(value);
        s = out.indexOf(".", s);
        if (s >= 0) {
            out.setCharAt(s, '°');
            final int e = ++s + 4;                  // Position after last MMSS fraction digit.
            final int m = e - out.length();         // Number of missing trailing zeros.
            if (m >= 0) {
                out.append("0".repeat(m));
            } else {
                out.insert(e, '.');
            }
            out.append('″').insert(s + 2, '′');     // Insert ′ between MM and SS.
        }
    }

    /**
     * Converts the given sexagesimal value to a decimal value.
     * Current implementation does not really performs a conversion because decimal values
     * are provided in the CSV files, except integer values because they can be used as-is.
     *
     * @param  value  the sexagesimal value.
     * @return the decimal value.
     */
    double toDecimalDegrees(final double value) {
        if (value == Math.rint(value)) {
            return value;
        } else {
            throw new IllegalArgumentException("Sexagesimal value without corresponding decimal value: " + value);
        }
    }

    /**
     * Returns a short text to insert in Javadoc for describing this unit.
     *
     * @return description of this geodetic CRS type for documentation purpose.
     */
    @Override
    public String toString() {
        return text;
    }
}
