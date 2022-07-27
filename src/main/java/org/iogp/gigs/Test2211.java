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
 * Verifies reference vertical transformations bundled with the geoscience software.
 *
 * <table class="gigs">
 * <caption>Test description</caption>
 * <tr>
 *   <th>Test method:</th>
 *   <td>Compare transformation definitions included in the software against the EPSG Dataset.</td>
 * </tr><tr>
 *   <th>Test data:</th>
 *   <td><a href="https://github.com/IOGP-GIGS/GIGSTestDataset/tree/main/GIGSTestDatasetFiles/GIGS%202200%20Predefined%20Geodetic%20Data%20Objects%20test%20data/ASCII/GIGS_lib_2211_VertTfm.txt">{@code GIGS_lib_2211_VertTfm.txt}</a>
 *       and EPSG Dataset.</td>
 * </tr><tr>
 *   <th>Tested API:</th>
 *   <td>{@link CoordinateOperationAuthorityFactory#createCoordinateOperation(String)}.</td>
 * </tr><tr>
 *   <th>Expected result:</th>
 *   <td>Transformation definitions bundled with the software should have same name, method name,
 *       defining parameters and parameter values as in EPSG Dataset. See current version of the EPSG Dataset.
 *       The values of the parameters should be correct to at least 10 significant figures.
 *       Transformations missing from the software or at variance with those in the EPSG Dataset should be reported.</td>
 * </tr></table>
 *
 *
 * <h2>Usage example</h2>
 * in order to specify their factories and run the tests in a JUnit framework,
 * implementers can define a subclass in their own test suite as in the example below:
 *
 * {@snippet lang="java" :
 * public class MyTest extends Test2211 {
 *     public MyTest() {
 *         super(new MyCoordinateOperationAuthorityFactory());
 *     }
 * }
 * }
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @author  Alexis Manin (Geomatys)
 * @version 1.0
 * @since   1.0
 */
@DisplayName("Vertical transformation")
public class Test2211 extends Series2000<Transformation> {
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
    public Test2211(final CoordinateOperationAuthorityFactory copFactory) {
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
            if (operation != null) {  // For consistency with the behavior in other classes.
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
        assertAliasesEqual (aliases, transformation, "Transformation");

        // Operation method.
        final OperationMethod m = transformation.getMethod();
        assertNotNull(m, "Transformation.getMethod()");
        assertNameEquals(true, methodName, m, "Transformation.getMethod()");

        if (isOperationVersionSupported) {
            assertEquals(version, transformation.getOperationVersion(), "Transformation.getOperationVersion()");
        }
    }

    /**
     * Tests “Baltic 1977 to AIOC95 depth (1)” transformation creation from the factory.
     *
     * <ul>
     *   <li>EPSG transformation code: <b>5445</b></li>
     *   <li>EPSG transformation name: <b>Baltic 1977 to AIOC95 depth (1)</b></li>
     *   <li>Transformation version: <b>AIOC95-Aze</b></li>
     *   <li>Operation method name: <b>Vertical Offset</b></li>
     *   <li>EPSG Usage Extent: <b>Azerbaijan - offshore and Sangachal¬†Open</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the transformation from the EPSG code.
     */
    @Test
    @DisplayName("Baltic 1977 to AIOC95 depth (1)")
    public void EPSG_5445() throws FactoryException {
        code       = 5445;
        name       = "Baltic 1977 to AIOC95 depth (1)";
        version    = "AIOC95-Aze";
        methodName = "Vertical Offset";
        verifyTransformation();
    }

    /**
     * Tests “Baltic 1977 to AIOC95 height (1)” transformation creation from the factory.
     *
     * <ul>
     *   <li>EPSG transformation code: <b>5443</b></li>
     *   <li>EPSG transformation name: <b>Baltic 1977 to AIOC95 height (1)</b></li>
     *   <li>Transformation version: <b>AIOC95-Aze</b></li>
     *   <li>Operation method name: <b>Vertical Offset</b></li>
     *   <li>EPSG Usage Extent: <b>Azerbaijan - offshore and Sangachal¬†Open</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the transformation from the EPSG code.
     */
    @Test
    @DisplayName("Baltic 1977 to AIOC95 height (1)")
    public void EPSG_5443() throws FactoryException {
        code       = 5443;
        name       = "Baltic 1977 to AIOC95 height (1)";
        version    = "AIOC95-Aze";
        methodName = "Vertical Offset";
        verifyTransformation();
    }

    /**
     * Tests “WGS 84 to EGM96 height (1)” transformation creation from the factory.
     *
     * <ul>
     *   <li>EPSG transformation code: <b>10084</b></li>
     *   <li>EPSG transformation name: <b>WGS 84 to EGM96 height (1)</b></li>
     *   <li>Alias(es) given by EPSG: <b>WGS 84 (G1150) to WGS 84</b>, <b>WGS 84 (G1674) to WGS 84</b>, <b>EGM96 - OHt [1]</b></li>
     *   <li>Transformation version: <b>NGA-World</b></li>
     *   <li>Operation method name: <b>Geographic3D to GravityRelatedHeight (EGM)</b></li>
     *   <li>EPSG Usage Extent: <b>World</b></li>
     * </ul>
     *
     * Remarks: Geoid model.
     *
     * @throws FactoryException if an error occurred while creating the transformation from the EPSG code.
     */
    @Test
    @DisplayName("WGS 84 to EGM96 height (1)")
    public void EPSG_10084() throws FactoryException {
        code       = 10084;
        name       = "WGS 84 to EGM96 height (1)";
        aliases    = new String[] {"WGS 84 (G1150) to WGS 84", "WGS 84 (G1674) to WGS 84", "EGM96 - OHt [1]"};
        version    = "NGA-World";
        methodName = "Geographic3D to GravityRelatedHeight (EGM)";
        verifyTransformation();
    }
}
