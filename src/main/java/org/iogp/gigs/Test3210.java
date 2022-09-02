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
package org.iogp.gigs;

import java.util.Map;
import javax.measure.Unit;
import org.opengis.util.FactoryException;
import org.opengis.referencing.crs.CRSFactory;
import org.opengis.referencing.crs.VerticalCRS;
import org.opengis.referencing.cs.AxisDirection;
import org.opengis.referencing.cs.CSFactory;
import org.opengis.referencing.cs.CoordinateSystemAxis;
import org.opengis.referencing.cs.VerticalCS;
import org.opengis.referencing.datum.DatumFactory;
import org.opengis.referencing.datum.VerticalDatum;
import org.iogp.gigs.internal.geoapi.Configuration;
import org.iogp.gigs.internal.geoapi.PseudoEpsgFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Verifies that the software allows correct definition of a user-defined vertical CRS.
 *
 * <table class="gigs">
 * <caption>Test description</caption>
 * <tr>
 *   <th>Test method:</th>
 *   <td>Create user-defined vertical CRS.</td>
 * </tr><tr>
 *   <th>Test data:</th>
 *   <td><a href="https://github.com/IOGP-GIGS/GIGSTestDataset/blob/main/GIGSTestDatasetFiles/GIGS%203200%20User-defined%20Geodetic%20Data%20Objects%20test%20data/ASCII/GIGS_user_3210_VerticalCRS.txt">{@code GIGS_user_3210_VerticalCRS.txt}</a>
 * </tr><tr>
 *   <th>Tested API:</th>
 *   <td>{@link CRSFactory#createVerticalCRS(Map, VerticalDatum, VerticalCS)} and<br>
 *       {@link CSFactory#createVerticalCS(Map, CoordinateSystemAxis)}}.</td>
 * </tr><tr>
 *   <th>Expected result:</th>
 *   <td>The geoscience software should accept the test data. The properties of the created objects will
 *  *       be compared with the properties given to the factory method.</td>
 * </tr></table>
 *
 *
 * <h2>Usage example</h2>
 * in order to specify their factories and run the tests in a JUnit framework, implementers can
 * define a subclass in their own test suite as in the example below:
 *
 * {@snippet lang="java" :
 * public class MyTest extends Test3210 {
 *     public MyTest() {
 *         super(new MyFactories());
 *     }
 * }
 * }
 *
 * @author  Michael Arneson (INT)
 * @version 1.0
 * @since   1.0
 */
@DisplayName("User-defined vertical CRS")
public class Test3210 extends Series3000<VerticalCRS> {
    /**
     * The vertical CRS created by the factory,
     * or {@code null} if not yet created or if CRS creation failed.
     */
    private VerticalCRS crs;

    /**
     * The vertical coordinate system used for the vertical CRS created by this factory.
     */
    private VerticalCS verticalCS;

    /**
     * The vertical datum used for the vertical CRS created by this factory.
     */
    private VerticalDatum datum;

    /**
     * Data about the vertical datum of the vertical CRS.
     *
     * @see #createDatum(TestMethod)
     */
    private final Test3209 datumTest;

    /**
     * Factory to use for building {@link VerticalCRS} instances, or {@code null} if none.
     */
    protected final CRSFactory crsFactory;

    /**
     * The factory to use for creating coordinate system instances.
     */
    protected final CSFactory csFactory;

    /**
     * Factory to use for building {@link VerticalDatum} instances, or {@code null} if none.
     * May also be used for building {@link Ellipsoid} and {@link PrimeMeridian} components.
     */
    protected final DatumFactory datumFactory;

    /**
     * The factory to use for creating coordinate system instances.
     */
    private final PseudoEpsgFactory epsgFactory;

    /**
     * Creates a new test using the given factories.
     * The factories needed by this class are {@link CRSFactory}, {@link CSFactory} and {@link DatumFactory}.
     * If a requested factory is {@code null}, then the tests which depend on it will be skipped.
     *
     * @param factories  factories for creating the instances to test.
     */
    public Test3210(final Factories factories) {
        crsFactory   = factories.crsFactory;
        csFactory    = factories.csFactory;
        datumFactory = factories.datumFactory;
        epsgFactory  = new PseudoEpsgFactory(units, datumFactory, csFactory, crsFactory, null, null, validators);

        datumTest = new Test3209(datumFactory);
        datumTest.skipTests = true;
        datumTest.skipIdentificationCheck = true;
    }

    /**
     * Returns information about the configuration of the test which has been run.
     * This method returns a map containing:
     *
     * <ul>
     *   <li>All the following values associated to the {@link Configuration.Key} of the same name:
     *     <ul>
     *       <li>{@link #isFactoryPreservingUserValues}</li>
     *       <li>{@link #csFactory}</li>
     *       <li>{@link #crsFactory}</li>
     *       <li>{@link #datumFactory}</li>
     *     </ul>
     *   </li>
     * </ul>
     *
     * @return the configuration of the test being run.
     */
    @Override
    Configuration configuration() {
        final Configuration op = super.configuration();
        assertNull(op.put(Configuration.Key.csFactory, csFactory));
        assertNull(op.put(Configuration.Key.crsFactory, crsFactory));
        assertNull(op.put(Configuration.Key.datumFactory, datumFactory));
        return op;
    }

    /**
     * Returns the vertical CRS instance to be tested. When this method is invoked for the first time,
     * it creates the projected CRS to test by invoking the corresponding method from {@link CRSFactory}
     * with the current {@link #properties properties} map, vertical datum, and vertical coordinate system in the
     * arguments. The created object is then cached and returned in all subsequent invocations of this method.
     *
     * @return the vertical CRS instance to test.
     * @throws FactoryException if an error occurred while creating the vertical CRS instance.
     */
    @Override
    public VerticalCRS getIdentifiedObject() throws FactoryException {
        if (crs == null) {
            crs = crsFactory.createVerticalCRS(properties, datum, verticalCS);
        }
        return crs;
    }

    /**
     * Sets the vertical CRS instance to verify. This method is invoked only by other test classes which need to
     * verify the vertical CRS contained in a CRS instead of the vertical CRS immediately after creation.
     *
     * @param  instance  the instance to verify.
     */
    final void setIdentifiedObject(final VerticalCRS instance) {
        crs = instance;
    }

    /**
     * Creates a vertical coordinate system from a code.
     *
     * @param  code  EPSG code of the Cartesian coordinate system to create.
     * @throws FactoryException if an error occurred while creating the coordinate system.
     */
    private void createVerticalCS(final int code) throws FactoryException {
        verticalCS = epsgFactory.createVerticalCS(String.valueOf(code));
        validators.validate(verticalCS);
    }

    /**
     * Verifies that the specified coordinate system axis has the expected values.
     *
     * @param name          the expected name.
     * @param abbreviation  the expected abbreviation.
     * @param direction     the expected axis direction.
     * @param unit          the expected axis unit.
     */
    private void verifyAxis(final String name, final String abbreviation,
                            final AxisDirection direction, final Unit<?> unit)
    {
        if (skipTests) {
            return;
        }
        final CoordinateSystemAxis axis = verticalCS.getAxis(0);
        assertEquals(name,         axis.getName().getCode());
        assertEquals(abbreviation, axis.getAbbreviation(), name);
        assertEquals(direction,    axis.getDirection(), name);
        assertEquals(unit,         axis.getUnit(), name);
    }

    /**
     * Verifies a vertical CRS for the given properties and verify its properties after construction.
     *
     * @throws FactoryException if an error occurred while creating the CRS instance.
     */
    final void verifyVerticalCRS() throws FactoryException {
        if (skipTests) {
            return;
        }
        if (crsFactory != null) {
            @SuppressWarnings("LocalVariableHidesMemberVariable")
            final VerticalCRS crs = getIdentifiedObject();
            assertNotNull(crs, "CRSFactory.createGeographicCRS(…) shall not return null.");
            validators.validate(crs);
            verifyIdentification(crs, getName(), String.valueOf(getCode()));

            datumTest.copyConfigurationFrom(this);
            datumTest.setIdentifiedObject(datum);
//TODO      datumTest.verifyVerticalDatum();
        }
    }

    /**
     * Creates a user-defined datum by executing the specified method from the {@link Test3209} class.
     *
     * @param  factory           the test method to use for creating the datum.
     * @throws FactoryException  if an error occurred while creating the datum.
     */
    private void createDatum(final TestMethod<Test3209> factory) throws FactoryException {
        factory.initialize(datumTest);
        datum = datumTest.getIdentifiedObject();
    }

    /**
     * Tests “GIGS vertCRS U1 depth” vertical CRS creation from the factory.
     *
     * <ul>
     *   <li>GIGS vertical CRS code: <b>64502</b></li>
     *   <li>GIGS vertical name: <b>GIGS vertCRS U1 depth</b></li>
     *   <li>EPSG equivalence: <b>5336 – Black Sea depth</b></li>
     *   <li>EPSG coordinate system code: <b>6498</b></li>
     * </ul>
     * <table class="gigs">
     *   <caption>Coordinate system axes</caption>
     *   <tr><th>Name</th><th>Abbreviation</th><th>Orientation</th><th>Unit</th></tr>
     *   <tr><td>Gravity-related depth</td><td>D</td><td>down</td><td>metre</td></tr>
     * </table>
     *
     * @throws FactoryException if an error occurred while creating the vertical CRS from the properties.
     */
    @Test
    @DisplayName("GIGS vertCRS U1 depth")
    public void GIGS_64502() throws FactoryException {
        setCodeAndName(64502, "GIGS vertCRS U1 depth");
        createDatum(Test3209::GIGS_66601);
        createVerticalCS(6498);
        verifyAxis("Gravity-related depth", "D", AxisDirection.DOWN, units.metre());
        verifyVerticalCRS();
    }

    /**
     * Tests “GIGS vertCRS U1 height” vertical CRS creation from the factory.
     *
     * <ul>
     *   <li>GIGS vertical CRS code: <b>64501</b></li>
     *   <li>GIGS vertical name: <b>GIGS vertCRS U1 height</b></li>
     *   <li>EPSG equivalence: <b>5735 – Black Sea height</b></li>
     *   <li>EPSG coordinate system code: <b>6499</b></li>
     * </ul>
     * <table class="gigs">
     *   <caption>Coordinate system axes</caption>
     *   <tr><th>Name</th><th>Abbreviation</th><th>Orientation</th><th>Unit</th></tr>
     *   <tr><td>Gravity-related height</td><td>H</td><td>up</td><td>metre</td></tr>
     * </table>
     *
     * @throws FactoryException if an error occurred while creating the vertical CRS from the properties.
     */
    @Test
    @DisplayName("GIGS vertCRS U1 height")
    public void GIGS_64501() throws FactoryException {
        setCodeAndName(64501, "GIGS vertCRS U1 height");
        createDatum(Test3209::GIGS_66601);
        createVerticalCS(6499);
        verifyAxis("Gravity-related height", "H", AxisDirection.UP, units.metre());
        verifyVerticalCRS();
    }

    /**
     * Tests “GIGS vertCRS U2 depth” vertical CRS creation from the factory.
     *
     * <ul>
     *   <li>GIGS vertical CRS code: <b>64504</b></li>
     *   <li>GIGS vertical name: <b>GIGS vertCRS U2 depth</b></li>
     *   <li>EPSG coordinate system code: <b>6495</b></li>
     * </ul>
     * <table class="gigs">
     *   <caption>Coordinate system axes</caption>
     *   <tr><th>Name</th><th>Abbreviation</th><th>Orientation</th><th>Unit</th></tr>
     *   <tr><td>Gravity-related depth</td><td>D</td><td>down</td><td>foot</td></tr>
     * </table>
     *
     * Remarks: No direct EPSG equivalent.
     * But would be Black Sea depth (ft).
     *
     * @throws FactoryException if an error occurred while creating the vertical CRS from the properties.
     */
    @Test
    @DisplayName("GIGS vertCRS U2 depth")
    public void GIGS_64504() throws FactoryException {
        setCodeAndName(64504, "GIGS vertCRS U2 depth");
        createDatum(Test3209::GIGS_66601);
        createVerticalCS(6495);
        verifyAxis("Gravity-related depth", "D", AxisDirection.DOWN, units.foot());
        verifyVerticalCRS();
    }

    /**
     * Tests “GIGS vertCRS U2 height” vertical CRS creation from the factory.
     *
     * <ul>
     *   <li>GIGS vertical CRS code: <b>64503</b></li>
     *   <li>GIGS vertical name: <b>GIGS vertCRS U2 height</b></li>
     *   <li>EPSG coordinate system code: <b>1030</b></li>
     * </ul>
     * <table class="gigs">
     *   <caption>Coordinate system axes</caption>
     *   <tr><th>Name</th><th>Abbreviation</th><th>Orientation</th><th>Unit</th></tr>
     *   <tr><td>Gravity-related height</td><td>H</td><td>up</td><td>foot</td></tr>
     * </table>
     *
     * Remarks: No direct EPSG equivalent.
     * But would be Black Sea height (ft).
     *
     * @throws FactoryException if an error occurred while creating the vertical CRS from the properties.
     */
    @Test
    @DisplayName("GIGS vertCRS U2 height")
    public void GIGS_64503() throws FactoryException {
        setCodeAndName(64503, "GIGS vertCRS U2 height");
        createDatum(Test3209::GIGS_66601);
        createVerticalCS(1030);
        verifyAxis("Gravity-related height", "H", AxisDirection.UP, units.foot());
        verifyVerticalCRS();
    }

    /**
     * Tests “GIGS vertCRS V1 depth” vertical CRS creation from the factory.
     *
     * <ul>
     *   <li>GIGS vertical CRS code: <b>64506</b></li>
     *   <li>GIGS vertical name: <b>GIGS vertCRS V1 depth</b></li>
     *   <li>EPSG equivalence: <b>5612 – Baltic 1977 depth</b></li>
     *   <li>EPSG coordinate system code: <b>6498</b></li>
     * </ul>
     * <table class="gigs">
     *   <caption>Coordinate system axes</caption>
     *   <tr><th>Name</th><th>Abbreviation</th><th>Orientation</th><th>Unit</th></tr>
     *   <tr><td>Gravity-related depth</td><td>D</td><td>down</td><td>metre</td></tr>
     * </table>
     *
     * @throws FactoryException if an error occurred while creating the vertical CRS from the properties.
     */
    @Test
    @DisplayName("GIGS vertCRS V1 depth")
    public void GIGS_64506() throws FactoryException {
        setCodeAndName(64506, "GIGS vertCRS V1 depth");
        createDatum(Test3209::GIGS_66602);
        createVerticalCS(6498);
        verifyAxis("Gravity-related depth", "D", AxisDirection.DOWN, units.metre());
        verifyVerticalCRS();
    }

    /**
     * Tests “GIGS vertCRS V1 height” vertical CRS creation from the factory.
     *
     * <ul>
     *   <li>GIGS vertical CRS code: <b>64505</b></li>
     *   <li>GIGS vertical name: <b>GIGS vertCRS V1 height</b></li>
     *   <li>EPSG equivalence: <b>5705 – Baltic 1977 height</b></li>
     *   <li>EPSG coordinate system code: <b>6499</b></li>
     * </ul>
     * <table class="gigs">
     *   <caption>Coordinate system axes</caption>
     *   <tr><th>Name</th><th>Abbreviation</th><th>Orientation</th><th>Unit</th></tr>
     *   <tr><td>Gravity-related height</td><td>H</td><td>up</td><td>metre</td></tr>
     * </table>
     *
     * @throws FactoryException if an error occurred while creating the vertical CRS from the properties.
     */
    @Test
    @DisplayName("GIGS vertCRS V1 height")
    public void GIGS_64505() throws FactoryException {
        setCodeAndName(64505, "GIGS vertCRS V1 height");
        createDatum(Test3209::GIGS_66602);
        createVerticalCS(6499);
        verifyAxis("Gravity-related height", "H", AxisDirection.UP, units.metre());
        verifyVerticalCRS();
    }

    /**
     * Tests “GIGS vertCRS V2 height” vertical CRS creation from the factory.
     *
     * <ul>
     *   <li>GIGS vertical CRS code: <b>64509</b></li>
     *   <li>GIGS vertical name: <b>GIGS vertCRS V2 height</b></li>
     *   <li>EPSG coordinate system code: <b>6497</b></li>
     * </ul>
     * <table class="gigs">
     *   <caption>Coordinate system axes</caption>
     *   <tr><th>Name</th><th>Abbreviation</th><th>Orientation</th><th>Unit</th></tr>
     *   <tr><td>Gravity-related height</td><td>H</td><td>up</td><td>ftUS</td></tr>
     * </table>
     *
     * Remarks: No direct EPSG equivalent.
     * But would be Baltic 1977 height (ftUS).
     *
     * @throws FactoryException if an error occurred while creating the vertical CRS from the properties.
     */
    @Test
    @DisplayName("GIGS vertCRS V2 height")
    public void GIGS_64509() throws FactoryException {
        setCodeAndName(64509, "GIGS vertCRS V2 height");
        createDatum(Test3209::GIGS_66602);
        createVerticalCS(6497);
        verifyAxis("Gravity-related height", "H", AxisDirection.UP, units.footSurveyUS());
        verifyVerticalCRS();
    }

    /**
     * Tests “GIGS vertCRS W1 depth” vertical CRS creation from the factory.
     *
     * <ul>
     *   <li>GIGS vertical CRS code: <b>64508</b></li>
     *   <li>GIGS vertical name: <b>GIGS vertCRS W1 depth</b></li>
     *   <li>EPSG equivalence: <b>5706 – Caspian depth</b></li>
     *   <li>EPSG coordinate system code: <b>6498</b></li>
     * </ul>
     * <table class="gigs">
     *   <caption>Coordinate system axes</caption>
     *   <tr><th>Name</th><th>Abbreviation</th><th>Orientation</th><th>Unit</th></tr>
     *   <tr><td>Gravity-related depth</td><td>D</td><td>down</td><td>metre</td></tr>
     * </table>
     *
     * @throws FactoryException if an error occurred while creating the vertical CRS from the properties.
     */
    @Test
    @DisplayName("GIGS vertCRS W1 depth")
    public void GIGS_64508() throws FactoryException {
        setCodeAndName(64508, "GIGS vertCRS W1 depth");
        createDatum(Test3209::GIGS_66603);
        createVerticalCS(6498);
        verifyAxis("Gravity-related depth", "D", AxisDirection.DOWN, units.metre());
        verifyVerticalCRS();
    }

    /**
     * Tests “GIGS vertCRS W1 height” vertical CRS creation from the factory.
     *
     * <ul>
     *   <li>GIGS vertical CRS code: <b>64507</b></li>
     *   <li>GIGS vertical name: <b>GIGS vertCRS W1 height</b></li>
     *   <li>EPSG equivalence: <b>5611 – Caspian height</b></li>
     *   <li>EPSG coordinate system code: <b>6499</b></li>
     * </ul>
     * <table class="gigs">
     *   <caption>Coordinate system axes</caption>
     *   <tr><th>Name</th><th>Abbreviation</th><th>Orientation</th><th>Unit</th></tr>
     *   <tr><td>Gravity-related height</td><td>H</td><td>up</td><td>metre</td></tr>
     * </table>
     *
     * @throws FactoryException if an error occurred while creating the vertical CRS from the properties.
     */
    @Test
    @DisplayName("GIGS vertCRS W1 height")
    public void GIGS_64507() throws FactoryException {
        setCodeAndName(64507, "GIGS vertCRS W1 height");
        createDatum(Test3209::GIGS_66603);
        createVerticalCS(6499);
        verifyAxis("Gravity-related height", "H", AxisDirection.UP, units.metre());
        verifyVerticalCRS();
    }
}
