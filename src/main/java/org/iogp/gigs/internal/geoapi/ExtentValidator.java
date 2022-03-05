/*
 *    GeoAPI - Java interfaces for OGC/ISO standards
 *    http://www.geoapi.org
 *
 *    Copyright (C) 2008-2021 Open Geospatial Consortium, Inc.
 *    All Rights Reserved. http://www.opengeospatial.org/ogc/legal
 *
 *    Permission to use, copy, and modify this software and its documentation, with
 *    or without modification, for any purpose and without fee or royalty is hereby
 *    granted, provided that you include the following on ALL copies of the software
 *    and documentation or portions thereof, including modifications, that you make:
 *
 *    1. The full text of this NOTICE in a location viewable to users of the
 *       redistributed or derivative work.
 *    2. Notice of any changes or modifications to the OGC files, including the
 *       date changes were made.
 *
 *    THIS SOFTWARE AND DOCUMENTATION IS PROVIDED "AS IS," AND COPYRIGHT HOLDERS MAKE
 *    NO REPRESENTATIONS OR WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 *    TO, WARRANTIES OF MERCHANTABILITY OR FITNESS FOR ANY PARTICULAR PURPOSE OR THAT
 *    THE USE OF THE SOFTWARE OR DOCUMENTATION WILL NOT INFRINGE ANY THIRD PARTY
 *    PATENTS, COPYRIGHTS, TRADEMARKS OR OTHER RIGHTS.
 *
 *    COPYRIGHT HOLDERS WILL NOT BE LIABLE FOR ANY DIRECT, INDIRECT, SPECIAL OR
 *    CONSEQUENTIAL DAMAGES ARISING OUT OF ANY USE OF THE SOFTWARE OR DOCUMENTATION.
 *
 *    The name and trademarks of copyright holders may NOT be used in advertising or
 *    publicity pertaining to the software without specific, written prior permission.
 *    Title to copyright in this software and any associated documentation will at all
 *    times remain with copyright holders.
 */
package org.iogp.gigs.internal.geoapi;

import org.opengis.metadata.*;
import org.opengis.metadata.extent.*;
import org.opengis.geometry.Geometry;

import static org.junit.Assert.*;
import static org.iogp.gigs.internal.geoapi.Assert.assertBetween;


/**
 * Validates {@link Extent} and related objects from the
 * {@code org.opengis.metadata.extent} package.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @version 1.0
 * @since   1.0
 */
public class ExtentValidator extends MetadataValidator {
    /**
     * Creates a new validator instance.
     *
     * @param container  the set of validators to use for validating other kinds of objects
     *                   (see {@linkplain #container field javadoc}).
     */
    public ExtentValidator(final ValidatorContainer container) {
        super(container, "org.opengis.metadata.extent");
    }

    /**
     * For each interface implemented by the given object, invokes the corresponding
     * {@code validate(…)} method defined in this class (if any).
     *
     * @param  object  the object to dispatch to {@code validate(…)} methods, or {@code null}.
     * @return number of {@code validate(…)} methods invoked in this class for the given object.
     */
    public int dispatch(final GeographicExtent object) {
        int n = 0;
        if (object != null) {
            if (object instanceof GeographicDescription) {validate((GeographicDescription) object); n++;}
            if (object instanceof GeographicBoundingBox) {validate((GeographicBoundingBox) object); n++;}
            if (object instanceof BoundingPolygon)       {validate((BoundingPolygon)       object); n++;}
        }
        return n;
    }

    /**
     * Validates the geographic description.
     *
     * @param  object  the object to validate, or {@code null}.
     */
    public void validate(final GeographicDescription object) {
        if (object == null) {
            return;
        }
        final Identifier identifier = object.getGeographicIdentifier();
        mandatory("GeographicDescription: must have an identifier.", identifier);
    }

    /**
     * Validates the bounding polygon.
     *
     * @param  object  the object to validate, or {@code null}.
     *
     * @todo Not yet implemented.
     */
    public void validate(final BoundingPolygon object) {
        if (object == null) {
            return;
        }
        for (final Geometry e : toArray(Geometry.class, object.getPolygons())) {
            // TODO
        }
    }

    /**
     * Validates the geographic bounding box.
     *
     * @param  object  the object to validate, or {@code null}.
     */
    public void validate(final GeographicBoundingBox object) {
        if (object == null) {
            return;
        }
        final double west  = object.getWestBoundLongitude();
        final double east  = object.getEastBoundLongitude();
        final double south = object.getSouthBoundLatitude();
        final double north = object.getNorthBoundLatitude();
        assertBetween("GeographicBoundingBox: illegal west bound.",  -180, +180, west);
        assertBetween("GeographicBoundingBox: illegal east bound.",  -180, +180, east);
        assertBetween("GeographicBoundingBox: illegal south bound.", -90,   +90, south);
        assertBetween("GeographicBoundingBox: illegal north bound.", -90,   +90, north);
        assertFalse("GeographicBoundingBox: invalid range of latitudes.",  south > north);          // Accept NaN.
        // Do not require west <= east, as this condition is not specified in ISO 19115.
        // Some implementations may use west > east for box spanning the anti-meridian.
    }

    /**
     * Validates the vertical extent.
     *
     * @param  object  the object to validate, or {@code null}.
     */
    public void validate(final VerticalExtent object) {
        if (object == null) {
            return;
        }
        final Double minimum = object.getMinimumValue();
        final Double maximum = object.getMaximumValue();
        mandatory("VerticalExtent: must have a minimum value.", minimum);
        mandatory("VerticalExtent: must have a maximum value.", maximum);
        if (minimum != null && maximum != null) {
            assertTrue("VerticalExtent: invalid range.", minimum <= maximum);
        }
        container.validate(object.getVerticalCRS());
    }

    /**
     * Validates the temporal extent.
     *
     * @param  object  the object to validate, or {@code null}.
     *
     * @todo Validation of temporal primitives not yet implemented.
     */
    public void validate(final TemporalExtent object) {
        if (object == null) {
            return;
        }
        if (object instanceof SpatialTemporalExtent) {
            for (final GeographicExtent e : toArray(GeographicExtent.class, ((SpatialTemporalExtent) object).getSpatialExtent())) {
                dispatch(e);
            }
        }
    }

    /**
     * Validates the given extent.
     *
     * @param  object  the object to validate, or {@code null}.
     */
    public void validate(final Extent object) {
        if (object == null) {
            return;
        }
        validateOptional(object.getDescription());
        for (GeographicExtent e : toArray(GeographicExtent.class, object.getGeographicElements())) dispatch(e);
        for (VerticalExtent   e : toArray(VerticalExtent  .class, object.getVerticalElements  ())) validate(e);
        for (TemporalExtent   e : toArray(TemporalExtent  .class, object.getTemporalElements  ())) validate(e);
    }
}
