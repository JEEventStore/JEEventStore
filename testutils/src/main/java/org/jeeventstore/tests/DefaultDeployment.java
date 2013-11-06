package org.jeeventstore.tests;

import java.io.File;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;

/**
 * Construct the Arquillian default deployment consisting of the core library
 * and its dependencies.  Uses Maven to compile the list of dependencies.
 * @author Alexander Langer
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
