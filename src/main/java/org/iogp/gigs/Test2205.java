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
import org.opengis.referencing.datum.GeodeticDatum;
import org.opengis.referencing.cs.CoordinateSystem;
import org.opengis.referencing.cs.AxisDirection;
import org.opengis.referencing.crs.GeodeticCRS;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.iogp.gigs.internal.geoapi.Configuration;

import static org.junit.jupiter.api.Assertions.*;
import static org.iogp.gigs.internal.geoapi.Assert.assertAxisDirectionsEqual;


/**
 * Verifies geodetic reference systems bundled with the geoscience software.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @author  Alexis Manin (Geomatys)
 * @version 1.0
 * @since   1.0
 */
public class Test2205 extends Series2000<GeodeticCRS> {
    /**
     * The expected axis directions of two-dimensional geographic CRS with longitude first.
     * This axis order does not appear in the EPSG database, but appears often in user-defined CRS.
     */
    static final AxisDirection[] GEOGRAPHIC_XY = {
        AxisDirection.EAST,
        AxisDirection.NORTH
    };

    /**
     * The expected axis directions of two-dimensional geographic CRS.
     */
    static final AxisDirection[] GEOGRAPHIC_2D = {
        AxisDirection.NORTH,
        AxisDirection.EAST
    };

    /**
     * The expected axis directions of three-dimensional geographic CRS.
     */
    static final AxisDirection[] GEOGRAPHIC_3D = {
        AxisDirection.NORTH,
        AxisDirection.EAST,
        AxisDirection.UP
    };

    /**
     * The expected axis directions of geocentric CRS.
     */
    static final AxisDirection[] GEOCENTRIC = {
        AxisDirection.GEOCENTRIC_X,
        AxisDirection.GEOCENTRIC_Y,
        AxisDirection.GEOCENTRIC_Z
    };

    /**
     * The CRS created by the factory,
     * or {@code null} if not yet created or if CRS creation failed.
     *
     * @see #crsAuthorityFactory
     */
    private GeodeticCRS crs;

    /**
     * Factory to use for building {@link GeodeticCRS} instances, or {@code null} if none.
     */
    protected final CRSAuthorityFactory crsAuthorityFactory;

    /**
     * Creates a new test using the given factory. If a given factory is {@code null},
     * then the tests which depend on it will be skipped.
     *
     * @param crsFactory  factory for creating {@link GeodeticCRS} instances.
     */
    public Test2205(final CRSAuthorityFactory crsFactory) {
        crsAuthorityFactory = crsFactory;
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
        assertNull(op.put(Configuration.Key.crsAuthorityFactory, crsAuthorityFactory));
        return op;
    }

    /**
     * Returns the CRS instance to be tested. When this method is invoked for the first time, it creates the
     * CRS to test by invoking the {@link CRSAuthorityFactory#createGeographicCRS(String)} method with the
     * current {@link #code} value in argument. The created object is then cached and returned in all subsequent
     * invocations of this method.
     *
     * @return the CRS instance to test.
     * @throws FactoryException if an error occurred while creating the CRS instance.
     */
    @Override
    public GeodeticCRS getIdentifiedObject() throws FactoryException {
        if (crs == null) {
            assumeNotNull(crsAuthorityFactory);
            try {
                crs = crsAuthorityFactory.createGeographicCRS(String.valueOf(code));
            } catch (NoSuchAuthorityCodeException e) {
                unsupportedCode(GeodeticCRS.class, code);
                throw e;
            }
        }
        return crs;
    }

    /**
     * Verifies the geographic or geocentric CRS.
     *
     * @param expectedDirections  either {@link #GEOGRAPHIC_2D}, {@link #GEOGRAPHIC_3D} or {@link #GEOCENTRIC}.
     */
    private void verifyGeodeticCRS(final AxisDirection[] expectedDirections) {
        assertNotNull(crs, "GeodeticCRS");

        // Geodetic CRS identifier.
        assertContainsCode("GeodeticCRS.getIdentifiers()", "EPSG", code, crs.getIdentifiers());

        // Geodetic CRS name.
        if (isStandardNameSupported) {
            configurationTip = Configuration.Key.isStandardNameSupported;
            assertEquals(name, getVerifiableName(crs), "GeodeticCRS.getName()");
            configurationTip = null;
        }

        // Geodetic CRS datum.
        final GeodeticDatum crsDatum = crs.getDatum();
        assertNotNull(crsDatum, "GeodeticCRS.getDatum()");
        validators.validate(crsDatum);

        // Geodetic CRS coordinate system.
        final CoordinateSystem cs = crs.getCoordinateSystem();
        assertNotNull(cs, "GeodeticCRS.getCoordinateSystem()");
        assertEquals(expectedDirections.length, cs.getDimension(), "GeodeticCRS.getCoordinateSystem().getDimension()");
        assertAxisDirectionsEqual("GeodeticCRS.getCoordinateSystem().getAxis(*)", cs, expectedDirections);
    }
}
