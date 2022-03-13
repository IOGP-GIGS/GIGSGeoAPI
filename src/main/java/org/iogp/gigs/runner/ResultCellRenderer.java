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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;


/**
 * The cell renderer for the tree of results.
 * This cell renderer can display cells in different color depending on the test status.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @version 1.0
 * @since   1.0
 */
@SuppressWarnings("serial")
final class ResultCellRenderer extends DefaultTreeCellRenderer {
    /**
     * With (in pixels) of the bar showing test coverage.
     */
    private static final int COVERAGE_BAR_WIDTH = 60;

    /**
     * Presumed width of vertical scroll bar + tree handlers + a margin (in pixels).
     */
    private static final int DECORATION_WIDTH = 68;

    /**
     * The color to use for aborted or failed tests.
     */
    private final Color ignoreColor, failureColor;

    /**
     * A container for this label with the {@linkplain #coverage} bar.
     * This is the renderer for a row in the tree.
     */
    private final JPanel renderer;

    /**
     * The renderer for test coverage.
     */
    private final Coverage coverage;

    /**
     * The scroll pane which contains the tree.
     * Used for computing the width.
     */
    private final JScrollPane container;

    /**
     * Creates a default cell renderer for {@link ResultTableModel}.
     *
     * @param  pane  scroll pane which contains the tree.
     */
    @SuppressWarnings("ThisEscapedInObjectConstruction")
    ResultCellRenderer(final JScrollPane pane) {
        ignoreColor  = Color.GRAY;
        failureColor = Color.RED;
        container    = pane;
        coverage     = new Coverage();
        renderer     = new JPanel(new BorderLayout());
        renderer.add(this, BorderLayout.CENTER);
        renderer.add(coverage, BorderLayout.EAST);
        renderer.setOpaque(false);
    }

    /**
     * Configures the renderer based on the passed in components.
     *
     * @param  tree      the tree where to render.
     * @param  value     the value to render.
     * @param  selected  whether node is selected.
     * @param  expanded  whether node is expanded.
     * @param  leaf      whether node is a leaf node.
     * @param  row       row index.
     * @param  hasFocus  whether node has focus.
     * @return the component for cell rendering.
     */
    @Override
    public Component getTreeCellRendererComponent(final JTree tree, Object value,
            final boolean selected, final boolean expanded, final boolean leaf,
            final int row, final boolean hasFocus)
    {
        ResultEntry report = null;
        value = ((DefaultMutableTreeNode) value).getUserObject();
        if (value instanceof ResultEntry) {
            report = (ResultEntry) value;
            value  = report.displayName;
        }
        super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
        coverage.report = report;
        if (report != null) {
            if (!selected) {
                switch (report.result.getStatus()) {
                    case ABORTED: {
                        setForeground(ignoreColor);
                        break;
                    }
                    case FAILED: {
                        setForeground(failureColor);
                        break;
                    }
                }
            }
        }
        /*
         * Compute a size which will make the coverage bars appear on the right side of the scroll area.
         * We compute this size every time that a cell is renderer, but it seems to be effective only in
         * the first case. All subsequent calls seem to have no effect, which leave the bars at a wrong
         * location if the window is resized. I have not understood the reason yet (maybe because layout
         * methods have been overridden by `DefaultTreeCellRenderer` as no-operation?).
         */
        Dimension size = getPreferredSize();
        size.width = container.getWidth() - DECORATION_WIDTH;
        renderer.setPreferredSize(size);
        return renderer;
    }

    /**
     * A panel showing the test coverage as a bar.
     */
    @SuppressWarnings("serial")
    private static final class Coverage extends JComponent {
        /**
         * The space to keep between two cells.
         */
        private static final int MARGIN = 1;

        /**
         * The test report to paint. This value shall be set by
         * {@link ResultCellRenderer#getTableCellRendererComponent}
         * before this component is rendered.
         */
        ResultEntry report;

        /**
         * Creates a new instance.
         */
        Coverage() {
            setOpaque(false);
            setPreferredSize(new Dimension(COVERAGE_BAR_WIDTH, 19));
        }

        /**
         * Paints the test coverage.
         *
         * @param  graphics  the graphics context to use for painting.
         */
        @Override
        protected void paintComponent(final Graphics graphics) {
            super.paintComponent(graphics);
            if (report != null) {
                final Rectangle bounds = getBounds();
                bounds.x       = MARGIN;
                bounds.y       = MARGIN;
                bounds.width  -= MARGIN*2;
                bounds.height -= MARGIN*2;
                report.drawCoverage((Graphics2D) graphics, bounds);
            }
        }
    }
}
