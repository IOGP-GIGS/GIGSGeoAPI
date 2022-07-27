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

import org.opengis.referencing.AuthorityFactory;
import org.opengis.referencing.ObjectFactory;
import org.opengis.util.Factory;


/**
 * Whether an object to use in the test is defined by user-provided parameters or by EPSG code.
 * In the former case, the object will be created using {@link ObjectFactory}.
 * In the latter case, the object will be created using {@link AuthorityFactory}.
 * This enumeration provides a way to document in Javadoc which factory is used for a particular test.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @version 1.0
 * @since   1.0
 */
public enum DefinitionSource {
    /**
     * Object is defined by the library.
     * Fetching this object requires the use of an {@link AuthorityFactory}.
     */
    LIBRARY(AuthorityFactory.class, "Fetched from EPSG dataset"),

    /**
     * Object is defined by the user providing all parameters.
     * Fetching this object requires the use of an {@link ObjectFactory}.
     */
    USER(ObjectFactory.class, "Described by user"),

    /**
     * Object is defined by the user providing all parameters together with an early-binding transformation.
     * Fetching this object requires the use of an {@link ObjectFactory}.
     */
    USER_EARLY_BOUND(ObjectFactory.class, "Described by user with early-binding");

    /**
     * Description of this source to write in the Javadoc.
     */
    private final String description;

    /**
     * The type of factory used for building the object.
     * Can be {@link AuthorityFactory} or {@link ObjectFactory}.
     */
    private final Class<? extends Factory> factoryType;

    /**
     * Creates a new enumeration value.
     *
     * @param  factoryType  the type of factory used for building the object.
     * @param  description  description of this source to write in the Javadoc.
     */
    private DefinitionSource(final Class<? extends Factory> factoryType, final String description) {
        this.factoryType = factoryType;
        this.description = description;
    }

    /**
     * Returns the definition source for the given string representation.
     *
     * @param  source  string representation of the definition source.
     * @return the definition source.
     * @throws IllegalArgumentException if the given argument is not recognized.
     */
    static DefinitionSource parse(final String source) {
        if ("Library"         .equalsIgnoreCase(source)) return LIBRARY;
        if ("User"            .equalsIgnoreCase(source)) return USER;
        if ("User Early-Bound".equalsIgnoreCase(source)) return USER_EARLY_BOUND;
        throw new IllegalArgumentException(source);
    }

    /**
     * Returns a short text to insert in Javadoc for describing this definition source.
     *
     * @return description of this source for documentation purpose.
     */
    @Override
    public String toString() {
        return description;
    }

    /**
     * Appends a text after {@link #description} for telling which factory is used for building the object.
     *
     * @param out                 where to append to text.
     * @param libraryFactoryType  the type of factory for definitions provided by the library, or {@code null} if unknown.
     * @param userFactoryType     the type of factory for definitions supplied by the user, or {@code null} if unknown.
     */
    final void appendFactoryInformation(final StringBuilder out,
            final Class<? extends AuthorityFactory> libraryFactoryType,
            final Class<? extends ObjectFactory>    userFactoryType)
    {
        Class<? extends Factory> f = factoryType;
        if (userFactoryType    != null && f.isAssignableFrom(userFactoryType))    f = userFactoryType;
        if (libraryFactoryType != null && f.isAssignableFrom(libraryFactoryType)) f = libraryFactoryType;
        out.append(" (build with {@link ").append(f.getSimpleName()).append("})");
    }
}
