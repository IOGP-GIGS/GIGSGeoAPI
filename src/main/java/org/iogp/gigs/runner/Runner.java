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

import java.awt.EventQueue;
import java.util.List;
import java.util.ArrayList;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.engine.support.descriptor.MethodSource;


/**
 * Provides methods for running the tests.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @version 1.0
 * @since   1.0
 */
final class Runner implements TestExecutionListener {
    /**
     * The result of each tests. All accesses to this list must be synchronized.
     */
    private final List<ResultEntry> entries;

    /**
     * The tree where to show results.
     * All changes in this tree must be done in the swing thread.
     */
    final JTree tree;

    /**
     * Creates a new, initially empty, runner.
     */
    Runner() {
        entries = new ArrayList<>();
        tree = new JTree(new DefaultTreeModel(new DefaultMutableTreeNode("Test results")));
        tree.setRootVisible(false);
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
            final ResultEntry entry = new ResultEntry(identifier, result);
            synchronized (entries) {
                entries.add(entry);
            }
            EventQueue.invokeLater(() -> {
                final DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
                final DefaultMutableTreeNode parent = series(model, entry.series);
                final int index = parent.getChildCount();
                parent.add(new DefaultMutableTreeNode(entry, false));
                model.nodesWereInserted(parent, new int[] {index});
            });
        }
    }

    /**
     * Returns the node of the series of the given name, creating it if needed.
     * This method must be invoked in Swing thread.
     *
     * @param  model  value of {@code tree.getModel()}.
     * @param  name   name of the series.
     * @return node of the series of the given name.
     */
    private DefaultMutableTreeNode series(final DefaultTreeModel model, final String name) {
        final DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
        DefaultMutableTreeNode series;
        for (int i = root.getChildCount(); --i >= 0;) {
            series = (DefaultMutableTreeNode) root.getChildAt(i);
            if (name.equals(series.getUserObject())) {
                return series;
            }
        }
        series = new DefaultMutableTreeNode(name);
        final int index = root.getChildCount();
        root.add(series);
        model.nodesWereInserted(root, new int[] {index});
        /*
         * The tree stay hidden if we do not expand the root
         * as soon as we can (after we got at least one child).
         */
        if (index == 0) {
            tree.expandPath(new TreePath(model.getRoot()));
        }
        return series;
    }
}
