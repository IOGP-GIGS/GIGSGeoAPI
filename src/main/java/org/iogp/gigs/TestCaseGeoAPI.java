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

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.opengis.util.Factory;
import org.iogp.gigs.internal.geoapi.Units;
import org.iogp.gigs.internal.geoapi.Validator;
import org.iogp.gigs.internal.geoapi.ValidatorContainer;
import org.iogp.gigs.internal.geoapi.Configuration;
import org.iogp.gigs.internal.geoapi.TestListener;
import org.iogp.gigs.internal.geoapi.TestEvent;
import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;


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
abstract class TestCaseGeoAPI {
    /**
     * The list of tests that are enabled.
     * This is a static field for now but will become configurable in a future version.
     */
    private static final Configuration config = new Configuration();

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
     * <blockquote><pre>&#64;Test
     *public void myTest() {
     *    if (isDerivativeSupported) {
     *        configurationTip = Configuration.Key.isDerivativeSupported;
     *        // Do some tests the require support of math transform derivatives.
     *    }
     *    configurationTip = null;
     *}</pre></blockquote>
     */
    transient Configuration.Key<Boolean> configurationTip;

    /**
     * Creates a new test.
     */
    TestCaseGeoAPI() {
        units = Units.getInstance();
        validators = ValidatorContainer.DEFAULT;
    }

    /**
     * Returns booleans indicating whether the given operations are enabled.
     *
     * @param  properties  the key for which the flags are wanted.
     * @return an array of the same length than {@code properties} in which each element at index
     *         <var>i</var> indicates whether the {@code properties[i]} test should be enabled.
     */
    @SafeVarargs
    final boolean[] getEnabledFlags(final Configuration.Key<Boolean>... properties) {
        final boolean[] isEnabled = new boolean[properties.length];
        for (int i=0; i<properties.length; i++) {
            final Boolean value = config.get(properties[i]);
            isEnabled[i] = (value == null) || value;
        }
        return isEnabled;
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
        configuration.put(Configuration.Key.units,      units);
        configuration.put(Configuration.Key.validators, validators);
        return configuration;
    }

    /**
     * A JUnit {@linkplain Rule rule} for listening to test execution events.
     *
     * <p>This field is public because JUnit requires us to do so, but should be considered
     * as an implementation details (it should have been a private field).</p>
     *
     * @deprecated To be replaced by JUnit 5 listener mechanism.
     */
    @Rule
    @Deprecated
    public final TestWatcher listener = new TestWatcher() {
        /**
         * A snapshot of the test listeners. We make this snapshot at rule creation time
         * in order to be sure that the same set of listeners is notified for all phases
         * of the test method being run.
         */
        private final TestListener[] listeners = TestListener.getTestListeners();

        /**
         * Invoked when a test is about to start.
         */
        @Override
        protected void starting(final Description description) {
            final TestEvent event = new TestEvent(TestCaseGeoAPI.this, description);
            for (final TestListener listener : listeners) {
                listener.starting(event);
            }
        }

        /**
         * Invoked when a test succeeds.
         */
        @Override
        protected void succeeded(final Description description) {
            final TestEvent event = new TestEvent(TestCaseGeoAPI.this, description);
            for (final TestListener listener : listeners) {
                listener.succeeded(event);
            }
        }

        /**
         * Invoked when a test fails. If the failure occurred in an optional part of the test,
         * logs an information message for helping the developer to disable that test if (s)he wishes.
         */
        @Override
        protected void failed(final Throwable exception, final Description description) {
            final TestEvent event = new TestEvent(TestCaseGeoAPI.this, description);
            final Configuration.Key<Boolean> tip = configurationTip;
            if (tip != null) {
                event.configurationTip = tip;
                final Logger logger = Logger.getLogger("org.iogp.gigs");
                final LogRecord record = new LogRecord(Level.INFO, "A test failure occurred while "
                        + "testing an optional feature. To skip that part of the test, set the '"
                        + tip.name() + "' boolean field to false or specify that value in the "
                        + "Configuration map.");
                record.setLoggerName(logger.getName());
                record.setSourceClassName(event.className);
                record.setSourceMethodName(event.methodName);
                logger.log(record);
            }
            for (final TestListener listener : listeners) {
                listener.failed(event, exception);
            }
        }

        /**
         * Invoked when a test method finishes (whether passing or failing)
         */
        @Override
        protected void finished(final Description description) {
            final TestEvent event = new TestEvent(TestCaseGeoAPI.this, description);
            for (final TestListener listener : listeners) {
                listener.finished(event);
            }
        }
    };
}
