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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.prefs.Preferences;
import java.util.concurrent.ExecutionException;

import java.awt.Desktop;
import java.awt.BorderLayout;
import java.awt.FileDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.SwingWorker;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.tree.TreePath;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import org.iogp.gigs.internal.TestSuite;


/**
 * The main frame of the test runner.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @version 1.0
 * @since   1.0
 */
@SuppressWarnings("serial")
final class MainFrame extends JFrame implements Runnable, ActionListener, TreeSelectionListener {
    /**
     * The preference key for the directory in which to select JAR files.
     */
    private static final String JAR_DIRECTORY_KEY = "jar.directory";

    /**
     * The desktop for browse operations, or {@code null} if unsupported.
     */
    private final Desktop desktop;

    /**
     * Labels used for rendering information from {@link ImplementationManifest}.
     *
     * @see #setManifest(ImplementationManifest)
     */
    private final JLabel title, vendor, version, specification, specVersion, specVendor;

    /**
     * Labels used for rendering information about the selected test.
     *
     * @see #setDetails(ResultEntry)
     */
    private final JLabel testName;

    /**
     * Labels used for rendering details about the test result.
     */
    private final JLabel testResult;

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
     * The object to use for running the tests.
     */
    private final Runner runner;

    /**
     * The test report which is currently shown in the "details" tab, or {@code null} if none.
     */
    private ResultEntry currentReport;

    /**
     * Where to save the last user choices, for the next run.
     */
    private final Preferences preferences;

    /**
     * Creates a new frame, which contains all our JUnit runner tabs.
     * There is no menu for this application.
     */
    @SuppressWarnings("ThisEscapedInObjectConstruction")
    MainFrame() {
        super("GIGS tests");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(800, 800);
        setLocationByPlatform(true);
        runner      = new Runner();
        desktop     = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        preferences = Preferences.userNodeForPackage(org.iogp.gigs.IntegrityTest.class);
        /*
         * The top panel, which show a description of the product being tested
         * (vendor name, URL, etc). This panel will be visible from every tabs.
         */
        add(new SwingPanelBuilder().createManifestPane(
                title         = new JLabel(),
                version       = new JLabel(),
                vendor        = new JLabel(),
                specification = new JLabel(),
                specVersion   = new JLabel(),
                specVendor    = new JLabel()), BorderLayout.NORTH);
        /*
         * The main panel, which will contain panes. The first pane shows the test results.
         * Next pane shows more information about test failures or about features supported
         * by the application being tested.
         */
        final JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setOneTouchExpandable(true);
        splitPane.setContinuousLayout(true);
        splitPane.setDividerLocation(300);
        splitPane.setResizeWeight(1);
        add(splitPane, BorderLayout.CENTER);
        /*
         * The main tab, showing the JUnit test results in a tree.
         */
        {   // For keeping variables in a local scope.
            final JTree tree = runner.tree;
            final JScrollPane pane = new JScrollPane(tree);
            tree.setCellRenderer(new ResultCellRenderer(pane));
            tree.getSelectionModel().addTreeSelectionListener(this);
            splitPane.setTopComponent(pane);
        }
        /*
         * A tab showing more information about a failed tests (for example the stack trace),
         * together with some information about the configuration.
         */
        final JButton viewJavadoc = new JButton("Online documentation");
        viewJavadoc.setEnabled(desktop != null && desktop.isSupported(Desktop.Action.BROWSE));
        viewJavadoc.setToolTipText("View javadoc for this test");
        viewJavadoc.addActionListener(this);
        splitPane.setBottomComponent(new SwingPanelBuilder().createDetailsPane(
                testName = new JLabel(), testResult = new JLabel(), viewJavadoc,
                new JTable(factories = new FactoryTableModel()),
                new JTable(configuration = new ConfigurationTableModel()),
                exception = new JTextArea()));
    }

    /**
     * Opens the file chooser dialog box for selecting JAR files. This method remember the
     * directory selected by the user last time this method was executed. This method is
     * invoked from the {@link Main} class.
     */
    @Override
    public void run() {
        final String directory = preferences.get(JAR_DIRECTORY_KEY, null);
        final FileDialog chooser = new FileDialog(this, "Select a GeoAPI implementation");
        chooser.setDirectory(directory);
        chooser.setFilenameFilter((file, name) -> name.endsWith(".jar"));
        chooser.setMultipleMode(true);
        chooser.setVisible(true);
        final File[] files = chooser.getFiles();
        if (files.length != 0) {
            preferences.put(JAR_DIRECTORY_KEY, chooser.getDirectory());
            new Loader(files).execute();
        }
    }

    /**
     * Sets the implementation identification.
     * This method is invoked in the Swing thread.
     *
     * @param  manifest  information about the library being tested.
     */
    private void setManifest(final ImplementationManifest manifest) {
        title        .setText(manifest != null ? manifest.title         : null);
        version      .setText(manifest != null ? manifest.version       : null);
        vendor       .setText(manifest != null ? manifest.vendor        : null);
        specification.setText(manifest != null ? manifest.specification : null);
        specVersion  .setText(manifest != null ? manifest.specVersion   : null);
        specVendor   .setText(manifest != null ? manifest.specVendor    : null);
    }

    /**
     * Updates the content of the "Details" pane with information relative to the given entry.
     * A {@code null} entry clears the "Details" pane.
     *
     * @param  entry  description of test result.
     */
    private void setDetails(final ResultEntry entry) {
        String progName   = null;
        String stacktrace = null;
        String result     = null;
        if (entry == null) {
            factories.entries     = Collections.emptyList();
            configuration.entries = Collections.emptyList();
        } else {
            result   = entry.result();
            progName = entry.programmaticName();
            switch (entry.result.getStatus()) {
                case FAILED: {
                    final Throwable exception = entry.result.getThrowable().orElse(null);
                    if (exception != null) {
                        final StringWriter buffer = new StringWriter();
                        final PrintWriter printer = new PrintWriter(buffer);
                        exception.printStackTrace(printer);
                        printer.flush();
                        stacktrace = buffer.toString();
                    }
                    break;
                }
            }
            factories.entries     = entry.factories;
            configuration.entries = entry.configuration;
        }
        factories    .fireTableDataChanged();
        configuration.fireTableDataChanged();
        testName     .setText(progName);
        testResult   .setText(result);
        exception    .setText(stacktrace);
        exception    .setCaretPosition(0);
        currentReport = entry;
    }

    /**
     * Invoked when the user clicked on a new row in the tree showing test results.
     * This method updates the "Details" tab with information relative to the test
     * in the selected row.
     *
     * @param  event  the event that characterizes the change.
     */
    @Override
    public void valueChanged(final TreeSelectionEvent event) {
        ResultEntry entry = null;
        final TreePath path = event.getNewLeadSelectionPath();
        if (path != null) {
            final DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
            final Object object = node.getUserObject();
            if (object instanceof ResultEntry) {
                entry = (ResultEntry) object;
            }
        }
        setDetails(entry);
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
                    JOptionPane.showMessageDialog(MainFrame.this, e.toString(),
                            "Can not open the browser", JOptionPane.ERROR_MESSAGE);
                }
            });
        }
    }

    /**
     * The worker for loading JAR files in background.
     */
    private final class Loader extends SwingWorker<Object,Object> {
        /**
         * The JAR files.
         */
        private final File[] files;

        /**
         * Creates a new worker which will loads the given JAR files.
         *
         * @param  files  the JAR files.
         */
        Loader(final File[] files) {
            this.files = files;
        }

        /**
         * Loads the given JAR files and creates a class loader for running the tests.
         *
         * @return {@code null} (ignored).
         */
        @Override
        protected Object doInBackground() throws IOException {
            final ImplementationManifest manifest = ImplementationManifest.parse(files);
            setManifest(manifest);
            TestSuite.INSTANCE.run(runner, manifest != null ? manifest.dependencies : files);
            return null;
        }

        /**
         * Invoked from the Swing thread when the task is over of failed.
         */
        @Override
        protected void done() {
            try {
                get();
            } catch (InterruptedException e) {
                // Should not happen at this point.
            } catch (ExecutionException e) {
                String message = e.getCause().toString().replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
                message = "<html><body><p style='width: 600px;'>An error occurred while processing the JAR files:</p>"
                        + "<p style='width: 600px;'>" + message + "</p></body></html>";
                JOptionPane.showMessageDialog(MainFrame.this, message,
                        "Can not use the JAR files", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
