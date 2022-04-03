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

import org.opengis.util.FactoryException;
import org.opengis.util.InternationalString;
import org.opengis.referencing.datum.Ellipsoid;
import org.opengis.referencing.datum.PrimeMeridian;
import org.opengis.referencing.datum.GeodeticDatum;
import org.opengis.referencing.datum.DatumFactory;
import org.opengis.referencing.datum.DatumAuthorityFactory;
import org.iogp.gigs.internal.geoapi.Configuration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Verifies that the software allows correct definition of a user-defined geodetic datum.
 * Each test method in this class instantiate exactly one {@link GeodeticDatum}.
 *
 * <table class="gigs">
 * <caption>Test description</caption>
 * <tr>
 *   <th>Test method:</th>
 *   <td>Create user-defined geodetic datum for each of several different datums.</td>
 * </tr><tr>
 *   <th>Test data:</th>
 *   <td><a href="https://github.com/IOGP-GIGS/GIGSTestDataset/blob/main/GIGSTestDatasetFiles/GIGS%203200%20User-defined%20Geodetic%20Data%20Objects%20test%20data/ASCII/GIGS_user_3204_GeodeticDatum.txt">{@code GIGS_user_3204_GeodeticDatum.txt}</a>
 * </tr><tr>
 *   <th>Tested API:</th>
 *   <td>{@link DatumFactory#createGeodeticDatum(Map, Ellipsoid, PrimeMeridian)}.</td>
 * </tr><tr>
 *   <th>Expected result:</th>
 *   <td>The software should accept the test data. The properties of the created objects will
 *       be compared with the properties given to the factory method.</td>
 * </tr></table>
 *
 *
 * <h2>Usage example</h2>
 * in order to specify their factories and run the tests in a JUnit framework,
 * implementers can define a subclass in their own test suite as in the example below:
 *
 * <blockquote><pre>public class MyTest extends Test3204 {
 *    public MyTest() {
 *        super(new MyDatumFactory(), new MyDatumAuthorityFactory());
 *    }
 *}</pre></blockquote>
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @author  Alexis Manin (Geomatys)
 * @version 1.0
 * @since   1.0
 */
@DisplayName("User-defined geodetic datum")
public class Test3204 extends Series3000<GeodeticDatum> {
    /**
     * The ellipsoid to use for building the {@linkplain #datum}.
     * This is created either by EPSG code or by user definition.
     *
     * @see #ellipsoidTest
     */
    private Ellipsoid ellipsoid;

    /**
     * The prime meridian to use for building the {@linkplain #datum}.
     * This is created either by EPSG code or by user definition.
     *
     * @see #primeMeridianTest
     */
    private PrimeMeridian primeMeridian;

    /**
     * The datum created by the factory,
     * or {@code null} if not yet created or if datum creation failed.
     */
    private GeodeticDatum datum;

    /**
     * Factory to use for building {@link GeodeticDatum} instances, or {@code null} if none.
     * This is the factory used by the {@link #getIdentifiedObject()} method.
     * May also be used for building {@link Ellipsoid} and {@link PrimeMeridian} components.
     */
    protected final DatumFactory datumFactory;

    /**
     * Factory to use for building {@link Ellipsoid} and {@link PrimeMeridian} components, or {@code null} if none.
     * This is used only for tests with EPSG codes for datum components.
     */
    protected final DatumAuthorityFactory datumAuthorityFactory;

    /**
     * Data about the geodetic datum {@linkplain #ellipsoid}.
     * This is used only for tests with user definitions for datum components.
     *
     * @see #setUserComponents(TestMethod, TestMethod)
     */
    private Test3202 ellipsoidTest;

    /**
     * Data about the geodetic datum {@linkplain #primeMeridian prime meridian}.
     * This is used only for tests with user definitions for datum components.
     *
     * @see #setUserComponents(TestMethod, TestMethod)
     */
    private Test3203 primeMeridianTest;

    /**
     * Creates a new test using the given factory. If a given factory is {@code null},
     * then the tests which depend on it will be skipped.
     *
     * @param datumFactory           factory for creating {@link GeodeticDatum} instances.
     * @param datumAuthorityFactory  factory for creating {@link Ellipsoid} and {@link PrimeMeridian} components from EPSG codes.
     */
    public Test3204(final DatumFactory datumFactory, final DatumAuthorityFactory datumAuthorityFactory) {
        this.datumFactory = datumFactory;
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
        assertNull(op.put(Configuration.Key.datumFactory, datumFactory));
        assertNull(op.put(Configuration.Key.datumAuthorityFactory, datumAuthorityFactory));
        return op;
    }

    /**
     * Creates an ellipsoid from the EPSG factory
     *
     * @param  code  EPSG code of the ellipsoid to create.
     * @throws FactoryException  if an error occurred while creating the ellipsoid.
     */
    private void createEllipsoid(final int code) throws FactoryException {
        ellipsoid = datumAuthorityFactory.createEllipsoid(String.valueOf(code));
    }

    /**
     * Creates a prime meridian from the EPSG factory
     *
     * @param  code  EPSG code of the prime meridian to create.
     * @throws FactoryException  if an error occurred while creating the prime meridian.
     */
    private void createPrimeMeridian(final int code) throws FactoryException {
        primeMeridian = datumAuthorityFactory.createPrimeMeridian(String.valueOf(code));
    }

    /**
     * Creates a user-defined ellipsoid by executing the specified method from the {@link Test3202} class.
     *
     * @param  factory           the test method to use for creating the ellipsoid.
     * @throws FactoryException  if an error occurred while creating the ellipsoid.
     */
    private void createEllipsoid(final TestMethod<Test3202> factory) throws FactoryException {
        ellipsoidTest = new Test3202(datumFactory);
        ellipsoidTest.skipTests = true;
        factory.test(ellipsoidTest);
        ellipsoid = ellipsoidTest.getIdentifiedObject();
    }

    /**
     * Creates a user-defined prime meridian by executing the specified method from the {@link Test3203} class.
     *
     * @param  factory           the test method to use for creating the prime meridian.
     * @throws FactoryException  if an error occurred while creating the prime meridian.
     */
    private void createPrimeMeridian(final TestMethod<Test3203> factory) throws FactoryException {
        primeMeridianTest = new Test3203(datumFactory, null);
        primeMeridianTest.skipTests = true;
        factory.test(primeMeridianTest);
        primeMeridian = primeMeridianTest.getIdentifiedObject();
    }

    /**
     * Returns the geodetic datum instance to be tested. When this method is invoked for the first time,
     * it creates the geodetic datum to test by invoking the corresponding method from {@link DatumFactory}
     * with the current {@link #properties properties} map in argument.
     * The created object is then cached and returned in all subsequent invocations of this method.
     *
     * @return the geodetic datum instance to test.
     * @throws FactoryException if an error occurred while creating the geodetic datum instance.
     */
    @Override
    public GeodeticDatum getIdentifiedObject() throws FactoryException {
        if (datum == null) {
            assumeNotNull(datumFactory);
            datum = datumFactory.createGeodeticDatum(properties, ellipsoid, primeMeridian);
        }
        return datum;
    }

    /**
     * Sets the origin of the geodetic datum to create.
     *
     * @param  origin  the origin of the datum to create.
     */
    private void setOrigin(final String origin) {
        assertNull(properties.put(GeodeticDatum.ANCHOR_POINT_KEY, origin), GeodeticDatum.ANCHOR_POINT_KEY);
    }

    /**
     * Verifies the properties of the geodetic datum given by {@link #getIdentifiedObject()}.
     *
     * @throws FactoryException if an error occurred while creating the datum.
     */
    final void verifyDatum() throws FactoryException {
        if (skipTests) {
            return;
        }
        final String name   = getName();
        final String code   = getCode();
        final String origin = (String) properties.get(GeodeticDatum.ANCHOR_POINT_KEY);
        final GeodeticDatum datum = getIdentifiedObject();
        assertNotNull(datum, "GeodeticDatum");
        validators.validate(datum);

        verifyIdentification(datum, name, code);
        if (ellipsoidTest != null) {
            ellipsoidTest.copyConfigurationFrom(this);
            ellipsoidTest.setIdentifiedObject(datum.getEllipsoid());
            ellipsoidTest.verifyEllipsoid();
        }
        if (primeMeridianTest != null) {
            primeMeridianTest.copyConfigurationFrom(this);
            primeMeridianTest.setIdentifiedObject(datum.getPrimeMeridian());
            primeMeridianTest.verifyPrimeMeridian();
        }
        if (origin != null) {
            final InternationalString actual = datum.getAnchorPoint();
            assertNotNull(actual, "GeodeticDatum.getAnchorPoint()");
            assertEquals(origin, actual.toString(), "GeodeticDatum.getAnchorPoint()");
        }
    }

    /**
     * Tests “GIGS geodetic datum A” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>GIGS datum code: <b>66001</b></li>
     *   <li>GIGS datum name: <b>GIGS geodetic datum A</b></li>
     *   <li>EPSG equivalence: <b>6326 – World Geodetic System 1984 ensemble</b></li>
     *   <li>Datum definition source: <b>User</b></li>
     *   <li>Ellipsoid name: <b>GIGS ellipsoid A</b></li>
     *   <li>Prime meridian name: <b>GIGS PM A</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the properties.
     *
     * @see Test2204#EPSG_6326()
     */
    @Test
    @DisplayName("GIGS geodetic datum A")
    public void GIGS_66001() throws FactoryException {
        setCodeAndName(66001, "GIGS geodetic datum A");
        createEllipsoid(Test3202::GIGS_67030);
        createPrimeMeridian(Test3203::GIGS_68901);
        verifyDatum();
    }

    /**
     * Tests “GIGS geodetic datum AA” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>GIGS datum code: <b>66326</b></li>
     *   <li>GIGS datum name: <b>GIGS geodetic datum AA</b></li>
     *   <li>EPSG equivalence: <b>6326 – World Geodetic System 1984 ensemble</b></li>
     *   <li>Datum definition source: <b>Library</b></li>
     *   <li>Ellipsoid name: <b>WGS 84</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the properties.
     *
     * @see Test2204#EPSG_6326()
     */
    @Test
    @DisplayName("GIGS geodetic datum AA")
    public void GIGS_66326() throws FactoryException {
        setCodeAndName(66326, "GIGS geodetic datum AA");
        createEllipsoid(7030);
        createPrimeMeridian(8901);
        verifyDatum();
    }

    /**
     * Tests “GIGS geodetic datum B” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>GIGS datum code: <b>66002</b></li>
     *   <li>GIGS datum name: <b>GIGS geodetic datum B</b></li>
     *   <li>EPSG equivalence: <b>6277 – Ordnance Survey of Great Britain 1936</b></li>
     *   <li>Datum definition source: <b>User</b></li>
     *   <li>Ellipsoid name: <b>GIGS ellipsoid B</b></li>
     *   <li>Prime meridian name: <b>GIGS PM A</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the properties.
     *
     * @see Test2204#EPSG_6277()
     */
    @Test
    @DisplayName("GIGS geodetic datum B")
    public void GIGS_66002() throws FactoryException {
        setCodeAndName(66002, "GIGS geodetic datum B");
        createEllipsoid(Test3202::GIGS_67001);
        createPrimeMeridian(Test3203::GIGS_68901);
        verifyDatum();
    }

    /**
     * Tests “GIGS geodetic datum BB” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>GIGS datum code: <b>66277</b></li>
     *   <li>GIGS datum name: <b>GIGS geodetic datum BB</b></li>
     *   <li>EPSG equivalence: <b>6277 – Ordnance Survey of Great Britain 1936</b></li>
     *   <li>Datum definition source: <b>Library</b></li>
     *   <li>Ellipsoid name: <b>Airy 1830</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the properties.
     *
     * @see Test2204#EPSG_6277()
     */
    @Test
    @DisplayName("GIGS geodetic datum BB")
    public void GIGS_66277() throws FactoryException {
        setCodeAndName(66277, "GIGS geodetic datum BB");
        createEllipsoid(7001);
        createPrimeMeridian(8901);
        verifyDatum();
    }

    /**
     * Tests “GIGS geodetic datum C” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>GIGS datum code: <b>66003</b></li>
     *   <li>GIGS datum name: <b>GIGS geodetic datum C</b></li>
     *   <li>EPSG equivalence: <b>6289 – Amersfoort</b></li>
     *   <li>Datum definition source: <b>User</b></li>
     *   <li>Ellipsoid name: <b>GIGS ellipsoid C</b></li>
     *   <li>Prime meridian name: <b>GIGS PM A</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the properties.
     *
     * @see Test2204#EPSG_6289()
     */
    @Test
    @DisplayName("GIGS geodetic datum C")
    public void GIGS_66003() throws FactoryException {
        setCodeAndName(66003, "GIGS geodetic datum C");
        createEllipsoid(Test3202::GIGS_67004);
        createPrimeMeridian(Test3203::GIGS_68901);
        verifyDatum();
    }

    /**
     * Tests “GIGS geodetic datum CC” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>GIGS datum code: <b>66289</b></li>
     *   <li>GIGS datum name: <b>GIGS geodetic datum CC</b></li>
     *   <li>EPSG equivalence: <b>6289 – Amersfoort</b></li>
     *   <li>Datum definition source: <b>Library</b></li>
     *   <li>Ellipsoid name: <b>Bessel 1841</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the properties.
     *
     * @see Test2204#EPSG_6289()
     */
    @Test
    @DisplayName("GIGS geodetic datum CC")
    public void GIGS_66289() throws FactoryException {
        setCodeAndName(66289, "GIGS geodetic datum CC");
        createEllipsoid(7004);
        createPrimeMeridian(8901);
        verifyDatum();
    }

    /**
     * Tests “GIGS geodetic datum D” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>GIGS datum code: <b>66004</b></li>
     *   <li>GIGS datum name: <b>GIGS geodetic datum D</b></li>
     *   <li>EPSG equivalence: <b>6813 – Batavia (Jakarta)</b></li>
     *   <li>Datum definition source: <b>User</b></li>
     *   <li>Ellipsoid name: <b>GIGS ellipsoid C</b></li>
     *   <li>Prime meridian name: <b>GIGS PM D</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the properties.
     *
     * @see Test2204#EPSG_6813()
     */
    @Test
    @DisplayName("GIGS geodetic datum D")
    public void GIGS_66004() throws FactoryException {
        setCodeAndName(66004, "GIGS geodetic datum D");
        createEllipsoid(Test3202::GIGS_67004);
        createPrimeMeridian(Test3203::GIGS_68908);
        verifyDatum();
    }

    /**
     * Tests “GIGS geodetic datum DD” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>GIGS datum code: <b>66813</b></li>
     *   <li>GIGS datum name: <b>GIGS geodetic datum DD</b></li>
     *   <li>EPSG equivalence: <b>6813 – Batavia (Jakarta)</b></li>
     *   <li>Datum definition source: <b>Library</b></li>
     *   <li>Ellipsoid name: <b>Bessel 1841</b></li>
     *   <li>Prime meridian name: <b>Jakarta</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the properties.
     *
     * @see Test2204#EPSG_6813()
     */
    @Test
    @DisplayName("GIGS geodetic datum DD")
    public void GIGS_66813() throws FactoryException {
        setCodeAndName(66813, "GIGS geodetic datum DD");
        createEllipsoid(7004);
        createPrimeMeridian(8908);
        verifyDatum();
    }

    /**
     * Tests “GIGS geodetic datum E” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>GIGS datum code: <b>66005</b></li>
     *   <li>GIGS datum name: <b>GIGS geodetic datum E</b></li>
     *   <li>EPSG equivalence: <b>6313 – Reseau National Belge 1972</b></li>
     *   <li>Datum definition source: <b>User</b></li>
     *   <li>Ellipsoid name: <b>GIGS ellipsoid E</b></li>
     *   <li>Prime meridian name: <b>GIGS PM A</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the properties.
     *
     * @see Test2204#EPSG_6313()
     */
    @Test
    @DisplayName("GIGS geodetic datum E")
    public void GIGS_66005() throws FactoryException {
        setCodeAndName(66005, "GIGS geodetic datum E");
        createEllipsoid(Test3202::GIGS_67022);
        createPrimeMeridian(Test3203::GIGS_68901);
        verifyDatum();
    }

    /**
     * Tests “GIGS geodetic datum EE” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>GIGS datum code: <b>66313</b></li>
     *   <li>GIGS datum name: <b>GIGS geodetic datum EE</b></li>
     *   <li>EPSG equivalence: <b>6313 – Reseau National Belge 1972</b></li>
     *   <li>Datum definition source: <b>Library</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the properties.
     *
     * @see Test2204#EPSG_6313()
     */
    @Test
    @DisplayName("GIGS geodetic datum EE")
    public void GIGS_66313() throws FactoryException {
        setCodeAndName(66313, "GIGS geodetic datum EE");
        createEllipsoid(7022);
        createPrimeMeridian(8901);
        verifyDatum();
    }

    /**
     * Tests “GIGS geodetic datum F” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>GIGS datum code: <b>66006</b></li>
     *   <li>GIGS datum name: <b>GIGS geodetic datum F</b></li>
     *   <li>EPSG equivalence: <b>6283 – Geocentric Datum of Australia 1994</b></li>
     *   <li>Datum definition source: <b>User</b></li>
     *   <li>Ellipsoid name: <b>GIGS ellipsoid F</b></li>
     *   <li>Prime meridian name: <b>GIGS PM A</b></li>
     *   <li>Datum origin: <b>Origin F</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the properties.
     *
     * @see Test2204#EPSG_6283()
     */
    @Test
    @DisplayName("GIGS geodetic datum F")
    public void GIGS_66006() throws FactoryException {
        setCodeAndName(66006, "GIGS geodetic datum F");
        setOrigin("Origin F");
        createEllipsoid(Test3202::GIGS_67019);
        createPrimeMeridian(Test3203::GIGS_68901);
        verifyDatum();
    }

    /**
     * Tests “GIGS geodetic datum FF” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>GIGS datum code: <b>66283</b></li>
     *   <li>GIGS datum name: <b>GIGS geodetic datum FF</b></li>
     *   <li>EPSG equivalence: <b>6283 – Geocentric Datum of Australia 1994</b></li>
     *   <li>Datum definition source: <b>Library</b></li>
     *   <li>Ellipsoid name: <b>GRS 1980</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the properties.
     *
     * @see Test2204#EPSG_6283()
     */
    @Test
    @DisplayName("GIGS geodetic datum FF")
    public void GIGS_66283() throws FactoryException {
        setCodeAndName(66283, "GIGS geodetic datum FF");
        createEllipsoid(7019);
        createPrimeMeridian(8901);
        verifyDatum();
    }

    /**
     * Tests “GIGS geodetic datum G” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>GIGS datum code: <b>66007</b></li>
     *   <li>GIGS datum name: <b>GIGS geodetic datum G</b></li>
     *   <li>EPSG equivalence (1 of 5): <b>6258 – European Terrestrial Reference System 1989 ensemble</b></li>
     *   <li>EPSG equivalence (2 of 5): <b>6742 – Geodetic Datum of Malaysia 2000</b></li>
     *   <li>EPSG equivalence (3 of 5): <b>6152 – NAD83 (High Accuracy Reference Network)</b></li>
     *   <li>EPSG equivalence (4 of 5): <b>6190 – Posiciones Geodesicas Argentinas 1998</b></li>
     *   <li>EPSG equivalence (5 of 5): <b>6674 – Sistema de Referencia Geocentrico para las AmericaS 2000</b></li>
     *   <li>Datum definition source: <b>User</b></li>
     *   <li>Ellipsoid name: <b>GIGS ellipsoid F</b></li>
     *   <li>Prime meridian name: <b>GIGS PM A</b></li>
     *   <li>Datum origin: <b>Origin(s) G</b></li>
     * </ul>
     *
     * Remarks: This GIGS Datum is functionally equivalent to any static datum defined as part of a geodetic CRS that uses the GRS 1980 ellipsoid.
     *
     * @throws FactoryException if an error occurred while creating the datum from the properties.
     */
    @Test
    @DisplayName("GIGS geodetic datum G")
    public void GIGS_66007() throws FactoryException {
        setCodeAndName(66007, "GIGS geodetic datum G");
        setOrigin("Origin(s) G");
        createEllipsoid(Test3202::GIGS_67019);
        createPrimeMeridian(Test3203::GIGS_68901);
        verifyDatum();
    }

    /**
     * Tests “GIGS geodetic datum H” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>GIGS datum code: <b>66008</b></li>
     *   <li>GIGS datum name: <b>GIGS geodetic datum H</b></li>
     *   <li>EPSG equivalence: <b>6807 – Nouvelle Triangulation Francaise (Paris)</b></li>
     *   <li>Datum definition source: <b>User</b></li>
     *   <li>Ellipsoid name: <b>GIGS ellipsoid H</b></li>
     *   <li>Prime meridian name: <b>GIGS PM H</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the properties.
     *
     * @see Test2204#EPSG_6807()
     */
    @Test
    @DisplayName("GIGS geodetic datum H")
    public void GIGS_66008() throws FactoryException {
        setCodeAndName(66008, "GIGS geodetic datum H");
        createEllipsoid(Test3202::GIGS_67011);
        createPrimeMeridian(Test3203::GIGS_68903);
        verifyDatum();
    }

    /**
     * Tests “GIGS geodetic datum HH” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>GIGS datum code: <b>66807</b></li>
     *   <li>GIGS datum name: <b>GIGS geodetic datum HH</b></li>
     *   <li>EPSG equivalence: <b>6807 – Nouvelle Triangulation Francaise (Paris)</b></li>
     *   <li>Datum definition source: <b>Library</b></li>
     *   <li>Ellipsoid name: <b>Clarke 1880 (IGN)</b></li>
     *   <li>Prime meridian name: <b>Paris</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the properties.
     *
     * @see Test2204#EPSG_6807()
     */
    @Test
    @DisplayName("GIGS geodetic datum HH")
    public void GIGS_66807() throws FactoryException {
        setCodeAndName(66807, "GIGS geodetic datum HH");
        createEllipsoid(7011);
        createPrimeMeridian(8903);
        verifyDatum();
    }

    /**
     * Tests “GIGS geodetic datum J” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>GIGS datum code: <b>66009</b></li>
     *   <li>GIGS datum name: <b>GIGS geodetic datum J</b></li>
     *   <li>EPSG equivalence: <b>6267 – North American Datum 1927</b></li>
     *   <li>Datum definition source: <b>User</b></li>
     *   <li>Ellipsoid name: <b>GIGS ellipsoid J</b></li>
     *   <li>Prime meridian name: <b>GIGS PM A</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the properties.
     *
     * @see Test2204#EPSG_6267()
     */
    @Test
    @DisplayName("GIGS geodetic datum J")
    public void GIGS_66009() throws FactoryException {
        setCodeAndName(66009, "GIGS geodetic datum J");
        createEllipsoid(Test3202::GIGS_67008);
        createPrimeMeridian(Test3203::GIGS_68901);
        verifyDatum();
    }

    /**
     * Tests “GIGS geodetic datum K” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>GIGS datum code: <b>66012</b></li>
     *   <li>GIGS datum name: <b>GIGS geodetic datum K</b></li>
     *   <li>EPSG equivalence: <b>6237 – Hungarian Datum 1972</b></li>
     *   <li>Datum definition source: <b>User</b></li>
     *   <li>Ellipsoid name: <b>GIGS ellipsoid K</b></li>
     *   <li>Prime meridian name: <b>GIGS PM A</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the properties.
     *
     * @see Test2204#EPSG_6237()
     */
    @Test
    @DisplayName("GIGS geodetic datum K")
    public void GIGS_66012() throws FactoryException {
        setCodeAndName(66012, "GIGS geodetic datum K");
        createEllipsoid(Test3202::GIGS_67036);
        createPrimeMeridian(Test3203::GIGS_68901);
        verifyDatum();
    }

    /**
     * Tests “GIGS geodetic datum L” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>GIGS datum code: <b>66011</b></li>
     *   <li>GIGS datum name: <b>GIGS geodetic datum L</b></li>
     *   <li>EPSG equivalence: <b>6211 – Batavia</b></li>
     *   <li>Datum definition source: <b>User</b></li>
     *   <li>Ellipsoid name: <b>GIGS ellipsoid C</b></li>
     *   <li>Prime meridian name: <b>GIGS PM A</b></li>
     *   <li>Datum origin: <b>Origin L</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the properties.
     *
     * @see Test2204#EPSG_6211()
     */
    @Test
    @DisplayName("GIGS geodetic datum L")
    public void GIGS_66011() throws FactoryException {
        setCodeAndName(66011, "GIGS geodetic datum L");
        setOrigin("Origin L");
        createEllipsoid(Test3202::GIGS_67004);
        createPrimeMeridian(Test3203::GIGS_68901);
        verifyDatum();
    }

    /**
     * Tests “GIGS geodetic datum M” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>GIGS datum code: <b>66016</b></li>
     *   <li>GIGS datum name: <b>GIGS geodetic datum M</b></li>
     *   <li>EPSG equivalence: <b>6230 – European Datum 1950</b></li>
     *   <li>Datum definition source: <b>User</b></li>
     *   <li>Ellipsoid name: <b>GIGS ellipsoid E</b></li>
     *   <li>Prime meridian name: <b>GIGS PM A</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the properties.
     *
     * @see Test2204#EPSG_6230()
     */
    @Test
    @DisplayName("GIGS geodetic datum M")
    public void GIGS_66016() throws FactoryException {
        setCodeAndName(66016, "GIGS geodetic datum M");
        createEllipsoid(Test3202::GIGS_67022);
        createPrimeMeridian(Test3203::GIGS_68901);
        verifyDatum();
    }

    /**
     * Tests “GIGS geodetic datum T” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>GIGS datum code: <b>66010</b></li>
     *   <li>GIGS datum name: <b>GIGS geodetic datum T</b></li>
     *   <li>EPSG equivalence: <b>6275 – Nouvelle Triangulation Francaise</b></li>
     *   <li>Datum definition source: <b>User</b></li>
     *   <li>Ellipsoid name: <b>GIGS ellipsoid H</b></li>
     *   <li>Prime meridian name: <b>GIGS PM A</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the properties.
     *
     * @see Test2204#EPSG_6275()
     */
    @Test
    @DisplayName("GIGS geodetic datum T")
    public void GIGS_66010() throws FactoryException {
        setCodeAndName(66010, "GIGS geodetic datum T");
        createEllipsoid(Test3202::GIGS_67011);
        createPrimeMeridian(Test3203::GIGS_68901);
        verifyDatum();
    }

    /**
     * Tests “GIGS geodetic datum X” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>GIGS datum code: <b>66013</b></li>
     *   <li>GIGS datum name: <b>GIGS geodetic datum X</b></li>
     *   <li>EPSG equivalence: <b>6202 – Australian Geodetic Datum 1966</b></li>
     *   <li>Datum definition source: <b>User</b></li>
     *   <li>Ellipsoid name: <b>GIGS ellipsoid X</b></li>
     *   <li>Prime meridian name: <b>GIGS PM A</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the properties.
     *
     * @see Test2204#EPSG_6202()
     */
    @Test
    @DisplayName("GIGS geodetic datum X")
    public void GIGS_66013() throws FactoryException {
        setCodeAndName(66013, "GIGS geodetic datum X");
        createEllipsoid(Test3202::GIGS_67003);
        createPrimeMeridian(Test3203::GIGS_68901);
        verifyDatum();
    }

    /**
     * Tests “GIGS geodetic datum Y” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>GIGS datum code: <b>66014</b></li>
     *   <li>GIGS datum name: <b>GIGS geodetic datum Y</b></li>
     *   <li>EPSG equivalence: <b>6284 – Pulkovo 1942</b></li>
     *   <li>Datum definition source: <b>User</b></li>
     *   <li>Ellipsoid name: <b>GIGS ellipsoid Y</b></li>
     *   <li>Prime meridian name: <b>GIGS PM A</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the properties.
     *
     * @see Test2204#EPSG_6284()
     */
    @Test
    @DisplayName("GIGS geodetic datum Y")
    public void GIGS_66014() throws FactoryException {
        setCodeAndName(66014, "GIGS geodetic datum Y");
        createEllipsoid(Test3202::GIGS_67024);
        createPrimeMeridian(Test3203::GIGS_68901);
        verifyDatum();
    }

    /**
     * Tests “GIGS geodetic datum Z” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>GIGS datum code: <b>66015</b></li>
     *   <li>GIGS datum name: <b>GIGS geodetic datum Z</b></li>
     *   <li>EPSG equivalence: <b>6269 – North American Datum 1983</b></li>
     *   <li>Datum definition source: <b>User</b></li>
     *   <li>Ellipsoid name: <b>GIGS ellipsoid F</b></li>
     *   <li>Prime meridian name: <b>GIGS PM A</b></li>
     *   <li>Datum origin: <b>Origin Z</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the properties.
     *
     * @see Test2204#EPSG_6269()
     */
    @Test
    @DisplayName("GIGS geodetic datum Z")
    public void GIGS_66015() throws FactoryException {
        setCodeAndName(66015, "GIGS geodetic datum Z");
        setOrigin("Origin Z");
        createEllipsoid(Test3202::GIGS_67019);
        createPrimeMeridian(Test3203::GIGS_68901);
        verifyDatum();
    }

    /**
     * Tests “GIGS geodetic datum ZZ” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>GIGS datum code: <b>66269</b></li>
     *   <li>GIGS datum name: <b>GIGS geodetic datum ZZ</b></li>
     *   <li>EPSG equivalence: <b>6269 – North American Datum 1983</b></li>
     *   <li>Datum definition source: <b>Library</b></li>
     *   <li>Ellipsoid name: <b>GRS 1980</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the properties.
     *
     * @see Test2204#EPSG_6269()
     */
    @Test
    @DisplayName("GIGS geodetic datum ZZ")
    public void GIGS_66269() throws FactoryException {
        setCodeAndName(66269, "GIGS geodetic datum ZZ");
        createEllipsoid(7019);
        createPrimeMeridian(8901);
        verifyDatum();
    }
}
