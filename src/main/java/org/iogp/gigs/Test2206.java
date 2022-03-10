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

        // Map projection identifier.
        assertContainsCode("Conversion.getIdentifiers()", "EPSG", code, conversion.getIdentifiers());

        // Map projection and operation name.
        if (isStandardNameSupported) {
            configurationTip = Configuration.Key.isStandardNameSupported;
            assertEquals(methodName, getVerifiableName(conversion.getMethod()), "Conversion.getMethod().getName()");

            final String actual = getVerifiableName(conversion);
            assertEquals(name, actual.substring(0, Math.min(name.length(), actual.length())), "Conversion.getName()");
            configurationTip = null;
        }
    }

    /**
     * Tests “3-degree Gauss-Kruger” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation codes: <b>various</b></li>
     *   <li>EPSG coordinate operation name: <b>3-degree Gauss-Kruger</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("3-degree Gauss-Kruger")
    public void test3DegreeGaussKruger() throws FactoryException {
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
        for (int code = 16261; code <= 16299; code++) {         // Loop over 39 codes
            createAndVerifyProjection(code);
        }
        for (int code = 16362; code <= 16398; code += 2) {      // Loop over 19 codes
            createAndVerifyProjection(code);
        }
    }

    /**
     * Tests “6-degree Gauss-Kruger” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation codes: <b>various</b></li>
     *   <li>EPSG coordinate operation name: <b>6-degree Gauss-Kruger</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("6-degree Gauss-Kruger")
    public void test6DegreeGaussKruger() throws FactoryException {
        name       = "6-degree Gauss-Kruger";
        methodName = "Transverse Mercator";
        for (int code = 16201; code <= 16260; code++) {    // Loop over 60 codes
            createAndVerifyProjection(code);
        }
    }

    /**
     * Tests “Alaska CS27” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation codes: <b>15002</b>, <b>15003</b>, <b>15004</b>,
     *       <b>15005</b>, <b>15006</b>, <b>15007</b>, <b>15008</b>, <b>15009</b></li>
     *   <li>EPSG coordinate operation name: <b>Alaska CS27</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Alaska CS27")
    public void testAlaskaCS27() throws FactoryException {
        name       = "Alaska CS27";
        methodName = "Transverse Mercator";
        for (int code = 15002; code <= 15009; code++) {    // Loop over 8 codes
            createAndVerifyProjection(code);
        }
    }

    /**
     * Tests “Aramco Lambert” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation codes: <b>19977</b></li>
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
     * Tests “Argentina” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation codes: <b>18031</b>, <b>18032</b>, <b>18033</b>,
     *       <b>18034</b>, <b>18035</b>, <b>18036</b>, <b>18037</b></li>
     *   <li>EPSG coordinate operation name: <b>Argentina</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Argentina")
    public void testArgentina() throws FactoryException {
        name       = "Argentina";
        methodName = "Transverse Mercator";
        for (int code = 18031; code <= 18037; code++) {    // Loop over 7 codes
            createAndVerifyProjection(code);
        }
    }

    /**
     * Tests “Australian Map Grid” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation codes: <b>various</b></li>
     *   <li>EPSG coordinate operation name: <b>Australian Map Grid</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Australian Map Grid")
    public void testAustralianMapGrid() throws FactoryException {
        name       = "Australian Map Grid";
        methodName = "Transverse Mercator";
        for (int code = 17448; code <= 17458; code++) {    // Loop over 11 codes
            createAndVerifyProjection(code);
        }
    }

    /**
     * Tests “BLM” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation codes: <b>15914</b>, <b>15915</b>, <b>15916</b>, <b>15917</b></li>
     *   <li>EPSG coordinate operation name: <b>BLM</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("BLM")
    public void testBLM() throws FactoryException {
        name       = "BLM";
        methodName = "Transverse Mercator";
        for (int code = 15914; code <= 15917; code++) {    // Loop over 4 codes
            createAndVerifyProjection(code);
        }
    }

    /**
     * Tests “Borneo RSO” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation codes: <b>19894</b></li>
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
     *   <li>EPSG coordinate operation codes: <b>19941</b></li>
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
     *   <li>EPSG coordinate operation codes: <b>19916</b></li>
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
     * Tests “California CS27” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation codes: <b>10404</b>, <b>10405</b>, <b>10406</b>, <b>10408</b></li>
     *   <li>EPSG coordinate operation name: <b>California CS27</b></li>
     *   <li>Coordinate operation method: <b>Lambert Conic Conformal (2SP)</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("California CS27")
    public void testCaliforniaCS27() throws FactoryException {
        name       = "California CS27";
        methodName = "Lambert Conic Conformal (2SP)";
        createAndVerifyProjection(10404);
        createAndVerifyProjection(10405);
        createAndVerifyProjection(10406);
        createAndVerifyProjection(10408);
    }

    /**
     * Tests “Colombia Bogota zone” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation codes: <b>18052</b></li>
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
     *   <li>EPSG coordinate operation codes: <b>18053</b></li>
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
     *   <li>EPSG coordinate operation codes: <b>18054</b></li>
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
     *   <li>EPSG coordinate operation codes: <b>18057</b></li>
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
     *   <li>EPSG coordinate operation codes: <b>18058</b></li>
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
     *   <li>EPSG coordinate operation codes: <b>18059</b></li>
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
     *   <li>EPSG coordinate operation codes: <b>18055</b></li>
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
     *   <li>EPSG coordinate operation codes: <b>18056</b></li>
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
     *   <li>EPSG coordinate operation codes: <b>18051</b></li>
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
     *   <li>EPSG coordinate operation codes: <b>18071</b></li>
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
     *   <li>EPSG coordinate operation codes: <b>18074</b></li>
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
     *   <li>EPSG coordinate operation codes: <b>18073</b></li>
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
     *   <li>EPSG coordinate operation codes: <b>18072</b></li>
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
     *   <li>EPSG coordinate operation codes: <b>19931</b></li>
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
     * Tests “Gauss-Kruger” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation codes: <b>various</b></li>
     *   <li>EPSG coordinate operation name: <b>Gauss-Kruger</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Gauss-Kruger")
    public void testGaussKruger() throws FactoryException {
        name       = "Gauss-Kruger";
        methodName = "Transverse Mercator";
        for (int code = 16301; code <= 16360; code++) {    // Loop over 60 codes
            createAndVerifyProjection(code);
        }
    }

    /**
     * Tests “Ghana National Grid” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation codes: <b>19959</b></li>
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
     * Tests “India” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation codes: <b>18231</b>, <b>18232</b>, <b>18233</b>, <b>18234</b>, <b>18235</b></li>
     *   <li>EPSG coordinate operation name: <b>India</b></li>
     *   <li>Coordinate operation method: <b>Lambert Conic Conformal (1SP)</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("India")
    public void testIndia() throws FactoryException {
        name       = "India";
        methodName = "Lambert Conic Conformal (1SP)";
        for (int code = 18231; code <= 18235; code++) {    // Loop over 5 codes
            createAndVerifyProjection(code);
        }
        createAndVerifyProjection(18236);
        createAndVerifyProjection(18237);
    }

    /**
     * Tests “India zone IIb (1937 metres)” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation codes: <b>18238</b></li>
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
     * Tests “Iraq zone” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation codes: <b>19906</b></li>
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
     *   <li>EPSG coordinate operation codes: <b>18121</b>, <b>18122</b></li>
     *   <li>EPSG coordinate operation name: <b>Italy</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Italy")
    public void testItaly() throws FactoryException {
        name       = "Italy";
        methodName = "Transverse Mercator";
        createAndVerifyProjection(18121);
        createAndVerifyProjection(18122);
    }

    /**
     * Tests “Laborde Grid” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation codes: <b>19861</b></li>
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
     *   <li>EPSG coordinate operation codes: <b>19911</b></li>
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
     * Tests “Lambert” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation codes: <b>18081</b>, <b>18082</b>, <b>18083</b></li>
     *   <li>EPSG coordinate operation name: <b>Lambert</b></li>
     *   <li>Coordinate operation method: <b>Lambert Conic Conformal (1SP)</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Lambert")
    public void testLambert() throws FactoryException {
        name       = "Lambert";
        methodName = "Lambert Conic Conformal (1SP)";
        createAndVerifyProjection(18081);
        createAndVerifyProjection(18082);
        createAndVerifyProjection(18083);
    }

    /**
     * Tests “Lambert-93” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation codes: <b>18085</b></li>
     *   <li>EPSG coordinate operation name: <b>Lambert-93</b></li>
     *   <li>Coordinate operation method: <b>Lambert Conic Conformal (2SP)</b></li>
     *   <li>Specific usage / Remarks: <b>Use grads</b></li>
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
     * Tests “Levant zone” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation codes: <b>19940</b></li>
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
     *   <li>EPSG coordinate operation codes: <b>18240</b>, <b>18241</b>, <b>18242</b>, <b>18243</b>,
     *       <b>18244</b>, <b>18245</b>, <b>18246</b>, <b>18247</b>, <b>18248</b></li>
     *   <li>EPSG coordinate operation name: <b>Libya</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Libya")
    public void testLibya() throws FactoryException {
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
     *   <li>EPSG coordinate operation codes: <b>18310</b>, <b>18311</b>, <b>18312</b>, <b>18313</b>,
     *       <b>18314</b>, <b>18315</b>, <b>18316</b>, <b>18317</b>, <b>18318</b>, <b>18319</b></li>
     *   <li>EPSG coordinate operation name: <b>Libya TM</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Libya TM")
    public void testLibyaTM() throws FactoryException {
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
     *   <li>EPSG coordinate operation codes: <b>11701</b></li>
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
     *   <li>EPSG coordinate operation codes: <b>11702</b></li>
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
     *   <li>EPSG coordinate operation codes: <b>12112</b></li>
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
     *   <li>EPSG coordinate operation codes: <b>12113</b></li>
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
     *   <li>EPSG coordinate operation codes: <b>19905</b></li>
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
     *   <li>EPSG coordinate operation codes: <b>13001</b></li>
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
     *   <li>EPSG coordinate operation codes: <b>19917</b></li>
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
     *   <li>EPSG coordinate operation codes: <b>18141</b></li>
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
     *   <li>EPSG coordinate operation codes: <b>18142</b></li>
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
     *   <li>EPSG coordinate operation codes: <b>19971</b></li>
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
     *   <li>EPSG coordinate operation codes: <b>18153</b></li>
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
     *   <li>EPSG coordinate operation codes: <b>18152</b></li>
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
     *   <li>EPSG coordinate operation codes: <b>18151</b></li>
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
     *   <li>EPSG coordinate operation codes: <b>18021</b></li>
     *   <li>EPSG coordinate operation name: <b>Nord Algerie</b></li>
     *   <li>Coordinate operation method: <b>Lambert Conic Conformal (1SP)</b></li>
     *   <li>Specific usage / Remarks: <b>Check not old parameters</b></li>
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
     *   <li>EPSG coordinate operation codes: <b>18181</b></li>
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
     *   <li>EPSG coordinate operation codes: <b>13501</b></li>
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
     *   <li>EPSG coordinate operation codes: <b>13502</b></li>
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
     *   <li>EPSG coordinate operation codes: <b>19895</b></li>
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
     *   <li>EPSG coordinate operation codes: <b>18162</b></li>
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
     *   <li>EPSG coordinate operation codes: <b>18163</b></li>
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
     *   <li>EPSG coordinate operation codes: <b>18161</b></li>
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
     *   <li>EPSG coordinate operation codes: <b>18171</b>, <b>18172</b>, <b>18173</b>, <b>18174</b>, <b>18175</b></li>
     *   <li>EPSG coordinate operation name: <b>Philippines</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("Philippines")
    public void testPhilippines() throws FactoryException {
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
     *   <li>EPSG coordinate operation codes: <b>19953</b></li>
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
     *   <li>EPSG coordinate operation codes: <b>19919</b></li>
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
     *   <li>EPSG coordinate operation codes: <b>19914</b></li>
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
     *   <li>EPSG coordinate operation codes: <b>19956</b></li>
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
     *   <li>EPSG coordinate operation codes: <b>19957</b></li>
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
     *   <li>EPSG coordinate operation codes: <b>19958</b></li>
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
     *   <li>EPSG coordinate operation codes: <b>19871</b></li>
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
     *   <li>EPSG coordinate operation codes: <b>19872</b></li>
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
     *   <li>EPSG coordinate operation codes: <b>17611</b></li>
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
     * Tests “SPCS83 Alaska” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation codes: <b>15032</b>, <b>15034</b>, <b>15035</b>,
     *       <b>15036</b>, <b>15037</b>, <b>15038</b>, <b>15039</b></li>
     *   <li>EPSG coordinate operation name: <b>SPCS83 Alaska</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("SPCS83 Alaska")
    public void testSPCS83Alaska() throws FactoryException {
        name       = "SPCS83 Alaska";
        methodName = "Transverse Mercator";
        createAndVerifyProjection(15032);
        for (int code = 15034; code <= 15039; code++) {    // Loop over 6 codes
            createAndVerifyProjection(code);
        }
    }

    /**
     * Tests “SPCS83 California” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation codes: <b>10434</b>, <b>10435</b>, <b>10436</b>,
     *       <b>15310</b>, <b>15311</b>, <b>15312</b></li>
     *   <li>EPSG coordinate operation name: <b>SPCS83 California</b></li>
     *   <li>Coordinate operation method: <b>Lambert Conic Conformal (2SP)</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("SPCS83 California")
    public void testSPCS83California() throws FactoryException {
        name       = "SPCS83 California";
        methodName = "Lambert Conic Conformal (2SP)";
        createAndVerifyProjection(10434);
        createAndVerifyProjection(10435);
        createAndVerifyProjection(10436);
        createAndVerifyProjection(15310);
        createAndVerifyProjection(15311);
        createAndVerifyProjection(15312);
    }

    /**
     * Tests “SPCS83 Louisiana North zone (meters)” coordinate operation creation from the factory.
     *
     * <ul>
     *   <li>EPSG coordinate operation codes: <b>11731</b></li>
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
     *   <li>EPSG coordinate operation codes: <b>15391</b></li>
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
     *   <li>EPSG coordinate operation codes: <b>11732</b></li>
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
     *   <li>EPSG coordinate operation codes: <b>15392</b></li>
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
     *   <li>EPSG coordinate operation codes: <b>15334</b></li>
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
     *   <li>EPSG coordinate operation codes: <b>12142</b></li>
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
     *   <li>EPSG coordinate operation codes: <b>15335</b></li>
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
     *   <li>EPSG coordinate operation codes: <b>12143</b></li>
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
     *   <li>EPSG coordinate operation codes: <b>13031</b></li>
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
     *   <li>EPSG coordinate operation codes: <b>15339</b></li>
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
     *   <li>EPSG coordinate operation codes: <b>13531</b></li>
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
     *   <li>EPSG coordinate operation codes: <b>15349</b></li>
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
     *   <li>EPSG coordinate operation codes: <b>13532</b></li>
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
     *   <li>EPSG coordinate operation codes: <b>15350</b></li>
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
     *   <li>EPSG coordinate operation codes: <b>14233</b></li>
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
     *   <li>EPSG coordinate operation codes: <b>15359</b></li>
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
     *   <li>EPSG coordinate operation codes: <b>14232</b></li>
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
     *   <li>EPSG coordinate operation codes: <b>15358</b></li>
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
     *   <li>EPSG coordinate operation codes: <b>14231</b></li>
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
     *   <li>EPSG coordinate operation codes: <b>15357</b></li>
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
     *   <li>EPSG coordinate operation codes: <b>14234</b></li>
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
     *   <li>EPSG coordinate operation codes: <b>15360</b></li>
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
     *   <li>EPSG coordinate operation codes: <b>14235</b></li>
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
     *   <li>EPSG coordinate operation codes: <b>15361</b></li>
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
     *   <li>EPSG coordinate operation codes: <b>14933</b></li>
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
     *   <li>EPSG coordinate operation codes: <b>14937</b></li>
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
     *   <li>EPSG coordinate operation codes: <b>19927</b></li>
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
     *   <li>EPSG coordinate operation codes: <b>19926</b></li>
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
     *   <li>EPSG coordinate operation codes: <b>18022</b></li>
     *   <li>EPSG coordinate operation name: <b>Sud Algerie</b></li>
     *   <li>Coordinate operation method: <b>Lambert Conic Conformal (1SP)</b></li>
     *   <li>Specific usage / Remarks: <b>Check not old parameters</b></li>
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
     *   <li>EPSG coordinate operation codes: <b>18182</b></li>
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
     *   <li>EPSG coordinate operation codes: <b>19948</b></li>
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
     *   <li>EPSG coordinate operation codes: <b>14203</b></li>
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
     *   <li>EPSG coordinate operation codes: <b>14201</b></li>
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
     *   <li>EPSG coordinate operation codes: <b>14204</b></li>
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
     *   <li>EPSG coordinate operation codes: <b>14205</b></li>
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
     *   <li>EPSG coordinate operation codes: <b>16400</b></li>
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
     *   <li>EPSG coordinate operation codes: <b>16709</b></li>
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
     *   <li>EPSG coordinate operation codes: <b>16611</b></li>
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
     *   <li>EPSG coordinate operation codes: <b>16612</b></li>
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
     *   <li>EPSG coordinate operation codes: <b>17001</b></li>
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
     *   <li>EPSG coordinate operation codes: <b>16405</b></li>
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
     *   <li>EPSG coordinate operation codes: <b>17005</b></li>
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
     *   <li>EPSG coordinate operation codes: <b>19925</b></li>
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
     *   <li>EPSG coordinate operation codes: <b>19975</b></li>
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
     *   <li>EPSG coordinate operation codes: <b>various</b></li>
     *   <li>EPSG coordinate operation name: <b>UTM</b></li>
     *   <li>Coordinate operation method: <b>Transverse Mercator</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the coordinate operation from the EPSG code.
     */
    @Test
    @DisplayName("UTM")
    public void testUTM() throws FactoryException {
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
     *   <li>EPSG coordinate operation codes: <b>14903</b></li>
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
