/*
 *    GeoAPI - Java interfaces for OGC/ISO standards
 *    http://www.geoapi.org
 *
 *    Copyright (C) 2011-2022 Open Geospatial Consortium, Inc.
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
package org.iogp.gigs.runner;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.net.URI;
import java.util.Set;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.StringTokenizer;
import java.util.Iterator;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReference;
import java.lang.module.ModuleDescriptor;


/**
 * Information about a GeoAPI implementation, as found in the JAR manifest.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @version 1.0
 * @since   1.0
 */
final class ImplementationManifest {
    /**
     * Name of the GeoAPI module to look for in the "request" statements.
     */
    private static final String GEOAPI_MODULE = "org.opengis.geoapi";

    /**
     * The implementation title, version, or vendor.
     * Any of those attributes except the title may be null.
     */
    final String title, version, vendor, specification, specVersion, specVendor;

    /**
     * The set of dependencies built from the class path, or {@code null}.
     */
    Path[] dependencies;

    /**
     * Creates a new manifest for the given attributes.
     *
     * @param title       the implementation title.
     * @param attributes  attributes from which to get version, vendor, etc.
     */
    private ImplementationManifest(final String title, final Attributes attributes) {
        this.title    = title;
        version       = (String) attributes.get(Attributes.Name.IMPLEMENTATION_VERSION);
        vendor        = (String) attributes.get(Attributes.Name.IMPLEMENTATION_VENDOR);
        specification = (String) attributes.get(Attributes.Name.SPECIFICATION_TITLE);
        specVersion   = (String) attributes.get(Attributes.Name.SPECIFICATION_VERSION);
        specVendor    = (String) attributes.get(Attributes.Name.SPECIFICATION_VENDOR);
    }

    /**
     * Parses the manifest entry of the given JAR files for information about GeoAPI implementation.
     *
     * @param  modules      contains the set of modules to examine.
     * @return information about the implementation, or {@code null} if none.
     * @throws IOException if an I/O error occurred when reading a file.
     */
    static ImplementationManifest parse(final ModuleFinder modules) throws IOException {
        final Set<File> classpath = new LinkedHashSet<>();
        ImplementationManifest manifest = null;
        for (ModuleReference ref : modules.findAll()) {
            final URI location = ref.location().orElse(null);
            if (location != null) {
                boolean isImpl = false;
                if (manifest == null) {
                    for (ModuleDescriptor.Requires req : ref.descriptor().requires()) {
                        isImpl = req.name().equals(GEOAPI_MODULE);
                        if (isImpl) break;
                    }
                }
                final ImplementationManifest candidate = parse(new File(location), classpath, isImpl);
                if (candidate != null) {
                    manifest = candidate;
                }
            }
        }
        /*
         * Removes any path element that duplicates a JAR file already on the module path,
         * and stores the remaining classpath entries in the ImplementationManifest object.
         */
        final String defcp = System.getProperty("jdk.module.path");
        if (defcp != null) {
            final Set<String> currentClasspath = new HashSet<>();
            final StringTokenizer tokens = new StringTokenizer(defcp, File.pathSeparator);
            while (tokens.hasMoreTokens()) {
                String file = tokens.nextToken();
                file = file.substring(file.lastIndexOf(File.separator) + 1);
                currentClasspath.add(file);
            }
            for (final Iterator<File> it = classpath.iterator(); it.hasNext();) {
                if (currentClasspath.contains(it.next().getName())) {
                    it.remove();
                }
            }
        }
        if (manifest != null) {
            manifest.dependencies = classpath.stream().map(File::toPath).toArray(Path[]::new);
        }
        return manifest;
    }

    /**
     * Parses the manifest entry of the given JAR file for information about GeoAPI implementation.
     * If such information is found, then this method returns the information in an
     * {@code ImplementationManifest} object. Otherwise this method returns {@code null}.
     *
     * @param  file       the JAR file to parse.
     * @param  classpath  a set in which to add classpath information.
     * @param  isImpl     whether the dependency is a GeoAPI implementation.
     * @return information about the implementation, or {@code null} if none.
     * @throws IOException if an error occurred while reading the JAR file.
     */
    private static ImplementationManifest parse(final File file, final Set<File> classpath, final boolean isImpl) throws IOException {
        ImplementationManifest impl = null;
        classpath.add(file.getAbsoluteFile());
        try (final JarFile jar = new JarFile(file, false)) {
            final Manifest manifest = jar.getManifest();
            if (manifest != null) {
                final Attributes attributes = manifest.getMainAttributes();
                if (attributes != null) {
                    if (isImpl) {
                        final String title = (String) attributes.get(Attributes.Name.IMPLEMENTATION_TITLE);
                        if (title != null) {
                            impl = new ImplementationManifest(title, attributes);
                        }
                    }
                    final String cp = (String) attributes.get(Attributes.Name.CLASS_PATH);
                    if (cp != null) {
                        final File directory = file.getParentFile();
                        final StringTokenizer tokens = new StringTokenizer(cp);
                        while (tokens.hasMoreTokens()) {
                            classpath.add(new File(directory, tokens.nextToken()).getAbsoluteFile());
                        }
                    }
                }
            }
        }
        return impl;
    }

    /**
     * Returns a string representation of the information provided in the manifest.
     */
    @Override
    public String toString() {
        final String lineSeparator = System.lineSeparator();
        final StringBuilder buffer = new StringBuilder();
        if (title   != null) buffer.append("Title:   ").append(title  ).append(lineSeparator);
        if (version != null) buffer.append("Version: ").append(version).append(lineSeparator);
        if (vendor  != null) buffer.append("Vendor:  ").append(vendor ).append(lineSeparator);
        return buffer.toString();
    }
}
