package org.jeeventstore.persistence.jpa;

import org.jeeventstore.persistence.PersistenceTestHelper;
import java.io.File;
import javax.ejb.EJBTransactionRequiredException;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jeeventstore.TestUTF8Utils;
import org.jeeventstore.persistence.AbstractPersistenceTest;
import org.jeeventstore.serialization.XMLSerializer;
import org.jeeventstore.tests.DefaultDeployment;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 *
 * @author Alexander Langer
 */
public class EventStorePersistenceJPATest extends AbstractPersistenceTest {

    @Deployment
    public static EnterpriseArchive deployment() {
        EnterpriseArchive ear = ShrinkWrap.create(EnterpriseArchive.class, "test.ear");
        DefaultDeployment.addDependencies(ear, "org.jeeventstore:jeeventstore-persistence-jpa", false);
        ear.addAsModule(ShrinkWrap.create(JavaArchive.class, "ejb.jar")
                .addAsManifestResource(new File("src/test/resources/META-INF/beans.xml"))
                .addAsManifestResource(new File("src/test/resources/META-INF/persistence.xml"))
                .addAsManifestResource(new File(
                        "src/test/resources/META-INF/ejb-jar-EventStorePersistenceJPATest.xml"),
                        "ejb-jar.xml")
                .addClass(XMLSerializer.class)
                .addClass(TestUTF8Utils.class)
                .addClass(PersistenceTestHelper.class)
                .addPackage(AbstractPersistenceTest.class.getPackage())
                .addPackage(EventStorePersistenceJPA.class.getPackage())
                );
        return ear;
    }

    @Test
    public void test_transaction_required() {
        try {
            getPersistence().allChanges("TEST");
            fail("Should have failed by now");
        } catch (EJBTransactionRequiredException e) {
            // expected
        }
        try {
            getPersistence().getFrom("TEST", "FOO", 0, 10l);
            fail("Should have failed by now");
        } catch (EJBTransactionRequiredException e) {
            // expected
        }
    }

}
