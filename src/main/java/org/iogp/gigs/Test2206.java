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
import org.opengis.referencing.operation.Conversion;
import org.opengis.referencing.operation.CoordinateOperation;
import org.opengis.referencing.operation.CoordinateOperationAuthorityFactory;
import org.iogp.gigs.internal.geoapi.Configuration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Verifies reference map projections bundled with the geoscience software.
 *
 * <table class="gigs">
 * <caption>Test description</caption>
 * <tr>
 *   <th>Test method:</th>
 *   <td>Compare map projection definitions included in the software against the EPSG Dataset.</td>
 * </tr><tr>
 *   <th>Test data:</th>
 *   <td><a href="doc-files/GIGS_2005_libProjection.csv">{@code GIGS_2005_libProjection.csv}</a>
 *       and EPSG Dataset.</td>
 * </tr><tr>
 *   <th>Tested API:</th>
 *   <td>{@link CoordinateOperationAuthorityFactory#createCoordinateOperation(String)}.</td>
 * </tr><tr>
 *   <th>Expected result:</th>
 *   <td>Map projection definitions bundled with the software should have the same name, method name,
 *       defining parameters and parameter values as in the EPSG Dataset. The values of the parameters
 *       should be correct to at least 10 significant figures. Map projections missing from the software
 *       or at variance with those in the EPSG Dataset should be reported.</td>
 * </tr></table>
 *
 *
 * <h2>Usage example</h2>
 * in order to specify their factories and run the tests in a JUnit framework, implementers can
 * define a subclass in their own test suite as in the example below:
 *
 * <blockquote><pre>public class MyTest extends Test2005 {
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
@DisplayName("Conversion")
public class Test2206 extends Series2000<Conversion> {
    /**
     * The name of the expected operation method.
     * This field is set by all test methods before to create and verify the {@link Conversion} instance.
     */
    public String methodName;

    /**
     * The coordinate conversion created by the factory,
     * or {@code null} if not yet created or if the conversion creation failed.
     *
     * @see #copAuthorityFactory
     */
    private Conversion conversion;

    /**
     * Factory to use for building {@link Conversion} instances, or {@code null} if none.
     * This is the factory used by the {@link #getIdentifiedObject()} method.
     */
    protected final CoordinateOperationAuthorityFactory copAuthorityFactory;

    /**
     * Creates a new test using the given factory. If a given factory is {@code null},
     * then the tests which depend on it will be skipped.
     *
     * @param copFactory  factory for creating {@link CoordinateOperation} instances.
     */
    public Test2206(final CoordinateOperationAuthorityFactory copFactory) {
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
     * Returns the coordinate operation instance to be tested. When this method is invoked for the first time, it creates
     * the operation to test by invoking the {@link CoordinateOperationAuthorityFactory#createCoordinateOperation(String)}
     * method with the current {@link #code} value in argument. The created object is then cached and returned in all
     * subsequent invocations of this method.
     *
     * @return the coordinate operation instance to test.
     * @throws FactoryException if an error occurred while creating the coordinate operation instance.
     */
    @Override
    public Conversion getIdentifiedObject() throws FactoryException {
        if (conversion == null) {
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
                unsupportedCode(Conversion.class, code);
                throw e;
            }
            if (operation != null) {            // For consistency with the behavior in other classes.
                assertInstanceOf(Conversion.class, operation, codeAsString);
                conversion = (Conversion) operation;
            }
        }
        return conversion;
    }

    /**
     * Verifies the properties of the conversion given by {@link #getIdentifiedObject()}.
     *
     * @param  code  the authority code of the projection to verify.
     * @throws FactoryException if an error occurred while creating the projection.
     */
    private void createAndVerifyProjection(final int code) throws FactoryException {
        this.code = code;
        conversion = null;              // For forcing the fetch of a new operation.

        final Conversion conversion = getIdentifiedObject();
        assertNotNull(conversion, "Conversion");
        validators.validate(conversion);

        // Map projection identifier and name.
        assertContainsCode("Conversion.getIdentifiers()", "EPSG", code, conversion.getIdentifiers());
        assertNameStartsWith(name, conversion, "Conversion.getName()");

        // Map projection name.
        assertNameEquals(methodName, conversion.getMethod(), "Conversion.getMethod().getName()");
    }

    /**
     * Tests “3-degree Gauss-Kruger” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>various</b></li>
     *   <li>EPSG coordinate operation name: <b>3-degree Gauss-Kruger</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("3-degree Gauss-Kruger")
    public void various3DegreeGaussKruger() throws FactoryException {
        name       = "3-degree Gauss-Kruger";
        methodName = "Transverse Mercator";
        for (int code = 16070; code <= 16089; code++) {         // Loop over 20 codes
            createAndVerifyProjection(code);
        }
        for (int code = 16091; code <= 16094; code++) {         // Loop over 4 codes
            createAndVerifyProjection(code);
        }
        createAndVerifyProjection(16099);
        for (int code = 16170; code <= 16194; code += 2) {      // Loop over 13 codes
            createAndVerifyProjection(code);
        }
        for (int code = 16276; code <= 16299; code++) {         // Loop over 24 codes
            createAndVerifyProjection(code);
        }
        for (int code = 16370; code <= 16398; code += 2) {      // Loop over 15 codes
            createAndVerifyProjection(code);
        }
    }

    /**
     * Tests “3-degree Gauss-Kruger CM 12E” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>16364</b></li>
     *   <li>EPSG coordinate operation name: <b>3-degree Gauss-Kruger CM 12E</b></li>
     *   <li>Alias(es) given by EPSG: <b>3-deg Gauss-Kruger 12E</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>World - N hemisphere - 3-degree CM 012°E</b></li>
     * </ul>
     *
     * Remarks: Without zone prefix in easting.
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("3-degree Gauss-Kruger CM 12E")
    public void EPSG_16364() throws FactoryException {
        name       = "3-degree Gauss-Kruger CM 12E";
        aliases    = new String[] {"3-deg Gauss-Kruger 12E"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(16364);
    }

    /**
     * Tests “3-degree Gauss-Kruger CM 18E” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>16366</b></li>
     *   <li>EPSG coordinate operation name: <b>3-degree Gauss-Kruger CM 18E</b></li>
     *   <li>Alias(es) given by EPSG: <b>3-deg Gauss-Kruger 18E</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>World - N hemisphere - 3-degree CM 018°E</b></li>
     * </ul>
     *
     * Remarks: Without zone prefix in easting.
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("3-degree Gauss-Kruger CM 18E")
    public void EPSG_16366() throws FactoryException {
        name       = "3-degree Gauss-Kruger CM 18E";
        aliases    = new String[] {"3-deg Gauss-Kruger 18E"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(16366);
    }

    /**
     * Tests “3-degree Gauss-Kruger CM 24E” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>16368</b></li>
     *   <li>EPSG coordinate operation name: <b>3-degree Gauss-Kruger CM 24E</b></li>
     *   <li>Alias(es) given by EPSG: <b>3-deg Gauss-Kruger 24E</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>World - N hemisphere - 3-degree CM 024°E</b></li>
     * </ul>
     *
     * Remarks: Without zone prefix in easting.
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("3-degree Gauss-Kruger CM 24E")
    public void EPSG_16368() throws FactoryException {
        name       = "3-degree Gauss-Kruger CM 24E";
        aliases    = new String[] {"3-deg Gauss-Kruger 24E"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(16368);
    }

    /**
     * Tests “3-degree Gauss-Kruger CM 6E” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>16362</b></li>
     *   <li>EPSG coordinate operation name: <b>3-degree Gauss-Kruger CM 6E</b></li>
     *   <li>Alias(es) given by EPSG: <b>3-deg Gauss-Kruger 6E</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>World - N hemisphere - 3-degree CM 006°E</b></li>
     * </ul>
     *
     * Remarks: Without zone prefix in easting.
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("3-degree Gauss-Kruger CM 6E")
    public void EPSG_16362() throws FactoryException {
        name       = "3-degree Gauss-Kruger CM 6E";
        aliases    = new String[] {"3-deg Gauss-Kruger 6E"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(16362);
    }

    /**
     * Tests “3-degree Gauss-Kruger zone 1” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>16261</b></li>
     *   <li>EPSG coordinate operation name: <b>3-degree Gauss-Kruger zone 1</b></li>
     *   <li>Alias(es) given by EPSG: <b>3-degree Gauss zone 1</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>World - N hemisphere - 3-degree CM 003°E</b></li>
     * </ul>
     *
     * Remarks: With zone prefix in easting.
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("3-degree Gauss-Kruger zone 1")
    public void EPSG_16261() throws FactoryException {
        name       = "3-degree Gauss-Kruger zone 1";
        aliases    = new String[] {"3-degree Gauss zone 1"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(16261);
    }

    /**
     * Tests “3-degree Gauss-Kruger zone 10” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>16270</b></li>
     *   <li>EPSG coordinate operation name: <b>3-degree Gauss-Kruger zone 10</b></li>
     *   <li>Alias(es) given by EPSG: <b>3-degree Gauss zone 10</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>World - N hemisphere - 3-degree CM 030°E</b></li>
     * </ul>
     *
     * Remarks: With zone prefix in easting.
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("3-degree Gauss-Kruger zone 10")
    public void EPSG_16270() throws FactoryException {
        name       = "3-degree Gauss-Kruger zone 10";
        aliases    = new String[] {"3-degree Gauss zone 10"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(16270);
    }

    /**
     * Tests “3-degree Gauss-Kruger zone 11” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>16271</b></li>
     *   <li>EPSG coordinate operation name: <b>3-degree Gauss-Kruger zone 11</b></li>
     *   <li>Alias(es) given by EPSG: <b>3-degree Gauss zone 11</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>World - N hemisphere - 3-degree CM 033°E</b></li>
     * </ul>
     *
     * Remarks: With zone prefix in easting.
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("3-degree Gauss-Kruger zone 11")
    public void EPSG_16271() throws FactoryException {
        name       = "3-degree Gauss-Kruger zone 11";
        aliases    = new String[] {"3-degree Gauss zone 11"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(16271);
    }

    /**
     * Tests “3-degree Gauss-Kruger zone 12” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>16272</b></li>
     *   <li>EPSG coordinate operation name: <b>3-degree Gauss-Kruger zone 12</b></li>
     *   <li>Alias(es) given by EPSG: <b>3-degree Gauss zone 12</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>World - N hemisphere - 3-degree CM 036°E</b></li>
     * </ul>
     *
     * Remarks: With zone prefix in easting.
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("3-degree Gauss-Kruger zone 12")
    public void EPSG_16272() throws FactoryException {
        name       = "3-degree Gauss-Kruger zone 12";
        aliases    = new String[] {"3-degree Gauss zone 12"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(16272);
    }

    /**
     * Tests “3-degree Gauss-Kruger zone 13” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>16273</b></li>
     *   <li>EPSG coordinate operation name: <b>3-degree Gauss-Kruger zone 13</b></li>
     *   <li>Alias(es) given by EPSG: <b>3-degree Gauss zone 13</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>World - N hemisphere - 3-degree CM 039°E</b></li>
     * </ul>
     *
     * Remarks: With zone prefix in easting.
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("3-degree Gauss-Kruger zone 13")
    public void EPSG_16273() throws FactoryException {
        name       = "3-degree Gauss-Kruger zone 13";
        aliases    = new String[] {"3-degree Gauss zone 13"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(16273);
    }

    /**
     * Tests “3-degree Gauss-Kruger zone 14” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>16274</b></li>
     *   <li>EPSG coordinate operation name: <b>3-degree Gauss-Kruger zone 14</b></li>
     *   <li>Alias(es) given by EPSG: <b>3-degree Gauss zone 14</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>World - N hemisphere - 3-degree CM 042°E</b></li>
     * </ul>
     *
     * Remarks: With zone prefix in easting.
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("3-degree Gauss-Kruger zone 14")
    public void EPSG_16274() throws FactoryException {
        name       = "3-degree Gauss-Kruger zone 14";
        aliases    = new String[] {"3-degree Gauss zone 14"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(16274);
    }

    /**
     * Tests “3-degree Gauss-Kruger zone 15” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>16275</b></li>
     *   <li>EPSG coordinate operation name: <b>3-degree Gauss-Kruger zone 15</b></li>
     *   <li>Alias(es) given by EPSG: <b>3-degree Gauss zone 15</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>World - N hemisphere - 3-degree CM 045°E</b></li>
     * </ul>
     *
     * Remarks: With zone prefix in easting.
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("3-degree Gauss-Kruger zone 15")
    public void EPSG_16275() throws FactoryException {
        name       = "3-degree Gauss-Kruger zone 15";
        aliases    = new String[] {"3-degree Gauss zone 15"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(16275);
    }

    /**
     * Tests “3-degree Gauss-Kruger zone 2” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>16262</b></li>
     *   <li>EPSG coordinate operation name: <b>3-degree Gauss-Kruger zone 2</b></li>
     *   <li>Alias(es) given by EPSG: <b>3-degree Gauss zone 2</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>World - N hemisphere - 3-degree CM 006°E</b></li>
     * </ul>
     *
     * Remarks: With zone prefix in easting.
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("3-degree Gauss-Kruger zone 2")
    public void EPSG_16262() throws FactoryException {
        name       = "3-degree Gauss-Kruger zone 2";
        aliases    = new String[] {"3-degree Gauss zone 2"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(16262);
    }

    /**
     * Tests “3-degree Gauss-Kruger zone 3” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>16263</b></li>
     *   <li>EPSG coordinate operation name: <b>3-degree Gauss-Kruger zone 3</b></li>
     *   <li>Alias(es) given by EPSG: <b>3-degree Gauss zone 3</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>World - N hemisphere - 3-degree CM 009°E</b></li>
     * </ul>
     *
     * Remarks: With zone prefix in easting.
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("3-degree Gauss-Kruger zone 3")
    public void EPSG_16263() throws FactoryException {
        name       = "3-degree Gauss-Kruger zone 3";
        aliases    = new String[] {"3-degree Gauss zone 3"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(16263);
    }

    /**
     * Tests “3-degree Gauss-Kruger zone 4” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>16264</b></li>
     *   <li>EPSG coordinate operation name: <b>3-degree Gauss-Kruger zone 4</b></li>
     *   <li>Alias(es) given by EPSG: <b>3-degree Gauss zone 4</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>World - N hemisphere - 3-degree CM 012°E</b></li>
     * </ul>
     *
     * Remarks: With zone prefix in easting.
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("3-degree Gauss-Kruger zone 4")
    public void EPSG_16264() throws FactoryException {
        name       = "3-degree Gauss-Kruger zone 4";
        aliases    = new String[] {"3-degree Gauss zone 4"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(16264);
    }

    /**
     * Tests “3-degree Gauss-Kruger zone 5” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>16265</b></li>
     *   <li>EPSG coordinate operation name: <b>3-degree Gauss-Kruger zone 5</b></li>
     *   <li>Alias(es) given by EPSG: <b>3-degree Gauss zone 5</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>World - N hemisphere - 3-degree CM 015°E</b></li>
     * </ul>
     *
     * Remarks: With zone prefix in easting.
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("3-degree Gauss-Kruger zone 5")
    public void EPSG_16265() throws FactoryException {
        name       = "3-degree Gauss-Kruger zone 5";
        aliases    = new String[] {"3-degree Gauss zone 5"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(16265);
    }

    /**
     * Tests “3-degree Gauss-Kruger zone 6” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>16266</b></li>
     *   <li>EPSG coordinate operation name: <b>3-degree Gauss-Kruger zone 6</b></li>
     *   <li>Alias(es) given by EPSG: <b>3-degree Gauss zone 6</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>World - N hemisphere - 3-degree CM 018°E</b></li>
     * </ul>
     *
     * Remarks: With zone prefix in easting.
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("3-degree Gauss-Kruger zone 6")
    public void EPSG_16266() throws FactoryException {
        name       = "3-degree Gauss-Kruger zone 6";
        aliases    = new String[] {"3-degree Gauss zone 6"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(16266);
    }

    /**
     * Tests “3-degree Gauss-Kruger zone 7” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>16267</b></li>
     *   <li>EPSG coordinate operation name: <b>3-degree Gauss-Kruger zone 7</b></li>
     *   <li>Alias(es) given by EPSG: <b>3-degree Gauss zone 7</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>World - N hemisphere - 3-degree CM 021°E</b></li>
     * </ul>
     *
     * Remarks: With zone prefix in easting.
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("3-degree Gauss-Kruger zone 7")
    public void EPSG_16267() throws FactoryException {
        name       = "3-degree Gauss-Kruger zone 7";
        aliases    = new String[] {"3-degree Gauss zone 7"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(16267);
    }

    /**
     * Tests “3-degree Gauss-Kruger zone 8” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>16268</b></li>
     *   <li>EPSG coordinate operation name: <b>3-degree Gauss-Kruger zone 8</b></li>
     *   <li>Alias(es) given by EPSG: <b>3-degree Gauss zone 8</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>World - N hemisphere - 3-degree CM 024°E</b></li>
     * </ul>
     *
     * Remarks: With zone prefix in easting.
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("3-degree Gauss-Kruger zone 8")
    public void EPSG_16268() throws FactoryException {
        name       = "3-degree Gauss-Kruger zone 8";
        aliases    = new String[] {"3-degree Gauss zone 8"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(16268);
    }

    /**
     * Tests “3-degree Gauss-Kruger zone 9” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>16269</b></li>
     *   <li>EPSG coordinate operation name: <b>3-degree Gauss-Kruger zone 9</b></li>
     *   <li>Alias(es) given by EPSG: <b>3-degree Gauss zone 9</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>World - N hemisphere - 3-degree CM 027°E</b></li>
     * </ul>
     *
     * Remarks: With zone prefix in easting.
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("3-degree Gauss-Kruger zone 9")
    public void EPSG_16269() throws FactoryException {
        name       = "3-degree Gauss-Kruger zone 9";
        aliases    = new String[] {"3-degree Gauss zone 9"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(16269);
    }

    /**
     * Tests “6-degree Gauss-Kruger” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>various</b></li>
     *   <li>EPSG coordinate operation name: <b>6-degree Gauss-Kruger</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("6-degree Gauss-Kruger")
    public void various6DegreeGaussKruger() throws FactoryException {
        name       = "6-degree Gauss-Kruger";
        methodName = "Transverse Mercator";
        for (int code = 16201; code <= 16260; code++) {    // Loop over 60 codes
            createAndVerifyProjection(code);
        }
    }

    /**
     * Tests “Alaska CS27 zone 2” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>15002</b></li>
     *   <li>EPSG coordinate operation name: <b>Alaska CS27 zone 2</b></li>
     *   <li>Alias(es) given by EPSG: <b>Alaska zone 2</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>USA - Alaska - 144°W to 141°W</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Alaska CS27 zone 2")
    public void EPSG_15002() throws FactoryException {
        name       = "Alaska CS27 zone 2";
        aliases    = new String[] {"Alaska zone 2"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(15002);
    }

    /**
     * Tests “Alaska CS27 zone 3” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>15003</b></li>
     *   <li>EPSG coordinate operation name: <b>Alaska CS27 zone 3</b></li>
     *   <li>Alias(es) given by EPSG: <b>Alaska zone 3</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>USA - Alaska - 148°W to 144°W</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Alaska CS27 zone 3")
    public void EPSG_15003() throws FactoryException {
        name       = "Alaska CS27 zone 3";
        aliases    = new String[] {"Alaska zone 3"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(15003);
    }

    /**
     * Tests “Alaska CS27 zone 4” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>15004</b></li>
     *   <li>EPSG coordinate operation name: <b>Alaska CS27 zone 4</b></li>
     *   <li>Alias(es) given by EPSG: <b>Alaska zone 4</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>USA - Alaska - 152°W to 148°W</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Alaska CS27 zone 4")
    public void EPSG_15004() throws FactoryException {
        name       = "Alaska CS27 zone 4";
        aliases    = new String[] {"Alaska zone 4"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(15004);
    }

    /**
     * Tests “Alaska CS27 zone 5” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>15005</b></li>
     *   <li>EPSG coordinate operation name: <b>Alaska CS27 zone 5</b></li>
     *   <li>Alias(es) given by EPSG: <b>Alaska zone 5</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>USA - Alaska - 156°W to 152°W</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Alaska CS27 zone 5")
    public void EPSG_15005() throws FactoryException {
        name       = "Alaska CS27 zone 5";
        aliases    = new String[] {"Alaska zone 5"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(15005);
    }

    /**
     * Tests “Alaska CS27 zone 6” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>15006</b></li>
     *   <li>EPSG coordinate operation name: <b>Alaska CS27 zone 6</b></li>
     *   <li>Alias(es) given by EPSG: <b>Alaska zone 6</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>USA - Alaska - 160°W to 156°W</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Alaska CS27 zone 6")
    public void EPSG_15006() throws FactoryException {
        name       = "Alaska CS27 zone 6";
        aliases    = new String[] {"Alaska zone 6"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(15006);
    }

    /**
     * Tests “Alaska CS27 zone 7” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>15007</b></li>
     *   <li>EPSG coordinate operation name: <b>Alaska CS27 zone 7</b></li>
     *   <li>Alias(es) given by EPSG: <b>Alaska zone 7</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>USA - Alaska - 164°W to 160°W</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Alaska CS27 zone 7")
    public void EPSG_15007() throws FactoryException {
        name       = "Alaska CS27 zone 7";
        aliases    = new String[] {"Alaska zone 7"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(15007);
    }

    /**
     * Tests “Alaska CS27 zone 8” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>15008</b></li>
     *   <li>EPSG coordinate operation name: <b>Alaska CS27 zone 8</b></li>
     *   <li>Alias(es) given by EPSG: <b>Alaska zone 8</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>USA - Alaska - north of 54.5°N; 168°W to 164°W</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Alaska CS27 zone 8")
    public void EPSG_15008() throws FactoryException {
        name       = "Alaska CS27 zone 8";
        aliases    = new String[] {"Alaska zone 8"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(15008);
    }

    /**
     * Tests “Alaska CS27 zone 9” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>15009</b></li>
     *   <li>EPSG coordinate operation name: <b>Alaska CS27 zone 9</b></li>
     *   <li>Alias(es) given by EPSG: <b>Alaska zone 9</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>USA - Alaska - north of 54.5°N; west of 168°W</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Alaska CS27 zone 9")
    public void EPSG_15009() throws FactoryException {
        name       = "Alaska CS27 zone 9";
        aliases    = new String[] {"Alaska zone 9"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(15009);
    }

    /**
     * Tests “Aramco Lambert” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>19977</b></li>
     *   <li>EPSG coordinate operation name: <b>Aramco Lambert</b></li>
     *   <li>Coordinate operation method: <b>Lambert Conic Conformal (2SP)</b></li>
     *   <li>EPSG Usage Extent: <b>Saudi Arabia - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Aramco Lambert")
    public void EPSG_19977() throws FactoryException {
        name       = "Aramco Lambert";
        methodName = "Lambert Conic Conformal (2SP)";
        createAndVerifyProjection(19977);
    }

    /**
     * Tests “Argentina zone 1” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>18031</b></li>
     *   <li>EPSG coordinate operation name: <b>Argentina zone 1</b></li>
     *   <li>Alias(es) given by EPSG: <b>Argentina 1</b>, <b>Gauss-Kruger zone 1</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>Argentina - west of 70.5°W</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Argentina zone 1")
    public void EPSG_18031() throws FactoryException {
        name       = "Argentina zone 1";
        aliases    = new String[] {"Argentina 1", "Gauss-Kruger zone 1"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(18031);
    }

    /**
     * Tests “Argentina zone 2” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>18032</b></li>
     *   <li>EPSG coordinate operation name: <b>Argentina zone 2</b></li>
     *   <li>Alias(es) given by EPSG: <b>Argentina 2</b>, <b>Gauss-Kruger zone 2</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>Argentina - 70.5°W to 67.5°W onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Argentina zone 2")
    public void EPSG_18032() throws FactoryException {
        name       = "Argentina zone 2";
        aliases    = new String[] {"Argentina 2", "Gauss-Kruger zone 2"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(18032);
    }

    /**
     * Tests “Argentina zone 3” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>18033</b></li>
     *   <li>EPSG coordinate operation name: <b>Argentina zone 3</b></li>
     *   <li>Alias(es) given by EPSG: <b>Argentina 3</b>, <b>Gauss-Kruger zone 3</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>Argentina - 67.5°W to 64.5°W onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Argentina zone 3")
    public void EPSG_18033() throws FactoryException {
        name       = "Argentina zone 3";
        aliases    = new String[] {"Argentina 3", "Gauss-Kruger zone 3"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(18033);
    }

    /**
     * Tests “Argentina zone 4” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>18034</b></li>
     *   <li>EPSG coordinate operation name: <b>Argentina zone 4</b></li>
     *   <li>Alias(es) given by EPSG: <b>Argentina 4</b>, <b>Gauss-Kruger zone 4</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>Argentina - 64.5°W to 61.5°W onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Argentina zone 4")
    public void EPSG_18034() throws FactoryException {
        name       = "Argentina zone 4";
        aliases    = new String[] {"Argentina 4", "Gauss-Kruger zone 4"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(18034);
    }

    /**
     * Tests “Argentina zone 5” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>18035</b></li>
     *   <li>EPSG coordinate operation name: <b>Argentina zone 5</b></li>
     *   <li>Alias(es) given by EPSG: <b>Argentina 5</b>, <b>Gauss-Kruger zone 5</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>Argentina - 61.5°W to 58.5°W onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Argentina zone 5")
    public void EPSG_18035() throws FactoryException {
        name       = "Argentina zone 5";
        aliases    = new String[] {"Argentina 5", "Gauss-Kruger zone 5"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(18035);
    }

    /**
     * Tests “Argentina zone 6” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>18036</b></li>
     *   <li>EPSG coordinate operation name: <b>Argentina zone 6</b></li>
     *   <li>Alias(es) given by EPSG: <b>Argentina 6</b>, <b>Gauss-Kruger zone 6</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>Argentina - 58.5°W to 55.5°W onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Argentina zone 6")
    public void EPSG_18036() throws FactoryException {
        name       = "Argentina zone 6";
        aliases    = new String[] {"Argentina 6", "Gauss-Kruger zone 6"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(18036);
    }

    /**
     * Tests “Argentina zone 7” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>18037</b></li>
     *   <li>EPSG coordinate operation name: <b>Argentina zone 7</b></li>
     *   <li>Alias(es) given by EPSG: <b>Argentina 7</b>, <b>Gauss-Kruger zone 7</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>Argentina - east of 55.5°W onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Argentina zone 7")
    public void EPSG_18037() throws FactoryException {
        name       = "Argentina zone 7";
        aliases    = new String[] {"Argentina 7", "Gauss-Kruger zone 7"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(18037);
    }

    /**
     * Tests “Australian Map Grid zone 48” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>17448</b></li>
     *   <li>EPSG coordinate operation name: <b>Australian Map Grid zone 48</b></li>
     *   <li>Alias(es) given by EPSG: <b>AMG zone 48</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>Australia - 102°E to 108°E</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Australian Map Grid zone 48")
    public void EPSG_17448() throws FactoryException {
        name       = "Australian Map Grid zone 48";
        aliases    = new String[] {"AMG zone 48"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(17448);
    }

    /**
     * Tests “Australian Map Grid zone 49” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>17449</b></li>
     *   <li>EPSG coordinate operation name: <b>Australian Map Grid zone 49</b></li>
     *   <li>Alias(es) given by EPSG: <b>AMG zone 49</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>Australia - 108°E to 114°E (EEZ)</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Australian Map Grid zone 49")
    public void EPSG_17449() throws FactoryException {
        name       = "Australian Map Grid zone 49";
        aliases    = new String[] {"AMG zone 49"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(17449);
    }

    /**
     * Tests “Australian Map Grid zone 50” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>17450</b></li>
     *   <li>EPSG coordinate operation name: <b>Australian Map Grid zone 50</b></li>
     *   <li>Alias(es) given by EPSG: <b>AMG zone 50</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>Australia - 114°E to 120°E (EEZ)</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Australian Map Grid zone 50")
    public void EPSG_17450() throws FactoryException {
        name       = "Australian Map Grid zone 50";
        aliases    = new String[] {"AMG zone 50"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(17450);
    }

    /**
     * Tests “Australian Map Grid zone 51” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>17451</b></li>
     *   <li>EPSG coordinate operation name: <b>Australian Map Grid zone 51</b></li>
     *   <li>Alias(es) given by EPSG: <b>AMG zone 51</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>Australia - 120°E to 126°E</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Australian Map Grid zone 51")
    public void EPSG_17451() throws FactoryException {
        name       = "Australian Map Grid zone 51";
        aliases    = new String[] {"AMG zone 51"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(17451);
    }

    /**
     * Tests “Australian Map Grid zone 52” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>17452</b></li>
     *   <li>EPSG coordinate operation name: <b>Australian Map Grid zone 52</b></li>
     *   <li>Alias(es) given by EPSG: <b>AMG zone 52</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>Australia - 126°E to 132°E</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Australian Map Grid zone 52")
    public void EPSG_17452() throws FactoryException {
        name       = "Australian Map Grid zone 52";
        aliases    = new String[] {"AMG zone 52"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(17452);
    }

    /**
     * Tests “Australian Map Grid zone 53” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>17453</b></li>
     *   <li>EPSG coordinate operation name: <b>Australian Map Grid zone 53</b></li>
     *   <li>Alias(es) given by EPSG: <b>AMG zone 53</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>Australia - 132°E to 138°E</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Australian Map Grid zone 53")
    public void EPSG_17453() throws FactoryException {
        name       = "Australian Map Grid zone 53";
        aliases    = new String[] {"AMG zone 53"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(17453);
    }

    /**
     * Tests “Australian Map Grid zone 54” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>17454</b></li>
     *   <li>EPSG coordinate operation name: <b>Australian Map Grid zone 54</b></li>
     *   <li>Alias(es) given by EPSG: <b>AMG zone 54</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>Australasia - Australia and PNG - 138°E to 144°E</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Australian Map Grid zone 54")
    public void EPSG_17454() throws FactoryException {
        name       = "Australian Map Grid zone 54";
        aliases    = new String[] {"AMG zone 54"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(17454);
    }

    /**
     * Tests “Australian Map Grid zone 55” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>17455</b></li>
     *   <li>EPSG coordinate operation name: <b>Australian Map Grid zone 55</b></li>
     *   <li>Alias(es) given by EPSG: <b>AMG zone 55</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>Australasia - Australia and PNG - 144°E to 150°E</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Australian Map Grid zone 55")
    public void EPSG_17455() throws FactoryException {
        name       = "Australian Map Grid zone 55";
        aliases    = new String[] {"AMG zone 55"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(17455);
    }

    /**
     * Tests “Australian Map Grid zone 56” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>17456</b></li>
     *   <li>EPSG coordinate operation name: <b>Australian Map Grid zone 56</b></li>
     *   <li>Alias(es) given by EPSG: <b>AMG zone 56</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>Australasia - Australia and PNG - 150°E to 156°E</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Australian Map Grid zone 56")
    public void EPSG_17456() throws FactoryException {
        name       = "Australian Map Grid zone 56";
        aliases    = new String[] {"AMG zone 56"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(17456);
    }

    /**
     * Tests “Australian Map Grid zone 57” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>17457</b></li>
     *   <li>EPSG coordinate operation name: <b>Australian Map Grid zone 57</b></li>
     *   <li>Alias(es) given by EPSG: <b>AMG zone 57</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>Australia - 156°E to 162°E</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Australian Map Grid zone 57")
    public void EPSG_17457() throws FactoryException {
        name       = "Australian Map Grid zone 57";
        aliases    = new String[] {"AMG zone 57"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(17457);
    }

    /**
     * Tests “Australian Map Grid zone 58” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>17458</b></li>
     *   <li>EPSG coordinate operation name: <b>Australian Map Grid zone 58</b></li>
     *   <li>Alias(es) given by EPSG: <b>AMG zone 58</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>Australia - EEZ east of 162°E</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Australian Map Grid zone 58")
    public void EPSG_17458() throws FactoryException {
        name       = "Australian Map Grid zone 58";
        aliases    = new String[] {"AMG zone 58"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(17458);
    }

    /**
     * Tests “BLM zone 14N (US survey feet)” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>15914</b></li>
     *   <li>EPSG coordinate operation name: <b>BLM zone 14N (US survey feet)</b></li>
     *   <li>Alias(es) given by EPSG: <b>BLM 14N (ftUS)</b>, <b>BLM zone 14N in feet</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>USA - 102°W to 96°W and GoM OCS</b></li>
     * </ul>
     *
     * Remarks: UTM in ftUS.
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("BLM zone 14N (US survey feet)")
    public void EPSG_15914() throws FactoryException {
        name       = "BLM zone 14N (US survey feet)";
        aliases    = new String[] {"BLM 14N (ftUS)", "BLM zone 14N in feet"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(15914);
    }

    /**
     * Tests “BLM zone 15N (US survey feet)” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>15915</b></li>
     *   <li>EPSG coordinate operation name: <b>BLM zone 15N (US survey feet)</b></li>
     *   <li>Alias(es) given by EPSG: <b>BLM 15N (ftUS)</b>, <b>BLM zone 15N in feet</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>USA - 96°W to 90°W and GoM OCS</b></li>
     * </ul>
     *
     * Remarks: UTM in ftUS.
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("BLM zone 15N (US survey feet)")
    public void EPSG_15915() throws FactoryException {
        name       = "BLM zone 15N (US survey feet)";
        aliases    = new String[] {"BLM 15N (ftUS)", "BLM zone 15N in feet"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(15915);
    }

    /**
     * Tests “BLM zone 16N (US survey feet)” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>15916</b></li>
     *   <li>EPSG coordinate operation name: <b>BLM zone 16N (US survey feet)</b></li>
     *   <li>Alias(es) given by EPSG: <b>BLM 16N (ftUS)</b>, <b>BLM zone 16N in feet</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>USA - 90°W to 84°W and GoM OCS</b></li>
     * </ul>
     *
     * Remarks: UTM in ftUS.
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("BLM zone 16N (US survey feet)")
    public void EPSG_15916() throws FactoryException {
        name       = "BLM zone 16N (US survey feet)";
        aliases    = new String[] {"BLM 16N (ftUS)", "BLM zone 16N in feet"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(15916);
    }

    /**
     * Tests “BLM zone 17N (US survey feet)” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>15917</b></li>
     *   <li>EPSG coordinate operation name: <b>BLM zone 17N (US survey feet)</b></li>
     *   <li>Alias(es) given by EPSG: <b>BLM 17N (ftUS)</b>, <b>BLM zone 17N in feet</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>USA - 84°W to 78°W and GoM OCS</b></li>
     * </ul>
     *
     * Remarks: UTM in ftUS.
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("BLM zone 17N (US survey feet)")
    public void EPSG_15917() throws FactoryException {
        name       = "BLM zone 17N (US survey feet)";
        aliases    = new String[] {"BLM 17N (ftUS)", "BLM zone 17N in feet"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(15917);
    }

    /**
     * Tests “Borneo RSO” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>19894</b></li>
     *   <li>EPSG coordinate operation name: <b>Borneo RSO</b></li>
     *   <li>Alias(es) given by EPSG: <b>East Malaysia BRSO</b>, <b>Brunei BRSO</b></li>
     *   <li>Coordinate operation method: <b>Hotine Oblique Mercator (variant A)</b></li>
     *   <li>EPSG Usage Extent: <b>Asia - Brunei and East Malaysia</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Borneo RSO")
    public void EPSG_19894() throws FactoryException {
        name       = "Borneo RSO";
        aliases    = new String[] {"East Malaysia BRSO", "Brunei BRSO"};
        methodName = "Hotine Oblique Mercator (variant A)";
        createAndVerifyProjection(19894);
    }

    /**
     * Tests “Brazil Polyconic” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>19941</b></li>
     *   <li>EPSG coordinate operation name: <b>Brazil Polyconic</b></li>
     *   <li>Coordinate operation method: <b>American Polyconic</b></li>
     *   <li>EPSG Usage Extent: <b>Brazil</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Brazil Polyconic")
    public void EPSG_19941() throws FactoryException {
        name       = "Brazil Polyconic";
        methodName = "American Polyconic";
        createAndVerifyProjection(19941);
    }

    /**
     * Tests “British National Grid” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>19916</b></li>
     *   <li>EPSG coordinate operation name: <b>British National Grid</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>UK - Britain and UKCS 49°45'N to 61°N; 9°W to 2°E</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("British National Grid")
    public void EPSG_19916() throws FactoryException {
        name       = "British National Grid";
        methodName = "Transverse Mercator";
        createAndVerifyProjection(19916);
    }

    /**
     * Tests “California CS27 zone IV” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>10404</b></li>
     *   <li>EPSG coordinate operation name: <b>California CS27 zone IV</b></li>
     *   <li>Alias(es) given by EPSG: <b>California zone IV</b></li>
     *   <li>Coordinate operation method: <b>Lambert Conic Conformal (2SP)</b></li>
     *   <li>EPSG Usage Extent: <b>USA - California - SPCS - 4</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("California CS27 zone IV")
    public void EPSG_10404() throws FactoryException {
        name       = "California CS27 zone IV";
        aliases    = new String[] {"California zone IV"};
        methodName = "Lambert Conic Conformal (2SP)";
        createAndVerifyProjection(10404);
    }

    /**
     * Tests “California CS27 zone V” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>10405</b></li>
     *   <li>EPSG coordinate operation name: <b>California CS27 zone V</b></li>
     *   <li>Alias(es) given by EPSG: <b>California zone V</b></li>
     *   <li>Coordinate operation method: <b>Lambert Conic Conformal (2SP)</b></li>
     *   <li>EPSG Usage Extent: <b>USA - California - SPCS27 - 5</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("California CS27 zone V")
    public void EPSG_10405() throws FactoryException {
        name       = "California CS27 zone V";
        aliases    = new String[] {"California zone V"};
        methodName = "Lambert Conic Conformal (2SP)";
        createAndVerifyProjection(10405);
    }

    /**
     * Tests “California CS27 zone VI” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>10406</b></li>
     *   <li>EPSG coordinate operation name: <b>California CS27 zone VI</b></li>
     *   <li>Alias(es) given by EPSG: <b>California zone VI</b></li>
     *   <li>Coordinate operation method: <b>Lambert Conic Conformal (2SP)</b></li>
     *   <li>EPSG Usage Extent: <b>USA - California - SPCS - 6</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("California CS27 zone VI")
    public void EPSG_10406() throws FactoryException {
        name       = "California CS27 zone VI";
        aliases    = new String[] {"California zone VI"};
        methodName = "Lambert Conic Conformal (2SP)";
        createAndVerifyProjection(10406);
    }

    /**
     * Tests “California CS27 zone VII” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>10408</b></li>
     *   <li>EPSG coordinate operation name: <b>California CS27 zone VII</b></li>
     *   <li>Coordinate operation method: <b>Lambert Conic Conformal (2SP)</b></li>
     *   <li>EPSG Usage Extent: <b>USA - California - SPCS27 - 7</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("California CS27 zone VII")
    public void EPSG_10408() throws FactoryException {
        name       = "California CS27 zone VII";
        methodName = "Lambert Conic Conformal (2SP)";
        createAndVerifyProjection(10408);
    }

    /**
     * Tests “Colombia Bogota zone” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>18052</b></li>
     *   <li>EPSG coordinate operation name: <b>Colombia Bogota zone</b></li>
     *   <li>Alias(es) given by EPSG: <b>Colombia Bogota</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>Colombia - 75°35'W to 72°35'W</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Colombia Bogota zone")
    public void EPSG_18052() throws FactoryException {
        name       = "Colombia Bogota zone";
        aliases    = new String[] {"Colombia Bogota"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(18052);
    }

    /**
     * Tests “Colombia East Central zone” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>18053</b></li>
     *   <li>EPSG coordinate operation name: <b>Colombia East Central zone</b></li>
     *   <li>Alias(es) given by EPSG: <b>Colombia 3E</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>Colombia - 72°35'W to 69°35'W</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Colombia East Central zone")
    public void EPSG_18053() throws FactoryException {
        name       = "Colombia East Central zone";
        aliases    = new String[] {"Colombia 3E"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(18053);
    }

    /**
     * Tests “Colombia East zone” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>18054</b></li>
     *   <li>EPSG coordinate operation name: <b>Colombia East zone</b></li>
     *   <li>Alias(es) given by EPSG: <b>Colombia 6E</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>Colombia - east of 69°35'W</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Colombia East zone")
    public void EPSG_18054() throws FactoryException {
        name       = "Colombia East zone";
        aliases    = new String[] {"Colombia 6E"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(18054);
    }

    /**
     * Tests “Colombia MAGNA Bogota zone” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>18057</b></li>
     *   <li>EPSG coordinate operation name: <b>Colombia MAGNA Bogota zone</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>Colombia - 75°35'W to 72°35'W</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Colombia MAGNA Bogota zone")
    public void EPSG_18057() throws FactoryException {
        name       = "Colombia MAGNA Bogota zone";
        methodName = "Transverse Mercator";
        createAndVerifyProjection(18057);
    }

    /**
     * Tests “Colombia MAGNA East Central zone” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>18058</b></li>
     *   <li>EPSG coordinate operation name: <b>Colombia MAGNA East Central zone</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>Colombia - 72°35'W to 69°35'W</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Colombia MAGNA East Central zone")
    public void EPSG_18058() throws FactoryException {
        name       = "Colombia MAGNA East Central zone";
        methodName = "Transverse Mercator";
        createAndVerifyProjection(18058);
    }

    /**
     * Tests “Colombia MAGNA East zone” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>18059</b></li>
     *   <li>EPSG coordinate operation name: <b>Colombia MAGNA East zone</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>Colombia - east of 69°35'W</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Colombia MAGNA East zone")
    public void EPSG_18059() throws FactoryException {
        name       = "Colombia MAGNA East zone";
        methodName = "Transverse Mercator";
        createAndVerifyProjection(18059);
    }

    /**
     * Tests “Colombia MAGNA Far West zone” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>18055</b></li>
     *   <li>EPSG coordinate operation name: <b>Colombia MAGNA Far West zone</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>Colombia - west of 78°35'W</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Colombia MAGNA Far West zone")
    public void EPSG_18055() throws FactoryException {
        name       = "Colombia MAGNA Far West zone";
        methodName = "Transverse Mercator";
        createAndVerifyProjection(18055);
    }

    /**
     * Tests “Colombia MAGNA West zone” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>18056</b></li>
     *   <li>EPSG coordinate operation name: <b>Colombia MAGNA West zone</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>Colombia - 78°35'W to 75°35'W</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Colombia MAGNA West zone")
    public void EPSG_18056() throws FactoryException {
        name       = "Colombia MAGNA West zone";
        methodName = "Transverse Mercator";
        createAndVerifyProjection(18056);
    }

    /**
     * Tests “Colombia West zone” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>18051</b></li>
     *   <li>EPSG coordinate operation name: <b>Colombia West zone</b></li>
     *   <li>Alias(es) given by EPSG: <b>Colombia 3W</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>Colombia - west of 75°35'W</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Colombia West zone")
    public void EPSG_18051() throws FactoryException {
        name       = "Colombia West zone";
        aliases    = new String[] {"Colombia 3W"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(18051);
    }

    /**
     * Tests “Egypt Blue Belt” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>18071</b></li>
     *   <li>EPSG coordinate operation name: <b>Egypt Blue Belt</b></li>
     *   <li>Alias(es) given by EPSG: <b>Blue Belt</b>, <b>Green Belt</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>Egypt - east of 33°E onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Egypt Blue Belt")
    public void EPSG_18071() throws FactoryException {
        name       = "Egypt Blue Belt";
        aliases    = new String[] {"Blue Belt", "Green Belt"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(18071);
    }

    /**
     * Tests “Egypt Extended Purple Belt” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>18074</b></li>
     *   <li>EPSG coordinate operation name: <b>Egypt Extended Purple Belt</b></li>
     *   <li>Alias(es) given by EPSG: <b>Extended Purple Belt</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>Egypt - west of 29°E; south of 28°11'N</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Egypt Extended Purple Belt")
    public void EPSG_18074() throws FactoryException {
        name       = "Egypt Extended Purple Belt";
        aliases    = new String[] {"Extended Purple Belt"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(18074);
    }

    /**
     * Tests “Egypt Purple Belt” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>18073</b></li>
     *   <li>EPSG coordinate operation name: <b>Egypt Purple Belt</b></li>
     *   <li>Alias(es) given by EPSG: <b>Purple Belt</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>Egypt - west of 29°E; north of 28°11'N</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Egypt Purple Belt")
    public void EPSG_18073() throws FactoryException {
        name       = "Egypt Purple Belt";
        aliases    = new String[] {"Purple Belt"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(18073);
    }

    /**
     * Tests “Egypt Red Belt” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>18072</b></li>
     *   <li>EPSG coordinate operation name: <b>Egypt Red Belt</b></li>
     *   <li>Alias(es) given by EPSG: <b>Red Belt</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>Egypt - 29°E to 33°E</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Egypt Red Belt")
    public void EPSG_18072() throws FactoryException {
        name       = "Egypt Red Belt";
        aliases    = new String[] {"Red Belt"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(18072);
    }

    /**
     * Tests “Egyseges Orszagos Vetuleti” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>19931</b></li>
     *   <li>EPSG coordinate operation name: <b>Egyseges Orszagos Vetuleti</b></li>
     *   <li>Alias(es) given by EPSG: <b>EOV</b></li>
     *   <li>Coordinate operation method: <b>Hotine Oblique Mercator (variant B)</b></li>
     *   <li>EPSG Usage Extent: <b>Hungary</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Egyseges Orszagos Vetuleti")
    public void EPSG_19931() throws FactoryException {
        name       = "Egyseges Orszagos Vetuleti";
        aliases    = new String[] {"EOV"};
        methodName = "Hotine Oblique Mercator (variant B)";
        createAndVerifyProjection(19931);
    }

    /**
     * Tests “Gauss-Kruger CM 105E” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>16318</b></li>
     *   <li>EPSG coordinate operation name: <b>Gauss-Kruger CM 105E</b></li>
     *   <li>Alias(es) given by EPSG: <b>6-degree Gauss-Kruger CM 105E</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>World - N hemisphere - 102°E to 108°E</b></li>
     * </ul>
     *
     * Remarks: Without zone prefix in easting.
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Gauss-Kruger CM 105E")
    public void EPSG_16318() throws FactoryException {
        name       = "Gauss-Kruger CM 105E";
        aliases    = new String[] {"6-degree Gauss-Kruger CM 105E"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(16318);
    }

    /**
     * Tests “Gauss-Kruger CM 105W” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>16343</b></li>
     *   <li>EPSG coordinate operation name: <b>Gauss-Kruger CM 105W</b></li>
     *   <li>Alias(es) given by EPSG: <b>6-degree Gauss-Kruger CM 105W</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>World - N hemisphere - 108°W to 102°W</b></li>
     * </ul>
     *
     * Remarks: Without zone prefix in easting.
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Gauss-Kruger CM 105W")
    public void EPSG_16343() throws FactoryException {
        name       = "Gauss-Kruger CM 105W";
        aliases    = new String[] {"6-degree Gauss-Kruger CM 105W"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(16343);
    }

    /**
     * Tests “Gauss-Kruger CM 111E” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>16319</b></li>
     *   <li>EPSG coordinate operation name: <b>Gauss-Kruger CM 111E</b></li>
     *   <li>Alias(es) given by EPSG: <b>6-degree Gauss-Kruger CM 111E</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>World - N hemisphere - 108°E to 114°E</b></li>
     * </ul>
     *
     * Remarks: Without zone prefix in easting.
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Gauss-Kruger CM 111E")
    public void EPSG_16319() throws FactoryException {
        name       = "Gauss-Kruger CM 111E";
        aliases    = new String[] {"6-degree Gauss-Kruger CM 111E"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(16319);
    }

    /**
     * Tests “Gauss-Kruger CM 111W” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>16342</b></li>
     *   <li>EPSG coordinate operation name: <b>Gauss-Kruger CM 111W</b></li>
     *   <li>Alias(es) given by EPSG: <b>6-degree Gauss-Kruger CM 111W</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>World - N hemisphere - 114°W to 108°W</b></li>
     * </ul>
     *
     * Remarks: Without zone prefix in easting.
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Gauss-Kruger CM 111W")
    public void EPSG_16342() throws FactoryException {
        name       = "Gauss-Kruger CM 111W";
        aliases    = new String[] {"6-degree Gauss-Kruger CM 111W"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(16342);
    }

    /**
     * Tests “Gauss-Kruger CM 117E” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>16320</b></li>
     *   <li>EPSG coordinate operation name: <b>Gauss-Kruger CM 117E</b></li>
     *   <li>Alias(es) given by EPSG: <b>6-degree Gauss-Kruger CM 117E</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>World - N hemisphere - 114°E to 120°E</b></li>
     * </ul>
     *
     * Remarks: Without zone prefix in easting.
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Gauss-Kruger CM 117E")
    public void EPSG_16320() throws FactoryException {
        name       = "Gauss-Kruger CM 117E";
        aliases    = new String[] {"6-degree Gauss-Kruger CM 117E"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(16320);
    }

    /**
     * Tests “Gauss-Kruger CM 117W” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>16341</b></li>
     *   <li>EPSG coordinate operation name: <b>Gauss-Kruger CM 117W</b></li>
     *   <li>Alias(es) given by EPSG: <b>6-degree Gauss-Kruger CM 117W</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>World - N hemisphere - 120°W to 114°W</b></li>
     * </ul>
     *
     * Remarks: Without zone prefix in easting.
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Gauss-Kruger CM 117W")
    public void EPSG_16341() throws FactoryException {
        name       = "Gauss-Kruger CM 117W";
        aliases    = new String[] {"6-degree Gauss-Kruger CM 117W"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(16341);
    }

    /**
     * Tests “Gauss-Kruger CM 123E” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>16321</b></li>
     *   <li>EPSG coordinate operation name: <b>Gauss-Kruger CM 123E</b></li>
     *   <li>Alias(es) given by EPSG: <b>6-degree Gauss-Kruger CM 123E</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>World - N hemisphere - 120°E to 126°E</b></li>
     * </ul>
     *
     * Remarks: Without zone prefix in easting.
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Gauss-Kruger CM 123E")
    public void EPSG_16321() throws FactoryException {
        name       = "Gauss-Kruger CM 123E";
        aliases    = new String[] {"6-degree Gauss-Kruger CM 123E"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(16321);
    }

    /**
     * Tests “Gauss-Kruger CM 123W” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>16340</b></li>
     *   <li>EPSG coordinate operation name: <b>Gauss-Kruger CM 123W</b></li>
     *   <li>Alias(es) given by EPSG: <b>6-degree Gauss-Kruger CM 123W</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>World - N hemisphere - 126°W to 120°W</b></li>
     * </ul>
     *
     * Remarks: Without zone prefix in easting.
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Gauss-Kruger CM 123W")
    public void EPSG_16340() throws FactoryException {
        name       = "Gauss-Kruger CM 123W";
        aliases    = new String[] {"6-degree Gauss-Kruger CM 123W"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(16340);
    }

    /**
     * Tests “Gauss-Kruger CM 129E” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>16322</b></li>
     *   <li>EPSG coordinate operation name: <b>Gauss-Kruger CM 129E</b></li>
     *   <li>Alias(es) given by EPSG: <b>6-degree Gauss-Kruger CM 129E</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>World - N hemisphere - 126°E to 132°E</b></li>
     * </ul>
     *
     * Remarks: Without zone prefix in easting.
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Gauss-Kruger CM 129E")
    public void EPSG_16322() throws FactoryException {
        name       = "Gauss-Kruger CM 129E";
        aliases    = new String[] {"6-degree Gauss-Kruger CM 129E"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(16322);
    }

    /**
     * Tests “Gauss-Kruger CM 129W” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>16339</b></li>
     *   <li>EPSG coordinate operation name: <b>Gauss-Kruger CM 129W</b></li>
     *   <li>Alias(es) given by EPSG: <b>6-degree Gauss-Kruger CM 129W</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>World - N hemisphere - 132°W to 126°W</b></li>
     * </ul>
     *
     * Remarks: Without zone prefix in easting.
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Gauss-Kruger CM 129W")
    public void EPSG_16339() throws FactoryException {
        name       = "Gauss-Kruger CM 129W";
        aliases    = new String[] {"6-degree Gauss-Kruger CM 129W"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(16339);
    }

    /**
     * Tests “Gauss-Kruger CM 135E” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>16323</b></li>
     *   <li>EPSG coordinate operation name: <b>Gauss-Kruger CM 135E</b></li>
     *   <li>Alias(es) given by EPSG: <b>6-degree Gauss-Kruger CM 135E</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>World - N hemisphere - 132°E to 138°E</b></li>
     * </ul>
     *
     * Remarks: Without zone prefix in easting.
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Gauss-Kruger CM 135E")
    public void EPSG_16323() throws FactoryException {
        name       = "Gauss-Kruger CM 135E";
        aliases    = new String[] {"6-degree Gauss-Kruger CM 135E"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(16323);
    }

    /**
     * Tests “Gauss-Kruger CM 135W” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>16338</b></li>
     *   <li>EPSG coordinate operation name: <b>Gauss-Kruger CM 135W</b></li>
     *   <li>Alias(es) given by EPSG: <b>6-degree Gauss-Kruger CM 135W</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>World - N hemisphere - 138°W to 132°W</b></li>
     * </ul>
     *
     * Remarks: Without zone prefix in easting.
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Gauss-Kruger CM 135W")
    public void EPSG_16338() throws FactoryException {
        name       = "Gauss-Kruger CM 135W";
        aliases    = new String[] {"6-degree Gauss-Kruger CM 135W"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(16338);
    }

    /**
     * Tests “Gauss-Kruger CM 141E” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>16324</b></li>
     *   <li>EPSG coordinate operation name: <b>Gauss-Kruger CM 141E</b></li>
     *   <li>Alias(es) given by EPSG: <b>6-degree Gauss-Kruger CM 141E</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>World - N hemisphere - 138°E to 144°E</b></li>
     * </ul>
     *
     * Remarks: Without zone prefix in easting.
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Gauss-Kruger CM 141E")
    public void EPSG_16324() throws FactoryException {
        name       = "Gauss-Kruger CM 141E";
        aliases    = new String[] {"6-degree Gauss-Kruger CM 141E"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(16324);
    }

    /**
     * Tests “Gauss-Kruger CM 141W” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>16337</b></li>
     *   <li>EPSG coordinate operation name: <b>Gauss-Kruger CM 141W</b></li>
     *   <li>Alias(es) given by EPSG: <b>6-degree Gauss-Kruger CM 141W</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>World - N hemisphere - 144°W to 138°W</b></li>
     * </ul>
     *
     * Remarks: Without zone prefix in easting.
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Gauss-Kruger CM 141W")
    public void EPSG_16337() throws FactoryException {
        name       = "Gauss-Kruger CM 141W";
        aliases    = new String[] {"6-degree Gauss-Kruger CM 141W"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(16337);
    }

    /**
     * Tests “Gauss-Kruger CM 147E” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>16325</b></li>
     *   <li>EPSG coordinate operation name: <b>Gauss-Kruger CM 147E</b></li>
     *   <li>Alias(es) given by EPSG: <b>6-degree Gauss-Kruger CM 147E</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>World - N hemisphere - 144°E to 150°E</b></li>
     * </ul>
     *
     * Remarks: Without zone prefix in easting.
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Gauss-Kruger CM 147E")
    public void EPSG_16325() throws FactoryException {
        name       = "Gauss-Kruger CM 147E";
        aliases    = new String[] {"6-degree Gauss-Kruger CM 147E"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(16325);
    }

    /**
     * Tests “Gauss-Kruger CM 147W” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>16336</b></li>
     *   <li>EPSG coordinate operation name: <b>Gauss-Kruger CM 147W</b></li>
     *   <li>Alias(es) given by EPSG: <b>6-degree Gauss-Kruger CM 147W</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>World - N hemisphere - 150°W to 144°W</b></li>
     * </ul>
     *
     * Remarks: Without zone prefix in easting.
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Gauss-Kruger CM 147W")
    public void EPSG_16336() throws FactoryException {
        name       = "Gauss-Kruger CM 147W";
        aliases    = new String[] {"6-degree Gauss-Kruger CM 147W"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(16336);
    }

    /**
     * Tests “Gauss-Kruger CM 153E” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>16326</b></li>
     *   <li>EPSG coordinate operation name: <b>Gauss-Kruger CM 153E</b></li>
     *   <li>Alias(es) given by EPSG: <b>6-degree Gauss-Kruger CM 153E</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>World - N hemisphere - 150°E to 156°E</b></li>
     * </ul>
     *
     * Remarks: Without zone prefix in easting.
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Gauss-Kruger CM 153E")
    public void EPSG_16326() throws FactoryException {
        name       = "Gauss-Kruger CM 153E";
        aliases    = new String[] {"6-degree Gauss-Kruger CM 153E"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(16326);
    }

    /**
     * Tests “Gauss-Kruger CM 153W” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>16335</b></li>
     *   <li>EPSG coordinate operation name: <b>Gauss-Kruger CM 153W</b></li>
     *   <li>Alias(es) given by EPSG: <b>6-degree Gauss-Kruger CM 153W</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>World - N hemisphere - 156°W to 150°W</b></li>
     * </ul>
     *
     * Remarks: Without zone prefix in easting.
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Gauss-Kruger CM 153W")
    public void EPSG_16335() throws FactoryException {
        name       = "Gauss-Kruger CM 153W";
        aliases    = new String[] {"6-degree Gauss-Kruger CM 153W"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(16335);
    }

    /**
     * Tests “Gauss-Kruger CM 159E” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>16327</b></li>
     *   <li>EPSG coordinate operation name: <b>Gauss-Kruger CM 159E</b></li>
     *   <li>Alias(es) given by EPSG: <b>6-degree Gauss-Kruger CM 159E</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>World - N hemisphere - 156°E to 162°E</b></li>
     * </ul>
     *
     * Remarks: Without zone prefix in easting.
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Gauss-Kruger CM 159E")
    public void EPSG_16327() throws FactoryException {
        name       = "Gauss-Kruger CM 159E";
        aliases    = new String[] {"6-degree Gauss-Kruger CM 159E"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(16327);
    }

    /**
     * Tests “Gauss-Kruger CM 159W” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>16334</b></li>
     *   <li>EPSG coordinate operation name: <b>Gauss-Kruger CM 159W</b></li>
     *   <li>Alias(es) given by EPSG: <b>6-degree Gauss-Kruger CM 159W</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>World - N hemisphere - 162°W to 156°W</b></li>
     * </ul>
     *
     * Remarks: Without zone prefix in easting.
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Gauss-Kruger CM 159W")
    public void EPSG_16334() throws FactoryException {
        name       = "Gauss-Kruger CM 159W";
        aliases    = new String[] {"6-degree Gauss-Kruger CM 159W"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(16334);
    }

    /**
     * Tests “Gauss-Kruger CM 15E” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>16303</b></li>
     *   <li>EPSG coordinate operation name: <b>Gauss-Kruger CM 15E</b></li>
     *   <li>Alias(es) given by EPSG: <b>6-degree Gauss-Kruger CM 15E</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>World - N hemisphere - 12°E to 18°E</b></li>
     * </ul>
     *
     * Remarks: Without zone prefix in easting.
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Gauss-Kruger CM 15E")
    public void EPSG_16303() throws FactoryException {
        name       = "Gauss-Kruger CM 15E";
        aliases    = new String[] {"6-degree Gauss-Kruger CM 15E"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(16303);
    }

    /**
     * Tests “Gauss-Kruger CM 15W” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>16358</b></li>
     *   <li>EPSG coordinate operation name: <b>Gauss-Kruger CM 15W</b></li>
     *   <li>Alias(es) given by EPSG: <b>6-degree Gauss-Kruger CM 15W</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>World - N hemisphere - 18°W to 12°W</b></li>
     * </ul>
     *
     * Remarks: Without zone prefix in easting.
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Gauss-Kruger CM 15W")
    public void EPSG_16358() throws FactoryException {
        name       = "Gauss-Kruger CM 15W";
        aliases    = new String[] {"6-degree Gauss-Kruger CM 15W"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(16358);
    }

    /**
     * Tests “Gauss-Kruger CM 165E” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>16328</b></li>
     *   <li>EPSG coordinate operation name: <b>Gauss-Kruger CM 165E</b></li>
     *   <li>Alias(es) given by EPSG: <b>6-degree Gauss-Kruger CM 165E</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>World - N hemisphere - 162°E to 168°E</b></li>
     * </ul>
     *
     * Remarks: Without zone prefix in easting.
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Gauss-Kruger CM 165E")
    public void EPSG_16328() throws FactoryException {
        name       = "Gauss-Kruger CM 165E";
        aliases    = new String[] {"6-degree Gauss-Kruger CM 165E"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(16328);
    }

    /**
     * Tests “Gauss-Kruger CM 165W” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>16333</b></li>
     *   <li>EPSG coordinate operation name: <b>Gauss-Kruger CM 165W</b></li>
     *   <li>Alias(es) given by EPSG: <b>6-degree Gauss-Kruger CM 165W</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>World - N hemisphere - 168°W to 162°W</b></li>
     * </ul>
     *
     * Remarks: Without zone prefix in easting.
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Gauss-Kruger CM 165W")
    public void EPSG_16333() throws FactoryException {
        name       = "Gauss-Kruger CM 165W";
        aliases    = new String[] {"6-degree Gauss-Kruger CM 165W"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(16333);
    }

    /**
     * Tests “Gauss-Kruger CM 171E” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>16329</b></li>
     *   <li>EPSG coordinate operation name: <b>Gauss-Kruger CM 171E</b></li>
     *   <li>Alias(es) given by EPSG: <b>6-degree Gauss-Kruger CM 171E</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>World - N hemisphere - 168°E to 174°E</b></li>
     * </ul>
     *
     * Remarks: Without zone prefix in easting.
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Gauss-Kruger CM 171E")
    public void EPSG_16329() throws FactoryException {
        name       = "Gauss-Kruger CM 171E";
        aliases    = new String[] {"6-degree Gauss-Kruger CM 171E"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(16329);
    }

    /**
     * Tests “Gauss-Kruger CM 171W” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>16332</b></li>
     *   <li>EPSG coordinate operation name: <b>Gauss-Kruger CM 171W</b></li>
     *   <li>Alias(es) given by EPSG: <b>6-degree Gauss-Kruger CM 171W</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>World - N hemisphere - 174°W to 168°W</b></li>
     * </ul>
     *
     * Remarks: Without zone prefix in easting.
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Gauss-Kruger CM 171W")
    public void EPSG_16332() throws FactoryException {
        name       = "Gauss-Kruger CM 171W";
        aliases    = new String[] {"6-degree Gauss-Kruger CM 171W"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(16332);
    }

    /**
     * Tests “Gauss-Kruger CM 177E” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>16330</b></li>
     *   <li>EPSG coordinate operation name: <b>Gauss-Kruger CM 177E</b></li>
     *   <li>Alias(es) given by EPSG: <b>6-degree Gauss-Kruger CM 177E</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>World - N hemisphere - 174°E to 180°E</b></li>
     * </ul>
     *
     * Remarks: Without zone prefix in easting.
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Gauss-Kruger CM 177E")
    public void EPSG_16330() throws FactoryException {
        name       = "Gauss-Kruger CM 177E";
        aliases    = new String[] {"6-degree Gauss-Kruger CM 177E"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(16330);
    }

    /**
     * Tests “Gauss-Kruger CM 177W” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>16331</b></li>
     *   <li>EPSG coordinate operation name: <b>Gauss-Kruger CM 177W</b></li>
     *   <li>Alias(es) given by EPSG: <b>6-degree Gauss-Kruger CM 177W</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>World - N hemisphere - 180°W to 174°W</b></li>
     * </ul>
     *
     * Remarks: Without zone prefix in easting.
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Gauss-Kruger CM 177W")
    public void EPSG_16331() throws FactoryException {
        name       = "Gauss-Kruger CM 177W";
        aliases    = new String[] {"6-degree Gauss-Kruger CM 177W"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(16331);
    }

    /**
     * Tests “Gauss-Kruger CM 21E” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>16304</b></li>
     *   <li>EPSG coordinate operation name: <b>Gauss-Kruger CM 21E</b></li>
     *   <li>Alias(es) given by EPSG: <b>6-degree Gauss-Kruger CM 21E</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>World - N hemisphere - 18°E to 24°E</b></li>
     * </ul>
     *
     * Remarks: Without zone prefix in easting.
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Gauss-Kruger CM 21E")
    public void EPSG_16304() throws FactoryException {
        name       = "Gauss-Kruger CM 21E";
        aliases    = new String[] {"6-degree Gauss-Kruger CM 21E"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(16304);
    }

    /**
     * Tests “Gauss-Kruger CM 21W” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>16357</b></li>
     *   <li>EPSG coordinate operation name: <b>Gauss-Kruger CM 21W</b></li>
     *   <li>Alias(es) given by EPSG: <b>6-degree Gauss-Kruger CM 21W</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>World - N hemisphere - 24°W to 18°W</b></li>
     * </ul>
     *
     * Remarks: Without zone prefix in easting.
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Gauss-Kruger CM 21W")
    public void EPSG_16357() throws FactoryException {
        name       = "Gauss-Kruger CM 21W";
        aliases    = new String[] {"6-degree Gauss-Kruger CM 21W"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(16357);
    }

    /**
     * Tests “Gauss-Kruger CM 27E” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>16305</b></li>
     *   <li>EPSG coordinate operation name: <b>Gauss-Kruger CM 27E</b></li>
     *   <li>Alias(es) given by EPSG: <b>6-degree Gauss-Kruger CM 27E</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>World - N hemisphere - 24°E to 30°E</b></li>
     * </ul>
     *
     * Remarks: Without zone prefix in easting.
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Gauss-Kruger CM 27E")
    public void EPSG_16305() throws FactoryException {
        name       = "Gauss-Kruger CM 27E";
        aliases    = new String[] {"6-degree Gauss-Kruger CM 27E"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(16305);
    }

    /**
     * Tests “Gauss-Kruger CM 27W” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>16356</b></li>
     *   <li>EPSG coordinate operation name: <b>Gauss-Kruger CM 27W</b></li>
     *   <li>Alias(es) given by EPSG: <b>6-degree Gauss-Kruger CM 27W</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>World - N hemisphere - 30°W to 24°W</b></li>
     * </ul>
     *
     * Remarks: Without zone prefix in easting.
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Gauss-Kruger CM 27W")
    public void EPSG_16356() throws FactoryException {
        name       = "Gauss-Kruger CM 27W";
        aliases    = new String[] {"6-degree Gauss-Kruger CM 27W"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(16356);
    }

    /**
     * Tests “Gauss-Kruger CM 33E” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>16306</b></li>
     *   <li>EPSG coordinate operation name: <b>Gauss-Kruger CM 33E</b></li>
     *   <li>Alias(es) given by EPSG: <b>6-degree Gauss-Kruger CM 33E</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>World - N hemisphere - 30°E to 36°E</b></li>
     * </ul>
     *
     * Remarks: Without zone prefix in easting.
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Gauss-Kruger CM 33E")
    public void EPSG_16306() throws FactoryException {
        name       = "Gauss-Kruger CM 33E";
        aliases    = new String[] {"6-degree Gauss-Kruger CM 33E"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(16306);
    }

    /**
     * Tests “Gauss-Kruger CM 33W” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>16355</b></li>
     *   <li>EPSG coordinate operation name: <b>Gauss-Kruger CM 33W</b></li>
     *   <li>Alias(es) given by EPSG: <b>6-degree Gauss-Kruger CM 33W</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>World - N hemisphere - 36°W to 30°W</b></li>
     * </ul>
     *
     * Remarks: Without zone prefix in easting.
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Gauss-Kruger CM 33W")
    public void EPSG_16355() throws FactoryException {
        name       = "Gauss-Kruger CM 33W";
        aliases    = new String[] {"6-degree Gauss-Kruger CM 33W"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(16355);
    }

    /**
     * Tests “Gauss-Kruger CM 39E” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>16307</b></li>
     *   <li>EPSG coordinate operation name: <b>Gauss-Kruger CM 39E</b></li>
     *   <li>Alias(es) given by EPSG: <b>6-degree Gauss-Kruger CM 39E</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>World - N hemisphere - 36°E to 42°E</b></li>
     * </ul>
     *
     * Remarks: Without zone prefix in easting.
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Gauss-Kruger CM 39E")
    public void EPSG_16307() throws FactoryException {
        name       = "Gauss-Kruger CM 39E";
        aliases    = new String[] {"6-degree Gauss-Kruger CM 39E"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(16307);
    }

    /**
     * Tests “Gauss-Kruger CM 39W” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>16354</b></li>
     *   <li>EPSG coordinate operation name: <b>Gauss-Kruger CM 39W</b></li>
     *   <li>Alias(es) given by EPSG: <b>6-degree Gauss-Kruger CM 39W</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>World - N hemisphere - 42°W to 36°W</b></li>
     * </ul>
     *
     * Remarks: Without zone prefix in easting.
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Gauss-Kruger CM 39W")
    public void EPSG_16354() throws FactoryException {
        name       = "Gauss-Kruger CM 39W";
        aliases    = new String[] {"6-degree Gauss-Kruger CM 39W"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(16354);
    }

    /**
     * Tests “Gauss-Kruger CM 3E” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>16301</b></li>
     *   <li>EPSG coordinate operation name: <b>Gauss-Kruger CM 3E</b></li>
     *   <li>Alias(es) given by EPSG: <b>6-degree Gauss-Kruger CM 3E</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>World - N hemisphere - 0°E to 6°E</b></li>
     * </ul>
     *
     * Remarks: Without zone prefix in easting.
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Gauss-Kruger CM 3E")
    public void EPSG_16301() throws FactoryException {
        name       = "Gauss-Kruger CM 3E";
        aliases    = new String[] {"6-degree Gauss-Kruger CM 3E"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(16301);
    }

    /**
     * Tests “Gauss-Kruger CM 3W” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>16360</b></li>
     *   <li>EPSG coordinate operation name: <b>Gauss-Kruger CM 3W</b></li>
     *   <li>Alias(es) given by EPSG: <b>6-degree Gauss-Kruger CM 3W</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>World - N hemisphere - 6°W to 0°W</b></li>
     * </ul>
     *
     * Remarks: Without zone prefix in easting.
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Gauss-Kruger CM 3W")
    public void EPSG_16360() throws FactoryException {
        name       = "Gauss-Kruger CM 3W";
        aliases    = new String[] {"6-degree Gauss-Kruger CM 3W"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(16360);
    }

    /**
     * Tests “Gauss-Kruger CM 45E” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>16308</b></li>
     *   <li>EPSG coordinate operation name: <b>Gauss-Kruger CM 45E</b></li>
     *   <li>Alias(es) given by EPSG: <b>6-degree Gauss-Kruger CM 45E</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>World - N hemisphere - 42°E to 48°E</b></li>
     * </ul>
     *
     * Remarks: Without zone prefix in easting.
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Gauss-Kruger CM 45E")
    public void EPSG_16308() throws FactoryException {
        name       = "Gauss-Kruger CM 45E";
        aliases    = new String[] {"6-degree Gauss-Kruger CM 45E"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(16308);
    }

    /**
     * Tests “Gauss-Kruger CM 45W” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>16353</b></li>
     *   <li>EPSG coordinate operation name: <b>Gauss-Kruger CM 45W</b></li>
     *   <li>Alias(es) given by EPSG: <b>6-degree Gauss-Kruger CM 45W</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>World - N hemisphere - 48°W to 42°W</b></li>
     * </ul>
     *
     * Remarks: Without zone prefix in easting.
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Gauss-Kruger CM 45W")
    public void EPSG_16353() throws FactoryException {
        name       = "Gauss-Kruger CM 45W";
        aliases    = new String[] {"6-degree Gauss-Kruger CM 45W"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(16353);
    }

    /**
     * Tests “Gauss-Kruger CM 51E” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>16309</b></li>
     *   <li>EPSG coordinate operation name: <b>Gauss-Kruger CM 51E</b></li>
     *   <li>Alias(es) given by EPSG: <b>6-degree Gauss-Kruger CM 51E</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>World - N hemisphere - 48°E to 54°E</b></li>
     * </ul>
     *
     * Remarks: Without zone prefix in easting.
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Gauss-Kruger CM 51E")
    public void EPSG_16309() throws FactoryException {
        name       = "Gauss-Kruger CM 51E";
        aliases    = new String[] {"6-degree Gauss-Kruger CM 51E"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(16309);
    }

    /**
     * Tests “Gauss-Kruger CM 51W” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>16352</b></li>
     *   <li>EPSG coordinate operation name: <b>Gauss-Kruger CM 51W</b></li>
     *   <li>Alias(es) given by EPSG: <b>6-degree Gauss-Kruger CM 51W</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>World - N hemisphere - 54°W to 48°W</b></li>
     * </ul>
     *
     * Remarks: Without zone prefix in easting.
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Gauss-Kruger CM 51W")
    public void EPSG_16352() throws FactoryException {
        name       = "Gauss-Kruger CM 51W";
        aliases    = new String[] {"6-degree Gauss-Kruger CM 51W"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(16352);
    }

    /**
     * Tests “Gauss-Kruger CM 57E” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>16310</b></li>
     *   <li>EPSG coordinate operation name: <b>Gauss-Kruger CM 57E</b></li>
     *   <li>Alias(es) given by EPSG: <b>6-degree Gauss-Kruger CM 57E</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>World - N hemisphere - 54°E to 60°E</b></li>
     * </ul>
     *
     * Remarks: Without zone prefix in easting.
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Gauss-Kruger CM 57E")
    public void EPSG_16310() throws FactoryException {
        name       = "Gauss-Kruger CM 57E";
        aliases    = new String[] {"6-degree Gauss-Kruger CM 57E"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(16310);
    }

    /**
     * Tests “Gauss-Kruger CM 57W” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>16351</b></li>
     *   <li>EPSG coordinate operation name: <b>Gauss-Kruger CM 57W</b></li>
     *   <li>Alias(es) given by EPSG: <b>6-degree Gauss-Kruger CM 57W</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>World - N hemisphere - 60°W to 54°W</b></li>
     * </ul>
     *
     * Remarks: Without zone prefix in easting.
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Gauss-Kruger CM 57W")
    public void EPSG_16351() throws FactoryException {
        name       = "Gauss-Kruger CM 57W";
        aliases    = new String[] {"6-degree Gauss-Kruger CM 57W"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(16351);
    }

    /**
     * Tests “Gauss-Kruger CM 63E” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>16311</b></li>
     *   <li>EPSG coordinate operation name: <b>Gauss-Kruger CM 63E</b></li>
     *   <li>Alias(es) given by EPSG: <b>6-degree Gauss-Kruger CM 63E</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>World - N hemisphere - 60°E to 66°E</b></li>
     * </ul>
     *
     * Remarks: Without zone prefix in easting.
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Gauss-Kruger CM 63E")
    public void EPSG_16311() throws FactoryException {
        name       = "Gauss-Kruger CM 63E";
        aliases    = new String[] {"6-degree Gauss-Kruger CM 63E"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(16311);
    }

    /**
     * Tests “Gauss-Kruger CM 63W” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>16350</b></li>
     *   <li>EPSG coordinate operation name: <b>Gauss-Kruger CM 63W</b></li>
     *   <li>Alias(es) given by EPSG: <b>6-degree Gauss-Kruger CM 63W</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>World - N hemisphere - 66°W to 60°W</b></li>
     * </ul>
     *
     * Remarks: Without zone prefix in easting.
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Gauss-Kruger CM 63W")
    public void EPSG_16350() throws FactoryException {
        name       = "Gauss-Kruger CM 63W";
        aliases    = new String[] {"6-degree Gauss-Kruger CM 63W"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(16350);
    }

    /**
     * Tests “Gauss-Kruger CM 69E” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>16312</b></li>
     *   <li>EPSG coordinate operation name: <b>Gauss-Kruger CM 69E</b></li>
     *   <li>Alias(es) given by EPSG: <b>6-degree Gauss-Kruger CM 69E</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>World - N hemisphere - 66°E to 72°E</b></li>
     * </ul>
     *
     * Remarks: Without zone prefix in easting.
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Gauss-Kruger CM 69E")
    public void EPSG_16312() throws FactoryException {
        name       = "Gauss-Kruger CM 69E";
        aliases    = new String[] {"6-degree Gauss-Kruger CM 69E"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(16312);
    }

    /**
     * Tests “Gauss-Kruger CM 69W” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>16349</b></li>
     *   <li>EPSG coordinate operation name: <b>Gauss-Kruger CM 69W</b></li>
     *   <li>Alias(es) given by EPSG: <b>6-degree Gauss-Kruger CM 69W</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>World - N hemisphere - 72°W to 66°W</b></li>
     * </ul>
     *
     * Remarks: Without zone prefix in easting.
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Gauss-Kruger CM 69W")
    public void EPSG_16349() throws FactoryException {
        name       = "Gauss-Kruger CM 69W";
        aliases    = new String[] {"6-degree Gauss-Kruger CM 69W"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(16349);
    }

    /**
     * Tests “Gauss-Kruger CM 75E” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>16313</b></li>
     *   <li>EPSG coordinate operation name: <b>Gauss-Kruger CM 75E</b></li>
     *   <li>Alias(es) given by EPSG: <b>6-degree Gauss-Kruger CM 75E</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>World - N hemisphere - 72°E to 78°E</b></li>
     * </ul>
     *
     * Remarks: Without zone prefix in easting.
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Gauss-Kruger CM 75E")
    public void EPSG_16313() throws FactoryException {
        name       = "Gauss-Kruger CM 75E";
        aliases    = new String[] {"6-degree Gauss-Kruger CM 75E"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(16313);
    }

    /**
     * Tests “Gauss-Kruger CM 75W” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>16348</b></li>
     *   <li>EPSG coordinate operation name: <b>Gauss-Kruger CM 75W</b></li>
     *   <li>Alias(es) given by EPSG: <b>6-degree Gauss-Kruger CM 75W</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>World - N hemisphere - 78°W to 72°W</b></li>
     * </ul>
     *
     * Remarks: Without zone prefix in easting.
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Gauss-Kruger CM 75W")
    public void EPSG_16348() throws FactoryException {
        name       = "Gauss-Kruger CM 75W";
        aliases    = new String[] {"6-degree Gauss-Kruger CM 75W"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(16348);
    }

    /**
     * Tests “Gauss-Kruger CM 81E” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>16314</b></li>
     *   <li>EPSG coordinate operation name: <b>Gauss-Kruger CM 81E</b></li>
     *   <li>Alias(es) given by EPSG: <b>6-degree Gauss-Kruger CM 81E</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>World - N hemisphere - 78°E to 84°E</b></li>
     * </ul>
     *
     * Remarks: Without zone prefix in easting.
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Gauss-Kruger CM 81E")
    public void EPSG_16314() throws FactoryException {
        name       = "Gauss-Kruger CM 81E";
        aliases    = new String[] {"6-degree Gauss-Kruger CM 81E"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(16314);
    }

    /**
     * Tests “Gauss-Kruger CM 81W” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>16347</b></li>
     *   <li>EPSG coordinate operation name: <b>Gauss-Kruger CM 81W</b></li>
     *   <li>Alias(es) given by EPSG: <b>6-degree Gauss-Kruger CM 81W</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>World - N hemisphere - 84°W to 78°W</b></li>
     * </ul>
     *
     * Remarks: Without zone prefix in easting.
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Gauss-Kruger CM 81W")
    public void EPSG_16347() throws FactoryException {
        name       = "Gauss-Kruger CM 81W";
        aliases    = new String[] {"6-degree Gauss-Kruger CM 81W"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(16347);
    }

    /**
     * Tests “Gauss-Kruger CM 87E” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>16315</b></li>
     *   <li>EPSG coordinate operation name: <b>Gauss-Kruger CM 87E</b></li>
     *   <li>Alias(es) given by EPSG: <b>6-degree Gauss-Kruger CM 87E</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>World - N hemisphere - 84°E to 90°E</b></li>
     * </ul>
     *
     * Remarks: Without zone prefix in easting.
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Gauss-Kruger CM 87E")
    public void EPSG_16315() throws FactoryException {
        name       = "Gauss-Kruger CM 87E";
        aliases    = new String[] {"6-degree Gauss-Kruger CM 87E"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(16315);
    }

    /**
     * Tests “Gauss-Kruger CM 87W” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>16346</b></li>
     *   <li>EPSG coordinate operation name: <b>Gauss-Kruger CM 87W</b></li>
     *   <li>Alias(es) given by EPSG: <b>6-degree Gauss-Kruger CM 87W</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>World - N hemisphere - 90°W to 84°W</b></li>
     * </ul>
     *
     * Remarks: Without zone prefix in easting.
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Gauss-Kruger CM 87W")
    public void EPSG_16346() throws FactoryException {
        name       = "Gauss-Kruger CM 87W";
        aliases    = new String[] {"6-degree Gauss-Kruger CM 87W"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(16346);
    }

    /**
     * Tests “Gauss-Kruger CM 93E” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>16316</b></li>
     *   <li>EPSG coordinate operation name: <b>Gauss-Kruger CM 93E</b></li>
     *   <li>Alias(es) given by EPSG: <b>6-degree Gauss-Kruger CM 93E</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>World - N hemisphere - 90°E to 96°E</b></li>
     * </ul>
     *
     * Remarks: Without zone prefix in easting.
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Gauss-Kruger CM 93E")
    public void EPSG_16316() throws FactoryException {
        name       = "Gauss-Kruger CM 93E";
        aliases    = new String[] {"6-degree Gauss-Kruger CM 93E"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(16316);
    }

    /**
     * Tests “Gauss-Kruger CM 93W” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>16345</b></li>
     *   <li>EPSG coordinate operation name: <b>Gauss-Kruger CM 93W</b></li>
     *   <li>Alias(es) given by EPSG: <b>6-degree Gauss-Kruger CM 93W</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>World - N hemisphere - 96°W to 90°W</b></li>
     * </ul>
     *
     * Remarks: Without zone prefix in easting.
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Gauss-Kruger CM 93W")
    public void EPSG_16345() throws FactoryException {
        name       = "Gauss-Kruger CM 93W";
        aliases    = new String[] {"6-degree Gauss-Kruger CM 93W"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(16345);
    }

    /**
     * Tests “Gauss-Kruger CM 99E” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>16317</b></li>
     *   <li>EPSG coordinate operation name: <b>Gauss-Kruger CM 99E</b></li>
     *   <li>Alias(es) given by EPSG: <b>6-degree Gauss-Kruger CM 99E</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>World - N hemisphere - 96°E to 102°E</b></li>
     * </ul>
     *
     * Remarks: Without zone prefix in easting.
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Gauss-Kruger CM 99E")
    public void EPSG_16317() throws FactoryException {
        name       = "Gauss-Kruger CM 99E";
        aliases    = new String[] {"6-degree Gauss-Kruger CM 99E"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(16317);
    }

    /**
     * Tests “Gauss-Kruger CM 99W” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>16344</b></li>
     *   <li>EPSG coordinate operation name: <b>Gauss-Kruger CM 99W</b></li>
     *   <li>Alias(es) given by EPSG: <b>6-degree Gauss-Kruger CM 99W</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>World - N hemisphere - 102°W to 96°W</b></li>
     * </ul>
     *
     * Remarks: Without zone prefix in easting.
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Gauss-Kruger CM 99W")
    public void EPSG_16344() throws FactoryException {
        name       = "Gauss-Kruger CM 99W";
        aliases    = new String[] {"6-degree Gauss-Kruger CM 99W"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(16344);
    }

    /**
     * Tests “Gauss-Kruger CM 9E” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>16302</b></li>
     *   <li>EPSG coordinate operation name: <b>Gauss-Kruger CM 9E</b></li>
     *   <li>Alias(es) given by EPSG: <b>6-degree Gauss-Kruger CM 9E</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>World - N hemisphere - 6°E to 12°E</b></li>
     * </ul>
     *
     * Remarks: Without zone prefix in easting.
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Gauss-Kruger CM 9E")
    public void EPSG_16302() throws FactoryException {
        name       = "Gauss-Kruger CM 9E";
        aliases    = new String[] {"6-degree Gauss-Kruger CM 9E"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(16302);
    }

    /**
     * Tests “Gauss-Kruger CM 9W” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>16359</b></li>
     *   <li>EPSG coordinate operation name: <b>Gauss-Kruger CM 9W</b></li>
     *   <li>Alias(es) given by EPSG: <b>6-degree Gauss-Kruger CM 9W</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>World - N hemisphere - 12°W to 6°W</b></li>
     * </ul>
     *
     * Remarks: Without zone prefix in easting.
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Gauss-Kruger CM 9W")
    public void EPSG_16359() throws FactoryException {
        name       = "Gauss-Kruger CM 9W";
        aliases    = new String[] {"6-degree Gauss-Kruger CM 9W"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(16359);
    }

    /**
     * Tests “Ghana National Grid” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>19959</b></li>
     *   <li>EPSG coordinate operation name: <b>Ghana National Grid</b></li>
     *   <li>Alias(es) given by EPSG: <b>Gold Coast Grid</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>Ghana</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Ghana National Grid")
    public void EPSG_19959() throws FactoryException {
        name       = "Ghana National Grid";
        aliases    = new String[] {"Gold Coast Grid"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(19959);
    }

    /**
     * Tests “India zone I (1962 metres)” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>18236</b></li>
     *   <li>EPSG coordinate operation name: <b>India zone I (1962 metres)</b></li>
     *   <li>Alias(es) given by EPSG: <b>India zone I</b></li>
     *   <li>Coordinate operation method: <b>Lambert Conic Conformal (1SP)</b></li>
     *   <li>EPSG Usage Extent: <b>Pakistan - 28°N to 35°35'N</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("India zone I (1962 metres)")
    public void EPSG_18236() throws FactoryException {
        name       = "India zone I (1962 metres)";
        aliases    = new String[] {"India zone I"};
        methodName = "Lambert Conic Conformal (1SP)";
        createAndVerifyProjection(18236);
    }

    /**
     * Tests “India zone I (1975 metres)” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>18231</b></li>
     *   <li>EPSG coordinate operation name: <b>India zone I (1975 metres)</b></li>
     *   <li>Alias(es) given by EPSG: <b>India zone I</b></li>
     *   <li>Coordinate operation method: <b>Lambert Conic Conformal (1SP)</b></li>
     *   <li>EPSG Usage Extent: <b>India - north of 28°N</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("India zone I (1975 metres)")
    public void EPSG_18231() throws FactoryException {
        name       = "India zone I (1975 metres)";
        aliases    = new String[] {"India zone I"};
        methodName = "Lambert Conic Conformal (1SP)";
        createAndVerifyProjection(18231);
    }

    /**
     * Tests “India zone IIa (1962 metres)” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>18237</b></li>
     *   <li>EPSG coordinate operation name: <b>India zone IIa (1962 metres)</b></li>
     *   <li>Alias(es) given by EPSG: <b>India zone IIa</b></li>
     *   <li>Coordinate operation method: <b>Lambert Conic Conformal (1SP)</b></li>
     *   <li>EPSG Usage Extent: <b>Pakistan - onshore south of 28°N</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("India zone IIa (1962 metres)")
    public void EPSG_18237() throws FactoryException {
        name       = "India zone IIa (1962 metres)";
        aliases    = new String[] {"India zone IIa"};
        methodName = "Lambert Conic Conformal (1SP)";
        createAndVerifyProjection(18237);
    }

    /**
     * Tests “India zone IIa (1975 metres)” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>18232</b></li>
     *   <li>EPSG coordinate operation name: <b>India zone IIa (1975 metres)</b></li>
     *   <li>Alias(es) given by EPSG: <b>India zone IIa</b></li>
     *   <li>Coordinate operation method: <b>Lambert Conic Conformal (1SP)</b></li>
     *   <li>EPSG Usage Extent: <b>India - onshore 21°N to 28°N and west of 82°E</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("India zone IIa (1975 metres)")
    public void EPSG_18232() throws FactoryException {
        name       = "India zone IIa (1975 metres)";
        aliases    = new String[] {"India zone IIa"};
        methodName = "Lambert Conic Conformal (1SP)";
        createAndVerifyProjection(18232);
    }

    /**
     * Tests “India zone IIb (1937 metres)” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>18238</b></li>
     *   <li>EPSG coordinate operation name: <b>India zone IIb (1937 metres)</b></li>
     *   <li>Alias(es) given by EPSG: <b>India zone IIb</b></li>
     *   <li>Coordinate operation method: <b>Lambert Conic Conformal (1SP)</b></li>
     *   <li>EPSG Usage Extent: <b>Bangladesh - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("India zone IIb (1937 metres)")
    public void EPSG_18238() throws FactoryException {
        name       = "India zone IIb (1937 metres)";
        aliases    = new String[] {"India zone IIb"};
        methodName = "Lambert Conic Conformal (1SP)";
        createAndVerifyProjection(18238);
    }

    /**
     * Tests “India zone IIb (1975 metres)” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>18235</b></li>
     *   <li>EPSG coordinate operation name: <b>India zone IIb (1975 metres)</b></li>
     *   <li>Alias(es) given by EPSG: <b>India zone IIb</b></li>
     *   <li>Coordinate operation method: <b>Lambert Conic Conformal (1SP)</b></li>
     *   <li>EPSG Usage Extent: <b>India - onshore north of 21°N and east of 82°E</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("India zone IIb (1975 metres)")
    public void EPSG_18235() throws FactoryException {
        name       = "India zone IIb (1975 metres)";
        aliases    = new String[] {"India zone IIb"};
        methodName = "Lambert Conic Conformal (1SP)";
        createAndVerifyProjection(18235);
    }

    /**
     * Tests “India zone IIIa (1975 metres)” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>18233</b></li>
     *   <li>EPSG coordinate operation name: <b>India zone IIIa (1975 metres)</b></li>
     *   <li>Alias(es) given by EPSG: <b>India zone IIIa</b></li>
     *   <li>Coordinate operation method: <b>Lambert Conic Conformal (1SP)</b></li>
     *   <li>EPSG Usage Extent: <b>India - onshore 15°N to 21°N</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("India zone IIIa (1975 metres)")
    public void EPSG_18233() throws FactoryException {
        name       = "India zone IIIa (1975 metres)";
        aliases    = new String[] {"India zone IIIa"};
        methodName = "Lambert Conic Conformal (1SP)";
        createAndVerifyProjection(18233);
    }

    /**
     * Tests “India zone IVa (1975 metres)” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>18234</b></li>
     *   <li>EPSG coordinate operation name: <b>India zone IVa (1975 metres)</b></li>
     *   <li>Alias(es) given by EPSG: <b>India zone IVa</b></li>
     *   <li>Coordinate operation method: <b>Lambert Conic Conformal (1SP)</b></li>
     *   <li>EPSG Usage Extent: <b>India - mainland south of 15°N</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("India zone IVa (1975 metres)")
    public void EPSG_18234() throws FactoryException {
        name       = "India zone IVa (1975 metres)";
        aliases    = new String[] {"India zone IVa"};
        methodName = "Lambert Conic Conformal (1SP)";
        createAndVerifyProjection(18234);
    }

    /**
     * Tests “Iraq zone” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>19906</b></li>
     *   <li>EPSG coordinate operation name: <b>Iraq zone</b></li>
     *   <li>Alias(es) given by EPSG: <b>IOEPC Lambert</b></li>
     *   <li>Coordinate operation method: <b>Lambert Conic Conformal (1SP)</b></li>
     *   <li>EPSG Usage Extent: <b>Asia - Middle East - Iraq zone</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Iraq zone")
    public void EPSG_19906() throws FactoryException {
        name       = "Iraq zone";
        aliases    = new String[] {"IOEPC Lambert"};
        methodName = "Lambert Conic Conformal (1SP)";
        createAndVerifyProjection(19906);
    }

    /**
     * Tests “Italy” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>18121</b>, <b>18122</b></li>
     *   <li>EPSG coordinate operation name: <b>Italy</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Italy")
    public void variousItaly() throws FactoryException {
        name       = "Italy";
        methodName = "Transverse Mercator";
        createAndVerifyProjection(18121);
        createAndVerifyProjection(18122);
    }

    /**
     * Tests “Laborde Grid” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>19861</b></li>
     *   <li>EPSG coordinate operation name: <b>Laborde Grid</b></li>
     *   <li>Coordinate operation method: <b>Laborde Oblique Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>Madagascar - onshore and nearshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Laborde Grid")
    public void EPSG_19861() throws FactoryException {
        name       = "Laborde Grid";
        methodName = "Laborde Oblique Mercator";
        createAndVerifyProjection(19861);
    }

    /**
     * Tests “Laborde Grid approximation” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>19911</b></li>
     *   <li>EPSG coordinate operation name: <b>Laborde Grid approximation</b></li>
     *   <li>Alias(es) given by EPSG: <b>Laborde Grid</b></li>
     *   <li>Coordinate operation method: <b>Hotine Oblique Mercator (variant B)</b></li>
     *   <li>EPSG Usage Extent: <b>Madagascar - onshore and nearshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Laborde Grid approximation")
    public void EPSG_19911() throws FactoryException {
        name       = "Laborde Grid approximation";
        aliases    = new String[] {"Laborde Grid"};
        methodName = "Hotine Oblique Mercator (variant B)";
        createAndVerifyProjection(19911);
    }

    /**
     * Tests “Lambert-93” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>18085</b></li>
     *   <li>EPSG coordinate operation name: <b>Lambert-93</b></li>
     *   <li>Coordinate operation method: <b>Lambert Conic Conformal (2SP)</b></li>
     *   <li>EPSG Usage Extent: <b>France - mainland onshore</b></li>
     * </ul>
     *
     * Remarks: Use grads.
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Lambert-93")
    public void EPSG_18085() throws FactoryException {
        name       = "Lambert-93";
        methodName = "Lambert Conic Conformal (2SP)";
        createAndVerifyProjection(18085);
    }

    /**
     * Tests “Lambert zone I” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>18081</b></li>
     *   <li>EPSG coordinate operation name: <b>Lambert zone I</b></li>
     *   <li>Alias(es) given by EPSG: <b>France zone I</b></li>
     *   <li>Coordinate operation method: <b>Lambert Conic Conformal (1SP)</b></li>
     *   <li>EPSG Usage Extent: <b>France - mainland north of 48.15°N</b></li>
     * </ul>
     *
     * Remarks: Use grads.
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Lambert zone I")
    public void EPSG_18081() throws FactoryException {
        name       = "Lambert zone I";
        aliases    = new String[] {"France zone I"};
        methodName = "Lambert Conic Conformal (1SP)";
        createAndVerifyProjection(18081);
    }

    /**
     * Tests “Lambert zone II” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>18082</b></li>
     *   <li>EPSG coordinate operation name: <b>Lambert zone II</b></li>
     *   <li>Alias(es) given by EPSG: <b>France zone II</b></li>
     *   <li>Coordinate operation method: <b>Lambert Conic Conformal (1SP)</b></li>
     *   <li>EPSG Usage Extent: <b>France - mainland 45.45°N to 48.15°N. Also all mainland.</b></li>
     * </ul>
     *
     * Remarks: Use grads.
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Lambert zone II")
    public void EPSG_18082() throws FactoryException {
        name       = "Lambert zone II";
        aliases    = new String[] {"France zone II"};
        methodName = "Lambert Conic Conformal (1SP)";
        createAndVerifyProjection(18082);
    }

    /**
     * Tests “Lambert zone III” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>18083</b></li>
     *   <li>EPSG coordinate operation name: <b>Lambert zone III</b></li>
     *   <li>Alias(es) given by EPSG: <b>France zone III</b></li>
     *   <li>Coordinate operation method: <b>Lambert Conic Conformal (1SP)</b></li>
     *   <li>EPSG Usage Extent: <b>France - mainland south of 45.45°N</b></li>
     * </ul>
     *
     * Remarks: Use grads.
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Lambert zone III")
    public void EPSG_18083() throws FactoryException {
        name       = "Lambert zone III";
        aliases    = new String[] {"France zone III"};
        methodName = "Lambert Conic Conformal (1SP)";
        createAndVerifyProjection(18083);
    }

    /**
     * Tests “Levant zone” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>19940</b></li>
     *   <li>EPSG coordinate operation name: <b>Levant zone</b></li>
     *   <li>Coordinate operation method: <b>Lambert Conic Near-Conformal</b></li>
     *   <li>EPSG Usage Extent: <b>Asia - Middle East - Lebanon and Syria onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Levant zone")
    public void EPSG_19940() throws FactoryException {
        name       = "Levant zone";
        methodName = "Lambert Conic Near-Conformal";
        createAndVerifyProjection(19940);
    }

    /**
     * Tests “Libya” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>18240</b>, <b>18241</b>, <b>18242</b>, <b>18243</b>,
     *       <b>18244</b>, <b>18245</b>, <b>18246</b>, <b>18247</b>, <b>18248</b></li>
     *   <li>EPSG coordinate operation name: <b>Libya</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Libya")
    public void variousLibya() throws FactoryException {
        name       = "Libya";
        methodName = "Transverse Mercator";
        for (int code = 18240; code <= 18248; code++) {    // Loop over 9 codes
            createAndVerifyProjection(code);
        }
    }

    /**
     * Tests “Libya TM” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>18310</b>, <b>18311</b>, <b>18312</b>, <b>18313</b>,
     *       <b>18314</b>, <b>18315</b>, <b>18316</b>, <b>18317</b>, <b>18318</b>, <b>18319</b></li>
     *   <li>EPSG coordinate operation name: <b>Libya TM</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Libya TM")
    public void variousLibyaTM() throws FactoryException {
        name       = "Libya TM";
        methodName = "Transverse Mercator";
        for (int code = 18310; code <= 18319; code++) {    // Loop over 10 codes
            createAndVerifyProjection(code);
        }
    }

    /**
     * Tests “Louisiana CS27 North zone” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>11701</b></li>
     *   <li>EPSG coordinate operation name: <b>Louisiana CS27 North zone</b></li>
     *   <li>Alias(es) given by EPSG: <b>Louisiana North</b></li>
     *   <li>Coordinate operation method: <b>Lambert Conic Conformal (2SP)</b></li>
     *   <li>EPSG Usage Extent: <b>USA - Louisiana - SPCS - N</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Louisiana CS27 North zone")
    public void EPSG_11701() throws FactoryException {
        name       = "Louisiana CS27 North zone";
        aliases    = new String[] {"Louisiana North"};
        methodName = "Lambert Conic Conformal (2SP)";
        createAndVerifyProjection(11701);
    }

    /**
     * Tests “Louisiana CS27 South zone” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>11702</b></li>
     *   <li>EPSG coordinate operation name: <b>Louisiana CS27 South zone</b></li>
     *   <li>Alias(es) given by EPSG: <b>Louisiana South</b></li>
     *   <li>Coordinate operation method: <b>Lambert Conic Conformal (2SP)</b></li>
     *   <li>EPSG Usage Extent: <b>USA - Louisiana - SPCS27 - S</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Louisiana CS27 South zone")
    public void EPSG_11702() throws FactoryException {
        name       = "Louisiana CS27 South zone";
        aliases    = new String[] {"Louisiana South"};
        methodName = "Lambert Conic Conformal (2SP)";
        createAndVerifyProjection(11702);
    }

    /**
     * Tests “Michigan CS27 Central zone” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>12112</b></li>
     *   <li>EPSG coordinate operation name: <b>Michigan CS27 Central zone</b></li>
     *   <li>Alias(es) given by EPSG: <b>Michigan Central</b></li>
     *   <li>Coordinate operation method: <b>Lambert Conic Conformal (2SP)</b></li>
     *   <li>EPSG Usage Extent: <b>USA - Michigan - SPCS - C</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Michigan CS27 Central zone")
    public void EPSG_12112() throws FactoryException {
        name       = "Michigan CS27 Central zone";
        aliases    = new String[] {"Michigan Central"};
        methodName = "Lambert Conic Conformal (2SP)";
        createAndVerifyProjection(12112);
    }

    /**
     * Tests “Michigan CS27 South zone” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>12113</b></li>
     *   <li>EPSG coordinate operation name: <b>Michigan CS27 South zone</b></li>
     *   <li>Alias(es) given by EPSG: <b>Michigan South</b></li>
     *   <li>Coordinate operation method: <b>Lambert Conic Conformal (2SP)</b></li>
     *   <li>EPSG Usage Extent: <b>USA - Michigan - SPCS - S</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Michigan CS27 South zone")
    public void EPSG_12113() throws FactoryException {
        name       = "Michigan CS27 South zone";
        aliases    = new String[] {"Michigan South"};
        methodName = "Lambert Conic Conformal (2SP)";
        createAndVerifyProjection(12113);
    }

    /**
     * Tests “Netherlands East Indies Equatorial Zone” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>19905</b></li>
     *   <li>EPSG coordinate operation name: <b>Netherlands East Indies Equatorial Zone</b></li>
     *   <li>Alias(es) given by EPSG: <b>NEIEZ</b></li>
     *   <li>Coordinate operation method: <b>Mercator (variant A)</b></li>
     *   <li>EPSG Usage Extent: <b>Indonesia - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Netherlands East Indies Equatorial Zone")
    public void EPSG_19905() throws FactoryException {
        name       = "Netherlands East Indies Equatorial Zone";
        aliases    = new String[] {"NEIEZ"};
        methodName = "Mercator (variant A)";
        createAndVerifyProjection(19905);
    }

    /**
     * Tests “New Mexico CS27 East zone” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>13001</b></li>
     *   <li>EPSG coordinate operation name: <b>New Mexico CS27 East zone</b></li>
     *   <li>Alias(es) given by EPSG: <b>New Mexico East</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>USA - New Mexico - SPCS - E</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("New Mexico CS27 East zone")
    public void EPSG_13001() throws FactoryException {
        name       = "New Mexico CS27 East zone";
        aliases    = new String[] {"New Mexico East"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(13001);
    }

    /**
     * Tests “New Zealand Map Grid” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>19917</b></li>
     *   <li>EPSG coordinate operation name: <b>New Zealand Map Grid</b></li>
     *   <li>Alias(es) given by EPSG: <b>NZMG</b></li>
     *   <li>Coordinate operation method: <b>New Zealand Map Grid</b></li>
     *   <li>EPSG Usage Extent: <b>New Zealand - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("New Zealand Map Grid")
    public void EPSG_19917() throws FactoryException {
        name       = "New Zealand Map Grid";
        aliases    = new String[] {"NZMG"};
        methodName = "New Zealand Map Grid";
        createAndVerifyProjection(19917);
    }

    /**
     * Tests “New Zealand North Island National Grid” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>18141</b></li>
     *   <li>EPSG coordinate operation name: <b>New Zealand North Island National Grid</b></li>
     *   <li>Alias(es) given by EPSG: <b>North Island Grid</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>New Zealand - North Island</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("New Zealand North Island National Grid")
    public void EPSG_18141() throws FactoryException {
        name       = "New Zealand North Island National Grid";
        aliases    = new String[] {"North Island Grid"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(18141);
    }

    /**
     * Tests “New Zealand South Island National Grid” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>18142</b></li>
     *   <li>EPSG coordinate operation name: <b>New Zealand South Island National Grid</b></li>
     *   <li>Alias(es) given by EPSG: <b>South Island Grid</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>New Zealand - South and Stewart Islands</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("New Zealand South Island National Grid")
    public void EPSG_18142() throws FactoryException {
        name       = "New Zealand South Island National Grid";
        aliases    = new String[] {"South Island Grid"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(18142);
    }

    /**
     * Tests “New Zealand Transverse Mercator 2000” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>19971</b></li>
     *   <li>EPSG coordinate operation name: <b>New Zealand Transverse Mercator 2000</b></li>
     *   <li>Alias(es) given by EPSG: <b>NZTM</b>, <b>New Zealand Transverse Mercator</b>, <b>NZTM2000</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>New Zealand - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("New Zealand Transverse Mercator 2000")
    public void EPSG_19971() throws FactoryException {
        name       = "New Zealand Transverse Mercator 2000";
        aliases    = new String[] {"NZTM", "New Zealand Transverse Mercator", "NZTM2000"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(19971);
    }

    /**
     * Tests “Nigeria East Belt” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>18153</b></li>
     *   <li>EPSG coordinate operation name: <b>Nigeria East Belt</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>Nigeria - east of 10.5°E</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Nigeria East Belt")
    public void EPSG_18153() throws FactoryException {
        name       = "Nigeria East Belt";
        methodName = "Transverse Mercator";
        createAndVerifyProjection(18153);
    }

    /**
     * Tests “Nigeria Mid Belt” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>18152</b></li>
     *   <li>EPSG coordinate operation name: <b>Nigeria Mid Belt</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>Nigeria - 6.5°E to 10.5°E</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Nigeria Mid Belt")
    public void EPSG_18152() throws FactoryException {
        name       = "Nigeria Mid Belt";
        methodName = "Transverse Mercator";
        createAndVerifyProjection(18152);
    }

    /**
     * Tests “Nigeria West Belt” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>18151</b></li>
     *   <li>EPSG coordinate operation name: <b>Nigeria West Belt</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>Nigeria - west of 6.5°E</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Nigeria West Belt")
    public void EPSG_18151() throws FactoryException {
        name       = "Nigeria West Belt";
        methodName = "Transverse Mercator";
        createAndVerifyProjection(18151);
    }

    /**
     * Tests “Nord Algerie” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>18021</b></li>
     *   <li>EPSG coordinate operation name: <b>Nord Algerie</b></li>
     *   <li>Coordinate operation method: <b>Lambert Conic Conformal (1SP)</b></li>
     *   <li>EPSG Usage Extent: <b>Algeria - north of 34°39'N</b></li>
     * </ul>
     *
     * Remarks: Check not old parameters.
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Nord Algerie")
    public void EPSG_18021() throws FactoryException {
        name       = "Nord Algerie";
        methodName = "Lambert Conic Conformal (1SP)";
        createAndVerifyProjection(18021);
    }

    /**
     * Tests “Nord Tunisie” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>18181</b></li>
     *   <li>EPSG coordinate operation name: <b>Nord Tunisie</b></li>
     *   <li>Coordinate operation method: <b>Lambert Conic Conformal (1SP)</b></li>
     *   <li>EPSG Usage Extent: <b>Tunisia - north of 34°39'N</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Nord Tunisie")
    public void EPSG_18181() throws FactoryException {
        name       = "Nord Tunisie";
        methodName = "Lambert Conic Conformal (1SP)";
        createAndVerifyProjection(18181);
    }

    /**
     * Tests “Oklahoma CS27 North zone” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>13501</b></li>
     *   <li>EPSG coordinate operation name: <b>Oklahoma CS27 North zone</b></li>
     *   <li>Alias(es) given by EPSG: <b>Oklahoma North</b></li>
     *   <li>Coordinate operation method: <b>Lambert Conic Conformal (2SP)</b></li>
     *   <li>EPSG Usage Extent: <b>USA - Oklahoma - SPCS - N</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Oklahoma CS27 North zone")
    public void EPSG_13501() throws FactoryException {
        name       = "Oklahoma CS27 North zone";
        aliases    = new String[] {"Oklahoma North"};
        methodName = "Lambert Conic Conformal (2SP)";
        createAndVerifyProjection(13501);
    }

    /**
     * Tests “Oklahoma CS27 South zone” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>13502</b></li>
     *   <li>EPSG coordinate operation name: <b>Oklahoma CS27 South zone</b></li>
     *   <li>Alias(es) given by EPSG: <b>Oklahoma South</b></li>
     *   <li>Coordinate operation method: <b>Lambert Conic Conformal (2SP)</b></li>
     *   <li>EPSG Usage Extent: <b>USA - Oklahoma - SPCS - S</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Oklahoma CS27 South zone")
    public void EPSG_13502() throws FactoryException {
        name       = "Oklahoma CS27 South zone";
        aliases    = new String[] {"Oklahoma South"};
        methodName = "Lambert Conic Conformal (2SP)";
        createAndVerifyProjection(13502);
    }

    /**
     * Tests “Peninsular RSO” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>19895</b></li>
     *   <li>EPSG coordinate operation name: <b>Peninsular RSO</b></li>
     *   <li>Coordinate operation method: <b>Hotine Oblique Mercator (variant A)</b></li>
     *   <li>EPSG Usage Extent: <b>Malaysia - West Malaysia</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Peninsular RSO")
    public void EPSG_19895() throws FactoryException {
        name       = "Peninsular RSO";
        methodName = "Hotine Oblique Mercator (variant A)";
        createAndVerifyProjection(19895);
    }

    /**
     * Tests “Peru central zone” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>18162</b></li>
     *   <li>EPSG coordinate operation name: <b>Peru central zone</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>Peru - 79°W to 73°W</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Peru central zone")
    public void EPSG_18162() throws FactoryException {
        name       = "Peru central zone";
        methodName = "Transverse Mercator";
        createAndVerifyProjection(18162);
    }

    /**
     * Tests “Peru east zone” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>18163</b></li>
     *   <li>EPSG coordinate operation name: <b>Peru east zone</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>Peru - east of 73°W</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Peru east zone")
    public void EPSG_18163() throws FactoryException {
        name       = "Peru east zone";
        methodName = "Transverse Mercator";
        createAndVerifyProjection(18163);
    }

    /**
     * Tests “Peru west zone” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>18161</b></li>
     *   <li>EPSG coordinate operation name: <b>Peru west zone</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>Peru - west of 79°W</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Peru west zone")
    public void EPSG_18161() throws FactoryException {
        name       = "Peru west zone";
        methodName = "Transverse Mercator";
        createAndVerifyProjection(18161);
    }

    /**
     * Tests “Philippines” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>18171</b>, <b>18172</b>, <b>18173</b>, <b>18174</b>, <b>18175</b></li>
     *   <li>EPSG coordinate operation name: <b>Philippines</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Philippines")
    public void variousPhilippines() throws FactoryException {
        name       = "Philippines";
        methodName = "Transverse Mercator";
        for (int code = 18171; code <= 18175; code++) {    // Loop over 5 codes
            createAndVerifyProjection(code);
        }
    }

    /**
     * Tests “Qatar Grid” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>19953</b></li>
     *   <li>EPSG coordinate operation name: <b>Qatar Grid</b></li>
     *   <li>Coordinate operation method: <b>Cassini-Soldner</b></li>
     *   <li>EPSG Usage Extent: <b>Qatar - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Qatar Grid")
    public void EPSG_19953() throws FactoryException {
        name       = "Qatar Grid";
        methodName = "Cassini-Soldner";
        createAndVerifyProjection(19953);
    }

    /**
     * Tests “Qatar National Grid” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>19919</b></li>
     *   <li>EPSG coordinate operation name: <b>Qatar National Grid</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>Qatar</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Qatar National Grid")
    public void EPSG_19919() throws FactoryException {
        name       = "Qatar National Grid";
        methodName = "Transverse Mercator";
        createAndVerifyProjection(19919);
    }

    /**
     * Tests “RD New” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>19914</b></li>
     *   <li>EPSG coordinate operation name: <b>RD New</b></li>
     *   <li>Coordinate operation method: <b>Oblique Stereographic</b></li>
     *   <li>EPSG Usage Extent: <b>Netherlands - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("RD New")
    public void EPSG_19914() throws FactoryException {
        name       = "RD New";
        methodName = "Oblique Stereographic";
        createAndVerifyProjection(19914);
    }

    /**
     * Tests “Rectified Skew Orthomorphic Borneo Grid (chains)” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>19956</b></li>
     *   <li>EPSG coordinate operation name: <b>Rectified Skew Orthomorphic Borneo Grid (chains)</b></li>
     *   <li>Alias(es) given by EPSG: <b>RSO Borneo (chSe)</b></li>
     *   <li>Coordinate operation method: <b>Hotine Oblique Mercator (variant B)</b></li>
     *   <li>EPSG Usage Extent: <b>Asia - Brunei and East Malaysia</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Rectified Skew Orthomorphic Borneo Grid (chains)")
    public void EPSG_19956() throws FactoryException {
        name       = "Rectified Skew Orthomorphic Borneo Grid (chains)";
        aliases    = new String[] {"RSO Borneo (chSe)"};
        methodName = "Hotine Oblique Mercator (variant B)";
        createAndVerifyProjection(19956);
    }

    /**
     * Tests “Rectified Skew Orthomorphic Borneo Grid (feet)” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>19957</b></li>
     *   <li>EPSG coordinate operation name: <b>Rectified Skew Orthomorphic Borneo Grid (feet)</b></li>
     *   <li>Alias(es) given by EPSG: <b>RSO Borneo (ftSe)</b></li>
     *   <li>Coordinate operation method: <b>Hotine Oblique Mercator (variant B)</b></li>
     *   <li>EPSG Usage Extent: <b>Malaysia - East Malaysia</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Rectified Skew Orthomorphic Borneo Grid (feet)")
    public void EPSG_19957() throws FactoryException {
        name       = "Rectified Skew Orthomorphic Borneo Grid (feet)";
        aliases    = new String[] {"RSO Borneo (ftSe)"};
        methodName = "Hotine Oblique Mercator (variant B)";
        createAndVerifyProjection(19957);
    }

    /**
     * Tests “Rectified Skew Orthomorphic Borneo Grid (metres)” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>19958</b></li>
     *   <li>EPSG coordinate operation name: <b>Rectified Skew Orthomorphic Borneo Grid (metres)</b></li>
     *   <li>Alias(es) given by EPSG: <b>RSO Borneo (m)</b></li>
     *   <li>Coordinate operation method: <b>Hotine Oblique Mercator (variant B)</b></li>
     *   <li>EPSG Usage Extent: <b>Asia - Brunei and East Malaysia</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Rectified Skew Orthomorphic Borneo Grid (metres)")
    public void EPSG_19958() throws FactoryException {
        name       = "Rectified Skew Orthomorphic Borneo Grid (metres)";
        aliases    = new String[] {"RSO Borneo (m)"};
        methodName = "Hotine Oblique Mercator (variant B)";
        createAndVerifyProjection(19958);
    }

    /**
     * Tests “Rectified Skew Orthomorphic Malaya Grid (chains)” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>19871</b></li>
     *   <li>EPSG coordinate operation name: <b>Rectified Skew Orthomorphic Malaya Grid (chains)</b></li>
     *   <li>Alias(es) given by EPSG: <b>RSO Malaya (m)</b></li>
     *   <li>Coordinate operation method: <b>Hotine Oblique Mercator (variant A)</b></li>
     *   <li>EPSG Usage Extent: <b>Malaysia - West Malaysia - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Rectified Skew Orthomorphic Malaya Grid (chains)")
    public void EPSG_19871() throws FactoryException {
        name       = "Rectified Skew Orthomorphic Malaya Grid (chains)";
        aliases    = new String[] {"RSO Malaya (m)"};
        methodName = "Hotine Oblique Mercator (variant A)";
        createAndVerifyProjection(19871);
    }

    /**
     * Tests “Rectified Skew Orthomorphic Malaya Grid (metres)” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>19872</b></li>
     *   <li>EPSG coordinate operation name: <b>Rectified Skew Orthomorphic Malaya Grid (metres)</b></li>
     *   <li>Alias(es) given by EPSG: <b>RSO Malaya (m)</b></li>
     *   <li>Coordinate operation method: <b>Hotine Oblique Mercator (variant A)</b></li>
     *   <li>EPSG Usage Extent: <b>Malaysia - West Malaysia - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Rectified Skew Orthomorphic Malaya Grid (metres)")
    public void EPSG_19872() throws FactoryException {
        name       = "Rectified Skew Orthomorphic Malaya Grid (metres)";
        aliases    = new String[] {"RSO Malaya (m)"};
        methodName = "Hotine Oblique Mercator (variant A)";
        createAndVerifyProjection(19872);
    }

    /**
     * Tests “South West African Survey Grid zone 11” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>17611</b></li>
     *   <li>EPSG coordinate operation name: <b>South West African Survey Grid zone 11</b></li>
     *   <li>Alias(es) given by EPSG: <b>SW African Grid zone 11</b>, <b>Lo22/11</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator (South Orientated)</b></li>
     *   <li>EPSG Usage Extent: <b>Namibia - west of 12°E</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("South West African Survey Grid zone 11")
    public void EPSG_17611() throws FactoryException {
        name       = "South West African Survey Grid zone 11";
        aliases    = new String[] {"SW African Grid zone 11", "Lo22/11"};
        methodName = "Transverse Mercator (South Orientated)";
        createAndVerifyProjection(17611);
    }

    /**
     * Tests “SPCS83 Alaska zone 2 (meters)” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>15032</b></li>
     *   <li>EPSG coordinate operation name: <b>SPCS83 Alaska zone 2 (meters)</b></li>
     *   <li>Alias(es) given by EPSG: <b>Alaska zone 2</b>, <b>Alaska CS83 zone 2</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>USA - Alaska - 144°W to 141°W</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("SPCS83 Alaska zone 2 (meters)")
    public void EPSG_15032() throws FactoryException {
        name       = "SPCS83 Alaska zone 2 (meters)";
        aliases    = new String[] {"Alaska zone 2", "Alaska CS83 zone 2"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(15032);
    }

    /**
     * Tests “SPCS83 Alaska zone 4 (meters)” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>15034</b></li>
     *   <li>EPSG coordinate operation name: <b>SPCS83 Alaska zone 4 (meters)</b></li>
     *   <li>Alias(es) given by EPSG: <b>Alaska zone 4</b>, <b>Alaska CS83 zone 4</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>USA - Alaska - 152°W to 148°W</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("SPCS83 Alaska zone 4 (meters)")
    public void EPSG_15034() throws FactoryException {
        name       = "SPCS83 Alaska zone 4 (meters)";
        aliases    = new String[] {"Alaska zone 4", "Alaska CS83 zone 4"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(15034);
    }

    /**
     * Tests “SPCS83 Alaska zone 5 (meters)” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>15035</b></li>
     *   <li>EPSG coordinate operation name: <b>SPCS83 Alaska zone 5 (meters)</b></li>
     *   <li>Alias(es) given by EPSG: <b>Alaska zone 5</b>, <b>Alaska CS83 zone 5</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>USA - Alaska - 156°W to 152°W</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("SPCS83 Alaska zone 5 (meters)")
    public void EPSG_15035() throws FactoryException {
        name       = "SPCS83 Alaska zone 5 (meters)";
        aliases    = new String[] {"Alaska zone 5", "Alaska CS83 zone 5"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(15035);
    }

    /**
     * Tests “SPCS83 Alaska zone 6 (meters)” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>15036</b></li>
     *   <li>EPSG coordinate operation name: <b>SPCS83 Alaska zone 6 (meters)</b></li>
     *   <li>Alias(es) given by EPSG: <b>Alaska zone 6</b>, <b>Alaska CS83 zone 6</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>USA - Alaska - 160°W to 156°W</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("SPCS83 Alaska zone 6 (meters)")
    public void EPSG_15036() throws FactoryException {
        name       = "SPCS83 Alaska zone 6 (meters)";
        aliases    = new String[] {"Alaska zone 6", "Alaska CS83 zone 6"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(15036);
    }

    /**
     * Tests “SPCS83 Alaska zone 7 (meters)” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>15037</b></li>
     *   <li>EPSG coordinate operation name: <b>SPCS83 Alaska zone 7 (meters)</b></li>
     *   <li>Alias(es) given by EPSG: <b>Alaska zone 7</b>, <b>Alaska CS83 zone 7</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>USA - Alaska - 164°W to 160°W</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("SPCS83 Alaska zone 7 (meters)")
    public void EPSG_15037() throws FactoryException {
        name       = "SPCS83 Alaska zone 7 (meters)";
        aliases    = new String[] {"Alaska zone 7", "Alaska CS83 zone 7"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(15037);
    }

    /**
     * Tests “SPCS83 Alaska zone 8 (meters)” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>15038</b></li>
     *   <li>EPSG coordinate operation name: <b>SPCS83 Alaska zone 8 (meters)</b></li>
     *   <li>Alias(es) given by EPSG: <b>Alaska zone 8</b>, <b>Alaska CS83 zone 8</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>USA - Alaska - north of 54.5°N; 168°W to 164°W</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("SPCS83 Alaska zone 8 (meters)")
    public void EPSG_15038() throws FactoryException {
        name       = "SPCS83 Alaska zone 8 (meters)";
        aliases    = new String[] {"Alaska zone 8", "Alaska CS83 zone 8"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(15038);
    }

    /**
     * Tests “SPCS83 Alaska zone 9 (meters)” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>15039</b></li>
     *   <li>EPSG coordinate operation name: <b>SPCS83 Alaska zone 9 (meters)</b></li>
     *   <li>Alias(es) given by EPSG: <b>Alaska zone 9</b>, <b>Alaska CS83 zone 9</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>USA - Alaska - north of 54.5°N; west of 168°W</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("SPCS83 Alaska zone 9 (meters)")
    public void EPSG_15039() throws FactoryException {
        name       = "SPCS83 Alaska zone 9 (meters)";
        aliases    = new String[] {"Alaska zone 9", "Alaska CS83 zone 9"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(15039);
    }

    /**
     * Tests “SPCS83 California zone 4 (meters)” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>10434</b></li>
     *   <li>EPSG coordinate operation name: <b>SPCS83 California zone 4 (meters)</b></li>
     *   <li>Alias(es) given by EPSG: <b>California zone 4</b>, <b>California CS83 zone 4</b></li>
     *   <li>Coordinate operation method: <b>Lambert Conic Conformal (2SP)</b></li>
     *   <li>EPSG Usage Extent: <b>USA - California - SPCS - 4</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("SPCS83 California zone 4 (meters)")
    public void EPSG_10434() throws FactoryException {
        name       = "SPCS83 California zone 4 (meters)";
        aliases    = new String[] {"California zone 4", "California CS83 zone 4"};
        methodName = "Lambert Conic Conformal (2SP)";
        createAndVerifyProjection(10434);
    }

    /**
     * Tests “SPCS83 California zone 4 (US Survey feet)” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>15310</b></li>
     *   <li>EPSG coordinate operation name: <b>SPCS83 California zone 4 (US Survey feet)</b></li>
     *   <li>Alias(es) given by EPSG: <b>California zone 4 (ftUS)</b></li>
     *   <li>Coordinate operation method: <b>Lambert Conic Conformal (2SP)</b></li>
     *   <li>EPSG Usage Extent: <b>USA - California - SPCS - 4</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("SPCS83 California zone 4 (US Survey feet)")
    public void EPSG_15310() throws FactoryException {
        name       = "SPCS83 California zone 4 (US Survey feet)";
        aliases    = new String[] {"California zone 4 (ftUS)"};
        methodName = "Lambert Conic Conformal (2SP)";
        createAndVerifyProjection(15310);
    }

    /**
     * Tests “SPCS83 California zone 5 (meters)” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>10435</b></li>
     *   <li>EPSG coordinate operation name: <b>SPCS83 California zone 5 (meters)</b></li>
     *   <li>Alias(es) given by EPSG: <b>California zone 5</b>, <b>California CS83 zone 5</b></li>
     *   <li>Coordinate operation method: <b>Lambert Conic Conformal (2SP)</b></li>
     *   <li>EPSG Usage Extent: <b>USA - California - SPCS83 - 5</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("SPCS83 California zone 5 (meters)")
    public void EPSG_10435() throws FactoryException {
        name       = "SPCS83 California zone 5 (meters)";
        aliases    = new String[] {"California zone 5", "California CS83 zone 5"};
        methodName = "Lambert Conic Conformal (2SP)";
        createAndVerifyProjection(10435);
    }

    /**
     * Tests “SPCS83 California zone 5 (US Survey feet)” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>15311</b></li>
     *   <li>EPSG coordinate operation name: <b>SPCS83 California zone 5 (US Survey feet)</b></li>
     *   <li>Alias(es) given by EPSG: <b>California zone 5 (ftUS)</b></li>
     *   <li>Coordinate operation method: <b>Lambert Conic Conformal (2SP)</b></li>
     *   <li>EPSG Usage Extent: <b>USA - California - SPCS83 - 5</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("SPCS83 California zone 5 (US Survey feet)")
    public void EPSG_15311() throws FactoryException {
        name       = "SPCS83 California zone 5 (US Survey feet)";
        aliases    = new String[] {"California zone 5 (ftUS)"};
        methodName = "Lambert Conic Conformal (2SP)";
        createAndVerifyProjection(15311);
    }

    /**
     * Tests “SPCS83 California zone 6 (meters)” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>10436</b></li>
     *   <li>EPSG coordinate operation name: <b>SPCS83 California zone 6 (meters)</b></li>
     *   <li>Alias(es) given by EPSG: <b>California zone 6</b>, <b>California CS83 zone 6</b></li>
     *   <li>Coordinate operation method: <b>Lambert Conic Conformal (2SP)</b></li>
     *   <li>EPSG Usage Extent: <b>USA - California - SPCS - 6</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("SPCS83 California zone 6 (meters)")
    public void EPSG_10436() throws FactoryException {
        name       = "SPCS83 California zone 6 (meters)";
        aliases    = new String[] {"California zone 6", "California CS83 zone 6"};
        methodName = "Lambert Conic Conformal (2SP)";
        createAndVerifyProjection(10436);
    }

    /**
     * Tests “SPCS83 California zone 6 (US Survey feet)” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>15312</b></li>
     *   <li>EPSG coordinate operation name: <b>SPCS83 California zone 6 (US Survey feet)</b></li>
     *   <li>Alias(es) given by EPSG: <b>California zone 6 (ftUS)</b></li>
     *   <li>Coordinate operation method: <b>Lambert Conic Conformal (2SP)</b></li>
     *   <li>EPSG Usage Extent: <b>USA - California - SPCS - 6</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("SPCS83 California zone 6 (US Survey feet)")
    public void EPSG_15312() throws FactoryException {
        name       = "SPCS83 California zone 6 (US Survey feet)";
        aliases    = new String[] {"California zone 6 (ftUS)"};
        methodName = "Lambert Conic Conformal (2SP)";
        createAndVerifyProjection(15312);
    }

    /**
     * Tests “SPCS83 Louisiana North zone (meters)” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>11731</b></li>
     *   <li>EPSG coordinate operation name: <b>SPCS83 Louisiana North zone (meters)</b></li>
     *   <li>Alias(es) given by EPSG: <b>Louisiana North</b>, <b>Louisiana CS83 North zone</b></li>
     *   <li>Coordinate operation method: <b>Lambert Conic Conformal (2SP)</b></li>
     *   <li>EPSG Usage Extent: <b>USA - Louisiana - SPCS - N</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("SPCS83 Louisiana North zone (meters)")
    public void EPSG_11731() throws FactoryException {
        name       = "SPCS83 Louisiana North zone (meters)";
        aliases    = new String[] {"Louisiana North", "Louisiana CS83 North zone"};
        methodName = "Lambert Conic Conformal (2SP)";
        createAndVerifyProjection(11731);
    }

    /**
     * Tests “SPCS83 Louisiana North zone (US Survey feet)” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>15391</b></li>
     *   <li>EPSG coordinate operation name: <b>SPCS83 Louisiana North zone (US Survey feet)</b></li>
     *   <li>Alias(es) given by EPSG: <b>Louisiana CS83 North zone (ftUS)</b></li>
     *   <li>Coordinate operation method: <b>Lambert Conic Conformal (2SP)</b></li>
     *   <li>EPSG Usage Extent: <b>USA - Louisiana - SPCS - N</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("SPCS83 Louisiana North zone (US Survey feet)")
    public void EPSG_15391() throws FactoryException {
        name       = "SPCS83 Louisiana North zone (US Survey feet)";
        aliases    = new String[] {"Louisiana CS83 North zone (ftUS)"};
        methodName = "Lambert Conic Conformal (2SP)";
        createAndVerifyProjection(15391);
    }

    /**
     * Tests “SPCS83 Louisiana South zone (meters)” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>11732</b></li>
     *   <li>EPSG coordinate operation name: <b>SPCS83 Louisiana South zone (meters)</b></li>
     *   <li>Alias(es) given by EPSG: <b>Louisiana South</b>, <b>Louisiana CS83 South zone</b></li>
     *   <li>Coordinate operation method: <b>Lambert Conic Conformal (2SP)</b></li>
     *   <li>EPSG Usage Extent: <b>USA - Louisiana - SPCS83 - S</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("SPCS83 Louisiana South zone (meters)")
    public void EPSG_11732() throws FactoryException {
        name       = "SPCS83 Louisiana South zone (meters)";
        aliases    = new String[] {"Louisiana South", "Louisiana CS83 South zone"};
        methodName = "Lambert Conic Conformal (2SP)";
        createAndVerifyProjection(11732);
    }

    /**
     * Tests “SPCS83 Louisiana South zone (US Survey feet)” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>15392</b></li>
     *   <li>EPSG coordinate operation name: <b>SPCS83 Louisiana South zone (US Survey feet)</b></li>
     *   <li>Alias(es) given by EPSG: <b>Louisiana CS83 South zone (ftUS)</b></li>
     *   <li>Coordinate operation method: <b>Lambert Conic Conformal (2SP)</b></li>
     *   <li>EPSG Usage Extent: <b>USA - Louisiana - SPCS83 - S</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("SPCS83 Louisiana South zone (US Survey feet)")
    public void EPSG_15392() throws FactoryException {
        name       = "SPCS83 Louisiana South zone (US Survey feet)";
        aliases    = new String[] {"Louisiana CS83 South zone (ftUS)"};
        methodName = "Lambert Conic Conformal (2SP)";
        createAndVerifyProjection(15392);
    }

    /**
     * Tests “SPCS83 Michigan Central zone (International feet)” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>15334</b></li>
     *   <li>EPSG coordinate operation name: <b>SPCS83 Michigan Central zone (International feet)</b></li>
     *   <li>Alias(es) given by EPSG: <b>Michigan Central (ft)</b></li>
     *   <li>Coordinate operation method: <b>Lambert Conic Conformal (2SP)</b></li>
     *   <li>EPSG Usage Extent: <b>USA - Michigan - SPCS - C</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("SPCS83 Michigan Central zone (International feet)")
    public void EPSG_15334() throws FactoryException {
        name       = "SPCS83 Michigan Central zone (International feet)";
        aliases    = new String[] {"Michigan Central (ft)"};
        methodName = "Lambert Conic Conformal (2SP)";
        createAndVerifyProjection(15334);
    }

    /**
     * Tests “SPCS83 Michigan Central zone (meters)” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>12142</b></li>
     *   <li>EPSG coordinate operation name: <b>SPCS83 Michigan Central zone (meters)</b></li>
     *   <li>Alias(es) given by EPSG: <b>Michigan Central</b>, <b>Michigan CS83 Central zone</b></li>
     *   <li>Coordinate operation method: <b>Lambert Conic Conformal (2SP)</b></li>
     *   <li>EPSG Usage Extent: <b>USA - Michigan - SPCS - C</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("SPCS83 Michigan Central zone (meters)")
    public void EPSG_12142() throws FactoryException {
        name       = "SPCS83 Michigan Central zone (meters)";
        aliases    = new String[] {"Michigan Central", "Michigan CS83 Central zone"};
        methodName = "Lambert Conic Conformal (2SP)";
        createAndVerifyProjection(12142);
    }

    /**
     * Tests “SPCS83 Michigan South zone (International feet)” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>15335</b></li>
     *   <li>EPSG coordinate operation name: <b>SPCS83 Michigan South zone (International feet)</b></li>
     *   <li>Alias(es) given by EPSG: <b>Michigan South (ft)</b></li>
     *   <li>Coordinate operation method: <b>Lambert Conic Conformal (2SP)</b></li>
     *   <li>EPSG Usage Extent: <b>USA - Michigan - SPCS - S</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("SPCS83 Michigan South zone (International feet)")
    public void EPSG_15335() throws FactoryException {
        name       = "SPCS83 Michigan South zone (International feet)";
        aliases    = new String[] {"Michigan South (ft)"};
        methodName = "Lambert Conic Conformal (2SP)";
        createAndVerifyProjection(15335);
    }

    /**
     * Tests “SPCS83 Michigan South zone (meters)” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>12143</b></li>
     *   <li>EPSG coordinate operation name: <b>SPCS83 Michigan South zone (meters)</b></li>
     *   <li>Alias(es) given by EPSG: <b>Michigan South</b>, <b>Michigan CS83 South zone</b></li>
     *   <li>Coordinate operation method: <b>Lambert Conic Conformal (2SP)</b></li>
     *   <li>EPSG Usage Extent: <b>USA - Michigan - SPCS - S</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("SPCS83 Michigan South zone (meters)")
    public void EPSG_12143() throws FactoryException {
        name       = "SPCS83 Michigan South zone (meters)";
        aliases    = new String[] {"Michigan South", "Michigan CS83 South zone"};
        methodName = "Lambert Conic Conformal (2SP)";
        createAndVerifyProjection(12143);
    }

    /**
     * Tests “SPCS83 New Mexico East zone (meters)” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>13031</b></li>
     *   <li>EPSG coordinate operation name: <b>SPCS83 New Mexico East zone (meters)</b></li>
     *   <li>Alias(es) given by EPSG: <b>New Mexico East</b>, <b>New Mexico CS83 East zone</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>USA - New Mexico - SPCS - E</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("SPCS83 New Mexico East zone (meters)")
    public void EPSG_13031() throws FactoryException {
        name       = "SPCS83 New Mexico East zone (meters)";
        aliases    = new String[] {"New Mexico East", "New Mexico CS83 East zone"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(13031);
    }

    /**
     * Tests “SPCS83 New Mexico East zone (US Survey feet)” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>15339</b></li>
     *   <li>EPSG coordinate operation name: <b>SPCS83 New Mexico East zone (US Survey feet)</b></li>
     *   <li>Alias(es) given by EPSG: <b>New Mexico East (ftUS)</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>USA - New Mexico - SPCS - E</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("SPCS83 New Mexico East zone (US Survey feet)")
    public void EPSG_15339() throws FactoryException {
        name       = "SPCS83 New Mexico East zone (US Survey feet)";
        aliases    = new String[] {"New Mexico East (ftUS)"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(15339);
    }

    /**
     * Tests “SPCS83 Oklahoma North zone (meters)” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>13531</b></li>
     *   <li>EPSG coordinate operation name: <b>SPCS83 Oklahoma North zone (meters)</b></li>
     *   <li>Alias(es) given by EPSG: <b>Oklahoma North</b>, <b>Oklahoma CS83 North zone</b></li>
     *   <li>Coordinate operation method: <b>Lambert Conic Conformal (2SP)</b></li>
     *   <li>EPSG Usage Extent: <b>USA - Oklahoma - SPCS - N</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("SPCS83 Oklahoma North zone (meters)")
    public void EPSG_13531() throws FactoryException {
        name       = "SPCS83 Oklahoma North zone (meters)";
        aliases    = new String[] {"Oklahoma North", "Oklahoma CS83 North zone"};
        methodName = "Lambert Conic Conformal (2SP)";
        createAndVerifyProjection(13531);
    }

    /**
     * Tests “SPCS83 Oklahoma North zone (US Survey feet)” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>15349</b></li>
     *   <li>EPSG coordinate operation name: <b>SPCS83 Oklahoma North zone (US Survey feet)</b></li>
     *   <li>Alias(es) given by EPSG: <b>Oklahoma North (ftUS)</b></li>
     *   <li>Coordinate operation method: <b>Lambert Conic Conformal (2SP)</b></li>
     *   <li>EPSG Usage Extent: <b>USA - Oklahoma - SPCS - N</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("SPCS83 Oklahoma North zone (US Survey feet)")
    public void EPSG_15349() throws FactoryException {
        name       = "SPCS83 Oklahoma North zone (US Survey feet)";
        aliases    = new String[] {"Oklahoma North (ftUS)"};
        methodName = "Lambert Conic Conformal (2SP)";
        createAndVerifyProjection(15349);
    }

    /**
     * Tests “SPCS83 Oklahoma South zone (meters)” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>13532</b></li>
     *   <li>EPSG coordinate operation name: <b>SPCS83 Oklahoma South zone (meters)</b></li>
     *   <li>Alias(es) given by EPSG: <b>Oklahoma South</b>, <b>Oklahoma CS83 South zone</b></li>
     *   <li>Coordinate operation method: <b>Lambert Conic Conformal (2SP)</b></li>
     *   <li>EPSG Usage Extent: <b>USA - Oklahoma - SPCS - S</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("SPCS83 Oklahoma South zone (meters)")
    public void EPSG_13532() throws FactoryException {
        name       = "SPCS83 Oklahoma South zone (meters)";
        aliases    = new String[] {"Oklahoma South", "Oklahoma CS83 South zone"};
        methodName = "Lambert Conic Conformal (2SP)";
        createAndVerifyProjection(13532);
    }

    /**
     * Tests “SPCS83 Oklahoma South zone (US Survey feet)” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>15350</b></li>
     *   <li>EPSG coordinate operation name: <b>SPCS83 Oklahoma South zone (US Survey feet)</b></li>
     *   <li>Alias(es) given by EPSG: <b>Oklahoma South (ftUS)</b></li>
     *   <li>Coordinate operation method: <b>Lambert Conic Conformal (2SP)</b></li>
     *   <li>EPSG Usage Extent: <b>USA - Oklahoma - SPCS - S</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("SPCS83 Oklahoma South zone (US Survey feet)")
    public void EPSG_15350() throws FactoryException {
        name       = "SPCS83 Oklahoma South zone (US Survey feet)";
        aliases    = new String[] {"Oklahoma South (ftUS)"};
        methodName = "Lambert Conic Conformal (2SP)";
        createAndVerifyProjection(15350);
    }

    /**
     * Tests “SPCS83 Texas Central zone (meters)” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>14233</b></li>
     *   <li>EPSG coordinate operation name: <b>SPCS83 Texas Central zone (meters)</b></li>
     *   <li>Alias(es) given by EPSG: <b>Texas Central</b>, <b>Texas CS83 Central zone</b></li>
     *   <li>Coordinate operation method: <b>Lambert Conic Conformal (2SP)</b></li>
     *   <li>EPSG Usage Extent: <b>USA - Texas - SPCS - C</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("SPCS83 Texas Central zone (meters)")
    public void EPSG_14233() throws FactoryException {
        name       = "SPCS83 Texas Central zone (meters)";
        aliases    = new String[] {"Texas Central", "Texas CS83 Central zone"};
        methodName = "Lambert Conic Conformal (2SP)";
        createAndVerifyProjection(14233);
    }

    /**
     * Tests “SPCS83 Texas Central zone (US Survey feet)” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>15359</b></li>
     *   <li>EPSG coordinate operation name: <b>SPCS83 Texas Central zone (US Survey feet)</b></li>
     *   <li>Alias(es) given by EPSG: <b>Texas Central (ftUS)</b></li>
     *   <li>Coordinate operation method: <b>Lambert Conic Conformal (2SP)</b></li>
     *   <li>EPSG Usage Extent: <b>USA - Texas - SPCS - C</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("SPCS83 Texas Central zone (US Survey feet)")
    public void EPSG_15359() throws FactoryException {
        name       = "SPCS83 Texas Central zone (US Survey feet)";
        aliases    = new String[] {"Texas Central (ftUS)"};
        methodName = "Lambert Conic Conformal (2SP)";
        createAndVerifyProjection(15359);
    }

    /**
     * Tests “SPCS83 Texas North Central zone (meters)” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>14232</b></li>
     *   <li>EPSG coordinate operation name: <b>SPCS83 Texas North Central zone (meters)</b></li>
     *   <li>Alias(es) given by EPSG: <b>Texas North Central</b>, <b>Texas CS83 North Central zone</b></li>
     *   <li>Coordinate operation method: <b>Lambert Conic Conformal (2SP)</b></li>
     *   <li>EPSG Usage Extent: <b>USA - Texas - SPCS - NC</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("SPCS83 Texas North Central zone (meters)")
    public void EPSG_14232() throws FactoryException {
        name       = "SPCS83 Texas North Central zone (meters)";
        aliases    = new String[] {"Texas North Central", "Texas CS83 North Central zone"};
        methodName = "Lambert Conic Conformal (2SP)";
        createAndVerifyProjection(14232);
    }

    /**
     * Tests “SPCS83 Texas North Central zone (US Survey feet)” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>15358</b></li>
     *   <li>EPSG coordinate operation name: <b>SPCS83 Texas North Central zone (US Survey feet)</b></li>
     *   <li>Alias(es) given by EPSG: <b>Texas North Central (ftUS)</b></li>
     *   <li>Coordinate operation method: <b>Lambert Conic Conformal (2SP)</b></li>
     *   <li>EPSG Usage Extent: <b>USA - Texas - SPCS - NC</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("SPCS83 Texas North Central zone (US Survey feet)")
    public void EPSG_15358() throws FactoryException {
        name       = "SPCS83 Texas North Central zone (US Survey feet)";
        aliases    = new String[] {"Texas North Central (ftUS)"};
        methodName = "Lambert Conic Conformal (2SP)";
        createAndVerifyProjection(15358);
    }

    /**
     * Tests “SPCS83 Texas North zone (meters)” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>14231</b></li>
     *   <li>EPSG coordinate operation name: <b>SPCS83 Texas North zone (meters)</b></li>
     *   <li>Alias(es) given by EPSG: <b>Texas North</b>, <b>Texas CS83 North zone</b></li>
     *   <li>Coordinate operation method: <b>Lambert Conic Conformal (2SP)</b></li>
     *   <li>EPSG Usage Extent: <b>USA - Texas - SPCS - N</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("SPCS83 Texas North zone (meters)")
    public void EPSG_14231() throws FactoryException {
        name       = "SPCS83 Texas North zone (meters)";
        aliases    = new String[] {"Texas North", "Texas CS83 North zone"};
        methodName = "Lambert Conic Conformal (2SP)";
        createAndVerifyProjection(14231);
    }

    /**
     * Tests “SPCS83 Texas North zone (US Survey feet)” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>15357</b></li>
     *   <li>EPSG coordinate operation name: <b>SPCS83 Texas North zone (US Survey feet)</b></li>
     *   <li>Alias(es) given by EPSG: <b>Texas North (ftUS)</b></li>
     *   <li>Coordinate operation method: <b>Lambert Conic Conformal (2SP)</b></li>
     *   <li>EPSG Usage Extent: <b>USA - Texas - SPCS - N</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("SPCS83 Texas North zone (US Survey feet)")
    public void EPSG_15357() throws FactoryException {
        name       = "SPCS83 Texas North zone (US Survey feet)";
        aliases    = new String[] {"Texas North (ftUS)"};
        methodName = "Lambert Conic Conformal (2SP)";
        createAndVerifyProjection(15357);
    }

    /**
     * Tests “SPCS83 Texas South Central zone (meters)” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>14234</b></li>
     *   <li>EPSG coordinate operation name: <b>SPCS83 Texas South Central zone (meters)</b></li>
     *   <li>Alias(es) given by EPSG: <b>Texas South Central</b>, <b>Texas CS83 South Central zone</b></li>
     *   <li>Coordinate operation method: <b>Lambert Conic Conformal (2SP)</b></li>
     *   <li>EPSG Usage Extent: <b>USA - Texas - SPCS83 - SC</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("SPCS83 Texas South Central zone (meters)")
    public void EPSG_14234() throws FactoryException {
        name       = "SPCS83 Texas South Central zone (meters)";
        aliases    = new String[] {"Texas South Central", "Texas CS83 South Central zone"};
        methodName = "Lambert Conic Conformal (2SP)";
        createAndVerifyProjection(14234);
    }

    /**
     * Tests “SPCS83 Texas South Central zone (US Survey feet)” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>15360</b></li>
     *   <li>EPSG coordinate operation name: <b>SPCS83 Texas South Central zone (US Survey feet)</b></li>
     *   <li>Alias(es) given by EPSG: <b>Texas South Central (ftUS)</b></li>
     *   <li>Coordinate operation method: <b>Lambert Conic Conformal (2SP)</b></li>
     *   <li>EPSG Usage Extent: <b>USA - Texas - SPCS83 - SC</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("SPCS83 Texas South Central zone (US Survey feet)")
    public void EPSG_15360() throws FactoryException {
        name       = "SPCS83 Texas South Central zone (US Survey feet)";
        aliases    = new String[] {"Texas South Central (ftUS)"};
        methodName = "Lambert Conic Conformal (2SP)";
        createAndVerifyProjection(15360);
    }

    /**
     * Tests “SPCS83 Texas South zone (meters)” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>14235</b></li>
     *   <li>EPSG coordinate operation name: <b>SPCS83 Texas South zone (meters)</b></li>
     *   <li>Alias(es) given by EPSG: <b>Texas South</b>, <b>Texas CS83 South zone</b></li>
     *   <li>Coordinate operation method: <b>Lambert Conic Conformal (2SP)</b></li>
     *   <li>EPSG Usage Extent: <b>USA - Texas - SPCS83 - S</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("SPCS83 Texas South zone (meters)")
    public void EPSG_14235() throws FactoryException {
        name       = "SPCS83 Texas South zone (meters)";
        aliases    = new String[] {"Texas South", "Texas CS83 South zone"};
        methodName = "Lambert Conic Conformal (2SP)";
        createAndVerifyProjection(14235);
    }

    /**
     * Tests “SPCS83 Texas South zone (US Survey feet)” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>15361</b></li>
     *   <li>EPSG coordinate operation name: <b>SPCS83 Texas South zone (US Survey feet)</b></li>
     *   <li>Alias(es) given by EPSG: <b>Texas South (ftUS)</b></li>
     *   <li>Coordinate operation method: <b>Lambert Conic Conformal (2SP)</b></li>
     *   <li>EPSG Usage Extent: <b>USA - Texas - SPCS83 - S</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("SPCS83 Texas South zone (US Survey feet)")
    public void EPSG_15361() throws FactoryException {
        name       = "SPCS83 Texas South zone (US Survey feet)";
        aliases    = new String[] {"Texas South (ftUS)"};
        methodName = "Lambert Conic Conformal (2SP)";
        createAndVerifyProjection(15361);
    }

    /**
     * Tests “SPCS83 Wyoming West Central zone (meters)” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>14933</b></li>
     *   <li>EPSG coordinate operation name: <b>SPCS83 Wyoming West Central zone (meters)</b></li>
     *   <li>Alias(es) given by EPSG: <b>Wyoming West Central</b>, <b>Wyoming CS83 West Central zone</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>USA - Wyoming - SPCS - WC</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("SPCS83 Wyoming West Central zone (meters)")
    public void EPSG_14933() throws FactoryException {
        name       = "SPCS83 Wyoming West Central zone (meters)";
        aliases    = new String[] {"Wyoming West Central", "Wyoming CS83 West Central zone"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(14933);
    }

    /**
     * Tests “SPCS83 Wyoming West Central zone (US Survey feet)” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>14937</b></li>
     *   <li>EPSG coordinate operation name: <b>SPCS83 Wyoming West Central zone (US Survey feet)</b></li>
     *   <li>Alias(es) given by EPSG: <b>Wyoming CS83 West Central zone (ftUS)</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>USA - Wyoming - SPCS - WC</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("SPCS83 Wyoming West Central zone (US Survey feet)")
    public void EPSG_14937() throws FactoryException {
        name       = "SPCS83 Wyoming West Central zone (US Survey feet)";
        aliases    = new String[] {"Wyoming CS83 West Central zone (ftUS)"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(14937);
    }

    /**
     * Tests “Stereo 33” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>19927</b></li>
     *   <li>EPSG coordinate operation name: <b>Stereo 33</b></li>
     *   <li>Coordinate operation method: <b>Oblique Stereographic</b></li>
     *   <li>EPSG Usage Extent: <b>Romania</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Stereo 33")
    public void EPSG_19927() throws FactoryException {
        name       = "Stereo 33";
        methodName = "Oblique Stereographic";
        createAndVerifyProjection(19927);
    }

    /**
     * Tests “Stereo 70” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>19926</b></li>
     *   <li>EPSG coordinate operation name: <b>Stereo 70</b></li>
     *   <li>Coordinate operation method: <b>Oblique Stereographic</b></li>
     *   <li>EPSG Usage Extent: <b>Romania</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Stereo 70")
    public void EPSG_19926() throws FactoryException {
        name       = "Stereo 70";
        methodName = "Oblique Stereographic";
        createAndVerifyProjection(19926);
    }

    /**
     * Tests “Sud Algerie” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>18022</b></li>
     *   <li>EPSG coordinate operation name: <b>Sud Algerie</b></li>
     *   <li>Coordinate operation method: <b>Lambert Conic Conformal (1SP)</b></li>
     *   <li>EPSG Usage Extent: <b>Algeria - 31°30'N to 34°39'N</b></li>
     * </ul>
     *
     * Remarks: Check not old parameters.
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Sud Algerie")
    public void EPSG_18022() throws FactoryException {
        name       = "Sud Algerie";
        methodName = "Lambert Conic Conformal (1SP)";
        createAndVerifyProjection(18022);
    }

    /**
     * Tests “Sud Tunisie” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>18182</b></li>
     *   <li>EPSG coordinate operation name: <b>Sud Tunisie</b></li>
     *   <li>Coordinate operation method: <b>Lambert Conic Conformal (1SP)</b></li>
     *   <li>EPSG Usage Extent: <b>Tunisia - south of 34°39'N</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Sud Tunisie")
    public void EPSG_18182() throws FactoryException {
        name       = "Sud Tunisie";
        methodName = "Lambert Conic Conformal (1SP)";
        createAndVerifyProjection(18182);
    }

    /**
     * Tests “Syria Lambert” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>19948</b></li>
     *   <li>EPSG coordinate operation name: <b>Syria Lambert</b></li>
     *   <li>Coordinate operation method: <b>Lambert Conic Conformal (1SP)</b></li>
     *   <li>EPSG Usage Extent: <b>Asia - Middle East - Lebanon and Syria onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Syria Lambert")
    public void EPSG_19948() throws FactoryException {
        name       = "Syria Lambert";
        methodName = "Lambert Conic Conformal (1SP)";
        createAndVerifyProjection(19948);
    }

    /**
     * Tests “Texas CS27 Central zone” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>14203</b></li>
     *   <li>EPSG coordinate operation name: <b>Texas CS27 Central zone</b></li>
     *   <li>Alias(es) given by EPSG: <b>Texas Central</b></li>
     *   <li>Coordinate operation method: <b>Lambert Conic Conformal (2SP)</b></li>
     *   <li>EPSG Usage Extent: <b>USA - Texas - SPCS - C</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Texas CS27 Central zone")
    public void EPSG_14203() throws FactoryException {
        name       = "Texas CS27 Central zone";
        aliases    = new String[] {"Texas Central"};
        methodName = "Lambert Conic Conformal (2SP)";
        createAndVerifyProjection(14203);
    }

    /**
     * Tests “Texas CS27 North zone” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>14201</b></li>
     *   <li>EPSG coordinate operation name: <b>Texas CS27 North zone</b></li>
     *   <li>Alias(es) given by EPSG: <b>Texas North</b></li>
     *   <li>Coordinate operation method: <b>Lambert Conic Conformal (2SP)</b></li>
     *   <li>EPSG Usage Extent: <b>USA - Texas - SPCS - N</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Texas CS27 North zone")
    public void EPSG_14201() throws FactoryException {
        name       = "Texas CS27 North zone";
        aliases    = new String[] {"Texas North"};
        methodName = "Lambert Conic Conformal (2SP)";
        createAndVerifyProjection(14201);
    }

    /**
     * Tests “Texas CS27 South Central zone” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>14204</b></li>
     *   <li>EPSG coordinate operation name: <b>Texas CS27 South Central zone</b></li>
     *   <li>Alias(es) given by EPSG: <b>Texas South Central</b></li>
     *   <li>Coordinate operation method: <b>Lambert Conic Conformal (2SP)</b></li>
     *   <li>EPSG Usage Extent: <b>USA - Texas - SPCS27 - SC</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Texas CS27 South Central zone")
    public void EPSG_14204() throws FactoryException {
        name       = "Texas CS27 South Central zone";
        aliases    = new String[] {"Texas South Central"};
        methodName = "Lambert Conic Conformal (2SP)";
        createAndVerifyProjection(14204);
    }

    /**
     * Tests “Texas CS27 South zone” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>14205</b></li>
     *   <li>EPSG coordinate operation name: <b>Texas CS27 South zone</b></li>
     *   <li>Alias(es) given by EPSG: <b>Texas South</b></li>
     *   <li>Coordinate operation method: <b>Lambert Conic Conformal (2SP)</b></li>
     *   <li>EPSG Usage Extent: <b>USA - Texas - SPCS27 - S</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Texas CS27 South zone")
    public void EPSG_14205() throws FactoryException {
        name       = "Texas CS27 South zone";
        aliases    = new String[] {"Texas South"};
        methodName = "Lambert Conic Conformal (2SP)";
        createAndVerifyProjection(14205);
    }

    /**
     * Tests “TM 0 N” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>16400</b></li>
     *   <li>EPSG coordinate operation name: <b>TM 0 N</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>UK - offshore - North Sea</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("TM 0 N")
    public void EPSG_16400() throws FactoryException {
        name       = "TM 0 N";
        methodName = "Transverse Mercator";
        createAndVerifyProjection(16400);
    }

    /**
     * Tests “TM 109 SE” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>16709</b></li>
     *   <li>EPSG coordinate operation name: <b>TM 109 SE</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>Indonesia - Java Sea - offshore northwest Java</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("TM 109 SE")
    public void EPSG_16709() throws FactoryException {
        name       = "TM 109 SE";
        methodName = "Transverse Mercator";
        createAndVerifyProjection(16709);
    }

    /**
     * Tests “TM 11.30 SE” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>16611</b></li>
     *   <li>EPSG coordinate operation name: <b>TM 11.30 SE</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>Angola - offshore block 15</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("TM 11.30 SE")
    public void EPSG_16611() throws FactoryException {
        name       = "TM 11.30 SE";
        methodName = "Transverse Mercator";
        createAndVerifyProjection(16611);
    }

    /**
     * Tests “TM 12 SE” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>16612</b></li>
     *   <li>EPSG coordinate operation name: <b>TM 12 SE</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>Angola - Angola proper - offshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("TM 12 SE")
    public void EPSG_16612() throws FactoryException {
        name       = "TM 12 SE";
        methodName = "Transverse Mercator";
        createAndVerifyProjection(16612);
    }

    /**
     * Tests “TM 1 NW” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>17001</b></li>
     *   <li>EPSG coordinate operation name: <b>TM 1 NW</b></li>
     *   <li>Alias(es) given by EPSG: <b>Ghana TM</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>Ghana - offshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("TM 1 NW")
    public void EPSG_17001() throws FactoryException {
        name       = "TM 1 NW";
        aliases    = new String[] {"Ghana TM"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(17001);
    }

    /**
     * Tests “TM 5 NE” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>16405</b></li>
     *   <li>EPSG coordinate operation name: <b>TM 5 NE</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>Netherlands - offshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("TM 5 NE")
    public void EPSG_16405() throws FactoryException {
        name       = "TM 5 NE";
        methodName = "Transverse Mercator";
        createAndVerifyProjection(16405);
    }

    /**
     * Tests “TM 5 NW” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>17005</b></li>
     *   <li>EPSG coordinate operation name: <b>TM 5 NW</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>Cote d'Ivoire (Ivory Coast) - offshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("TM 5 NW")
    public void EPSG_17005() throws FactoryException {
        name       = "TM 5 NW";
        methodName = "Transverse Mercator";
        createAndVerifyProjection(17005);
    }

    /**
     * Tests “Trinidad Grid” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>19925</b></li>
     *   <li>EPSG coordinate operation name: <b>Trinidad Grid</b></li>
     *   <li>Coordinate operation method: <b>Cassini-Soldner</b></li>
     *   <li>EPSG Usage Extent: <b>Trinidad and Tobago - Trinidad</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Trinidad Grid")
    public void EPSG_19925() throws FactoryException {
        name       = "Trinidad Grid";
        methodName = "Cassini-Soldner";
        createAndVerifyProjection(19925);
    }

    /**
     * Tests “Trinidad Grid (Clarke's feet)” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>19975</b></li>
     *   <li>EPSG coordinate operation name: <b>Trinidad Grid (Clarke's feet)</b></li>
     *   <li>Coordinate operation method: <b>Cassini-Soldner</b></li>
     *   <li>EPSG Usage Extent: <b>Trinidad and Tobago - Trinidad</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Trinidad Grid (Clarke's feet)")
    public void EPSG_19975() throws FactoryException {
        name       = "Trinidad Grid (Clarke's feet)";
        methodName = "Cassini-Soldner";
        createAndVerifyProjection(19975);
    }

    /**
     * Tests “UTM” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>various</b></li>
     *   <li>EPSG coordinate operation name: <b>UTM</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("UTM")
    public void variousUTM() throws FactoryException {
        name       = "UTM";
        methodName = "Transverse Mercator";
        for (int code = 16001; code <= 16060; code++) {    // Loop over 60 codes
            createAndVerifyProjection(code);
        }
        for (int code = 16101; code <= 16160; code++) {    // Loop over 60 codes
            createAndVerifyProjection(code);
        }
    }

    /**
     * Tests “Wyoming CS27 West Central zone” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation code: <b>14903</b></li>
     *   <li>EPSG coordinate operation name: <b>Wyoming CS27 West Central zone</b></li>
     *   <li>Alias(es) given by EPSG: <b>Wyoming West Central</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     *   <li>EPSG Usage Extent: <b>USA - Wyoming - SPCS - WC</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Wyoming CS27 West Central zone")
    public void EPSG_14903() throws FactoryException {
        name       = "Wyoming CS27 West Central zone";
        aliases    = new String[] {"Wyoming West Central"};
        methodName = "Transverse Mercator";
        createAndVerifyProjection(14903);
    }
}
