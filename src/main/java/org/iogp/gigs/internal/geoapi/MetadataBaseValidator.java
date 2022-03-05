/*
 *    GeoAPI - Java interfaces for OGC/ISO standards
 *    http://www.geoapi.org
 *
 *    Copyright (C) 2012-2021 Open Geospatial Consortium, Inc.
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

import java.util.Collection;
import org.opengis.metadata.*;
import org.opengis.metadata.citation.*;
import org.opengis.metadata.identification.*;


/**
 * Validates {@link Metadata} and related objects from the {@code org.opengis.metadata} package.
 * This validator is named {@code MetadataBaseValidator} for consistency with the {@code "mdb"}
 * namespace in XML schema. {@link Metadata} is usually the root of the metadata tree.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @version 1.0
 * @since   1.0
 */
public class MetadataBaseValidator extends MetadataValidator {
    /**
     * Creates a new validator instance.
     *
     * @param container  the set of validators to use for validating other kinds of objects
     *                   (see {@linkplain #container field javadoc}).
     */
    public MetadataBaseValidator(final ValidatorContainer container) {
        super(container, "org.opengis.metadata");
    }

    /**
     * Validates the given metadata.
     *
     * @param  object  the object to validate, or {@code null}.
     */
    public void validate(final Metadata object) {
        if (object == null) {
            return;
        }
        for (final ResponsibleParty e : toArray(ResponsibleParty.class, object.getContacts())) {
            container.validate(e);
        }
        validate(object.getSpatialRepresentationInfo());
        validate(object.getReferenceSystemInfo());
        validate(object.getMetadataExtensionInfo());

        final Collection<? extends Identification> identifications = object.getIdentificationInfo();
        mandatory("Metadata: shall have an identification information.",
                (identifications != null && identifications.isEmpty()) ? null : identifications);
        validate(identifications);

        validate(object.getContentInfo());
        validate(object.getDataQualityInfo());
        validate(object.getPortrayalCatalogueInfo());
        validate(object.getMetadataConstraints());
        validate(object.getApplicationSchemaInfo());
        validate(object.getAcquisitionInformation());
    }

    /**
     * Validates the given identifier.
     *
     * @param  object  the identifier to validate, or {@code null}.
     */
    public void validate(final Identifier object) {
        if (object == null) {
            return;
        }
        mandatory("Identifier: shall have a code.", object.getCode());
        final Citation citation = object.getAuthority();
        if (citation != this) { // Avoid never ending loop (TODO: find a better way).
            container.validate(citation);
        }
    }
}
