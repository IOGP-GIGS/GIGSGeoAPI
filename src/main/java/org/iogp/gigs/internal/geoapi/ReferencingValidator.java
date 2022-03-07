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

import java.util.Collection;

import org.opengis.referencing.*;
import org.opengis.referencing.cs.*;
import org.opengis.referencing.crs.*;
import org.opengis.referencing.datum.*;
import org.opengis.referencing.operation.*;
import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.metadata.Identifier;
import org.opengis.util.GenericName;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Base class for validators of {@link IdentifiedObject} and related objects from the
 * {@code org.opengis.referencing} package.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @version 1.0
 * @since   1.0
 */
public abstract class ReferencingValidator extends Validator {
    /**
     * Provider of units of measurement (degree, metre, second, <i>etc</i>).
     * This field is set to the {@link Units#getInstance() default provider} for now
     * (it may be revisited in a future GeoAPI-conformance version).
     */
    final Units units = Units.getInstance();

    /**
     * Creates a new validator instance.
     *
     * @param container    the set of validators to use for validating other kinds of objects
     *                     (see {@linkplain #container field javadoc}).
     * @param packageName  the name of the package containing the classes to be validated.
     */
    protected ReferencingValidator(final ValidatorContainer container, final String packageName) {
        super(container, packageName);
    }

    /**
     * For each interface implemented by the given object, invokes the corresponding
     * {@code validate(…)} method defined in this package (if any).
     *
     * @param  object  the object to dispatch to {@code validate(…)} methods, or {@code null}.
     */
    public final void dispatchObject(final IdentifiedObject object) {
        int n = 0;
        if (object != null) {
            if (object instanceof CoordinateReferenceSystem)  {container.validate((CoordinateReferenceSystem)  object); n++;}
            if (object instanceof CoordinateSystem)           {container.validate((CoordinateSystem)           object); n++;}
            if (object instanceof CoordinateSystemAxis)       {container.validate((CoordinateSystemAxis)       object); n++;}
            if (object instanceof Datum)                      {container.validate((Datum)                      object); n++;}
            if (object instanceof Ellipsoid)                  {container.validate((Ellipsoid)                  object); n++;}
            if (object instanceof PrimeMeridian)              {container.validate((PrimeMeridian)              object); n++;}
            if (object instanceof GeneralParameterDescriptor) {container.validate((GeneralParameterDescriptor) object); n++;}
            if (object instanceof CoordinateOperation)        {container.validate((CoordinateOperation)        object); n++;}
            if (object instanceof OperationMethod)            {container.validate((OperationMethod)            object); n++;}
            if (n == 0) {
                if (object instanceof ReferenceSystem) {
                    validateReferenceSystem((ReferenceSystem) object);
                } else {
                    validateIdentifiedObject(object);
                }
            }
        }
    }

    /**
     * Ensures that the given identifier has a {@linkplain Identifier#getCode() code}.
     *
     * @param  object  the object to validate, or {@code null}.
     */
    public void validate(final Identifier object) {
        container.validate(object);
    }

    /**
     * Performs the validation that are common to all reference systems. This method is
     * invoked by {@code validate} methods after they have determined the type of their
     * argument.
     *
     * @param  object  the object to validate (can not be null).
     */
    final void validateReferenceSystem(final ReferenceSystem object) {
        validateIdentifiedObject(object);
        container.validate(object.getScope());
        container.validate(object.getDomainOfValidity());
    }

    /**
     * Performs the validation that are common to all identified objects. This method is
     * invoked by {@code validate} methods after they have determined the type of their
     * argument.
     *
     * @param  object  the object to validate (can not be null).
     */
    final void validateIdentifiedObject(final IdentifiedObject object) {
        validate(object.getName());
        final Collection<? extends Identifier> identifiers = object.getIdentifiers();
        if (identifiers != null) {
            validate(identifiers);
            for (final Identifier id : identifiers) {
                assertNotNull(id, "IdentifiedObject: getIdentifiers() can not contain null element.");
                validate(id);
            }
        }
        final Collection<? extends GenericName> alias = object.getAlias();
        if (alias != null) {
            validate(alias);
            for (final GenericName name : alias) {
                assertNotNull(alias, "IdentifiedObject: getAlias() can not contain null element.");
                container.validate(name);
            }
        }
        container.validate(object.getRemarks());
    }
}
