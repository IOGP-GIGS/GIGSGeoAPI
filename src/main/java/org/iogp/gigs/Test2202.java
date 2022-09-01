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
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.datum.DatumAuthorityFactory;
import org.opengis.referencing.datum.Ellipsoid;
import org.iogp.gigs.internal.geoapi.Configuration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;


/**
 * Verifies reference ellipsoid parameters bundled with the geoscience software.
 *
 * <table class="gigs">
 * <caption>Test description</caption>
 * <tr>
 *   <th>Test method:</th>
 *   <td>Compare ellipsoid definitions included in the software against the EPSG Dataset.</td>
 * </tr><tr>
 *   <th>Test data:</th>
 *   <td><a href="https://github.com/IOGP-GIGS/GIGSTestDataset/tree/main/GIGSTestDatasetFiles/GIGS%202200%20Predefined%20Geodetic%20Data%20Objects%20test%20data/ASCII/GIGS_lib_2202_Ellipsoid.txt">{@code GIGS_lib_2202_Ellipsoid.txt}</a>
 *   and EPSG Dataset.
 *   Contains EPSG {@linkplain #code code} and {@linkplain #name name} for the ellipsoid,
 *   commonly encountered {@linkplain #aliases alternative name(s)} for the same object,
 *   the value and units for the {@link #semiMajorAxis semi-major axis},
 *   the conversion ratio to metres for these units, and then a second parameter which will be either
 *   the value of the {@linkplain #inverseFlattening inverse flattening} (unitless) or
 *   the value of the {@link #semiMinorAxis semi-minor axis} (in the same units as the semi-major axis).
 *   This class additionally contain a flag to indicate that the figure {@linkplain #isSphere is a sphere}:
 *   if {@code false} the figure is an oblate ellipsoid.</td>
 * </tr><tr>
 *   <th>Tested API:</th>
 *   <td>{@link DatumAuthorityFactory#createEllipsoid(String)}.</td>
 * </tr><tr>
 *   <th>Expected result:</th>
 *   <td>Ellipsoid definitions bundled with software, if any, should have same name and defining parameters
 *   as in the EPSG Dataset. Equivalent alternative parameters are acceptable but should be reported.
 *   The values of the parameters should be correct to at least 10 significant figures.
 *   For ellipsoids defined by Clarke and Everest, as well as those adopted by IUGG as International,
 *   several variants exist. These must be clearly distinguished.
 *   Ellipsoids missing from the software or at variance with those in the EPSG Dataset should be reported.</td>
 * </tr></table>
 *
 *
 * <h2>Usage example</h2>
 * in order to specify their factories and run the tests in a JUnit framework,
 * implementers can define a subclass in their own test suite as in the example below:
 *
 * {@snippet lang="java" :
 * public class MyTest extends Test2202 {
 *     public MyTest() {
 *         super(new MyDatumAuthorityFactory());
 *     }
 * }
 * }
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @author  Alexis Manin (Geomatys)
 * @version 1.0
 * @since   1.0
 */
@DisplayName("Ellipsoid")
public class Test2202 extends Series2000<Ellipsoid> {
    /**
     * The conversion factor from the unit of {@link #semiMajorAxis} to metres.
     */
    private double toMetres;

    /**
     * The {@link #semiMajorAxis} value converted to a length in metres.
     */
    private double semiMajorInMetres;

    /**
     * The expected semi-major axis length, in the units specified by the EPSG dataset.
     * This value can also be obtained as a length in metres by a call to the {@link #getSemiMajorAxis(boolean)} method.
     * This field is set by all test methods before to create and verify the {@link Ellipsoid} instance.
     *
     * @see #getSemiMajorAxis(boolean)
     */
    public double semiMajorAxis;

    /**
     * The expected semi-minor axis length, or {@link Double#NaN} if the second defining parameters is not this field.
     * If not {@code NaN}, the value is in the same units than {@link #semiMajorAxis}.
     * This value can be obtained as a length in metres by a call to the {@link #getSemiMinorAxis(boolean)} method.
     * This field is set by all test methods before to create and verify the {@link Ellipsoid} instance.
     *
     * @see #getSemiMinorAxis(boolean)
     */
    public double semiMinorAxis;

    /**
     * The expected inverse flattening, or {@link Double#NaN} if the second defining parameters is not this field.
     * This field is set by all test methods before to create and verify the {@link Ellipsoid} instance.
     */
    public double inverseFlattening;

    /**
     * Indicates if the figure of the Earth is a sphere. If {@code false} the figure is an oblate ellipsoid.
     * This field is set by all test methods before to create and verify the {@link Ellipsoid} instance.
     */
    public boolean isSphere;

    /**
     * The ellipsoid created by the factory,
     * or {@code null} if not yet created or if the ellipsoid creation failed.
     *
     * @see #datumAuthorityFactory
     */
    private Ellipsoid ellipsoid;

    /**
     * Factory to use for building {@link Ellipsoid} instances, or {@code null} if none.
     * This is the factory used by the {@link #getIdentifiedObject()} method.
     */
    protected final DatumAuthorityFactory datumAuthorityFactory;

    /**
     * Creates a new test using the given factory. If a given factory is {@code null},
     * then the tests which depend on it will be skipped.
     *
     * @param datumFactory  factory for creating {@link Ellipsoid} instances.
     */
    public Test2202(final DatumAuthorityFactory datumFactory) {
        datumAuthorityFactory = datumFactory;
    }

    /**
     * Returns information about the configuration of the test which has been run.
     * This method returns a map containing:
     *
     * <ul>
     *   <li>All the following values associated to the {@link org.opengis.test.Configuration.Key} of the same name:
     *     <ul>
     *       <li>{@link #isStandardIdentifierSupported}</li>
     *       <li>{@link #isStandardNameSupported}</li>
     *       <li>{@link #isStandardAliasSupported}</li>
     *       <li>{@link #isDependencyIdentificationSupported}</li>
     *       <li>{@link #isDeprecatedObjectCreationSupported}</li>
     *       <li>{@link #datumAuthorityFactory}</li>
     *     </ul>
     *   </li>
     * </ul>
     *
     * @return the configuration of the test being run.
     */
    @Override
    Configuration configuration() {
        final Configuration op = super.configuration();
        assertNull(op.put(Configuration.Key.datumAuthorityFactory, datumAuthorityFactory));
        return op;
    }

    /**
     * Returns the ellipsoid instance to be tested. When this method is invoked for the first time, it creates the
     * ellipsoid to test by invoking the {@link DatumAuthorityFactory#createEllipsoid(String)} method with the current
     * {@link #code} value in argument. The created object is then cached and returned in all subsequent invocations of
     * this method.
     *
     * @return the ellipsoid instance to test.
     * @throws FactoryException if an error occurred while creating the ellipsoid instance.
     */
    @Override
    public Ellipsoid getIdentifiedObject() throws FactoryException {
        if (ellipsoid == null) {
            assumeNotNull(datumAuthorityFactory);
            try {
                ellipsoid = datumAuthorityFactory.createEllipsoid(String.valueOf(code));
            } catch (NoSuchAuthorityCodeException e) {
                unsupportedCode(Ellipsoid.class, code);
                throw e;
            }
        }
        return ellipsoid;
    }

    /**
     * Sets the ellipsoid instance to be tested.
     * This is used for testing datum dependencies.
     *
     * @param  dependency  the datum dependency to test.
     */
    final void setIdentifiedObject(final Ellipsoid dependency) {
        assertNull(ellipsoid);
        ellipsoid = dependency;
    }

    /**
     * Returns the length of the semi-major axis, either in the units specified by the EPSG dataset or in metres.
     *
     * @param  inMetres {@code true} for the length in metres.
     * @return the semi-major axis length, guaranteed to be in metres if {@code inMetres} is {@code true}.
     *
     * @see #semiMajorAxis
     */
    public double getSemiMajorAxis(final boolean inMetres) {
        assertEquals(semiMajorInMetres, semiMajorAxis*toMetres, 0.01, "Inconsistent semi-major axis length in metres.");
        return inMetres ? semiMajorInMetres : semiMajorAxis;
    }

    /**
     * Returns the length of the semi-minor axis, either in the units specified by the EPSG dataset or in metres.
     * This method can be invoked only if the semi-minor axis length is the second defining parameter.
     *
     * @param  inMetres {@code true} for the length in metres.
     * @return the semi-minor axis length, guaranteed to be in metres if {@code inMetres} is {@code true}.
     *
     * @see #semiMinorAxis
     */
    public double getSemiMinorAxis(final boolean inMetres) {
        double value = semiMinorAxis;
        assertFalse(Double.isNaN(value), "Semi-minor axis length is not the second defining parameter.");
        if (inMetres) {
            semiMinorAxis *= toMetres;
        }
        return value;
    }

    /**
     * Verifies the properties of the ellipsoid given by {@link #getIdentifiedObject()}.
     *
     * @throws FactoryException if an error occurred while creating the ellipsoid.
     */
    private void verifyEllipsoid() throws FactoryException {
        final Ellipsoid ellipsoid = getIdentifiedObject();
        assertNotNull(ellipsoid, "Ellipsoid");
        validators.validate(ellipsoid);

        // Ellipsoid identification.
        assertIdentifierEquals(code, ellipsoid, "Ellipsoid");
        assertNameEquals(true, name, ellipsoid, "Ellipsoid");
        assertAliasesEqual (aliases, ellipsoid, "Ellipsoid");
        /*
         * Get the axis lengths and their unit. Null units are assumed to mean metres
         * (whether we accept null unit or not is determined by the validators).
         * If the implementation uses metre units but the EPSG definition expected
         * another unit, convert the axis lengths from the later units to metre units.
         */
        final Unit<Length> unit = ellipsoid.getAxisUnit();
        final boolean inMetres = toMetres != 1 && (unit == null || unit.equals(units.metre()));
        double expectedAxisLength = getSemiMajorAxis(inMetres);
        assertEquals(expectedAxisLength, ellipsoid.getSemiMajorAxis(), TOLERANCE*expectedAxisLength,
                     "Ellipsoid.getSemiMajorAxis()");

        if (!Double.isNaN(semiMinorAxis)) {
            expectedAxisLength = getSemiMinorAxis(inMetres);
            assertEquals(expectedAxisLength, ellipsoid.getSemiMinorAxis(), TOLERANCE*expectedAxisLength,
                         "Ellipsoid.getSemiMinorAxis()");
        }
        if (!Double.isNaN(inverseFlattening)) {
            assertEquals(inverseFlattening, ellipsoid.getInverseFlattening(), TOLERANCE*inverseFlattening,
                         "Ellipsoid.getInverseFlattening()");
        }
        assertEquals(isSphere, ellipsoid.isSphere(), "Ellipsoid.isSphere()");
    }

    /**
     * Tests “Airy 1830” ellipsoid creation from the factory.
     *
     * <ul>
     *   <li>EPSG ellipsoid code: <b>7001</b></li>
     *   <li>EPSG ellipsoid name: <b>Airy 1830</b></li>
     *   <li>Semi-major axis (<var>a</var>): <b>6377563.396 metre</b></li>
     *   <li>Inverse flattening (1/<var>f</var>): <b>299.3249646</b></li>
     *   <li>EPSG Usage Extent: <b>United Kingdom</b></li>
     * </ul>
     *
     * Remarks: Original definition is a=20923713.
     * B=20853810 feet of 1796.
     * 1/f is given to 7 decimal places.
     * For the 1936 retriangulation OSGB defines the relationship of 10 feet of 1796 to the International metre through ([10^0.48401603]/10) exactly = 0.3048007491.
     *
     * @throws FactoryException if an error occurred while creating the ellipsoid from the EPSG code.
     */
    @Test
    @DisplayName("Airy 1830")
    public void EPSG_7001() throws FactoryException {
        code              = 7001;
        name              = "Airy 1830";
        toMetres          = 1.0;
        semiMajorInMetres = 6377563.396;
        semiMajorAxis     = 6377563.396;
        semiMinorAxis     = Double.NaN;
        inverseFlattening = 299.3249646;
        verifyEllipsoid();
    }

    /**
     * Tests “Airy Modified 1849” ellipsoid creation from the factory.
     *
     * <ul>
     *   <li>EPSG ellipsoid code: <b>7002</b></li>
     *   <li>EPSG ellipsoid name: <b>Airy Modified 1849</b></li>
     *   <li>Semi-major axis (<var>a</var>): <b>6377340.189 metre</b></li>
     *   <li>Inverse flattening (1/<var>f</var>): <b>299.3249646</b></li>
     *   <li>EPSG Usage Extent: <b>Ireland</b></li>
     * </ul>
     *
     * Remarks: OSGB Airy 1830 figure (ellipsoid code 7001) rescaled by 0.999965 to best fit the scale of the 19th century primary triangulation of Ireland.
     *
     * @throws FactoryException if an error occurred while creating the ellipsoid from the EPSG code.
     */
    @Test
    @DisplayName("Airy Modified 1849")
    public void EPSG_7002() throws FactoryException {
        code              = 7002;
        name              = "Airy Modified 1849";
        toMetres          = 1.0;
        semiMajorInMetres = 6377340.189;
        semiMajorAxis     = 6377340.189;
        semiMinorAxis     = Double.NaN;
        inverseFlattening = 299.3249646;
        verifyEllipsoid();
    }

    /**
     * Tests “Australian National Spheroid” ellipsoid creation from the factory.
     *
     * <ul>
     *   <li>EPSG ellipsoid code: <b>7003</b></li>
     *   <li>EPSG ellipsoid name: <b>Australian National Spheroid</b></li>
     *   <li>Alias(es) given by EPSG: <b>ANS</b></li>
     *   <li>Semi-major axis (<var>a</var>): <b>6378160.0 metre</b></li>
     *   <li>Inverse flattening (1/<var>f</var>): <b>298.25</b></li>
     *   <li>EPSG Usage Extent: <b>Australia</b></li>
     * </ul>
     *
     * Remarks: Based on the GRS 1967 figure but with 1/f taken to 2 decimal places exactly.
     *  The dimensions are also used as the GRS 1967 Modified ellipsoid (see code 7050).
     *
     * @throws FactoryException if an error occurred while creating the ellipsoid from the EPSG code.
     */
    @Test
    @DisplayName("Australian National Spheroid")
    public void EPSG_7003() throws FactoryException {
        code              = 7003;
        name              = "Australian National Spheroid";
        aliases           = new String[] {"ANS"};
        toMetres          = 1.0;
        semiMajorInMetres = 6378160.0;
        semiMajorAxis     = 6378160.0;
        semiMinorAxis     = Double.NaN;
        inverseFlattening = 298.25;
        verifyEllipsoid();
    }

    /**
     * Tests “Average Terrestrial System 1977” ellipsoid creation from the factory.
     *
     * <ul>
     *   <li>EPSG ellipsoid code: <b>7041</b></li>
     *   <li>EPSG ellipsoid name: <b>Average Terrestrial System 1977</b></li>
     *   <li>Semi-major axis (<var>a</var>): <b>6378135.0 metre</b></li>
     *   <li>Inverse flattening (1/<var>f</var>): <b>298.257</b></li>
     *   <li>EPSG Usage Extent: <b>Canada</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the ellipsoid from the EPSG code.
     */
    @Test
    @DisplayName("Average Terrestrial System 1977")
    public void EPSG_7041() throws FactoryException {
        code              = 7041;
        name              = "Average Terrestrial System 1977";
        toMetres          = 1.0;
        semiMajorInMetres = 6378135.0;
        semiMajorAxis     = 6378135.0;
        semiMinorAxis     = Double.NaN;
        inverseFlattening = 298.257;
        verifyEllipsoid();
    }

    /**
     * Tests “Bessel 1841” ellipsoid creation from the factory.
     *
     * <ul>
     *   <li>EPSG ellipsoid code: <b>7004</b></li>
     *   <li>EPSG ellipsoid name: <b>Bessel 1841</b></li>
     *   <li>Semi-major axis (<var>a</var>): <b>6377397.155 metre</b></li>
     *   <li>Inverse flattening (1/<var>f</var>): <b>299.1528128</b></li>
     *   <li>EPSG Usage Extent: <b>Numerous</b></li>
     * </ul>
     *
     * Remarks: Original Bessel definition is a=3272077.14 and b=3261139.33 toise.
     * This used a weighted mean of values from several authors but did not account for differences
     * in the length of the various toise: the Bessel toise is therefore of uncertain length.
     *
     * @throws FactoryException if an error occurred while creating the ellipsoid from the EPSG code.
     */
    @Test
    @DisplayName("Bessel 1841")
    public void EPSG_7004() throws FactoryException {
        code              = 7004;
        name              = "Bessel 1841";
        toMetres          = 1.0;
        semiMajorInMetres = 6377397.155;
        semiMajorAxis     = 6377397.155;
        semiMinorAxis     = Double.NaN;
        inverseFlattening = 299.1528128;
        verifyEllipsoid();
    }

    /**
     * Tests “Bessel Modified” ellipsoid creation from the factory.
     *
     * <ul>
     *   <li>EPSG ellipsoid code: <b>7005</b></li>
     *   <li>EPSG ellipsoid name: <b>Bessel Modified</b></li>
     *   <li>Semi-major axis (<var>a</var>): <b>6377492.018 metre</b></li>
     *   <li>Inverse flattening (1/<var>f</var>): <b>299.1528128</b></li>
     *   <li>EPSG Usage Extent: <b>Norway; Sweden</b></li>
     * </ul>
     *
     * Remarks: 1mm increase in semi-major axis.
     *
     * @throws FactoryException if an error occurred while creating the ellipsoid from the EPSG code.
     */
    @Test
    @DisplayName("Bessel Modified")
    public void EPSG_7005() throws FactoryException {
        code              = 7005;
        name              = "Bessel Modified";
        toMetres          = 1.0;
        semiMajorInMetres = 6377492.018;
        semiMajorAxis     = 6377492.018;
        semiMinorAxis     = Double.NaN;
        inverseFlattening = 299.1528128;
        verifyEllipsoid();
    }

    /**
     * Tests “Bessel Namibia (GLM)” ellipsoid creation from the factory.
     *
     * <ul>
     *   <li>EPSG ellipsoid code: <b>7046</b></li>
     *   <li>EPSG ellipsoid name: <b>Bessel Namibia (GLM)</b></li>
     *   <li>Semi-major axis (<var>a</var>): <b>6377397.155 German legal metre</b></li>
     *   <li>Inverse flattening (1/<var>f</var>): <b>299.1528128</b></li>
     *   <li>EPSG Usage Extent: <b>Namibia</b></li>
     * </ul>
     *
     * Remarks: The semi-major axis has the same value as the Bessel 1841 ellipsoid (code 7004)
     * but is in different units - German Legal Metres rather than International metres - hence
     * a different size. a = 6377483.865 International metres.
     *
     * @throws FactoryException if an error occurred while creating the ellipsoid from the EPSG code.
     */
    @Test
    @DisplayName("Bessel Namibia (GLM)")
    public void EPSG_7046() throws FactoryException {
        code              = 7046;
        name              = "Bessel Namibia (GLM)";
        toMetres          = 1.0000135965;
        semiMajorInMetres = 6377483.86528042;
        semiMajorAxis     = 6377397.155;
        semiMinorAxis     = Double.NaN;
        inverseFlattening = 299.1528128;
        verifyEllipsoid();
    }

    /**
     * Tests “Clarke 1858” ellipsoid creation from the factory.
     *
     * <ul>
     *   <li>EPSG ellipsoid code: <b>7007</b></li>
     *   <li>EPSG ellipsoid name: <b>Clarke 1858</b></li>
     *   <li>Semi-major axis (<var>a</var>): <b>20926348 Clarke's foot</b></li>
     *   <li>Semi-minor axis (<var>b</var>): <b>20855233 Clarke's foot</b></li>
     *   <li>EPSG Usage Extent: <b>Numerous</b></li>
     * </ul>
     *
     * Remarks: Clarke's 1858/II solution. Derived parameters:
     * a = 6378293.645m using his 1865 ratio of 0.3047972654 feet per metre; 1/f = 294.26068.
     * In Australia and Amoco Trinidad 1/f taken to two decimal places (294.26 exactly).
     * Elsewhere a and b used to derive 1/f.
     *
     * @throws FactoryException if an error occurred while creating the ellipsoid from the EPSG code.
     */
    @Test
    @DisplayName("Clarke 1858")
    public void EPSG_7007() throws FactoryException {
        code              = 7007;
        name              = "Clarke 1858";
        toMetres          = 0.3047972654;
        semiMajorInMetres = 6378293.64520876;
        semiMajorAxis     = 20926348.0;
        semiMinorAxis     = 20855233.0;
        inverseFlattening = Double.NaN;
        verifyEllipsoid();
    }

    /**
     * Tests “Clarke 1866” ellipsoid creation from the factory.
     *
     * <ul>
     *   <li>EPSG ellipsoid code: <b>7008</b></li>
     *   <li>EPSG ellipsoid name: <b>Clarke 1866</b></li>
     *   <li>Semi-major axis (<var>a</var>): <b>6378206.4 metre</b></li>
     *   <li>Semi-minor axis (<var>b</var>): <b>6356583.8 metre</b></li>
     *   <li>EPSG Usage Extent: <b>Numerous</b></li>
     * </ul>
     *
     * Remarks: Original definition a=20926062 and b=20855121 (British) feet.
     * Uses Clarke's 1865 inch-metre ratio of 39.370432 to obtain metres.
     * (Metric value then converted to US survey feet for use in the US and international feet for use in Cayman Islands).
     *
     * @throws FactoryException if an error occurred while creating the ellipsoid from the EPSG code.
     */
    @Test
    @DisplayName("Clarke 1866")
    public void EPSG_7008() throws FactoryException {
        code              = 7008;
        name              = "Clarke 1866";
        toMetres          = 1.0;
        semiMajorInMetres = 6378206.4;
        semiMajorAxis     = 6378206.4;
        semiMinorAxis     = 6356583.8;
        inverseFlattening = Double.NaN;
        verifyEllipsoid();
    }

    /**
     * Tests “Clarke 1866 Authalic Sphere” spheroid creation from the factory.
     *
     * <ul>
     *   <li>EPSG ellipsoid code: <b>7052</b></li>
     *   <li>EPSG ellipsoid name: <b>Clarke 1866 Authalic Sphere</b></li>
     *   <li>Semi-major axis (<var>a</var>): <b>6370997.0 metre</b></li>
     *   <li>Semi-minor axis (<var>b</var>): <b>6370997.0 metre</b></li>
     *   <li>EPSG Usage Extent: <b>Numerous</b></li>
     * </ul>
     *
     * Remarks: Authalic sphere derived from Clarke 1866 ellipsoid (code 7008).
     *
     * @throws FactoryException if an error occurred while creating the ellipsoid from the EPSG code.
     */
    @Test
    @DisplayName("Clarke 1866 Authalic Sphere")
    public void EPSG_7052() throws FactoryException {
        code              = 7052;
        name              = "Clarke 1866 Authalic Sphere";
        toMetres          = 1.0;
        semiMajorInMetres = 6370997.0;
        semiMajorAxis     = 6370997.0;
        semiMinorAxis     = 6370997.0;
        inverseFlattening = Double.NaN;
        isSphere          = true;
        verifyEllipsoid();
    }

    /**
     * Tests “Clarke 1880” ellipsoid creation from the factory.
     *
     * <ul>
     *   <li>EPSG ellipsoid code: <b>7034</b></li>
     *   <li>EPSG ellipsoid name: <b>Clarke 1880</b></li>
     *   <li>Semi-major axis (<var>a</var>): <b>20926202 Clarke's foot</b></li>
     *   <li>Semi-minor axis (<var>b</var>): <b>20854895 Clarke's foot</b></li>
     *   <li>EPSG Usage Extent: <b>Numerous</b></li>
     * </ul>
     *
     * Remarks: Clarke gave a and b and also 1/f=293.465 (to 3 decimal places exactly).
     *
     * @throws FactoryException if an error occurred while creating the ellipsoid from the EPSG code.
     */
    @Test
    @DisplayName("Clarke 1880")
    public void EPSG_7034() throws FactoryException {
        code              = 7034;
        name              = "Clarke 1880";
        toMetres          = 0.3047972654;
        semiMajorInMetres = 6378249.14480801;
        semiMajorAxis     = 20926202.0;
        semiMinorAxis     = 20854895.0;
        inverseFlattening = Double.NaN;
        verifyEllipsoid();
    }

    /**
     * Tests “Clarke 1880 (Arc)” ellipsoid creation from the factory.
     *
     * <ul>
     *   <li>EPSG ellipsoid code: <b>7013</b></li>
     *   <li>EPSG ellipsoid name: <b>Clarke 1880 (Arc)</b></li>
     *   <li>Alias(es) given by EPSG: <b>Modified Clarke 1880 (South Africa)</b>, <b>Clarke 1880 (Cape)</b></li>
     *   <li>Semi-major axis (<var>a</var>): <b>6378249.145 metre</b></li>
     *   <li>Inverse flattening (1/<var>f</var>): <b>293.4663077</b></li>
     *   <li>EPSG Usage Extent: <b>South Africa</b></li>
     * </ul>
     *
     * Remarks: Adopts Clarke's value for a with derived 1/f.
     *  Uses his 1865 ratio of 39.370432 inch per metre to convert semi-major axis to metres.
     *
     * @throws FactoryException if an error occurred while creating the ellipsoid from the EPSG code.
     */
    @Test
    @DisplayName("Clarke 1880 (Arc)")
    public void EPSG_7013() throws FactoryException {
        code              = 7013;
        name              = "Clarke 1880 (Arc)";
        aliases           = new String[] {"Modified Clarke 1880 (South Africa)", "Clarke 1880 (Cape)"};
        toMetres          = 1.0;
        semiMajorInMetres = 6378249.145;
        semiMajorAxis     = 6378249.145;
        semiMinorAxis     = Double.NaN;
        inverseFlattening = 293.4663077;
        verifyEllipsoid();
    }

    /**
     * Tests “Clarke 1880 (Benoit)” ellipsoid creation from the factory.
     *
     * <ul>
     *   <li>EPSG ellipsoid code: <b>7010</b></li>
     *   <li>EPSG ellipsoid name: <b>Clarke 1880 (Benoit)</b></li>
     *   <li>Semi-major axis (<var>a</var>): <b>6378300.789 metre</b></li>
     *   <li>Semi-minor axis (<var>b</var>): <b>6356566.435 metre</b></li>
     *   <li>EPSG Usage Extent: <b>Numerous</b></li>
     * </ul>
     *
     * Remarks: Adopts Clarke's values for a and b.
     * Uses Benoit's 1895 ratio of 0.9143992 metres per yard to convert to metres.
     *
     * @throws FactoryException if an error occurred while creating the ellipsoid from the EPSG code.
     */
    @Test
    @DisplayName("Clarke 1880 (Benoit)")
    public void EPSG_7010() throws FactoryException {
        code              = 7010;
        name              = "Clarke 1880 (Benoit)";
        toMetres          = 1.0;
        semiMajorInMetres = 6378300.789;
        semiMajorAxis     = 6378300.789;
        semiMinorAxis     = 6356566.435;
        inverseFlattening = Double.NaN;
        verifyEllipsoid();
    }

    /**
     * Tests “Clarke 1880 (IGN)” ellipsoid creation from the factory.
     *
     * <ul>
     *   <li>EPSG ellipsoid code: <b>7011</b></li>
     *   <li>EPSG ellipsoid name: <b>Clarke 1880 (IGN)</b></li>
     *   <li>Semi-major axis (<var>a</var>): <b>6378249.2 metre</b></li>
     *   <li>Semi-minor axis (<var>b</var>): <b>6356515.0 metre</b></li>
     *   <li>EPSG Usage Extent: <b>Numerous</b></li>
     * </ul>
     *
     * Remarks: Adopts Clarke's values for a and b using his 1865 ratio
     * of 39.370432 inches per metre to convert axes to metres.
     *
     * @throws FactoryException if an error occurred while creating the ellipsoid from the EPSG code.
     */
    @Test
    @DisplayName("Clarke 1880 (IGN)")
    public void EPSG_7011() throws FactoryException {
        code              = 7011;
        name              = "Clarke 1880 (IGN)";
        toMetres          = 1.0;
        semiMajorInMetres = 6378249.2;
        semiMajorAxis     = 6378249.2;
        semiMinorAxis     = 6356515.0;
        inverseFlattening = Double.NaN;
        verifyEllipsoid();
    }

    /**
     * Tests “Clarke 1880 (international foot)” ellipsoid creation from the factory.
     *
     * <ul>
     *   <li>EPSG ellipsoid code: <b>7055</b></li>
     *   <li>EPSG ellipsoid name: <b>Clarke 1880 (international foot)</b></li>
     *   <li>Semi-major axis (<var>a</var>): <b>20926202 foot</b></li>
     *   <li>Semi-minor axis (<var>b</var>): <b>20854895 foot</b></li>
     *   <li>EPSG Usage Extent: <b>Numerous</b></li>
     * </ul>
     *
     * Remarks: Clarke's 1880 definition in feet assumed for the purposes of metric conversion to be international foot.
     * A = 6378306.370 metres.
     * 1/f derived from a and b = 293.4663077.
     *
     * @throws FactoryException if an error occurred while creating the ellipsoid from the EPSG code.
     */
    @Test
    @DisplayName("Clarke 1880 (international foot)")
    public void EPSG_7055() throws FactoryException {
        code              = 7055;
        name              = "Clarke 1880 (international foot)";
        toMetres          = 0.3048;
        semiMajorInMetres = 6378306.3696;
        semiMajorAxis     = 20926202.0;
        semiMinorAxis     = 20854895.0;
        inverseFlattening = Double.NaN;
        verifyEllipsoid();
    }

    /**
     * Tests “Clarke 1866 Michigan” ellipsoid creation from the factory <em>(deprecated)</em>.
     * This is test is executed only if {@link #isDeprecatedObjectCreationSupported} is {@code true}.
     *
     * <ul>
     *   <li>EPSG ellipsoid code: <b>7009</b></li>
     *   <li>EPSG ellipsoid name: <b>Clarke 1866 Michigan</b></li>
     *   <li>Semi-major axis (<var>a</var>): <b>20926631.531</b></li>
     *   <li>Semi-minor axis (<var>b</var>): <b>20855688.674</b></li>
     *   <li><b>Deprecated:</b> Ellipsoid scaling moved from datum to map projection to accord with NGS practice.</li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the ellipsoid from the EPSG code.
     */
    @Test
    @DisplayName("Clarke 1866 Michigan")
    public void EPSG_7009() throws FactoryException {
        code              = 7009;
        name              = "Clarke 1866 Michigan";
        toMetres          = 0.30480061;
        semiMajorInMetres = 6378450.048;
        semiMajorAxis     = 20926631.531;
        semiMinorAxis     = 20855688.674;
        inverseFlattening = Double.NaN;
        assumeTrue(isDeprecatedObjectCreationSupported, "Creation of deprecated objects not supported.");
        verifyEllipsoid();
    }

    /**
     * Tests “Clarke 1880 (RGS)” ellipsoid creation from the factory.
     *
     * <ul>
     *   <li>EPSG ellipsoid code: <b>7012</b></li>
     *   <li>EPSG ellipsoid name: <b>Clarke 1880 (RGS)</b></li>
     *   <li>Alias(es) given by EPSG: <b>Clarke Modified 1880</b></li>
     *   <li>Semi-major axis (<var>a</var>): <b>6378249.145 metre</b></li>
     *   <li>Inverse flattening (1/<var>f</var>): <b>293.465</b></li>
     *   <li>EPSG Usage Extent: <b>Numerous</b></li>
     * </ul>
     *
     * Remarks: Adopts Clarke's values for a and 1/f.
     * Adopts his 1865 ratio of 39.370432 inches per metre to convert semi-major axis to metres.
     *
     * @throws FactoryException if an error occurred while creating the ellipsoid from the EPSG code.
     */
    @Test
    @DisplayName("Clarke 1880 (RGS)")
    public void EPSG_7012() throws FactoryException {
        code              = 7012;
        name              = "Clarke 1880 (RGS)";
        aliases           = new String[] {"Clarke Modified 1880"};
        toMetres          = 1.0;
        semiMajorInMetres = 6378249.145;
        semiMajorAxis     = 6378249.145;
        semiMinorAxis     = Double.NaN;
        inverseFlattening = 293.465;
        verifyEllipsoid();
    }

    /**
     * Tests “Clarke 1880 (SGA 1922)” ellipsoid creation from the factory.
     *
     * <ul>
     *   <li>EPSG ellipsoid code: <b>7014</b></li>
     *   <li>EPSG ellipsoid name: <b>Clarke 1880 (SGA 1922)</b></li>
     *   <li>Semi-major axis (<var>a</var>): <b>6378249.2 metre</b></li>
     *   <li>Inverse flattening (1/<var>f</var>): <b>293.46598</b></li>
     *   <li>EPSG Usage Extent: <b>France</b></li>
     * </ul>
     *
     * Remarks: Used in Old French Triangulation (ATF).
     * Uses Clarke's 1865 inch-metre ratio of 39.370432 to convert axes to metres.
     *
     * @throws FactoryException if an error occurred while creating the ellipsoid from the EPSG code.
     */
    @Test
    @DisplayName("Clarke 1880 (SGA 1922)")
    public void EPSG_7014() throws FactoryException {
        code              = 7014;
        name              = "Clarke 1880 (SGA 1922)";
        toMetres          = 1.0;
        semiMajorInMetres = 6378249.2;
        semiMajorAxis     = 6378249.2;
        semiMinorAxis     = Double.NaN;
        inverseFlattening = 293.46598;
        verifyEllipsoid();
    }

    /**
     * Tests “Danish 1876” ellipsoid creation from the factory.
     *
     * <ul>
     *   <li>EPSG ellipsoid code: <b>7051</b></li>
     *   <li>EPSG ellipsoid name: <b>Danish 1876</b></li>
     *   <li>Semi-major axis (<var>a</var>): <b>6377019.27 metre</b></li>
     *   <li>Inverse flattening (1/<var>f</var>): <b>300</b></li>
     *   <li>EPSG Usage Extent: <b>Denmark</b></li>
     * </ul>
     *
     * Remarks: Semi-major axis originally given as 3271883.25 toise.
     * Uses toise to French metre ratio of 1.94903631 to two decimal place precision.
     *
     * @throws FactoryException if an error occurred while creating the ellipsoid from the EPSG code.
     */
    @Test
    @DisplayName("Danish 1876")
    public void EPSG_7051() throws FactoryException {
        code              = 7051;
        name              = "Danish 1876";
        toMetres          = 1.0;
        semiMajorInMetres = 6377019.27;
        semiMajorAxis     = 6377019.27;
        semiMinorAxis     = Double.NaN;
        inverseFlattening = 300.0;
        verifyEllipsoid();
    }

    /**
     * Tests “Everest (1830 Definition)” ellipsoid creation from the factory.
     *
     * <ul>
     *   <li>EPSG ellipsoid code: <b>7042</b></li>
     *   <li>EPSG ellipsoid name: <b>Everest (1830 Definition)</b></li>
     *   <li>Semi-major axis (<var>a</var>): <b>20922931.8 Indian foot</b></li>
     *   <li>Semi-minor axis (<var>b</var>): <b>20853374.58 Indian foot</b></li>
     *   <li>EPSG Usage Extent: <b>Asia</b></li>
     * </ul>
     *
     * Remarks: Everest gave a and b to 2 decimal places and also 1/f=300.8017 (to 4 decimal places exactly).
     *
     * @throws FactoryException if an error occurred while creating the ellipsoid from the EPSG code.
     */
    @Test
    @DisplayName("Everest (1830 Definition)")
    public void EPSG_7042() throws FactoryException {
        code              = 7042;
        name              = "Everest (1830 Definition)";
        toMetres          = 0.304799510248147;
        semiMajorInMetres = 6377299.36559538;
        semiMajorAxis     = 20922931.8;
        semiMinorAxis     = 20853374.58;
        inverseFlattening = Double.NaN;
        verifyEllipsoid();
    }

    /**
     * Tests “Everest 1830 (1937 Adjustment)” ellipsoid creation from the factory.
     *
     * <ul>
     *   <li>EPSG ellipsoid code: <b>7015</b></li>
     *   <li>EPSG ellipsoid name: <b>Everest 1830 (1937 Adjustment)</b></li>
     *   <li>Semi-major axis (<var>a</var>): <b>6377276.345 metre</b></li>
     *   <li>Inverse flattening (1/<var>f</var>): <b>300.8017</b></li>
     *   <li>EPSG Usage Extent: <b>India</b></li>
     * </ul>
     *
     * Remarks: Used for the 1937 readjustment of Indian triangulation.
     * Clarke's 1865 Indian-British foot ratio (0.99999566) and Benoit's 1898 British inch-metre ratio (39.370113)
     * rounded as 0.30479841 exactly and applied to Everest's 1830 definition taken as a and 1/f.
     *
     * @throws FactoryException if an error occurred while creating the ellipsoid from the EPSG code.
     */
    @Test
    @DisplayName("Everest 1830 (1937 Adjustment)")
    public void EPSG_7015() throws FactoryException {
        code              = 7015;
        name              = "Everest 1830 (1937 Adjustment)";
        toMetres          = 1.0;
        semiMajorInMetres = 6377276.345;
        semiMajorAxis     = 6377276.345;
        semiMinorAxis     = Double.NaN;
        inverseFlattening = 300.8017;
        verifyEllipsoid();
    }

    /**
     * Tests “Everest 1830 (1962 Definition)” ellipsoid creation from the factory.
     *
     * <ul>
     *   <li>EPSG ellipsoid code: <b>7044</b></li>
     *   <li>EPSG ellipsoid name: <b>Everest 1830 (1962 Definition)</b></li>
     *   <li>Semi-major axis (<var>a</var>): <b>6377301.243 metre</b></li>
     *   <li>Inverse flattening (1/<var>f</var>): <b>300.8017255</b></li>
     *   <li>EPSG Usage Extent: <b>Asia</b></li>
     * </ul>
     *
     * Remarks: Used by Pakistan since metrication.
     * Clarke's 1865 Indian foot-British foot ratio (0.99999566) and his 1865 British inch-metre ratio (39.369971)
     * rounded with slight error as 1 Ind ft = 0.3047995m exactly and applied to Everest's 1830 definition of a &amp; b.
     *
     * @throws FactoryException if an error occurred while creating the ellipsoid from the EPSG code.
     */
    @Test
    @DisplayName("Everest 1830 (1962 Definition)")
    public void EPSG_7044() throws FactoryException {
        code              = 7044;
        name              = "Everest 1830 (1962 Definition)";
        toMetres          = 1.0;
        semiMajorInMetres = 6377301.243;
        semiMajorAxis     = 6377301.243;
        semiMinorAxis     = Double.NaN;
        inverseFlattening = 300.8017255;
        verifyEllipsoid();
    }

    /**
     * Tests “Everest 1830 (1967 Definition)” ellipsoid creation from the factory.
     *
     * <ul>
     *   <li>EPSG ellipsoid code: <b>7016</b></li>
     *   <li>EPSG ellipsoid name: <b>Everest 1830 (1967 Definition)</b></li>
     *   <li>Semi-major axis (<var>a</var>): <b>6377298.556 metre</b></li>
     *   <li>Inverse flattening (1/<var>f</var>): <b>300.8017</b></li>
     *   <li>EPSG Usage Extent: <b>East Malaysia</b></li>
     * </ul>
     *
     * Remarks: Applies Sears 1922 inch-metre ratio of 39.370147 to Everest 1830 original
     * definition of a and 1/f but with a taken to be in British rather than Indian feet.
     *
     * @throws FactoryException if an error occurred while creating the ellipsoid from the EPSG code.
     */
    @Test
    @DisplayName("Everest 1830 (1967 Definition)")
    public void EPSG_7016() throws FactoryException {
        code              = 7016;
        name              = "Everest 1830 (1967 Definition)";
        toMetres          = 1.0;
        semiMajorInMetres = 6377298.556;
        semiMajorAxis     = 6377298.556;
        semiMinorAxis     = Double.NaN;
        inverseFlattening = 300.8017;
        verifyEllipsoid();
    }

    /**
     * Tests “Everest 1830 (1975 Definition)” ellipsoid creation from the factory.
     *
     * <ul>
     *   <li>EPSG ellipsoid code: <b>7045</b></li>
     *   <li>EPSG ellipsoid name: <b>Everest 1830 (1975 Definition)</b></li>
     *   <li>Semi-major axis (<var>a</var>): <b>6377299.151 metre</b></li>
     *   <li>Inverse flattening (1/<var>f</var>): <b>300.8017255</b></li>
     *   <li>EPSG Usage Extent: <b>Asia</b></li>
     * </ul>
     *
     * Remarks: Used by India since metrication.
     * Clarke's 1865 Indian foot-British foot ratio (0.99999566) and his 1865 British inch-metre ratio (39.369971)
     * rounded as 1 Ind ft = 0.3047995m exactly applied to Everest's 1830 original definition taken as a and b.
     *
     * @throws FactoryException if an error occurred while creating the ellipsoid from the EPSG code.
     */
    @Test
    @DisplayName("Everest 1830 (1975 Definition)")
    public void EPSG_7045() throws FactoryException {
        code              = 7045;
        name              = "Everest 1830 (1975 Definition)";
        toMetres          = 1.0;
        semiMajorInMetres = 6377299.151;
        semiMajorAxis     = 6377299.151;
        semiMinorAxis     = Double.NaN;
        inverseFlattening = 300.8017255;
        verifyEllipsoid();
    }

    /**
     * Tests “Everest 1830 (RSO 1969)” ellipsoid creation from the factory.
     *
     * <ul>
     *   <li>EPSG ellipsoid code: <b>7056</b></li>
     *   <li>EPSG ellipsoid name: <b>Everest 1830 (RSO 1969)</b></li>
     *   <li>Semi-major axis (<var>a</var>): <b>6377295.664 metre</b></li>
     *   <li>Inverse flattening (1/<var>f</var>): <b>300.8017</b></li>
     *   <li>EPSG Usage Extent: <b>Malaysia</b></li>
     * </ul>
     *
     * Remarks: Adopted for 1969 metrication of peninsula Malaysia RSO grid.
     * Uses Sears 1922 yard-metre ratio truncated to 6 significant figures
     * applied to Everest 1830 original definition of a and 1/f but with a
     * taken to be in British rather than Indian feet.
     *
     * @throws FactoryException if an error occurred while creating the ellipsoid from the EPSG code.
     */
    @Test
    @DisplayName("Everest 1830 (RSO 1969)")
    public void EPSG_7056() throws FactoryException {
        code              = 7056;
        name              = "Everest 1830 (RSO 1969)";
        toMetres          = 1.0;
        semiMajorInMetres = 6377295.664;
        semiMajorAxis     = 6377295.664;
        semiMinorAxis     = Double.NaN;
        inverseFlattening = 300.8017;
        verifyEllipsoid();
    }

    /**
     * Tests “Everest 1830 Modified” ellipsoid creation from the factory.
     *
     * <ul>
     *   <li>EPSG ellipsoid code: <b>7018</b></li>
     *   <li>EPSG ellipsoid name: <b>Everest 1830 Modified</b></li>
     *   <li>Semi-major axis (<var>a</var>): <b>6377304.063 metre</b></li>
     *   <li>Inverse flattening (1/<var>f</var>): <b>300.8017</b></li>
     *   <li>EPSG Usage Extent: <b>West Malaysia</b></li>
     * </ul>
     *
     * Remarks: Applies Benoit 1898 inch-metre ratio of 39.370113 to Everest 1830 original
     * definition of a and 1/f but with a taken to be in British rather than Indian feet.
     *
     * @throws FactoryException if an error occurred while creating the ellipsoid from the EPSG code.
     */
    @Test
    @DisplayName("Everest 1830 Modified")
    public void EPSG_7018() throws FactoryException {
        code              = 7018;
        name              = "Everest 1830 Modified";
        toMetres          = 1.0;
        semiMajorInMetres = 6377304.063;
        semiMajorAxis     = 6377304.063;
        semiMinorAxis     = Double.NaN;
        inverseFlattening = 300.8017;
        verifyEllipsoid();
    }

    /**
     * Tests “GEM 10C” ellipsoid creation from the factory.
     *
     * <ul>
     *   <li>EPSG ellipsoid code: <b>7031</b></li>
     *   <li>EPSG ellipsoid name: <b>GEM 10C</b></li>
     *   <li>Semi-major axis (<var>a</var>): <b>6378137.0 metre</b></li>
     *   <li>Inverse flattening (1/<var>f</var>): <b>298.257223563</b></li>
     *   <li>EPSG Usage Extent: <b>Numerous</b></li>
     * </ul>
     *
     * Remarks: Used for  GEM 10C Gravity Potential Model.
     *
     * @throws FactoryException if an error occurred while creating the ellipsoid from the EPSG code.
     */
    @Test
    @DisplayName("GEM 10C")
    public void EPSG_7031() throws FactoryException {
        code              = 7031;
        name              = "GEM 10C";
        toMetres          = 1.0;
        semiMajorInMetres = 6378137.0;
        semiMajorAxis     = 6378137.0;
        semiMinorAxis     = Double.NaN;
        inverseFlattening = 298.257223563;
        verifyEllipsoid();
    }

    /**
     * Tests “GRS 1967” ellipsoid creation from the factory.
     *
     * <ul>
     *   <li>EPSG ellipsoid code: <b>7036</b></li>
     *   <li>EPSG ellipsoid name: <b>GRS 1967</b></li>
     *   <li>Alias(es) given by EPSG: <b>International 1967</b></li>
     *   <li>Semi-major axis (<var>a</var>): <b>6378160.0 metre</b></li>
     *   <li>Inverse flattening (1/<var>f</var>): <b>298.247167427</b></li>
     *   <li>EPSG Usage Extent: <b>Australia</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the ellipsoid from the EPSG code.
     */
    @Test
    @DisplayName("GRS 1967")
    public void EPSG_7036() throws FactoryException {
        code              = 7036;
        name              = "GRS 1967";
        aliases           = new String[] {"International 1967"};
        toMetres          = 1.0;
        semiMajorInMetres = 6378160.0;
        semiMajorAxis     = 6378160.0;
        semiMinorAxis     = Double.NaN;
        inverseFlattening = 298.247167427;
        verifyEllipsoid();
    }

    /**
     * Tests “GRS 1967 Modified” ellipsoid creation from the factory.
     *
     * <ul>
     *   <li>EPSG ellipsoid code: <b>7050</b></li>
     *   <li>EPSG ellipsoid name: <b>GRS 1967 Modified</b></li>
     *   <li>Alias(es) given by EPSG: <b>GRS 1967</b></li>
     *   <li>Semi-major axis (<var>a</var>): <b>6378160.0 metre</b></li>
     *   <li>Inverse flattening (1/<var>f</var>): <b>298.25</b></li>
     *   <li>EPSG Usage Extent: <b>Australia</b></li>
     * </ul>
     *
     * Remarks: Based on the GRS 1967 figure (code 7036) but with 1/f taken to 2 decimal places exactly.
     * Used with SAD69 and TWD67 datums.
     * The dimensions are also used as the Australian National Spheroid (code 7003).
     *
     * @throws FactoryException if an error occurred while creating the ellipsoid from the EPSG code.
     */
    @Test
    @DisplayName("GRS 1967 Modified")
    public void EPSG_7050() throws FactoryException {
        code              = 7050;
        name              = "GRS 1967 Modified";
        aliases           = new String[] {"GRS 1967"};
        toMetres          = 1.0;
        semiMajorInMetres = 6378160.0;
        semiMajorAxis     = 6378160.0;
        semiMinorAxis     = Double.NaN;
        inverseFlattening = 298.25;
        verifyEllipsoid();
    }

    /**
     * Tests “GRS 1980” ellipsoid creation from the factory.
     *
     * <ul>
     *   <li>EPSG ellipsoid code: <b>7019</b></li>
     *   <li>EPSG ellipsoid name: <b>GRS 1980</b></li>
     *   <li>Alias(es) given by EPSG: <b>International 1979</b></li>
     *   <li>Semi-major axis (<var>a</var>): <b>6378137.0 metre</b></li>
     *   <li>Inverse flattening (1/<var>f</var>): <b>298.257222101</b></li>
     *   <li>EPSG Usage Extent: <b>Numerous</b></li>
     * </ul>
     *
     * Remarks: Adopted by IUGG 1979 Canberra.
     *
     * @throws FactoryException if an error occurred while creating the ellipsoid from the EPSG code.
     */
    @Test
    @DisplayName("GRS 1980")
    public void EPSG_7019() throws FactoryException {
        code              = 7019;
        name              = "GRS 1980";
        aliases           = new String[] {"International 1979"};
        toMetres          = 1.0;
        semiMajorInMetres = 6378137.0;
        semiMajorAxis     = 6378137.0;
        semiMinorAxis     = Double.NaN;
        inverseFlattening = 298.257222101;
        verifyEllipsoid();
    }

    /**
     * Tests “GRS 1980 Authalic Sphere” spheroid creation from the factory.
     *
     * <ul>
     *   <li>EPSG ellipsoid code: <b>7048</b></li>
     *   <li>EPSG ellipsoid name: <b>GRS 1980 Authalic Sphere</b></li>
     *   <li>Semi-major axis (<var>a</var>): <b>6371007.0 metre</b></li>
     *   <li>Semi-minor axis (<var>b</var>): <b>6371007.0 metre</b></li>
     *   <li>EPSG Usage Extent: <b>Australia</b></li>
     * </ul>
     *
     * Remarks: Authalic sphere derived from GRS 1980 ellipsoid (code 7019).
     * 1/f is infinite.
     *
     * @throws FactoryException if an error occurred while creating the ellipsoid from the EPSG code.
     */
    @Test
    @DisplayName("GRS 1980 Authalic Sphere")
    public void EPSG_7048() throws FactoryException {
        code              = 7048;
        name              = "GRS 1980 Authalic Sphere";
        toMetres          = 1.0;
        semiMajorInMetres = 6371007.0;
        semiMajorAxis     = 6371007.0;
        semiMinorAxis     = 6371007.0;
        inverseFlattening = Double.NaN;
        isSphere          = true;
        verifyEllipsoid();
    }

    /**
     * Tests “Helmert 1906” ellipsoid creation from the factory.
     *
     * <ul>
     *   <li>EPSG ellipsoid code: <b>7020</b></li>
     *   <li>EPSG ellipsoid name: <b>Helmert 1906</b></li>
     *   <li>Semi-major axis (<var>a</var>): <b>6378200.0 metre</b></li>
     *   <li>Inverse flattening (1/<var>f</var>): <b>298.3</b></li>
     *   <li>EPSG Usage Extent: <b>Egypt</b></li>
     * </ul>
     *
     * Remarks: Helmert 1906/III solution.
     *
     * @throws FactoryException if an error occurred while creating the ellipsoid from the EPSG code.
     */
    @Test
    @DisplayName("Helmert 1906")
    public void EPSG_7020() throws FactoryException {
        code              = 7020;
        name              = "Helmert 1906";
        toMetres          = 1.0;
        semiMajorInMetres = 6378200.0;
        semiMajorAxis     = 6378200.0;
        semiMinorAxis     = Double.NaN;
        inverseFlattening = 298.3;
        verifyEllipsoid();
    }

    /**
     * Tests “Hough 1960” ellipsoid creation from the factory.
     *
     * <ul>
     *   <li>EPSG ellipsoid code: <b>7053</b></li>
     *   <li>EPSG ellipsoid name: <b>Hough 1960</b></li>
     *   <li>Semi-major axis (<var>a</var>): <b>6378270.0 metre</b></li>
     *   <li>Inverse flattening (1/<var>f</var>): <b>297</b></li>
     *   <li>EPSG Usage Extent: <b>USA</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the ellipsoid from the EPSG code.
     */
    @Test
    @DisplayName("Hough 1960")
    public void EPSG_7053() throws FactoryException {
        code              = 7053;
        name              = "Hough 1960";
        toMetres          = 1.0;
        semiMajorInMetres = 6378270.0;
        semiMajorAxis     = 6378270.0;
        semiMinorAxis     = Double.NaN;
        inverseFlattening = 297.0;
        verifyEllipsoid();
    }

    /**
     * Tests “Hughes 1980” ellipsoid creation from the factory.
     *
     * <ul>
     *   <li>EPSG ellipsoid code: <b>7058</b></li>
     *   <li>EPSG ellipsoid name: <b>Hughes 1980</b></li>
     *   <li>Semi-major axis (<var>a</var>): <b>6378273.0 metre</b></li>
     *   <li>Semi-minor axis (<var>b</var>): <b>6356889.449 metre</b></li>
     *   <li>EPSG Usage Extent: <b>Polar</b></li>
     * </ul>
     *
     * Remarks: Semi-minor axis derived from eccentricity = 0.081816153.
     * Semi-major axis (a) sometimes given as 3443.992nm which OGP suspects is a derived approximation.
     * IOGP conversion assumes 1nm=1852m exactly.
     *
     * @throws FactoryException if an error occurred while creating the ellipsoid from the EPSG code.
     */
    @Test
    @DisplayName("Hughes 1980")
    public void EPSG_7058() throws FactoryException {
        code              = 7058;
        name              = "Hughes 1980";
        toMetres          = 1.0;
        semiMajorInMetres = 6378273.0;
        semiMajorAxis     = 6378273.0;
        semiMinorAxis     = 6356889.449;
        inverseFlattening = Double.NaN;
        verifyEllipsoid();
    }

    /**
     * Tests “IAG 1975” ellipsoid creation from the factory.
     *
     * <ul>
     *   <li>EPSG ellipsoid code: <b>7049</b></li>
     *   <li>EPSG ellipsoid name: <b>IAG 1975</b></li>
     *   <li>Alias(es) given by EPSG: <b>Xian 1980</b></li>
     *   <li>Semi-major axis (<var>a</var>): <b>6378140.0 metre</b></li>
     *   <li>Inverse flattening (1/<var>f</var>): <b>298.257</b></li>
     *   <li>EPSG Usage Extent: <b>China</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the ellipsoid from the EPSG code.
     */
    @Test
    @DisplayName("IAG 1975")
    public void EPSG_7049() throws FactoryException {
        code              = 7049;
        name              = "IAG 1975";
        aliases           = new String[] {"Xian 1980"};
        toMetres          = 1.0;
        semiMajorInMetres = 6378140.0;
        semiMajorAxis     = 6378140.0;
        semiMinorAxis     = Double.NaN;
        inverseFlattening = 298.257;
        verifyEllipsoid();
    }

    /**
     * Tests “Indonesian National Spheroid” ellipsoid creation from the factory.
     *
     * <ul>
     *   <li>EPSG ellipsoid code: <b>7021</b></li>
     *   <li>EPSG ellipsoid name: <b>Indonesian National Spheroid</b></li>
     *   <li>Semi-major axis (<var>a</var>): <b>6378160.0 metre</b></li>
     *   <li>Inverse flattening (1/<var>f</var>): <b>298.247</b></li>
     *   <li>EPSG Usage Extent: <b>Indonesia</b></li>
     * </ul>
     *
     * Remarks: Based on the GRS 1967 figure but with 1/f taken to 3 decimal places exactly.
     *
     * @throws FactoryException if an error occurred while creating the ellipsoid from the EPSG code.
     */
    @Test
    @DisplayName("Indonesian National Spheroid")
    public void EPSG_7021() throws FactoryException {
        code              = 7021;
        name              = "Indonesian National Spheroid";
        toMetres          = 1.0;
        semiMajorInMetres = 6378160.0;
        semiMajorAxis     = 6378160.0;
        semiMinorAxis     = Double.NaN;
        inverseFlattening = 298.247;
        verifyEllipsoid();
    }

    /**
     * Tests “International 1924” ellipsoid creation from the factory.
     *
     * <ul>
     *   <li>EPSG ellipsoid code: <b>7022</b></li>
     *   <li>EPSG ellipsoid name: <b>International 1924</b></li>
     *   <li>Alias(es) given by EPSG: <b>Hayford 1909</b></li>
     *   <li>Semi-major axis (<var>a</var>): <b>6378388.0 metre</b></li>
     *   <li>Inverse flattening (1/<var>f</var>): <b>297</b></li>
     *   <li>EPSG Usage Extent: <b>Numerous</b></li>
     * </ul>
     *
     * Remarks: Adopted by IUGG 1924 in Madrid. Based on Hayford 1909/1910 figures.
     *
     * @throws FactoryException if an error occurred while creating the ellipsoid from the EPSG code.
     */
    @Test
    @DisplayName("International 1924")
    public void EPSG_7022() throws FactoryException {
        code              = 7022;
        name              = "International 1924";
        aliases           = new String[] {"Hayford 1909"};
        toMetres          = 1.0;
        semiMajorInMetres = 6378388.0;
        semiMajorAxis     = 6378388.0;
        semiMinorAxis     = Double.NaN;
        inverseFlattening = 297.0;
        verifyEllipsoid();
    }

    /**
     * Tests “International 1924 Authalic Sphere” spheroid creation from the factory.
     *
     * <ul>
     *   <li>EPSG ellipsoid code: <b>7057</b></li>
     *   <li>EPSG ellipsoid name: <b>International 1924 Authalic Sphere</b></li>
     *   <li>Semi-major axis (<var>a</var>): <b>6371228.0 metre</b></li>
     *   <li>Semi-minor axis (<var>b</var>): <b>6371228.0 metre</b></li>
     *   <li>EPSG Usage Extent: <b>Numerous</b></li>
     * </ul>
     *
     * Remarks: Authalic sphere derived from International 1924 ellipsoid (code 7022).
     *
     * @throws FactoryException if an error occurred while creating the ellipsoid from the EPSG code.
     */
    @Test
    @DisplayName("International 1924 Authalic Sphere")
    public void EPSG_7057() throws FactoryException {
        code              = 7057;
        name              = "International 1924 Authalic Sphere";
        toMetres          = 1.0;
        semiMajorInMetres = 6371228.0;
        semiMajorAxis     = 6371228.0;
        semiMinorAxis     = 6371228.0;
        inverseFlattening = Double.NaN;
        isSphere          = true;
        verifyEllipsoid();
    }

    /**
     * Tests “Krassowsky 1940” ellipsoid creation from the factory.
     *
     * <ul>
     *   <li>EPSG ellipsoid code: <b>7024</b></li>
     *   <li>EPSG ellipsoid name: <b>Krassowsky 1940</b></li>
     *   <li>Semi-major axis (<var>a</var>): <b>6378245.0 metre</b></li>
     *   <li>Inverse flattening (1/<var>f</var>): <b>298.3</b></li>
     *   <li>EPSG Usage Extent: <b>Russia; Asia</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the ellipsoid from the EPSG code.
     */
    @Test
    @DisplayName("Krassowsky 1940")
    public void EPSG_7024() throws FactoryException {
        code              = 7024;
        name              = "Krassowsky 1940";
        toMetres          = 1.0;
        semiMajorInMetres = 6378245.0;
        semiMajorAxis     = 6378245.0;
        semiMinorAxis     = Double.NaN;
        inverseFlattening = 298.3;
        verifyEllipsoid();
    }

    /**
     * Tests “NWL 9D” ellipsoid creation from the factory.
     *
     * <ul>
     *   <li>EPSG ellipsoid code: <b>7025</b></li>
     *   <li>EPSG ellipsoid name: <b>NWL 9D</b></li>
     *   <li>Alias(es) given by EPSG: <b>WGS 66</b></li>
     *   <li>Semi-major axis (<var>a</var>): <b>6378145.0 metre</b></li>
     *   <li>Inverse flattening (1/<var>f</var>): <b>298.25</b></li>
     *   <li>EPSG Usage Extent: <b>Numerous</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the ellipsoid from the EPSG code.
     */
    @Test
    @DisplayName("NWL 9D")
    public void EPSG_7025() throws FactoryException {
        code              = 7025;
        name              = "NWL 9D";
        aliases           = new String[] {"WGS 66"};
        toMetres          = 1.0;
        semiMajorInMetres = 6378145.0;
        semiMajorAxis     = 6378145.0;
        semiMinorAxis     = Double.NaN;
        inverseFlattening = 298.25;
        verifyEllipsoid();
    }

    /**
     * Tests “OSU86F” ellipsoid creation from the factory.
     *
     * <ul>
     *   <li>EPSG ellipsoid code: <b>7032</b></li>
     *   <li>EPSG ellipsoid name: <b>OSU86F</b></li>
     *   <li>Semi-major axis (<var>a</var>): <b>6378136.2 metre</b></li>
     *   <li>Inverse flattening (1/<var>f</var>): <b>298.257223563</b></li>
     *   <li>EPSG Usage Extent: <b>Numerous</b></li>
     * </ul>
     *
     * Remarks: Used for OSU86 gravity potential (geoidal) model.
     *
     * @throws FactoryException if an error occurred while creating the ellipsoid from the EPSG code.
     */
    @Test
    @DisplayName("OSU86F")
    public void EPSG_7032() throws FactoryException {
        code              = 7032;
        name              = "OSU86F";
        toMetres          = 1.0;
        semiMajorInMetres = 6378136.2;
        semiMajorAxis     = 6378136.2;
        semiMinorAxis     = Double.NaN;
        inverseFlattening = 298.257223563;
        verifyEllipsoid();
    }

    /**
     * Tests “OSU91A” ellipsoid creation from the factory.
     *
     * <ul>
     *   <li>EPSG ellipsoid code: <b>7033</b></li>
     *   <li>EPSG ellipsoid name: <b>OSU91A</b></li>
     *   <li>Semi-major axis (<var>a</var>): <b>6378136.3 metre</b></li>
     *   <li>Inverse flattening (1/<var>f</var>): <b>298.257223563</b></li>
     *   <li>EPSG Usage Extent: <b>Numerous</b></li>
     * </ul>
     *
     * Remarks: Used for OSU91 gravity potential (geoidal) model.
     *
     * @throws FactoryException if an error occurred while creating the ellipsoid from the EPSG code.
     */
    @Test
    @DisplayName("OSU91A")
    public void EPSG_7033() throws FactoryException {
        code              = 7033;
        name              = "OSU91A";
        toMetres          = 1.0;
        semiMajorInMetres = 6378136.3;
        semiMajorAxis     = 6378136.3;
        semiMinorAxis     = Double.NaN;
        inverseFlattening = 298.257223563;
        verifyEllipsoid();
    }

    /**
     * Tests “Plessis 1817” ellipsoid creation from the factory.
     *
     * <ul>
     *   <li>EPSG ellipsoid code: <b>7027</b></li>
     *   <li>EPSG ellipsoid name: <b>Plessis 1817</b></li>
     *   <li>Semi-major axis (<var>a</var>): <b>6376523.0 metre</b></li>
     *   <li>Inverse flattening (1/<var>f</var>): <b>308.64</b></li>
     *   <li>EPSG Usage Extent: <b>Numerous</b></li>
     * </ul>
     *
     * Remarks: Rescaling of Delambre 1810 figure (a=6376985 m) to make meridional arc from equator to pole equal to 10000000 metres exactly.
     *
     * @throws FactoryException if an error occurred while creating the ellipsoid from the EPSG code.
     */
    @Test
    @DisplayName("Plessis 1817")
    public void EPSG_7027() throws FactoryException {
        code              = 7027;
        name              = "Plessis 1817";
        toMetres          = 1.0;
        semiMajorInMetres = 6376523.0;
        semiMajorAxis     = 6376523.0;
        semiMinorAxis     = Double.NaN;
        inverseFlattening = 308.64;
        verifyEllipsoid();
    }

    /**
     * Tests “Popular Visualisation Sphere” spheroid creation from the factory <em>(deprecated)</em>.
     * This is test is executed only if {@link #isDeprecatedObjectCreationSupported} is {@code true}.
     *
     * <ul>
     *   <li>EPSG ellipsoid code: <b>7059</b></li>
     *   <li>EPSG ellipsoid name: <b>Popular Visualisation Sphere</b></li>
     *   <li>Semi-major axis (<var>a</var>): <b>6378137</b></li>
     *   <li>Semi-minor axis (<var>b</var>): <b>6378137</b></li>
     *   <li><b>Deprecated:</b> IOGP revised its approach to description of Popular Visualisation CRS.</li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the ellipsoid from the EPSG code.
     */
    @Test
    @DisplayName("Popular Visualisation Sphere")
    public void EPSG_7059() throws FactoryException {
        code              = 7059;
        name              = "Popular Visualisation Sphere";
        toMetres          = 1.0;
        semiMajorInMetres = 6378137.0;
        semiMajorAxis     = 6378137.0;
        semiMinorAxis     = 6378137.0;
        inverseFlattening = Double.NaN;
        isSphere          = true;
        assumeTrue(isDeprecatedObjectCreationSupported, "Creation of deprecated objects not supported.");
        verifyEllipsoid();
    }

    /**
     * Tests “PZ-90” ellipsoid creation from the factory.
     *
     * <ul>
     *   <li>EPSG ellipsoid code: <b>7054</b></li>
     *   <li>EPSG ellipsoid name: <b>PZ-90</b></li>
     *   <li>Semi-major axis (<var>a</var>): <b>6378136.0 metre</b></li>
     *   <li>Inverse flattening (1/<var>f</var>): <b>298.257839303</b></li>
     *   <li>EPSG Usage Extent: <b>Russia</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the ellipsoid from the EPSG code.
     */
    @Test
    @DisplayName("PZ-90")
    public void EPSG_7054() throws FactoryException {
        code              = 7054;
        name              = "PZ-90";
        toMetres          = 1.0;
        semiMajorInMetres = 6378136.0;
        semiMajorAxis     = 6378136.0;
        semiMinorAxis     = Double.NaN;
        inverseFlattening = 298.257839303;
        verifyEllipsoid();
    }

    /**
     * Tests “Struve 1860” ellipsoid creation from the factory.
     *
     * <ul>
     *   <li>EPSG ellipsoid code: <b>7028</b></li>
     *   <li>EPSG ellipsoid name: <b>Struve 1860</b></li>
     *   <li>Semi-major axis (<var>a</var>): <b>6378298.3 metre</b></li>
     *   <li>Inverse flattening (1/<var>f</var>): <b>294.73</b></li>
     *   <li>EPSG Usage Extent: <b>Numerous</b></li>
     * </ul>
     *
     * Remarks: Original definition of semi-major axis given as 3272539 toise.
     *
     * @throws FactoryException if an error occurred while creating the ellipsoid from the EPSG code.
     */
    @Test
    @DisplayName("Struve 1860")
    public void EPSG_7028() throws FactoryException {
        code              = 7028;
        name              = "Struve 1860";
        toMetres          = 1.0;
        semiMajorInMetres = 6378298.3;
        semiMajorAxis     = 6378298.3;
        semiMinorAxis     = Double.NaN;
        inverseFlattening = 294.73;
        verifyEllipsoid();
    }

    /**
     * Tests “War Office” ellipsoid creation from the factory.
     *
     * <ul>
     *   <li>EPSG ellipsoid code: <b>7029</b></li>
     *   <li>EPSG ellipsoid name: <b>War Office</b></li>
     *   <li>Alias(es) given by EPSG: <b>McCaw 1924</b></li>
     *   <li>Semi-major axis (<var>a</var>): <b>6378300.0 metre</b></li>
     *   <li>Inverse flattening (1/<var>f</var>): <b>296</b></li>
     *   <li>EPSG Usage Extent: <b>Ghana</b></li>
     * </ul>
     *
     * Remarks: In non-metric form.
     * A=20926201 Gold Coast feet.
     * DMA Technical Manual 8358.1 and data derived from this quotes value for semi-major axis as 6378300.58m:
     * IOGP recommends use of defined value 6378300m exactly.
     *
     * @throws FactoryException if an error occurred while creating the ellipsoid from the EPSG code.
     */
    @Test
    @DisplayName("War Office")
    public void EPSG_7029() throws FactoryException {
        code              = 7029;
        name              = "War Office";
        aliases           = new String[] {"McCaw 1924"};
        toMetres          = 1.0;
        semiMajorInMetres = 6378300.0;
        semiMajorAxis     = 6378300.0;
        semiMinorAxis     = Double.NaN;
        inverseFlattening = 296.0;
        verifyEllipsoid();
    }

    /**
     * Tests “WGS 72” ellipsoid creation from the factory.
     *
     * <ul>
     *   <li>EPSG ellipsoid code: <b>7043</b></li>
     *   <li>EPSG ellipsoid name: <b>WGS 72</b></li>
     *   <li>Alias(es) given by EPSG: <b>NWL 10D</b></li>
     *   <li>Semi-major axis (<var>a</var>): <b>6378135.0 metre</b></li>
     *   <li>Inverse flattening (1/<var>f</var>): <b>298.26</b></li>
     *   <li>EPSG Usage Extent: <b>Numerous</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the ellipsoid from the EPSG code.
     */
    @Test
    @DisplayName("WGS 72")
    public void EPSG_7043() throws FactoryException {
        code              = 7043;
        name              = "WGS 72";
        aliases           = new String[] {"NWL 10D"};
        toMetres          = 1.0;
        semiMajorInMetres = 6378135.0;
        semiMajorAxis     = 6378135.0;
        semiMinorAxis     = Double.NaN;
        inverseFlattening = 298.26;
        verifyEllipsoid();
    }

    /**
     * Tests “WGS 84” ellipsoid creation from the factory.
     *
     * <ul>
     *   <li>EPSG ellipsoid code: <b>7030</b></li>
     *   <li>EPSG ellipsoid name: <b>WGS 84</b></li>
     *   <li>Alias(es) given by EPSG: <b>WGS84</b></li>
     *   <li>Semi-major axis (<var>a</var>): <b>6378137.0 metre</b></li>
     *   <li>Inverse flattening (1/<var>f</var>): <b>298.257223563</b></li>
     *   <li>EPSG Usage Extent: <b>Numerous</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the ellipsoid from the EPSG code.
     */
    @Test
    @DisplayName("WGS 84")
    public void EPSG_7030() throws FactoryException {
        code              = 7030;
        name              = "WGS 84";
        aliases           = new String[] {"WGS84"};
        toMetres          = 1.0;
        semiMajorInMetres = 6378137.0;
        semiMajorAxis     = 6378137.0;
        semiMinorAxis     = Double.NaN;
        inverseFlattening = 298.257223563;
        verifyEllipsoid();
    }
}
