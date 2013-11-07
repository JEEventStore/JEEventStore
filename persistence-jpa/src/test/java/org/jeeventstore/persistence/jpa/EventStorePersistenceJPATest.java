package org.jeeventstore.persistence.jpa;

import java.io.File;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.testng.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jeeventstore.tests.DefaultDeployment;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 *
 * @author Alexander Langer
 */
public class EventStorePersistenceJPATest extends Arquillian {

    @Deployment
    public static EnterpriseArchive deployment() {
        EnterpriseArchive ear = ShrinkWrap.create(EnterpriseArchive.class, "test.ear");
        DefaultDeployment.addDependencies(ear, "org.jeeventstore:jeeventstore-persistence-jpa", false);
        DefaultDeployment.addDependencies(ear, "org.jeeventstore:ntstore-serialization-gson", true);
        ear.addAsModule(ShrinkWrap.create(JavaArchive.class, "ejb.jar")
                .addAsManifestResource(new File("src/test/resources/META-INF/beans.xml"))
                .addAsManifestResource(new File("src/test/resources/META-INF/persistence.xml"))
                .addAsManifestResource(new File(
                        "src/test/resources/META-INF/ejb-jar-EventStorePersistenceJPATest.xml"),
                        "ejb-jar.xml")
                .addPackage(EventStorePersistenceJPA.class.getPackage())
                );
        return ear;
    }

    @Test
    public void test_it() {
        System.out.println("ding dong");
    }
    
}
