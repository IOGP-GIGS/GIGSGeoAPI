/*
 *    GeoAPI - Java interfaces for OGC/ISO standards
 *    http://www.geoapi.org
 *
 *    Copyright (C) 2023 Open Geospatial Consortium, Inc.
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

import java.util.Map;
import java.util.TreeMap;
import java.util.Comparator;
import java.awt.Component;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import org.iogp.gigs.internal.PrivateAccessor;


/**
 * An action showing in a modal dialog box the configuration of all tests.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @version 1.0
 * @since   1.0
 */
final class AllConfigurations implements ActionListener, Comparator<Object> {
    /**
     * Creates a new action.
     */
    AllConfigurations() {
    }

    /**
     * Invoked when the user requested to see the configurations.
     *
     * @param  event  ignored.
     */
    @Override
    public void actionPerformed(final ActionEvent event) {
        final Frame parent = (Frame) SwingUtilities.getWindowAncestor((Component) event.getSource());
        final JTextArea text = new JTextArea(getTestConfigurations());
        text.setEditable(false);
        text.setFont(Font.decode("Monospaced"));
        final JDialog dialog = new JDialog(parent, "Configuration of tests", true);
        dialog.add(new JScrollPane(text));
        dialog.setSize(800, 600);
        dialog.setVisible(true);
    }

    /**
     * {@return the configuration of tests as a text.}
     */
    private String getTestConfigurations() {
        final String lineSeparator = System.lineSeparator();
        final StringBuilder text = new StringBuilder(1000);
        text.append("# Configuration file for Geospatial Integrity of Geoscience Software (GIGS) tests").append(lineSeparator)
            .append("# https://gigs.iogp.org/").append(lineSeparator)
            .append('#').append(lineSeparator)
            .append("# This file disables some GIGS tests that are known to fail.").append(lineSeparator)
            .append(lineSeparator);

        final var properties = new TreeMap<>(this);
        properties.putAll(PrivateAccessor.INSTANCE.getTestConfigurations());
        for (final Map.Entry<Object,Object> entry : properties.entrySet()) {
            text.append(entry.getKey()).append('=').append(entry.getValue()).append(lineSeparator);
        }
        return text.toString();
    }

    /**
     * Compares two keys for order.
     *
     * @param  k1  the first key to compare.
     * @param  k2  the second key to compare.
     * @return the order (negative, 0 or positive).
     */
    @Override
    public int compare(final Object k1, final Object k2) {
        return k1.toString().compareTo(k2.toString());
    }
}
