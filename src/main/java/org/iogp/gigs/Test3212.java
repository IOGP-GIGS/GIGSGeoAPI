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
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.CRSFactory;
import org.opengis.referencing.crs.GeodeticCRS;
import org.opengis.referencing.crs.ProjectedCRS;
import org.opengis.referencing.cs.CSFactory;
import org.opengis.referencing.datum.DatumAuthorityFactory;
import org.opengis.referencing.datum.DatumFactory;
import org.opengis.referencing.datum.Ellipsoid;
import org.opengis.referencing.datum.GeodeticDatum;
import org.opengis.referencing.datum.PrimeMeridian;
import org.opengis.referencing.operation.ConcatenatedOperation;
import org.opengis.referencing.operation.CoordinateOperationFactory;
import org.opengis.referencing.operation.MathTransformFactory;
import org.opengis.referencing.operation.Transformation;
import org.iogp.gigs.internal.geoapi.Configuration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


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
 *   <td><a href="https://github.com/IOGP-GIGS/GIGSTestDataset/blob/main/GIGSTestDatasetFiles/GIGS%203200%20User-defined%20Geodetic%20Data%20Objects%20test%20data/ASCII/GIGS_user_3212_ConcatTfm.txt">{@code GIGS_user_3212_ConcatTfm.txt}</a>
 * </tr><tr>
 *   <th>Tested API:</th>
 *   <td>{@link CoordinateOperationFactory#createConcatenatedOperation(Map, CoordinateOperation...)}.</td>
 * </tr><tr>
 *   <th>Expected result:</th>
 *   <td>The geoscience software should accept the test data. The properties of the created objects will
 *       be compared with the properties given to the factory method.</td>
 * </tr></table>
 *
 *
 * <h2>Usage example</h2>
 * in order to specify their factories and run the tests in a JUnit framework, implementers can
 * define a subclass in their own test suite as in the example below:
 *
 * {@snippet lang="java" :
 * public class MyTest extends Test3212 {
 *     public MyTest() {
 *         super(new MyFactories());
 *     }
 * }
 * }
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
     *
     * @see #createStep1Transformation(TestMethod)
     */
    private final Test3208 step1TransformationTest;

    /**
     * Data about the second transformation in the concatenated transformation.
     *
     * @see #createStep2Transformation(TestMethod)
     */
    private final Test3208 step2TransformationTest;

    /**
     * The first transformation in the concatenated transformation created by this factory.
     */
    private Transformation step1Transformation;

    /**
     * The second transformation in the concatenated transformation created by this factory.
     */
    private Transformation step2Transformation;

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
    public Test3212(final Factories factories) {
        mtFactory             = factories.mtFactory;
        copFactory            = factories.copFactory;
        crsFactory            = factories.crsFactory;
        csFactory             = factories.csFactory;
        datumFactory          = factories.datumFactory;
        crsAuthorityFactory   = factories.crsAuthorityFactory;
        datumAuthorityFactory = factories.datumAuthorityFactory;

        step1TransformationTest = new Test3208(factories);
        step1TransformationTest.skipTests = true;
        step1TransformationTest.skipIdentificationCheck = true;

        step2TransformationTest = new Test3208(factories);
        step2TransformationTest.skipTests = true;
        step2TransformationTest.skipIdentificationCheck = true;
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
        @SuppressWarnings("LocalVariableHidesMemberVariable")
        final ConcatenatedOperation concatTransformation = getIdentifiedObject();
        assertNotNull(concatTransformation, "ConcatenatedOperation");
        validators.validate(concatTransformation);

        step1TransformationTest.copyConfigurationFrom(this);
        step1TransformationTest.setIdentifiedObject(step1Transformation);
        step1TransformationTest.verifyTransformation();

        step2TransformationTest.copyConfigurationFrom(this);
        step2TransformationTest.setIdentifiedObject(step2Transformation);
        step2TransformationTest.verifyTransformation();
    }

    /**
     * Creates a user-defined transformation by executing the specified method from the {@link Test3208} class. The
     * transformation created by this method is the first transformation in a concatenated transformation.
     *
     * @param  factory           the test method to use for creating the transformation.
     * @throws FactoryException  if an error occurred while creating the transformation.
     */
    private void createStep1Transformation(final TestMethod<Test3208> factory) throws FactoryException {
        factory.initialize(step1TransformationTest);
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
        factory.initialize(step2TransformationTest);
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

    /**
     * Tests “GIGS_68178”  transformation from the factory.
     *
     * <ul>
     *   <li>GIGS transformation code: <b>68178</b></li>
     *   <li>Step 1 GIGS Transform Code: <b>61759</b></li>
     *   <li>Step 1 GIGS Transform Name: <b>GIGS D to L (1)</b></li>
     *   <li>Step 2 GIGS Transform Code: <b>61123</b></li>
     *   <li>Step 2 GIGS Transform Name: <b>GIGS L to A (1)</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the transformation from the properties.
     */
    @Test
    @DisplayName("GIGS_68178")
    public void GIGS_68178() throws FactoryException {
        setCodeAndName(68178, "GIGS_68178");
        createStep1Transformation(Test3208::GIGS_61759);
        createStep2Transformation(Test3208::GIGS_61123);
        verifyTransformation();
    }
}
