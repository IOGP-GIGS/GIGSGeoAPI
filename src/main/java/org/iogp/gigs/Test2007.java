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
 *   <td><a href="doc-files/GIGS_2007_libGeodTfm.csv">{@code GIGS_2007_libGeodTfm.csv}</a>
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
 * <blockquote><pre>public class MyTest extends Test2007 {
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
public class Test2007 extends Series2000<Transformation> {
    /**
     * Name of the expected transformation method.
     * This field is set by all test methods before to create and verify the {@link Transformation} instance.
     */
    public String methodName;

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
    public Test2007(final CoordinateOperationAuthorityFactory copFactory) {
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

        // Transformation identifier and name.
        assertContainsCode("Transformation.getIdentifiers()", "EPSG", code, transformation.getIdentifiers());
        assertNameEquals(name, transformation, "Transformation.getName()");

        // Operation method.
        final OperationMethod m = transformation.getMethod();
        assertNotNull(m, "Transformation.getMethod()");
        assertNameEquals(methodName, m, "Transformation.getMethod().getName()");
    }

    /**
     * Tests “AGD66 to GDA94 (11)” transformation creation from the factory.
     *
     * <ul>
     *   <li>EPSG transformation code: <b>1803</b></li>
     *   <li>EPSG transformation name: <b>AGD66 to GDA94 (11)</b></li>
     *   <li>Transformation method: <b>NTv2</b></li>
     *   <li>Particularly important to E&amp;P industry.</li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the transformation from the EPSG code.
     */
    @Test
    public void testAGD66_to_GDA94() throws FactoryException {
        important = true;
        name       = "AGD66 to GDA94 (11)";
        methodName = "NTv2";
        verifyTransformation();
    }

    /**
     * Tests “AGD66 to WGS 84 (17)” transformation creation from the factory.
     *
     * <ul>
     *   <li>EPSG transformation code: <b>15786</b></li>
     *   <li>EPSG transformation name: <b>AGD66 to WGS 84 (17)</b></li>
     *   <li>Transformation method: <b>NTv2</b></li>
     *   <li>Specific usage / Remarks: <b>EPSG copy of 1803.</b></li>
     *   <li>Particularly important to E&amp;P industry.</li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the transformation from the EPSG code.
     */
    @Test
    public void testAGD66_to_WGS84() throws FactoryException {
        important = true;
        name       = "AGD66 to WGS 84 (17)";
        methodName = "NTv2";
        verifyTransformation();
    }

    /**
     * Tests “AGD84 to GDA94 (5)” transformation creation from the factory.
     *
     * <ul>
     *   <li>EPSG transformation code: <b>1804</b></li>
     *   <li>EPSG transformation name: <b>AGD84 to GDA94 (5)</b></li>
     *   <li>Transformation method: <b>NTv2</b></li>
     *   <li>Particularly important to E&amp;P industry.</li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the transformation from the EPSG code.
     */
    @Test
    public void testAGD84_to_GDA94() throws FactoryException {
        important = true;
        name       = "AGD84 to GDA94 (5)";
        methodName = "NTv2";
        verifyTransformation();
    }

    /**
     * Tests “AGD84 to WGS 84 (9)” transformation creation from the factory.
     *
     * <ul>
     *   <li>EPSG transformation code: <b>15785</b></li>
     *   <li>EPSG transformation name: <b>AGD84 to WGS 84 (9)</b></li>
     *   <li>Transformation method: <b>NTv2</b></li>
     *   <li>Specific usage / Remarks: <b>EPSG copy of 1804.</b></li>
     *   <li>Particularly important to E&amp;P industry.</li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the transformation from the EPSG code.
     */
    @Test
    public void testAGD84_to_WGS84() throws FactoryException {
        important = true;
        name       = "AGD84 to WGS 84 (9)";
        methodName = "NTv2";
        verifyTransformation();
    }

    /**
     * Tests “Amersfoort to WGS 84 (3)” transformation creation from the factory.
     *
     * <ul>
     *   <li>EPSG transformation code: <b>15934</b></li>
     *   <li>EPSG transformation name: <b>Amersfoort to WGS 84 (3)</b></li>
     *   <li>Transformation method: <b>Coordinate Frame rotation</b></li>
     *   <li>Specific usage / Remarks: <b>Uses unusual unit (microradian) as rotation unit.</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the transformation from the EPSG code.
     */
    @Test
    public void testAmersfoort_to_WGS84() throws FactoryException {
        name       = "Amersfoort to WGS 84 (3)";
        methodName = "Coordinate Frame rotation";
        verifyTransformation();
    }

    /**
     * Tests “Bogota 1975 to MAGNA-SIRGAS (9)” transformation creation from the factory.
     *
     * <ul>
     *   <li>EPSG transformation code: <b>15730</b></li>
     *   <li>EPSG transformation name: <b>Bogota 1975 to MAGNA-SIRGAS (9)</b></li>
     *   <li>Transformation method: <b>Molodensky-Badekas 10-parameter transformation</b></li>
     *   <li>Specific usage / Remarks: <b>Uses unusual unit (radian) as rotation unit.</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the transformation from the EPSG code.
     */
    @Test
    public void testBogota1975_to_MAGNASIRGAS() throws FactoryException {
        name       = "Bogota 1975 to MAGNA-SIRGAS (9)";
        methodName = "Molodensky-Badekas 10-parameter transformation";
        verifyTransformation();
    }

    /**
     * Tests “Bogota 1975 to WGS 84 (3)” transformation creation from the factory.
     *
     * <ul>
     *   <li>EPSG transformation code: <b>15715</b></li>
     *   <li>EPSG transformation name: <b>Bogota 1975 to WGS 84 (3)</b></li>
     *   <li>Transformation method: <b>Coordinate Frame rotation</b></li>
     *   <li>Specific usage / Remarks: <b>Uses unusual unit (radian) as rotation unit.</b></li>
     *   <li>Particularly important to E&amp;P industry.</li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the transformation from the EPSG code.
     */
    @Test
    public void testBogota1975_to_WGS84() throws FactoryException {
        important = true;
        name       = "Bogota 1975 to WGS 84 (3)";
        methodName = "Coordinate Frame rotation";
        verifyTransformation();
    }

    /**
     * Tests “Camacupa to WGS 84 (10)” transformation creation from the factory.
     *
     * <ul>
     *   <li>EPSG transformation code: <b>1327</b></li>
     *   <li>EPSG transformation name: <b>Camacupa to WGS 84 (10)</b></li>
     *   <li>Transformation method: <b>Geocentric translations</b></li>
     *   <li>Particularly important to E&amp;P industry.</li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the transformation from the EPSG code.
     */
    @Test
    public void testCamacupa_to_WGS84() throws FactoryException {
        important = true;
        name       = "Camacupa to WGS 84 (10)";
        methodName = "Geocentric translations";
        verifyTransformation();
    }

    /**
     * Tests “CH1903 to WGS 84 (1)” transformation creation from the factory.
     *
     * <ul>
     *   <li>EPSG transformation code: <b>1753</b></li>
     *   <li>EPSG transformation name: <b>CH1903 to WGS 84 (1)</b></li>
     *   <li>Transformation method: <b>Coordinate Frame rotation</b></li>
     *   <li>Specific usage / Remarks: <b>Uses unusual unit (centesimal second) as rotation unit.</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the transformation from the EPSG code.
     */
    @Test
    public void testCH1903_to_WGS84() throws FactoryException {
        name       = "CH1903 to WGS 84 (1)";
        methodName = "Coordinate Frame rotation";
        verifyTransformation();
    }

    /**
     * Tests “ED50 to WGS 84 (18)” transformation creation from the factory.
     *
     * <ul>
     *   <li>EPSG transformation code: <b>1311</b></li>
     *   <li>EPSG transformation name: <b>ED50 to WGS 84 (18)</b></li>
     *   <li>Transformation method: <b>Position Vector 7-param. transformation</b></li>
     *   <li>Particularly important to E&amp;P industry.</li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the transformation from the EPSG code.
     */
    @Test
    public void testED50_to_WGS84_18() throws FactoryException {
        important = true;
        name       = "ED50 to WGS 84 (18)";
        methodName = "Position Vector 7-param. transformation";
        verifyTransformation();
    }

    /**
     * Tests “ED50 to WGS 84 (23)” transformation creation from the factory.
     *
     * <ul>
     *   <li>EPSG transformation code: <b>1612</b></li>
     *   <li>EPSG transformation name: <b>ED50 to WGS 84 (23)</b></li>
     *   <li>Transformation method: <b>Position Vector 7-param. transformation</b></li>
     *   <li>Particularly important to E&amp;P industry.</li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the transformation from the EPSG code.
     */
    @Test
    public void testED50_to_WGS84_23() throws FactoryException {
        important = true;
        name       = "ED50 to WGS 84 (23)";
        methodName = "Position Vector 7-param. transformation";
        verifyTransformation();
    }

    /**
     * Tests “ED50 to WGS 84 (24)” transformation creation from the factory.
     *
     * <ul>
     *   <li>EPSG transformation code: <b>1613</b></li>
     *   <li>EPSG transformation name: <b>ED50 to WGS 84 (24)</b></li>
     *   <li>Transformation method: <b>Position Vector 7-param. transformation</b></li>
     *   <li>Particularly important to E&amp;P industry.</li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the transformation from the EPSG code.
     */
    @Test
    public void testED50_to_WGS84_24() throws FactoryException {
        important = true;
        name       = "ED50 to WGS 84 (24)";
        methodName = "Position Vector 7-param. transformation";
        verifyTransformation();
    }

    /**
     * Tests “ED50 to WGS 84 (32)” transformation creation from the factory.
     *
     * <ul>
     *   <li>EPSG transformation code: <b>1999</b></li>
     *   <li>EPSG transformation name: <b>ED50 to WGS 84 (32)</b></li>
     *   <li>Transformation method: <b>Position Vector 7-param. transformation</b></li>
     *   <li>Particularly important to E&amp;P industry.</li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the transformation from the EPSG code.
     */
    @Test
    public void testED50_to_WGS84_32() throws FactoryException {
        important = true;
        name       = "ED50 to WGS 84 (32)";
        methodName = "Position Vector 7-param. transformation";
        verifyTransformation();
    }

    /**
     * Tests “ED50 to WGS 84 (36)” transformation creation from the factory.
     *
     * <ul>
     *   <li>EPSG transformation code: <b>1998</b></li>
     *   <li>EPSG transformation name: <b>ED50 to WGS 84 (36)</b></li>
     *   <li>Transformation method: <b>Position Vector 7-param. transformation</b></li>
     *   <li>Particularly important to E&amp;P industry.</li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the transformation from the EPSG code.
     */
    @Test
    public void testED50_to_WGS84_36() throws FactoryException {
        important = true;
        name       = "ED50 to WGS 84 (36)";
        methodName = "Position Vector 7-param. transformation";
        verifyTransformation();
    }

    /**
     * Tests “La Canoa to WGS 84 (2)” transformation creation from the factory.
     *
     * <ul>
     *   <li>EPSG transformation code: <b>1096</b></li>
     *   <li>EPSG transformation name: <b>La Canoa to WGS 84 (2)</b></li>
     *   <li>Transformation method: <b>Molodensky-Badekas 10-parameter transformation</b></li>
     *   <li>Specific usage / Remarks: <b>Identify whether 1095 or 1096 or both are given.</b></li>
     *   <li>Particularly important to E&amp;P industry.</li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the transformation from the EPSG code.
     */
    @Test
    public void testLaCanoa_to_WGS84() throws FactoryException {
        important = true;
        name       = "La Canoa to WGS 84 (2)";
        methodName = "Molodensky-Badekas 10-parameter transformation";
        verifyTransformation();
    }

    /**
     * Tests “NAD27 to NAD83 (1)” transformation creation from the factory.
     *
     * <ul>
     *   <li>EPSG transformation code: <b>1241</b></li>
     *   <li>EPSG transformation name: <b>NAD27 to NAD83 (1)</b></li>
     *   <li>Transformation method: <b>NADCON</b></li>
     *   <li>Particularly important to E&amp;P industry.</li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the transformation from the EPSG code.
     */
    @Test
    public void testNAD27_to_NAD83_1() throws FactoryException {
        important = true;
        name       = "NAD27 to NAD83 (1)";
        methodName = "NADCON";
        verifyTransformation();
    }

    /**
     * Tests “NAD27 to NAD83 (2)” transformation creation from the factory.
     *
     * <ul>
     *   <li>EPSG transformation code: <b>1243</b></li>
     *   <li>EPSG transformation name: <b>NAD27 to NAD83 (2)</b></li>
     *   <li>Transformation method: <b>NADCON</b></li>
     *   <li>Particularly important to E&amp;P industry.</li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the transformation from the EPSG code.
     */
    @Test
    public void testNAD27_to_NAD83_2() throws FactoryException {
        important = true;
        name       = "NAD27 to NAD83 (2)";
        methodName = "NADCON";
        verifyTransformation();
    }

    /**
     * Tests “NAD27 to NAD83 (4)” transformation creation from the factory.
     *
     * <ul>
     *   <li>EPSG transformation code: <b>1313</b></li>
     *   <li>EPSG transformation name: <b>NAD27 to NAD83 (4)</b></li>
     *   <li>Transformation method: <b>NTv2</b></li>
     *   <li>Particularly important to E&amp;P industry.</li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the transformation from the EPSG code.
     */
    @Test
    public void testNAD27_to_NAD83_4() throws FactoryException {
        important = true;
        name       = "NAD27 to NAD83 (4)";
        methodName = "NTv2";
        verifyTransformation();
    }

    /**
     * Tests “NAD27 to WGS 84 (33)” transformation creation from the factory.
     *
     * <ul>
     *   <li>EPSG transformation code: <b>1693</b></li>
     *   <li>EPSG transformation name: <b>NAD27 to WGS 84 (33)</b></li>
     *   <li>Transformation method: <b>NTv2</b></li>
     *   <li>Specific usage / Remarks: <b>EPSG copy of 1313.</b></li>
     *   <li>Particularly important to E&amp;P industry.</li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the transformation from the EPSG code.
     */
    @Test
    public void testNAD27_to_WGS84() throws FactoryException {
        important = true;
        name       = "NAD27 to WGS 84 (33)";
        methodName = "NTv2";
        verifyTransformation();
    }

    /**
     * Tests “NAD27 to WGS 84 (79)” transformation creation from the factory.
     *
     * <ul>
     *   <li>EPSG transformation code: <b>15851</b></li>
     *   <li>EPSG transformation name: <b>NAD27 to WGS 84 (79)</b></li>
     *   <li>Transformation method: <b>NADCON</b></li>
     *   <li>Specific usage / Remarks: <b>EPSG copy of 1241.</b></li>
     *   <li>Particularly important to E&amp;P industry.</li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the transformation from the EPSG code.
     */
    @Test
    public void testNAD27_to_WGS84_79() throws FactoryException {
        important = true;
        name       = "NAD27 to WGS 84 (79)";
        methodName = "NADCON";
        verifyTransformation();
    }

    /**
     * Tests “NAD27 to WGS 84 (85)” transformation creation from the factory.
     *
     * <ul>
     *   <li>EPSG transformation code: <b>15864</b></li>
     *   <li>EPSG transformation name: <b>NAD27 to WGS 84 (85)</b></li>
     *   <li>Transformation method: <b>NADCON</b></li>
     *   <li>Specific usage / Remarks: <b>EPSG copy of 1243.</b></li>
     *   <li>Particularly important to E&amp;P industry.</li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the transformation from the EPSG code.
     */
    @Test
    public void testNAD27_to_WGS84_85() throws FactoryException {
        important = true;
        name       = "NAD27 to WGS 84 (85)";
        methodName = "NADCON";
        verifyTransformation();
    }

    /**
     * Tests “NTF (Paris) to NTF (1)” transformation creation from the factory.
     *
     * <ul>
     *   <li>EPSG transformation code: <b>1763</b></li>
     *   <li>EPSG transformation name: <b>NTF (Paris) to NTF (1)</b></li>
     *   <li>Transformation method: <b>Longitude rotation</b></li>
     *   <li>Specific usage / Remarks: <b>Uses unusual unit (grad) as rotation unit.</b></li>
     *   <li>Particularly important to E&amp;P industry.</li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the transformation from the EPSG code.
     */
    @Test
    public void testNTF_Paris__to_NTF() throws FactoryException {
        important = true;
        name       = "NTF (Paris) to NTF (1)";
        methodName = "Longitude rotation";
        verifyTransformation();
    }

    /**
     * Tests “PSAD56 to WGS 84 (13)” transformation creation from the factory.
     *
     * <ul>
     *   <li>EPSG transformation code: <b>1095</b></li>
     *   <li>EPSG transformation name: <b>PSAD56 to WGS 84 (13)</b></li>
     *   <li>Transformation method: <b>Molodensky-Badekas 10-parameter transformation</b></li>
     *   <li>Specific usage / Remarks: <b>Identify whether 1095 or 1096 or both are given.</b></li>
     *   <li>Particularly important to E&amp;P industry.</li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the transformation from the EPSG code.
     */
    @Test
    public void testPSAD56_to_WGS84() throws FactoryException {
        important = true;
        name       = "PSAD56 to WGS 84 (13)";
        methodName = "Molodensky-Badekas 10-parameter transformation";
        verifyTransformation();
    }
}
