package org.iogp.gigs;

import org.iogp.gigs.internal.geoapi.Configuration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.opengis.referencing.datum.*;
import org.opengis.util.FactoryException;
import org.opengis.util.InternationalString;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("User-defined vertical datum")
public class Test3209 extends Series3000<VerticalDatum> {

    /**
     * The vertical datum created by the factory, or {@code null} if not yet created or if datum creation failed.
     *
     * @see #datumFactory
     */
    private VerticalDatum datum;

    /**
     * The vertical datum type used to create a vertical datum.
     *
     * @see #datumFactory
     */
    private VerticalDatumType datumType;

    /**
     * Factory to use for building {@link VerticalDatum} instances, or {@code null} if none.
     */
    protected final DatumFactory datumFactory;

    /**
     * Creates a new test using the given factory. If a given factory is {@code null},
     * then the tests which depend on it will be skipped.
     *
     * @param datumFactory  factory for creating {@link VerticalDatum} instances.
     */
    public Test3209(final DatumFactory datumFactory) {
        this.datumFactory = datumFactory;
    }

    /**
     * Returns information about the configuration of the test which has been run.
     * This method returns a map containing:
     *
     * <ul>
     *   <li>All the following values associated to the {@link org.opengis.test.Configuration.Key} of the same name:
     *     <ul>
     *       <li>{@link #isFactoryPreservingUserValues}</li>
     *       <li>{@link #datumFactory}</li>
     *     </ul>
     *   </li>
     * </ul>
     *
     * @return the configuration of the test being run.
     */
    @Override
    Configuration configuration() {
        final Configuration op = super.configuration();
        assertNull(op.put(Configuration.Key.datumFactory, datumFactory));
        return op;
    }

    /**
     * Returns the vertical datum instance to be tested. When this method is invoked for the first time, it creates the
     * vertical datum to test by invoking the {@link DatumFactory#createVerticalDatum(Map, VerticalDatumType)} method with the
     * current {@link #datumType} value in argument. The created object is then cached and returned in all subsequent
     * invocations of this method.
     *
     * @return the vertical datum instance to test.
     * @throws FactoryException if an error occurred while creating the vertical datum instance.
     */
    @Override
    public VerticalDatum getIdentifiedObject() throws FactoryException {
        if (datum == null) {
            datum = datumFactory.createVerticalDatum(properties, datumType);
        }
        return datum;
    }

    /**
     * Verifies the properties of the vertical datum given by {@link #getIdentifiedObject()}.
     *
     * @throws FactoryException if an error occurred while creating the datum.
     */
    private void verifyVerticalDatum() throws FactoryException {
        if (skipTests) {
            return;
        }
        final String name   = getName();
        final String code   = getCode();
        final String origin = (String) properties.get(GeodeticDatum.ANCHOR_POINT_KEY);
        final VerticalDatum datum = getIdentifiedObject();
        assertNotNull(datum, "VerticalDatum");
        validators.validate(datum);

        verifyIdentification(datum, name, code);
        if (origin != null) {
            final InternationalString actual = datum.getAnchorPoint();
            assertNotNull(actual, "VerticalDatum.getAnchorPoint()");
            assertEquals(origin, actual.toString(), "VerticalDatum.getAnchorPoint()");
        }
    }

    /**
     * Tests “GIGS vertical datum U”  vertical datum from the factory.
     *
     * <ul>
     *   <li>GIGS datum code: <b>66601</b></li>
     *   <li>GIGS datum name: <b>GIGS vertical datum U</b></li>
     *   <li>Datum Origin: <b>Origin U</b></li>
     *   <li>EPSG equivalence: <b>5134 – Black Sea</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the properties.
     */
    @Test
    @DisplayName("GIGS vertical datum U")
    public void GIGS_66601() throws FactoryException {
        setCodeAndName(66601, "GIGS vertical datum U");
        properties.put(Datum.ANCHOR_POINT_KEY, "Origin U");
        datumType = VerticalDatumType.GEOIDAL;
        verifyVerticalDatum();
    }

    /**
     * Tests “GIGS vertical datum V”  vertical datum from the factory.
     *
     * <ul>
     *   <li>GIGS datum code: <b>66602</b></li>
     *   <li>GIGS datum name: <b>GIGS vertical datum V</b></li>
     *   <li>Datum Origin: <b>Origin V</b></li>
     *   <li>EPSG equivalence: <b>5105 – Baltic 1977</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the properties.
     */
    @Test
    @DisplayName("GIGS vertical datum V")
    public void GIGS_66602() throws FactoryException {
        setCodeAndName(66602, "GIGS vertical datum V");
        properties.put(Datum.ANCHOR_POINT_KEY, "Origin V");
        datumType = VerticalDatumType.GEOIDAL;
        verifyVerticalDatum();
    }

    /**
     * Tests “GIGS vertical datum W”  vertical datum from the factory.
     *
     * <ul>
     *   <li>GIGS datum code: <b>66603</b></li>
     *   <li>GIGS datum name: <b>GIGS vertical datum W</b></li>
     *   <li>Datum Origin: <b>Origin W</b></li>
     *   <li>EPSG equivalence: <b>5106 – Caspian Sea</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the datum from the properties.
     */
    @Test
    @DisplayName("GIGS vertical datum W")
    public void GIGS_66603() throws FactoryException {
        setCodeAndName(66603, "GIGS vertical datum W");
        properties.put(Datum.ANCHOR_POINT_KEY, "Origin W");
        datumType = VerticalDatumType.GEOIDAL;
        verifyVerticalDatum();
    }

}
