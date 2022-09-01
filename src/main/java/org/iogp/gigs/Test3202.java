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

import javax.measure.Unit;
import javax.measure.quantity.Length;
import org.opengis.util.FactoryException;
import org.opengis.referencing.datum.Ellipsoid;
import org.opengis.referencing.datum.DatumFactory;
import org.iogp.gigs.internal.geoapi.Configuration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Verifies that the software allows correct definition of a user-defined ellipsoid.
 *
 * <table class="gigs">
 * <caption>Test description</caption>
 * <tr>
 *   <th>Test method:</th>
 *   <td>Create user-defined ellipsoid for each of several different ellipsoids.</td>
 * </tr><tr>
 *   <th>Test data:</th>
 *   <td><a href="https://github.com/IOGP-GIGS/GIGSTestDataset/blob/main/GIGSTestDatasetFiles/GIGS%203200%20User-defined%20Geodetic%20Data%20Objects%20test%20data/ASCII/GIGS_user_3202_Ellipsoid.txt">{@code GIGS_user_3202_Ellipsoid.txt}</a>
 * </tr><tr>
 *   <th>Tested API:</th>
 *   <td>{@link DatumFactory#createEllipsoid(Map, double, double, Unit)} and<br>
 *       {@link DatumFactory#createFlattenedSphere(Map, double, double, Unit)}.</td>
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
 * {@snippet lang="java" :
 * public class MyTest extends Test3202 {
 *     public MyTest() {
 *         super(new MyDatumFactory());
 *     }
 * }
 * }
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @author  Alexis Manin (Geomatys)
 * @version 1.0
 * @since   1.0
 */
@DisplayName("User-defined ellipsoid")
public class Test3202 extends Series3000<Ellipsoid> {
    /**
     * Tolerance threshold for inverse flattening factor.
     * We set the tolerance to 0.5 unit of last digit in the values given in the GIGS test file.
     */
    private static final double IVF_TOLERANCE = 0.5E-9;

    /**
     * The ellipsoid semi-major axis length, in metres.
     * This field is set by all test methods before to create and verify the {@link Ellipsoid} instance.
     */
    public double semiMajorInMetres;

    /**
     * The ellipsoid semi-major axis length, in unit of {@link #axisUnit}.
     * This field is set by all test methods before to create and verify the {@link Ellipsoid} instance.
     */
    public double semiMajorAxis;

    /**
     * The ellipsoid semi-minor axis length, in unit of {@link #axisUnit}.
     * This field is set by all test methods before to create and verify the {@link Ellipsoid} instance.
     */
    public double semiMinorAxis;

    /**
     * The {@link #semiMajorAxis} and {@link #semiMinorAxis} unit of measurement.
     * This field is set by all test methods before to create and verify the {@link Ellipsoid} instance.
     */
    public Unit<Length> axisUnit;

    /**
     * Tolerance threshold in the comparison of axis lengths.
     */
    private double axisTolerance;

    /**
     * The inverse flattening factor (dimensionless),
     * or {@link Double#POSITIVE_INFINITY} if the ellipsoid is a sphere.
     * This field is set by all test methods before to create and verify the {@link Ellipsoid} instance.
     */
    public double inverseFlattening;

    /**
     * {@code false} if the second defining parameter is the {@link #semiMinorAxis} length, or
     * {@code true} if it is the {@link #inverseFlattening}.
     * This field is set by all test methods before to create and verify the {@link Ellipsoid} instance.
     */
    public boolean isIvfDefinitive;

    /**
     * {@code true} if the ellipsoid is a sphere. In such case, {@link #semiMinorAxis} = {@link #semiMajorAxis}
     * and {@link #inverseFlattening} is infinite.
     * This field is set by all test methods before to create and verify the {@link Ellipsoid} instance.
     */
    public boolean isSphere;

    /**
     * The ellipsoid created by the factory,
     * or {@code null} if not yet created or if the ellipsoid creation failed.
     *
     * @see #datumFactory
     */
    private Ellipsoid ellipsoid;

    /**
     * Factory to use for building {@link Ellipsoid} instances, or {@code null} if none.
     * This is the factory used by the {@link #getIdentifiedObject()} method.
     */
    protected final DatumFactory datumFactory;

    /**
     * Creates a new test using the given factory. If a given factory is {@code null},
     * then the tests which depend on it will be skipped.
     *
     * @param datumFactory  factory for creating {@link Ellipsoid} instances.
     */
    public Test3202(final DatumFactory datumFactory) {
        this.datumFactory = datumFactory;
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
     *     </ul>
     *   </li>
     * </ul>
     *
     * @return the configuration of the test being run.
     */
    @Override
    Configuration configuration() {
        final Configuration op = super.configuration();
        assertNull(op.put(Configuration.Key.datumFactory, datumFactory));
        return op;
    }

    /**
     * Sets the ellipsoid instance to verify. This method is invoked only by other test classes which need to
     * verify the ellipsoid contained in a geodetic datum instead of the ellipsoid immediately after creation.
     *
     * @param  instance  the instance to verify.
     */
    final void setIdentifiedObject(final Ellipsoid instance) {
        ellipsoid = instance;
    }

    /**
     * Returns the ellipsoid instance to be tested. When this method is invoked for the first time,
     * it creates the ellipsoid to test by invoking the corresponding method from {@link DatumFactory}
     * with the current {@link #properties properties} map in argument.
     * The created object is then cached and returned in all subsequent invocations of this method.
     *
     * @return the ellipsoid instance to test.
     * @throws FactoryException if an error occurred while creating the ellipsoid instance.
     */
    @Override
    public Ellipsoid getIdentifiedObject() throws FactoryException {
        if (ellipsoid == null) {
            assumeNotNull(datumFactory);
            if (isIvfDefinitive) {
                ellipsoid = datumFactory.createFlattenedSphere(properties, semiMajorAxis, inverseFlattening, axisUnit);
            } else {
                ellipsoid = datumFactory.createEllipsoid(properties, semiMajorAxis, semiMinorAxis, axisUnit);
            }
        }
        return ellipsoid;
    }

    /**
     * Verifies the properties of the ellipsoid given by {@link #getIdentifiedObject()}.
     *
     * @throws FactoryException if an error occurred while creating the ellipsoid.
     */
    final void verifyEllipsoid() throws FactoryException {
        if (skipTests) {
            return;
        }
        final String name = getName();
        final String code = getCode();
        @SuppressWarnings("LocalVariableHidesMemberVariable")
        final Ellipsoid ellipsoid = getIdentifiedObject();
        assertNotNull(ellipsoid, "Ellipsoid");
        validators.validate(ellipsoid);
        if (isFactoryPreservingUserValues) {
            configurationTip = Configuration.Key.isFactoryPreservingUserValues;
            final double ivfTolerance = isIvfDefinitive ? IVF_TOLERANCE : 0.0005;
            assertEquals(axisUnit,          ellipsoid.getAxisUnit(),                         "Ellipsoid.getAxisUnit()");
            assertEquals(semiMajorAxis,     ellipsoid.getSemiMajorAxis(),     axisTolerance, "Ellipsoid.getSemiMajorAxis()");
            assertEquals(semiMinorAxis,     ellipsoid.getSemiMinorAxis(),     axisTolerance, "Ellipsoid.getSemiMinorAxis()");
            assertEquals(inverseFlattening, ellipsoid.getInverseFlattening(), ivfTolerance,  "Ellipsoid.getInverseFlattening()");
            assertEquals(isIvfDefinitive,   ellipsoid.isIvfDefinitive(),                     "Ellipsoid.isIvfDefinitive()");
            assertEquals(isSphere,          ellipsoid.isSphere(),                            "Ellipsoid.isSphere()");
            configurationTip = null;
        }
        verifyIdentification(ellipsoid, name, code);
        /*
         * Verify axis length value by applying conversion to a fixed unit of measurement.
         * The value does not need to be in the unit of measurement specified by the test.
         */
        if (isIvfDefinitive) {
            verifyFlattenedSphere(ellipsoid, name, semiMajorAxis, inverseFlattening, axisUnit);
        }
        assertEquals(semiMajorInMetres,
                ellipsoid.getAxisUnit().getConverterTo(units.metre()).convert(ellipsoid.getSemiMajorAxis()), 0.1);
    }

    /**
     * Tests “GIGS ellipsoid A” flattened sphere creation from the factory.
     *
     * <ul>
     *   <li>GIGS ellipsoid code: <b>67030</b></li>
     *   <li>GIGS ellipsoid name: <b>GIGS ellipsoid A</b></li>
     *   <li>EPSG equivalence: <b>7030 – WGS 84</b></li>
     *   <li>Semi-major axis (<var>a</var>): <b>6378137.0 metre</b></li>
     *   <li>Semi-minor axis (<var>b</var>): <b>6356752.3 metre</b></li>
     *   <li>Inverse flattening (1/<var>f</var>): <b>298.257223563</b></li>
     * </ul>
     *
     * Remarks: Defined using a and 1/f.
     *
     * @throws FactoryException if an error occurred while creating the ellipsoid from the properties.
     *
     * @see Test2202#EPSG_7030()
     */
    @Test
    @DisplayName("GIGS ellipsoid A")
    public void GIGS_67030() throws FactoryException {
        setCodeAndName(67030, "GIGS ellipsoid A");
        semiMajorInMetres = 6378137.0;
        semiMajorAxis     = 6378137.0;
        semiMinorAxis     = 6356752.3;
        axisUnit          = units.metre();
        axisTolerance     = 0.05;
        inverseFlattening = 298.257223563;
        isIvfDefinitive   = true;
        verifyEllipsoid();
    }

    /**
     * Tests “GIGS ellipsoid B” flattened sphere creation from the factory.
     *
     * <ul>
     *   <li>GIGS ellipsoid code: <b>67001</b></li>
     *   <li>GIGS ellipsoid name: <b>GIGS ellipsoid B</b></li>
     *   <li>EPSG equivalence: <b>7001 – Airy 1830</b></li>
     *   <li>Semi-major axis (<var>a</var>): <b>6377563.396 metre</b></li>
     *   <li>Semi-minor axis (<var>b</var>): <b>6356256.909 metre</b></li>
     *   <li>Inverse flattening (1/<var>f</var>): <b>299.3249646</b></li>
     * </ul>
     *
     * Remarks: Defined using a and 1/f.
     *
     * @throws FactoryException if an error occurred while creating the ellipsoid from the properties.
     *
     * @see Test2202#EPSG_7001()
     */
    @Test
    @DisplayName("GIGS ellipsoid B")
    public void GIGS_67001() throws FactoryException {
        setCodeAndName(67001, "GIGS ellipsoid B");
        semiMajorInMetres = 6377563.396;
        semiMajorAxis     = 6377563.396;
        semiMinorAxis     = 6356256.909;
        axisUnit          = units.metre();
        axisTolerance     = 0.0005;
        inverseFlattening = 299.3249646;
        isIvfDefinitive   = true;
        verifyEllipsoid();
    }

    /**
     * Tests “GIGS ellipsoid C” flattened sphere creation from the factory.
     *
     * <ul>
     *   <li>GIGS ellipsoid code: <b>67004</b></li>
     *   <li>GIGS ellipsoid name: <b>GIGS ellipsoid C</b></li>
     *   <li>EPSG equivalence: <b>7004 – Bessel 1841</b></li>
     *   <li>Semi-major axis (<var>a</var>): <b>6377397.155 metre</b></li>
     *   <li>Semi-minor axis (<var>b</var>): <b>6356078.963 metre</b></li>
     *   <li>Inverse flattening (1/<var>f</var>): <b>299.1528128</b></li>
     * </ul>
     *
     * Remarks: Defined using a and 1/f.
     *
     * @throws FactoryException if an error occurred while creating the ellipsoid from the properties.
     *
     * @see Test2202#EPSG_7004()
     */
    @Test
    @DisplayName("GIGS ellipsoid C")
    public void GIGS_67004() throws FactoryException {
        setCodeAndName(67004, "GIGS ellipsoid C");
        semiMajorInMetres = 6377397.155;
        semiMajorAxis     = 6377397.155;
        semiMinorAxis     = 6356078.963;
        axisUnit          = units.metre();
        axisTolerance     = 0.0005;
        inverseFlattening = 299.1528128;
        isIvfDefinitive   = true;
        verifyEllipsoid();
    }

    /**
     * Tests “GIGS ellipsoid E” flattened sphere creation from the factory.
     *
     * <ul>
     *   <li>GIGS ellipsoid code: <b>67022</b></li>
     *   <li>GIGS ellipsoid name: <b>GIGS ellipsoid E</b></li>
     *   <li>EPSG equivalence: <b>7022 – International 1924</b></li>
     *   <li>Semi-major axis (<var>a</var>): <b>6378388.0 metre</b></li>
     *   <li>Semi-minor axis (<var>b</var>): <b>6356911.9 metre</b></li>
     *   <li>Inverse flattening (1/<var>f</var>): <b>297</b></li>
     * </ul>
     *
     * Remarks: Defined using a and 1/f.
     *
     * @throws FactoryException if an error occurred while creating the ellipsoid from the properties.
     *
     * @see Test2202#EPSG_7022()
     */
    @Test
    @DisplayName("GIGS ellipsoid E")
    public void GIGS_67022() throws FactoryException {
        setCodeAndName(67022, "GIGS ellipsoid E");
        semiMajorInMetres = 6378388.0;
        semiMajorAxis     = 6378388.0;
        semiMinorAxis     = 6356911.9;
        axisUnit          = units.metre();
        axisTolerance     = 0.05;
        inverseFlattening = 297.0;
        isIvfDefinitive   = true;
        verifyEllipsoid();
    }

    /**
     * Tests “GIGS ellipsoid F” flattened sphere creation from the factory.
     *
     * <ul>
     *   <li>GIGS ellipsoid code: <b>67019</b></li>
     *   <li>GIGS ellipsoid name: <b>GIGS ellipsoid F</b></li>
     *   <li>EPSG equivalence: <b>7019 – GRS 1980</b></li>
     *   <li>Semi-major axis (<var>a</var>): <b>6378.137 kilometre (6378137.0 metres)</b></li>
     *   <li>Semi-minor axis (<var>b</var>): <b>6356.752 kilometre (6356752.0 metres)</b></li>
     *   <li>Inverse flattening (1/<var>f</var>): <b>298.257222101</b></li>
     * </ul>
     *
     * Remarks: CAUTION defined in kilometre.
     * Not metre as per EPSG entity.
     *
     * @throws FactoryException if an error occurred while creating the ellipsoid from the properties.
     *
     * @see Test2202#EPSG_7019()
     */
    @Test
    @DisplayName("GIGS ellipsoid F")
    public void GIGS_67019() throws FactoryException {
        setCodeAndName(67019, "GIGS ellipsoid F");
        semiMajorInMetres = 6378137.0;
        semiMajorAxis     = 6378.137;
        semiMinorAxis     = 6356.752;
        axisUnit          = units.kilometre();
        axisTolerance     = 0.0005;
        inverseFlattening = 298.257222101;
        isIvfDefinitive   = true;
        verifyEllipsoid();
    }

    /**
     * Tests “GIGS ellipsoid H” ellipsoid creation from the factory.
     *
     * <ul>
     *   <li>GIGS ellipsoid code: <b>67011</b></li>
     *   <li>GIGS ellipsoid name: <b>GIGS ellipsoid H</b></li>
     *   <li>EPSG equivalence: <b>7011 – Clarke 1880 (IGN)</b></li>
     *   <li>Semi-major axis (<var>a</var>): <b>6378249.2 metre</b></li>
     *   <li>Semi-minor axis (<var>b</var>): <b>6356515.0 metre</b></li>
     *   <li>Inverse flattening (1/<var>f</var>): <b>293.466</b></li>
     * </ul>
     *
     * Remarks: Defined using a and b.
     * Calculated 1/f = 293.4660213.
     *
     * @throws FactoryException if an error occurred while creating the ellipsoid from the properties.
     *
     * @see Test2202#EPSG_7011()
     */
    @Test
    @DisplayName("GIGS ellipsoid H")
    public void GIGS_67011() throws FactoryException {
        setCodeAndName(67011, "GIGS ellipsoid H");
        semiMajorInMetres = 6378249.2;
        semiMajorAxis     = 6378249.2;
        semiMinorAxis     = 6356515.0;
        axisUnit          = units.metre();
        axisTolerance     = 0.05;
        inverseFlattening = 293.466;
        verifyEllipsoid();
    }

    /**
     * Tests “GIGS ellipsoid I” sphere creation from the factory.
     *
     * <ul>
     *   <li>GIGS ellipsoid code: <b>67052</b></li>
     *   <li>GIGS ellipsoid name: <b>GIGS ellipsoid I</b></li>
     *   <li>EPSG equivalence: <b>7052 – Clarke 1866 Authalic Sphere</b></li>
     *   <li>Semi-major axis (<var>a</var>): <b>6370997.0 metre</b></li>
     *   <li>Semi-minor axis (<var>b</var>): <b>6370997.0 metre</b></li>
     *   <li>Inverse flattening (1/<var>f</var>): <b>Infinity</b></li>
     * </ul>
     *
     * Remarks: Sphere.
     *
     * @throws FactoryException if an error occurred while creating the ellipsoid from the properties.
     *
     * @see Test2202#EPSG_7052()
     */
    @Test
    @DisplayName("GIGS ellipsoid I")
    public void GIGS_67052() throws FactoryException {
        setCodeAndName(67052, "GIGS ellipsoid I");
        semiMajorInMetres = 6370997.0;
        semiMajorAxis     = 6370997.0;
        semiMinorAxis     = 6370997.0;
        axisUnit          = units.metre();
        axisTolerance     = 0.05;
        inverseFlattening = Double.POSITIVE_INFINITY;
        isSphere          = true;
        verifyEllipsoid();
    }

    /**
     * Tests “GIGS ellipsoid J” flattened sphere creation from the factory.
     *
     * <ul>
     *   <li>GIGS ellipsoid code: <b>67008</b></li>
     *   <li>GIGS ellipsoid name: <b>GIGS ellipsoid J</b></li>
     *   <li>EPSG equivalence: <b>7008 – Clarke 1866</b></li>
     *   <li>Semi-major axis (<var>a</var>): <b>20925832.164 US survey foot (6378206.4 metres)</b></li>
     *   <li>Semi-minor axis (<var>b</var>): <b>20854892.017 US survey foot (6356583.8 metres)</b></li>
     *   <li>Inverse flattening (1/<var>f</var>): <b>294.978698214</b></li>
     * </ul>
     *
     * Remarks: Not metres.
     *
     * @throws FactoryException if an error occurred while creating the ellipsoid from the properties.
     *
     * @see Test2202#EPSG_7008()
     */
    @Test
    @DisplayName("GIGS ellipsoid J")
    public void GIGS_67008() throws FactoryException {
        setCodeAndName(67008, "GIGS ellipsoid J");
        semiMajorInMetres = 6378206.4;
        semiMajorAxis     = 20925832.164;
        semiMinorAxis     = 20854892.017;
        axisUnit          = units.footSurveyUS();
        axisTolerance     = 0.0005;
        inverseFlattening = 294.978698214;
        isIvfDefinitive   = true;
        verifyEllipsoid();
    }

    /**
     * Tests “GIGS ellipsoid K” flattened sphere creation from the factory.
     *
     * <ul>
     *   <li>GIGS ellipsoid code: <b>67036</b></li>
     *   <li>GIGS ellipsoid name: <b>GIGS ellipsoid K</b></li>
     *   <li>EPSG equivalence: <b>7036 – GRS 1967</b></li>
     *   <li>Semi-major axis (<var>a</var>): <b>6378160.0 metre</b></li>
     *   <li>Semi-minor axis (<var>b</var>): <b>6356774.5 metre</b></li>
     *   <li>Inverse flattening (1/<var>f</var>): <b>298.247167427</b></li>
     * </ul>
     *
     * Remarks: Defined using a and 1/f.
     *
     * @throws FactoryException if an error occurred while creating the ellipsoid from the properties.
     *
     * @see Test2202#EPSG_7036()
     */
    @Test
    @DisplayName("GIGS ellipsoid K")
    public void GIGS_67036() throws FactoryException {
        setCodeAndName(67036, "GIGS ellipsoid K");
        semiMajorInMetres = 6378160.0;
        semiMajorAxis     = 6378160.0;
        semiMinorAxis     = 6356774.5;
        axisUnit          = units.metre();
        axisTolerance     = 0.05;
        inverseFlattening = 298.247167427;
        isIvfDefinitive   = true;
        verifyEllipsoid();
    }

    /**
     * Tests “GIGS ellipsoid X” flattened sphere creation from the factory.
     *
     * <ul>
     *   <li>GIGS ellipsoid code: <b>67003</b></li>
     *   <li>GIGS ellipsoid name: <b>GIGS ellipsoid X</b></li>
     *   <li>EPSG equivalence: <b>7003 – Australian National Spheroid</b></li>
     *   <li>Semi-major axis (<var>a</var>): <b>6378160.0 metre</b></li>
     *   <li>Semi-minor axis (<var>b</var>): <b>6356774.7 metre</b></li>
     *   <li>Inverse flattening (1/<var>f</var>): <b>298.25</b></li>
     * </ul>
     *
     * Remarks: Defined using a and 1/f.
     *
     * @throws FactoryException if an error occurred while creating the ellipsoid from the properties.
     *
     * @see Test2202#EPSG_7003()
     */
    @Test
    @DisplayName("GIGS ellipsoid X")
    public void GIGS_67003() throws FactoryException {
        setCodeAndName(67003, "GIGS ellipsoid X");
        semiMajorInMetres = 6378160.0;
        semiMajorAxis     = 6378160.0;
        semiMinorAxis     = 6356774.7;
        axisUnit          = units.metre();
        axisTolerance     = 0.05;
        inverseFlattening = 298.25;
        isIvfDefinitive   = true;
        verifyEllipsoid();
    }

    /**
     * Tests “GIGS ellipsoid Y” flattened sphere creation from the factory.
     *
     * <ul>
     *   <li>GIGS ellipsoid code: <b>67024</b></li>
     *   <li>GIGS ellipsoid name: <b>GIGS ellipsoid Y</b></li>
     *   <li>EPSG equivalence: <b>7024 – Krassowsky 1940</b></li>
     *   <li>Semi-major axis (<var>a</var>): <b>6378245.0 metre</b></li>
     *   <li>Semi-minor axis (<var>b</var>): <b>6356863.0 metre</b></li>
     *   <li>Inverse flattening (1/<var>f</var>): <b>298.3</b></li>
     * </ul>
     *
     * Remarks: Defined using a and 1/f.
     *
     * @throws FactoryException if an error occurred while creating the ellipsoid from the properties.
     *
     * @see Test2202#EPSG_7024()
     */
    @Test
    @DisplayName("GIGS ellipsoid Y")
    public void GIGS_67024() throws FactoryException {
        setCodeAndName(67024, "GIGS ellipsoid Y");
        semiMajorInMetres = 6378245.0;
        semiMajorAxis     = 6378245.0;
        semiMinorAxis     = 6356863.0;
        axisUnit          = units.metre();
        axisTolerance     = 0.05;
        inverseFlattening = 298.3;
        isIvfDefinitive   = true;
        verifyEllipsoid();
    }
}
