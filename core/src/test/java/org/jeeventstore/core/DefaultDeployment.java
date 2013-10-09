package org.jeeventstore.core;

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

        try {
            File[] libs = resolve(artifact);

            int i = 0;
            for (File f : libs) {
                System.out.println("DefaultDeployment, add " + i + ": " + f.getAbsolutePath());
                ear.addAsLibrary(f);
            }
        } catch (RuntimeException e) {
            // printing the error helps with testing
            System.err.println(">>>>>> ERROR: " + e + " / " + e.getCause());
            throw e;
        }
        return ear;
    }

}
