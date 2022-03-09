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

    /**
     * Tests loading the data from the {@code "GIGS_lib_2202_Ellipsoid.txt"} file.
     * The purpose of this test is to ensure that the file is fully loaded.
     * A few sampled records are tested in this process.
     *
     * @throws IOException if an error occurred while reading the test data.
     */
    @Test
    public void testFileLoading() throws IOException {
        final int[] expectedCodes = {
            7001, 7002, 7003, 7004, 7005, 7007, 7008, 7010, 7011, 7012, 7013, 7014, 7015,
            7016, 7018, 7019, 7020, 7021, 7022, 7024, 7025, 7027, 7028, 7029, 7030, 7031,
            7032, 7033, 7034, 7036, 7041, 7042, 7043, 7044, 7045, 7046, 7048, 7049, 7050,
            7051, 7052, 7053, 7054, 7055, 7056, 7057, 7058
        };

        // We will inspect only the first 5 columns for this test.
        final DataParser data = new DataParser(Series.PREDEFINED, "GIGS_lib_2202_Ellipsoid.txt",
            Integer.class,      // [ 0]: EPSG Ellipsoid Code
            String .class,      // [ 1]: EPSG Ellipsoid Name
            String .class,      // [ 2]: Alias(es) given by EPSG
            Double .class);     // [ 3]: Semi-major axis (a)

        int index = 0;
        while (data.next()) {
            final int      code      = data.getInt    (0);
            final String   name      = data.getString (1);
            final String[] aliases   = data.getStrings(2);
            final double   semiMajor = data.getDouble (3);
            final String   message   = "EPSG:" + code;
            assertEquals (expectedCodes[index], code, message);
            assertNotNull(name, message);
            assertFalse  (name.isEmpty(), message);
            assertNotNull(aliases, message);
            assertTrue   (semiMajor > 0, message);
            switch (code) {
                case 7043: {
                    assertEquals("WGS 72", name, message);
                    assertEquals(6378135,  semiMajor, 0, message);
                    assertArrayEquals(new String[] {"NWL 10D"}, aliases, message);
                    break;
                }
                case 7030: {
                    assertEquals("WGS 84", name, message);
                    assertEquals(6378137,  semiMajor, 0, message);
                    assertArrayEquals(new String[] {"WGS84"}, aliases, message);
                    break;
                }
                case 7013: {
                    assertEquals("Clarke 1880 (Arc)", name, message);
                    assertEquals(6378249.145, semiMajor, 1E-4, message);
                    assertArrayEquals(new String[] {"Modified Clarke 1880 (South Africa)", "Clarke 1880 (Cape)"}, aliases, message);
                    break;
                }
            }
            index++;
        }
        assertEquals(expectedCodes.length, index, "Missing records.");
    }
}
