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
import org.opengis.util.NoSuchIdentifierException;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.ProjectedCRS;
import org.opengis.referencing.cs.AxisDirection;
import org.opengis.referencing.cs.CartesianCS;
import org.iogp.gigs.internal.geoapi.Configuration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.iogp.gigs.internal.geoapi.Assert.assertAxisDirectionsEqual;


/**
 * Verifies reference projected CRSs bundled with the geoscience software.
 *
 * <table class="gigs">
 * <caption>Test description</caption>
 * <tr>
 *   <th>Test method:</th>
 *   <td>Compare projected CRS definitions included in the software against the EPSG Dataset.</td>
 * </tr><tr>
 *   <th>Test data:</th>
 *   <td><a href="https://github.com/IOGP-GIGS/GIGSTestDataset/tree/main/GIGSTestDatasetFiles/GIGS%202200%20Predefined%20Geodetic%20Data%20Objects%20test%20data/ASCII/GIGS_lib_2207_ProjectedCRS.txt">{@code GIGS_lib_2207_ProjectedCRS.txt}</a>
 *       and EPSG Dataset.</td>
 * </tr><tr>
 *   <th>Tested API:</th>
 *   <td>{@link CRSAuthorityFactory#createProjectedCRS(String)}.</td>
 * </tr><tr>
 *   <th>Expected result:</th>
 *   <td>Projected CRS definitions bundled with the software should have the same name, coordinate system
 *       (including units and axes abbreviations and axes order) and map projection as in the EPSG Dataset.
 *       CRSs missing from the software or at variance with those in the EPSG Dataset should be reported.</td>
 * </tr></table>
 *
 *
 * <h2>Usage example</h2>
 * in order to specify their factories and run the tests in a JUnit framework,
 * implementers can define a subclass in their own test suite as in the example below:
 *
 * {@snippet lang="java" :
 * public class MyTest extends Test2207 {
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
@DisplayName("Projected CRS")
public class Test2207 extends Series2000<ProjectedCRS> {
    /**
     * The EPSG code of the expected datum.
     * This field is set by all test methods before to create and verify the {@link ProjectedCRS} instance.
     */
    public int datumCode;

    /**
     * The name of the base geographic CRS.
     * This field is set by all test methods before to create and verify the {@link ProjectedCRS} instance.
     */
    public String geographicCRS;

    /**
     * {@code true} if the expected axis directions are ({@link AxisDirection#NORTH NORTH},
     * {@link AxisDirection#EAST EAST}) instead of the usual ({@code EAST}, {@code NORTH}).
     * This field is set by all test methods before to create and verify the {@link ProjectedCRS} instance.
     */
    public boolean isNorthAxisFirst;

    /**
     * {@code true} if the <var>x</var> values are increasing toward {@link AxisDirection#WEST WEST}
     * instead of {@link AxisDirection#EAST EAST}.
     * This field is set by all test methods before to create and verify the {@link ProjectedCRS} instance.
     */
    public boolean isWestOrientated;

    /**
     * {@code true} if the <var>y</var> values are increasing toward {@link AxisDirection#SOUTH SOUTH}
     * instead of {@link AxisDirection#NORTH NORTH}.
     * This field is set by all test methods before to create and verify the {@link ProjectedCRS} instance.
     */
    public boolean isSouthOrientated;

    /**
     * The CRS created by the factory, or {@code null} if not yet created or if CRS creation failed.
     *
     * @see #crsAuthorityFactory
     */
    private ProjectedCRS crs;

    /**
     * Factory to use for building {@link ProjectedCRS} instances, or {@code null} if none.
     * This is the factory used by the {@link #getIdentifiedObject()} method.
     */
    protected final CRSAuthorityFactory crsAuthorityFactory;

    /**
     * Creates a new test using the given factory. If a given factory is {@code null},
     * then the tests which depend on it will be skipped.
     *
     * @param crsFactory  factory for creating {@link ProjectedCRS} instances.
     */
    public Test2207(final CRSAuthorityFactory crsFactory) {
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
     * Returns the projected CRS instance to be tested. When this method is invoked for the first time, it creates the
     * CRS to test by invoking the {@link CRSAuthorityFactory#createProjectedCRS(String)} method with the current
     * {@link #code} value in argument. The created object is then cached and returned in all subsequent invocations
     * of this method.
     *
     * @return the projected CRS instance to test.
     * @throws FactoryException if an error occurred while creating the projected CRS instance.
     */
    @Override
    public ProjectedCRS getIdentifiedObject() throws FactoryException {
        if (crs == null) {
            assumeNotNull(crsAuthorityFactory);
            try {
                crs = crsAuthorityFactory.createProjectedCRS(String.valueOf(code));
            } catch (NoSuchIdentifierException e) {
                /*
                 * Relaxed the exception type from NoSuchAuthorityCodeException because CoordinateOperation creation
                 * will typically use MathTransformFactory under the hood, which throws NoSuchIdentifierException for
                 * non-implemented operation methods (may be identified by their name rather than EPSG code).
                 */
                unsupportedCode(ProjectedCRS.class, code);
                throw e;
            }
        }
        return crs;
    }

    /**
     * Verifies the properties of the projected CRS given by {@link #getIdentifiedObject()}.
     *
     * @param  code  authority code of the projected CRS to verify.
     * @throws FactoryException if an error occurred while creating the projected CRS instance.
     */
    private void createAndVerifyProjectedCRS(final int code) throws FactoryException {
        this.code = code;
        crs = null;                 // For forcing the fetch of a new projected CRS.

        @SuppressWarnings("LocalVariableHidesMemberVariable")
        final ProjectedCRS crs = getIdentifiedObject();
        assertNotNull(crs, "ProjectedCRS");
        validators.validate(crs);

        // Projected CRS identification.
        assertIdentifierEquals (code, crs, "ProjectedCRS");
        assertNameEquals(false, name, crs, "ProjectedCRS");
        assertAliasesEqual  (aliases, crs, "ProjectedCRS");

        // Projected CRS components.
        if (isDependencyIdentificationSupported) {
            configurationTip = Configuration.Key.isDependencyIdentificationSupported;
            assertIdentifierEquals(datumCode, crs.getDatum(), "ProjectedCRS.getDatum()");
            assertNameEquals(true, geographicCRS, crs.getBaseCRS(), "ProjectedCRS.getBaseCRS()");
            configurationTip = null;
        }

        // Projected CRS coordinate system.
        final CartesianCS cs = crs.getCoordinateSystem();
        assertNotNull(crs, "ProjectedCRS.getCoordinateSystem()");
        assertEquals(2, cs.getDimension(), "ProjectedCRS.getCoordinateSystem().getDimension()");

        // Coordinate sytem axis directions.
        final AxisDirection[] directions = new AxisDirection[2];
        directions[isNorthAxisFirst ? 1 : 0] = isWestOrientated  ? AxisDirection.WEST  : AxisDirection.EAST;
        directions[isNorthAxisFirst ? 0 : 1] = isSouthOrientated ? AxisDirection.SOUTH : AxisDirection.NORTH;
        assertAxisDirectionsEqual("ProjectedCRS.getCoordinateSystem().getAxis(*)", cs, directions);
    }

    /**
     * Tests “Abidjan 1987 / TM 5 NW” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>2165</b></li>
     *   <li>EPSG projected CRS name: <b>Abidjan 1987 / TM 5 NW</b></li>
     *   <li>Alias(es) given by EPSG: <b>Cote d'Ivoire / TM 5 NW</b>, <b>Port Bouet / TM 5 NW</b></li>
     *   <li>Geographic CRS name: <b>Abidjan 1987</b></li>
     *   <li>EPSG Usage Extent: <b>Cote d'Ivoire (Ivory Coast) - offshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Abidjan 1987 / TM 5 NW")
    public void EPSG_2165() throws FactoryException {
        name              = "Abidjan 1987 / TM 5 NW";
        aliases           = new String[] {"Cote d'Ivoire / TM 5 NW", "Port Bouet / TM 5 NW"};
        geographicCRS     = "Abidjan 1987";
        datumCode         = 6143;
        createAndVerifyProjectedCRS(2165);
    }

    /**
     * Tests “Abidjan 1987 / UTM zone 29N” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>2043</b></li>
     *   <li>EPSG projected CRS name: <b>Abidjan 1987 / UTM zone 29N</b></li>
     *   <li>Alias(es) given by EPSG: <b>Abidjan 87 / UTM 29N</b>, <b>Port Bouet / UTM zone 29N</b>, <b>Côte d'Ivoire / UTM zone 29N</b></li>
     *   <li>Geographic CRS name: <b>Abidjan 1987</b></li>
     *   <li>EPSG Usage Extent: <b>Cote d'Ivoire (Ivory Coast) - west of 6°W</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Abidjan 1987 / UTM zone 29N")
    public void EPSG_2043() throws FactoryException {
        name              = "Abidjan 1987 / UTM zone 29N";
        aliases           = new String[] {"Abidjan 87 / UTM 29N", "Port Bouet / UTM zone 29N", "Côte d'Ivoire / UTM zone 29N"};
        geographicCRS     = "Abidjan 1987";
        datumCode         = 6143;
        createAndVerifyProjectedCRS(2043);
    }

    /**
     * Tests “Abidjan 1987 / UTM zone 30N” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>2041</b></li>
     *   <li>EPSG projected CRS name: <b>Abidjan 1987 / UTM zone 30N</b></li>
     *   <li>Alias(es) given by EPSG: <b>Abidjan 87 / UTM 30N</b>, <b>Port Bouet / UTM zone 30N</b>, <b>Côte d'Ivoire / UTM zone 30N</b></li>
     *   <li>Geographic CRS name: <b>Abidjan 1987</b></li>
     *   <li>EPSG Usage Extent: <b>Cote d'Ivoire (Ivory Coast) - east of 6°W</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Abidjan 1987 / UTM zone 30N")
    public void EPSG_2041() throws FactoryException {
        name              = "Abidjan 1987 / UTM zone 30N";
        aliases           = new String[] {"Abidjan 87 / UTM 30N", "Port Bouet / UTM zone 30N", "Côte d'Ivoire / UTM zone 30N"};
        geographicCRS     = "Abidjan 1987";
        datumCode         = 6143;
        createAndVerifyProjectedCRS(2041);
    }

    /**
     * Tests “Accra / Ghana National Grid” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>2136</b></li>
     *   <li>EPSG projected CRS name: <b>Accra / Ghana National Grid</b></li>
     *   <li>Alias(es) given by EPSG: <b>Accra / Gold Coast Grid</b>, <b>Accra / Ghana Nat. Grid</b></li>
     *   <li>Geographic CRS name: <b>Accra</b></li>
     *   <li>EPSG Usage Extent: <b>Ghana - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Accra / Ghana National Grid")
    public void EPSG_2136() throws FactoryException {
        name              = "Accra / Ghana National Grid";
        aliases           = new String[] {"Accra / Gold Coast Grid", "Accra / Ghana Nat. Grid"};
        geographicCRS     = "Accra";
        datumCode         = 6168;
        createAndVerifyProjectedCRS(2136);
    }

    /**
     * Tests “Accra / TM 1 NW” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>2137</b></li>
     *   <li>EPSG projected CRS name: <b>Accra / TM 1 NW</b></li>
     *   <li>Alias(es) given by EPSG: <b>Accra / Ghana TM</b></li>
     *   <li>Geographic CRS name: <b>Accra</b></li>
     *   <li>EPSG Usage Extent: <b>Ghana - offshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Accra / TM 1 NW")
    public void EPSG_2137() throws FactoryException {
        name              = "Accra / TM 1 NW";
        aliases           = new String[] {"Accra / Ghana TM"};
        geographicCRS     = "Accra";
        datumCode         = 6168;
        createAndVerifyProjectedCRS(2137);
    }

    /**
     * Tests “AGD66 / AMG” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>20249</b>, <b>20250</b>, <b>20251</b>, <b>20252</b>, <b>20253</b>, <b>20254</b>, <b>20255</b>, <b>20256</b></li>
     *   <li>EPSG projected CRS name: <b>AGD66 / AMG</b></li>
     *   <li>Geographic CRS name: <b>AGD66</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("AGD66 / AMG")
    @SuppressWarnings("LocalVariableHidesMemberVariable")
    public void variousAGD66AMG() throws FactoryException {
        name              = "AGD66 / AMG";
        geographicCRS     = "AGD66";
        datumCode         = 6202;
        for (int code = 20249; code <= 20256; code++) {    // Loop over 8 codes
            createAndVerifyProjectedCRS(code);
        }
    }

    /**
     * Tests “AGD84 / AMG” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>20349</b>, <b>20350</b>, <b>20351</b>, <b>20352</b>, <b>20353</b>, <b>20354</b>, <b>20355</b>, <b>20356</b></li>
     *   <li>EPSG projected CRS name: <b>AGD84 / AMG</b></li>
     *   <li>Geographic CRS name: <b>AGD84</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("AGD84 / AMG")
    @SuppressWarnings("LocalVariableHidesMemberVariable")
    public void variousAGD84AMG() throws FactoryException {
        name              = "AGD84 / AMG";
        geographicCRS     = "AGD84";
        datumCode         = 6203;
        for (int code = 20349; code <= 20356; code++) {    // Loop over 8 codes
            createAndVerifyProjectedCRS(code);
        }
    }

    /**
     * Tests “Ain el Abd / Aramco Lambert” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>2318</b></li>
     *   <li>EPSG projected CRS name: <b>Ain el Abd / Aramco Lambert</b></li>
     *   <li>Alias(es) given by EPSG: <b>Ain el Abd / Aramco Lamb</b></li>
     *   <li>Geographic CRS name: <b>Ain el Abd</b></li>
     *   <li>EPSG Usage Extent: <b>Saudi Arabia - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Ain el Abd / Aramco Lambert")
    public void EPSG_2318() throws FactoryException {
        name              = "Ain el Abd / Aramco Lambert";
        aliases           = new String[] {"Ain el Abd / Aramco Lamb"};
        geographicCRS     = "Ain el Abd";
        datumCode         = 6204;
        createAndVerifyProjectedCRS(2318);
    }

    /**
     * Tests “Ain el Abd / UTM zone 36N” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>20436</b></li>
     *   <li>EPSG projected CRS name: <b>Ain el Abd / UTM zone 36N</b></li>
     *   <li>Alias(es) given by EPSG: <b>Ain el Abd / UTM 36N</b></li>
     *   <li>Geographic CRS name: <b>Ain el Abd</b></li>
     *   <li>EPSG Usage Extent: <b>Saudi Arabia - onshore west of 36°E</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Ain el Abd / UTM zone 36N")
    public void EPSG_20436() throws FactoryException {
        name              = "Ain el Abd / UTM zone 36N";
        aliases           = new String[] {"Ain el Abd / UTM 36N"};
        geographicCRS     = "Ain el Abd";
        datumCode         = 6204;
        createAndVerifyProjectedCRS(20436);
    }

    /**
     * Tests “Ain el Abd / UTM zone 37N” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>20437</b></li>
     *   <li>EPSG projected CRS name: <b>Ain el Abd / UTM zone 37N</b></li>
     *   <li>Alias(es) given by EPSG: <b>Ain el Abd / UTM 37N</b></li>
     *   <li>Geographic CRS name: <b>Ain el Abd</b></li>
     *   <li>EPSG Usage Extent: <b>Saudi Arabia - onshore 36°E to 42°E</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Ain el Abd / UTM zone 37N")
    public void EPSG_20437() throws FactoryException {
        name              = "Ain el Abd / UTM zone 37N";
        aliases           = new String[] {"Ain el Abd / UTM 37N"};
        geographicCRS     = "Ain el Abd";
        datumCode         = 6204;
        createAndVerifyProjectedCRS(20437);
    }

    /**
     * Tests “Amersfoort / RD New” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>28992</b></li>
     *   <li>EPSG projected CRS name: <b>Amersfoort / RD New</b></li>
     *   <li>Alias(es) given by EPSG: <b>Stelsel van de Rijksdriehoeksmeting</b></li>
     *   <li>Geographic CRS name: <b>Amersfoort</b></li>
     *   <li>EPSG Usage Extent: <b>Netherlands - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Amersfoort / RD New")
    public void EPSG_28992() throws FactoryException {
        name              = "Amersfoort / RD New";
        aliases           = new String[] {"Stelsel van de Rijksdriehoeksmeting"};
        geographicCRS     = "Amersfoort";
        datumCode         = 6289;
        createAndVerifyProjectedCRS(28992);
    }

    /**
     * Tests “Aratu / UTM” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>20822</b>, <b>20823</b>, <b>20824</b></li>
     *   <li>EPSG projected CRS name: <b>Aratu / UTM</b></li>
     *   <li>Geographic CRS name: <b>Aratu</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Aratu / UTM")
    public void variousAratuUTM() throws FactoryException {
        name              = "Aratu / UTM";
        geographicCRS     = "Aratu";
        datumCode         = 6208;
        createAndVerifyProjectedCRS(20822);
        createAndVerifyProjectedCRS(20823);
        createAndVerifyProjectedCRS(20824);
    }

    /**
     * Tests “Batavia / NEIEZ” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>3001</b></li>
     *   <li>EPSG projected CRS name: <b>Batavia / NEIEZ</b></li>
     *   <li>Geographic CRS name: <b>Batavia</b></li>
     *   <li>EPSG Usage Extent: <b>Indonesia - Bali; Java and western Sumatra onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Batavia / NEIEZ")
    public void EPSG_3001() throws FactoryException {
        name              = "Batavia / NEIEZ";
        geographicCRS     = "Batavia";
        datumCode         = 6211;
        createAndVerifyProjectedCRS(3001);
    }

    /**
     * Tests “Batavia / TM 109 SE” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>2308</b></li>
     *   <li>EPSG projected CRS name: <b>Batavia / TM 109 SE</b></li>
     *   <li>Alias(es) given by EPSG: <b>Genuk / TM 109 SE</b></li>
     *   <li>Geographic CRS name: <b>Batavia</b></li>
     *   <li>EPSG Usage Extent: <b>Indonesia - Java Sea - offshore northwest Java</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Batavia / TM 109 SE")
    public void EPSG_2308() throws FactoryException {
        name              = "Batavia / TM 109 SE";
        aliases           = new String[] {"Genuk / TM 109 SE"};
        geographicCRS     = "Batavia";
        datumCode         = 6211;
        createAndVerifyProjectedCRS(2308);
    }

    /**
     * Tests “Batavia / UTM zone 48S” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>21148</b></li>
     *   <li>EPSG projected CRS name: <b>Batavia / UTM zone 48S</b></li>
     *   <li>Alias(es) given by EPSG: <b>Genuk / UTM zone 48S</b></li>
     *   <li>Geographic CRS name: <b>Batavia</b></li>
     *   <li>EPSG Usage Extent: <b>Indonesia - Java and Java Sea - west of 108°E</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Batavia / UTM zone 48S")
    public void EPSG_21148() throws FactoryException {
        name              = "Batavia / UTM zone 48S";
        aliases           = new String[] {"Genuk / UTM zone 48S"};
        geographicCRS     = "Batavia";
        datumCode         = 6211;
        createAndVerifyProjectedCRS(21148);
    }

    /**
     * Tests “Batavia / UTM zone 49S” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>21149</b></li>
     *   <li>EPSG projected CRS name: <b>Batavia / UTM zone 49S</b></li>
     *   <li>Alias(es) given by EPSG: <b>Genuk / UTM zone 49S</b></li>
     *   <li>Geographic CRS name: <b>Batavia</b></li>
     *   <li>EPSG Usage Extent: <b>Indonesia - Java and Java Sea - 108°E to 114°E</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Batavia / UTM zone 49S")
    public void EPSG_21149() throws FactoryException {
        name              = "Batavia / UTM zone 49S";
        aliases           = new String[] {"Genuk / UTM zone 49S"};
        geographicCRS     = "Batavia";
        datumCode         = 6211;
        createAndVerifyProjectedCRS(21149);
    }

    /**
     * Tests “Batavia / UTM zone 50S” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>21150</b></li>
     *   <li>EPSG projected CRS name: <b>Batavia / UTM zone 50S</b></li>
     *   <li>Alias(es) given by EPSG: <b>Genuk / UTM zone 50S</b></li>
     *   <li>Geographic CRS name: <b>Batavia</b></li>
     *   <li>EPSG Usage Extent: <b>Indonesia - Java and Java Sea - east of 114°E</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Batavia / UTM zone 50S")
    public void EPSG_21150() throws FactoryException {
        name              = "Batavia / UTM zone 50S";
        aliases           = new String[] {"Genuk / UTM zone 50S"};
        geographicCRS     = "Batavia";
        datumCode         = 6211;
        createAndVerifyProjectedCRS(21150);
    }

    /**
     * Tests “Beijing 1954 / Gauss-Kruger zone 13” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>21413</b></li>
     *   <li>EPSG projected CRS name: <b>Beijing 1954 / Gauss-Kruger zone 13</b></li>
     *   <li>Alias(es) given by EPSG: <b>Beijing / GK zone 13</b>, <b>Beijing 1954 / 6-degree Gauss-Kruger zone 13</b></li>
     *   <li>Geographic CRS name: <b>Beijing 1954</b></li>
     *   <li>EPSG Usage Extent: <b>China - west of 78°E</b></li>
     * </ul>
     *
     * Remarks: Check axes order and abbreviations.
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Beijing 1954 / Gauss-Kruger zone 13")
    public void EPSG_21413() throws FactoryException {
        name              = "Beijing 1954 / Gauss-Kruger zone 13";
        aliases           = new String[] {"Beijing / GK zone 13", "Beijing 1954 / 6-degree Gauss-Kruger zone 13"};
        geographicCRS     = "Beijing 1954";
        datumCode         = 6214;
        isNorthAxisFirst  = true;
        createAndVerifyProjectedCRS(21413);
    }

    /**
     * Tests “Beijing 1954 / Gauss-Kruger zone 14” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>21414</b></li>
     *   <li>EPSG projected CRS name: <b>Beijing 1954 / Gauss-Kruger zone 14</b></li>
     *   <li>Alias(es) given by EPSG: <b>Beijing / GK zone 14</b>, <b>Beijing 1954 / 6-degree Gauss-Kruger zone 14</b></li>
     *   <li>Geographic CRS name: <b>Beijing 1954</b></li>
     *   <li>EPSG Usage Extent: <b>China - 78°E to 84°E</b></li>
     * </ul>
     *
     * Remarks: Check axes order and abbreviations.
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Beijing 1954 / Gauss-Kruger zone 14")
    public void EPSG_21414() throws FactoryException {
        name              = "Beijing 1954 / Gauss-Kruger zone 14";
        aliases           = new String[] {"Beijing / GK zone 14", "Beijing 1954 / 6-degree Gauss-Kruger zone 14"};
        geographicCRS     = "Beijing 1954";
        datumCode         = 6214;
        isNorthAxisFirst  = true;
        createAndVerifyProjectedCRS(21414);
    }

    /**
     * Tests “Beijing 1954 / Gauss-Kruger zone 15” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>21415</b></li>
     *   <li>EPSG projected CRS name: <b>Beijing 1954 / Gauss-Kruger zone 15</b></li>
     *   <li>Alias(es) given by EPSG: <b>Beijing / GK zone 15</b>, <b>Beijing 1954 / 6-degree Gauss-Kruger zone 15</b></li>
     *   <li>Geographic CRS name: <b>Beijing 1954</b></li>
     *   <li>EPSG Usage Extent: <b>China - 84°E to 90°E</b></li>
     * </ul>
     *
     * Remarks: Check axes order and abbreviations.
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Beijing 1954 / Gauss-Kruger zone 15")
    public void EPSG_21415() throws FactoryException {
        name              = "Beijing 1954 / Gauss-Kruger zone 15";
        aliases           = new String[] {"Beijing / GK zone 15", "Beijing 1954 / 6-degree Gauss-Kruger zone 15"};
        geographicCRS     = "Beijing 1954";
        datumCode         = 6214;
        isNorthAxisFirst  = true;
        createAndVerifyProjectedCRS(21415);
    }

    /**
     * Tests “Beijing 1954 / Gauss-Kruger zone 16” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>21416</b></li>
     *   <li>EPSG projected CRS name: <b>Beijing 1954 / Gauss-Kruger zone 16</b></li>
     *   <li>Alias(es) given by EPSG: <b>Beijing / GK zone 16</b>, <b>Beijing 1954 / 6-degree Gauss-Kruger zone 16</b></li>
     *   <li>Geographic CRS name: <b>Beijing 1954</b></li>
     *   <li>EPSG Usage Extent: <b>China - 90°E to 96°E</b></li>
     * </ul>
     *
     * Remarks: Check axes order and abbreviations.
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Beijing 1954 / Gauss-Kruger zone 16")
    public void EPSG_21416() throws FactoryException {
        name              = "Beijing 1954 / Gauss-Kruger zone 16";
        aliases           = new String[] {"Beijing / GK zone 16", "Beijing 1954 / 6-degree Gauss-Kruger zone 16"};
        geographicCRS     = "Beijing 1954";
        datumCode         = 6214;
        isNorthAxisFirst  = true;
        createAndVerifyProjectedCRS(21416);
    }

    /**
     * Tests “Beijing 1954 / Gauss-Kruger zone 17” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>21417</b></li>
     *   <li>EPSG projected CRS name: <b>Beijing 1954 / Gauss-Kruger zone 17</b></li>
     *   <li>Alias(es) given by EPSG: <b>Beijing / GK zone 17</b>, <b>Beijing 1954 / 6-degree Gauss-Kruger zone 17</b></li>
     *   <li>Geographic CRS name: <b>Beijing 1954</b></li>
     *   <li>EPSG Usage Extent: <b>China - 96°E to 102°E</b></li>
     * </ul>
     *
     * Remarks: Check axes order and abbreviations.
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Beijing 1954 / Gauss-Kruger zone 17")
    public void EPSG_21417() throws FactoryException {
        name              = "Beijing 1954 / Gauss-Kruger zone 17";
        aliases           = new String[] {"Beijing / GK zone 17", "Beijing 1954 / 6-degree Gauss-Kruger zone 17"};
        geographicCRS     = "Beijing 1954";
        datumCode         = 6214;
        isNorthAxisFirst  = true;
        createAndVerifyProjectedCRS(21417);
    }

    /**
     * Tests “Beijing 1954 / Gauss-Kruger zone 18” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>21418</b></li>
     *   <li>EPSG projected CRS name: <b>Beijing 1954 / Gauss-Kruger zone 18</b></li>
     *   <li>Alias(es) given by EPSG: <b>Beijing / GK zone 18</b>, <b>Beijing 1954 / 6-degree Gauss-Kruger zone 18</b></li>
     *   <li>Geographic CRS name: <b>Beijing 1954</b></li>
     *   <li>EPSG Usage Extent: <b>China - 102°E to 108°E onshore</b></li>
     * </ul>
     *
     * Remarks: Check axes order and abbreviations.
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Beijing 1954 / Gauss-Kruger zone 18")
    public void EPSG_21418() throws FactoryException {
        name              = "Beijing 1954 / Gauss-Kruger zone 18";
        aliases           = new String[] {"Beijing / GK zone 18", "Beijing 1954 / 6-degree Gauss-Kruger zone 18"};
        geographicCRS     = "Beijing 1954";
        datumCode         = 6214;
        isNorthAxisFirst  = true;
        createAndVerifyProjectedCRS(21418);
    }

    /**
     * Tests “Beijing 1954 / Gauss-Kruger zone 19” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>21419</b></li>
     *   <li>EPSG projected CRS name: <b>Beijing 1954 / Gauss-Kruger zone 19</b></li>
     *   <li>Alias(es) given by EPSG: <b>Beijing / GK zone 19</b>, <b>Beijing 1954 / 6-degree Gauss-Kruger zone 19</b></li>
     *   <li>Geographic CRS name: <b>Beijing 1954</b></li>
     *   <li>EPSG Usage Extent: <b>China - 108°E to 114°E onshore</b></li>
     * </ul>
     *
     * Remarks: Check axes order and abbreviations.
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Beijing 1954 / Gauss-Kruger zone 19")
    public void EPSG_21419() throws FactoryException {
        name              = "Beijing 1954 / Gauss-Kruger zone 19";
        aliases           = new String[] {"Beijing / GK zone 19", "Beijing 1954 / 6-degree Gauss-Kruger zone 19"};
        geographicCRS     = "Beijing 1954";
        datumCode         = 6214;
        isNorthAxisFirst  = true;
        createAndVerifyProjectedCRS(21419);
    }

    /**
     * Tests “Beijing 1954 / Gauss-Kruger zone 20” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>21420</b></li>
     *   <li>EPSG projected CRS name: <b>Beijing 1954 / Gauss-Kruger zone 20</b></li>
     *   <li>Alias(es) given by EPSG: <b>Beijing / GK zone 20</b>, <b>Beijing 1954 / 6-degree Gauss-Kruger zone 20</b></li>
     *   <li>Geographic CRS name: <b>Beijing 1954</b></li>
     *   <li>EPSG Usage Extent: <b>China - 114°E to 120°E onshore</b></li>
     * </ul>
     *
     * Remarks: Check axes order and abbreviations.
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Beijing 1954 / Gauss-Kruger zone 20")
    public void EPSG_21420() throws FactoryException {
        name              = "Beijing 1954 / Gauss-Kruger zone 20";
        aliases           = new String[] {"Beijing / GK zone 20", "Beijing 1954 / 6-degree Gauss-Kruger zone 20"};
        geographicCRS     = "Beijing 1954";
        datumCode         = 6214;
        isNorthAxisFirst  = true;
        createAndVerifyProjectedCRS(21420);
    }

    /**
     * Tests “Beijing 1954 / Gauss-Kruger zone 21” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>21421</b></li>
     *   <li>EPSG projected CRS name: <b>Beijing 1954 / Gauss-Kruger zone 21</b></li>
     *   <li>Alias(es) given by EPSG: <b>Beijing / GK zone 21</b>, <b>Beijing 1954 / 6-degree Gauss-Kruger zone 21</b></li>
     *   <li>Geographic CRS name: <b>Beijing 1954</b></li>
     *   <li>EPSG Usage Extent: <b>China - 120°E to 126°E onshore</b></li>
     * </ul>
     *
     * Remarks: Check axes order and abbreviations.
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Beijing 1954 / Gauss-Kruger zone 21")
    public void EPSG_21421() throws FactoryException {
        name              = "Beijing 1954 / Gauss-Kruger zone 21";
        aliases           = new String[] {"Beijing / GK zone 21", "Beijing 1954 / 6-degree Gauss-Kruger zone 21"};
        geographicCRS     = "Beijing 1954";
        datumCode         = 6214;
        isNorthAxisFirst  = true;
        createAndVerifyProjectedCRS(21421);
    }

    /**
     * Tests “Beijing 1954 / Gauss-Kruger zone 22” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>21422</b></li>
     *   <li>EPSG projected CRS name: <b>Beijing 1954 / Gauss-Kruger zone 22</b></li>
     *   <li>Alias(es) given by EPSG: <b>Beijing / GK zone 22</b>, <b>Beijing 1954 / 6-degree Gauss-Kruger zone 22</b></li>
     *   <li>Geographic CRS name: <b>Beijing 1954</b></li>
     *   <li>EPSG Usage Extent: <b>China - 126°E to 132°E onshore</b></li>
     * </ul>
     *
     * Remarks: Check axes order and abbreviations.
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Beijing 1954 / Gauss-Kruger zone 22")
    public void EPSG_21422() throws FactoryException {
        name              = "Beijing 1954 / Gauss-Kruger zone 22";
        aliases           = new String[] {"Beijing / GK zone 22", "Beijing 1954 / 6-degree Gauss-Kruger zone 22"};
        geographicCRS     = "Beijing 1954";
        datumCode         = 6214;
        isNorthAxisFirst  = true;
        createAndVerifyProjectedCRS(21422);
    }

    /**
     * Tests “Beijing 1954 / Gauss-Kruger zone 23” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>21423</b></li>
     *   <li>EPSG projected CRS name: <b>Beijing 1954 / Gauss-Kruger zone 23</b></li>
     *   <li>Alias(es) given by EPSG: <b>Beijing / GK zone 23</b>, <b>Beijing 1954 / 6-degree Gauss-Kruger zone 23</b></li>
     *   <li>Geographic CRS name: <b>Beijing 1954</b></li>
     *   <li>EPSG Usage Extent: <b>China - east of 132°E</b></li>
     * </ul>
     *
     * Remarks: Check axes order and abbreviations.
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Beijing 1954 / Gauss-Kruger zone 23")
    public void EPSG_21423() throws FactoryException {
        name              = "Beijing 1954 / Gauss-Kruger zone 23";
        aliases           = new String[] {"Beijing / GK zone 23", "Beijing 1954 / 6-degree Gauss-Kruger zone 23"};
        geographicCRS     = "Beijing 1954";
        datumCode         = 6214;
        isNorthAxisFirst  = true;
        createAndVerifyProjectedCRS(21423);
    }

    /**
     * Tests “Bogota 1975 / Colombia Bogota zone” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>21897</b></li>
     *   <li>EPSG projected CRS name: <b>Bogota 1975 / Colombia Bogota zone</b></li>
     *   <li>Alias(es) given by EPSG: <b>Bogota / Colombia Bogota</b></li>
     *   <li>Geographic CRS name: <b>Bogota 1975</b></li>
     *   <li>EPSG Usage Extent: <b>Colombia - 75°35'W to 72°35'W</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Bogota 1975 / Colombia Bogota zone")
    public void EPSG_21897() throws FactoryException {
        name              = "Bogota 1975 / Colombia Bogota zone";
        aliases           = new String[] {"Bogota / Colombia Bogota"};
        geographicCRS     = "Bogota 1975";
        datumCode         = 6218;
        isNorthAxisFirst  = true;
        createAndVerifyProjectedCRS(21897);
    }

    /**
     * Tests “Bogota 1975 / Colombia East Central zone” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>21898</b></li>
     *   <li>EPSG projected CRS name: <b>Bogota 1975 / Colombia East Central zone</b></li>
     *   <li>Alias(es) given by EPSG: <b>Bogota / Colombia 3E</b></li>
     *   <li>Geographic CRS name: <b>Bogota 1975</b></li>
     *   <li>EPSG Usage Extent: <b>Colombia - 72°35'W to 69°35'W</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Bogota 1975 / Colombia East Central zone")
    public void EPSG_21898() throws FactoryException {
        name              = "Bogota 1975 / Colombia East Central zone";
        aliases           = new String[] {"Bogota / Colombia 3E"};
        geographicCRS     = "Bogota 1975";
        datumCode         = 6218;
        isNorthAxisFirst  = true;
        createAndVerifyProjectedCRS(21898);
    }

    /**
     * Tests “Bogota 1975 / Colombia East zone” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>21899</b></li>
     *   <li>EPSG projected CRS name: <b>Bogota 1975 / Colombia East zone</b></li>
     *   <li>Alias(es) given by EPSG: <b>Bogota / Colombia 6E</b></li>
     *   <li>Geographic CRS name: <b>Bogota 1975</b></li>
     *   <li>EPSG Usage Extent: <b>Colombia - east of 69°35'W</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Bogota 1975 / Colombia East zone")
    public void EPSG_21899() throws FactoryException {
        name              = "Bogota 1975 / Colombia East zone";
        aliases           = new String[] {"Bogota / Colombia 6E"};
        geographicCRS     = "Bogota 1975";
        datumCode         = 6218;
        isNorthAxisFirst  = true;
        createAndVerifyProjectedCRS(21899);
    }

    /**
     * Tests “Bogota 1975 / Colombia West zone” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>21896</b></li>
     *   <li>EPSG projected CRS name: <b>Bogota 1975 / Colombia West zone</b></li>
     *   <li>Alias(es) given by EPSG: <b>Bogota / Colombia 3W</b></li>
     *   <li>Geographic CRS name: <b>Bogota 1975</b></li>
     *   <li>EPSG Usage Extent: <b>Colombia - west of 75°35'W</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Bogota 1975 / Colombia West zone")
    public void EPSG_21896() throws FactoryException {
        name              = "Bogota 1975 / Colombia West zone";
        aliases           = new String[] {"Bogota / Colombia 3W"};
        geographicCRS     = "Bogota 1975";
        datumCode         = 6218;
        isNorthAxisFirst  = true;
        createAndVerifyProjectedCRS(21896);
    }

    /**
     * Tests “Camacupa 1948 / TM 11.30 SE” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>22091</b></li>
     *   <li>EPSG projected CRS name: <b>Camacupa 1948 / TM 11.30 SE</b></li>
     *   <li>Alias(es) given by EPSG: <b>Camacupa / TM 11.30 SE</b></li>
     *   <li>Geographic CRS name: <b>Camacupa</b></li>
     *   <li>EPSG Usage Extent: <b>Angola - offshore block 15</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Camacupa 1948 / TM 11.30 SE")
    public void EPSG_22091() throws FactoryException {
        name              = "Camacupa 1948 / TM 11.30 SE";
        aliases           = new String[] {"Camacupa / TM 11.30 SE"};
        geographicCRS     = "Camacupa";
        datumCode         = 6220;
        createAndVerifyProjectedCRS(22091);
    }

    /**
     * Tests “Camacupa 1948 / TM 12 SE” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>22092</b></li>
     *   <li>EPSG projected CRS name: <b>Camacupa 1948 / TM 12 SE</b></li>
     *   <li>Alias(es) given by EPSG: <b>Camacupa / TM 12 SE</b></li>
     *   <li>Geographic CRS name: <b>Camacupa</b></li>
     *   <li>EPSG Usage Extent: <b>Angola - Angola proper - offshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Camacupa 1948 / TM 12 SE")
    public void EPSG_22092() throws FactoryException {
        name              = "Camacupa 1948 / TM 12 SE";
        aliases           = new String[] {"Camacupa / TM 12 SE"};
        geographicCRS     = "Camacupa";
        datumCode         = 6220;
        createAndVerifyProjectedCRS(22092);
    }

    /**
     * Tests “Camacupa 1948 / UTM zone 32S” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>22032</b></li>
     *   <li>EPSG projected CRS name: <b>Camacupa 1948 / UTM zone 32S</b></li>
     *   <li>Alias(es) given by EPSG: <b>Camacupa / UTM zone 32S</b></li>
     *   <li>Geographic CRS name: <b>Camacupa</b></li>
     *   <li>EPSG Usage Extent: <b>Angola - Angola proper - offshore - west of 12°E</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Camacupa 1948 / UTM zone 32S")
    public void EPSG_22032() throws FactoryException {
        name              = "Camacupa 1948 / UTM zone 32S";
        aliases           = new String[] {"Camacupa / UTM zone 32S"};
        geographicCRS     = "Camacupa";
        datumCode         = 6220;
        createAndVerifyProjectedCRS(22032);
    }

    /**
     * Tests “Camacupa 1948 / UTM zone 33S” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>22033</b></li>
     *   <li>EPSG projected CRS name: <b>Camacupa 1948 / UTM zone 33S</b></li>
     *   <li>Alias(es) given by EPSG: <b>Camacupa / UTM zone 33S</b></li>
     *   <li>Geographic CRS name: <b>Camacupa</b></li>
     *   <li>EPSG Usage Extent: <b>Angola - Angola proper - 12°E to 18°E</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Camacupa 1948 / UTM zone 33S")
    public void EPSG_22033() throws FactoryException {
        name              = "Camacupa 1948 / UTM zone 33S";
        aliases           = new String[] {"Camacupa / UTM zone 33S"};
        geographicCRS     = "Camacupa";
        datumCode         = 6220;
        createAndVerifyProjectedCRS(22033);
    }

    /**
     * Tests “Campo Inchauspe / Argentina 1” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>22191</b></li>
     *   <li>EPSG projected CRS name: <b>Campo Inchauspe / Argentina 1</b></li>
     *   <li>Alias(es) given by EPSG: <b>C Inchauspe / Argentina 1</b>, <b>Campo Inchauspe / Gauss-Kruger zone 1</b>, <b>C Inchauspe /Argentina 1</b></li>
     *   <li>Geographic CRS name: <b>Campo Inchauspe</b></li>
     *   <li>EPSG Usage Extent: <b>Argentina - west of 70.5°W</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Campo Inchauspe / Argentina 1")
    public void EPSG_22191() throws FactoryException {
        name              = "Campo Inchauspe / Argentina 1";
        aliases           = new String[] {"C Inchauspe / Argentina 1", "Campo Inchauspe / Gauss-Kruger zone 1", "C Inchauspe /Argentina 1"};
        geographicCRS     = "Campo Inchauspe";
        datumCode         = 6221;
        isNorthAxisFirst  = true;
        createAndVerifyProjectedCRS(22191);
    }

    /**
     * Tests “Campo Inchauspe / Argentina 2” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>22192</b></li>
     *   <li>EPSG projected CRS name: <b>Campo Inchauspe / Argentina 2</b></li>
     *   <li>Alias(es) given by EPSG: <b>C Inchauspe / Argentina 2</b>, <b>Campo Inchauspe / Gauss-Kruger zone 2</b>, <b>C Inchauspe /Argentina 2</b></li>
     *   <li>Geographic CRS name: <b>Campo Inchauspe</b></li>
     *   <li>EPSG Usage Extent: <b>Argentina - 70.5°W to 67.5°W mainland onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Campo Inchauspe / Argentina 2")
    public void EPSG_22192() throws FactoryException {
        name              = "Campo Inchauspe / Argentina 2";
        aliases           = new String[] {"C Inchauspe / Argentina 2", "Campo Inchauspe / Gauss-Kruger zone 2", "C Inchauspe /Argentina 2"};
        geographicCRS     = "Campo Inchauspe";
        datumCode         = 6221;
        isNorthAxisFirst  = true;
        createAndVerifyProjectedCRS(22192);
    }

    /**
     * Tests “Campo Inchauspe / Argentina 3” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>22193</b></li>
     *   <li>EPSG projected CRS name: <b>Campo Inchauspe / Argentina 3</b></li>
     *   <li>Alias(es) given by EPSG: <b>C Inchauspe / Argentina 3</b>, <b>Campo Inchauspe / Gauss-Kruger zone 3</b>, <b>C Inchauspe /Argentina 3</b></li>
     *   <li>Geographic CRS name: <b>Campo Inchauspe</b></li>
     *   <li>EPSG Usage Extent: <b>Argentina - 67.5°W to 64.5°W mainland onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Campo Inchauspe / Argentina 3")
    public void EPSG_22193() throws FactoryException {
        name              = "Campo Inchauspe / Argentina 3";
        aliases           = new String[] {"C Inchauspe / Argentina 3", "Campo Inchauspe / Gauss-Kruger zone 3", "C Inchauspe /Argentina 3"};
        geographicCRS     = "Campo Inchauspe";
        datumCode         = 6221;
        isNorthAxisFirst  = true;
        createAndVerifyProjectedCRS(22193);
    }

    /**
     * Tests “Campo Inchauspe / Argentina 4” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>22194</b></li>
     *   <li>EPSG projected CRS name: <b>Campo Inchauspe / Argentina 4</b></li>
     *   <li>Alias(es) given by EPSG: <b>C Inchauspe / Argentina 4</b>, <b>Campo Inchauspe / Gauss-Kruger zone 4</b>, <b>C Inchauspe /Argentina 4</b></li>
     *   <li>Geographic CRS name: <b>Campo Inchauspe</b></li>
     *   <li>EPSG Usage Extent: <b>Argentina - 64.5°W to 61.5°W mainland onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Campo Inchauspe / Argentina 4")
    public void EPSG_22194() throws FactoryException {
        name              = "Campo Inchauspe / Argentina 4";
        aliases           = new String[] {"C Inchauspe / Argentina 4", "Campo Inchauspe / Gauss-Kruger zone 4", "C Inchauspe /Argentina 4"};
        geographicCRS     = "Campo Inchauspe";
        datumCode         = 6221;
        isNorthAxisFirst  = true;
        createAndVerifyProjectedCRS(22194);
    }

    /**
     * Tests “Campo Inchauspe / Argentina 5” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>22195</b></li>
     *   <li>EPSG projected CRS name: <b>Campo Inchauspe / Argentina 5</b></li>
     *   <li>Alias(es) given by EPSG: <b>C Inchauspe / Argentina 5</b>, <b>Campo Inchauspe / Gauss-Kruger zone 5</b>, <b>C Inchauspe /Argentina 5</b></li>
     *   <li>Geographic CRS name: <b>Campo Inchauspe</b></li>
     *   <li>EPSG Usage Extent: <b>Argentina - 61.5°W to 58.5°W onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Campo Inchauspe / Argentina 5")
    public void EPSG_22195() throws FactoryException {
        name              = "Campo Inchauspe / Argentina 5";
        aliases           = new String[] {"C Inchauspe / Argentina 5", "Campo Inchauspe / Gauss-Kruger zone 5", "C Inchauspe /Argentina 5"};
        geographicCRS     = "Campo Inchauspe";
        datumCode         = 6221;
        isNorthAxisFirst  = true;
        createAndVerifyProjectedCRS(22195);
    }

    /**
     * Tests “Campo Inchauspe / Argentina 6” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>22196</b></li>
     *   <li>EPSG projected CRS name: <b>Campo Inchauspe / Argentina 6</b></li>
     *   <li>Alias(es) given by EPSG: <b>C Inchauspe / Argentina 6</b>, <b>Campo Inchauspe / Gauss-Kruger zone 6</b>, <b>C Inchauspe /Argentina 6</b></li>
     *   <li>Geographic CRS name: <b>Campo Inchauspe</b></li>
     *   <li>EPSG Usage Extent: <b>Argentina - 58.5°W to 55.5°W onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Campo Inchauspe / Argentina 6")
    public void EPSG_22196() throws FactoryException {
        name              = "Campo Inchauspe / Argentina 6";
        aliases           = new String[] {"C Inchauspe / Argentina 6", "Campo Inchauspe / Gauss-Kruger zone 6", "C Inchauspe /Argentina 6"};
        geographicCRS     = "Campo Inchauspe";
        datumCode         = 6221;
        isNorthAxisFirst  = true;
        createAndVerifyProjectedCRS(22196);
    }

    /**
     * Tests “Campo Inchauspe / Argentina 7” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>22197</b></li>
     *   <li>EPSG projected CRS name: <b>Campo Inchauspe / Argentina 7</b></li>
     *   <li>Alias(es) given by EPSG: <b>C Inchauspe / Argentina 7</b>, <b>Campo Inchauspe / Gauss-Kruger zone 7</b>, <b>C Inchauspe /Argentina 7</b></li>
     *   <li>Geographic CRS name: <b>Campo Inchauspe</b></li>
     *   <li>EPSG Usage Extent: <b>Argentina - east of 55.5°W onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Campo Inchauspe / Argentina 7")
    public void EPSG_22197() throws FactoryException {
        name              = "Campo Inchauspe / Argentina 7";
        aliases           = new String[] {"C Inchauspe / Argentina 7", "Campo Inchauspe / Gauss-Kruger zone 7", "C Inchauspe /Argentina 7"};
        geographicCRS     = "Campo Inchauspe";
        datumCode         = 6221;
        isNorthAxisFirst  = true;
        createAndVerifyProjectedCRS(22197);
    }

    /**
     * Tests “Campo Inchauspe / UTM zone 19S” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>2315</b></li>
     *   <li>EPSG projected CRS name: <b>Campo Inchauspe / UTM zone 19S</b></li>
     *   <li>Alias(es) given by EPSG: <b>C Inchauspe / UTM 19S</b>, <b>Campo Inchauspe /UTM 19S</b></li>
     *   <li>Geographic CRS name: <b>Campo Inchauspe</b></li>
     *   <li>EPSG Usage Extent: <b>Argentina - Tierra del Fuego offshore west of 66°W</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Campo Inchauspe / UTM zone 19S")
    public void EPSG_2315() throws FactoryException {
        name              = "Campo Inchauspe / UTM zone 19S";
        aliases           = new String[] {"C Inchauspe / UTM 19S", "Campo Inchauspe /UTM 19S"};
        geographicCRS     = "Campo Inchauspe";
        datumCode         = 6221;
        createAndVerifyProjectedCRS(2315);
    }

    /**
     * Tests “Campo Inchauspe / UTM zone 20S” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>2316</b></li>
     *   <li>EPSG projected CRS name: <b>Campo Inchauspe / UTM zone 20S</b></li>
     *   <li>Alias(es) given by EPSG: <b>C Inchauspe / UTM 20S</b>, <b>Campo Inchauspe /UTM 20S</b></li>
     *   <li>Geographic CRS name: <b>Campo Inchauspe</b></li>
     *   <li>EPSG Usage Extent: <b>Argentina - Tierra del Fuego offshore east of 66°W</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Campo Inchauspe / UTM zone 20S")
    public void EPSG_2316() throws FactoryException {
        name              = "Campo Inchauspe / UTM zone 20S";
        aliases           = new String[] {"C Inchauspe / UTM 20S", "Campo Inchauspe /UTM 20S"};
        geographicCRS     = "Campo Inchauspe";
        datumCode         = 6221;
        createAndVerifyProjectedCRS(2316);
    }

    /**
     * Tests “Carthage / Nord Tunisie” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>22391</b></li>
     *   <li>EPSG projected CRS name: <b>Carthage / Nord Tunisie</b></li>
     *   <li>Geographic CRS name: <b>Carthage</b></li>
     *   <li>EPSG Usage Extent: <b>Tunisia - north of 34°39'N</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Carthage / Nord Tunisie")
    public void EPSG_22391() throws FactoryException {
        name              = "Carthage / Nord Tunisie";
        geographicCRS     = "Carthage";
        datumCode         = 6223;
        createAndVerifyProjectedCRS(22391);
    }

    /**
     * Tests “Carthage / Sud Tunisie” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>22392</b></li>
     *   <li>EPSG projected CRS name: <b>Carthage / Sud Tunisie</b></li>
     *   <li>Geographic CRS name: <b>Carthage</b></li>
     *   <li>EPSG Usage Extent: <b>Tunisia - south of 34°39'N</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Carthage / Sud Tunisie")
    public void EPSG_22392() throws FactoryException {
        name              = "Carthage / Sud Tunisie";
        geographicCRS     = "Carthage";
        datumCode         = 6223;
        createAndVerifyProjectedCRS(22392);
    }

    /**
     * Tests “Dealul Piscului 1930 / Stereo 33” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>31600</b></li>
     *   <li>EPSG projected CRS name: <b>Dealul Piscului 1930 / Stereo 33</b></li>
     *   <li>Alias(es) given by EPSG: <b>Stereo 33</b>, <b>Dealul Piscului 1933/ Stereo 33</b>, <b>Stereo 30</b></li>
     *   <li>Geographic CRS name: <b>Dealul Piscului 1930</b></li>
     *   <li>EPSG Usage Extent: <b>Romania - onshore</b></li>
     * </ul>
     *
     * Remarks: Check axes order and abbreviations.
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Dealul Piscului 1930 / Stereo 33")
    public void EPSG_31600() throws FactoryException {
        name              = "Dealul Piscului 1930 / Stereo 33";
        aliases           = new String[] {"Stereo 33", "Dealul Piscului 1933/ Stereo 33", "Stereo 30"};
        geographicCRS     = "Dealul Piscului 1930";
        datumCode         = 6316;
        createAndVerifyProjectedCRS(31600);
    }

    /**
     * Tests “Deir ez Zor / Levant Zone” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>22700</b></li>
     *   <li>EPSG projected CRS name: <b>Deir ez Zor / Levant Zone</b></li>
     *   <li>Alias(es) given by EPSG: <b>Levant / Levant Zone</b></li>
     *   <li>Geographic CRS name: <b>Deir ez Zor</b></li>
     *   <li>EPSG Usage Extent: <b>Asia - Middle East - Lebanon and Syria onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Deir ez Zor / Levant Zone")
    public void EPSG_22700() throws FactoryException {
        name              = "Deir ez Zor / Levant Zone";
        aliases           = new String[] {"Levant / Levant Zone"};
        geographicCRS     = "Deir ez Zor";
        datumCode         = 6227;
        createAndVerifyProjectedCRS(22700);
    }

    /**
     * Tests “Deir ez Zor / Syria Lambert” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>22770</b></li>
     *   <li>EPSG projected CRS name: <b>Deir ez Zor / Syria Lambert</b></li>
     *   <li>Alias(es) given by EPSG: <b>Levant / Syria Lambert</b></li>
     *   <li>Geographic CRS name: <b>Deir ez Zor</b></li>
     *   <li>EPSG Usage Extent: <b>Asia - Middle East - Lebanon and Syria onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Deir ez Zor / Syria Lambert")
    public void EPSG_22770() throws FactoryException {
        name              = "Deir ez Zor / Syria Lambert";
        aliases           = new String[] {"Levant / Syria Lambert"};
        geographicCRS     = "Deir ez Zor";
        datumCode         = 6227;
        createAndVerifyProjectedCRS(22770);
    }

    /**
     * Tests “DGN95 / UTM zone 46N” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>23866</b></li>
     *   <li>EPSG projected CRS name: <b>DGN95 / UTM zone 46N</b></li>
     *   <li>Alias(es) given by EPSG: <b>IGD95 / UTM zone 46N</b></li>
     *   <li>Geographic CRS name: <b>DGN95</b></li>
     *   <li>EPSG Usage Extent: <b>Indonesia - west of 96°E; N hemisphere</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("DGN95 / UTM zone 46N")
    public void EPSG_23866() throws FactoryException {
        name              = "DGN95 / UTM zone 46N";
        aliases           = new String[] {"IGD95 / UTM zone 46N"};
        geographicCRS     = "DGN95";
        datumCode         = 6755;
        createAndVerifyProjectedCRS(23866);
    }

    /**
     * Tests “DGN95 / UTM zone 47N” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>23867</b></li>
     *   <li>EPSG projected CRS name: <b>DGN95 / UTM zone 47N</b></li>
     *   <li>Alias(es) given by EPSG: <b>IGD95 / UTM zone 47N</b></li>
     *   <li>Geographic CRS name: <b>DGN95</b></li>
     *   <li>EPSG Usage Extent: <b>Indonesia - 96°E to 102°E; N hemisphere</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("DGN95 / UTM zone 47N")
    public void EPSG_23867() throws FactoryException {
        name              = "DGN95 / UTM zone 47N";
        aliases           = new String[] {"IGD95 / UTM zone 47N"};
        geographicCRS     = "DGN95";
        datumCode         = 6755;
        createAndVerifyProjectedCRS(23867);
    }

    /**
     * Tests “DGN95 / UTM zone 47S” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>23877</b></li>
     *   <li>EPSG projected CRS name: <b>DGN95 / UTM zone 47S</b></li>
     *   <li>Alias(es) given by EPSG: <b>IGD95 / UTM zone 47S</b></li>
     *   <li>Geographic CRS name: <b>DGN95</b></li>
     *   <li>EPSG Usage Extent: <b>Indonesia - 96°E to 102°E; S hemisphere</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("DGN95 / UTM zone 47S")
    public void EPSG_23877() throws FactoryException {
        name              = "DGN95 / UTM zone 47S";
        aliases           = new String[] {"IGD95 / UTM zone 47S"};
        geographicCRS     = "DGN95";
        datumCode         = 6755;
        createAndVerifyProjectedCRS(23877);
    }

    /**
     * Tests “DGN95 / UTM zone 48N” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>23868</b></li>
     *   <li>EPSG projected CRS name: <b>DGN95 / UTM zone 48N</b></li>
     *   <li>Alias(es) given by EPSG: <b>IGD95 / UTM zone 48N</b></li>
     *   <li>Geographic CRS name: <b>DGN95</b></li>
     *   <li>EPSG Usage Extent: <b>Indonesia - 102°E to 108°E; N hemisphere</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("DGN95 / UTM zone 48N")
    public void EPSG_23868() throws FactoryException {
        name              = "DGN95 / UTM zone 48N";
        aliases           = new String[] {"IGD95 / UTM zone 48N"};
        geographicCRS     = "DGN95";
        datumCode         = 6755;
        createAndVerifyProjectedCRS(23868);
    }

    /**
     * Tests “DGN95 / UTM zone 48S” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>23878</b></li>
     *   <li>EPSG projected CRS name: <b>DGN95 / UTM zone 48S</b></li>
     *   <li>Alias(es) given by EPSG: <b>IGD95 / UTM zone 48S</b></li>
     *   <li>Geographic CRS name: <b>DGN95</b></li>
     *   <li>EPSG Usage Extent: <b>Indonesia - 102°E to 108°E; S hemisphere</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("DGN95 / UTM zone 48S")
    public void EPSG_23878() throws FactoryException {
        name              = "DGN95 / UTM zone 48S";
        aliases           = new String[] {"IGD95 / UTM zone 48S"};
        geographicCRS     = "DGN95";
        datumCode         = 6755;
        createAndVerifyProjectedCRS(23878);
    }

    /**
     * Tests “DGN95 / UTM zone 49N” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>23869</b></li>
     *   <li>EPSG projected CRS name: <b>DGN95 / UTM zone 49N</b></li>
     *   <li>Alias(es) given by EPSG: <b>IGD95 / UTM zone 49N</b></li>
     *   <li>Geographic CRS name: <b>DGN95</b></li>
     *   <li>EPSG Usage Extent: <b>Indonesia - 108°E to 114°E; N hemisphere</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("DGN95 / UTM zone 49N")
    public void EPSG_23869() throws FactoryException {
        name              = "DGN95 / UTM zone 49N";
        aliases           = new String[] {"IGD95 / UTM zone 49N"};
        geographicCRS     = "DGN95";
        datumCode         = 6755;
        createAndVerifyProjectedCRS(23869);
    }

    /**
     * Tests “DGN95 / UTM zone 49S” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>23879</b></li>
     *   <li>EPSG projected CRS name: <b>DGN95 / UTM zone 49S</b></li>
     *   <li>Alias(es) given by EPSG: <b>IGD95 / UTM zone 49S</b></li>
     *   <li>Geographic CRS name: <b>DGN95</b></li>
     *   <li>EPSG Usage Extent: <b>Indonesia - 108°E to 114°E; S hemisphere</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("DGN95 / UTM zone 49S")
    public void EPSG_23879() throws FactoryException {
        name              = "DGN95 / UTM zone 49S";
        aliases           = new String[] {"IGD95 / UTM zone 49S"};
        geographicCRS     = "DGN95";
        datumCode         = 6755;
        createAndVerifyProjectedCRS(23879);
    }

    /**
     * Tests “DGN95 / UTM zone 50N” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>23870</b></li>
     *   <li>EPSG projected CRS name: <b>DGN95 / UTM zone 50N</b></li>
     *   <li>Alias(es) given by EPSG: <b>IGD95 / UTM zone 50N</b></li>
     *   <li>Geographic CRS name: <b>DGN95</b></li>
     *   <li>EPSG Usage Extent: <b>Indonesia - 114°E to 120°E; N hemisphere</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("DGN95 / UTM zone 50N")
    public void EPSG_23870() throws FactoryException {
        name              = "DGN95 / UTM zone 50N";
        aliases           = new String[] {"IGD95 / UTM zone 50N"};
        geographicCRS     = "DGN95";
        datumCode         = 6755;
        createAndVerifyProjectedCRS(23870);
    }

    /**
     * Tests “DGN95 / UTM zone 50S” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>23880</b></li>
     *   <li>EPSG projected CRS name: <b>DGN95 / UTM zone 50S</b></li>
     *   <li>Alias(es) given by EPSG: <b>IGD95 / UTM zone 50S</b></li>
     *   <li>Geographic CRS name: <b>DGN95</b></li>
     *   <li>EPSG Usage Extent: <b>Indonesia - 114°E to 120°E; S hemisphere</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("DGN95 / UTM zone 50S")
    public void EPSG_23880() throws FactoryException {
        name              = "DGN95 / UTM zone 50S";
        aliases           = new String[] {"IGD95 / UTM zone 50S"};
        geographicCRS     = "DGN95";
        datumCode         = 6755;
        createAndVerifyProjectedCRS(23880);
    }

    /**
     * Tests “DGN95 / UTM zone 51N” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>23871</b></li>
     *   <li>EPSG projected CRS name: <b>DGN95 / UTM zone 51N</b></li>
     *   <li>Alias(es) given by EPSG: <b>IGD95 / UTM zone 51N</b></li>
     *   <li>Geographic CRS name: <b>DGN95</b></li>
     *   <li>EPSG Usage Extent: <b>Indonesia - 120°E to 126°E; N hemisphere</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("DGN95 / UTM zone 51N")
    public void EPSG_23871() throws FactoryException {
        name              = "DGN95 / UTM zone 51N";
        aliases           = new String[] {"IGD95 / UTM zone 51N"};
        geographicCRS     = "DGN95";
        datumCode         = 6755;
        createAndVerifyProjectedCRS(23871);
    }

    /**
     * Tests “DGN95 / UTM zone 51S” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>23881</b></li>
     *   <li>EPSG projected CRS name: <b>DGN95 / UTM zone 51S</b></li>
     *   <li>Alias(es) given by EPSG: <b>IGD95 / UTM zone 51S</b></li>
     *   <li>Geographic CRS name: <b>DGN95</b></li>
     *   <li>EPSG Usage Extent: <b>Indonesia - 120°E to 126°E; S hemisphere</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("DGN95 / UTM zone 51S")
    public void EPSG_23881() throws FactoryException {
        name              = "DGN95 / UTM zone 51S";
        aliases           = new String[] {"IGD95 / UTM zone 51S"};
        geographicCRS     = "DGN95";
        datumCode         = 6755;
        createAndVerifyProjectedCRS(23881);
    }

    /**
     * Tests “DGN95 / UTM zone 52N” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>23872</b></li>
     *   <li>EPSG projected CRS name: <b>DGN95 / UTM zone 52N</b></li>
     *   <li>Alias(es) given by EPSG: <b>IGD95 / UTM zone 52N</b></li>
     *   <li>Geographic CRS name: <b>DGN95</b></li>
     *   <li>EPSG Usage Extent: <b>Indonesia - 126°E to 132°E; N hemisphere</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("DGN95 / UTM zone 52N")
    public void EPSG_23872() throws FactoryException {
        name              = "DGN95 / UTM zone 52N";
        aliases           = new String[] {"IGD95 / UTM zone 52N"};
        geographicCRS     = "DGN95";
        datumCode         = 6755;
        createAndVerifyProjectedCRS(23872);
    }

    /**
     * Tests “DGN95 / UTM zone 52S” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>23882</b></li>
     *   <li>EPSG projected CRS name: <b>DGN95 / UTM zone 52S</b></li>
     *   <li>Alias(es) given by EPSG: <b>IGD95 / UTM zone 52S</b></li>
     *   <li>Geographic CRS name: <b>DGN95</b></li>
     *   <li>EPSG Usage Extent: <b>Indonesia - 126°E to 132°E; S hemisphere</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("DGN95 / UTM zone 52S")
    public void EPSG_23882() throws FactoryException {
        name              = "DGN95 / UTM zone 52S";
        aliases           = new String[] {"IGD95 / UTM zone 52S"};
        geographicCRS     = "DGN95";
        datumCode         = 6755;
        createAndVerifyProjectedCRS(23882);
    }

    /**
     * Tests “DGN95 / UTM zone 53S” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>23883</b></li>
     *   <li>EPSG projected CRS name: <b>DGN95 / UTM zone 53S</b></li>
     *   <li>Alias(es) given by EPSG: <b>IGD95 / UTM zone 53S</b></li>
     *   <li>Geographic CRS name: <b>DGN95</b></li>
     *   <li>EPSG Usage Extent: <b>Indonesia - 132°E to 138°E; S hemisphere</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("DGN95 / UTM zone 53S")
    public void EPSG_23883() throws FactoryException {
        name              = "DGN95 / UTM zone 53S";
        aliases           = new String[] {"IGD95 / UTM zone 53S"};
        geographicCRS     = "DGN95";
        datumCode         = 6755;
        createAndVerifyProjectedCRS(23883);
    }

    /**
     * Tests “DGN95 / UTM zone 54S” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>23884</b></li>
     *   <li>EPSG projected CRS name: <b>DGN95 / UTM zone 54S</b></li>
     *   <li>Alias(es) given by EPSG: <b>IGD95 / UTM zone 54S</b></li>
     *   <li>Geographic CRS name: <b>DGN95</b></li>
     *   <li>EPSG Usage Extent: <b>Indonesia - east of 138°E; S hemisphere</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("DGN95 / UTM zone 54S")
    public void EPSG_23884() throws FactoryException {
        name              = "DGN95 / UTM zone 54S";
        aliases           = new String[] {"IGD95 / UTM zone 54S"};
        geographicCRS     = "DGN95";
        datumCode         = 6755;
        createAndVerifyProjectedCRS(23884);
    }

    /**
     * Tests “DHDN / 3-degree Gauss-Kruger zone 2” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>31466</b></li>
     *   <li>EPSG projected CRS name: <b>DHDN / 3-degree Gauss-Kruger zone 2</b></li>
     *   <li>Alias(es) given by EPSG: <b>DHDN / Gauss-Kruger zone 2</b>, <b>DE_DHDN / GK_3</b>, <b>DHDN / 3GK zone 2</b></li>
     *   <li>Geographic CRS name: <b>DHDN</b></li>
     *   <li>EPSG Usage Extent: <b>Germany - West Germany - west of 7.5°E</b></li>
     * </ul>
     *
     * Remarks: Check axes order and abbreviations.
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("DHDN / 3-degree Gauss-Kruger zone 2")
    public void EPSG_31466() throws FactoryException {
        name              = "DHDN / 3-degree Gauss-Kruger zone 2";
        aliases           = new String[] {"DHDN / Gauss-Kruger zone 2", "DE_DHDN / GK_3", "DHDN / 3GK zone 2"};
        geographicCRS     = "DHDN";
        datumCode         = 6314;
        isNorthAxisFirst  = true;
        createAndVerifyProjectedCRS(31466);
    }

    /**
     * Tests “DHDN / 3-degree Gauss-Kruger zone 3” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>31467</b></li>
     *   <li>EPSG projected CRS name: <b>DHDN / 3-degree Gauss-Kruger zone 3</b></li>
     *   <li>Alias(es) given by EPSG: <b>DHDN / Gauss-Kruger zone 3</b>, <b>DE_DHDN / GK_3</b>, <b>DHDN / 3GK zone 3</b></li>
     *   <li>Geographic CRS name: <b>DHDN</b></li>
     *   <li>EPSG Usage Extent: <b>Germany - West-Germany - 7.5°E to 10.5°E</b></li>
     * </ul>
     *
     * Remarks: Check axes order and abbreviations.
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("DHDN / 3-degree Gauss-Kruger zone 3")
    public void EPSG_31467() throws FactoryException {
        name              = "DHDN / 3-degree Gauss-Kruger zone 3";
        aliases           = new String[] {"DHDN / Gauss-Kruger zone 3", "DE_DHDN / GK_3", "DHDN / 3GK zone 3"};
        geographicCRS     = "DHDN";
        datumCode         = 6314;
        isNorthAxisFirst  = true;
        createAndVerifyProjectedCRS(31467);
    }

    /**
     * Tests “DHDN / 3-degree Gauss-Kruger zone 4” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>31468</b></li>
     *   <li>EPSG projected CRS name: <b>DHDN / 3-degree Gauss-Kruger zone 4</b></li>
     *   <li>Alias(es) given by EPSG: <b>DHDN / Gauss-Kruger zone 4</b>, <b>DE_DHDN / GK_3</b>, <b>DHDN / 3GK zone 4</b></li>
     *   <li>Geographic CRS name: <b>DHDN</b></li>
     *   <li>EPSG Usage Extent: <b>Germany - West Germany - 10.5°E to 13.5°E</b></li>
     * </ul>
     *
     * Remarks: Check axes order and abbreviations.
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("DHDN / 3-degree Gauss-Kruger zone 4")
    public void EPSG_31468() throws FactoryException {
        name              = "DHDN / 3-degree Gauss-Kruger zone 4";
        aliases           = new String[] {"DHDN / Gauss-Kruger zone 4", "DE_DHDN / GK_3", "DHDN / 3GK zone 4"};
        geographicCRS     = "DHDN";
        datumCode         = 6314;
        isNorthAxisFirst  = true;
        createAndVerifyProjectedCRS(31468);
    }

    /**
     * Tests “DHDN / 3-degree Gauss-Kruger zone 5” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>31469</b></li>
     *   <li>EPSG projected CRS name: <b>DHDN / 3-degree Gauss-Kruger zone 5</b></li>
     *   <li>Alias(es) given by EPSG: <b>DHDN / Gauss-Kruger zone 5</b>, <b>DE_DHDN / GK_3</b>, <b>DHDN / 3GK zone 5</b></li>
     *   <li>Geographic CRS name: <b>DHDN</b></li>
     *   <li>EPSG Usage Extent: <b>Germany - West Germany - east of 13.5°E</b></li>
     * </ul>
     *
     * Remarks: Check axes order and abbreviations.
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("DHDN / 3-degree Gauss-Kruger zone 5")
    public void EPSG_31469() throws FactoryException {
        name              = "DHDN / 3-degree Gauss-Kruger zone 5";
        aliases           = new String[] {"DHDN / Gauss-Kruger zone 5", "DE_DHDN / GK_3", "DHDN / 3GK zone 5"};
        geographicCRS     = "DHDN";
        datumCode         = 6314;
        isNorthAxisFirst  = true;
        createAndVerifyProjectedCRS(31469);
    }

    /**
     * Tests “ED50(ED77) / UTM zone 38N” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>2058</b></li>
     *   <li>EPSG projected CRS name: <b>ED50(ED77) / UTM zone 38N</b></li>
     *   <li>Alias(es) given by EPSG: <b>ED50(ED77) / UTM 38N</b></li>
     *   <li>Geographic CRS name: <b>ED50(ED77)</b></li>
     *   <li>EPSG Usage Extent: <b>Iran - west of 48°E</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("ED50(ED77) / UTM zone 38N")
    public void EPSG_2058() throws FactoryException {
        name              = "ED50(ED77) / UTM zone 38N";
        aliases           = new String[] {"ED50(ED77) / UTM 38N"};
        geographicCRS     = "ED50(ED77)";
        datumCode         = 6154;
        createAndVerifyProjectedCRS(2058);
    }

    /**
     * Tests “ED50(ED77) / UTM zone 39N” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>2059</b></li>
     *   <li>EPSG projected CRS name: <b>ED50(ED77) / UTM zone 39N</b></li>
     *   <li>Alias(es) given by EPSG: <b>ED50(ED77) / UTM 39N</b></li>
     *   <li>Geographic CRS name: <b>ED50(ED77)</b></li>
     *   <li>EPSG Usage Extent: <b>Iran - 48°E to 54°E</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("ED50(ED77) / UTM zone 39N")
    public void EPSG_2059() throws FactoryException {
        name              = "ED50(ED77) / UTM zone 39N";
        aliases           = new String[] {"ED50(ED77) / UTM 39N"};
        geographicCRS     = "ED50(ED77)";
        datumCode         = 6154;
        createAndVerifyProjectedCRS(2059);
    }

    /**
     * Tests “ED50(ED77) / UTM zone 40N” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>2060</b></li>
     *   <li>EPSG projected CRS name: <b>ED50(ED77) / UTM zone 40N</b></li>
     *   <li>Alias(es) given by EPSG: <b>ED50(ED77) / UTM 40N</b></li>
     *   <li>Geographic CRS name: <b>ED50(ED77)</b></li>
     *   <li>EPSG Usage Extent: <b>Iran - 54°E to 60°E</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("ED50(ED77) / UTM zone 40N")
    public void EPSG_2060() throws FactoryException {
        name              = "ED50(ED77) / UTM zone 40N";
        aliases           = new String[] {"ED50(ED77) / UTM 40N"};
        geographicCRS     = "ED50(ED77)";
        datumCode         = 6154;
        createAndVerifyProjectedCRS(2060);
    }

    /**
     * Tests “ED50 / TM 0 N” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>23090</b></li>
     *   <li>EPSG projected CRS name: <b>ED50 / TM 0 N</b></li>
     *   <li>Geographic CRS name: <b>ED50</b></li>
     *   <li>EPSG Usage Extent: <b>UK - offshore - North Sea</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("ED50 / TM 0 N")
    public void EPSG_23090() throws FactoryException {
        name              = "ED50 / TM 0 N";
        geographicCRS     = "ED50";
        datumCode         = 6230;
        createAndVerifyProjectedCRS(23090);
    }

    /**
     * Tests “ED50 / TM 5 NE” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>23095</b></li>
     *   <li>EPSG projected CRS name: <b>ED50 / TM 5 NE</b></li>
     *   <li>Geographic CRS name: <b>ED50</b></li>
     *   <li>EPSG Usage Extent: <b>Netherlands - offshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("ED50 / TM 5 NE")
    public void EPSG_23095() throws FactoryException {
        name              = "ED50 / TM 5 NE";
        geographicCRS     = "ED50";
        datumCode         = 6230;
        createAndVerifyProjectedCRS(23095);
    }

    /**
     * Tests “ED50 / UTM” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>23028</b>, <b>23029</b>, <b>23032</b>, <b>23033</b></li>
     *   <li>EPSG projected CRS name: <b>ED50 / UTM</b></li>
     *   <li>Alias(es) given by EPSG: <b>(ES_ED50 / UTM - see alias remarks)</b></li>
     *   <li>Geographic CRS name: <b>ED50</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("ED50 / UTM")
    public void variousED50UTM() throws FactoryException {
        name              = "ED50 / UTM";
        geographicCRS     = "ED50";
        datumCode         = 6230;
        createAndVerifyProjectedCRS(23028);
        createAndVerifyProjectedCRS(23029);
        createAndVerifyProjectedCRS(23032);
        createAndVerifyProjectedCRS(23033);
    }

    /**
     * Tests “ED50 / UTM zone 30N” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>23030</b></li>
     *   <li>EPSG projected CRS name: <b>ED50 / UTM zone 30N</b></li>
     *   <li>Alias(es) given by EPSG: <b>(GI_ED50 / UTM - see alias remarks)</b></li>
     *   <li>Geographic CRS name: <b>ED50</b></li>
     *   <li>EPSG Usage Extent: <b>Europe - 6°W to 0°W and ED50 by country</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("ED50 / UTM zone 30N")
    public void EPSG_23030() throws FactoryException {
        name              = "ED50 / UTM zone 30N";
        geographicCRS     = "ED50";
        datumCode         = 6230;
        createAndVerifyProjectedCRS(23030);
    }

    /**
     * Tests “ED50 / UTM zone 31N” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>23031</b></li>
     *   <li>EPSG projected CRS name: <b>ED50 / UTM zone 31N</b></li>
     *   <li>Alias(es) given by EPSG: <b>(ES_ED50 / UTM - see alias remarks)</b></li>
     *   <li>Geographic CRS name: <b>ED50</b></li>
     *   <li>EPSG Usage Extent: <b>Europe - 0°E to 6°E and ED50 by country</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("ED50 / UTM zone 31N")
    public void EPSG_23031() throws FactoryException {
        name              = "ED50 / UTM zone 31N";
        geographicCRS     = "ED50";
        datumCode         = 6230;
        createAndVerifyProjectedCRS(23031);
    }

    /**
     * Tests “ED50 / UTM zone 34N” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>23034</b></li>
     *   <li>EPSG projected CRS name: <b>ED50 / UTM zone 34N</b></li>
     *   <li>Geographic CRS name: <b>ED50</b></li>
     *   <li>EPSG Usage Extent: <b>Europe - 18°E to 24°E and ED50 by country</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("ED50 / UTM zone 34N")
    public void EPSG_23034() throws FactoryException {
        name              = "ED50 / UTM zone 34N";
        geographicCRS     = "ED50";
        datumCode         = 6230;
        createAndVerifyProjectedCRS(23034);
    }

    /**
     * Tests “ED50 / UTM zone 35N” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>23035</b></li>
     *   <li>EPSG projected CRS name: <b>ED50 / UTM zone 35N</b></li>
     *   <li>Alias(es) given by EPSG: <b>(TR_ED50 / UTM - see alias remarks)</b></li>
     *   <li>Geographic CRS name: <b>ED50</b></li>
     *   <li>EPSG Usage Extent: <b>Europe - 24°E to 30°E and ED50 by country</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("ED50 / UTM zone 35N")
    public void EPSG_23035() throws FactoryException {
        name              = "ED50 / UTM zone 35N";
        geographicCRS     = "ED50";
        datumCode         = 6230;
        createAndVerifyProjectedCRS(23035);
    }

    /**
     * Tests “Egypt 1907 / Blue Belt” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>22991</b></li>
     *   <li>EPSG projected CRS name: <b>Egypt 1907 / Blue Belt</b></li>
     *   <li>Alias(es) given by EPSG: <b>Egypt 1907 / Green Belt</b></li>
     *   <li>Geographic CRS name: <b>Egypt 1907</b></li>
     *   <li>EPSG Usage Extent: <b>Egypt - east of 33°E onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Egypt 1907 / Blue Belt")
    public void EPSG_22991() throws FactoryException {
        name              = "Egypt 1907 / Blue Belt";
        aliases           = new String[] {"Egypt 1907 / Green Belt"};
        geographicCRS     = "Egypt 1907";
        datumCode         = 6229;
        createAndVerifyProjectedCRS(22991);
    }

    /**
     * Tests “Egypt 1907 / Extended Purple Belt” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>22994</b></li>
     *   <li>EPSG projected CRS name: <b>Egypt 1907 / Extended Purple Belt</b></li>
     *   <li>Alias(es) given by EPSG: <b>Egypt 1907 / Ext. Purple</b></li>
     *   <li>Geographic CRS name: <b>Egypt 1907</b></li>
     *   <li>EPSG Usage Extent: <b>Egypt - west of 29°E; south of 28°11'N</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Egypt 1907 / Extended Purple Belt")
    public void EPSG_22994() throws FactoryException {
        name              = "Egypt 1907 / Extended Purple Belt";
        aliases           = new String[] {"Egypt 1907 / Ext. Purple"};
        geographicCRS     = "Egypt 1907";
        datumCode         = 6229;
        createAndVerifyProjectedCRS(22994);
    }

    /**
     * Tests “Egypt 1907 / Purple Belt” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>22993</b></li>
     *   <li>EPSG projected CRS name: <b>Egypt 1907 / Purple Belt</b></li>
     *   <li>Geographic CRS name: <b>Egypt 1907</b></li>
     *   <li>EPSG Usage Extent: <b>Egypt - west of 29°E; north of 28°11'N</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Egypt 1907 / Purple Belt")
    public void EPSG_22993() throws FactoryException {
        name              = "Egypt 1907 / Purple Belt";
        geographicCRS     = "Egypt 1907";
        datumCode         = 6229;
        createAndVerifyProjectedCRS(22993);
    }

    /**
     * Tests “Egypt 1907 / Red Belt” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>22992</b></li>
     *   <li>EPSG projected CRS name: <b>Egypt 1907 / Red Belt</b></li>
     *   <li>Geographic CRS name: <b>Egypt 1907</b></li>
     *   <li>EPSG Usage Extent: <b>Egypt - 29°E to 33°E</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Egypt 1907 / Red Belt")
    public void EPSG_22992() throws FactoryException {
        name              = "Egypt 1907 / Red Belt";
        geographicCRS     = "Egypt 1907";
        datumCode         = 6229;
        createAndVerifyProjectedCRS(22992);
    }

    /**
     * Tests “Egypt Gulf of Suez S-650 TL / Red Belt” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>3355</b></li>
     *   <li>EPSG projected CRS name: <b>Egypt Gulf of Suez S-650 TL / Red Belt</b></li>
     *   <li>Alias(es) given by EPSG: <b>S-650 TL / Red Belt</b></li>
     *   <li>Geographic CRS name: <b>Egypt Gulf of Suez S-650 TL</b></li>
     *   <li>EPSG Usage Extent: <b>Egypt - Gulf of Suez</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Egypt Gulf of Suez S-650 TL / Red Belt")
    public void EPSG_3355() throws FactoryException {
        name              = "Egypt Gulf of Suez S-650 TL / Red Belt";
        aliases           = new String[] {"S-650 TL / Red Belt"};
        geographicCRS     = "Egypt Gulf of Suez S-650 TL";
        datumCode         = 6706;
        createAndVerifyProjectedCRS(3355);
    }

    /**
     * Tests “ELD79 / Libya” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>2068</b>, <b>2069</b>, <b>2070</b>, <b>2071</b>, <b>2072</b>, <b>2073</b>, <b>2074</b>, <b>2075</b>, <b>2076</b></li>
     *   <li>EPSG projected CRS name: <b>ELD79 / Libya</b></li>
     *   <li>Geographic CRS name: <b>ELD79</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("ELD79 / Libya")
    @SuppressWarnings("LocalVariableHidesMemberVariable")
    public void variousELD79Libya() throws FactoryException {
        name              = "ELD79 / Libya";
        geographicCRS     = "ELD79";
        datumCode         = 6159;
        for (int code = 2068; code <= 2076; code++) {    // Loop over 9 codes
            createAndVerifyProjectedCRS(code);
        }
    }

    /**
     * Tests “ELD79 / UTM” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>2077</b>, <b>2078</b>, <b>2079</b>, <b>2080</b></li>
     *   <li>EPSG projected CRS name: <b>ELD79 / UTM</b></li>
     *   <li>Geographic CRS name: <b>ELD79</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("ELD79 / UTM")
    @SuppressWarnings("LocalVariableHidesMemberVariable")
    public void variousELD79UTM() throws FactoryException {
        name              = "ELD79 / UTM";
        geographicCRS     = "ELD79";
        datumCode         = 6159;
        for (int code = 2077; code <= 2080; code++) {    // Loop over 4 codes
            createAndVerifyProjectedCRS(code);
        }
    }

    /**
     * Tests “ETRS89 / UTM” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>25828</b>, <b>25829</b>, <b>25830</b>, <b>25831</b>, <b>25832</b>, <b>25833</b>, <b>25834</b>, <b>25835</b></li>
     *   <li>EPSG projected CRS name: <b>ETRS89 / UTM</b></li>
     *   <li>Geographic CRS name: <b>ETRS89</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("ETRS89 / UTM")
    @SuppressWarnings("LocalVariableHidesMemberVariable")
    public void variousETRS89UTM() throws FactoryException {
        name              = "ETRS89 / UTM";
        geographicCRS     = "ETRS89";
        datumCode         = 6258;
        for (int code = 25828; code <= 25835; code++) {    // Loop over 8 codes
            createAndVerifyProjectedCRS(code);
        }
    }

    /**
     * Tests “Fahud / UTM” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>23239</b>, <b>23240</b></li>
     *   <li>EPSG projected CRS name: <b>Fahud / UTM</b></li>
     *   <li>Geographic CRS name: <b>Fahud</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Fahud / UTM")
    public void variousFahudUTM() throws FactoryException {
        name              = "Fahud / UTM";
        geographicCRS     = "Fahud";
        datumCode         = 6232;
        createAndVerifyProjectedCRS(23239);
        createAndVerifyProjectedCRS(23240);
    }

    /**
     * Tests “FD58 / Iraq zone” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>3200</b></li>
     *   <li>EPSG projected CRS name: <b>FD58 / Iraq zone</b></li>
     *   <li>Alias(es) given by EPSG: <b>Final Datum 1958 / Iraq zone</b></li>
     *   <li>Geographic CRS name: <b>FD58</b></li>
     *   <li>EPSG Usage Extent: <b>Iran - FD58</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("FD58 / Iraq zone")
    public void EPSG_3200() throws FactoryException {
        name              = "FD58 / Iraq zone";
        aliases           = new String[] {"Final Datum 1958 / Iraq zone"};
        geographicCRS     = "FD58";
        datumCode         = 6132;
        createAndVerifyProjectedCRS(3200);
    }

    /**
     * Tests “GDA94 / MGA” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>28349</b>, <b>28350</b>, <b>28351</b>, <b>28352</b>, <b>28353</b>, <b>28354</b>, <b>28355</b>, <b>28356</b></li>
     *   <li>EPSG projected CRS name: <b>GDA94 / MGA</b></li>
     *   <li>Geographic CRS name: <b>GDA94</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("GDA94 / MGA")
    @SuppressWarnings("LocalVariableHidesMemberVariable")
    public void variousGDA94MGA() throws FactoryException {
        name              = "GDA94 / MGA";
        geographicCRS     = "GDA94";
        datumCode         = 6283;
        for (int code = 28349; code <= 28356; code++) {    // Loop over 8 codes
            createAndVerifyProjectedCRS(code);
        }
    }

    /**
     * Tests “GDM2000 / East Malaysia BRSO” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>3376</b></li>
     *   <li>EPSG projected CRS name: <b>GDM2000 / East Malaysia BRSO</b></li>
     *   <li>Alias(es) given by EPSG: <b>GDM2000 / E Malaysia RSO</b></li>
     *   <li>Geographic CRS name: <b>GDM2000</b></li>
     *   <li>EPSG Usage Extent: <b>Malaysia - East Malaysia</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("GDM2000 / East Malaysia BRSO")
    public void EPSG_3376() throws FactoryException {
        name              = "GDM2000 / East Malaysia BRSO";
        aliases           = new String[] {"GDM2000 / E Malaysia RSO"};
        geographicCRS     = "GDM2000";
        datumCode         = 6742;
        createAndVerifyProjectedCRS(3376);
    }

    /**
     * Tests “GDM2000 / Peninsula RSO” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>3375</b></li>
     *   <li>EPSG projected CRS name: <b>GDM2000 / Peninsula RSO</b></li>
     *   <li>Geographic CRS name: <b>GDM2000</b></li>
     *   <li>EPSG Usage Extent: <b>Malaysia - West Malaysia</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("GDM2000 / Peninsula RSO")
    public void EPSG_3375() throws FactoryException {
        name              = "GDM2000 / Peninsula RSO";
        geographicCRS     = "GDM2000";
        datumCode         = 6742;
        createAndVerifyProjectedCRS(3375);
    }

    /**
     * Tests “HD72 / EOV” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>23700</b></li>
     *   <li>EPSG projected CRS name: <b>HD72 / EOV</b></li>
     *   <li>Geographic CRS name: <b>HD72</b></li>
     *   <li>EPSG Usage Extent: <b>Hungary</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("HD72 / EOV")
    public void EPSG_23700() throws FactoryException {
        name              = "HD72 / EOV";
        geographicCRS     = "HD72";
        datumCode         = 6237;
        createAndVerifyProjectedCRS(23700);
    }

    /**
     * Tests “IGN Astro 1960 / UTM zone 28N” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>3367</b></li>
     *   <li>EPSG projected CRS name: <b>IGN Astro 1960 / UTM zone 28N</b></li>
     *   <li>Alias(es) given by EPSG: <b>Mauritanian Mining Cadastre 1999 / UTM zone 28N</b>, <b>IGN Astro 1960 / UTM 28N</b></li>
     *   <li>Geographic CRS name: <b>IGN Astro 1960</b></li>
     *   <li>EPSG Usage Extent: <b>Mauritania - west of 12°W onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("IGN Astro 1960 / UTM zone 28N")
    public void EPSG_3367() throws FactoryException {
        name              = "IGN Astro 1960 / UTM zone 28N";
        aliases           = new String[] {"Mauritanian Mining Cadastre 1999 / UTM zone 28N", "IGN Astro 1960 / UTM 28N"};
        geographicCRS     = "IGN Astro 1960";
        datumCode         = 6700;
        createAndVerifyProjectedCRS(3367);
    }

    /**
     * Tests “IGN Astro 1960 / UTM zone 29N” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>3368</b></li>
     *   <li>EPSG projected CRS name: <b>IGN Astro 1960 / UTM zone 29N</b></li>
     *   <li>Alias(es) given by EPSG: <b>Mauritanian Mining Cadastre 1999 / UTM zone 29N</b>, <b>IGN Astro 1960 / UTM 29N</b></li>
     *   <li>Geographic CRS name: <b>IGN Astro 1960</b></li>
     *   <li>EPSG Usage Extent: <b>Mauritania - 12°W to 6°W</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("IGN Astro 1960 / UTM zone 29N")
    public void EPSG_3368() throws FactoryException {
        name              = "IGN Astro 1960 / UTM zone 29N";
        aliases           = new String[] {"Mauritanian Mining Cadastre 1999 / UTM zone 29N", "IGN Astro 1960 / UTM 29N"};
        geographicCRS     = "IGN Astro 1960";
        datumCode         = 6700;
        createAndVerifyProjectedCRS(3368);
    }

    /**
     * Tests “IGN Astro 1960 / UTM zone 30N” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>3369</b></li>
     *   <li>EPSG projected CRS name: <b>IGN Astro 1960 / UTM zone 30N</b></li>
     *   <li>Alias(es) given by EPSG: <b>Mauritanian Mining Cadastre 1999 / UTM zone 30N</b>, <b>IGN Astro 1960 / UTM 30N</b></li>
     *   <li>Geographic CRS name: <b>IGN Astro 1960</b></li>
     *   <li>EPSG Usage Extent: <b>Mauritania - east of 6°W</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("IGN Astro 1960 / UTM zone 30N")
    public void EPSG_3369() throws FactoryException {
        name              = "IGN Astro 1960 / UTM zone 30N";
        aliases           = new String[] {"Mauritanian Mining Cadastre 1999 / UTM zone 30N", "IGN Astro 1960 / UTM 30N"};
        geographicCRS     = "IGN Astro 1960";
        datumCode         = 6700;
        createAndVerifyProjectedCRS(3369);
    }

    /**
     * Tests “Indian 1954 / UTM zone 46N” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>23946</b></li>
     *   <li>EPSG projected CRS name: <b>Indian 1954 / UTM zone 46N</b></li>
     *   <li>Alias(es) given by EPSG: <b>Indian 1954 / UTM 46N</b></li>
     *   <li>Geographic CRS name: <b>Indian 1954</b></li>
     *   <li>EPSG Usage Extent: <b>Myanmar (Burma) - onshore west of 96°E</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Indian 1954 / UTM zone 46N")
    public void EPSG_23946() throws FactoryException {
        name              = "Indian 1954 / UTM zone 46N";
        aliases           = new String[] {"Indian 1954 / UTM 46N"};
        geographicCRS     = "Indian 1954";
        datumCode         = 6239;
        createAndVerifyProjectedCRS(23946);
    }

    /**
     * Tests “Indian 1954 / UTM zone 47N” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>23947</b></li>
     *   <li>EPSG projected CRS name: <b>Indian 1954 / UTM zone 47N</b></li>
     *   <li>Alias(es) given by EPSG: <b>Indian 1954 / UTM 47N</b></li>
     *   <li>Geographic CRS name: <b>Indian 1954</b></li>
     *   <li>EPSG Usage Extent: <b>Asia - Myanmar and Thailand - 96°E to 102°E</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Indian 1954 / UTM zone 47N")
    public void EPSG_23947() throws FactoryException {
        name              = "Indian 1954 / UTM zone 47N";
        aliases           = new String[] {"Indian 1954 / UTM 47N"};
        geographicCRS     = "Indian 1954";
        datumCode         = 6239;
        createAndVerifyProjectedCRS(23947);
    }

    /**
     * Tests “Indian 1954 / UTM zone 48N” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>23948</b></li>
     *   <li>EPSG projected CRS name: <b>Indian 1954 / UTM zone 48N</b></li>
     *   <li>Alias(es) given by EPSG: <b>Indian 1954 / UTM 48N</b></li>
     *   <li>Geographic CRS name: <b>Indian 1954</b></li>
     *   <li>EPSG Usage Extent: <b>Thailand - onshore east of 102°E</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Indian 1954 / UTM zone 48N")
    public void EPSG_23948() throws FactoryException {
        name              = "Indian 1954 / UTM zone 48N";
        aliases           = new String[] {"Indian 1954 / UTM 48N"};
        geographicCRS     = "Indian 1954";
        datumCode         = 6239;
        createAndVerifyProjectedCRS(23948);
    }

    /**
     * Tests “Indian 1960 / UTM zone 48N” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>3148</b></li>
     *   <li>EPSG projected CRS name: <b>Indian 1960 / UTM zone 48N</b></li>
     *   <li>Alias(es) given by EPSG: <b>Indian (DMA Reduced) / UTM zone 48N</b>, <b>Indian 1960 / UTM 48N</b></li>
     *   <li>Geographic CRS name: <b>Indian 1960</b></li>
     *   <li>EPSG Usage Extent: <b>Asia - Cambodia and Vietnam - west of 108°E</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Indian 1960 / UTM zone 48N")
    public void EPSG_3148() throws FactoryException {
        name              = "Indian 1960 / UTM zone 48N";
        aliases           = new String[] {"Indian (DMA Reduced) / UTM zone 48N", "Indian 1960 / UTM 48N"};
        geographicCRS     = "Indian 1960";
        datumCode         = 6131;
        createAndVerifyProjectedCRS(3148);
    }

    /**
     * Tests “Indian 1960 / UTM zone 49N” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>3149</b></li>
     *   <li>EPSG projected CRS name: <b>Indian 1960 / UTM zone 49N</b></li>
     *   <li>Alias(es) given by EPSG: <b>Indian (DMA Reduced) / UTM zone 49N</b>, <b>Indian 1960 / UTM 49N</b></li>
     *   <li>Geographic CRS name: <b>Indian 1960</b></li>
     *   <li>EPSG Usage Extent: <b>Vietnam - east of 108°E onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Indian 1960 / UTM zone 49N")
    public void EPSG_3149() throws FactoryException {
        name              = "Indian 1960 / UTM zone 49N";
        aliases           = new String[] {"Indian (DMA Reduced) / UTM zone 49N", "Indian 1960 / UTM 49N"};
        geographicCRS     = "Indian 1960";
        datumCode         = 6131;
        createAndVerifyProjectedCRS(3149);
    }

    /**
     * Tests “Indian 1975 / UTM zone 47N” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>24047</b></li>
     *   <li>EPSG projected CRS name: <b>Indian 1975 / UTM zone 47N</b></li>
     *   <li>Alias(es) given by EPSG: <b>Indian 1975 / UTM 47N</b></li>
     *   <li>Geographic CRS name: <b>Indian 1975</b></li>
     *   <li>EPSG Usage Extent: <b>Thailand - onshore and GoT 96°E to102°E</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Indian 1975 / UTM zone 47N")
    public void EPSG_24047() throws FactoryException {
        name              = "Indian 1975 / UTM zone 47N";
        aliases           = new String[] {"Indian 1975 / UTM 47N"};
        geographicCRS     = "Indian 1975";
        datumCode         = 6240;
        createAndVerifyProjectedCRS(24047);
    }

    /**
     * Tests “Indian 1975 / UTM zone 48N” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>24048</b></li>
     *   <li>EPSG projected CRS name: <b>Indian 1975 / UTM zone 48N</b></li>
     *   <li>Alias(es) given by EPSG: <b>Indian 1975 / UTM 48N</b></li>
     *   <li>Geographic CRS name: <b>Indian 1975</b></li>
     *   <li>EPSG Usage Extent: <b>Thailand - east of 102°E</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Indian 1975 / UTM zone 48N")
    public void EPSG_24048() throws FactoryException {
        name              = "Indian 1975 / UTM zone 48N";
        aliases           = new String[] {"Indian 1975 / UTM 48N"};
        geographicCRS     = "Indian 1975";
        datumCode         = 6240;
        createAndVerifyProjectedCRS(24048);
    }

    /**
     * Tests “Kalianpur 1937 / India zone IIb” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>24375</b></li>
     *   <li>EPSG projected CRS name: <b>Kalianpur 1937 / India zone IIb</b></li>
     *   <li>Alias(es) given by EPSG: <b>Kalianpur 37 / India IIb</b></li>
     *   <li>Geographic CRS name: <b>Kalianpur 1937</b></li>
     *   <li>EPSG Usage Extent: <b>Bangladesh - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Kalianpur 1937 / India zone IIb")
    public void EPSG_24375() throws FactoryException {
        name              = "Kalianpur 1937 / India zone IIb";
        aliases           = new String[] {"Kalianpur 37 / India IIb"};
        geographicCRS     = "Kalianpur 1937";
        datumCode         = 6144;
        createAndVerifyProjectedCRS(24375);
    }

    /**
     * Tests “Kalianpur 1937 / UTM zone 45N” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>24305</b></li>
     *   <li>EPSG projected CRS name: <b>Kalianpur 1937 / UTM zone 45N</b></li>
     *   <li>Alias(es) given by EPSG: <b>Kalianpur 37 / UTM 45N</b></li>
     *   <li>Geographic CRS name: <b>Kalianpur 1937</b></li>
     *   <li>EPSG Usage Extent: <b>Bangladesh - onshore west of 90°E</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Kalianpur 1937 / UTM zone 45N")
    public void EPSG_24305() throws FactoryException {
        name              = "Kalianpur 1937 / UTM zone 45N";
        aliases           = new String[] {"Kalianpur 37 / UTM 45N"};
        geographicCRS     = "Kalianpur 1937";
        datumCode         = 6144;
        createAndVerifyProjectedCRS(24305);
    }

    /**
     * Tests “Kalianpur 1937 / UTM zone 46N” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>24306</b></li>
     *   <li>EPSG projected CRS name: <b>Kalianpur 1937 / UTM zone 46N</b></li>
     *   <li>Alias(es) given by EPSG: <b>Kalianpur 37 / UTM 46N</b></li>
     *   <li>Geographic CRS name: <b>Kalianpur 1937</b></li>
     *   <li>EPSG Usage Extent: <b>Bangladesh - onshore east of 90°E</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Kalianpur 1937 / UTM zone 46N")
    public void EPSG_24306() throws FactoryException {
        name              = "Kalianpur 1937 / UTM zone 46N";
        aliases           = new String[] {"Kalianpur 37 / UTM 46N"};
        geographicCRS     = "Kalianpur 1937";
        datumCode         = 6144;
        createAndVerifyProjectedCRS(24306);
    }

    /**
     * Tests “Kalianpur 1962 / India zone I” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>24376</b></li>
     *   <li>EPSG projected CRS name: <b>Kalianpur 1962 / India zone I</b></li>
     *   <li>Alias(es) given by EPSG: <b>Kalianpur 62 / India I</b></li>
     *   <li>Geographic CRS name: <b>Kalianpur 1962</b></li>
     *   <li>EPSG Usage Extent: <b>Pakistan - 28°N to 35°35'N</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Kalianpur 1962 / India zone I")
    public void EPSG_24376() throws FactoryException {
        name              = "Kalianpur 1962 / India zone I";
        aliases           = new String[] {"Kalianpur 62 / India I"};
        geographicCRS     = "Kalianpur 1962";
        datumCode         = 6145;
        createAndVerifyProjectedCRS(24376);
    }

    /**
     * Tests “Kalianpur 1962 / India zone IIa” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>24377</b></li>
     *   <li>EPSG projected CRS name: <b>Kalianpur 1962 / India zone IIa</b></li>
     *   <li>Alias(es) given by EPSG: <b>Kalianpur 62 / India IIa</b></li>
     *   <li>Geographic CRS name: <b>Kalianpur 1962</b></li>
     *   <li>EPSG Usage Extent: <b>Pakistan - onshore south of 28°N</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Kalianpur 1962 / India zone IIa")
    public void EPSG_24377() throws FactoryException {
        name              = "Kalianpur 1962 / India zone IIa";
        aliases           = new String[] {"Kalianpur 62 / India IIa"};
        geographicCRS     = "Kalianpur 1962";
        datumCode         = 6145;
        createAndVerifyProjectedCRS(24377);
    }

    /**
     * Tests “Kalianpur 1962 / UTM zone 42N” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>24312</b></li>
     *   <li>EPSG projected CRS name: <b>Kalianpur 1962 / UTM zone 42N</b></li>
     *   <li>Alias(es) given by EPSG: <b>Kalianpur 62 / UTM 42N</b></li>
     *   <li>Geographic CRS name: <b>Kalianpur 1962</b></li>
     *   <li>EPSG Usage Extent: <b>Pakistan - onshore 66°E to 72°E</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Kalianpur 1962 / UTM zone 42N")
    public void EPSG_24312() throws FactoryException {
        name              = "Kalianpur 1962 / UTM zone 42N";
        aliases           = new String[] {"Kalianpur 62 / UTM 42N"};
        geographicCRS     = "Kalianpur 1962";
        datumCode         = 6145;
        createAndVerifyProjectedCRS(24312);
    }

    /**
     * Tests “Kalianpur 1962 / UTM zone 43N” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>24313</b></li>
     *   <li>EPSG projected CRS name: <b>Kalianpur 1962 / UTM zone 43N</b></li>
     *   <li>Alias(es) given by EPSG: <b>Kalianpur 62 / UTM 43N</b></li>
     *   <li>Geographic CRS name: <b>Kalianpur 1962</b></li>
     *   <li>EPSG Usage Extent: <b>Pakistan - east of 72°E</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Kalianpur 1962 / UTM zone 43N")
    public void EPSG_24313() throws FactoryException {
        name              = "Kalianpur 1962 / UTM zone 43N";
        aliases           = new String[] {"Kalianpur 62 / UTM 43N"};
        geographicCRS     = "Kalianpur 1962";
        datumCode         = 6145;
        createAndVerifyProjectedCRS(24313);
    }

    /**
     * Tests “Kalianpur 1975 / India zone IIa” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>24379</b></li>
     *   <li>EPSG projected CRS name: <b>Kalianpur 1975 / India zone IIa</b></li>
     *   <li>Alias(es) given by EPSG: <b>Kalianpur 75 / India IIa</b></li>
     *   <li>Geographic CRS name: <b>Kalianpur 1975</b></li>
     *   <li>EPSG Usage Extent: <b>India - onshore 21°N to 28°N and west of 82°E</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Kalianpur 1975 / India zone IIa")
    public void EPSG_24379() throws FactoryException {
        name              = "Kalianpur 1975 / India zone IIa";
        aliases           = new String[] {"Kalianpur 75 / India IIa"};
        geographicCRS     = "Kalianpur 1975";
        datumCode         = 6146;
        createAndVerifyProjectedCRS(24379);
    }

    /**
     * Tests “Kalianpur 1975 / India zone IIb” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>24380</b></li>
     *   <li>EPSG projected CRS name: <b>Kalianpur 1975 / India zone IIb</b></li>
     *   <li>Alias(es) given by EPSG: <b>Kalianpur 75 / India IIb</b></li>
     *   <li>Geographic CRS name: <b>Kalianpur 1975</b></li>
     *   <li>EPSG Usage Extent: <b>India - onshore north of 21°N and east of 82°E</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Kalianpur 1975 / India zone IIb")
    public void EPSG_24380() throws FactoryException {
        name              = "Kalianpur 1975 / India zone IIb";
        aliases           = new String[] {"Kalianpur 75 / India IIb"};
        geographicCRS     = "Kalianpur 1975";
        datumCode         = 6146;
        createAndVerifyProjectedCRS(24380);
    }

    /**
     * Tests “Kalianpur 1975 / UTM zone 42N” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>24342</b></li>
     *   <li>EPSG projected CRS name: <b>Kalianpur 1975 / UTM zone 42N</b></li>
     *   <li>Alias(es) given by EPSG: <b>Kalianpur 75 / UTM 42N</b></li>
     *   <li>Geographic CRS name: <b>Kalianpur 1975</b></li>
     *   <li>EPSG Usage Extent: <b>India - onshore west of 72°E</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Kalianpur 1975 / UTM zone 42N")
    public void EPSG_24342() throws FactoryException {
        name              = "Kalianpur 1975 / UTM zone 42N";
        aliases           = new String[] {"Kalianpur 75 / UTM 42N"};
        geographicCRS     = "Kalianpur 1975";
        datumCode         = 6146;
        createAndVerifyProjectedCRS(24342);
    }

    /**
     * Tests “Kalianpur 1975 / UTM zone 43N” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>24343</b></li>
     *   <li>EPSG projected CRS name: <b>Kalianpur 1975 / UTM zone 43N</b></li>
     *   <li>Alias(es) given by EPSG: <b>Kalianpur 75 / UTM 43N</b></li>
     *   <li>Geographic CRS name: <b>Kalianpur 1975</b></li>
     *   <li>EPSG Usage Extent: <b>India - mainland 72°E to 78°E</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Kalianpur 1975 / UTM zone 43N")
    public void EPSG_24343() throws FactoryException {
        name              = "Kalianpur 1975 / UTM zone 43N";
        aliases           = new String[] {"Kalianpur 75 / UTM 43N"};
        geographicCRS     = "Kalianpur 1975";
        datumCode         = 6146;
        createAndVerifyProjectedCRS(24343);
    }

    /**
     * Tests “Kertau (RSO) / RSO Malaya (ch)” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>3167</b></li>
     *   <li>EPSG projected CRS name: <b>Kertau (RSO) / RSO Malaya (ch)</b></li>
     *   <li>Alias(es) given by EPSG: <b>Kertau (RSO) / RSO (ch)</b></li>
     *   <li>Geographic CRS name: <b>Kertau (RSO)</b></li>
     *   <li>EPSG Usage Extent: <b>Malaysia - West Malaysia - onshore</b></li>
     * </ul>
     *
     * Remarks: Check UoM.
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Kertau (RSO) / RSO Malaya (ch)")
    public void EPSG_3167() throws FactoryException {
        name              = "Kertau (RSO) / RSO Malaya (ch)";
        aliases           = new String[] {"Kertau (RSO) / RSO (ch)"};
        geographicCRS     = "Kertau (RSO)";
        datumCode         = 6751;
        createAndVerifyProjectedCRS(3167);
    }

    /**
     * Tests “Kertau (RSO) / RSO Malaya (m)” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>3168</b></li>
     *   <li>EPSG projected CRS name: <b>Kertau (RSO) / RSO Malaya (m)</b></li>
     *   <li>Alias(es) given by EPSG: <b>Kertau (RSO) / RSO (m)</b></li>
     *   <li>Geographic CRS name: <b>Kertau (RSO)</b></li>
     *   <li>EPSG Usage Extent: <b>Malaysia - West Malaysia - onshore</b></li>
     * </ul>
     *
     * Remarks: Check UoM.
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Kertau (RSO) / RSO Malaya (m)")
    public void EPSG_3168() throws FactoryException {
        name              = "Kertau (RSO) / RSO Malaya (m)";
        aliases           = new String[] {"Kertau (RSO) / RSO (m)"};
        geographicCRS     = "Kertau (RSO)";
        datumCode         = 6751;
        createAndVerifyProjectedCRS(3168);
    }

    /**
     * Tests “Kertau 1968 / UTM zone 47N” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>24547</b></li>
     *   <li>EPSG projected CRS name: <b>Kertau 1968 / UTM zone 47N</b></li>
     *   <li>Alias(es) given by EPSG: <b>MRT68 / UTM zone 47N</b></li>
     *   <li>Geographic CRS name: <b>Kertau 1968</b></li>
     *   <li>EPSG Usage Extent: <b>Malaysia - West Malaysia - onshore west of 102°E</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Kertau 1968 / UTM zone 47N")
    public void EPSG_24547() throws FactoryException {
        name              = "Kertau 1968 / UTM zone 47N";
        aliases           = new String[] {"MRT68 / UTM zone 47N"};
        geographicCRS     = "Kertau 1968";
        datumCode         = 6245;
        createAndVerifyProjectedCRS(24547);
    }

    /**
     * Tests “Kertau 1968 / UTM zone 48N” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>24548</b></li>
     *   <li>EPSG projected CRS name: <b>Kertau 1968 / UTM zone 48N</b></li>
     *   <li>Alias(es) given by EPSG: <b>MRT68 / UTM zone 48N</b></li>
     *   <li>Geographic CRS name: <b>Kertau 1968</b></li>
     *   <li>EPSG Usage Extent: <b>Malaysia - West Malaysia - east of 102°E</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Kertau 1968 / UTM zone 48N")
    public void EPSG_24548() throws FactoryException {
        name              = "Kertau 1968 / UTM zone 48N";
        aliases           = new String[] {"MRT68 / UTM zone 48N"};
        geographicCRS     = "Kertau 1968";
        datumCode         = 6245;
        createAndVerifyProjectedCRS(24548);
    }

    /**
     * Tests “KOC Lambert” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>24600</b></li>
     *   <li>EPSG projected CRS name: <b>KOC Lambert</b></li>
     *   <li>Geographic CRS name: <b>KOC</b></li>
     *   <li>EPSG Usage Extent: <b>Kuwait - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("KOC Lambert")
    public void EPSG_24600() throws FactoryException {
        name              = "KOC Lambert";
        geographicCRS     = "KOC";
        datumCode         = 6246;
        createAndVerifyProjectedCRS(24600);
    }

    /**
     * Tests “LGD2006 / Libya TM zone 10” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>3195</b></li>
     *   <li>EPSG projected CRS name: <b>LGD2006 / Libya TM zone 10</b></li>
     *   <li>Alias(es) given by EPSG: <b>LGD2006 / Libya TM 10</b></li>
     *   <li>Geographic CRS name: <b>LGD2006</b></li>
     *   <li>EPSG Usage Extent: <b>Libya - 18°E to 20°E onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("LGD2006 / Libya TM zone 10")
    public void EPSG_3195() throws FactoryException {
        name              = "LGD2006 / Libya TM zone 10";
        aliases           = new String[] {"LGD2006 / Libya TM 10"};
        geographicCRS     = "LGD2006";
        datumCode         = 6754;
        createAndVerifyProjectedCRS(3195);
    }

    /**
     * Tests “LGD2006 / Libya TM zone 11” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>3196</b></li>
     *   <li>EPSG projected CRS name: <b>LGD2006 / Libya TM zone 11</b></li>
     *   <li>Alias(es) given by EPSG: <b>LGD2006 / Libya TM 11</b></li>
     *   <li>Geographic CRS name: <b>LGD2006</b></li>
     *   <li>EPSG Usage Extent: <b>Libya - 20°E to 22°E onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("LGD2006 / Libya TM zone 11")
    public void EPSG_3196() throws FactoryException {
        name              = "LGD2006 / Libya TM zone 11";
        aliases           = new String[] {"LGD2006 / Libya TM 11"};
        geographicCRS     = "LGD2006";
        datumCode         = 6754;
        createAndVerifyProjectedCRS(3196);
    }

    /**
     * Tests “LGD2006 / Libya TM zone 12” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>3197</b></li>
     *   <li>EPSG projected CRS name: <b>LGD2006 / Libya TM zone 12</b></li>
     *   <li>Alias(es) given by EPSG: <b>LGD2006 / Libya TM 12</b></li>
     *   <li>Geographic CRS name: <b>LGD2006</b></li>
     *   <li>EPSG Usage Extent: <b>Libya - 22°E to 24°E onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("LGD2006 / Libya TM zone 12")
    public void EPSG_3197() throws FactoryException {
        name              = "LGD2006 / Libya TM zone 12";
        aliases           = new String[] {"LGD2006 / Libya TM 12"};
        geographicCRS     = "LGD2006";
        datumCode         = 6754;
        createAndVerifyProjectedCRS(3197);
    }

    /**
     * Tests “LGD2006 / Libya TM zone 13” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>3198</b></li>
     *   <li>EPSG projected CRS name: <b>LGD2006 / Libya TM zone 13</b></li>
     *   <li>Alias(es) given by EPSG: <b>LGD2006 / Libya TM 13</b></li>
     *   <li>Geographic CRS name: <b>LGD2006</b></li>
     *   <li>EPSG Usage Extent: <b>Libya - east of 24°E onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("LGD2006 / Libya TM zone 13")
    public void EPSG_3198() throws FactoryException {
        name              = "LGD2006 / Libya TM zone 13";
        aliases           = new String[] {"LGD2006 / Libya TM 13"};
        geographicCRS     = "LGD2006";
        datumCode         = 6754;
        createAndVerifyProjectedCRS(3198);
    }

    /**
     * Tests “LGD2006 / Libya TM zone 5” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>3190</b></li>
     *   <li>EPSG projected CRS name: <b>LGD2006 / Libya TM zone 5</b></li>
     *   <li>Alias(es) given by EPSG: <b>LGD2006 / Libya TM 5</b></li>
     *   <li>Geographic CRS name: <b>LGD2006</b></li>
     *   <li>EPSG Usage Extent: <b>Libya - west of 10°E</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("LGD2006 / Libya TM zone 5")
    public void EPSG_3190() throws FactoryException {
        name              = "LGD2006 / Libya TM zone 5";
        aliases           = new String[] {"LGD2006 / Libya TM 5"};
        geographicCRS     = "LGD2006";
        datumCode         = 6754;
        createAndVerifyProjectedCRS(3190);
    }

    /**
     * Tests “LGD2006 / Libya TM zone 6” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>3191</b></li>
     *   <li>EPSG projected CRS name: <b>LGD2006 / Libya TM zone 6</b></li>
     *   <li>Alias(es) given by EPSG: <b>LGD2006 / Libya TM 6</b></li>
     *   <li>Geographic CRS name: <b>LGD2006</b></li>
     *   <li>EPSG Usage Extent: <b>Libya - 10°E to 12°E onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("LGD2006 / Libya TM zone 6")
    public void EPSG_3191() throws FactoryException {
        name              = "LGD2006 / Libya TM zone 6";
        aliases           = new String[] {"LGD2006 / Libya TM 6"};
        geographicCRS     = "LGD2006";
        datumCode         = 6754;
        createAndVerifyProjectedCRS(3191);
    }

    /**
     * Tests “LGD2006 / Libya TM zone 7” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>3192</b></li>
     *   <li>EPSG projected CRS name: <b>LGD2006 / Libya TM zone 7</b></li>
     *   <li>Alias(es) given by EPSG: <b>LGD2006 / Libya TM 7</b></li>
     *   <li>Geographic CRS name: <b>LGD2006</b></li>
     *   <li>EPSG Usage Extent: <b>Libya - 12°E to 14°E onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("LGD2006 / Libya TM zone 7")
    public void EPSG_3192() throws FactoryException {
        name              = "LGD2006 / Libya TM zone 7";
        aliases           = new String[] {"LGD2006 / Libya TM 7"};
        geographicCRS     = "LGD2006";
        datumCode         = 6754;
        createAndVerifyProjectedCRS(3192);
    }

    /**
     * Tests “LGD2006 / Libya TM zone 8” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>3193</b></li>
     *   <li>EPSG projected CRS name: <b>LGD2006 / Libya TM zone 8</b></li>
     *   <li>Alias(es) given by EPSG: <b>LGD2006 / Libya TM 8</b></li>
     *   <li>Geographic CRS name: <b>LGD2006</b></li>
     *   <li>EPSG Usage Extent: <b>Libya - 14°E to 16°E onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("LGD2006 / Libya TM zone 8")
    public void EPSG_3193() throws FactoryException {
        name              = "LGD2006 / Libya TM zone 8";
        aliases           = new String[] {"LGD2006 / Libya TM 8"};
        geographicCRS     = "LGD2006";
        datumCode         = 6754;
        createAndVerifyProjectedCRS(3193);
    }

    /**
     * Tests “LGD2006 / Libya TM zone 9” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>3194</b></li>
     *   <li>EPSG projected CRS name: <b>LGD2006 / Libya TM zone 9</b></li>
     *   <li>Alias(es) given by EPSG: <b>LGD2006 / Libya TM 9</b></li>
     *   <li>Geographic CRS name: <b>LGD2006</b></li>
     *   <li>EPSG Usage Extent: <b>Libya - 16°E to 18°E onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("LGD2006 / Libya TM zone 9")
    public void EPSG_3194() throws FactoryException {
        name              = "LGD2006 / Libya TM zone 9";
        aliases           = new String[] {"LGD2006 / Libya TM 9"};
        geographicCRS     = "LGD2006";
        datumCode         = 6754;
        createAndVerifyProjectedCRS(3194);
    }

    /**
     * Tests “LGD2006 / UTM” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>3201</b>, <b>3202</b>, <b>3203</b></li>
     *   <li>EPSG projected CRS name: <b>LGD2006 / UTM</b></li>
     *   <li>Geographic CRS name: <b>LGD2006</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("LGD2006 / UTM")
    public void variousLGD2006UTM() throws FactoryException {
        name              = "LGD2006 / UTM";
        geographicCRS     = "LGD2006";
        datumCode         = 6754;
        createAndVerifyProjectedCRS(3201);
        createAndVerifyProjectedCRS(3202);
        createAndVerifyProjectedCRS(3203);
    }

    /**
     * Tests “LGD2006 / UTM zone 32N” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>3199</b></li>
     *   <li>EPSG projected CRS name: <b>LGD2006 / UTM zone 32N</b></li>
     *   <li>Geographic CRS name: <b>LGD2006</b></li>
     *   <li>EPSG Usage Extent: <b>Libya - west of 12°E</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("LGD2006 / UTM zone 32N")
    public void EPSG_3199() throws FactoryException {
        name              = "LGD2006 / UTM zone 32N";
        geographicCRS     = "LGD2006";
        datumCode         = 6754;
        createAndVerifyProjectedCRS(3199);
    }

    /**
     * Tests “Luzon 1911 / Philippines zone I” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>25391</b></li>
     *   <li>EPSG projected CRS name: <b>Luzon 1911 / Philippines zone I</b></li>
     *   <li>Alias(es) given by EPSG: <b>Luzon / Philippines I</b></li>
     *   <li>Geographic CRS name: <b>Luzon 1911</b></li>
     *   <li>EPSG Usage Extent: <b>Philippines - zone I onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Luzon 1911 / Philippines zone I")
    public void EPSG_25391() throws FactoryException {
        name              = "Luzon 1911 / Philippines zone I";
        aliases           = new String[] {"Luzon / Philippines I"};
        geographicCRS     = "Luzon 1911";
        datumCode         = 6253;
        createAndVerifyProjectedCRS(25391);
    }

    /**
     * Tests “Luzon 1911 / Philippines zone II” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>25392</b></li>
     *   <li>EPSG projected CRS name: <b>Luzon 1911 / Philippines zone II</b></li>
     *   <li>Alias(es) given by EPSG: <b>Luzon / Philippines II</b></li>
     *   <li>Geographic CRS name: <b>Luzon 1911</b></li>
     *   <li>EPSG Usage Extent: <b>Philippines - zone II onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Luzon 1911 / Philippines zone II")
    public void EPSG_25392() throws FactoryException {
        name              = "Luzon 1911 / Philippines zone II";
        aliases           = new String[] {"Luzon / Philippines II"};
        geographicCRS     = "Luzon 1911";
        datumCode         = 6253;
        createAndVerifyProjectedCRS(25392);
    }

    /**
     * Tests “Luzon 1911 / Philippines zone III” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>25393</b></li>
     *   <li>EPSG projected CRS name: <b>Luzon 1911 / Philippines zone III</b></li>
     *   <li>Alias(es) given by EPSG: <b>Luzon / Philippines III</b></li>
     *   <li>Geographic CRS name: <b>Luzon 1911</b></li>
     *   <li>EPSG Usage Extent: <b>Philippines - zone III onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Luzon 1911 / Philippines zone III")
    public void EPSG_25393() throws FactoryException {
        name              = "Luzon 1911 / Philippines zone III";
        aliases           = new String[] {"Luzon / Philippines III"};
        geographicCRS     = "Luzon 1911";
        datumCode         = 6253;
        createAndVerifyProjectedCRS(25393);
    }

    /**
     * Tests “Luzon 1911 / Philippines zone IV” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>25394</b></li>
     *   <li>EPSG projected CRS name: <b>Luzon 1911 / Philippines zone IV</b></li>
     *   <li>Alias(es) given by EPSG: <b>Luzon / Philippines IV</b></li>
     *   <li>Geographic CRS name: <b>Luzon 1911</b></li>
     *   <li>EPSG Usage Extent: <b>Philippines - zone IV onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Luzon 1911 / Philippines zone IV")
    public void EPSG_25394() throws FactoryException {
        name              = "Luzon 1911 / Philippines zone IV";
        aliases           = new String[] {"Luzon / Philippines IV"};
        geographicCRS     = "Luzon 1911";
        datumCode         = 6253;
        createAndVerifyProjectedCRS(25394);
    }

    /**
     * Tests “Luzon 1911 / Philippines zone V” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>25395</b></li>
     *   <li>EPSG projected CRS name: <b>Luzon 1911 / Philippines zone V</b></li>
     *   <li>Alias(es) given by EPSG: <b>Luzon / Philippines V</b></li>
     *   <li>Geographic CRS name: <b>Luzon 1911</b></li>
     *   <li>EPSG Usage Extent: <b>Philippines - zone V onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Luzon 1911 / Philippines zone V")
    public void EPSG_25395() throws FactoryException {
        name              = "Luzon 1911 / Philippines zone V";
        aliases           = new String[] {"Luzon / Philippines V"};
        geographicCRS     = "Luzon 1911";
        datumCode         = 6253;
        createAndVerifyProjectedCRS(25395);
    }

    /**
     * Tests “MAGNA-SIRGAS / Colombia Bogota zone” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>3116</b></li>
     *   <li>EPSG projected CRS name: <b>MAGNA-SIRGAS / Colombia Bogota zone</b></li>
     *   <li>Alias(es) given by EPSG: <b>MAGNA-SIRGAS / Col Bog</b></li>
     *   <li>Geographic CRS name: <b>MAGNA-SIRGAS</b></li>
     *   <li>EPSG Usage Extent: <b>Colombia - 75°35'W to 72°35'W</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("MAGNA-SIRGAS / Colombia Bogota zone")
    public void EPSG_3116() throws FactoryException {
        name              = "MAGNA-SIRGAS / Colombia Bogota zone";
        aliases           = new String[] {"MAGNA-SIRGAS / Col Bog"};
        geographicCRS     = "MAGNA-SIRGAS";
        datumCode         = 6686;
        isNorthAxisFirst  = true;
        createAndVerifyProjectedCRS(3116);
    }

    /**
     * Tests “MAGNA-SIRGAS / Colombia East Central zone” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>3117</b></li>
     *   <li>EPSG projected CRS name: <b>MAGNA-SIRGAS / Colombia East Central zone</b></li>
     *   <li>Alias(es) given by EPSG: <b>MAGNA-SIRGAS / Col EC</b></li>
     *   <li>Geographic CRS name: <b>MAGNA-SIRGAS</b></li>
     *   <li>EPSG Usage Extent: <b>Colombia - 72°35'W to 69°35'W</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("MAGNA-SIRGAS / Colombia East Central zone")
    public void EPSG_3117() throws FactoryException {
        name              = "MAGNA-SIRGAS / Colombia East Central zone";
        aliases           = new String[] {"MAGNA-SIRGAS / Col EC"};
        geographicCRS     = "MAGNA-SIRGAS";
        datumCode         = 6686;
        isNorthAxisFirst  = true;
        createAndVerifyProjectedCRS(3117);
    }

    /**
     * Tests “MAGNA-SIRGAS / Colombia East zone” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>3118</b></li>
     *   <li>EPSG projected CRS name: <b>MAGNA-SIRGAS / Colombia East zone</b></li>
     *   <li>Alias(es) given by EPSG: <b>MAGNA-SIRGAS / Col E</b></li>
     *   <li>Geographic CRS name: <b>MAGNA-SIRGAS</b></li>
     *   <li>EPSG Usage Extent: <b>Colombia - east of 69°35'W</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("MAGNA-SIRGAS / Colombia East zone")
    public void EPSG_3118() throws FactoryException {
        name              = "MAGNA-SIRGAS / Colombia East zone";
        aliases           = new String[] {"MAGNA-SIRGAS / Col E"};
        geographicCRS     = "MAGNA-SIRGAS";
        datumCode         = 6686;
        isNorthAxisFirst  = true;
        createAndVerifyProjectedCRS(3118);
    }

    /**
     * Tests “MAGNA-SIRGAS / Colombia Far West zone” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>3114</b></li>
     *   <li>EPSG projected CRS name: <b>MAGNA-SIRGAS / Colombia Far West zone</b></li>
     *   <li>Alias(es) given by EPSG: <b>MAGNA-SIRGAS / Col FW</b></li>
     *   <li>Geographic CRS name: <b>MAGNA-SIRGAS</b></li>
     *   <li>EPSG Usage Extent: <b>Colombia - west of 78°35'W</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("MAGNA-SIRGAS / Colombia Far West zone")
    public void EPSG_3114() throws FactoryException {
        name              = "MAGNA-SIRGAS / Colombia Far West zone";
        aliases           = new String[] {"MAGNA-SIRGAS / Col FW"};
        geographicCRS     = "MAGNA-SIRGAS";
        datumCode         = 6686;
        isNorthAxisFirst  = true;
        createAndVerifyProjectedCRS(3114);
    }

    /**
     * Tests “MAGNA-SIRGAS / Colombia West zone” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>3115</b></li>
     *   <li>EPSG projected CRS name: <b>MAGNA-SIRGAS / Colombia West zone</b></li>
     *   <li>Alias(es) given by EPSG: <b>MAGNA-SIRGAS / Col W</b></li>
     *   <li>Geographic CRS name: <b>MAGNA-SIRGAS</b></li>
     *   <li>EPSG Usage Extent: <b>Colombia - 78°35'W to 75°35'W</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("MAGNA-SIRGAS / Colombia West zone")
    public void EPSG_3115() throws FactoryException {
        name              = "MAGNA-SIRGAS / Colombia West zone";
        aliases           = new String[] {"MAGNA-SIRGAS / Col W"};
        geographicCRS     = "MAGNA-SIRGAS";
        datumCode         = 6686;
        isNorthAxisFirst  = true;
        createAndVerifyProjectedCRS(3115);
    }

    /**
     * Tests “Malongo 1987 / UTM zone 32S” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>25932</b></li>
     *   <li>EPSG projected CRS name: <b>Malongo 1987 / UTM zone 32S</b></li>
     *   <li>Alias(es) given by EPSG: <b>Malongo 1987 / UTM 32S</b>, <b>Mhast / UTM zone 32S</b></li>
     *   <li>Geographic CRS name: <b>Malongo 1987</b></li>
     *   <li>EPSG Usage Extent: <b>Africa - Angola (Cabinda) and DR Congo (Zaire) - offshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Malongo 1987 / UTM zone 32S")
    public void EPSG_25932() throws FactoryException {
        name              = "Malongo 1987 / UTM zone 32S";
        aliases           = new String[] {"Malongo 1987 / UTM 32S", "Mhast / UTM zone 32S"};
        geographicCRS     = "Malongo 1987";
        datumCode         = 6259;
        createAndVerifyProjectedCRS(25932);
    }

    /**
     * Tests “Manoca 1962 / UTM zone 32N” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>2215</b></li>
     *   <li>EPSG projected CRS name: <b>Manoca 1962 / UTM zone 32N</b></li>
     *   <li>Alias(es) given by EPSG: <b>Manoca 1962 / UTM 32N</b></li>
     *   <li>Geographic CRS name: <b>Manoca 1962</b></li>
     *   <li>EPSG Usage Extent: <b>Cameroon - coastal area</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Manoca 1962 / UTM zone 32N")
    public void EPSG_2215() throws FactoryException {
        name              = "Manoca 1962 / UTM zone 32N";
        aliases           = new String[] {"Manoca 1962 / UTM 32N"};
        geographicCRS     = "Manoca 1962";
        datumCode         = 6193;
        createAndVerifyProjectedCRS(2215);
    }

    /**
     * Tests “Mauritania 1999 / UTM zone 28N” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>3343</b></li>
     *   <li>EPSG projected CRS name: <b>Mauritania 1999 / UTM zone 28N</b></li>
     *   <li>Alias(es) given by EPSG: <b>Mauritania 99 / UTM 28N</b></li>
     *   <li>Geographic CRS name: <b>Mauritania 1999</b></li>
     *   <li>EPSG Usage Extent: <b>Mauritania - 18°W to 12°W</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Mauritania 1999 / UTM zone 28N")
    public void EPSG_3343() throws FactoryException {
        name              = "Mauritania 1999 / UTM zone 28N";
        aliases           = new String[] {"Mauritania 99 / UTM 28N"};
        geographicCRS     = "Mauritania 1999";
        datumCode         = 6702;
        createAndVerifyProjectedCRS(3343);
    }

    /**
     * Tests “Mauritania 1999 / UTM zone 29N” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>3344</b></li>
     *   <li>EPSG projected CRS name: <b>Mauritania 1999 / UTM zone 29N</b></li>
     *   <li>Alias(es) given by EPSG: <b>Mauritania 99 / UTM 29N</b></li>
     *   <li>Geographic CRS name: <b>Mauritania 1999</b></li>
     *   <li>EPSG Usage Extent: <b>Mauritania - 12°W to 6°W</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Mauritania 1999 / UTM zone 29N")
    public void EPSG_3344() throws FactoryException {
        name              = "Mauritania 1999 / UTM zone 29N";
        aliases           = new String[] {"Mauritania 99 / UTM 29N"};
        geographicCRS     = "Mauritania 1999";
        datumCode         = 6702;
        createAndVerifyProjectedCRS(3344);
    }

    /**
     * Tests “Mauritania 1999 / UTM zone 30N” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>3345</b></li>
     *   <li>EPSG projected CRS name: <b>Mauritania 1999 / UTM zone 30N</b></li>
     *   <li>Alias(es) given by EPSG: <b>Mauritania 99 / UTM 30N</b></li>
     *   <li>Geographic CRS name: <b>Mauritania 1999</b></li>
     *   <li>EPSG Usage Extent: <b>Mauritania - east of 6°W</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Mauritania 1999 / UTM zone 30N")
    public void EPSG_3345() throws FactoryException {
        name              = "Mauritania 1999 / UTM zone 30N";
        aliases           = new String[] {"Mauritania 99 / UTM 30N"};
        geographicCRS     = "Mauritania 1999";
        datumCode         = 6702;
        createAndVerifyProjectedCRS(3345);
    }

    /**
     * Tests “Mhast (offshore) / UTM zone 32S” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>3354</b></li>
     *   <li>EPSG projected CRS name: <b>Mhast (offshore) / UTM zone 32S</b></li>
     *   <li>Alias(es) given by EPSG: <b>Mhast / UTM zone 32S</b>, <b>Mhast offshore / UTM 32S</b></li>
     *   <li>Geographic CRS name: <b>Mhast (offshore)</b></li>
     *   <li>EPSG Usage Extent: <b>Africa - Angola (Cabinda) and DR Congo (Zaire) - offshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Mhast (offshore) / UTM zone 32S")
    public void EPSG_3354() throws FactoryException {
        name              = "Mhast (offshore) / UTM zone 32S";
        aliases           = new String[] {"Mhast / UTM zone 32S", "Mhast offshore / UTM 32S"};
        geographicCRS     = "Mhast (offshore)";
        datumCode         = 6705;
        createAndVerifyProjectedCRS(3354);
    }

    /**
     * Tests “Mhast (onshore) / UTM zone 32S” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>3353</b></li>
     *   <li>EPSG projected CRS name: <b>Mhast (onshore) / UTM zone 32S</b></li>
     *   <li>Alias(es) given by EPSG: <b>Mhast / UTM zone 32S</b>, <b>Mhast onshore / UTM 32S</b></li>
     *   <li>Geographic CRS name: <b>Mhast (onshore)</b></li>
     *   <li>EPSG Usage Extent: <b>Africa - Angola (Cabinda) and DR Congo (Zaire) - coastal</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Mhast (onshore) / UTM zone 32S")
    public void EPSG_3353() throws FactoryException {
        name              = "Mhast (onshore) / UTM zone 32S";
        aliases           = new String[] {"Mhast / UTM zone 32S", "Mhast onshore / UTM 32S"};
        geographicCRS     = "Mhast (onshore)";
        datumCode         = 6704;
        createAndVerifyProjectedCRS(3353);
    }

    /**
     * Tests “Minna / Nigeria East Belt” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>26393</b></li>
     *   <li>EPSG projected CRS name: <b>Minna / Nigeria East Belt</b></li>
     *   <li>Alias(es) given by EPSG: <b>Minna / Nigeria East</b></li>
     *   <li>Geographic CRS name: <b>Minna</b></li>
     *   <li>EPSG Usage Extent: <b>Nigeria - east of 10.5°E</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Minna / Nigeria East Belt")
    public void EPSG_26393() throws FactoryException {
        name              = "Minna / Nigeria East Belt";
        aliases           = new String[] {"Minna / Nigeria East"};
        geographicCRS     = "Minna";
        datumCode         = 6263;
        createAndVerifyProjectedCRS(26393);
    }

    /**
     * Tests “Minna / Nigeria Mid Belt” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>26392</b></li>
     *   <li>EPSG projected CRS name: <b>Minna / Nigeria Mid Belt</b></li>
     *   <li>Geographic CRS name: <b>Minna</b></li>
     *   <li>EPSG Usage Extent: <b>Nigeria - 6.5°E to 10.5°E</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Minna / Nigeria Mid Belt")
    public void EPSG_26392() throws FactoryException {
        name              = "Minna / Nigeria Mid Belt";
        geographicCRS     = "Minna";
        datumCode         = 6263;
        createAndVerifyProjectedCRS(26392);
    }

    /**
     * Tests “Minna / Nigeria West Belt” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>26391</b></li>
     *   <li>EPSG projected CRS name: <b>Minna / Nigeria West Belt</b></li>
     *   <li>Alias(es) given by EPSG: <b>Minna / Nigeria West</b></li>
     *   <li>Geographic CRS name: <b>Minna</b></li>
     *   <li>EPSG Usage Extent: <b>Nigeria - west of 6.5°E</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Minna / Nigeria West Belt")
    public void EPSG_26391() throws FactoryException {
        name              = "Minna / Nigeria West Belt";
        aliases           = new String[] {"Minna / Nigeria West"};
        geographicCRS     = "Minna";
        datumCode         = 6263;
        createAndVerifyProjectedCRS(26391);
    }

    /**
     * Tests “Minna / UTM” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>26331</b>, <b>26332</b></li>
     *   <li>EPSG projected CRS name: <b>Minna / UTM</b></li>
     *   <li>Geographic CRS name: <b>Minna</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Minna / UTM")
    public void variousMinnaUTM() throws FactoryException {
        name              = "Minna / UTM";
        geographicCRS     = "Minna";
        datumCode         = 6263;
        createAndVerifyProjectedCRS(26331);
        createAndVerifyProjectedCRS(26332);
    }

    /**
     * Tests “Monte Mario / Italy zone 1” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>3003</b></li>
     *   <li>EPSG projected CRS name: <b>Monte Mario / Italy zone 1</b></li>
     *   <li>Alias(es) given by EPSG: <b>Rome 1940 / Italy zone 1</b></li>
     *   <li>Geographic CRS name: <b>Monte Mario</b></li>
     *   <li>EPSG Usage Extent: <b>Italy - west of 12°E</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Monte Mario / Italy zone 1")
    public void EPSG_3003() throws FactoryException {
        name              = "Monte Mario / Italy zone 1";
        aliases           = new String[] {"Rome 1940 / Italy zone 1"};
        geographicCRS     = "Monte Mario";
        datumCode         = 6265;
        createAndVerifyProjectedCRS(3003);
    }

    /**
     * Tests “Monte Mario / Italy zone 2” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>3004</b></li>
     *   <li>EPSG projected CRS name: <b>Monte Mario / Italy zone 2</b></li>
     *   <li>Alias(es) given by EPSG: <b>Rome 1940 / Italy zone 2</b></li>
     *   <li>Geographic CRS name: <b>Monte Mario</b></li>
     *   <li>EPSG Usage Extent: <b>Italy - east of 12°E</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Monte Mario / Italy zone 2")
    public void EPSG_3004() throws FactoryException {
        name              = "Monte Mario / Italy zone 2";
        aliases           = new String[] {"Rome 1940 / Italy zone 2"};
        geographicCRS     = "Monte Mario";
        datumCode         = 6265;
        createAndVerifyProjectedCRS(3004);
    }

    /**
     * Tests “NAD27 / BLM 14N (ftUS)” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>32064</b></li>
     *   <li>EPSG projected CRS name: <b>NAD27 / BLM 14N (ftUS)</b></li>
     *   <li>Alias(es) given by EPSG: <b>NAD27 / UTM zone 14N (ftUS)</b></li>
     *   <li>Geographic CRS name: <b>NAD27</b></li>
     *   <li>EPSG Usage Extent: <b>USA - 102°W to 96°W and GoM OCS</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("NAD27 / BLM 14N (ftUS)")
    public void EPSG_32064() throws FactoryException {
        name              = "NAD27 / BLM 14N (ftUS)";
        aliases           = new String[] {"NAD27 / UTM zone 14N (ftUS)"};
        geographicCRS     = "NAD27";
        datumCode         = 6267;
        createAndVerifyProjectedCRS(32064);
    }

    /**
     * Tests “NAD27 / BLM 15N (ftUS)” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>32065</b></li>
     *   <li>EPSG projected CRS name: <b>NAD27 / BLM 15N (ftUS)</b></li>
     *   <li>Alias(es) given by EPSG: <b>NAD27 / UTM zone 15N (ftUS)</b></li>
     *   <li>Geographic CRS name: <b>NAD27</b></li>
     *   <li>EPSG Usage Extent: <b>USA - 96°W to 90°W and GoM OCS</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("NAD27 / BLM 15N (ftUS)")
    public void EPSG_32065() throws FactoryException {
        name              = "NAD27 / BLM 15N (ftUS)";
        aliases           = new String[] {"NAD27 / UTM zone 15N (ftUS)"};
        geographicCRS     = "NAD27";
        datumCode         = 6267;
        createAndVerifyProjectedCRS(32065);
    }

    /**
     * Tests “NAD27 / BLM 16N (ftUS)” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>32066</b></li>
     *   <li>EPSG projected CRS name: <b>NAD27 / BLM 16N (ftUS)</b></li>
     *   <li>Alias(es) given by EPSG: <b>NAD27 / UTM zone 16N (ftUS)</b></li>
     *   <li>Geographic CRS name: <b>NAD27</b></li>
     *   <li>EPSG Usage Extent: <b>USA - 90°W to 84°W and GoM OCS</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("NAD27 / BLM 16N (ftUS)")
    public void EPSG_32066() throws FactoryException {
        name              = "NAD27 / BLM 16N (ftUS)";
        aliases           = new String[] {"NAD27 / UTM zone 16N (ftUS)"};
        geographicCRS     = "NAD27";
        datumCode         = 6267;
        createAndVerifyProjectedCRS(32066);
    }

    /**
     * Tests “NAD27 / New Mexico East” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>32012</b></li>
     *   <li>EPSG projected CRS name: <b>NAD27 / New Mexico East</b></li>
     *   <li>Geographic CRS name: <b>NAD27</b></li>
     *   <li>EPSG Usage Extent: <b>USA - New Mexico - SPCS - E</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("NAD27 / New Mexico East")
    public void EPSG_32012() throws FactoryException {
        name              = "NAD27 / New Mexico East";
        geographicCRS     = "NAD27";
        datumCode         = 6267;
        createAndVerifyProjectedCRS(32012);
    }

    /**
     * Tests “NAD27 / Texas South Central” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>32040</b></li>
     *   <li>EPSG projected CRS name: <b>NAD27 / Texas South Central</b></li>
     *   <li>Alias(es) given by EPSG: <b>NAD27 / Texas South Cen.</b></li>
     *   <li>Geographic CRS name: <b>NAD27</b></li>
     *   <li>EPSG Usage Extent: <b>USA - Texas - SPCS27 - SC</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("NAD27 / Texas South Central")
    public void EPSG_32040() throws FactoryException {
        name              = "NAD27 / Texas South Central";
        aliases           = new String[] {"NAD27 / Texas South Cen."};
        geographicCRS     = "NAD27";
        datumCode         = 6267;
        createAndVerifyProjectedCRS(32040);
    }

    /**
     * Tests “NAD27 / UTM” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>26711</b>, <b>26712</b></li>
     *   <li>EPSG projected CRS name: <b>NAD27 / UTM</b></li>
     *   <li>Geographic CRS name: <b>NAD27</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("NAD27 / UTM")
    public void variousNAD27UTM() throws FactoryException {
        name              = "NAD27 / UTM";
        geographicCRS     = "NAD27";
        datumCode         = 6267;
        createAndVerifyProjectedCRS(26711);
        createAndVerifyProjectedCRS(26712);
    }

    /**
     * Tests “NAD83(CSRS) / UTM zone 11N” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>2955</b></li>
     *   <li>EPSG projected CRS name: <b>NAD83(CSRS) / UTM zone 11N</b></li>
     *   <li>Alias(es) given by EPSG: <b>NAD83(CSRS) / UTM 11N</b></li>
     *   <li>Geographic CRS name: <b>NAD83(CSRS)</b></li>
     *   <li>EPSG Usage Extent: <b>Canada - 120°W to 114°W</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("NAD83(CSRS) / UTM zone 11N")
    public void EPSG_2955() throws FactoryException {
        name              = "NAD83(CSRS) / UTM zone 11N";
        aliases           = new String[] {"NAD83(CSRS) / UTM 11N"};
        geographicCRS     = "NAD83(CSRS)";
        datumCode         = 6140;
        createAndVerifyProjectedCRS(2955);
    }

    /**
     * Tests “NAD83(CSRS) / UTM zone 12N” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>2956</b></li>
     *   <li>EPSG projected CRS name: <b>NAD83(CSRS) / UTM zone 12N</b></li>
     *   <li>Alias(es) given by EPSG: <b>NAD83(CSRS) / UTM 12N</b></li>
     *   <li>Geographic CRS name: <b>NAD83(CSRS)</b></li>
     *   <li>EPSG Usage Extent: <b>Canada - 114°W to 108°W</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("NAD83(CSRS) / UTM zone 12N")
    public void EPSG_2956() throws FactoryException {
        name              = "NAD83(CSRS) / UTM zone 12N";
        aliases           = new String[] {"NAD83(CSRS) / UTM 12N"};
        geographicCRS     = "NAD83(CSRS)";
        datumCode         = 6140;
        createAndVerifyProjectedCRS(2956);
    }

    /**
     * Tests “NAD83(HARN) / Michigan South” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>2809</b></li>
     *   <li>EPSG projected CRS name: <b>NAD83(HARN) / Michigan South</b></li>
     *   <li>Alias(es) given by EPSG: <b>NAD83(HPGN) / Michigan South</b>, <b>NAD83(HARN) / Michigan South (m)</b>, <b>NAD83(HARN) / MI S (m)</b></li>
     *   <li>Geographic CRS name: <b>NAD83(HARN)</b></li>
     *   <li>EPSG Usage Extent: <b>USA - Michigan - SPCS - S</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("NAD83(HARN) / Michigan South")
    public void EPSG_2809() throws FactoryException {
        name              = "NAD83(HARN) / Michigan South";
        aliases           = new String[] {"NAD83(HPGN) / Michigan South", "NAD83(HARN) / Michigan South (m)", "NAD83(HARN) / MI S (m)"};
        geographicCRS     = "NAD83(HARN)";
        datumCode         = 6152;
        createAndVerifyProjectedCRS(2809);
    }

    /**
     * Tests “NAD83(HARN) / Michigan South (ft)” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>2898</b></li>
     *   <li>EPSG projected CRS name: <b>NAD83(HARN) / Michigan South (ft)</b></li>
     *   <li>Alias(es) given by EPSG: <b>NAD83(HPGN) / Michigan South (ft)</b>, <b>NAD83(HARN) / MI S (ft)</b></li>
     *   <li>Geographic CRS name: <b>NAD83(HARN)</b></li>
     *   <li>EPSG Usage Extent: <b>USA - Michigan - SPCS - S</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("NAD83(HARN) / Michigan South (ft)")
    public void EPSG_2898() throws FactoryException {
        name              = "NAD83(HARN) / Michigan South (ft)";
        aliases           = new String[] {"NAD83(HPGN) / Michigan South (ft)", "NAD83(HARN) / MI S (ft)"};
        geographicCRS     = "NAD83(HARN)";
        datumCode         = 6152;
        createAndVerifyProjectedCRS(2898);
    }

    /**
     * Tests “NAD83(HARN) / Texas North Central” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>2845</b></li>
     *   <li>EPSG projected CRS name: <b>NAD83(HARN) / Texas North Central</b></li>
     *   <li>Alias(es) given by EPSG: <b>NAD83(HPGN) / Texas North Central</b>, <b>NAD83(HARN) / Texas North Central (m)</b>, <b>NAD83(HARN) / TX NC (m)</b></li>
     *   <li>Geographic CRS name: <b>NAD83(HARN)</b></li>
     *   <li>EPSG Usage Extent: <b>USA - Texas - SPCS - NC</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("NAD83(HARN) / Texas North Central")
    public void EPSG_2845() throws FactoryException {
        name              = "NAD83(HARN) / Texas North Central";
        aliases           = new String[] {"NAD83(HPGN) / Texas North Central", "NAD83(HARN) / Texas North Central (m)", "NAD83(HARN) / TX NC (m)"};
        geographicCRS     = "NAD83(HARN)";
        datumCode         = 6152;
        createAndVerifyProjectedCRS(2845);
    }

    /**
     * Tests “NAD83(HARN) / Texas North Central (ftUS)” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>2917</b></li>
     *   <li>EPSG projected CRS name: <b>NAD83(HARN) / Texas North Central (ftUS)</b></li>
     *   <li>Alias(es) given by EPSG: <b>NAD83(HPGN) / Texas North Central (ftUS)</b>, <b>NAD83(HARN) / TX NC (ftUS)</b></li>
     *   <li>Geographic CRS name: <b>NAD83(HARN)</b></li>
     *   <li>EPSG Usage Extent: <b>USA - Texas - SPCS - NC</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("NAD83(HARN) / Texas North Central (ftUS)")
    public void EPSG_2917() throws FactoryException {
        name              = "NAD83(HARN) / Texas North Central (ftUS)";
        aliases           = new String[] {"NAD83(HPGN) / Texas North Central (ftUS)", "NAD83(HARN) / TX NC (ftUS)"};
        geographicCRS     = "NAD83(HARN)";
        datumCode         = 6152;
        createAndVerifyProjectedCRS(2917);
    }

    /**
     * Tests “NAD83 / Michigan South” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>26990</b></li>
     *   <li>EPSG projected CRS name: <b>NAD83 / Michigan South</b></li>
     *   <li>Alias(es) given by EPSG: <b>NAD83 / Michigan South (m)</b></li>
     *   <li>Geographic CRS name: <b>NAD83</b></li>
     *   <li>EPSG Usage Extent: <b>USA - Michigan - SPCS - S</b></li>
     * </ul>
     *
     * Remarks: Check UoM.
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("NAD83 / Michigan South")
    public void EPSG_26990() throws FactoryException {
        name              = "NAD83 / Michigan South";
        aliases           = new String[] {"NAD83 / Michigan South (m)"};
        geographicCRS     = "NAD83";
        datumCode         = 6269;
        createAndVerifyProjectedCRS(26990);
    }

    /**
     * Tests “NAD83 / Michigan South (ft)” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>2253</b></li>
     *   <li>EPSG projected CRS name: <b>NAD83 / Michigan South (ft)</b></li>
     *   <li>Alias(es) given by EPSG: <b>NAD83 / Michigan S (ft)</b></li>
     *   <li>Geographic CRS name: <b>NAD83</b></li>
     *   <li>EPSG Usage Extent: <b>USA - Michigan - SPCS - S</b></li>
     * </ul>
     *
     * Remarks: Check UoM.
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("NAD83 / Michigan South (ft)")
    public void EPSG_2253() throws FactoryException {
        name              = "NAD83 / Michigan South (ft)";
        aliases           = new String[] {"NAD83 / Michigan S (ft)"};
        geographicCRS     = "NAD83";
        datumCode         = 6269;
        createAndVerifyProjectedCRS(2253);
    }

    /**
     * Tests “NAD83 / Texas South Central” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>32140</b></li>
     *   <li>EPSG projected CRS name: <b>NAD83 / Texas South Central</b></li>
     *   <li>Alias(es) given by EPSG: <b>NAD83 / Texas South Cen.</b>, <b>NAD83 / Texas South Central (m)</b></li>
     *   <li>Geographic CRS name: <b>NAD83</b></li>
     *   <li>EPSG Usage Extent: <b>USA - Texas - SPCS83 - SC</b></li>
     * </ul>
     *
     * Remarks: Check UoM.
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("NAD83 / Texas South Central")
    public void EPSG_32140() throws FactoryException {
        name              = "NAD83 / Texas South Central";
        aliases           = new String[] {"NAD83 / Texas South Cen.", "NAD83 / Texas South Central (m)"};
        geographicCRS     = "NAD83";
        datumCode         = 6269;
        createAndVerifyProjectedCRS(32140);
    }

    /**
     * Tests “NAD83 / Texas South Central (ftUS)” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>2278</b></li>
     *   <li>EPSG projected CRS name: <b>NAD83 / Texas South Central (ftUS)</b></li>
     *   <li>Alias(es) given by EPSG: <b>NAD83 / Texas SC (ftUS)</b></li>
     *   <li>Geographic CRS name: <b>NAD83</b></li>
     *   <li>EPSG Usage Extent: <b>USA - Texas - SPCS83 - SC</b></li>
     * </ul>
     *
     * Remarks: Check UoM.
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("NAD83 / Texas South Central (ftUS)")
    public void EPSG_2278() throws FactoryException {
        name              = "NAD83 / Texas South Central (ftUS)";
        aliases           = new String[] {"NAD83 / Texas SC (ftUS)"};
        geographicCRS     = "NAD83";
        datumCode         = 6269;
        createAndVerifyProjectedCRS(2278);
    }

    /**
     * Tests “NAD83 / UTM” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>26911</b>, <b>26912</b></li>
     *   <li>EPSG projected CRS name: <b>NAD83 / UTM</b></li>
     *   <li>Geographic CRS name: <b>NAD83</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("NAD83 / UTM")
    public void variousNAD83UTM() throws FactoryException {
        name              = "NAD83 / UTM";
        geographicCRS     = "NAD83";
        datumCode         = 6269;
        createAndVerifyProjectedCRS(26911);
        createAndVerifyProjectedCRS(26912);
    }

    /**
     * Tests “NAD83 / Wyoming East (ftUS)” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>3736</b></li>
     *   <li>EPSG projected CRS name: <b>NAD83 / Wyoming East (ftUS)</b></li>
     *   <li>Alias(es) given by EPSG: <b>NAD83 / Wyoming E (ftUS)</b></li>
     *   <li>Geographic CRS name: <b>NAD83</b></li>
     *   <li>EPSG Usage Extent: <b>USA - Wyoming - SPCS - E</b></li>
     * </ul>
     *
     * Remarks: Check UoM.
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("NAD83 / Wyoming East (ftUS)")
    public void EPSG_3736() throws FactoryException {
        name              = "NAD83 / Wyoming East (ftUS)";
        aliases           = new String[] {"NAD83 / Wyoming E (ftUS)"};
        geographicCRS     = "NAD83";
        datumCode         = 6269;
        createAndVerifyProjectedCRS(3736);
    }

    /**
     * Tests “NAD83 / Wyoming East Central” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>32156</b></li>
     *   <li>EPSG projected CRS name: <b>NAD83 / Wyoming East Central</b></li>
     *   <li>Alias(es) given by EPSG: <b>NAD83 / Wyoming E. Cen.</b>, <b>NAD83 / Wyoming East Central (m)</b></li>
     *   <li>Geographic CRS name: <b>NAD83</b></li>
     *   <li>EPSG Usage Extent: <b>USA - Wyoming - SPCS - EC</b></li>
     * </ul>
     *
     * Remarks: Check UoM.
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("NAD83 / Wyoming East Central")
    public void EPSG_32156() throws FactoryException {
        name              = "NAD83 / Wyoming East Central";
        aliases           = new String[] {"NAD83 / Wyoming E. Cen.", "NAD83 / Wyoming East Central (m)"};
        geographicCRS     = "NAD83";
        datumCode         = 6269;
        createAndVerifyProjectedCRS(32156);
    }

    /**
     * Tests “Nahrwan 1967 / UTM zone 39N” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>27039</b></li>
     *   <li>EPSG projected CRS name: <b>Nahrwan 1967 / UTM zone 39N</b></li>
     *   <li>Alias(es) given by EPSG: <b>Nahrwan 1967 / UTM 39N</b></li>
     *   <li>Geographic CRS name: <b>Nahrwan 1967</b></li>
     *   <li>EPSG Usage Extent: <b>Asia - Middle East - Qatar offshore and UAE west of 54°E</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Nahrwan 1967 / UTM zone 39N")
    public void EPSG_27039() throws FactoryException {
        name              = "Nahrwan 1967 / UTM zone 39N";
        aliases           = new String[] {"Nahrwan 1967 / UTM 39N"};
        geographicCRS     = "Nahrwan 1967";
        datumCode         = 6270;
        createAndVerifyProjectedCRS(27039);
    }

    /**
     * Tests “Nahrwan 1967 / UTM zone 40N” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>27040</b></li>
     *   <li>EPSG projected CRS name: <b>Nahrwan 1967 / UTM zone 40N</b></li>
     *   <li>Alias(es) given by EPSG: <b>Nahrwan 1967 / UTM 40N</b></li>
     *   <li>Geographic CRS name: <b>Nahrwan 1967</b></li>
     *   <li>EPSG Usage Extent: <b>UAE - east of 54°E</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Nahrwan 1967 / UTM zone 40N")
    public void EPSG_27040() throws FactoryException {
        name              = "Nahrwan 1967 / UTM zone 40N";
        aliases           = new String[] {"Nahrwan 1967 / UTM 40N"};
        geographicCRS     = "Nahrwan 1967";
        datumCode         = 6270;
        createAndVerifyProjectedCRS(27040);
    }

    /**
     * Tests “Naparima 1955 / UTM zone 20N” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>2067</b></li>
     *   <li>EPSG projected CRS name: <b>Naparima 1955 / UTM zone 20N</b></li>
     *   <li>Alias(es) given by EPSG: <b>Naparima 1955 / UTM 20N</b></li>
     *   <li>Geographic CRS name: <b>Naparima 1955</b></li>
     *   <li>EPSG Usage Extent: <b>Trinidad and Tobago - Trinidad - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Naparima 1955 / UTM zone 20N")
    public void EPSG_2067() throws FactoryException {
        name              = "Naparima 1955 / UTM zone 20N";
        aliases           = new String[] {"Naparima 1955 / UTM 20N"};
        geographicCRS     = "Naparima 1955";
        datumCode         = 6158;
        createAndVerifyProjectedCRS(2067);
    }

    /**
     * Tests “Nord Sahara 1959 / Nord Algerie” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>30791</b></li>
     *   <li>EPSG projected CRS name: <b>Nord Sahara 1959 / Nord Algerie</b></li>
     *   <li>Alias(es) given by EPSG: <b>Nord Sahara 1959 / Lambert Nord Voirol Unifie 1960</b>, <b>Nord Sahara / N Algerie</b>, <b>Nord Sahara 1959 / Lambert Algerie Nord</b>, <b>Nord Sahara 1959 / LAN</b>, <b>Nord Sahara 1959 / Voirol Unifie Nord</b></li>
     *   <li>Geographic CRS name: <b>Nord Sahara 1959</b></li>
     *   <li>EPSG Usage Extent: <b>Algeria - north of 34°39'N</b></li>
     * </ul>
     *
     * Remarks: Check old Voirol.
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Nord Sahara 1959 / Nord Algerie")
    public void EPSG_30791() throws FactoryException {
        name              = "Nord Sahara 1959 / Nord Algerie";
        aliases           = new String[] {"Nord Sahara 1959 / Lambert Nord Voirol Unifie 1960", "Nord Sahara / N Algerie", "Nord Sahara 1959 / Lambert Algerie Nord", "Nord Sahara 1959 / LAN", "Nord Sahara 1959 / Voirol Unifie Nord"};
        geographicCRS     = "Nord Sahara 1959";
        datumCode         = 6307;
        createAndVerifyProjectedCRS(30791);
    }

    /**
     * Tests “Nord Sahara 1959 / Sud Algerie” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>30792</b></li>
     *   <li>EPSG projected CRS name: <b>Nord Sahara 1959 / Sud Algerie</b></li>
     *   <li>Alias(es) given by EPSG: <b>Nord Sahara 1959 / Lambert Sud Voirol Unifie 1960</b>, <b>Nord Sahara / S Algerie</b>, <b>Nord Sahara 1959 / Lambert Algerie Sud</b>, <b>Nord Sahara 1959 / LAS</b>, <b>Nord Sahara 1959 / Voirol Unifie Sud</b></li>
     *   <li>Geographic CRS name: <b>Nord Sahara 1959</b></li>
     *   <li>EPSG Usage Extent: <b>Algeria - 31°30'N to 34°39'N</b></li>
     * </ul>
     *
     * Remarks: Check old Voirol.
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Nord Sahara 1959 / Sud Algerie")
    public void EPSG_30792() throws FactoryException {
        name              = "Nord Sahara 1959 / Sud Algerie";
        aliases           = new String[] {"Nord Sahara 1959 / Lambert Sud Voirol Unifie 1960", "Nord Sahara / S Algerie", "Nord Sahara 1959 / Lambert Algerie Sud", "Nord Sahara 1959 / LAS", "Nord Sahara 1959 / Voirol Unifie Sud"};
        geographicCRS     = "Nord Sahara 1959";
        datumCode         = 6307;
        createAndVerifyProjectedCRS(30792);
    }

    /**
     * Tests “Nord Sahara 1959 / UTM zone 30N” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>30730</b></li>
     *   <li>EPSG projected CRS name: <b>Nord Sahara 1959 / UTM zone 30N</b></li>
     *   <li>Alias(es) given by EPSG: <b>Nord Sahara / UTM 30N</b></li>
     *   <li>Geographic CRS name: <b>Nord Sahara 1959</b></li>
     *   <li>EPSG Usage Extent: <b>Algeria - 6°W to 0°W</b></li>
     * </ul>
     *
     * Remarks: Check old Voirol.
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Nord Sahara 1959 / UTM zone 30N")
    public void EPSG_30730() throws FactoryException {
        name              = "Nord Sahara 1959 / UTM zone 30N";
        aliases           = new String[] {"Nord Sahara / UTM 30N"};
        geographicCRS     = "Nord Sahara 1959";
        datumCode         = 6307;
        createAndVerifyProjectedCRS(30730);
    }

    /**
     * Tests “Nord Sahara 1959 / UTM zone 31N” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>30731</b></li>
     *   <li>EPSG projected CRS name: <b>Nord Sahara 1959 / UTM zone 31N</b></li>
     *   <li>Alias(es) given by EPSG: <b>Nord Sahara / UTM 31N</b></li>
     *   <li>Geographic CRS name: <b>Nord Sahara 1959</b></li>
     *   <li>EPSG Usage Extent: <b>Algeria - 0°E to 6°E</b></li>
     * </ul>
     *
     * Remarks: Check old Voirol.
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Nord Sahara 1959 / UTM zone 31N")
    public void EPSG_30731() throws FactoryException {
        name              = "Nord Sahara 1959 / UTM zone 31N";
        aliases           = new String[] {"Nord Sahara / UTM 31N"};
        geographicCRS     = "Nord Sahara 1959";
        datumCode         = 6307;
        createAndVerifyProjectedCRS(30731);
    }

    /**
     * Tests “Nord Sahara 1959 / UTM zone 32N” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>30732</b></li>
     *   <li>EPSG projected CRS name: <b>Nord Sahara 1959 / UTM zone 32N</b></li>
     *   <li>Alias(es) given by EPSG: <b>Nord Sahara / UTM 32N</b></li>
     *   <li>Geographic CRS name: <b>Nord Sahara 1959</b></li>
     *   <li>EPSG Usage Extent: <b>Algeria - east of 6°E</b></li>
     * </ul>
     *
     * Remarks: Check old Voirol.
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Nord Sahara 1959 / UTM zone 32N")
    public void EPSG_30732() throws FactoryException {
        name              = "Nord Sahara 1959 / UTM zone 32N";
        aliases           = new String[] {"Nord Sahara / UTM 32N"};
        geographicCRS     = "Nord Sahara 1959";
        datumCode         = 6307;
        createAndVerifyProjectedCRS(30732);
    }

    /**
     * Tests “NTF (Paris) / Lambert zone I” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>27571</b></li>
     *   <li>EPSG projected CRS name: <b>NTF (Paris) / Lambert zone I</b></li>
     *   <li>Alias(es) given by EPSG: <b>NTF (Paris) / France I</b></li>
     *   <li>Geographic CRS name: <b>NTF (Paris)</b></li>
     *   <li>EPSG Usage Extent: <b>France - mainland north of 48.15°N</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("NTF (Paris) / Lambert zone I")
    public void EPSG_27571() throws FactoryException {
        name              = "NTF (Paris) / Lambert zone I";
        aliases           = new String[] {"NTF (Paris) / France I"};
        geographicCRS     = "NTF (Paris)";
        datumCode         = 6807;
        createAndVerifyProjectedCRS(27571);
    }

    /**
     * Tests “NTF (Paris) / Lambert zone II” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>27572</b></li>
     *   <li>EPSG projected CRS name: <b>NTF (Paris) / Lambert zone II</b></li>
     *   <li>Alias(es) given by EPSG: <b>NTF (Paris) / France II</b>, <b>NTF (Paris) / Lambert zone II etendu</b></li>
     *   <li>Geographic CRS name: <b>NTF (Paris)</b></li>
     *   <li>EPSG Usage Extent: <b>France - mainland 45.45°N to 48.15°N. Also all mainland.</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("NTF (Paris) / Lambert zone II")
    public void EPSG_27572() throws FactoryException {
        name              = "NTF (Paris) / Lambert zone II";
        aliases           = new String[] {"NTF (Paris) / France II", "NTF (Paris) / Lambert zone II etendu"};
        geographicCRS     = "NTF (Paris)";
        datumCode         = 6807;
        createAndVerifyProjectedCRS(27572);
    }

    /**
     * Tests “NTF (Paris) / Lambert zone III” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>27573</b></li>
     *   <li>EPSG projected CRS name: <b>NTF (Paris) / Lambert zone III</b></li>
     *   <li>Alias(es) given by EPSG: <b>NTF (Paris) / France III</b></li>
     *   <li>Geographic CRS name: <b>NTF (Paris)</b></li>
     *   <li>EPSG Usage Extent: <b>France - mainland south of 45.45°N</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("NTF (Paris) / Lambert zone III")
    public void EPSG_27573() throws FactoryException {
        name              = "NTF (Paris) / Lambert zone III";
        aliases           = new String[] {"NTF (Paris) / France III"};
        geographicCRS     = "NTF (Paris)";
        datumCode         = 6807;
        createAndVerifyProjectedCRS(27573);
    }

    /**
     * Tests “NZGD2000 / New Zealand Transverse Mercator 2000” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>2193</b></li>
     *   <li>EPSG projected CRS name: <b>NZGD2000 / New Zealand Transverse Mercator 2000</b></li>
     *   <li>Alias(es) given by EPSG: <b>NZGD2000 / NZTM</b>, <b>NZGD2000 / New Zealand Transverse Mercator</b>, <b>NZGD2000 / NZTM2000</b></li>
     *   <li>Geographic CRS name: <b>NZGD2000</b></li>
     *   <li>EPSG Usage Extent: <b>New Zealand - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("NZGD2000 / New Zealand Transverse Mercator 2000")
    public void EPSG_2193() throws FactoryException {
        name              = "NZGD2000 / New Zealand Transverse Mercator 2000";
        aliases           = new String[] {"NZGD2000 / NZTM", "NZGD2000 / New Zealand Transverse Mercator", "NZGD2000 / NZTM2000"};
        geographicCRS     = "NZGD2000";
        datumCode         = 6167;
        isNorthAxisFirst  = true;
        createAndVerifyProjectedCRS(2193);
    }

    /**
     * Tests “NZGD2000 / UTM” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>2133</b>, <b>2134</b>, <b>2135</b></li>
     *   <li>EPSG projected CRS name: <b>NZGD2000 / UTM</b></li>
     *   <li>Geographic CRS name: <b>NZGD2000</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("NZGD2000 / UTM")
    public void variousNZGD2000UTM() throws FactoryException {
        name              = "NZGD2000 / UTM";
        geographicCRS     = "NZGD2000";
        datumCode         = 6167;
        createAndVerifyProjectedCRS(2133);
        createAndVerifyProjectedCRS(2134);
        createAndVerifyProjectedCRS(2135);
    }

    /**
     * Tests “NZGD49 / New Zealand Map Grid” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>27200</b></li>
     *   <li>EPSG projected CRS name: <b>NZGD49 / New Zealand Map Grid</b></li>
     *   <li>Alias(es) given by EPSG: <b>NZGD49 / NZ Map Grid</b></li>
     *   <li>Geographic CRS name: <b>NZGD49</b></li>
     *   <li>EPSG Usage Extent: <b>New Zealand - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("NZGD49 / New Zealand Map Grid")
    public void EPSG_27200() throws FactoryException {
        name              = "NZGD49 / New Zealand Map Grid";
        aliases           = new String[] {"NZGD49 / NZ Map Grid"};
        geographicCRS     = "NZGD49";
        datumCode         = 6272;
        createAndVerifyProjectedCRS(27200);
    }

    /**
     * Tests “NZGD49 / North Island Grid” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>27291</b></li>
     *   <li>EPSG projected CRS name: <b>NZGD49 / North Island Grid</b></li>
     *   <li>Alias(es) given by EPSG: <b>GD49 / North Island Grid</b></li>
     *   <li>Geographic CRS name: <b>NZGD49</b></li>
     *   <li>EPSG Usage Extent: <b>New Zealand - North Island</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("NZGD49 / North Island Grid")
    public void EPSG_27291() throws FactoryException {
        name              = "NZGD49 / North Island Grid";
        aliases           = new String[] {"GD49 / North Island Grid"};
        geographicCRS     = "NZGD49";
        datumCode         = 6272;
        createAndVerifyProjectedCRS(27291);
    }

    /**
     * Tests “NZGD49 / South Island Grid” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>27292</b></li>
     *   <li>EPSG projected CRS name: <b>NZGD49 / South Island Grid</b></li>
     *   <li>Alias(es) given by EPSG: <b>GD49 / South Island Grid</b></li>
     *   <li>Geographic CRS name: <b>NZGD49</b></li>
     *   <li>EPSG Usage Extent: <b>New Zealand - South and Stewart Islands</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("NZGD49 / South Island Grid")
    public void EPSG_27292() throws FactoryException {
        name              = "NZGD49 / South Island Grid";
        aliases           = new String[] {"GD49 / South Island Grid"};
        geographicCRS     = "NZGD49";
        datumCode         = 6272;
        createAndVerifyProjectedCRS(27292);
    }

    /**
     * Tests “OSGB36 / British National Grid” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>27700</b></li>
     *   <li>EPSG projected CRS name: <b>OSGB36 / British National Grid</b></li>
     *   <li>Alias(es) given by EPSG: <b>British National Grid</b></li>
     *   <li>Geographic CRS name: <b>OSGB36</b></li>
     *   <li>EPSG Usage Extent: <b>UK - Britain and UKCS 49°45'N to 61°N; 9°W to 2°E</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("OSGB36 / British National Grid")
    public void EPSG_27700() throws FactoryException {
        name              = "OSGB36 / British National Grid";
        aliases           = new String[] {"British National Grid"};
        geographicCRS     = "OSGB36";
        datumCode         = 6277;
        createAndVerifyProjectedCRS(27700);
    }

    /**
     * Tests “Pointe Noire / UTM zone 32S” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>28232</b></li>
     *   <li>EPSG projected CRS name: <b>Pointe Noire / UTM zone 32S</b></li>
     *   <li>Alias(es) given by EPSG: <b>Point Noire / UTM 32S</b>, <b>Congo 1960 Pointe Noire / UTM zone 32S</b></li>
     *   <li>Geographic CRS name: <b>Pointe Noire</b></li>
     *   <li>EPSG Usage Extent: <b>Congo</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Pointe Noire / UTM zone 32S")
    public void EPSG_28232() throws FactoryException {
        name              = "Pointe Noire / UTM zone 32S";
        aliases           = new String[] {"Point Noire / UTM 32S", "Congo 1960 Pointe Noire / UTM zone 32S"};
        geographicCRS     = "Pointe Noire";
        datumCode         = 6282;
        createAndVerifyProjectedCRS(28232);
    }

    /**
     * Tests “POSGAR 94 / Argentina 1” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>22181</b></li>
     *   <li>EPSG projected CRS name: <b>POSGAR 94 / Argentina 1</b></li>
     *   <li>Alias(es) given by EPSG: <b>POSGAR 94 / Gauss-Kruger zone 1</b></li>
     *   <li>Geographic CRS name: <b>POSGAR 94</b></li>
     *   <li>EPSG Usage Extent: <b>Argentina - west of 70.5°W</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("POSGAR 94 / Argentina 1")
    public void EPSG_22181() throws FactoryException {
        name              = "POSGAR 94 / Argentina 1";
        aliases           = new String[] {"POSGAR 94 / Gauss-Kruger zone 1"};
        geographicCRS     = "POSGAR 94";
        datumCode         = 6694;
        isNorthAxisFirst  = true;
        createAndVerifyProjectedCRS(22181);
    }

    /**
     * Tests “POSGAR 94 / Argentina 2” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>22182</b></li>
     *   <li>EPSG projected CRS name: <b>POSGAR 94 / Argentina 2</b></li>
     *   <li>Alias(es) given by EPSG: <b>POSGAR 94 / Gauss-Kruger zone 2</b></li>
     *   <li>Geographic CRS name: <b>POSGAR 94</b></li>
     *   <li>EPSG Usage Extent: <b>Argentina - 70.5°W to 67.5°W onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("POSGAR 94 / Argentina 2")
    public void EPSG_22182() throws FactoryException {
        name              = "POSGAR 94 / Argentina 2";
        aliases           = new String[] {"POSGAR 94 / Gauss-Kruger zone 2"};
        geographicCRS     = "POSGAR 94";
        datumCode         = 6694;
        isNorthAxisFirst  = true;
        createAndVerifyProjectedCRS(22182);
    }

    /**
     * Tests “POSGAR 94 / Argentina 3” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>22183</b></li>
     *   <li>EPSG projected CRS name: <b>POSGAR 94 / Argentina 3</b></li>
     *   <li>Alias(es) given by EPSG: <b>POSGAR 94 / Gauss-Kruger zone 3</b></li>
     *   <li>Geographic CRS name: <b>POSGAR 94</b></li>
     *   <li>EPSG Usage Extent: <b>Argentina - 67.5°W to 64.5°W onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("POSGAR 94 / Argentina 3")
    public void EPSG_22183() throws FactoryException {
        name              = "POSGAR 94 / Argentina 3";
        aliases           = new String[] {"POSGAR 94 / Gauss-Kruger zone 3"};
        geographicCRS     = "POSGAR 94";
        datumCode         = 6694;
        isNorthAxisFirst  = true;
        createAndVerifyProjectedCRS(22183);
    }

    /**
     * Tests “POSGAR 94 / Argentina 4” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>22184</b></li>
     *   <li>EPSG projected CRS name: <b>POSGAR 94 / Argentina 4</b></li>
     *   <li>Alias(es) given by EPSG: <b>POSGAR 94 / Gauss-Kruger zone 4</b></li>
     *   <li>Geographic CRS name: <b>POSGAR 94</b></li>
     *   <li>EPSG Usage Extent: <b>Argentina - 64.5°W to 61.5°W onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("POSGAR 94 / Argentina 4")
    public void EPSG_22184() throws FactoryException {
        name              = "POSGAR 94 / Argentina 4";
        aliases           = new String[] {"POSGAR 94 / Gauss-Kruger zone 4"};
        geographicCRS     = "POSGAR 94";
        datumCode         = 6694;
        isNorthAxisFirst  = true;
        createAndVerifyProjectedCRS(22184);
    }

    /**
     * Tests “POSGAR 94 / Argentina 5” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>22185</b></li>
     *   <li>EPSG projected CRS name: <b>POSGAR 94 / Argentina 5</b></li>
     *   <li>Alias(es) given by EPSG: <b>POSGAR 94 / Gauss-Kruger zone 5</b></li>
     *   <li>Geographic CRS name: <b>POSGAR 94</b></li>
     *   <li>EPSG Usage Extent: <b>Argentina - 61.5°W to 58.5°W onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("POSGAR 94 / Argentina 5")
    public void EPSG_22185() throws FactoryException {
        name              = "POSGAR 94 / Argentina 5";
        aliases           = new String[] {"POSGAR 94 / Gauss-Kruger zone 5"};
        geographicCRS     = "POSGAR 94";
        datumCode         = 6694;
        isNorthAxisFirst  = true;
        createAndVerifyProjectedCRS(22185);
    }

    /**
     * Tests “POSGAR 94 / Argentina 6” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>22186</b></li>
     *   <li>EPSG projected CRS name: <b>POSGAR 94 / Argentina 6</b></li>
     *   <li>Alias(es) given by EPSG: <b>POSGAR 94 / Gauss-Kruger zone 6</b></li>
     *   <li>Geographic CRS name: <b>POSGAR 94</b></li>
     *   <li>EPSG Usage Extent: <b>Argentina - 58.5°W to 55.5°W onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("POSGAR 94 / Argentina 6")
    public void EPSG_22186() throws FactoryException {
        name              = "POSGAR 94 / Argentina 6";
        aliases           = new String[] {"POSGAR 94 / Gauss-Kruger zone 6"};
        geographicCRS     = "POSGAR 94";
        datumCode         = 6694;
        isNorthAxisFirst  = true;
        createAndVerifyProjectedCRS(22186);
    }

    /**
     * Tests “POSGAR 94 / Argentina 7” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>22187</b></li>
     *   <li>EPSG projected CRS name: <b>POSGAR 94 / Argentina 7</b></li>
     *   <li>Alias(es) given by EPSG: <b>POSGAR 94 / Gauss-Kruger zone 7</b></li>
     *   <li>Geographic CRS name: <b>POSGAR 94</b></li>
     *   <li>EPSG Usage Extent: <b>Argentina - east of 55.5°W onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("POSGAR 94 / Argentina 7")
    public void EPSG_22187() throws FactoryException {
        name              = "POSGAR 94 / Argentina 7";
        aliases           = new String[] {"POSGAR 94 / Gauss-Kruger zone 7"};
        geographicCRS     = "POSGAR 94";
        datumCode         = 6694;
        isNorthAxisFirst  = true;
        createAndVerifyProjectedCRS(22187);
    }

    /**
     * Tests “POSGAR 98 / Argentina 1” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>22171</b></li>
     *   <li>EPSG projected CRS name: <b>POSGAR 98 / Argentina 1</b></li>
     *   <li>Alias(es) given by EPSG: <b>POSGAR 98 / Gauss-Kruger zone 1</b></li>
     *   <li>Geographic CRS name: <b>POSGAR 98</b></li>
     *   <li>EPSG Usage Extent: <b>Argentina - west of 70.5°W</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("POSGAR 98 / Argentina 1")
    public void EPSG_22171() throws FactoryException {
        name              = "POSGAR 98 / Argentina 1";
        aliases           = new String[] {"POSGAR 98 / Gauss-Kruger zone 1"};
        geographicCRS     = "POSGAR 98";
        datumCode         = 6190;
        isNorthAxisFirst  = true;
        createAndVerifyProjectedCRS(22171);
    }

    /**
     * Tests “POSGAR 98 / Argentina 2” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>22172</b></li>
     *   <li>EPSG projected CRS name: <b>POSGAR 98 / Argentina 2</b></li>
     *   <li>Alias(es) given by EPSG: <b>POSGAR 98 / Gauss-Kruger zone 2</b></li>
     *   <li>Geographic CRS name: <b>POSGAR 98</b></li>
     *   <li>EPSG Usage Extent: <b>Argentina - 70.5°W to 67.5°W onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("POSGAR 98 / Argentina 2")
    public void EPSG_22172() throws FactoryException {
        name              = "POSGAR 98 / Argentina 2";
        aliases           = new String[] {"POSGAR 98 / Gauss-Kruger zone 2"};
        geographicCRS     = "POSGAR 98";
        datumCode         = 6190;
        isNorthAxisFirst  = true;
        createAndVerifyProjectedCRS(22172);
    }

    /**
     * Tests “POSGAR 98 / Argentina 3” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>22173</b></li>
     *   <li>EPSG projected CRS name: <b>POSGAR 98 / Argentina 3</b></li>
     *   <li>Alias(es) given by EPSG: <b>POSGAR 98 / Gauss-Kruger zone 3</b></li>
     *   <li>Geographic CRS name: <b>POSGAR 98</b></li>
     *   <li>EPSG Usage Extent: <b>Argentina - 67.5°W to 64.5°W onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("POSGAR 98 / Argentina 3")
    public void EPSG_22173() throws FactoryException {
        name              = "POSGAR 98 / Argentina 3";
        aliases           = new String[] {"POSGAR 98 / Gauss-Kruger zone 3"};
        geographicCRS     = "POSGAR 98";
        datumCode         = 6190;
        isNorthAxisFirst  = true;
        createAndVerifyProjectedCRS(22173);
    }

    /**
     * Tests “POSGAR 98 / Argentina 4” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>22174</b></li>
     *   <li>EPSG projected CRS name: <b>POSGAR 98 / Argentina 4</b></li>
     *   <li>Alias(es) given by EPSG: <b>POSGAR 98 / Gauss-Kruger zone 4</b></li>
     *   <li>Geographic CRS name: <b>POSGAR 98</b></li>
     *   <li>EPSG Usage Extent: <b>Argentina - 64.5°W to 61.5°W onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("POSGAR 98 / Argentina 4")
    public void EPSG_22174() throws FactoryException {
        name              = "POSGAR 98 / Argentina 4";
        aliases           = new String[] {"POSGAR 98 / Gauss-Kruger zone 4"};
        geographicCRS     = "POSGAR 98";
        datumCode         = 6190;
        isNorthAxisFirst  = true;
        createAndVerifyProjectedCRS(22174);
    }

    /**
     * Tests “POSGAR 98 / Argentina 5” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>22175</b></li>
     *   <li>EPSG projected CRS name: <b>POSGAR 98 / Argentina 5</b></li>
     *   <li>Alias(es) given by EPSG: <b>POSGAR 98 / Gauss-Kruger zone 5</b></li>
     *   <li>Geographic CRS name: <b>POSGAR 98</b></li>
     *   <li>EPSG Usage Extent: <b>Argentina - 61.5°W to 58.5°W onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("POSGAR 98 / Argentina 5")
    public void EPSG_22175() throws FactoryException {
        name              = "POSGAR 98 / Argentina 5";
        aliases           = new String[] {"POSGAR 98 / Gauss-Kruger zone 5"};
        geographicCRS     = "POSGAR 98";
        datumCode         = 6190;
        isNorthAxisFirst  = true;
        createAndVerifyProjectedCRS(22175);
    }

    /**
     * Tests “POSGAR 98 / Argentina 6” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>22176</b></li>
     *   <li>EPSG projected CRS name: <b>POSGAR 98 / Argentina 6</b></li>
     *   <li>Alias(es) given by EPSG: <b>POSGAR 98 / Gauss-Kruger zone 6</b></li>
     *   <li>Geographic CRS name: <b>POSGAR 98</b></li>
     *   <li>EPSG Usage Extent: <b>Argentina - 58.5°W to 55.5°W onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("POSGAR 98 / Argentina 6")
    public void EPSG_22176() throws FactoryException {
        name              = "POSGAR 98 / Argentina 6";
        aliases           = new String[] {"POSGAR 98 / Gauss-Kruger zone 6"};
        geographicCRS     = "POSGAR 98";
        datumCode         = 6190;
        isNorthAxisFirst  = true;
        createAndVerifyProjectedCRS(22176);
    }

    /**
     * Tests “POSGAR 98 / Argentina 7” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>22177</b></li>
     *   <li>EPSG projected CRS name: <b>POSGAR 98 / Argentina 7</b></li>
     *   <li>Alias(es) given by EPSG: <b>POSGAR 98 / Gauss-Kruger zone 7</b></li>
     *   <li>Geographic CRS name: <b>POSGAR 98</b></li>
     *   <li>EPSG Usage Extent: <b>Argentina - east of 55.5°W onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("POSGAR 98 / Argentina 7")
    public void EPSG_22177() throws FactoryException {
        name              = "POSGAR 98 / Argentina 7";
        aliases           = new String[] {"POSGAR 98 / Gauss-Kruger zone 7"};
        geographicCRS     = "POSGAR 98";
        datumCode         = 6190;
        isNorthAxisFirst  = true;
        createAndVerifyProjectedCRS(22177);
    }

    /**
     * Tests “PRS92 / Philippines zone 1” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>3121</b></li>
     *   <li>EPSG projected CRS name: <b>PRS92 / Philippines zone 1</b></li>
     *   <li>Alias(es) given by EPSG: <b>New Luzon / Philippines zone 1</b>, <b>PRS92 / Philippines 1</b></li>
     *   <li>Geographic CRS name: <b>PRS92</b></li>
     *   <li>EPSG Usage Extent: <b>Philippines - zone I</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("PRS92 / Philippines zone 1")
    public void EPSG_3121() throws FactoryException {
        name              = "PRS92 / Philippines zone 1";
        aliases           = new String[] {"New Luzon / Philippines zone 1", "PRS92 / Philippines 1"};
        geographicCRS     = "PRS92";
        datumCode         = 6683;
        createAndVerifyProjectedCRS(3121);
    }

    /**
     * Tests “PRS92 / Philippines zone 2” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>3122</b></li>
     *   <li>EPSG projected CRS name: <b>PRS92 / Philippines zone 2</b></li>
     *   <li>Alias(es) given by EPSG: <b>New Luzon / Philippines zone 2</b>, <b>PRS92 / Philippines 2</b></li>
     *   <li>Geographic CRS name: <b>PRS92</b></li>
     *   <li>EPSG Usage Extent: <b>Philippines - zone II</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("PRS92 / Philippines zone 2")
    public void EPSG_3122() throws FactoryException {
        name              = "PRS92 / Philippines zone 2";
        aliases           = new String[] {"New Luzon / Philippines zone 2", "PRS92 / Philippines 2"};
        geographicCRS     = "PRS92";
        datumCode         = 6683;
        createAndVerifyProjectedCRS(3122);
    }

    /**
     * Tests “PRS92 / Philippines zone 3” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>3123</b></li>
     *   <li>EPSG projected CRS name: <b>PRS92 / Philippines zone 3</b></li>
     *   <li>Alias(es) given by EPSG: <b>New Luzon / Philippines zone 3</b>, <b>PRS92 / Philippines 3</b></li>
     *   <li>Geographic CRS name: <b>PRS92</b></li>
     *   <li>EPSG Usage Extent: <b>Philippines - zone III</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("PRS92 / Philippines zone 3")
    public void EPSG_3123() throws FactoryException {
        name              = "PRS92 / Philippines zone 3";
        aliases           = new String[] {"New Luzon / Philippines zone 3", "PRS92 / Philippines 3"};
        geographicCRS     = "PRS92";
        datumCode         = 6683;
        createAndVerifyProjectedCRS(3123);
    }

    /**
     * Tests “PRS92 / Philippines zone 4” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>3124</b></li>
     *   <li>EPSG projected CRS name: <b>PRS92 / Philippines zone 4</b></li>
     *   <li>Alias(es) given by EPSG: <b>New Luzon / Philippines zone 4</b>, <b>PRS92 / Philippines 4</b></li>
     *   <li>Geographic CRS name: <b>PRS92</b></li>
     *   <li>EPSG Usage Extent: <b>Philippines - zone IV</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("PRS92 / Philippines zone 4")
    public void EPSG_3124() throws FactoryException {
        name              = "PRS92 / Philippines zone 4";
        aliases           = new String[] {"New Luzon / Philippines zone 4", "PRS92 / Philippines 4"};
        geographicCRS     = "PRS92";
        datumCode         = 6683;
        createAndVerifyProjectedCRS(3124);
    }

    /**
     * Tests “PRS92 / Philippines zone 5” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>3125</b></li>
     *   <li>EPSG projected CRS name: <b>PRS92 / Philippines zone 5</b></li>
     *   <li>Alias(es) given by EPSG: <b>New Luzon / Philippines zone 5</b>, <b>PRS92 / Philippines 5</b></li>
     *   <li>Geographic CRS name: <b>PRS92</b></li>
     *   <li>EPSG Usage Extent: <b>Philippines - zone V</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("PRS92 / Philippines zone 5")
    public void EPSG_3125() throws FactoryException {
        name              = "PRS92 / Philippines zone 5";
        aliases           = new String[] {"New Luzon / Philippines zone 5", "PRS92 / Philippines 5"};
        geographicCRS     = "PRS92";
        datumCode         = 6683;
        createAndVerifyProjectedCRS(3125);
    }

    /**
     * Tests “PSAD56 / Peru central zone” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>24892</b></li>
     *   <li>EPSG projected CRS name: <b>PSAD56 / Peru central zone</b></li>
     *   <li>Alias(es) given by EPSG: <b>PSAD56 / Peru central</b></li>
     *   <li>Geographic CRS name: <b>PSAD56</b></li>
     *   <li>EPSG Usage Extent: <b>Peru - 79°W to 73°W</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("PSAD56 / Peru central zone")
    public void EPSG_24892() throws FactoryException {
        name              = "PSAD56 / Peru central zone";
        aliases           = new String[] {"PSAD56 / Peru central"};
        geographicCRS     = "PSAD56";
        datumCode         = 6248;
        createAndVerifyProjectedCRS(24892);
    }

    /**
     * Tests “PSAD56 / Peru east zone” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>24893</b></li>
     *   <li>EPSG projected CRS name: <b>PSAD56 / Peru east zone</b></li>
     *   <li>Geographic CRS name: <b>PSAD56</b></li>
     *   <li>EPSG Usage Extent: <b>Peru - east of 73°W</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("PSAD56 / Peru east zone")
    public void EPSG_24893() throws FactoryException {
        name              = "PSAD56 / Peru east zone";
        geographicCRS     = "PSAD56";
        datumCode         = 6248;
        createAndVerifyProjectedCRS(24893);
    }

    /**
     * Tests “PSAD56 / Peru west zone” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>24891</b></li>
     *   <li>EPSG projected CRS name: <b>PSAD56 / Peru west zone</b></li>
     *   <li>Geographic CRS name: <b>PSAD56</b></li>
     *   <li>EPSG Usage Extent: <b>Peru - west of 79°W</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("PSAD56 / Peru west zone")
    public void EPSG_24891() throws FactoryException {
        name              = "PSAD56 / Peru west zone";
        geographicCRS     = "PSAD56";
        datumCode         = 6248;
        createAndVerifyProjectedCRS(24891);
    }

    /**
     * Tests “PSAD56 / UTM” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>24817</b>, <b>24818</b>, <b>24819</b>, <b>24820</b>, <b>24877</b>, <b>24878</b>, <b>24879</b></li>
     *   <li>EPSG projected CRS name: <b>PSAD56 / UTM</b></li>
     *   <li>Geographic CRS name: <b>PSAD56</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("PSAD56 / UTM")
    @SuppressWarnings("LocalVariableHidesMemberVariable")
    public void variousPSAD56UTM() throws FactoryException {
        name              = "PSAD56 / UTM";
        geographicCRS     = "PSAD56";
        datumCode         = 6248;
        for (int code = 24817; code <= 24820; code++) {    // Loop over 4 codes
            createAndVerifyProjectedCRS(code);
        }
        createAndVerifyProjectedCRS(24877);
        createAndVerifyProjectedCRS(24878);
        createAndVerifyProjectedCRS(24879);
    }

    /**
     * Tests “PSD93 / UTM zone 40N” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>3440</b></li>
     *   <li>EPSG projected CRS name: <b>PSD93 / UTM zone 40N</b></li>
     *   <li>Geographic CRS name: <b>PSD93</b></li>
     *   <li>EPSG Usage Extent: <b>Oman - onshore east of 54°E</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("PSD93 / UTM zone 40N")
    public void EPSG_3440() throws FactoryException {
        name              = "PSD93 / UTM zone 40N";
        geographicCRS     = "PSD93";
        datumCode         = 6134;
        createAndVerifyProjectedCRS(3440);
    }

    /**
     * Tests “Pulkovo 1942(58) / Gauss-Kruger zone 4” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>3334</b></li>
     *   <li>EPSG projected CRS name: <b>Pulkovo 1942(58) / Gauss-Kruger zone 4</b></li>
     *   <li>Alias(es) given by EPSG: <b>System 1942/21 (6)</b>, <b>S-42 zone 4</b>, <b>S-42 zone 34</b>, <b>Pulkovo 1942(58) / 6-degree Gauss-Kruger zone 4</b>, <b>Pulkovo 42(58) / GK zn 4</b></li>
     *   <li>Geographic CRS name: <b>Pulkovo 1942(58)</b></li>
     *   <li>EPSG Usage Extent: <b>Europe - 18°E to 24°E onshore and S-42(58) by country</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Pulkovo 1942(58) / Gauss-Kruger zone 4")
    public void EPSG_3334() throws FactoryException {
        name              = "Pulkovo 1942(58) / Gauss-Kruger zone 4";
        aliases           = new String[] {"System 1942/21 (6)", "S-42 zone 4", "S-42 zone 34", "Pulkovo 1942(58) / 6-degree Gauss-Kruger zone 4", "Pulkovo 42(58) / GK zn 4"};
        geographicCRS     = "Pulkovo 1942(58)";
        datumCode         = 6179;
        isNorthAxisFirst  = true;
        createAndVerifyProjectedCRS(3334);
    }

    /**
     * Tests “Pulkovo 1942(58) / Gauss-Kruger zone 5” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>3335</b></li>
     *   <li>EPSG projected CRS name: <b>Pulkovo 1942(58) / Gauss-Kruger zone 5</b></li>
     *   <li>Alias(es) given by EPSG: <b>System 1942/27 (6)</b>, <b>S-42 zone 5</b>, <b>S-42 zone 35</b>, <b>Pulkovo 1942(58) / 6-degree Gauss-Kruger zone 5</b>, <b>Pulkovo 42(58) / GK zn 5</b></li>
     *   <li>Geographic CRS name: <b>Pulkovo 1942(58)</b></li>
     *   <li>EPSG Usage Extent: <b>Europe - 24°E to 30°E onshore and S-42(58) by country</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Pulkovo 1942(58) / Gauss-Kruger zone 5")
    public void EPSG_3335() throws FactoryException {
        name              = "Pulkovo 1942(58) / Gauss-Kruger zone 5";
        aliases           = new String[] {"System 1942/27 (6)", "S-42 zone 5", "S-42 zone 35", "Pulkovo 1942(58) / 6-degree Gauss-Kruger zone 5", "Pulkovo 42(58) / GK zn 5"};
        geographicCRS     = "Pulkovo 1942(58)";
        datumCode         = 6179;
        isNorthAxisFirst  = true;
        createAndVerifyProjectedCRS(3335);
    }

    /**
     * Tests “Pulkovo 1942(58) / Stereo70” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>3844</b></li>
     *   <li>EPSG projected CRS name: <b>Pulkovo 1942(58) / Stereo70</b></li>
     *   <li>Alias(es) given by EPSG: <b>Stereo 70</b>, <b>S-42 / Stereo 70</b>, <b>Dealul Piscului 1970/ Stereo 70</b></li>
     *   <li>Geographic CRS name: <b>Pulkovo 1942(58)</b></li>
     *   <li>EPSG Usage Extent: <b>Romania</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Pulkovo 1942(58) / Stereo70")
    public void EPSG_3844() throws FactoryException {
        name              = "Pulkovo 1942(58) / Stereo70";
        aliases           = new String[] {"Stereo 70", "S-42 / Stereo 70", "Dealul Piscului 1970/ Stereo 70"};
        geographicCRS     = "Pulkovo 1942(58)";
        datumCode         = 6179;
        isNorthAxisFirst  = true;
        createAndVerifyProjectedCRS(3844);
    }

    /**
     * Tests “Pulkovo 1942(83) / Gauss-Kruger zone 4” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>3836</b></li>
     *   <li>EPSG projected CRS name: <b>Pulkovo 1942(83) / Gauss-Kruger zone 4</b></li>
     *   <li>Alias(es) given by EPSG: <b>Pulkovo 1942(83) / 6-degree Gauss-Kruger zone 4</b>, <b>S-42 zone 4</b>, <b>Pulkovo 42(83) / GK zn 4</b></li>
     *   <li>Geographic CRS name: <b>Pulkovo 1942(83)</b></li>
     *   <li>EPSG Usage Extent: <b>Europe - 18°E to 24°E onshore and S-42(83) by country</b></li>
     * </ul>
     *
     * Remarks: Check axes order and abbreviations.
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Pulkovo 1942(83) / Gauss-Kruger zone 4")
    public void EPSG_3836() throws FactoryException {
        name              = "Pulkovo 1942(83) / Gauss-Kruger zone 4";
        aliases           = new String[] {"Pulkovo 1942(83) / 6-degree Gauss-Kruger zone 4", "S-42 zone 4", "Pulkovo 42(83) / GK zn 4"};
        geographicCRS     = "Pulkovo 1942(83)";
        datumCode         = 6178;
        isNorthAxisFirst  = true;
        createAndVerifyProjectedCRS(3836);
    }

    /**
     * Tests “Pulkovo 1942 / Gauss-Kruger zone 16” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>28416</b></li>
     *   <li>EPSG projected CRS name: <b>Pulkovo 1942 / Gauss-Kruger zone 16</b></li>
     *   <li>Alias(es) given by EPSG: <b>S-42 zone 16</b>, <b>Pulkovo 1942 / 6-degree Gauss-Kruger zone 16</b></li>
     *   <li>Geographic CRS name: <b>Pulkovo 1942</b></li>
     *   <li>EPSG Usage Extent: <b>Russia - 90°E to 96°E onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Pulkovo 1942 / Gauss-Kruger zone 16")
    public void EPSG_28416() throws FactoryException {
        name              = "Pulkovo 1942 / Gauss-Kruger zone 16";
        aliases           = new String[] {"S-42 zone 16", "Pulkovo 1942 / 6-degree Gauss-Kruger zone 16"};
        geographicCRS     = "Pulkovo 1942";
        datumCode         = 6284;
        isNorthAxisFirst  = true;
        createAndVerifyProjectedCRS(28416);
    }

    /**
     * Tests “Pulkovo 1942 / Gauss-Kruger zone 24” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>28424</b></li>
     *   <li>EPSG projected CRS name: <b>Pulkovo 1942 / Gauss-Kruger zone 24</b></li>
     *   <li>Alias(es) given by EPSG: <b>S-42 zone 24</b>, <b>Pulkovo 1942 / 6-degree Gauss-Kruger zone 24</b></li>
     *   <li>Geographic CRS name: <b>Pulkovo 1942</b></li>
     *   <li>EPSG Usage Extent: <b>Russia - 138°E to 144°E onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Pulkovo 1942 / Gauss-Kruger zone 24")
    public void EPSG_28424() throws FactoryException {
        name              = "Pulkovo 1942 / Gauss-Kruger zone 24";
        aliases           = new String[] {"S-42 zone 24", "Pulkovo 1942 / 6-degree Gauss-Kruger zone 24"};
        geographicCRS     = "Pulkovo 1942";
        datumCode         = 6284;
        isNorthAxisFirst  = true;
        createAndVerifyProjectedCRS(28424);
    }

    /**
     * Tests “Pulkovo 1942 / Gauss-Kruger zone 9” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>28409</b></li>
     *   <li>EPSG projected CRS name: <b>Pulkovo 1942 / Gauss-Kruger zone 9</b></li>
     *   <li>Alias(es) given by EPSG: <b>S-42 zone 9</b>, <b>Pulkovo 1942 / 6-degree Gauss-Kruger zone 9</b></li>
     *   <li>Geographic CRS name: <b>Pulkovo 1942</b></li>
     *   <li>EPSG Usage Extent: <b>Asia - FSU onshore 48°E to 54°E</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Pulkovo 1942 / Gauss-Kruger zone 9")
    public void EPSG_28409() throws FactoryException {
        name              = "Pulkovo 1942 / Gauss-Kruger zone 9";
        aliases           = new String[] {"S-42 zone 9", "Pulkovo 1942 / 6-degree Gauss-Kruger zone 9"};
        geographicCRS     = "Pulkovo 1942";
        datumCode         = 6284;
        isNorthAxisFirst  = true;
        createAndVerifyProjectedCRS(28409);
    }

    /**
     * Tests “Qatar 1948 / Qatar Grid” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>2099</b></li>
     *   <li>EPSG projected CRS name: <b>Qatar 1948 / Qatar Grid</b></li>
     *   <li>Alias(es) given by EPSG: <b>Qatar Plane CS</b></li>
     *   <li>Geographic CRS name: <b>Qatar 1948</b></li>
     *   <li>EPSG Usage Extent: <b>Qatar - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Qatar 1948 / Qatar Grid")
    public void EPSG_2099() throws FactoryException {
        name              = "Qatar 1948 / Qatar Grid";
        aliases           = new String[] {"Qatar Plane CS"};
        geographicCRS     = "Qatar 1948";
        datumCode         = 6286;
        createAndVerifyProjectedCRS(2099);
    }

    /**
     * Tests “Qatar 1974 / Qatar National Grid” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>28600</b></li>
     *   <li>EPSG projected CRS name: <b>Qatar 1974 / Qatar National Grid</b></li>
     *   <li>Alias(es) given by EPSG: <b>Qatar National Grid</b></li>
     *   <li>Geographic CRS name: <b>Qatar 1974</b></li>
     *   <li>EPSG Usage Extent: <b>Qatar - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Qatar 1974 / Qatar National Grid")
    public void EPSG_28600() throws FactoryException {
        name              = "Qatar 1974 / Qatar National Grid";
        aliases           = new String[] {"Qatar National Grid"};
        geographicCRS     = "Qatar 1974";
        datumCode         = 6285;
        createAndVerifyProjectedCRS(28600);
    }

    /**
     * Tests “QND95 / Qatar National Grid” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>2932</b></li>
     *   <li>EPSG projected CRS name: <b>QND95 / Qatar National Grid</b></li>
     *   <li>Alias(es) given by EPSG: <b>QND95 / Qatar Nat Grid</b></li>
     *   <li>Geographic CRS name: <b>QND95</b></li>
     *   <li>EPSG Usage Extent: <b>Qatar - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("QND95 / Qatar National Grid")
    public void EPSG_2932() throws FactoryException {
        name              = "QND95 / Qatar National Grid";
        aliases           = new String[] {"QND95 / Qatar Nat Grid"};
        geographicCRS     = "QND95";
        datumCode         = 6614;
        createAndVerifyProjectedCRS(2932);
    }

    /**
     * Tests “REGVEN / UTM” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>2201</b>, <b>2202</b>, <b>2203</b></li>
     *   <li>EPSG projected CRS name: <b>REGVEN / UTM</b></li>
     *   <li>Geographic CRS name: <b>REGVEN</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("REGVEN / UTM")
    public void variousREGVENUTM() throws FactoryException {
        name              = "REGVEN / UTM";
        geographicCRS     = "REGVEN";
        datumCode         = 6189;
        createAndVerifyProjectedCRS(2201);
        createAndVerifyProjectedCRS(2202);
        createAndVerifyProjectedCRS(2203);
    }

    /**
     * Tests “RGF93 / Lambert-93” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>2154</b></li>
     *   <li>EPSG projected CRS name: <b>RGF93 / Lambert-93</b></li>
     *   <li>Geographic CRS name: <b>RGF93</b></li>
     *   <li>EPSG Usage Extent: <b>France</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("RGF93 / Lambert-93")
    public void EPSG_2154() throws FactoryException {
        name              = "RGF93 / Lambert-93";
        geographicCRS     = "RGF93";
        datumCode         = 6171;
        createAndVerifyProjectedCRS(2154);
    }

    /**
     * Tests “SAD69 / Brazil Polyconic” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>29101</b></li>
     *   <li>EPSG projected CRS name: <b>SAD69 / Brazil Polyconic</b></li>
     *   <li>Geographic CRS name: <b>SAD69</b></li>
     *   <li>EPSG Usage Extent: <b>Brazil</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("SAD69 / Brazil Polyconic")
    public void EPSG_29101() throws FactoryException {
        name              = "SAD69 / Brazil Polyconic";
        geographicCRS     = "SAD69";
        datumCode         = 6618;
        createAndVerifyProjectedCRS(29101);
    }

    /**
     * Tests “SAD69 / UTM zone 24S” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>29194</b></li>
     *   <li>EPSG projected CRS name: <b>SAD69 / UTM zone 24S</b></li>
     *   <li>Geographic CRS name: <b>SAD69</b></li>
     *   <li>EPSG Usage Extent: <b>Brazil - 42°W to 36°W</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("SAD69 / UTM zone 24S")
    public void EPSG_29194() throws FactoryException {
        name              = "SAD69 / UTM zone 24S";
        geographicCRS     = "SAD69";
        datumCode         = 6618;
        createAndVerifyProjectedCRS(29194);
    }

    /**
     * Tests “Schwarzeck / Lo22/11” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>29371</b></li>
     *   <li>EPSG projected CRS name: <b>Schwarzeck / Lo22/11</b></li>
     *   <li>Alias(es) given by EPSG: <b>SW African CS zone 11</b>, <b>South West African Coord. System zone 11</b></li>
     *   <li>Geographic CRS name: <b>Schwarzeck</b></li>
     *   <li>EPSG Usage Extent: <b>Namibia - west of 12°E</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Schwarzeck / Lo22/11")
    public void EPSG_29371() throws FactoryException {
        name              = "Schwarzeck / Lo22/11";
        aliases           = new String[] {"SW African CS zone 11", "South West African Coord. System zone 11"};
        geographicCRS     = "Schwarzeck";
        datumCode         = 6293;
        isWestOrientated  = true;
        isSouthOrientated = true;
        createAndVerifyProjectedCRS(29371);
    }

    /**
     * Tests “Schwarzeck / UTM zone 33S” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>29333</b></li>
     *   <li>EPSG projected CRS name: <b>Schwarzeck / UTM zone 33S</b></li>
     *   <li>Alias(es) given by EPSG: <b>Schwarzeck / UTM 33S</b></li>
     *   <li>Geographic CRS name: <b>Schwarzeck</b></li>
     *   <li>EPSG Usage Extent: <b>Namibia - offshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Schwarzeck / UTM zone 33S")
    public void EPSG_29333() throws FactoryException {
        name              = "Schwarzeck / UTM zone 33S";
        aliases           = new String[] {"Schwarzeck / UTM 33S"};
        geographicCRS     = "Schwarzeck";
        datumCode         = 6293;
        createAndVerifyProjectedCRS(29333);
    }

    /**
     * Tests “SIRGAS 1995 / UTM zone 22S” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>31997</b></li>
     *   <li>EPSG projected CRS name: <b>SIRGAS 1995 / UTM zone 22S</b></li>
     *   <li>Alias(es) given by EPSG: <b>SIRGAS / UTM zone 22S</b></li>
     *   <li>Geographic CRS name: <b>SIRGAS 1995</b></li>
     *   <li>EPSG Usage Extent: <b>South America - 54°W to 48°W; S hemisphere</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("SIRGAS 1995 / UTM zone 22S")
    public void EPSG_31997() throws FactoryException {
        name              = "SIRGAS 1995 / UTM zone 22S";
        aliases           = new String[] {"SIRGAS / UTM zone 22S"};
        geographicCRS     = "SIRGAS 1995";
        datumCode         = 6170;
        createAndVerifyProjectedCRS(31997);
    }

    /**
     * Tests “SIRGAS 2000 / UTM zone 22S” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>31982</b></li>
     *   <li>EPSG projected CRS name: <b>SIRGAS 2000 / UTM zone 22S</b></li>
     *   <li>Alias(es) given by EPSG: <b>SIRGAS 2000 / UTM 22S</b>, <b>SIRGAS2000 / UTM zone 22S</b></li>
     *   <li>Geographic CRS name: <b>SIRGAS 2000</b></li>
     *   <li>EPSG Usage Extent: <b>South America - 54°W to 48°W; S hemisphere and SIRGAS 2000 by country</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("SIRGAS 2000 / UTM zone 22S")
    public void EPSG_31982() throws FactoryException {
        name              = "SIRGAS 2000 / UTM zone 22S";
        aliases           = new String[] {"SIRGAS 2000 / UTM 22S", "SIRGAS2000 / UTM zone 22S"};
        geographicCRS     = "SIRGAS 2000";
        datumCode         = 6674;
        createAndVerifyProjectedCRS(31982);
    }

    /**
     * Tests “Tananarive (Paris) / Laborde Grid” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>29701</b></li>
     *   <li>EPSG projected CRS name: <b>Tananarive (Paris) / Laborde Grid</b></li>
     *   <li>Geographic CRS name: <b>Tananarive (Paris)</b></li>
     *   <li>EPSG Usage Extent: <b>Madagascar - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Tananarive (Paris) / Laborde Grid")
    public void EPSG_29701() throws FactoryException {
        name              = "Tananarive (Paris) / Laborde Grid";
        geographicCRS     = "Tananarive (Paris)";
        datumCode         = 6810;
        createAndVerifyProjectedCRS(29701);
    }

    /**
     * Tests “Tananarive (Paris) / Laborde Grid approximation” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>29702</b></li>
     *   <li>EPSG projected CRS name: <b>Tananarive (Paris) / Laborde Grid approximation</b></li>
     *   <li>Alias(es) given by EPSG: <b>Tananarive / Laborde app</b></li>
     *   <li>Geographic CRS name: <b>Tananarive (Paris)</b></li>
     *   <li>EPSG Usage Extent: <b>Madagascar - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Tananarive (Paris) / Laborde Grid approximation")
    public void EPSG_29702() throws FactoryException {
        name              = "Tananarive (Paris) / Laborde Grid approximation";
        aliases           = new String[] {"Tananarive / Laborde app"};
        geographicCRS     = "Tananarive (Paris)";
        datumCode         = 6810;
        isNorthAxisFirst  = true;
        createAndVerifyProjectedCRS(29702);
    }

    /**
     * Tests “Tananarive / UTM zone 38S” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>29738</b></li>
     *   <li>EPSG projected CRS name: <b>Tananarive / UTM zone 38S</b></li>
     *   <li>Alias(es) given by EPSG: <b>Tananarive / UTM 38S</b></li>
     *   <li>Geographic CRS name: <b>Tananarive</b></li>
     *   <li>EPSG Usage Extent: <b>Madagascar - nearshore - west of 48°E</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Tananarive / UTM zone 38S")
    public void EPSG_29738() throws FactoryException {
        name              = "Tananarive / UTM zone 38S";
        aliases           = new String[] {"Tananarive / UTM 38S"};
        geographicCRS     = "Tananarive";
        datumCode         = 6297;
        createAndVerifyProjectedCRS(29738);
    }

    /**
     * Tests “TC(1948) / UTM zone 39N” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>30339</b></li>
     *   <li>EPSG projected CRS name: <b>TC(1948) / UTM zone 39N</b></li>
     *   <li>Geographic CRS name: <b>TC(1948)</b></li>
     *   <li>EPSG Usage Extent: <b>UAE - Abu Dhabi - onshore west of 54°E</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("TC(1948) / UTM zone 39N")
    public void EPSG_30339() throws FactoryException {
        name              = "TC(1948) / UTM zone 39N";
        geographicCRS     = "TC(1948)";
        datumCode         = 6303;
        createAndVerifyProjectedCRS(30339);
    }

    /**
     * Tests “Timbalai 1948 / RSO Borneo (ch)” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>29871</b></li>
     *   <li>EPSG projected CRS name: <b>Timbalai 1948 / RSO Borneo (ch)</b></li>
     *   <li>Alias(es) given by EPSG: <b>Timbalai / Borneo (ch)</b></li>
     *   <li>Geographic CRS name: <b>Timbalai 1948</b></li>
     *   <li>EPSG Usage Extent: <b>Asia - Brunei and East Malaysia</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Timbalai 1948 / RSO Borneo (ch)")
    public void EPSG_29871() throws FactoryException {
        name              = "Timbalai 1948 / RSO Borneo (ch)";
        aliases           = new String[] {"Timbalai / Borneo (ch)"};
        geographicCRS     = "Timbalai 1948";
        datumCode         = 6298;
        createAndVerifyProjectedCRS(29871);
    }

    /**
     * Tests “Timbalai 1948 / RSO Borneo (ftSe)” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>29872</b></li>
     *   <li>EPSG projected CRS name: <b>Timbalai 1948 / RSO Borneo (ftSe)</b></li>
     *   <li>Alias(es) given by EPSG: <b>Timbalai / Borneo (ftSe)</b></li>
     *   <li>Geographic CRS name: <b>Timbalai 1948</b></li>
     *   <li>EPSG Usage Extent: <b>Malaysia - East Malaysia</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Timbalai 1948 / RSO Borneo (ftSe)")
    public void EPSG_29872() throws FactoryException {
        name              = "Timbalai 1948 / RSO Borneo (ftSe)";
        aliases           = new String[] {"Timbalai / Borneo (ftSe)"};
        geographicCRS     = "Timbalai 1948";
        datumCode         = 6298;
        createAndVerifyProjectedCRS(29872);
    }

    /**
     * Tests “Timbalai 1948 / RSO Borneo (m)” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>29873</b></li>
     *   <li>EPSG projected CRS name: <b>Timbalai 1948 / RSO Borneo (m)</b></li>
     *   <li>Alias(es) given by EPSG: <b>Timbalai / Borneo (m)</b>, <b>BT68 / RSO Borneo (m)</b>, <b>Timbalai 1968 / RSO Borneo (m)</b></li>
     *   <li>Geographic CRS name: <b>Timbalai 1948</b></li>
     *   <li>EPSG Usage Extent: <b>Asia - Brunei and East Malaysia</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Timbalai 1948 / RSO Borneo (m)")
    public void EPSG_29873() throws FactoryException {
        name              = "Timbalai 1948 / RSO Borneo (m)";
        aliases           = new String[] {"Timbalai / Borneo (m)", "BT68 / RSO Borneo (m)", "Timbalai 1968 / RSO Borneo (m)"};
        geographicCRS     = "Timbalai 1948";
        datumCode         = 6298;
        createAndVerifyProjectedCRS(29873);
    }

    /**
     * Tests “Timbalai 1948 / UTM zone 50N” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>29850</b></li>
     *   <li>EPSG projected CRS name: <b>Timbalai 1948 / UTM zone 50N</b></li>
     *   <li>Alias(es) given by EPSG: <b>Timbalai 1948 / UTM 50N</b>, <b>BT68 / UTM zone 50N</b>, <b>Timbalai 1968 / UTM zone 50N</b></li>
     *   <li>Geographic CRS name: <b>Timbalai 1948</b></li>
     *   <li>EPSG Usage Extent: <b>Asia - Brunei and East Malaysia - 114°E to 120°E</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Timbalai 1948 / UTM zone 50N")
    public void EPSG_29850() throws FactoryException {
        name              = "Timbalai 1948 / UTM zone 50N";
        aliases           = new String[] {"Timbalai 1948 / UTM 50N", "BT68 / UTM zone 50N", "Timbalai 1968 / UTM zone 50N"};
        geographicCRS     = "Timbalai 1948";
        datumCode         = 6298;
        createAndVerifyProjectedCRS(29850);
    }

    /**
     * Tests “Trinidad 1903 / Trinidad Grid” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>30200</b></li>
     *   <li>EPSG projected CRS name: <b>Trinidad 1903 / Trinidad Grid</b></li>
     *   <li>Alias(es) given by EPSG: <b>Trinidad 1903 / Cassini</b></li>
     *   <li>Geographic CRS name: <b>Trinidad 1903</b></li>
     *   <li>EPSG Usage Extent: <b>Trinidad and Tobago - Trinidad</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Trinidad 1903 / Trinidad Grid")
    public void EPSG_30200() throws FactoryException {
        name              = "Trinidad 1903 / Trinidad Grid";
        aliases           = new String[] {"Trinidad 1903 / Cassini"};
        geographicCRS     = "Trinidad 1903";
        datumCode         = 6302;
        createAndVerifyProjectedCRS(30200);
    }

    /**
     * Tests “Trinidad 1903 / Trinidad Grid (ftCla)” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>2314</b></li>
     *   <li>EPSG projected CRS name: <b>Trinidad 1903 / Trinidad Grid (ftCla)</b></li>
     *   <li>Alias(es) given by EPSG: <b>Trinidad 03 Grid (ftCla)</b></li>
     *   <li>Geographic CRS name: <b>Trinidad 1903</b></li>
     *   <li>EPSG Usage Extent: <b>Trinidad and Tobago - Trinidad</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Trinidad 1903 / Trinidad Grid (ftCla)")
    public void EPSG_2314() throws FactoryException {
        name              = "Trinidad 1903 / Trinidad Grid (ftCla)";
        aliases           = new String[] {"Trinidad 03 Grid (ftCla)"};
        geographicCRS     = "Trinidad 1903";
        datumCode         = 6302;
        createAndVerifyProjectedCRS(2314);
    }

    /**
     * Tests “WGS 72BE / UTM zone 48N” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>32448</b></li>
     *   <li>EPSG projected CRS name: <b>WGS 72BE / UTM zone 48N</b></li>
     *   <li>Geographic CRS name: <b>WGS 72BE</b></li>
     *   <li>EPSG Usage Extent: <b>World - N hemisphere - 102°E to 108°E - by country and WGS 72BE</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("WGS 72BE / UTM zone 48N")
    public void EPSG_32448() throws FactoryException {
        name              = "WGS 72BE / UTM zone 48N";
        geographicCRS     = "WGS 72BE";
        datumCode         = 6324;
        createAndVerifyProjectedCRS(32448);
    }

    /**
     * Tests “WGS 84 / UTM” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>various</b></li>
     *   <li>EPSG projected CRS name: <b>WGS 84 / UTM</b></li>
     *   <li>Geographic CRS name: <b>WGS 84</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("WGS 84 / UTM")
    @SuppressWarnings("LocalVariableHidesMemberVariable")
    public void variousWGS84UTM() throws FactoryException {
        name              = "WGS 84 / UTM";
        geographicCRS     = "WGS 84";
        datumCode         = 6326;
        for (int code = 32601; code <= 32660; code++) {    // Loop over 60 codes
            createAndVerifyProjectedCRS(code);
        }
        for (int code = 32701; code <= 32760; code++) {    // Loop over 60 codes
            createAndVerifyProjectedCRS(code);
        }
    }

    /**
     * Tests “Xian 1980 / Gauss-Kruger CM 105E” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>2343</b></li>
     *   <li>EPSG projected CRS name: <b>Xian 1980 / Gauss-Kruger CM 105E</b></li>
     *   <li>Alias(es) given by EPSG: <b>Xian 1980 / 6-degree Gauss-Kruger CM 105E</b>, <b>Xian 1980 / G-K CM 105E</b></li>
     *   <li>Geographic CRS name: <b>Xian 1980</b></li>
     *   <li>EPSG Usage Extent: <b>China - 102°E to 108°E onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Xian 1980 / Gauss-Kruger CM 105E")
    public void EPSG_2343() throws FactoryException {
        name              = "Xian 1980 / Gauss-Kruger CM 105E";
        aliases           = new String[] {"Xian 1980 / 6-degree Gauss-Kruger CM 105E", "Xian 1980 / G-K CM 105E"};
        geographicCRS     = "Xian 1980";
        datumCode         = 6610;
        isNorthAxisFirst  = true;
        createAndVerifyProjectedCRS(2343);
    }

    /**
     * Tests “Xian 1980 / Gauss-Kruger CM 111E” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>2344</b></li>
     *   <li>EPSG projected CRS name: <b>Xian 1980 / Gauss-Kruger CM 111E</b></li>
     *   <li>Alias(es) given by EPSG: <b>Xian 1980 / 6-degree Gauss-Kruger CM 111E</b>, <b>Xian 1980 / G-K CM 111E</b></li>
     *   <li>Geographic CRS name: <b>Xian 1980</b></li>
     *   <li>EPSG Usage Extent: <b>China - 108°E to 114°E onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Xian 1980 / Gauss-Kruger CM 111E")
    public void EPSG_2344() throws FactoryException {
        name              = "Xian 1980 / Gauss-Kruger CM 111E";
        aliases           = new String[] {"Xian 1980 / 6-degree Gauss-Kruger CM 111E", "Xian 1980 / G-K CM 111E"};
        geographicCRS     = "Xian 1980";
        datumCode         = 6610;
        isNorthAxisFirst  = true;
        createAndVerifyProjectedCRS(2344);
    }

    /**
     * Tests “Xian 1980 / Gauss-Kruger CM 117E” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>2345</b></li>
     *   <li>EPSG projected CRS name: <b>Xian 1980 / Gauss-Kruger CM 117E</b></li>
     *   <li>Alias(es) given by EPSG: <b>Xian 1980 / 6-degree Gauss-Kruger CM 117E</b>, <b>Xian 1980 / G-K CM 117E</b></li>
     *   <li>Geographic CRS name: <b>Xian 1980</b></li>
     *   <li>EPSG Usage Extent: <b>China - 114°E to 120°E onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Xian 1980 / Gauss-Kruger CM 117E")
    public void EPSG_2345() throws FactoryException {
        name              = "Xian 1980 / Gauss-Kruger CM 117E";
        aliases           = new String[] {"Xian 1980 / 6-degree Gauss-Kruger CM 117E", "Xian 1980 / G-K CM 117E"};
        geographicCRS     = "Xian 1980";
        datumCode         = 6610;
        isNorthAxisFirst  = true;
        createAndVerifyProjectedCRS(2345);
    }

    /**
     * Tests “Xian 1980 / Gauss-Kruger CM 123E” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>2346</b></li>
     *   <li>EPSG projected CRS name: <b>Xian 1980 / Gauss-Kruger CM 123E</b></li>
     *   <li>Alias(es) given by EPSG: <b>Xian 1980 / 6-degree Gauss-Kruger CM 123E</b>, <b>Xian 1980 / G-K CM 123E</b></li>
     *   <li>Geographic CRS name: <b>Xian 1980</b></li>
     *   <li>EPSG Usage Extent: <b>China - 120°E to 126°E onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Xian 1980 / Gauss-Kruger CM 123E")
    public void EPSG_2346() throws FactoryException {
        name              = "Xian 1980 / Gauss-Kruger CM 123E";
        aliases           = new String[] {"Xian 1980 / 6-degree Gauss-Kruger CM 123E", "Xian 1980 / G-K CM 123E"};
        geographicCRS     = "Xian 1980";
        datumCode         = 6610;
        isNorthAxisFirst  = true;
        createAndVerifyProjectedCRS(2346);
    }

    /**
     * Tests “Xian 1980 / Gauss-Kruger CM 129E” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>2347</b></li>
     *   <li>EPSG projected CRS name: <b>Xian 1980 / Gauss-Kruger CM 129E</b></li>
     *   <li>Alias(es) given by EPSG: <b>Xian 1980 / 6-degree Gauss-Kruger CM 129E</b>, <b>Xian 1980 / G-K CM 129E</b></li>
     *   <li>Geographic CRS name: <b>Xian 1980</b></li>
     *   <li>EPSG Usage Extent: <b>China - 126°E to 132°E onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Xian 1980 / Gauss-Kruger CM 129E")
    public void EPSG_2347() throws FactoryException {
        name              = "Xian 1980 / Gauss-Kruger CM 129E";
        aliases           = new String[] {"Xian 1980 / 6-degree Gauss-Kruger CM 129E", "Xian 1980 / G-K CM 129E"};
        geographicCRS     = "Xian 1980";
        datumCode         = 6610;
        isNorthAxisFirst  = true;
        createAndVerifyProjectedCRS(2347);
    }

    /**
     * Tests “Xian 1980 / Gauss-Kruger CM 135E” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>2348</b></li>
     *   <li>EPSG projected CRS name: <b>Xian 1980 / Gauss-Kruger CM 135E</b></li>
     *   <li>Alias(es) given by EPSG: <b>Xian 1980 / 6-degree Gauss-Kruger CM 135E</b>, <b>Xian 1980 / G-K CM 135E</b></li>
     *   <li>Geographic CRS name: <b>Xian 1980</b></li>
     *   <li>EPSG Usage Extent: <b>China - east of 132°E</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Xian 1980 / Gauss-Kruger CM 135E")
    public void EPSG_2348() throws FactoryException {
        name              = "Xian 1980 / Gauss-Kruger CM 135E";
        aliases           = new String[] {"Xian 1980 / 6-degree Gauss-Kruger CM 135E", "Xian 1980 / G-K CM 135E"};
        geographicCRS     = "Xian 1980";
        datumCode         = 6610;
        isNorthAxisFirst  = true;
        createAndVerifyProjectedCRS(2348);
    }

    /**
     * Tests “Xian 1980 / Gauss-Kruger CM 75E” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>2338</b></li>
     *   <li>EPSG projected CRS name: <b>Xian 1980 / Gauss-Kruger CM 75E</b></li>
     *   <li>Alias(es) given by EPSG: <b>Xian 1980 / 6-degree Gauss-Kruger CM 75E</b>, <b>Xian 1980 / G-K CM 75E</b></li>
     *   <li>Geographic CRS name: <b>Xian 1980</b></li>
     *   <li>EPSG Usage Extent: <b>China - west of 78°E</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Xian 1980 / Gauss-Kruger CM 75E")
    public void EPSG_2338() throws FactoryException {
        name              = "Xian 1980 / Gauss-Kruger CM 75E";
        aliases           = new String[] {"Xian 1980 / 6-degree Gauss-Kruger CM 75E", "Xian 1980 / G-K CM 75E"};
        geographicCRS     = "Xian 1980";
        datumCode         = 6610;
        isNorthAxisFirst  = true;
        createAndVerifyProjectedCRS(2338);
    }

    /**
     * Tests “Xian 1980 / Gauss-Kruger CM 81E” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>2339</b></li>
     *   <li>EPSG projected CRS name: <b>Xian 1980 / Gauss-Kruger CM 81E</b></li>
     *   <li>Alias(es) given by EPSG: <b>Xian 1980 / 6-degree Gauss-Kruger CM 81E</b>, <b>Xian 1980 / G-K CM 81E</b></li>
     *   <li>Geographic CRS name: <b>Xian 1980</b></li>
     *   <li>EPSG Usage Extent: <b>China - 78°E to 84°E</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Xian 1980 / Gauss-Kruger CM 81E")
    public void EPSG_2339() throws FactoryException {
        name              = "Xian 1980 / Gauss-Kruger CM 81E";
        aliases           = new String[] {"Xian 1980 / 6-degree Gauss-Kruger CM 81E", "Xian 1980 / G-K CM 81E"};
        geographicCRS     = "Xian 1980";
        datumCode         = 6610;
        isNorthAxisFirst  = true;
        createAndVerifyProjectedCRS(2339);
    }

    /**
     * Tests “Xian 1980 / Gauss-Kruger CM 87E” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>2340</b></li>
     *   <li>EPSG projected CRS name: <b>Xian 1980 / Gauss-Kruger CM 87E</b></li>
     *   <li>Alias(es) given by EPSG: <b>Xian 1980 / 6-degree Gauss-Kruger CM 87E</b>, <b>Xian 1980 / G-K CM 87E</b></li>
     *   <li>Geographic CRS name: <b>Xian 1980</b></li>
     *   <li>EPSG Usage Extent: <b>China - 84°E to 90°E</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Xian 1980 / Gauss-Kruger CM 87E")
    public void EPSG_2340() throws FactoryException {
        name              = "Xian 1980 / Gauss-Kruger CM 87E";
        aliases           = new String[] {"Xian 1980 / 6-degree Gauss-Kruger CM 87E", "Xian 1980 / G-K CM 87E"};
        geographicCRS     = "Xian 1980";
        datumCode         = 6610;
        isNorthAxisFirst  = true;
        createAndVerifyProjectedCRS(2340);
    }

    /**
     * Tests “Xian 1980 / Gauss-Kruger CM 93E” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>2341</b></li>
     *   <li>EPSG projected CRS name: <b>Xian 1980 / Gauss-Kruger CM 93E</b></li>
     *   <li>Alias(es) given by EPSG: <b>Xian 1980 / 6-degree Gauss-Kruger CM 93E</b>, <b>Xian 1980 / G-K CM 93E</b></li>
     *   <li>Geographic CRS name: <b>Xian 1980</b></li>
     *   <li>EPSG Usage Extent: <b>China - 90°E to 96°E</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Xian 1980 / Gauss-Kruger CM 93E")
    public void EPSG_2341() throws FactoryException {
        name              = "Xian 1980 / Gauss-Kruger CM 93E";
        aliases           = new String[] {"Xian 1980 / 6-degree Gauss-Kruger CM 93E", "Xian 1980 / G-K CM 93E"};
        geographicCRS     = "Xian 1980";
        datumCode         = 6610;
        isNorthAxisFirst  = true;
        createAndVerifyProjectedCRS(2341);
    }

    /**
     * Tests “Xian 1980 / Gauss-Kruger CM 99E” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>2342</b></li>
     *   <li>EPSG projected CRS name: <b>Xian 1980 / Gauss-Kruger CM 99E</b></li>
     *   <li>Alias(es) given by EPSG: <b>Xian 1980 / 6-degree Gauss-Kruger CM 99E</b>, <b>Xian 1980 / G-K CM 99E</b></li>
     *   <li>Geographic CRS name: <b>Xian 1980</b></li>
     *   <li>EPSG Usage Extent: <b>China - 96°E to 102°E</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Xian 1980 / Gauss-Kruger CM 99E")
    public void EPSG_2342() throws FactoryException {
        name              = "Xian 1980 / Gauss-Kruger CM 99E";
        aliases           = new String[] {"Xian 1980 / 6-degree Gauss-Kruger CM 99E", "Xian 1980 / G-K CM 99E"};
        geographicCRS     = "Xian 1980";
        datumCode         = 6610;
        isNorthAxisFirst  = true;
        createAndVerifyProjectedCRS(2342);
    }

    /**
     * Tests “Yemen NGN96 / UTM zone 38N” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>2089</b></li>
     *   <li>EPSG projected CRS name: <b>Yemen NGN96 / UTM zone 38N</b></li>
     *   <li>Alias(es) given by EPSG: <b>Yemen NGN96 / UTM 38N</b></li>
     *   <li>Geographic CRS name: <b>Yemen NGN96</b></li>
     *   <li>EPSG Usage Extent: <b>Yemen - 42°E to 48°E</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Yemen NGN96 / UTM zone 38N")
    public void EPSG_2089() throws FactoryException {
        name              = "Yemen NGN96 / UTM zone 38N";
        aliases           = new String[] {"Yemen NGN96 / UTM 38N"};
        geographicCRS     = "Yemen NGN96";
        datumCode         = 6163;
        createAndVerifyProjectedCRS(2089);
    }

    /**
     * Tests “Yemen NGN96 / UTM zone 39N” projected CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG projected CRS code: <b>2090</b></li>
     *   <li>EPSG projected CRS name: <b>Yemen NGN96 / UTM zone 39N</b></li>
     *   <li>Alias(es) given by EPSG: <b>Yemen NGN96 / UTM 39N</b></li>
     *   <li>Geographic CRS name: <b>Yemen NGN96</b></li>
     *   <li>EPSG Usage Extent: <b>Yemen - 48°E to 54°E</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the projected CRS from the EPSG code.
     */
    @Test
    @DisplayName("Yemen NGN96 / UTM zone 39N")
    public void EPSG_2090() throws FactoryException {
        name              = "Yemen NGN96 / UTM zone 39N";
        aliases           = new String[] {"Yemen NGN96 / UTM 39N"};
        geographicCRS     = "Yemen NGN96";
        datumCode         = 6163;
        createAndVerifyProjectedCRS(2090);
    }
}
