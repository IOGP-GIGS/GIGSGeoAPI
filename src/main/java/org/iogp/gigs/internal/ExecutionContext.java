/*
 * GIGS - Geospatial Integrity of Geoscience Software
 * https://gigs.iogp.org/
 *
 * Copyright (C) 2022-2023 International Association of Oil and Gas Producers.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */
package org.iogp.gigs.internal;

import java.util.Collections;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.iogp.gigs.*;
import org.iogp.gigs.internal.geoapi.Configuration;
import org.iogp.gigs.internal.geoapi.Units;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;


/**
 * Context about the execution of a test.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @version 1.0
 * @since   1.0
 *
 * @todo To move to GeoAPI internal.
 */
public final class ExecutionContext implements ParameterResolver {
    /**
     * All factories found. May contain null elements.
     * This field is non-null only during test execution.
     */
    private volatile Factories factories;

    /**
     * The test under execution, or {@code null} if none.
     * This is set by {@link IntegrityTest#saveReference()} after test execution.
     */
    public volatile IntegrityTest executing;

    /**
     * If a test failure occurred in an optional test, the configuration key for disabling that test.
     * Otherwise {@code null}. This is set by {@link IntegrityTest#saveReference()} after test execution.
     */
    public volatile Configuration.Key<Boolean> configurationTip;

    /**
     * Creates a new suite.
     */
    private ExecutionContext() {
    }

    /**
     * The singleton instance of this context. Ideally we should not have this
     * static field, but I did not yet found another way to get this reference.
     */
    public static final ExecutionContext INSTANCE = new ExecutionContext();

    /**
     * Executes tests specified by the given selectors.
     *
     * @param  loader     class loader to use for loading the factories provided by the implementation to test.
     * @param  launcher   the JUnit object to use for running tests.
     * @param  selectors  the tests to execute.
     */
    public void execute(final ClassLoader loader, final Launcher launcher, final DiscoverySelector... selectors) {
        final LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request().selectors(selectors).build();
        try {
            // For class initialization before we invoke `PrivateAccessor.INSTANCE` mehod.
            Class.forName(IntegrityTest.class.getName(), true, ExecutionContext.class.getClassLoader());
        } catch (ClassNotFoundException e) {
            // Should never happen. Continue anyway and let JVM handle the error.
            Logger.getLogger("org.iogp.gigs").log(Level.WARNING, e.toString(), e);
        }
        try {
            PrivateAccessor.INSTANCE.configureFor(loader);
            factories = new DiscoveredFactories(loader);
            Units.setInstance(loader);
            launcher.execute(request);
        } finally {
            factories = null;
            Units.setInstance(null);
            PrivateAccessor.INSTANCE.configureFor(null);
        }
    }

    /**
     * Determines if this resolver supports resolution of an argument.
     * This is used for dependency injection.
     *
     * @param  pc  the context for the parameter for which an argument should be resolved.
     * @param  ec  the extension context (ignored).
     * @return whether this resolver can resolve the parameter.
     */
    @Override
    public boolean supportsParameter(ParameterContext pc, ExtensionContext ec) {
        return Factories.isSupported(pc.getParameter().getType());
    }

    /**
     * Resolves an argument.
     * This is used for dependency injection.
     *
     * @param  pc  the context for the parameter for which an argument should be resolved.
     * @param  ec  the extension context (ignored).
     * @return the argument value (may be null).
     */
    @Override
    public Object resolveParameter(ParameterContext pc, ExtensionContext ec) {
        final Class<?> type = pc.getParameter().getType();
        @SuppressWarnings("LocalVariableHidesMemberVariable")
        final Factories factories = this.factories;
        if (factories != null) {
            return factories.get(type).orElse(null);
        }
        if (type == Factories.class) {
            return new Factories() {};
        }
        return null;
    }

    /**
     * Returns the configuration associated to the test under execution.
     *
     * @return configuration of last executed test.
     */
    public Map<Configuration.Key<?>,Object> configuration() {
        @SuppressWarnings("LocalVariableHidesMemberVariable")
        final IntegrityTest executing = this.executing;
        if (executing != null) {
            return PrivateAccessor.INSTANCE.configuration(executing).map();
        }
        return Collections.emptyMap();
    }
}
