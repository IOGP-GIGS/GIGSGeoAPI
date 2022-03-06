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
package org.iogp.gigs.internal;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.MalformedURLException;
import java.util.ServiceLoader;
import java.util.function.Consumer;
import org.iogp.gigs.*;
import org.iogp.gigs.internal.geoapi.Units;
import org.opengis.referencing.cs.CSFactory;
import org.opengis.referencing.cs.CSAuthorityFactory;
import org.opengis.referencing.crs.CRSFactory;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.datum.DatumFactory;
import org.opengis.referencing.datum.DatumAuthorityFactory;
import org.opengis.referencing.operation.CoordinateOperationFactory;
import org.opengis.referencing.operation.CoordinateOperationAuthorityFactory;
import org.opengis.referencing.operation.MathTransformFactory;


/**
 * Pseudo test suite for every GIGS tests.
 * This {@code TestSuite} class provides some static fields for specifying explicitly which factories to use.
 *
 * <p>This is a temporary class, to be replaced after migration to JUnit 5.</p>
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @version 1.0
 * @since   1.0
 */
public final class TestSuite {
    private TestSuite() {
    }

    /**
     * The class loader to use for loading factories.
     */
    private static ClassLoader loader;

    /**
     * Specifies the JAR files containing the implementation to test, then run tests.
     *
     * @param  runner    the tests runner.
     * @param  jarFiles  JAR files of the implementation to test.
     * @throws MalformedURLException if a file can not be converted to a URK.
     */
    public static void run(final Consumer<Class<?>[]> runner, final File... jarFiles) throws MalformedURLException {
        final URL[] urls = new URL[jarFiles.length];
        for (int i=0; i < urls.length; i++) {
            urls[i] = jarFiles[i].toURI().toURL();
        }
        try {
            loader = new URLClassLoader(urls, TestSuite.class.getClassLoader());
            Units.setInstance(loader);
            csFactory             = factory(CSFactory.class);
            crsFactory            = factory(CRSFactory.class);
            datumFactory          = factory(DatumFactory.class);
            copFactory            = factory(CoordinateOperationFactory.class);
            csAuthorityFactory    = factory(CSAuthorityFactory.class);
            crsAuthorityFactory   = factory(CRSAuthorityFactory.class);
            datumAuthorityFactory = factory(DatumAuthorityFactory.class);
            copAuthorityFactory   = factory(CoordinateOperationAuthorityFactory.class);
            mtFactory             = factory(MathTransformFactory.class);
            runner.accept(new Class<?>[] {
                T2001.class, T2002.class, T2003.class, T2004.class, T2005.class, T2006.class, T2007.class, T2008.class, T2009.class,
                T3002.class, T3003.class, T3004.class, T3005.class
            });
        } finally {
            Units.setInstance(null);
            loader                = null;
            csFactory             = null;
            crsFactory            = null;
            datumFactory          = null;
            copFactory            = null;
            csAuthorityFactory    = null;
            crsAuthorityFactory   = null;
            datumAuthorityFactory = null;
            copAuthorityFactory   = null;
            mtFactory             = null;
        }
    }

    /**
     * Returns the factory of the given type.
     */
    private static <T> T factory(final Class<T> type) {
        for (final T factory : ServiceLoader.load(type, loader)) {
            // TODO: should we apply some filter?
            return factory;
        }
        return null;
    }

    private static CSFactory csFactory;

    private static CRSFactory crsFactory;

    private static DatumFactory datumFactory;

    private static CoordinateOperationFactory copFactory;

    private static CSAuthorityFactory csAuthorityFactory;

    private static CRSAuthorityFactory crsAuthorityFactory;

    private static DatumAuthorityFactory datumAuthorityFactory;

    private static CoordinateOperationAuthorityFactory copAuthorityFactory;

    private static MathTransformFactory mtFactory;

    public static class T2001 extends Test2001 {
        public T2001() {
            super(TestSuite.csAuthorityFactory);
        }
    }
    public static class T2002 extends Test2002 {public T2002() {super(TestSuite.datumAuthorityFactory);}}
    public static class T2003 extends Test2003 {public T2003() {super(TestSuite.datumAuthorityFactory);}}
    public static class T2004 extends Test2004 {public T2004() {super(TestSuite.datumAuthorityFactory, TestSuite.crsAuthorityFactory);}}
    public static class T2005 extends Test2005 {public T2005() {super(TestSuite.copAuthorityFactory);}}
    public static class T2006 extends Test2006 {public T2006() {super(TestSuite.crsAuthorityFactory);}}
    public static class T2007 extends Test2007 {public T2007() {super(TestSuite.copAuthorityFactory);}}
    public static class T2008 extends Test2008 {public T2008() {super(TestSuite.datumAuthorityFactory, TestSuite.crsAuthorityFactory);}}
    public static class T2009 extends Test2009 {public T2009() {super(TestSuite.copAuthorityFactory);}}
    public static class T3002 extends Test3002 {public T3002() {super(TestSuite.datumFactory);}}
    public static class T3003 extends Test3003 {public T3003() {super(TestSuite.datumFactory);}}
    public static class T3004 extends Test3004 {public T3004() {super(TestSuite.datumFactory, TestSuite.csFactory, TestSuite.crsFactory);}}
    public static class T3005 extends Test3005 {public T3005() {super(TestSuite.copFactory, TestSuite.mtFactory);}}
}
