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

import java.util.List;
import java.util.Collections;
import javax.swing.JMenuItem;
import javax.swing.JTable;
import javax.swing.JPopupMenu;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;


/**
 * The table model for the list of configuration entries.
 * The table contains a column with checkbox telling whether the configuration option is enabled.
 * If user changes the checkbox status, the test is rerun with the new configuration.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @version 1.0
 * @since   1.0
 */
@SuppressWarnings("serial")
final class ConfigurationTableModel extends AbstractTableModel {
    /**
     * Index of columns handled by this model.
     */
    private static final int KEY_COLUMN   = 0,
                             VALUE_COLUMN = 1,
                             PASS_COLUMN  = 2;

    /**
     * The titles of all columns.
     */
    private static final String[] COLUMN_TITLES;
    static {
        COLUMN_TITLES = new String[3];
        COLUMN_TITLES[KEY_COLUMN]   = "Feature";
        COLUMN_TITLES[VALUE_COLUMN] = "Enabled";
        COLUMN_TITLES[PASS_COLUMN]  = "Remarks";
    };

    /**
     * The configuration entries. This reference is set to the {@link ResultEntry#configuration}
     * list of the test currently shown in the "details" pane. This reference changes every time
     * that details are shown for a different test.
     */
    List<TestAspect> entries;

    /**
     * Creates an initially empty table model.
     */
    ConfigurationTableModel() {
        entries = Collections.emptyList();
    }

    /**
     * Returns a new table view using this model. This method is defined as a convenient place
     * where to configure the view (column width, <i>etc.</i>) if desired.
     *
     * @return the configured table view.
     */
    JTable createView() {
        final JTable view = new JTable(this);
        final TableColumnModel columns = view.getColumnModel();
        columns.getColumn(KEY_COLUMN)  .setPreferredWidth(400);
        columns.getColumn(VALUE_COLUMN).setPreferredWidth(100);
        columns.getColumn(PASS_COLUMN) .setPreferredWidth(300);
        final var menus = new JPopupMenu();
        final var shows = new JMenuItem("Show all configurations");
        shows.addActionListener(new AllConfigurations());
        menus.add(shows);
        view.setComponentPopupMenu(menus);
        return view;
    }

    /**
     * Returns the number of columns in this table.
     */
    @Override
    public int getColumnCount() {
        return COLUMN_TITLES.length;
    }

    /**
     * Returns the name of the given column.
     */
    @Override
    public String getColumnName(final int column) {
        return COLUMN_TITLES[column];
    }

    /**
     * Returns the type of values in the given column.
     */
    @Override
    public Class<?> getColumnClass(final int column) {
        switch (column) {
            case KEY_COLUMN:   return String.class;
            case VALUE_COLUMN: return Boolean.class;
            case PASS_COLUMN:  return String.class;
            default: throw new IndexOutOfBoundsException(String.valueOf(column));
        }
    }

    /**
     * Returns the number of rows in this table.
     */
    @Override
    public int getRowCount() {
        return entries.size();
    }

    /**
     * Returns the value in the given cell.
     */
    @Override
    public Object getValueAt(final int row, final int column) {
        final TestAspect entry = entries.get(row);
        switch (column) {
            case KEY_COLUMN:   return ResultEntry.separateWords(entry.name(), true, "");
            case VALUE_COLUMN: return entry.status() != TestAspect.Status.DISABLED;
            case PASS_COLUMN:  return entry.status() == TestAspect.Status.FAILED ? "Failed" : null;
            default: throw new IndexOutOfBoundsException(String.valueOf(column));
        }
    }

    /**
     * Returns whether the specified cell can be edited.
     * This is {@code true} for the checkbox column and {@code false} for all other columns.
     */
    @Override
    public boolean isCellEditable(final int row, final int column) {
        return (column == VALUE_COLUMN) && entries.get(row).isEditable();
    }

    /**
     * Invoked when the user changed the status of a checkbox for enabling or disabling a configuration item.
     */
    @Override
    public void setValueAt(final Object value, final int row, final int column) {
        if (column == VALUE_COLUMN) {
            entries.get(row).execute((Boolean) value);
        }
    }
}
