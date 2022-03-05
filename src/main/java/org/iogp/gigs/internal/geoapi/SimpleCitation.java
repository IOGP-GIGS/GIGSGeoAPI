/*
 *    GeoAPI - Java interfaces for OGC/ISO standards
 *    http://www.geoapi.org
 *
 *    Copyright (C) 2011-2021 Open Geospatial Consortium, Inc.
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
import java.util.Collection;
import java.util.Collections;
import org.opengis.metadata.Identifier;
import org.opengis.metadata.citation.Citation;
import org.opengis.metadata.citation.CitationDate;
import org.opengis.metadata.citation.PresentationForm;
import org.opengis.metadata.citation.ResponsibleParty;
import org.opengis.metadata.citation.Series;
import org.opengis.util.InternationalString;



/**
 * A simple {@link Citation} implementation for testing purpose only.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @version 1.0
 * @since   1.0
 */
final class SimpleCitation implements Citation {
    /**
     * The citation title to be returned by {@link #getTitle()}.
     */
    private final InternationalString title;

    /**
     * Creates a new citation having the given title.
     *
     * @param title  the citation title to be returned by {@link #getTitle()}.
     */
    SimpleCitation(final InternationalString title) {
        this.title = title;
    }

    /**
     * Returns the title specified at construction time.
     * This is the only {@link Citation} mandatory property.
     */
    @Override
    public InternationalString getTitle() {
        return title;
    }

    @Override
    public Collection<? extends InternationalString> getAlternateTitles() {
        return Collections.emptyList();
    }

    @Override
    public Collection<? extends CitationDate> getDates() {
        return Collections.emptyList();
    }

    @Override
    public InternationalString getEdition() {
        return null;
    }

    @Override
    public Date getEditionDate() {
        return null;
    }

    @Override
    public Collection<? extends Identifier> getIdentifiers() {
        return Collections.emptyList();
    }

    @Override
    public Collection<? extends ResponsibleParty> getCitedResponsibleParties() {
        return Collections.emptyList();
    }

    @Override
    public Collection<PresentationForm> getPresentationForms() {
        return Collections.emptyList();
    }

    @Override
    public Series getSeries() {
        return null;
    }

    @Override
    public InternationalString getOtherCitationDetails() {
        return null;
    }

    @Override
    public InternationalString getCollectiveTitle() {
        return null;
    }

    @Override
    public String getISBN() {
        return null;
    }

    @Override
    public String getISSN() {
        return null;
    }
}
