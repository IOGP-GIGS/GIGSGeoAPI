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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValue;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.operation.*;
import org.opengis.util.FactoryException;
import org.opengis.util.NoSuchIdentifierException;

import javax.measure.Unit;
import javax.measure.quantity.Angle;
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Length;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Verifies that the software allows correct definition of a user-defined conversion.
 *
 * <table class="gigs">
 * <caption>Test description</caption>
 * <tr>
 *   <th>Test method:</th>
 *   <td>Create user-defined projection for each of several different conversions.</td>
 * </tr><tr>
 *   <th>Test data:</th>
 *   <td><a href="https://github.com/IOGP-GIGS/GIGSTestDataset/blob/main/GIGSTestDatasetFiles/GIGS%203200%20User-defined%20Geodetic%20Data%20Objects%20test%20data/ASCII/GIGS_user_3206_Conversion.txt">{@code GIGS_user_3206_Conversion.txt}</a>
 * </tr><tr>
 *   <th>Tested API:</th>
 *   <td>{@link MathTransformFactory#getAvailableMethods(Class)} and<br>
 *       {@link CoordinateOperationFactory#createDefiningConversion(Map, OperationMethod, ParameterValueGroup)}.</td>
 * </tr><tr>
 *   <th>Expected result:</th>
 *   <td>The geoscience software should accept the test data. The order in which the projection parameters
 *       are entered is not critical, although that given in the test dataset is recommended.</td>
 * </tr></table>
 *
 *
 * <h2>Usage example</h2>
 * in order to specify their factories and run the tests in a JUnit framework, implementers can
 * define a subclass in their own test suite as in the example below:
 *
 * <blockquote><pre>public class MyTest extends Test3006 {
 *    public MyTest() {
 *        super(new MyCoordinateOperationFactory(), new MyMathTransformFactory());
 *    }
 *}</pre></blockquote>
 *
 * @author  Michael Arneson (INT)
 * @version 1.0
 * @since   1.0
 */
@DisplayName("User-defined conversion")
public class Test3206 extends Series3000<Conversion> {
    /**
     * The name of the operation method to use.
     * This field is set by all test methods before to create and verify the {@link Conversion} instance.
     */
    public String methodName;

    /**
     * The parameters of the conversion to create.
     * This field is set by all test methods before to create and verify the {@link Conversion} instance.
     */
    public ParameterValueGroup definition;

    /**
     * The coordinate operation method.
     */
    private OperationMethod method;

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
    protected final CoordinateOperationFactory copFactory;

    /**
     * The factory to use for fetching operation methods, or {@code null} if none.
     */
    protected final MathTransformFactory mtFactory;

    /**
     * Creates a new test using the given factory. If a given factory is {@code null},
     * then the tests which depend on it will be skipped.
     *
     * @param copFactory  factory for creating {@link Conversion} instances.
     * @param mtFactory   factory to use for fetching operation methods, or {@code null} if none.
     */
    public Test3206(final CoordinateOperationFactory copFactory, final MathTransformFactory mtFactory) {
        this.copFactory = copFactory;
        this.mtFactory = mtFactory;
    }


    /**
     * Returns information about the configuration of the test which has been run.
     * This method returns a map containing:
     *
     * <ul>
     *   <li>All the following values associated to the {@link org.opengis.test.Configuration.Key} of the same name:
     *     <ul>
     *       <li>{@link #isFactoryPreservingUserValues}</li>
     *       <li>{@link #copFactory}</li>
     *       <li>{@link #mtFactory}</li>
     *     </ul>
     *   </li>
     * </ul>
     *
     * @return the configuration of the test being run.
     */
    @Override
    public Configuration configuration() {
        final Configuration op = super.configuration();
        assertNull(op.put(Configuration.Key.copFactory, copFactory));
        assertNull(op.put(Configuration.Key.mtFactory,  mtFactory));
        return op;
    }

    /**
     * Returns the operation method of the given name. This is a temporary method until
     * {@code getOperationMethod(…)} is added directly in the {@link CoordinateOperationFactory} interface.
     *
     * @param  methodName  name of the operation method to fetch.
     * @return operation method for the given name.
     * @throws NoSuchIdentifierException if the given name is not recognized.
     */
    private OperationMethod getOperationMethod(final String methodName) throws NoSuchIdentifierException {
        for (final OperationMethod candidate : mtFactory.getAvailableMethods(SingleOperation.class)) {
            if (methodName.equalsIgnoreCase(candidate.getName().getCode())) {
                return candidate;
            }
        }
        throw new NoSuchIdentifierException("Operation method \"" + methodName + "\" not found.", methodName);
    }

    /**
     * Instantiates the {@link #definition} field.
     * It is caller's responsibility to set the parameter values.
     *
     * @throws FactoryException if an error occurred while creating the parameters.
     */
    private void createDefaultParameters() throws FactoryException {
        assumeNotNull(copFactory);
        assumeNotNull(mtFactory);
        /*
         * Get the OperationMethod defined by the library. Libraries are not required
         * to implement every possible operation methods, in which case unimplemented
         * methods will be reported. If the method is supported, then this method will
         * verify the following properties:
         *
         *  - The number of source dimensions
         *  - The number of target dimensions
         *
         * We currently do not verify the name because libraries often have their own
         * hard-coded implementations of operation methods instead of creating new
         * instances from the given properties.
         */
        try {
            method = getOperationMethod(methodName);
        } catch (NoSuchIdentifierException e) {
            unsupportedCode(OperationMethod.class, methodName);
            throw e;
        }
        if (method == null) {
            fail("CoordinateOperationFactory.getOperationMethod(\"" + methodName + "\") shall not return null.");
        }
        validators.validate(method);
        assertEquals(Integer.valueOf(2), method.getSourceDimensions(), "OperationMethod.getSourceDimensions()");
        assertEquals(Integer.valueOf(2), method.getTargetDimensions(), "OperationMethod.getTargetDimensions()");
        definition = method.getParameters().createValue();
    }

    /**
     * Returns the conversion instance to be tested. When this method is invoked for the first time,
     * it creates the coordinate operation to test by invoking the corresponding method from
     * {@link CoordinateOperationFactory} with the current {@link #properties properties} map in argument.
     * The created object is then cached and returned in all subsequent invocations of this method.
     *
     * @return the conversion instance to test.
     * @throws FactoryException if an error occurred while creating the conversion instance.
     */
    @Override
    public Conversion getIdentifiedObject() throws FactoryException {
        if (conversion == null) {
            validators.validate(definition);
            final CoordinateOperation operation = copFactory.createDefiningConversion(properties, method, definition);
            if (operation != null) {            // For consistency with the behavior in other classes.
                assertInstanceOf(Conversion.class, operation, getName());
                conversion = (Conversion) operation;
            }
        }
        return conversion;
    }

    /**
     * Sets the conversion instance to verify. This method is invoked only by other test classes which need to
     * verify the conversion contained in a CRS instead of the conversion immediately after creation.
     *
     * @param  instance  the instance to verify.
     */
    final void setIdentifiedObject(final Conversion instance) {
        conversion = instance;
    }


    /**
     * Verifies the properties of the conversion given by {@link #getIdentifiedObject()}.
     *
     * @throws FactoryException if an error occurred while creating the conversion.
     */
    final void verifyConversion() throws FactoryException {
        if (skipTests) {
            return;
        }
        final String name = getName();
        final String code = getCode();
        final Conversion conversion = getIdentifiedObject();
        assertNotNull(conversion, "Conversion");
        validators.validate(conversion);
        verifyIdentification(conversion, name, code);
        /*
         * Now verify the properties of the projection we just created. We require the parameter
         * group to contain at least the values that we gave to it. If the library defines some
         * additional parameters, then those extra parameters will be ignored.
         */
        final ParameterValueGroup projectionParameters = conversion.getParameterValues();
        assertNotNull(projectionParameters, "Conversion.getParameterValues()");
        if (isFactoryPreservingUserValues) {
            for (final GeneralParameterValue info : definition.values()) {
                final String paramName = getName(info.getDescriptor());
                if ("semi_major".equalsIgnoreCase(paramName) ||
                        "semi_minor".equalsIgnoreCase(paramName))
                {
                    /*
                     * Semi-major and semi-minor parameters were part of OGC 01-009 specification.
                     * Since ISO 19111 they are not anymore explicit part of projection parameters.
                     * However some implementations may still use them, so we make this test tolerant.
                     */
                    continue;
                }
                if (info instanceof ParameterValue<?>) {
                    final ParameterValue<?> expected = (ParameterValue<?>) info;
                    final ParameterValue<?> param = projectionParameters.parameter(paramName);
                    assertNotNull(param, paramName);
                    final double value = expected.doubleValue();
                    assertEquals(value, param.doubleValue(expected.getUnit()), StrictMath.abs(TOLERANCE * value), paramName);
                }
            }
        }
    }

    /**
     * Tests “GIGS conversion 1”  conversion from the factory.
     *
     * <ul>
     *   <li>GIGS conversion code: <b>65001</b></li>
     *   <li>GIGS conversion name: <b>GIGS conversion 1</b></li>
     *   <li>EPSG equivalence: <b>16031 – UTM zone 31N</b></li>
     * </ul>
     * <table class="ogc">
     *   <caption>Conversion parameters</caption>
     *   <tr><th>Parameter name</th><th>Value</th></tr>
     *   <tr><td>Latitude of natural origin</td><td>0.0°</td></tr>
     *   <tr><td>Longitude of natural origin</td><td>3.0°</td></tr>
     *   <tr><td>Scale factor at natural origin</td><td>0.9996 Unity</td></tr>
     *   <tr><td>False easting</td><td>500000.0 metres</td></tr>
     *   <tr><td>False northing</td><td>0.0 metre</td></tr>
     * </table>
     *
     * @throws FactoryException if an error occurred while creating the conversion from the properties.
     */
    @Test
    @DisplayName("GIGS conversion 1")
    public void GIGS_65001() throws FactoryException {
        setCodeAndName(65001, "GIGS conversion 1");
        methodName = "Transverse Mercator";
        createDefaultParameters();
        definition.parameter("Latitude of natural origin").setValue(0.0, units.degree());
        definition.parameter("Longitude of natural origin").setValue(3.0, units.degree());
        definition.parameter("Scale factor at natural origin").setValue(0.9996, units.one());
        definition.parameter("False easting").setValue(500000.0, units.metre());
        definition.parameter("False northing").setValue(0.0, units.metre());
        verifyConversion();
    }

    /**
     * Tests “GIGS conversion 10”  conversion from the factory.
     *
     * <ul>
     *   <li>GIGS conversion code: <b>65010</b></li>
     *   <li>GIGS conversion name: <b>GIGS conversion 10</b></li>
     *   <li>EPSG equivalence: <b>17521 – South African Survey Grid zone 21</b></li>
     * </ul>
     * <table class="ogc">
     *   <caption>Conversion parameters</caption>
     *   <tr><th>Parameter name</th><th>Value</th></tr>
     *   <tr><td>Latitude of natural origin</td><td>0.0°</td></tr>
     *   <tr><td>Longitude of natural origin</td><td>21.0°</td></tr>
     *   <tr><td>Scale factor at natural origin</td><td>1.0 Unity</td></tr>
     *   <tr><td>False easting</td><td>0.0 metre</td></tr>
     *   <tr><td>False northing</td><td>0.0 metre</td></tr>
     * </table>
     *
     * @throws FactoryException if an error occurred while creating the conversion from the properties.
     */
    @Test
    @DisplayName("GIGS conversion 10")
    public void GIGS_65010() throws FactoryException {
        setCodeAndName(65010, "GIGS conversion 10");
        methodName = "Transverse Mercator (South Orientated)";
        createDefaultParameters();
        definition.parameter("Latitude of natural origin").setValue(0.0, units.degree());
        definition.parameter("Longitude of natural origin").setValue(21.0, units.degree());
        definition.parameter("Scale factor at natural origin").setValue(1.0, units.one());
        definition.parameter("False easting").setValue(0.0, units.metre());
        definition.parameter("False northing").setValue(0.0, units.metre());
        verifyConversion();
    }

    /**
     * Tests “GIGS conversion 11”  conversion from the factory.
     *
     * <ul>
     *   <li>GIGS conversion code: <b>65011</b></li>
     *   <li>GIGS conversion name: <b>GIGS conversion 11</b></li>
     *   <li>EPSG equivalence: <b>18035 – Argentina zone 5</b></li>
     * </ul>
     * <table class="ogc">
     *   <caption>Conversion parameters</caption>
     *   <tr><th>Parameter name</th><th>Value</th></tr>
     *   <tr><td>Latitude of natural origin</td><td>-90.0°</td></tr>
     *   <tr><td>Longitude of natural origin</td><td>-60.0°</td></tr>
     *   <tr><td>Scale factor at natural origin</td><td>1.0 Unity</td></tr>
     *   <tr><td>False easting</td><td>5500000.0 metres</td></tr>
     *   <tr><td>False northing</td><td>0.0 metre</td></tr>
     * </table>
     *
     * @throws FactoryException if an error occurred while creating the conversion from the properties.
     */
    @Test
    @DisplayName("GIGS conversion 11")
    public void GIGS_65011() throws FactoryException {
        setCodeAndName(65011, "GIGS conversion 11");
        methodName = "Transverse Mercator";
        createDefaultParameters();
        definition.parameter("Latitude of natural origin").setValue(-90.0, units.degree());
        definition.parameter("Longitude of natural origin").setValue(-60.0, units.degree());
        definition.parameter("Scale factor at natural origin").setValue(1.0, units.one());
        definition.parameter("False easting").setValue(5500000.0, units.metre());
        definition.parameter("False northing").setValue(0.0, units.metre());
        verifyConversion();
    }

    /**
     * Tests “GIGS conversion 12”  conversion from the factory.
     *
     * <ul>
     *   <li>GIGS conversion code: <b>65012</b></li>
     *   <li>GIGS conversion name: <b>GIGS conversion 12</b></li>
     *   <li>EPSG equivalence: <b>19941 – Brazil Polyconic</b></li>
     * </ul>
     * <table class="ogc">
     *   <caption>Conversion parameters</caption>
     *   <tr><th>Parameter name</th><th>Value</th></tr>
     *   <tr><td>Latitude of natural origin</td><td>0.0°</td></tr>
     *   <tr><td>Longitude of natural origin</td><td>-54.0°</td></tr>
     *   <tr><td>False easting</td><td>5000000.0 metres</td></tr>
     *   <tr><td>False northing</td><td>1.0E7 metres</td></tr>
     * </table>
     *
     * @throws FactoryException if an error occurred while creating the conversion from the properties.
     */
    @Test
    @DisplayName("GIGS conversion 12")
    public void GIGS_65012() throws FactoryException {
        setCodeAndName(65012, "GIGS conversion 12");
        methodName = "American Polyconic";
        createDefaultParameters();
        definition.parameter("Latitude of natural origin").setValue(0.0, units.degree());
        definition.parameter("Longitude of natural origin").setValue(-54.0, units.degree());
        definition.parameter("False easting").setValue(5000000.0, units.metre());
        definition.parameter("False northing").setValue(1.0E7, units.metre());
        verifyConversion();
    }

    /**
     * Tests “GIGS conversion 13”  conversion from the factory.
     *
     * <ul>
     *   <li>GIGS conversion code: <b>65013</b></li>
     *   <li>GIGS conversion name: <b>GIGS conversion 13</b></li>
     * </ul>
     * <table class="ogc">
     *   <caption>Conversion parameters</caption>
     *   <tr><th>Parameter name</th><th>Value</th></tr>
     *   <tr><td>Latitude of projection centre</td><td>4.0°</td></tr>
     *   <tr><td>Longitude of projection centre</td><td>115.0°</td></tr>
     *   <tr><td>Azimuth of initial line</td><td>53.31580994°</td></tr>
     *   <tr><td>Angle from Rectified to Skew Grid</td><td>53.13010236°</td></tr>
     *   <tr><td>Scale factor on initial line</td><td>0.99984 Unity</td></tr>
     *   <tr><td>Easting at projection centre</td><td>590521.147 metres</td></tr>
     *   <tr><td>Northing at projection centre</td><td>442890.861 metres</td></tr>
     * </table>
     *
     * Remarks: No direct EPSG equivalent.
     * EPSG 19894 but referenced using Hotine Oblique Mercator (variant B) rather than Hotine Oblique Mercator (variant A) method.
     *
     * @throws FactoryException if an error occurred while creating the conversion from the properties.
     */
    @Test
    @DisplayName("GIGS conversion 13")
    public void GIGS_65013() throws FactoryException {
        setCodeAndName(65013, "GIGS conversion 13");
        methodName = "Hotine Oblique Mercator (variant B)";
        createDefaultParameters();
        definition.parameter("Latitude of projection centre").setValue(4.0, units.degree());
        definition.parameter("Longitude of projection centre").setValue(115.0, units.degree());
        definition.parameter("Azimuth of initial line").setValue(53.31580994, units.degree());
        definition.parameter("Angle from Rectified to Skew Grid").setValue(53.13010236, units.degree());
        definition.parameter("Scale factor on initial line").setValue(0.99984, units.one());
        definition.parameter("Easting at projection centre").setValue(590521.147, units.metre());
        definition.parameter("Northing at projection centre").setValue(442890.861, units.metre());
        verifyConversion();
    }

    /**
     * Tests “GIGS conversion 14”  conversion from the factory.
     *
     * <ul>
     *   <li>GIGS conversion code: <b>65014</b></li>
     *   <li>GIGS conversion name: <b>GIGS conversion 14</b></li>
     *   <li>EPSG equivalence: <b>19894 – Borneo RSO</b></li>
     * </ul>
     * <table class="ogc">
     *   <caption>Conversion parameters</caption>
     *   <tr><th>Parameter name</th><th>Value</th></tr>
     *   <tr><td>Latitude of projection centre</td><td>4.0°</td></tr>
     *   <tr><td>Longitude of projection centre</td><td>115.0°</td></tr>
     *   <tr><td>Azimuth of initial line</td><td>53.31580994°</td></tr>
     *   <tr><td>Angle from Rectified to Skew Grid</td><td>53.13010236°</td></tr>
     *   <tr><td>Scale factor on initial line</td><td>0.99984 Unity</td></tr>
     *   <tr><td>False easting</td><td>0.0 metre</td></tr>
     *   <tr><td>False northing</td><td>0.0 metre</td></tr>
     * </table>
     *
     * @throws FactoryException if an error occurred while creating the conversion from the properties.
     */
    @Test
    @DisplayName("GIGS conversion 14")
    public void GIGS_65014() throws FactoryException {
        setCodeAndName(65014, "GIGS conversion 14");
        methodName = "Hotine Oblique Mercator (variant A)";
        createDefaultParameters();
        definition.parameter("Latitude of projection centre").setValue(4.0, units.degree());
        definition.parameter("Longitude of projection centre").setValue(115.0, units.degree());
        definition.parameter("Azimuth of initial line").setValue(53.31580994, units.degree());
        definition.parameter("Angle from Rectified to Skew Grid").setValue(53.13010236, units.degree());
        definition.parameter("Scale factor on initial line").setValue(0.99984, units.one());
        definition.parameter("False easting").setValue(0.0, units.metre());
        definition.parameter("False northing").setValue(0.0, units.metre());
        verifyConversion();
    }

    /**
     * Tests “GIGS conversion 15”  conversion from the factory.
     *
     * <ul>
     *   <li>GIGS conversion code: <b>65015</b></li>
     *   <li>GIGS conversion name: <b>GIGS conversion 15</b></li>
     *   <li>EPSG equivalence: <b>19893 – Johor Grid</b></li>
     * </ul>
     * <table class="ogc">
     *   <caption>Conversion parameters</caption>
     *   <tr><th>Parameter name</th><th>Value</th></tr>
     *   <tr><td>Latitude of natural origin</td><td>2.121679722°</td></tr>
     *   <tr><td>Longitude of natural origin</td><td>103.4279362°</td></tr>
     *   <tr><td>False easting</td><td>-14810.562 metres</td></tr>
     *   <tr><td>False northing</td><td>8758.32 metres</td></tr>
     * </table>
     *
     * @throws FactoryException if an error occurred while creating the conversion from the properties.
     */
    @Test
    @DisplayName("GIGS conversion 15")
    public void GIGS_65015() throws FactoryException {
        setCodeAndName(65015, "GIGS conversion 15");
        methodName = "Cassini-Soldner";
        createDefaultParameters();
        definition.parameter("Latitude of natural origin").setValue(2.121679722, units.degree());
        definition.parameter("Longitude of natural origin").setValue(103.4279362, units.degree());
        definition.parameter("False easting").setValue(-14810.562, units.metre());
        definition.parameter("False northing").setValue(8758.32, units.metre());
        verifyConversion();
    }

    /**
     * Tests “GIGS conversion 16”  conversion from the factory.
     *
     * <ul>
     *   <li>GIGS conversion code: <b>65016</b></li>
     *   <li>GIGS conversion name: <b>GIGS conversion 16</b></li>
     *   <li>EPSG equivalence: <b>19986 – Europe Equal Area 2001</b></li>
     * </ul>
     * <table class="ogc">
     *   <caption>Conversion parameters</caption>
     *   <tr><th>Parameter name</th><th>Value</th></tr>
     *   <tr><td>Latitude of natural origin</td><td>52.0°</td></tr>
     *   <tr><td>Longitude of natural origin</td><td>10.0°</td></tr>
     *   <tr><td>False easting</td><td>4321000.0 metres</td></tr>
     *   <tr><td>False northing</td><td>3210000.0 metres</td></tr>
     * </table>
     *
     * @throws FactoryException if an error occurred while creating the conversion from the properties.
     */
    @Test
    @DisplayName("GIGS conversion 16")
    public void GIGS_65016() throws FactoryException {
        setCodeAndName(65016, "GIGS conversion 16");
        methodName = "Lambert Azimuthal Equal Area";
        createDefaultParameters();
        definition.parameter("Latitude of natural origin").setValue(52.0, units.degree());
        definition.parameter("Longitude of natural origin").setValue(10.0, units.degree());
        definition.parameter("False easting").setValue(4321000.0, units.metre());
        definition.parameter("False northing").setValue(3210000.0, units.metre());
        verifyConversion();
    }

    /**
     * Tests “GIGS conversion 17”  conversion from the factory.
     *
     * <ul>
     *   <li>GIGS conversion code: <b>65017</b></li>
     *   <li>GIGS conversion name: <b>GIGS conversion 17</b></li>
     *   <li>EPSG equivalence: <b>15362 – SPCS83 Utah North zone (International feet)</b></li>
     * </ul>
     * <table class="ogc">
     *   <caption>Conversion parameters</caption>
     *   <tr><th>Parameter name</th><th>Value</th></tr>
     *   <tr><td>Latitude of false origin</td><td>40.33333333°</td></tr>
     *   <tr><td>Longitude of false origin</td><td>-111.5°</td></tr>
     *   <tr><td>Latitude of 1st standard parallel</td><td>41.78333333°</td></tr>
     *   <tr><td>Latitude of 2nd standard parallel</td><td>40.71666667°</td></tr>
     *   <tr><td>Easting at false origin</td><td>1640419.948 foots</td></tr>
     *   <tr><td>Northing at false origin</td><td>3280839.895 foots</td></tr>
     * </table>
     *
     * @throws FactoryException if an error occurred while creating the conversion from the properties.
     */
    @Test
    @DisplayName("GIGS conversion 17")
    public void GIGS_65017() throws FactoryException {
        setCodeAndName(65017, "GIGS conversion 17");
        methodName = "Lambert Conic Conformal (2SP)";
        createDefaultParameters();
        definition.parameter("Latitude of false origin").setValue(40.33333333, units.degree());
        definition.parameter("Longitude of false origin").setValue(-111.5, units.degree());
        definition.parameter("Latitude of 1st standard parallel").setValue(41.78333333, units.degree());
        definition.parameter("Latitude of 2nd standard parallel").setValue(40.71666667, units.degree());
        definition.parameter("Easting at false origin").setValue(1640419.948, units.foot());
        definition.parameter("Northing at false origin").setValue(3280839.895, units.foot());
        verifyConversion();
    }

    /**
     * Tests “GIGS conversion 18”  conversion from the factory.
     *
     * <ul>
     *   <li>GIGS conversion code: <b>65018</b></li>
     *   <li>GIGS conversion name: <b>GIGS conversion 18</b></li>
     *   <li>EPSG equivalence: <b>15297 – SPCS83 Utah North zone (US Survey feet)</b></li>
     * </ul>
     * <table class="ogc">
     *   <caption>Conversion parameters</caption>
     *   <tr><th>Parameter name</th><th>Value</th></tr>
     *   <tr><td>Latitude of false origin</td><td>40.33333333°</td></tr>
     *   <tr><td>Longitude of false origin</td><td>-111.5°</td></tr>
     *   <tr><td>Latitude of 1st standard parallel</td><td>41.78333333°</td></tr>
     *   <tr><td>Latitude of 2nd standard parallel</td><td>40.71666667°</td></tr>
     *   <tr><td>Easting at false origin</td><td>1640416.667 US survey foots</td></tr>
     *   <tr><td>Northing at false origin</td><td>3280833.333 US survey foots</td></tr>
     * </table>
     *
     * @throws FactoryException if an error occurred while creating the conversion from the properties.
     */
    @Test
    @DisplayName("GIGS conversion 18")
    public void GIGS_65018() throws FactoryException {
        setCodeAndName(65018, "GIGS conversion 18");
        methodName = "Lambert Conic Conformal (2SP)";
        createDefaultParameters();
        definition.parameter("Latitude of false origin").setValue(40.33333333, units.degree());
        definition.parameter("Longitude of false origin").setValue(-111.5, units.degree());
        definition.parameter("Latitude of 1st standard parallel").setValue(41.78333333, units.degree());
        definition.parameter("Latitude of 2nd standard parallel").setValue(40.71666667, units.degree());
        definition.parameter("Easting at false origin").setValue(1640416.667, units.footSurveyUS());
        definition.parameter("Northing at false origin").setValue(3280833.333, units.footSurveyUS());
        verifyConversion();
    }

    /**
     * Tests “GIGS conversion 19”  conversion from the factory.
     *
     * <ul>
     *   <li>GIGS conversion code: <b>65019</b></li>
     *   <li>GIGS conversion name: <b>GIGS conversion 19</b></li>
     *   <li>EPSG equivalence: <b>18082 – Lambert zone II</b></li>
     * </ul>
     * <table class="ogc">
     *   <caption>Conversion parameters</caption>
     *   <tr><th>Parameter name</th><th>Value</th></tr>
     *   <tr><td>Latitude of natural origin</td><td>52.0 grads</td></tr>
     *   <tr><td>Longitude of natural origin</td><td>0.0 grad</td></tr>
     *   <tr><td>Scale factor at natural origin</td><td>0.99987742 Unity</td></tr>
     *   <tr><td>False easting</td><td>600000.0 metres</td></tr>
     *   <tr><td>False northing</td><td>2200000.0 metres</td></tr>
     * </table>
     *
     * Remarks: Referenced to Paris meridian.
     *
     * @throws FactoryException if an error occurred while creating the conversion from the properties.
     */
    @Test
    @DisplayName("GIGS conversion 19")
    public void GIGS_65019() throws FactoryException {
        setCodeAndName(65019, "GIGS conversion 19");
        methodName = "Lambert Conic Conformal (1SP)";
        createDefaultParameters();
        definition.parameter("Latitude of natural origin").setValue(52.0, units.grad());
        definition.parameter("Longitude of natural origin").setValue(0.0, units.grad());
        definition.parameter("Scale factor at natural origin").setValue(0.99987742, units.one());
        definition.parameter("False easting").setValue(600000.0, units.metre());
        definition.parameter("False northing").setValue(2200000.0, units.metre());
        verifyConversion();
    }

    /**
     * Tests “GIGS conversion 2”  conversion from the factory.
     *
     * <ul>
     *   <li>GIGS conversion code: <b>65002</b></li>
     *   <li>GIGS conversion name: <b>GIGS conversion 2</b></li>
     *   <li>EPSG equivalence: <b>19916 – British National Grid</b></li>
     * </ul>
     * <table class="ogc">
     *   <caption>Conversion parameters</caption>
     *   <tr><th>Parameter name</th><th>Value</th></tr>
     *   <tr><td>Latitude of natural origin</td><td>49.0°</td></tr>
     *   <tr><td>Longitude of natural origin</td><td>-2.0°</td></tr>
     *   <tr><td>Scale factor at natural origin</td><td>0.999601272 Unity</td></tr>
     *   <tr><td>False easting</td><td>400000.0 metres</td></tr>
     *   <tr><td>False northing</td><td>-100000.0 metres</td></tr>
     * </table>
     *
     * Remarks: If application is unable to define a TM with origin away from the equator use conversion 65021 and 65022 definitions instead.
     *
     * @throws FactoryException if an error occurred while creating the conversion from the properties.
     */
    @Test
    @DisplayName("GIGS conversion 2")
    public void GIGS_65002() throws FactoryException {
        setCodeAndName(65002, "GIGS conversion 2");
        methodName = "Transverse Mercator";
        createDefaultParameters();
        definition.parameter("Latitude of natural origin").setValue(49.0, units.degree());
        definition.parameter("Longitude of natural origin").setValue(-2.0, units.degree());
        definition.parameter("Scale factor at natural origin").setValue(0.999601272, units.one());
        definition.parameter("False easting").setValue(400000.0, units.metre());
        definition.parameter("False northing").setValue(-100000.0, units.metre());
        verifyConversion();
    }

    /**
     * Tests “GIGS conversion 23”  conversion from the factory.
     *
     * <ul>
     *   <li>GIGS conversion code: <b>65023</b></li>
     *   <li>GIGS conversion name: <b>GIGS conversion 23</b></li>
     * </ul>
     * <table class="ogc">
     *   <caption>Conversion parameters</caption>
     *   <tr><th>Parameter name</th><th>Value</th></tr>
     *   <tr><td>Latitude of natural origin</td><td>0.0°</td></tr>
     *   <tr><td>Longitude of natural origin</td><td>3.0°</td></tr>
     *   <tr><td>Scale factor at natural origin</td><td>0.9996 Unity</td></tr>
     *   <tr><td>False easting</td><td>1640416.667 US survey foots</td></tr>
     *   <tr><td>False northing</td><td>0.0 US survey foot</td></tr>
     * </table>
     *
     * Remarks: No direct equivalent.
     * But would be called BLM 31N (ftUS).
     * EPSG 16031 (UTM zone 31N) but Units in ftUS rather than m.
     *
     * @throws FactoryException if an error occurred while creating the conversion from the properties.
     */
    @Test
    @DisplayName("GIGS conversion 23")
    public void GIGS_65023() throws FactoryException {
        setCodeAndName(65023, "GIGS conversion 23");
        methodName = "Transverse Mercator";
        createDefaultParameters();
        definition.parameter("Latitude of natural origin").setValue(0.0, units.degree());
        definition.parameter("Longitude of natural origin").setValue(3.0, units.degree());
        definition.parameter("Scale factor at natural origin").setValue(0.9996, units.one());
        definition.parameter("False easting").setValue(1640416.667, units.footSurveyUS());
        definition.parameter("False northing").setValue(0.0, units.footSurveyUS());
        verifyConversion();
    }

    /**
     * Tests “GIGS conversion 24”  conversion from the factory.
     *
     * <ul>
     *   <li>GIGS conversion code: <b>65024</b></li>
     *   <li>GIGS conversion name: <b>GIGS conversion 24</b></li>
     *   <li>EPSG equivalence: <b>19884 – Caspian Sea Mercator</b></li>
     * </ul>
     * <table class="ogc">
     *   <caption>Conversion parameters</caption>
     *   <tr><th>Parameter name</th><th>Value</th></tr>
     *   <tr><td>Latitude of 1st standard parallel</td><td>42.0°</td></tr>
     *   <tr><td>Longitude of natural origin</td><td>51.0°</td></tr>
     *   <tr><td>False easting</td><td>0.0 metre</td></tr>
     *   <tr><td>False northing</td><td>0.0 metre</td></tr>
     * </table>
     *
     * @throws FactoryException if an error occurred while creating the conversion from the properties.
     */
    @Test
    @DisplayName("GIGS conversion 24")
    public void GIGS_65024() throws FactoryException {
        setCodeAndName(65024, "GIGS conversion 24");
        methodName = "Mercator (variant B)";
        createDefaultParameters();
        definition.parameter("Latitude of 1st standard parallel").setValue(42.0, units.degree());
        definition.parameter("Longitude of natural origin").setValue(51.0, units.degree());
        definition.parameter("False easting").setValue(0.0, units.metre());
        definition.parameter("False northing").setValue(0.0, units.metre());
        verifyConversion();
    }

    /**
     * Tests “GIGS conversion 25”  conversion from the factory.
     *
     * <ul>
     *   <li>GIGS conversion code: <b>65025</b></li>
     *   <li>GIGS conversion name: <b>GIGS conversion 25</b></li>
     * </ul>
     * <table class="ogc">
     *   <caption>Conversion parameters</caption>
     *   <tr><th>Parameter name</th><th>Value</th></tr>
     *   <tr><td>Latitude of natural origin</td><td>46.8°</td></tr>
     *   <tr><td>Longitude of natural origin</td><td>2.337229167°</td></tr>
     *   <tr><td>Scale factor at natural origin</td><td>0.99987742 Unity</td></tr>
     *   <tr><td>False easting</td><td>600000.0 metres</td></tr>
     *   <tr><td>False northing</td><td>2200000.0 metres</td></tr>
     * </table>
     *
     * Remarks: No direct EPSG equivalent.
     * Deprecated EPSG 18086 France EuroLambert.
     * Remains relevant as represents LCC 1SP.
     * Not to be confused with replacement EPSG 18085 Lambert-93 (LCC 2SP).
     *
     * @throws FactoryException if an error occurred while creating the conversion from the properties.
     */
    @Test
    @DisplayName("GIGS conversion 25")
    public void GIGS_65025() throws FactoryException {
        setCodeAndName(65025, "GIGS conversion 25");
        methodName = "Lambert Conic Conformal (1SP)";
        createDefaultParameters();
        definition.parameter("Latitude of natural origin").setValue(46.8, units.degree());
        definition.parameter("Longitude of natural origin").setValue(2.337229167, units.degree());
        definition.parameter("Scale factor at natural origin").setValue(0.99987742, units.one());
        definition.parameter("False easting").setValue(600000.0, units.metre());
        definition.parameter("False northing").setValue(2200000.0, units.metre());
        verifyConversion();
    }

    /**
     * Tests “GIGS conversion 26”  conversion from the factory.
     *
     * <ul>
     *   <li>GIGS conversion code: <b>65026</b></li>
     *   <li>GIGS conversion name: <b>GIGS conversion 26</b></li>
     *   <li>EPSG equivalence: <b>19931 – Egyseges Orszagos Vetuleti</b></li>
     * </ul>
     * <table class="ogc">
     *   <caption>Conversion parameters</caption>
     *   <tr><th>Parameter name</th><th>Value</th></tr>
     *   <tr><td>Latitude of projection centre</td><td>47.1443937°</td></tr>
     *   <tr><td>Longitude of projection centre</td><td>19.0485718°</td></tr>
     *   <tr><td>Azimuth of initial line</td><td>90.0°</td></tr>
     *   <tr><td>Angle from Rectified to Skew Grid</td><td>90.0°</td></tr>
     *   <tr><td>Scale factor on initial line</td><td>0.99993 Unity</td></tr>
     *   <tr><td>Easting at projection centre</td><td>650000.0 metres</td></tr>
     *   <tr><td>Northing at projection centre</td><td>200000.0 metres</td></tr>
     * </table>
     *
     * @throws FactoryException if an error occurred while creating the conversion from the properties.
     */
    @Test
    @DisplayName("GIGS conversion 26")
    public void GIGS_65026() throws FactoryException {
        setCodeAndName(65026, "GIGS conversion 26");
        methodName = "Hotine Oblique Mercator (variant B)";
        createDefaultParameters();
        definition.parameter("Latitude of projection centre").setValue(47.1443937, units.degree());
        definition.parameter("Longitude of projection centre").setValue(19.0485718, units.degree());
        definition.parameter("Azimuth of initial line").setValue(90.0, units.degree());
        definition.parameter("Angle from Rectified to Skew Grid").setValue(90.0, units.degree());
        definition.parameter("Scale factor on initial line").setValue(0.99993, units.one());
        definition.parameter("Easting at projection centre").setValue(650000.0, units.metre());
        definition.parameter("Northing at projection centre").setValue(200000.0, units.metre());
        verifyConversion();
    }

    /**
     * Tests “GIGS conversion 27”  conversion from the factory.
     *
     * <ul>
     *   <li>GIGS conversion code: <b>65027</b></li>
     *   <li>GIGS conversion name: <b>GIGS conversion 27</b></li>
     *   <li>EPSG equivalence: <b>19905 – Netherlands East Indies Equatorial Zone</b></li>
     * </ul>
     * <table class="ogc">
     *   <caption>Conversion parameters</caption>
     *   <tr><th>Parameter name</th><th>Value</th></tr>
     *   <tr><td>Latitude of natural origin</td><td>0.0°</td></tr>
     *   <tr><td>Longitude of natural origin</td><td>110.0°</td></tr>
     *   <tr><td>Scale factor at natural origin</td><td>0.997 Unity</td></tr>
     *   <tr><td>False easting</td><td>3900000.0 metres</td></tr>
     *   <tr><td>False northing</td><td>900000.0 metres</td></tr>
     * </table>
     *
     * @throws FactoryException if an error occurred while creating the conversion from the properties.
     */
    @Test
    @DisplayName("GIGS conversion 27")
    public void GIGS_65027() throws FactoryException {
        setCodeAndName(65027, "GIGS conversion 27");
        methodName = "Mercator (variant A)";
        createDefaultParameters();
        definition.parameter("Latitude of natural origin").setValue(0.0, units.degree());
        definition.parameter("Longitude of natural origin").setValue(110.0, units.degree());
        definition.parameter("Scale factor at natural origin").setValue(0.997, units.one());
        definition.parameter("False easting").setValue(3900000.0, units.metre());
        definition.parameter("False northing").setValue(900000.0, units.metre());
        verifyConversion();
    }

    /**
     * Tests “GIGS conversion 28”  conversion from the factory.
     *
     * <ul>
     *   <li>GIGS conversion code: <b>65028</b></li>
     *   <li>GIGS conversion name: <b>GIGS conversion 28</b></li>
     *   <li>EPSG equivalence: <b>16008 – UTM zone 8N</b></li>
     * </ul>
     * <table class="ogc">
     *   <caption>Conversion parameters</caption>
     *   <tr><th>Parameter name</th><th>Value</th></tr>
     *   <tr><td>Latitude of natural origin</td><td>0.0°</td></tr>
     *   <tr><td>Longitude of natural origin</td><td>-135.0°</td></tr>
     *   <tr><td>Scale factor at natural origin</td><td>0.9996 Unity</td></tr>
     *   <tr><td>False easting</td><td>500000.0 metres</td></tr>
     *   <tr><td>False northing</td><td>0.0 metre</td></tr>
     * </table>
     *
     * @throws FactoryException if an error occurred while creating the conversion from the properties.
     */
    @Test
    @DisplayName("GIGS conversion 28")
    public void GIGS_65028() throws FactoryException {
        setCodeAndName(65028, "GIGS conversion 28");
        methodName = "Transverse Mercator";
        createDefaultParameters();
        definition.parameter("Latitude of natural origin").setValue(0.0, units.degree());
        definition.parameter("Longitude of natural origin").setValue(-135.0, units.degree());
        definition.parameter("Scale factor at natural origin").setValue(0.9996, units.one());
        definition.parameter("False easting").setValue(500000.0, units.metre());
        definition.parameter("False northing").setValue(0.0, units.metre());
        verifyConversion();
    }

    /**
     * Tests “GIGS conversion 2 alt A”  conversion from the factory.
     *
     * <ul>
     *   <li>GIGS conversion code: <b>65021</b></li>
     *   <li>GIGS conversion name: <b>GIGS conversion 2 alt A</b></li>
     * </ul>
     * <table class="ogc">
     *   <caption>Conversion parameters</caption>
     *   <tr><th>Parameter name</th><th>Value</th></tr>
     *   <tr><td>Latitude of natural origin</td><td>0.0°</td></tr>
     *   <tr><td>Longitude of natural origin</td><td>-2.0°</td></tr>
     *   <tr><td>Scale factor at natural origin</td><td>0.999601272 Unity</td></tr>
     *   <tr><td>False easting</td><td>400000.0 metres</td></tr>
     *   <tr><td>False northing</td><td>-5527462.688 metres</td></tr>
     * </table>
     *
     * Remarks: No direct EPSG equivalent.
     * Alternative when applied to WGS 84 ellipsoid.
     * Only needed if 61002 is not possible.
     *
     * @throws FactoryException if an error occurred while creating the conversion from the properties.
     */
    @Test
    @DisplayName("GIGS conversion 2 alt A")
    public void GIGS_65021() throws FactoryException {
        setCodeAndName(65021, "GIGS conversion 2 alt A");
        methodName = "Transverse Mercator";
        createDefaultParameters();
        definition.parameter("Latitude of natural origin").setValue(0.0, units.degree());
        definition.parameter("Longitude of natural origin").setValue(-2.0, units.degree());
        definition.parameter("Scale factor at natural origin").setValue(0.999601272, units.one());
        definition.parameter("False easting").setValue(400000.0, units.metre());
        definition.parameter("False northing").setValue(-5527462.688, units.metre());
        verifyConversion();
    }

    /**
     * Tests “GIGS conversion 2 alt B”  conversion from the factory.
     *
     * <ul>
     *   <li>GIGS conversion code: <b>65022</b></li>
     *   <li>GIGS conversion name: <b>GIGS conversion 2 alt B</b></li>
     * </ul>
     * <table class="ogc">
     *   <caption>Conversion parameters</caption>
     *   <tr><th>Parameter name</th><th>Value</th></tr>
     *   <tr><td>Latitude of natural origin</td><td>0.0°</td></tr>
     *   <tr><td>Longitude of natural origin</td><td>-2.0°</td></tr>
     *   <tr><td>Scale factor at natural origin</td><td>0.999601272 Unity</td></tr>
     *   <tr><td>False easting</td><td>400000.0 metres</td></tr>
     *   <tr><td>False northing</td><td>-5527063.816 metres</td></tr>
     * </table>
     *
     * Remarks: No direct EPSG equivalent.
     * Alternative when applied to Airy 1830 ellipsoid.
     * Only needed if 61002 is not possible.
     *
     * @throws FactoryException if an error occurred while creating the conversion from the properties.
     */
    @Test
    @DisplayName("GIGS conversion 2 alt B")
    public void GIGS_65022() throws FactoryException {
        setCodeAndName(65022, "GIGS conversion 2 alt B");
        methodName = "Transverse Mercator";
        createDefaultParameters();
        definition.parameter("Latitude of natural origin").setValue(0.0, units.degree());
        definition.parameter("Longitude of natural origin").setValue(-2.0, units.degree());
        definition.parameter("Scale factor at natural origin").setValue(0.999601272, units.one());
        definition.parameter("False easting").setValue(400000.0, units.metre());
        definition.parameter("False northing").setValue(-5527063.816, units.metre());
        verifyConversion();
    }

    /**
     * Tests “GIGS conversion 4”  conversion from the factory.
     *
     * <ul>
     *   <li>GIGS conversion code: <b>65004</b></li>
     *   <li>GIGS conversion name: <b>GIGS conversion 4</b></li>
     *   <li>EPSG equivalence: <b>19914 – RD New</b></li>
     * </ul>
     * <table class="ogc">
     *   <caption>Conversion parameters</caption>
     *   <tr><th>Parameter name</th><th>Value</th></tr>
     *   <tr><td>Latitude of natural origin</td><td>52.15616056°</td></tr>
     *   <tr><td>Longitude of natural origin</td><td>5.387638889°</td></tr>
     *   <tr><td>Scale factor at natural origin</td><td>0.9999079 metre</td></tr>
     *   <tr><td>False easting</td><td>155000.0 metres</td></tr>
     *   <tr><td>False northing</td><td>463000.0 metres</td></tr>
     * </table>
     *
     * @throws FactoryException if an error occurred while creating the conversion from the properties.
     */
    @Test
    @DisplayName("GIGS conversion 4")
    public void GIGS_65004() throws FactoryException {
        setCodeAndName(65004, "GIGS conversion 4");
        methodName = "Oblique Stereographic";
        createDefaultParameters();
        definition.parameter("Latitude of natural origin").setValue(52.15616056, units.degree());
        definition.parameter("Longitude of natural origin").setValue(5.387638889, units.degree());
        definition.parameter("Scale factor at natural origin").setValue(0.9999079, units.metre());
        definition.parameter("False easting").setValue(155000.0, units.metre());
        definition.parameter("False northing").setValue(463000.0, units.metre());
        verifyConversion();
    }

    /**
     * Tests “GIGS conversion 5”  conversion from the factory.
     *
     * <ul>
     *   <li>GIGS conversion code: <b>65005</b></li>
     *   <li>GIGS conversion name: <b>GIGS conversion 5</b></li>
     *   <li>EPSG equivalence: <b>5328 – Netherlands East Indies Equatorial Zone (Jkt)</b></li>
     * </ul>
     * <table class="ogc">
     *   <caption>Conversion parameters</caption>
     *   <tr><th>Parameter name</th><th>Value</th></tr>
     *   <tr><td>Latitude of natural origin</td><td>0.0°</td></tr>
     *   <tr><td>Longitude of natural origin</td><td>3.192280556°</td></tr>
     *   <tr><td>Scale factor at natural origin</td><td>0.997 Unity</td></tr>
     *   <tr><td>False easting</td><td>3900000.0 metres</td></tr>
     *   <tr><td>False northing</td><td>900000.0 metres</td></tr>
     * </table>
     *
     * Remarks: EPSG 19905 but referenced to Jakarta meridian rather than Greenwich meridian.
     * May also be defined as Mercator (1 SP) with SP1 at 4.454051545897510067.
     *
     * @throws FactoryException if an error occurred while creating the conversion from the properties.
     */
    @Test
    @DisplayName("GIGS conversion 5")
    public void GIGS_65005() throws FactoryException {
        setCodeAndName(65005, "GIGS conversion 5");
        methodName = "Mercator (variant A)";
        createDefaultParameters();
        definition.parameter("Latitude of natural origin").setValue(0.0, units.degree());
        definition.parameter("Longitude of natural origin").setValue(3.192280556, units.degree());
        definition.parameter("Scale factor at natural origin").setValue(0.997, units.one());
        definition.parameter("False easting").setValue(3900000.0, units.metre());
        definition.parameter("False northing").setValue(900000.0, units.metre());
        verifyConversion();
    }

    /**
     * Tests “GIGS conversion 6”  conversion from the factory.
     *
     * <ul>
     *   <li>GIGS conversion code: <b>65006</b></li>
     *   <li>GIGS conversion name: <b>GIGS conversion 6</b></li>
     *   <li>EPSG equivalence: <b>19961 – Belgian Lambert 72</b></li>
     * </ul>
     * <table class="ogc">
     *   <caption>Conversion parameters</caption>
     *   <tr><th>Parameter name</th><th>Value</th></tr>
     *   <tr><td>Latitude of false origin</td><td>90.0°</td></tr>
     *   <tr><td>Longitude of false origin</td><td>4.367486667°</td></tr>
     *   <tr><td>Latitude of 1st standard parallel</td><td>51.16666723°</td></tr>
     *   <tr><td>Latitude of 2nd standard parallel</td><td>49.8333339°</td></tr>
     *   <tr><td>Easting at false origin</td><td>150000.013 metres</td></tr>
     *   <tr><td>Northing at false origin</td><td>5400088.438 metres</td></tr>
     * </table>
     *
     * @throws FactoryException if an error occurred while creating the conversion from the properties.
     */
    @Test
    @DisplayName("GIGS conversion 6")
    public void GIGS_65006() throws FactoryException {
        setCodeAndName(65006, "GIGS conversion 6");
        methodName = "Lambert Conic Conformal (2SP)";
        createDefaultParameters();
        definition.parameter("Latitude of false origin").setValue(90.0, units.degree());
        definition.parameter("Longitude of false origin").setValue(4.367486667, units.degree());
        definition.parameter("Latitude of 1st standard parallel").setValue(51.16666723, units.degree());
        definition.parameter("Latitude of 2nd standard parallel").setValue(49.8333339, units.degree());
        definition.parameter("Easting at false origin").setValue(150000.013, units.metre());
        definition.parameter("Northing at false origin").setValue(5400088.438, units.metre());
        verifyConversion();
    }

    /**
     * Tests “GIGS conversion 7”  conversion from the factory.
     *
     * <ul>
     *   <li>GIGS conversion code: <b>65007</b></li>
     *   <li>GIGS conversion name: <b>GIGS conversion 7</b></li>
     *   <li>EPSG equivalence: <b>17454 – Australian Map Grid zone 54</b></li>
     * </ul>
     * <table class="ogc">
     *   <caption>Conversion parameters</caption>
     *   <tr><th>Parameter name</th><th>Value</th></tr>
     *   <tr><td>Latitude of natural origin</td><td>0.0°</td></tr>
     *   <tr><td>Longitude of natural origin</td><td>141.0°</td></tr>
     *   <tr><td>Scale factor at natural origin</td><td>0.9996 Unity</td></tr>
     *   <tr><td>False easting</td><td>500000.0 metres</td></tr>
     *   <tr><td>False northing</td><td>1.0E7 metres</td></tr>
     * </table>
     *
     * @throws FactoryException if an error occurred while creating the conversion from the properties.
     */
    @Test
    @DisplayName("GIGS conversion 7")
    public void GIGS_65007() throws FactoryException {
        setCodeAndName(65007, "GIGS conversion 7");
        methodName = "Transverse Mercator";
        createDefaultParameters();
        definition.parameter("Latitude of natural origin").setValue(0.0, units.degree());
        definition.parameter("Longitude of natural origin").setValue(141.0, units.degree());
        definition.parameter("Scale factor at natural origin").setValue(0.9996, units.one());
        definition.parameter("False easting").setValue(500000.0, units.metre());
        definition.parameter("False northing").setValue(1.0E7, units.metre());
        verifyConversion();
    }

    /**
     * Tests “GIGS conversion 8”  conversion from the factory.
     *
     * <ul>
     *   <li>GIGS conversion code: <b>65008</b></li>
     *   <li>GIGS conversion name: <b>GIGS conversion 8</b></li>
     *   <li>EPSG equivalence: <b>17455 – Australian Map Grid zone 55</b></li>
     * </ul>
     * <table class="ogc">
     *   <caption>Conversion parameters</caption>
     *   <tr><th>Parameter name</th><th>Value</th></tr>
     *   <tr><td>Latitude of natural origin</td><td>0.0°</td></tr>
     *   <tr><td>Longitude of natural origin</td><td>147.0°</td></tr>
     *   <tr><td>Scale factor at natural origin</td><td>0.9996 Unity</td></tr>
     *   <tr><td>False easting</td><td>500000.0 metres</td></tr>
     *   <tr><td>False northing</td><td>1.0E7 metres</td></tr>
     * </table>
     *
     * @throws FactoryException if an error occurred while creating the conversion from the properties.
     */
    @Test
    @DisplayName("GIGS conversion 8")
    public void GIGS_65008() throws FactoryException {
        setCodeAndName(65008, "GIGS conversion 8");
        methodName = "Transverse Mercator";
        createDefaultParameters();
        definition.parameter("Latitude of natural origin").setValue(0.0, units.degree());
        definition.parameter("Longitude of natural origin").setValue(147.0, units.degree());
        definition.parameter("Scale factor at natural origin").setValue(0.9996, units.one());
        definition.parameter("False easting").setValue(500000.0, units.metre());
        definition.parameter("False northing").setValue(1.0E7, units.metre());
        verifyConversion();
    }

    /**
     * Tests “GIGS conversion 9”  conversion from the factory.
     *
     * <ul>
     *   <li>GIGS conversion code: <b>65009</b></li>
     *   <li>GIGS conversion name: <b>GIGS conversion 9</b></li>
     *   <li>EPSG equivalence: <b>17365 – Australian Albers</b></li>
     * </ul>
     * <table class="ogc">
     *   <caption>Conversion parameters</caption>
     *   <tr><th>Parameter name</th><th>Value</th></tr>
     *   <tr><td>Latitude of false origin</td><td>0.0°</td></tr>
     *   <tr><td>Longitude of false origin</td><td>132.0°</td></tr>
     *   <tr><td>Latitude of 1st standard parallel</td><td>-18.0°</td></tr>
     *   <tr><td>Latitude of 2nd standard parallel</td><td>-36.0°</td></tr>
     *   <tr><td>Easting at false origin</td><td>0.0 metre</td></tr>
     *   <tr><td>Northing at false origin</td><td>0.0 metre</td></tr>
     * </table>
     *
     * @throws FactoryException if an error occurred while creating the conversion from the properties.
     */
    @Test
    @DisplayName("GIGS conversion 9")
    public void GIGS_65009() throws FactoryException {
        setCodeAndName(65009, "GIGS conversion 9");
        methodName = "Albers Equal Area";
        createDefaultParameters();
        definition.parameter("Latitude of false origin").setValue(0.0, units.degree());
        definition.parameter("Longitude of false origin").setValue(132.0, units.degree());
        definition.parameter("Latitude of 1st standard parallel").setValue(-18.0, units.degree());
        definition.parameter("Latitude of 2nd standard parallel").setValue(-36.0, units.degree());
        definition.parameter("Easting at false origin").setValue(0.0, units.metre());
        definition.parameter("Northing at false origin").setValue(0.0, units.metre());
        verifyConversion();
    }
}
