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
package org.iogp.gigs.runner;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.TestExecutionListener;


/**
 * Provides methods for running the tests.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @version 1.0
 * @since   1.0
 */
final class Runner implements TestExecutionListener {
    /**
     * The result of each tests. All a access to this list must be synchronized.
     */
    private final List<ResultEntry> entries;

    /**
     * The listeners to inform of any new entry. Note that those listeners will
     * <strong>not</strong> be notified from the Swing thread. It is listener
     * responsibility to be safe regarding the Swing events queue.
     */
    private ChangeListener[] listeners;

    /**
     * The single change event to reuse every time an event is fired.
     */
    private final ChangeEvent event;

    /**
     * Creates a new, initially empty, runner.
     */
    Runner() {
        entries   = new ArrayList<>();
        listeners = new ChangeListener[0];
        event     = new ChangeEvent(this);
    }

    /**
     * Returns all entries. This method returns a copy of the internal array.
     * Changes to this {@code ReportData} object will not be reflected in that array.
     */
    ResultEntry[] getEntries() {
        synchronized (entries) {
            return entries.toArray(new ResultEntry[entries.size()]);
        }
    }

    /**
     * Called when a test finished, successfully or not.
     */
    @Override
    public void executionFinished​(final TestIdentifier identifier, final TestExecutionResult result) {
        final ResultEntry entry = new ResultEntry(identifier, result);
        final ChangeListener[] list;
        synchronized (entries) {
            entries.add(entry);
            list = listeners;
        }
        for (final ChangeListener listener : list) {
            listener.stateChanged(event);
        }
    }

    /**
     * Adds a change listener to be invoked when new entries are added.
     * This is of interest mostly to swing widgets - we don't use this
     * listener for collecting new information.
     *
     * <p>Note that the listeners given to this method will <strong>not</strong> be notified from the
     * Swing thread. It is listener responsibility to be safe regarding the Swing events queue.</p>
     */
    final void addChangeListener(final ChangeListener listener) {
        synchronized (entries) {
            ChangeListener[] list = listeners;
            final int length = list.length;
            list = Arrays.copyOf(list, length + 1);
            list[length] = listener;
            listeners = list;
        }
    }
}
