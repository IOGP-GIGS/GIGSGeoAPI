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
import org.opengis.referencing.crs.VerticalCRS;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.datum.VerticalDatum;
import org.iogp.gigs.internal.geoapi.Configuration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Verifies reference vertical CRSs bundled with the geoscience software.
 *
 * <table class="gigs">
 * <caption>Test description</caption>
 * <tr>
 *   <th>Test method:</th>
 *   <td>Compare vertical CRS definitions included in the software against the EPSG Dataset.</td>
 * </tr><tr>
 *   <th>Test data:</th>
 *   <td><a href="https://github.com/IOGP-GIGS/GIGSTestDataset/tree/main/GIGSTestDatasetFiles/GIGS%202200%20Predefined%20Geodetic%20Data%20Objects%20test%20data/ASCII/GIGS_lib_2210_VerticalCRS.txt">{@code GIGS_lib_2210_VerticalCRS.txt}</a>
 *       and EPSG Dataset.</td>
 * </tr><tr>
 *   <th>Tested API:</th>
 *   <td>{@link CRSAuthorityFactory#createVerticalCRS(String)}.</td>
 * </tr><tr>
 *   <th>Expected result:</th>
 *   <td>Definitions bundled with the software should have the same name and coordinate system
 *       (including axes direction and units) as in EPSG Dataset. CRSs missing from the software
 *       or at variance with those in the EPSG Dataset should be reported.</td>
 * </tr></table>
 *
 *
 * <h2>Usage example</h2>
 * in order to specify their factories and run the tests in a JUnit framework,
 * implementers can define a subclass in their own test suite as in the example below:
 *
 * {@snippet lang="java" :
 * public class MyTest extends Test2210 {
 *     public MyTest() {
 *         super(new MyCRSAuthorityFactory());
 *     }
 * }
 * }
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @author  Alexis Manin (Geomatys)
 * @version 1.0
 * @since   1.0
 */
@DisplayName("Vertical CRS")
public class Test2210 extends Series2000<VerticalCRS> {
    /**
     * The EPSG code of the expected {@link VerticalDatum}.
     * This field is set by all test methods before to create and verify the {@link VerticalCRS} instance.
     */
    public int datumCode;

    /**
     * The vertical CRS created by the factory, or {@code null} if not yet created or if CRS creation failed.
     *
     * @see #crsAuthorityFactory
     */
    private VerticalCRS crs;

    /**
     * Factory to use for building {@link VerticalCRS} instances, or {@code null} if none.
     * This is the factory used by the {@link #getIdentifiedObject()} method.
     */
    protected final CRSAuthorityFactory crsAuthorityFactory;

    /**
     * Creates a new test using the given factory. If a given factory is {@code null},
     * then the tests which depend on it will be skipped.
     *
     * @param crsFactory  factory for creating {@link VerticalCRS} instances.
     */
    public Test2210(final CRSAuthorityFactory crsFactory) {
        crsAuthorityFactory = crsFactory;
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
     *       <li>{@link #crsAuthorityFactory}</li>
     *     </ul>
     *   </li>
     * </ul>
     *
     * @return the configuration of the test being run.
     */
    @Override
    Configuration configuration() {
        final Configuration op = super.configuration();
        assertNull(op.put(Configuration.Key.crsAuthorityFactory, crsAuthorityFactory));
        return op;
    }

    /**
     * Returns the vertical CRS instance to be tested. When this method is invoked for the first time, it creates the
     * vertical CRS to test by invoking the {@link CRSAuthorityFactory#createVerticalCRS(String)} method with the
     * current {@link #code} value in argument. The created object is then cached and returned in all subsequent
     * invocations of this method.
     *
     * @return the vertical CRS instance to test.
     * @throws FactoryException if an error occurred while creating the vertical CRS instance.
     */
    @Override
    public VerticalCRS getIdentifiedObject() throws FactoryException {
        if (crs == null) {
            assumeNotNull(crsAuthorityFactory);
            try {
                crs = crsAuthorityFactory.createVerticalCRS(String.valueOf(code));
            } catch (NoSuchAuthorityCodeException e) {
                unsupportedCode(VerticalCRS.class, code, e);
            }
        }
        return crs;
    }

    /**
     * Returns an instance of the datum test class initialized to the datum of current CRS.
     *
     * @return instance for testing a dependency of current CRS.
     */
    private Test2209 datumTest() {
        final Test2209 test = new Test2209(null);
        test.configureAsDependency(this);
        test.setIdentifiedObject(crs.getDatum());
        return test;
    }

    /**
     * Verifies the properties of the vertical CRS given by {@link #getIdentifiedObject()}.
     *
     * @throws FactoryException if an error occurred while creating the vertical CRS.
     */
    private void verifyVerticalCRS() throws FactoryException {
        @SuppressWarnings("LocalVariableHidesMemberVariable")
        final VerticalCRS crs = getIdentifiedObject();
        assertNotNull(crs, "VerticalCRS");
        validators.validate(crs);

        // CRS identification.
        assertIdentifierEquals(code, crs, "VerticalCRS");
        assertNameEquals(true, name, crs, "VerticalCRS");
        assertAliasesEqual (aliases, crs, "VerticalCRS");

        // Datum associated to the CRS.
        final VerticalDatum datum = crs.getDatum();
        assertNotNull(datum, "VerticalCRS.getDatum()");

        // Datum identification.
        if (isDependencyIdentificationSupported) {
            configurationTip = Configuration.Key.isDependencyIdentificationSupported;
            assertIdentifierEquals(datumCode, datum, "VerticalCRS.getDatum()");
            configurationTip = null;
        }
    }

    /**
     * Tests “AHD (Tasmania) height” vertical CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>5712</b></li>
     *   <li>EPSG CRS name: <b>AHD (Tasmania) height</b></li>
     *   <li>Alias(es) given by EPSG: <b>Australian Height Datum height</b></li>
     *   <li>EPSG datum code: <b>5112</b></li>
     *   <li>EPSG Usage Extent: <b>Australia - Tasmania mainland onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the vertical CRS from the EPSG code.
     */
    @Test
    @DisplayName("AHD (Tasmania) height")
    public void EPSG_5712() throws FactoryException {
        code      = 5712;
        name      = "AHD (Tasmania) height";
        aliases   = new String[] {"Australian Height Datum height"};
        datumCode = 5112;
        verifyVerticalCRS();
        datumTest().EPSG_5112();
    }

    /**
     * Tests “AHD height” vertical CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>5711</b></li>
     *   <li>EPSG CRS name: <b>AHD height</b></li>
     *   <li>EPSG datum code: <b>5111</b></li>
     *   <li>EPSG Usage Extent: <b>Australia Christmas and Cocos - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the vertical CRS from the EPSG code.
     */
    @Test
    @DisplayName("AHD height")
    public void EPSG_5711() throws FactoryException {
        code      = 5711;
        name      = "AHD height";
        datumCode = 5111;
        verifyVerticalCRS();
        datumTest().EPSG_5111();
    }

    /**
     * Tests “AIOC95 depth” vertical CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>5734</b></li>
     *   <li>EPSG CRS name: <b>AIOC95 depth</b></li>
     *   <li>Alias(es) given by EPSG: <b>Australian Height Datum height</b></li>
     *   <li>EPSG datum code: <b>5133</b></li>
     *   <li>EPSG Usage Extent: <b>Azerbaijan - offshore and Sangachal</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the vertical CRS from the EPSG code.
     */
    @Test
    @DisplayName("AIOC95 depth")
    public void EPSG_5734() throws FactoryException {
        code      = 5734;
        name      = "AIOC95 depth";
        aliases   = new String[] {"Australian Height Datum height"};
        datumCode = 5133;
        verifyVerticalCRS();
        datumTest().EPSG_5133();
    }

    /**
     * Tests “AIOC95 height” vertical CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>5797</b></li>
     *   <li>EPSG CRS name: <b>AIOC95 height</b></li>
     *   <li>EPSG datum code: <b>5133</b></li>
     *   <li>EPSG Usage Extent: <b>Azerbaijan - offshore and Sangachal</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the vertical CRS from the EPSG code.
     */
    @Test
    @DisplayName("AIOC95 height")
    public void EPSG_5797() throws FactoryException {
        code      = 5797;
        name      = "AIOC95 height";
        datumCode = 5133;
        verifyVerticalCRS();
        datumTest().EPSG_5133();
    }

    /**
     * Tests “Baltic 1977 depth” vertical CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>5612</b></li>
     *   <li>EPSG CRS name: <b>Baltic 1977 depth</b></li>
     *   <li>Alias(es) given by EPSG: <b>Kronstadt 1977 height</b></li>
     *   <li>EPSG datum code: <b>5105</b></li>
     *   <li>EPSG Usage Extent: <b>Europe - FSU onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the vertical CRS from the EPSG code.
     */
    @Test
    @DisplayName("Baltic 1977 depth")
    public void EPSG_5612() throws FactoryException {
        code      = 5612;
        name      = "Baltic 1977 depth";
        aliases   = new String[] {"Kronstadt 1977 height"};
        datumCode = 5105;
        verifyVerticalCRS();
        datumTest().EPSG_5105();
    }

    /**
     * Tests “Baltic 1977 height” vertical CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>5705</b></li>
     *   <li>EPSG CRS name: <b>Baltic 1977 height</b></li>
     *   <li>EPSG datum code: <b>5105</b></li>
     *   <li>EPSG Usage Extent: <b>Europe - FSU onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the vertical CRS from the EPSG code.
     */
    @Test
    @DisplayName("Baltic 1977 height")
    public void EPSG_5705() throws FactoryException {
        code      = 5705;
        name      = "Baltic 1977 height";
        datumCode = 5105;
        verifyVerticalCRS();
        datumTest().EPSG_5105();
    }

    /**
     * Tests “Baltic 1982 height” vertical CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>5786</b></li>
     *   <li>EPSG CRS name: <b>Baltic 1982 height</b></li>
     *   <li>Alias(es) given by EPSG: <b>Baltic 1982</b></li>
     *   <li>EPSG datum code: <b>5184</b></li>
     *   <li>EPSG Usage Extent: <b>Bulgaria - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the vertical CRS from the EPSG code.
     */
    @Test
    @DisplayName("Baltic 1982 height")
    public void EPSG_5786() throws FactoryException {
        code      = 5786;
        name      = "Baltic 1982 height";
        aliases   = new String[] {"Baltic 1982"};
        datumCode = 5184;
        verifyVerticalCRS();
        datumTest().EPSG_5184();
    }

    /**
     * Tests “Bandar Abbas height” vertical CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>5752</b></li>
     *   <li>EPSG CRS name: <b>Bandar Abbas height</b></li>
     *   <li>EPSG datum code: <b>5150</b></li>
     *   <li>EPSG Usage Extent: <b>Iran - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the vertical CRS from the EPSG code.
     */
    @Test
    @DisplayName("Bandar Abbas height")
    public void EPSG_5752() throws FactoryException {
        code      = 5752;
        name      = "Bandar Abbas height";
        datumCode = 5150;
        verifyVerticalCRS();
        datumTest().EPSG_5150();
    }

    /**
     * Tests “Caspian depth” vertical CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>5706</b></li>
     *   <li>EPSG CRS name: <b>Caspian depth</b></li>
     *   <li>Alias(es) given by EPSG: <b>Canadian Geodetic Vertical Datum of 1928 height</b></li>
     *   <li>EPSG datum code: <b>5106</b></li>
     *   <li>EPSG Usage Extent: <b>Asia - FSU - Caspian Sea</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the vertical CRS from the EPSG code.
     */
    @Test
    @DisplayName("Caspian depth")
    public void EPSG_5706() throws FactoryException {
        code      = 5706;
        name      = "Caspian depth";
        aliases   = new String[] {"Canadian Geodetic Vertical Datum of 1928 height"};
        datumCode = 5106;
        verifyVerticalCRS();
        datumTest().EPSG_5106();
    }

    /**
     * Tests “CGVD28 height” vertical CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>5713</b></li>
     *   <li>EPSG CRS name: <b>CGVD28 height</b></li>
     *   <li>EPSG datum code: <b>5114</b></li>
     *   <li>EPSG Usage Extent: <b>Canada</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the vertical CRS from the EPSG code.
     */
    @Test
    @DisplayName("CGVD28 height")
    public void EPSG_5713() throws FactoryException {
        code      = 5713;
        name      = "CGVD28 height";
        datumCode = 5114;
        verifyVerticalCRS();
        datumTest().EPSG_5114();
    }

    /**
     * Tests “DHHN85 height” vertical CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>5784</b></li>
     *   <li>EPSG CRS name: <b>DHHN85 height</b></li>
     *   <li>EPSG datum code: <b>5182</b></li>
     *   <li>EPSG Usage Extent: <b>Germany - West Germany all states</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the vertical CRS from the EPSG code.
     */
    @Test
    @DisplayName("DHHN85 height")
    public void EPSG_5784() throws FactoryException {
        code      = 5784;
        name      = "DHHN85 height";
        datumCode = 5182;
        verifyVerticalCRS();
        datumTest().EPSG_5182();
    }

    /**
     * Tests “DHHN92 height” vertical CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>5783</b></li>
     *   <li>EPSG CRS name: <b>DHHN92 height</b></li>
     *   <li>EPSG datum code: <b>5181</b></li>
     *   <li>EPSG Usage Extent: <b>Germany - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the vertical CRS from the EPSG code.
     */
    @Test
    @DisplayName("DHHN92 height")
    public void EPSG_5783() throws FactoryException {
        code      = 5783;
        name      = "DHHN92 height";
        datumCode = 5181;
        verifyVerticalCRS();
        datumTest().EPSG_5181();
    }

    /**
     * Tests “EGM96 height” vertical CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>5773</b></li>
     *   <li>EPSG CRS name: <b>EGM96 height</b></li>
     *   <li>EPSG datum code: <b>5171</b></li>
     *   <li>EPSG Usage Extent: <b>World</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the vertical CRS from the EPSG code.
     */
    @Test
    @DisplayName("EGM96 height")
    public void EPSG_5773() throws FactoryException {
        code      = 5773;
        name      = "EGM96 height";
        datumCode = 5171;
        verifyVerticalCRS();
        datumTest().EPSG_5171();
    }

    /**
     * Tests “EVRF2000 height” vertical CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>5730</b></li>
     *   <li>EPSG CRS name: <b>EVRF2000 height</b></li>
     *   <li>EPSG datum code: <b>5129</b></li>
     *   <li>EPSG Usage Extent: <b>Europe</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the vertical CRS from the EPSG code.
     */
    @Test
    @DisplayName("EVRF2000 height")
    public void EPSG_5730() throws FactoryException {
        code      = 5730;
        name      = "EVRF2000 height";
        datumCode = 5129;
        verifyVerticalCRS();
        datumTest().EPSG_5129();
    }

    /**
     * Tests “EVRF2007 height” vertical CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>5621</b></li>
     *   <li>EPSG CRS name: <b>EVRF2007 height</b></li>
     *   <li>Alias(es) given by EPSG: <b>Fahud Height Datum height</b></li>
     *   <li>EPSG datum code: <b>5215</b></li>
     *   <li>EPSG Usage Extent: <b>Europe</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the vertical CRS from the EPSG code.
     */
    @Test
    @DisplayName("EVRF2007 height")
    public void EPSG_5621() throws FactoryException {
        code      = 5621;
        name      = "EVRF2007 height";
        aliases   = new String[] {"Fahud Height Datum height"};
        datumCode = 5215;
        verifyVerticalCRS();
        datumTest().EPSG_5215();
    }

    /**
     * Tests “Fahud HD height” vertical CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>5725</b></li>
     *   <li>EPSG CRS name: <b>Fahud HD height</b></li>
     *   <li>EPSG datum code: <b>5124</b></li>
     *   <li>EPSG Usage Extent: <b>Oman - mainland</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the vertical CRS from the EPSG code.
     */
    @Test
    @DisplayName("Fahud HD height")
    public void EPSG_5725() throws FactoryException {
        code      = 5725;
        name      = "Fahud HD height";
        datumCode = 5124;
        verifyVerticalCRS();
        datumTest().EPSG_5124();
    }

    /**
     * Tests “Fao height” vertical CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>5751</b></li>
     *   <li>EPSG CRS name: <b>Fao height</b></li>
     *   <li>EPSG datum code: <b>5149</b></li>
     *   <li>EPSG Usage Extent: <b>Asia - Middle East -SE Iraq and SW Iran</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the vertical CRS from the EPSG code.
     */
    @Test
    @DisplayName("Fao height")
    public void EPSG_5751() throws FactoryException {
        code      = 5751;
        name      = "Fao height";
        datumCode = 5149;
        verifyVerticalCRS();
        datumTest().EPSG_5149();
    }

    /**
     * Tests “KOC CD height” vertical CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>5790</b></li>
     *   <li>EPSG CRS name: <b>KOC CD height</b></li>
     *   <li>EPSG datum code: <b>5188</b></li>
     *   <li>EPSG Usage Extent: <b>Kuwait - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the vertical CRS from the EPSG code.
     */
    @Test
    @DisplayName("KOC CD height")
    public void EPSG_5790() throws FactoryException {
        code      = 5790;
        name      = "KOC CD height";
        datumCode = 5188;
        verifyVerticalCRS();
        datumTest().EPSG_5188();
    }

    /**
     * Tests “KOC WD depth” vertical CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>5789</b></li>
     *   <li>EPSG CRS name: <b>KOC WD depth</b></li>
     *   <li>EPSG datum code: <b>5187</b></li>
     *   <li>EPSG Usage Extent: <b>Kuwait - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the vertical CRS from the EPSG code.
     */
    @Test
    @DisplayName("KOC WD depth")
    public void EPSG_5789() throws FactoryException {
        code      = 5789;
        name      = "KOC WD depth";
        datumCode = 5187;
        verifyVerticalCRS();
        datumTest().EPSG_5187();
    }

    /**
     * Tests “KOC WD depth (ft)” vertical CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>5614</b></li>
     *   <li>EPSG CRS name: <b>KOC WD depth (ft)</b></li>
     *   <li>Alias(es) given by EPSG: <b>PWD height</b></li>
     *   <li>EPSG datum code: <b>5187</b></li>
     *   <li>EPSG Usage Extent: <b>Kuwait - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the vertical CRS from the EPSG code.
     */
    @Test
    @DisplayName("KOC WD depth (ft)")
    public void EPSG_5614() throws FactoryException {
        code      = 5614;
        name      = "KOC WD depth (ft)";
        aliases   = new String[] {"PWD height"};
        datumCode = 5187;
        verifyVerticalCRS();
        datumTest().EPSG_5187();
    }

    /**
     * Tests “Kuwait PWD height” vertical CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>5788</b></li>
     *   <li>EPSG CRS name: <b>Kuwait PWD height</b></li>
     *   <li>EPSG datum code: <b>5186</b></li>
     *   <li>EPSG Usage Extent: <b>Kuwait - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the vertical CRS from the EPSG code.
     */
    @Test
    @DisplayName("Kuwait PWD height")
    public void EPSG_5788() throws FactoryException {
        code      = 5788;
        name      = "Kuwait PWD height";
        datumCode = 5186;
        verifyVerticalCRS();
        datumTest().EPSG_5186();
    }

    /**
     * Tests “Lagos 1955 height” vertical CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>5796</b></li>
     *   <li>EPSG CRS name: <b>Lagos 1955 height</b></li>
     *   <li>Alias(es) given by EPSG: <b>mean sea level depth</b></li>
     *   <li>EPSG datum code: <b>5194</b></li>
     *   <li>EPSG Usage Extent: <b>Nigeria - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the vertical CRS from the EPSG code.
     */
    @Test
    @DisplayName("Lagos 1955 height")
    public void EPSG_5796() throws FactoryException {
        code      = 5796;
        name      = "Lagos 1955 height";
        aliases   = new String[] {"mean sea level depth"};
        datumCode = 5194;
        verifyVerticalCRS();
        datumTest().EPSG_5194();
    }

    /**
     * Tests “MSL depth” vertical CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>5715</b></li>
     *   <li>EPSG CRS name: <b>MSL depth</b></li>
     *   <li>Alias(es) given by EPSG: <b>mean sea level height</b></li>
     *   <li>EPSG datum code: <b>5100</b></li>
     *   <li>EPSG Usage Extent: <b>World</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the vertical CRS from the EPSG code.
     */
    @Test
    @DisplayName("MSL depth")
    public void EPSG_5715() throws FactoryException {
        code      = 5715;
        name      = "MSL depth";
        aliases   = new String[] {"mean sea level height"};
        datumCode = 5100;
        verifyVerticalCRS();
        datumTest().EPSG_5100();
    }

    /**
     * Tests “msl height” vertical CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>5714</b></li>
     *   <li>EPSG CRS name: <b>msl height</b></li>
     *   <li>Alias(es) given by EPSG: <b>Normaal Amsterdams Peil height</b></li>
     *   <li>EPSG datum code: <b>5100</b></li>
     *   <li>EPSG Usage Extent: <b>World</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the vertical CRS from the EPSG code.
     */
    @Test
    @DisplayName("msl height")
    public void EPSG_5714() throws FactoryException {
        code      = 5714;
        name      = "msl height";
        aliases   = new String[] {"Normaal Amsterdams Peil height"};
        datumCode = 5100;
        verifyVerticalCRS();
        datumTest().EPSG_5100();
    }

    /**
     * Tests “NAP height” vertical CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>5709</b></li>
     *   <li>EPSG CRS name: <b>NAP height</b></li>
     *   <li>Alias(es) given by EPSG: <b>North American Vertical Datum of 1988 height (m)</b></li>
     *   <li>EPSG datum code: <b>5109</b></li>
     *   <li>EPSG Usage Extent: <b>Netherlands</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the vertical CRS from the EPSG code.
     */
    @Test
    @DisplayName("NAP height")
    public void EPSG_5709() throws FactoryException {
        code      = 5709;
        name      = "NAP height";
        aliases   = new String[] {"North American Vertical Datum of 1988 height (m)"};
        datumCode = 5109;
        verifyVerticalCRS();
        datumTest().EPSG_5109();
    }

    /**
     * Tests “NAVD88 height” vertical CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>5703</b></li>
     *   <li>EPSG CRS name: <b>NAVD88 height</b></li>
     *   <li>EPSG datum code: <b>5103</b></li>
     *   <li>EPSG Usage Extent: <b>North America - Mexico and USA - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the vertical CRS from the EPSG code.
     */
    @Test
    @DisplayName("NAVD88 height")
    public void EPSG_5703() throws FactoryException {
        code      = 5703;
        name      = "NAVD88 height";
        datumCode = 5103;
        verifyVerticalCRS();
        datumTest().EPSG_5103();
    }

    /**
     * Tests “NGF-IGN69 height” vertical CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>5720</b></li>
     *   <li>EPSG CRS name: <b>NGF-IGN69 height</b></li>
     *   <li>EPSG datum code: <b>5119</b></li>
     *   <li>EPSG Usage Extent: <b>France - mainland onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the vertical CRS from the EPSG code.
     */
    @Test
    @DisplayName("NGF-IGN69 height")
    public void EPSG_5720() throws FactoryException {
        code      = 5720;
        name      = "NGF-IGN69 height";
        datumCode = 5119;
        verifyVerticalCRS();
        datumTest().EPSG_5119();
    }

    /**
     * Tests “NGF Lallemand height” vertical CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>5719</b></li>
     *   <li>EPSG CRS name: <b>NGF Lallemand height</b></li>
     *   <li>Alias(es) given by EPSG: <b>National Geodetic Vertical Datum of 1929 height (ftUS)</b></li>
     *   <li>EPSG datum code: <b>5118</b></li>
     *   <li>EPSG Usage Extent: <b>France - mainland onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the vertical CRS from the EPSG code.
     */
    @Test
    @DisplayName("NGF Lallemand height")
    public void EPSG_5719() throws FactoryException {
        code      = 5719;
        name      = "NGF Lallemand height";
        aliases   = new String[] {"National Geodetic Vertical Datum of 1929 height (ftUS)"};
        datumCode = 5118;
        verifyVerticalCRS();
        datumTest().EPSG_5118();
    }

    /**
     * Tests “NGVD29 height (ftUS)” vertical CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>5702</b></li>
     *   <li>EPSG CRS name: <b>NGVD29 height (ftUS)</b></li>
     *   <li>Alias(es) given by EPSG: <b>Newlyn height</b></li>
     *   <li>EPSG datum code: <b>5102</b></li>
     *   <li>EPSG Usage Extent: <b>USA - CONUS - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the vertical CRS from the EPSG code.
     */
    @Test
    @DisplayName("NGVD29 height (ftUS)")
    public void EPSG_5702() throws FactoryException {
        code      = 5702;
        name      = "NGVD29 height (ftUS)";
        aliases   = new String[] {"Newlyn height"};
        datumCode = 5102;
        verifyVerticalCRS();
        datumTest().EPSG_5102();
    }

    /**
     * Tests “ODN height” vertical CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>5701</b></li>
     *   <li>EPSG CRS name: <b>ODN height</b></li>
     *   <li>Alias(es) given by EPSG: <b>PDO Height Datum 1993 height</b></li>
     *   <li>EPSG datum code: <b>5101</b></li>
     *   <li>EPSG Usage Extent: <b>UK - Great Britain mainland onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the vertical CRS from the EPSG code.
     */
    @Test
    @DisplayName("ODN height")
    public void EPSG_5701() throws FactoryException {
        code      = 5701;
        name      = "ODN height";
        aliases   = new String[] {"PDO Height Datum 1993 height"};
        datumCode = 5101;
        verifyVerticalCRS();
        datumTest().EPSG_5101();
    }

    /**
     * Tests “PHD93 height” vertical CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>5724</b></li>
     *   <li>EPSG CRS name: <b>PHD93 height</b></li>
     *   <li>Alias(es) given by EPSG: <b>Huang Hai 1956 height</b></li>
     *   <li>EPSG datum code: <b>5123</b></li>
     *   <li>EPSG Usage Extent: <b>Oman - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the vertical CRS from the EPSG code.
     */
    @Test
    @DisplayName("PHD93 height")
    public void EPSG_5724() throws FactoryException {
        code      = 5724;
        name      = "PHD93 height";
        aliases   = new String[] {"Huang Hai 1956 height"};
        datumCode = 5123;
        verifyVerticalCRS();
        datumTest().EPSG_5123();
    }

    /**
     * Tests “Yellow Sea 1956 height” vertical CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>5736</b></li>
     *   <li>EPSG CRS name: <b>Yellow Sea 1956 height</b></li>
     *   <li>Alias(es) given by EPSG: <b>Huang Hai 1985 height</b></li>
     *   <li>EPSG datum code: <b>5104</b></li>
     *   <li>EPSG Usage Extent: <b>China - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the vertical CRS from the EPSG code.
     */
    @Test
    @DisplayName("Yellow Sea 1956 height")
    public void EPSG_5736() throws FactoryException {
        code      = 5736;
        name      = "Yellow Sea 1956 height";
        aliases   = new String[] {"Huang Hai 1985 height"};
        datumCode = 5104;
        verifyVerticalCRS();
        datumTest().EPSG_5104();
    }

    /**
     * Tests “Yellow Sea 1985 height” vertical CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>5737</b></li>
     *   <li>EPSG CRS name: <b>Yellow Sea 1985 height</b></li>
     *   <li>EPSG datum code: <b>5137</b></li>
     *   <li>EPSG Usage Extent: <b>China - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the vertical CRS from the EPSG code.
     */
    @Test
    @DisplayName("Yellow Sea 1985 height")
    public void EPSG_5737() throws FactoryException {
        code      = 5737;
        name      = "Yellow Sea 1985 height";
        datumCode = 5137;
        verifyVerticalCRS();
        datumTest().EPSG_5137();
    }
}
