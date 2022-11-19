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
 * The geodetic CRS types. The name of each enumeration value shall match the name
 * of a {@code AxisDirection[]} constant in {@code org.iogp.gigs.Test2205} class.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @version 1.0
 * @since   1.0
 */
public enum GeodeticCrsType {
    /**
     * The "Geographic 2D" type declared in CSV file.
     */
    GEOGRAPHIC_2D("Geographic 2D"),

    /**
     * The "Geographic 3D" type declared in CSV file.
     */
    GEOGRAPHIC_3D("Geographic 3D"),

    /**
     * The "Geocentric" type declared in CSV file.
     */
    GEOCENTRIC("Geocentric");

    /**
     * The text used in the CSV file for identifying this type.
     */
    final String label;

    /**
     * Creates a new enumeration value.
     *
     * @param  label  text used in the CSV file for identifying this type.
     */
    private GeodeticCrsType(final String label) {
        this.label = label;
    }

    /**
     * Returns a short text to insert in Javadoc for describing this geodetic CRS type.
     *
     * @return description of this geodetic CRS type for documentation purpose.
     */
    @Override
    public String toString() {
        return label;
    }

    /**
     * Returns the enumeration value for the given type.
     *
     * @param  type  the text used in the CSV file for identifying the type.
     * @return the enumeration value for the given label.
     * @throws IllegalArgumentException if the given label cannot be matched to an enumeration value.
     */
    static GeodeticCrsType parse(final String type) {
        for (final GeodeticCrsType e : values()) {
            if (e.label.equalsIgnoreCase(type)) {
                return e;
            }
        }
        throw new IllegalArgumentException("Unexpected type: " + type);
    }
}
