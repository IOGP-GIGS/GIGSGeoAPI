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
import org.opengis.util.FactoryException;
import javax.measure.Unit;
import javax.measure.quantity.Angle;
import javax.measure.quantity.Length;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Verifies that the software allows correct definition of a user-defined geodetic CRS.
 *
 * <table class="gigs">
 * <caption>Test description</caption>
 * <tr>
 *   <th>Test method:</th>
 *   <td>Create user-defined objects for each of several different geodetic CRSs.</td>
 * </tr><tr>
 *   <th>Test data:</th>
 *   <td><a href="https://github.com/IOGP-GIGS/GIGSTestDataset/blob/main/GIGSTestDatasetFiles/GIGS%203200%20User-defined%20Geodetic%20Data%20Objects%20test%20data/ASCII/GIGS_user_3205_GeodeticCRS.txt">{@code GIGS_user_3205_GeodeticCRS.txt}</a>
 * </tr><tr>
 *   <th>Tested API:</th>
 *   <td>{@link CRSFactory#createGeographicCRS(Map, GeodeticDatum, EllipsoidalCS)} and<br>
 *       {@link CSFactory#createEllipsoidalCS(Map, CoordinateSystemAxis, CoordinateSystemAxis)}.</td>
 * </tr><tr>
 *   <th>Expected result:</th>
 *   <td>The geoscience software should accept the test data. The properties of the created objects will
 *       be compared with the properties given to the factory method.</td>
 * </tr></table>
 *
 *
 * <h2>Usage example</h2>
 * in order to specify their factories and run the tests in a JUnit framework, implementers can
 * define a subclass in their own test suite as in the example below:
 *
 * <blockquote><pre>public class MyTest extends Test3205 {
 *    public MyTest() {
 *        super(new MyDatumFactory(), new MyDatumAuthorityFactory(),
 *              new MyCSFactory(), MyCoordinateOperationFactory());
 *    }
 *}</pre></blockquote>
 *
 * @author  Michael Arneson (INT)
 * @author  Martin Desruisseaux (Geomatys)
 * @version 1.0
 * @since   1.0
 */
@DisplayName("User-defined geodetic CRS")
public class Test3205 extends Series3000<GeodeticCRS> {
    /**
     * Whether the CRS to create is geocentric.
     * Otherwise it is assumed geographic.
     */
    private boolean isGeocentric;

    /**
     * The CRS created by the factory,
     * or {@code null} if not yet created or if CRS creation failed.
     */
    private GeodeticCRS crs;

    /**
     * The Coordinate System EPSG Code of the CRS.
     */
    public int csCode;

    /**
     * The datum to use for building the CRS.
     * This is created either by EPSG code or by user definition.
     *
     * @see #datumTest
     */
    private GeodeticDatum datum;

    /**
     * Factory to use for building {@link GeodeticCRS} instances, or {@code null} if none.
     */
    protected final CRSFactory crsFactory;

    /**
     * Factory to use for building {@link GeodeticDatum} instances, or {@code null} if none.
     * May also be used for building {@link Ellipsoid} and {@link PrimeMeridian} components.
     */
    protected final DatumFactory datumFactory;

    /**
     * Factory to use for building {@link GeodeticDatum} and {@link PrimeMeridian} components, or {@code null} if none.
     * This is used only for tests with EPSG codes for datum components.
     *
     * @see #createDatum(int)
     */
    protected final DatumAuthorityFactory datumAuthorityFactory;

    /**
     * Data about the geodetic datum.
     * This is used only for tests with user definitions for CRS components.
     *
     * @see #createDatum(TestMethod)
     */
    private Test3204 datumTest;

    /**
     * The factory to use for creating coordinate system instances.
     */
    private final EPSGMock epsgFactory;

    /**
     * Creates a new test using the given factory. If a given factory is {@code null},
     * then the tests which depend on it will be skipped.
     *
     * @param crsFactory             factory for creating {@link GeodeticCRS} instances.
     * @param csFactory              factory for creating {@code CoordinateSystem} instances.
     * @param datumFactory           factory for creating {@link GeodeticDatum} instances.
     * @param datumAuthorityFactory  factory for creating {@link Ellipsoid} and {@link PrimeMeridian} components from EPSG codes.
     */
    public Test3205(final CRSFactory crsFactory, final CSFactory csFactory, final DatumFactory datumFactory,
                    final DatumAuthorityFactory datumAuthorityFactory)
    {
        this.crsFactory            = crsFactory;
        this.epsgFactory           = new EPSGMock(units, datumFactory, csFactory, validators);
        this.datumFactory          = datumFactory;
        this.datumAuthorityFactory = datumAuthorityFactory;
    }

    /**
     * Returns information about the configuration of the test which has been run.
     * This method returns a map containing:
     *
     * <ul>
     *   <li>All the following values associated to the {@link org.opengis.test.Configuration.Key} of the same name:
     *     <ul>
     *       <li>{@link #isFactoryPreservingUserValues}</li>
     *       <li>{@link #crsFactory}</li>
     *       <li>{@code csFactory}</li>
     *       <li>{@link #datumFactory}</li>
     *       <li>{@link #datumAuthorityFactory}</li>
     *     </ul>
     *   </li>
     * </ul>
     *
     * @return the configuration of the test being run.
     */
    @Override
    public Configuration configuration() {
        final Configuration op = super.configuration();
        assertNull(op.put(Configuration.Key.crsFactory, crsFactory));
        assertNull(op.put(Configuration.Key.csFactory, epsgFactory.getCSFactory()));
        assertNull(op.put(Configuration.Key.datumFactory, datumFactory));
        assertNull(op.put(Configuration.Key.datumAuthorityFactory, datumAuthorityFactory));
        return op;
    }

    /**
     * Creates a datum from the EPSG factory.
     *
     * @param  code      EPSG code of the datum to create.
     * @param  verifier  the test method to use for verifying the datum, or {@code null} if none.
     * @throws FactoryException if an error occurred while creating the datum.
     */
    private void createDatum(final int code, final TestMethod<Test3204> verifier) throws FactoryException {
        if (verifier != null) {
            datumTest = new Test3204(datumFactory, datumAuthorityFactory);
            datumTest.skipIdentificationCheck = true;
            datumTest.skipTests = true;
            verifier.test(datumTest);
        }
        datum = datumAuthorityFactory.createGeodeticDatum(String.valueOf(code));
    }

    /**
     * Creates a user-defined datum by executing the specified method from the {@link Test3204} class.
     *
     * @param  factory           the test method to use for creating the datum.
     * @throws FactoryException  if an error occurred while creating the datum.
     */
    private void createDatum(final TestMethod<Test3204> factory) throws FactoryException {
        datumTest = new Test3204(datumFactory, datumAuthorityFactory);
        datumTest.skipTests = true;
        factory.test(datumTest);
        datum = datumTest.getIdentifiedObject();
    }

    /**
     * Returns the geodetic CRS instance to be tested. When this method is invoked for the first time,
     * it creates the geodetic CRS to test by invoking the corresponding method from {@link #crsFactory}
     * with the current {@link #properties properties} map in argument.
     * The created object is then cached and returned in all subsequent invocations of this method.
     *
     * @return the geodetic CRS instance to test.
     * @throws FactoryException if an error occurred while creating the geodetic datum instance.
     */
    @Override
    public GeodeticCRS getIdentifiedObject() throws FactoryException {
        if (crs == null) {
            assumeNotNull(crsFactory);
            if (isGeocentric) {
                CartesianCS cs = epsgFactory.createCartesianCS(String.valueOf(csCode));
                crs = crsFactory.createGeocentricCRS(properties, datum, cs);
            } else {
                EllipsoidalCS cs = epsgFactory.createEllipsoidalCS(String.valueOf(csCode));
                crs = crsFactory.createGeographicCRS(properties, datum, cs);
            }
        }
        return crs;
    }

    /**
     * Sets the geographic 2D CRS instance to verify. This method is invoked only by other test classes which need to
     * verify the geographic 2D CRS contained in a CRS instead of the Geographic 2D CRS immediately after creation.
     *
     * @param  instance  the instance to verify.
     */
    final void setIdentifiedObject(final GeographicCRS instance) {
        crs = instance;
    }

    /**
     * Verifies a geographic CRS for the given properties and verify its properties after construction.
     *
     * @throws FactoryException if an error occurred while creating the CRS instance.
     */
    final void verifyGeographicCRS() throws FactoryException {
        if (skipTests) {
            return;
        }
        if (crsFactory != null) {
            final GeodeticCRS crs = getIdentifiedObject();
            assertInstanceOf(GeographicCRS.class, crs, "CRSFactory.createGeographicCRS(…) shall not return null.");
            validators.validate((GeographicCRS) crs);
            verifyIdentificationAndDatum(crs);
            /*
             * Verify axes: may be two- or three-dimensional, (φ,λ) or (λ,φ) order, in angular degrees or grads.
             * Those properties are inferred from the coordinate system code.
             */
            Unit<Angle> angularUnit = units.degree();
            AxisDirection[] directions = Test2205.GEOGRAPHIC_2D;
            switch (csCode) {
                case 6403: angularUnit = units.grad(); break;
                case 6423: directions = Test2205.GEOGRAPHIC_3D; break;
                case 6424: directions = Test2205.GEOGRAPHIC_XY; break;
            }
            verifyCoordinateSystem(crs.getCoordinateSystem(), EllipsoidalCS.class,
                    directions, angularUnit, angularUnit, units.metre());
        }
    }

    /**
     * Verifies a geocentric CRS for the given properties and verify its properties after construction.
     *
     * @throws FactoryException if an error occurred while creating the CRS instance.
     */
    final void verifyGeocentricCRS() throws FactoryException {
        if (skipTests) {
            return;
        }
        if (crsFactory != null) {
            final GeodeticCRS crs = getIdentifiedObject();
            assertInstanceOf(GeocentricCRS.class, crs, "CRSFactory.createGeocentricCRS(…) shall not return null.");
            validators.validate((GeocentricCRS) crs);
            verifyIdentificationAndDatum(crs);
            /*
             * Verify axes.
             */
            Unit<Length> linearUnit = units.metre();
            AxisDirection[] directions = Test2205.GEOCENTRIC;
            verifyCoordinateSystem(crs.getCoordinateSystem(), EllipsoidalCS.class,
                    directions, linearUnit, linearUnit, units.metre());
        }
    }

    /**
     * Use {@link Test3204} for verifying the datum.
     *
     * @param  crs  the CRS to validate.
     * @throws FactoryException if the test methods needed to create an object and that creation failed.
     *         Should not happen, because this test method is not expected to create new objects.
     */
    private void verifyIdentificationAndDatum(final GeodeticCRS crs) throws FactoryException {
        verifyIdentification(crs, getName(), String.valueOf(getCode()));
        if (datumTest != null) {
            datumTest.copyConfigurationFrom(this);
            datumTest.setIdentifiedObject(crs.getDatum());
            datumTest.verifyDatum();
        }
    }

    /**
     * Tests “GIGS geocenCRS A” Geocentric CRS from the factory.
     *
     * <ul>
     *   <li>GIGS CRS code: <b>64001</b></li>
     *   <li>GIGS CRS name: <b>GIGS geocenCRS A</b></li>
     *   <li>EPSG equivalence: <b>4978 – WGS 84</b></li>
     *   <li>Datum definition source: <b>Described by user</b> (build with {@link DatumFactory})</li>
     *   <li>Coordinate System code: <b>6500</b></li>
     *   <li>Geodetic CRS Type: <b>Geocentric</b></li>
     *   <li>GIGS Datum code: <b>66001</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the properties.
     */
    @Test
    @DisplayName("GIGS geocenCRS A")
    public void GIGS_64001() throws FactoryException {
        setCodeAndName(64001, "GIGS geocenCRS A");
        createDatum(Test3204::GIGS_66001);
        csCode = 6500;
        isGeocentric = true;
        verifyGeocentricCRS();
    }

    /**
     * Tests “GIGS geog3DCRS A” Geographic 3D CRS from the factory.
     *
     * <ul>
     *   <li>GIGS CRS code: <b>64002</b></li>
     *   <li>GIGS CRS name: <b>GIGS geog3DCRS A</b></li>
     *   <li>EPSG equivalence: <b>4979 – WGS 84</b></li>
     *   <li>Datum definition source: <b>Described by user</b> (build with {@link DatumFactory})</li>
     *   <li>Coordinate System code: <b>6423</b></li>
     *   <li>Geodetic CRS Type: <b>Geographic 3D</b></li>
     *   <li>GIGS Datum code: <b>66001</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the properties.
     */
    @Test
    @DisplayName("GIGS geog3DCRS A")
    public void GIGS_64002() throws FactoryException {
        setCodeAndName(64002, "GIGS geog3DCRS A");
        createDatum(Test3204::GIGS_66001);
        csCode = 6423;
        verifyGeographicCRS();
    }

    /**
     * Tests “GIGS geog3DCRS B” Geographic 3D CRS from the factory.
     *
     * <ul>
     *   <li>GIGS CRS code: <b>64019</b></li>
     *   <li>GIGS CRS name: <b>GIGS geog3DCRS B</b></li>
     *   <li>Datum definition source: <b>Described by user</b> (build with {@link DatumFactory})</li>
     *   <li>Coordinate System code: <b>6423</b></li>
     *   <li>Geodetic CRS Type: <b>Geographic 3D</b></li>
     *   <li>GIGS Datum code: <b>66002</b></li>
     * </ul>
     *
     * Remarks: No direct EPSG equivalent.
     *
     * @throws FactoryException if an error occurred while creating the CRS from the properties.
     */
    @Test
    @DisplayName("GIGS geog3DCRS B")
    public void GIGS_64019() throws FactoryException {
        setCodeAndName(64019, "GIGS geog3DCRS B");
        createDatum(Test3204::GIGS_66002);
        csCode = 6423;
        verifyGeographicCRS();
    }

    /**
     * Tests “GIGS geog3DCRS C” Geographic 3D CRS from the factory.
     *
     * <ul>
     *   <li>GIGS CRS code: <b>64021</b></li>
     *   <li>GIGS CRS name: <b>GIGS geog3DCRS C</b></li>
     *   <li>Datum definition source: <b>Described by user</b> (build with {@link DatumFactory})</li>
     *   <li>Coordinate System code: <b>6423</b></li>
     *   <li>Geodetic CRS Type: <b>Geographic 3D</b></li>
     *   <li>GIGS Datum code: <b>66003</b></li>
     * </ul>
     *
     * Remarks: No direct EPSG equivalent.
     *
     * @throws FactoryException if an error occurred while creating the CRS from the properties.
     */
    @Test
    @DisplayName("GIGS geog3DCRS C")
    public void GIGS_64021() throws FactoryException {
        setCodeAndName(64021, "GIGS geog3DCRS C");
        createDatum(Test3204::GIGS_66003);
        csCode = 6423;
        verifyGeographicCRS();
    }

    /**
     * Tests “GIGS geog3DCRS E” Geographic 3D CRS from the factory.
     *
     * <ul>
     *   <li>GIGS CRS code: <b>64022</b></li>
     *   <li>GIGS CRS name: <b>GIGS geog3DCRS E</b></li>
     *   <li>Datum definition source: <b>Described by user</b> (build with {@link DatumFactory})</li>
     *   <li>Coordinate System code: <b>6423</b></li>
     *   <li>Geodetic CRS Type: <b>Geographic 3D</b></li>
     *   <li>GIGS Datum code: <b>66005</b></li>
     * </ul>
     *
     * Remarks: No direct EPSG equivalent.
     *
     * @throws FactoryException if an error occurred while creating the CRS from the properties.
     */
    @Test
    @DisplayName("GIGS geog3DCRS E")
    public void GIGS_64022() throws FactoryException {
        setCodeAndName(64022, "GIGS geog3DCRS E");
        createDatum(Test3204::GIGS_66005);
        csCode = 6423;
        verifyGeographicCRS();
    }

    /**
     * Tests “GIGS geogCRS A” Geographic 2D CRS from the factory.
     *
     * <ul>
     *   <li>GIGS CRS code: <b>64003</b></li>
     *   <li>GIGS CRS name: <b>GIGS geogCRS A</b></li>
     *   <li>EPSG equivalence: <b>4326 – WGS 84</b></li>
     *   <li>Datum definition source: <b>Described by user</b> (build with {@link DatumFactory})</li>
     *   <li>Coordinate System code: <b>6422</b></li>
     *   <li>Geodetic CRS Type: <b>Geographic 2D</b></li>
     *   <li>GIGS Datum code: <b>66001</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the properties.
     */
    @Test
    @DisplayName("GIGS geogCRS A")
    public void GIGS_64003() throws FactoryException {
        setCodeAndName(64003, "GIGS geogCRS A");
        createDatum(Test3204::GIGS_66001);
        csCode = 6422;
        verifyGeographicCRS();
    }

    /**
     * Tests “GIGS geogCRS AA” Geographic 2D CRS from the factory.
     *
     * <ul>
     *   <li>GIGS CRS code: <b>64326</b></li>
     *   <li>GIGS CRS name: <b>GIGS geogCRS AA</b></li>
     *   <li>EPSG equivalence: <b>4326 – WGS 84</b></li>
     *   <li>Datum definition source: <b>Fetched from EPSG dataset</b> (build with {@link DatumAuthorityFactory})</li>
     *   <li>Coordinate System code: <b>6422</b></li>
     *   <li>Geodetic CRS Type: <b>Geographic 2D</b></li>
     *   <li>GIGS Datum code: <b>66326</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the properties.
     */
    @Test
    @DisplayName("GIGS geogCRS AA")
    public void GIGS_64326() throws FactoryException {
        setCodeAndName(64326, "GIGS geogCRS AA");
        createDatum(6326, Test3204::GIGS_66326);
        csCode = 6422;
        verifyGeographicCRS();
    }

    /**
     * Tests “GIGS geogCRS Agr” Geographic 2D CRS from the factory.
     *
     * <ul>
     *   <li>GIGS CRS code: <b>64033</b></li>
     *   <li>GIGS CRS name: <b>GIGS geogCRS Agr</b></li>
     *   <li>Datum definition source: <b>Described by user</b> (build with {@link DatumFactory})</li>
     *   <li>Coordinate System code: <b>6403</b></li>
     *   <li>Geodetic CRS Type: <b>Geographic 2D</b></li>
     *   <li>GIGS Datum code: <b>66001</b></li>
     * </ul>
     *
     * Remarks: No direct EPSG equivalent.
     * WGS 84 in grads.
     *
     * @throws FactoryException if an error occurred while creating the CRS from the properties.
     */
    @Test
    @DisplayName("GIGS geogCRS Agr")
    public void GIGS_64033() throws FactoryException {
        setCodeAndName(64033, "GIGS geogCRS Agr");
        createDatum(Test3204::GIGS_66001);
        csCode = 6403;
        verifyGeographicCRS();
    }

    /**
     * Tests “GIGS geogCRS Alonlat” Geographic 2D CRS from the factory.
     *
     * <ul>
     *   <li>GIGS CRS code: <b>64004</b></li>
     *   <li>GIGS CRS name: <b>GIGS geogCRS Alonlat</b></li>
     *   <li>Datum definition source: <b>Described by user</b> (build with {@link DatumFactory})</li>
     *   <li>Coordinate System code: <b>6424</b></li>
     *   <li>Geodetic CRS Type: <b>Geographic 2D</b></li>
     *   <li>GIGS Datum code: <b>66001</b></li>
     * </ul>
     *
     * Remarks: No direct EPSG equivalent.
     * WGS 84 with CS axes changed.
     *
     * @throws FactoryException if an error occurred while creating the CRS from the properties.
     */
    @Test
    @DisplayName("GIGS geogCRS Alonlat")
    public void GIGS_64004() throws FactoryException {
        setCodeAndName(64004, "GIGS geogCRS Alonlat");
        createDatum(Test3204::GIGS_66001);
        csCode = 6424;
        verifyGeographicCRS();
    }

    /**
     * Tests “GIGS geogCRS B” Geographic 2D CRS from the factory.
     *
     * <ul>
     *   <li>GIGS CRS code: <b>64005</b></li>
     *   <li>GIGS CRS name: <b>GIGS geogCRS B</b></li>
     *   <li>EPSG equivalence: <b>4277 – OSGB36</b></li>
     *   <li>Datum definition source: <b>Described by user</b> (build with {@link DatumFactory})</li>
     *   <li>Coordinate System code: <b>6422</b></li>
     *   <li>Geodetic CRS Type: <b>Geographic 2D</b></li>
     *   <li>GIGS Datum code: <b>66002</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the properties.
     */
    @Test
    @DisplayName("GIGS geogCRS B")
    public void GIGS_64005() throws FactoryException {
        setCodeAndName(64005, "GIGS geogCRS B");
        createDatum(Test3204::GIGS_66002);
        csCode = 6422;
        verifyGeographicCRS();
    }

    /**
     * Tests “GIGS geogCRS BB” Geographic 2D CRS from the factory.
     *
     * <ul>
     *   <li>GIGS CRS code: <b>64277</b></li>
     *   <li>GIGS CRS name: <b>GIGS geogCRS BB</b></li>
     *   <li>EPSG equivalence: <b>4277 – OSGB36</b></li>
     *   <li>Datum definition source: <b>Fetched from EPSG dataset</b> (build with {@link DatumAuthorityFactory})</li>
     *   <li>Coordinate System code: <b>6422</b></li>
     *   <li>Geodetic CRS Type: <b>Geographic 2D</b></li>
     *   <li>GIGS Datum code: <b>66277</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the properties.
     */
    @Test
    @DisplayName("GIGS geogCRS BB")
    public void GIGS_64277() throws FactoryException {
        setCodeAndName(64277, "GIGS geogCRS BB");
        createDatum(6277, Test3204::GIGS_66277);
        csCode = 6422;
        verifyGeographicCRS();
    }

    /**
     * Tests “GIGS geogCRS C” Geographic 2D CRS from the factory.
     *
     * <ul>
     *   <li>GIGS CRS code: <b>64006</b></li>
     *   <li>GIGS CRS name: <b>GIGS geogCRS C</b></li>
     *   <li>EPSG equivalence: <b>4289 – Amersfoort</b></li>
     *   <li>Datum definition source: <b>Described by user</b> (build with {@link DatumFactory})</li>
     *   <li>Coordinate System code: <b>6422</b></li>
     *   <li>Geodetic CRS Type: <b>Geographic 2D</b></li>
     *   <li>GIGS Datum code: <b>66003</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the properties.
     */
    @Test
    @DisplayName("GIGS geogCRS C")
    public void GIGS_64006() throws FactoryException {
        setCodeAndName(64006, "GIGS geogCRS C");
        createDatum(Test3204::GIGS_66003);
        csCode = 6422;
        verifyGeographicCRS();
    }

    /**
     * Tests “GIGS geogCRS CC” Geographic 2D CRS from the factory.
     *
     * <ul>
     *   <li>GIGS CRS code: <b>64289</b></li>
     *   <li>GIGS CRS name: <b>GIGS geogCRS CC</b></li>
     *   <li>EPSG equivalence: <b>4289 – Amersfoort</b></li>
     *   <li>Datum definition source: <b>Fetched from EPSG dataset</b> (build with {@link DatumAuthorityFactory})</li>
     *   <li>Coordinate System code: <b>6422</b></li>
     *   <li>Geodetic CRS Type: <b>Geographic 2D</b></li>
     *   <li>GIGS Datum code: <b>66289</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the properties.
     */
    @Test
    @DisplayName("GIGS geogCRS CC")
    public void GIGS_64289() throws FactoryException {
        setCodeAndName(64289, "GIGS geogCRS CC");
        createDatum(6289, Test3204::GIGS_66289);
        csCode = 6422;
        verifyGeographicCRS();
    }

    /**
     * Tests “GIGS geogCRS D” Geographic 2D CRS from the factory.
     *
     * <ul>
     *   <li>GIGS CRS code: <b>64007</b></li>
     *   <li>GIGS CRS name: <b>GIGS geogCRS D</b></li>
     *   <li>EPSG equivalence: <b>4813 – Batavia (Jakarta)</b></li>
     *   <li>Datum definition source: <b>Described by user</b> (build with {@link DatumFactory})</li>
     *   <li>Coordinate System code: <b>6422</b></li>
     *   <li>Geodetic CRS Type: <b>Geographic 2D</b></li>
     *   <li>GIGS Datum code: <b>66004</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the properties.
     */
    @Test
    @DisplayName("GIGS geogCRS D")
    public void GIGS_64007() throws FactoryException {
        setCodeAndName(64007, "GIGS geogCRS D");
        createDatum(Test3204::GIGS_66004);
        csCode = 6422;
        verifyGeographicCRS();
    }

    /**
     * Tests “GIGS geogCRS DD” Geographic 2D CRS from the factory.
     *
     * <ul>
     *   <li>GIGS CRS code: <b>64813</b></li>
     *   <li>GIGS CRS name: <b>GIGS geogCRS DD</b></li>
     *   <li>EPSG equivalence: <b>4813 – Batavia (Jakarta)</b></li>
     *   <li>Datum definition source: <b>Fetched from EPSG dataset</b> (build with {@link DatumAuthorityFactory})</li>
     *   <li>Coordinate System code: <b>6422</b></li>
     *   <li>Geodetic CRS Type: <b>Geographic 2D</b></li>
     *   <li>GIGS Datum code: <b>66813</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the properties.
     */
    @Test
    @DisplayName("GIGS geogCRS DD")
    public void GIGS_64813() throws FactoryException {
        setCodeAndName(64813, "GIGS geogCRS DD");
        createDatum(6813, Test3204::GIGS_66813);
        csCode = 6422;
        verifyGeographicCRS();
    }

    /**
     * Tests “GIGS geogCRS E” Geographic 2D CRS from the factory.
     *
     * <ul>
     *   <li>GIGS CRS code: <b>64008</b></li>
     *   <li>GIGS CRS name: <b>GIGS geogCRS E</b></li>
     *   <li>EPSG equivalence: <b>4313 – Belge 1972</b></li>
     *   <li>Datum definition source: <b>Described by user</b> (build with {@link DatumFactory})</li>
     *   <li>Coordinate System code: <b>6422</b></li>
     *   <li>Geodetic CRS Type: <b>Geographic 2D</b></li>
     *   <li>GIGS Datum code: <b>66005</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the properties.
     */
    @Test
    @DisplayName("GIGS geogCRS E")
    public void GIGS_64008() throws FactoryException {
        setCodeAndName(64008, "GIGS geogCRS E");
        createDatum(Test3204::GIGS_66005);
        csCode = 6422;
        verifyGeographicCRS();
    }

    /**
     * Tests “GIGS geogCRS EE” Geographic 2D CRS from the factory.
     *
     * <ul>
     *   <li>GIGS CRS code: <b>64313</b></li>
     *   <li>GIGS CRS name: <b>GIGS geogCRS EE</b></li>
     *   <li>EPSG equivalence: <b>4313 – Belge 1972</b></li>
     *   <li>Datum definition source: <b>Fetched from EPSG dataset</b> (build with {@link DatumAuthorityFactory})</li>
     *   <li>Coordinate System code: <b>6422</b></li>
     *   <li>Geodetic CRS Type: <b>Geographic 2D</b></li>
     *   <li>GIGS Datum code: <b>66313</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the properties.
     */
    @Test
    @DisplayName("GIGS geogCRS EE")
    public void GIGS_64313() throws FactoryException {
        setCodeAndName(64313, "GIGS geogCRS EE");
        createDatum(6313, Test3204::GIGS_66313);
        csCode = 6422;
        verifyGeographicCRS();
    }

    /**
     * Tests “GIGS geogCRS F” Geographic 2D CRS from the factory.
     *
     * <ul>
     *   <li>GIGS CRS code: <b>64009</b></li>
     *   <li>GIGS CRS name: <b>GIGS geogCRS F</b></li>
     *   <li>EPSG equivalence: <b>4283 – GDA94</b></li>
     *   <li>Datum definition source: <b>Described by user</b> (build with {@link DatumFactory})</li>
     *   <li>Coordinate System code: <b>6422</b></li>
     *   <li>Geodetic CRS Type: <b>Geographic 2D</b></li>
     *   <li>GIGS Datum code: <b>66006</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the properties.
     */
    @Test
    @DisplayName("GIGS geogCRS F")
    public void GIGS_64009() throws FactoryException {
        setCodeAndName(64009, "GIGS geogCRS F");
        createDatum(Test3204::GIGS_66006);
        csCode = 6422;
        verifyGeographicCRS();
    }

    /**
     * Tests “GIGS geogCRS FF” Geographic 2D CRS from the factory.
     *
     * <ul>
     *   <li>GIGS CRS code: <b>64283</b></li>
     *   <li>GIGS CRS name: <b>GIGS geogCRS FF</b></li>
     *   <li>EPSG equivalence: <b>4283 – GDA94</b></li>
     *   <li>Datum definition source: <b>Fetched from EPSG dataset</b> (build with {@link DatumAuthorityFactory})</li>
     *   <li>Coordinate System code: <b>6422</b></li>
     *   <li>Geodetic CRS Type: <b>Geographic 2D</b></li>
     *   <li>GIGS Datum code: <b>66283</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the properties.
     */
    @Test
    @DisplayName("GIGS geogCRS FF")
    public void GIGS_64283() throws FactoryException {
        setCodeAndName(64283, "GIGS geogCRS FF");
        createDatum(6283, Test3204::GIGS_66283);
        csCode = 6422;
        verifyGeographicCRS();
    }

    /**
     * Tests “GIGS geogCRS G” Geographic 2D CRS from the factory.
     *
     * <ul>
     *   <li>GIGS CRS code: <b>64010</b></li>
     *   <li>GIGS CRS name: <b>GIGS geogCRS G</b></li>
     *   <li>EPSG equivalence (1 of 6): <b>4258 – ETRS89</b></li>
     *   <li>EPSG equivalence (2 of 6): <b>4742 – GDM2000</b></li>
     *   <li>EPSG equivalence (3 of 6): <b>4152 – NAD83(HARN)</b></li>
     *   <li>EPSG equivalence (4 of 6): <b>4190 – POSGAR98</b></li>
     *   <li>EPSG equivalence (5 of 6): <b>4674 – SIRGAS 2000</b></li>
     *   <li>EPSG equivalence (6 of 6): <b>4148 – Hartebeesthoek94</b></li>
     *   <li>Datum definition source: <b>Described by user</b> (build with {@link DatumFactory})</li>
     *   <li>Coordinate System code: <b>6422</b></li>
     *   <li>Geodetic CRS Type: <b>Geographic 2D</b></li>
     *   <li>GIGS Datum code: <b>66007</b></li>
     * </ul>
     *
     * Remarks: This GIGS CRS is functionally equivalent to any geodetic CRS with a static datum using the GRS 1980 ellipsoid.
     *
     * @throws FactoryException if an error occurred while creating the CRS from the properties.
     */
    @Test
    @DisplayName("GIGS geogCRS G")
    public void GIGS_64010() throws FactoryException {
        setCodeAndName(64010, "GIGS geogCRS G");
        createDatum(Test3204::GIGS_66007);
        csCode = 6422;
        verifyGeographicCRS();
    }

    /**
     * Tests “GIGS geogCRS H” Geographic 2D CRS from the factory.
     *
     * <ul>
     *   <li>GIGS CRS code: <b>64011</b></li>
     *   <li>GIGS CRS name: <b>GIGS geogCRS H</b></li>
     *   <li>EPSG equivalence: <b>4807 – NTF (Paris)</b></li>
     *   <li>Datum definition source: <b>Described by user</b> (build with {@link DatumFactory})</li>
     *   <li>Coordinate System code: <b>6403</b></li>
     *   <li>Geodetic CRS Type: <b>Geographic 2D</b></li>
     *   <li>GIGS Datum code: <b>66008</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the properties.
     */
    @Test
    @DisplayName("GIGS geogCRS H")
    public void GIGS_64011() throws FactoryException {
        setCodeAndName(64011, "GIGS geogCRS H");
        createDatum(Test3204::GIGS_66008);
        csCode = 6403;
        verifyGeographicCRS();
    }

    /**
     * Tests “GIGS geogCRS HH” Geographic 2D CRS from the factory.
     *
     * <ul>
     *   <li>GIGS CRS code: <b>64807</b></li>
     *   <li>GIGS CRS name: <b>GIGS geogCRS HH</b></li>
     *   <li>EPSG equivalence: <b>4807 – NTF(Paris)</b></li>
     *   <li>Datum definition source: <b>Fetched from EPSG dataset</b> (build with {@link DatumAuthorityFactory})</li>
     *   <li>Coordinate System code: <b>6422</b></li>
     *   <li>Geodetic CRS Type: <b>Geographic 2D</b></li>
     *   <li>GIGS Datum code: <b>66807</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the properties.
     */
    @Test
    @DisplayName("GIGS geogCRS HH")
    public void GIGS_64807() throws FactoryException {
        setCodeAndName(64807, "GIGS geogCRS HH");
        createDatum(6807, Test3204::GIGS_66807);
        csCode = 6422;
        verifyGeographicCRS();
    }

    /**
     * Tests “GIGS geogCRS J” Geographic 2D CRS from the factory.
     *
     * <ul>
     *   <li>GIGS CRS code: <b>64012</b></li>
     *   <li>GIGS CRS name: <b>GIGS geogCRS J</b></li>
     *   <li>EPSG equivalence: <b>4267 – NAD27</b></li>
     *   <li>Datum definition source: <b>Described by user</b> (build with {@link DatumFactory})</li>
     *   <li>Coordinate System code: <b>6422</b></li>
     *   <li>Geodetic CRS Type: <b>Geographic 2D</b></li>
     *   <li>GIGS Datum code: <b>66009</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the properties.
     */
    @Test
    @DisplayName("GIGS geogCRS J")
    public void GIGS_64012() throws FactoryException {
        setCodeAndName(64012, "GIGS geogCRS J");
        createDatum(Test3204::GIGS_66009);
        csCode = 6422;
        verifyGeographicCRS();
    }

    /**
     * Tests “GIGS geogCRS K” Geographic 2D CRS from the factory.
     *
     * <ul>
     *   <li>GIGS CRS code: <b>64015</b></li>
     *   <li>GIGS CRS name: <b>GIGS geogCRS K</b></li>
     *   <li>EPSG equivalence: <b>4237 – HD72</b></li>
     *   <li>Datum definition source: <b>Described by user</b> (build with {@link DatumFactory})</li>
     *   <li>Coordinate System code: <b>6422</b></li>
     *   <li>Geodetic CRS Type: <b>Geographic 2D</b></li>
     *   <li>GIGS Datum code: <b>66012</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the properties.
     */
    @Test
    @DisplayName("GIGS geogCRS K")
    public void GIGS_64015() throws FactoryException {
        setCodeAndName(64015, "GIGS geogCRS K");
        createDatum(Test3204::GIGS_66012);
        csCode = 6422;
        verifyGeographicCRS();
    }

    /**
     * Tests “GIGS geogCRS L” Geographic 2D CRS from the factory.
     *
     * <ul>
     *   <li>GIGS CRS code: <b>64014</b></li>
     *   <li>GIGS CRS name: <b>GIGS geogCRS L</b></li>
     *   <li>EPSG equivalence: <b>4211 – Batavia</b></li>
     *   <li>Datum definition source: <b>Described by user</b> (build with {@link DatumFactory})</li>
     *   <li>Coordinate System code: <b>6422</b></li>
     *   <li>Geodetic CRS Type: <b>Geographic 2D</b></li>
     *   <li>GIGS Datum code: <b>66011</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the properties.
     */
    @Test
    @DisplayName("GIGS geogCRS L")
    public void GIGS_64014() throws FactoryException {
        setCodeAndName(64014, "GIGS geogCRS L");
        createDatum(Test3204::GIGS_66011);
        csCode = 6422;
        verifyGeographicCRS();
    }

    /**
     * Tests “GIGS geogCRS M” Geographic 2D CRS from the factory.
     *
     * <ul>
     *   <li>GIGS CRS code: <b>64020</b></li>
     *   <li>GIGS CRS name: <b>GIGS geogCRS M</b></li>
     *   <li>EPSG equivalence: <b>4230 – ED50</b></li>
     *   <li>Datum definition source: <b>Described by user</b> (build with {@link DatumFactory})</li>
     *   <li>Coordinate System code: <b>6422</b></li>
     *   <li>Geodetic CRS Type: <b>Geographic 2D</b></li>
     *   <li>GIGS Datum code: <b>66016</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the properties.
     */
    @Test
    @DisplayName("GIGS geogCRS M")
    public void GIGS_64020() throws FactoryException {
        setCodeAndName(64020, "GIGS geogCRS M");
        createDatum(Test3204::GIGS_66016);
        csCode = 6422;
        verifyGeographicCRS();
    }

    /**
     * Tests “GIGS geogCRS T” Geographic 2D CRS from the factory.
     *
     * <ul>
     *   <li>GIGS CRS code: <b>64013</b></li>
     *   <li>GIGS CRS name: <b>GIGS geogCRS T</b></li>
     *   <li>EPSG equivalence: <b>4275 – NTF</b></li>
     *   <li>Datum definition source: <b>Described by user</b> (build with {@link DatumFactory})</li>
     *   <li>Coordinate System code: <b>6403</b></li>
     *   <li>Geodetic CRS Type: <b>Geographic 2D</b></li>
     *   <li>GIGS Datum code: <b>66010</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the properties.
     */
    @Test
    @DisplayName("GIGS geogCRS T")
    public void GIGS_64013() throws FactoryException {
        setCodeAndName(64013, "GIGS geogCRS T");
        createDatum(Test3204::GIGS_66010);
        csCode = 6403;
        verifyGeographicCRS();
    }

    /**
     * Tests “GIGS geogCRS X” Geographic 2D CRS from the factory.
     *
     * <ul>
     *   <li>GIGS CRS code: <b>64016</b></li>
     *   <li>GIGS CRS name: <b>GIGS geogCRS X</b></li>
     *   <li>EPSG equivalence: <b>4202 – AGD66</b></li>
     *   <li>Datum definition source: <b>Described by user</b> (build with {@link DatumFactory})</li>
     *   <li>Coordinate System code: <b>6422</b></li>
     *   <li>Geodetic CRS Type: <b>Geographic 2D</b></li>
     *   <li>GIGS Datum code: <b>66013</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the properties.
     */
    @Test
    @DisplayName("GIGS geogCRS X")
    public void GIGS_64016() throws FactoryException {
        setCodeAndName(64016, "GIGS geogCRS X");
        createDatum(Test3204::GIGS_66013);
        csCode = 6422;
        verifyGeographicCRS();
    }

    /**
     * Tests “GIGS geogCRS Y” Geographic 2D CRS from the factory.
     *
     * <ul>
     *   <li>GIGS CRS code: <b>64017</b></li>
     *   <li>GIGS CRS name: <b>GIGS geogCRS Y</b></li>
     *   <li>EPSG equivalence: <b>4284 – Pulkovo 1942</b></li>
     *   <li>Datum definition source: <b>Described by user</b> (build with {@link DatumFactory})</li>
     *   <li>Coordinate System code: <b>6422</b></li>
     *   <li>Geodetic CRS Type: <b>Geographic 2D</b></li>
     *   <li>GIGS Datum code: <b>66014</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the properties.
     */
    @Test
    @DisplayName("GIGS geogCRS Y")
    public void GIGS_64017() throws FactoryException {
        setCodeAndName(64017, "GIGS geogCRS Y");
        createDatum(Test3204::GIGS_66014);
        csCode = 6422;
        verifyGeographicCRS();
    }

    /**
     * Tests “GIGS geogCRS Z” Geographic 2D CRS from the factory.
     *
     * <ul>
     *   <li>GIGS CRS code: <b>64018</b></li>
     *   <li>GIGS CRS name: <b>GIGS geogCRS Z</b></li>
     *   <li>EPSG equivalence: <b>4269 – NAD83</b></li>
     *   <li>Datum definition source: <b>Described by user</b> (build with {@link DatumFactory})</li>
     *   <li>Coordinate System code: <b>6422</b></li>
     *   <li>Geodetic CRS Type: <b>Geographic 2D</b></li>
     *   <li>GIGS Datum code: <b>66015</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the properties.
     */
    @Test
    @DisplayName("GIGS geogCRS Z")
    public void GIGS_64018() throws FactoryException {
        setCodeAndName(64018, "GIGS geogCRS Z");
        createDatum(Test3204::GIGS_66015);
        csCode = 6422;
        verifyGeographicCRS();
    }

    /**
     * Tests “GIGS geogCRS ZZ” Geographic 2D CRS from the factory.
     *
     * <ul>
     *   <li>GIGS CRS code: <b>64269</b></li>
     *   <li>GIGS CRS name: <b>GIGS geogCRS ZZ</b></li>
     *   <li>EPSG equivalence: <b>4269 – NAD83</b></li>
     *   <li>Datum definition source: <b>Fetched from EPSG dataset</b> (build with {@link DatumAuthorityFactory})</li>
     *   <li>Coordinate System code: <b>6422</b></li>
     *   <li>Geodetic CRS Type: <b>Geographic 2D</b></li>
     *   <li>GIGS Datum code: <b>66269</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the properties.
     */
    @Test
    @DisplayName("GIGS geogCRS ZZ")
    public void GIGS_64269() throws FactoryException {
        setCodeAndName(64269, "GIGS geogCRS ZZ");
        createDatum(6269, Test3204::GIGS_66269);
        csCode = 6422;
        verifyGeographicCRS();
    }
}
