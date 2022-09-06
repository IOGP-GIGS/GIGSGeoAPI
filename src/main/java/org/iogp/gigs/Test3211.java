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
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.CRSFactory;
import org.opengis.referencing.crs.GeodeticCRS;
import org.opengis.referencing.crs.ProjectedCRS;
import org.opengis.referencing.crs.VerticalCRS;
import org.opengis.referencing.cs.CSFactory;
import org.opengis.referencing.datum.DatumFactory;
import org.opengis.referencing.datum.GeodeticDatum;
import org.opengis.referencing.operation.CoordinateOperationFactory;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.MathTransformFactory;
import org.opengis.referencing.operation.OperationMethod;
import org.opengis.referencing.operation.Transformation;
import org.iogp.gigs.internal.geoapi.Configuration;
import org.iogp.gigs.internal.geoapi.Pending;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
 *   <td><a href="https://github.com/IOGP-GIGS/GIGSTestDataset/blob/main/GIGSTestDatasetFiles/GIGS%203200%20User-defined%20Geodetic%20Data%20Objects%20test%20data/ASCII/GIGS_user_3211_VertTfm.txt">{@code GIGS_user_3211_VertTfm.txt}</a>
 * </tr><tr>
 *   <th>Tested API:</th>
 *   <td>{@link MathTransformFactory#getDefaultParameters(String)} and<br>
 *       {@code CoordinateOperationFactory.createTransformation(Map, CoordinateReferenceSystem, CoordinateReferenceSystem, OperationMethod, MathTransform)}.</td>
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
 *         super(new MyFactories());
 *     }
 * }
 * }
 *
 * @author  Michael Arneson (INT)
 * @author  Martin Desruisseaux (Geomatys)
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
     * The parameters defining the transformation to create.
     * This field is set by all test methods before to create and verify the {@link Transformation} instance.
     */
    private SimpleParameter[] parameters;

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
    protected final CoordinateOperationFactory copFactory;

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
     * Data about the source CRS of the transformation.
     *
     * @see #createSourceCRS(TestMethod)
     */
    private final Test3210 sourceCRSTest;

    /**
     * Data about the target CRS of the transformation.
     *
     * @see #createTargetCRS(TestMethod)
     */
    private final Test3210 targetCRSTest;

    /**
     * The source CRS of the transformation created by this factory.
     */
    private VerticalCRS sourceCRS;

    /**
     * The target CRS of the transformation created by this factory.
     */
    private VerticalCRS targetCRS;

    /**
     * Creates a new test using the given factories.
     * The factories needed by this class are {@link CoordinateOperationFactory},
     * {@link MathTransformFactory}, {@link CRSFactory}, {@link CSFactory}, {@link DatumFactory}
     * and optionally {@link CRSAuthorityFactory}.
     * If a requested factory is {@code null}, then the tests which depend on it will be skipped.
     *
     * <h4>Authority factory usage</h4>
     * The authority factory is used only for some test cases where the components are fetched by EPSG codes
     * instead of being built by user. Those test cases are identified by the "definition source" line in Javadoc.
     *
     * @param factories  factories for creating the instances to test.
     */
    public Test3211(final Factories factories) {
        mtFactory           = factories.mtFactory;
        copFactory          = factories.copFactory;
        crsFactory          = factories.crsFactory;
        csFactory           = factories.csFactory;
        datumFactory        = factories.datumFactory;
        crsAuthorityFactory = factories.crsAuthorityFactory;

        sourceCRSTest = new Test3210(factories);
        sourceCRSTest.skipTests = true;
        sourceCRSTest.skipIdentificationCheck = true;

        targetCRSTest = new Test3210(factories);
        targetCRSTest.skipTests = true;
        targetCRSTest.skipIdentificationCheck = true;
    }

    /**
     * Returns information about the configuration of the test which has been run.
     * This method returns a map containing:
     *
     * <ul>
     *   <li>All the following values associated to the {@link Configuration.Key} of the same name:
     *     <ul>
     *       <li>{@link #isFactoryPreservingUserValues}</li>
     *       <li>{@link #mtFactory}</li>
     *       <li>{@link #copFactory}</li>
     *       <li>{@link #crsFactory}</li>
     *       <li>{@link #csFactory}</li>
     *       <li>{@link #datumFactory}</li>
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
        assertNull(op.put(Configuration.Key.mtFactory,           mtFactory));
        assertNull(op.put(Configuration.Key.copFactory,          copFactory));
        assertNull(op.put(Configuration.Key.datumFactory,        datumFactory));
        assertNull(op.put(Configuration.Key.csFactory,           csFactory));
        assertNull(op.put(Configuration.Key.crsFactory,          crsFactory));
        assertNull(op.put(Configuration.Key.crsAuthorityFactory, crsAuthorityFactory));
        return op;
    }

    /**
     * Creates a user-defined source CRS by executing the specified method from the {@link Test3210} class.
     *
     * @param  factory           the test method to use for creating the source CRS.
     * @throws FactoryException  if an error occurred while creating the source CRS.
     */
    private void createSourceCRS(final TestMethod<Test3210> factory) throws FactoryException {
        factory.initialize(sourceCRSTest);
        sourceCRS = sourceCRSTest.getIdentifiedObject();
    }

    /**
     * Creates a user-defined target CRS by executing the specified method from the {@link Test3210} class.
     *
     * @param  factory           the test method to use for creating the target CRS.
     * @throws FactoryException  if an error occurred while creating the target CRS.
     */
    private void createTargetCRS(final TestMethod<Test3210> factory) throws FactoryException {
        factory.initialize(targetCRSTest);
        targetCRS = targetCRSTest.getIdentifiedObject();
    }

    /**
     * Creates a target CRS from the EPSG factory
     *
     * @param  code  EPSG code of the target CRS to create.
     * @throws FactoryException  if an error occurred while creating the target CRS.
     */
    private void createTargetCRS(final int code) throws FactoryException {
        targetCRS = crsAuthorityFactory.createVerticalCRS(String.valueOf(code));
    }

    /**
     * Instantiates the {@link #parameters} field.
     *
     * @param  method      name of the transformation method.
     * @param  definition  all parameter values defining the operation.
     */
    @SuppressWarnings("AssignmentToCollectionOrArrayFieldFromParameter")
    private void createParameters(final String method, final SimpleParameter... definition) {
        methodName = method;
        parameters = definition;
        properties.put(Transformation.OPERATION_VERSION_KEY, "GIGS Transformation");
    }

    /**
     * Returns the vertical transformation instance to be tested. When this method is invoked for the first time, it
     * creates the transformation to test by invoking the {@link MathTransformFactory#getDefaultParameters(String)}
     * method with the current {@link #methodName} value in argument and then specifying the parameters by invoking
     * {@link MathTransformFactory#createParameterizedTransform(ParameterValueGroup)}.
     *
     * In order to create a transformation that is created from CRS and parameters,
     * an implementation-specific API needs to be called.
     * Details of the implementation-specific part are internal to GIGS test suite
     * and will tentatively be removed with GeoAPI 3.1.
     *
     * The created object is then cached and returned in all subsequent invocations of this method.
     *
     * @return the transformation instance to test.
     * @throws FactoryException if an error occurred while creating the transformation instance.
     */
    @Override
    public Transformation getIdentifiedObject() throws FactoryException {
        if (transformation == null) {
            assumeNotNull(mtFactory);
            final ParameterValueGroup definition;
            try {
                definition = mtFactory.getDefaultParameters(methodName);
            } catch (NoSuchIdentifierException e) {
                unsupportedCode(OperationMethod.class, methodName, e);
                throw e;
            }
            for (final SimpleParameter p : parameters) {
                p.setValueInto(definition);
            }
            validators.validate(definition);
            MathTransform transform = mtFactory.createParameterizedTransform(definition);
            OperationMethod method = mtFactory.getLastMethodUsed();
            assumeNotNull(copFactory);
            transformation = Pending.getInstance(copFactory).createTransformation(properties, sourceCRS, targetCRS, method, definition, transform);
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
        @SuppressWarnings("LocalVariableHidesMemberVariable")
        final Transformation transformation = getIdentifiedObject();
        assertNotNull(transformation, "Transformation");
        validators.validate(transformation);

        sourceCRSTest.copyConfigurationFrom(this);
        sourceCRSTest.setIdentifiedObject(sourceCRS);
//TODO  sourceCRSTest.verifyVerticalCRS();

        targetCRSTest.copyConfigurationFrom(this);
        targetCRSTest.setIdentifiedObject(targetCRS);
//TODO  targetCRSTest.verifyVerticalCRS();
        /*
         * Verifies that the parameter values provided by the implementation are equal to the expected values.
         * If the actual group contains more parameters than expected, the extra parameters are ignored.
         * Parameter order and parameter descriptors are ignored.
         */
        if (isFactoryPreservingUserValues) {
            verifyIdentification(transformation.getMethod(), methodName, null);
            final ParameterValueGroup definition = transformation.getParameterValues();
            assertNotNull(definition, "Transformation.getParameterValues()");
            for (final SimpleParameter p : parameters) {
                p.verify(definition);
            }
        }
    }

    /**
     * Tests “GIGS vertCRS U1 height to GIGS vertCRS V1 height” transformation from the factory.
     *
     * <ul>
     *   <li>GIGS transformation code: <b>61503</b></li>
     *   <li>EPSG Transformation Method: <b>Vertical Offset and Slope</b></li>
     * </ul>
     * <table class="gigs">
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
    @DisplayName("GIGS vertCRS U1 height to GIGS vertCRS V1 height")
    public void GIGS_61503() throws FactoryException {
        setCodeAndName(61503, "GIGS vertCRS U1 height to GIGS vertCRS V1 height");
        createSourceCRS(Test3210::GIGS_64501);
        createTargetCRS(Test3210::GIGS_64505);
        createParameters("Vertical Offset and Slope",
            new SimpleParameter("Ordinate 1 of evaluation point", 52, units.degree()),
            new SimpleParameter("Ordinate 2 of evaluation point", 3, units.degree()),
            new SimpleParameter("Vertical offset", -0.486, units.metre()),
            new SimpleParameter("Inclination in latitude", -0.003, units.arcSecond()),
            new SimpleParameter("Inclination in longitude", 0.006, units.arcSecond()));
        verifyTransformation();
    }

    /**
     * Tests “GIGS vertCRS V1 depth to GIGS vertCRS W1 depth” transformation from the factory.
     *
     * <ul>
     *   <li>GIGS transformation code: <b>65440</b></li>
     *   <li>EPSG Transformation Method: <b>Vertical offset</b></li>
     *   <li>EPSG equivalence: <b>5440 – Baltic 1977 depth to Caspian depth (1)</b></li>
     * </ul>
     * <table class="gigs">
     *   <caption>Transformation parameters</caption>
     *   <tr><th>Parameter name</th><th>Value</th></tr>
     *   <tr><td>Vertical offset</td><td>-28 metres</td></tr>
     * </table>
     *
     * @throws FactoryException if an error occurred while creating the transformation from the properties.
     */
    @Test
    @DisplayName("GIGS vertCRS V1 depth to GIGS vertCRS W1 depth")
    public void GIGS_65440() throws FactoryException {
        setCodeAndName(65440, "GIGS vertCRS V1 depth to GIGS vertCRS W1 depth");
        createSourceCRS(Test3210::GIGS_64506);
        createTargetCRS(Test3210::GIGS_64508);
        createParameters("Vertical Offset",
            new SimpleParameter("Vertical offset", -28, units.metre()));
        verifyTransformation();
    }

    /**
     * Tests “GIGS vertCRS V1 depth to GIGS vertCRS W1 height” transformation from the factory.
     *
     * <ul>
     *   <li>GIGS transformation code: <b>65441</b></li>
     *   <li>EPSG Transformation Method: <b>Vertical offset</b></li>
     * </ul>
     * <table class="gigs">
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
    @DisplayName("GIGS vertCRS V1 depth to GIGS vertCRS W1 height")
    public void GIGS_65441() throws FactoryException {
        setCodeAndName(65441, "GIGS vertCRS V1 depth to GIGS vertCRS W1 height");
        createSourceCRS(Test3210::GIGS_64506);
        createTargetCRS(Test3210::GIGS_64507);
        createParameters("Vertical Offset",
            new SimpleParameter("Vertical offset", 28, units.metre()));
        verifyTransformation();
    }

    /**
     * Tests “GIGS vertCRS V1 depth to MSL depth” transformation from the factory.
     *
     * <ul>
     *   <li>GIGS transformation code: <b>61502</b></li>
     *   <li>EPSG Transformation Method: <b>Vertical offset</b></li>
     * </ul>
     * <table class="gigs">
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
    @DisplayName("GIGS vertCRS V1 depth to MSL depth")
    public void GIGS_61502() throws FactoryException {
        setCodeAndName(61502, "GIGS vertCRS V1 depth to MSL depth");
        createSourceCRS(Test3210::GIGS_64506);
        createTargetCRS(5715);
        createParameters("Vertical Offset",
            new SimpleParameter("Vertical offset", 0, units.metre()));
        verifyTransformation();
    }

    /**
     * Tests “GIGS vertCRS V1 height to GIGS vertCRS U1 height” transformation from the factory.
     *
     * <ul>
     *   <li>GIGS transformation code: <b>65447</b></li>
     *   <li>EPSG Transformation Method: <b>Vertical offset</b></li>
     *   <li>EPSG equivalence: <b>5447 – Baltic 1977 height to Black Sea height (1)</b></li>
     * </ul>
     * <table class="gigs">
     *   <caption>Transformation parameters</caption>
     *   <tr><th>Parameter name</th><th>Value</th></tr>
     *   <tr><td>Vertical offset</td><td>0.4 metre</td></tr>
     * </table>
     *
     * @throws FactoryException if an error occurred while creating the transformation from the properties.
     */
    @Test
    @DisplayName("GIGS vertCRS V1 height to GIGS vertCRS U1 height")
    public void GIGS_65447() throws FactoryException {
        setCodeAndName(65447, "GIGS vertCRS V1 height to GIGS vertCRS U1 height");
        createSourceCRS(Test3210::GIGS_64505);
        createTargetCRS(Test3210::GIGS_64501);
        createParameters("Vertical Offset",
            new SimpleParameter("Vertical offset", 0.4, units.metre()));
        verifyTransformation();
    }

    /**
     * Tests “GIGS vertCRS V1 height to GIGS vertCRS W1 depth” transformation from the factory.
     *
     * <ul>
     *   <li>GIGS transformation code: <b>65400</b></li>
     *   <li>EPSG Transformation Method: <b>Vertical offset</b></li>
     * </ul>
     * <table class="gigs">
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
    @DisplayName("GIGS vertCRS V1 height to GIGS vertCRS W1 depth")
    public void GIGS_65400() throws FactoryException {
        setCodeAndName(65400, "GIGS vertCRS V1 height to GIGS vertCRS W1 depth");
        createSourceCRS(Test3210::GIGS_64505);
        createTargetCRS(Test3210::GIGS_64508);
        createParameters("Vertical Offset",
            new SimpleParameter("Vertical offset", -28, units.metre()));
        verifyTransformation();
    }

    /**
     * Tests “GIGS vertCRS V1 height to GIGS vertCRS W1 height” transformation from the factory.
     *
     * <ul>
     *   <li>GIGS transformation code: <b>65438</b></li>
     *   <li>EPSG Transformation Method: <b>Vertical offset</b></li>
     *   <li>EPSG equivalence: <b>5438 – Baltic 1977 height to Caspian height (1)</b></li>
     * </ul>
     * <table class="gigs">
     *   <caption>Transformation parameters</caption>
     *   <tr><th>Parameter name</th><th>Value</th></tr>
     *   <tr><td>Vertical offset</td><td>28 metres</td></tr>
     * </table>
     *
     * @throws FactoryException if an error occurred while creating the transformation from the properties.
     */
    @Test
    @DisplayName("GIGS vertCRS V1 height to GIGS vertCRS W1 height")
    public void GIGS_65438() throws FactoryException {
        setCodeAndName(65438, "GIGS vertCRS V1 height to GIGS vertCRS W1 height");
        createSourceCRS(Test3210::GIGS_64505);
        createTargetCRS(Test3210::GIGS_64507);
        createParameters("Vertical Offset",
            new SimpleParameter("Vertical offset", 28, units.metre()));
        verifyTransformation();
    }

    /**
     * Tests “GIGS vertCRS V1 height to MSL height” transformation from the factory.
     *
     * <ul>
     *   <li>GIGS transformation code: <b>61501</b></li>
     *   <li>EPSG Transformation Method: <b>Vertical offset</b></li>
     * </ul>
     * <table class="gigs">
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
    @DisplayName("GIGS vertCRS V1 height to MSL height")
    public void GIGS_61501() throws FactoryException {
        setCodeAndName(61501, "GIGS vertCRS V1 height to MSL height");
        createSourceCRS(Test3210::GIGS_64505);
        createTargetCRS(5714);
        createParameters("Vertical Offset",
            new SimpleParameter("Vertical offset", 0, units.metre()));
        verifyTransformation();
    }
}
