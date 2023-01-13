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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.awt.Desktop;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;


/**
 * The pane showing details about a single test selected by the user.
 * Contains table for describing the configuration (options, factories)
 * and the stack trace if the test failed.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @version 1.0
 * @since   1.0
 */
final class TestDetails implements ActionListener {
    /**
     * The test report which is currently shown in the "details" tab, or {@code null} if none.
     */
    private ResultEntry currentReport;

    /**
     * Labels used for rendering information about the selected test.
     *
     * @see #setTest(ResultEntry, ResultEntry)
     */
    private final JLabel testName;

    /**
     * Labels used for rendering details about the test result.
     */
    private final JLabel testResult;

    /**
     * Labels used for rendering a tip about a configuration that may be applied
     * for allowing a failed test to pass.
     */
    private final JLabel configurationTip;

    /**
     * The factories used for the test case, to be reported in the "details" tab.
     */
    private final FactoryTableModel factories;

    /**
     * The configuration specified by the implementer for the test case,
     * to be reported in the "details" tab.
     */
    private final ConfigurationTableModel configuration;

    /**
     * Where to report stack trace.
     */
    private final JTextArea exception;

    /**
     * The desktop for browse operations, or {@code null} if unsupported.
     */
    private final Desktop desktop;

    /**
     * Constructs a new details pane.
     */
    TestDetails() {
        testName         = new JLabel();
        testResult       = new JLabel();
        configurationTip = new JLabel();
        configuration    = new ConfigurationTableModel();
        factories        = new FactoryTableModel();
        exception        = new JTextArea();
        desktop          = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
    }

    /**
     * Creates the Swing component for viewing the test details.
     *
     * @return the Swing view for details about a selected test.
     */
    final Component createView() {
        final JButton viewJavadoc = new JButton("Online documentation");
        viewJavadoc.setEnabled(desktop != null && desktop.isSupported(Desktop.Action.BROWSE));
        viewJavadoc.setToolTipText("View javadoc for this test");
        viewJavadoc.addActionListener(this);
        return new SwingPanelBuilder().createDetailsPane(testName, testResult, configurationTip,
                viewJavadoc, factories.createView(), configuration.createView(), exception);
    }

    /**
     * Updates the content of the "Details" pane with information relative to the given entry.
     * If the {@code replace} entry is non-null, then this method will set the entry only if
     * it replaces the specified previous entry.
     *
     * @param  replace  the previous entry to replace, or {@code null} for unconditional new entry.
     * @param  entry    description of test result, or {@code null} for clearing the "Details" pane.
     */
    final void setTest(final ResultEntry replace, final ResultEntry entry) {
        if (replace != null && replace != currentReport) {
            return;
        }
        final int numUpdatedRows;       // -1 for all rows.
        String progName   = null;
        String stacktrace = null;
        String result     = null;
        String tip        = null;
        if (entry == null) {
            factories.entries     = Collections.emptyList();
            configuration.entries = Collections.emptyList();
            numUpdatedRows        = -1;
        } else {
            result   = entry.getResultText();
            progName = entry.getProgrammaticName();
            tip      = entry.getConfigurationTip();
            switch (entry.result.getStatus()) {
                case FAILED: {
                    final Throwable ex = entry.result.getThrowable().orElse(null);
                    if (ex != null) {
                        final StringWriter buffer = new StringWriter();
                        final PrintWriter printer = new PrintWriter(buffer);
                        ex.printStackTrace(printer);
                        printer.flush();
                        stacktrace = buffer.toString();
                    }
                    break;
                }
            }
            factories.entries     = entry.factories;
            configuration.entries = entry.configuration;
            numUpdatedRows = entry.useSameConfigurationKeys(replace) ? configuration.getRowCount() - 1 : -1;
        }
        factories.fireTableDataChanged();
        if (numUpdatedRows < 0) {
            configuration.fireTableDataChanged();
        } else {
            // Avoid `fireTableDataChanged()` in order to preserve row selection status.
            configuration.fireTableRowsUpdated(0, numUpdatedRows);
        }
        configurationTip.setText(tip);
        testName        .setText(progName);
        testResult      .setText(toHTML(result));
        exception       .setText(stacktrace);
        exception       .setCaretPosition(0);
        exception       .setEnabled(stacktrace != null);
        currentReport = entry;
    }

    /**
     * Converts the given text to HTML
     *
     * @param  text  the text to convert. Can be null.
     * @return the converted text, or null if the given text was null.
     */
    private static String toHTML(String text) {
        if (text == null) {
            return null;
        }
        text = text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
        return "<html>" + text + "</html>";
    }

    /**
     * Invoked when the user pressed the "View javadoc" button.
     *
     * @param  event  ignored.
     */
    @Override
    public void actionPerformed(final ActionEvent event) {
        if (currentReport != null) {
            currentReport.getJavadocURL().ifPresent((uri) -> {
                try {
                    desktop.browse(uri);
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(exception, e.toString(),
                            "Can not open the browser", JOptionPane.ERROR_MESSAGE);
                }
            });
        }
    }
}
