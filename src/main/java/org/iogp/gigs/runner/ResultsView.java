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

import java.awt.Component;
import java.awt.EventQueue;
import java.util.AbstractList;
import java.util.Collections;
import java.util.Comparator;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import org.junit.platform.engine.TestExecutionResult;


/**
 * A Swing view over the results of all tests.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @version 1.0
 * @since   1.0
 */
final class ResultsView implements TreeSelectionListener {
    /**
     * The tree where to show the results.
     * All changes in this tree must be done in the swing thread.
     */
    private final JTree tree;

    /**
     * Where to show more details about the selected test.
     */
    private final TestDetails details;

    /**
     * Creates a new, initially empty, view.
     *
     * @param  details  where to show details about a test selected by the user.
     */
    ResultsView(final TestDetails details) {
        this.details = details;
        tree = new JTree(new DefaultTreeModel(new DefaultMutableTreeNode("Test results")));
        tree.setRootVisible(false);
    }

    /**
     * Creates the Swing component for viewing the test results.
     *
     * @return the Swing view for the test results.
     */
    final Component createView() {
        final JScrollPane pane = new JScrollPane(tree);
        tree.setCellRenderer(new ResultCellRenderer(pane));
        tree.getSelectionModel().addTreeSelectionListener(this);
        return pane;
    }

    /**
     * Invoked when the user clicked on a new row in the tree showing test results.
     * This method updates the "Details" tab with information relative to the test
     * in the selected row.
     *
     * @param  event  the event that characterizes the change.
     */
    @Override
    public void valueChanged(final TreeSelectionEvent event) {
        ResultEntry entry = null;
        final TreePath path = event.getNewLeadSelectionPath();
        if (path != null) {
            final DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
            final Object object = node.getUserObject();
            if (object instanceof ResultEntry) {
                entry = (ResultEntry) object;
            }
        }
        details.setTest(entry);
    }

    /**
     * Adds or replaces a tree node for the given test result.
     * If a node already exists for the test, its value will be replaced.
     * Otherwise a new node will be inserted in alphabetical order.
     *
     * <p>This method can be invoked in any thread.</p>
     *
     * @param  entry  the test result to add or replace.
     */
    final void addOrReplace​(final ResultEntry entry) {
        EventQueue.invokeLater(() -> {
            final DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
            final DefaultMutableTreeNode parent = series(model, entry.series);
            final Children searcher = new Children(parent);
            searcher.insert(model, entry);
            if (entry.result.getStatus() != TestExecutionResult.Status.SUCCESSFUL) {
                tree.expandPath(new TreePath(new Object[] {model.getRoot(), parent}));
            }
        });
    }

    /**
     * Returns the node of the series of the given name, creating it if needed.
     * This method must be invoked in the Swing thread.
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

    /**
     * View of the nodes as a list. This class implements also the comparator used for keeping elements
     * in the list in alphabetical order. This is used for finding where to insert a new node.
     * This object live only temporarily, it is not stored.
     */
    private static final class Children extends AbstractList<DefaultMutableTreeNode> implements Comparator<Object> {
        /**
         * The node of the test series.
         * Children of this nodes are result of test methods in the series.
         */
        private final DefaultMutableTreeNode series;

        /**
         * Creates a new list of test results.
         *
         * @param  series  the node of the test series.
         */
        Children(final DefaultMutableTreeNode series) {
            this.series = series;
        }

        /**
         * {@return the child node at the given index.}
         */
        @Override public DefaultMutableTreeNode get(final int index) {
            return (DefaultMutableTreeNode) series.getChildAt(index);
        }

        /**
         * {@return the number of child nodes.}
         */
        @Override public int size() {
            return series.getChildCount();
        }

        /**
         * Returns the given object as an instance of {@code ResultEntry}.
         *
         * @param  obj  the {@code ResultEntry} or {@code DefaultMutableTreeNode} instance.
         * @return the given object as a {@code ResultEntry} instance.
         */
        private static ResultEntry cast(Object obj) {
            if (obj instanceof DefaultMutableTreeNode) {
                obj = ((DefaultMutableTreeNode) obj).getUserObject();
            }
            return (ResultEntry) obj;
        }

        /**
         * Compares two nodes for order.
         *
         * @param  o1  the first node to compare.
         * @param  o2  the second node to compare.
         * @return negative if {@code o1} should be sorted before {@code o2}, positive if the converse.
         */
        @Override
        public int compare(final Object o1, final Object o2) {
            return cast(o1).displayName.compareToIgnoreCase(cast(o2).displayName);
        }

        /**
         * Inserts the given entry in the list of children.
         *
         * @param  model  the model to notify.
         * @param  entry  the entry to add.
         */
        final void insert(final DefaultTreeModel model, final ResultEntry entry) {
            int i = Collections.binarySearch(this, entry, this);
            if (i >= 0) {
                final DefaultMutableTreeNode node = (DefaultMutableTreeNode) series.getChildAt(i);
                node.setUserObject(entry);
                model.nodeChanged(node);
            } else {
                i = ~i;             // Tild operator, not minus.
                series.insert(new DefaultMutableTreeNode(entry, false), i);
                model.nodesWereInserted(series, new int[] {i});
            }
        }
    }
}
