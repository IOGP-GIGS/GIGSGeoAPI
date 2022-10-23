/*
 *    GeoAPI - Java interfaces for OGC/ISO standards
 *    http://www.geoapi.org
 *
 *    Copyright (C) 2008-2021 Open Geospatial Consortium, Inc.
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
package org.iogp.gigs;

import org.opengis.util.Factory;
import org.iogp.gigs.internal.geoapi.Units;
import org.iogp.gigs.internal.geoapi.Validator;
import org.iogp.gigs.internal.geoapi.ValidatorContainer;
import org.iogp.gigs.internal.geoapi.Configuration;


/**
 * A copy of the {@code org.opengis.test.TestCase} class.
 * That base class is temporarily copied for allowing us to make evolution
 * such as migration to JUnit 5 without waiting for next GeoAPI release.
 *
 * <p>This class may be deleted (replaced by the GeoAPI class) in a future version.</p>
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @version 1.0
 * @since   1.0
 */
abstract class ConformanceTest {
    /**
     * Provider of units of measurement (degree, metre, second, <i>etc</i>), never {@code null}.
     * The {@link Units#degree()}, {@link Units#metre() metre()} and other methods shall return
     * {@link javax.measure.Unit} instances compatible with the units created by the {@link Factory}
     * instances to be tested. Those {@code Unit<?>} instances depend on the Unit of Measurement (JSR-373)
     * implementation used by the factories.
     */
    final Units units;

    /**
     * The set of {@link Validator} instances to use for verifying objects conformance (never {@code null}).
     */
    final ValidatorContainer validators;

    /**
     * A tip set by subclasses during the execution of some optional tests.
     * In case of optional test failure, if this field is non-null, then a message will be logged at the
     * {@link java.util.logging.Level#INFO} for giving some tips to the developer about how he can disable the test.
     *
     * <p><b>Example</b></p>
     * {@snippet lang="java" :
     *     @Test
     *     public void myTest() {
     *         if (isDerivativeSupported) {
     *             configurationTip = Configuration.Key.isDerivativeSupported;
     *             // Do some tests the require support of math transform derivatives.
     *         }
     *         configurationTip = null;
     *     }
     * }
     */
    transient Configuration.Key<Boolean> configurationTip;

    /**
     * Creates a new test.
     */
    ConformanceTest() {
        units = Units.getInstance();
        validators = ValidatorContainer.DEFAULT;
    }

    /**
     * Returns information about the configuration of the test which has been run.
     * The content of this map depends on the {@code TestCase} subclass.
     *
     * @return the configuration of the test being run, or an empty map if none.
     *         This method returns a modifiable map in order to allow subclasses to modify it.
     */
    Configuration configuration() {
        final Configuration configuration = new Configuration();
        configuration.put(Configuration.Key.units,      units.system);
        configuration.put(Configuration.Key.validators, validators);
        return configuration;
    }
}
