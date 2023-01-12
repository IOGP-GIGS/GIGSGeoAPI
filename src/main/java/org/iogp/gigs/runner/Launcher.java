/*
 * GIGS - Geospatial Integrity of Geoscience Software
 * https://gigs.iogp.org/
 *
 * Copyright (C) 2022 International Association of Oil and Gas Producers.
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
package org.iogp.gigs.runner;

import java.io.Console;
import java.io.PrintWriter;
import javax.swing.UIManager;


/**
 * Entry point for running GIGS tests in a Graphical User Interface (GUI).
 *
 * <h2>Configuration</h2>
 * If the {@systemProperty org.iogp.gigs.config} system property is specified
 * (typically with the {@code -D} option on the command line),
 * then its value shall by the path to a file in
 * {@linkplain java.util.Properties#load(java.io.Reader) Java property file format}.
 * The properties in that file shall have the following syntax:
 *
 * <pre>class.method.key=value</pre>
 *
 * Where:
 *
 * <ul>
 *   <li><var>class</var>  is the name of a class (without package) in the {@link org.iogp.gigs} package;</li>
 *   <li><var>method</var> is the name of a method in the class named by <var>class</var>;</li>
 *   <li><var>key</var>    is the name of a test option such as {@code isStandardAliasSupported};</li>
 *   <li><var>value</var>  is the value to assign to that option.</li>
 * </ul>
 *
 * <p>Special cases:</p>
 * <ul>
 *   <li>If {@code class.method} is {@code "*"}, then the option applies to all tests.</li>
 * </ul>
 *
 * Currently, only boolean options are supported.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @version 1.0
 * @since   1.0
 */
public final class Launcher {
    /**
     * Do not allow instantiation of this class.
     */
    private Launcher() {
    }

    /**
     * The application entry point. Current implementation {@linkplain #startSwingApplication()
     * starts the Swing application}. Future versions may provides different alternatives based
     * on the command-line arguments.
     *
     * @param arguments  must be an empty string in current version.
     *        Future versions may accept some command-line arguments.
     */
    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    public static void main(final String[] arguments) {
        if (arguments.length == 0) {
            /*
             * Use cross-platform look and feel (instead of system look and file)
             * because `ResultCellRenderer` has some assumptions about sizes in pixels.
             */
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) try {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                } catch (Exception e) {
                    // Ignore - keep the default L&F.
                }
            }
            startSwingApplication();
        } else {
            final Console console = System.console();
            final PrintWriter out = (console != null) ? console.writer() : new PrintWriter(System.out, true);
            out.println("Usage: java -jar gigs.jar");
            out.flush();
        }
    }

    /**
     * Starts the swing application.
     */
    public static void startSwingApplication() {
        final MainFrame frame = new MainFrame("GIGS tests");
        frame.show();
    }
}
