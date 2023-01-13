/*
 * GIGS - Geospatial Integrity of Geoscience Software
 * https://gigs.iogp.org/
 *
 * Copyright (C) 2023 International Association of Oil and Gas Producers.
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

import java.util.HashSet;
import java.lang.reflect.Method;
import org.iogp.gigs.internal.TestSuite;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Verifies the annotations on all test methods of the test suite.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @version 1.0
 * @since   1.0
 */
public final class AnnotationsTest {
    /**
     * Creates a new test case.
     */
    public AnnotationsTest() {
    }

    /**
     * Verifies that all tests have a {@link DisplayName} annotation with no name collision.
     * This is important because the Swing application uses display names as keys for sorting
     * and a name collision would cause two (or more) tests to appear as only one test.
     */
    @Test
    public void verifyDisplayNames() {
        final var names = new HashSet<>();
        for (final Class<?> test : new TestSuite().getTestClasses()) {
            for (final Method method : test.getMethods()) {
                if (method.getAnnotation(Test.class) != null) {
                    final DisplayName a = method.getAnnotation(DisplayName.class);
                    assertNotNull(a, () -> "Missing @DisplayName annotation in " + method);
                    assertTrue(names.add(a.value()), () -> "Duplicated name: " + a.value());
                }
            }
            assertFalse(names.isEmpty(), () -> "No test found in " + test);
            names.clear();
        }
    }
}
