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
import org.opengis.referencing.datum.DatumAuthorityFactory;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.datum.PrimeMeridian;
import org.iogp.gigs.internal.geoapi.Configuration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Verifies reference prime meridians bundled with the geoscience software.
 *
 * <table class="gigs">
 * <caption>Test description</caption>
 * <tr>
 *   <th>Test method:</th>
 *   <td>Compare prime meridian definitions included in the software against the EPSG Dataset.</td>
 * </tr><tr>
 *   <th>Test data:</th>
 *   <td><a href="doc-files/GIGS_2003_libPrimeMeridian.csv">{@code GIGS_2003_libPrimeMeridian.csv}</a>
 *       and EPSG Dataset.</td>
 * </tr><tr>
 *   <th>Tested API:</th>
 *   <td>{@link DatumAuthorityFactory#createPrimeMeridian(String)}.</td>
 * </tr><tr>
 *   <th>Expected result:</th>
 *   <td>Prime meridian definitions bundled with the software should have the same name and Greenwich Longitude
 *       as in the EPSG Dataset. Equivalent alternative units are acceptable but should be reported.
 *       The values of the Greenwich Longitude should be correct to at least 7 decimal places (of degrees or grads).
 *       Meridians missing from the software or at variance with those in the EPSG Dataset should be reported.</td>
 * </tr></table>
 *
 *
 * <h2>Usage example</h2>
 * in order to specify their factories and run the tests in a JUnit framework,
 * implementers can define a subclass in their own test suite as in the example below:
 *
 * <blockquote><pre>public class MyTest extends Test2003 {
 *    public MyTest() {
 *        super(new MyDatumAuthorityFactory());
 *    }
 *}</pre></blockquote>
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @author  Alexis Manin (Geomatys)
 * @version 1.0
 * @since   1.0
 */
public class Test2003 extends Series2000<PrimeMeridian> {
    /**
     * The expected Greenwich longitude in decimal degrees.
     * This field is set by all test methods before to create and verify the {@link PrimeMeridian} instance.
     */
    public double greenwichLongitude;

    /**
     * The prime meridian created by the factory,
     * or {@code null} if not yet created or if the prime meridian creation failed.
     *
     * @see #datumAuthorityFactory
     */
    private PrimeMeridian primeMeridian;

    /**
     * Factory to use for building {@link PrimeMeridian} instances, or {@code null} if none.
     * This is the factory used by the {@link #getIdentifiedObject()} method.
     */
    protected final DatumAuthorityFactory datumAuthorityFactory;

    /**
     * Creates a new test using the given factory. If a given factory is {@code null},
     * then the tests which depend on it will be skipped.
     *
     * @param datumFactory  factory for creating {@link PrimeMeridian} instances.
     */
    public Test2003(final DatumAuthorityFactory datumFactory) {
        datumAuthorityFactory = datumFactory;
    }

    /**
     * Returns information about the configuration of the test which has been run.
     * This method returns a map containing:
     *
     * <ul>
     *   <li>All the following values associated to the {@link org.opengis.test.Configuration.Key} of the same name:
     *     <ul>
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
     * Returns the prime meridian instance to be tested. When this method is invoked for the first time, it creates the
     * prime meridian to test by invoking the {@link DatumAuthorityFactory#createPrimeMeridian(String)} method with the
     * current {@link #code} value in argument. The created object is then cached and returned in all subsequent
     * invocations of this method.
     *
     * @return the prime meridian instance to test.
     * @throws FactoryException if an error occurred while creating the prime meridian instance.
     */
    @Override
    public PrimeMeridian getIdentifiedObject() throws FactoryException {
        if (primeMeridian == null) {
            assumeNotNull(datumAuthorityFactory);
            try {
                primeMeridian = datumAuthorityFactory.createPrimeMeridian(String.valueOf(code));
            } catch (NoSuchAuthorityCodeException e) {
                unsupportedCode(PrimeMeridian.class, code);
                throw e;
            }
        }
        return primeMeridian;
    }

    /**
     * Verifies the properties of the prime meridian given by {@link #getIdentifiedObject()}.
     *
     * @throws FactoryException if an error occurred while creating the prime meridian.
     */
    private void verifyPrimeMeridian() throws FactoryException {
        final PrimeMeridian pm = getIdentifiedObject();
        assertNotNull(pm, "PrimeMeridian");
        validators.validate(pm);

        // Prime meridian identifiers.
        assertContainsCode("PrimeMeridian.getIdentifiers()", "EPSG", code, pm.getIdentifiers());

        // Prime meridian name.
        if (isStandardNameSupported) {
            configurationTip = Configuration.Key.isStandardNameSupported;
            assertEquals(name, getVerifiableName(pm), "PrimeMeridian.getName()");
            configurationTip = null;
        }

        // Prime meridian alias.
        if (isStandardAliasSupported) {
            configurationTip = Configuration.Key.isStandardAliasSupported;
            assertContainsAll("PrimeMeridian.getAlias()", aliases, pm.getAlias());
            configurationTip = null;
        }
        /*
         * Before to compare the Greenwich longitude, convert the expected angular value from decimal degrees
         * to the units actually used by the implementation. We do the conversion that way rather than the
         * opposite way in order to have a more appropriate error message in case of failure.
         */
        final Unit<Angle> unit = pm.getAngularUnit();
        double longitude = greenwichLongitude;
        final Unit<Angle> degree = units.degree();
        if (unit != null && !unit.equals(degree)) {
            longitude = degree.getConverterTo(unit).convert(longitude);
        }
        assertEquals(longitude, pm.getGreenwichLongitude(), ANGULAR_TOLERANCE, "PrimeMeridian.getGreenwichLongitude()");
    }

    /**
     * Tests “Athens” prime meridian creation from the factory.
     *
     * <ul>
     *   <li>EPSG prime meridian code: <b>8912</b></li>
     *   <li>EPSG prime meridian name: <b>Athens</b></li>
     *   <li>Greenwich longitude: <b>23°42′58.815″</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the prime meridian from the EPSG code.
     */
    @Test
    public void testAthens() throws FactoryException {
        code               = 8912;
        name               = "Athens";
        aliases            = NONE;
        greenwichLongitude = 23.7163375;
        verifyPrimeMeridian();
    }

    /**
     * Tests “Bern” prime meridian creation from the factory.
     *
     * <ul>
     *   <li>EPSG prime meridian code: <b>8907</b></li>
     *   <li>EPSG prime meridian name: <b>Bern</b></li>
     *   <li>Greenwich longitude: <b>7°26′22.5″</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the prime meridian from the EPSG code.
     */
    @Test
    public void testBern() throws FactoryException {
        code               = 8907;
        name               = "Bern";
        aliases            = NONE;
        greenwichLongitude = 7.439583333333333;
        verifyPrimeMeridian();
    }

    /**
     * Tests “Bogota” prime meridian creation from the factory.
     *
     * <ul>
     *   <li>EPSG prime meridian code: <b>8904</b></li>
     *   <li>EPSG prime meridian name: <b>Bogota</b></li>
     *   <li>Greenwich longitude: <b>-74°04′51.3″</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the prime meridian from the EPSG code.
     *
     * @see Test3003#testBogota()
     */
    @Test
    public void testBogota() throws FactoryException {
        code               = 8904;
        name               = "Bogota";
        aliases            = NONE;
        greenwichLongitude = -74.08091666666667;
        verifyPrimeMeridian();
    }

    /**
     * Tests “Brussels” prime meridian creation from the factory.
     *
     * <ul>
     *   <li>EPSG prime meridian code: <b>8910</b></li>
     *   <li>EPSG prime meridian name: <b>Brussels</b></li>
     *   <li>Greenwich longitude: <b>4°22′04.71″</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the prime meridian from the EPSG code.
     */
    @Test
    public void testBrussels() throws FactoryException {
        code               = 8910;
        name               = "Brussels";
        aliases            = NONE;
        greenwichLongitude = 4.367975;
        verifyPrimeMeridian();
    }

    /**
     * Tests “Ferro” prime meridian creation from the factory.
     *
     * <ul>
     *   <li>EPSG prime meridian code: <b>8909</b></li>
     *   <li>EPSG prime meridian name: <b>Ferro</b></li>
     *   <li>Greenwich longitude: <b>-17°40′</b></li>
     *   <li>Particularly important to E&amp;P industry.</li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the prime meridian from the EPSG code.
     */
    @Test
    public void testFerro() throws FactoryException {
        important          = true;
        code               = 8909;
        name               = "Ferro";
        aliases            = NONE;
        greenwichLongitude = -17.666666666666668;
        verifyPrimeMeridian();
    }

    /**
     * Tests “Greenwich” prime meridian creation from the factory.
     *
     * <ul>
     *   <li>EPSG prime meridian code: <b>8901</b></li>
     *   <li>EPSG prime meridian name: <b>Greenwich</b></li>
     *   <li>Greenwich longitude: <b>0°</b></li>
     *   <li>Particularly important to E&amp;P industry.</li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the prime meridian from the EPSG code.
     *
     * @see Test3003#testGreenwich()
     */
    @Test
    public void testGreenwich() throws FactoryException {
        important          = true;
        code               = 8901;
        name               = "Greenwich";
        aliases            = NONE;
        greenwichLongitude = 0.0;
        verifyPrimeMeridian();
    }

    /**
     * Tests “Jakarta” prime meridian creation from the factory.
     *
     * <ul>
     *   <li>EPSG prime meridian code: <b>8908</b></li>
     *   <li>EPSG prime meridian name: <b>Jakarta</b></li>
     *   <li>Greenwich longitude: <b>106°48′27.79″</b></li>
     *   <li>Particularly important to E&amp;P industry.</li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the prime meridian from the EPSG code.
     *
     * @see Test3003#testJakarta()
     */
    @Test
    public void testJakarta() throws FactoryException {
        important          = true;
        code               = 8908;
        name               = "Jakarta";
        aliases            = NONE;
        greenwichLongitude = 106.80771944444444;
        verifyPrimeMeridian();
    }

    /**
     * Tests “Lisbon” prime meridian creation from the factory.
     *
     * <ul>
     *   <li>EPSG prime meridian code: <b>8902</b></li>
     *   <li>EPSG prime meridian name: <b>Lisbon</b></li>
     *   <li>Greenwich longitude: <b>-9°07′54.862″</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the prime meridian from the EPSG code.
     */
    @Test
    public void testLisbon() throws FactoryException {
        code               = 8902;
        name               = "Lisbon";
        aliases            = NONE;
        greenwichLongitude = -9.13190611111111;
        verifyPrimeMeridian();
    }

    /**
     * Tests “Madrid” prime meridian creation from the factory.
     *
     * <ul>
     *   <li>EPSG prime meridian code: <b>8905</b></li>
     *   <li>EPSG prime meridian name: <b>Madrid</b></li>
     *   <li>Greenwich longitude: <b>-3°41′14.55″</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the prime meridian from the EPSG code.
     */
    @Test
    public void testMadrid() throws FactoryException {
        code               = 8905;
        name               = "Madrid";
        aliases            = NONE;
        greenwichLongitude = -3.687375;
        verifyPrimeMeridian();
    }

    /**
     * Tests “Oslo” prime meridian creation from the factory.
     *
     * <ul>
     *   <li>EPSG prime meridian code: <b>8913</b></li>
     *   <li>EPSG prime meridian name: <b>Oslo</b></li>
     *   <li>Alias(es) given by EPSG: <b>Kristiania</b></li>
     *   <li>Greenwich longitude: <b>10°43′22.5″</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the prime meridian from the EPSG code.
     */
    @Test
    public void testOslo() throws FactoryException {
        code               = 8913;
        name               = "Oslo";
        aliases            = new String[] {"Kristiania"};
        greenwichLongitude = 10.722916666666666;
        verifyPrimeMeridian();
    }

    /**
     * Tests “Paris” prime meridian creation from the factory.
     *
     * <ul>
     *   <li>EPSG prime meridian code: <b>8903</b></li>
     *   <li>EPSG prime meridian name: <b>Paris</b></li>
     *   <li>Greenwich longitude: <b>2.5969213</b></li>
     *   <li>Specific usage / Remarks: <b>Equivalent to 2°20'14.025\".</b></li>
     *   <li>Particularly important to E&amp;P industry.</li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the prime meridian from the EPSG code.
     *
     * @see Test3003#testParis()
     */
    @Test
    public void testParis() throws FactoryException {
        important          = true;
        code               = 8903;
        name               = "Paris";
        aliases            = NONE;
        greenwichLongitude = 2.33722917;
        verifyPrimeMeridian();
    }

    /**
     * Tests “Paris RGS” prime meridian creation from the factory.
     *
     * <ul>
     *   <li>EPSG prime meridian code: <b>8914</b></li>
     *   <li>EPSG prime meridian name: <b>Paris RGS</b></li>
     *   <li>Greenwich longitude: <b>2°20′13.95″</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the prime meridian from the EPSG code.
     */
    @Test
    public void testParisRGS() throws FactoryException {
        code               = 8914;
        name               = "Paris RGS";
        aliases            = NONE;
        greenwichLongitude = 2.3372083333333333;
        verifyPrimeMeridian();
    }

    /**
     * Tests “Rome” prime meridian creation from the factory.
     *
     * <ul>
     *   <li>EPSG prime meridian code: <b>8906</b></li>
     *   <li>EPSG prime meridian name: <b>Rome</b></li>
     *   <li>Greenwich longitude: <b>12°27′08.4″</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the prime meridian from the EPSG code.
     */
    @Test
    public void testRome() throws FactoryException {
        code               = 8906;
        name               = "Rome";
        aliases            = NONE;
        greenwichLongitude = 12.452333333333334;
        verifyPrimeMeridian();
    }

    /**
     * Tests “Stockholm” prime meridian creation from the factory.
     *
     * <ul>
     *   <li>EPSG prime meridian code: <b>8911</b></li>
     *   <li>EPSG prime meridian name: <b>Stockholm</b></li>
     *   <li>Greenwich longitude: <b>18°03′29.8″</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the prime meridian from the EPSG code.
     */
    @Test
    public void testStockholm() throws FactoryException {
        code               = 8911;
        name               = "Stockholm";
        aliases            = NONE;
        greenwichLongitude = 18.05827777777778;
        verifyPrimeMeridian();
    }
}
