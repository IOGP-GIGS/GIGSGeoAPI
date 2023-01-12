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

import java.io.File;
import java.io.IOException;
import java.util.prefs.Preferences;
import java.util.concurrent.ExecutionException;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FileDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingWorker;
import javax.swing.JOptionPane;
import javax.swing.JSplitPane;

import org.iogp.gigs.internal.TestSuite;


/**
 * The main frame of the test runner.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @version 1.0
 * @since   1.0
 */
final class MainFrame implements Runnable {
    /**
     * The preference key for the directory in which to select JAR files.
     */
    private static final String JAR_DIRECTORY_KEY = "jar.directory";

    /**
     * The main frame.
     */
    private final JFrame frame;

    /**
     * Labels used for rendering information from {@link ImplementationManifest}.
     *
     * @see #setManifest(ImplementationManifest)
     */
    private final JLabel title, vendor, version, specification, specVersion, specVendor;

    /**
     * The tree where to show test results.
     */
    private final ResultsView results;

    /**
     * The object to use for running the tests.
     * Created after user selected the implementation to test.
     * This is used for running tests again if requested.
     */
    private Runner runner;

    /**
     * Where to save the last user choices, for the next run.
     */
    private final Preferences preferences;

    /**
     * Creates a new frame with all tabs.
     * There is no menu for this application.
     *
     * @param  windowTitle  the window title.
     */
    @SuppressWarnings("ThisEscapedInObjectConstruction")
    MainFrame(final String windowTitle) {
        frame = new JFrame(windowTitle);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(400, 400));
        frame.setSize(800, 800);
        frame.setLocationByPlatform(true);
        preferences = Preferences.userNodeForPackage(org.iogp.gigs.IntegrityTest.class);
        /*
         * The top panel, which show a description of the product being tested
         * (vendor name, URL, etc). This panel will be visible from every tabs.
         */
        frame.add(new SwingPanelBuilder().createManifestPane(
                title         = new JLabel(),
                version       = new JLabel(),
                vendor        = new JLabel(),
                specification = new JLabel(),
                specVersion   = new JLabel(),
                specVendor    = new JLabel()), BorderLayout.NORTH);
        /*
         * The main panel, which will contain panes. The top pane shows results of all tests.
         * Bottom pane shows more information about test failures or about features supported
         * by the application being tested.
         */
        final TestDetails details = new TestDetails();
        results = new ResultsView(details);
        final JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setOneTouchExpandable(true);
        splitPane.setContinuousLayout(true);
        splitPane.setDividerLocation(300);
        splitPane.setResizeWeight(1);
        splitPane.setTopComponent(results.createView());
        splitPane.setBottomComponent(details.createView());
        frame.add(splitPane, BorderLayout.CENTER);
    }

    /**
     * Shows the application.
     */
    final void show() {
        frame.setVisible(true);
        EventQueue.invokeLater(this);
    }

    /**
     * Opens the file chooser dialog box for selecting JAR files. This method remember the
     * directory selected by the user last time this method was executed. This method is
     * invoked from the {@link Main} class.
     */
    @Override
    public void run() {
        final String directory = preferences.get(JAR_DIRECTORY_KEY, null);
        final FileDialog chooser = new FileDialog(frame, "Select a GeoAPI implementation");
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
     * The worker for loading JAR files in background.
     * This worker return the runner used for executing the tests.
     */
    private final class Loader extends SwingWorker<Runner,Object> {
        /**
         * The JAR files.
         */
        private final File[] files;

        /**
         * Creates a new worker which will load the given JAR files.
         *
         * @param  files  the JAR files.
         */
        Loader(final File[] files) {
            this.files = files;
        }

        /**
         * Loads the given JAR files and creates a class loader for running the tests.
         * This method is invoked in a background thread and is invoked only once.
         *
         * @return the runner used for executing the tests.
         */
        @Override
        protected Runner doInBackground() throws IOException {
            final ImplementationManifest manifest = ImplementationManifest.parse(files);
            EventQueue.invokeLater(() -> setManifest(manifest));
            final File[] implementation = (manifest != null) ? manifest.dependencies : files;
            final Runner runner = new Runner(new TestSuite(), implementation, results);
            runner.executeAll();
            return runner;
        }

        /**
         * Invoked from the Swing thread when the task is over of failed.
         */
        @Override
        protected void done() {
            try {
                runner = get();
            } catch (InterruptedException e) {
                // Should not happen at this point.
            } catch (ExecutionException e) {
                String message = e.getCause().toString().replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
                message = "<html><body><p style='width: 600px;'>An error occurred while processing the JAR files:</p>"
                        + "<p style='width: 600px;'>" + message + "</p></body></html>";
                JOptionPane.showMessageDialog(frame, message,
                        "Can not use the JAR files", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
