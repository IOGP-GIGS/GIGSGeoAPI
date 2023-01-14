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

import java.awt.Font;
import java.awt.Insets;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.border.Border;
import javax.swing.BorderFactory;
import javax.swing.Box;


/**
 * An utility class for building various panels used by {@link MainFrame} or other components.
 * This class extends {@link GridBagConstraints} only for opportunist reason - do not rely on that.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @version 1.0
 * @since   1.0
 */
@SuppressWarnings("serial")
final class SwingPanelBuilder extends GridBagConstraints {
    /**
     * Creates a new builder.
     */
    SwingPanelBuilder() {
    }

    /**
     * Creates the panel where to display {@link ImplementationManifest} information.
     *
     * @param  title          where to write title of the library to test.
     * @param  version        where to write version of the library to test.
     * @param  vendor         where to write implementer of the library to test.
     * @param  specification  where to write GeoAPI title.
     * @param  specVersion    where to write GeoAPI version.
     * @param  specVendor     where to write GeoAPI vendor (which is OGC).
     * @return panel with information about tested library.
     */
    JPanel createManifestPane(final JLabel title, final JLabel version, final JLabel vendor,
            final JLabel specification, final JLabel specVersion, final JLabel specVendor)
    {
        final JPanel panel = new JPanel(new GridBagLayout());
        final JLabel implementsLabel;
        gridx=0; weightx=0; anchor=WEST; insets.left = 12;
        gridy=0; panel.add(createLabel("Title:",   title),   this);
        gridy++; panel.add(createLabel("Version:", version), this);
        gridy++; panel.add(createLabel("Vendor:",  vendor),  this);
        gridx=1; weightx=1;
        gridy=0; panel.add(title,   this);
        gridy++; panel.add(version, this);
        gridy++; panel.add(vendor,  this);
        gridx=2; weightx=0;
        gridy=0; panel.add(implementsLabel = createLabel("implements:", specification), this);
        gridx=3; weightx=1;
        gridy=0; panel.add(specification, this);
        gridy++; panel.add(specVersion,   this);
        gridy++; panel.add(specVendor,    this);

        implementsLabel.setFont(implementsLabel.getFont().deriveFont(Font.ITALIC));

        final Border space = BorderFactory.createEmptyBorder(6, 6, 6, 6);
        panel.setBorder(
                BorderFactory.createCompoundBorder(space,
                BorderFactory.createCompoundBorder(
                BorderFactory.createEtchedBorder(), space)));
        panel.setOpaque(false);
        return panel;
    }

    /**
     * Creates the panel where to display details about a particular test.
     *
     * @param  testName          where to write the test method name.
     * @param  testResult        where to write the test result.
     * @param  configurationTip  where to write tip about configuration for allowing test to pass.
     * @param  viewJavadoc       button for showing test javadoc.
     * @param  factories         table showing available factories.
     * @param  configuration     configuration at the time the test was executed.
     * @param  exception         the exception if the test failed, or {@code null} otherwise.
     * @return the panel showing details about selected test.
     */
    JPanel createDetailsPane(final JLabel testName, final JLabel testResult, final JLabel configurationTip,
            final JButton viewJavadoc, final JTable factories, final JTable configuration, final JTextArea exception)
    {
        final Font monospaced = Font.decode("Monospaced");
        testName.setFont(monospaced);

        final JPanel desc = new JPanel(new GridBagLayout());
        /*
         * Upper part of the pane with name of the test, the result and a tip.
         * The "Javadoc" button will be on the right side of the first line.
         * This button has a height bigger than the line,
         * so we put extra space on the top of the first line.
         */
        insets.left=12; insets.bottom=4;
        gridx=0; anchor=SOUTHWEST; fill=HORIZONTAL;
        gridy=0; desc.add(createLabel("Test method:", testName), this); anchor=NORTHWEST;
        gridy++; desc.add(createLabel("Result:",    testResult), this);
        gridy++; desc.add(createLabel("Tip:", configurationTip), this);
        gridx++; weightx=1; anchor=SOUTHWEST;
        gridy=0; desc.add(testName,         this); anchor=NORTHWEST; fill=BOTH; gridwidth=2;
        gridy++; desc.add(testResult,       this);
        gridy++; desc.add(configurationTip, this);
        /*
         * Javadoc button in the upper-right corner.
         */
        gridx++; gridy=0; gridwidth=1; weightx=0;
        anchor=NORTH; fill=HORIZONTAL;
        desc.add(viewJavadoc, this);
        /*
         * Wrap the test description part in another pane. On the left side of the description,
         * we put an invisible component of fixed height. It force the description to have some
         * minimal height. The intent is to keep the position of the configuration table more
         * stable when there is a change in the number of lines in the test failure description.
         */
        final JPanel panel = new JPanel(new GridBagLayout());
        gridx=0; anchor=NORTHWEST; fill=NONE; insets.left=0; insets.bottom=0;
        panel.add(Box.createVerticalStrut(120), this);
        gridx++; weightx=1; fill=HORIZONTAL;
        panel.add(desc, this);
        /*
         * Bottom part of the pane with a table showing the configuration.
         * Users can enable or disable some aspects of the test.
         */
        gridx=0; gridy++; gridwidth=2; weighty=1;
        anchor=CENTER; fill=BOTH; insets.top=12;
        final JTabbedPane tabs = new JTabbedPane();
        panel.add(tabs, this);
        /*
         * If new tabs are added below, make sure that the index of the "Exception"
         * tab match the index given to `tabs.setEnabledAt(…)` in the listener.
         */
        tabs.addTab("Factories",     new JScrollPane(factories));
        tabs.addTab("Configuration", new JScrollPane(configuration));
        tabs.addTab("Stack trace",   new JScrollPane(exception));
        exception.addPropertyChangeListener("enabled", (event) -> {
            tabs.setEnabledAt(2, (Boolean) event.getNewValue());
            // Number 2 above is the index of "Exception" tab.
        });
        exception.setEditable(false);
        panel.setOpaque(false);
        return panel;
    }

    /**
     * Creates a new label with the given text. The created label will be a header
     * for the given component.
     *
     * @param  title     text of the label to create.
     * @param  labelFor  the component for which to create a label.
     * @return the label for the given component.
     */
    private static JLabel createLabel(final String title, final JComponent labelFor) {
        final JLabel label = new JLabel(title);
        label.setLabelFor(labelFor);
        return label;
    }

    /**
     * Invoked by the layout manager when a component is added.
     *
     * @return a copy of this builder.
     */
    @Override
    @SuppressWarnings("CloneDoesntCallSuperClone")          // Okay because this class is final.
    public Object clone() {
        return new GridBagConstraints(gridx, gridy, gridwidth, gridheight,
                weightx, weighty, anchor, fill, (Insets) insets.clone(), ipadx, ipady);
    }
}
