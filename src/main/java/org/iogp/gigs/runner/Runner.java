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

import java.util.Set;
import java.util.HashSet;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.io.IOException;
import java.lang.module.Configuration;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReference;
import javax.swing.SwingWorker;

import org.iogp.gigs.internal.TestSuite;
import org.iogp.gigs.internal.ExecutionContext;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.discovery.ClassSelector;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.core.LauncherFactory;


/**
 * Provides methods for running the tests,
 * together with a view of the results as a Swing tree.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @version 1.0
 * @since   1.0
 */
final class Runner implements TestExecutionListener {
    /**
     * Set of classes containing the tests to execute.
     * A test suite is independent of the implementation to test.
     */
    private final TestSuite suite;

    /**
     * The module layer to use for loading the implementation classes.
     */
    private final ModuleLayer layer;

    /**
     * The JUnit object to use for running tests.
     */
    private final Launcher launcher;

    /**
     * Where the test results are shown.
     */
    private final ResultsView destination;

    /**
     * Creates a new runner.
     *
     * @param  suite           set of classes containing the tests to execute.
     * @param  implementation  all JAR files required by the implementation to test.
     * @param  destination     where to show the test results.
     * @throws IOException if an implementation JAR file cannot be parsed.
     */
    @SuppressWarnings("ThisEscapedInObjectConstruction")
    Runner(final TestSuite suite, final Path[] implementation, final ModuleFinder modules, final ResultsView destination) throws IOException {
        this.suite = suite;
        this.destination = destination;
        final URL[] urls = new URL[implementation.length];
        for (int i=0; i < urls.length; i++) {
            urls[i] = implementation[i].toUri().toURL();
        }
        final ClassLoader loader;
        final ModuleLayer parent;
        final Configuration config;
        loader   = new URLClassLoader(urls, Runner.class.getClassLoader());
        parent   = Runner.class.getModule().getLayer();
        config   = parent.configuration().resolveAndBind(ModuleFinder.of(), modules, getAllModuleNames(modules));
        layer    = parent.defineModulesWithOneLoader(config, loader);
        launcher = LauncherFactory.create();
        launcher.registerTestExecutionListeners(this);
    }

    /**
     * Returns the names of all modules that the given finder can see.
     */
    private static Set<String> getAllModuleNames(final ModuleFinder modules) {
        final var names = new HashSet<String>();
        for (ModuleReference ref : modules.findAll()) {
            names.add(ref.descriptor().name());
        }
        return names;
    }

    /**
     * Executes all tests and shows the result in the destination {@code ResultsView}.
     * This method should be invoked in a background thread.
     */
    final void executeAll() {
        final Class<?>[] tests = suite.getTestClasses();
        final ClassSelector[] selectors = new ClassSelector[tests.length];
        for (int i=0; i<selectors.length; i++) {
            selectors[i] = DiscoverySelectors.selectClass(tests[i]);
        }
        ExecutionContext.INSTANCE.execute(layer, launcher, selectors);
    }

    /**
     * Executes a single test in a background thread.
     * This method can be invoked in the Swing thread.
     *
     * @param  test  the test to execute.
     */
    final void execute(final MethodSource test) {
        new SwingWorker<Object,Object>() {
            /**
             * Invoked in a background thread for running the single test.
             * The {@link #executionFinished(TestIdentifier, TestExecutionResult)}
             * method will be invoked after test execution.
             *
             * @return {@code null} (ignored).
             */
            @Override protected Runner doInBackground() {
                var selector = DiscoverySelectors.selectMethod(test.getJavaClass(), test.getJavaMethod());
                ExecutionContext.INSTANCE.execute(layer, launcher, selector);
                return null;
            }
        }.execute();
    }

    /**
     * Called in background thread when a test finished, successfully or not.
     * This method is invoked after each method, but also after each class.
     * We collect the results only for test methods.
     *
     * @param  identifier  identification of the test method or test class.
     * @param  result      result of the test.
     */
    @Override
    public void executionFinished​(final TestIdentifier identifier, final TestExecutionResult result) {
        if (identifier.getSource().orElse(null) instanceof MethodSource) {
            destination.addOrReplace​(new ResultEntry(this, identifier, result));
        }
    }
}
