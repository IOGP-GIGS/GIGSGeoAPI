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

import java.net.URI;
import java.util.Map;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Locale;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import org.iogp.gigs.internal.geoapi.Configuration;
import org.iogp.gigs.internal.ExecutionContext;
import org.iogp.gigs.internal.PrivateAccessor;
import org.junit.jupiter.api.DisplayName;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.support.descriptor.MethodSource;


/**
 * The result of the execution of a single test. This object contains the test method name,
 * some information about the configuration and the stack trace if an error occurred.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @version 1.0
 * @since   1.0
 */
final class ResultEntry {
    /**
     * The base URL of {@code geoapi-conformance} javadoc. The trailing slash is mandatory.
     */
    private static final String JAVADOC_BASEURL = "https://iogp-gigs.github.io/GIGSGeoAPI/";

    /**
     * The runner which has been used for running this test.
     * This is used for re-executing the test if needed.
     */
    private final Runner runner;

    /**
     * The Java method which is the source of the test for which we are providing a result.
     */
    private final MethodSource source;

    /**
     * The human-readable name of the series to which this test belong.
     * Specified by the {@code DisplayName} annotation on the test class.
     */
    final String series;

    /**
     * The human-readable name for the test method.
     * Specified by the {@code DisplayName} annotation on the test method.
     */
    final String displayName;

    /**
     * The factories declared in the configuration. Each row in this list is an array of length 4.
     * The array elements are:
     *
     * <ol>
     *   <li>The factory category (i.e. GeoAPI interface)</li>
     *   <li>The implementation simple class name</li>
     *   <li>The vendor name (may be null)</li>
     *   <li>The authority name (may be null)</li>
     * </ol>
     *
     * @see SwingFactoryTableModel
     */
    final List<String[]> factories;

    /**
     * The configuration specified by the implementer for this test.
     */
    final List<TestAspect> configuration;

    /**
     * If a test failure occurred in an optional test, the configuration key for disabling that test.
     * Otherwise {@code null}.
     */
    private final Configuration.Key<Boolean> configurationTip;

    /**
     * The test status, optionally with the exception.
     */
    final TestExecutionResult result;

    /**
     * An estimation of the test coverage, as a floating point value between 0 and 1.
     */
    private final float coverage;

    /**
     * {@code true} if the tolerance threshold has been relaxed.
     */
    private boolean isToleranceRelaxed;

    /**
     * Creates a new entry for the given result. The {@linkplain TestIdentifier#getSource() source} of the test
     * must be an instance of {@link MethodSource}; it is caller's responsibility to verify this condition.
     *
     * @param identifier  identification of the test provided by JUnit.
     * @param result      result of the test (success, failure, aborted).
     */
    ResultEntry(final Runner runner, final TestIdentifier identifier, final TestExecutionResult result) {
        this.runner = runner;
        this.result = result;
        result.getThrowable().ifPresent(ResultEntry::trimStackTrace);
        source = (MethodSource) identifier.getSource().get();
        final Class<?> c = source.getJavaClass();
        String className;
        if (c != null) {
            final DisplayName dn = c.getAnnotation(DisplayName.class);
            className = (dn != null) ? dn.value() : c.getSimpleName();
        } else {
            className = source.getClassName();
            className = className.substring(className.lastIndexOf('.') + 1);
        }
        series = className;
        displayName = identifier.getDisplayName();
        /*
         * Extract information from the configuration:
         *  - Computes an estimation of test coverage as a number between 0 and 1.
         *  - Get the list of factories.
         */
        int numTests=1, numSupported=1;
        configurationTip = ExecutionContext.INSTANCE.configurationTip;
        factories        = new ArrayList<>();
        configuration    = new ArrayList<>();
        for (Map.Entry<Configuration.Key<?>,Object> entry : ExecutionContext.INSTANCE.configuration().entrySet()) {
            final Configuration.Key<?> key = entry.getKey();
            final String   name  = key.name();
            final Class<?> type  = key.valueType();
            final Object   value = entry.getValue();
            /*
             * Note: we assume that a test with every optional features marked as "unsupported"
             * (`isFooSupported = false`) still do some test, so we unconditionally start the
             * count with 1 supported test.
             */
            if ((type == Boolean.class) && name.startsWith("is")) {
                if (name.endsWith("Supported")) {
                    final TestAspect.Status so;
                    if (Boolean.FALSE.equals(value)) {
                        so = TestAspect.Status.DISABLED;
                    } else {
                        numSupported++;
                        so = (key == configurationTip) ? TestAspect.Status.FAILED : TestAspect.Status.ENABLED;
                    }
                    configuration.add(new TestAspect(this, key, so));
                    numTests++;
                } else if (name.equals("isToleranceRelaxed")) {
                    isToleranceRelaxed = (Boolean) value;
                }
            }
            /*
             * Check for factories. See the javadoc of the `factories` field
             * for the meaning of array elements.
             */
            FactoryTableModel.addTo(type, value, factories);
        }
        coverage = numSupported / ((float) numTests);
    }

    /**
     * Puts space between words in the given string.
     * The first letter is never modified.
     *
     * @param  name          camel-case name to separate into words.
     * @param  toLowerCase   whether to put the first letter in lower case.
     * @param  suffix        suffix to append, or an empty string if none.
     * @return the given name as a sentence.
     */
    static String separateWords(final String name, final boolean toLowerCase, final String suffix) {
        StringBuilder buffer = null;
        for (int i = name.length(); i >= 2;) {
            final int c = name.codePointBefore(i);
            final int nc = Character.charCount(c);
            i -= nc;
            if (Character.isUpperCase(c) || Character.isDigit(c)) {
                /*
                 * If we have a lower case letter followed by an upper case letter, unconditionally
                 * insert a space between them. If we have 2 consecutive upper case letters (actually
                 * anything except a space and a lower case letter, followed by an upper case letter),
                 * insert a space only if the next character is lower case. The later rule is an
                 * attempt to handle abbreviations, like "URLEncoding" to "URL Encoding".
                 */
                final int cb = name.codePointBefore(i);
                if (Character.isSpaceChar(cb)) {
                    continue;
                }
                if (!Character.isLowerCase(cb)) {
                    final int next = i + nc;
                    if (next >= name.length() || !Character.isLowerCase(name.codePointAt(next))) {
                        continue;
                    }
                }
                if (buffer == null) {
                    buffer = new StringBuilder(name);
                }
                if (toLowerCase && nc == 1) {
                    final int lowerCase = Character.toLowerCase(c);
                    if (Character.charCount(lowerCase) == 1) {                  // Paranoiac check.
                        buffer.setCharAt(i, (char) lowerCase);
                    }
                }
                buffer.insert(i, ' ');
            }
        }
        return (buffer != null) ? buffer.append(suffix).toString() : name.concat(suffix);
    }

    /**
     * Trims the stack trace of the given exception and all its cause, removing everything
     * after the last {@code org.iogp.gigs} package which is not this runner package.
     *
     * @param  exception  the exception to trim in-place.
     */
    private static void trimStackTrace(Throwable exception) {
        final StackTraceElement[] stackTrace = exception.getStackTrace();
        for (int i=stackTrace.length; --i>=0;) {
            final String className = stackTrace[i].getClassName();
            if (className.startsWith("org.iogp.gigs.") &&
               !className.startsWith("org.iogp.gigs.runner."))
            {
                exception.setStackTrace(Arrays.copyOf(stackTrace, i+1));
                break;
            }
        }
    }

    /**
     * Returns the URL to the javadoc of the test method. Users can follow this URL
     * in order to have more details about the test data or procedure.
     *
     * @return the URI to the javadoc of the test method, or {@code null} if none.
     */
    public Optional<URI> getJavadocURL() {
        String method = source.getMethodName();
        final int s = method.indexOf('[');
        if (s >= 0) {
            method = method.substring(0, s);
        }
        return Optional.of(URI.create(JAVADOC_BASEURL + source.getClassName().replace('.', '/') + ".html#" + method + "()"));
    }

    /**
     * Returns the Java name of the test class and test method.
     *
     * @return name of the test in Java source code
     */
    String getProgrammaticName() {
        return source.getClassName() + '.' + source.getMethodName();
    }

    /**
     * Returns a string representation of the result.
     *
     * @return the result, or {@code null} if none.
     */
    String getResultText() {
        if (result != null) {
            final Throwable exception = result.getThrowable().orElse(null);
            if (exception != null) {
                final String message = exception.getLocalizedMessage();
                if (message != null) {
                    return message;
                }
            }
            return result.getStatus().name().toLowerCase(Locale.US);
        }
        return null;
    }

    /**
     * Returns a tip about a configuration change that may be done for avoiding test failure.
     *
     * @return configuration that may be disabled, or {@code null} if no failure.
     */
    String getConfigurationTip() {
        if (configurationTip != null) {
            return separateWords(configurationTip.name(), true, "?");
        }
        return null;
    }

    /**
     * Draws a shape representing the test coverage using the given graphics handler.
     * This method changes the graphics paint, so caller should restore it to whatever
     * paint they want to use after this method call.
     *
     * @param graphics  the graphics where to draw.
     * @param bounds    the region where to draw. <strong>Will be modified by this method</strong>.
     */
    void drawCoverage(final Graphics2D graphics, final Rectangle bounds) {
        final Color color;
        switch (result.getStatus()) {
            case SUCCESSFUL: {
                color = isToleranceRelaxed ? Color.ORANGE : Color.GREEN;
                break;
            }
            case FAILED: {
                color = Color.RED;
                break;
            }
            case ABORTED: {
                color = Color.GRAY;
                break;
            }
            default: {
                return;                         // Do not paint anything.
            }
        }
        final Paint p = graphics.getPaint();
        final int width = bounds.width;
        bounds.width = StrictMath.round(width * coverage);
        graphics.setColor(color);
        graphics.fill(bounds);
        bounds.width = width;
        graphics.setColor(color.darker());
        graphics.draw(bounds);
        graphics.setPaint(p);                   // Restore previous color.
    }

    /**
     * Re-execute this test.
     */
    void setAspectAndExecute(final Configuration.Key<Boolean> aspect, final Boolean value) {
        PrivateAccessor.INSTANCE.setTestSpecificOption(source.getJavaMethod(), aspect, value);
        runner.execute(source);
    }

    /**
     * Returns {@code true} if this entry uses the same configuration keys than the specified entry.
     *
     * @param  other  the other entry to compare to.
     * @return whether the two entries use the same configuration keys.
     */
    boolean useSameConfigurationKeys(final ResultEntry other) {
        if (other != null) {
            final int n = configuration.size();
            if (n == other.configuration.size()) {
                for (int i=0; i<n; i++) {
                    if (!configuration.get(i).useSameConfigurationKey(other.configuration.get(i))) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a string representation of this entry.
     */
    @Override
    public String toString() {
        return displayName + ": " + result.getStatus();
    }
}
