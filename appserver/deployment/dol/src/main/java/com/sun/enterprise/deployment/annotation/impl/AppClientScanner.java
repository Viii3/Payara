/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2012 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
// Portions Copyright [2024] [Payara Foundation and/or its affiliates]

package com.sun.enterprise.deployment.annotation.impl;

import com.sun.enterprise.deployment.ApplicationClientDescriptor;
import org.glassfish.apf.impl.AnnotationUtils;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.classmodel.reflect.Parser;
import org.jvnet.hk2.annotations.Service;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;

/**
 * Implementation of the Scanner interface for AppClient
 * <p>
 * This scanner overrides process(ReadableArchive...) so that when used in the
 * ACC it will work correctly with InputJarArchive readable archives, not just the
 * expanded directory archives during deployment on the server.
 *
 * @author Shing Wai Chan
 * @author tjquinn
 */
@Service(name="car")
@PerLookup
public class AppClientScanner extends ModuleScanner<ApplicationClientDescriptor> {
    @Override
    public void process(ReadableArchive archive, ApplicationClientDescriptor bundleDesc, ClassLoader classLoader, Parser parser) throws IOException {
        setParser(parser);
        doProcess(archive, bundleDesc, classLoader);
        completeProcess(bundleDesc, archive);
        calculateResults(bundleDesc);
    }

    @Override
    public void process(File archiveFile, ApplicationClientDescriptor bundleDesc, ClassLoader classLoader) throws IOException {
        /*
         * This variant should not be invoked, but we need to have it here to
         * satisfy the interface contract.  For this app client scanner, its
         * own process(ReadableArchive...) method will be invoked rather than
         * the one implemented at ModuleScanner.  This is to allow the app
         * client one to support InputJarArchives as well as FileArchives.  This
         * is important because the ACC deals with JARs directly rather than
         * expanding them into directories.
         */
        throw new UnsupportedOperationException("Not supported.");
    }

    /**
     * This scanner will scan the given main class for annotation processing.
     * The archiveFile and libJarFiles correspond to classpath.
     * @param archiveFile
     * @param desc
     * @param classLoader
     */
    private void doProcess(ReadableArchive archive, ApplicationClientDescriptor desc,
            ClassLoader classLoader) throws IOException {
        if (AnnotationUtils.getLogger().isLoggable(Level.FINE)) {
            AnnotationUtils.getLogger().fine("archiveFile is " + archive.getURI().toASCIIString());
            AnnotationUtils.getLogger().fine("classLoader is " + classLoader);
        }

        //always add main class
        String mainClassName = desc.getMainClassName();
        addScanClassName(mainClassName);        

        // add callback handle if it exist in appclient-client.xml
        String callbackHandler = desc.getCallbackHandler();
        if (callbackHandler != null && !callbackHandler.trim().equals("")) {
            addScanClassName(desc.getCallbackHandler());
        }

        this.classLoader = classLoader;
        this.archiveFile = null; // = archive;
    }


    private File scanJar(URI uriToAdd) {
        return new File(uriToAdd);
    }

    private URI scanURI(URI uriToAdd) throws IOException {
        if (uriToAdd.getScheme().equals("jar")) {
            try {
                uriToAdd = new URI("file", uriToAdd.getSchemeSpecificPart(), null);
            } catch (URISyntaxException ex) {
                throw new IOException(ex);
            }
        }
        return uriToAdd;
    }
}
