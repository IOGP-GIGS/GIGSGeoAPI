/*
 *    GeoAPI - Java interfaces for OGC/ISO standards
 *    http://www.geoapi.org
 *
 *    Copyright (C) 2011-2022 Open Geospatial Consortium, Inc.
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
package org.iogp.gigs.runner;

import org.iogp.gigs.internal.geoapi.Configuration;


/**
 * Information (including enabled status) of an <em>aspect</em> of a test.
 * An aspect is an optional feature, for example the aliases of a CRS
 * or the capability of a math transform to compute Jacobian matrices.
 * The optional aspect to test are configured by {@link Configuration.Key}s.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @version 1.0
 * @since   1.0
 */
final class TestAspect {
    /**
     * The test which has been executed, together with the overall test result.
     */
    private final ResultEntry test;

    /**
     * Aspect of the test described by this {@code TestAspect}.
     */
    private final Configuration.Key<?> aspect;

    /**
     * The enabled/disabled/failed status of the aspect of the test.
     */
    private Status status;

    /**
     * Whether an optional aspect of a test case is enabled,
     * and if yes whether the test was successful.
     */
    enum Status {
        /**
         * The test aspect is enabled and has been executed successfully.
         */
        ENABLED,

        /**
         * The test aspect is disabled.
         */
        DISABLED,

        /**
         * The test aspect is enabled and the verification failed.
         */
        FAILED
    }

    /**
     * Creates a new instance describing an aspect of the specified test.
     *
     * @param test    the test which has been executed, together with the overall test result.
     * @param aspect  an aspect of the test described by this {@code TestAspect}.
     * @param status  the enabled/disabled/failed status of the aspect of the test.
     */
    TestAspect(final ResultEntry test, final Configuration.Key<?> aspect, Status status) {
        this.test   = test;
        this.aspect = aspect;
        this.status = status;
    }

    /**
     * Returns the name to show for this aspect.
     *
     * @return the name to show in the widget.
     */
    String name() {
        return aspect.name();
    }

    /**
     * Returns {@code true} if this aspect is modifiable.
     * Current implementation allows to edit only boolean values.
     *
     * @return whether this aspect is modifiable.
     */
    boolean isEditable() {
        return Boolean.class.equals(aspect.valueType());
    }

    /**
     * Returns {@code true} if this aspect uses the same configuration key than the specified aspect.
     *
     * @param  other  the other aspect to compare to.
     * @return whether the two aspect uses the same configuration key.
     */
    boolean useSameConfigurationKey(final TestAspect other) {
        return aspect.equals(other.aspect);
    }

    /**
     * Returns the status of this test aspect.
     *
     * @return the status to show in the widget.
     */
    Status status() {
        return status;
    }

    /**
     * Runs again the test with a new enabled or disabled status for this test aspect.
     *
     * @param  runner   the runner to use for re-executing the test.
     * @param  enabled  whether to enable this aspect of a test.
     */
    @SuppressWarnings("unchecked")
    void execute(final boolean enabled) {
        status = enabled ? TestAspect.Status.ENABLED : TestAspect.Status.DISABLED;
        test.setAspectAndExecute(aspect.cast(Boolean.class), enabled);
    }
}
