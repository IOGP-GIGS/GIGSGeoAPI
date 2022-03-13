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
import java.awt.EventQueue;
import javax.swing.UIManager;


/**
 * Entry point for running GIGS tests in a Graphical User Interface (GUI).
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
            out.println("Usage: java -jar geoapi-conformance.jar");
            out.flush();
        }
    }

    /**
     * Starts the swing application.
     */
    public static void startSwingApplication() {
        final MainFrame frame = new MainFrame("GIGS tests");
        frame.setVisible(true);
        EventQueue.invokeLater(frame);
    }
}
