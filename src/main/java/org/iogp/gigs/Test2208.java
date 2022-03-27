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
import org.opengis.referencing.operation.Transformation;
import org.opengis.referencing.operation.CoordinateOperation;
import org.opengis.referencing.operation.CoordinateOperationAuthorityFactory;
import org.opengis.referencing.operation.OperationMethod;
import org.iogp.gigs.internal.geoapi.Configuration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Verifies reference coordinate transformations bundled with the geoscience software.
 *
 * <table class="gigs">
 * <caption>Test description</caption>
 * <tr>
 *   <th>Test method:</th>
 *   <td>Compare transformation definitions included in the software against the EPSG Dataset.</td>
 * </tr><tr>
 *   <th>Test data:</th>
 *   <td><a href="https://github.com/IOGP-GIGS/GIGSTestDataset/tree/main/GIGSTestDatasetFiles/GIGS%202200%20Predefined%20Geodetic%20Data%20Objects%20test%20data/ASCII/GIGS_lib_2208_CoordTfm.txt">{@code GIGS_lib_2208_CoordTfm.txt}</a>
 *       and EPSG Dataset.</td>
 * </tr><tr>
 *   <th>Tested API:</th>
 *   <td>{@link CoordinateOperationAuthorityFactory#createCoordinateOperation(String)}.</td>
 * </tr><tr>
 *   <th>Expected result:</th>
 *   <td>Transformation definitions bundled with the software should have the same name, method name,
 *       defining parameters and parameter values as in EPSG Dataset. The values of the parameters should
 *       be correct to at least 10 significant figures. Transformations missing from the software or at
 *       variance with those in the EPSG Dataset should be reported.</td>
 * </tr></table>
 *
 *
 * <h2>Usage example</h2>
 * in order to specify their factories and run the tests in a JUnit framework,
 * implementers can define a subclass in their own test suite as in the example below:
 *
 * <blockquote><pre>public class MyTest extends Test2208 {
 *    public MyTest() {
 *        super(new MyCoordinateOperationAuthorityFactory());
 *    }
 *}</pre></blockquote>
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @author  Alexis Manin (Geomatys)
 * @version 1.0
 * @since   1.0
 */
@DisplayName("Coordinate transformation")
public class Test2208 extends Series2000<Transformation> {
    /**
     * Name of the expected transformation method.
     * This field is set by all test methods before to create and verify the {@link Transformation} instance.
     */
    public String methodName;

    /**
     * The transformation version.
     * This field is set by all test methods before to create and verify the {@link Transformation} instance.
     */
    public String version;

    /**
     * The coordinate transformation created by the factory,
     * or {@code null} if not yet created or if CRS creation failed.
     *
     * @see #copAuthorityFactory
     */
    private Transformation transformation;

    /**
     * Factory to use for building {@link Transformation} instances, or {@code null} if none.
     * This is the factory used by the {@link #getIdentifiedObject()} method.
     */
    protected final CoordinateOperationAuthorityFactory copAuthorityFactory;

    /**
     * Creates a new test using the given factory. If a given factory is {@code null},
     * then the tests which depend on it will be skipped.
     *
     * @param copFactory  factory for creating {@link Transformation} instances.
     */
    public Test2208(final CoordinateOperationAuthorityFactory copFactory) {
        copAuthorityFactory = copFactory;
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
     *       <li>{@link #isOperationVersionSupported}</li>
     *       <li>{@link #copAuthorityFactory}</li>
     *     </ul>
     *   </li>
     * </ul>
     *
     * @return the configuration of the test being run.
     */
    @Override
    Configuration configuration() {
        final Configuration op = super.configuration();
        assertNull(op.put(Configuration.Key.isOperationVersionSupported, isOperationVersionSupported));
        assertNull(op.put(Configuration.Key.copAuthorityFactory, copAuthorityFactory));
        return op;
    }

    /**
     * Returns the transformation instance to be tested. When this method is invoked for the first time, it creates the
     * transformation to test by invoking the {@link CoordinateOperationAuthorityFactory#createCoordinateOperation(String)}
     * method with the current {@link #code} value in argument. The created object is then cached and returned in all
     * subsequent invocations of this method.
     *
     * @return the transformation instance to test.
     * @throws FactoryException if an error occurred while creating the transformation instance.
     */
    @Override
    public Transformation getIdentifiedObject() throws FactoryException {
        if (transformation == null) {
            assumeNotNull(copAuthorityFactory);
            final String codeAsString = String.valueOf(code);
            final CoordinateOperation operation;
            try {
                operation = copAuthorityFactory.createCoordinateOperation(codeAsString);
            } catch (NoSuchIdentifierException e) {
                /*
                 * Relaxed the exception type from NoSuchAuthorityCodeException because CoordinateOperation creation
                 * will typically use MathTransformFactory under the hood, which throws NoSuchIdentifierException for
                 * non-implemented operation methods (may be identified by their name rather than EPSG code).
                 */
                unsupportedCode(Transformation.class, code);
                throw e;
            }
            if (operation != null) {                        // For consistency with the behavior in other classes.
                assertInstanceOf(Transformation.class, operation, codeAsString);
                transformation = (Transformation) operation;
            }
        }
        return transformation;
    }

    /**
     * Verifies the properties of the transformation given by {@link #getIdentifiedObject()}.
     *
     * @throws FactoryException if an error occurred while creating the transformation.
     */
    private void verifyTransformation() throws FactoryException {
        final Transformation transformation = getIdentifiedObject();
        assertNotNull(transformation, "Transformation");
        validators.validate(transformation);

        // Transformation identification.
        assertIdentifierEquals(code, transformation, "Transformation");
        assertNameEquals(true, name, transformation, "Transformation");
        assertAliasesEqual( aliases, transformation, "Transformation");

        // Operation method.
        final OperationMethod method = transformation.getMethod();
        assertNotNull(method, "Transformation.getMethod()");
        assertNameEquals(true, methodName, method, "Transformation.getMethod()");

        if (isOperationVersionSupported) {
            assertEquals(version, transformation.getOperationVersion(), "Transformation.getOperationVersion()");
        }
    }

    /**
     * Tests “AGD66 to GDA94 (11)” transformation creation from the factory.
     *
     * <ul>
     *   <li>EPSG transformation code: <b>1803</b></li>
     *   <li>EPSG transformation name: <b>AGD66 to GDA94 (11)</b></li>
     *   <li>Alias(es) given by EPSG: <b>AGD66 to GDA94 [GA v2]</b></li>
     *   <li>Transformation version: <b>ICSM-Aus 0.1m</b></li>
     *   <li>Operation method name: <b>NTv2</b></li>
     *   <li>EPSG Usage Extent: <b>Australia - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the transformation from the EPSG code.
     */
    @Test
    @DisplayName("AGD66 to GDA94 (11)")
    public void EPSG_1803() throws FactoryException {
        code       = 1803;
        name       = "AGD66 to GDA94 (11)";
        aliases    = new String[] {"AGD66 to GDA94 [GA v2]"};
        version    = "ICSM-Aus 0.1m";
        methodName = "NTv2";
        verifyTransformation();
    }

    /**
     * Tests “AGD66 to WGS 84 (17)” transformation creation from the factory.
     *
     * <ul>
     *   <li>EPSG transformation code: <b>15786</b></li>
     *   <li>EPSG transformation name: <b>AGD66 to WGS 84 (17)</b></li>
     *   <li>Transformation version: <b>OGP-Aus 0.1m</b></li>
     *   <li>Operation method name: <b>NTv2</b></li>
     *   <li>EPSG Usage Extent: <b>Australia - onshore</b></li>
     * </ul>
     *
     * Remarks: EPSG copy of 1803.
     *
     * @throws FactoryException if an error occurred while creating the transformation from the EPSG code.
     */
    @Test
    @DisplayName("AGD66 to WGS 84 (17)")
    public void EPSG_15786() throws FactoryException {
        code       = 15786;
        name       = "AGD66 to WGS 84 (17)";
        version    = "OGP-Aus 0.1m";
        methodName = "NTv2";
        verifyTransformation();
    }

    /**
     * Tests “AGD84 to GDA94 (5)” transformation creation from the factory.
     *
     * <ul>
     *   <li>EPSG transformation code: <b>1804</b></li>
     *   <li>EPSG transformation name: <b>AGD84 to GDA94 (5)</b></li>
     *   <li>Alias(es) given by EPSG: <b>AGD84 to GDA94 [GA v2]</b></li>
     *   <li>Transformation version: <b>Auslig-Aus 0.1m</b></li>
     *   <li>Operation method name: <b>NTv2</b></li>
     *   <li>EPSG Usage Extent: <b>Australia</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the transformation from the EPSG code.
     */
    @Test
    @DisplayName("AGD84 to GDA94 (5)")
    public void EPSG_1804() throws FactoryException {
        code       = 1804;
        name       = "AGD84 to GDA94 (5)";
        aliases    = new String[] {"AGD84 to GDA94 [GA v2]"};
        version    = "Auslig-Aus 0.1m";
        methodName = "NTv2";
        verifyTransformation();
    }

    /**
     * Tests “AGD84 to WGS 84 (9)” transformation creation from the factory.
     *
     * <ul>
     *   <li>EPSG transformation code: <b>15785</b></li>
     *   <li>EPSG transformation name: <b>AGD84 to WGS 84 (9)</b></li>
     *   <li>Transformation version: <b>OGP-Aus 1m</b></li>
     *   <li>Operation method name: <b>NTv2</b></li>
     *   <li>EPSG Usage Extent: <b>Australia</b></li>
     * </ul>
     *
     * Remarks: EPSG copy of 1804.
     *
     * @throws FactoryException if an error occurred while creating the transformation from the EPSG code.
     */
    @Test
    @DisplayName("AGD84 to WGS 84 (9)")
    public void EPSG_15785() throws FactoryException {
        code       = 15785;
        name       = "AGD84 to WGS 84 (9)";
        version    = "OGP-Aus 1m";
        methodName = "NTv2";
        verifyTransformation();
    }

    /**
     * Tests “Amersfoort to WGS 84 (3)” transformation creation from the factory.
     *
     * <ul>
     *   <li>EPSG transformation code: <b>15934</b></li>
     *   <li>EPSG transformation name: <b>Amersfoort to WGS 84 (3)</b></li>
     *   <li>Transformation version: <b>OGP-Nld</b></li>
     *   <li>Operation method name: <b>Coordinate Frame rotation</b></li>
     *   <li>EPSG Usage Extent: <b>Netherlands - onshore</b></li>
     * </ul>
     *
     * Remarks: Uses unusual unit (microradian) as rotation unit.
     *
     * @throws FactoryException if an error occurred while creating the transformation from the EPSG code.
     */
    @Test
    @DisplayName("Amersfoort to WGS 84 (3)")
    public void EPSG_15934() throws FactoryException {
        code       = 15934;
        name       = "Amersfoort to WGS 84 (3)";
        version    = "OGP-Nld";
        methodName = "Coordinate Frame rotation";
        verifyTransformation();
    }

    /**
     * Tests “Bogota 1975 to MAGNA-SIRGAS (9)” transformation creation from the factory.
     *
     * <ul>
     *   <li>EPSG transformation code: <b>15730</b></li>
     *   <li>EPSG transformation name: <b>Bogota 1975 to MAGNA-SIRGAS (9)</b></li>
     *   <li>Transformation version: <b>IGAC-Col MB reg 1</b></li>
     *   <li>Operation method name: <b>Molodensky-Badekas 10-parameter transformation</b></li>
     *   <li>EPSG Usage Extent: <b>Colombia region 1</b></li>
     * </ul>
     *
     * Remarks: Uses unusual unit (radian) as rotation unit.
     *
     * @throws FactoryException if an error occurred while creating the transformation from the EPSG code.
     */
    @Test
    @DisplayName("Bogota 1975 to MAGNA-SIRGAS (9)")
    public void EPSG_15730() throws FactoryException {
        code       = 15730;
        name       = "Bogota 1975 to MAGNA-SIRGAS (9)";
        version    = "IGAC-Col MB reg 1";
        methodName = "Molodensky-Badekas 10-parameter transformation";
        verifyTransformation();
    }

    /**
     * Tests “Bogota 1975 to WGS 84 (3)” transformation creation from the factory.
     *
     * <ul>
     *   <li>EPSG transformation code: <b>15715</b></li>
     *   <li>EPSG transformation name: <b>Bogota 1975 to WGS 84 (3)</b></li>
     *   <li>Transformation version: <b>EPSG-Col reg 1</b></li>
     *   <li>Operation method name: <b>Coordinate Frame rotation</b></li>
     *   <li>EPSG Usage Extent: <b>Colombia region 1</b></li>
     * </ul>
     *
     * Remarks: Uses unusual unit (radian) as rotation unit.
     *
     * @throws FactoryException if an error occurred while creating the transformation from the EPSG code.
     */
    @Test
    @DisplayName("Bogota 1975 to WGS 84 (3)")
    public void EPSG_15715() throws FactoryException {
        code       = 15715;
        name       = "Bogota 1975 to WGS 84 (3)";
        version    = "EPSG-Col reg 1";
        methodName = "Coordinate Frame rotation";
        verifyTransformation();
    }

    /**
     * Tests “Camacupa 1948 to WGS 84 (10)” transformation creation from the factory.
     *
     * <ul>
     *   <li>EPSG transformation code: <b>1327</b></li>
     *   <li>EPSG transformation name: <b>Camacupa 1948 to WGS 84 (10)</b></li>
     *   <li>Alias(es) given by EPSG: <b>Camacupa to WGS 84 (10)</b></li>
     *   <li>Transformation version: <b>ELF-Ago N</b></li>
     *   <li>Operation method name: <b>Geocentric translations</b></li>
     *   <li>EPSG Usage Extent: <b>Angola - offshore blocks 2 3 17-18 and 31-33</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the transformation from the EPSG code.
     */
    @Test
    @DisplayName("Camacupa 1948 to WGS 84 (10)")
    public void EPSG_1327() throws FactoryException {
        code       = 1327;
        name       = "Camacupa 1948 to WGS 84 (10)";
        aliases    = new String[] {"Camacupa to WGS 84 (10)"};
        version    = "ELF-Ago N";
        methodName = "Geocentric translations";
        verifyTransformation();
    }

    /**
     * Tests “CH1903 to WGS 84 (1)” transformation creation from the factory.
     *
     * <ul>
     *   <li>EPSG transformation code: <b>1753</b></li>
     *   <li>EPSG transformation name: <b>CH1903 to WGS 84 (1)</b></li>
     *   <li>Alias(es) given by EPSG: <b>GRANIT87-Parameters</b></li>
     *   <li>Transformation version: <b>BfL-CH 1</b></li>
     *   <li>Operation method name: <b>Coordinate Frame rotation</b></li>
     *   <li>EPSG Usage Extent: <b>Europe - Liechtenstein and Switzerland</b></li>
     * </ul>
     *
     * Remarks: Uses unusual unit (centesimal second) as rotation unit.
     *
     * @throws FactoryException if an error occurred while creating the transformation from the EPSG code.
     */
    @Test
    @DisplayName("CH1903 to WGS 84 (1)")
    public void EPSG_1753() throws FactoryException {
        code       = 1753;
        name       = "CH1903 to WGS 84 (1)";
        aliases    = new String[] {"GRANIT87-Parameters"};
        version    = "BfL-CH 1";
        methodName = "Coordinate Frame rotation";
        verifyTransformation();
    }

    /**
     * Tests “ED50 to WGS 84 (18)” transformation creation from the factory.
     *
     * <ul>
     *   <li>EPSG transformation code: <b>1311</b></li>
     *   <li>EPSG transformation name: <b>ED50 to WGS 84 (18)</b></li>
     *   <li>Alias(es) given by EPSG: <b>ED50 to WGS 84 (Common Offshore)</b></li>
     *   <li>Transformation version: <b>UKOOA-CO</b></li>
     *   <li>Operation method name: <b>Position Vector 7-param. transformation</b></li>
     *   <li>EPSG Usage Extent: <b>Europe - common offshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the transformation from the EPSG code.
     */
    @Test
    @DisplayName("ED50 to WGS 84 (18)")
    public void EPSG_1311() throws FactoryException {
        code       = 1311;
        name       = "ED50 to WGS 84 (18)";
        aliases    = new String[] {"ED50 to WGS 84 (Common Offshore)"};
        version    = "UKOOA-CO";
        methodName = "Position Vector 7-param. transformation";
        verifyTransformation();
    }

    /**
     * Tests “ED50 to WGS 84 (23)” transformation creation from the factory.
     *
     * <ul>
     *   <li>EPSG transformation code: <b>1612</b></li>
     *   <li>EPSG transformation name: <b>ED50 to WGS 84 (23)</b></li>
     *   <li>Transformation version: <b>EPSG-Nor N62 2001</b></li>
     *   <li>Operation method name: <b>Position Vector 7-param. transformation</b></li>
     *   <li>EPSG Usage Extent: <b>Norway - offshore north of 62°N; Svalbard</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the transformation from the EPSG code.
     */
    @Test
    @DisplayName("ED50 to WGS 84 (23)")
    public void EPSG_1612() throws FactoryException {
        code       = 1612;
        name       = "ED50 to WGS 84 (23)";
        version    = "EPSG-Nor N62 2001";
        methodName = "Position Vector 7-param. transformation";
        verifyTransformation();
    }

    /**
     * Tests “ED50 to WGS 84 (24)” transformation creation from the factory.
     *
     * <ul>
     *   <li>EPSG transformation code: <b>1613</b></li>
     *   <li>EPSG transformation name: <b>ED50 to WGS 84 (24)</b></li>
     *   <li>Transformation version: <b>EPSG-Nor S62 2001</b></li>
     *   <li>Operation method name: <b>Position Vector 7-param. transformation</b></li>
     *   <li>EPSG Usage Extent: <b>Norway - North Sea - offshore south of 62°N</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the transformation from the EPSG code.
     */
    @Test
    @DisplayName("ED50 to WGS 84 (24)")
    public void EPSG_1613() throws FactoryException {
        code       = 1613;
        name       = "ED50 to WGS 84 (24)";
        version    = "EPSG-Nor S62 2001";
        methodName = "Position Vector 7-param. transformation";
        verifyTransformation();
    }

    /**
     * Tests “ED50 to WGS 84 (32)” transformation creation from the factory.
     *
     * <ul>
     *   <li>EPSG transformation code: <b>1999</b></li>
     *   <li>EPSG transformation name: <b>ED50 to WGS 84 (32)</b></li>
     *   <li>Transformation version: <b>NAM-Nld-Nsea</b></li>
     *   <li>Operation method name: <b>Position Vector 7-param. transformation</b></li>
     *   <li>EPSG Usage Extent: <b>Netherlands - offshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the transformation from the EPSG code.
     */
    @Test
    @DisplayName("ED50 to WGS 84 (32)")
    public void EPSG_1999() throws FactoryException {
        code       = 1999;
        name       = "ED50 to WGS 84 (32)";
        version    = "NAM-Nld-Nsea";
        methodName = "Position Vector 7-param. transformation";
        verifyTransformation();
    }

    /**
     * Tests “ED50 to WGS 84 (36)” transformation creation from the factory.
     *
     * <ul>
     *   <li>EPSG transformation code: <b>1998</b></li>
     *   <li>EPSG transformation name: <b>ED50 to WGS 84 (36)</b></li>
     *   <li>Transformation version: <b>EPSG-Ger Nsea</b></li>
     *   <li>Operation method name: <b>Position Vector 7-param. transformation</b></li>
     *   <li>EPSG Usage Extent: <b>Germany - offshore North Sea</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the transformation from the EPSG code.
     */
    @Test
    @DisplayName("ED50 to WGS 84 (36)")
    public void EPSG_1998() throws FactoryException {
        code       = 1998;
        name       = "ED50 to WGS 84 (36)";
        version    = "EPSG-Ger Nsea";
        methodName = "Position Vector 7-param. transformation";
        verifyTransformation();
    }

    /**
     * Tests “La Canoa to WGS 84 (13)” transformation creation from the factory.
     *
     * <ul>
     *   <li>EPSG transformation code: <b>1096</b></li>
     *   <li>EPSG transformation name: <b>La Canoa to WGS 84 (13)</b></li>
     *   <li>Transformation version: <b>EPSG-Ven</b></li>
     *   <li>Operation method name: <b>Molodensky-Badekas 10-parameter transformation</b></li>
     *   <li>EPSG Usage Extent: <b>Venezuela - onshore</b></li>
     * </ul>
     *
     * Remarks: Identify whether 1095 or 1096 or both are given.
     *
     * @throws FactoryException if an error occurred while creating the transformation from the EPSG code.
     */
    @Test
    @DisplayName("La Canoa to WGS 84 (13)")
    public void EPSG_1096() throws FactoryException {
        code       = 1096;
        name       = "La Canoa to WGS 84 (13)";
        version    = "EPSG-Ven";
        methodName = "Molodensky-Badekas 10-parameter transformation";
        verifyTransformation();
    }

    /**
     * Tests “NAD27 to NAD83 (1)” transformation creation from the factory.
     *
     * <ul>
     *   <li>EPSG transformation code: <b>1241</b></li>
     *   <li>EPSG transformation name: <b>NAD27 to NAD83 (1)</b></li>
     *   <li>Transformation version: <b>NGS-Usa Conus</b></li>
     *   <li>Operation method name: <b>NADCON</b></li>
     *   <li>EPSG Usage Extent: <b>USA - CONUS including EEZ</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the transformation from the EPSG code.
     */
    @Test
    @DisplayName("NAD27 to NAD83 (1)")
    public void EPSG_1241() throws FactoryException {
        code       = 1241;
        name       = "NAD27 to NAD83 (1)";
        version    = "NGS-Usa Conus";
        methodName = "NADCON";
        verifyTransformation();
    }

    /**
     * Tests “NAD27 to NAD83 (2)” transformation creation from the factory.
     *
     * <ul>
     *   <li>EPSG transformation code: <b>1243</b></li>
     *   <li>EPSG transformation name: <b>NAD27 to NAD83 (2)</b></li>
     *   <li>Transformation version: <b>NGS-Usa AK</b></li>
     *   <li>Operation method name: <b>NADCON</b></li>
     *   <li>EPSG Usage Extent: <b>USA - Alaska including EEZ</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the transformation from the EPSG code.
     */
    @Test
    @DisplayName("NAD27 to NAD83 (2)")
    public void EPSG_1243() throws FactoryException {
        code       = 1243;
        name       = "NAD27 to NAD83 (2)";
        version    = "NGS-Usa AK";
        methodName = "NADCON";
        verifyTransformation();
    }

    /**
     * Tests “NAD27 to NAD83 (4)” transformation creation from the factory.
     *
     * <ul>
     *   <li>EPSG transformation code: <b>1313</b></li>
     *   <li>EPSG transformation name: <b>NAD27 to NAD83 (4)</b></li>
     *   <li>Alias(es) given by EPSG: <b>NAD27 to NAD83(Original) [CAv1]</b></li>
     *   <li>Transformation version: <b>GC-Can NT2</b></li>
     *   <li>Operation method name: <b>NTv2</b></li>
     *   <li>EPSG Usage Extent: <b>Canada</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the transformation from the EPSG code.
     */
    @Test
    @DisplayName("NAD27 to NAD83 (4)")
    public void EPSG_1313() throws FactoryException {
        code       = 1313;
        name       = "NAD27 to NAD83 (4)";
        aliases    = new String[] {"NAD27 to NAD83(Original) [CAv1]"};
        version    = "GC-Can NT2";
        methodName = "NTv2";
        verifyTransformation();
    }

    /**
     * Tests “NAD27 to WGS 84 (33)” transformation creation from the factory.
     *
     * <ul>
     *   <li>EPSG transformation code: <b>1693</b></li>
     *   <li>EPSG transformation name: <b>NAD27 to WGS 84 (33)</b></li>
     *   <li>Transformation version: <b>EPSG-Can</b></li>
     *   <li>Operation method name: <b>NTv2</b></li>
     *   <li>EPSG Usage Extent: <b>Canada</b></li>
     * </ul>
     *
     * Remarks: EPSG copy of 1313.
     *
     * @throws FactoryException if an error occurred while creating the transformation from the EPSG code.
     */
    @Test
    @DisplayName("NAD27 to WGS 84 (33)")
    public void EPSG_1693() throws FactoryException {
        code       = 1693;
        name       = "NAD27 to WGS 84 (33)";
        version    = "EPSG-Can";
        methodName = "NTv2";
        verifyTransformation();
    }

    /**
     * Tests “NAD27 to WGS 84 (79)” transformation creation from the factory.
     *
     * <ul>
     *   <li>EPSG transformation code: <b>15851</b></li>
     *   <li>EPSG transformation name: <b>NAD27 to WGS 84 (79)</b></li>
     *   <li>Transformation version: <b>OGP-Usa Conus</b></li>
     *   <li>Operation method name: <b>NADCON</b></li>
     *   <li>EPSG Usage Extent: <b>USA - CONUS including EEZ</b></li>
     * </ul>
     *
     * Remarks: EPSG copy of 1241.
     *
     * @throws FactoryException if an error occurred while creating the transformation from the EPSG code.
     */
    @Test
    @DisplayName("NAD27 to WGS 84 (79)")
    public void EPSG_15851() throws FactoryException {
        code       = 15851;
        name       = "NAD27 to WGS 84 (79)";
        version    = "OGP-Usa Conus";
        methodName = "NADCON";
        verifyTransformation();
    }

    /**
     * Tests “NAD27 to WGS 84 (85)” transformation creation from the factory.
     *
     * <ul>
     *   <li>EPSG transformation code: <b>15864</b></li>
     *   <li>EPSG transformation name: <b>NAD27 to WGS 84 (85)</b></li>
     *   <li>Transformation version: <b>OGP-Usa AK</b></li>
     *   <li>Operation method name: <b>NADCON</b></li>
     *   <li>EPSG Usage Extent: <b>USA - Alaska including EEZ</b></li>
     * </ul>
     *
     * Remarks: EPSG copy of 1243.
     *
     * @throws FactoryException if an error occurred while creating the transformation from the EPSG code.
     */
    @Test
    @DisplayName("NAD27 to WGS 84 (85)")
    public void EPSG_15864() throws FactoryException {
        code       = 15864;
        name       = "NAD27 to WGS 84 (85)";
        version    = "OGP-Usa AK";
        methodName = "NADCON";
        verifyTransformation();
    }

    /**
     * Tests “NTF (Paris) to NTF (1)” transformation creation from the factory.
     *
     * <ul>
     *   <li>EPSG transformation code: <b>1763</b></li>
     *   <li>EPSG transformation name: <b>NTF (Paris) to NTF (1)</b></li>
     *   <li>Transformation version: <b>IGN-Fra</b></li>
     *   <li>Operation method name: <b>Longitude rotation</b></li>
     *   <li>EPSG Usage Extent: <b>France - onshore - mainland and Corsica</b></li>
     * </ul>
     *
     * Remarks: Uses unusual unit (grad) as rotation unit.
     *
     * @throws FactoryException if an error occurred while creating the transformation from the EPSG code.
     */
    @Test
    @DisplayName("NTF (Paris) to NTF (1)")
    public void EPSG_1763() throws FactoryException {
        code       = 1763;
        name       = "NTF (Paris) to NTF (1)";
        version    = "IGN-Fra";
        methodName = "Longitude rotation";
        verifyTransformation();
    }

    /**
     * Tests “PSAD56 to WGS 84 (13)” transformation creation from the factory.
     *
     * <ul>
     *   <li>EPSG transformation code: <b>1095</b></li>
     *   <li>EPSG transformation name: <b>PSAD56 to WGS 84 (13)</b></li>
     *   <li>Transformation version: <b>EPSG-Ven</b></li>
     *   <li>Operation method name: <b>Molodensky-Badekas 10-parameter transformation</b></li>
     *   <li>EPSG Usage Extent: <b>Venezuela - onshore</b></li>
     * </ul>
     *
     * Remarks: Identify whether 1095 or 1096 or both are given.
     *
     * @throws FactoryException if an error occurred while creating the transformation from the EPSG code.
     */
    @Test
    @DisplayName("PSAD56 to WGS 84 (13)")
    public void EPSG_1095() throws FactoryException {
        code       = 1095;
        name       = "PSAD56 to WGS 84 (13)";
        version    = "EPSG-Ven";
        methodName = "Molodensky-Badekas 10-parameter transformation";
        verifyTransformation();
    }
}
