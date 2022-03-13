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

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
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
     * The space to keep between two cells.
     */
    private static final int MARGIN = 2;

    /**
     * The color to use for aborted or failed tests.
     */
    private final Color ignoreColor, failureColor;

    /**
     * The scroll pane which contains the tree.
     * Used for computing the width.
     */
    private final JScrollPane container;

    /**
     * The test report to paint. This value shall be set by
     * {@link ResultCellRenderer#getTableCellRendererComponent}
     * before this component is rendered.
     */
    private ResultEntry report;

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
        report = null;
        value = ((DefaultMutableTreeNode) value).getUserObject();
        if (value instanceof ResultEntry) {
            report = (ResultEntry) value;
            value  = report.displayName;
        }
        super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
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
        return this;
    }

    /**
     * Paints the tree label, then paints the bar showing test coverage.
     *
     * @param  graphics  the graphics context to use for painting.
     */
    @Override
    protected void paintComponent(final Graphics graphics) {
        super.paintComponent(graphics);
        if (report != null) {
            final Shape     clip    = graphics.getClip();
            final Rectangle limit   = clip.getBounds();
            final Rectangle bounds  = getBounds();
            final int       textEnd = bounds.width;
            limit .width   = container.getWidth() - DECORATION_WIDTH;
            bounds.x       = limit.width - (COVERAGE_BAR_WIDTH + MARGIN);
            bounds.y       = MARGIN;
            bounds.height -= MARGIN*2;
            bounds.width   = COVERAGE_BAR_WIDTH;
            limit.x        = textEnd + MARGIN;
            limit.width   -= limit.x;
            graphics.setClip(limit);
            report.drawCoverage((Graphics2D) graphics, bounds);
            graphics.setClip(clip);
        }
    }
}
