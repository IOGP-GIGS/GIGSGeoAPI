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
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.datum.DatumAuthorityFactory;
import org.opengis.referencing.datum.Ellipsoid;
import org.opengis.referencing.datum.GeodeticDatum;
import org.opengis.referencing.datum.PrimeMeridian;
import org.opengis.util.FactoryException;


/**
 * Verifies reference geodetic datums and CRSs bundled with the geoscience software.
 * Each test method in this class instantiate exactly one {@link GeodeticDatum}.
 *
 * <table class="gigs">
 * <caption>Test description</caption>
 * <tr>
 *   <th>Test method:</th>
 *   <td>Compare geodetic datum definitions included in the geoscience software against the EPSG Dataset.</td>
 * </tr><tr>
 *   <th>Test data:</th>
 *   <td><a href="doc-files/GIGS_lib_2204_GeodeticDatum.txt">{@code GIGS_lib_2204_GeodeticDatum.txt}</a>
 *       and EPSG Dataset.
 *       Tests for component logical consistency: for example, if a higher-level library-defined component
 *       such as ED50 datum is selected it should then not be possible to change any of its lower-level
 *       components such as the ellipsoid from the pre-defined value (in this example International 1924).</td>
 * </tr><tr>
 *   <th>Tested API:</th>
 *   <td>{@link DatumAuthorityFactory#createGeodeticDatum(String)}.</td>
 * </tr><tr>
 *   <th>Expected result:</th>
 *   <td>Definitions bundled with the software should have the same name and associated ellipsoid and prime meridian
 *       as in the EPSG Dataset.</td>
 * </tr></table>
 *
 *
 * <h2>Usage example</h2>
 * in order to specify their factories and run the tests in a JUnit framework,
 * implementers can define a subclass in their own test suite as in the example below:
 *
 * <blockquote><pre>public class MyTest extends Test2204 {
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
@DisplayName("Geodetic datum")
public class Test2204 extends Series2000<GeodeticDatum> {
    /**
     * The name of the expected ellipsoid.
     * This field is set by all test methods before to create and verify the {@link Ellipsoid} instance.
     */
    public String ellipsoidName;

    /**
     * The name of the expected prime meridian.
     * This field is set by all test methods before to create and verify the {@link PrimeMeridian} instance.
     */
    public String primeMeridianName;

    /**
     * The datum created by the factory,
     * or {@code null} if not yet created or if datum creation failed.
     *
     * @see #datumAuthorityFactory
     */
    private GeodeticDatum datum;

    /**
     * Factory to use for building {@link GeodeticDatum} instances, or {@code null} if none.
     * This is the factory used by the {@link #getIdentifiedObject()} method.
     */
    protected final DatumAuthorityFactory datumAuthorityFactory;

    /**
     * Creates a new test using the given factories. If a given factory is {@code null},
     * then the tests which depend on it will be skipped.
     *
     * @param datumFactory  factory for creating {@link GeodeticDatum} instances.
     */
    public Test2204(final DatumAuthorityFactory datumFactory) {
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
     * Returns the datum instance to be tested. When this method is invoked for the first time, it creates the
     * datum to test by invoking the {@link DatumAuthorityFactory#createGeodeticDatum(String)} method with the
     * current {@link #code} value in argument. The created object is then cached and returned in all subsequent
     * invocations of this method.
     *
     * @return the datum instance to test.
     * @throws FactoryException if an error occurred while creating the datum instance.
     */
    @Override
    public GeodeticDatum getIdentifiedObject() throws FactoryException {
        if (datum == null) {
            assumeNotNull(datumAuthorityFactory);
            try {
                datum = datumAuthorityFactory.createGeodeticDatum(String.valueOf(code));
            } catch (NoSuchAuthorityCodeException e) {
                unsupportedCode(GeodeticDatum.class, code);
                throw e;
            }
        }
        return datum;
    }

    /**
     * Verifies the properties of the geodetic datum given by {@link #getIdentifiedObject()}.
     *
     * @throws FactoryException if an error occurred while creating the datum.
     */
    private void verifyDatum() throws FactoryException {
        final GeodeticDatum datum = getIdentifiedObject();
        assertNotNull(datum, "GeodeticDatum");
        validators.validate(datum);

        assertContainsCode("GeodeticDatum.getIdentifiers()", "EPSG", code, datum.getIdentifiers());
        if (isStandardNameSupported) {
            configurationTip = Configuration.Key.isStandardNameSupported;
            assertEquals(name, getVerifiableName(datum), "GeodeticDatum.getName()");
        }

        // Geodetic datum ellipsoid.
        final Ellipsoid e = datum.getEllipsoid();
        assertNotNull(e, "GeodeticDatum.getEllipsoid()");

        // Ellipsoid name.
        if (isDependencyIdentificationSupported && isStandardNameSupported) {
            configurationTip = Configuration.Key.isDependencyIdentificationSupported;
            assertEquals(ellipsoidName, getVerifiableName(e), "GeodeticDatum.getEllipsoid().getName()");
            configurationTip = null;
        }

        // Geodetic datum prime meridian.
        final PrimeMeridian pm = datum.getPrimeMeridian();
        assertNotNull(pm, "GeodeticDatum.getPrimeMeridian()");

        // Prime meridian name.
        if (isDependencyIdentificationSupported && isStandardNameSupported) {
            configurationTip = Configuration.Key.isDependencyIdentificationSupported;
            assertEquals(primeMeridianName, getVerifiableName(pm), "GeodeticDatum.getPrimeMeridian().getName()");
            configurationTip = null;
        }
    }

    /**
     * Tests “Abidjan 1987” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6143</b></li>
     *   <li>EPSG datum name: <b>Abidjan 1987</b></li>
     *   <li>Alias(es) given by EPSG: <b>Côte d'Ivoire (Ivory Coast)</b></li>
     *   <li>Ellipsoid name: <b>Clarke 1880 (RGS)</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Cote d'Ivoire (Ivory Coast)</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Abidjan 1987")
    public void EPSG_6143() throws FactoryException {
        code              = 6143;
        name              = "Abidjan 1987";
        aliases           = new String[] {"Côte d'Ivoire (Ivory Coast)"};
        ellipsoidName     = "Clarke 1880 (RGS)";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Accra” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6168</b></li>
     *   <li>EPSG datum name: <b>Accra</b></li>
     *   <li>Ellipsoid name: <b>War Office</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Ghana</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Accra")
    public void EPSG_6168() throws FactoryException {
        code              = 6168;
        name              = "Accra";
        ellipsoidName     = "War Office";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Adindan” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6201</b></li>
     *   <li>EPSG datum name: <b>Adindan</b></li>
     *   <li>Alias(es) given by EPSG: <b>Blue Nile 1958</b></li>
     *   <li>Ellipsoid name: <b>Clarke 1880 (RGS)</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Africa - Eritrea; Ethiopia; South Sudan and Sudan</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Adindan")
    public void EPSG_6201() throws FactoryException {
        code              = 6201;
        name              = "Adindan";
        aliases           = new String[] {"Blue Nile 1958"};
        ellipsoidName     = "Clarke 1880 (RGS)";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Afgooye” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6205</b></li>
     *   <li>EPSG datum name: <b>Afgooye</b></li>
     *   <li>Ellipsoid name: <b>Krassowsky 1940</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Somalia - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Afgooye")
    public void EPSG_6205() throws FactoryException {
        code              = 6205;
        name              = "Afgooye";
        ellipsoidName     = "Krassowsky 1940";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Agadez” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6206</b></li>
     *   <li>EPSG datum name: <b>Agadez</b></li>
     *   <li>Ellipsoid name: <b>Clarke 1880 (IGN)</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Niger</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Agadez")
    public void EPSG_6206() throws FactoryException {
        code              = 6206;
        name              = "Agadez";
        ellipsoidName     = "Clarke 1880 (IGN)";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Ain el Abd 1970” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6204</b></li>
     *   <li>EPSG datum name: <b>Ain el Abd 1970</b></li>
     *   <li>Alias(es) given by EPSG: <b>Ain el Abd</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Asia - Middle East - Bahrain; Kuwait and Saudi Arabia</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Ain el Abd 1970")
    public void EPSG_6204() throws FactoryException {
        code              = 6204;
        name              = "Ain el Abd 1970";
        aliases           = new String[] {"Ain el Abd"};
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Albanian 1987” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6191</b></li>
     *   <li>EPSG datum name: <b>Albanian 1987</b></li>
     *   <li>Alias(es) given by EPSG: <b>ALB86</b></li>
     *   <li>Ellipsoid name: <b>Krassowsky 1940</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Albania - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Albanian 1987")
    public void EPSG_6191() throws FactoryException {
        code              = 6191;
        name              = "Albanian 1987";
        aliases           = new String[] {"ALB86"};
        ellipsoidName     = "Krassowsky 1940";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “American Samoa 1962” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6169</b></li>
     *   <li>EPSG datum name: <b>American Samoa 1962</b></li>
     *   <li>Ellipsoid name: <b>Clarke 1866</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>American Samoa - 2 main island groups</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("American Samoa 1962")
    public void EPSG_6169() throws FactoryException {
        code              = 6169;
        name              = "American Samoa 1962";
        ellipsoidName     = "Clarke 1866";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Amersfoort” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6289</b></li>
     *   <li>EPSG datum name: <b>Amersfoort</b></li>
     *   <li>Ellipsoid name: <b>Bessel 1841</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Netherlands - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Amersfoort")
    public void EPSG_6289() throws FactoryException {
        code              = 6289;
        name              = "Amersfoort";
        ellipsoidName     = "Bessel 1841";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Ammassalik 1958” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6196</b></li>
     *   <li>EPSG datum name: <b>Ammassalik 1958</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Greenland - Ammassalik area</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Ammassalik 1958")
    public void EPSG_6196() throws FactoryException {
        code              = 6196;
        name              = "Ammassalik 1958";
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Ancienne Triangulation Francaise (Paris)” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6901</b></li>
     *   <li>EPSG datum name: <b>Ancienne Triangulation Francaise (Paris)</b></li>
     *   <li>Alias(es) given by EPSG: <b>ATF (Paris)</b></li>
     *   <li>Ellipsoid name: <b>Plessis 1817</b></li>
     *   <li>Prime meridian name: <b>Paris RGS</b></li>
     *   <li>EPSG Usage Extent: <b>France - mainland onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Ancienne Triangulation Francaise (Paris)")
    public void EPSG_6901() throws FactoryException {
        code              = 6901;
        name              = "Ancienne Triangulation Francaise (Paris)";
        aliases           = new String[] {"ATF (Paris)"};
        ellipsoidName     = "Plessis 1817";
        primeMeridianName = "Paris RGS";
        verifyDatum();
    }

    /**
     * Tests “Anguilla 1957” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6600</b></li>
     *   <li>EPSG datum name: <b>Anguilla 1957</b></li>
     *   <li>Ellipsoid name: <b>Clarke 1880 (RGS)</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Anguilla - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Anguilla 1957")
    public void EPSG_6600() throws FactoryException {
        code              = 6600;
        name              = "Anguilla 1957";
        ellipsoidName     = "Clarke 1880 (RGS)";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Antigua 1943” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6601</b></li>
     *   <li>EPSG datum name: <b>Antigua 1943</b></li>
     *   <li>Ellipsoid name: <b>Clarke 1880 (RGS)</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Antigua - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Antigua 1943")
    public void EPSG_6601() throws FactoryException {
        code              = 6601;
        name              = "Antigua 1943";
        ellipsoidName     = "Clarke 1880 (RGS)";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Aratu” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6208</b></li>
     *   <li>EPSG datum name: <b>Aratu</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Brazil - Aratu</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Aratu")
    public void EPSG_6208() throws FactoryException {
        code              = 6208;
        name              = "Aratu";
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Arc 1950” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6209</b></li>
     *   <li>EPSG datum name: <b>Arc 1950</b></li>
     *   <li>Ellipsoid name: <b>Clarke 1880 (Arc)</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Africa - Botswana; Malawi; Zambia; Zimbabwe</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Arc 1950")
    public void EPSG_6209() throws FactoryException {
        code              = 6209;
        name              = "Arc 1950";
        ellipsoidName     = "Clarke 1880 (Arc)";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Arc 1960” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6210</b></li>
     *   <li>EPSG datum name: <b>Arc 1960</b></li>
     *   <li>Ellipsoid name: <b>Clarke 1880 (RGS)</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Africa - Burundi; Kenya; Rwanda; Tanzania and Uganda</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Arc 1960")
    public void EPSG_6210() throws FactoryException {
        code              = 6210;
        name              = "Arc 1960";
        ellipsoidName     = "Clarke 1880 (RGS)";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Ascension Island 1958” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6712</b></li>
     *   <li>EPSG datum name: <b>Ascension Island 1958</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>St Helena - Ascension Island</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Ascension Island 1958")
    public void EPSG_6712() throws FactoryException {
        code              = 6712;
        name              = "Ascension Island 1958";
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Astro DOS 71” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6710</b></li>
     *   <li>EPSG datum name: <b>Astro DOS 71</b></li>
     *   <li>Alias(es) given by EPSG: <b>ASTRO DOS 71/4</b>, <b>St. Helena 1971</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>St Helena - St Helena Island</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Astro DOS 71")
    public void EPSG_6710() throws FactoryException {
        code              = 6710;
        name              = "Astro DOS 71";
        aliases           = new String[] {"ASTRO DOS 71/4", "St. Helena 1971"};
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Australian Antarctic Datum 1998” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6176</b></li>
     *   <li>EPSG datum name: <b>Australian Antarctic Datum 1998</b></li>
     *   <li>Alias(es) given by EPSG: <b>AAD98</b></li>
     *   <li>Ellipsoid name: <b>GRS 1980</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Antarctica - Australian sector</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Australian Antarctic Datum 1998")
    public void EPSG_6176() throws FactoryException {
        code              = 6176;
        name              = "Australian Antarctic Datum 1998";
        aliases           = new String[] {"AAD98"};
        ellipsoidName     = "GRS 1980";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Australian Geodetic Datum 1966” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6202</b></li>
     *   <li>EPSG datum name: <b>Australian Geodetic Datum 1966</b></li>
     *   <li>Alias(es) given by EPSG: <b>AGD66</b></li>
     *   <li>Ellipsoid name: <b>Australian National Spheroid</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Australasia - Australia and PNG - AGD66</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Australian Geodetic Datum 1966")
    public void EPSG_6202() throws FactoryException {
        code              = 6202;
        name              = "Australian Geodetic Datum 1966";
        aliases           = new String[] {"AGD66"};
        ellipsoidName     = "Australian National Spheroid";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Australian Geodetic Datum 1984” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6203</b></li>
     *   <li>EPSG datum name: <b>Australian Geodetic Datum 1984</b></li>
     *   <li>Alias(es) given by EPSG: <b>AGD84</b></li>
     *   <li>Ellipsoid name: <b>Australian National Spheroid</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Australia - AGD84</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Australian Geodetic Datum 1984")
    public void EPSG_6203() throws FactoryException {
        code              = 6203;
        name              = "Australian Geodetic Datum 1984";
        aliases           = new String[] {"AGD84"};
        ellipsoidName     = "Australian National Spheroid";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Average Terrestrial System 1977” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6122</b></li>
     *   <li>EPSG datum name: <b>Average Terrestrial System 1977</b></li>
     *   <li>Alias(es) given by EPSG: <b>ATS77</b></li>
     *   <li>Ellipsoid name: <b>Average Terrestrial System 1977</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Canada - Maritime Provinces</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Average Terrestrial System 1977")
    public void EPSG_6122() throws FactoryException {
        code              = 6122;
        name              = "Average Terrestrial System 1977";
        aliases           = new String[] {"ATS77"};
        ellipsoidName     = "Average Terrestrial System 1977";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Ayabelle Lighthouse” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6713</b></li>
     *   <li>EPSG datum name: <b>Ayabelle Lighthouse</b></li>
     *   <li>Ellipsoid name: <b>Clarke 1880 (RGS)</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Djibouti</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Ayabelle Lighthouse")
    public void EPSG_6713() throws FactoryException {
        code              = 6713;
        name              = "Ayabelle Lighthouse";
        ellipsoidName     = "Clarke 1880 (RGS)";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Azores Central Islands 1948” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6183</b></li>
     *   <li>EPSG datum name: <b>Azores Central Islands 1948</b></li>
     *   <li>Alias(es) given by EPSG: <b>Graciosa</b>, <b>Azores Central 1948</b>, <b>Base SW</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Portugal - Azores C - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Azores Central Islands 1948")
    public void EPSG_6183() throws FactoryException {
        code              = 6183;
        name              = "Azores Central Islands 1948";
        aliases           = new String[] {"Graciosa", "Azores Central 1948", "Base SW"};
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Azores Central Islands 1995” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6665</b></li>
     *   <li>EPSG datum name: <b>Azores Central Islands 1995</b></li>
     *   <li>Alias(es) given by EPSG: <b>Graciosa</b>, <b>Azores Central 1995</b>, <b>Base SW</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Portugal - Azores C - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Azores Central Islands 1995")
    public void EPSG_6665() throws FactoryException {
        code              = 6665;
        name              = "Azores Central Islands 1995";
        aliases           = new String[] {"Graciosa", "Azores Central 1995", "Base SW"};
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Azores Occidental Islands 1939” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6182</b></li>
     *   <li>EPSG datum name: <b>Azores Occidental Islands 1939</b></li>
     *   <li>Alias(es) given by EPSG: <b>Observatario Flores</b>, <b>Azores Occidental 1939</b>, <b>Observatorio Meteorologico 1939</b>, <b>Observatorio 1966</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Portugal - Azores W - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Azores Occidental Islands 1939")
    public void EPSG_6182() throws FactoryException {
        code              = 6182;
        name              = "Azores Occidental Islands 1939";
        aliases           = new String[] {"Observatario Flores", "Azores Occidental 1939", "Observatorio Meteorologico 1939", "Observatorio 1966"};
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Azores Oriental Islands 1940” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6184</b></li>
     *   <li>EPSG datum name: <b>Azores Oriental Islands 1940</b></li>
     *   <li>Alias(es) given by EPSG: <b>Sao Bras</b>, <b>Azores Oriental 1940</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Portugal - Azores E - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Azores Oriental Islands 1940")
    public void EPSG_6184() throws FactoryException {
        code              = 6184;
        name              = "Azores Oriental Islands 1940";
        aliases           = new String[] {"Sao Bras", "Azores Oriental 1940"};
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Azores Oriental Islands 1995” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6664</b></li>
     *   <li>EPSG datum name: <b>Azores Oriental Islands 1995</b></li>
     *   <li>Alias(es) given by EPSG: <b>Sao Bras</b>, <b>Azores Oriental 1995</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Portugal - Azores E - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Azores Oriental Islands 1995")
    public void EPSG_6664() throws FactoryException {
        code              = 6664;
        name              = "Azores Oriental Islands 1995";
        aliases           = new String[] {"Sao Bras", "Azores Oriental 1995"};
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Barbados 1938” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6212</b></li>
     *   <li>EPSG datum name: <b>Barbados 1938</b></li>
     *   <li>Ellipsoid name: <b>Clarke 1880 (RGS)</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Barbados - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Barbados 1938")
    public void EPSG_6212() throws FactoryException {
        code              = 6212;
        name              = "Barbados 1938";
        ellipsoidName     = "Clarke 1880 (RGS)";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Batavia” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6211</b></li>
     *   <li>EPSG datum name: <b>Batavia</b></li>
     *   <li>Alias(es) given by EPSG: <b>Genuk</b></li>
     *   <li>Ellipsoid name: <b>Bessel 1841</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Indonesia - Java; Java Sea and western Sumatra</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Batavia")
    public void EPSG_6211() throws FactoryException {
        code              = 6211;
        name              = "Batavia";
        aliases           = new String[] {"Genuk"};
        ellipsoidName     = "Bessel 1841";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Batavia (Jakarta)” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6813</b></li>
     *   <li>EPSG datum name: <b>Batavia (Jakarta)</b></li>
     *   <li>Ellipsoid name: <b>Bessel 1841</b></li>
     *   <li>Prime meridian name: <b>Jakarta</b></li>
     *   <li>EPSG Usage Extent: <b>Indonesia - Bali; Java and western Sumatra onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Batavia (Jakarta)")
    public void EPSG_6813() throws FactoryException {
        code              = 6813;
        name              = "Batavia (Jakarta)";
        ellipsoidName     = "Bessel 1841";
        primeMeridianName = "Jakarta";
        verifyDatum();
    }

    /**
     * Tests “Beduaram” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6213</b></li>
     *   <li>EPSG datum name: <b>Beduaram</b></li>
     *   <li>Ellipsoid name: <b>Clarke 1880 (IGN)</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Niger - southeast</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Beduaram")
    public void EPSG_6213() throws FactoryException {
        code              = 6213;
        name              = "Beduaram";
        ellipsoidName     = "Clarke 1880 (IGN)";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Beijing 1954” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6214</b></li>
     *   <li>EPSG datum name: <b>Beijing 1954</b></li>
     *   <li>Ellipsoid name: <b>Krassowsky 1940</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>China</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Beijing 1954")
    public void EPSG_6214() throws FactoryException {
        code              = 6214;
        name              = "Beijing 1954";
        ellipsoidName     = "Krassowsky 1940";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Bellevue” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6714</b></li>
     *   <li>EPSG datum name: <b>Bellevue</b></li>
     *   <li>Alias(es) given by EPSG: <b>Bellevue (IGN)</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Vanuatu - southern islands</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Bellevue")
    public void EPSG_6714() throws FactoryException {
        code              = 6714;
        name              = "Bellevue";
        aliases           = new String[] {"Bellevue (IGN)"};
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Bermuda 1957” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6216</b></li>
     *   <li>EPSG datum name: <b>Bermuda 1957</b></li>
     *   <li>Ellipsoid name: <b>Clarke 1866</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Bermuda - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Bermuda 1957")
    public void EPSG_6216() throws FactoryException {
        code              = 6216;
        name              = "Bermuda 1957";
        ellipsoidName     = "Clarke 1866";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Bermuda 2000” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6762</b></li>
     *   <li>EPSG datum name: <b>Bermuda 2000</b></li>
     *   <li>Alias(es) given by EPSG: <b>BDA2000</b></li>
     *   <li>Ellipsoid name: <b>WGS 84</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Bermuda</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Bermuda 2000")
    public void EPSG_6762() throws FactoryException {
        code              = 6762;
        name              = "Bermuda 2000";
        aliases           = new String[] {"BDA2000"};
        ellipsoidName     = "WGS 84";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Bern 1938” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6306</b></li>
     *   <li>EPSG datum name: <b>Bern 1938</b></li>
     *   <li>Ellipsoid name: <b>Bessel 1841</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Europe - Liechtenstein and Switzerland</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Bern 1938")
    public void EPSG_6306() throws FactoryException {
        code              = 6306;
        name              = "Bern 1938";
        ellipsoidName     = "Bessel 1841";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Bissau” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6165</b></li>
     *   <li>EPSG datum name: <b>Bissau</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Guinea-Bissau - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Bissau")
    public void EPSG_6165() throws FactoryException {
        code              = 6165;
        name              = "Bissau";
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Bogota 1975” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6218</b></li>
     *   <li>EPSG datum name: <b>Bogota 1975</b></li>
     *   <li>Alias(es) given by EPSG: <b>Bogota</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Colombia - mainland and offshore Caribbean</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Bogota 1975")
    public void EPSG_6218() throws FactoryException {
        code              = 6218;
        name              = "Bogota 1975";
        aliases           = new String[] {"Bogota"};
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Bogota 1975 (Bogota)” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6802</b></li>
     *   <li>EPSG datum name: <b>Bogota 1975 (Bogota)</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Bogota</b></li>
     *   <li>EPSG Usage Extent: <b>Colombia - mainland</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Bogota 1975 (Bogota)")
    public void EPSG_6802() throws FactoryException {
        code              = 6802;
        name              = "Bogota 1975 (Bogota)";
        ellipsoidName     = "International 1924";
        primeMeridianName = "Bogota";
        verifyDatum();
    }

    /**
     * Tests “Bukit Rimpah” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6219</b></li>
     *   <li>EPSG datum name: <b>Bukit Rimpah</b></li>
     *   <li>Ellipsoid name: <b>Bessel 1841</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Indonesia - Banga &amp; Belitung Islands</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Bukit Rimpah")
    public void EPSG_6219() throws FactoryException {
        code              = 6219;
        name              = "Bukit Rimpah";
        ellipsoidName     = "Bessel 1841";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Camacupa 1948” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6220</b></li>
     *   <li>EPSG datum name: <b>Camacupa 1948</b></li>
     *   <li>Alias(es) given by EPSG: <b>Camacupa</b></li>
     *   <li>Ellipsoid name: <b>Clarke 1880 (RGS)</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Angola - Angola proper</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Camacupa 1948")
    public void EPSG_6220() throws FactoryException {
        code              = 6220;
        name              = "Camacupa 1948";
        aliases           = new String[] {"Camacupa"};
        ellipsoidName     = "Clarke 1880 (RGS)";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Camp Area Astro” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6715</b></li>
     *   <li>EPSG datum name: <b>Camp Area Astro</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Antarctica - Camp McMurdo area</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Camp Area Astro")
    public void EPSG_6715() throws FactoryException {
        code              = 6715;
        name              = "Camp Area Astro";
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Campo Inchauspe” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6221</b></li>
     *   <li>EPSG datum name: <b>Campo Inchauspe</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Argentina - mainland onshore and offshore TdF</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Campo Inchauspe")
    public void EPSG_6221() throws FactoryException {
        code              = 6221;
        name              = "Campo Inchauspe";
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Cape” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6222</b></li>
     *   <li>EPSG datum name: <b>Cape</b></li>
     *   <li>Alias(es) given by EPSG: <b>South Africa</b></li>
     *   <li>Ellipsoid name: <b>Clarke 1880 (Arc)</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Africa - Botswana; Eswatini; Lesotho and South Africa</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Cape")
    public void EPSG_6222() throws FactoryException {
        code              = 6222;
        name              = "Cape";
        aliases           = new String[] {"South Africa"};
        ellipsoidName     = "Clarke 1880 (Arc)";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Cape Canaveral” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6717</b></li>
     *   <li>EPSG datum name: <b>Cape Canaveral</b></li>
     *   <li>Ellipsoid name: <b>Clarke 1866</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>North America - Bahamas and USA - Florida - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Cape Canaveral")
    public void EPSG_6717() throws FactoryException {
        code              = 6717;
        name              = "Cape Canaveral";
        ellipsoidName     = "Clarke 1866";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Carthage” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6223</b></li>
     *   <li>EPSG datum name: <b>Carthage</b></li>
     *   <li>Ellipsoid name: <b>Clarke 1880 (IGN)</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Tunisia</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Carthage")
    public void EPSG_6223() throws FactoryException {
        code              = 6223;
        name              = "Carthage";
        ellipsoidName     = "Clarke 1880 (IGN)";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Carthage (Paris)” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6816</b></li>
     *   <li>EPSG datum name: <b>Carthage (Paris)</b></li>
     *   <li>Ellipsoid name: <b>Clarke 1880 (IGN)</b></li>
     *   <li>Prime meridian name: <b>Paris</b></li>
     *   <li>EPSG Usage Extent: <b>Tunisia - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Carthage (Paris)")
    public void EPSG_6816() throws FactoryException {
        code              = 6816;
        name              = "Carthage (Paris)";
        ellipsoidName     = "Clarke 1880 (IGN)";
        primeMeridianName = "Paris";
        verifyDatum();
    }

    /**
     * Tests “Centre Spatial Guyanais 1967” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6623</b></li>
     *   <li>EPSG datum name: <b>Centre Spatial Guyanais 1967</b></li>
     *   <li>Alias(es) given by EPSG: <b>CSG67</b>, <b>Guyane Francaise</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>French Guiana - coastal area</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Centre Spatial Guyanais 1967")
    public void EPSG_6623() throws FactoryException {
        code              = 6623;
        name              = "Centre Spatial Guyanais 1967";
        aliases           = new String[] {"CSG67", "Guyane Francaise"};
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “CH1903” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6149</b></li>
     *   <li>EPSG datum name: <b>CH1903</b></li>
     *   <li>Ellipsoid name: <b>Bessel 1841</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Europe - Liechtenstein and Switzerland</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("CH1903")
    public void EPSG_6149() throws FactoryException {
        code              = 6149;
        name              = "CH1903";
        ellipsoidName     = "Bessel 1841";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “CH1903 (Bern)” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6801</b></li>
     *   <li>EPSG datum name: <b>CH1903 (Bern)</b></li>
     *   <li>Ellipsoid name: <b>Bessel 1841</b></li>
     *   <li>Prime meridian name: <b>Bern</b></li>
     *   <li>EPSG Usage Extent: <b>Europe - Liechtenstein and Switzerland</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("CH1903 (Bern)")
    public void EPSG_6801() throws FactoryException {
        code              = 6801;
        name              = "CH1903 (Bern)";
        ellipsoidName     = "Bessel 1841";
        primeMeridianName = "Bern";
        verifyDatum();
    }

    /**
     * Tests “CH1903+” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6150</b></li>
     *   <li>EPSG datum name: <b>CH1903+</b></li>
     *   <li>Ellipsoid name: <b>Bessel 1841</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Europe - Liechtenstein and Switzerland</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("CH1903+")
    public void EPSG_6150() throws FactoryException {
        code              = 6150;
        name              = "CH1903+";
        ellipsoidName     = "Bessel 1841";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Chatham Islands Datum 1971” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6672</b></li>
     *   <li>EPSG datum name: <b>Chatham Islands Datum 1971</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>New Zealand - Chatham Islands group</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Chatham Islands Datum 1971")
    public void EPSG_6672() throws FactoryException {
        code              = 6672;
        name              = "Chatham Islands Datum 1971";
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Chatham Islands Datum 1979” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6673</b></li>
     *   <li>EPSG datum name: <b>Chatham Islands Datum 1979</b></li>
     *   <li>Alias(es) given by EPSG: <b>CI1979</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>New Zealand - Chatham Islands group</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Chatham Islands Datum 1979")
    public void EPSG_6673() throws FactoryException {
        code              = 6673;
        name              = "Chatham Islands Datum 1979";
        aliases           = new String[] {"CI1979"};
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Chos Malal 1914” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6160</b></li>
     *   <li>EPSG datum name: <b>Chos Malal 1914</b></li>
     *   <li>Alias(es) given by EPSG: <b>Quini-Huao</b>, <b>Quiñi-Huao</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Argentina - Mendoza and Neuquen</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Chos Malal 1914")
    public void EPSG_6160() throws FactoryException {
        code              = 6160;
        name              = "Chos Malal 1914";
        aliases           = new String[] {"Quini-Huao", "Quiñi-Huao"};
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Chua” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6224</b></li>
     *   <li>EPSG datum name: <b>Chua</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>South America - Brazil - south of 18°S and west of 54°W + DF; N Paraguay</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Chua")
    public void EPSG_6224() throws FactoryException {
        code              = 6224;
        name              = "Chua";
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Cocos Islands 1965” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6708</b></li>
     *   <li>EPSG datum name: <b>Cocos Islands 1965</b></li>
     *   <li>Ellipsoid name: <b>Australian National Spheroid</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Cocos (Keeling) Islands - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Cocos Islands 1965")
    public void EPSG_6708() throws FactoryException {
        code              = 6708;
        name              = "Cocos Islands 1965";
        ellipsoidName     = "Australian National Spheroid";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Combani 1950” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6632</b></li>
     *   <li>EPSG datum name: <b>Combani 1950</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Mayotte - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Combani 1950")
    public void EPSG_6632() throws FactoryException {
        code              = 6632;
        name              = "Combani 1950";
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Conakry 1905” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6315</b></li>
     *   <li>EPSG datum name: <b>Conakry 1905</b></li>
     *   <li>Ellipsoid name: <b>Clarke 1880 (IGN)</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Guinea - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Conakry 1905")
    public void EPSG_6315() throws FactoryException {
        code              = 6315;
        name              = "Conakry 1905";
        ellipsoidName     = "Clarke 1880 (IGN)";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Congo 1960 Pointe Noire” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6282</b></li>
     *   <li>EPSG datum name: <b>Congo 1960 Pointe Noire</b></li>
     *   <li>Ellipsoid name: <b>Clarke 1880 (IGN)</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Congo</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Congo 1960 Pointe Noire")
    public void EPSG_6282() throws FactoryException {
        code              = 6282;
        name              = "Congo 1960 Pointe Noire";
        ellipsoidName     = "Clarke 1880 (IGN)";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Corrego Alegre 1970-72” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6225</b></li>
     *   <li>EPSG datum name: <b>Corrego Alegre 1970-72</b></li>
     *   <li>Alias(es) given by EPSG: <b>Corrego Alegre</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Brazil - Corrego Alegre 1970-1972</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Corrego Alegre 1970-72")
    public void EPSG_6225() throws FactoryException {
        code              = 6225;
        name              = "Corrego Alegre 1970-72";
        aliases           = new String[] {"Corrego Alegre"};
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Croatian Terrestrial Reference System” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6761</b></li>
     *   <li>EPSG datum name: <b>Croatian Terrestrial Reference System</b></li>
     *   <li>Alias(es) given by EPSG: <b>HTRS96</b></li>
     *   <li>Ellipsoid name: <b>GRS 1980</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Croatia</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Croatian Terrestrial Reference System")
    public void EPSG_6761() throws FactoryException {
        code              = 6761;
        name              = "Croatian Terrestrial Reference System";
        aliases           = new String[] {"HTRS96"};
        ellipsoidName     = "GRS 1980";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Dabola 1981” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6155</b></li>
     *   <li>EPSG datum name: <b>Dabola 1981</b></li>
     *   <li>Ellipsoid name: <b>Clarke 1880 (IGN)</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Guinea - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Dabola 1981")
    public void EPSG_6155() throws FactoryException {
        code              = 6155;
        name              = "Dabola 1981";
        ellipsoidName     = "Clarke 1880 (IGN)";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Datum 73” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6274</b></li>
     *   <li>EPSG datum name: <b>Datum 73</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Portugal - mainland - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Datum 73")
    public void EPSG_6274() throws FactoryException {
        code              = 6274;
        name              = "Datum 73";
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Datum Geodesi Nasional 1995” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6755</b></li>
     *   <li>EPSG datum name: <b>Datum Geodesi Nasional 1995</b></li>
     *   <li>Alias(es) given by EPSG: <b>DGN95</b>, <b>Indonesian Geodetic Datum 1995</b>, <b>IGD95</b></li>
     *   <li>Ellipsoid name: <b>WGS 84</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Indonesia</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Datum Geodesi Nasional 1995")
    public void EPSG_6755() throws FactoryException {
        code              = 6755;
        name              = "Datum Geodesi Nasional 1995";
        aliases           = new String[] {"DGN95", "Indonesian Geodetic Datum 1995", "IGD95"};
        ellipsoidName     = "WGS 84";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Dealul Piscului 1930” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6316</b></li>
     *   <li>EPSG datum name: <b>Dealul Piscului 1930</b></li>
     *   <li>Alias(es) given by EPSG: <b>Dealul Piscului 1933</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Romania - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Dealul Piscului 1930")
    public void EPSG_6316() throws FactoryException {
        code              = 6316;
        name              = "Dealul Piscului 1930";
        aliases           = new String[] {"Dealul Piscului 1933"};
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Deception Island” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6736</b></li>
     *   <li>EPSG datum name: <b>Deception Island</b></li>
     *   <li>Ellipsoid name: <b>Clarke 1880 (RGS)</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Antarctica - Deception Island</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Deception Island")
    public void EPSG_6736() throws FactoryException {
        code              = 6736;
        name              = "Deception Island";
        ellipsoidName     = "Clarke 1880 (RGS)";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Deir ez Zor” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6227</b></li>
     *   <li>EPSG datum name: <b>Deir ez Zor</b></li>
     *   <li>Alias(es) given by EPSG: <b>Levant</b></li>
     *   <li>Ellipsoid name: <b>Clarke 1880 (IGN)</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Asia - Middle East - Lebanon and Syria onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Deir ez Zor")
    public void EPSG_6227() throws FactoryException {
        code              = 6227;
        name              = "Deir ez Zor";
        aliases           = new String[] {"Levant"};
        ellipsoidName     = "Clarke 1880 (IGN)";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Deutsches Hauptdreiecksnetz” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6314</b></li>
     *   <li>EPSG datum name: <b>Deutsches Hauptdreiecksnetz</b></li>
     *   <li>Alias(es) given by EPSG: <b>DHDN</b></li>
     *   <li>Ellipsoid name: <b>Bessel 1841</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Germany - West Germany all states</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Deutsches Hauptdreiecksnetz")
    public void EPSG_6314() throws FactoryException {
        code              = 6314;
        name              = "Deutsches Hauptdreiecksnetz";
        aliases           = new String[] {"DHDN"};
        ellipsoidName     = "Bessel 1841";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Diego Garcia 1969” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6724</b></li>
     *   <li>EPSG datum name: <b>Diego Garcia 1969</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>British Indian Ocean Territory - Diego Garcia</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Diego Garcia 1969")
    public void EPSG_6724() throws FactoryException {
        code              = 6724;
        name              = "Diego Garcia 1969";
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Dominica 1945” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6602</b></li>
     *   <li>EPSG datum name: <b>Dominica 1945</b></li>
     *   <li>Ellipsoid name: <b>Clarke 1880 (RGS)</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Dominica - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Dominica 1945")
    public void EPSG_6602() throws FactoryException {
        code              = 6602;
        name              = "Dominica 1945";
        ellipsoidName     = "Clarke 1880 (RGS)";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Douala 1948” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6192</b></li>
     *   <li>EPSG datum name: <b>Douala 1948</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Cameroon - coastal area</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Douala 1948")
    public void EPSG_6192() throws FactoryException {
        code              = 6192;
        name              = "Douala 1948";
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Easter Island 1967” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6719</b></li>
     *   <li>EPSG datum name: <b>Easter Island 1967</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Chile - Easter Island onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Easter Island 1967")
    public void EPSG_6719() throws FactoryException {
        code              = 6719;
        name              = "Easter Island 1967";
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Egypt 1907” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6229</b></li>
     *   <li>EPSG datum name: <b>Egypt 1907</b></li>
     *   <li>Alias(es) given by EPSG: <b>Old Egyptian</b></li>
     *   <li>Ellipsoid name: <b>Helmert 1906</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Egypt</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Egypt 1907")
    public void EPSG_6229() throws FactoryException {
        code              = 6229;
        name              = "Egypt 1907";
        aliases           = new String[] {"Old Egyptian"};
        ellipsoidName     = "Helmert 1906";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Egypt 1930” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6199</b></li>
     *   <li>EPSG datum name: <b>Egypt 1930</b></li>
     *   <li>Alias(es) given by EPSG: <b>New Egyptian</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Egypt - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Egypt 1930")
    public void EPSG_6199() throws FactoryException {
        code              = 6199;
        name              = "Egypt 1930";
        aliases           = new String[] {"New Egyptian"};
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Egypt Gulf of Suez S-650 TL” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6706</b></li>
     *   <li>EPSG datum name: <b>Egypt Gulf of Suez S-650 TL</b></li>
     *   <li>Alias(es) given by EPSG: <b>S-650 TL</b></li>
     *   <li>Ellipsoid name: <b>Helmert 1906</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Egypt - Gulf of Suez</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Egypt Gulf of Suez S-650 TL")
    public void EPSG_6706() throws FactoryException {
        code              = 6706;
        name              = "Egypt Gulf of Suez S-650 TL";
        aliases           = new String[] {"S-650 TL"};
        ellipsoidName     = "Helmert 1906";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Estonia 1992” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6133</b></li>
     *   <li>EPSG datum name: <b>Estonia 1992</b></li>
     *   <li>Alias(es) given by EPSG: <b>EST92</b></li>
     *   <li>Ellipsoid name: <b>GRS 1980</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Estonia - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Estonia 1992")
    public void EPSG_6133() throws FactoryException {
        code              = 6133;
        name              = "Estonia 1992";
        aliases           = new String[] {"EST92"};
        ellipsoidName     = "GRS 1980";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Estonia 1997” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6180</b></li>
     *   <li>EPSG datum name: <b>Estonia 1997</b></li>
     *   <li>Alias(es) given by EPSG: <b>EST97</b></li>
     *   <li>Ellipsoid name: <b>GRS 1980</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Estonia</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Estonia 1997")
    public void EPSG_6180() throws FactoryException {
        code              = 6180;
        name              = "Estonia 1997";
        aliases           = new String[] {"EST97"};
        ellipsoidName     = "GRS 1980";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “European Datum 1950” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6230</b></li>
     *   <li>EPSG datum name: <b>European Datum 1950</b></li>
     *   <li>Alias(es) given by EPSG: <b>ED50</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Europe - ED50 by country</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("European Datum 1950")
    public void EPSG_6230() throws FactoryException {
        code              = 6230;
        name              = "European Datum 1950";
        aliases           = new String[] {"ED50"};
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “European Datum 1950(1977)” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6154</b></li>
     *   <li>EPSG datum name: <b>European Datum 1950(1977)</b></li>
     *   <li>Alias(es) given by EPSG: <b>ED50(ED77)</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Iran</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("European Datum 1950(1977)")
    public void EPSG_6154() throws FactoryException {
        code              = 6154;
        name              = "European Datum 1950(1977)";
        aliases           = new String[] {"ED50(ED77)"};
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “European Datum 1979” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6668</b></li>
     *   <li>EPSG datum name: <b>European Datum 1979</b></li>
     *   <li>Alias(es) given by EPSG: <b>ED79</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Europe - west</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("European Datum 1979")
    public void EPSG_6668() throws FactoryException {
        code              = 6668;
        name              = "European Datum 1979";
        aliases           = new String[] {"ED79"};
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “European Datum 1987” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6231</b></li>
     *   <li>EPSG datum name: <b>European Datum 1987</b></li>
     *   <li>Alias(es) given by EPSG: <b>ED87</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Europe - west</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("European Datum 1987")
    public void EPSG_6231() throws FactoryException {
        code              = 6231;
        name              = "European Datum 1987";
        aliases           = new String[] {"ED87"};
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “European Libyan Datum 1979” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6159</b></li>
     *   <li>EPSG datum name: <b>European Libyan Datum 1979</b></li>
     *   <li>Alias(es) given by EPSG: <b>ELD79</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Libya</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("European Libyan Datum 1979")
    public void EPSG_6159() throws FactoryException {
        code              = 6159;
        name              = "European Libyan Datum 1979";
        aliases           = new String[] {"ELD79"};
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “European Terrestrial Reference System 1989” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6258</b></li>
     *   <li>EPSG datum name: <b>European Terrestrial Reference System 1989</b></li>
     *   <li>Alias(es) given by EPSG: <b>ETRS89</b>, <b>European Terrestrial Reference System 1989</b></li>
     *   <li>Ellipsoid name: <b>GRS 1980</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Europe - ETRF by country</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("European Terrestrial Reference System 1989")
    public void EPSG_6258() throws FactoryException {
        code              = 6258;
        name              = "European Terrestrial Reference System 1989";
        aliases           = new String[] {"ETRS89", "European Terrestrial Reference System 1989"};
        ellipsoidName     = "GRS 1980";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Fahud” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6232</b></li>
     *   <li>EPSG datum name: <b>Fahud</b></li>
     *   <li>Ellipsoid name: <b>Clarke 1880 (RGS)</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Oman - mainland</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Fahud")
    public void EPSG_6232() throws FactoryException {
        code              = 6232;
        name              = "Fahud";
        ellipsoidName     = "Clarke 1880 (RGS)";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Faroe Datum 1954” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6741</b></li>
     *   <li>EPSG datum name: <b>Faroe Datum 1954</b></li>
     *   <li>Alias(es) given by EPSG: <b>FD54</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Faroe Islands - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Faroe Datum 1954")
    public void EPSG_6741() throws FactoryException {
        code              = 6741;
        name              = "Faroe Datum 1954";
        aliases           = new String[] {"FD54"};
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Fatu Iva 72” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6688</b></li>
     *   <li>EPSG datum name: <b>Fatu Iva 72</b></li>
     *   <li>Alias(es) given by EPSG: <b>MHEFO 55</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>French Polynesia - Marquesas Islands - Fatu Hiva</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Fatu Iva 72")
    public void EPSG_6688() throws FactoryException {
        code              = 6688;
        name              = "Fatu Iva 72";
        aliases           = new String[] {"MHEFO 55"};
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Fiji 1956” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6721</b></li>
     *   <li>EPSG datum name: <b>Fiji 1956</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Fiji - main islands</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Fiji 1956")
    public void EPSG_6721() throws FactoryException {
        code              = 6721;
        name              = "Fiji 1956";
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Fiji Geodetic Datum 1986” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6720</b></li>
     *   <li>EPSG datum name: <b>Fiji Geodetic Datum 1986</b></li>
     *   <li>Alias(es) given by EPSG: <b>FGD 1986</b>, <b>Fiji 1986</b></li>
     *   <li>Ellipsoid name: <b>WGS 72</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Fiji - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Fiji Geodetic Datum 1986")
    public void EPSG_6720() throws FactoryException {
        code              = 6720;
        name              = "Fiji Geodetic Datum 1986";
        aliases           = new String[] {"FGD 1986", "Fiji 1986"};
        ellipsoidName     = "WGS 72";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Final Datum 1958” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6132</b></li>
     *   <li>EPSG datum name: <b>Final Datum 1958</b></li>
     *   <li>Alias(es) given by EPSG: <b>FD58</b></li>
     *   <li>Ellipsoid name: <b>Clarke 1880 (RGS)</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Iran - FD58</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Final Datum 1958")
    public void EPSG_6132() throws FactoryException {
        code              = 6132;
        name              = "Final Datum 1958";
        aliases           = new String[] {"FD58"};
        ellipsoidName     = "Clarke 1880 (RGS)";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “fk89” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6753</b></li>
     *   <li>EPSG datum name: <b>fk89</b></li>
     *   <li>Alias(es) given by EPSG: <b>FD54a</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Faroe Islands - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("fk89")
    public void EPSG_6753() throws FactoryException {
        code              = 6753;
        name              = "fk89";
        aliases           = new String[] {"FD54a"};
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Fort Marigot” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6621</b></li>
     *   <li>EPSG datum name: <b>Fort Marigot</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Guadeloupe - St Martin and St Barthelemy - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Fort Marigot")
    public void EPSG_6621() throws FactoryException {
        code              = 6621;
        name              = "Fort Marigot";
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Gan 1970” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6684</b></li>
     *   <li>EPSG datum name: <b>Gan 1970</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Maldives - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Gan 1970")
    public void EPSG_6684() throws FactoryException {
        code              = 6684;
        name              = "Gan 1970";
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Garoua” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6197</b></li>
     *   <li>EPSG datum name: <b>Garoua</b></li>
     *   <li>Ellipsoid name: <b>Clarke 1880 (RGS)</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Cameroon - Garoua area</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Garoua")
    public void EPSG_6197() throws FactoryException {
        code              = 6197;
        name              = "Garoua";
        ellipsoidName     = "Clarke 1880 (RGS)";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Geocentric Datum of Australia 1994” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6283</b></li>
     *   <li>EPSG datum name: <b>Geocentric Datum of Australia 1994</b></li>
     *   <li>Alias(es) given by EPSG: <b>GDA94</b></li>
     *   <li>Ellipsoid name: <b>GRS 1980</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Australia - GDA</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Geocentric Datum of Australia 1994")
    public void EPSG_6283() throws FactoryException {
        code              = 6283;
        name              = "Geocentric Datum of Australia 1994";
        aliases           = new String[] {"GDA94"};
        ellipsoidName     = "GRS 1980";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Geocentric datum of Korea” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6737</b></li>
     *   <li>EPSG datum name: <b>Geocentric datum of Korea</b></li>
     *   <li>Alias(es) given by EPSG: <b>Korea 2000</b></li>
     *   <li>Ellipsoid name: <b>GRS 1980</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Korea; Republic of (South Korea)</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Geocentric datum of Korea")
    public void EPSG_6737() throws FactoryException {
        code              = 6737;
        name              = "Geocentric datum of Korea";
        aliases           = new String[] {"Korea 2000"};
        ellipsoidName     = "GRS 1980";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Geodetic Datum of 1965” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6300</b></li>
     *   <li>EPSG datum name: <b>Geodetic Datum of 1965</b></li>
     *   <li>Alias(es) given by EPSG: <b>TM75</b>, <b>1975 Mapping Adjustment</b></li>
     *   <li>Ellipsoid name: <b>Airy Modified 1849</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Europe - Ireland (Republic and Ulster) - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Geodetic Datum of 1965")
    public void EPSG_6300() throws FactoryException {
        code              = 6300;
        name              = "Geodetic Datum of 1965";
        aliases           = new String[] {"TM75", "1975 Mapping Adjustment"};
        ellipsoidName     = "Airy Modified 1849";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Geodetic Datum of Malaysia 2000” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6742</b></li>
     *   <li>EPSG datum name: <b>Geodetic Datum of Malaysia 2000</b></li>
     *   <li>Alias(es) given by EPSG: <b>GDM2000</b></li>
     *   <li>Ellipsoid name: <b>GRS 1980</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Malaysia</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Geodetic Datum of Malaysia 2000")
    public void EPSG_6742() throws FactoryException {
        code              = 6742;
        name              = "Geodetic Datum of Malaysia 2000";
        aliases           = new String[] {"GDM2000"};
        ellipsoidName     = "GRS 1980";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Grand Cayman Geodetic Datum 1959” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6723</b></li>
     *   <li>EPSG datum name: <b>Grand Cayman Geodetic Datum 1959</b></li>
     *   <li>Alias(es) given by EPSG: <b>GCGD59</b>, <b>Grand Cayman 1959</b></li>
     *   <li>Ellipsoid name: <b>Clarke 1866</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Cayman Islands - Grand Cayman</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Grand Cayman Geodetic Datum 1959")
    public void EPSG_6723() throws FactoryException {
        code              = 6723;
        name              = "Grand Cayman Geodetic Datum 1959";
        aliases           = new String[] {"GCGD59", "Grand Cayman 1959"};
        ellipsoidName     = "Clarke 1866";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Grand Comoros” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6646</b></li>
     *   <li>EPSG datum name: <b>Grand Comoros</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Comoros - Njazidja (Grande Comore)</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Grand Comoros")
    public void EPSG_6646() throws FactoryException {
        code              = 6646;
        name              = "Grand Comoros";
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Greek” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6120</b></li>
     *   <li>EPSG datum name: <b>Greek</b></li>
     *   <li>Ellipsoid name: <b>Bessel 1841</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Greece - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Greek")
    public void EPSG_6120() throws FactoryException {
        code              = 6120;
        name              = "Greek";
        ellipsoidName     = "Bessel 1841";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Greek (Athens)” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6815</b></li>
     *   <li>EPSG datum name: <b>Greek (Athens)</b></li>
     *   <li>Ellipsoid name: <b>Bessel 1841</b></li>
     *   <li>Prime meridian name: <b>Athens</b></li>
     *   <li>EPSG Usage Extent: <b>Greece - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Greek (Athens)")
    public void EPSG_6815() throws FactoryException {
        code              = 6815;
        name              = "Greek (Athens)";
        ellipsoidName     = "Bessel 1841";
        primeMeridianName = "Athens";
        verifyDatum();
    }

    /**
     * Tests “Greek Geodetic Reference System 1987” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6121</b></li>
     *   <li>EPSG datum name: <b>Greek Geodetic Reference System 1987</b></li>
     *   <li>Alias(es) given by EPSG: <b>GGRS87</b></li>
     *   <li>Ellipsoid name: <b>GRS 1980</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Greece - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Greek Geodetic Reference System 1987")
    public void EPSG_6121() throws FactoryException {
        code              = 6121;
        name              = "Greek Geodetic Reference System 1987";
        aliases           = new String[] {"GGRS87"};
        ellipsoidName     = "GRS 1980";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Greenland 1996” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6747</b></li>
     *   <li>EPSG datum name: <b>Greenland 1996</b></li>
     *   <li>Alias(es) given by EPSG: <b>GR96</b></li>
     *   <li>Ellipsoid name: <b>GRS 1980</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Greenland</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Greenland 1996")
    public void EPSG_6747() throws FactoryException {
        code              = 6747;
        name              = "Greenland 1996";
        aliases           = new String[] {"GR96"};
        ellipsoidName     = "GRS 1980";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Grenada 1953” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6603</b></li>
     *   <li>EPSG datum name: <b>Grenada 1953</b></li>
     *   <li>Ellipsoid name: <b>Clarke 1880 (RGS)</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Grenada and southern Grenadines - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Grenada 1953")
    public void EPSG_6603() throws FactoryException {
        code              = 6603;
        name              = "Grenada 1953";
        ellipsoidName     = "Clarke 1880 (RGS)";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Guadeloupe 1948” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6622</b></li>
     *   <li>EPSG datum name: <b>Guadeloupe 1948</b></li>
     *   <li>Alias(es) given by EPSG: <b>Sainte Anne</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Guadeloupe - Grande-Terre and surrounding islands - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Guadeloupe 1948")
    public void EPSG_6622() throws FactoryException {
        code              = 6622;
        name              = "Guadeloupe 1948";
        aliases           = new String[] {"Sainte Anne"};
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Guam 1963” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6675</b></li>
     *   <li>EPSG datum name: <b>Guam 1963</b></li>
     *   <li>Ellipsoid name: <b>Clarke 1866</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Pacific - Guam and NMI - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Guam 1963")
    public void EPSG_6675() throws FactoryException {
        code              = 6675;
        name              = "Guam 1963";
        ellipsoidName     = "Clarke 1866";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Gulshan 303” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6682</b></li>
     *   <li>EPSG datum name: <b>Gulshan 303</b></li>
     *   <li>Ellipsoid name: <b>Everest 1830 (1937 Adjustment)</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Bangladesh</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Gulshan 303")
    public void EPSG_6682() throws FactoryException {
        code              = 6682;
        name              = "Gulshan 303";
        ellipsoidName     = "Everest 1830 (1937 Adjustment)";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Gunung Segara” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6613</b></li>
     *   <li>EPSG datum name: <b>Gunung Segara</b></li>
     *   <li>Alias(es) given by EPSG: <b>Segara</b>, <b>Samboja</b>, <b>P2 Exc</b>, <b>P2 Exc-T9</b></li>
     *   <li>Ellipsoid name: <b>Bessel 1841</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Indonesia - Kalimantan E</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Gunung Segara")
    public void EPSG_6613() throws FactoryException {
        code              = 6613;
        name              = "Gunung Segara";
        aliases           = new String[] {"Segara", "Samboja", "P2 Exc", "P2 Exc-T9"};
        ellipsoidName     = "Bessel 1841";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Gunung Segara (Jakarta)” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6820</b></li>
     *   <li>EPSG datum name: <b>Gunung Segara (Jakarta)</b></li>
     *   <li>Alias(es) given by EPSG: <b>Segara (Jakarta)</b></li>
     *   <li>Ellipsoid name: <b>Bessel 1841</b></li>
     *   <li>Prime meridian name: <b>Jakarta</b></li>
     *   <li>EPSG Usage Extent: <b>Indonesia - Kalimantan E</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Gunung Segara (Jakarta)")
    public void EPSG_6820() throws FactoryException {
        code              = 6820;
        name              = "Gunung Segara (Jakarta)";
        aliases           = new String[] {"Segara (Jakarta)"};
        ellipsoidName     = "Bessel 1841";
        primeMeridianName = "Jakarta";
        verifyDatum();
    }

    /**
     * Tests “Hanoi 1972” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6147</b></li>
     *   <li>EPSG datum name: <b>Hanoi 1972</b></li>
     *   <li>Ellipsoid name: <b>Krassowsky 1940</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Vietnam - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Hanoi 1972")
    public void EPSG_6147() throws FactoryException {
        code              = 6147;
        name              = "Hanoi 1972";
        ellipsoidName     = "Krassowsky 1940";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Hartebeesthoek94” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6148</b></li>
     *   <li>EPSG datum name: <b>Hartebeesthoek94</b></li>
     *   <li>Ellipsoid name: <b>WGS 84</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Africa - South Africa; Lesotho and Eswatini.</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Hartebeesthoek94")
    public void EPSG_6148() throws FactoryException {
        code              = 6148;
        name              = "Hartebeesthoek94";
        ellipsoidName     = "WGS 84";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Helle 1954” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6660</b></li>
     *   <li>EPSG datum name: <b>Helle 1954</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Jan Mayen - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Helle 1954")
    public void EPSG_6660() throws FactoryException {
        code              = 6660;
        name              = "Helle 1954";
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Herat North” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6255</b></li>
     *   <li>EPSG datum name: <b>Herat North</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Afghanistan</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Herat North")
    public void EPSG_6255() throws FactoryException {
        code              = 6255;
        name              = "Herat North";
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Hito XVIII 1963” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6254</b></li>
     *   <li>EPSG datum name: <b>Hito XVIII 1963</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>South America - Tierra del Fuego</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Hito XVIII 1963")
    public void EPSG_6254() throws FactoryException {
        code              = 6254;
        name              = "Hito XVIII 1963";
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Hjorsey 1955” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6658</b></li>
     *   <li>EPSG datum name: <b>Hjorsey 1955</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Iceland - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Hjorsey 1955")
    public void EPSG_6658() throws FactoryException {
        code              = 6658;
        name              = "Hjorsey 1955";
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Hong Kong 1963” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6738</b></li>
     *   <li>EPSG datum name: <b>Hong Kong 1963</b></li>
     *   <li>Alias(es) given by EPSG: <b>HK63</b></li>
     *   <li>Ellipsoid name: <b>Clarke 1858</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>China - Hong Kong</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Hong Kong 1963")
    public void EPSG_6738() throws FactoryException {
        code              = 6738;
        name              = "Hong Kong 1963";
        aliases           = new String[] {"HK63"};
        ellipsoidName     = "Clarke 1858";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Hong Kong 1963(67)” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6739</b></li>
     *   <li>EPSG datum name: <b>Hong Kong 1963(67)</b></li>
     *   <li>Alias(es) given by EPSG: <b>HK63(67)</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>China - Hong Kong</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Hong Kong 1963(67)")
    public void EPSG_6739() throws FactoryException {
        code              = 6739;
        name              = "Hong Kong 1963(67)";
        aliases           = new String[] {"HK63(67)"};
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Hong Kong 1980” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6611</b></li>
     *   <li>EPSG datum name: <b>Hong Kong 1980</b></li>
     *   <li>Alias(es) given by EPSG: <b>HK80</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>China - Hong Kong</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Hong Kong 1980")
    public void EPSG_6611() throws FactoryException {
        code              = 6611;
        name              = "Hong Kong 1980";
        aliases           = new String[] {"HK80"};
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Hungarian Datum 1909” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>1024</b></li>
     *   <li>EPSG datum name: <b>Hungarian Datum 1909</b></li>
     *   <li>Alias(es) given by EPSG: <b>HD1909</b></li>
     *   <li>Ellipsoid name: <b>Bessel 1841</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Hungary</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Hungarian Datum 1909")
    public void EPSG_1024() throws FactoryException {
        code              = 1024;
        name              = "Hungarian Datum 1909";
        aliases           = new String[] {"HD1909"};
        ellipsoidName     = "Bessel 1841";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Hungarian Datum 1972” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6237</b></li>
     *   <li>EPSG datum name: <b>Hungarian Datum 1972</b></li>
     *   <li>Alias(es) given by EPSG: <b>HD72</b></li>
     *   <li>Ellipsoid name: <b>GRS 1967</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Hungary</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Hungarian Datum 1972")
    public void EPSG_6237() throws FactoryException {
        code              = 6237;
        name              = "Hungarian Datum 1972";
        aliases           = new String[] {"HD72"};
        ellipsoidName     = "GRS 1967";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Hu Tzu Shan 1950” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6236</b></li>
     *   <li>EPSG datum name: <b>Hu Tzu Shan 1950</b></li>
     *   <li>Alias(es) given by EPSG: <b>Hu Tzu Shan</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Taiwan - onshore - mainland and Penghu</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Hu Tzu Shan 1950")
    public void EPSG_6236() throws FactoryException {
        code              = 6236;
        name              = "Hu Tzu Shan 1950";
        aliases           = new String[] {"Hu Tzu Shan"};
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “IGC 1962 Arc of the 6th Parallel South” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6697</b></li>
     *   <li>EPSG datum name: <b>IGC 1962 Arc of the 6th Parallel South</b></li>
     *   <li>Alias(es) given by EPSG: <b>IGC 1962 6th Parallel South</b></li>
     *   <li>Ellipsoid name: <b>Clarke 1880 (RGS)</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Congo DR (Zaire) - 6th parallel south</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("IGC 1962 Arc of the 6th Parallel South")
    public void EPSG_6697() throws FactoryException {
        code              = 6697;
        name              = "IGC 1962 Arc of the 6th Parallel South";
        aliases           = new String[] {"IGC 1962 6th Parallel South"};
        ellipsoidName     = "Clarke 1880 (RGS)";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “IGN 1962 Kerguelen” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6698</b></li>
     *   <li>EPSG datum name: <b>IGN 1962 Kerguelen</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>French Southern Territories - Kerguelen onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("IGN 1962 Kerguelen")
    public void EPSG_6698() throws FactoryException {
        code              = 6698;
        name              = "IGN 1962 Kerguelen";
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “IGN53 Mare” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6641</b></li>
     *   <li>EPSG datum name: <b>IGN53 Mare</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>New Caledonia - Mare</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("IGN53 Mare")
    public void EPSG_6641() throws FactoryException {
        code              = 6641;
        name              = "IGN53 Mare";
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “IGN56 Lifou” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6633</b></li>
     *   <li>EPSG datum name: <b>IGN56 Lifou</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>New Caledonia - Lifou</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("IGN56 Lifou")
    public void EPSG_6633() throws FactoryException {
        code              = 6633;
        name              = "IGN56 Lifou";
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “IGN63 Hiva Oa” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6689</b></li>
     *   <li>EPSG datum name: <b>IGN63 Hiva Oa</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>French Polynesia - Marquesas Islands - Hiva Oa and Tahuata</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("IGN63 Hiva Oa")
    public void EPSG_6689() throws FactoryException {
        code              = 6689;
        name              = "IGN63 Hiva Oa";
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “IGN72 Grande Terre” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6634</b></li>
     *   <li>EPSG datum name: <b>IGN72 Grande Terre</b></li>
     *   <li>Alias(es) given by EPSG: <b>MHNC72</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>New Caledonia - Grande Terre</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("IGN72 Grande Terre")
    public void EPSG_6634() throws FactoryException {
        code              = 6634;
        name              = "IGN72 Grande Terre";
        aliases           = new String[] {"MHNC72"};
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “IGN72 Nuku Hiva” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6630</b></li>
     *   <li>EPSG datum name: <b>IGN72 Nuku Hiva</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>French Polynesia - Marquesas Islands - Nuku Hiva; Ua Huka and Ua Pou</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("IGN72 Nuku Hiva")
    public void EPSG_6630() throws FactoryException {
        code              = 6630;
        name              = "IGN72 Nuku Hiva";
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “IGN Astro 1960” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6700</b></li>
     *   <li>EPSG datum name: <b>IGN Astro 1960</b></li>
     *   <li>Ellipsoid name: <b>Clarke 1880 (RGS)</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Mauritania - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("IGN Astro 1960")
    public void EPSG_6700() throws FactoryException {
        code              = 6700;
        name              = "IGN Astro 1960";
        ellipsoidName     = "Clarke 1880 (RGS)";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Indian 1954” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6239</b></li>
     *   <li>EPSG datum name: <b>Indian 1954</b></li>
     *   <li>Ellipsoid name: <b>Everest 1830 (1937 Adjustment)</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Asia - Myanmar and Thailand onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Indian 1954")
    public void EPSG_6239() throws FactoryException {
        code              = 6239;
        name              = "Indian 1954";
        ellipsoidName     = "Everest 1830 (1937 Adjustment)";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Indian 1960” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6131</b></li>
     *   <li>EPSG datum name: <b>Indian 1960</b></li>
     *   <li>Alias(es) given by EPSG: <b>Indian (DMA Reduced)</b></li>
     *   <li>Ellipsoid name: <b>Everest 1830 (1937 Adjustment)</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Asia - Cambodia and Vietnam - onshore &amp; Cuu Long basin</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Indian 1960")
    public void EPSG_6131() throws FactoryException {
        code              = 6131;
        name              = "Indian 1960";
        aliases           = new String[] {"Indian (DMA Reduced)"};
        ellipsoidName     = "Everest 1830 (1937 Adjustment)";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Indian 1975” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6240</b></li>
     *   <li>EPSG datum name: <b>Indian 1975</b></li>
     *   <li>Ellipsoid name: <b>Everest 1830 (1937 Adjustment)</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Thailand - onshore and Gulf of Thailand</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Indian 1975")
    public void EPSG_6240() throws FactoryException {
        code              = 6240;
        name              = "Indian 1975";
        ellipsoidName     = "Everest 1830 (1937 Adjustment)";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Indonesian Datum 1974” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6238</b></li>
     *   <li>EPSG datum name: <b>Indonesian Datum 1974</b></li>
     *   <li>Alias(es) given by EPSG: <b>ID74</b></li>
     *   <li>Ellipsoid name: <b>Indonesian National Spheroid</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Indonesia - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Indonesian Datum 1974")
    public void EPSG_6238() throws FactoryException {
        code              = 6238;
        name              = "Indonesian Datum 1974";
        aliases           = new String[] {"ID74"};
        ellipsoidName     = "Indonesian National Spheroid";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Institut Geographique du Congo Belge 1955” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6701</b></li>
     *   <li>EPSG datum name: <b>Institut Geographique du Congo Belge 1955</b></li>
     *   <li>Alias(es) given by EPSG: <b>IGCB 1955</b>, <b>Bas Congo 1955</b></li>
     *   <li>Ellipsoid name: <b>Clarke 1880 (RGS)</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Congo DR (Zaire) - Bas Congo</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Institut Geographique du Congo Belge 1955")
    public void EPSG_6701() throws FactoryException {
        code              = 6701;
        name              = "Institut Geographique du Congo Belge 1955";
        aliases           = new String[] {"IGCB 1955", "Bas Congo 1955"};
        ellipsoidName     = "Clarke 1880 (RGS)";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Iraq-Kuwait Boundary Datum 1992” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6667</b></li>
     *   <li>EPSG datum name: <b>Iraq-Kuwait Boundary Datum 1992</b></li>
     *   <li>Alias(es) given by EPSG: <b>IKBD-92</b></li>
     *   <li>Ellipsoid name: <b>WGS 84</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Asia - Middle East - Iraq-Kuwait boundary</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Iraq-Kuwait Boundary Datum 1992")
    public void EPSG_6667() throws FactoryException {
        code              = 6667;
        name              = "Iraq-Kuwait Boundary Datum 1992";
        aliases           = new String[] {"IKBD-92"};
        ellipsoidName     = "WGS 84";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “IRENET95” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6173</b></li>
     *   <li>EPSG datum name: <b>IRENET95</b></li>
     *   <li>Alias(es) given by EPSG: <b>ETRS89</b></li>
     *   <li>Ellipsoid name: <b>GRS 1980</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Europe - Ireland (Republic and Ulster) - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("IRENET95")
    public void EPSG_6173() throws FactoryException {
        code              = 6173;
        name              = "IRENET95";
        aliases           = new String[] {"ETRS89"};
        ellipsoidName     = "GRS 1980";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Islands Network 1993” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6659</b></li>
     *   <li>EPSG datum name: <b>Islands Network 1993</b></li>
     *   <li>Alias(es) given by EPSG: <b>ISN93</b></li>
     *   <li>Ellipsoid name: <b>GRS 1980</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Iceland</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Islands Network 1993")
    public void EPSG_6659() throws FactoryException {
        code              = 6659;
        name              = "Islands Network 1993";
        aliases           = new String[] {"ISN93"};
        ellipsoidName     = "GRS 1980";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Israel 1993” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6141</b></li>
     *   <li>EPSG datum name: <b>Israel 1993</b></li>
     *   <li>Ellipsoid name: <b>GRS 1980</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Asia - Middle East - Israel and Palestine Territory onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Israel 1993")
    public void EPSG_6141() throws FactoryException {
        code              = 6141;
        name              = "Israel 1993";
        ellipsoidName     = "GRS 1980";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Istituto Geografico Militaire 1995” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6670</b></li>
     *   <li>EPSG datum name: <b>Istituto Geografico Militaire 1995</b></li>
     *   <li>Alias(es) given by EPSG: <b>IGM95</b></li>
     *   <li>Ellipsoid name: <b>WGS 84</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Italy - including San Marino and Vatican</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Istituto Geografico Militaire 1995")
    public void EPSG_6670() throws FactoryException {
        code              = 6670;
        name              = "Istituto Geografico Militaire 1995";
        aliases           = new String[] {"IGM95"};
        ellipsoidName     = "WGS 84";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Iwo Jima 1945” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6709</b></li>
     *   <li>EPSG datum name: <b>Iwo Jima 1945</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Japan - Iwo Jima</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Iwo Jima 1945")
    public void EPSG_6709() throws FactoryException {
        code              = 6709;
        name              = "Iwo Jima 1945";
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Jamaica 1875” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6241</b></li>
     *   <li>EPSG datum name: <b>Jamaica 1875</b></li>
     *   <li>Ellipsoid name: <b>Clarke 1880</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Jamaica - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Jamaica 1875")
    public void EPSG_6241() throws FactoryException {
        code              = 6241;
        name              = "Jamaica 1875";
        ellipsoidName     = "Clarke 1880";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Jamaica 1969” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6242</b></li>
     *   <li>EPSG datum name: <b>Jamaica 1969</b></li>
     *   <li>Alias(es) given by EPSG: <b>JAD69</b></li>
     *   <li>Ellipsoid name: <b>Clarke 1866</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Jamaica - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Jamaica 1969")
    public void EPSG_6242() throws FactoryException {
        code              = 6242;
        name              = "Jamaica 1969";
        aliases           = new String[] {"JAD69"};
        ellipsoidName     = "Clarke 1866";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Jamaica 2001” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6758</b></li>
     *   <li>EPSG datum name: <b>Jamaica 2001</b></li>
     *   <li>Alias(es) given by EPSG: <b>JAD2001</b></li>
     *   <li>Ellipsoid name: <b>WGS 84</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Jamaica</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Jamaica 2001")
    public void EPSG_6758() throws FactoryException {
        code              = 6758;
        name              = "Jamaica 2001";
        aliases           = new String[] {"JAD2001"};
        ellipsoidName     = "WGS 84";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Japanese Geodetic Datum 2000” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6612</b></li>
     *   <li>EPSG datum name: <b>Japanese Geodetic Datum 2000</b></li>
     *   <li>Alias(es) given by EPSG: <b>JGD2000</b></li>
     *   <li>Ellipsoid name: <b>GRS 1980</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Japan</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Japanese Geodetic Datum 2000")
    public void EPSG_6612() throws FactoryException {
        code              = 6612;
        name              = "Japanese Geodetic Datum 2000";
        aliases           = new String[] {"JGD2000"};
        ellipsoidName     = "GRS 1980";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Johnston Island 1961” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6725</b></li>
     *   <li>EPSG datum name: <b>Johnston Island 1961</b></li>
     *   <li>Alias(es) given by EPSG: <b>Johnston Atoll 1961</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Johnston Island</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Johnston Island 1961")
    public void EPSG_6725() throws FactoryException {
        code              = 6725;
        name              = "Johnston Island 1961";
        aliases           = new String[] {"Johnston Atoll 1961"};
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Jouik 1961” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6679</b></li>
     *   <li>EPSG datum name: <b>Jouik 1961</b></li>
     *   <li>Ellipsoid name: <b>Clarke 1880 (RGS)</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Mauritania - north coast</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Jouik 1961")
    public void EPSG_6679() throws FactoryException {
        code              = 6679;
        name              = "Jouik 1961";
        ellipsoidName     = "Clarke 1880 (RGS)";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Kalianpur 1880” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6243</b></li>
     *   <li>EPSG datum name: <b>Kalianpur 1880</b></li>
     *   <li>Ellipsoid name: <b>Everest (1830 Definition)</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Asia - Bangladesh; India; Myanmar; Pakistan - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Kalianpur 1880")
    public void EPSG_6243() throws FactoryException {
        code              = 6243;
        name              = "Kalianpur 1880";
        ellipsoidName     = "Everest (1830 Definition)";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Kalianpur 1937” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6144</b></li>
     *   <li>EPSG datum name: <b>Kalianpur 1937</b></li>
     *   <li>Ellipsoid name: <b>Everest 1830 (1937 Adjustment)</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Asia - Bangladesh; India; Myanmar; Pakistan - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Kalianpur 1937")
    public void EPSG_6144() throws FactoryException {
        code              = 6144;
        name              = "Kalianpur 1937";
        ellipsoidName     = "Everest 1830 (1937 Adjustment)";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Kalianpur 1962” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6145</b></li>
     *   <li>EPSG datum name: <b>Kalianpur 1962</b></li>
     *   <li>Ellipsoid name: <b>Everest 1830 (1962 Definition)</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Pakistan</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Kalianpur 1962")
    public void EPSG_6145() throws FactoryException {
        code              = 6145;
        name              = "Kalianpur 1962";
        ellipsoidName     = "Everest 1830 (1962 Definition)";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Kalianpur 1975” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6146</b></li>
     *   <li>EPSG datum name: <b>Kalianpur 1975</b></li>
     *   <li>Ellipsoid name: <b>Everest 1830 (1975 Definition)</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>India - mainland</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Kalianpur 1975")
    public void EPSG_6146() throws FactoryException {
        code              = 6146;
        name              = "Kalianpur 1975";
        ellipsoidName     = "Everest 1830 (1975 Definition)";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Kandawala” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6244</b></li>
     *   <li>EPSG datum name: <b>Kandawala</b></li>
     *   <li>Ellipsoid name: <b>Everest 1830 (1937 Adjustment)</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Sri Lanka - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Kandawala")
    public void EPSG_6244() throws FactoryException {
        code              = 6244;
        name              = "Kandawala";
        ellipsoidName     = "Everest 1830 (1937 Adjustment)";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Karbala 1979” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6743</b></li>
     *   <li>EPSG datum name: <b>Karbala 1979</b></li>
     *   <li>Alias(es) given by EPSG: <b>Karbala 1979 (Polservice)</b></li>
     *   <li>Ellipsoid name: <b>Clarke 1880 (RGS)</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Iraq - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Karbala 1979")
    public void EPSG_6743() throws FactoryException {
        code              = 6743;
        name              = "Karbala 1979";
        aliases           = new String[] {"Karbala 1979 (Polservice)"};
        ellipsoidName     = "Clarke 1880 (RGS)";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Kartastokoordinaattijarjestelma (1966)” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6123</b></li>
     *   <li>EPSG datum name: <b>Kartastokoordinaattijarjestelma (1966)</b></li>
     *   <li>Alias(es) given by EPSG: <b>KKJ</b>, <b>Kartastokoordinaattijärjestelmä (1966)</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Finland - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Kartastokoordinaattijarjestelma (1966)")
    public void EPSG_6123() throws FactoryException {
        code              = 6123;
        name              = "Kartastokoordinaattijarjestelma (1966)";
        aliases           = new String[] {"KKJ", "Kartastokoordinaattijärjestelmä (1966)"};
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Kasai 1953” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6696</b></li>
     *   <li>EPSG datum name: <b>Kasai 1953</b></li>
     *   <li>Ellipsoid name: <b>Clarke 1880 (RGS)</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Congo DR (Zaire) - Kasai - SE</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Kasai 1953")
    public void EPSG_6696() throws FactoryException {
        code              = 6696;
        name              = "Kasai 1953";
        ellipsoidName     = "Clarke 1880 (RGS)";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Katanga 1955” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6695</b></li>
     *   <li>EPSG datum name: <b>Katanga 1955</b></li>
     *   <li>Ellipsoid name: <b>Clarke 1866</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Congo DR (Zaire) - Katanga</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Katanga 1955")
    public void EPSG_6695() throws FactoryException {
        code              = 6695;
        name              = "Katanga 1955";
        ellipsoidName     = "Clarke 1866";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Kertau (RSO)” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6751</b></li>
     *   <li>EPSG datum name: <b>Kertau (RSO)</b></li>
     *   <li>Ellipsoid name: <b>Everest 1830 (RSO 1969)</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Asia - Malaysia (west) and Singapore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Kertau (RSO)")
    public void EPSG_6751() throws FactoryException {
        code              = 6751;
        name              = "Kertau (RSO)";
        ellipsoidName     = "Everest 1830 (RSO 1969)";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Kertau 1968” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6245</b></li>
     *   <li>EPSG datum name: <b>Kertau 1968</b></li>
     *   <li>Alias(es) given by EPSG: <b>Malaysia Revised Triangulation 1968</b>, <b>MRT68</b></li>
     *   <li>Ellipsoid name: <b>Everest 1830 Modified</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Asia - Malaysia (west including SCS) and Singapore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Kertau 1968")
    public void EPSG_6245() throws FactoryException {
        code              = 6245;
        name              = "Kertau 1968";
        aliases           = new String[] {"Malaysia Revised Triangulation 1968", "MRT68"};
        ellipsoidName     = "Everest 1830 Modified";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Korean Datum 1985” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6162</b></li>
     *   <li>EPSG datum name: <b>Korean Datum 1985</b></li>
     *   <li>Ellipsoid name: <b>Bessel 1841</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Korea; Republic of (South Korea) - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Korean Datum 1985")
    public void EPSG_6162() throws FactoryException {
        code              = 6162;
        name              = "Korean Datum 1985";
        ellipsoidName     = "Bessel 1841";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Korean Datum 1995” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6166</b></li>
     *   <li>EPSG datum name: <b>Korean Datum 1995</b></li>
     *   <li>Ellipsoid name: <b>WGS 84</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Korea; Republic of (South Korea) - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Korean Datum 1995")
    public void EPSG_6166() throws FactoryException {
        code              = 6166;
        name              = "Korean Datum 1995";
        ellipsoidName     = "WGS 84";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Kousseri” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6198</b></li>
     *   <li>EPSG datum name: <b>Kousseri</b></li>
     *   <li>Ellipsoid name: <b>Clarke 1880 (RGS)</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Cameroon - N'Djamena area</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Kousseri")
    public void EPSG_6198() throws FactoryException {
        code              = 6198;
        name              = "Kousseri";
        ellipsoidName     = "Clarke 1880 (RGS)";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Kusaie 1951” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6735</b></li>
     *   <li>EPSG datum name: <b>Kusaie 1951</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Micronesia - Kosrae (Kusaie)</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Kusaie 1951")
    public void EPSG_6735() throws FactoryException {
        code              = 6735;
        name              = "Kusaie 1951";
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Kuwait Oil Company” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6246</b></li>
     *   <li>EPSG datum name: <b>Kuwait Oil Company</b></li>
     *   <li>Alias(es) given by EPSG: <b>KOC</b></li>
     *   <li>Ellipsoid name: <b>Clarke 1880 (RGS)</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Kuwait - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Kuwait Oil Company")
    public void EPSG_6246() throws FactoryException {
        code              = 6246;
        name              = "Kuwait Oil Company";
        aliases           = new String[] {"KOC"};
        ellipsoidName     = "Clarke 1880 (RGS)";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Kuwait Utility” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6319</b></li>
     *   <li>EPSG datum name: <b>Kuwait Utility</b></li>
     *   <li>Alias(es) given by EPSG: <b>KUDAMS</b></li>
     *   <li>Ellipsoid name: <b>GRS 1980</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Kuwait - Kuwait City</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Kuwait Utility")
    public void EPSG_6319() throws FactoryException {
        code              = 6319;
        name              = "Kuwait Utility";
        aliases           = new String[] {"KUDAMS"};
        ellipsoidName     = "GRS 1980";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “La Canoa” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6247</b></li>
     *   <li>EPSG datum name: <b>La Canoa</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Venezuela - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("La Canoa")
    public void EPSG_6247() throws FactoryException {
        code              = 6247;
        name              = "La Canoa";
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Lake” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6249</b></li>
     *   <li>EPSG datum name: <b>Lake</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Venezuela - Lake Maracaibo</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Lake")
    public void EPSG_6249() throws FactoryException {
        code              = 6249;
        name              = "Lake";
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Lao 1993” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6677</b></li>
     *   <li>EPSG datum name: <b>Lao 1993</b></li>
     *   <li>Ellipsoid name: <b>Krassowsky 1940</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Laos</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Lao 1993")
    public void EPSG_6677() throws FactoryException {
        code              = 6677;
        name              = "Lao 1993";
        ellipsoidName     = "Krassowsky 1940";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Lao National Datum 1997” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6678</b></li>
     *   <li>EPSG datum name: <b>Lao National Datum 1997</b></li>
     *   <li>Alias(es) given by EPSG: <b>Lao 1997</b></li>
     *   <li>Ellipsoid name: <b>Krassowsky 1940</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Laos</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Lao National Datum 1997")
    public void EPSG_6678() throws FactoryException {
        code              = 6678;
        name              = "Lao National Datum 1997";
        aliases           = new String[] {"Lao 1997"};
        ellipsoidName     = "Krassowsky 1940";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Latvia 1992” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6661</b></li>
     *   <li>EPSG datum name: <b>Latvia 1992</b></li>
     *   <li>Alias(es) given by EPSG: <b>LKS92</b></li>
     *   <li>Ellipsoid name: <b>GRS 1980</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Latvia</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Latvia 1992")
    public void EPSG_6661() throws FactoryException {
        code              = 6661;
        name              = "Latvia 1992";
        aliases           = new String[] {"LKS92"};
        ellipsoidName     = "GRS 1980";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Leigon” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6250</b></li>
     *   <li>EPSG datum name: <b>Leigon</b></li>
     *   <li>Ellipsoid name: <b>Clarke 1880 (RGS)</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Ghana</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Leigon")
    public void EPSG_6250() throws FactoryException {
        code              = 6250;
        name              = "Leigon";
        ellipsoidName     = "Clarke 1880 (RGS)";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Le Pouce 1934” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6699</b></li>
     *   <li>EPSG datum name: <b>Le Pouce 1934</b></li>
     *   <li>Alias(es) given by EPSG: <b>Le Pouce (Mauritius 94)</b>, <b>Le Pouce (Mauritius PN 94)</b></li>
     *   <li>Ellipsoid name: <b>Clarke 1880 (RGS)</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Mauritius - mainland</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Le Pouce 1934")
    public void EPSG_6699() throws FactoryException {
        code              = 6699;
        name              = "Le Pouce 1934";
        aliases           = new String[] {"Le Pouce (Mauritius 94)", "Le Pouce (Mauritius PN 94)"};
        ellipsoidName     = "Clarke 1880 (RGS)";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Liberia 1964” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6251</b></li>
     *   <li>EPSG datum name: <b>Liberia 1964</b></li>
     *   <li>Ellipsoid name: <b>Clarke 1880 (RGS)</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Liberia - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Liberia 1964")
    public void EPSG_6251() throws FactoryException {
        code              = 6251;
        name              = "Liberia 1964";
        ellipsoidName     = "Clarke 1880 (RGS)";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Libyan Geodetic Datum 2006” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6754</b></li>
     *   <li>EPSG datum name: <b>Libyan Geodetic Datum 2006</b></li>
     *   <li>Alias(es) given by EPSG: <b>LGD2006</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Libya</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Libyan Geodetic Datum 2006")
    public void EPSG_6754() throws FactoryException {
        code              = 6754;
        name              = "Libyan Geodetic Datum 2006";
        aliases           = new String[] {"LGD2006"};
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Lisbon 1890” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6666</b></li>
     *   <li>EPSG datum name: <b>Lisbon 1890</b></li>
     *   <li>Ellipsoid name: <b>Bessel 1841</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Portugal - mainland - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Lisbon 1890")
    public void EPSG_6666() throws FactoryException {
        code              = 6666;
        name              = "Lisbon 1890";
        ellipsoidName     = "Bessel 1841";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Lisbon 1890 (Lisbon)” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6904</b></li>
     *   <li>EPSG datum name: <b>Lisbon 1890 (Lisbon)</b></li>
     *   <li>Ellipsoid name: <b>Bessel 1841</b></li>
     *   <li>Prime meridian name: <b>Lisbon</b></li>
     *   <li>EPSG Usage Extent: <b>Portugal - mainland - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Lisbon 1890 (Lisbon)")
    public void EPSG_6904() throws FactoryException {
        code              = 6904;
        name              = "Lisbon 1890 (Lisbon)";
        ellipsoidName     = "Bessel 1841";
        primeMeridianName = "Lisbon";
        verifyDatum();
    }

    /**
     * Tests “Lisbon 1937” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6207</b></li>
     *   <li>EPSG datum name: <b>Lisbon 1937</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Portugal - mainland - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Lisbon 1937")
    public void EPSG_6207() throws FactoryException {
        code              = 6207;
        name              = "Lisbon 1937";
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Lisbon 1937 (Lisbon)” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6803</b></li>
     *   <li>EPSG datum name: <b>Lisbon 1937 (Lisbon)</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Lisbon</b></li>
     *   <li>EPSG Usage Extent: <b>Portugal - mainland - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Lisbon 1937 (Lisbon)")
    public void EPSG_6803() throws FactoryException {
        code              = 6803;
        name              = "Lisbon 1937 (Lisbon)";
        ellipsoidName     = "International 1924";
        primeMeridianName = "Lisbon";
        verifyDatum();
    }

    /**
     * Tests “Lithuania 1994 (ETRS89)” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6126</b></li>
     *   <li>EPSG datum name: <b>Lithuania 1994 (ETRS89)</b></li>
     *   <li>Alias(es) given by EPSG: <b>LKS94 (ETRS89)</b></li>
     *   <li>Ellipsoid name: <b>GRS 1980</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Lithuania</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Lithuania 1994 (ETRS89)")
    public void EPSG_6126() throws FactoryException {
        code              = 6126;
        name              = "Lithuania 1994 (ETRS89)";
        aliases           = new String[] {"LKS94 (ETRS89)"};
        ellipsoidName     = "GRS 1980";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Locodjo 1965” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6142</b></li>
     *   <li>EPSG datum name: <b>Locodjo 1965</b></li>
     *   <li>Alias(es) given by EPSG: <b>Côte d'Ivoire (Ivory Coast)</b></li>
     *   <li>Ellipsoid name: <b>Clarke 1880 (RGS)</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Cote d'Ivoire (Ivory Coast)</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Locodjo 1965")
    public void EPSG_6142() throws FactoryException {
        code              = 6142;
        name              = "Locodjo 1965";
        aliases           = new String[] {"Côte d'Ivoire (Ivory Coast)"};
        ellipsoidName     = "Clarke 1880 (RGS)";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Loma Quintana” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6288</b></li>
     *   <li>EPSG datum name: <b>Loma Quintana</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Venezuela - north of 7°45'N</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Loma Quintana")
    public void EPSG_6288() throws FactoryException {
        code              = 6288;
        name              = "Loma Quintana";
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Lome” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6252</b></li>
     *   <li>EPSG datum name: <b>Lome</b></li>
     *   <li>Ellipsoid name: <b>Clarke 1880 (IGN)</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Togo</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Lome")
    public void EPSG_6252() throws FactoryException {
        code              = 6252;
        name              = "Lome";
        ellipsoidName     = "Clarke 1880 (IGN)";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Luxembourg 1930” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6181</b></li>
     *   <li>EPSG datum name: <b>Luxembourg 1930</b></li>
     *   <li>Alias(es) given by EPSG: <b>LUREF</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Luxembourg</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Luxembourg 1930")
    public void EPSG_6181() throws FactoryException {
        code              = 6181;
        name              = "Luxembourg 1930";
        aliases           = new String[] {"LUREF"};
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Luzon 1911” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6253</b></li>
     *   <li>EPSG datum name: <b>Luzon 1911</b></li>
     *   <li>Ellipsoid name: <b>Clarke 1866</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Philippines - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Luzon 1911")
    public void EPSG_6253() throws FactoryException {
        code              = 6253;
        name              = "Luzon 1911";
        ellipsoidName     = "Clarke 1866";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “M'poraloko” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6266</b></li>
     *   <li>EPSG datum name: <b>M'poraloko</b></li>
     *   <li>Ellipsoid name: <b>Clarke 1880 (IGN)</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Gabon</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("M'poraloko")
    public void EPSG_6266() throws FactoryException {
        code              = 6266;
        name              = "M'poraloko";
        ellipsoidName     = "Clarke 1880 (IGN)";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Madrid 1870 (Madrid)” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6903</b></li>
     *   <li>EPSG datum name: <b>Madrid 1870 (Madrid)</b></li>
     *   <li>Alias(es) given by EPSG: <b>Madrid</b></li>
     *   <li>Ellipsoid name: <b>Struve 1860</b></li>
     *   <li>Prime meridian name: <b>Madrid</b></li>
     *   <li>EPSG Usage Extent: <b>Spain - mainland onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Madrid 1870 (Madrid)")
    public void EPSG_6903() throws FactoryException {
        code              = 6903;
        name              = "Madrid 1870 (Madrid)";
        aliases           = new String[] {"Madrid"};
        ellipsoidName     = "Struve 1860";
        primeMeridianName = "Madrid";
        verifyDatum();
    }

    /**
     * Tests “Madzansua” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6128</b></li>
     *   <li>EPSG datum name: <b>Madzansua</b></li>
     *   <li>Ellipsoid name: <b>Clarke 1866</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Mozambique - west - Tete province</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Madzansua")
    public void EPSG_6128() throws FactoryException {
        code              = 6128;
        name              = "Madzansua";
        ellipsoidName     = "Clarke 1866";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Mahe 1971” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6256</b></li>
     *   <li>EPSG datum name: <b>Mahe 1971</b></li>
     *   <li>Ellipsoid name: <b>Clarke 1880 (RGS)</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Seychelles - Mahe Island</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Mahe 1971")
    public void EPSG_6256() throws FactoryException {
        code              = 6256;
        name              = "Mahe 1971";
        ellipsoidName     = "Clarke 1880 (RGS)";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Makassar” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6257</b></li>
     *   <li>EPSG datum name: <b>Makassar</b></li>
     *   <li>Ellipsoid name: <b>Bessel 1841</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Indonesia - Sulawesi SW</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Makassar")
    public void EPSG_6257() throws FactoryException {
        code              = 6257;
        name              = "Makassar";
        ellipsoidName     = "Bessel 1841";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Makassar (Jakarta)” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6804</b></li>
     *   <li>EPSG datum name: <b>Makassar (Jakarta)</b></li>
     *   <li>Ellipsoid name: <b>Bessel 1841</b></li>
     *   <li>Prime meridian name: <b>Jakarta</b></li>
     *   <li>EPSG Usage Extent: <b>Indonesia - Sulawesi SW</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Makassar (Jakarta)")
    public void EPSG_6804() throws FactoryException {
        code              = 6804;
        name              = "Makassar (Jakarta)";
        ellipsoidName     = "Bessel 1841";
        primeMeridianName = "Jakarta";
        verifyDatum();
    }

    /**
     * Tests “Malongo 1987” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6259</b></li>
     *   <li>EPSG datum name: <b>Malongo 1987</b></li>
     *   <li>Alias(es) given by EPSG: <b>Mhast</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Africa - Angola (Cabinda) and DR Congo (Zaire) - offshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Malongo 1987")
    public void EPSG_6259() throws FactoryException {
        code              = 6259;
        name              = "Malongo 1987";
        aliases           = new String[] {"Mhast"};
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Manoca 1962” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6193</b></li>
     *   <li>EPSG datum name: <b>Manoca 1962</b></li>
     *   <li>Ellipsoid name: <b>Clarke 1880 (IGN)</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Cameroon - coastal area</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Manoca 1962")
    public void EPSG_6193() throws FactoryException {
        code              = 6193;
        name              = "Manoca 1962";
        ellipsoidName     = "Clarke 1880 (IGN)";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Marco Geocentrico Nacional de Referencia” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6686</b></li>
     *   <li>EPSG datum name: <b>Marco Geocentrico Nacional de Referencia</b></li>
     *   <li>Alias(es) given by EPSG: <b>MAGNA-SIRGAS</b></li>
     *   <li>Ellipsoid name: <b>GRS 1980</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Colombia</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Marco Geocentrico Nacional de Referencia")
    public void EPSG_6686() throws FactoryException {
        code              = 6686;
        name              = "Marco Geocentrico Nacional de Referencia";
        aliases           = new String[] {"MAGNA-SIRGAS"};
        ellipsoidName     = "GRS 1980";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Marcus Island 1952” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6711</b></li>
     *   <li>EPSG datum name: <b>Marcus Island 1952</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Japan - Minamitori-shima (Marcus Island) - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Marcus Island 1952")
    public void EPSG_6711() throws FactoryException {
        code              = 6711;
        name              = "Marcus Island 1952";
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Marshall Islands 1960” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6732</b></li>
     *   <li>EPSG datum name: <b>Marshall Islands 1960</b></li>
     *   <li>Ellipsoid name: <b>Hough 1960</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Pacific - Marshall Islands; Wake - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Marshall Islands 1960")
    public void EPSG_6732() throws FactoryException {
        code              = 6732;
        name              = "Marshall Islands 1960";
        ellipsoidName     = "Hough 1960";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Martinique 1938” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6625</b></li>
     *   <li>EPSG datum name: <b>Martinique 1938</b></li>
     *   <li>Alias(es) given by EPSG: <b>Fort Desaix</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Martinique - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Martinique 1938")
    public void EPSG_6625() throws FactoryException {
        code              = 6625;
        name              = "Martinique 1938";
        aliases           = new String[] {"Fort Desaix"};
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Massawa” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6262</b></li>
     *   <li>EPSG datum name: <b>Massawa</b></li>
     *   <li>Ellipsoid name: <b>Bessel 1841</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Eritrea</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Massawa")
    public void EPSG_6262() throws FactoryException {
        code              = 6262;
        name              = "Massawa";
        ellipsoidName     = "Bessel 1841";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Maupiti 83” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6692</b></li>
     *   <li>EPSG datum name: <b>Maupiti 83</b></li>
     *   <li>Alias(es) given by EPSG: <b>MOP 1983</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>French Polynesia - Society Islands - Maupiti</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Maupiti 83")
    public void EPSG_6692() throws FactoryException {
        code              = 6692;
        name              = "Maupiti 83";
        aliases           = new String[] {"MOP 1983"};
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Mauritania 1999” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6702</b></li>
     *   <li>EPSG datum name: <b>Mauritania 1999</b></li>
     *   <li>Ellipsoid name: <b>GRS 1980</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Mauritania</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Mauritania 1999")
    public void EPSG_6702() throws FactoryException {
        code              = 6702;
        name              = "Mauritania 1999";
        ellipsoidName     = "GRS 1980";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Merchich” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6261</b></li>
     *   <li>EPSG datum name: <b>Merchich</b></li>
     *   <li>Ellipsoid name: <b>Clarke 1880 (IGN)</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Africa - Morocco and Western Sahara - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Merchich")
    public void EPSG_6261() throws FactoryException {
        code              = 6261;
        name              = "Merchich";
        ellipsoidName     = "Clarke 1880 (IGN)";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Mhast (offshore)” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6705</b></li>
     *   <li>EPSG datum name: <b>Mhast (offshore)</b></li>
     *   <li>Alias(es) given by EPSG: <b>Mhast</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Africa - Angola (Cabinda) and DR Congo (Zaire) - offshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Mhast (offshore)")
    public void EPSG_6705() throws FactoryException {
        code              = 6705;
        name              = "Mhast (offshore)";
        aliases           = new String[] {"Mhast"};
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Mhast (onshore)” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6704</b></li>
     *   <li>EPSG datum name: <b>Mhast (onshore)</b></li>
     *   <li>Alias(es) given by EPSG: <b>Mhast</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Africa - Angola (Cabinda) and DR Congo (Zaire) - coastal</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Mhast (onshore)")
    public void EPSG_6704() throws FactoryException {
        code              = 6704;
        name              = "Mhast (onshore)";
        aliases           = new String[] {"Mhast"};
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Midway 1961” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6727</b></li>
     *   <li>EPSG datum name: <b>Midway 1961</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Midway Islands - Sand and Eastern Islands</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Midway 1961")
    public void EPSG_6727() throws FactoryException {
        code              = 6727;
        name              = "Midway 1961";
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Militar-Geographische Institut” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6312</b></li>
     *   <li>EPSG datum name: <b>Militar-Geographische Institut</b></li>
     *   <li>Alias(es) given by EPSG: <b>MGI</b></li>
     *   <li>Ellipsoid name: <b>Bessel 1841</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Austria</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Militar-Geographische Institut")
    public void EPSG_6312() throws FactoryException {
        code              = 6312;
        name              = "Militar-Geographische Institut";
        aliases           = new String[] {"MGI"};
        ellipsoidName     = "Bessel 1841";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Militar-Geographische Institut (Ferro)” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6805</b></li>
     *   <li>EPSG datum name: <b>Militar-Geographische Institut (Ferro)</b></li>
     *   <li>Alias(es) given by EPSG: <b>MGI (Ferro)</b></li>
     *   <li>Ellipsoid name: <b>Bessel 1841</b></li>
     *   <li>Prime meridian name: <b>Ferro</b></li>
     *   <li>EPSG Usage Extent: <b>Europe - Austria and former Yugoslavia onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Militar-Geographische Institut (Ferro)")
    public void EPSG_6805() throws FactoryException {
        code              = 6805;
        name              = "Militar-Geographische Institut (Ferro)";
        aliases           = new String[] {"MGI (Ferro)"};
        ellipsoidName     = "Bessel 1841";
        primeMeridianName = "Ferro";
        verifyDatum();
    }

    /**
     * Tests “Minna” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6263</b></li>
     *   <li>EPSG datum name: <b>Minna</b></li>
     *   <li>Ellipsoid name: <b>Clarke 1880 (RGS)</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Nigeria</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Minna")
    public void EPSG_6263() throws FactoryException {
        code              = 6263;
        name              = "Minna";
        ellipsoidName     = "Clarke 1880 (RGS)";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Missao Hidrografico Angola y Sao Tome 1951” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6703</b></li>
     *   <li>EPSG datum name: <b>Missao Hidrografico Angola y Sao Tome 1951</b></li>
     *   <li>Alias(es) given by EPSG: <b>Mhast 1951</b></li>
     *   <li>Ellipsoid name: <b>Clarke 1880 (RGS)</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Angola - Cabinda</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Missao Hidrografico Angola y Sao Tome 1951")
    public void EPSG_6703() throws FactoryException {
        code              = 6703;
        name              = "Missao Hidrografico Angola y Sao Tome 1951";
        aliases           = new String[] {"Mhast 1951"};
        ellipsoidName     = "Clarke 1880 (RGS)";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Monte Mario” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6265</b></li>
     *   <li>EPSG datum name: <b>Monte Mario</b></li>
     *   <li>Alias(es) given by EPSG: <b>Rome 1940</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Italy - including San Marino and Vatican</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Monte Mario")
    public void EPSG_6265() throws FactoryException {
        code              = 6265;
        name              = "Monte Mario";
        aliases           = new String[] {"Rome 1940"};
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Monte Mario (Rome)” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6806</b></li>
     *   <li>EPSG datum name: <b>Monte Mario (Rome)</b></li>
     *   <li>Alias(es) given by EPSG: <b>Rome 1940 (Rome)</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Rome</b></li>
     *   <li>EPSG Usage Extent: <b>Italy - including San Marino and Vatican</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Monte Mario (Rome)")
    public void EPSG_6806() throws FactoryException {
        code              = 6806;
        name              = "Monte Mario (Rome)";
        aliases           = new String[] {"Rome 1940 (Rome)"};
        ellipsoidName     = "International 1924";
        primeMeridianName = "Rome";
        verifyDatum();
    }

    /**
     * Tests “Montserrat 1958” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6604</b></li>
     *   <li>EPSG datum name: <b>Montserrat 1958</b></li>
     *   <li>Ellipsoid name: <b>Clarke 1880 (RGS)</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Montserrat - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Montserrat 1958")
    public void EPSG_6604() throws FactoryException {
        code              = 6604;
        name              = "Montserrat 1958";
        ellipsoidName     = "Clarke 1880 (RGS)";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Moorea 87” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6691</b></li>
     *   <li>EPSG datum name: <b>Moorea 87</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>French Polynesia - Society Islands - Moorea</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Moorea 87")
    public void EPSG_6691() throws FactoryException {
        code              = 6691;
        name              = "Moorea 87";
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “MOP78” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6639</b></li>
     *   <li>EPSG datum name: <b>MOP78</b></li>
     *   <li>Alias(es) given by EPSG: <b>Uvea SHOM 1978</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Wallis and Futuna - Wallis</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("MOP78")
    public void EPSG_6639() throws FactoryException {
        code              = 6639;
        name              = "MOP78";
        aliases           = new String[] {"Uvea SHOM 1978"};
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Mount Dillon” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6157</b></li>
     *   <li>EPSG datum name: <b>Mount Dillon</b></li>
     *   <li>Ellipsoid name: <b>Clarke 1858</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Trinidad and Tobago - Tobago - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Mount Dillon")
    public void EPSG_6157() throws FactoryException {
        code              = 6157;
        name              = "Mount Dillon";
        ellipsoidName     = "Clarke 1858";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Moznet (ITRF94)” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6130</b></li>
     *   <li>EPSG datum name: <b>Moznet (ITRF94)</b></li>
     *   <li>Alias(es) given by EPSG: <b>Moznet</b></li>
     *   <li>Ellipsoid name: <b>WGS 84</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Mozambique</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Moznet (ITRF94)")
    public void EPSG_6130() throws FactoryException {
        code              = 6130;
        name              = "Moznet (ITRF94)";
        aliases           = new String[] {"Moznet"};
        ellipsoidName     = "WGS 84";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “NAD83 (High Accuracy Regional Network)” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6152</b></li>
     *   <li>EPSG datum name: <b>NAD83 (High Accuracy Regional Network)</b></li>
     *   <li>Alias(es) given by EPSG: <b>NAD83(HARN)</b>, <b>NAD83 (High Precision Geodetic Network)</b>, <b>NAD83(HPGN)</b>, <b>Guam Geodetic Network 1993</b>, <b>NAD83</b></li>
     *   <li>Ellipsoid name: <b>GRS 1980</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>USA - HARN</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("NAD83 (High Accuracy Regional Network)")
    public void EPSG_6152() throws FactoryException {
        code              = 6152;
        name              = "NAD83 (High Accuracy Regional Network)";
        aliases           = new String[] {"NAD83(HARN)", "NAD83 (High Precision Geodetic Network)", "NAD83(HPGN)", "Guam Geodetic Network 1993", "NAD83"};
        ellipsoidName     = "GRS 1980";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “NAD83 (National Spatial Reference System 2007)” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6759</b></li>
     *   <li>EPSG datum name: <b>NAD83 (National Spatial Reference System 2007)</b></li>
     *   <li>Ellipsoid name: <b>GRS 1980</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>USA - CONUS and Alaska; PRVI</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("NAD83 (National Spatial Reference System 2007)")
    public void EPSG_6759() throws FactoryException {
        code              = 6759;
        name              = "NAD83 (National Spatial Reference System 2007)";
        ellipsoidName     = "GRS 1980";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “NAD83 Canadian Spatial Reference System” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6140</b></li>
     *   <li>EPSG datum name: <b>NAD83 Canadian Spatial Reference System</b></li>
     *   <li>Alias(es) given by EPSG: <b>NAD83(CSRS)</b></li>
     *   <li>Ellipsoid name: <b>GRS 1980</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Canada</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("NAD83 Canadian Spatial Reference System")
    public void EPSG_6140() throws FactoryException {
        code              = 6140;
        name              = "NAD83 Canadian Spatial Reference System";
        aliases           = new String[] {"NAD83(CSRS)"};
        ellipsoidName     = "GRS 1980";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Nahrwan 1934” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6744</b></li>
     *   <li>EPSG datum name: <b>Nahrwan 1934</b></li>
     *   <li>Ellipsoid name: <b>Clarke 1880 (RGS)</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Asia - Middle East - Iraq and SW Iran</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Nahrwan 1934")
    public void EPSG_6744() throws FactoryException {
        code              = 6744;
        name              = "Nahrwan 1934";
        ellipsoidName     = "Clarke 1880 (RGS)";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Nahrwan 1967” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6270</b></li>
     *   <li>EPSG datum name: <b>Nahrwan 1967</b></li>
     *   <li>Ellipsoid name: <b>Clarke 1880 (RGS)</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Asia - Middle East - Qatar offshore and UAE</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Nahrwan 1967")
    public void EPSG_6270() throws FactoryException {
        code              = 6270;
        name              = "Nahrwan 1967";
        ellipsoidName     = "Clarke 1880 (RGS)";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Nakhl-e Ghanem” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6693</b></li>
     *   <li>EPSG datum name: <b>Nakhl-e Ghanem</b></li>
     *   <li>Ellipsoid name: <b>WGS 84</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Iran - Kangan district</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Nakhl-e Ghanem")
    public void EPSG_6693() throws FactoryException {
        code              = 6693;
        name              = "Nakhl-e Ghanem";
        ellipsoidName     = "WGS 84";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Naparima 1955” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6158</b></li>
     *   <li>EPSG datum name: <b>Naparima 1955</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Trinidad and Tobago - Trinidad - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Naparima 1955")
    public void EPSG_6158() throws FactoryException {
        code              = 6158;
        name              = "Naparima 1955";
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Naparima 1972” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6271</b></li>
     *   <li>EPSG datum name: <b>Naparima 1972</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Trinidad and Tobago - Tobago - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Naparima 1972")
    public void EPSG_6271() throws FactoryException {
        code              = 6271;
        name              = "Naparima 1972";
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “National Geodetic Network” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6318</b></li>
     *   <li>EPSG datum name: <b>National Geodetic Network</b></li>
     *   <li>Alias(es) given by EPSG: <b>NGN</b></li>
     *   <li>Ellipsoid name: <b>WGS 84</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Kuwait - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("National Geodetic Network")
    public void EPSG_6318() throws FactoryException {
        code              = 6318;
        name              = "National Geodetic Network";
        aliases           = new String[] {"NGN"};
        ellipsoidName     = "WGS 84";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “NEA74 Noumea” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6644</b></li>
     *   <li>EPSG datum name: <b>NEA74 Noumea</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>New Caledonia - Grande Terre - Noumea</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("NEA74 Noumea")
    public void EPSG_6644() throws FactoryException {
        code              = 6644;
        name              = "NEA74 Noumea";
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “New Zealand Geodetic Datum 1949” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6272</b></li>
     *   <li>EPSG datum name: <b>New Zealand Geodetic Datum 1949</b></li>
     *   <li>Alias(es) given by EPSG: <b>GD49</b>, <b>NZGD49</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>New Zealand - onshore and nearshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("New Zealand Geodetic Datum 1949")
    public void EPSG_6272() throws FactoryException {
        code              = 6272;
        name              = "New Zealand Geodetic Datum 1949";
        aliases           = new String[] {"GD49", "NZGD49"};
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “New Zealand Geodetic Datum 2000” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6167</b></li>
     *   <li>EPSG datum name: <b>New Zealand Geodetic Datum 2000</b></li>
     *   <li>Alias(es) given by EPSG: <b>NZGD2000</b></li>
     *   <li>Ellipsoid name: <b>GRS 1980</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>New Zealand</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("New Zealand Geodetic Datum 2000")
    public void EPSG_6167() throws FactoryException {
        code              = 6167;
        name              = "New Zealand Geodetic Datum 2000";
        aliases           = new String[] {"NZGD2000"};
        ellipsoidName     = "GRS 1980";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “NGO 1948” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6273</b></li>
     *   <li>EPSG datum name: <b>NGO 1948</b></li>
     *   <li>Ellipsoid name: <b>Bessel Modified</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Norway - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("NGO 1948")
    public void EPSG_6273() throws FactoryException {
        code              = 6273;
        name              = "NGO 1948";
        ellipsoidName     = "Bessel Modified";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “NGO 1948 (Oslo)” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6817</b></li>
     *   <li>EPSG datum name: <b>NGO 1948 (Oslo)</b></li>
     *   <li>Ellipsoid name: <b>Bessel Modified</b></li>
     *   <li>Prime meridian name: <b>Oslo</b></li>
     *   <li>EPSG Usage Extent: <b>Norway - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("NGO 1948 (Oslo)")
    public void EPSG_6817() throws FactoryException {
        code              = 6817;
        name              = "NGO 1948 (Oslo)";
        ellipsoidName     = "Bessel Modified";
        primeMeridianName = "Oslo";
        verifyDatum();
    }

    /**
     * Tests “Nord Sahara 1959” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6307</b></li>
     *   <li>EPSG datum name: <b>Nord Sahara 1959</b></li>
     *   <li>Ellipsoid name: <b>Clarke 1880 (RGS)</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Algeria</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Nord Sahara 1959")
    public void EPSG_6307() throws FactoryException {
        code              = 6307;
        name              = "Nord Sahara 1959";
        ellipsoidName     = "Clarke 1880 (RGS)";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “North American Datum 1927” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6267</b></li>
     *   <li>EPSG datum name: <b>North American Datum 1927</b></li>
     *   <li>Alias(es) given by EPSG: <b>NAD27</b></li>
     *   <li>Ellipsoid name: <b>Clarke 1866</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>North America - NAD27</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("North American Datum 1927")
    public void EPSG_6267() throws FactoryException {
        code              = 6267;
        name              = "North American Datum 1927";
        aliases           = new String[] {"NAD27"};
        ellipsoidName     = "Clarke 1866";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “North American Datum 1927 (1976)” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6608</b></li>
     *   <li>EPSG datum name: <b>North American Datum 1927 (1976)</b></li>
     *   <li>Alias(es) given by EPSG: <b>NAD27(76)</b></li>
     *   <li>Ellipsoid name: <b>Clarke 1866</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Canada - Ontario</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("North American Datum 1927 (1976)")
    public void EPSG_6608() throws FactoryException {
        code              = 6608;
        name              = "North American Datum 1927 (1976)";
        aliases           = new String[] {"NAD27(76)"};
        ellipsoidName     = "Clarke 1866";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “North American Datum 1927 (CGQ77)” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6609</b></li>
     *   <li>EPSG datum name: <b>North American Datum 1927 (CGQ77)</b></li>
     *   <li>Alias(es) given by EPSG: <b>CGQ77</b></li>
     *   <li>Ellipsoid name: <b>Clarke 1866</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Canada - Quebec</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("North American Datum 1927 (CGQ77)")
    public void EPSG_6609() throws FactoryException {
        code              = 6609;
        name              = "North American Datum 1927 (CGQ77)";
        aliases           = new String[] {"CGQ77"};
        ellipsoidName     = "Clarke 1866";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “North American Datum 1983” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6269</b></li>
     *   <li>EPSG datum name: <b>North American Datum 1983</b></li>
     *   <li>Alias(es) given by EPSG: <b>NAD83(1986)</b>, <b>NAD83</b>, <b>NAD83(Original)</b></li>
     *   <li>Ellipsoid name: <b>GRS 1980</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>North America - NAD83</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("North American Datum 1983")
    public void EPSG_6269() throws FactoryException {
        code              = 6269;
        name              = "North American Datum 1983";
        aliases           = new String[] {"NAD83(1986)", "NAD83", "NAD83(Original)"};
        ellipsoidName     = "GRS 1980";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Nouakchott 1965” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6680</b></li>
     *   <li>EPSG datum name: <b>Nouakchott 1965</b></li>
     *   <li>Ellipsoid name: <b>Clarke 1880 (RGS)</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Mauritania - central coast</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Nouakchott 1965")
    public void EPSG_6680() throws FactoryException {
        code              = 6680;
        name              = "Nouakchott 1965";
        ellipsoidName     = "Clarke 1880 (RGS)";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Nouvelle Triangulation Francaise” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6275</b></li>
     *   <li>EPSG datum name: <b>Nouvelle Triangulation Francaise</b></li>
     *   <li>Alias(es) given by EPSG: <b>NTF</b></li>
     *   <li>Ellipsoid name: <b>Clarke 1880 (IGN)</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>France - onshore - mainland and Corsica</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Nouvelle Triangulation Francaise")
    public void EPSG_6275() throws FactoryException {
        code              = 6275;
        name              = "Nouvelle Triangulation Francaise";
        aliases           = new String[] {"NTF"};
        ellipsoidName     = "Clarke 1880 (IGN)";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Nouvelle Triangulation Francaise (Paris)” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6807</b></li>
     *   <li>EPSG datum name: <b>Nouvelle Triangulation Francaise (Paris)</b></li>
     *   <li>Ellipsoid name: <b>Clarke 1880 (IGN)</b></li>
     *   <li>Prime meridian name: <b>Paris</b></li>
     *   <li>EPSG Usage Extent: <b>France - onshore - mainland and Corsica</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Nouvelle Triangulation Francaise (Paris)")
    public void EPSG_6807() throws FactoryException {
        code              = 6807;
        name              = "Nouvelle Triangulation Francaise (Paris)";
        ellipsoidName     = "Clarke 1880 (IGN)";
        primeMeridianName = "Paris";
        verifyDatum();
    }

    /**
     * Tests “NSWC 9Z-2” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6276</b></li>
     *   <li>EPSG datum name: <b>NSWC 9Z-2</b></li>
     *   <li>Ellipsoid name: <b>NWL 9D</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>World</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("NSWC 9Z-2")
    public void EPSG_6276() throws FactoryException {
        code              = 6276;
        name              = "NSWC 9Z-2";
        ellipsoidName     = "NWL 9D";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Observatario” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6129</b></li>
     *   <li>EPSG datum name: <b>Observatario</b></li>
     *   <li>Ellipsoid name: <b>Clarke 1866</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Mozambique - south</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Observatario")
    public void EPSG_6129() throws FactoryException {
        code              = 6129;
        name              = "Observatario";
        ellipsoidName     = "Clarke 1866";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Old Hawaiian” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6135</b></li>
     *   <li>EPSG datum name: <b>Old Hawaiian</b></li>
     *   <li>Ellipsoid name: <b>Clarke 1866</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>USA - Hawaii - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Old Hawaiian")
    public void EPSG_6135() throws FactoryException {
        code              = 6135;
        name              = "Old Hawaiian";
        ellipsoidName     = "Clarke 1866";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Ordnance Survey of Great Britain 1936” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6277</b></li>
     *   <li>EPSG datum name: <b>Ordnance Survey of Great Britain 1936</b></li>
     *   <li>Ellipsoid name: <b>Airy 1830</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>UK - Britain and UKCS 49°45'N to 61°N; 9°W to 2°E</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Ordnance Survey of Great Britain 1936")
    public void EPSG_6277() throws FactoryException {
        code              = 6277;
        name              = "Ordnance Survey of Great Britain 1936";
        ellipsoidName     = "Airy 1830";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “OS (SN) 1980” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6279</b></li>
     *   <li>EPSG datum name: <b>OS (SN) 1980</b></li>
     *   <li>Alias(es) given by EPSG: <b>OS(SN)80</b></li>
     *   <li>Ellipsoid name: <b>Airy 1830</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Europe - British Isles - UK and Ireland onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("OS (SN) 1980")
    public void EPSG_6279() throws FactoryException {
        code              = 6279;
        name              = "OS (SN) 1980";
        aliases           = new String[] {"OS(SN)80"};
        ellipsoidName     = "Airy 1830";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “OSGB 1970 (SN)” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6278</b></li>
     *   <li>EPSG datum name: <b>OSGB 1970 (SN)</b></li>
     *   <li>Alias(es) given by EPSG: <b>OSGB70</b></li>
     *   <li>Ellipsoid name: <b>Airy 1830</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>UK - Great Britain; Isle of Man</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("OSGB 1970 (SN)")
    public void EPSG_6278() throws FactoryException {
        code              = 6278;
        name              = "OSGB 1970 (SN)";
        aliases           = new String[] {"OSGB70"};
        ellipsoidName     = "Airy 1830";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “OSNI 1952” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6188</b></li>
     *   <li>EPSG datum name: <b>OSNI 1952</b></li>
     *   <li>Ellipsoid name: <b>Airy 1830</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>UK - Northern Ireland - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("OSNI 1952")
    public void EPSG_6188() throws FactoryException {
        code              = 6188;
        name              = "OSNI 1952";
        ellipsoidName     = "Airy 1830";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Palestine 1923” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6281</b></li>
     *   <li>EPSG datum name: <b>Palestine 1923</b></li>
     *   <li>Alias(es) given by EPSG: <b>Old Israeli Datum</b>, <b>OID</b></li>
     *   <li>Ellipsoid name: <b>Clarke 1880 (Benoit)</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Asia - Middle East - Israel; Jordan and Palestine onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Palestine 1923")
    public void EPSG_6281() throws FactoryException {
        code              = 6281;
        name              = "Palestine 1923";
        aliases           = new String[] {"Old Israeli Datum", "OID"};
        ellipsoidName     = "Clarke 1880 (Benoit)";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Pampa del Castillo” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6161</b></li>
     *   <li>EPSG datum name: <b>Pampa del Castillo</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Argentina - 42.5°S to 50.3°S</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Pampa del Castillo")
    public void EPSG_6161() throws FactoryException {
        code              = 6161;
        name              = "Pampa del Castillo";
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Parametrop Zemp 1990” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6740</b></li>
     *   <li>EPSG datum name: <b>Parametrop Zemp 1990</b></li>
     *   <li>Alias(es) given by EPSG: <b>PZ-90</b></li>
     *   <li>Ellipsoid name: <b>PZ-90</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>World</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Parametrop Zemp 1990")
    public void EPSG_6740() throws FactoryException {
        code              = 6740;
        name              = "Parametrop Zemp 1990";
        aliases           = new String[] {"PZ-90"};
        ellipsoidName     = "PZ-90";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “PDO Survey Datum 1993” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6134</b></li>
     *   <li>EPSG datum name: <b>PDO Survey Datum 1993</b></li>
     *   <li>Alias(es) given by EPSG: <b>PSD93</b></li>
     *   <li>Ellipsoid name: <b>Clarke 1880 (RGS)</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Oman - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("PDO Survey Datum 1993")
    public void EPSG_6134() throws FactoryException {
        code              = 6134;
        name              = "PDO Survey Datum 1993";
        aliases           = new String[] {"PSD93"};
        ellipsoidName     = "Clarke 1880 (RGS)";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Petrels 1972” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6636</b></li>
     *   <li>EPSG datum name: <b>Petrels 1972</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Antarctica - Adelie Land - Petrels island</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Petrels 1972")
    public void EPSG_6636() throws FactoryException {
        code              = 6636;
        name              = "Petrels 1972";
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Philippine Reference System 1992” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6683</b></li>
     *   <li>EPSG datum name: <b>Philippine Reference System 1992</b></li>
     *   <li>Alias(es) given by EPSG: <b>PRS92</b>, <b>Modified Luzon Datum</b></li>
     *   <li>Ellipsoid name: <b>Clarke 1866</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Philippines</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Philippine Reference System 1992")
    public void EPSG_6683() throws FactoryException {
        code              = 6683;
        name              = "Philippine Reference System 1992";
        aliases           = new String[] {"PRS92", "Modified Luzon Datum"};
        ellipsoidName     = "Clarke 1866";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Phoenix Islands 1966” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6716</b></li>
     *   <li>EPSG datum name: <b>Phoenix Islands 1966</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Kiribati - Phoenix Islands</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Phoenix Islands 1966")
    public void EPSG_6716() throws FactoryException {
        code              = 6716;
        name              = "Phoenix Islands 1966";
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Pico de la Nieves 1984” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6728</b></li>
     *   <li>EPSG datum name: <b>Pico de la Nieves 1984</b></li>
     *   <li>Alias(es) given by EPSG: <b>PN84</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Spain - Canary Islands western</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Pico de la Nieves 1984")
    public void EPSG_6728() throws FactoryException {
        code              = 6728;
        name              = "Pico de la Nieves 1984";
        aliases           = new String[] {"PN84"};
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Pitcairn 1967” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6729</b></li>
     *   <li>EPSG datum name: <b>Pitcairn 1967</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Pitcairn - Pitcairn Island</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Pitcairn 1967")
    public void EPSG_6729() throws FactoryException {
        code              = 6729;
        name              = "Pitcairn 1967";
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Pitcairn 2006” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6763</b></li>
     *   <li>EPSG datum name: <b>Pitcairn 2006</b></li>
     *   <li>Ellipsoid name: <b>WGS 84</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Pitcairn - Pitcairn Island</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Pitcairn 2006")
    public void EPSG_6763() throws FactoryException {
        code              = 6763;
        name              = "Pitcairn 2006";
        ellipsoidName     = "WGS 84";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Point 58” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6620</b></li>
     *   <li>EPSG datum name: <b>Point 58</b></li>
     *   <li>Alias(es) given by EPSG: <b>12th Parallel traverse</b></li>
     *   <li>Ellipsoid name: <b>Clarke 1880 (RGS)</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Africa - 12th parallel N</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Point 58")
    public void EPSG_6620() throws FactoryException {
        code              = 6620;
        name              = "Point 58";
        aliases           = new String[] {"12th Parallel traverse"};
        ellipsoidName     = "Clarke 1880 (RGS)";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Pointe Geologie Perroud 1950” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6637</b></li>
     *   <li>EPSG datum name: <b>Pointe Geologie Perroud 1950</b></li>
     *   <li>Alias(es) given by EPSG: <b>Perroud 1950</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Antarctica - Adelie Land coastal area</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Pointe Geologie Perroud 1950")
    public void EPSG_6637() throws FactoryException {
        code              = 6637;
        name              = "Pointe Geologie Perroud 1950";
        aliases           = new String[] {"Perroud 1950"};
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Porto Santo 1936” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6615</b></li>
     *   <li>EPSG datum name: <b>Porto Santo 1936</b></li>
     *   <li>Alias(es) given by EPSG: <b>Madeira SE Base</b>, <b>Base SE</b>, <b>Porto Santo</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Portugal - Madeira archipelago onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Porto Santo 1936")
    public void EPSG_6615() throws FactoryException {
        code              = 6615;
        name              = "Porto Santo 1936";
        aliases           = new String[] {"Madeira SE Base", "Base SE", "Porto Santo"};
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Porto Santo 1995” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6663</b></li>
     *   <li>EPSG datum name: <b>Porto Santo 1995</b></li>
     *   <li>Alias(es) given by EPSG: <b>Base SE</b>, <b>Madeira SE Base 1995</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Portugal - Madeira archipelago onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Porto Santo 1995")
    public void EPSG_6663() throws FactoryException {
        code              = 6663;
        name              = "Porto Santo 1995";
        aliases           = new String[] {"Base SE", "Madeira SE Base 1995"};
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Posiciones Geodesicas Argentinas 1994” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6694</b></li>
     *   <li>EPSG datum name: <b>Posiciones Geodesicas Argentinas 1994</b></li>
     *   <li>Alias(es) given by EPSG: <b>POSGAR 94</b>, <b>POSGAR</b>, <b>Posiciones Geodésicas Argentinas 1994</b></li>
     *   <li>Ellipsoid name: <b>WGS 84</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Argentina</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Posiciones Geodesicas Argentinas 1994")
    public void EPSG_6694() throws FactoryException {
        code              = 6694;
        name              = "Posiciones Geodesicas Argentinas 1994";
        aliases           = new String[] {"POSGAR 94", "POSGAR", "Posiciones Geodésicas Argentinas 1994"};
        ellipsoidName     = "WGS 84";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Posiciones Geodesicas Argentinas 1998” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6190</b></li>
     *   <li>EPSG datum name: <b>Posiciones Geodesicas Argentinas 1998</b></li>
     *   <li>Alias(es) given by EPSG: <b>POSGAR 98</b>, <b>Posiciones Geodésicas Argentinas 1998</b>, <b>POSGAR</b></li>
     *   <li>Ellipsoid name: <b>GRS 1980</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Argentina</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Posiciones Geodesicas Argentinas 1998")
    public void EPSG_6190() throws FactoryException {
        code              = 6190;
        name              = "Posiciones Geodesicas Argentinas 1998";
        aliases           = new String[] {"POSGAR 98", "Posiciones Geodésicas Argentinas 1998", "POSGAR"};
        ellipsoidName     = "GRS 1980";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Potsdam Datum/83” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6746</b></li>
     *   <li>EPSG datum name: <b>Potsdam Datum/83</b></li>
     *   <li>Alias(es) given by EPSG: <b>PD/83</b></li>
     *   <li>Ellipsoid name: <b>Bessel 1841</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Germany - Thuringen</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Potsdam Datum/83")
    public void EPSG_6746() throws FactoryException {
        code              = 6746;
        name              = "Potsdam Datum/83";
        aliases           = new String[] {"PD/83"};
        ellipsoidName     = "Bessel 1841";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Provisional South American Datum 1956” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6248</b></li>
     *   <li>EPSG datum name: <b>Provisional South American Datum 1956</b></li>
     *   <li>Alias(es) given by EPSG: <b>PSAD56</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>South America - PSAD56 by country</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Provisional South American Datum 1956")
    public void EPSG_6248() throws FactoryException {
        code              = 6248;
        name              = "Provisional South American Datum 1956";
        aliases           = new String[] {"PSAD56"};
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Puerto Rico” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6139</b></li>
     *   <li>EPSG datum name: <b>Puerto Rico</b></li>
     *   <li>Ellipsoid name: <b>Clarke 1866</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Caribbean - Puerto Rico and Virgin Islands - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Puerto Rico")
    public void EPSG_6139() throws FactoryException {
        code              = 6139;
        name              = "Puerto Rico";
        ellipsoidName     = "Clarke 1866";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Pulkovo 1942” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6284</b></li>
     *   <li>EPSG datum name: <b>Pulkovo 1942</b></li>
     *   <li>Ellipsoid name: <b>Krassowsky 1940</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Europe - FSU onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Pulkovo 1942")
    public void EPSG_6284() throws FactoryException {
        code              = 6284;
        name              = "Pulkovo 1942";
        ellipsoidName     = "Krassowsky 1940";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Pulkovo 1942(58)” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6179</b></li>
     *   <li>EPSG datum name: <b>Pulkovo 1942(58)</b></li>
     *   <li>Alias(es) given by EPSG: <b>42/58</b></li>
     *   <li>Ellipsoid name: <b>Krassowsky 1940</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Europe - onshore - eastern - S-42(58)</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Pulkovo 1942(58)")
    public void EPSG_6179() throws FactoryException {
        code              = 6179;
        name              = "Pulkovo 1942(58)";
        aliases           = new String[] {"42/58"};
        ellipsoidName     = "Krassowsky 1940";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Pulkovo 1942(83)” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6178</b></li>
     *   <li>EPSG datum name: <b>Pulkovo 1942(83)</b></li>
     *   <li>Alias(es) given by EPSG: <b>42/83</b></li>
     *   <li>Ellipsoid name: <b>Krassowsky 1940</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Europe - onshore - eastern - S-42(83)</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Pulkovo 1942(83)")
    public void EPSG_6178() throws FactoryException {
        code              = 6178;
        name              = "Pulkovo 1942(83)";
        aliases           = new String[] {"42/83"};
        ellipsoidName     = "Krassowsky 1940";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Pulkovo 1995” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6200</b></li>
     *   <li>EPSG datum name: <b>Pulkovo 1995</b></li>
     *   <li>Ellipsoid name: <b>Krassowsky 1940</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Russia</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Pulkovo 1995")
    public void EPSG_6200() throws FactoryException {
        code              = 6200;
        name              = "Pulkovo 1995";
        ellipsoidName     = "Krassowsky 1940";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Qatar 1948” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6286</b></li>
     *   <li>EPSG datum name: <b>Qatar 1948</b></li>
     *   <li>Ellipsoid name: <b>Helmert 1906</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Qatar - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Qatar 1948")
    public void EPSG_6286() throws FactoryException {
        code              = 6286;
        name              = "Qatar 1948";
        ellipsoidName     = "Helmert 1906";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Qatar 1974” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6285</b></li>
     *   <li>EPSG datum name: <b>Qatar 1974</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Qatar</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Qatar 1974")
    public void EPSG_6285() throws FactoryException {
        code              = 6285;
        name              = "Qatar 1974";
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Qatar National Datum 1995” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6614</b></li>
     *   <li>EPSG datum name: <b>Qatar National Datum 1995</b></li>
     *   <li>Alias(es) given by EPSG: <b>QND95</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Qatar - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Qatar National Datum 1995")
    public void EPSG_6614() throws FactoryException {
        code              = 6614;
        name              = "Qatar National Datum 1995";
        aliases           = new String[] {"QND95"};
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Qornoq 1927” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6194</b></li>
     *   <li>EPSG datum name: <b>Qornoq 1927</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Greenland - west coast</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Qornoq 1927")
    public void EPSG_6194() throws FactoryException {
        code              = 6194;
        name              = "Qornoq 1927";
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Rassadiran” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6153</b></li>
     *   <li>EPSG datum name: <b>Rassadiran</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Iran - Taheri refinery</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Rassadiran")
    public void EPSG_6153() throws FactoryException {
        code              = 6153;
        name              = "Rassadiran";
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Rauenberg Datum/83” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6745</b></li>
     *   <li>EPSG datum name: <b>Rauenberg Datum/83</b></li>
     *   <li>Alias(es) given by EPSG: <b>RD/83</b></li>
     *   <li>Ellipsoid name: <b>Bessel 1841</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Germany - Saxony</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Rauenberg Datum/83")
    public void EPSG_6745() throws FactoryException {
        code              = 6745;
        name              = "Rauenberg Datum/83";
        aliases           = new String[] {"RD/83"};
        ellipsoidName     = "Bessel 1841";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Red Geodesica Venezolana” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6189</b></li>
     *   <li>EPSG datum name: <b>Red Geodesica Venezolana</b></li>
     *   <li>Alias(es) given by EPSG: <b>SIRGAS-REGVEN</b>, <b>REGVEN</b></li>
     *   <li>Ellipsoid name: <b>GRS 1980</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Venezuela</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Red Geodesica Venezolana")
    public void EPSG_6189() throws FactoryException {
        code              = 6189;
        name              = "Red Geodesica Venezolana";
        aliases           = new String[] {"SIRGAS-REGVEN", "REGVEN"};
        ellipsoidName     = "GRS 1980";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Reseau de Reference des Antilles Francaises 1991” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>1047</b></li>
     *   <li>EPSG datum name: <b>Reseau de Reference des Antilles Francaises 1991</b></li>
     *   <li>Alias(es) given by EPSG: <b>RRAF91</b></li>
     *   <li>Ellipsoid name: <b>GRS 1980</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Caribbean - French Antilles</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Reseau de Reference des Antilles Francaises 1991")
    public void EPSG_1047() throws FactoryException {
        code              = 1047;
        name              = "Reseau de Reference des Antilles Francaises 1991";
        aliases           = new String[] {"RRAF91"};
        ellipsoidName     = "GRS 1980";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Reseau Geodesique de la Polynesie Francaise” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6687</b></li>
     *   <li>EPSG datum name: <b>Reseau Geodesique de la Polynesie Francaise</b></li>
     *   <li>Alias(es) given by EPSG: <b>RGPF</b></li>
     *   <li>Ellipsoid name: <b>GRS 1980</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>French Polynesia</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Reseau Geodesique de la Polynesie Francaise")
    public void EPSG_6687() throws FactoryException {
        code              = 6687;
        name              = "Reseau Geodesique de la Polynesie Francaise";
        aliases           = new String[] {"RGPF"};
        ellipsoidName     = "GRS 1980";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Reseau Geodesique de la Reunion 1992” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6627</b></li>
     *   <li>EPSG datum name: <b>Reseau Geodesique de la Reunion 1992</b></li>
     *   <li>Alias(es) given by EPSG: <b>RGR92</b></li>
     *   <li>Ellipsoid name: <b>GRS 1980</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Reunion</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Reseau Geodesique de la Reunion 1992")
    public void EPSG_6627() throws FactoryException {
        code              = 6627;
        name              = "Reseau Geodesique de la Reunion 1992";
        aliases           = new String[] {"RGR92"};
        ellipsoidName     = "GRS 1980";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Reseau Geodesique de Nouvelle Caledonie 91-93” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6749</b></li>
     *   <li>EPSG datum name: <b>Reseau Geodesique de Nouvelle Caledonie 91-93</b></li>
     *   <li>Alias(es) given by EPSG: <b>RGNC91-93</b>, <b>RGNC</b></li>
     *   <li>Ellipsoid name: <b>GRS 1980</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>New Caledonia</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Reseau Geodesique de Nouvelle Caledonie 91-93")
    public void EPSG_6749() throws FactoryException {
        code              = 6749;
        name              = "Reseau Geodesique de Nouvelle Caledonie 91-93";
        aliases           = new String[] {"RGNC91-93", "RGNC"};
        ellipsoidName     = "GRS 1980";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Reseau Geodesique Francais 1993” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6171</b></li>
     *   <li>EPSG datum name: <b>Reseau Geodesique Francais 1993</b></li>
     *   <li>Alias(es) given by EPSG: <b>RGF93</b>, <b>Réseau Géodésique Français 1993</b></li>
     *   <li>Ellipsoid name: <b>GRS 1980</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>France</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Reseau Geodesique Francais 1993")
    public void EPSG_6171() throws FactoryException {
        code              = 6171;
        name              = "Reseau Geodesique Francais 1993";
        aliases           = new String[] {"RGF93", "Réseau Géodésique Français 1993"};
        ellipsoidName     = "GRS 1980";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Reseau Geodesique Francais Guyane 1995” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6624</b></li>
     *   <li>EPSG datum name: <b>Reseau Geodesique Francais Guyane 1995</b></li>
     *   <li>Alias(es) given by EPSG: <b>RGFG95</b></li>
     *   <li>Ellipsoid name: <b>GRS 1980</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>French Guiana</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Reseau Geodesique Francais Guyane 1995")
    public void EPSG_6624() throws FactoryException {
        code              = 6624;
        name              = "Reseau Geodesique Francais Guyane 1995";
        aliases           = new String[] {"RGFG95"};
        ellipsoidName     = "GRS 1980";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Reseau National Belge 1950” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6215</b></li>
     *   <li>EPSG datum name: <b>Reseau National Belge 1950</b></li>
     *   <li>Alias(es) given by EPSG: <b>Belge 1950</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Belgium - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Reseau National Belge 1950")
    public void EPSG_6215() throws FactoryException {
        code              = 6215;
        name              = "Reseau National Belge 1950";
        aliases           = new String[] {"Belge 1950"};
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Reseau National Belge 1950 (Brussels)” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6809</b></li>
     *   <li>EPSG datum name: <b>Reseau National Belge 1950 (Brussels)</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Brussels</b></li>
     *   <li>EPSG Usage Extent: <b>Belgium - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Reseau National Belge 1950 (Brussels)")
    public void EPSG_6809() throws FactoryException {
        code              = 6809;
        name              = "Reseau National Belge 1950 (Brussels)";
        ellipsoidName     = "International 1924";
        primeMeridianName = "Brussels";
        verifyDatum();
    }

    /**
     * Tests “Reseau National Belge 1972” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6313</b></li>
     *   <li>EPSG datum name: <b>Reseau National Belge 1972</b></li>
     *   <li>Alias(es) given by EPSG: <b>Belge 1972</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Belgium - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Reseau National Belge 1972")
    public void EPSG_6313() throws FactoryException {
        code              = 6313;
        name              = "Reseau National Belge 1972";
        aliases           = new String[] {"Belge 1972"};
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Reunion 1947” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6626</b></li>
     *   <li>EPSG datum name: <b>Reunion 1947</b></li>
     *   <li>Alias(es) given by EPSG: <b>Piton des Neiges</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Reunion - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Reunion 1947")
    public void EPSG_6626() throws FactoryException {
        code              = 6626;
        name              = "Reunion 1947";
        aliases           = new String[] {"Piton des Neiges"};
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Reykjavik 1900” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6657</b></li>
     *   <li>EPSG datum name: <b>Reykjavik 1900</b></li>
     *   <li>Ellipsoid name: <b>Danish 1876</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Iceland - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Reykjavik 1900")
    public void EPSG_6657() throws FactoryException {
        code              = 6657;
        name              = "Reykjavik 1900";
        ellipsoidName     = "Danish 1876";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Rikets koordinatsystem 1990” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6124</b></li>
     *   <li>EPSG datum name: <b>Rikets koordinatsystem 1990</b></li>
     *   <li>Alias(es) given by EPSG: <b>RT90</b></li>
     *   <li>Ellipsoid name: <b>Bessel 1841</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Sweden</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Rikets koordinatsystem 1990")
    public void EPSG_6124() throws FactoryException {
        code              = 6124;
        name              = "Rikets koordinatsystem 1990";
        aliases           = new String[] {"RT90"};
        ellipsoidName     = "Bessel 1841";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Ross Sea Region Geodetic Datum 2000” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6764</b></li>
     *   <li>EPSG datum name: <b>Ross Sea Region Geodetic Datum 2000</b></li>
     *   <li>Alias(es) given by EPSG: <b>RSRGD2000</b></li>
     *   <li>Ellipsoid name: <b>GRS 1980</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Antarctica - Ross Sea Region</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Ross Sea Region Geodetic Datum 2000")
    public void EPSG_6764() throws FactoryException {
        code              = 6764;
        name              = "Ross Sea Region Geodetic Datum 2000";
        aliases           = new String[] {"RSRGD2000"};
        ellipsoidName     = "GRS 1980";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Saint Pierre et Miquelon 1950” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6638</b></li>
     *   <li>EPSG datum name: <b>Saint Pierre et Miquelon 1950</b></li>
     *   <li>Alias(es) given by EPSG: <b>St. Pierre et Miquelon 1950</b></li>
     *   <li>Ellipsoid name: <b>Clarke 1866</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>St Pierre and Miquelon - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Saint Pierre et Miquelon 1950")
    public void EPSG_6638() throws FactoryException {
        code              = 6638;
        name              = "Saint Pierre et Miquelon 1950";
        aliases           = new String[] {"St. Pierre et Miquelon 1950"};
        ellipsoidName     = "Clarke 1866";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Santo 1965” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6730</b></li>
     *   <li>EPSG datum name: <b>Santo 1965</b></li>
     *   <li>Alias(es) given by EPSG: <b>Santo (DOS)</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Vanuatu - northern islands</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Santo 1965")
    public void EPSG_6730() throws FactoryException {
        code              = 6730;
        name              = "Santo 1965";
        aliases           = new String[] {"Santo (DOS)"};
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Sapper Hill 1943” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6292</b></li>
     *   <li>EPSG datum name: <b>Sapper Hill 1943</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Falkland Islands - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Sapper Hill 1943")
    public void EPSG_6292() throws FactoryException {
        code              = 6292;
        name              = "Sapper Hill 1943";
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Schwarzeck” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6293</b></li>
     *   <li>EPSG datum name: <b>Schwarzeck</b></li>
     *   <li>Ellipsoid name: <b>Bessel Namibia (GLM)</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Namibia</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Schwarzeck")
    public void EPSG_6293() throws FactoryException {
        code              = 6293;
        name              = "Schwarzeck";
        ellipsoidName     = "Bessel Namibia (GLM)";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Scoresbysund 1952” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6195</b></li>
     *   <li>EPSG datum name: <b>Scoresbysund 1952</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Greenland - Scoresbysund area</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Scoresbysund 1952")
    public void EPSG_6195() throws FactoryException {
        code              = 6195;
        name              = "Scoresbysund 1952";
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Selvagem Grande” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6616</b></li>
     *   <li>EPSG datum name: <b>Selvagem Grande</b></li>
     *   <li>Alias(es) given by EPSG: <b>Selvagem Grande 1938</b>, <b>Marco Astro</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Portugal - Selvagens onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Selvagem Grande")
    public void EPSG_6616() throws FactoryException {
        code              = 6616;
        name              = "Selvagem Grande";
        aliases           = new String[] {"Selvagem Grande 1938", "Marco Astro"};
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Serindung” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6295</b></li>
     *   <li>EPSG datum name: <b>Serindung</b></li>
     *   <li>Ellipsoid name: <b>Bessel 1841</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Indonesia - Kalimantan W - coastal</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Serindung")
    public void EPSG_6295() throws FactoryException {
        code              = 6295;
        name              = "Serindung";
        ellipsoidName     = "Bessel 1841";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Sierra Leone 1968” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6175</b></li>
     *   <li>EPSG datum name: <b>Sierra Leone 1968</b></li>
     *   <li>Ellipsoid name: <b>Clarke 1880 (RGS)</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Sierra Leone - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Sierra Leone 1968")
    public void EPSG_6175() throws FactoryException {
        code              = 6175;
        name              = "Sierra Leone 1968";
        ellipsoidName     = "Clarke 1880 (RGS)";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Sierra Leone Colony 1924” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6174</b></li>
     *   <li>EPSG datum name: <b>Sierra Leone Colony 1924</b></li>
     *   <li>Alias(es) given by EPSG: <b>Sierra Leone Peninsular 1924</b></li>
     *   <li>Ellipsoid name: <b>War Office</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Sierra Leone - Freetown Peninsula</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Sierra Leone Colony 1924")
    public void EPSG_6174() throws FactoryException {
        code              = 6174;
        name              = "Sierra Leone Colony 1924";
        aliases           = new String[] {"Sierra Leone Peninsular 1924"};
        ellipsoidName     = "War Office";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Sistema de Referencia Geocentrico para America del Sur 1995” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6170</b></li>
     *   <li>EPSG datum name: <b>Sistema de Referencia Geocentrico para America del Sur 1995</b></li>
     *   <li>Alias(es) given by EPSG: <b>SIRGAS 1995</b></li>
     *   <li>Ellipsoid name: <b>GRS 1980</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>South America - SIRGAS 1995 by country</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Sistema de Referencia Geocentrico para America del Sur 1995")
    public void EPSG_6170() throws FactoryException {
        code              = 6170;
        name              = "Sistema de Referencia Geocentrico para America del Sur 1995";
        aliases           = new String[] {"SIRGAS 1995"};
        ellipsoidName     = "GRS 1980";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Sistema de Referencia Geocentrico para America del Sur 2000” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6674</b></li>
     *   <li>EPSG datum name: <b>Sistema de Referencia Geocentrico para America del Sur 2000</b></li>
     *   <li>Alias(es) given by EPSG: <b>SIRGAS 2000</b>, <b>Sistema de Referencia Geocentrico para America del Sur 2000</b>, <b>SIRGAS2000</b></li>
     *   <li>Ellipsoid name: <b>GRS 1980</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Latin America - SIRGAS 2000 by country</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Sistema de Referencia Geocentrico para America del Sur 2000")
    public void EPSG_6674() throws FactoryException {
        code              = 6674;
        name              = "Sistema de Referencia Geocentrico para America del Sur 2000";
        aliases           = new String[] {"SIRGAS 2000", "Sistema de Referencia Geocentrico para America del Sur 2000", "SIRGAS2000"};
        ellipsoidName     = "GRS 1980";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Sister Islands Geodetic Datum 1961” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6726</b></li>
     *   <li>EPSG datum name: <b>Sister Islands Geodetic Datum 1961</b></li>
     *   <li>Alias(es) given by EPSG: <b>SIGD61</b>, <b>Little Cayman Geodetic Datum 1961</b></li>
     *   <li>Ellipsoid name: <b>Clarke 1866</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Cayman Islands - Little Cayman and Cayman Brac</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Sister Islands Geodetic Datum 1961")
    public void EPSG_6726() throws FactoryException {
        code              = 6726;
        name              = "Sister Islands Geodetic Datum 1961";
        aliases           = new String[] {"SIGD61", "Little Cayman Geodetic Datum 1961"};
        ellipsoidName     = "Clarke 1866";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Slovenia Geodetic Datum 1996” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6765</b></li>
     *   <li>EPSG datum name: <b>Slovenia Geodetic Datum 1996</b></li>
     *   <li>Alias(es) given by EPSG: <b>D96</b>, <b>Slovenia 1996</b></li>
     *   <li>Ellipsoid name: <b>GRS 1980</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Slovenia</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Slovenia Geodetic Datum 1996")
    public void EPSG_6765() throws FactoryException {
        code              = 6765;
        name              = "Slovenia Geodetic Datum 1996";
        aliases           = new String[] {"D96", "Slovenia 1996"};
        ellipsoidName     = "GRS 1980";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Solomon 1968” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6718</b></li>
     *   <li>EPSG datum name: <b>Solomon 1968</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Solomon Islands - onshore main islands</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Solomon 1968")
    public void EPSG_6718() throws FactoryException {
        code              = 6718;
        name              = "Solomon 1968";
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “South American Datum 1969” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6618</b></li>
     *   <li>EPSG datum name: <b>South American Datum 1969</b></li>
     *   <li>Ellipsoid name: <b>GRS 1967 Modified</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>South America - SAD69 by country</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("South American Datum 1969")
    public void EPSG_6618() throws FactoryException {
        code              = 6618;
        name              = "South American Datum 1969";
        ellipsoidName     = "GRS 1967 Modified";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “South Georgia 1968” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6722</b></li>
     *   <li>EPSG datum name: <b>South Georgia 1968</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>South Georgia - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("South Georgia 1968")
    public void EPSG_6722() throws FactoryException {
        code              = 6722;
        name              = "South Georgia 1968";
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “South Yemen” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6164</b></li>
     *   <li>EPSG datum name: <b>South Yemen</b></li>
     *   <li>Ellipsoid name: <b>Krassowsky 1940</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Yemen - South Yemen - mainland</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("South Yemen")
    public void EPSG_6164() throws FactoryException {
        code              = 6164;
        name              = "South Yemen";
        ellipsoidName     = "Krassowsky 1940";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “St. George Island” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6138</b></li>
     *   <li>EPSG datum name: <b>St. George Island</b></li>
     *   <li>Ellipsoid name: <b>Clarke 1866</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>USA - Alaska - St. George Island</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("St. George Island")
    public void EPSG_6138() throws FactoryException {
        code              = 6138;
        name              = "St. George Island";
        ellipsoidName     = "Clarke 1866";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “St. Kitts 1955” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6605</b></li>
     *   <li>EPSG datum name: <b>St. Kitts 1955</b></li>
     *   <li>Ellipsoid name: <b>Clarke 1880 (RGS)</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>St Kitts and Nevis - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("St. Kitts 1955")
    public void EPSG_6605() throws FactoryException {
        code              = 6605;
        name              = "St. Kitts 1955";
        ellipsoidName     = "Clarke 1880 (RGS)";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “St. Lawrence Island” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6136</b></li>
     *   <li>EPSG datum name: <b>St. Lawrence Island</b></li>
     *   <li>Ellipsoid name: <b>Clarke 1866</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>USA - Alaska - St. Lawrence Island</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("St. Lawrence Island")
    public void EPSG_6136() throws FactoryException {
        code              = 6136;
        name              = "St. Lawrence Island";
        ellipsoidName     = "Clarke 1866";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “St. Lucia 1955” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6606</b></li>
     *   <li>EPSG datum name: <b>St. Lucia 1955</b></li>
     *   <li>Ellipsoid name: <b>Clarke 1880 (RGS)</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>St Lucia - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("St. Lucia 1955")
    public void EPSG_6606() throws FactoryException {
        code              = 6606;
        name              = "St. Lucia 1955";
        ellipsoidName     = "Clarke 1880 (RGS)";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “St. Paul Island” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6137</b></li>
     *   <li>EPSG datum name: <b>St. Paul Island</b></li>
     *   <li>Ellipsoid name: <b>Clarke 1866</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>USA - Alaska - St. Paul Island</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("St. Paul Island")
    public void EPSG_6137() throws FactoryException {
        code              = 6137;
        name              = "St. Paul Island";
        ellipsoidName     = "Clarke 1866";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “St. Vincent 1945” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6607</b></li>
     *   <li>EPSG datum name: <b>St. Vincent 1945</b></li>
     *   <li>Ellipsoid name: <b>Clarke 1880 (RGS)</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>St Vincent and the Grenadines - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("St. Vincent 1945")
    public void EPSG_6607() throws FactoryException {
        code              = 6607;
        name              = "St. Vincent 1945";
        ellipsoidName     = "Clarke 1880 (RGS)";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “ST71 Belep” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6643</b></li>
     *   <li>EPSG datum name: <b>ST71 Belep</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>New Caledonia - Belep</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("ST71 Belep")
    public void EPSG_6643() throws FactoryException {
        code              = 6643;
        name              = "ST71 Belep";
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “ST84 Ile des Pins” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6642</b></li>
     *   <li>EPSG datum name: <b>ST84 Ile des Pins</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>New Caledonia - Ile des Pins</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("ST84 Ile des Pins")
    public void EPSG_6642() throws FactoryException {
        code              = 6642;
        name              = "ST84 Ile des Pins";
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “ST87 Ouvea” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6750</b></li>
     *   <li>EPSG datum name: <b>ST87 Ouvea</b></li>
     *   <li>Ellipsoid name: <b>WGS 84</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>New Caledonia - Ouvea</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("ST87 Ouvea")
    public void EPSG_6750() throws FactoryException {
        code              = 6750;
        name              = "ST87 Ouvea";
        ellipsoidName     = "WGS 84";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Stockholm 1938” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6308</b></li>
     *   <li>EPSG datum name: <b>Stockholm 1938</b></li>
     *   <li>Alias(es) given by EPSG: <b>Rikets koordinatsystem 1938</b>, <b>RT38</b></li>
     *   <li>Ellipsoid name: <b>Bessel 1841</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Sweden - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Stockholm 1938")
    public void EPSG_6308() throws FactoryException {
        code              = 6308;
        name              = "Stockholm 1938";
        aliases           = new String[] {"Rikets koordinatsystem 1938", "RT38"};
        ellipsoidName     = "Bessel 1841";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Stockholm 1938 (Stockholm)” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6814</b></li>
     *   <li>EPSG datum name: <b>Stockholm 1938 (Stockholm)</b></li>
     *   <li>Ellipsoid name: <b>Bessel 1841</b></li>
     *   <li>Prime meridian name: <b>Stockholm</b></li>
     *   <li>EPSG Usage Extent: <b>Sweden - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Stockholm 1938 (Stockholm)")
    public void EPSG_6814() throws FactoryException {
        code              = 6814;
        name              = "Stockholm 1938 (Stockholm)";
        ellipsoidName     = "Bessel 1841";
        primeMeridianName = "Stockholm";
        verifyDatum();
    }

    /**
     * Tests “SVY21” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6757</b></li>
     *   <li>EPSG datum name: <b>SVY21</b></li>
     *   <li>Ellipsoid name: <b>WGS 84</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Singapore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("SVY21")
    public void EPSG_6757() throws FactoryException {
        code              = 6757;
        name              = "SVY21";
        ellipsoidName     = "WGS 84";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “SWEREF99” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6619</b></li>
     *   <li>EPSG datum name: <b>SWEREF99</b></li>
     *   <li>Ellipsoid name: <b>GRS 1980</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Sweden</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("SWEREF99")
    public void EPSG_6619() throws FactoryException {
        code              = 6619;
        name              = "SWEREF99";
        ellipsoidName     = "GRS 1980";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Swiss Terrestrial Reference Frame 1995” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6151</b></li>
     *   <li>EPSG datum name: <b>Swiss Terrestrial Reference Frame 1995</b></li>
     *   <li>Alias(es) given by EPSG: <b>CHTRF95</b></li>
     *   <li>Ellipsoid name: <b>GRS 1980</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Europe - Liechtenstein and Switzerland</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Swiss Terrestrial Reference Frame 1995")
    public void EPSG_6151() throws FactoryException {
        code              = 6151;
        name              = "Swiss Terrestrial Reference Frame 1995";
        aliases           = new String[] {"CHTRF95"};
        ellipsoidName     = "GRS 1980";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “System of the Unified Trigonometrical Cadastral Network” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6156</b></li>
     *   <li>EPSG datum name: <b>System of the Unified Trigonometrical Cadastral Network</b></li>
     *   <li>Alias(es) given by EPSG: <b>S-JTSK</b>, <b>Systém Jednotné trigonometrické sít? katastrální</b>, <b>Systém Jednotnej trigonometrickej siete katastrálnej</b></li>
     *   <li>Ellipsoid name: <b>Bessel 1841</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Europe - Czechoslovakia</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("System of the Unified Trigonometrical Cadastral Network")
    public void EPSG_6156() throws FactoryException {
        code              = 6156;
        name              = "System of the Unified Trigonometrical Cadastral Network";
        aliases           = new String[] {"S-JTSK", "Systém Jednotné trigonometrické sít? katastrální", "Systém Jednotnej trigonometrickej siete katastrálnej"};
        ellipsoidName     = "Bessel 1841";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “System of the Unified Trigonometrical Cadastral Network (Ferro)” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6818</b></li>
     *   <li>EPSG datum name: <b>System of the Unified Trigonometrical Cadastral Network (Ferro)</b></li>
     *   <li>Alias(es) given by EPSG: <b>Systém Jednotné trigonometrické sít? katastrální (Ferro)</b>, <b>Systém Jednotnej trigonometrickej siete katastrálnej (Ferro)</b>, <b>S-JTSK (Ferro)</b></li>
     *   <li>Ellipsoid name: <b>Bessel 1841</b></li>
     *   <li>Prime meridian name: <b>Ferro</b></li>
     *   <li>EPSG Usage Extent: <b>Europe - Czechoslovakia</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("System of the Unified Trigonometrical Cadastral Network (Ferro)")
    public void EPSG_6818() throws FactoryException {
        code              = 6818;
        name              = "System of the Unified Trigonometrical Cadastral Network (Ferro)";
        aliases           = new String[] {"Systém Jednotné trigonometrické sít? katastrální (Ferro)", "Systém Jednotnej trigonometrickej siete katastrálnej (Ferro)", "S-JTSK (Ferro)"};
        ellipsoidName     = "Bessel 1841";
        primeMeridianName = "Ferro";
        verifyDatum();
    }

    /**
     * Tests “Tahaa 54” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6629</b></li>
     *   <li>EPSG datum name: <b>Tahaa 54</b></li>
     *   <li>Alias(es) given by EPSG: <b>Tahaa</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>French Polynesia - Society Islands - Bora Bora; Huahine; Raiatea; Tahaa</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Tahaa 54")
    public void EPSG_6629() throws FactoryException {
        code              = 6629;
        name              = "Tahaa 54";
        aliases           = new String[] {"Tahaa"};
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Tahiti 52” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6628</b></li>
     *   <li>EPSG datum name: <b>Tahiti 52</b></li>
     *   <li>Alias(es) given by EPSG: <b>IGN 1952</b>, <b>Tahiti</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>French Polynesia - Society Islands - Moorea and Tahiti</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Tahiti 52")
    public void EPSG_6628() throws FactoryException {
        code              = 6628;
        name              = "Tahiti 52";
        aliases           = new String[] {"IGN 1952", "Tahiti"};
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Tahiti 79” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6690</b></li>
     *   <li>EPSG datum name: <b>Tahiti 79</b></li>
     *   <li>Alias(es) given by EPSG: <b>IGN79 Tahiti</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>French Polynesia - Society Islands - Tahiti</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Tahiti 79")
    public void EPSG_6690() throws FactoryException {
        code              = 6690;
        name              = "Tahiti 79";
        aliases           = new String[] {"IGN79 Tahiti"};
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Taiwan Datum 1967” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>1025</b></li>
     *   <li>EPSG datum name: <b>Taiwan Datum 1967</b></li>
     *   <li>Alias(es) given by EPSG: <b>TWD67</b></li>
     *   <li>Ellipsoid name: <b>GRS 1967 Modified</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Taiwan - onshore - mainland and Penghu</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Taiwan Datum 1967")
    public void EPSG_1025() throws FactoryException {
        code              = 1025;
        name              = "Taiwan Datum 1967";
        aliases           = new String[] {"TWD67"};
        ellipsoidName     = "GRS 1967 Modified";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Taiwan Datum 1997” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>1026</b></li>
     *   <li>EPSG datum name: <b>Taiwan Datum 1997</b></li>
     *   <li>Alias(es) given by EPSG: <b>TWD97</b></li>
     *   <li>Ellipsoid name: <b>GRS 1980</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Taiwan</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Taiwan Datum 1997")
    public void EPSG_1026() throws FactoryException {
        code              = 1026;
        name              = "Taiwan Datum 1997";
        aliases           = new String[] {"TWD97"};
        ellipsoidName     = "GRS 1980";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Tananarive 1925” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6297</b></li>
     *   <li>EPSG datum name: <b>Tananarive 1925</b></li>
     *   <li>Alias(es) given by EPSG: <b>Tananarive</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Madagascar - onshore and nearshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Tananarive 1925")
    public void EPSG_6297() throws FactoryException {
        code              = 6297;
        name              = "Tananarive 1925";
        aliases           = new String[] {"Tananarive"};
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Tananarive 1925 (Paris)” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6810</b></li>
     *   <li>EPSG datum name: <b>Tananarive 1925 (Paris)</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Paris</b></li>
     *   <li>EPSG Usage Extent: <b>Madagascar - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Tananarive 1925 (Paris)")
    public void EPSG_6810() throws FactoryException {
        code              = 6810;
        name              = "Tananarive 1925 (Paris)";
        ellipsoidName     = "International 1924";
        primeMeridianName = "Paris";
        verifyDatum();
    }

    /**
     * Tests “Tern Island 1961” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6707</b></li>
     *   <li>EPSG datum name: <b>Tern Island 1961</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>USA - Hawaii - Tern Island and Sorel Atoll</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Tern Island 1961")
    public void EPSG_6707() throws FactoryException {
        code              = 6707;
        name              = "Tern Island 1961";
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Tete” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6127</b></li>
     *   <li>EPSG datum name: <b>Tete</b></li>
     *   <li>Alias(es) given by EPSG: <b>Tete 1960</b></li>
     *   <li>Ellipsoid name: <b>Clarke 1866</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Mozambique - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Tete")
    public void EPSG_6127() throws FactoryException {
        code              = 6127;
        name              = "Tete";
        aliases           = new String[] {"Tete 1960"};
        ellipsoidName     = "Clarke 1866";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Timbalai 1948” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6298</b></li>
     *   <li>EPSG datum name: <b>Timbalai 1948</b></li>
     *   <li>Alias(es) given by EPSG: <b>Timbalai 1968</b>, <b>Borneo Triangulation of 1968</b>, <b>BT68</b></li>
     *   <li>Ellipsoid name: <b>Everest 1830 (1967 Definition)</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Asia - Brunei and East Malaysia</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Timbalai 1948")
    public void EPSG_6298() throws FactoryException {
        code              = 6298;
        name              = "Timbalai 1948";
        aliases           = new String[] {"Timbalai 1968", "Borneo Triangulation of 1968", "BT68"};
        ellipsoidName     = "Everest 1830 (1967 Definition)";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “TM65” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6299</b></li>
     *   <li>EPSG datum name: <b>TM65</b></li>
     *   <li>Ellipsoid name: <b>Airy Modified 1849</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Europe - Ireland (Republic and Ulster) - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("TM65")
    public void EPSG_6299() throws FactoryException {
        code              = 6299;
        name              = "TM65";
        ellipsoidName     = "Airy Modified 1849";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Tokyo” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6301</b></li>
     *   <li>EPSG datum name: <b>Tokyo</b></li>
     *   <li>Alias(es) given by EPSG: <b>Tokyo 1918</b></li>
     *   <li>Ellipsoid name: <b>Bessel 1841</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Asia - Japan and Korea</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Tokyo")
    public void EPSG_6301() throws FactoryException {
        code              = 6301;
        name              = "Tokyo";
        aliases           = new String[] {"Tokyo 1918"};
        ellipsoidName     = "Bessel 1841";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Trinidad 1903” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6302</b></li>
     *   <li>EPSG datum name: <b>Trinidad 1903</b></li>
     *   <li>Ellipsoid name: <b>Clarke 1858</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Trinidad and Tobago - Trinidad</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Trinidad 1903")
    public void EPSG_6302() throws FactoryException {
        code              = 6302;
        name              = "Trinidad 1903";
        ellipsoidName     = "Clarke 1858";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Tristan 1968” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6734</b></li>
     *   <li>EPSG datum name: <b>Tristan 1968</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>St Helena - Tristan da Cunha</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Tristan 1968")
    public void EPSG_6734() throws FactoryException {
        code              = 6734;
        name              = "Tristan 1968";
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Trucial Coast 1948” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6303</b></li>
     *   <li>EPSG datum name: <b>Trucial Coast 1948</b></li>
     *   <li>Alias(es) given by EPSG: <b>TC(1948)</b></li>
     *   <li>Ellipsoid name: <b>Helmert 1906</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>UAE - Abu Dhabi and Dubai - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Trucial Coast 1948")
    public void EPSG_6303() throws FactoryException {
        code              = 6303;
        name              = "Trucial Coast 1948";
        aliases           = new String[] {"TC(1948)"};
        ellipsoidName     = "Helmert 1906";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Vanua Levu 1915” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6748</b></li>
     *   <li>EPSG datum name: <b>Vanua Levu 1915</b></li>
     *   <li>Alias(es) given by EPSG: <b>Vanua Levu 1917</b></li>
     *   <li>Ellipsoid name: <b>Clarke 1880 (international foot)</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Fiji - Vanua Levu and Taveuni</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Vanua Levu 1915")
    public void EPSG_6748() throws FactoryException {
        code              = 6748;
        name              = "Vanua Levu 1915";
        aliases           = new String[] {"Vanua Levu 1917"};
        ellipsoidName     = "Clarke 1880 (international foot)";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Vientiane 1982” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6676</b></li>
     *   <li>EPSG datum name: <b>Vientiane 1982</b></li>
     *   <li>Ellipsoid name: <b>Krassowsky 1940</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Laos</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Vientiane 1982")
    public void EPSG_6676() throws FactoryException {
        code              = 6676;
        name              = "Vientiane 1982";
        ellipsoidName     = "Krassowsky 1940";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Vietnam 2000” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6756</b></li>
     *   <li>EPSG datum name: <b>Vietnam 2000</b></li>
     *   <li>Alias(es) given by EPSG: <b>VN-2000</b></li>
     *   <li>Ellipsoid name: <b>WGS 84</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Vietnam - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Vietnam 2000")
    public void EPSG_6756() throws FactoryException {
        code              = 6756;
        name              = "Vietnam 2000";
        aliases           = new String[] {"VN-2000"};
        ellipsoidName     = "WGS 84";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Viti Levu 1912” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6752</b></li>
     *   <li>EPSG datum name: <b>Viti Levu 1912</b></li>
     *   <li>Alias(es) given by EPSG: <b>Viti Levu 1916</b></li>
     *   <li>Ellipsoid name: <b>Clarke 1880 (international foot)</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Fiji - Viti Levu</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Viti Levu 1912")
    public void EPSG_6752() throws FactoryException {
        code              = 6752;
        name              = "Viti Levu 1912";
        aliases           = new String[] {"Viti Levu 1916"};
        ellipsoidName     = "Clarke 1880 (international foot)";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Voirol 1875” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6304</b></li>
     *   <li>EPSG datum name: <b>Voirol 1875</b></li>
     *   <li>Ellipsoid name: <b>Clarke 1880 (IGN)</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Algeria - north of 32°N</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Voirol 1875")
    public void EPSG_6304() throws FactoryException {
        code              = 6304;
        name              = "Voirol 1875";
        ellipsoidName     = "Clarke 1880 (IGN)";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Voirol 1875 (Paris)” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6811</b></li>
     *   <li>EPSG datum name: <b>Voirol 1875 (Paris)</b></li>
     *   <li>Ellipsoid name: <b>Clarke 1880 (IGN)</b></li>
     *   <li>Prime meridian name: <b>Paris</b></li>
     *   <li>EPSG Usage Extent: <b>Algeria - north of 32°N</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Voirol 1875 (Paris)")
    public void EPSG_6811() throws FactoryException {
        code              = 6811;
        name              = "Voirol 1875 (Paris)";
        ellipsoidName     = "Clarke 1880 (IGN)";
        primeMeridianName = "Paris";
        verifyDatum();
    }

    /**
     * Tests “Voirol 1879” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6671</b></li>
     *   <li>EPSG datum name: <b>Voirol 1879</b></li>
     *   <li>Ellipsoid name: <b>Clarke 1880 (IGN)</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Algeria - north of 32°N</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Voirol 1879")
    public void EPSG_6671() throws FactoryException {
        code              = 6671;
        name              = "Voirol 1879";
        ellipsoidName     = "Clarke 1880 (IGN)";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Voirol 1879 (Paris)” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6821</b></li>
     *   <li>EPSG datum name: <b>Voirol 1879 (Paris)</b></li>
     *   <li>Ellipsoid name: <b>Clarke 1880 (IGN)</b></li>
     *   <li>Prime meridian name: <b>Paris</b></li>
     *   <li>EPSG Usage Extent: <b>Algeria - north of 32°N</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Voirol 1879 (Paris)")
    public void EPSG_6821() throws FactoryException {
        code              = 6821;
        name              = "Voirol 1879 (Paris)";
        ellipsoidName     = "Clarke 1880 (IGN)";
        primeMeridianName = "Paris";
        verifyDatum();
    }

    /**
     * Tests “Wake Island 1952” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6733</b></li>
     *   <li>EPSG datum name: <b>Wake Island 1952</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Wake - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Wake Island 1952")
    public void EPSG_6733() throws FactoryException {
        code              = 6733;
        name              = "Wake Island 1952";
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “WGS 72 Transit Broadcast Ephemeris” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6324</b></li>
     *   <li>EPSG datum name: <b>WGS 72 Transit Broadcast Ephemeris</b></li>
     *   <li>Alias(es) given by EPSG: <b>WGS 72BE</b></li>
     *   <li>Ellipsoid name: <b>WGS 72</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>World</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("WGS 72 Transit Broadcast Ephemeris")
    public void EPSG_6324() throws FactoryException {
        code              = 6324;
        name              = "WGS 72 Transit Broadcast Ephemeris";
        aliases           = new String[] {"WGS 72BE"};
        ellipsoidName     = "WGS 72";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “World Geodetic System 1966” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6760</b></li>
     *   <li>EPSG datum name: <b>World Geodetic System 1966</b></li>
     *   <li>Alias(es) given by EPSG: <b>WGS 66</b></li>
     *   <li>Ellipsoid name: <b>NWL 9D</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>World</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("World Geodetic System 1966")
    public void EPSG_6760() throws FactoryException {
        code              = 6760;
        name              = "World Geodetic System 1966";
        aliases           = new String[] {"WGS 66"};
        ellipsoidName     = "NWL 9D";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “World Geodetic System 1972” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6322</b></li>
     *   <li>EPSG datum name: <b>World Geodetic System 1972</b></li>
     *   <li>Alias(es) given by EPSG: <b>WGS 72</b></li>
     *   <li>Ellipsoid name: <b>WGS 72</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>World</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("World Geodetic System 1972")
    public void EPSG_6322() throws FactoryException {
        code              = 6322;
        name              = "World Geodetic System 1972";
        aliases           = new String[] {"WGS 72"};
        ellipsoidName     = "WGS 72";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “World Geodetic System 1984” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6326</b></li>
     *   <li>EPSG datum name: <b>World Geodetic System 1984</b></li>
     *   <li>Alias(es) given by EPSG: <b>WGS 84</b>, <b>World Geodetic System 1984</b></li>
     *   <li>Ellipsoid name: <b>WGS 84</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>World</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("World Geodetic System 1984")
    public void EPSG_6326() throws FactoryException {
        code              = 6326;
        name              = "World Geodetic System 1984";
        aliases           = new String[] {"WGS 84", "World Geodetic System 1984"};
        ellipsoidName     = "WGS 84";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Xian 1980” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6610</b></li>
     *   <li>EPSG datum name: <b>Xian 1980</b></li>
     *   <li>Ellipsoid name: <b>IAG 1975</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>China - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Xian 1980")
    public void EPSG_6610() throws FactoryException {
        code              = 6610;
        name              = "Xian 1980";
        ellipsoidName     = "IAG 1975";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Yacare” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6309</b></li>
     *   <li>EPSG datum name: <b>Yacare</b></li>
     *   <li>Alias(es) given by EPSG: <b>ROU-USAMS</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Uruguay - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Yacare")
    public void EPSG_6309() throws FactoryException {
        code              = 6309;
        name              = "Yacare";
        aliases           = new String[] {"ROU-USAMS"};
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Yemen National Geodetic Network 1996” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6163</b></li>
     *   <li>EPSG datum name: <b>Yemen National Geodetic Network 1996</b></li>
     *   <li>Alias(es) given by EPSG: <b>YNGN96</b></li>
     *   <li>Ellipsoid name: <b>WGS 84</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Yemen</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Yemen National Geodetic Network 1996")
    public void EPSG_6163() throws FactoryException {
        code              = 6163;
        name              = "Yemen National Geodetic Network 1996";
        aliases           = new String[] {"YNGN96"};
        ellipsoidName     = "WGS 84";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Yoff” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6310</b></li>
     *   <li>EPSG datum name: <b>Yoff</b></li>
     *   <li>Ellipsoid name: <b>Clarke 1880 (IGN)</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Senegal</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Yoff")
    public void EPSG_6310() throws FactoryException {
        code              = 6310;
        name              = "Yoff";
        ellipsoidName     = "Clarke 1880 (IGN)";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }

    /**
     * Tests “Zanderij” geodetic datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>6311</b></li>
     *   <li>EPSG datum name: <b>Zanderij</b></li>
     *   <li>Ellipsoid name: <b>International 1924</b></li>
     *   <li>Prime meridian name: <b>Greenwich</b></li>
     *   <li>EPSG Usage Extent: <b>Suriname</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the EPSG code.
     */
    @Test
    @DisplayName("Zanderij")
    public void EPSG_6311() throws FactoryException {
        code              = 6311;
        name              = "Zanderij";
        ellipsoidName     = "International 1924";
        primeMeridianName = "Greenwich";
        verifyDatum();
    }
}
