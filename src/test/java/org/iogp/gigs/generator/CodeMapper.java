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

import java.util.Map;
import java.util.HashMap;
import java.util.OptionalInt;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Maps GIGS codes to EPSG codes or conversely.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @version 1.0
 * @since   1.0
 */
final class CodeMapper {
    /**
     * Mapping from GIGS codes to EPSG codes.
     */
    private final Map<Integer,Integer> dependencies;

    /**
     * Loads (GIGS code, EPSG code) mapping from a file of {@link Series#USER_DEFINED} tests.
     *
     * @param  filename    test data to load.
     * @param  epsgColumn  column of EPSG codes.
     * @param  reverse     {@code true} for reverse mapping (from EPSG to GIGS).
     * @throws IOException if an error occurred while reading the test data.
     */
    CodeMapper(final String filename, final int epsgColumn, final boolean reverse) throws IOException {
        final Class<?>[] columnTypes = new Class<?>[epsgColumn + 1];
        columnTypes[epsgColumn] = String.class;     // Should be first.
        columnTypes[0] = Integer.class;
        dependencies = new HashMap<>();
        final DataParser data = new DataParser(Series.USER_DEFINED, filename, columnTypes);
        while (data.next()) {
            final int[] codes = data.getInts(epsgColumn);
            if (codes.length == 1) {
                final Integer gigs = data.getInt(0);
                final Integer epsg = codes[0];
                if (reverse) {
                    assertNull(dependencies.put(epsg, gigs));
                } else {
                    assertNull(dependencies.put(gigs, epsg));
                }
            }
        }
    }

    /**
     * Converts the given code, making sure the result is not null.
     *
     * @param  code  code to convert.
     * @return converted code.
     */
    final int convert(final int code) {
        Integer value = dependencies.get(code);
        assertNotNull(value, () -> "No mapping for code " + code);
        return value;
    }

    /**
     * Converts the given code.
     *
     * @param  code  code to convert.
     * @return converted code.
     */
    final OptionalInt optional(final int code) {
        Integer value = dependencies.get(code);
        return (value != null) ? OptionalInt.of(value) : OptionalInt.empty();
    }
}
