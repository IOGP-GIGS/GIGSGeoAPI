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

import java.io.IOException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Tests the {@link DataParser} class.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @version 1.0
 * @since   1.0
 */
public final class DataParserTest {
    /**
     * Tests {@link DataParser#parseRow(String, Class[])}.
     *
     * @throws IOException if an error occurred while parsing the row.
     */
    @Test
    @SuppressWarnings("UnnecessaryBoxing")
    public void testParseRow() throws IOException {
        final Object[] values = DataParser.parseRow("8901\ttrue\tGreenwich\t\t\"0°\"\tsexagesimal degree\t0",
            Integer.class, Boolean.class, String.class, String.class, String.class, String.class, Double.class);

        assertEquals(Integer.valueOf(8901), values[0], "EPSG Prime Meridian Code");
        assertEquals(Boolean.TRUE,          values[1], "Particularly important to E&P industry?");
        assertEquals("Greenwich",           values[2], "EPSG Prime Meridian Name");
        assertEquals(null,                  values[3], "EPSG Alias");
        assertEquals("0°",                  values[4], "Longitude from Greenwich (sexagesimal)");
        assertEquals("sexagesimal degree",  values[5], "Unit Name");
        assertEquals(Double.valueOf(0.0),   values[6], "Longitude from Greenwich (degrees)");
    }
}
