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
import org.opengis.referencing.crs.GeographicCRS;
import org.opengis.referencing.crs.ProjectedCRS;
import org.opengis.referencing.cs.CSFactory;
import org.opengis.referencing.datum.DatumAuthorityFactory;
import org.opengis.referencing.datum.DatumFactory;
import org.opengis.referencing.datum.Ellipsoid;
import org.opengis.referencing.datum.GeodeticDatum;
import org.opengis.referencing.datum.PrimeMeridian;
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
 * Verifies that the software allows correct definition of a user-defined transformations.
 *
 * <table class="gigs">
 * <caption>Test description</caption>
 * <tr>
 *   <th>Test method:</th>
 *   <td>Create user-defined transformations.</td>
 * </tr><tr>
 *   <th>Test data:</th>
 *   <td><a href="https://github.com/IOGP-GIGS/GIGSTestDataset/blob/main/GIGSTestDatasetFiles/GIGS%203200%20User-defined%20Geodetic%20Data%20Objects%20test%20data/ASCII/GIGS_user_3208_CoordTfm.txt">{@code GIGS_user_3208_CoordTfm.txt}</a>
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
 *
 * <h2>Usage example</h2>
 * in order to specify their factories and run the tests in a JUnit framework, implementers can
 * define a subclass in their own test suite as in the example below:
 *
 * {@snippet lang="java" :
 * public class MyTest extends Test3208 {
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
@DisplayName("User-defined coordinate transformations")
public class Test3208 extends Series3000<Transformation> {
    /**
     * Name of the transformation method.
     */
    public String methodName;

    /**
     * The parameters defining the transformation to create.
     * This field is set by all test methods before to create and verify the {@link Transformation} instance.
     * The name of this parameter group is typically {@link #methodName}.
     */
    public ParameterValueGroup definition;

    /**
     * The coordinate transformation created by the factory,
     * or {@code null} if not yet created or if the transformation creation failed.
     */
    private Transformation transformation;

    /**
     * The factory to use for fetching operation methods, or {@code null} if none.
     */
    protected final MathTransformFactory mtFactory;

    /**
     * The factory to use for creating transformation instances, or {@code null} if none.
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
     * Factory to use for building {@link GeodeticDatum} and {@link PrimeMeridian} components, or {@code null} if none.
     * This is used only for tests with EPSG codes for datum components.
     */
    protected final DatumAuthorityFactory datumAuthorityFactory;

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
    private final Test3205 sourceCRSTest;

    /**
     * Data about the target CRS of the transformation.
     *
     * @see #createTargetCRS(TestMethod)
     */
    private final Test3205 targetCRSTest;

    /**
     * The source CRS of the transformation created by this test.
     */
    private GeographicCRS sourceCRS;

    /**
     * The target CRS of the transformation created by this test.
     */
    private GeographicCRS targetCRS;

    /**
     * Creates a new test using the given factories.
     * The factories needed by this class are {@link CoordinateOperationFactory},
     * {@link MathTransformFactory}, {@link CRSFactory}, {@link CSFactory}, {@link DatumFactory}
     * and optionally {@link CRSAuthorityFactory} with {@link DatumAuthorityFactory}.
     * If a requested factory is {@code null}, then the tests which depend on it will be skipped.
     *
     * <h4>Authority factory usage</h4>
     * The authority factory is used only for some test cases where the components are fetched by EPSG codes
     * instead of being built by user. Those test cases are identified by the "definition source" line in Javadoc.
     *
     * @param factories  factories for creating the instances to test.
     */
    public Test3208(final Factories factories) {
        mtFactory             = factories.mtFactory;
        copFactory            = factories.copFactory;
        crsFactory            = factories.crsFactory;
        csFactory             = factories.csFactory;
        datumFactory          = factories.datumFactory;
        crsAuthorityFactory   = factories.crsAuthorityFactory;
        datumAuthorityFactory = factories.datumAuthorityFactory;

        sourceCRSTest = new Test3205(factories);
        sourceCRSTest.skipTests = true;
        sourceCRSTest.skipIdentificationCheck = true;

        targetCRSTest = new Test3205(factories);
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
        assertNull(op.put(Configuration.Key.mtFactory,             mtFactory));
        assertNull(op.put(Configuration.Key.copFactory,            copFactory));
        assertNull(op.put(Configuration.Key.crsFactory,            crsFactory));
        assertNull(op.put(Configuration.Key.csFactory,             csFactory));
        assertNull(op.put(Configuration.Key.datumFactory,          datumFactory));
        assertNull(op.put(Configuration.Key.crsAuthorityFactory,   crsAuthorityFactory));
        assertNull(op.put(Configuration.Key.datumAuthorityFactory, datumAuthorityFactory));
        return op;
    }

    /**
     * Creates a user-defined source CRS by executing the specified method from the {@link Test3205} class.
     *
     * @param  factory  the test method to use for creating the source CRS.
     * @throws FactoryException if an error occurred while creating the source CRS.
     * @throws ClassCastException if the CRS is not geographic.
     */
    private void createSourceCRS(final TestMethod<Test3205> factory) throws FactoryException {
        factory.initialize(sourceCRSTest);
        sourceCRS = (GeographicCRS) sourceCRSTest.getIdentifiedObject();
    }

    /**
     * Creates a user-defined target CRS by executing the specified method from the {@link Test3205} class.
     *
     * @param  factory  the test method to use for creating the target CRS.
     * @throws FactoryException if an error occurred while creating the target CRS.
     * @throws ClassCastException if the CRS is not geographic.
     */
    private void createTargetCRS(final TestMethod<Test3205> factory) throws FactoryException {
        factory.initialize(targetCRSTest);
        targetCRS = (GeographicCRS) targetCRSTest.getIdentifiedObject();
    }

    /**
     * Creates a target CRS from the EPSG factory
     *
     * @param  code      EPSG code of the target CRS to create.
     * @param  verifier  the test to use for verifying the object created by EPSG factory.
     * @throws FactoryException  if an error occurred while creating the target CRS.
     */
    private void createTargetCRS(final int code, final TestMethod<Test3205> verifier) throws FactoryException {
        verifier.initialize(targetCRSTest);
        targetCRS = crsAuthorityFactory.createGeographicCRS(String.valueOf(code));
    }

    /**
     * Sets the axis lengths of source and target ellipsoids.
     */
    private void setEllipsoidAxisLengths() {
        final Ellipsoid sourceEllipsoid = sourceCRS.getDatum().getEllipsoid();
        final Ellipsoid targetEllipsoid = targetCRS.getDatum().getEllipsoid();
        definition.parameter("src_semi_major").setValue(sourceEllipsoid.getSemiMajorAxis(), sourceEllipsoid.getAxisUnit());
        definition.parameter("src_semi_minor").setValue(sourceEllipsoid.getSemiMinorAxis(), sourceEllipsoid.getAxisUnit());
        definition.parameter("tgt_semi_major").setValue(targetEllipsoid.getSemiMajorAxis(), targetEllipsoid.getAxisUnit());
        definition.parameter("tgt_semi_minor").setValue(targetEllipsoid.getSemiMinorAxis(), targetEllipsoid.getAxisUnit());
    }

    /**
     * Instantiates the {@link #definition} field.
     * It is caller's responsibility to set the parameter values.
     *
     * @param  method  name of the transformation method.
     */
    private void createDefaultParameters(final String method) {
        methodName = method;
        assumeNotNull(mtFactory);
        try {
            definition = mtFactory.getDefaultParameters(method);
        } catch (NoSuchIdentifierException e) {
            unsupportedCode(OperationMethod.class, method, e);
        }
        properties.put(Transformation.OPERATION_VERSION_KEY, "GIGS Transformation");
    }

    /**
     * Returns the transformation instance to be tested. When this method is invoked for the first time, it creates the
     * transformation to test by invoking the {@link MathTransformFactory#getDefaultParameters(String)}
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
            MathTransform transform = mtFactory.createParameterizedTransform(definition);
            OperationMethod method = mtFactory.getLastMethodUsed();
            transformation = Pending.getInstance(copFactory).createTransformation(properties, sourceCRS, targetCRS, method, transform);
        }
        return transformation;
    }

    /**
     * Sets the transformation instance to verify. This method is invoked only by other test classes which need to
     * verify the transformation contained in a concatenated transformation instead of the transformation immediately
     * after creation.
     *
     * @param  instance  the instance to verify.
     */
    final void setIdentifiedObject(final Transformation instance) {
        transformation = instance;
    }

    /**
     * Verifies the properties of the transformation given by {@link #getIdentifiedObject()}.
     *
     * @throws FactoryException if an error occurred while creating the transformation.
     */
    final void verifyTransformation() throws FactoryException {
        if (skipTests) {
            return;
        }
        @SuppressWarnings("LocalVariableHidesMemberVariable")
        final Transformation transformation = getIdentifiedObject();
        assertNotNull(transformation, "Transformation");
        validators.validate(transformation);

        sourceCRSTest.copyConfigurationFrom(this);
        sourceCRSTest.setIdentifiedObject(sourceCRS);
        sourceCRSTest.verifyGeographicCRS();

        targetCRSTest.copyConfigurationFrom(this);
        targetCRSTest.setIdentifiedObject(targetCRS);
        targetCRSTest.verifyGeographicCRS();

        // Operation method.
        final OperationMethod method = transformation.getMethod();
        assertNotNull(method, "Transformation.getMethod()");
    }

    /**
     * Tests “GIGS geogCRS A to WGS 84 (1)” transformation from the factory.
     *
     * <ul>
     *   <li>GIGS transformation code: <b>61001</b></li>
     *   <li>GIGS transformation name: <b>GIGS geogCRS A to WGS 84 (1)</b></li>
     *   <li>EPSG Transformation Method: <b>Geocentric translations</b></li>
     * </ul>
     * <table class="gigs">
     *   <caption>Transformation parameters</caption>
     *   <tr><th>Parameter name</th><th>Value</th></tr>
     *   <tr><td>X-axis translation</td><td>0 metre</td></tr>
     *   <tr><td>Y-axis translation</td><td>0 metre</td></tr>
     *   <tr><td>Z-axis translation</td><td>0 metre</td></tr>
     * </table>
     *
     * Remarks: No direct EPSG equivalent.
     *
     * @throws FactoryException if an error occurred while creating the transformation from the properties.
     */
    @Test
    @DisplayName("GIGS geogCRS A to WGS 84 (1)")
    public void GIGS_61001() throws FactoryException {
        setCodeAndName(61001, "GIGS geogCRS A to WGS 84 (1)");
        createSourceCRS(Test3205::GIGS_64003);
        createTargetCRS(4326, Test3205::GIGS_64003);
        createDefaultParameters("Geocentric translations (geog2D domain)");
        definition.parameter("X-axis translation").setValue(0.0, units.metre());
        definition.parameter("Y-axis translation").setValue(0.0, units.metre());
        definition.parameter("Z-axis translation").setValue(0.0, units.metre());
        setEllipsoidAxisLengths();
        verifyTransformation();
    }

    /**
     * Tests “GIGS geogCRS B to GIGS geogCRS A (1)” transformation from the factory.
     *
     * <ul>
     *   <li>GIGS transformation code: <b>61196</b></li>
     *   <li>GIGS transformation name: <b>GIGS geogCRS B to GIGS geogCRS A (1)</b></li>
     *   <li>EPSG Transformation Method: <b>Geocentric translations</b></li>
     *   <li>EPSG equivalence: <b>1196 – OSGB36 to WGS 84 (2)</b></li>
     * </ul>
     * <table class="gigs">
     *   <caption>Transformation parameters</caption>
     *   <tr><th>Parameter name</th><th>Value</th></tr>
     *   <tr><td>X-axis translation</td><td>371 metres</td></tr>
     *   <tr><td>Y-axis translation</td><td>-112 metres</td></tr>
     *   <tr><td>Z-axis translation</td><td>434 metres</td></tr>
     * </table>
     *
     * @throws FactoryException if an error occurred while creating the transformation from the properties.
     */
    @Test
    @DisplayName("GIGS geogCRS B to GIGS geogCRS A (1)")
    public void GIGS_61196() throws FactoryException {
        setCodeAndName(61196, "GIGS geogCRS B to GIGS geogCRS A (1)");
        createSourceCRS(Test3205::GIGS_64005);
        createTargetCRS(Test3205::GIGS_64003);
        createDefaultParameters("Geocentric translations (geog2D domain)");
        definition.parameter("X-axis translation").setValue(371.0, units.metre());
        definition.parameter("Y-axis translation").setValue(-112.0, units.metre());
        definition.parameter("Z-axis translation").setValue(434.0, units.metre());
        setEllipsoidAxisLengths();
        verifyTransformation();
    }

    /**
     * Tests “GIGS geogCRS B to GIGS geogCRS A (2)” transformation from the factory.
     *
     * <ul>
     *   <li>GIGS transformation code: <b>61314</b></li>
     *   <li>GIGS transformation name: <b>GIGS geogCRS B to GIGS geogCRS A (2)</b></li>
     *   <li>EPSG Transformation Method: <b>Position Vector 7-param. transformation</b></li>
     *   <li>EPSG equivalence: <b>1314 – OSGB36 to WGS 84 (6)</b></li>
     * </ul>
     * <table class="gigs">
     *   <caption>Transformation parameters</caption>
     *   <tr><th>Parameter name</th><th>Value</th></tr>
     *   <tr><td>X-axis translation</td><td>446.448 metres</td></tr>
     *   <tr><td>Y-axis translation</td><td>-125.157 metres</td></tr>
     *   <tr><td>Z-axis translation</td><td>542.06 metres</td></tr>
     *   <tr><td>X-axis rotation</td><td>0.15 arc-second</td></tr>
     *   <tr><td>Y-axis rotation</td><td>0.247 arc-second</td></tr>
     *   <tr><td>Z-axis rotation</td><td>0.842 arc-second</td></tr>
     *   <tr><td>Scale difference</td><td>-20.489 parts per millions</td></tr>
     * </table>
     *
     * @throws FactoryException if an error occurred while creating the transformation from the properties.
     */
    @Test
    @DisplayName("GIGS geogCRS B to GIGS geogCRS A (2)")
    public void GIGS_61314() throws FactoryException {
        setCodeAndName(61314, "GIGS geogCRS B to GIGS geogCRS A (2)");
        createSourceCRS(Test3205::GIGS_64005);
        createTargetCRS(Test3205::GIGS_64003);
        createDefaultParameters("Position Vector transformation (geog2D domain)");
        definition.parameter("X-axis translation").setValue(446.448, units.metre());
        definition.parameter("Y-axis translation").setValue(-125.157, units.metre());
        definition.parameter("Z-axis translation").setValue(542.06, units.metre());
        definition.parameter("X-axis rotation").setValue(0.15, units.arcSecond());
        definition.parameter("Y-axis rotation").setValue(0.247, units.arcSecond());
        definition.parameter("Z-axis rotation").setValue(0.842, units.arcSecond());
        definition.parameter("Scale difference").setValue(-20.489, units.ppm());
        setEllipsoidAxisLengths();
        verifyTransformation();
    }

    /**
     * Tests “GIGS geogCRS C to GIGS geogCRS A (1)” transformation from the factory.
     *
     * <ul>
     *   <li>GIGS transformation code: <b>61002</b></li>
     *   <li>GIGS transformation name: <b>GIGS geogCRS C to GIGS geogCRS A (1)</b></li>
     *   <li>EPSG Transformation Method: <b>Geocentric translations</b></li>
     * </ul>
     * <table class="gigs">
     *   <caption>Transformation parameters</caption>
     *   <tr><th>Parameter name</th><th>Value</th></tr>
     *   <tr><td>X-axis translation</td><td>593 metres</td></tr>
     *   <tr><td>Y-axis translation</td><td>26 metres</td></tr>
     *   <tr><td>Z-axis translation</td><td>479 metres</td></tr>
     * </table>
     *
     * Remarks: No direct EPSG equivalent.
     *
     * @throws FactoryException if an error occurred while creating the transformation from the properties.
     */
    @Test
    @DisplayName("GIGS geogCRS C to GIGS geogCRS A (1)")
    public void GIGS_61002() throws FactoryException {
        setCodeAndName(61002, "GIGS geogCRS C to GIGS geogCRS A (1)");
        createSourceCRS(Test3205::GIGS_64006);
        createTargetCRS(Test3205::GIGS_64003);
        createDefaultParameters("Geocentric translations (geog2D domain)");
        definition.parameter("X-axis translation").setValue(593.0, units.metre());
        definition.parameter("Y-axis translation").setValue(26.0, units.metre());
        definition.parameter("Z-axis translation").setValue(479.0, units.metre());
        setEllipsoidAxisLengths();
        verifyTransformation();
    }

    /**
     * Tests “GIGS geogCRS C to GIGS geogCRS A (2)” transformation from the factory.
     *
     * <ul>
     *   <li>GIGS transformation code: <b>15934</b></li>
     *   <li>GIGS transformation name: <b>GIGS geogCRS C to GIGS geogCRS A (2)</b></li>
     *   <li>EPSG Transformation Method: <b>Coordinate Frame rotation</b></li>
     *   <li>EPSG equivalence: <b>15934 – Amersfoort to WGS 84 (3)</b></li>
     * </ul>
     * <table class="gigs">
     *   <caption>Transformation parameters</caption>
     *   <tr><th>Parameter name</th><th>Value</th></tr>
     *   <tr><td>X-axis translation</td><td>565.2369 metres</td></tr>
     *   <tr><td>Y-axis translation</td><td>50.0087 metres</td></tr>
     *   <tr><td>Z-axis translation</td><td>465.658 metres</td></tr>
     *   <tr><td>X-axis rotation</td><td>1.9725 microradians</td></tr>
     *   <tr><td>Y-axis rotation</td><td>-1.7004 microradians</td></tr>
     *   <tr><td>Z-axis rotation</td><td>9.0677 microradians</td></tr>
     *   <tr><td>Scale difference</td><td>4.0812 parts per millions</td></tr>
     * </table>
     *
     * @throws FactoryException if an error occurred while creating the transformation from the properties.
     */
    @Test
    @DisplayName("GIGS geogCRS C to GIGS geogCRS A (2)")
    public void GIGS_15934() throws FactoryException {
        setCodeAndName(15934, "GIGS geogCRS C to GIGS geogCRS A (2)");
        createSourceCRS(Test3205::GIGS_64006);
        createTargetCRS(Test3205::GIGS_64003);
        createDefaultParameters("Coordinate Frame rotation (geog2D domain)");
        definition.parameter("X-axis translation").setValue(565.2369, units.metre());
        definition.parameter("Y-axis translation").setValue(50.0087, units.metre());
        definition.parameter("Z-axis translation").setValue(465.658, units.metre());
        definition.parameter("X-axis rotation").setValue(1.9725, units.microradian());
        definition.parameter("Y-axis rotation").setValue(-1.7004, units.microradian());
        definition.parameter("Z-axis rotation").setValue(9.0677, units.microradian());
        definition.parameter("Scale difference").setValue(4.0812, units.ppm());
        setEllipsoidAxisLengths();
        verifyTransformation();
    }

    /**
     * Tests “GIGS geogCRS C to GIGS geogCRS A (3)” transformation from the factory.
     *
     * <ul>
     *   <li>GIGS transformation code: <b>61003</b></li>
     *   <li>GIGS transformation name: <b>GIGS geogCRS C to GIGS geogCRS A (3)</b></li>
     *   <li>EPSG Transformation Method: <b>Molodensky-Badekas 10-parameter transformation</b></li>
     * </ul>
     * <table class="gigs">
     *   <caption>Transformation parameters</caption>
     *   <tr><th>Parameter name</th><th>Value</th></tr>
     *   <tr><td>X-axis translation</td><td>593.0297 metres</td></tr>
     *   <tr><td>Y-axis translation</td><td>26.0038 metres</td></tr>
     *   <tr><td>Z-axis translation</td><td>478.7534 metres</td></tr>
     *   <tr><td>X-axis rotation</td><td>0.4069 arc-second</td></tr>
     *   <tr><td>Y-axis rotation</td><td>-0.3507 arc-second</td></tr>
     *   <tr><td>Z-axis rotation</td><td>1.8703 arc-seconds</td></tr>
     *   <tr><td>Scale difference</td><td>4.0812 parts per millions</td></tr>
     *   <tr><td>Ordinate 1 of evaluation point</td><td>3903453.1482 metres</td></tr>
     *   <tr><td>Ordinate 2 of evaluation point</td><td>368135.3134 metres</td></tr>
     *   <tr><td>Ordinate 3 of evaluation point</td><td>5012970.3051 metres</td></tr>
     * </table>
     *
     * Remarks: No direct EPSG equivalent.
     * Parameter values taken from 15740 Amersfoort to ETRS89 (4).
     *
     * @throws FactoryException if an error occurred while creating the transformation from the properties.
     */
    @Test
    @DisplayName("GIGS geogCRS C to GIGS geogCRS A (3)")
    public void GIGS_61003() throws FactoryException {
        setCodeAndName(61003, "GIGS geogCRS C to GIGS geogCRS A (3)");
        createSourceCRS(Test3205::GIGS_64006);
        createTargetCRS(Test3205::GIGS_64003);
        createDefaultParameters("Molodensky-Badekas (PV geog2D domain)");
        definition.parameter("X-axis translation").setValue(593.0297, units.metre());
        definition.parameter("Y-axis translation").setValue(26.0038, units.metre());
        definition.parameter("Z-axis translation").setValue(478.7534, units.metre());
        definition.parameter("X-axis rotation").setValue(0.4069, units.arcSecond());
        definition.parameter("Y-axis rotation").setValue(-0.3507, units.arcSecond());
        definition.parameter("Z-axis rotation").setValue(1.8703, units.arcSecond());
        definition.parameter("Scale difference").setValue(4.0812, units.ppm());
        definition.parameter("Ordinate 1 of evaluation point").setValue(3903453.1482, units.metre());
        definition.parameter("Ordinate 2 of evaluation point").setValue(368135.3134, units.metre());
        definition.parameter("Ordinate 3 of evaluation point").setValue(5012970.3051, units.metre());
        setEllipsoidAxisLengths();
        verifyTransformation();
    }

    /**
     * Tests “GIGS geogCRS D to GIGS geogCRS L (1)” transformation from the factory.
     *
     * <ul>
     *   <li>GIGS transformation code: <b>61759</b></li>
     *   <li>GIGS transformation name: <b>GIGS geogCRS D to GIGS geogCRS L (1)</b></li>
     *   <li>EPSG Transformation Method: <b>Longitude rotation</b></li>
     *   <li>EPSG equivalence: <b>1759 – Batavia (Jakarta) to Batavia (1)</b></li>
     * </ul>
     * <table class="gigs">
     *   <caption>Transformation parameters</caption>
     *   <tr><th>Parameter name</th><th>Value</th></tr>
     *   <tr><td>Longitude offset</td><td>106°48′27.79″ (106.8077194°)</td></tr>
     * </table>
     *
     * @throws FactoryException if an error occurred while creating the transformation from the properties.
     */
    @Test
    @DisplayName("GIGS geogCRS D to GIGS geogCRS L (1)")
    public void GIGS_61759() throws FactoryException {
        setCodeAndName(61759, "GIGS geogCRS D to GIGS geogCRS L (1)");
        createSourceCRS(Test3205::GIGS_64007);
        createTargetCRS(Test3205::GIGS_64014);
        createDefaultParameters("Longitude rotation");
        definition.parameter("Longitude offset").setValue(106.8077194, units.degree());
        verifyTransformation();
    }

    /**
     * Tests “GIGS geogCRS E to GIGS geogCRS A (1)” transformation from the factory.
     *
     * <ul>
     *   <li>GIGS transformation code: <b>61610</b></li>
     *   <li>GIGS transformation name: <b>GIGS geogCRS E to GIGS geogCRS A (1)</b></li>
     *   <li>EPSG Transformation Method: <b>Geocentric translations</b></li>
     *   <li>EPSG equivalence: <b>1610 – BD72 to WGS 84 (2)</b></li>
     * </ul>
     * <table class="gigs">
     *   <caption>Transformation parameters</caption>
     *   <tr><th>Parameter name</th><th>Value</th></tr>
     *   <tr><td>X-axis translation</td><td>-125.8 metres</td></tr>
     *   <tr><td>Y-axis translation</td><td>79.9 metres</td></tr>
     *   <tr><td>Z-axis translation</td><td>-100.5 metres</td></tr>
     * </table>
     *
     * @throws FactoryException if an error occurred while creating the transformation from the properties.
     */
    @Test
    @DisplayName("GIGS geogCRS E to GIGS geogCRS A (1)")
    public void GIGS_61610() throws FactoryException {
        setCodeAndName(61610, "GIGS geogCRS E to GIGS geogCRS A (1)");
        createSourceCRS(Test3205::GIGS_64008);
        createTargetCRS(Test3205::GIGS_64003);
        createDefaultParameters("Geocentric translations (geog2D domain)");
        definition.parameter("X-axis translation").setValue(-125.8, units.metre());
        definition.parameter("Y-axis translation").setValue(79.9, units.metre());
        definition.parameter("Z-axis translation").setValue(-100.5, units.metre());
        setEllipsoidAxisLengths();
        verifyTransformation();
    }

    /**
     * Tests “GIGS geogCRS E to GIGS geogCRS A (2)” transformation from the factory.
     *
     * <ul>
     *   <li>GIGS transformation code: <b>15929</b></li>
     *   <li>GIGS transformation name: <b>GIGS geogCRS E to GIGS geogCRS A (2)</b></li>
     *   <li>EPSG Transformation Method: <b>Coordinate Frame rotation</b></li>
     *   <li>EPSG equivalence: <b>15929 – BD72 to WGS 84 (3)</b></li>
     * </ul>
     * <table class="gigs">
     *   <caption>Transformation parameters</caption>
     *   <tr><th>Parameter name</th><th>Value</th></tr>
     *   <tr><td>X-axis translation</td><td>-106.8686 metres</td></tr>
     *   <tr><td>Y-axis translation</td><td>52.2978 metres</td></tr>
     *   <tr><td>Z-axis translation</td><td>-103.7239 metres</td></tr>
     *   <tr><td>X-axis rotation</td><td>-0.3366 arc-second</td></tr>
     *   <tr><td>Y-axis rotation</td><td>0.457 arc-second</td></tr>
     *   <tr><td>Z-axis rotation</td><td>-1.8422 arc-seconds</td></tr>
     *   <tr><td>Scale difference</td><td>-1.2747 parts per millions</td></tr>
     * </table>
     *
     * @throws FactoryException if an error occurred while creating the transformation from the properties.
     */
    @Test
    @DisplayName("GIGS geogCRS E to GIGS geogCRS A (2)")
    public void GIGS_15929() throws FactoryException {
        setCodeAndName(15929, "GIGS geogCRS E to GIGS geogCRS A (2)");
        createSourceCRS(Test3205::GIGS_64008);
        createTargetCRS(Test3205::GIGS_64003);
        createDefaultParameters("Coordinate Frame rotation (geog2D domain)");
        definition.parameter("X-axis translation").setValue(-106.8686, units.metre());
        definition.parameter("Y-axis translation").setValue(52.2978, units.metre());
        definition.parameter("Z-axis translation").setValue(-103.7239, units.metre());
        definition.parameter("X-axis rotation").setValue(-0.3366, units.arcSecond());
        definition.parameter("Y-axis rotation").setValue(0.457, units.arcSecond());
        definition.parameter("Z-axis rotation").setValue(-1.8422, units.arcSecond());
        definition.parameter("Scale difference").setValue(-1.2747, units.ppm());
        setEllipsoidAxisLengths();
        verifyTransformation();
    }

    /**
     * Tests “GIGS geogCRS F to GIGS geogCRS A (1)” transformation from the factory.
     *
     * <ul>
     *   <li>GIGS transformation code: <b>61150</b></li>
     *   <li>GIGS transformation name: <b>GIGS geogCRS F to GIGS geogCRS A (1)</b></li>
     *   <li>EPSG Transformation Method: <b>Geocentric translations</b></li>
     *   <li>EPSG equivalence: <b>1150 – GDA94 to WGS 84 (1)</b></li>
     * </ul>
     * <table class="gigs">
     *   <caption>Transformation parameters</caption>
     *   <tr><th>Parameter name</th><th>Value</th></tr>
     *   <tr><td>X-axis translation</td><td>0 metre</td></tr>
     *   <tr><td>Y-axis translation</td><td>0 metre</td></tr>
     *   <tr><td>Z-axis translation</td><td>0 metre</td></tr>
     * </table>
     *
     * @throws FactoryException if an error occurred while creating the transformation from the properties.
     */
    @Test
    @DisplayName("GIGS geogCRS F to GIGS geogCRS A (1)")
    public void GIGS_61150() throws FactoryException {
        setCodeAndName(61150, "GIGS geogCRS F to GIGS geogCRS A (1)");
        createSourceCRS(Test3205::GIGS_64009);
        createTargetCRS(Test3205::GIGS_64003);
        createDefaultParameters("Geocentric translations (geog2D domain)");
        definition.parameter("X-axis translation").setValue(0.0, units.metre());
        definition.parameter("Y-axis translation").setValue(0.0, units.metre());
        definition.parameter("Z-axis translation").setValue(0.0, units.metre());
        setEllipsoidAxisLengths();
        verifyTransformation();
    }

    /**
     * Tests “GIGS geogCRS H to GIGS geogCRS T (1)” transformation from the factory.
     *
     * <ul>
     *   <li>GIGS transformation code: <b>61763</b></li>
     *   <li>GIGS transformation name: <b>GIGS geogCRS H to GIGS geogCRS T (1)</b></li>
     *   <li>EPSG Transformation Method: <b>Longitude rotation</b></li>
     *   <li>EPSG equivalence: <b>1763 – NTF (Paris) to NTF (1)</b></li>
     * </ul>
     * <table class="gigs">
     *   <caption>Transformation parameters</caption>
     *   <tr><th>Parameter name</th><th>Value</th></tr>
     *   <tr><td>Longitude offset</td><td>2.5969213 grads</td></tr>
     * </table>
     *
     * @throws FactoryException if an error occurred while creating the transformation from the properties.
     */
    @Test
    @DisplayName("GIGS geogCRS H to GIGS geogCRS T (1)")
    public void GIGS_61763() throws FactoryException {
        setCodeAndName(61763, "GIGS geogCRS H to GIGS geogCRS T (1)");
        createSourceCRS(Test3205::GIGS_64011);
        createTargetCRS(Test3205::GIGS_64013);
        createDefaultParameters("Longitude rotation");
        definition.parameter("Longitude offset").setValue(2.5969213, units.grad());
        verifyTransformation();
    }

    /**
     * Tests “GIGS geogCRS J to GIGS geogCRS A (1)” transformation from the factory.
     *
     * <ul>
     *   <li>GIGS transformation code: <b>61173</b></li>
     *   <li>GIGS transformation name: <b>GIGS geogCRS J to GIGS geogCRS A (1)</b></li>
     *   <li>EPSG Transformation Method: <b>Geocentric translations</b></li>
     *   <li>EPSG equivalence: <b>1173 – NAD27 to WGS 84 (4)</b></li>
     * </ul>
     * <table class="gigs">
     *   <caption>Transformation parameters</caption>
     *   <tr><th>Parameter name</th><th>Value</th></tr>
     *   <tr><td>X-axis translation</td><td>-8 metres</td></tr>
     *   <tr><td>Y-axis translation</td><td>160 metres</td></tr>
     *   <tr><td>Z-axis translation</td><td>176 metres</td></tr>
     * </table>
     *
     * @throws FactoryException if an error occurred while creating the transformation from the properties.
     */
    @Test
    @DisplayName("GIGS geogCRS J to GIGS geogCRS A (1)")
    public void GIGS_61173() throws FactoryException {
        setCodeAndName(61173, "GIGS geogCRS J to GIGS geogCRS A (1)");
        createSourceCRS(Test3205::GIGS_64012);
        createTargetCRS(Test3205::GIGS_64003);
        createDefaultParameters("Geocentric translations (geog2D domain)");
        definition.parameter("X-axis translation").setValue(-8.0, units.metre());
        definition.parameter("Y-axis translation").setValue(160.0, units.metre());
        definition.parameter("Z-axis translation").setValue(176.0, units.metre());
        setEllipsoidAxisLengths();
        verifyTransformation();
    }

    /**
     * Tests “GIGS geogCRS J to GIGS geogCRS A (2)” transformation from the factory.
     *
     * <ul>
     *   <li>GIGS transformation code: <b>61004</b></li>
     *   <li>GIGS transformation name: <b>GIGS geogCRS J to GIGS geogCRS A (2)</b></li>
     *   <li>EPSG Transformation Method: <b>NADCON</b></li>
     * </ul>
     * <table class="gigs">
     *   <caption>Transformation parameters</caption>
     *   <tr><th>Parameter name</th><th>Value</th></tr>
     *   <tr><td>Latitude difference file</td><td>n_slope.las</td></tr>
     *   <tr><td>Longitude difference file</td><td>n_slope.los</td></tr>
     * </table>
     *
     * Remarks: No direct EPSG equivalent.
     * Files included with GIGS Test Procedure dataset.
     *
     * @throws FactoryException if an error occurred while creating the transformation from the properties.
     */
    @Test
    @DisplayName("GIGS geogCRS J to GIGS geogCRS A (2)")
    public void GIGS_61004() throws FactoryException {
        setCodeAndName(61004, "GIGS geogCRS J to GIGS geogCRS A (2)");
        createSourceCRS(Test3205::GIGS_64012);
        createTargetCRS(Test3205::GIGS_64003);
        createDefaultParameters("NADCON");
        definition.parameter("Latitude difference file").setValue("n_slope.las");
        definition.parameter("Longitude difference file").setValue("n_slope.los");
        verifyTransformation();
    }

    /**
     * Tests “GIGS geogCRS J to GIGS geogCRS A (3)” transformation from the factory.
     *
     * <ul>
     *   <li>GIGS transformation code: <b>61692</b></li>
     *   <li>GIGS transformation name: <b>GIGS geogCRS J to GIGS geogCRS A (3)</b></li>
     *   <li>EPSG Transformation Method: <b>NTv2</b></li>
     *   <li>EPSG equivalence: <b>1692 – NAD27 to WGS 84 (34)</b></li>
     * </ul>
     * <table class="gigs">
     *   <caption>Transformation parameters</caption>
     *   <tr><th>Parameter name</th><th>Value</th></tr>
     *   <tr><td>Latitude and longitude difference file</td><td>QUE27-98.gsb</td></tr>
     * </table>
     *
     * Remarks: File included with GIGS Test Procedure dataset.
     *
     * @throws FactoryException if an error occurred while creating the transformation from the properties.
     */
    @Test
    @DisplayName("GIGS geogCRS J to GIGS geogCRS A (3)")
    public void GIGS_61692() throws FactoryException {
        setCodeAndName(61692, "GIGS geogCRS J to GIGS geogCRS A (3)");
        createSourceCRS(Test3205::GIGS_64012);
        createTargetCRS(Test3205::GIGS_64003);
        createDefaultParameters("NTv2");
        definition.parameter("Latitude and longitude difference file").setValue("QUE27-98.gsb");
        verifyTransformation();
    }

    /**
     * Tests “GIGS geogCRS K to GIGS geogCRS A (1)” transformation from the factory.
     *
     * <ul>
     *   <li>GIGS transformation code: <b>61242</b></li>
     *   <li>GIGS transformation name: <b>GIGS geogCRS K to GIGS geogCRS A (1)</b></li>
     *   <li>EPSG Transformation Method: <b>Geocentric translations</b></li>
     *   <li>EPSG equivalence: <b>1242 – HD72 to WGS 84 (4)</b></li>
     * </ul>
     * <table class="gigs">
     *   <caption>Transformation parameters</caption>
     *   <tr><th>Parameter name</th><th>Value</th></tr>
     *   <tr><td>X-axis translation</td><td>52.17 metres</td></tr>
     *   <tr><td>Y-axis translation</td><td>-71.82 metres</td></tr>
     *   <tr><td>Z-axis translation</td><td>-14.9 metres</td></tr>
     * </table>
     *
     * @throws FactoryException if an error occurred while creating the transformation from the properties.
     */
    @Test
    @DisplayName("GIGS geogCRS K to GIGS geogCRS A (1)")
    public void GIGS_61242() throws FactoryException {
        setCodeAndName(61242, "GIGS geogCRS K to GIGS geogCRS A (1)");
        createSourceCRS(Test3205::GIGS_64015);
        createTargetCRS(Test3205::GIGS_64003);
        createDefaultParameters("Geocentric translations (geog2D domain)");
        definition.parameter("X-axis translation").setValue(52.17, units.metre());
        definition.parameter("Y-axis translation").setValue(-71.82, units.metre());
        definition.parameter("Z-axis translation").setValue(-14.9, units.metre());
        setEllipsoidAxisLengths();
        verifyTransformation();
    }

    /**
     * Tests “GIGS geogCRS L to GIGS geogCRS A (1)” transformation from the factory.
     *
     * <ul>
     *   <li>GIGS transformation code: <b>61123</b></li>
     *   <li>GIGS transformation name: <b>GIGS geogCRS L to GIGS geogCRS A (1)</b></li>
     *   <li>EPSG Transformation Method: <b>Geocentric translations</b></li>
     *   <li>EPSG equivalence: <b>8452 – Batavia to WGS 84 (1)</b></li>
     * </ul>
     * <table class="gigs">
     *   <caption>Transformation parameters</caption>
     *   <tr><th>Parameter name</th><th>Value</th></tr>
     *   <tr><td>X-axis translation</td><td>-377 metres</td></tr>
     *   <tr><td>Y-axis translation</td><td>681 metres</td></tr>
     *   <tr><td>Z-axis translation</td><td>-50 metres</td></tr>
     * </table>
     *
     * Remarks: EPSG tfm 1123 deprecated due to change in area of applicability.
     * Values remain unchanged.
     * GIGS tfm code maintained for continuity.
     *
     * @throws FactoryException if an error occurred while creating the transformation from the properties.
     */
    @Test
    @DisplayName("GIGS geogCRS L to GIGS geogCRS A (1)")
    public void GIGS_61123() throws FactoryException {
        setCodeAndName(61123, "GIGS geogCRS L to GIGS geogCRS A (1)");
        createSourceCRS(Test3205::GIGS_64014);
        createTargetCRS(Test3205::GIGS_64003);
        createDefaultParameters("Geocentric translations (geog2D domain)");
        definition.parameter("X-axis translation").setValue(-377.0, units.metre());
        definition.parameter("Y-axis translation").setValue(681.0, units.metre());
        definition.parameter("Z-axis translation").setValue(-50.0, units.metre());
        setEllipsoidAxisLengths();
        verifyTransformation();
    }

    /**
     * Tests “GIGS geogCRS M to GIGS geogCRS A (1)” transformation from the factory.
     *
     * <ul>
     *   <li>GIGS transformation code: <b>61275</b></li>
     *   <li>GIGS transformation name: <b>GIGS geogCRS M to GIGS geogCRS A (1)</b></li>
     *   <li>EPSG Transformation Method: <b>Geocentric translations</b></li>
     *   <li>EPSG equivalence: <b>1275 – ED50 to WGS 84 (17)</b></li>
     * </ul>
     * <table class="gigs">
     *   <caption>Transformation parameters</caption>
     *   <tr><th>Parameter name</th><th>Value</th></tr>
     *   <tr><td>X-axis translation</td><td>-84 metres</td></tr>
     *   <tr><td>Y-axis translation</td><td>-97 metres</td></tr>
     *   <tr><td>Z-axis translation</td><td>-117 metres</td></tr>
     * </table>
     *
     * @throws FactoryException if an error occurred while creating the transformation from the properties.
     */
    @Test
    @DisplayName("GIGS geogCRS M to GIGS geogCRS A (1)")
    public void GIGS_61275() throws FactoryException {
        setCodeAndName(61275, "GIGS geogCRS M to GIGS geogCRS A (1)");
        createSourceCRS(Test3205::GIGS_64020);
        createTargetCRS(Test3205::GIGS_64003);
        createDefaultParameters("Geocentric translations (geog2D domain)");
        definition.parameter("X-axis translation").setValue(-84.0, units.metre());
        definition.parameter("Y-axis translation").setValue(-97.0, units.metre());
        definition.parameter("Z-axis translation").setValue(-117.0, units.metre());
        setEllipsoidAxisLengths();
        verifyTransformation();
    }

    /**
     * Tests “GIGS geogCRS T to GIGS geogCRS A (1)” transformation from the factory.
     *
     * <ul>
     *   <li>GIGS transformation code: <b>61193</b></li>
     *   <li>GIGS transformation name: <b>GIGS geogCRS T to GIGS geogCRS A (1)</b></li>
     *   <li>EPSG Transformation Method: <b>Geocentric translations</b></li>
     *   <li>EPSG equivalence: <b>1193 – NTF to WGS 84 (1)</b></li>
     * </ul>
     * <table class="gigs">
     *   <caption>Transformation parameters</caption>
     *   <tr><th>Parameter name</th><th>Value</th></tr>
     *   <tr><td>X-axis translation</td><td>-168 metres</td></tr>
     *   <tr><td>Y-axis translation</td><td>-60 metres</td></tr>
     *   <tr><td>Z-axis translation</td><td>320 metres</td></tr>
     * </table>
     *
     * @throws FactoryException if an error occurred while creating the transformation from the properties.
     */
    @Test
    @DisplayName("GIGS geogCRS T to GIGS geogCRS A (1)")
    public void GIGS_61193() throws FactoryException {
        setCodeAndName(61193, "GIGS geogCRS T to GIGS geogCRS A (1)");
        createSourceCRS(Test3205::GIGS_64013);
        createTargetCRS(Test3205::GIGS_64003);
        createDefaultParameters("Geocentric translations (geog2D domain)");
        definition.parameter("X-axis translation").setValue(-168.0, units.metre());
        definition.parameter("Y-axis translation").setValue(-60.0, units.metre());
        definition.parameter("Z-axis translation").setValue(320.0, units.metre());
        setEllipsoidAxisLengths();
        verifyTransformation();
    }

    /**
     * Tests “GIGS geogCRS X to GIGS geogCRS A (1)” transformation from the factory.
     *
     * <ul>
     *   <li>GIGS transformation code: <b>15788</b></li>
     *   <li>GIGS transformation name: <b>GIGS geogCRS X to GIGS geogCRS A (1)</b></li>
     *   <li>EPSG Transformation Method: <b>Geocentric translations</b></li>
     *   <li>EPSG equivalence: <b>15788 – AGD66 to WGS 84 (16)</b></li>
     * </ul>
     * <table class="gigs">
     *   <caption>Transformation parameters</caption>
     *   <tr><th>Parameter name</th><th>Value</th></tr>
     *   <tr><td>X-axis translation</td><td>-127.8 metres</td></tr>
     *   <tr><td>Y-axis translation</td><td>-52.3 metres</td></tr>
     *   <tr><td>Z-axis translation</td><td>152.9 metres</td></tr>
     * </table>
     *
     * @throws FactoryException if an error occurred while creating the transformation from the properties.
     */
    @Test
    @DisplayName("GIGS geogCRS X to GIGS geogCRS A (1)")
    public void GIGS_15788() throws FactoryException {
        setCodeAndName(15788, "GIGS geogCRS X to GIGS geogCRS A (1)");
        createSourceCRS(Test3205::GIGS_64016);
        createTargetCRS(Test3205::GIGS_64003);
        createDefaultParameters("Geocentric translations (geog2D domain)");
        definition.parameter("X-axis translation").setValue(-127.8, units.metre());
        definition.parameter("Y-axis translation").setValue(-52.3, units.metre());
        definition.parameter("Z-axis translation").setValue(152.9, units.metre());
        setEllipsoidAxisLengths();
        verifyTransformation();
    }

    /**
     * Tests “GIGS geogCRS Y to GIGS geogCRS A (1)” transformation from the factory.
     *
     * <ul>
     *   <li>GIGS transformation code: <b>61254</b></li>
     *   <li>GIGS transformation name: <b>GIGS geogCRS Y to GIGS geogCRS A (1)</b></li>
     *   <li>EPSG Transformation Method: <b>Geocentric translations</b></li>
     *   <li>EPSG equivalence: <b>1254 – Pulkovo 1942 to WGS 84 (1)</b></li>
     * </ul>
     * <table class="gigs">
     *   <caption>Transformation parameters</caption>
     *   <tr><th>Parameter name</th><th>Value</th></tr>
     *   <tr><td>X-axis translation</td><td>28 metres</td></tr>
     *   <tr><td>Y-axis translation</td><td>-130 metres</td></tr>
     *   <tr><td>Z-axis translation</td><td>-95 metres</td></tr>
     * </table>
     *
     * @throws FactoryException if an error occurred while creating the transformation from the properties.
     */
    @Test
    @DisplayName("GIGS geogCRS Y to GIGS geogCRS A (1)")
    public void GIGS_61254() throws FactoryException {
        setCodeAndName(61254, "GIGS geogCRS Y to GIGS geogCRS A (1)");
        createSourceCRS(Test3205::GIGS_64017);
        createTargetCRS(Test3205::GIGS_64003);
        createDefaultParameters("Geocentric translations (geog2D domain)");
        definition.parameter("X-axis translation").setValue(28.0, units.metre());
        definition.parameter("Y-axis translation").setValue(-130.0, units.metre());
        definition.parameter("Z-axis translation").setValue(-95.0, units.metre());
        setEllipsoidAxisLengths();
        verifyTransformation();
    }

    /**
     * Tests “GIGS geogCRS Z to GIGS geogCRS A (1)” transformation from the factory.
     *
     * <ul>
     *   <li>GIGS transformation code: <b>61188</b></li>
     *   <li>GIGS transformation name: <b>GIGS geogCRS Z to GIGS geogCRS A (1)</b></li>
     *   <li>EPSG Transformation Method: <b>Geocentric translations</b></li>
     *   <li>EPSG equivalence: <b>1188 – NAD83 to WGS 84 (1)</b></li>
     * </ul>
     * <table class="gigs">
     *   <caption>Transformation parameters</caption>
     *   <tr><th>Parameter name</th><th>Value</th></tr>
     *   <tr><td>X-axis translation</td><td>0 metre</td></tr>
     *   <tr><td>Y-axis translation</td><td>0 metre</td></tr>
     *   <tr><td>Z-axis translation</td><td>0 metre</td></tr>
     * </table>
     *
     * @throws FactoryException if an error occurred while creating the transformation from the properties.
     */
    @Test
    @DisplayName("GIGS geogCRS Z to GIGS geogCRS A (1)")
    public void GIGS_61188() throws FactoryException {
        setCodeAndName(61188, "GIGS geogCRS Z to GIGS geogCRS A (1)");
        createSourceCRS(Test3205::GIGS_64018);
        createTargetCRS(Test3205::GIGS_64003);
        createDefaultParameters("Geocentric translations (geog2D domain)");
        definition.parameter("X-axis translation").setValue(0.0, units.metre());
        definition.parameter("Y-axis translation").setValue(0.0, units.metre());
        definition.parameter("Z-axis translation").setValue(0.0, units.metre());
        setEllipsoidAxisLengths();
        verifyTransformation();
    }
}
