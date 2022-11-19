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

import java.util.Date;
import org.opengis.metadata.*;
import org.opengis.metadata.citation.*;
import org.opengis.util.InternationalString;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Validates {@link Citation} and related objects from the
 * {@code org.opengis.metadata.citation} package.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @version 1.0
 * @since   1.0
 */
public class CitationValidator extends MetadataValidator {
    /**
     * Creates a new validator instance.
     *
     * @param container  the set of validators to use for validating other kinds of objects
     *                   (see {@linkplain #container field javadoc}).
     */
    public CitationValidator(final ValidatorContainer container) {
        super(container, "org.opengis.metadata.citation");
    }

    /**
     * Validates the given citation.
     *
     * @param  object  the object to validate, or {@code null}.
     */
    public void validate(final Citation object) {
        if (object == null) {
            return;
        }
        validateMandatory(object.getTitle());
        validateOptional (object.getEdition());
        for (final InternationalString e : toArray(InternationalString.class, object.getAlternateTitles())) {
            container.validate(e);
        }
        for (final Identifier e : toArray(Identifier.class, object.getIdentifiers())) {
            container.validate(e);
        }
        validate(toArray(CitationDate.class, object.getDates()));
        for (final ResponsibleParty e : toArray(ResponsibleParty.class, object.getCitedResponsibleParties())) {
            validate(e);
        }
    }

    /**
     * Validates citation dates. If more than one dates is given, then this method will check
     * for the following constraints:
     *
     * <ul>
     *   <li>{@link DateType#CREATION} shall be before or equals to all other type of dates, ignoring user-defined codes.</li>
     *   <li>{@link DateType#LAST_UPDATE} shall be before or equals to {@link DateType#NEXT_UPDATE}.</li>
     *   <li>{@link DateType#VALIDITY_BEGINS} shall be before or equals to {@link DateType#VALIDITY_EXPIRES}.</li>
     * </ul>
     *
     * Those constraints are verified in their iteration order. It is possible for example to have more than one
     * (<var>validity begins</var>, <var>validity expires</var>) pair.
     *
     * @param dates  the citation dates to validate.
     */
    public void validate(final CitationDate... dates) {
        if (dates == null) {
            return;
        }
        Date creation = null;
        final int lastOrdinal = DateType.REVISION.ordinal();
        for (final CitationDate date : dates) {
            if (date != null) {
                final DateType type = date.getDateType();
                final Date     time = date.getDate();
                mandatory("CitationDate: shall have a date type.", type);
                mandatory("CitationDate: shall have a timestamp.", time);
                if (type != null && time != null) {
                    if (type.equals(DateType.CREATION)) {
                        creation = time;
                    }
                    if (type.ordinal() <= lastOrdinal) {
                        assertOrdered(DateType.CREATION, creation, type, time);
                    }
                }
            }
        }
    }

    /**
     * Asserts that the date {@code d2} is equal or after {@code d1}.
     *
     * @param  t1  type of the first date.
     * @param  d1  the first date to compare.
     * @param  t2  type of the second date.
     * @param  d2  the second data to compare.
     */
    private static void assertOrdered(final DateType t1, final Date d1, final DateType t2, final Date d2) {
        if (d1 != null && d2.before(d1)) {
            fail("The ‘" + t2.identifier() + "’ date (" + d2 + ") shall be equal or after "
               + "the ‘" + t1.identifier() + "’ date (" + d1 + ").");
        }
    }

    /**
     * Validates the given responsible party.
     *
     * @param  object  the object to validate, or {@code null}.
     */
    public void validate(final ResponsibleParty object) {
        if (object == null) {
            return;
        }
        mandatory("ResponsibleParty: shall have a role.", object.getRole());
    }

    /**
     * Validates the given contact information.
     *
     * @param  object  the object to validate, or {@code null}.
     */
    public void validate(final Contact object) {
        if (object == null) {
            return;
        }
        validateOptional(object.getContactInstructions());
    }

    /**
     * Validates the given telephone information.
     *
     * @param  object  the object to validate, or {@code null}.
     */
    public void validate(final Telephone object) {
    }

    /**
     * Validates the given address.
     *
     * @param  object  the object to validate, or {@code null}.
     */
    public void validate(final Address object) {
        if (object == null) {
            return;
        }
        validate(object.getDeliveryPoints());
        validateOptional(object.getCity());
        validateOptional(object.getAdministrativeArea());
        validateOptional(object.getCountry());
        validate(object.getElectronicMailAddresses());
    }

    /**
     * Validates the given online resource.
     *
     * @param  object  the object to validate, or {@code null}.
     */
    public void validate(final OnlineResource object) {
        if (object == null) {
            return;
        }
        mandatory("OnlineResource: shall have a linkage.", object.getLinkage());
        validateOptional(object.getDescription());
    }
}
