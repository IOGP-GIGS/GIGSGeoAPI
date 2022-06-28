package org.iogp.gigs;

import org.iogp.gigs.internal.geoapi.Configuration;
import org.iogp.gigs.internal.sis.TransformationFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.crs.*;
import org.opengis.referencing.cs.CSFactory;
import org.opengis.referencing.datum.*;
import org.opengis.referencing.operation.*;
import org.opengis.util.FactoryException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies that the software allows correct definition of a user-defined transformations.
 *
 * <table class="gigs">
 * <caption>Test description</caption>
 * <tr>
 *   <th>Test method:</th>
 *   <td>Create user-defined projected CRS.</td>
 * </tr><tr>
 *   <th>Test data:</th>
 *   <td><a href="doc-files/GIGS_3005_userProjection.csv">{@code GIGS_user_3208_CoordTfm.txt}</a>.</td>
 *   <td><a href="https://github.com/IOGP-GIGS/GIGSTestDataset/blob/main/GIGSTestDatasetFiles/GIGS%203200%20User-defined%20Geodetic%20Data%20Objects%20test%20data/ASCII/GIGS_user_3208_CoordTfm.txt">{@code GIGS_user_3208_CoordTfm.txt}</a>
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
 *
 * <h2>Usage example</h2>
 * in order to specify their factories and run the tests in a JUnit framework, implementers can
 * define a subclass in their own test suite as in the example below:
 *
 * <blockquote><pre>public class MyTest extends Test3008 {
 *    public MyTest() {
 *        super(new MyMathTransformFactory(), new MyTransformationFactory(),
 *          new MyDatumFactory(), new MyCRSFactory(),
 *          new MyCoordinateOperationFactory(), new MyDatumAuthorityFactory(),
 *          new MyCSFactory(), new MyCRSFactory(),
 *          new MyCRSAuthorityFactory());
 *    }
 *}</pre></blockquote>
 *
 * @author  Michael Arneson (INT)
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
     * The coordinate transformation created by the factory,
     * or {@code null} if not yet created or if CRS creation failed.
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
     * The parameters used to create the
     */
    private ParameterValueGroup parameterValueGroup;

    /**
     * Data about the source CRS of the transformation.
     * This is used only for tests with user definitions for CRS components.
     *
     * @see #setUserComponents(TestMethod, TestMethod)
     */
    private Test3205Geog2DCRS sourceCRSTest;

    /**
     * Data about the target CRS of the transformation.
     * This is used only for tests with user definitions for CRS components.
     *
     * @see #setUserComponents(TestMethod, TestMethod)
     */
    private Test3205Geog2DCRS targetCRSTest;

    /**
     * The source CRS of the transformation created by this factory.
     */
    private GeographicCRS sourceCRS;

    /**
     * The target CRS of the transformation created by this factory.
     */
    private GeographicCRS targetCRS;

    /**
     * Creates a new test using the given factory. If a given factory is {@code null},
     * then the tests which depend on it will be skipped.
     *
     * @param mtFactory              factory for creating {@link Transformation} instances.
     * @param transformationFactory  factory for creating {@link Transformation} instances.
     * @param datumFactory           factory for creating {@link GeodeticDatum} instances.
     * @param csFactory              factory for creating {@code CoordinateSystem} instances.
     * @param crsFactory             factory for creating {@link GeodeticCRS} instances.
     * @param datumAuthorityFactory  factory for creating {@link Ellipsoid} and {@link PrimeMeridian} components from EPSG codes.
     * @param crsAuthorityFactory    factory for creating {@link GeodeticCRS} instances.
     */
    public Test3208(final MathTransformFactory mtFactory, TransformationFactory transformationFactory,
                    final DatumFactory datumFactory, final DatumAuthorityFactory datumAuthorityFactory,
                    final CSFactory csFactory, final CRSFactory crsFactory,
                    CRSAuthorityFactory crsAuthorityFactory) {
        this.mtFactory = mtFactory;
        this.transformationFactory = transformationFactory;
        this.datumFactory = datumFactory;
        this.datumAuthorityFactory = datumAuthorityFactory;
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
     *       <li>{@link #datumAuthorityFactory}</li>
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
        assertNull(op.put(Configuration.Key.datumAuthorityFactory, datumAuthorityFactory));
        assertNull(op.put(Configuration.Key.csFactory, csFactory));
        assertNull(op.put(Configuration.Key.crsFactory, crsFactory));
        assertNull(op.put(Configuration.Key.crsAuthorityFactory, crsAuthorityFactory));
        return op;
    }

    /**
     * Returns the transformation instance to be tested. When this method is invoked for the first time, it creates the
     * transformation to test by invoking the {@link MathTransformFactory#getDefaultParameters(String)}
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
     * Verifies the properties of the transformation given by {@link #getIdentifiedObject()}.
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
            sourceCRSTest.setIdentifiedObject((GeographicCRS) sourceCRS);
            sourceCRSTest.verifyGeographicCRS();
        }
        if (targetCRSTest != null) {
            targetCRSTest.copyConfigurationFrom(this);
            targetCRSTest.setIdentifiedObject((GeographicCRS) targetCRS);
            targetCRSTest.verifyGeographicCRS();
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
     * Creates a user-defined source CRS by executing the specified method from the {@link Test3205Geog2DCRS} class.
     *
     * @param  factory           the test method to use for creating the source CRS.
     * @throws FactoryException  if an error occurred while creating the source CRS.
     */
    private void createSourceCRS(final TestMethod<Test3205Geog2DCRS> factory) throws FactoryException {
        sourceCRSTest = new Test3205Geog2DCRS(datumFactory, datumAuthorityFactory, csFactory, crsFactory);
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
        this.sourceCRS = this.crsAuthorityFactory.createGeographicCRS(String.valueOf(code));
    }

    /**
     * Creates a user-defined target CRS by executing the specified method from the {@link Test3205Geog2DCRS} class.
     *
     * @param  factory           the test method to use for creating the target CRS.
     * @throws FactoryException  if an error occurred while creating the target CRS.
     */
    private void createTargetCRS(final TestMethod<Test3205Geog2DCRS> factory) throws FactoryException {
        targetCRSTest = new Test3205Geog2DCRS(datumFactory, datumAuthorityFactory, csFactory, crsFactory);
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
        this.targetCRS = this.crsAuthorityFactory.createGeographicCRS(String.valueOf(code));
    }

    /**
     * Tests “GIGS geogCRS A to WGS 84 (1)”  transformation from the factory.
     *
     * <ul>
     *   <li>GIGS transformation code: <b>61001</b></li>
     *   <li>GIGS transformation name: <b>GIGS geogCRS A to WGS 84 (1)</b></li>
     *   <li>EPSG Transformation Method: <b>Geocentric translations</b></li>
     * </ul>
     * <table class="ogc">
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
        properties.put(CoordinateOperation.OPERATION_VERSION_KEY, "GIGS Transformation");
        methodName = "Geocentric translations (geog2D domain)";
        createDefaultParameters();
        createSourceCRS(Test3205Geog2DCRS::GIGS_64003);
        createTargetCRS(4326);
        parameterValueGroup.parameter("X-axis translation").setValue(0, units.metre());
        parameterValueGroup.parameter("Y-axis translation").setValue(0, units.metre());
        parameterValueGroup.parameter("Z-axis translation").setValue(0, units.metre());
        Ellipsoid sourceEllipsoid = sourceCRS.getDatum().getEllipsoid();
        parameterValueGroup.parameter("src_semi_major").setValue(sourceEllipsoid.getSemiMajorAxis(), sourceEllipsoid.getAxisUnit());
        parameterValueGroup.parameter("src_semi_minor").setValue(sourceEllipsoid.getSemiMinorAxis(), sourceEllipsoid.getAxisUnit());
        Ellipsoid targetEllipsoid = targetCRS.getDatum().getEllipsoid();
        parameterValueGroup.parameter("tgt_semi_major").setValue(targetEllipsoid.getSemiMajorAxis(), targetEllipsoid.getAxisUnit());
        parameterValueGroup.parameter("tgt_semi_minor").setValue(targetEllipsoid.getSemiMinorAxis(), targetEllipsoid.getAxisUnit());
        verifyTransformation();
    }

    /**
     * Tests “GIGS geogCRS B to GIGS geogCRS A (1)”  transformation from the factory.
     *
     * <ul>
     *   <li>GIGS transformation code: <b>61196</b></li>
     *   <li>GIGS transformation name: <b>GIGS geogCRS B to GIGS geogCRS A (1)</b></li>
     *   <li>EPSG Transformation Method: <b>Geocentric translations</b></li>
     *   <li>EPSG equivalence: <b>1196 – OSGB36 to WGS 84 (2)</b></li>
     * </ul>
     * <table class="ogc">
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
        properties.put(CoordinateOperation.OPERATION_VERSION_KEY, "GIGS Transformation");
        methodName = "Geocentric translations (geog2D domain)";
        createDefaultParameters();
        createSourceCRS(Test3205Geog2DCRS::GIGS_64005);
        createTargetCRS(Test3205Geog2DCRS::GIGS_64003);
        parameterValueGroup.parameter("X-axis translation").setValue(371, units.metre());
        parameterValueGroup.parameter("Y-axis translation").setValue(-112, units.metre());
        parameterValueGroup.parameter("Z-axis translation").setValue(434, units.metre());
        Ellipsoid sourceEllipsoid = sourceCRS.getDatum().getEllipsoid();
        parameterValueGroup.parameter("src_semi_major").setValue(sourceEllipsoid.getSemiMajorAxis(), sourceEllipsoid.getAxisUnit());
        parameterValueGroup.parameter("src_semi_minor").setValue(sourceEllipsoid.getSemiMinorAxis(), sourceEllipsoid.getAxisUnit());
        Ellipsoid targetEllipsoid = targetCRS.getDatum().getEllipsoid();
        parameterValueGroup.parameter("tgt_semi_major").setValue(targetEllipsoid.getSemiMajorAxis(), targetEllipsoid.getAxisUnit());
        parameterValueGroup.parameter("tgt_semi_minor").setValue(targetEllipsoid.getSemiMinorAxis(), targetEllipsoid.getAxisUnit());
        verifyTransformation();
    }

    /**
     * Tests “GIGS geogCRS B to GIGS geogCRS A (2)”  transformation from the factory.
     *
     * <ul>
     *   <li>GIGS transformation code: <b>61314</b></li>
     *   <li>GIGS transformation name: <b>GIGS geogCRS B to GIGS geogCRS A (2)</b></li>
     *   <li>EPSG Transformation Method: <b>Position Vector 7-param. transformation</b></li>
     *   <li>EPSG equivalence: <b>1314 – OSGB36 to WGS 84 (6)</b></li>
     * </ul>
     * <table class="ogc">
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
        properties.put(CoordinateOperation.OPERATION_VERSION_KEY, "GIGS Transformation");
        methodName = "Position Vector transformation (geog2D domain)";
        createDefaultParameters();
        createSourceCRS(Test3205Geog2DCRS::GIGS_64005);
        createTargetCRS(Test3205Geog2DCRS::GIGS_64003);
        parameterValueGroup.parameter("X-axis translation").setValue(446.448, units.metre());
        parameterValueGroup.parameter("Y-axis translation").setValue(-125.157, units.metre());
        parameterValueGroup.parameter("Z-axis translation").setValue(542.06, units.metre());
        parameterValueGroup.parameter("X-axis rotation").setValue(0.15, units.arcSecond());
        parameterValueGroup.parameter("Y-axis rotation").setValue(0.247, units.arcSecond());
        parameterValueGroup.parameter("Z-axis rotation").setValue(0.842, units.arcSecond());
        parameterValueGroup.parameter("Scale difference").setValue(-20.489, units.ppm());
        Ellipsoid sourceEllipsoid = sourceCRS.getDatum().getEllipsoid();
        parameterValueGroup.parameter("src_semi_major").setValue(sourceEllipsoid.getSemiMajorAxis(), sourceEllipsoid.getAxisUnit());
        parameterValueGroup.parameter("src_semi_minor").setValue(sourceEllipsoid.getSemiMinorAxis(), sourceEllipsoid.getAxisUnit());
        Ellipsoid targetEllipsoid = targetCRS.getDatum().getEllipsoid();
        parameterValueGroup.parameter("tgt_semi_major").setValue(targetEllipsoid.getSemiMajorAxis(), targetEllipsoid.getAxisUnit());
        parameterValueGroup.parameter("tgt_semi_minor").setValue(targetEllipsoid.getSemiMinorAxis(), targetEllipsoid.getAxisUnit());
        verifyTransformation();
    }

    /**
     * Tests “GIGS geogCRS C to GIGS geogCRS A (1)”  transformation from the factory.
     *
     * <ul>
     *   <li>GIGS transformation code: <b>61002</b></li>
     *   <li>GIGS transformation name: <b>GIGS geogCRS C to GIGS geogCRS A (1)</b></li>
     *   <li>EPSG Transformation Method: <b>Geocentric translations</b></li>
     * </ul>
     * <table class="ogc">
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
        properties.put(CoordinateOperation.OPERATION_VERSION_KEY, "GIGS Transformation");
        methodName = "Geocentric translations (geog2D domain)";
        createDefaultParameters();
        createSourceCRS(Test3205Geog2DCRS::GIGS_64006);
        createTargetCRS(Test3205Geog2DCRS::GIGS_64003);
        parameterValueGroup.parameter("X-axis translation").setValue(593, units.metre());
        parameterValueGroup.parameter("Y-axis translation").setValue(26, units.metre());
        parameterValueGroup.parameter("Z-axis translation").setValue(479, units.metre());
        Ellipsoid sourceEllipsoid = sourceCRS.getDatum().getEllipsoid();
        parameterValueGroup.parameter("src_semi_major").setValue(sourceEllipsoid.getSemiMajorAxis(), sourceEllipsoid.getAxisUnit());
        parameterValueGroup.parameter("src_semi_minor").setValue(sourceEllipsoid.getSemiMinorAxis(), sourceEllipsoid.getAxisUnit());
        Ellipsoid targetEllipsoid = targetCRS.getDatum().getEllipsoid();
        parameterValueGroup.parameter("tgt_semi_major").setValue(targetEllipsoid.getSemiMajorAxis(), targetEllipsoid.getAxisUnit());
        parameterValueGroup.parameter("tgt_semi_minor").setValue(targetEllipsoid.getSemiMinorAxis(), targetEllipsoid.getAxisUnit());
        verifyTransformation();
    }

    /**
     * Tests “GIGS geogCRS C to GIGS geogCRS A (2)”  transformation from the factory.
     *
     * <ul>
     *   <li>GIGS transformation code: <b>15934</b></li>
     *   <li>GIGS transformation name: <b>GIGS geogCRS C to GIGS geogCRS A (2)</b></li>
     *   <li>EPSG Transformation Method: <b>Coordinate Frame rotation</b></li>
     *   <li>EPSG equivalence: <b>15934 – Amersfoort to WGS 84 (3)</b></li>
     * </ul>
     * <table class="ogc">
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
        properties.put(CoordinateOperation.OPERATION_VERSION_KEY, "GIGS Transformation");
        methodName = "Coordinate Frame rotation (geog2D domain)";
        createDefaultParameters();
        createSourceCRS(Test3205Geog2DCRS::GIGS_64006);
        createTargetCRS(Test3205Geog2DCRS::GIGS_64003);
        parameterValueGroup.parameter("X-axis translation").setValue(565.2369, units.metre());
        parameterValueGroup.parameter("Y-axis translation").setValue(50.0087, units.metre());
        parameterValueGroup.parameter("Z-axis translation").setValue(465.658, units.metre());
        parameterValueGroup.parameter("X-axis rotation").setValue(1.9725, units.microradian());
        parameterValueGroup.parameter("Y-axis rotation").setValue(-1.7004, units.microradian());
        parameterValueGroup.parameter("Z-axis rotation").setValue(9.0677, units.microradian());
        parameterValueGroup.parameter("Scale difference").setValue(4.0812, units.ppm());
        Ellipsoid sourceEllipsoid = sourceCRS.getDatum().getEllipsoid();
        parameterValueGroup.parameter("src_semi_major").setValue(sourceEllipsoid.getSemiMajorAxis(), sourceEllipsoid.getAxisUnit());
        parameterValueGroup.parameter("src_semi_minor").setValue(sourceEllipsoid.getSemiMinorAxis(), sourceEllipsoid.getAxisUnit());
        Ellipsoid targetEllipsoid = targetCRS.getDatum().getEllipsoid();
        parameterValueGroup.parameter("tgt_semi_major").setValue(targetEllipsoid.getSemiMajorAxis(), targetEllipsoid.getAxisUnit());
        parameterValueGroup.parameter("tgt_semi_minor").setValue(targetEllipsoid.getSemiMinorAxis(), targetEllipsoid.getAxisUnit());
        verifyTransformation();
    }

    /**
     * Tests “GIGS geogCRS C to GIGS geogCRS A (3)”  transformation from the factory.
     *
     * <ul>
     *   <li>GIGS transformation code: <b>61003</b></li>
     *   <li>GIGS transformation name: <b>GIGS geogCRS C to GIGS geogCRS A (3)</b></li>
     *   <li>EPSG Transformation Method: <b>Molodensky-Badekas 10-parameter transformation</b></li>
     * </ul>
     * <table class="ogc">
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
        properties.put(CoordinateOperation.OPERATION_VERSION_KEY, "GIGS Transformation");
        methodName = "Molodensky";
        createDefaultParameters();
        createSourceCRS(Test3205Geog2DCRS::GIGS_64006);
        createTargetCRS(Test3205Geog2DCRS::GIGS_64003);
        parameterValueGroup.parameter("X-axis translation").setValue(593.0297, units.metre());
        parameterValueGroup.parameter("Y-axis translation").setValue(26.0038, units.metre());
        parameterValueGroup.parameter("Z-axis translation").setValue(478.7534, units.metre());
        parameterValueGroup.parameter("X-axis rotation").setValue(0.4069, units.arcSecond());
        parameterValueGroup.parameter("Y-axis rotation").setValue(-0.3507, units.arcSecond());
        parameterValueGroup.parameter("Z-axis rotation").setValue(1.8703, units.arcSecond());
        parameterValueGroup.parameter("Scale difference").setValue(4.0812, units.ppm());
        parameterValueGroup.parameter("Ordinate 1 of evaluation point").setValue(3903453.1482, units.metre());
        parameterValueGroup.parameter("Ordinate 2 of evaluation point").setValue(368135.3134, units.metre());
        parameterValueGroup.parameter("Ordinate 3 of evaluation point").setValue(5012970.3051, units.metre());
        Ellipsoid sourceEllipsoid = sourceCRS.getDatum().getEllipsoid();
        parameterValueGroup.parameter("src_semi_major").setValue(sourceEllipsoid.getSemiMajorAxis(), sourceEllipsoid.getAxisUnit());
        parameterValueGroup.parameter("src_semi_minor").setValue(sourceEllipsoid.getSemiMinorAxis(), sourceEllipsoid.getAxisUnit());
        Ellipsoid targetEllipsoid = targetCRS.getDatum().getEllipsoid();
        parameterValueGroup.parameter("tgt_semi_major").setValue(targetEllipsoid.getSemiMajorAxis(), targetEllipsoid.getAxisUnit());
        parameterValueGroup.parameter("tgt_semi_minor").setValue(targetEllipsoid.getSemiMinorAxis(), targetEllipsoid.getAxisUnit());
        verifyTransformation();
    }

    /**
     * Tests “GIGS geogCRS E to GIGS geogCRS A (1)”  transformation from the factory.
     *
     * <ul>
     *   <li>GIGS transformation code: <b>61610</b></li>
     *   <li>GIGS transformation name: <b>GIGS geogCRS E to GIGS geogCRS A (1)</b></li>
     *   <li>EPSG Transformation Method: <b>Geocentric translations</b></li>
     *   <li>EPSG equivalence: <b>1610 – BD72 to WGS 84 (2)</b></li>
     * </ul>
     * <table class="ogc">
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
        properties.put(CoordinateOperation.OPERATION_VERSION_KEY, "GIGS Transformation");
        methodName = "Geocentric translations (geog2D domain)";
        createDefaultParameters();
        createSourceCRS(Test3205Geog2DCRS::GIGS_64008);
        createTargetCRS(Test3205Geog2DCRS::GIGS_64003);
        parameterValueGroup.parameter("X-axis translation").setValue(-125.8, units.metre());
        parameterValueGroup.parameter("Y-axis translation").setValue(79.9, units.metre());
        parameterValueGroup.parameter("Z-axis translation").setValue(-100.5, units.metre());
        Ellipsoid sourceEllipsoid = sourceCRS.getDatum().getEllipsoid();
        parameterValueGroup.parameter("src_semi_major").setValue(sourceEllipsoid.getSemiMajorAxis(), sourceEllipsoid.getAxisUnit());
        parameterValueGroup.parameter("src_semi_minor").setValue(sourceEllipsoid.getSemiMinorAxis(), sourceEllipsoid.getAxisUnit());
        Ellipsoid targetEllipsoid = targetCRS.getDatum().getEllipsoid();
        parameterValueGroup.parameter("tgt_semi_major").setValue(targetEllipsoid.getSemiMajorAxis(), targetEllipsoid.getAxisUnit());
        parameterValueGroup.parameter("tgt_semi_minor").setValue(targetEllipsoid.getSemiMinorAxis(), targetEllipsoid.getAxisUnit());
        verifyTransformation();
    }

    /**
     * Tests “GIGS geogCRS E to GIGS geogCRS A (2)”  transformation from the factory.
     *
     * <ul>
     *   <li>GIGS transformation code: <b>15929</b></li>
     *   <li>GIGS transformation name: <b>GIGS geogCRS E to GIGS geogCRS A (2)</b></li>
     *   <li>EPSG Transformation Method: <b>Coordinate Frame rotation</b></li>
     *   <li>EPSG equivalence: <b>15929 – BD72 to WGS 84 (3)</b></li>
     * </ul>
     * <table class="ogc">
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
        properties.put(CoordinateOperation.OPERATION_VERSION_KEY, "GIGS Transformation");
        methodName = "Coordinate Frame rotation (geog2D domain)";
        createDefaultParameters();
        createSourceCRS(Test3205Geog2DCRS::GIGS_64008);
        createTargetCRS(Test3205Geog2DCRS::GIGS_64003);
        parameterValueGroup.parameter("X-axis translation").setValue(-106.8686, units.metre());
        parameterValueGroup.parameter("Y-axis translation").setValue(52.2978, units.metre());
        parameterValueGroup.parameter("Z-axis translation").setValue(-103.7239, units.metre());
        parameterValueGroup.parameter("X-axis rotation").setValue(-0.3366, units.arcSecond());
        parameterValueGroup.parameter("Y-axis rotation").setValue(0.457, units.arcSecond());
        parameterValueGroup.parameter("Z-axis rotation").setValue(-1.8422, units.arcSecond());
        parameterValueGroup.parameter("Scale difference").setValue(-1.2747, units.ppm());
        Ellipsoid sourceEllipsoid = sourceCRS.getDatum().getEllipsoid();
        parameterValueGroup.parameter("src_semi_major").setValue(sourceEllipsoid.getSemiMajorAxis(), sourceEllipsoid.getAxisUnit());
        parameterValueGroup.parameter("src_semi_minor").setValue(sourceEllipsoid.getSemiMinorAxis(), sourceEllipsoid.getAxisUnit());
        Ellipsoid targetEllipsoid = targetCRS.getDatum().getEllipsoid();
        parameterValueGroup.parameter("tgt_semi_major").setValue(targetEllipsoid.getSemiMajorAxis(), targetEllipsoid.getAxisUnit());
        parameterValueGroup.parameter("tgt_semi_minor").setValue(targetEllipsoid.getSemiMinorAxis(), targetEllipsoid.getAxisUnit());
        verifyTransformation();
    }

    /**
     * Tests “GIGS geogCRS F to GIGS geogCRS A (1)”  transformation from the factory.
     *
     * <ul>
     *   <li>GIGS transformation code: <b>61150</b></li>
     *   <li>GIGS transformation name: <b>GIGS geogCRS F to GIGS geogCRS A (1)</b></li>
     *   <li>EPSG Transformation Method: <b>Geocentric translations</b></li>
     *   <li>EPSG equivalence: <b>1150 – GDA94 to WGS 84 (1)</b></li>
     * </ul>
     * <table class="ogc">
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
        properties.put(CoordinateOperation.OPERATION_VERSION_KEY, "GIGS Transformation");
        methodName = "Geocentric translations (geog2D domain)";
        createDefaultParameters();
        createSourceCRS(Test3205Geog2DCRS::GIGS_64009);
        createTargetCRS(Test3205Geog2DCRS::GIGS_64003);
        parameterValueGroup.parameter("X-axis translation").setValue(0, units.metre());
        parameterValueGroup.parameter("Y-axis translation").setValue(0, units.metre());
        parameterValueGroup.parameter("Z-axis translation").setValue(0, units.metre());
        Ellipsoid sourceEllipsoid = sourceCRS.getDatum().getEllipsoid();
        parameterValueGroup.parameter("src_semi_major").setValue(sourceEllipsoid.getSemiMajorAxis(), sourceEllipsoid.getAxisUnit());
        parameterValueGroup.parameter("src_semi_minor").setValue(sourceEllipsoid.getSemiMinorAxis(), sourceEllipsoid.getAxisUnit());
        Ellipsoid targetEllipsoid = targetCRS.getDatum().getEllipsoid();
        parameterValueGroup.parameter("tgt_semi_major").setValue(targetEllipsoid.getSemiMajorAxis(), targetEllipsoid.getAxisUnit());
        parameterValueGroup.parameter("tgt_semi_minor").setValue(targetEllipsoid.getSemiMinorAxis(), targetEllipsoid.getAxisUnit());
        verifyTransformation();
    }

    /**
     * Tests “GIGS geogCRS H to GIGS geogCRS T (1)”  transformation from the factory.
     *
     * <ul>
     *   <li>GIGS transformation code: <b>61763</b></li>
     *   <li>GIGS transformation name: <b>GIGS geogCRS H to GIGS geogCRS T (1)</b></li>
     *   <li>EPSG Transformation Method: <b>Longitude rotation</b></li>
     *   <li>EPSG equivalence: <b>1763 – NTF (Paris) to NTF (1)</b></li>
     * </ul>
     * <table class="ogc">
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
        properties.put(CoordinateOperation.OPERATION_VERSION_KEY, "GIGS Transformation");
        methodName = "Longitude rotation";
        createDefaultParameters();
        createSourceCRS(Test3205Geog2DCRS::GIGS_64011);
        createTargetCRS(Test3205Geog2DCRS::GIGS_64013);
        parameterValueGroup.parameter("Longitude offset").setValue(2.5969213, units.grad());
        verifyTransformation();
    }

    /**
     * Tests “GIGS geogCRS J to GIGS geogCRS A (1)”  transformation from the factory.
     *
     * <ul>
     *   <li>GIGS transformation code: <b>61173</b></li>
     *   <li>GIGS transformation name: <b>GIGS geogCRS J to GIGS geogCRS A (1)</b></li>
     *   <li>EPSG Transformation Method: <b>Geocentric translations</b></li>
     *   <li>EPSG equivalence: <b>1173 – NAD27 to WGS 84 (4)</b></li>
     * </ul>
     * <table class="ogc">
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
        properties.put(CoordinateOperation.OPERATION_VERSION_KEY, "GIGS Transformation");
        methodName = "Geocentric translations (geog2D domain)";
        createDefaultParameters();
        createSourceCRS(Test3205Geog2DCRS::GIGS_64012);
        createTargetCRS(Test3205Geog2DCRS::GIGS_64003);
        parameterValueGroup.parameter("X-axis translation").setValue(-8, units.metre());
        parameterValueGroup.parameter("Y-axis translation").setValue(160, units.metre());
        parameterValueGroup.parameter("Z-axis translation").setValue(176, units.metre());
        Ellipsoid sourceEllipsoid = sourceCRS.getDatum().getEllipsoid();
        parameterValueGroup.parameter("src_semi_major").setValue(sourceEllipsoid.getSemiMajorAxis(), sourceEllipsoid.getAxisUnit());
        parameterValueGroup.parameter("src_semi_minor").setValue(sourceEllipsoid.getSemiMinorAxis(), sourceEllipsoid.getAxisUnit());
        Ellipsoid targetEllipsoid = targetCRS.getDatum().getEllipsoid();
        parameterValueGroup.parameter("tgt_semi_major").setValue(targetEllipsoid.getSemiMajorAxis(), targetEllipsoid.getAxisUnit());
        parameterValueGroup.parameter("tgt_semi_minor").setValue(targetEllipsoid.getSemiMinorAxis(), targetEllipsoid.getAxisUnit());
        verifyTransformation();
    }

    /**
     * Tests “GIGS geogCRS J to GIGS geogCRS A (2)”  transformation from the factory.
     *
     * <ul>
     *   <li>GIGS transformation code: <b>61004</b></li>
     *   <li>GIGS transformation name: <b>GIGS geogCRS J to GIGS geogCRS A (2)</b></li>
     *   <li>EPSG Transformation Method: <b>NADCON</b></li>
     * </ul>
     * <table class="ogc">
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
        properties.put(CoordinateOperation.OPERATION_VERSION_KEY, "GIGS Transformation");
        methodName = "NADCON";
        createDefaultParameters();
        createSourceCRS(Test3205Geog2DCRS::GIGS_64012);
        createTargetCRS(Test3205Geog2DCRS::GIGS_64003);
        parameterValueGroup.parameter("Latitude difference file").setValue("n_slope.las");
        parameterValueGroup.parameter("Longitude difference file").setValue("n_slope.los");
        verifyTransformation();
    }

    /**
     * Tests “GIGS geogCRS J to GIGS geogCRS A (3)”  transformation from the factory.
     *
     * <ul>
     *   <li>GIGS transformation code: <b>61692</b></li>
     *   <li>GIGS transformation name: <b>GIGS geogCRS J to GIGS geogCRS A (3)</b></li>
     *   <li>EPSG Transformation Method: <b>NTv2</b></li>
     *   <li>EPSG equivalence: <b>1692 – NAD27 to WGS 84 (34)</b></li>
     * </ul>
     * <table class="ogc">
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
        properties.put(CoordinateOperation.OPERATION_VERSION_KEY, "GIGS Transformation");
        methodName = "NTv2";
        createDefaultParameters();
        createSourceCRS(Test3205Geog2DCRS::GIGS_64012);
        createTargetCRS(Test3205Geog2DCRS::GIGS_64003);
        parameterValueGroup.parameter("Latitude and longitude difference file").setValue("QUE27-98.gsb");
        verifyTransformation();
    }

    /**
     * Tests “GIGS geogCRS K to GIGS geogCRS A (1)”  transformation from the factory.
     *
     * <ul>
     *   <li>GIGS transformation code: <b>61242</b></li>
     *   <li>GIGS transformation name: <b>GIGS geogCRS K to GIGS geogCRS A (1)</b></li>
     *   <li>EPSG Transformation Method: <b>Geocentric translations</b></li>
     *   <li>EPSG equivalence: <b>1242 – HD72 to WGS 84 (4)</b></li>
     * </ul>
     * <table class="ogc">
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
        properties.put(CoordinateOperation.OPERATION_VERSION_KEY, "GIGS Transformation");
        methodName = "Geocentric translations (geog2D domain)";
        createDefaultParameters();
        createSourceCRS(Test3205Geog2DCRS::GIGS_64015);
        createTargetCRS(Test3205Geog2DCRS::GIGS_64003);
        parameterValueGroup.parameter("X-axis translation").setValue(52.17, units.metre());
        parameterValueGroup.parameter("Y-axis translation").setValue(-71.82, units.metre());
        parameterValueGroup.parameter("Z-axis translation").setValue(-14.9, units.metre());
        Ellipsoid sourceEllipsoid = sourceCRS.getDatum().getEllipsoid();
        parameterValueGroup.parameter("src_semi_major").setValue(sourceEllipsoid.getSemiMajorAxis(), sourceEllipsoid.getAxisUnit());
        parameterValueGroup.parameter("src_semi_minor").setValue(sourceEllipsoid.getSemiMinorAxis(), sourceEllipsoid.getAxisUnit());
        Ellipsoid targetEllipsoid = targetCRS.getDatum().getEllipsoid();
        parameterValueGroup.parameter("tgt_semi_major").setValue(targetEllipsoid.getSemiMajorAxis(), targetEllipsoid.getAxisUnit());
        parameterValueGroup.parameter("tgt_semi_minor").setValue(targetEllipsoid.getSemiMinorAxis(), targetEllipsoid.getAxisUnit());
        verifyTransformation();
    }

    /**
     * Tests “GIGS geogCRS M to GIGS geogCRS A (1)”  transformation from the factory.
     *
     * <ul>
     *   <li>GIGS transformation code: <b>61275</b></li>
     *   <li>GIGS transformation name: <b>GIGS geogCRS M to GIGS geogCRS A (1)</b></li>
     *   <li>EPSG Transformation Method: <b>Geocentric translations</b></li>
     *   <li>EPSG equivalence: <b>1275 – ED50 to WGS 84 (17)</b></li>
     * </ul>
     * <table class="ogc">
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
        properties.put(CoordinateOperation.OPERATION_VERSION_KEY, "GIGS Transformation");
        methodName = "Geocentric translations (geog2D domain)";
        createDefaultParameters();
        createSourceCRS(Test3205Geog2DCRS::GIGS_64020);
        createTargetCRS(Test3205Geog2DCRS::GIGS_64003);
        parameterValueGroup.parameter("X-axis translation").setValue(-84, units.metre());
        parameterValueGroup.parameter("Y-axis translation").setValue(-97, units.metre());
        parameterValueGroup.parameter("Z-axis translation").setValue(-117, units.metre());
        Ellipsoid sourceEllipsoid = sourceCRS.getDatum().getEllipsoid();
        parameterValueGroup.parameter("src_semi_major").setValue(sourceEllipsoid.getSemiMajorAxis(), sourceEllipsoid.getAxisUnit());
        parameterValueGroup.parameter("src_semi_minor").setValue(sourceEllipsoid.getSemiMinorAxis(), sourceEllipsoid.getAxisUnit());
        Ellipsoid targetEllipsoid = targetCRS.getDatum().getEllipsoid();
        parameterValueGroup.parameter("tgt_semi_major").setValue(targetEllipsoid.getSemiMajorAxis(), targetEllipsoid.getAxisUnit());
        parameterValueGroup.parameter("tgt_semi_minor").setValue(targetEllipsoid.getSemiMinorAxis(), targetEllipsoid.getAxisUnit());
        verifyTransformation();
    }

    /**
     * Tests “GIGS geogCRS T to GIGS geogCRS A (1)”  transformation from the factory.
     *
     * <ul>
     *   <li>GIGS transformation code: <b>61193</b></li>
     *   <li>GIGS transformation name: <b>GIGS geogCRS T to GIGS geogCRS A (1)</b></li>
     *   <li>EPSG Transformation Method: <b>Geocentric translations</b></li>
     *   <li>EPSG equivalence: <b>1193 – NTF to WGS 84 (1)</b></li>
     * </ul>
     * <table class="ogc">
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
        properties.put(CoordinateOperation.OPERATION_VERSION_KEY, "GIGS Transformation");
        methodName = "Geocentric translations (geog2D domain)";
        createDefaultParameters();
        createSourceCRS(Test3205Geog2DCRS::GIGS_64013);
        createTargetCRS(Test3205Geog2DCRS::GIGS_64003);
        parameterValueGroup.parameter("X-axis translation").setValue(-168, units.metre());
        parameterValueGroup.parameter("Y-axis translation").setValue(-60, units.metre());
        parameterValueGroup.parameter("Z-axis translation").setValue(320, units.metre());
        Ellipsoid sourceEllipsoid = sourceCRS.getDatum().getEllipsoid();
        parameterValueGroup.parameter("src_semi_major").setValue(sourceEllipsoid.getSemiMajorAxis(), sourceEllipsoid.getAxisUnit());
        parameterValueGroup.parameter("src_semi_minor").setValue(sourceEllipsoid.getSemiMinorAxis(), sourceEllipsoid.getAxisUnit());
        Ellipsoid targetEllipsoid = targetCRS.getDatum().getEllipsoid();
        parameterValueGroup.parameter("tgt_semi_major").setValue(targetEllipsoid.getSemiMajorAxis(), targetEllipsoid.getAxisUnit());
        parameterValueGroup.parameter("tgt_semi_minor").setValue(targetEllipsoid.getSemiMinorAxis(), targetEllipsoid.getAxisUnit());
        verifyTransformation();
    }

    /**
     * Tests “GIGS geogCRS X to GIGS geogCRS A (1)”  transformation from the factory.
     *
     * <ul>
     *   <li>GIGS transformation code: <b>15788</b></li>
     *   <li>GIGS transformation name: <b>GIGS geogCRS X to GIGS geogCRS A (1)</b></li>
     *   <li>EPSG Transformation Method: <b>Geocentric translations</b></li>
     *   <li>EPSG equivalence: <b>15788 – AGD66 to WGS 84 (16)</b></li>
     * </ul>
     * <table class="ogc">
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
        properties.put(CoordinateOperation.OPERATION_VERSION_KEY, "GIGS Transformation");
        methodName = "Geocentric translations (geog2D domain)";
        createDefaultParameters();
        createSourceCRS(Test3205Geog2DCRS::GIGS_64016);
        createTargetCRS(Test3205Geog2DCRS::GIGS_64003);
        parameterValueGroup.parameter("X-axis translation").setValue(-127.8, units.metre());
        parameterValueGroup.parameter("Y-axis translation").setValue(-52.3, units.metre());
        parameterValueGroup.parameter("Z-axis translation").setValue(152.9, units.metre());
        Ellipsoid sourceEllipsoid = sourceCRS.getDatum().getEllipsoid();
        parameterValueGroup.parameter("src_semi_major").setValue(sourceEllipsoid.getSemiMajorAxis(), sourceEllipsoid.getAxisUnit());
        parameterValueGroup.parameter("src_semi_minor").setValue(sourceEllipsoid.getSemiMinorAxis(), sourceEllipsoid.getAxisUnit());
        Ellipsoid targetEllipsoid = targetCRS.getDatum().getEllipsoid();
        parameterValueGroup.parameter("tgt_semi_major").setValue(targetEllipsoid.getSemiMajorAxis(), targetEllipsoid.getAxisUnit());
        parameterValueGroup.parameter("tgt_semi_minor").setValue(targetEllipsoid.getSemiMinorAxis(), targetEllipsoid.getAxisUnit());
        verifyTransformation();
    }

    /**
     * Tests “GIGS geogCRS Y to GIGS geogCRS A (1)”  transformation from the factory.
     *
     * <ul>
     *   <li>GIGS transformation code: <b>61254</b></li>
     *   <li>GIGS transformation name: <b>GIGS geogCRS Y to GIGS geogCRS A (1)</b></li>
     *   <li>EPSG Transformation Method: <b>Geocentric translations</b></li>
     *   <li>EPSG equivalence: <b>1254 – Pulkovo 1942 to WGS 84 (1)</b></li>
     * </ul>
     * <table class="ogc">
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
        properties.put(CoordinateOperation.OPERATION_VERSION_KEY, "GIGS Transformation");
        methodName = "Geocentric translations (geog2D domain)";
        createDefaultParameters();
        createSourceCRS(Test3205Geog2DCRS::GIGS_64017);
        createTargetCRS(Test3205Geog2DCRS::GIGS_64003);
        parameterValueGroup.parameter("X-axis translation").setValue(28, units.metre());
        parameterValueGroup.parameter("Y-axis translation").setValue(-130, units.metre());
        parameterValueGroup.parameter("Z-axis translation").setValue(-95, units.metre());
        Ellipsoid sourceEllipsoid = sourceCRS.getDatum().getEllipsoid();
        parameterValueGroup.parameter("src_semi_major").setValue(sourceEllipsoid.getSemiMajorAxis(), sourceEllipsoid.getAxisUnit());
        parameterValueGroup.parameter("src_semi_minor").setValue(sourceEllipsoid.getSemiMinorAxis(), sourceEllipsoid.getAxisUnit());
        Ellipsoid targetEllipsoid = targetCRS.getDatum().getEllipsoid();
        parameterValueGroup.parameter("tgt_semi_major").setValue(targetEllipsoid.getSemiMajorAxis(), targetEllipsoid.getAxisUnit());
        parameterValueGroup.parameter("tgt_semi_minor").setValue(targetEllipsoid.getSemiMinorAxis(), targetEllipsoid.getAxisUnit());
        verifyTransformation();
    }

    /**
     * Tests “GIGS geogCRS Z to GIGS geogCRS A (1)”  transformation from the factory.
     *
     * <ul>
     *   <li>GIGS transformation code: <b>61188</b></li>
     *   <li>GIGS transformation name: <b>GIGS geogCRS Z to GIGS geogCRS A (1)</b></li>
     *   <li>EPSG Transformation Method: <b>Geocentric translations</b></li>
     *   <li>EPSG equivalence: <b>1188 – NAD83 to WGS 84 (1)</b></li>
     * </ul>
     * <table class="ogc">
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
        properties.put(CoordinateOperation.OPERATION_VERSION_KEY, "GIGS Transformation");
        methodName = "Geocentric translations (geog2D domain)";
        createDefaultParameters();
        createSourceCRS(Test3205Geog2DCRS::GIGS_64018);
        createTargetCRS(Test3205Geog2DCRS::GIGS_64003);
        parameterValueGroup.parameter("X-axis translation").setValue(0, units.metre());
        parameterValueGroup.parameter("Y-axis translation").setValue(0, units.metre());
        parameterValueGroup.parameter("Z-axis translation").setValue(0, units.metre());
        Ellipsoid sourceEllipsoid = sourceCRS.getDatum().getEllipsoid();
        parameterValueGroup.parameter("src_semi_major").setValue(sourceEllipsoid.getSemiMajorAxis(), sourceEllipsoid.getAxisUnit());
        parameterValueGroup.parameter("src_semi_minor").setValue(sourceEllipsoid.getSemiMinorAxis(), sourceEllipsoid.getAxisUnit());
        Ellipsoid targetEllipsoid = targetCRS.getDatum().getEllipsoid();
        parameterValueGroup.parameter("tgt_semi_major").setValue(targetEllipsoid.getSemiMajorAxis(), targetEllipsoid.getAxisUnit());
        parameterValueGroup.parameter("tgt_semi_minor").setValue(targetEllipsoid.getSemiMinorAxis(), targetEllipsoid.getAxisUnit());
        verifyTransformation();
    }
}
