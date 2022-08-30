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
import javax.measure.quantity.Angle;
import org.opengis.util.FactoryException;
import org.opengis.referencing.datum.PrimeMeridian;
import org.opengis.referencing.datum.DatumFactory;
import org.opengis.referencing.cs.CSAuthorityFactory;
import org.iogp.gigs.internal.geoapi.Configuration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Verifies that the software allows correct definition of a user-defined prime meridian.
 *
 * <table class="gigs">
 * <caption>Test description</caption>
 * <tr>
 *   <th>Test method:</th>
 *   <td>Create user-defined prime meridian for each of several different prime meridians.</td>
 * </tr><tr>
 *   <th>Test data:</th>
 *   <td><a href="https://github.com/IOGP-GIGS/GIGSTestDataset/blob/main/GIGSTestDatasetFiles/GIGS%203200%20User-defined%20Geodetic%20Data%20Objects%20test%20data/ASCII/GIGS_user_3203_PrimeMeridian.txt">{@code GIGS_user_3203_PrimeMeridian.txt}</a>
 * </tr><tr>
 *   <th>Tested API:</th>
 *   <td>{@link DatumFactory#createPrimeMeridian(Map, double, Unit)}.</td>
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
 * public class MyTest extends Test3203 {
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
@DisplayName("User-defined prime meridian")
public class Test3203 extends Series3000<PrimeMeridian> {
    /**
     * The prime meridian Greenwich longitude, in decimal degrees.
     * This field is set by all test methods before to create and verify the {@link PrimeMeridian} instance.
     */
    public double longitudeInDegrees;

    /**
     * The prime meridian Greenwich longitude, in unit of {@link #angularUnit}.
     * This field is set by all test methods before to create and verify the {@link PrimeMeridian} instance.
     */
    public double greenwichLongitude;

    /**
     * The unit of measurement of {@link #greenwichLongitude}.
     * This field is set by all test methods before to create and verify the {@link PrimeMeridian} instance.
     */
    public Unit<Angle> angularUnit;

    /**
     * The prime meridian created by the factory,
     * or {@code null} if not yet created or if the prime meridian creation failed.
     *
     * @see #datumFactory
     */
    private PrimeMeridian primeMeridian;

    /**
     * Factory to use for building {@link PrimeMeridian} instances, or {@code null} if none.
     * This is the factory used by the {@link #getIdentifiedObject()} method.
     */
    protected final DatumFactory datumFactory;

    /**
     * Factory to use for sexagesimal units, or {@code null} if none.
     * This is used only if the test needs a sexagesimal unit.
     *
     * @see CSAuthorityFactory#createUnit(String)
     */
    protected final CSAuthorityFactory unitFactory;

    /**
     * Creates a new test using the given factory. If a given factory is {@code null},
     * then the tests which depend on it will be skipped.
     *
     * <h4>Authority factory usage</h4>
     * The coordinate system factory is used only if the test needs a sexagesimal unit,
     * because the standard {@link javax.measure.spi.SystemOfUnits} API can not create them.
     * If needed, the EPSG code used is 9110.
     *
     * @param datumFactory  factory for creating {@link PrimeMeridian} instances.
     * @param unitFactory   the factory to use for sexagesimal units, or {@code null} if none.
     */
    public Test3203(final DatumFactory datumFactory, final CSAuthorityFactory unitFactory) {
        this.datumFactory = datumFactory;
        this.unitFactory  = unitFactory;
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
     *       <li>{@link #unitFactory}</li>
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
        assertNull(op.put(Configuration.Key.csAuthorityFactory, unitFactory));
        return op;
    }

    /**
     * Sets the prime meridian instance to verify. This method is invoked only by other test classes
     * which need to verify the prime meridian contained in a geodetic datum instead of the prime
     * meridian immediately after creation.
     *
     * @param  instance  the instance to verify.
     */
    final void setIdentifiedObject(final PrimeMeridian instance) {
        primeMeridian = instance;
    }

    /**
     * Returns the prime meridian instance to be tested. When this method is invoked for the first time,
     * it creates the prime meridian to test by invoking the corresponding method from {@link DatumFactory}
     * with the current {@link #properties properties} map in argument.
     * The created object is then cached and returned in all subsequent invocations of this method.
     *
     * @return the prime meridian instance to test.
     * @throws FactoryException if an error occurred while creating the prime meridian instance.
     */
    @Override
    public PrimeMeridian getIdentifiedObject() throws FactoryException {
        if (primeMeridian == null) {
            assumeNotNull(datumFactory);
            primeMeridian = datumFactory.createPrimeMeridian(properties, greenwichLongitude, angularUnit);
        }
        return primeMeridian;
    }

    /**
     * Sets {@link #angularUnit} to a sexagesimal units if supported, or use decimal degrees otherwise.
     */
    private void setSexagesimalUnit(final int code) {
        if (unitFactory != null) try {
            angularUnit = unitFactory.createUnit(String.valueOf(code)).asType(Angle.class);
            return;
        } catch (FactoryException e) {
            // Ignore and fallback on decimal degrees.
        }
        greenwichLongitude = longitudeInDegrees;
        angularUnit = units.degree();
    }

    /**
     * Verifies the properties of the prime meridian given by {@link #getIdentifiedObject()}.
     *
     * @throws FactoryException if an error occurred while creating the prime meridian.
     */
    final void verifyPrimeMeridian() throws FactoryException {
        if (skipTests) {
            return;
        }
        final String name = getName();
        final String code = getCode();
        final PrimeMeridian primeMeridian = getIdentifiedObject();
        assertNotNull(primeMeridian, "PrimeMeridian");
        validators.validate(primeMeridian);
        /*
         * If the implementation supports storing the value as specified by the test, check for an exact match.
         */
        if (isFactoryPreservingUserValues) {
            configurationTip = Configuration.Key.isFactoryPreservingUserValues;
            assertEquals(angularUnit, primeMeridian.getAngularUnit(), "PrimeMeridian.getAngularUnit()");
            assertEquals(greenwichLongitude, primeMeridian.getGreenwichLongitude(), ANGULAR_TOLERANCE, "PrimeMeridian.getGreenwichLongitude()");
            configurationTip = null;
        }
        /*
         * Verify Greenwich value by applying conversion to a fixed unit of measurement.
         * The value does not need to be in the unit of measurement specified by the test.
         */
        verifyIdentification(primeMeridian, name, code);
        verifyPrimeMeridian(primeMeridian, name, greenwichLongitude, angularUnit);
        assertEquals(longitudeInDegrees,
                     primeMeridian.getAngularUnit().getConverterTo(units.degree()).convert(primeMeridian.getGreenwichLongitude()),
                     ANGULAR_TOLERANCE);
    }

    /**
     * Tests “GIGS PM A” prime meridian creation from the factory.
     *
     * <ul>
     *   <li>GIGS prime meridian code: <b>68901</b></li>
     *   <li>GIGS prime meridian name: <b>GIGS PM A</b></li>
     *   <li>EPSG equivalence: <b>8901 – Greenwich</b></li>
     *   <li>Greenwich longitude: <b>0 degree</b></li>
     * </ul>
     *
     * Remarks: International reference meridian as defined by IERS.
     *
     * @throws FactoryException if an error occurred while creating the prime meridian from the properties.
     *
     * @see Test2203#EPSG_8901()
     */
    @Test
    @DisplayName("GIGS PM A")
    public void GIGS_68901() throws FactoryException {
        setCodeAndName(68901, "GIGS PM A");
        longitudeInDegrees = 0.0;
        greenwichLongitude = 0.0;
        angularUnit        = units.degree();
        verifyPrimeMeridian();
    }

    /**
     * Tests “GIGS PM D” prime meridian creation from the factory.
     *
     * <ul>
     *   <li>GIGS prime meridian code: <b>68908</b></li>
     *   <li>GIGS prime meridian name: <b>GIGS PM D</b></li>
     *   <li>EPSG equivalence: <b>8908 – Jakarta</b></li>
     *   <li>Greenwich longitude: <b>106.482779 sexagesimal DMS (106.807719444444°)</b></li>
     * </ul>
     *
     * Remarks: 1924 determination. Supersedes 1910 value of 106 48 37.05 E of Greenwich.
     *
     * @throws FactoryException if an error occurred while creating the prime meridian from the properties.
     *
     * @see Test2203#EPSG_8908()
     */
    @Test
    @DisplayName("GIGS PM D")
    public void GIGS_68908() throws FactoryException {
        setCodeAndName(68908, "GIGS PM D");
        longitudeInDegrees = 106.807719444444;
        greenwichLongitude = 106.482779;
        setSexagesimalUnit(9110);
        verifyPrimeMeridian();
    }

    /**
     * Tests “GIGS PM H” prime meridian creation from the factory.
     *
     * <ul>
     *   <li>GIGS prime meridian code: <b>68903</b></li>
     *   <li>GIGS prime meridian name: <b>GIGS PM H</b></li>
     *   <li>EPSG equivalence: <b>8903 – Paris</b></li>
     *   <li>Greenwich longitude: <b>2.5969213 grad (2.33722917°)</b></li>
     * </ul>
     *
     * Remarks: Equivalent to 2 20 14.025.
     *
     * @throws FactoryException if an error occurred while creating the prime meridian from the properties.
     *
     * @see Test2203#EPSG_8903()
     */
    @Test
    @DisplayName("GIGS PM H")
    public void GIGS_68903() throws FactoryException {
        setCodeAndName(68903, "GIGS PM H");
        longitudeInDegrees = 2.33722917;
        greenwichLongitude = 2.5969213;
        angularUnit        = units.grad();
        verifyPrimeMeridian();
    }

    /**
     * Tests “GIGS PM I” prime meridian creation from the factory.
     *
     * <ul>
     *   <li>GIGS prime meridian code: <b>68904</b></li>
     *   <li>GIGS prime meridian name: <b>GIGS PM I</b></li>
     *   <li>EPSG equivalence: <b>8904 – Bogota</b></li>
     *   <li>Greenwich longitude: <b>-74.04513 sexagesimal DMS (-74.08091666667°)</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the prime meridian from the properties.
     *
     * @see Test2203#EPSG_8904()
     */
    @Test
    @DisplayName("GIGS PM I")
    public void GIGS_68904() throws FactoryException {
        setCodeAndName(68904, "GIGS PM I");
        longitudeInDegrees = -74.08091666667;
        greenwichLongitude = -74.04513;
        setSexagesimalUnit(9110);
        verifyPrimeMeridian();
    }
}
