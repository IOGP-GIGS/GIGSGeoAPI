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
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.datum.DatumAuthorityFactory;
import org.opengis.referencing.datum.VerticalDatum;
import org.iogp.gigs.internal.geoapi.Configuration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Verifies reference vertical datums with the geoscience software.
 *
 * <table class="gigs">
 * <caption>Test description</caption>
 * <tr>
 *   <th>Test method:</th>
 *   <td>Compare vertical datum definitions included in the software against the EPSG Dataset.</td>
 * </tr><tr>
 *   <th>Test data:</th>
 *   <td><a href="https://github.com/IOGP-GIGS/GIGSTestDataset/tree/main/GIGSTestDatasetFiles/GIGS%202200%20Predefined%20Geodetic%20Data%20Objects%20test%20data/ASCII/GIGS_lib_2209_VerticalDatum.txt">{@code GIGS_lib_2209_VerticalDatum.txt}</a>
 *       and EPSG Dataset.</td>
 * </tr><tr>
 *   <th>Tested API:</th>
 *   <td>{@link DatumAuthorityFactory#createVerticalDatum(String)}.</td>
 * </tr><tr>
 *   <th>Expected result:</th>
 *   <td>Definitions bundled with the software should have the same name as in EPSG Dataset.
 *       Datums missing from the software or at variance with those in the EPSG Dataset should be reported.</td>
 * </tr></table>
 *
 *
 * <h2>Usage example</h2>
 * in order to specify their factories and run the tests in a JUnit framework,
 * implementers can define a subclass in their own test suite as in the example below:
 *
 * {@snippet lang="java" :
 * public class MyTest extends Test2209 {
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
@DisplayName("Vertical datum")
public class Test2209 extends Series2000<VerticalDatum> {
    /**
     * The vertical datum created by the factory, or {@code null} if not yet created or if datum creation failed.
     *
     * @see #datumAuthorityFactory
     */
    private VerticalDatum datum;

    /**
     * Factory to use for building {@link VerticalDatum} instances, or {@code null} if none.
     */
    protected final DatumAuthorityFactory datumAuthorityFactory;

    /**
     * Creates a new test using the given factory. If a given factory is {@code null},
     * then the tests which depend on it will be skipped.
     *
     * @param datumFactory  factory for creating {@link VerticalDatum} instances.
     */
    public Test2209(final DatumAuthorityFactory datumFactory) {
        datumAuthorityFactory = datumFactory;
    }

    /**
     * Returns information about the configuration of the test which has been run.
     * This method returns a map containing:
     *
     * <ul>
     *   <li>All the following values associated to the {@link Configuration.Key} of the same name:
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
     * Returns the vertical datum instance to be tested. When this method is invoked for the first time, it creates the
     * vertical datum to test by invoking the {@link DatumAuthorityFactory#createVerticalDatum(String)} method with the
     * current {@link #code} value in argument. The created object is then cached and returned in all subsequent
     * invocations of this method.
     *
     * @return the vertical datum instance to test.
     * @throws FactoryException if an error occurred while creating the vertical datum instance.
     */
    @Override
    public VerticalDatum getIdentifiedObject() throws FactoryException {
        if (datum == null) {
            assumeNotNull(datumAuthorityFactory);
            try {
                datum = datumAuthorityFactory.createVerticalDatum(String.valueOf(code));
            } catch (NoSuchAuthorityCodeException e) {
                unsupportedCode(VerticalDatum.class, code);
                throw e;
            }
        }
        return datum;
    }

    /**
     * Sets the datum instance to be tested.
     * This is used for testing vertical CRS dependencies.
     *
     * @param  dependency  the CRS dependency to test.
     */
    final void setIdentifiedObject(final VerticalDatum dependency) {
        assertNull(datum);
        datum = dependency;
    }

    /**
     * Creates a vertical datum for the current {@link #code}, then verifies its name and properties.
     *
     * @throws FactoryException if an error occurred while creating the vertical datum instance.
     */
    private void verifyVerticalDatum() throws FactoryException {
        @SuppressWarnings("LocalVariableHidesMemberVariable")
        final VerticalDatum datum = getIdentifiedObject();
        assertNotNull(datum, "VerticalDatum");
        validators.validate(datum);

        // Datum identification.
        assertIdentifierEquals(code, datum, "VerticalDatum");
        assertNameEquals(true, name, datum, "VerticalDatum");
        assertAliasesEqual( aliases, datum, "VerticalDatum");
    }

    /**
     * Tests “AIOC 1995” vertical datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>5133</b></li>
     *   <li>EPSG datum name: <b>AIOC 1995</b></li>
     *   <li>Alias(es) given by EPSG: <b>AIOC95</b></li>
     *   <li>EPSG Usage Extent: <b>Azerbaijan - offshore and Sangachal</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the vertical datum from the EPSG code.
     */
    @Test
    @DisplayName("AIOC 1995")
    public void EPSG_5133() throws FactoryException {
        code    = 5133;
        name    = "AIOC 1995";
        aliases = new String[] {"AIOC95"};
        verifyVerticalDatum();
    }

    /**
     * Tests “Australian Height Datum” vertical datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>5111</b></li>
     *   <li>EPSG datum name: <b>Australian Height Datum</b></li>
     *   <li>Alias(es) given by EPSG: <b>AHD</b>, <b>AHD71</b>, <b>AHD-TAS83</b></li>
     *   <li>EPSG Usage Extent: <b>Australia Christmas and Cocos - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the vertical datum from the EPSG code.
     */
    @Test
    @DisplayName("Australian Height Datum")
    public void EPSG_5111() throws FactoryException {
        code    = 5111;
        name    = "Australian Height Datum";
        aliases = new String[] {"AHD", "AHD71", "AHD-TAS83"};
        verifyVerticalDatum();
    }

    /**
     * Tests “Australian Height Datum (Tasmania)” vertical datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>5112</b></li>
     *   <li>EPSG datum name: <b>Australian Height Datum (Tasmania)</b></li>
     *   <li>Alias(es) given by EPSG: <b>AHD (Tasmania)</b></li>
     *   <li>EPSG Usage Extent: <b>Australia - Tasmania mainland onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the vertical datum from the EPSG code.
     */
    @Test
    @DisplayName("Australian Height Datum (Tasmania)")
    public void EPSG_5112() throws FactoryException {
        code    = 5112;
        name    = "Australian Height Datum (Tasmania)";
        aliases = new String[] {"AHD (Tasmania)"};
        verifyVerticalDatum();
    }

    /**
     * Tests “Baltic 1977” vertical datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>5105</b></li>
     *   <li>EPSG datum name: <b>Baltic 1977</b></li>
     *   <li>Alias(es) given by EPSG: <b>Baltic</b>, <b>Baltic Sea</b></li>
     *   <li>EPSG Usage Extent: <b>Europe - FSU onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the vertical datum from the EPSG code.
     */
    @Test
    @DisplayName("Baltic 1977")
    public void EPSG_5105() throws FactoryException {
        code    = 5105;
        name    = "Baltic 1977";
        aliases = new String[] {"Baltic", "Baltic Sea"};
        verifyVerticalDatum();
    }

    /**
     * Tests “Baltic 1982” vertical datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>5184</b></li>
     *   <li>EPSG datum name: <b>Baltic 1982</b></li>
     *   <li>EPSG Usage Extent: <b>Bulgaria - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the vertical datum from the EPSG code.
     */
    @Test
    @DisplayName("Baltic 1982")
    public void EPSG_5184() throws FactoryException {
        code    = 5184;
        name    = "Baltic 1982";
        verifyVerticalDatum();
    }

    /**
     * Tests “Bandar Abbas” vertical datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>5150</b></li>
     *   <li>EPSG datum name: <b>Bandar Abbas</b></li>
     *   <li>EPSG Usage Extent: <b>Iran - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the vertical datum from the EPSG code.
     */
    @Test
    @DisplayName("Bandar Abbas")
    public void EPSG_5150() throws FactoryException {
        code    = 5150;
        name    = "Bandar Abbas";
        verifyVerticalDatum();
    }

    /**
     * Tests “Canadian Geodetic Vertical Datum of 1928” vertical datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>5114</b></li>
     *   <li>EPSG datum name: <b>Canadian Geodetic Vertical Datum of 1928</b></li>
     *   <li>Alias(es) given by EPSG: <b>CVD28</b>, <b>Canadian Vertical Datum of 1928</b>, <b>CGVD28</b></li>
     *   <li>EPSG Usage Extent: <b>Canada</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the vertical datum from the EPSG code.
     */
    @Test
    @DisplayName("Canadian Geodetic Vertical Datum of 1928")
    public void EPSG_5114() throws FactoryException {
        code    = 5114;
        name    = "Canadian Geodetic Vertical Datum of 1928";
        aliases = new String[] {"CVD28", "Canadian Vertical Datum of 1928", "CGVD28"};
        verifyVerticalDatum();
    }

    /**
     * Tests “Caspian Sea” vertical datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>5106</b></li>
     *   <li>EPSG datum name: <b>Caspian Sea</b></li>
     *   <li>Alias(es) given by EPSG: <b>Caspian</b></li>
     *   <li>EPSG Usage Extent: <b>Asia - FSU - Caspian Sea</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the vertical datum from the EPSG code.
     */
    @Test
    @DisplayName("Caspian Sea")
    public void EPSG_5106() throws FactoryException {
        code    = 5106;
        name    = "Caspian Sea";
        aliases = new String[] {"Caspian"};
        verifyVerticalDatum();
    }

    /**
     * Tests “Deutches Haupthohennetz 1985” vertical datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>5182</b></li>
     *   <li>EPSG datum name: <b>Deutches Haupthohennetz 1985</b></li>
     *   <li>Alias(es) given by EPSG: <b>DHHN85</b>, <b>Deutsches Haupth√∂hennetz 1985</b></li>
     *   <li>EPSG Usage Extent: <b>Germany - West Germany all states</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the vertical datum from the EPSG code.
     */
    @Test
    @DisplayName("Deutches Haupthohennetz 1985")
    public void EPSG_5182() throws FactoryException {
        code    = 5182;
        name    = "Deutches Haupthohennetz 1985";
        aliases = new String[] {"DHHN85", "Deutsches Haupth√∂hennetz 1985"};
        verifyVerticalDatum();
    }

    /**
     * Tests “Deutches Haupthohennetz 1992” vertical datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>5181</b></li>
     *   <li>EPSG datum name: <b>Deutches Haupthohennetz 1992</b></li>
     *   <li>Alias(es) given by EPSG: <b>DHHN92</b>, <b>Deutsches Haupth√∂hennetz 1992</b></li>
     *   <li>EPSG Usage Extent: <b>Germany - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the vertical datum from the EPSG code.
     */
    @Test
    @DisplayName("Deutches Haupthohennetz 1992")
    public void EPSG_5181() throws FactoryException {
        code    = 5181;
        name    = "Deutches Haupthohennetz 1992";
        aliases = new String[] {"DHHN92", "Deutsches Haupth√∂hennetz 1992"};
        verifyVerticalDatum();
    }

    /**
     * Tests “EGM96 geoid” vertical datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>5171</b></li>
     *   <li>EPSG datum name: <b>EGM96 geoid</b></li>
     *   <li>Alias(es) given by EPSG: <b>EGM96</b></li>
     *   <li>EPSG Usage Extent: <b>World</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the vertical datum from the EPSG code.
     */
    @Test
    @DisplayName("EGM96 geoid")
    public void EPSG_5171() throws FactoryException {
        code    = 5171;
        name    = "EGM96 geoid";
        aliases = new String[] {"EGM96"};
        verifyVerticalDatum();
    }

    /**
     * Tests “European Vertical Reference Frame 2000” vertical datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>5129</b></li>
     *   <li>EPSG datum name: <b>European Vertical Reference Frame 2000</b></li>
     *   <li>Alias(es) given by EPSG: <b>EVRF2000</b></li>
     *   <li>EPSG Usage Extent: <b>Europe</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the vertical datum from the EPSG code.
     */
    @Test
    @DisplayName("European Vertical Reference Frame 2000")
    public void EPSG_5129() throws FactoryException {
        code    = 5129;
        name    = "European Vertical Reference Frame 2000";
        aliases = new String[] {"EVRF2000"};
        verifyVerticalDatum();
    }

    /**
     * Tests “European Vertical Reference Frame 2007” vertical datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>5215</b></li>
     *   <li>EPSG datum name: <b>European Vertical Reference Frame 2007</b></li>
     *   <li>Alias(es) given by EPSG: <b>EVRF2007</b></li>
     *   <li>EPSG Usage Extent: <b>Europe</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the vertical datum from the EPSG code.
     */
    @Test
    @DisplayName("European Vertical Reference Frame 2007")
    public void EPSG_5215() throws FactoryException {
        code    = 5215;
        name    = "European Vertical Reference Frame 2007";
        aliases = new String[] {"EVRF2007"};
        verifyVerticalDatum();
    }

    /**
     * Tests “Fahud Height Datum” vertical datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>5124</b></li>
     *   <li>EPSG datum name: <b>Fahud Height Datum</b></li>
     *   <li>Alias(es) given by EPSG: <b>Fahud HD</b></li>
     *   <li>EPSG Usage Extent: <b>Oman - mainland</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the vertical datum from the EPSG code.
     */
    @Test
    @DisplayName("Fahud Height Datum")
    public void EPSG_5124() throws FactoryException {
        code    = 5124;
        name    = "Fahud Height Datum";
        aliases = new String[] {"Fahud HD"};
        verifyVerticalDatum();
    }

    /**
     * Tests “Fao” vertical datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>5149</b></li>
     *   <li>EPSG datum name: <b>Fao</b></li>
     *   <li>Alias(es) given by EPSG: <b>British Vertical Datum</b></li>
     *   <li>EPSG Usage Extent: <b>Asia - Middle East -SE Iraq and SW Iran</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the vertical datum from the EPSG code.
     */
    @Test
    @DisplayName("Fao")
    public void EPSG_5149() throws FactoryException {
        code    = 5149;
        name    = "Fao";
        aliases = new String[] {"British Vertical Datum"};
        verifyVerticalDatum();
    }

    /**
     * Tests “KOC Construction Datum” vertical datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>5188</b></li>
     *   <li>EPSG datum name: <b>KOC Construction Datum</b></li>
     *   <li>Alias(es) given by EPSG: <b>CD</b></li>
     *   <li>EPSG Usage Extent: <b>Kuwait - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the vertical datum from the EPSG code.
     */
    @Test
    @DisplayName("KOC Construction Datum")
    public void EPSG_5188() throws FactoryException {
        code    = 5188;
        name    = "KOC Construction Datum";
        aliases = new String[] {"CD"};
        verifyVerticalDatum();
    }

    /**
     * Tests “KOC Well Datum” vertical datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>5187</b></li>
     *   <li>EPSG datum name: <b>KOC Well Datum</b></li>
     *   <li>Alias(es) given by EPSG: <b>WD</b></li>
     *   <li>EPSG Usage Extent: <b>Kuwait - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the vertical datum from the EPSG code.
     */
    @Test
    @DisplayName("KOC Well Datum")
    public void EPSG_5187() throws FactoryException {
        code    = 5187;
        name    = "KOC Well Datum";
        aliases = new String[] {"WD"};
        verifyVerticalDatum();
    }

    /**
     * Tests “Kuwait PWD” vertical datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>5186</b></li>
     *   <li>EPSG datum name: <b>Kuwait PWD</b></li>
     *   <li>Alias(es) given by EPSG: <b>PWD</b></li>
     *   <li>EPSG Usage Extent: <b>Kuwait - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the vertical datum from the EPSG code.
     */
    @Test
    @DisplayName("Kuwait PWD")
    public void EPSG_5186() throws FactoryException {
        code    = 5186;
        name    = "Kuwait PWD";
        aliases = new String[] {"PWD"};
        verifyVerticalDatum();
    }

    /**
     * Tests “Lagos 1955” vertical datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>5194</b></li>
     *   <li>EPSG datum name: <b>Lagos 1955</b></li>
     *   <li>EPSG Usage Extent: <b>Nigeria - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the vertical datum from the EPSG code.
     */
    @Test
    @DisplayName("Lagos 1955")
    public void EPSG_5194() throws FactoryException {
        code    = 5194;
        name    = "Lagos 1955";
        verifyVerticalDatum();
    }

    /**
     * Tests “Mean Sea Level” vertical datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>5100</b></li>
     *   <li>EPSG datum name: <b>Mean Sea Level</b></li>
     *   <li>Alias(es) given by EPSG: <b>MSL</b></li>
     *   <li>EPSG Usage Extent: <b>World</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the vertical datum from the EPSG code.
     */
    @Test
    @DisplayName("Mean Sea Level")
    public void EPSG_5100() throws FactoryException {
        code    = 5100;
        name    = "Mean Sea Level";
        aliases = new String[] {"MSL"};
        verifyVerticalDatum();
    }

    /**
     * Tests “National Geodetic Vertical Datum 1929” vertical datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>5102</b></li>
     *   <li>EPSG datum name: <b>National Geodetic Vertical Datum 1929</b></li>
     *   <li>Alias(es) given by EPSG: <b>NGVD29</b></li>
     *   <li>EPSG Usage Extent: <b>USA - CONUS - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the vertical datum from the EPSG code.
     */
    @Test
    @DisplayName("National Geodetic Vertical Datum 1929")
    public void EPSG_5102() throws FactoryException {
        code    = 5102;
        name    = "National Geodetic Vertical Datum 1929";
        aliases = new String[] {"NGVD29"};
        verifyVerticalDatum();
    }

    /**
     * Tests “Nivellement General de la France - IGN69” vertical datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>5119</b></li>
     *   <li>EPSG datum name: <b>Nivellement General de la France - IGN69</b></li>
     *   <li>Alias(es) given by EPSG: <b>NGF-IGN69</b>, <b>Nivellement general de la France</b>, <b>NGF</b></li>
     *   <li>EPSG Usage Extent: <b>France - mainland onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the vertical datum from the EPSG code.
     */
    @Test
    @DisplayName("Nivellement General de la France - IGN69")
    public void EPSG_5119() throws FactoryException {
        code    = 5119;
        name    = "Nivellement General de la France - IGN69";
        aliases = new String[] {"NGF-IGN69", "Nivellement general de la France", "NGF"};
        verifyVerticalDatum();
    }

    /**
     * Tests “Nivellement General de la France - Lallemand” vertical datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>5118</b></li>
     *   <li>EPSG datum name: <b>Nivellement General de la France - Lallemand</b></li>
     *   <li>Alias(es) given by EPSG: <b>NGF - Lallemand</b>, <b>NGF</b>, <b>Nivellement general de la France</b></li>
     *   <li>EPSG Usage Extent: <b>France - mainland onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the vertical datum from the EPSG code.
     */
    @Test
    @DisplayName("Nivellement General de la France - Lallemand")
    public void EPSG_5118() throws FactoryException {
        code    = 5118;
        name    = "Nivellement General de la France - Lallemand";
        aliases = new String[] {"NGF - Lallemand", "NGF", "Nivellement general de la France"};
        verifyVerticalDatum();
    }

    /**
     * Tests “Normaal Amsterdams Peil” vertical datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>5109</b></li>
     *   <li>EPSG datum name: <b>Normaal Amsterdams Peil</b></li>
     *   <li>Alias(es) given by EPSG: <b>NAP</b></li>
     *   <li>EPSG Usage Extent: <b>Netherlands</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the vertical datum from the EPSG code.
     */
    @Test
    @DisplayName("Normaal Amsterdams Peil")
    public void EPSG_5109() throws FactoryException {
        code    = 5109;
        name    = "Normaal Amsterdams Peil";
        aliases = new String[] {"NAP"};
        verifyVerticalDatum();
    }

    /**
     * Tests “North American Vertical Datum 1988” vertical datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>5103</b></li>
     *   <li>EPSG datum name: <b>North American Vertical Datum 1988</b></li>
     *   <li>Alias(es) given by EPSG: <b>NAVD88</b></li>
     *   <li>EPSG Usage Extent: <b>North America - Mexico and USA - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the vertical datum from the EPSG code.
     */
    @Test
    @DisplayName("North American Vertical Datum 1988")
    public void EPSG_5103() throws FactoryException {
        code    = 5103;
        name    = "North American Vertical Datum 1988";
        aliases = new String[] {"NAVD88"};
        verifyVerticalDatum();
    }

    /**
     * Tests “Ordnance Datum Newlyn” vertical datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>5101</b></li>
     *   <li>EPSG datum name: <b>Ordnance Datum Newlyn</b></li>
     *   <li>Alias(es) given by EPSG: <b>ODN</b>, <b>Newlyn</b></li>
     *   <li>EPSG Usage Extent: <b>UK - Great Britain mainland onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the vertical datum from the EPSG code.
     */
    @Test
    @DisplayName("Ordnance Datum Newlyn")
    public void EPSG_5101() throws FactoryException {
        code    = 5101;
        name    = "Ordnance Datum Newlyn";
        aliases = new String[] {"ODN", "Newlyn"};
        verifyVerticalDatum();
    }

    /**
     * Tests “PDO Height Datum 1993” vertical datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>5123</b></li>
     *   <li>EPSG datum name: <b>PDO Height Datum 1993</b></li>
     *   <li>Alias(es) given by EPSG: <b>PHD93</b></li>
     *   <li>EPSG Usage Extent: <b>Oman - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the vertical datum from the EPSG code.
     */
    @Test
    @DisplayName("PDO Height Datum 1993")
    public void EPSG_5123() throws FactoryException {
        code    = 5123;
        name    = "PDO Height Datum 1993";
        aliases = new String[] {"PHD93"};
        verifyVerticalDatum();
    }

    /**
     * Tests “Yellow Sea 1956” vertical datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>5104</b></li>
     *   <li>EPSG datum name: <b>Yellow Sea 1956</b></li>
     *   <li>Alias(es) given by EPSG: <b>Huang Hai 1956</b>, <b>Yellow Sea</b></li>
     *   <li>EPSG Usage Extent: <b>China - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the vertical datum from the EPSG code.
     */
    @Test
    @DisplayName("Yellow Sea 1956")
    public void EPSG_5104() throws FactoryException {
        code    = 5104;
        name    = "Yellow Sea 1956";
        aliases = new String[] {"Huang Hai 1956", "Yellow Sea"};
        verifyVerticalDatum();
    }

    /**
     * Tests “Yellow Sea 1985” vertical datum creation from the factory.
     *
     * <ul>
     *   <li>EPSG datum code: <b>5137</b></li>
     *   <li>EPSG datum name: <b>Yellow Sea 1985</b></li>
     *   <li>Alias(es) given by EPSG: <b>Huang Hai 1985</b></li>
     *   <li>EPSG Usage Extent: <b>China - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the vertical datum from the EPSG code.
     */
    @Test
    @DisplayName("Yellow Sea 1985")
    public void EPSG_5137() throws FactoryException {
        code    = 5137;
        name    = "Yellow Sea 1985";
        aliases = new String[] {"Huang Hai 1985"};
        verifyVerticalDatum();
    }
}
