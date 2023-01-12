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

import java.util.List;
import java.util.AbstractList;

import org.opengis.util.*;
import org.opengis.metadata.*;
import org.opengis.metadata.extent.*;
import org.opengis.metadata.citation.*;
import org.opengis.geometry.*;
import org.opengis.parameter.*;
import org.opengis.referencing.*;
import org.opengis.referencing.cs.*;
import org.opengis.referencing.crs.*;
import org.opengis.referencing.datum.*;
import org.opengis.referencing.operation.*;


/**
 * A set of convenience methods for validating GeoAPI implementations. Every {@code validate}
 * method defined in this class delegate their work to one of many {@code Validator} objects
 * in various packages. Vendors can change the value of fields in this class if they wish to
 * override some validation process.
 *
 * <h2>Customization</h2>
 * All {@code validate(…)} methods in this class are final because this class is not the extension
 * point for overriding validation processes. Instead, extend the appropriate {@link Validator}
 * subclass and assign an instance to the corresponding field in this class. For example in order
 * to override the validation of {@link org.opengis.referencing.crs.GeographicCRS} objects, one
 * can do:
 *
 * {@snippet lang="java" :
 *     ValidatorContainer container = new ValidationContainer();
 *     container.crs = new CRSValidator(container) {
 *         @Override
 *         public void validate(GeographicCRS object) {
 *             super.validate(object);
 *             // Perform additional validation here.
 *         }
 *     }
 * }
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @version 1.0
 * @since   1.0
 */
public class ValidatorContainer implements Cloneable {
    /**
     * The default container to be used by all static {@code validate} methods.
     * Vendors can change the validators referenced by this container, or change
     * their setting.
     *
     * <p>This field is not final in order to allow vendors to switch easily between
     * different configurations, for example:</p>
     *
     * {@snippet lang="java" :
     *     ValidatorContainer original = ValidatorContainer.DEFAULT;
     *     ValidatorContainer.DEFAULT = myConfig;
     *     // ... do some tests ...
     *     ValidatorContainer.DEFAULT = original;
     * }
     */
    public static ValidatorContainer DEFAULT = new ValidatorContainer();

    /**
     * The validator for {@link GenericName} and related objects.
     * Vendors can change this field to a different validator, or change the setting
     * of the referenced validator. This field shall not be set to {@code null} however.
     */
    public NameValidator naming = new NameValidator(this);

    /**
     * The validator for {@link Metadata} and related objects.
     * Vendors can change this field to a different validator, or change the setting
     * of the referenced validator. This field shall not be set to {@code null} however.
     */
    public MetadataBaseValidator metadata = new MetadataBaseValidator(this);

    /**
     * The validator for {@link Citation} and related objects.
     * Vendors can change this field to a different validator, or change the setting
     * of the referenced validator. This field shall not be set to {@code null} however.
     */
    public CitationValidator citation = new CitationValidator(this);

    /**
     * The validator for {@link Extent} and related objects.
     * Vendors can change this field to a different validator, or change the setting
     * of the referenced validator. This field shall not be set to {@code null} however.
     */
    public ExtentValidator extent = new ExtentValidator(this);

    /**
     * The validator for {@link Datum} and related objects.
     * Vendors can change this field to a different validator, or change the setting
     * of the referenced validator. This field shall not be set to {@code null} however.
     */
    public DatumValidator datum = new DatumValidator(this);

    /**
     * The validator for {@link CoordinateSystem} and related objects.
     * Vendors can change this field to a different validator, or change the setting
     * of the referenced validator. This field shall not be set to {@code null} however.
     */
    public CSValidator cs = new CSValidator(this);

    /**
     * The validator for {@link CoordinateReferenceSystem} and related objects.
     * Vendors can change this field to a different validator, or change the setting
     * of the referenced validator. This field shall not be set to {@code null} however.
     */
    public CRSValidator crs = new CRSValidator(this);

    /**
     * The validator for {@link ParameterValue} and related objects.
     * Vendors can change this field to a different validator, or change the setting
     * of the referenced validator. This field shall not be set to {@code null} however.
     */
    public ParameterValidator parameter = new ParameterValidator(this);

    /**
     * The validator for {@link CoordinateOperation} and related objects.
     * Vendors can change this field to a different validator, or change the setting
     * of the referenced validator. This field shall not be set to {@code null} however.
     */
    public OperationValidator coordinateOperation = new OperationValidator(this);

    /**
     * The validator for {@link Geometry} and related objects.
     * Vendors can change this field to a different validator, or change the setting
     * of the referenced validator. This field shall not be set to {@code null} however.
     */
    public GeometryValidator geometry = new GeometryValidator(this);

    /**
     * An unmodifiable "live" list of all validators. Any change to the value of a field declared
     * in this class is reflected immediately in this list (so this list is <cite>unmodifiable</cite>
     * but not <cite>immutable</cite>). This list is convenient if the same setting must be applied
     * on all validators, for example in order to change their {@link Validator#logger logger} setting
     * or to set their set {@link Validator#requireMandatoryAttributes requireMandatoryAttributes}
     * field to {@code false}.
     */
    public final List<Validator> all = new AbstractList<>() {
        /** Returns the number of elements in this list. */
        @Override public int size() {
            return 11;
        }

        /** Returns the validator at the given index. */
        @Override public Validator get(final int index) {
            switch (index) {
                case  0: return naming;
                case  1: return metadata;
                case  2: return citation;
                case  3: return extent;
                case  4: return datum;
                case  5: return cs;
                case  6: return crs;
                case  7: return parameter;
                case  8: return coordinateOperation;
                case  9: return geometry;
                default: throw new IndexOutOfBoundsException(String.valueOf(index));
            }
        }
    };

    /**
     * Creates a new {@code ValidatorContainer} initialized with new {@link Validator} instances.
     * Note that this constructor does not inherit the configuration of the {@link ValidatorContainer#DEFAULT}
     * instance. To inherit that default configuration, use <code>DEFAULT.{@linkplain #clone()}</code>
     * instead.
     */
    public ValidatorContainer() {
    }

    /**
     * Returns a new container using the same validators than this instance. After this method call,
     * the two {@code ValidatorContainer} instances will share the same {@link Validator} instances.
     *
     * <p>This method is typically used in order to use the default configuration with a few
     * changes, as in the example below:</p>
     *
     * {@snippet lang="java" :
     *     ValidatorContainer myContainer = ValidatorContainer.DEFAULT.clone();
     *     myContainer.crs = new CRSValidator();
     *     myContainer.crs.CRSValidator.enforceStandardNames = false;
     * }
     *
     * @return a new {@code ValidatorContainer} instance using the same {@link Validator} instances.
     */
    @Override
    public ValidatorContainer clone() {
        try {
            return (ValidatorContainer) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);                    // Should never happen.
        }
    }

    /**
     * For each interface implemented by the given object, invokes the corresponding
     * {@code validate(…)} method defined in this class (if any).
     * Use this method only if the type is unknown at compile-time.
     *
     * @param  object The object to dispatch to {@code validate(…)} methods, or {@code null}.
     */
    public final void dispatch(final Object object) {
        if (object instanceof Metadata)              validate((Metadata)              object);
        if (object instanceof Citation)              validate((Citation)              object);
        if (object instanceof CitationDate)          validate((CitationDate)          object);
        if (object instanceof CitationDate[])        validate((CitationDate[])        object);
        if (object instanceof Contact)               validate((Contact)               object);
        if (object instanceof Telephone)             validate((Telephone)             object);
        if (object instanceof Address)               validate((Address)               object);
        if (object instanceof OnlineResource)        validate((OnlineResource)        object);
        if (object instanceof Extent)                validate((Extent)                object);
        if (object instanceof GeographicExtent)      validate((GeographicExtent)      object);
        if (object instanceof VerticalExtent)        validate((VerticalExtent)        object);
        if (object instanceof TemporalExtent)        validate((TemporalExtent)        object);
        if (object instanceof IdentifiedObject)      validate((IdentifiedObject)      object);
        if (object instanceof Identifier)            validate((Identifier)            object);
        if (object instanceof GenericName)           validate((GenericName)           object);
        if (object instanceof NameSpace)             validate((NameSpace)             object);
        if (object instanceof GeneralParameterValue) validate((GeneralParameterValue) object);
        if (object instanceof Envelope)              validate((Envelope)              object);
        if (object instanceof DirectPosition)        validate((DirectPosition)        object);
        if (object instanceof InternationalString)   validate((InternationalString)   object);
    }

    /**
     * Tests the conformance of the given object.
     *
     * @param  object  the object to test, or {@code null}.
     *
     * @see MetadataBaseValidator#validate(Metadata)
     */
    public final void validate(final Metadata object) {
        metadata.validate(object);
    }

    /**
     * Tests the conformance of the given object.
     *
     * @param  object  the object to test, or {@code null}.
     *
     * @see CitationValidator#validate(Citation)
     */
    public final void validate(final Citation object) {
        citation.validate(object);
    }

    /**
     * Tests the conformance of the given objects.
     *
     * @param  object  the objects to test, or {@code null}.
     *
     * @see CitationValidator#validate(CitationDate...)
     */
    public final void validate(final CitationDate... object) {
        citation.validate(object);
    }

    /**
     * Tests the conformance of the given object.
     *
     * @param  object  the object to test, or {@code null}.
     *
     * @see CitationValidator#validate(ResponsibleParty)
     */
    public final void validate(final ResponsibleParty object) {
        citation.validate(object);
    }

    /**
     * Tests the conformance of the given object.
     *
     * @param  object  the object to test, or {@code null}.
     *
     * @see CitationValidator#validate(Contact)
     */
    public final void validate(final Contact object) {
        citation.validate(object);
    }

    /**
     * Tests the conformance of the given object.
     *
     * @param  object  the object to test, or {@code null}.
     *
     * @see CitationValidator#validate(Telephone)
     */
    public final void validate(final Telephone object) {
        citation.validate(object);
    }

    /**
     * Tests the conformance of the given object.
     *
     * @param  object  the object to test, or {@code null}.
     *
     * @see CitationValidator#validate(Address)
     */
    public final void validate(final Address object) {
        citation.validate(object);
    }

    /**
     * Tests the conformance of the given object.
     *
     * @param  object  the object to test, or {@code null}.
     *
     * @see CitationValidator#validate(OnlineResource)
     */
    public final void validate(final OnlineResource object) {
        citation.validate(object);
    }

    /**
     * Tests the conformance of the given object.
     *
     * @param  object  the object to test, or {@code null}.
     *
     * @see ExtentValidator#validate(Extent)
     */
    public final void validate(final Extent object) {
        extent.validate(object);
    }

    /**
     * Tests the conformance of the given object.
     *
     * @param  object  the object to test, or {@code null}.
     *
     * @see ExtentValidator#validate(TemporalExtent)
     */
    public final void validate(final TemporalExtent object) {
        extent.validate(object);
    }

    /**
     * Tests the conformance of the given object.
     *
     * @param  object  the object to test, or {@code null}.
     *
     * @see ExtentValidator#validate(VerticalExtent)
     */
    public final void validate(final VerticalExtent object) {
        extent.validate(object);
    }

    /**
     * Tests the conformance of the given object.
     *
     * @param  object  the object to test, or {@code null}.
     *
     * @see ExtentValidator#dispatch(GeographicExtent)
     */
    public final void validate(final GeographicExtent object) {
        extent.dispatch(object);
    }

    /**
     * Tests the conformance of the given object.
     *
     * @param  object  the object to test, or {@code null}.
     *
     * @see ExtentValidator#validate(GeographicDescription)
     */
    public final void validate(final GeographicDescription object) {
        extent.validate(object);
    }

    /**
     * Tests the conformance of the given object.
     *
     * @param  object  the object to test, or {@code null}.
     *
     * @see ExtentValidator#validate(BoundingPolygon)
     */
    public final void validate(final BoundingPolygon object) {
        extent.validate(object);
    }

    /**
     * Tests the conformance of the given object.
     *
     * @param  object  the object to test, or {@code null}.
     *
     * @see ExtentValidator#validate(GeographicBoundingBox)
     */
    public final void validate(final GeographicBoundingBox object) {
        extent.validate(object);
    }

    /**
     * Tests the conformance of the given object.
     *
     * @param  object  the object to test, or {@code null}.
     *
     * @see GeometryValidator#validate(Envelope)
     */
    public final void validate(final Envelope object) {
        geometry.validate(object);
    }

    /**
     * Tests the conformance of the given object.
     *
     * @param  object  the object to test, or {@code null}.
     *
     * @see GeometryValidator#validate(DirectPosition)
     */
    public final void validate(final DirectPosition object) {
        geometry.validate(object);
    }

    /**
     * Tests the conformance of the given object.
     *
     * @param  object  the object to test, or {@code null}.
     *
     * @see CRSValidator#dispatch(CoordinateReferenceSystem)
     */
    public final void validate(final CoordinateReferenceSystem object) {
        crs.dispatch(object);
    }

    /**
     * Tests the conformance of the given object.
     *
     * @param  object  the object to test, or {@code null}.
     *
     * @see CRSValidator#validate(GeocentricCRS)
     */
    public final void validate(final GeocentricCRS object) {
        crs.validate(object);
    }

    /**
     * Tests the conformance of the given object.
     *
     * @param  object  the object to test, or {@code null}.
     *
     * @see CRSValidator#validate(GeographicCRS)
     */
    public final void validate(final GeographicCRS object) {
        crs.validate(object);
    }

    /**
     * Validates the given coordinate reference system.
     *
     * @param  object  the object to validate, or {@code null}.
     *
     * @see CRSValidator#validate(ProjectedCRS)
     */
    public final void validate(final ProjectedCRS object) {
        crs.validate(object);
    }

    /**
     * Validates the given coordinate reference system.
     *
     * @param  object  the object to validate, or {@code null}.
     *
     * @see CRSValidator#validate(DerivedCRS)
     */
    public final void validate(final DerivedCRS object) {
        crs.validate(object);
    }

    /**
     * Validates the given coordinate reference system.
     *
     * @param  object  the object to validate, or {@code null}.
     *
     * @see CRSValidator#validate(ImageCRS)
     */
    public final void validate(final ImageCRS object) {
        crs.validate(object);
    }

    /**
     * Validates the given coordinate reference system.
     *
     * @param  object  the object to validate, or {@code null}.
     *
     * @see CRSValidator#validate(EngineeringCRS)
     */
    public final void validate(final EngineeringCRS object) {
        crs.validate(object);
    }

    /**
     * Validates the given coordinate reference system.
     *
     * @param  object  the object to validate, or {@code null}.
     *
     * @see CRSValidator#validate(VerticalCRS)
     */
    public final void validate(final VerticalCRS object) {
        crs.validate(object);
    }

    /**
     * Validates the given coordinate reference system.
     *
     * @param  object  the object to validate, or {@code null}.
     *
     * @see CRSValidator#validate(TemporalCRS)
     */
    public final void validate(final TemporalCRS object) {
        crs.validate(object);
    }

    /**
     * Validates the given coordinate reference system.
     *
     * @param  object  the object to validate, or {@code null}.
     *
     * @see CRSValidator#validate(CompoundCRS)
     */
    public final void validate(final CompoundCRS object) {
        crs.validate(object);
    }

    /**
     * Tests the conformance of the given object.
     *
     * @param  object  the object to test, or {@code null}.
     *
     * @see CSValidator#dispatch(CoordinateSystem)
     */
    public final void validate(final CoordinateSystem object) {
        cs.dispatch(object);
    }

    /**
     * Tests the conformance of the given object.
     *
     * @param  object  the object to test, or {@code null}.
     *
     * @see CSValidator#validate(CartesianCS)
     */
    public final void validate(final CartesianCS object) {
        cs.validate(object);
    }

    /**
     * Tests the conformance of the given object.
     *
     * @param  object  the object to test, or {@code null}.
     *
     * @see CSValidator#validate(EllipsoidalCS)
     */
    public final void validate(final EllipsoidalCS object) {
        cs.validate(object);
    }

    /**
     * Tests the conformance of the given object.
     *
     * @param  object  the object to test, or {@code null}.
     *
     * @see CSValidator#validate(SphericalCS)
     */
    public final void validate(final SphericalCS object) {
        cs.validate(object);
    }

    /**
     * Tests the conformance of the given object.
     *
     * @param  object  the object to test, or {@code null}.
     *
     * @see CSValidator#validate(CylindricalCS)
     */
    public final void validate(final CylindricalCS object) {
        cs.validate(object);
    }

    /**
     * Tests the conformance of the given object.
     *
     * @param  object  the object to test, or {@code null}.
     *
     * @see CSValidator#validate(PolarCS)
     */
    public final void validate(final PolarCS object) {
        cs.validate(object);
    }

    /**
     * Tests the conformance of the given object.
     *
     * @param  object  the object to test, or {@code null}.
     *
     * @see CSValidator#validate(LinearCS)
     */
    public final void validate(final LinearCS object) {
        cs.validate(object);
    }

    /**
     * Tests the conformance of the given object.
     *
     * @param  object  the object to test, or {@code null}.
     *
     * @see CSValidator#validate(VerticalCS)
     */
    public final void validate(final VerticalCS object) {
        cs.validate(object);
    }

    /**
     * Tests the conformance of the given object.
     *
     * @param  object  the object to test, or {@code null}.
     *
     * @see CSValidator#validate(TimeCS)
     */
    public final void validate(final TimeCS object) {
        cs.validate(object);
    }

    /**
     * Tests the conformance of the given object.
     *
     * @param  object  the object to test, or {@code null}.
     *
     * @see CSValidator#validate(UserDefinedCS)
     */
    public final void validate(final UserDefinedCS object) {
        cs.validate(object);
    }

    /**
     * Tests the conformance of the given object.
     *
     * @param  object  the object to test, or {@code null}.
     *
     * @see CSValidator#validate(CoordinateSystemAxis)
     */
    public final void validate(final CoordinateSystemAxis object) {
        cs.validate(object);
    }

    /**
     * Tests the conformance of the given object.
     *
     * @param  object  the object to test, or {@code null}.
     *
     * @see DatumValidator#dispatch(Datum)
     */
    public final void validate(final Datum object) {
        datum.dispatch(object);
    }

    /**
     * Tests the conformance of the given object.
     *
     * @param  object  the object to test, or {@code null}.
     *
     * @see DatumValidator#validate(PrimeMeridian)
     */
    public final void validate(final PrimeMeridian object) {
        datum.validate(object);
    }

    /**
     * Tests the conformance of the given object.
     *
     * @param  object  the object to test, or {@code null}.
     *
     * @see DatumValidator#validate(Ellipsoid)
     */
    public final void validate(final Ellipsoid object) {
        datum.validate(object);
    }

    /**
     * Tests the conformance of the given object.
     *
     * @param  object  the object to test, or {@code null}.
     *
     * @see DatumValidator#validate(GeodeticDatum)
     */
    public final void validate(final GeodeticDatum object) {
        datum.validate(object);
    }

    /**
     * Tests the conformance of the given object.
     *
     * @param  object  the object to test, or {@code null}.
     *
     * @see DatumValidator#validate(VerticalDatum)
     */
    public final void validate(final VerticalDatum object) {
        datum.validate(object);
    }

    /**
     * Tests the conformance of the given object.
     *
     * @param  object  the object to test, or {@code null}.
     *
     * @see DatumValidator#validate(TemporalDatum)
     */
    public final void validate(final TemporalDatum object) {
        datum.validate(object);
    }

    /**
     * Tests the conformance of the given object.
     *
     * @param  object  the object to test, or {@code null}.
     *
     * @see DatumValidator#validate(ImageDatum)
     */
    public final void validate(final ImageDatum object) {
        datum.validate(object);
    }

    /**
     * Tests the conformance of the given object.
     *
     * @param  object  the object to test, or {@code null}.
     *
     * @see DatumValidator#validate(EngineeringDatum)
     */
    public final void validate(final EngineeringDatum object) {
        datum.validate(object);
    }

    /**
     * Tests the conformance of the given object.
     *
     * @param  object  the object to test, or {@code null}.
     *
     * @see OperationValidator#dispatch(CoordinateOperation)
     */
    public final void validate(final CoordinateOperation object) {
        coordinateOperation.dispatch(object);
    }

    /**
     * Tests the conformance of the given object.
     *
     * @param  object  the object to test, or {@code null}.
     *
     * @see OperationValidator#validate(Conversion)
     */
    public final void validate(final Conversion object) {
        coordinateOperation.validate(object);
    }

    /**
     * Tests the conformance of the given object.
     *
     * @param  object  the object to test, or {@code null}.
     *
     * @see OperationValidator#validate(Transformation)
     */
    public final void validate(final Transformation object) {
        coordinateOperation.validate(object);
    }

    /**
     * Tests the conformance of the given object.
     *
     * @param  object  the object to test, or {@code null}.
     *
     * @see OperationValidator#validate(ConcatenatedOperation)
     */
    public final void validate(final ConcatenatedOperation object) {
        coordinateOperation.validate(object);
    }

    /**
     * Tests the conformance of the given object.
     *
     * @param  object  the object to test, or {@code null}.
     *
     * @see OperationValidator#validate(PassThroughOperation)
     */
    public final void validate(final PassThroughOperation object) {
        coordinateOperation.validate(object);
    }

    /**
     * Tests the conformance of the given object.
     *
     * @param  object  the object to test, or {@code null}.
     *
     * @see OperationValidator#validate(OperationMethod)
     */
    public final void validate(final OperationMethod object) {
        coordinateOperation.validate(object);
    }

    /**
     * Tests the conformance of the given object.
     *
     * @param  object  the object to test, or {@code null}.
     *
     * @see OperationValidator#validate(OperationMethod)
     */
    public final void validate(final Formula object) {
        coordinateOperation.validate(object);
    }

    /**
     * Tests the conformance of the given object.
     *
     * @param  object  the object to test, or {@code null}.
     *
     * @see OperationValidator#validate(MathTransform)
     */
    public final void validate(final MathTransform object) {
        coordinateOperation.validate(object);
    }

    /**
     * Tests the conformance of the given object.
     *
     * @param  object  the object to test, or {@code null}.
     *
     * @see ParameterValidator#dispatch(GeneralParameterDescriptor)
     */
    public final void validate(final GeneralParameterDescriptor object) {
        parameter.dispatch(object);
    }

    /**
     * Tests the conformance of the given object.
     *
     * @param  object  the object to test, or {@code null}.
     *
     * @see ParameterValidator#validate(ParameterDescriptor)
     */
    public final void validate(final ParameterDescriptor<?> object) {
        parameter.validate(object);
    }

    /**
     * Tests the conformance of the given object.
     *
     * @param  object  the object to test, or {@code null}.
     *
     * @see ParameterValidator#validate(ParameterDescriptorGroup)
     */
    public final void validate(final ParameterDescriptorGroup object) {
        parameter.validate(object);
    }

    /**
     * Tests the conformance of the given object.
     *
     * @param  object  the object to test, or {@code null}.
     *
     * @see ParameterValidator#dispatch(GeneralParameterValue)
     */
    public final void validate(final GeneralParameterValue object) {
        parameter.dispatch(object);
    }

    /**
     * Tests the conformance of the given object.
     *
     * @param  object  the object to test, or {@code null}.
     *
     * @see ParameterValidator#validate(ParameterValue)
     */
    public final void validate(final ParameterValue<?> object) {
        parameter.validate(object);
    }

    /**
     * Tests the conformance of the given object.
     *
     * @param  object  the object to test, or {@code null}.
     *
     * @see ParameterValidator#validate(ParameterValueGroup)
     */
    public final void validate(final ParameterValueGroup object) {
        parameter.validate(object);
    }

    /**
     * Tests the conformance of the given object.
     *
     * @param  object  the object to test, or {@code null}.
     *
     * @see ReferencingValidator#dispatchObject(IdentifiedObject)
     */
    public final void validate(final IdentifiedObject object) {
        crs.dispatchObject(object);
    }

    /**
     * Tests the conformance of the given object.
     *
     * @param  object  the object to test, or {@code null}.
     *
     * @see MetadataBaseValidator#validate(Identifier)
     */
    public final void validate(final Identifier object) {
        metadata.validate(object);
    }

    /**
     * Tests the conformance of the given object.
     *
     * @param  object  the object to test, or {@code null}.
     *
     * @see NameValidator#dispatch(GenericName)
     */
    public final void validate(final GenericName object) {
        naming.dispatch(object);
    }

    /**
     * Tests the conformance of the given object.
     *
     * @param  object  the object to test, or {@code null}.
     *
     * @see NameValidator#validate(LocalName)
     */
    public final void validate(final LocalName object) {
        naming.validate(object);
    }

    /**
     * Tests the conformance of the given object.
     *
     * @param  object  the object to test, or {@code null}.
     *
     * @see NameValidator#validate(ScopedName)
     */
    public final void validate(final ScopedName object) {
        naming.validate(object);
    }

    /**
     * Tests the conformance of the given object.
     *
     * @param  object  the object to test, or {@code null}.
     *
     * @see NameValidator#validate(NameSpace)
     */
    public final void validate(final NameSpace object) {
        naming.validate(object);
    }

    /**
     * Tests the conformance of the given object.
     *
     * @param  object  the object to test, or {@code null}.
     *
     * @see NameValidator#validate(InternationalString)
     */
    public final void validate(final InternationalString object) {
        naming.validate(object);
    }
}
