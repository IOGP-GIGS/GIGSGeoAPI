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
package org.iogp.gigs;

import java.util.Map;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.LogRecord;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.InputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import org.iogp.gigs.internal.geoapi.Configuration;


/**
 * Global configuration of the tests together with a test-by-test customization.
 * The customization is specified in properties files read from the following locations, in that order:
 *
 * <ol>
 *   <li>{@code "org.iogp.gigs.config"} system property documented in {@link org.iogp.gigs.runner.Launcher}.</li>
 *   <li>{@code META-INF/GIGS.properties} in the JAR files of the implementation.</li>
 * </ol>
 *
 * The first applicable choice in above list has precedence over the other choices.
 * For example if a system property is specified, the {@code META-INF/GIGS.properties} file is ignored.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @version 1.0
 * @since   1.0
 */
final class ConfigurationMap {
    /**
     * The unique instance of {@code ConfigurationMap}.
     */
    static final ConfigurationMap INSTANCE = new ConfigurationMap();

    /**
     * The global configuration.
     */
    final Configuration global;

    /**
     * Test-by-test customization of the configuration.
     * All accesses to this map shall be synchronized.
     */
    private final Map<Method, Map<Configuration.Key<Boolean>, Boolean>> byTest;

    /**
     * The module layer for which the the configuration has been loaded.
     * If no configuration has been loaded yet, then this is {@code null}.
     */
    private ModuleLayer loaded;

    /**
     * Creates the unique instance.
     */
    private ConfigurationMap() {
        global = new Configuration();
        byTest = new HashMap<>();
    }

    /**
     * Loads the configuration declared by the system property or by the implementer.
     * If the module layer is the same one than the layer specified in the last call,
     * then this method does nothing.
     *
     * @param  layer  module layer of the implementation to test, or {@code null} if none.
     */
    final void configureFor(final ModuleLayer layer) {
        if (layer != null && layer != loaded) {
            loaded = layer;
            global.clear();
            byTest.clear();
            if (!loadProperties("org.iogp.gigs.config")) {
                final Properties properties = new Properties();
                for (final Module module : layer.modules()) {
                    try (InputStream in = module.getResourceAsStream("META-INF/GIGS.properties")) {
                        if (in != null) {
                            properties.load(in);
                            parse(properties);
                            break;
                        }
                    } catch (IOException e) {
                        warning("Can not load from META-INF.", e);
                    }
                }
            }
        }
    }

    /**
     * Loads properties from the file specified by the given system property.
     * If an {@link IOException} occurs, a warning is logged but execution continue.
     * The consequence is that more tests than intended may be enabled,
     * which may result in more test failures.
     *
     * @param  propertyName  name of the system property specifying the file to load.
     * @return whether properties have been found and loaded.
     */
    private boolean loadProperties(final String propertyName) {
        final String filename = System.getProperty(propertyName);
        if (filename != null) {
            final Properties properties = new Properties();
            try (InputStream in = Files.newInputStream(Paths.get(filename))) {
                properties.load(in);
            } catch (IOException e) {
                warning("Can not load \"" + filename + "\".", e);
            }
            if (!properties.isEmpty()) {
                parse(properties);
                return true;
            }
        }
        return false;
    }

    /**
     * Parses the content of the given properties file.
     * Unrecognized properties will be logged as warnings.
     *
     * @param  properties  the properties to store.
     */
    private void parse(final Map<? super String, ? super String> properties) {
        synchronized (byTest) {
            for (final Map.Entry<?,?> entry : properties.entrySet()) {
                final String property = entry.getKey().toString();
                final int    keySep   = property.lastIndexOf('.');
                final String option   = property.substring(keySep + 1).trim();
                if (!option.isEmpty()) {
                    final int testSep = property.lastIndexOf('.', keySep - 1);
                    final String test = property.substring(testSep + 1, keySep).trim();
                    if (!test.isEmpty()) {
                        final boolean isGlobal = test.equals("*");
                        final String classe = property.substring(0, Math.max(0, testSep)).trim();
                        if (classe.isEmpty() == isGlobal) {
                            final Method method;
                            if (isGlobal) {
                                method = null;
                            } else try {
                                method = Class.forName("org.iogp.gigs." + classe).getMethod(test, (Class<?>[]) null);
                            } catch (ReflectiveOperationException e) {
                                warning("Non-existent test case: " + property, e);
                                continue;
                            }
                            Configuration.Key.valueOf(option).ifPresentOrElse((key -> {
                                if (Boolean.class.equals(key.valueType())) {
                                    final Boolean value = Boolean.valueOf(entry.getValue().toString());
                                    setTestSpecificOption(method, key.cast(Boolean.class), value);
                                } else {
                                    warning("The \"" + option + "\" option is not a boolean.", null);
                                }
                            }), () -> warning("Unknown configuration key: " + option, null));
                            continue;
                        }
                    }
                }
                warning("Invalid syntax for configuration property: " + property, null);
            }
        }
    }

    /**
     * Logs a warning about a property that cannot be parsed.
     * This is considered a non-fatal error; it will not stop execution.
     * Continuing execution may cause more test failures than expected
     * because configuration may not be as intended.
     *
     * @param message  message to log.
     * @param cause    cause of this warnings, or {@code null} if none.
     */
    private static void warning(final String message, final Exception cause) {
        final Logger logger = Logger.getLogger("org.iogp.gigs");
        final LogRecord record = new LogRecord(Level.WARNING, message);
        record.setLoggerName(logger.getName());
        record.setSourceClassName(IntegrityTest.class.getName());       // Taken as the public entry point.
        record.setSourceMethodName("<clinit>");
        record.setThrown(cause);
        logger.log(record);
    }

    /**
     * Enables or disables optional aspects specifically for a single test.
     *
     * @param  test    the test to configure.
     * @param  method  the test method which is about to be executed.
     */
    final void applyTestSpecificOptions(final IntegrityTest test, final Method method) {
        synchronized (byTest) {
            final Map<Configuration.Key<Boolean>, Boolean> currentTest = byTest.get(method);
            if (currentTest != null) {
                final Configuration.Key<Boolean>[] options = test.getOptionKeys();
                for (int i=0; i<options.length; i++) {
                    final Boolean enabled = currentTest.get(options[i]);
                    if (enabled != null) {
                        test.setOptionEnabled(i, enabled);
                    }
                }
            }
        }
    }

    /**
     * Enables or disables an optional aspect for a specific test method.
     *
     * @param  method  the test method to configure, or {@code null} for global configuration.
     * @param  aspect  the test aspect to enable or disable.
     * @param  value   the new enabled status, or {@code null} for removing.
     */
    final void setTestSpecificOption(final Method method, final Configuration.Key<Boolean> aspect, final Boolean value) {
        synchronized (byTest) {
            if (value != null) {
                if (method == null) {
                    global.put(aspect, value);
                } else {
                    byTest.computeIfAbsent(method, (k) -> new HashMap<>()).put(aspect, value);
                }
            } else if (method == null) {
                global.remove(aspect);
            } else {
                final Map<Configuration.Key<Boolean>, Boolean> map = byTest.get(method);
                if (map != null) {
                    map.remove(aspect);
                }
            }
        }
    }

    /**
     * Gets the configuration of tests as a properties file.
     * The returned map does not include the global configuration.
     *
     * @return configuration of all tests.
     */
    final Properties getTestConfigurations() {
        final Properties properties = new Properties();
        final StringBuilder builder = new StringBuilder();
        synchronized (byTest) {
            for (final Map.Entry<Method, Map<Configuration.Key<Boolean>, Boolean>> test : byTest.entrySet()) {
                final Method method = test.getKey();
                builder.append(method.getDeclaringClass().getSimpleName()).append('.')
                       .append(method.getName()).append('.');
                final int s = builder.length();
                for (final Map.Entry<Configuration.Key<Boolean>, Boolean> aspect : test.getValue().entrySet()) {
                    final String key = builder.append(aspect.getKey().name()).toString();
                    properties.setProperty(key, aspect.getValue().toString());
                    builder.setLength(s);
                }
                builder.setLength(0);
            }
        }
        return properties;
    }
}
