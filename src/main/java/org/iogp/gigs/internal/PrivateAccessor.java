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
package org.iogp.gigs.internal;

import java.lang.reflect.Method;
import org.iogp.gigs.IntegrityTest;
import org.iogp.gigs.internal.geoapi.Configuration;


/**
 * Accessors for private methods that we do not want to make public at this time.
 * This is a temporary hack to be removed after we have settle a public API.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @version 1.0
 * @since   1.0
 */
public class PrivateAccessor {
    /**
     * The unique accessor instance. Shall never be {@code null}.
     * This is initialized by {@link IntegrityTest} static initializer.
     */
    public static volatile PrivateAccessor INSTANCE = new PrivateAccessor();

    /**
     * Creates a new accessor.
     */
    protected PrivateAccessor() {
    }

    /**
     * Returns information about the configuration of a test.
     *
     * @param  test  the test for which to get the configuration.
     * @return the configuration of the specified test, or an empty map if none.
     */
    public Configuration configuration(IntegrityTest test) {
        return new Configuration();
    }

    /**
     * Enables or disables an optional aspect for a specific test method.
     *
     * @param  method  the test method to configure, or {@code null} for global configuration.
     * @param  aspect  the test aspect to enable or disable.
     * @param  value   the new enabled status, or {@code null} for removing.
     */
    public void setTestSpecificOption(Method method, Configuration.Key<Boolean> aspect, Boolean value) {
    }
}
