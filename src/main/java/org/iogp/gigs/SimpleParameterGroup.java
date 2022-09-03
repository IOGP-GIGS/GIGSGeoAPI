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

import java.util.List;
import org.opengis.metadata.Identifier;
import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValue;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.parameter.ParameterNotFoundException;


/**
 * A {@code ParameterValueGroup} implementation for {@link SimpleParameter} instances.
 * In order to keep the class simpler, this parameter group is also its own descriptor.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @version 1.0
 * @since   1.0
 */
final class SimpleParameterGroup extends SimpleIdentifiedObject
        implements ParameterValueGroup, ParameterDescriptorGroup
{
    /**
     * The list of parameters included in this group.
     */
    private final List<SimpleParameter> parameters;

    /**
     * Creates a new parameter group of the given name.
     *
     * @param name   the parameter group name.
     * @param param  the parameters to be included in this group.
     */
    SimpleParameterGroup(final String name, final SimpleParameter... param) {
        super(name);
        parameters = List.of(param);
    }

    /**
     * Returns the descriptor of the parameter group. Since this simple class implements both the
     * {@linkplain ParameterValueGroup value} and {@linkplain ParameterDescriptorGroup descriptor}
     * interfaces, this method returns {@code this}.
     *
     * @return {@code this} descriptor.
     */
    @Override
    public ParameterDescriptorGroup getDescriptor() {
        return this;
    }

    @Override
    public int getMinimumOccurs() {
        return 1;
    }

    @Override
    public int getMaximumOccurs() {
        return 1;
    }

    /**
     * Returns the parameter descriptors in this group.
     * The list returned by this method is unmodifiable.
     *
     * <div class="note"><b>Implementation note:</b>
     * since the simple classes in this package implement both the {@linkplain GeneralParameterValue value}
     * and the {@linkplain GeneralParameterDescriptor descriptor} interfaces, this method returns the same list
     * than the {@link #values()} methods.</div>
     *
     * @return the parameter descriptors in this group as an unmodifiable list.
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<GeneralParameterDescriptor> descriptors() {
        return (List) parameters;                           // Cast is safe for unmodifiable list.
    }

    /**
     * Returns the parameter values in this group.
     * The list returned by this method is unmodifiable.
     *
     * <div class="note"><b>Implementation note:</b>
     * since the simple classes in this package implement both the {@linkplain GeneralParameterValue value}
     * and the {@linkplain GeneralParameterDescriptor descriptor} interfaces, this method returns the same list
     * than the {@link #descriptors()} methods.</div>
     *
     * @return the parameter values in this group as an unmodifiable list.
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<GeneralParameterValue> values() {
        return (List) parameters;                           // Cast is safe for unmodifiable list.
    }

    /**
     * Returns the parameter descriptor in this group for the specified
     * {@linkplain Identifier#getCode() identifier code}.
     *
     * <div class="note"><b>Implementation note:</b>
     * since the simple classes in this package implement both the {@linkplain GeneralParameterValue value}
     * and the {@linkplain GeneralParameterDescriptor descriptor} interfaces, this method is synonymous to
     * {@link #parameter(String)}.</div>
     *
     * @param  name  the case insensitive {@linkplain Identifier#getCode() identifier code} of the parameter to search for.
     * @return the parameter for the given identifier code.
     * @throws ParameterNotFoundException if there is no parameter for the given identifier code.
     */
    @Override
    public GeneralParameterDescriptor descriptor(final String name) throws ParameterNotFoundException {
        for (final SimpleParameter candidate : parameters) {
            if (name.equalsIgnoreCase(candidate.getName().getCode())) {
                return candidate;
            }
        }
        throw new ParameterNotFoundException("No such parameter: " + name, name);
    }

    /**
     * Returns the value in this group for the specified {@linkplain Identifier#getCode() identifier code}.
     *
     * @param  name  the case insensitive {@linkplain Identifier#getCode() identifier code} of the parameter to search for.
     * @return the parameter value for the given identifier code.
     * @throws ParameterNotFoundException if there is no parameter value for the given identifier code.
     */
    @Override
    public ParameterValue<?> parameter(final String name) throws ParameterNotFoundException {
        for (final SimpleParameter candidate : parameters) {
            if (name.equalsIgnoreCase(candidate.getName().getCode())) {
                return candidate;
            }
        }
        throw new ParameterNotFoundException("No such parameter: " + name, name);
    }

    /**
     * Throws an exception, since this simple parameter group does not support subgroups.
     */
    @Override
    public List<ParameterValueGroup> groups(final String name) throws ParameterNotFoundException {
        throw new ParameterNotFoundException("No such parameter group: " + name, name);
    }

    /**
     * Throws an exception, since this simple parameter group does not support subgroups.
     */
    @Override
    public ParameterValueGroup addGroup(String name) throws ParameterNotFoundException {
        throw new ParameterNotFoundException("No such parameter group: " + name, name);
    }

    /**
     * Unsupported operation because this parameter implementation is immutable.
     */
    @Override
    public SimpleParameterGroup createValue() {
        throw new UnsupportedOperationException(SimpleParameter.IMMUTABLE);
    }

    /**
     * Not needed because this parameter implementation is immutable.
     */
    @Override
    @SuppressWarnings("CloneDoesntCallSuperClone")
    public ParameterValueGroup clone() {
        return this;
    }

    /**
     * Compares the given object with this parameter group for equality.
     */
    @Override
    public boolean equals(final Object object) {
        if (object instanceof SimpleParameterGroup) {
            final SimpleParameterGroup other = (SimpleParameterGroup) object;
            return name.equals(other.name) && parameters.equals(other.parameters);
        }
        return false;
    }

    /**
     * Returns a hash code value for this parameter group.
     */
    @Override
    public int hashCode() {
        return ~name.hashCode() + 37*parameters.hashCode();
    }
}
