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

import java.util.Arrays;
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
     * Creates a new test case.
     */
    public DataParserTest() {
    }

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

    /**
     * Tests parsing data having a range of integer values.
     */
    @Test
    public void testGetInts() {
        final DataParser data = new DataParser(Arrays.asList(new Object[][] {
            new String[] {"16001-16060; 16101-16160"},
            new String[] {"16261-16299; 16070-16089; 16099; 16091-16094"},
            new String[] {"16362-16398 +2; 16170-16194 +2"}
        }));
        assertTrue(data.next());
        assertEquals("16001-16060; 16101-16160", data.getString(0));
        assertArrayEquals(new int[] {
            16001, 16002, 16003, 16004, 16005, 16006, 16007, 16008, 16009, 16010,
            16011, 16012, 16013, 16014, 16015, 16016, 16017, 16018, 16019, 16020,
            16021, 16022, 16023, 16024, 16025, 16026, 16027, 16028, 16029, 16030,
            16031, 16032, 16033, 16034, 16035, 16036, 16037, 16038, 16039, 16040,
            16041, 16042, 16043, 16044, 16045, 16046, 16047, 16048, 16049, 16050,
            16051, 16052, 16053, 16054, 16055, 16056, 16057, 16058, 16059, 16060,
            16101, 16102, 16103, 16104, 16105, 16106, 16107, 16108, 16109, 16110,
            16111, 16112, 16113, 16114, 16115, 16116, 16117, 16118, 16119, 16120,
            16121, 16122, 16123, 16124, 16125, 16126, 16127, 16128, 16129, 16130,
            16131, 16132, 16133, 16134, 16135, 16136, 16137, 16138, 16139, 16140,
            16141, 16142, 16143, 16144, 16145, 16146, 16147, 16148, 16149, 16150,
            16151, 16152, 16153, 16154, 16155, 16156, 16157, 16158, 16159, 16160
        }, data.getInts(0), "16001-16060; 16101-16160");

        assertTrue(data.next());
        assertEquals("16261-16299; 16070-16089; 16099; 16091-16094", data.getString(0));
        assertArrayEquals(new int[] {
            16261, 16262, 16263, 16264, 16265, 16266, 16267, 16268, 16269, 16270,
            16271, 16272, 16273, 16274, 16275, 16276, 16277, 16278, 16279, 16280,
            16281, 16282, 16283, 16284, 16285, 16286, 16287, 16288, 16289, 16290,
            16291, 16292, 16293, 16294, 16295, 16296, 16297, 16298, 16299, 16070,
            16071, 16072, 16073, 16074, 16075, 16076, 16077, 16078, 16079, 16080,
            16081, 16082, 16083, 16084, 16085, 16086, 16087, 16088, 16089, 16099,
            16091, 16092, 16093, 16094
        }, data.getInts(0), "16261-16299; 16070-16089; 16099; 16091-16094");

        assertTrue(data.next());
        assertEquals("16362-16398 +2; 16170-16194 +2", data.getString(0));
        assertArrayEquals(new int[] {
            16362, 16364, 16366, 16368, 16370, 16372, 16374, 16376, 16378, 16380,
            16382, 16384, 16386, 16388, 16390, 16392, 16394, 16396, 16398, 16170,
            16172, 16174, 16176, 16178, 16180, 16182, 16184, 16186, 16188, 16190,
            16192, 16194
        }, data.getInts(0), "16362-16398 +2; 16170-16194 +2");
    }
}
