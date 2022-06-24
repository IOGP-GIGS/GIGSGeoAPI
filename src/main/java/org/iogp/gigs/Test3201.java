package org.iogp.gigs;

import org.iogp.gigs.internal.geoapi.Configuration;
import org.iogp.gigs.internal.geoapi.Units;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.opengis.referencing.IdentifiedObject;
import org.opengis.referencing.datum.Ellipsoid;
import org.opengis.referencing.datum.GeodeticDatum;
import org.opengis.referencing.datum.PrimeMeridian;
import org.opengis.util.FactoryException;

import javax.measure.IncommensurableException;
import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.UnitConverter;
import javax.measure.quantity.Angle;
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Length;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies that the software allows correct definition of a user-defined unit.
 *
 * <table class="gigs">
 * <caption>Test description</caption>
 * <tr>
 *   <th>Test method:</th>
 *   <td>Create user-defined unit for each of several different units.</td>
 * </tr><tr>
 *   <th>Test data:</th>
 *   <td><a href="https://github.com/IOGP-GIGS/GIGSTestDataset/tree/main/GIGSTestDatasetFiles/GIGS%202200%20Predefined%20Geodetic%20Data%20Objects%20test%20data/ASCII/GIGS_lib_3201_Unit.txt">{@code GIGS_lib_3201_Unit.txt}</a>
 *   and EPSG Dataset.
 *   Contains EPSG {@linkplain #code code} and {@linkplain #name name} for the unit of measure, together with the
 *   {@linkplain #unitToBase ratio} of the unit to the ISO {@linkplain #baseUnit base unit} for that unit type.
 *   The test methods are separated in three blocks for linear units, angular units and scaling units.</td>
 * </tr><tr>
 *   <th>Expected result:</th>
 *   <td>The software should accept the test data. The properties of the created objects will
 *       be compared with the properties given to the factory method.</td>
 * </tr></table>
 *
 * @author  Michael Arneson (INT)
 * @version 1.0
 * @since   1.0
 */
@DisplayName("User-defined unit")
public class Test3201 extends Series3000<Unit<?>> {

    /**
     * Amount of {@link Unit#getSystemUnit()  base units} in one {@linkplain #getIdentifiedObject() tested unit}.
     * If this amount is not a constant (as in sexagesimal unit), then this factor is set to {@link Double#NaN}.
     * This field is set by all test methods before to create and verify the {@link Unit} instance.
     */
    private double unitToBase;

    /**
     * The base unit of the unit to create. This field will have one of the following values:
     *
     * <ul>
     *   <li>{@link Units#metre()} if the unit to create is linear,</li>
     *   <li>{@link Units#radian()} if the unit to create is angular (except sexagesimal unit),</li>
     *   <li>{@link Units#one()} if the unit to create is a scale or a coefficient, or</li>
     *   <li>(exceptionally) {@link Units#degree()} in the special case of {@link #testSexagesimalDegree()}.
     *       Note that sexagesimal units also have the {@linkplain #unitToBase ratio of the unit to base unit}
     *       set to {@link Double#NaN}.</li>
     * </ul>
     *
     * This field is set by all test methods before to create and verify the {@link Unit} instance.
     */
    private Unit<?> baseUnit;

    /**
     * The unit of measurement created by the factory,
     * or {@code null} if not yet created or if the unit creation failed.
     *
     * @see #SystemOfUnits
     */
    private Unit<?> unit;


    /**
     * Creates a new test.
     */
    public Test3201() {
    }

    /**
     * Returns the unit instance to be tested. When this method is invoked for the first time, it creates the unit
     * to test by invoking necessary call from {@link Unit#multiply(double)} the baseUnit using the baseToUnit
     * parameter.
     * The created object is then cached and returned in all subsequent invocations of this method.
     *
     * @return the unit instance to test.
     */
    @Override
    public Unit<?> getIdentifiedObject() {
        if (unit == null) {
            unit = baseUnit.multiply(unitToBase);
        }
        return unit;
    }

    /**
     * Gets the unit given by {@link #getIdentifiedObject()},
     * then creates and returns the converter from that unit to the base unit.
     *
     * @return converter from the unit given by the identified object to test.
     * @throws FactoryException if an error occurred while creating the unit from the EPSG code.
     */
    private UnitConverter createConverter() throws FactoryException {
        final Unit<?> unit = getIdentifiedObject();
        assertNotNull(unit, "Unit");
        try {
            return unit.getConverterToAny(baseUnit);
        } catch (IncommensurableException e) {
            fail("Can not convert “" + getName() + "” from “" + unit + "” to “" + baseUnit + "”.", e);
            throw new AssertionError();
        }
    }

    /**
     * Converts random values using the unit converter and compares against the expected value.
     * The expected values are obtained by multiplying the values to convert by the given factor.
     *
     * @param  converter  the converter from tested {@link #unit} to the base unit.
     */
    private void verifyLinearConversions(final UnitConverter converter) {
        final Random random = new Random();
        final double tolerance = TOLERANCE * unitToBase;
        assertEquals(0, converter.convert(0), tolerance, getName());
        assertEquals( unitToBase, converter.convert( 1), tolerance, getName());
        assertEquals(-unitToBase, converter.convert(-1), tolerance, getName());
        for (double sample = -90; sample <= 90; sample += 4*random.nextDouble()) {
            final double expected = sample * unitToBase;
            assertEquals(expected, converter.convert(sample), tolerance, getName());
        }
    }

    /**
     * Tests “GIGS unit A0” unit creation from the factory.
     *
     * <ul>
     *   <li>GIGS unit: <b>69101</b></li>
     *   <li>GIGS unit name: <b>GIGS unit A0</b></li>
     *   <li>EPSG equivalence: <b>9101 – radian</b></li>
     *   <li>Unit type: <b>Angle</b></li>
     *   <li>Base Units per Unit: <b>1</b></li>
     * </ul>
     *
     * Remarks: ISO/EPSG angle base unit.
     *
     * @throws FactoryException if an error occurred while creating the unit from the properties.
     *
     * @see Test2201#EPSG_9101()
     */
    @Test
    @DisplayName("GIGS unit A0")
    public void GIGS_69101() throws FactoryException {
        setCodeAndName(69101, "GIGS unit A0");
        unitToBase = 1.0;
        baseUnit   = units.system.getUnit(Angle.class);
        verifyLinearConversions(createConverter());
    }

    /**
     * Tests “GIGS unit A1” unit creation from the factory.
     *
     * <ul>
     *   <li>GIGS unit: <b>69109</b></li>
     *   <li>GIGS unit name: <b>GIGS unit A1</b></li>
     *   <li>EPSG equivalence: <b>9109 – microradian</b></li>
     *   <li>Unit type: <b>Angle</b></li>
     *   <li>Base Units per Unit: <b>1.0E-6</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the unit from the properties.
     *
     * @see Test2201#EPSG_9109()
     */
    @Test
    @DisplayName("GIGS unit A1")
    public void GIGS_69109() throws FactoryException {
        setCodeAndName(69109, "GIGS unit A1");
        unitToBase = 1.0E-6;
        baseUnit   = units.system.getUnit(Angle.class);
        verifyLinearConversions(createConverter());
    }

    /**
     * Tests “GIGS unit A2” unit creation from the factory.
     *
     * <ul>
     *   <li>GIGS unit: <b>69102</b></li>
     *   <li>GIGS unit name: <b>GIGS unit A2</b></li>
     *   <li>EPSG equivalence: <b>9102 – degree</b></li>
     *   <li>Unit type: <b>Angle</b></li>
     *   <li>Base Units per Unit: <b>0.017453293</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the unit from the properties.
     *
     * @see Test2201#EPSG_9102()
     */
    @Test
    @DisplayName("GIGS unit A2")
    public void GIGS_69102() throws FactoryException {
        setCodeAndName(69102, "GIGS unit A2");
        unitToBase = 0.017453293;
        baseUnit   = units.system.getUnit(Angle.class);
        verifyLinearConversions(createConverter());
    }

    /**
     * Tests “GIGS unit A3” unit creation from the factory.
     *
     * <ul>
     *   <li>GIGS unit: <b>69104</b></li>
     *   <li>GIGS unit name: <b>GIGS unit A3</b></li>
     *   <li>EPSG equivalence: <b>9104 – arc-second</b></li>
     *   <li>Unit type: <b>Angle</b></li>
     *   <li>Base Units per Unit: <b>4.84814E-6</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the unit from the properties.
     *
     * @see Test2201#EPSG_9104()
     */
    @Test
    @DisplayName("GIGS unit A3")
    public void GIGS_69104() throws FactoryException {
        setCodeAndName(69104, "GIGS unit A3");
        unitToBase = 4.84814E-6;
        baseUnit   = units.system.getUnit(Angle.class);
        verifyLinearConversions(createConverter());
    }

    /**
     * Tests “GIGS unit A4” unit creation from the factory.
     *
     * <ul>
     *   <li>GIGS unit: <b>69105</b></li>
     *   <li>GIGS unit name: <b>GIGS unit A4</b></li>
     *   <li>EPSG equivalence: <b>9105 – grad</b></li>
     *   <li>Unit type: <b>Angle</b></li>
     *   <li>Base Units per Unit: <b>0.015707963</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the unit from the properties.
     *
     * @see Test2201#EPSG_9105()
     */
    @Test
    @DisplayName("GIGS unit A4")
    public void GIGS_69105() throws FactoryException {
        setCodeAndName(69105, "GIGS unit A4");
        unitToBase = 0.015707963;
        baseUnit   = units.system.getUnit(Angle.class);
        verifyLinearConversions(createConverter());
    }

    /**
     * Tests “GIGS unit A5” unit creation from the factory.
     *
     * <ul>
     *   <li>GIGS unit: <b>69113</b></li>
     *   <li>GIGS unit name: <b>GIGS unit A5</b></li>
     *   <li>EPSG equivalence: <b>9113 – centesimal second</b></li>
     *   <li>Unit type: <b>Angle</b></li>
     *   <li>Base Units per Unit: <b>1.5708E-6</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the unit from the properties.
     *
     * @see Test2201#EPSG_9113()
     */
    @Test
    @DisplayName("GIGS unit A5")
    public void GIGS_69113() throws FactoryException {
        setCodeAndName(69113, "GIGS unit A5");
        unitToBase = 1.5708E-6;
        baseUnit   = units.system.getUnit(Angle.class);
        verifyLinearConversions(createConverter());
    }

    /**
     * Tests “GIGS unit L0” unit creation from the factory.
     *
     * <ul>
     *   <li>GIGS unit: <b>69001</b></li>
     *   <li>GIGS unit name: <b>GIGS unit L0</b></li>
     *   <li>EPSG equivalence: <b>9001 – metre</b></li>
     *   <li>Unit type: <b>Linear</b></li>
     *   <li>Base Units per Unit: <b>1</b></li>
     * </ul>
     *
     * Remarks: ISO/EPSG length base unit.
     *
     * @throws FactoryException if an error occurred while creating the unit from the properties.
     *
     * @see Test2201#EPSG_9001()
     */
    @Test
    @DisplayName("GIGS unit L0")
    public void GIGS_69001() throws FactoryException {
        setCodeAndName(69001, "GIGS unit L0");
        unitToBase = 1.0;
        baseUnit   = units.system.getUnit(Length.class);
        verifyLinearConversions(createConverter());
    }

    /**
     * Tests “GIGS unit L1” unit creation from the factory.
     *
     * <ul>
     *   <li>GIGS unit: <b>69036</b></li>
     *   <li>GIGS unit name: <b>GIGS unit L1</b></li>
     *   <li>EPSG equivalence: <b>9036 – kilometre</b></li>
     *   <li>Unit type: <b>Linear</b></li>
     *   <li>Base Units per Unit: <b>1000</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the unit from the properties.
     *
     * @see Test2201#EPSG_9036()
     */
    @Test
    @DisplayName("GIGS unit L1")
    public void GIGS_69036() throws FactoryException {
        setCodeAndName(69036, "GIGS unit L1");
        unitToBase = 1000.0;
        baseUnit   = units.system.getUnit(Length.class);
        verifyLinearConversions(createConverter());
    }

    /**
     * Tests “GIGS unit L10 (parts per million)” unit creation from the factory.
     *
     * <ul>
     *   <li>GIGS unit: <b>69301</b></li>
     *   <li>GIGS unit name: <b>GIGS unit L10 (parts per million)</b></li>
     *   <li>EPSG equivalence: <b>9301 – parts per million</b></li>
     *   <li>Unit type: <b>Linear</b></li>
     *   <li>Base Units per Unit: <b>20.116756</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the unit from the properties.
     *
     * @see Test2201#EPSG_9301()
     */
    @Test
    @DisplayName("GIGS unit L10 (parts per million)")
    public void GIGS_69301() throws FactoryException {
        setCodeAndName(69301, "GIGS unit L10 (parts per million)");
        unitToBase = 20.116756;
        baseUnit   = units.system.getUnit(Length.class);
        verifyLinearConversions(createConverter());
    }

    /**
     * Tests “GIGS unit L11” unit creation from the factory.
     *
     * <ul>
     *   <li>GIGS unit: <b>69084</b></li>
     *   <li>GIGS unit name: <b>GIGS unit L11</b></li>
     *   <li>EPSG equivalence: <b>9084 – Indian yard</b></li>
     *   <li>Unit type: <b>Linear</b></li>
     *   <li>Base Units per Unit: <b>0.914398531</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the unit from the properties.
     *
     * @see Test2201#EPSG_9084()
     */
    @Test
    @DisplayName("GIGS unit L11")
    public void GIGS_69084() throws FactoryException {
        setCodeAndName(69084, "GIGS unit L11");
        unitToBase = 0.914398531;
        baseUnit   = units.system.getUnit(Length.class);
        verifyLinearConversions(createConverter());
    }

    /**
     * Tests “GIGS unit L12” unit creation from the factory.
     *
     * <ul>
     *   <li>GIGS unit: <b>69094</b></li>
     *   <li>GIGS unit name: <b>GIGS unit L12</b></li>
     *   <li>EPSG equivalence: <b>9094 – Gold Coast foot</b></li>
     *   <li>Unit type: <b>Linear</b></li>
     *   <li>Base Units per Unit: <b>0.30479971</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the unit from the properties.
     *
     * @see Test2201#EPSG_9094()
     */
    @Test
    @DisplayName("GIGS unit L12")
    public void GIGS_69094() throws FactoryException {
        setCodeAndName(69094, "GIGS unit L12");
        unitToBase = 0.30479971;
        baseUnit   = units.system.getUnit(Length.class);
        verifyLinearConversions(createConverter());
    }

    /**
     * Tests “GIGS unit L13” unit creation from the factory.
     *
     * <ul>
     *   <li>GIGS unit: <b>69098</b></li>
     *   <li>GIGS unit name: <b>GIGS unit L13</b></li>
     *   <li>EPSG equivalence: <b>9098 – link</b></li>
     *   <li>Unit type: <b>Linear</b></li>
     *   <li>Base Units per Unit: <b>0.201168</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the unit from the properties.
     *
     * @see Test2201#EPSG_9098()
     */
    @Test
    @DisplayName("GIGS unit L13")
    public void GIGS_69098() throws FactoryException {
        setCodeAndName(69098, "GIGS unit L13");
        unitToBase = 0.201168;
        baseUnit   = units.system.getUnit(Length.class);
        verifyLinearConversions(createConverter());
    }

    /**
     * Tests “GIGS unit L2” unit creation from the factory.
     *
     * <ul>
     *   <li>GIGS unit: <b>69002</b></li>
     *   <li>GIGS unit name: <b>GIGS unit L2</b></li>
     *   <li>EPSG equivalence: <b>9002 – foot</b></li>
     *   <li>Unit type: <b>Linear</b></li>
     *   <li>Base Units per Unit: <b>0.3048</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the unit from the properties.
     *
     * @see Test2201#EPSG_9002()
     */
    @Test
    @DisplayName("GIGS unit L2")
    public void GIGS_69002() throws FactoryException {
        setCodeAndName(69002, "GIGS unit L2");
        unitToBase = 0.3048;
        baseUnit   = units.system.getUnit(Length.class);
        verifyLinearConversions(createConverter());
    }

    /**
     * Tests “GIGS unit L3” unit creation from the factory.
     *
     * <ul>
     *   <li>GIGS unit: <b>69003</b></li>
     *   <li>GIGS unit name: <b>GIGS unit L3</b></li>
     *   <li>EPSG equivalence: <b>9003 – US survey foot</b></li>
     *   <li>Unit type: <b>Linear</b></li>
     *   <li>Base Units per Unit: <b>0.30480061</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the unit from the properties.
     *
     * @see Test2201#EPSG_9003()
     */
    @Test
    @DisplayName("GIGS unit L3")
    public void GIGS_69003() throws FactoryException {
        setCodeAndName(69003, "GIGS unit L3");
        unitToBase = 0.30480061;
        baseUnit   = units.system.getUnit(Length.class);
        verifyLinearConversions(createConverter());
    }

    /**
     * Tests “GIGS unit L4” unit creation from the factory.
     *
     * <ul>
     *   <li>GIGS unit: <b>69031</b></li>
     *   <li>GIGS unit name: <b>GIGS unit L4</b></li>
     *   <li>EPSG equivalence: <b>9031 – German legal metre</b></li>
     *   <li>Unit type: <b>Linear</b></li>
     *   <li>Base Units per Unit: <b>1.000013597</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the unit from the properties.
     *
     * @see Test2201#EPSG_9031()
     */
    @Test
    @DisplayName("GIGS unit L4")
    public void GIGS_69031() throws FactoryException {
        setCodeAndName(69031, "GIGS unit L4");
        unitToBase = 1.000013597;
        baseUnit   = units.system.getUnit(Length.class);
        verifyLinearConversions(createConverter());
    }

    /**
     * Tests “GIGS unit L5” unit creation from the factory.
     *
     * <ul>
     *   <li>GIGS unit: <b>69005</b></li>
     *   <li>GIGS unit name: <b>GIGS unit L5</b></li>
     *   <li>EPSG equivalence: <b>9005 – Clarke's foot</b></li>
     *   <li>Unit type: <b>Linear</b></li>
     *   <li>Base Units per Unit: <b>0.304797265</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the unit from the properties.
     *
     * @see Test2201#EPSG_9005()
     */
    @Test
    @DisplayName("GIGS unit L5")
    public void GIGS_69005() throws FactoryException {
        setCodeAndName(69005, "GIGS unit L5");
        unitToBase = 0.304797265;
        baseUnit   = units.system.getUnit(Length.class);
        verifyLinearConversions(createConverter());
    }

    /**
     * Tests “GIGS unit L6” unit creation from the factory.
     *
     * <ul>
     *   <li>GIGS unit: <b>69039</b></li>
     *   <li>GIGS unit name: <b>GIGS unit L6</b></li>
     *   <li>EPSG equivalence: <b>9039 – Clarke's link</b></li>
     *   <li>Unit type: <b>Linear</b></li>
     *   <li>Base Units per Unit: <b>0.201166195</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the unit from the properties.
     *
     * @see Test2201#EPSG_9039()
     */
    @Test
    @DisplayName("GIGS unit L6")
    public void GIGS_69039() throws FactoryException {
        setCodeAndName(69039, "GIGS unit L6");
        unitToBase = 0.201166195;
        baseUnit   = units.system.getUnit(Length.class);
        verifyLinearConversions(createConverter());
    }

    /**
     * Tests “GIGS unit L7” unit creation from the factory.
     *
     * <ul>
     *   <li>GIGS unit: <b>69042</b></li>
     *   <li>GIGS unit name: <b>GIGS unit L7</b></li>
     *   <li>EPSG equivalence: <b>9042 – British chain (Sears 1922)</b></li>
     *   <li>Unit type: <b>Linear</b></li>
     *   <li>Base Units per Unit: <b>20.11676512</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the unit from the properties.
     *
     * @see Test2201#EPSG_9042()
     */
    @Test
    @DisplayName("GIGS unit L7")
    public void GIGS_69042() throws FactoryException {
        setCodeAndName(69042, "GIGS unit L7");
        unitToBase = 20.11676512;
        baseUnit   = units.system.getUnit(Length.class);
        verifyLinearConversions(createConverter());
    }

    /**
     * Tests “GIGS unit L8” unit creation from the factory.
     *
     * <ul>
     *   <li>GIGS unit: <b>69041</b></li>
     *   <li>GIGS unit name: <b>GIGS unit L8</b></li>
     *   <li>EPSG equivalence: <b>9041 – British foot (Sears 1922)</b></li>
     *   <li>Unit type: <b>Linear</b></li>
     *   <li>Base Units per Unit: <b>0.304799472</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the unit from the properties.
     *
     * @see Test2201#EPSG_9041()
     */
    @Test
    @DisplayName("GIGS unit L8")
    public void GIGS_69041() throws FactoryException {
        setCodeAndName(69041, "GIGS unit L8");
        unitToBase = 0.304799472;
        baseUnit   = units.system.getUnit(Length.class);
        verifyLinearConversions(createConverter());
    }

    /**
     * Tests “GIGS unit L9” unit creation from the factory.
     *
     * <ul>
     *   <li>GIGS unit: <b>69040</b></li>
     *   <li>GIGS unit name: <b>GIGS unit L9</b></li>
     *   <li>EPSG equivalence: <b>9040 – British yard (Sears 1922)</b></li>
     *   <li>Unit type: <b>Linear</b></li>
     *   <li>Base Units per Unit: <b>0.914398415</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the unit from the properties.
     *
     * @see Test2201#EPSG_9040()
     */
    @Test
    @DisplayName("GIGS unit L9")
    public void GIGS_69040() throws FactoryException {
        setCodeAndName(69040, "GIGS unit L9");
        unitToBase = 0.914398415;
        baseUnit   = units.system.getUnit(Length.class);
        verifyLinearConversions(createConverter());
    }

    /**
     * Tests “GIGS unit U0” unit creation from the factory.
     *
     * <ul>
     *   <li>GIGS unit: <b>69201</b></li>
     *   <li>GIGS unit name: <b>GIGS unit U0</b></li>
     *   <li>EPSG equivalence: <b>9201 – unity</b></li>
     *   <li>Unit type: <b>Scale</b></li>
     *   <li>Base Units per Unit: <b>1</b></li>
     * </ul>
     *
     * Remarks: ISO/EPSG scale base unit.
     *
     * @throws FactoryException if an error occurred while creating the unit from the properties.
     *
     * @see Test2201#EPSG_9201()
     */
    @Test
    @DisplayName("GIGS unit U0")
    public void GIGS_69201() throws FactoryException {
        setCodeAndName(69201, "GIGS unit U0");
        unitToBase = 1.0;
        baseUnit   = units.system.getUnit(Dimensionless.class);
        verifyLinearConversions(createConverter());
    }

    /**
     * Tests “GIGS unit U1” unit creation from the factory.
     *
     * <ul>
     *   <li>GIGS unit: <b>69202</b></li>
     *   <li>GIGS unit name: <b>GIGS unit U1</b></li>
     *   <li>EPSG equivalence: <b>9202 – parts per million</b></li>
     *   <li>Unit type: <b>Scale</b></li>
     *   <li>Base Units per Unit: <b>1.0E-6</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the unit from the properties.
     *
     * @see Test2201#EPSG_9202()
     */
    @Test
    @DisplayName("GIGS unit U1")
    public void GIGS_69202() throws FactoryException {
        setCodeAndName(69202, "GIGS unit U1");
        unitToBase = 1.0E-6;
        baseUnit   = units.system.getUnit(Dimensionless.class);
        verifyLinearConversions(createConverter());
    }

}