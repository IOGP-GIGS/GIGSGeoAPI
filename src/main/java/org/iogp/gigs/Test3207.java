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

import org.iogp.gigs.internal.geoapi.Configuration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.opengis.referencing.crs.*;
import org.opengis.referencing.cs.*;
import org.opengis.referencing.datum.*;
import org.opengis.referencing.operation.*;
import org.opengis.util.FactoryException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Verifies that the software allows correct definition of a user-defined projected CRS.
 *
 * <table class="gigs">
 * <caption>Test description</caption>
 * <tr>
 *   <th>Test method:</th>
 *   <td>Create user-defined projected CRS.</td>
 * </tr><tr>
 *   <th>Test data:</th>
 *   <td><a href="https://github.com/IOGP-GIGS/GIGSTestDataset/blob/main/GIGSTestDatasetFiles/GIGS%203200%20User-defined%20Geodetic%20Data%20Objects%20test%20data/ASCII/GIGS_user_3207_ProjectedCRS.txt">{@code GIGS_user_3207_ProjectedCRS.txt}</a>
 * </tr><tr>
 *   <th>Tested API:</th>
 *   <td>{@link CRSFactory#createProjectedCRS(Map, GeographicCRS, Conversion, CartesianCS)} and<br>
 *       {@link CSFactory#createCartesianCS(Map, CoordinateSystemAxis, CoordinateSystemAxis)}.</td>
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
 * public class MyTest extends Test3207 {
 *     public MyTest() {
 *         super(new MyDatumFactory(), new MyDatumAuthorityFactory(),
 *               new MyCSFactory(), new MyCRSFactory(),
 *               new MyCoordinateOperationFactory(), new MyMathTransformFactory(),
 *               new MyCoordinateOperationAuthorityFactory());
 *     }
 * }
 * }
 *
 * @author  Michael Arneson (INT)
 * @version 1.0
 * @since   1.0
 */
@DisplayName("User-defined projected CRS")
public class Test3207 extends Series3000<ProjectedCRS> {
    /**
     * The projected CRS created by the factory,
     * or {@code null} if not yet created or if projected CRS creation failed.
     */
    private ProjectedCRS crs;

    /**
     * The base CRS of the projected CRS created by this factory.
     */
    private GeographicCRS baseCRS;

    /**
     * The coordinate conversion used for the projected CRS created by this factory.
     *
     * @see #copAuthorityFactory
     */
    private Conversion conversion;

    /**
     * The cartesian coordinate system used for the projected CRS created by this factory.
     */
    private CartesianCS cartesianCS;

    /**
     * Factory to use for building {@link Conversion} instances, or {@code null} if none.
     * This is the factory used by the {@link #getIdentifiedObject()} method.
     */
    protected final CoordinateOperationFactory copFactory;

    /**
     * The factory to use for fetching operation methods, or {@code null} if none.
     */
    protected final MathTransformFactory mtFactory;

    /**
     * Factory to use for building {@link ProjectedCRS} instances, or {@code null} if none.
     */
    protected final CRSFactory crsFactory;

    /**
     * The factory to use for creating coordinate system instances.
     */
    protected final CSFactory csFactory;

    /**
     * Factory to use for building {@link GeodeticDatum} instances, or {@code null} if none.
     * May also be used for building {@link Ellipsoid} and {@link PrimeMeridian} components.
     */
    protected final DatumFactory datumFactory;

    /**
     * Factory to use for building {@link GeodeticDatum} and {@link PrimeMeridian} components, or {@code null} if none.
     * This is used only for tests with EPSG codes for datum components.
     */
    protected final DatumAuthorityFactory datumAuthorityFactory;

    /**
     * Factory to use for building {@link Conversion} instances, or {@code null} if none.
     * This is the factory used by the {@link #getIdentifiedObject()} method.
     */
    protected final CoordinateOperationAuthorityFactory copAuthorityFactory;

    /**
     * The factory to use for creating coordinate system instances.
     */
    private final EPSGMock epsgFactory;

    /**
     * Data about the base CRS of the projected CRS.
     * This is used only for tests with user definitions for CRS components.
     *
     * @see #createBaseCRS(TestMethod)
     */
    private Test3205 baseCRSTest;

    /**
     * Data about the conversion of the projected CRS.
     * This is used only for tests with user definitions for CRS components.
     *
     * @see #createConversion(TestMethod)
     */
    private Test3206 conversionTest;

    /**
     * Creates a new test using the given factory. If a given factory is {@code null},
     * then the tests which depend on it will be skipped.
     *
     * @param datumFactory           factory for creating {@link GeodeticDatum} instances.
     * @param datumAuthorityFactory  factory for creating {@link Ellipsoid} and {@link PrimeMeridian} components from EPSG codes.
     * @param csFactory              factory for creating {@code CoordinateSystem} instances.
     * @param crsFactory             factory for creating {@link GeodeticCRS} instances.
     * @param copFactory             factory for creating {@link Transformation} instances.
     * @param mtFactory              factory for creating {@link Transformation} instances.
     * @param copAuthorityFactory    factory for creating {@link Transformation} instances.
     */
    public Test3207(final DatumFactory datumFactory, final DatumAuthorityFactory datumAuthorityFactory,
                    final CSFactory csFactory, final CRSFactory crsFactory,
                    final CoordinateOperationFactory copFactory, final MathTransformFactory mtFactory,
                    final CoordinateOperationAuthorityFactory copAuthorityFactory)
    {
        this.copFactory = copFactory;
        this.mtFactory = mtFactory;
        this.datumFactory = datumFactory;
        this.datumAuthorityFactory = datumAuthorityFactory;
        this.crsFactory   = crsFactory;
        this.csFactory  = csFactory;
        this.copAuthorityFactory = copAuthorityFactory;
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
     *       <li>{@link #copFactory}</li>
     *       <li>{@link #mtFactory}</li>
     *       <li>{@link #datumFactory}</li>
     *       <li>{@link #datumAuthorityFactory}</li>
     *       <li>{@link #csFactory}</li>
     *       <li>{@link #crsFactory}</li>
     *       <li>{@link #copAuthorityFactory}</li>
     *     </ul>
     *   </li>
     * </ul>
     *
     * @return the configuration of the test being run.
     */
    @Override
    public Configuration configuration() {
        final Configuration op = super.configuration();
        assertNull(op.put(Configuration.Key.copFactory, copFactory));
        assertNull(op.put(Configuration.Key.mtFactory,  mtFactory));
        assertNull(op.put(Configuration.Key.datumFactory, datumFactory));
        assertNull(op.put(Configuration.Key.datumAuthorityFactory, datumAuthorityFactory));
        assertNull(op.put(Configuration.Key.csFactory, csFactory));
        assertNull(op.put(Configuration.Key.crsFactory, crsFactory));
        assertNull(op.put(Configuration.Key.copAuthorityFactory, copAuthorityFactory));
        return op;
    }

    /**
     * Returns the projected CRS instance to be tested. When this method is invoked for the first time,
     * it creates the projected CRS to test by invoking the corresponding method from {@link CRSFactory}
     * with the current {@link #properties properties} map, base CRS, conversion, and cartesian coordinate system in the
     * arguments. The created object is then cached and returned in all subsequent invocations of this method.
     *
     * @return the projected CRS instance to test.
     * @throws FactoryException if an error occurred while creating the projected CRS instance.
     */
    @Override
    public ProjectedCRS getIdentifiedObject() throws FactoryException {
        if (crs == null) {
            crs = crsFactory.createProjectedCRS(properties, baseCRS, conversion, cartesianCS);
        }
        return crs;
    }

    /**
     * Creates a user-defined base CRS by executing the specified method from the {@link Test3205} class.
     *
     * @param  factory           the test method to use for creating the base CRS.
     * @throws FactoryException  if an error occurred while creating the base CRS.
     */
    private void createBaseCRS(final TestMethod<Test3205> factory) throws FactoryException {
        baseCRSTest = new Test3205(crsFactory, csFactory, datumFactory, datumAuthorityFactory);
        baseCRSTest.skipTests = true;
        factory.test(baseCRSTest);
        baseCRS = (GeographicCRS) baseCRSTest.getIdentifiedObject();
    }

    /**
     * Creates a user-defined conversion by executing the specified method from the {@link Test3206} class.
     *
     * @param  factory           the test method to use for creating the conversion.
     * @throws FactoryException  if an error occurred while creating the conversion.
     */
    private void createConversion(final TestMethod<Test3206> factory) throws FactoryException {
        conversionTest = new Test3206(copFactory, mtFactory);
        conversionTest.skipTests = true;
        factory.test(conversionTest);
        conversion = conversionTest.getIdentifiedObject();
    }

    /**
     * Creates a conversion from the EPSG factory
     *
     * @param  code  EPSG code of the conversion to create.
     * @throws FactoryException  if an error occurred while creating the conversion.
     */
    private void createConversion(final int code) throws FactoryException {
        conversion = (Conversion) copAuthorityFactory.createCoordinateOperation(String.valueOf(code));
    }

    /**
     * Creates a Cartesian coordinate system from a code and verify that it got the expected axes.
     *
     * @param  code   EPSG code of the Cartesian coordiante system to create.
     * @param  axis1  first expected axis.
     * @param  axis2  second expected axis.
     * @throws FactoryException  if an error occurred while creating the coordinate system.
     */
    private void createCartesianCS(final int code, final CoordinateSystemAxis axis1, final CoordinateSystemAxis axis2)
            throws FactoryException
    {
        cartesianCS = epsgFactory.createCartesianCS(String.valueOf(code));
        verifyAxis(axis1, cartesianCS.getAxis(0));
        verifyAxis(axis2, cartesianCS.getAxis(1));
    }

    /**
     * Verifies that the given coordinate system axis has the expected values.
     *
     * @param expected  expected axis.
     * @param actual    actual axis.
     */
    private static void verifyAxis(final CoordinateSystemAxis expected, final CoordinateSystemAxis actual) {
        final String name = expected.getName().getCode();
        assertEquals(name, actual.getName().getCode());
        assertEquals(expected.getAbbreviation(), actual.getAbbreviation(), name);
        assertEquals(expected.getDirection(), actual.getDirection(), name);
        assertEquals(expected.getUnit(), actual.getUnit(), name);
    }

    /**
     * Verifies the properties of the projected CRS given by {@link #getIdentifiedObject()}.
     *
     * @throws FactoryException if an error occurred while creating the projected CRS.
     */
    private void verifyProjectedCRS() throws FactoryException {
        if (skipTests) {
            return;
        }
        final String name = getName();
        final String code = getCode();
        final ProjectedCRS projectedCRS = getIdentifiedObject();
        assertNotNull(projectedCRS, "PrimeMeridian");
        verifyIdentification(projectedCRS, name, code);
        validators.validate(projectedCRS);
        // Projected CRS base CRS.
        if (baseCRSTest != null) {
            baseCRSTest.copyConfigurationFrom(this);
            baseCRSTest.setIdentifiedObject(baseCRS);
            baseCRSTest.verifyGeographicCRS();
        }
        // Projected CRS conversion.
        if (conversionTest != null) {
            conversionTest.copyConfigurationFrom(this);
            conversionTest.setIdentifiedObject(conversion);
            conversionTest.verifyConversion();
        }
        //validate the cartesian cs.
        validators.validate(cartesianCS);
        // Projected CRS coordinate system.
        final CartesianCS cs = crs.getCoordinateSystem();
        assertNotNull(crs, "ProjectedCRS.getCoordinateSystem()");
        assertEquals(2, cs.getDimension(), "ProjectedCRS.getCoordinateSystem().getDimension()");
    }

    /**
     * Tests “GIGS projCRS A1” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>GIGS projected CRS code: <b>62001</b></li>
     *   <li>GIGS projectedCRS name: <b>GIGS projCRS A1</b></li>
     *   <li>EPSG equivalence: <b>32631 – WGS 84 / UTM zone 31N</b></li>
     *   <li>GIGS base CRS code: <b>64003</b></li>
     *   <li>GIGS conversion code: <b>65001</b></li>
     *   <li>Conversion definition source: <b>Described by user</b> (build with {@link ObjectFactory})</li>
     *   <li>EPSG coordinate system code: <b>4400</b></li>
     * </ul>
     * <table class="ogc">
     *   <caption>Coordinate system axes</caption>
     *   <tr><th>Name</th><th>Abbreviation</th><th>Orientation</th><th>Unit</th></tr>
     *   <tr><td>Easting</td><td>E</td><td>east</td><td>metre</td></tr>
     *   <tr><td>Northing</td><td>N</td><td>north</td><td>metre</td></tr>
     * </table>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the properties.
     */
    @Test
    @DisplayName("GIGS projCRS A1")
    public void GIGS_62001() throws FactoryException {
        setCodeAndName(62001, "GIGS projCRS A1");
        createBaseCRS(Test3205::GIGS_64003);
        createConversion(Test3206::GIGS_65001);
        CoordinateSystemAxis axis1 = epsgFactory.createCoordinateSystemAxis("Easting", "E", AxisDirection.EAST, units.metre());
        CoordinateSystemAxis axis2 = epsgFactory.createCoordinateSystemAxis("Northing", "N", AxisDirection.NORTH, units.metre());
        createCartesianCS(4400, axis1, axis2);
        verifyProjectedCRS();
    }

    /**
     * Tests “GIGS projCRS A1-2” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>GIGS projected CRS code: <b>62002</b></li>
     *   <li>GIGS projectedCRS name: <b>GIGS projCRS A1-2</b></li>
     *   <li>GIGS base CRS code: <b>64003</b></li>
     *   <li>GIGS conversion code: <b>65001</b></li>
     *   <li>Conversion definition source: <b>Described by user</b> (build with {@link ObjectFactory})</li>
     *   <li>EPSG coordinate system code: <b>4500</b></li>
     * </ul>
     * <table class="ogc">
     *   <caption>Coordinate system axes</caption>
     *   <tr><th>Name</th><th>Abbreviation</th><th>Orientation</th><th>Unit</th></tr>
     *   <tr><td>Northing</td><td>N</td><td>north</td><td>metre</td></tr>
     *   <tr><td>Easting</td><td>E</td><td>east</td><td>metre</td></tr>
     * </table>
     *
     * Remarks: No direct EPSG equivalent.
     * Similar to WGS 84 / UTM zone 31N but with different Coordinate System.
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the properties.
     */
    @Test
    @DisplayName("GIGS projCRS A1-2")
    public void GIGS_62002() throws FactoryException {
        setCodeAndName(62002, "GIGS projCRS A1-2");
        createBaseCRS(Test3205::GIGS_64003);
        createConversion(Test3206::GIGS_65001);
        CoordinateSystemAxis axis1 = epsgFactory.createCoordinateSystemAxis("Northing", "N", AxisDirection.NORTH, units.metre());
        CoordinateSystemAxis axis2 = epsgFactory.createCoordinateSystemAxis("Easting", "E", AxisDirection.EAST, units.metre());
        createCartesianCS(4500, axis1, axis2);
        verifyProjectedCRS();
    }

    /**
     * Tests “GIGS projCRS A1-3” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>GIGS projected CRS code: <b>62003</b></li>
     *   <li>GIGS projectedCRS name: <b>GIGS projCRS A1-3</b></li>
     *   <li>GIGS base CRS code: <b>64003</b></li>
     *   <li>GIGS conversion code: <b>65001</b></li>
     *   <li>Conversion definition source: <b>Described by user</b> (build with {@link ObjectFactory})</li>
     *   <li>EPSG coordinate system code: <b>4499</b></li>
     * </ul>
     * <table class="ogc">
     *   <caption>Coordinate system axes</caption>
     *   <tr><th>Name</th><th>Abbreviation</th><th>Orientation</th><th>Unit</th></tr>
     *   <tr><td>Easting</td><td>X</td><td>east</td><td>metre</td></tr>
     *   <tr><td>Northing</td><td>Y</td><td>north</td><td>metre</td></tr>
     * </table>
     *
     * Remarks: No direct EPSG equivalent.
     * Similar to WGS 84 / UTM zone 31N but with different Coordinate System.
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the properties.
     */
    @Test
    @DisplayName("GIGS projCRS A1-3")
    public void GIGS_62003() throws FactoryException {
        setCodeAndName(62003, "GIGS projCRS A1-3");
        createBaseCRS(Test3205::GIGS_64003);
        createConversion(Test3206::GIGS_65001);
        CoordinateSystemAxis axis1 = epsgFactory.createCoordinateSystemAxis("Easting", "X", AxisDirection.EAST, units.metre());
        CoordinateSystemAxis axis2 = epsgFactory.createCoordinateSystemAxis("Northing", "Y", AxisDirection.NORTH, units.metre());
        createCartesianCS(4499, axis1, axis2);
        verifyProjectedCRS();
    }

    /**
     * Tests “GIGS projCRS A1-4” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>GIGS projected CRS code: <b>62004</b></li>
     *   <li>GIGS projectedCRS name: <b>GIGS projCRS A1-4</b></li>
     *   <li>GIGS base CRS code: <b>64003</b></li>
     *   <li>GIGS conversion code: <b>65001</b></li>
     *   <li>Conversion definition source: <b>Described by user</b> (build with {@link ObjectFactory})</li>
     *   <li>EPSG coordinate system code: <b>4532</b></li>
     * </ul>
     * <table class="ogc">
     *   <caption>Coordinate system axes</caption>
     *   <tr><th>Name</th><th>Abbreviation</th><th>Orientation</th><th>Unit</th></tr>
     *   <tr><td>Northing</td><td>Y</td><td>north</td><td>metre</td></tr>
     *   <tr><td>Easting</td><td>X</td><td>east</td><td>metre</td></tr>
     * </table>
     *
     * Remarks: No direct EPSG equivalent.
     * Similar to WGS 84 / UTM zone 31N but with different Coordinate System.
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the properties.
     */
    @Test
    @DisplayName("GIGS projCRS A1-4")
    public void GIGS_62004() throws FactoryException {
        setCodeAndName(62004, "GIGS projCRS A1-4");
        createBaseCRS(Test3205::GIGS_64003);
        createConversion(Test3206::GIGS_65001);
        CoordinateSystemAxis axis1 = epsgFactory.createCoordinateSystemAxis("Northing", "Y", AxisDirection.NORTH, units.metre());
        CoordinateSystemAxis axis2 = epsgFactory.createCoordinateSystemAxis("Easting", "X", AxisDirection.EAST, units.metre());
        createCartesianCS(4532, axis1, axis2);
        verifyProjectedCRS();
    }

    /**
     * Tests “GIGS projCRS A1-5” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>GIGS projected CRS code: <b>62005</b></li>
     *   <li>GIGS projectedCRS name: <b>GIGS projCRS A1-5</b></li>
     *   <li>GIGS base CRS code: <b>64003</b></li>
     *   <li>GIGS conversion code: <b>65001</b></li>
     *   <li>Conversion definition source: <b>Described by user</b> (build with {@link ObjectFactory})</li>
     *   <li>EPSG coordinate system code: <b>4498</b></li>
     * </ul>
     * <table class="ogc">
     *   <caption>Coordinate system axes</caption>
     *   <tr><th>Name</th><th>Abbreviation</th><th>Orientation</th><th>Unit</th></tr>
     *   <tr><td>Easting</td><td>Y</td><td>east</td><td>metre</td></tr>
     *   <tr><td>Northing</td><td>X</td><td>north</td><td>metre</td></tr>
     * </table>
     *
     * Remarks: No direct EPSG equivalent.
     * Similar to WGS 84 / UTM zone 31N but with different Coordinate System.
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the properties.
     */
    @Test
    @DisplayName("GIGS projCRS A1-5")
    public void GIGS_62005() throws FactoryException {
        setCodeAndName(62005, "GIGS projCRS A1-5");
        createBaseCRS(Test3205::GIGS_64003);
        createConversion(Test3206::GIGS_65001);
        CoordinateSystemAxis axis1 = epsgFactory.createCoordinateSystemAxis("Easting", "Y", AxisDirection.EAST, units.metre());
        CoordinateSystemAxis axis2 = epsgFactory.createCoordinateSystemAxis("Northing", "X", AxisDirection.NORTH, units.metre());
        createCartesianCS(4498, axis1, axis2);
        verifyProjectedCRS();
    }

    /**
     * Tests “GIGS projCRS A1-6” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>GIGS projected CRS code: <b>62006</b></li>
     *   <li>GIGS projectedCRS name: <b>GIGS projCRS A1-6</b></li>
     *   <li>GIGS base CRS code: <b>64003</b></li>
     *   <li>GIGS conversion code: <b>65001</b></li>
     *   <li>Conversion definition source: <b>Described by user</b> (build with {@link ObjectFactory})</li>
     *   <li>EPSG coordinate system code: <b>4530</b></li>
     * </ul>
     * <table class="ogc">
     *   <caption>Coordinate system axes</caption>
     *   <tr><th>Name</th><th>Abbreviation</th><th>Orientation</th><th>Unit</th></tr>
     *   <tr><td>Northing</td><td>X</td><td>north</td><td>metre</td></tr>
     *   <tr><td>Easting</td><td>Y</td><td>east</td><td>metre</td></tr>
     * </table>
     *
     * Remarks: No direct EPSG equivalent.
     * Similar to WGS 84 / UTM zone 31N but with different Coordinate System.
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the properties.
     */
    @Test
    @DisplayName("GIGS projCRS A1-6")
    public void GIGS_62006() throws FactoryException {
        setCodeAndName(62006, "GIGS projCRS A1-6");
        createBaseCRS(Test3205::GIGS_64003);
        createConversion(Test3206::GIGS_65001);
        CoordinateSystemAxis axis1 = epsgFactory.createCoordinateSystemAxis("Northing", "X", AxisDirection.NORTH, units.metre());
        CoordinateSystemAxis axis2 = epsgFactory.createCoordinateSystemAxis("Easting", "Y", AxisDirection.EAST, units.metre());
        createCartesianCS(4530, axis1, axis2);
        verifyProjectedCRS();
    }

    /**
     * Tests “GIGS projCRS A2” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>GIGS projected CRS code: <b>62007</b></li>
     *   <li>GIGS projectedCRS name: <b>GIGS projCRS A2</b></li>
     *   <li>GIGS base CRS code: <b>64003</b></li>
     *   <li>GIGS conversion code: <b>65002</b></li>
     *   <li>Conversion definition source: <b>Described by user</b> (build with {@link ObjectFactory})</li>
     *   <li>EPSG coordinate system code: <b>4400</b></li>
     * </ul>
     * <table class="ogc">
     *   <caption>Coordinate system axes</caption>
     *   <tr><th>Name</th><th>Abbreviation</th><th>Orientation</th><th>Unit</th></tr>
     *   <tr><td>Easting</td><td>E</td><td>east</td><td>metre</td></tr>
     *   <tr><td>Northing</td><td>N</td><td>north</td><td>metre</td></tr>
     * </table>
     *
     * Remarks: No direct EPSG equivalent.
     * Would be equivalent to WGS 84 / British National Grid.
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the properties.
     */
    @Test
    @DisplayName("GIGS projCRS A2")
    public void GIGS_62007() throws FactoryException {
        setCodeAndName(62007, "GIGS projCRS A2");
        createBaseCRS(Test3205::GIGS_64003);
        createConversion(Test3206::GIGS_65002);
        CoordinateSystemAxis axis1 = epsgFactory.createCoordinateSystemAxis("Easting", "E", AxisDirection.EAST, units.metre());
        CoordinateSystemAxis axis2 = epsgFactory.createCoordinateSystemAxis("Northing", "N", AxisDirection.NORTH, units.metre());
        createCartesianCS(4400, axis1, axis2);
        verifyProjectedCRS();
    }

    /**
     * Tests “GIGS projCRS A21” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>GIGS projected CRS code: <b>62008</b></li>
     *   <li>GIGS projectedCRS name: <b>GIGS projCRS A21</b></li>
     *   <li>GIGS base CRS code: <b>64003</b></li>
     *   <li>GIGS conversion code: <b>65021</b></li>
     *   <li>Conversion definition source: <b>Described by user</b> (build with {@link ObjectFactory})</li>
     *   <li>EPSG coordinate system code: <b>4400</b></li>
     * </ul>
     * <table class="ogc">
     *   <caption>Coordinate system axes</caption>
     *   <tr><th>Name</th><th>Abbreviation</th><th>Orientation</th><th>Unit</th></tr>
     *   <tr><td>Easting</td><td>E</td><td>east</td><td>metre</td></tr>
     *   <tr><td>Northing</td><td>N</td><td>north</td><td>metre</td></tr>
     * </table>
     *
     * Remarks: No direct EPSG equivalent.
     * Use as alternative to CRS 62007 only if in test 3005 it has not been possible to define proj code 61002
     * (The CRS A2 is required for Data Operations tests).
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the properties.
     */
    @Test
    @DisplayName("GIGS projCRS A21")
    public void GIGS_62008() throws FactoryException {
        setCodeAndName(62008, "GIGS projCRS A21");
        createBaseCRS(Test3205::GIGS_64003);
        createConversion(Test3206::GIGS_65021);
        CoordinateSystemAxis axis1 = epsgFactory.createCoordinateSystemAxis("Easting", "E", AxisDirection.EAST, units.metre());
        CoordinateSystemAxis axis2 = epsgFactory.createCoordinateSystemAxis("Northing", "N", AxisDirection.NORTH, units.metre());
        createCartesianCS(4400, axis1, axis2);
        verifyProjectedCRS();
    }

    /**
     * Tests “GIGS projCRS A23” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>GIGS projected CRS code: <b>62027</b></li>
     *   <li>GIGS projectedCRS name: <b>GIGS projCRS A23</b></li>
     *   <li>GIGS base CRS code: <b>64003</b></li>
     *   <li>GIGS conversion code: <b>65023</b></li>
     *   <li>Conversion definition source: <b>Described by user</b> (build with {@link ObjectFactory})</li>
     *   <li>EPSG coordinate system code: <b>4497</b></li>
     * </ul>
     * <table class="ogc">
     *   <caption>Coordinate system axes</caption>
     *   <tr><th>Name</th><th>Abbreviation</th><th>Orientation</th><th>Unit</th></tr>
     *   <tr><td>Easting</td><td>X</td><td>east</td><td>US survey foot</td></tr>
     *   <tr><td>Northing</td><td>Y</td><td>north</td><td>US survey foot</td></tr>
     * </table>
     *
     * Remarks: No direct EPSG equivalent.
     * Would be equivalent to WGS 84 / BLM 31N (ftUS).
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the properties.
     */
    @Test
    @DisplayName("GIGS projCRS A23")
    public void GIGS_62027() throws FactoryException {
        setCodeAndName(62027, "GIGS projCRS A23");
        createBaseCRS(Test3205::GIGS_64003);
        createConversion(Test3206::GIGS_65023);
        CoordinateSystemAxis axis1 = epsgFactory.createCoordinateSystemAxis("Easting", "X", AxisDirection.EAST, units.footSurveyUS());
        CoordinateSystemAxis axis2 = epsgFactory.createCoordinateSystemAxis("Northing", "Y", AxisDirection.NORTH, units.footSurveyUS());
        createCartesianCS(4497, axis1, axis2);
        verifyProjectedCRS();
    }

    /**
     * Tests “GIGS projCRS AA1” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>GIGS projected CRS code: <b>62028</b></li>
     *   <li>GIGS projectedCRS name: <b>GIGS projCRS AA1</b></li>
     *   <li>EPSG equivalence: <b>32631 – WGS 84 / UTM zone 31N</b></li>
     *   <li>GIGS base CRS code: <b>64326</b></li>
     *   <li>GIGS conversion code: <b>16031</b></li>
     *   <li>Conversion definition source: <b>Fetched from EPSG dataset</b> (build with {@link AuthorityFactory})</li>
     *   <li>EPSG coordinate system code: <b>4400</b></li>
     * </ul>
     * <table class="ogc">
     *   <caption>Coordinate system axes</caption>
     *   <tr><th>Name</th><th>Abbreviation</th><th>Orientation</th><th>Unit</th></tr>
     *   <tr><td>Easting</td><td>E</td><td>east</td><td>metre</td></tr>
     *   <tr><td>Northing</td><td>N</td><td>north</td><td>metre</td></tr>
     * </table>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the properties.
     */
    @Test
    @DisplayName("GIGS projCRS AA1")
    public void GIGS_62028() throws FactoryException {
        setCodeAndName(62028, "GIGS projCRS AA1");
        createBaseCRS(Test3205::GIGS_64326);
        createConversion(16031);
        CoordinateSystemAxis axis1 = epsgFactory.createCoordinateSystemAxis("Easting", "E", AxisDirection.EAST, units.metre());
        CoordinateSystemAxis axis2 = epsgFactory.createCoordinateSystemAxis("Northing", "N", AxisDirection.NORTH, units.metre());
        createCartesianCS(4400, axis1, axis2);
        verifyProjectedCRS();
    }

    /**
     * Tests “GIGS projCRS B2” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>GIGS projected CRS code: <b>62009</b></li>
     *   <li>GIGS projectedCRS name: <b>GIGS projCRS B2</b></li>
     *   <li>EPSG equivalence: <b>27700 – OSGB36 / British National Grid</b></li>
     *   <li>GIGS base CRS code: <b>64005</b></li>
     *   <li>GIGS conversion code: <b>65002</b></li>
     *   <li>Conversion definition source: <b>Described by user</b> (build with {@link ObjectFactory})</li>
     *   <li>EPSG coordinate system code: <b>4400</b></li>
     * </ul>
     * <table class="ogc">
     *   <caption>Coordinate system axes</caption>
     *   <tr><th>Name</th><th>Abbreviation</th><th>Orientation</th><th>Unit</th></tr>
     *   <tr><td>Easting</td><td>E</td><td>east</td><td>metre</td></tr>
     *   <tr><td>Northing</td><td>N</td><td>north</td><td>metre</td></tr>
     * </table>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the properties.
     */
    @Test
    @DisplayName("GIGS projCRS B2")
    public void GIGS_62009() throws FactoryException {
        setCodeAndName(62009, "GIGS projCRS B2");
        createBaseCRS(Test3205::GIGS_64005);
        createConversion(Test3206::GIGS_65002);
        CoordinateSystemAxis axis1 = epsgFactory.createCoordinateSystemAxis("Easting", "E", AxisDirection.EAST, units.metre());
        CoordinateSystemAxis axis2 = epsgFactory.createCoordinateSystemAxis("Northing", "N", AxisDirection.NORTH, units.metre());
        createCartesianCS(4400, axis1, axis2);
        verifyProjectedCRS();
    }

    /**
     * Tests “GIGS projCRS B22” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>GIGS projected CRS code: <b>62010</b></li>
     *   <li>GIGS projectedCRS name: <b>GIGS projCRS B22</b></li>
     *   <li>GIGS base CRS code: <b>64005</b></li>
     *   <li>GIGS conversion code: <b>65022</b></li>
     *   <li>Conversion definition source: <b>Described by user</b> (build with {@link ObjectFactory})</li>
     *   <li>EPSG coordinate system code: <b>4400</b></li>
     * </ul>
     * <table class="ogc">
     *   <caption>Coordinate system axes</caption>
     *   <tr><th>Name</th><th>Abbreviation</th><th>Orientation</th><th>Unit</th></tr>
     *   <tr><td>Easting</td><td>E</td><td>east</td><td>metre</td></tr>
     *   <tr><td>Northing</td><td>N</td><td>north</td><td>metre</td></tr>
     * </table>
     *
     * Remarks: No direct EPSG equivalent.
     * Use as alternative to CRS 62009 only if in test 3005 it has not been possible to define proj code 61002
     * (The CRS B1 is required for Data Operations tests).
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the properties.
     */
    @Test
    @DisplayName("GIGS projCRS B22")
    public void GIGS_62010() throws FactoryException {
        setCodeAndName(62010, "GIGS projCRS B22");
        createBaseCRS(Test3205::GIGS_64005);
        createConversion(Test3206::GIGS_65022);
        CoordinateSystemAxis axis1 = epsgFactory.createCoordinateSystemAxis("Easting", "E", AxisDirection.EAST, units.metre());
        CoordinateSystemAxis axis2 = epsgFactory.createCoordinateSystemAxis("Northing", "N", AxisDirection.NORTH, units.metre());
        createCartesianCS(4400, axis1, axis2);
        verifyProjectedCRS();
    }

    /**
     * Tests “GIGS projCRS BB2” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>GIGS projected CRS code: <b>62029</b></li>
     *   <li>GIGS projectedCRS name: <b>GIGS projCRS BB2</b></li>
     *   <li>EPSG equivalence: <b>27700 – OSGB36 / British National Grid</b></li>
     *   <li>GIGS base CRS code: <b>64277</b></li>
     *   <li>GIGS conversion code: <b>19916</b></li>
     *   <li>Conversion definition source: <b>Fetched from EPSG dataset</b> (build with {@link AuthorityFactory})</li>
     *   <li>EPSG coordinate system code: <b>4400</b></li>
     * </ul>
     * <table class="ogc">
     *   <caption>Coordinate system axes</caption>
     *   <tr><th>Name</th><th>Abbreviation</th><th>Orientation</th><th>Unit</th></tr>
     *   <tr><td>Easting</td><td>E</td><td>east</td><td>metre</td></tr>
     *   <tr><td>Northing</td><td>N</td><td>north</td><td>metre</td></tr>
     * </table>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the properties.
     */
    @Test
    @DisplayName("GIGS projCRS BB2")
    public void GIGS_62029() throws FactoryException {
        setCodeAndName(62029, "GIGS projCRS BB2");
        createBaseCRS(Test3205::GIGS_64277);
        createConversion(19916);
        CoordinateSystemAxis axis1 = epsgFactory.createCoordinateSystemAxis("Easting", "E", AxisDirection.EAST, units.metre());
        CoordinateSystemAxis axis2 = epsgFactory.createCoordinateSystemAxis("Northing", "N", AxisDirection.NORTH, units.metre());
        createCartesianCS(4400, axis1, axis2);
        verifyProjectedCRS();
    }

    /**
     * Tests “GIGS projCRS C4” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>GIGS projected CRS code: <b>62011</b></li>
     *   <li>GIGS projectedCRS name: <b>GIGS projCRS C4</b></li>
     *   <li>EPSG equivalence: <b>28992 – Amersfoort / RD New</b></li>
     *   <li>GIGS base CRS code: <b>64006</b></li>
     *   <li>GIGS conversion code: <b>65004</b></li>
     *   <li>Conversion definition source: <b>Described by user</b> (build with {@link ObjectFactory})</li>
     *   <li>EPSG coordinate system code: <b>4499</b></li>
     * </ul>
     * <table class="ogc">
     *   <caption>Coordinate system axes</caption>
     *   <tr><th>Name</th><th>Abbreviation</th><th>Orientation</th><th>Unit</th></tr>
     *   <tr><td>Easting</td><td>X</td><td>east</td><td>metre</td></tr>
     *   <tr><td>Northing</td><td>Y</td><td>north</td><td>metre</td></tr>
     * </table>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the properties.
     */
    @Test
    @DisplayName("GIGS projCRS C4")
    public void GIGS_62011() throws FactoryException {
        setCodeAndName(62011, "GIGS projCRS C4");
        createBaseCRS(Test3205::GIGS_64006);
        createConversion(Test3206::GIGS_65004);
        CoordinateSystemAxis axis1 = epsgFactory.createCoordinateSystemAxis("Easting", "X", AxisDirection.EAST, units.metre());
        CoordinateSystemAxis axis2 = epsgFactory.createCoordinateSystemAxis("Northing", "Y", AxisDirection.NORTH, units.metre());
        createCartesianCS(4499, axis1, axis2);
        verifyProjectedCRS();
    }

    /**
     * Tests “GIGS projCRS CC4” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>GIGS projected CRS code: <b>62030</b></li>
     *   <li>GIGS projectedCRS name: <b>GIGS projCRS CC4</b></li>
     *   <li>EPSG equivalence: <b>28992 – Amersfoort / RD New</b></li>
     *   <li>GIGS base CRS code: <b>64289</b></li>
     *   <li>GIGS conversion code: <b>19914</b></li>
     *   <li>Conversion definition source: <b>Fetched from EPSG dataset</b> (build with {@link AuthorityFactory})</li>
     *   <li>EPSG coordinate system code: <b>4499</b></li>
     * </ul>
     * <table class="ogc">
     *   <caption>Coordinate system axes</caption>
     *   <tr><th>Name</th><th>Abbreviation</th><th>Orientation</th><th>Unit</th></tr>
     *   <tr><td>Easting</td><td>X</td><td>east</td><td>metre</td></tr>
     *   <tr><td>Northing</td><td>Y</td><td>north</td><td>metre</td></tr>
     * </table>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the properties.
     */
    @Test
    @DisplayName("GIGS projCRS CC4")
    public void GIGS_62030() throws FactoryException {
        setCodeAndName(62030, "GIGS projCRS CC4");
        createBaseCRS(Test3205::GIGS_64289);
        createConversion(19914);
        CoordinateSystemAxis axis1 = epsgFactory.createCoordinateSystemAxis("Easting", "X", AxisDirection.EAST, units.metre());
        CoordinateSystemAxis axis2 = epsgFactory.createCoordinateSystemAxis("Northing", "Y", AxisDirection.NORTH, units.metre());
        createCartesianCS(4499, axis1, axis2);
        verifyProjectedCRS();
    }

    /**
     * Tests “GIGS projCRS D5” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>GIGS projected CRS code: <b>62012</b></li>
     *   <li>GIGS projectedCRS name: <b>GIGS projCRS D5</b></li>
     *   <li>EPSG equivalence: <b>5330 – Batavia (Jakarta) / NEIEZ</b></li>
     *   <li>GIGS base CRS code: <b>64007</b></li>
     *   <li>GIGS conversion code: <b>65005</b></li>
     *   <li>Conversion definition source: <b>Described by user</b> (build with {@link ObjectFactory})</li>
     *   <li>EPSG coordinate system code: <b>4499</b></li>
     * </ul>
     * <table class="ogc">
     *   <caption>Coordinate system axes</caption>
     *   <tr><th>Name</th><th>Abbreviation</th><th>Orientation</th><th>Unit</th></tr>
     *   <tr><td>Easting</td><td>X</td><td>east</td><td>metre</td></tr>
     *   <tr><td>Northing</td><td>Y</td><td>north</td><td>metre</td></tr>
     * </table>
     *
     * Remarks: Equivalent to Batavia / NEIEZ (code 3001) except that its definition is with respect to the Jakarta meridian.
     * See GIGS projCRS L27 (62037).
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the properties.
     */
    @Test
    @DisplayName("GIGS projCRS D5")
    public void GIGS_62012() throws FactoryException {
        setCodeAndName(62012, "GIGS projCRS D5");
        createBaseCRS(Test3205::GIGS_64007);
        createConversion(Test3206::GIGS_65005);
        CoordinateSystemAxis axis1 = epsgFactory.createCoordinateSystemAxis("Easting", "X", AxisDirection.EAST, units.metre());
        CoordinateSystemAxis axis2 = epsgFactory.createCoordinateSystemAxis("Northing", "Y", AxisDirection.NORTH, units.metre());
        createCartesianCS(4499, axis1, axis2);
        verifyProjectedCRS();
    }

    /**
     * Tests “GIGS projCRS E6” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>GIGS projected CRS code: <b>62013</b></li>
     *   <li>GIGS projectedCRS name: <b>GIGS projCRS E6</b></li>
     *   <li>EPSG equivalence: <b>31370 – Belge 1972 / Belgian Lambert 72</b></li>
     *   <li>GIGS base CRS code: <b>64008</b></li>
     *   <li>GIGS conversion code: <b>65006</b></li>
     *   <li>Conversion definition source: <b>Described by user</b> (build with {@link ObjectFactory})</li>
     *   <li>EPSG coordinate system code: <b>4499</b></li>
     * </ul>
     * <table class="ogc">
     *   <caption>Coordinate system axes</caption>
     *   <tr><th>Name</th><th>Abbreviation</th><th>Orientation</th><th>Unit</th></tr>
     *   <tr><td>Easting</td><td>X</td><td>east</td><td>metre</td></tr>
     *   <tr><td>Northing</td><td>Y</td><td>north</td><td>metre</td></tr>
     * </table>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the properties.
     */
    @Test
    @DisplayName("GIGS projCRS E6")
    public void GIGS_62013() throws FactoryException {
        setCodeAndName(62013, "GIGS projCRS E6");
        createBaseCRS(Test3205::GIGS_64008);
        createConversion(Test3206::GIGS_65006);
        CoordinateSystemAxis axis1 = epsgFactory.createCoordinateSystemAxis("Easting", "X", AxisDirection.EAST, units.metre());
        CoordinateSystemAxis axis2 = epsgFactory.createCoordinateSystemAxis("Northing", "Y", AxisDirection.NORTH, units.metre());
        createCartesianCS(4499, axis1, axis2);
        verifyProjectedCRS();
    }

    /**
     * Tests “GIGS projCRS EE6” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>GIGS projected CRS code: <b>62031</b></li>
     *   <li>GIGS projectedCRS name: <b>GIGS projCRS EE6</b></li>
     *   <li>EPSG equivalence: <b>31370 – Belge 1972 / Belgian Lambert 72</b></li>
     *   <li>GIGS base CRS code: <b>64313</b></li>
     *   <li>GIGS conversion code: <b>19961</b></li>
     *   <li>Conversion definition source: <b>Fetched from EPSG dataset</b> (build with {@link AuthorityFactory})</li>
     *   <li>EPSG coordinate system code: <b>4499</b></li>
     * </ul>
     * <table class="ogc">
     *   <caption>Coordinate system axes</caption>
     *   <tr><th>Name</th><th>Abbreviation</th><th>Orientation</th><th>Unit</th></tr>
     *   <tr><td>Easting</td><td>X</td><td>east</td><td>metre</td></tr>
     *   <tr><td>Northing</td><td>Y</td><td>north</td><td>metre</td></tr>
     * </table>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the properties.
     */
    @Test
    @DisplayName("GIGS projCRS EE6")
    public void GIGS_62031() throws FactoryException {
        setCodeAndName(62031, "GIGS projCRS EE6");
        createBaseCRS(Test3205::GIGS_64313);
        createConversion(19961);
        CoordinateSystemAxis axis1 = epsgFactory.createCoordinateSystemAxis("Easting", "X", AxisDirection.EAST, units.metre());
        CoordinateSystemAxis axis2 = epsgFactory.createCoordinateSystemAxis("Northing", "Y", AxisDirection.NORTH, units.metre());
        createCartesianCS(4499, axis1, axis2);
        verifyProjectedCRS();
    }

    /**
     * Tests “GIGS projCRS F7” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>GIGS projected CRS code: <b>62014</b></li>
     *   <li>GIGS projectedCRS name: <b>GIGS projCRS F7</b></li>
     *   <li>EPSG equivalence: <b>28354 – GDA94 / MGA zone 54</b></li>
     *   <li>GIGS base CRS code: <b>64009</b></li>
     *   <li>GIGS conversion code: <b>65007</b></li>
     *   <li>Conversion definition source: <b>Described by user</b> (build with {@link ObjectFactory})</li>
     *   <li>EPSG coordinate system code: <b>4400</b></li>
     * </ul>
     * <table class="ogc">
     *   <caption>Coordinate system axes</caption>
     *   <tr><th>Name</th><th>Abbreviation</th><th>Orientation</th><th>Unit</th></tr>
     *   <tr><td>Easting</td><td>E</td><td>east</td><td>metre</td></tr>
     *   <tr><td>Northing</td><td>N</td><td>north</td><td>metre</td></tr>
     * </table>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the properties.
     */
    @Test
    @DisplayName("GIGS projCRS F7")
    public void GIGS_62014() throws FactoryException {
        setCodeAndName(62014, "GIGS projCRS F7");
        createBaseCRS(Test3205::GIGS_64009);
        createConversion(Test3206::GIGS_65007);
        CoordinateSystemAxis axis1 = epsgFactory.createCoordinateSystemAxis("Easting", "E", AxisDirection.EAST, units.metre());
        CoordinateSystemAxis axis2 = epsgFactory.createCoordinateSystemAxis("Northing", "N", AxisDirection.NORTH, units.metre());
        createCartesianCS(4400, axis1, axis2);
        verifyProjectedCRS();
    }

    /**
     * Tests “GIGS projCRS F8” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>GIGS projected CRS code: <b>62015</b></li>
     *   <li>GIGS projectedCRS name: <b>GIGS projCRS F8</b></li>
     *   <li>EPSG equivalence: <b>28355 – GDA94 / MGA zone 55</b></li>
     *   <li>GIGS base CRS code: <b>64009</b></li>
     *   <li>GIGS conversion code: <b>65008</b></li>
     *   <li>Conversion definition source: <b>Described by user</b> (build with {@link ObjectFactory})</li>
     *   <li>EPSG coordinate system code: <b>4400</b></li>
     * </ul>
     * <table class="ogc">
     *   <caption>Coordinate system axes</caption>
     *   <tr><th>Name</th><th>Abbreviation</th><th>Orientation</th><th>Unit</th></tr>
     *   <tr><td>Easting</td><td>E</td><td>east</td><td>metre</td></tr>
     *   <tr><td>Northing</td><td>N</td><td>north</td><td>metre</td></tr>
     * </table>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the properties.
     */
    @Test
    @DisplayName("GIGS projCRS F8")
    public void GIGS_62015() throws FactoryException {
        setCodeAndName(62015, "GIGS projCRS F8");
        createBaseCRS(Test3205::GIGS_64009);
        createConversion(Test3206::GIGS_65008);
        CoordinateSystemAxis axis1 = epsgFactory.createCoordinateSystemAxis("Easting", "E", AxisDirection.EAST, units.metre());
        CoordinateSystemAxis axis2 = epsgFactory.createCoordinateSystemAxis("Northing", "N", AxisDirection.NORTH, units.metre());
        createCartesianCS(4400, axis1, axis2);
        verifyProjectedCRS();
    }

    /**
     * Tests “GIGS projCRS F9” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>GIGS projected CRS code: <b>62016</b></li>
     *   <li>GIGS projectedCRS name: <b>GIGS projCRS F9</b></li>
     *   <li>EPSG equivalence: <b>3577 – GDA94 / Australian Albers</b></li>
     *   <li>GIGS base CRS code: <b>64009</b></li>
     *   <li>GIGS conversion code: <b>65009</b></li>
     *   <li>Conversion definition source: <b>Described by user</b> (build with {@link ObjectFactory})</li>
     *   <li>EPSG coordinate system code: <b>4400</b></li>
     * </ul>
     * <table class="ogc">
     *   <caption>Coordinate system axes</caption>
     *   <tr><th>Name</th><th>Abbreviation</th><th>Orientation</th><th>Unit</th></tr>
     *   <tr><td>Easting</td><td>E</td><td>east</td><td>metre</td></tr>
     *   <tr><td>Northing</td><td>N</td><td>north</td><td>metre</td></tr>
     * </table>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the properties.
     */
    @Test
    @DisplayName("GIGS projCRS F9")
    public void GIGS_62016() throws FactoryException {
        setCodeAndName(62016, "GIGS projCRS F9");
        createBaseCRS(Test3205::GIGS_64009);
        createConversion(Test3206::GIGS_65009);
        CoordinateSystemAxis axis1 = epsgFactory.createCoordinateSystemAxis("Easting", "E", AxisDirection.EAST, units.metre());
        CoordinateSystemAxis axis2 = epsgFactory.createCoordinateSystemAxis("Northing", "N", AxisDirection.NORTH, units.metre());
        createCartesianCS(4400, axis1, axis2);
        verifyProjectedCRS();
    }

    /**
     * Tests “GIGS projCRS FF8” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>GIGS projected CRS code: <b>62032</b></li>
     *   <li>GIGS projectedCRS name: <b>GIGS projCRS FF8</b></li>
     *   <li>EPSG equivalence: <b>28354 – GDA94 / MGA zone 54</b></li>
     *   <li>GIGS base CRS code: <b>64283</b></li>
     *   <li>GIGS conversion code: <b>17354</b></li>
     *   <li>Conversion definition source: <b>Fetched from EPSG dataset</b> (build with {@link AuthorityFactory})</li>
     *   <li>EPSG coordinate system code: <b>4400</b></li>
     * </ul>
     * <table class="ogc">
     *   <caption>Coordinate system axes</caption>
     *   <tr><th>Name</th><th>Abbreviation</th><th>Orientation</th><th>Unit</th></tr>
     *   <tr><td>Easting</td><td>E</td><td>east</td><td>metre</td></tr>
     *   <tr><td>Northing</td><td>N</td><td>north</td><td>metre</td></tr>
     * </table>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the properties.
     */
    @Test
    @DisplayName("GIGS projCRS FF8")
    public void GIGS_62032() throws FactoryException {
        setCodeAndName(62032, "GIGS projCRS FF8");
        createBaseCRS(Test3205::GIGS_64283);
        createConversion(17354);
        CoordinateSystemAxis axis1 = epsgFactory.createCoordinateSystemAxis("Easting", "E", AxisDirection.EAST, units.metre());
        CoordinateSystemAxis axis2 = epsgFactory.createCoordinateSystemAxis("Northing", "N", AxisDirection.NORTH, units.metre());
        createCartesianCS(4400, axis1, axis2);
        verifyProjectedCRS();
    }

    /**
     * Tests “GIGS projCRS G10” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>GIGS projected CRS code: <b>62017</b></li>
     *   <li>GIGS projectedCRS name: <b>GIGS projCRS G10</b></li>
     *   <li>EPSG equivalence: <b>2049 – Hartebeesthoek94 / Lo21</b></li>
     *   <li>GIGS base CRS code: <b>64010</b></li>
     *   <li>GIGS conversion code: <b>65010</b></li>
     *   <li>Conversion definition source: <b>Described by user</b> (build with {@link ObjectFactory})</li>
     *   <li>EPSG coordinate system code: <b>6503</b></li>
     * </ul>
     * <table class="ogc">
     *   <caption>Coordinate system axes</caption>
     *   <tr><th>Name</th><th>Abbreviation</th><th>Orientation</th><th>Unit</th></tr>
     *   <tr><td>Westing</td><td>Y</td><td>west</td><td>metre</td></tr>
     *   <tr><td>Southing</td><td>X</td><td>south</td><td>metre</td></tr>
     * </table>
     *
     * Remarks: Equivalence applies only to late-binding applications.
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the properties.
     */
    @Test
    @DisplayName("GIGS projCRS G10")
    public void GIGS_62017() throws FactoryException {
        setCodeAndName(62017, "GIGS projCRS G10");
        createBaseCRS(Test3205::GIGS_64010);
        createConversion(Test3206::GIGS_65010);
        CoordinateSystemAxis axis1 = epsgFactory.createCoordinateSystemAxis("Westing", "Y", AxisDirection.WEST, units.metre());
        CoordinateSystemAxis axis2 = epsgFactory.createCoordinateSystemAxis("Southing", "X", AxisDirection.SOUTH, units.metre());
        createCartesianCS(6503, axis1, axis2);
        verifyProjectedCRS();
    }

    /**
     * Tests “GIGS projCRS G11” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>GIGS projected CRS code: <b>62018</b></li>
     *   <li>GIGS projectedCRS name: <b>GIGS projCRS G11</b></li>
     *   <li>EPSG equivalence: <b>22175 – POSGAR 98 / Argentina 5</b></li>
     *   <li>GIGS base CRS code: <b>64010</b></li>
     *   <li>GIGS conversion code: <b>65011</b></li>
     *   <li>Conversion definition source: <b>Described by user</b> (build with {@link ObjectFactory})</li>
     *   <li>EPSG coordinate system code: <b>4530</b></li>
     * </ul>
     * <table class="ogc">
     *   <caption>Coordinate system axes</caption>
     *   <tr><th>Name</th><th>Abbreviation</th><th>Orientation</th><th>Unit</th></tr>
     *   <tr><td>Northing</td><td>X</td><td>north</td><td>metre</td></tr>
     *   <tr><td>Easting</td><td>Y</td><td>east</td><td>metre</td></tr>
     * </table>
     *
     * Remarks: Equivalence applies only to late-binding applications.
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the properties.
     */
    @Test
    @DisplayName("GIGS projCRS G11")
    public void GIGS_62018() throws FactoryException {
        setCodeAndName(62018, "GIGS projCRS G11");
        createBaseCRS(Test3205::GIGS_64010);
        createConversion(Test3206::GIGS_65011);
        CoordinateSystemAxis axis1 = epsgFactory.createCoordinateSystemAxis("Northing", "X", AxisDirection.NORTH, units.metre());
        CoordinateSystemAxis axis2 = epsgFactory.createCoordinateSystemAxis("Easting", "Y", AxisDirection.EAST, units.metre());
        createCartesianCS(4530, axis1, axis2);
        verifyProjectedCRS();
    }

    /**
     * Tests “GIGS projCRS G12” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>GIGS projected CRS code: <b>62019</b></li>
     *   <li>GIGS projectedCRS name: <b>GIGS projCRS G12</b></li>
     *   <li>EPSG equivalence: <b>5880 – SIRGAS2000 / Brazil Polyconic</b></li>
     *   <li>GIGS base CRS code: <b>64010</b></li>
     *   <li>GIGS conversion code: <b>65012</b></li>
     *   <li>Conversion definition source: <b>Described by user</b> (build with {@link ObjectFactory})</li>
     *   <li>EPSG coordinate system code: <b>4499</b></li>
     * </ul>
     * <table class="ogc">
     *   <caption>Coordinate system axes</caption>
     *   <tr><th>Name</th><th>Abbreviation</th><th>Orientation</th><th>Unit</th></tr>
     *   <tr><td>Easting</td><td>X</td><td>east</td><td>metre</td></tr>
     *   <tr><td>Northing</td><td>Y</td><td>north</td><td>metre</td></tr>
     * </table>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the properties.
     */
    @Test
    @DisplayName("GIGS projCRS G12")
    public void GIGS_62019() throws FactoryException {
        setCodeAndName(62019, "GIGS projCRS G12");
        createBaseCRS(Test3205::GIGS_64010);
        createConversion(Test3206::GIGS_65012);
        CoordinateSystemAxis axis1 = epsgFactory.createCoordinateSystemAxis("Easting", "X", AxisDirection.EAST, units.metre());
        CoordinateSystemAxis axis2 = epsgFactory.createCoordinateSystemAxis("Northing", "Y", AxisDirection.NORTH, units.metre());
        createCartesianCS(4499, axis1, axis2);
        verifyProjectedCRS();
    }

    /**
     * Tests “GIGS projCRS G13” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>GIGS projected CRS code: <b>62020</b></li>
     *   <li>GIGS projectedCRS name: <b>GIGS projCRS G13</b></li>
     *   <li>GIGS base CRS code: <b>64010</b></li>
     *   <li>GIGS conversion code: <b>65013</b></li>
     *   <li>Conversion definition source: <b>Described by user</b> (build with {@link ObjectFactory})</li>
     *   <li>EPSG coordinate system code: <b>4400</b></li>
     * </ul>
     * <table class="ogc">
     *   <caption>Coordinate system axes</caption>
     *   <tr><th>Name</th><th>Abbreviation</th><th>Orientation</th><th>Unit</th></tr>
     *   <tr><td>Easting</td><td>E</td><td>east</td><td>metre</td></tr>
     *   <tr><td>Northing</td><td>N</td><td>north</td><td>metre</td></tr>
     * </table>
     *
     * Remarks: No direct EPSG equivalent.
     * Functionally equivalent to GDM2000 / East Malaysia BRSO.
     * Utilising different projection.
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the properties.
     */
    @Test
    @DisplayName("GIGS projCRS G13")
    public void GIGS_62020() throws FactoryException {
        setCodeAndName(62020, "GIGS projCRS G13");
        createBaseCRS(Test3205::GIGS_64010);
        createConversion(Test3206::GIGS_65013);
        CoordinateSystemAxis axis1 = epsgFactory.createCoordinateSystemAxis("Easting", "E", AxisDirection.EAST, units.metre());
        CoordinateSystemAxis axis2 = epsgFactory.createCoordinateSystemAxis("Northing", "N", AxisDirection.NORTH, units.metre());
        createCartesianCS(4400, axis1, axis2);
        verifyProjectedCRS();
    }

    /**
     * Tests “GIGS projCRS G14” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>GIGS projected CRS code: <b>62021</b></li>
     *   <li>GIGS projectedCRS name: <b>GIGS projCRS G14</b></li>
     *   <li>EPSG equivalence: <b>3376 – GDM2000 / East Malaysia BRSO</b></li>
     *   <li>GIGS base CRS code: <b>64010</b></li>
     *   <li>GIGS conversion code: <b>65014</b></li>
     *   <li>Conversion definition source: <b>Described by user</b> (build with {@link ObjectFactory})</li>
     *   <li>EPSG coordinate system code: <b>4400</b></li>
     * </ul>
     * <table class="ogc">
     *   <caption>Coordinate system axes</caption>
     *   <tr><th>Name</th><th>Abbreviation</th><th>Orientation</th><th>Unit</th></tr>
     *   <tr><td>Easting</td><td>E</td><td>east</td><td>metre</td></tr>
     *   <tr><td>Northing</td><td>N</td><td>north</td><td>metre</td></tr>
     * </table>
     *
     * Remarks: Equivalence applies only to late-binding applications.
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the properties.
     */
    @Test
    @DisplayName("GIGS projCRS G14")
    public void GIGS_62021() throws FactoryException {
        setCodeAndName(62021, "GIGS projCRS G14");
        createBaseCRS(Test3205::GIGS_64010);
        createConversion(Test3206::GIGS_65014);
        CoordinateSystemAxis axis1 = epsgFactory.createCoordinateSystemAxis("Easting", "E", AxisDirection.EAST, units.metre());
        CoordinateSystemAxis axis2 = epsgFactory.createCoordinateSystemAxis("Northing", "N", AxisDirection.NORTH, units.metre());
        createCartesianCS(4400, axis1, axis2);
        verifyProjectedCRS();
    }

    /**
     * Tests “GIGS projCRS G15” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>GIGS projected CRS code: <b>62022</b></li>
     *   <li>GIGS projectedCRS name: <b>GIGS projCRS G15</b></li>
     *   <li>EPSG equivalence: <b>3377 – GDM2000 / Johor Grid</b></li>
     *   <li>GIGS base CRS code: <b>64010</b></li>
     *   <li>GIGS conversion code: <b>65015</b></li>
     *   <li>Conversion definition source: <b>Described by user</b> (build with {@link ObjectFactory})</li>
     *   <li>EPSG coordinate system code: <b>4499</b></li>
     * </ul>
     * <table class="ogc">
     *   <caption>Coordinate system axes</caption>
     *   <tr><th>Name</th><th>Abbreviation</th><th>Orientation</th><th>Unit</th></tr>
     *   <tr><td>Easting</td><td>X</td><td>east</td><td>metre</td></tr>
     *   <tr><td>Northing</td><td>Y</td><td>north</td><td>metre</td></tr>
     * </table>
     *
     * Remarks: Equivalence applies only to late-binding applications.
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the properties.
     */
    @Test
    @DisplayName("GIGS projCRS G15")
    public void GIGS_62022() throws FactoryException {
        setCodeAndName(62022, "GIGS projCRS G15");
        createBaseCRS(Test3205::GIGS_64010);
        createConversion(Test3206::GIGS_65015);
        CoordinateSystemAxis axis1 = epsgFactory.createCoordinateSystemAxis("Easting", "X", AxisDirection.EAST, units.metre());
        CoordinateSystemAxis axis2 = epsgFactory.createCoordinateSystemAxis("Northing", "Y", AxisDirection.NORTH, units.metre());
        createCartesianCS(4499, axis1, axis2);
        verifyProjectedCRS();
    }

    /**
     * Tests “GIGS projCRS G16” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>GIGS projected CRS code: <b>62023</b></li>
     *   <li>GIGS projectedCRS name: <b>GIGS projCRS G16</b></li>
     *   <li>EPSG equivalence: <b>3035 – ETRS89-extended / LAEA Europe</b></li>
     *   <li>GIGS base CRS code: <b>64010</b></li>
     *   <li>GIGS conversion code: <b>65016</b></li>
     *   <li>Conversion definition source: <b>Described by user</b> (build with {@link ObjectFactory})</li>
     *   <li>EPSG coordinate system code: <b>4532</b></li>
     * </ul>
     * <table class="ogc">
     *   <caption>Coordinate system axes</caption>
     *   <tr><th>Name</th><th>Abbreviation</th><th>Orientation</th><th>Unit</th></tr>
     *   <tr><td>Northing</td><td>Y</td><td>north</td><td>metre</td></tr>
     *   <tr><td>Easting</td><td>X</td><td>east</td><td>metre</td></tr>
     * </table>
     *
     * Remarks: Equivalence applies only to late-binding applications.
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the properties.
     */
    @Test
    @DisplayName("GIGS projCRS G16")
    public void GIGS_62023() throws FactoryException {
        setCodeAndName(62023, "GIGS projCRS G16");
        createBaseCRS(Test3205::GIGS_64010);
        createConversion(Test3206::GIGS_65016);
        CoordinateSystemAxis axis1 = epsgFactory.createCoordinateSystemAxis("Northing", "Y", AxisDirection.NORTH, units.metre());
        CoordinateSystemAxis axis2 = epsgFactory.createCoordinateSystemAxis("Easting", "X", AxisDirection.EAST, units.metre());
        createCartesianCS(4532, axis1, axis2);
        verifyProjectedCRS();
    }

    /**
     * Tests “GIGS projCRS G17” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>GIGS projected CRS code: <b>62024</b></li>
     *   <li>GIGS projectedCRS name: <b>GIGS projCRS G17</b></li>
     *   <li>EPSG equivalence: <b>2921 – NAD83(HARN) / Utah North (ft)</b></li>
     *   <li>GIGS base CRS code: <b>64010</b></li>
     *   <li>GIGS conversion code: <b>65017</b></li>
     *   <li>Conversion definition source: <b>Described by user</b> (build with {@link ObjectFactory})</li>
     *   <li>EPSG coordinate system code: <b>4495</b></li>
     * </ul>
     * <table class="ogc">
     *   <caption>Coordinate system axes</caption>
     *   <tr><th>Name</th><th>Abbreviation</th><th>Orientation</th><th>Unit</th></tr>
     *   <tr><td>Easting</td><td>X</td><td>east</td><td>foot</td></tr>
     *   <tr><td>Northing</td><td>Y</td><td>north</td><td>foot</td></tr>
     * </table>
     *
     * Remarks: Equivalence applies only to late-binding applications.
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the properties.
     */
    @Test
    @DisplayName("GIGS projCRS G17")
    public void GIGS_62024() throws FactoryException {
        setCodeAndName(62024, "GIGS projCRS G17");
        createBaseCRS(Test3205::GIGS_64010);
        createConversion(Test3206::GIGS_65017);
        CoordinateSystemAxis axis1 = epsgFactory.createCoordinateSystemAxis("Easting", "X", AxisDirection.EAST, units.foot());
        CoordinateSystemAxis axis2 = epsgFactory.createCoordinateSystemAxis("Northing", "Y", AxisDirection.NORTH, units.foot());
        createCartesianCS(4495, axis1, axis2);
        verifyProjectedCRS();
    }

    /**
     * Tests “GIGS projCRS G18” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>GIGS projected CRS code: <b>62025</b></li>
     *   <li>GIGS projectedCRS name: <b>GIGS projCRS G18</b></li>
     *   <li>EPSG equivalence: <b>3568 – NAD83(HARN) / Utah North (ftUS)</b></li>
     *   <li>GIGS base CRS code: <b>64010</b></li>
     *   <li>GIGS conversion code: <b>65018</b></li>
     *   <li>Conversion definition source: <b>Described by user</b> (build with {@link ObjectFactory})</li>
     *   <li>EPSG coordinate system code: <b>4497</b></li>
     * </ul>
     * <table class="ogc">
     *   <caption>Coordinate system axes</caption>
     *   <tr><th>Name</th><th>Abbreviation</th><th>Orientation</th><th>Unit</th></tr>
     *   <tr><td>Easting</td><td>X</td><td>east</td><td>US survey foot</td></tr>
     *   <tr><td>Northing</td><td>Y</td><td>north</td><td>US survey foot</td></tr>
     * </table>
     *
     * Remarks: Equivalence applies only to late-binding applications.
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the properties.
     */
    @Test
    @DisplayName("GIGS projCRS G18")
    public void GIGS_62025() throws FactoryException {
        setCodeAndName(62025, "GIGS projCRS G18");
        createBaseCRS(Test3205::GIGS_64010);
        createConversion(Test3206::GIGS_65018);
        CoordinateSystemAxis axis1 = epsgFactory.createCoordinateSystemAxis("Easting", "X", AxisDirection.EAST, units.footSurveyUS());
        CoordinateSystemAxis axis2 = epsgFactory.createCoordinateSystemAxis("Northing", "Y", AxisDirection.NORTH, units.footSurveyUS());
        createCartesianCS(4497, axis1, axis2);
        verifyProjectedCRS();
    }

    /**
     * Tests “GIGS projCRS H19” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>GIGS projected CRS code: <b>62026</b></li>
     *   <li>GIGS projectedCRS name: <b>GIGS projCRS H19</b></li>
     *   <li>EPSG equivalence: <b>27572 – NTF (Paris) / Lambert zone II</b></li>
     *   <li>GIGS base CRS code: <b>64011</b></li>
     *   <li>GIGS conversion code: <b>65019</b></li>
     *   <li>Conversion definition source: <b>Described by user</b> (build with {@link ObjectFactory})</li>
     *   <li>EPSG coordinate system code: <b>4499</b></li>
     * </ul>
     * <table class="ogc">
     *   <caption>Coordinate system axes</caption>
     *   <tr><th>Name</th><th>Abbreviation</th><th>Orientation</th><th>Unit</th></tr>
     *   <tr><td>Easting</td><td>X</td><td>east</td><td>metre</td></tr>
     *   <tr><td>Northing</td><td>Y</td><td>north</td><td>metre</td></tr>
     * </table>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the properties.
     */
    @Test
    @DisplayName("GIGS projCRS H19")
    public void GIGS_62026() throws FactoryException {
        setCodeAndName(62026, "GIGS projCRS H19");
        createBaseCRS(Test3205::GIGS_64011);
        createConversion(Test3206::GIGS_65019);
        CoordinateSystemAxis axis1 = epsgFactory.createCoordinateSystemAxis("Easting", "X", AxisDirection.EAST, units.metre());
        CoordinateSystemAxis axis2 = epsgFactory.createCoordinateSystemAxis("Northing", "Y", AxisDirection.NORTH, units.metre());
        createCartesianCS(4499, axis1, axis2);
        verifyProjectedCRS();
    }

    /**
     * Tests “GIGS projCRS HH19” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>GIGS projected CRS code: <b>62033</b></li>
     *   <li>GIGS projectedCRS name: <b>GIGS projCRS HH19</b></li>
     *   <li>EPSG equivalence: <b>27572 – NTF (Paris) / Lambert zone II</b></li>
     *   <li>GIGS base CRS code: <b>64807</b></li>
     *   <li>GIGS conversion code: <b>18082</b></li>
     *   <li>Conversion definition source: <b>Fetched from EPSG dataset</b> (build with {@link AuthorityFactory})</li>
     *   <li>EPSG coordinate system code: <b>4499</b></li>
     * </ul>
     * <table class="ogc">
     *   <caption>Coordinate system axes</caption>
     *   <tr><th>Name</th><th>Abbreviation</th><th>Orientation</th><th>Unit</th></tr>
     *   <tr><td>Easting</td><td>X</td><td>east</td><td>metre</td></tr>
     *   <tr><td>Northing</td><td>Y</td><td>north</td><td>metre</td></tr>
     * </table>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the properties.
     */
    @Test
    @DisplayName("GIGS projCRS HH19")
    public void GIGS_62033() throws FactoryException {
        setCodeAndName(62033, "GIGS projCRS HH19");
        createBaseCRS(Test3205::GIGS_64807);
        createConversion(18082);
        CoordinateSystemAxis axis1 = epsgFactory.createCoordinateSystemAxis("Easting", "X", AxisDirection.EAST, units.metre());
        CoordinateSystemAxis axis2 = epsgFactory.createCoordinateSystemAxis("Northing", "Y", AxisDirection.NORTH, units.metre());
        createCartesianCS(4499, axis1, axis2);
        verifyProjectedCRS();
    }

    /**
     * Tests “GIGS projCRS J28” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>GIGS projected CRS code: <b>62038</b></li>
     *   <li>GIGS projectedCRS name: <b>GIGS projCRS J28</b></li>
     *   <li>EPSG equivalence: <b>26708 – NAD27 / UTM zone 8N</b></li>
     *   <li>GIGS base CRS code: <b>64012</b></li>
     *   <li>GIGS conversion code: <b>65028</b></li>
     *   <li>Conversion definition source: <b>Described by user</b> (build with {@link ObjectFactory})</li>
     *   <li>EPSG coordinate system code: <b>4400</b></li>
     * </ul>
     * <table class="ogc">
     *   <caption>Coordinate system axes</caption>
     *   <tr><th>Name</th><th>Abbreviation</th><th>Orientation</th><th>Unit</th></tr>
     *   <tr><td>Easting</td><td>E</td><td>east</td><td>metre</td></tr>
     *   <tr><td>Northing</td><td>N</td><td>north</td><td>metre</td></tr>
     * </table>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the properties.
     */
    @Test
    @DisplayName("GIGS projCRS J28")
    public void GIGS_62038() throws FactoryException {
        setCodeAndName(62038, "GIGS projCRS J28");
        createBaseCRS(Test3205::GIGS_64012);
        createConversion(Test3206::GIGS_65028);
        CoordinateSystemAxis axis1 = epsgFactory.createCoordinateSystemAxis("Easting", "E", AxisDirection.EAST, units.metre());
        CoordinateSystemAxis axis2 = epsgFactory.createCoordinateSystemAxis("Northing", "N", AxisDirection.NORTH, units.metre());
        createCartesianCS(4400, axis1, axis2);
        verifyProjectedCRS();
    }

    /**
     * Tests “GIGS projCRS K26” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>GIGS projected CRS code: <b>62036</b></li>
     *   <li>GIGS projectedCRS name: <b>GIGS projCRS K26</b></li>
     *   <li>EPSG equivalence: <b>23700 – HD72 / EOV</b></li>
     *   <li>GIGS base CRS code: <b>64015</b></li>
     *   <li>GIGS conversion code: <b>65026</b></li>
     *   <li>Conversion definition source: <b>Described by user</b> (build with {@link ObjectFactory})</li>
     *   <li>EPSG coordinate system code: <b>4498</b></li>
     * </ul>
     * <table class="ogc">
     *   <caption>Coordinate system axes</caption>
     *   <tr><th>Name</th><th>Abbreviation</th><th>Orientation</th><th>Unit</th></tr>
     *   <tr><td>Easting</td><td>Y</td><td>east</td><td>metre</td></tr>
     *   <tr><td>Northing</td><td>X</td><td>north</td><td>metre</td></tr>
     * </table>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the properties.
     */
    @Test
    @DisplayName("GIGS projCRS K26")
    public void GIGS_62036() throws FactoryException {
        setCodeAndName(62036, "GIGS projCRS K26");
        createBaseCRS(Test3205::GIGS_64015);
        createConversion(Test3206::GIGS_65026);
        CoordinateSystemAxis axis1 = epsgFactory.createCoordinateSystemAxis("Easting", "Y", AxisDirection.EAST, units.metre());
        CoordinateSystemAxis axis2 = epsgFactory.createCoordinateSystemAxis("Northing", "X", AxisDirection.NORTH, units.metre());
        createCartesianCS(4498, axis1, axis2);
        verifyProjectedCRS();
    }

    /**
     * Tests “GIGS projCRS L27” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>GIGS projected CRS code: <b>62037</b></li>
     *   <li>GIGS projectedCRS name: <b>GIGS projCRS L27</b></li>
     *   <li>EPSG equivalence: <b>3001 – Batavia / NEIEZ</b></li>
     *   <li>GIGS base CRS code: <b>64014</b></li>
     *   <li>GIGS conversion code: <b>65027</b></li>
     *   <li>Conversion definition source: <b>Described by user</b> (build with {@link ObjectFactory})</li>
     *   <li>EPSG coordinate system code: <b>4499</b></li>
     * </ul>
     * <table class="ogc">
     *   <caption>Coordinate system axes</caption>
     *   <tr><th>Name</th><th>Abbreviation</th><th>Orientation</th><th>Unit</th></tr>
     *   <tr><td>Easting</td><td>X</td><td>east</td><td>metre</td></tr>
     *   <tr><td>Northing</td><td>Y</td><td>north</td><td>metre</td></tr>
     * </table>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the properties.
     */
    @Test
    @DisplayName("GIGS projCRS L27")
    public void GIGS_62037() throws FactoryException {
        setCodeAndName(62037, "GIGS projCRS L27");
        createBaseCRS(Test3205::GIGS_64014);
        createConversion(Test3206::GIGS_65027);
        CoordinateSystemAxis axis1 = epsgFactory.createCoordinateSystemAxis("Easting", "X", AxisDirection.EAST, units.metre());
        CoordinateSystemAxis axis2 = epsgFactory.createCoordinateSystemAxis("Northing", "Y", AxisDirection.NORTH, units.metre());
        createCartesianCS(4499, axis1, axis2);
        verifyProjectedCRS();
    }

    /**
     * Tests “GIGS projCRS M25” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>GIGS projected CRS code: <b>62035</b></li>
     *   <li>GIGS projectedCRS name: <b>GIGS projCRS M25</b></li>
     *   <li>GIGS base CRS code: <b>64003</b></li>
     *   <li>GIGS conversion code: <b>65025</b></li>
     *   <li>Conversion definition source: <b>Described by user</b> (build with {@link ObjectFactory})</li>
     *   <li>EPSG coordinate system code: <b>4499</b></li>
     * </ul>
     * <table class="ogc">
     *   <caption>Coordinate system axes</caption>
     *   <tr><th>Name</th><th>Abbreviation</th><th>Orientation</th><th>Unit</th></tr>
     *   <tr><td>Easting</td><td>X</td><td>east</td><td>metre</td></tr>
     *   <tr><td>Northing</td><td>Y</td><td>north</td><td>metre</td></tr>
     * </table>
     *
     * Remarks: No direct EPSG equivalent.
     * Deprecated EPSG projCRS 2192 ED50 / France EuroLambert.
     * Remains relevant as represents LCC 1SP.
     * Not to be confused with replacement EPSG projCRS 2154 RGF93 / Lambert-93 (LCC 2SP).
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the properties.
     */
    @Test
    @DisplayName("GIGS projCRS M25")
    public void GIGS_62035() throws FactoryException {
        setCodeAndName(62035, "GIGS projCRS M25");
        createBaseCRS(Test3205::GIGS_64003);
        createConversion(Test3206::GIGS_65025);
        CoordinateSystemAxis axis1 = epsgFactory.createCoordinateSystemAxis("Easting", "X", AxisDirection.EAST, units.metre());
        CoordinateSystemAxis axis2 = epsgFactory.createCoordinateSystemAxis("Northing", "Y", AxisDirection.NORTH, units.metre());
        createCartesianCS(4499, axis1, axis2);
        verifyProjectedCRS();
    }

    /**
     * Tests “GIGS projCRS Y24” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>GIGS projected CRS code: <b>62034</b></li>
     *   <li>GIGS projectedCRS name: <b>GIGS projCRS Y24</b></li>
     *   <li>EPSG equivalence: <b>3388 – Pulkovo 1942 / Caspian Sea Mercator</b></li>
     *   <li>GIGS base CRS code: <b>64003</b></li>
     *   <li>GIGS conversion code: <b>65024</b></li>
     *   <li>Conversion definition source: <b>Described by user</b> (build with {@link ObjectFactory})</li>
     *   <li>EPSG coordinate system code: <b>4534</b></li>
     * </ul>
     * <table class="ogc">
     *   <caption>Coordinate system axes</caption>
     *   <tr><th>Name</th><th>Abbreviation</th><th>Orientation</th><th>Unit</th></tr>
     *   <tr><td>Northing</td><td>none</td><td>north</td><td>metre</td></tr>
     *   <tr><td>Easting</td><td>none</td><td>east</td><td>metre</td></tr>
     * </table>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the properties.
     */
    @Test
    @DisplayName("GIGS projCRS Y24")
    public void GIGS_62034() throws FactoryException {
        setCodeAndName(62034, "GIGS projCRS Y24");
        createBaseCRS(Test3205::GIGS_64003);
        createConversion(Test3206::GIGS_65024);
        CoordinateSystemAxis axis1 = epsgFactory.createCoordinateSystemAxis("Northing", "none", AxisDirection.NORTH, units.metre());
        CoordinateSystemAxis axis2 = epsgFactory.createCoordinateSystemAxis("Easting", "none", AxisDirection.EAST, units.metre());
        createCartesianCS(4534, axis1, axis2);
        verifyProjectedCRS();
    }

    /**
     * Tests “GIGS projCRS Z28” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>GIGS projected CRS code: <b>62039</b></li>
     *   <li>GIGS projectedCRS name: <b>GIGS projCRS Z28</b></li>
     *   <li>EPSG equivalence: <b>26908 – NAD83 / UTM zone 8N</b></li>
     *   <li>GIGS base CRS code: <b>64012</b></li>
     *   <li>GIGS conversion code: <b>65028</b></li>
     *   <li>Conversion definition source: <b>Described by user</b> (build with {@link ObjectFactory})</li>
     *   <li>EPSG coordinate system code: <b>4400</b></li>
     * </ul>
     * <table class="ogc">
     *   <caption>Coordinate system axes</caption>
     *   <tr><th>Name</th><th>Abbreviation</th><th>Orientation</th><th>Unit</th></tr>
     *   <tr><td>Easting</td><td>E</td><td>east</td><td>metre</td></tr>
     *   <tr><td>Northing</td><td>N</td><td>north</td><td>metre</td></tr>
     * </table>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the properties.
     */
    @Test
    @DisplayName("GIGS projCRS Z28")
    public void GIGS_62039() throws FactoryException {
        setCodeAndName(62039, "GIGS projCRS Z28");
        createBaseCRS(Test3205::GIGS_64012);
        createConversion(Test3206::GIGS_65028);
        CoordinateSystemAxis axis1 = epsgFactory.createCoordinateSystemAxis("Easting", "E", AxisDirection.EAST, units.metre());
        CoordinateSystemAxis axis2 = epsgFactory.createCoordinateSystemAxis("Northing", "N", AxisDirection.NORTH, units.metre());
        createCartesianCS(4400, axis1, axis2);
        verifyProjectedCRS();
    }
}
