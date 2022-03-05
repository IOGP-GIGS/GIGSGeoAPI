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

import java.util.EventObject;
import java.lang.reflect.Method;
import org.iogp.gigs.IntegrityTest;
import org.junit.runner.Description;


/**
 * Events provided to {@linkplain TestListener test listeners} when a test begin, complete or fail.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @version 1.0
 * @since   1.0
 *
 * @deprecated To be replaced by JUnit 5 listener mechanism.
 */
@Deprecated
public final class TestEvent extends EventObject {
    /**
     * For cross-version compatibility.
     */
    private static final long serialVersionUID = -3409373706089551108L;

    /**
     * The fully-qualified name of the class which contain the test method being executed.
     */
    public final String className;

    /**
     * The name of the test method which is executed, followed by a sequential number.
     * The sequential number is used when the same test method has been executed many time
     * because more than one {@linkplain org.opengis.util.Factory factory} has been found on
     * the classpath.
     */
    public final String methodName;

    /**
     * If a test failure occurred in an optional test, the configuration key for disabling
     * that test. Otherwise {@code null}.
     */
    public Configuration.Key<Boolean> configurationTip;

    /**
     * Creates a new event for the given source.
     *
     * @param  source  the {@link IntegrityTest} which is the source of this event.
     * @param  description  a description of the test event.
     */
    public TestEvent(final Object source, final Description description) {
        super(source);
        className  = description.getClassName();
        methodName = description.getMethodName();
    }

    /**
     * Returns the configuration associated to the source.
     * We use reflection for avoiding to put the configuration in public API (for now).
     */
    public final Configuration configuration() {
        try {
            Method m = IntegrityTest.class.getSuperclass().getDeclaredMethod("configuration");
            m.setAccessible(true);
            return (Configuration) m.invoke(getSource());
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);                    // Should never happen.
        }
    }
}
