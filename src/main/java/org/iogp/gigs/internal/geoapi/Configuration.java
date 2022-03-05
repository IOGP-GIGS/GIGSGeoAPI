/*
 *    GeoAPI - Java interfaces for OGC/ISO standards
 *    http://www.geoapi.org
 *
 *    Copyright (C) 2011-2021 Open Geospatial Consortium, Inc.
 *    All Rights Reserved. http://www.opengeospatial.org/ogc/legal
 *
 *    Permission to use, copy, and modify this software and its documentation, with
 *    or without modification, for any purpose and without fee or royalty is hereby
 *    granted, provided that you include the following on ALL copies of the software
 *    and documentation or portions thereof, including modifications, that you make:
 *
 *    1. The full text of this NOTICE in a location viewable to users of the
 *       redistributed or derivative work.
 *    2. Notice of any changes or modifications to the OGC files, including the
 *       date changes were made.
 *
 *    THIS SOFTWARE AND DOCUMENTATION IS PROVIDED "AS IS," AND COPYRIGHT HOLDERS MAKE
 *    NO REPRESENTATIONS OR WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 *    TO, WARRANTIES OF MERCHANTABILITY OR FITNESS FOR ANY PARTICULAR PURPOSE OR THAT
 *    THE USE OF THE SOFTWARE OR DOCUMENTATION WILL NOT INFRINGE ANY THIRD PARTY
 *    PATENTS, COPYRIGHTS, TRADEMARKS OR OTHER RIGHTS.
 *
 *    COPYRIGHT HOLDERS WILL NOT BE LIABLE FOR ANY DIRECT, INDIRECT, SPECIAL OR
 *    CONSEQUENTIAL DAMAGES ARISING OUT OF ANY USE OF THE SOFTWARE OR DOCUMENTATION.
 *
 *    The name and trademarks of copyright holders may NOT be used in advertising or
 *    publicity pertaining to the software without specific, written prior permission.
 *    Title to copyright in this software and any associated documentation will at all
 *    times remain with copyright holders.
 */
package org.iogp.gigs.internal.geoapi;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import org.opengis.util.CodeList;
import org.opengis.referencing.IdentifiedObject;


/**
 * Contains information about the test environment, like available factories and disabled tests.
 * This is a placeholder for a class defined in GeoAPI 3.1-SNAPSGOT,
 * to be removed after GeoAPI 3.1 has been released.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @version 1.0
 * @since   1.0
 */
public final class Configuration {
    /**
     * The map were to store the configuration entries.
     */
    private final Map<Key<?>,Object> properties;

    /**
     * Creates a new, initially empty, configuration map.
     */
    public Configuration() {
        properties = new LinkedHashMap<>();
    }

    /**
     * Returns the value to which the specified key is mapped, or {@code null}
     * if this map contains no mapping for the key.
     *
     * @param  <T>  the value type, which is determined by the key.
     * @param  key  the key whose associated value is to be returned.
     * @return the value to which the specified key is mapped, or {@code null}
     *         if this map contains no mapping for the key.
     * @throws NullPointerException if the specified key is null.
     */
    public <T> T get(final Key<T> key) {
        return key.type.cast(properties.get(key));
    }

    /**
     * Type-safe keys that can be used in a {@link Configuration} map.
     */
    public static final class Key<T> extends CodeList<Key<?>> {
        /**
         * For cross-version compatibility.
         */
        private static final long serialVersionUID = -5920183652024058448L;

        /**
         * The list of all keys created. Contains the key constants declared in this class, and
         * any key that the user may have created. Must be declared before any key declaration.
         */
        private static final List<Key<?>> VALUES = new ArrayList<>(32);

        /**
         * Whether the {@link IdentifiedObject} instances have {@linkplain IdentifiedObject#getName()
         * names} matching the names declared in the EPSG database.
         */
        public static final Key<Boolean> isStandardNameSupported =
                new Key<>(Boolean.class, "isStandardNameSupported");

        /**
         * Whether the {@link IdentifiedObject} instances have at least the
         * {@linkplain IdentifiedObject#getAlias() aliases} declared in the EPSG database.
         */
        public static final Key<Boolean> isStandardAliasSupported =
                new Key<>(Boolean.class, "isStandardAliasSupported");

        /**
         * Whether the {@link IdentifiedObject} instances created indirectly by the factories
         * are expected to have correct identification information.
         */
        public static final Key<Boolean> isDependencyIdentificationSupported =
                new Key<>(Boolean.class, "isDependencyIdentificationSupported");

        /**
         * Whether the authority factory supports creation of deprecated {@link IdentifiedObject} instances.
         */
        public static final Key<Boolean> isDeprecatedObjectCreationSupported =
                new Key<>(Boolean.class, "isDeprecatedObjectCreationSupported");

        /**
         * Whether the objects created by the tested {@link org.opengis.referencing.ObjectFactory} use the
         * specified values <i>as-is</i>. This flag should be set to {@code false} if the factory performs
         * any of the following operations:
         *
         * <ul>
         *   <li>Convert numerical values from user-provided linear units to metres.</li>
         *   <li>Convert numerical values from user-provided angular units to degrees.</li>
         *   <li>Change ellipsoid second defining parameter
         *       (e.g. from <i>semi-major axis length</i> to an equivalent <i>inverse flattening factor</i>).</li>
         *   <li>Change map projection parameters
         *       (e.g. from <i>standard parallel</i> to an equivalent <i>scale factor</i>).</li>
         *   <li>Any other change that preserve numeric equivalence.</li>
         * </ul>
         *
         * If the factory does not perform any of the above conversions, then this flag can be {@code true}.
         */
        public static final Key<Boolean> isFactoryPreservingUserValues =
                new Key<>(Boolean.class, "isFactoryPreservingUserValues");

        /**
         * The type of values associated to this key.
         */
        private final Class<T> type;

        /**
         * Constructs a key with the given name. The new key is
         * automatically added to the list returned by {@link #values}.
         *
         * @param  type  the type of values associated to the new key.
         * @param  name  the key name. This name must not be in use by any other key.
         */
        private Key(final Class<T> type, final String name) {
            super(name, VALUES);
            this.type = type;
        }

        /**
         * Returns the list of {@code Key}s.
         *
         * @return the list of keys declared in the current JVM.
         */
        public static Key<?>[] values() {
            synchronized (VALUES) {
                return VALUES.toArray(new Key<?>[VALUES.size()]);
            }
        }

        /**
         * Returns the list of codes of the same kind than this code list element.
         * Invoking this method is equivalent to invoking {@link #values()}, except that
         * this method can be invoked on an instance of the parent {@code CodeList} class.
         *
         * @return all code {@linkplain #values() values} for this code list.
         */
        @Override
        public Key<?>[] family() {
            return values();
        }

        /**
         * Returns the type of values assigned to this key.
         *
         * @return the value type.
         */
        public Class<T> valueType() {
            return type;
        }
    }
}
