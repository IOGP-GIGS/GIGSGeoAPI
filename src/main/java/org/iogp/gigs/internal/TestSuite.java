/*
 * GIGS - Geospatial Integrity of Geoscience Software
 * https://gigs.iogp.org/
 *
 * Copyright (C) 2022-2023 International Association of Oil and Gas Producers.
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
package org.iogp.gigs.internal;

import org.iogp.gigs.*;


/**
 * Collection of all GIGS tests.
 * A test suite is independent of the implementation to test.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @author  Michael Arneson (INT)
 * @version 1.0
 * @since   1.0
 *
 * @todo We need to define an abstract base class for allowing execution of GeoAPI and GIGS tests
 *       with the same application.
 */
public final class TestSuite {
    /**
     * Creates a new suite.
     */
    public TestSuite() {
    }

    /**
     * Returns the classes providing all tests in this suite.
     *
     * @return all tests in this suite.
     */
    public Class<?>[] getTestClasses() {
        /*
         * Prepare the tests plan. We use the GIGS class loader here,
         * not yet the class loader for the factories to be tested.
         */
        return new Class<?>[] {
            Test2201.class, Test2202.class, Test2203.class, Test2204.class, Test2205.class, Test2206.class,
            Test2207.class, Test2208.class, Test2209.class, Test2210.class, Test2211.class, Test3201.class,
            Test3202.class, Test3203.class, Test3204.class, Test3205.class, Test3206.class, Test3207.class,
            Test3208.class, Test3209.class, Test3210.class, Test3211.class, Test3212.class
        };
    }
}
