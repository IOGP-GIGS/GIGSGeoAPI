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
import org.iogp.gigs.internal.sis.TransformationFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.CRSFactory;
import org.opengis.referencing.crs.VerticalCRS;
import org.opengis.referencing.cs.CSFactory;
import org.opengis.referencing.datum.DatumFactory;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.MathTransformFactory;
import org.opengis.referencing.operation.OperationMethod;
import org.opengis.referencing.operation.Transformation;
import org.opengis.util.FactoryException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Verifies that the software allows correct definition of a user-defined vertical transformations.
 *
 * <table class="gigs">
 * <caption>Test description</caption>
 * <tr>
 *   <th>Test method:</th>
 *   <td>Create user-defined vertical transformation.</td>
 * </tr><tr>
 *   <th>Test data:</th>
 *   <td><a href="doc-files/GIGS_3005_userProjection.csv">{@code GIGS_user_3208_CoordTfm.txt}</a>.</td>
 *   <td><a href="https://github.com/IOGP-GIGS/GIGSTestDataset/blob/main/GIGSTestDatasetFiles/GIGS%203200%20User-defined%20Geodetic%20Data%20Objects%20test%20data/ASCII/GIGS_user_3211_VertTfm.txt">{@code GIGS_user_3211_VertTfm.txt}</a>
 * </tr><tr>
 *   <th>Tested API:</th>
 *   <td>{@link MathTransformFactory#getDefaultParameters(String)} and<br>
 *       {@link TransformationFactory#createTransformation(Map, CoordinateReferenceSystem, CoordinateReferenceSystem, OperationMethod, MathTransform)}.</td>
 * </tr><tr>
 *   <th>Expected result:</th>
 *   <td>The geoscience software should accept the test data. The order in which the transformation parameters
 *       are entered is not critical, although that given in the test dataset is recommended.</td>
 * </tr></table>
 *
 * <h2>Usage example</h2>
 * in order to specify their factories and run the tests in a JUnit framework, implementers can
 * define a subclass in their own test suite as in the example below:
 *
 * {@snippet lang="java" :
 * public class MyTest extends Test3211 {
 *     public MyTest() {
 *         super(new MyMathTransformFactory(), new MyTransformationFactory(),
 *               new MyDatumFactory(), new MyCSFactory(),
 *               new MyCRSFactory(), new MyCRSAuthorityFactory());
 *     }
 * }
 * }
 *
 * @author  Michael Arneson (INT)
 * @version 1.0
 * @since   1.0
 */
@DisplayName("User-defined vertical transformations")
public class Test3211 extends Series3000<Transformation> {
    /**
     * Name of the transformation method.
     */
    public String methodName;

    /**
     * The coordinate transformation created by the factory,
     * or {@code null} if not yet created or if the vertical transformation creation failed.
     */
    private Transformation transformation;

    /**
     * The factory to use for fetching operation methods, or {@code null} if none.
     */
    protected final MathTransformFactory mtFactory;

    /**
     * The factory used to create DefaultTransformation, or {@code null} if none. This factory only works for Apache SIS
     */
    protected final TransformationFactory transformationFactory;

    /**
     * Factory to use for building {@link GeodeticCRS} instances, or {@code null} if none.
     */
    protected final CRSFactory crsFactory;

    /**
     * The factory to use for creating coordinate system instances.
     */
    private final CSFactory csFactory;

    /**
     * Factory to use for building {@link GeodeticDatum} instances, or {@code null} if none.
     * May also be used for building {@link Ellipsoid} and {@link PrimeMeridian} components.
     */
    protected final DatumFactory datumFactory;

    /**
     * Factory to use for building {@link ProjectedCRS} instances, or {@code null} if none.
     * This is the factory used by the {@link #getIdentifiedObject()} method.
     */
    protected final CRSAuthorityFactory crsAuthorityFactory;

    /**
     * The parameters used to create the
     */
    private ParameterValueGroup parameterValueGroup;

    /**
     * Data about the source CRS of the transformation.
     * This is used only for tests with user definitions for CRS components.
     *
     * @see #createSourceCRS(TestMethod)
     */
    private Test3210 sourceCRSTest;

    /**
     * Data about the target CRS of the transformation.
     * This is used only for tests with user definitions for CRS components.
     *
     * @see #createTargetCRS(TestMethod)
     */
    private Test3210 targetCRSTest;

    /**
     * The source CRS of the transformation created by this factory.
     */
    private VerticalCRS sourceCRS;

    /**
     * The target CRS of the transformation created by this factory.
     */
    private VerticalCRS targetCRS;

    /**
     * Creates a new test using the given factory. If a given factory is {@code null},
     * then the tests which depend on it will be skipped.
     *
     * @param mtFactory              factory for creating {@link Transformation} instances.
     * @param transformationFactory  factory for creating {@link Transformation} instances.
     * @param datumFactory           factory for creating {@link GeodeticDatum} instances.
     * @param csFactory              factory for creating {@code CoordinateSystem} instances.
     * @param crsFactory             factory for creating {@link GeodeticCRS} instances.
     * @param crsAuthorityFactory    factory for creating {@link GeodeticCRS} instances.
     */
    public Test3211(final MathTransformFactory mtFactory, TransformationFactory transformationFactory,
                    final DatumFactory datumFactory, final CSFactory csFactory, final CRSFactory crsFactory,
                    CRSAuthorityFactory crsAuthorityFactory)
    {
        this.mtFactory = mtFactory;
        this.transformationFactory = transformationFactory;
        this.datumFactory = datumFactory;
        this.csFactory = csFactory;
        this.crsFactory = crsFactory;
        this.crsAuthorityFactory = crsAuthorityFactory;
    }

    /**
     * Returns information about the configuration of the test which has been run.
     * This method returns a map containing:
     *
     * <ul>
     *   <li>All the following values associated to the {@link org.opengis.test.Configuration.Key} of the same name:
     *     <ul>
     *       <li>{@link #isFactoryPreservingUserValues}</li>
     *       <li>{@link #mtFactory}</li>
     *       <li>{@link #transformationFactory}</li>
     *       <li>{@link #datumFactory}</li>
     *       <li>{@link #csFactory}</li>
     *       <li>{@link #crsFactory}</li>
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
        assertNull(op.put(Configuration.Key.mtFactory, mtFactory));
        assertNull(op.put(Configuration.Key.transformationFactory, transformationFactory));
        assertNull(op.put(Configuration.Key.datumFactory, datumFactory));
        assertNull(op.put(Configuration.Key.csFactory, csFactory));
        assertNull(op.put(Configuration.Key.crsFactory, crsFactory));
        assertNull(op.put(Configuration.Key.crsAuthorityFactory, crsAuthorityFactory));
        return op;
    }

    /**
     * Returns the vertical transformation instance to be tested. When this method is invoked for the first time, it
     * creates the transformation to test by invoking the {@link MathTransformFactory#getDefaultParameters(String)}
     * method with the current {@link #methodName} value in argument and then specifying the parameters by invoking
     * {@link MathTransformFactory#createParameterizedTransform(ParameterValueGroup)}. In order to create a
     * transformation that is created from CRS and parameters, the Apache SIS api needs to be called. The call to the
     * Apache SIS api is done by invoking {@link TransformationFactory#createTransformation(Map,
     * CoordinateReferenceSystem, CoordinateReferenceSystem, OperationMethod, MathTransform)}. The created object
     * is then cached and returned in all subsequent invocations of this method.
     *
     * @return the transformation instance to test.
     * @throws FactoryException if an error occurred while creating the transformation instance.
     */
    @Override
    public Transformation getIdentifiedObject() throws FactoryException {
        if (transformation == null) {
            MathTransform transform = mtFactory.createParameterizedTransform(parameterValueGroup);
            OperationMethod method = mtFactory.getLastMethodUsed();
            transformation = transformationFactory.createTransformation(properties, sourceCRS, targetCRS, method, transform);
        }
        return transformation;
    }

    /**
     * Verifies the properties of the vertical transformation given by {@link #getIdentifiedObject()}.
     *
     * @throws FactoryException if an error occurred while creating the transformation.
     */
    private void verifyTransformation() throws FactoryException {
        if (skipTests) {
            return;
        }
        final Transformation transformation = getIdentifiedObject();
        assertNotNull(transformation, "Transformation");
        validators.validate(transformation);

        if (sourceCRSTest != null) {
            sourceCRSTest.copyConfigurationFrom(this);
            sourceCRSTest.setIdentifiedObject(sourceCRS);
            sourceCRSTest.verifyVerticalCRS();
        }
        if (targetCRSTest != null) {
            targetCRSTest.copyConfigurationFrom(this);
            targetCRSTest.setIdentifiedObject(targetCRS);
            targetCRSTest.verifyVerticalCRS();
        }

        // Operation method.
        final OperationMethod method = transformation.getMethod();
        assertNotNull(method, "Transformation.getMethod()");
    }

    /**
     * Instantiates the {@link #parameterValueGroup} field.
     * It is caller's responsibility to set the parameter values.
     *
     * @throws FactoryException if an error occurred while creating the parameters.
     */
    private void createDefaultParameters() throws FactoryException {
        assumeNotNull(mtFactory);
        this.parameterValueGroup = mtFactory.getDefaultParameters(methodName);
    }

    /**
     * Creates a user-defined source CRS by executing the specified method from the {@link Test3210} class.
     *
     * @param  factory           the test method to use for creating the source CRS.
     * @throws FactoryException  if an error occurred while creating the source CRS.
     */
    private void createSourceCRS(final TestMethod<Test3210> factory) throws FactoryException {
        sourceCRSTest = new Test3210(csFactory, crsFactory, datumFactory);
        sourceCRSTest.skipTests = true;
        factory.test(sourceCRSTest);
        sourceCRS = sourceCRSTest.getIdentifiedObject();
    }

    /**
     * Creates a source CRS from the EPSG factory
     *
     * @param  code  EPSG code of the source CRS to create.
     * @throws FactoryException  if an error occurred while creating the source CRS.
     */
    private void createSourceCRS(final int code) throws FactoryException {
        this.sourceCRS = this.crsAuthorityFactory.createVerticalCRS(String.valueOf(code));
    }

    /**
     * Creates a user-defined target CRS by executing the specified method from the {@link Test3210} class.
     *
     * @param  factory           the test method to use for creating the target CRS.
     * @throws FactoryException  if an error occurred while creating the target CRS.
     */
    private void createTargetCRS(final TestMethod<Test3210> factory) throws FactoryException {
        targetCRSTest = new Test3210(csFactory, crsFactory, datumFactory);
        targetCRSTest.skipTests = true;
        factory.test(targetCRSTest);
        targetCRS = targetCRSTest.getIdentifiedObject();
    }

    /**
     * Creates a target CRS from the EPSG factory
     *
     * @param  code  EPSG code of the target CRS to create.
     * @throws FactoryException  if an error occurred while creating the target CRS.
     */
    private void createTargetCRS(final int code) throws FactoryException {
        this.targetCRS = this.crsAuthorityFactory.createVerticalCRS(String.valueOf(code));
    }

    /**
     * Tests “GIGS_61501”  transformation from the factory.
     *
     * <ul>
     *   <li>GIGS transformation code: <b>61501</b></li>
     *   <li>EPSG Transformation Method: <b>Vertical offset</b></li>
     * </ul>
     * <table class="ogc">
     *   <caption>Transformation parameters</caption>
     *   <tr><th>Parameter name</th><th>Value</th></tr>
     *   <tr><td>Vertical offset</td><td>0 metre</td></tr>
     * </table>
     *
     * Remarks: No direct EPSG equivalent.
     *
     * @throws FactoryException if an error occurred while creating the transformation from the properties.
     */
    @Test
    @DisplayName("GIGS_61501")
    public void GIGS_61501() throws FactoryException {
        setCodeAndName(61501, "GIGS_61501");
        properties.put(Transformation.OPERATION_VERSION_KEY, "GIGS Transformation");
        methodName = "Vertical offset";
        createDefaultParameters();
        createSourceCRS(Test3210::GIGS_64505);
        createTargetCRS(5714);
        parameterValueGroup.parameter("Vertical offset").setValue(0, units.metre());
        verifyTransformation();
    }

    /**
     * Tests “GIGS_61502”  transformation from the factory.
     *
     * <ul>
     *   <li>GIGS transformation code: <b>61502</b></li>
     *   <li>EPSG Transformation Method: <b>Vertical offset</b></li>
     * </ul>
     * <table class="ogc">
     *   <caption>Transformation parameters</caption>
     *   <tr><th>Parameter name</th><th>Value</th></tr>
     *   <tr><td>Vertical offset</td><td>0 metre</td></tr>
     * </table>
     *
     * Remarks: No direct EPSG equivalent.
     *
     * @throws FactoryException if an error occurred while creating the transformation from the properties.
     */
    @Test
    @DisplayName("GIGS_61502")
    public void GIGS_61502() throws FactoryException {
        setCodeAndName(61502, "GIGS_61502");
        properties.put(Transformation.OPERATION_VERSION_KEY, "GIGS Transformation");
        methodName = "Vertical offset";
        createDefaultParameters();
        createSourceCRS(Test3210::GIGS_64506);
        createTargetCRS(5715);
        parameterValueGroup.parameter("Vertical offset").setValue(0, units.metre());
        verifyTransformation();
    }

    /**
     * Tests “GIGS_61503”  transformation from the factory.
     *
     * <ul>
     *   <li>GIGS transformation code: <b>61503</b></li>
     *   <li>EPSG Transformation Method: <b>Vertical Offset and Slope</b></li>
     * </ul>
     * <table class="ogc">
     *   <caption>Transformation parameters</caption>
     *   <tr><th>Parameter name</th><th>Value</th></tr>
     *   <tr><td>Ordinate 1 of evaluation point</td><td>52°</td></tr>
     *   <tr><td>Ordinate 2 of evaluation point</td><td>3°</td></tr>
     *   <tr><td>Vertical offset</td><td>-0.486 metre</td></tr>
     *   <tr><td>Inclination in latitude</td><td>-0.003 arc-second</td></tr>
     *   <tr><td>Inclination in longitude</td><td>0.006 arc-second</td></tr>
     * </table>
     *
     * Remarks: No direct EPSG equivalent.
     * Extracted subset of 1503 NAD83 to NAD83(HARN) (30).
     *
     * @throws FactoryException if an error occurred while creating the transformation from the properties.
     */
    @Test
    @DisplayName("GIGS_61503")
    public void GIGS_61503() throws FactoryException {
        setCodeAndName(61503, "GIGS_61503");
        properties.put(Transformation.OPERATION_VERSION_KEY, "GIGS Transformation");
        methodName = "Vertical Offset and Slope";
        createDefaultParameters();
        createSourceCRS(Test3210::GIGS_64501);
        createTargetCRS(Test3210::GIGS_64505);
        parameterValueGroup.parameter("Ordinate 1 of evaluation point").setValue(52, units.degree());
        parameterValueGroup.parameter("Ordinate 2 of evaluation point").setValue(3, units.degree());
        parameterValueGroup.parameter("Vertical offset").setValue(-0.486, units.metre());
        parameterValueGroup.parameter("Inclination in latitude").setValue(-0.003, units.arcSecond());
        parameterValueGroup.parameter("Inclination in longitude").setValue(0.006, units.arcSecond());
        verifyTransformation();
    }

    /**
     * Tests “GIGS_65400”  transformation from the factory.
     *
     * <ul>
     *   <li>GIGS transformation code: <b>65400</b></li>
     *   <li>EPSG Transformation Method: <b>Vertical offset</b></li>
     * </ul>
     * <table class="ogc">
     *   <caption>Transformation parameters</caption>
     *   <tr><th>Parameter name</th><th>Value</th></tr>
     *   <tr><td>Vertical offset</td><td>-28 metres</td></tr>
     * </table>
     *
     * Remarks: EPSG equivalent deprecated but remains relevant.
     * Previous equivalent 5400 Baltic height to Caspian depth (1).
     *
     * @throws FactoryException if an error occurred while creating the transformation from the properties.
     */
    @Test
    @DisplayName("GIGS_65400")
    public void GIGS_65400() throws FactoryException {
        setCodeAndName(65400, "GIGS_65400");
        properties.put(Transformation.OPERATION_VERSION_KEY, "GIGS Transformation");
        methodName = "Vertical offset";
        createDefaultParameters();
        createSourceCRS(Test3210::GIGS_64505);
        createTargetCRS(Test3210::GIGS_64508);
        parameterValueGroup.parameter("Vertical offset").setValue(-28, units.metre());
        verifyTransformation();
    }

    /**
     * Tests “GIGS_65438”  transformation from the factory.
     *
     * <ul>
     *   <li>GIGS transformation code: <b>65438</b></li>
     *   <li>EPSG Transformation Method: <b>Vertical offset</b></li>
     *   <li>EPSG equivalence: <b>5438 – Baltic 1977 height to Caspian height (1)</b></li>
     * </ul>
     * <table class="ogc">
     *   <caption>Transformation parameters</caption>
     *   <tr><th>Parameter name</th><th>Value</th></tr>
     *   <tr><td>Vertical offset</td><td>28 metres</td></tr>
     * </table>
     *
     * @throws FactoryException if an error occurred while creating the transformation from the properties.
     */
    @Test
    @DisplayName("GIGS_65438")
    public void GIGS_65438() throws FactoryException {
        setCodeAndName(65438, "GIGS_65438");
        properties.put(Transformation.OPERATION_VERSION_KEY, "GIGS Transformation");
        methodName = "Vertical offset";
        createDefaultParameters();
        createSourceCRS(Test3210::GIGS_64505);
        createTargetCRS(Test3210::GIGS_64507);
        parameterValueGroup.parameter("Vertical offset").setValue(28, units.metre());
        verifyTransformation();
    }

    /**
     * Tests “GIGS_65440”  transformation from the factory.
     *
     * <ul>
     *   <li>GIGS transformation code: <b>65440</b></li>
     *   <li>EPSG Transformation Method: <b>Vertical offset</b></li>
     *   <li>EPSG equivalence: <b>5440 – Baltic 1977 depth to Caspian depth (1)</b></li>
     * </ul>
     * <table class="ogc">
     *   <caption>Transformation parameters</caption>
     *   <tr><th>Parameter name</th><th>Value</th></tr>
     *   <tr><td>Vertical offset</td><td>-28 metres</td></tr>
     * </table>
     *
     * @throws FactoryException if an error occurred while creating the transformation from the properties.
     */
    @Test
    @DisplayName("GIGS_65440")
    public void GIGS_65440() throws FactoryException {
        setCodeAndName(65440, "GIGS_65440");
        properties.put(Transformation.OPERATION_VERSION_KEY, "GIGS Transformation");
        methodName = "Vertical offset";
        createDefaultParameters();
        createSourceCRS(Test3210::GIGS_64506);
        createTargetCRS(Test3210::GIGS_64508);
        parameterValueGroup.parameter("Vertical offset").setValue(-28, units.metre());
        verifyTransformation();
    }

    /**
     * Tests “GIGS_65441”  transformation from the factory.
     *
     * <ul>
     *   <li>GIGS transformation code: <b>65441</b></li>
     *   <li>EPSG Transformation Method: <b>Vertical offset</b></li>
     * </ul>
     * <table class="ogc">
     *   <caption>Transformation parameters</caption>
     *   <tr><th>Parameter name</th><th>Value</th></tr>
     *   <tr><td>Vertical offset</td><td>28 metres</td></tr>
     * </table>
     *
     * Remarks: EPSG equivalent deprecated but remains relevant.
     * Previous equivalent 5441 Baltic depth to Caspian height (1).
     *
     * @throws FactoryException if an error occurred while creating the transformation from the properties.
     */
    @Test
    @DisplayName("GIGS_65441")
    public void GIGS_65441() throws FactoryException {
        setCodeAndName(65441, "GIGS_65441");
        properties.put(Transformation.OPERATION_VERSION_KEY, "GIGS Transformation");
        methodName = "Vertical offset";
        createDefaultParameters();
        createSourceCRS(Test3210::GIGS_64506);
        createTargetCRS(Test3210::GIGS_64507);
        parameterValueGroup.parameter("Vertical offset").setValue(28, units.metre());
        verifyTransformation();
    }

    /**
     * Tests “GIGS_65447”  transformation from the factory.
     *
     * <ul>
     *   <li>GIGS transformation code: <b>65447</b></li>
     *   <li>EPSG Transformation Method: <b>Vertical offset</b></li>
     *   <li>EPSG equivalence: <b>5447 – Baltic 1977 height to Black Sea height (1)</b></li>
     * </ul>
     * <table class="ogc">
     *   <caption>Transformation parameters</caption>
     *   <tr><th>Parameter name</th><th>Value</th></tr>
     *   <tr><td>Vertical offset</td><td>0.4 metre</td></tr>
     * </table>
     *
     * @throws FactoryException if an error occurred while creating the transformation from the properties.
     */
    @Test
    @DisplayName("GIGS_65447")
    public void GIGS_65447() throws FactoryException {
        setCodeAndName(65447, "GIGS_65447");
        properties.put(Transformation.OPERATION_VERSION_KEY, "GIGS Transformation");
        methodName = "Vertical offset";
        createDefaultParameters();
        createSourceCRS(Test3210::GIGS_64505);
        createTargetCRS(Test3210::GIGS_64501);
        parameterValueGroup.parameter("Vertical offset").setValue(0.4, units.metre());
        verifyTransformation();
    }
}
