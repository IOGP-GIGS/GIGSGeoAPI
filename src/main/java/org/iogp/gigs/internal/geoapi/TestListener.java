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

import java.util.Arrays;
import java.util.EventListener;


/**
 * A listener which is notified when a test begin, complete or fail.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @version 1.0
 * @since   1.0
 *
 * @deprecated To be replaced by JUnit 5 listener mechanism.
 */
@Deprecated
public abstract class TestListener implements EventListener {
    /**
     * The test listeners. We intentionally copy the full array every time a listener is
     * added or removed. We do not clone the array used by the {@link #listener} field,
     * so it is important that any array instance is never modified after creation.
     *
     * @see #addTestListener(TestListener)
     * @see #removeTestListener(TestListener)
     * @see #getTestListeners()
     */
    private static TestListener[] listeners = new TestListener[0];

    /**
     * Creates a new listener.
     */
    protected TestListener() {
    }

    /**
     * Adds a listener to be informed every time a test begin or finish, either on success
     * or failure. This method does not check if the given listener was already registered
     * (i.e. the same listener may be added more than once).
     *
     * @param listener The listener to add. {@code null} values are silently ignored.
     *
     * @deprecated To be replaced by JUnit 5 listener mechanism.
     */
    @Deprecated
    public static synchronized void addTestListener(final TestListener listener) {
        if (listener != null) {
            final int length = listeners.length;
            listeners = Arrays.copyOf(listeners, length + 1);
            listeners[length] = listener;
        }
    }

    /**
     * Removes a previously {@linkplain #addTestListener(TestListener) added} listener. If the
     * given listener has been added more than once, then only the last occurrence is removed.
     * If the given listener is not found, then this method does nothing.
     *
     * @param listener  the listener to remove. {@code null} values are silently ignored.
     *
     * @deprecated To be replaced by JUnit 5 listener mechanism.
     */
    @Deprecated
    public static synchronized void removeTestListener(final TestListener listener) {
        for (int i=listeners.length; --i>=0;) {
            if (listeners[i] == listener) {
                final int length = listeners.length - 1;
                System.arraycopy(listeners, i, listeners, i+1, length-i);
                listeners = Arrays.copyOf(listeners, length);
                break;
            }
        }
    }

    /**
     * Returns all currently registered test listeners, or an empty array if none.
     * This method returns directly the internal array, so it is important to never modify it.
     * This method is for internal usage only.
     */
    @SuppressWarnings("ReturnOfCollectionOrArrayField")
    public static synchronized TestListener[] getTestListeners() {
        return listeners;
    }

    /**
     * Invoked when a test is about to start.
     *
     * @param event  a description of the test which is about to be run.
     */
    public abstract void starting(TestEvent event);

    /**
     * Invoked when a test succeeds.
     *
     * @param event  a description of the test which has been run.
     */
    public abstract void succeeded(TestEvent event);

    /**
     * Invoked when a test fails.
     *
     * @param event      a description of the test which has been run.
     * @param exception  the exception that occurred during the execution.
     */
    public abstract void failed(TestEvent event, Throwable exception);

    /**
     * Invoked when a test method finishes (whether passing or failing).
     *
     * @param event  a description of the test which has been run.
     */
    public abstract void finished(TestEvent event);
}
