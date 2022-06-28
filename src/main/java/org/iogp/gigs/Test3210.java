package org.iogp.gigs;

import org.iogp.gigs.internal.geoapi.Configuration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.opengis.referencing.IdentifiedObject;
import org.opengis.referencing.crs.*;
import org.opengis.referencing.cs.*;
import org.opengis.referencing.datum.*;
import org.opengis.util.FactoryException;

import javax.measure.Unit;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

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
 *   <td>The geoscience software should accept the test data. The order in which the projection parameters
 *       are entered is not critical, although that given in the test dataset is recommended.</td>
 * </tr></table>
 *
 *
 * <h2>Usage example</h2>
 * in order to specify their factories and run the tests in a JUnit framework, implementers can
 * define a subclass in their own test suite as in the example below:
 *
 * <blockquote><pre>public class MyTest extends Test3010 {
 *    public MyTest() {
 *        super(new MyDatumFactory(), new MyDatumAuthorityFactory(),
 *          new MyCSFactory(), new MyCRSFactory(),
 *          new MyCoordinateOperationFactory(), new MyMathTransformFactory(),
 *          new MyCoordinateOperationAuthorityFactory());
 *    }
 *}</pre></blockquote>
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
     * This is used only for tests with user definitions for CRS components.
     *
     * @see #setUserComponents(TestMethod, TestMethod)
     */
    private Test3209 datumTest;

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
    private final EPSGMock epsgFactory;

    /**
     * Creates a new test using the given factory. If a given factory is {@code null},
     * then the tests which depend on it will be skipped.
     *
     * @param csFactory              factory for creating {@code CoordinateSystem} instances.
     * @param crsFactory             factory for creating {@link GeodeticCRS} instances.
     * @param datumFactory           factory for creating {@link VerticalDatum} instances.
     */
    public Test3210(final CSFactory csFactory, final CRSFactory crsFactory, DatumFactory datumFactory) {
        this.crsFactory = crsFactory;
        this.csFactory = csFactory;
        this.datumFactory = datumFactory;
        this.epsgFactory  = new EPSGMock(units, datumFactory, csFactory, validators);
    }

    /**
     * Returns information about the configuration of the test which has been run.
     * This method returns a map containing:
     *
     * <ul>
     *   <li>All the following values associated to the {@link org.opengis.test.Configuration.Key} of the same name:
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
    public Configuration configuration() {
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
     * Verifies a vertical CRS for the given properties and verify its properties after construction.
     *
     * @throws FactoryException if an error occurred while creating the CRS instance.
     */
    final void verifyVerticalCRS() throws FactoryException
    {
        if (skipTests) {
            return;
        }
        if (crsFactory != null) {
            final VerticalCRS crs = getIdentifiedObject();
            assertNotNull(crs, "CRSFactory.createGeographicCRS(…) shall not return null.");
            validators.validate(crs);
            verifyIdentification(crs, getName(), String.valueOf(getCode()));

            if (datumTest != null) {
                datumTest.copyConfigurationFrom(this);
                datumTest.setIdentifiedObject(datum);
                datumTest.verifyVerticalDatum();
            }
        }
    }

    /**
     * Creates a user-defined datum by executing the specified method from the {@link Test3209} class.
     *
     * @param  factory           the test method to use for creating the datum.
     * @throws FactoryException  if an error occurred while creating the datum.
     */
    private void createDatum(final TestMethod<Test3209> factory) throws FactoryException {
        datumTest = new Test3209(datumFactory);
        datumTest.skipTests = true;
        factory.test(datumTest);
        datum = datumTest.getIdentifiedObject();
    }

    /**
     * Tests “GIGS vertCRS U1 depth” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>GIGS vertical CRS code: <b>64502</b></li>
     *   <li>GIGS vertical name: <b>GIGS vertCRS U1 depth</b></li>
     *   <li>EPSG equivalence: <b>5336 – Black Sea depth</b></li>
     *   <li>EPSG coordinate system code: <b>6498</b></li>
     *   <li>Axis 1 name: <b>Gravity-related depth</b></li>
     *   <li>Axis 1 abbreviation: <b>D</b></li>
     *   <li>Axis 1 orientation: <b>down</b></li>
     *   <li>Axis 1 unit: <b>metre</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the vertical CRS from the properties.
     */
    @Test
    @DisplayName("GIGS vertCRS U1 depth")
    public void GIGS_64502() throws FactoryException {
        setCodeAndName(64502, "GIGS vertCRS U1 depth");
        createDatum(Test3209::GIGS_66601);
        CoordinateSystemAxis axis1 = epsgFactory.createCoordinateSystemAxis("Gravity-related depth", "D", AxisDirection.DOWN, units.metre());
        verticalCS = epsgFactory.createVerticalCS("6498", axis1);
        verifyVerticalCRS();
    }


    /**
     * Tests “GIGS vertCRS U1 height” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>GIGS vertical CRS code: <b>64501</b></li>
     *   <li>GIGS vertical name: <b>GIGS vertCRS U1 height</b></li>
     *   <li>EPSG equivalence: <b>5735 – Black Sea height</b></li>
     *   <li>EPSG coordinate system code: <b>6499</b></li>
     *   <li>Axis 1 name: <b>Gravity-related height</b></li>
     *   <li>Axis 1 abbreviation: <b>H</b></li>
     *   <li>Axis 1 orientation: <b>up</b></li>
     *   <li>Axis 1 unit: <b>metre</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the vertical CRS from the properties.
     */
    @Test
    @DisplayName("GIGS vertCRS U1 height")
    public void GIGS_64501() throws FactoryException {
        setCodeAndName(64501, "GIGS vertCRS U1 height");
        createDatum(Test3209::GIGS_66601);
        CoordinateSystemAxis axis1 = epsgFactory.createCoordinateSystemAxis("Gravity-related height", "H", AxisDirection.UP, units.metre());
        verticalCS = epsgFactory.createVerticalCS("6499", axis1);
        verifyVerticalCRS();
    }


    /**
     * Tests “GIGS vertCRS U2 depth” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>GIGS vertical CRS code: <b>64504</b></li>
     *   <li>GIGS vertical name: <b>GIGS vertCRS U2 depth</b></li>
     *   <li>EPSG coordinate system code: <b>6495</b></li>
     *   <li>Axis 1 name: <b>Gravity-related depth</b></li>
     *   <li>Axis 1 abbreviation: <b>D</b></li>
     *   <li>Axis 1 orientation: <b>down</b></li>
     *   <li>Axis 1 unit: <b>foot</b></li>
     * </ul>
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
        CoordinateSystemAxis axis1 = epsgFactory.createCoordinateSystemAxis("Gravity-related depth", "D", AxisDirection.DOWN, units.foot());
        verticalCS = epsgFactory.createVerticalCS("6495", axis1);
        verifyVerticalCRS();
    }


    /**
     * Tests “GIGS vertCRS U2 height” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>GIGS vertical CRS code: <b>64503</b></li>
     *   <li>GIGS vertical name: <b>GIGS vertCRS U2 height</b></li>
     *   <li>EPSG coordinate system code: <b>1030</b></li>
     *   <li>Axis 1 name: <b>Gravity-related height</b></li>
     *   <li>Axis 1 abbreviation: <b>H</b></li>
     *   <li>Axis 1 orientation: <b>up</b></li>
     *   <li>Axis 1 unit: <b>foot</b></li>
     * </ul>
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
        CoordinateSystemAxis axis1 = epsgFactory.createCoordinateSystemAxis("Gravity-related height", "H", AxisDirection.UP, units.foot());
        verticalCS = epsgFactory.createVerticalCS("1030", axis1);
        verifyVerticalCRS();
    }


    /**
     * Tests “GIGS vertCRS V1 depth” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>GIGS vertical CRS code: <b>64506</b></li>
     *   <li>GIGS vertical name: <b>GIGS vertCRS V1 depth</b></li>
     *   <li>EPSG equivalence: <b>5612 – Baltic 1977 depth</b></li>
     *   <li>EPSG coordinate system code: <b>6498</b></li>
     *   <li>Axis 1 name: <b>Gravity-related depth</b></li>
     *   <li>Axis 1 abbreviation: <b>D</b></li>
     *   <li>Axis 1 orientation: <b>down</b></li>
     *   <li>Axis 1 unit: <b>metre</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the vertical CRS from the properties.
     */
    @Test
    @DisplayName("GIGS vertCRS V1 depth")
    public void GIGS_64506() throws FactoryException {
        setCodeAndName(64506, "GIGS vertCRS V1 depth");
        createDatum(Test3209::GIGS_66602);
        CoordinateSystemAxis axis1 = epsgFactory.createCoordinateSystemAxis("Gravity-related depth", "D", AxisDirection.DOWN, units.metre());
        verticalCS = epsgFactory.createVerticalCS("6498", axis1);
        verifyVerticalCRS();
    }


    /**
     * Tests “GIGS vertCRS V1 height” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>GIGS vertical CRS code: <b>64505</b></li>
     *   <li>GIGS vertical name: <b>GIGS vertCRS V1 height</b></li>
     *   <li>EPSG equivalence: <b>5705 – Baltic 1977 height</b></li>
     *   <li>EPSG coordinate system code: <b>6499</b></li>
     *   <li>Axis 1 name: <b>Gravity-related height</b></li>
     *   <li>Axis 1 abbreviation: <b>H</b></li>
     *   <li>Axis 1 orientation: <b>up</b></li>
     *   <li>Axis 1 unit: <b>metre</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the vertical CRS from the properties.
     */
    @Test
    @DisplayName("GIGS vertCRS V1 height")
    public void GIGS_64505() throws FactoryException {
        setCodeAndName(64505, "GIGS vertCRS V1 height");
        createDatum(Test3209::GIGS_66602);
        CoordinateSystemAxis axis1 = epsgFactory.createCoordinateSystemAxis("Gravity-related height", "H", AxisDirection.UP, units.metre());
        verticalCS = epsgFactory.createVerticalCS("6499", axis1);
        verifyVerticalCRS();
    }


    /**
     * Tests “GIGS vertCRS V2 height” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>GIGS vertical CRS code: <b>64509</b></li>
     *   <li>GIGS vertical name: <b>GIGS vertCRS V2 height</b></li>
     *   <li>EPSG coordinate system code: <b>6497</b></li>
     *   <li>Axis 1 name: <b>Gravity-related height</b></li>
     *   <li>Axis 1 abbreviation: <b>H</b></li>
     *   <li>Axis 1 orientation: <b>up</b></li>
     *   <li>Axis 1 unit: <b>ftUS</b></li>
     * </ul>
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
        CoordinateSystemAxis axis1 = epsgFactory.createCoordinateSystemAxis("Gravity-related height", "H", AxisDirection.UP, units.footSurveyUS());
        verticalCS = epsgFactory.createVerticalCS("6497", axis1);
        verifyVerticalCRS();
    }


    /**
     * Tests “GIGS vertCRS W1 depth” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>GIGS vertical CRS code: <b>64508</b></li>
     *   <li>GIGS vertical name: <b>GIGS vertCRS W1 depth</b></li>
     *   <li>EPSG equivalence: <b>5706 – Caspian depth</b></li>
     *   <li>EPSG coordinate system code: <b>6498</b></li>
     *   <li>Axis 1 name: <b>Gravity-related depth</b></li>
     *   <li>Axis 1 abbreviation: <b>D</b></li>
     *   <li>Axis 1 orientation: <b>down</b></li>
     *   <li>Axis 1 unit: <b>metre</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the vertical CRS from the properties.
     */
    @Test
    @DisplayName("GIGS vertCRS W1 depth")
    public void GIGS_64508() throws FactoryException {
        setCodeAndName(64508, "GIGS vertCRS W1 depth");
        createDatum(Test3209::GIGS_66603);
        CoordinateSystemAxis axis1 = epsgFactory.createCoordinateSystemAxis("Gravity-related depth", "D", AxisDirection.DOWN, units.metre());
        verticalCS = epsgFactory.createVerticalCS("6498", axis1);
        verifyVerticalCRS();
    }


    /**
     * Tests “GIGS vertCRS W1 height” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>GIGS vertical CRS code: <b>64507</b></li>
     *   <li>GIGS vertical name: <b>GIGS vertCRS W1 height</b></li>
     *   <li>EPSG equivalence: <b>5611 – Caspian height</b></li>
     *   <li>EPSG coordinate system code: <b>6499</b></li>
     *   <li>Axis 1 name: <b>Gravity-related height</b></li>
     *   <li>Axis 1 abbreviation: <b>H</b></li>
     *   <li>Axis 1 orientation: <b>up</b></li>
     *   <li>Axis 1 unit: <b>metre</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the vertical CRS from the properties.
     */
    @Test
    @DisplayName("GIGS vertCRS W1 height")
    public void GIGS_64507() throws FactoryException {
        setCodeAndName(64507, "GIGS vertCRS W1 height");
        createDatum(Test3209::GIGS_66603);
        CoordinateSystemAxis axis1 = epsgFactory.createCoordinateSystemAxis("Gravity-related height", "H", AxisDirection.UP, units.metre());
        verticalCS = epsgFactory.createVerticalCS("6499", axis1);
        verifyVerticalCRS();
    }
}
