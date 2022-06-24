package org.iogp.gigs;

import org.iogp.gigs.internal.geoapi.Configuration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.crs.*;
import org.opengis.referencing.cs.AxisDirection;
import org.opengis.referencing.cs.CSFactory;
import org.opengis.referencing.cs.CartesianCS;
import org.opengis.referencing.cs.EllipsoidalCS;
import org.opengis.referencing.datum.*;
import org.opengis.referencing.operation.Conversion;
import org.opengis.referencing.operation.CoordinateOperationFactory;
import org.opengis.referencing.operation.OperationMethod;
import org.opengis.referencing.operation.Transformation;
import org.opengis.util.FactoryException;

import javax.measure.Unit;
import javax.measure.quantity.Angle;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
/**
 * Verifies that the software allows correct definition of a user-defined geographic 2D CRS.
 *
 * <table class="gigs">
 * <caption>Test description</caption>
 * <tr>
 *   <th>Test method:</th>
 *   <td>Create user-defined projection for each of several different geographic 2D CRSs.</td>
 * </tr><tr>
 *   <th>Test data:</th>
 *   <td><a href="doc-files/GIGS_3005_userProjection.csv">{@code GIGS_user_3205_GeodeticCRS.txt}</a>.</td>
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
 * <blockquote><pre>public class MyTest extends Test3205Geog2DCRS {
 *    public MyTest() {
 *        super(new MyCoordinateOperationFactory());
 *    }
 *}</pre></blockquote>
 *
 * @author  Michael Arneson (INT)
 * @version 1.0
 * @since   1.0
 */
@DisplayName("User-defined geographic 2D CRS")
public class Test3205Geog2DCRS extends Series3000<GeographicCRS> {

    /**
     * The CRS created by the factory,
     * or {@code null} if not yet created or if CRS creation failed.
     */
    private GeographicCRS crs;

    /**
     * The Coordinate System EPSG Code of the CRS.
     */
    private int csCode;

    /**
     * The dataum to use for building the CRS.
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
     */
    protected final DatumAuthorityFactory datumAuthorityFactory;

    /**
     * Data about the geodetic datum.
     * This is used only for tests with user definitions for CRS components.
     *
     * @see #setUserComponents(TestMethod, TestMethod)
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
     * @param datumFactory           factory for creating {@link GeodeticDatum} instances.
     * @param datumAuthorityFactory  factory for creating {@link Ellipsoid} and {@link PrimeMeridian} components from EPSG codes.
     * @param csFactory              factory for creating {@code CoordinateSystem} instances.
     * @param crsFactory             factory for creating {@link GeodeticCRS} instances.
     */
    public Test3205Geog2DCRS(final DatumFactory datumFactory, final DatumAuthorityFactory datumAuthorityFactory,
                             final CSFactory csFactory, final CRSFactory crsFactory) {
        this.datumFactory = datumFactory;
        this.datumAuthorityFactory = datumAuthorityFactory;
        this.crsFactory   = crsFactory;
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
     *       <li>{@link #datumFactory}</li>
     *       <li>{@code csFactory}</li>
     *       <li>{@link #crsFactory}</li>
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
        assertNull(op.put(Configuration.Key.csFactory, epsgFactory.getCSFactory()));
        assertNull(op.put(Configuration.Key.crsFactory, crsFactory));
        return op;
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
     * Returns the geographic 2D CRS instance to be tested. When this method is invoked for the first time,
     * it creates the geographic 2D CRS to test by invoking the corresponding method from {@link EPSGMock}
     * with the current {@link #properties properties} map in argument.
     * The created object is then cached and returned in all subsequent invocations of this method.
     *
     * @return the geographic 2D CRS instance to test.
     * @throws FactoryException if an error occurred while creating the geodetic datum instance.
     */
    @Override
    public GeographicCRS getIdentifiedObject() throws FactoryException {
        if (crs == null) {
            crs = crsFactory.createGeographicCRS(properties, datum, epsgFactory.createEllipsoidalCS(String.valueOf(csCode)));
        }
        return crs;
    }

    /**
     * Sets the Geographic 2D CRS instance to verify. This method is invoked only by other test classes which need to
     * verify the Geographic 2D CRS contained in a CRS instead of the Geographic 2D CRS immediately after creation.
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
    final void verifyGeographicCRS() throws FactoryException
    {
        if (skipTests) {
            return;
        }
        if (crsFactory != null) {
            final GeographicCRS crs = getIdentifiedObject();
            assertNotNull(crs, "CRSFactory.createGeographicCRS(…) shall not return null.");
            validators.validate(crs);
            verifyIdentification(crs, getName(), String.valueOf(getCode()));
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
     * Tests “GIGS geogCRS A”  geographic 2D CRS from the factory.
     *
     * <ul>
     *   <li>GIGS geographic 2D CRS code: <b>64003</b></li>
     *   <li>GIGS geographic 2D CRS name: <b>GIGS geogCRS A</b></li>
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
        csCode=6422;
        verifyGeographicCRS();
    }

    /**
     * Tests “GIGS geogCRS AA”  geographic 2D CRS from the factory.
     *
     * <ul>
     *   <li>GIGS geographic 2D CRS code: <b>64326</b></li>
     *   <li>GIGS geographic 2D CRS name: <b>GIGS geogCRS AA</b></li>
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
        createDatum(Test3204::GIGS_66326);
        csCode=6422;
        verifyGeographicCRS();
    }

    /**
     * Tests “GIGS geogCRS Agr”  geographic 2D CRS from the factory.
     *
     * <ul>
     *   <li>GIGS geographic 2D CRS code: <b>64033</b></li>
     *   <li>GIGS geographic 2D CRS name: <b>GIGS geogCRS Agr</b></li>
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
        csCode=6403;
        verifyGeographicCRS();
    }

    /**
     * Tests “GIGS geogCRS Alonlat”  geographic 2D CRS from the factory.
     *
     * <ul>
     *   <li>GIGS geographic 2D CRS code: <b>64004</b></li>
     *   <li>GIGS geographic 2D CRS name: <b>GIGS geogCRS Alonlat</b></li>
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
        csCode=6424;
        verifyGeographicCRS();
    }

    /**
     * Tests “GIGS geogCRS B”  geographic 2D CRS from the factory.
     *
     * <ul>
     *   <li>GIGS geographic 2D CRS code: <b>64005</b></li>
     *   <li>GIGS geographic 2D CRS name: <b>GIGS geogCRS B</b></li>
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
        csCode=6422;
        verifyGeographicCRS();
    }

    /**
     * Tests “GIGS geogCRS C”  geographic 2D CRS from the factory.
     *
     * <ul>
     *   <li>GIGS geographic 2D CRS code: <b>64006</b></li>
     *   <li>GIGS geographic 2D CRS name: <b>GIGS geogCRS C</b></li>
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
     * Tests “GIGS geogCRS CC”  geographic 2D CRS from the factory.
     *
     * <ul>
     *   <li>GIGS geographic 2D CRS code: <b>64289</b></li>
     *   <li>GIGS geographic 2D CRS name: <b>GIGS geogCRS CC</b></li>
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
        createDatum(Test3204::GIGS_66289);
        csCode=6422;
        verifyGeographicCRS();
    }

    /**
     * Tests “GIGS geogCRS D”  geographic 2D CRS from the factory.
     *
     * <ul>
     *   <li>GIGS geographic 2D CRS code: <b>64007</b></li>
     *   <li>GIGS geographic 2D CRS name: <b>GIGS geogCRS D</b></li>
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
        csCode=6422;
        verifyGeographicCRS();
    }

    /**
     * Tests “GIGS geogCRS DD”  geographic 2D CRS from the factory.
     *
     * <ul>
     *   <li>GIGS geographic 2D CRS code: <b>64813</b></li>
     *   <li>GIGS geographic 2D CRS name: <b>GIGS geogCRS DD</b></li>
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
        createDatum(Test3204::GIGS_66813);
        csCode=6422;
        verifyGeographicCRS();
    }

    /**
     * Tests “GIGS geogCRS E”  geographic 2D CRS from the factory.
     *
     * <ul>
     *   <li>GIGS geographic 2D CRS code: <b>64008</b></li>
     *   <li>GIGS geographic 2D CRS name: <b>GIGS geogCRS E</b></li>
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
        csCode=6422;
        verifyGeographicCRS();
    }

    /**
     * Tests “GIGS geogCRS F”  geographic 2D CRS from the factory.
     *
     * <ul>
     *   <li>GIGS geographic 2D CRS code: <b>64009</b></li>
     *   <li>GIGS geographic 2D CRS name: <b>GIGS geogCRS F</b></li>
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
        csCode=6422;
        verifyGeographicCRS();
    }

    /**
     * Tests “GIGS geogCRS FF”  geographic 2D CRS from the factory.
     *
     * <ul>
     *   <li>GIGS geographic 2D CRS code: <b>64283</b></li>
     *   <li>GIGS geographic 2D CRS name: <b>GIGS geogCRS FF</b></li>
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
        createDatum(Test3204::GIGS_66283);
        csCode=6422;
        verifyGeographicCRS();
    }

    /**
     * Tests “GIGS geogCRS G”  geographic 2D CRS from the factory.
     *
     * <ul>
     *   <li>GIGS geographic 2D CRS code: <b>64010</b></li>
     *   <li>GIGS geographic 2D CRS name: <b>GIGS geogCRS G</b></li>
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
        csCode=6422;
        verifyGeographicCRS();
    }

    /**
     * Tests “GIGS geogCRS H”  geographic 2D CRS from the factory.
     *
     * <ul>
     *   <li>GIGS geographic 2D CRS code: <b>64011</b></li>
     *   <li>GIGS geographic 2D CRS name: <b>GIGS geogCRS H</b></li>
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
        csCode=6403;
        verifyGeographicCRS();
    }

    /**
     * Tests “GIGS geogCRS HH”  geographic 2D CRS from the factory.
     *
     * <ul>
     *   <li>GIGS geographic 2D CRS code: <b>64807</b></li>
     *   <li>GIGS geographic 2D CRS name: <b>GIGS geogCRS HH</b></li>
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
        createDatum(Test3204::GIGS_66807);
        csCode=6422;
        verifyGeographicCRS();
    }

    /**
     * Tests “GIGS geogCRS J”  geographic 2D CRS from the factory.
     *
     * <ul>
     *   <li>GIGS geographic 2D CRS code: <b>64012</b></li>
     *   <li>GIGS geographic 2D CRS name: <b>GIGS geogCRS J</b></li>
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
        csCode=6422;
        verifyGeographicCRS();
    }

    /**
     * Tests “GIGS geogCRS K”  geographic 2D CRS from the factory.
     *
     * <ul>
     *   <li>GIGS geographic 2D CRS code: <b>64015</b></li>
     *   <li>GIGS geographic 2D CRS name: <b>GIGS geogCRS K</b></li>
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
        csCode=6422;
        verifyGeographicCRS();
    }

    /**
     * Tests “GIGS geogCRS M”  geographic 2D CRS from the factory.
     *
     * <ul>
     *   <li>GIGS geographic 2D CRS code: <b>64020</b></li>
     *   <li>GIGS geographic 2D CRS name: <b>GIGS geogCRS M</b></li>
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
        csCode=6422;
        verifyGeographicCRS();
    }

    /**
     * Tests “GIGS geogCRS T”  geographic 2D CRS from the factory.
     *
     * <ul>
     *   <li>GIGS geographic 2D CRS code: <b>64013</b></li>
     *   <li>GIGS geographic 2D CRS name: <b>GIGS geogCRS T</b></li>
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
        csCode=6403;
        verifyGeographicCRS();
    }

    /**
     * Tests “GIGS geogCRS X”  geographic 2D CRS from the factory.
     *
     * <ul>
     *   <li>GIGS geographic 2D CRS code: <b>64016</b></li>
     *   <li>GIGS geographic 2D CRS name: <b>GIGS geogCRS X</b></li>
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
        csCode=6422;
        verifyGeographicCRS();
    }

    /**
     * Tests “GIGS geogCRS Y”  geographic 2D CRS from the factory.
     *
     * <ul>
     *   <li>GIGS geographic 2D CRS code: <b>64017</b></li>
     *   <li>GIGS geographic 2D CRS name: <b>GIGS geogCRS Y</b></li>
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
        csCode=6422;
        verifyGeographicCRS();
    }

    /**
     * Tests “GIGS geogCRS Z”  geographic 2D CRS from the factory.
     *
     * <ul>
     *   <li>GIGS geographic 2D CRS code: <b>64018</b></li>
     *   <li>GIGS geographic 2D CRS name: <b>GIGS geogCRS Z</b></li>
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
        csCode=6422;
        verifyGeographicCRS();
    }

    /**
     * Tests “GIGS geogCRS ZZ”  geographic 2D CRS from the factory.
     *
     * <ul>
     *   <li>GIGS geographic 2D CRS code: <b>64269</b></li>
     *   <li>GIGS geographic 2D CRS name: <b>GIGS geogCRS ZZ</b></li>
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
        createDatum(Test3204::GIGS_66269);
        csCode=6422;
        verifyGeographicCRS();
    }

}
