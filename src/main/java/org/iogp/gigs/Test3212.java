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
import org.opengis.referencing.crs.*;
import org.opengis.referencing.cs.CSFactory;
import org.opengis.referencing.datum.*;
import org.opengis.referencing.operation.*;
import org.opengis.util.FactoryException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Verifies that the software allows correct definition of a user-defined concatenated transformations.
 *
 * <table class="gigs">
 * <caption>Test description</caption>
 * <tr>
 *   <th>Test method:</th>
 *   <td>Create user-defined concatenated CRS.</td>
 * </tr><tr>
 *   <th>Test data:</th>
 *   <td><a href="doc-files/GIGS_3005_userProjection.csv">{@code GIGS_user_3208_CoordTfm.txt}</a>.</td>
 *   <td><a href="https://github.com/IOGP-GIGS/GIGSTestDataset/blob/main/GIGSTestDatasetFiles/GIGS%203200%20User-defined%20Geodetic%20Data%20Objects%20test%20data/ASCII/GIGS_user_3211_VertTfm.txt">{@code GIGS_user_3211_VertTfm.txt}</a>
 * </tr><tr>
 *   <th>Tested API:</th>
 *   <td>{@link CoordinateOperationFactory#createConcatenatedOperation(Map, CoordinateOperation...)}.</td>
 * </tr><tr>
 *   <th>Expected result:</th>
 *   <td>The geoscience software should accept the test data. The properties of the created objects will
 *  *       be compared with the properties given to the factory method.</td>
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
 *          new MyDatumFactory(), new MyDatumAuthorityFactory(),
 *          new MyCSFactory(), new MyCRSFactory(),
 *          new MyCRSAuthorityFactory(), MyCoordinateOperationFactory());
 *    }
 *}</pre></blockquote>
 *
 * @author  Michael Arneson (INT)
 * @version 1.0
 * @since   1.0
 */
@DisplayName("User-defined concatenated transformations")
public class Test3212 extends Series3000<ConcatenatedOperation> {

    /**
     * The concatenated transformation created by the factory,
     * or {@code null} if not yet created or if the concatenated transform creation failed.
     */
    private ConcatenatedOperation concatTransformation;

    /**
     * Factory to use for building {@link Transformation} instances, or {@code null} if none.
     * This is the factory used by the {@link #getIdentifiedObject()} method.
     */
    protected final CoordinateOperationFactory copFactory;

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
     * Data about the first transformation in the concatenated transformation.
     * This is used only for tests with user definitions for CRS components.
     *
     * @see #setUserComponents(TestMethod, TestMethod)
     */
    private Test3208 step1TransformationTest;

    /**
     * Data about the second transformation in the concatenated transformation.
     * This is used only for tests with user definitions for CRS components.
     *
     * @see #setUserComponents(TestMethod, TestMethod)
     */
    private Test3208 step2TransformationTest;

    /**
     * The first transformation in the concatenated transformation created by this factory.
     */
    private Transformation step1Transformation;

    /**
     * The second transformation in the concatenated transformation created by this factory.
     */
    private Transformation step2Transformation;

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
    public Test3212(final MathTransformFactory mtFactory, TransformationFactory transformationFactory,
                    final DatumFactory datumFactory, final DatumAuthorityFactory datumAuthorityFactory,
                    final CSFactory csFactory, final CRSFactory crsFactory,
                    final CRSAuthorityFactory crsAuthorityFactory, final CoordinateOperationFactory copFactory) {
        this.mtFactory = mtFactory;
        this.transformationFactory = transformationFactory;
        this.datumFactory = datumFactory;
        this.datumAuthorityFactory = datumAuthorityFactory;
        this.csFactory = csFactory;
        this.crsFactory = crsFactory;
        this.crsAuthorityFactory = crsAuthorityFactory;
        this.copFactory = copFactory;
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
     *       <li>{@link #copFactory}</li>
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
        assertNull(op.put(Configuration.Key.copFactory, copFactory));
        return op;
    }

    /**
     * Returns the concatenated transformation instance to be tested. When this method is invoked for the first time,
     * it creates the
     * transformation to test by invoking the {@link CoordinateOperationFactory#createConcatenatedOperation(Map,
     * CoordinateOperation...)} method with  current {@link #properties properties} map, first transformation, and
     * second transformation. The created object is then cached and returned in all subsequent invocations of this
     * method.
     *
     * @return the concatenated transformation instance to test.
     * @throws FactoryException if an error occurred while creating the concatenated transformation instance.
     */
    @Override
    public ConcatenatedOperation getIdentifiedObject() throws FactoryException {
        if (concatTransformation == null) {
            concatTransformation = (ConcatenatedOperation) copFactory.createConcatenatedOperation(properties, step1Transformation, step2Transformation);
        }
        return concatTransformation;
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
        final ConcatenatedOperation transformation = getIdentifiedObject();
        assertNotNull(transformation, "Transformation");
        validators.validate(transformation);

        if (step1TransformationTest != null) {
            step1TransformationTest.copyConfigurationFrom(this);
            step1TransformationTest.setIdentifiedObject(step1Transformation);
            step1TransformationTest.verifyTransformation();
        }
        if (step2TransformationTest != null) {
            step2TransformationTest.copyConfigurationFrom(this);
            step2TransformationTest.setIdentifiedObject(step2Transformation);
            step2TransformationTest.verifyTransformation();
        }
    }

    /**
     * Creates a user-defined transformation by executing the specified method from the {@link Test3208} class. The
     * transformation created by this method is the first transformation in a concatenated transformation.
     *
     * @param  factory           the test method to use for creating the transformation.
     * @throws FactoryException  if an error occurred while creating the transformation.
     */
    private void createStep1Transformation(final TestMethod<Test3208> factory) throws FactoryException {
        step1TransformationTest = new Test3208(mtFactory, transformationFactory, datumFactory, datumAuthorityFactory,
                csFactory, crsFactory, crsAuthorityFactory);
        step1TransformationTest.skipTests = true;
        factory.test(step1TransformationTest);
        step1Transformation = step1TransformationTest.getIdentifiedObject();
    }

    /**
     * Creates a user-defined transformation by executing the specified method from the {@link Test3208} class. The
     * transformation created by this method is the second transformation in a concatenated transformation.
     *
     * @param  factory           the test method to use for creating the transformation.
     * @throws FactoryException  if an error occurred while creating the transformation.
     */
    private void createStep2Transformation(final TestMethod<Test3208> factory) throws FactoryException {
        step2TransformationTest = new Test3208(mtFactory, transformationFactory, datumFactory, datumAuthorityFactory,
                csFactory, crsFactory, crsAuthorityFactory);
        step2TransformationTest.skipTests = true;
        factory.test(step2TransformationTest);
        step2Transformation = step2TransformationTest.getIdentifiedObject();
    }

    /**
     * Tests “GIGS_68094”  transformation from the factory.
     *
     * <ul>
     *   <li>GIGS transformation code: <b>68094</b></li>
     *   <li>Step 1 GIGS Transform Code: <b>61763</b></li>
     *   <li>Step 1 GIGS Transform Name: <b>GIGS H to T (1)</b></li>
     *   <li>Step 2 GIGS Transform Code: <b>61193</b></li>
     *   <li>Step 2 GIGS Transform Name: <b>GIGS T to A (1)</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the transformation from the properties.
     */
    @Test
    @DisplayName("GIGS_68094")
    public void GIGS_68094() throws FactoryException {
        setCodeAndName(68094, "GIGS_68094");
        createStep1Transformation(Test3208::GIGS_61763);
        createStep2Transformation(Test3208::GIGS_61193);
        verifyTransformation();
    }
}
