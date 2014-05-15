/*
 * Copyright (c) 2013-2014 Red Rainbow IT Solutions GmbH, Germany
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package org.jeeventstore.tests;

import java.io.File;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;

/**
 * Construct the Arquillian default deployment consisting of the core library
 * and its dependencies.  Uses Maven to compile the list of dependencies.
 */
public class DefaultDeployment {

    /**
     * Resolve the dependencies for the given maven artifact.
     * @param artifact
     * @return A list of files pointing to the dependencies.
     */
    static File[] resolve(String artifact) {
	return Maven.resolver().loadPomFromFile("pom.xml")
		.resolve(artifact)
		.withTransitivity()
                .as(File.class); 
    }

    public static EnterpriseArchive ear(String artifact) {
	System.out.println("Generating standard EAR deployment");
	EnterpriseArchive ear = ShrinkWrap.create(EnterpriseArchive.class);
        addDependencies(ear, artifact, true);
        return ear;
    }

    public static void addDependencies(EnterpriseArchive ear, String artifact, boolean includeArtifact) {
        String lib = artifact.split(":")[1];
        try {
            File[] libs = resolve(artifact);
            for (int i = 0; i < libs.length; i++) {
                if (i == 0 && !includeArtifact)
                    continue;
                File f = libs[i];
                String filename = (i > 0 ? f.getName() : lib + ".jar");
                System.out.println("Adding dependency #" + i 
                        + ": " + f.getAbsolutePath() 
                        + " as " + filename);
                ear.addAsLibrary(f, filename);
            }
        } catch (RuntimeException e) {
            // printing the error helps with testing
            System.err.println(">>>>>> ERROR: " + e + " / " + e.getCause());
            throw e;
        }
    }

}
