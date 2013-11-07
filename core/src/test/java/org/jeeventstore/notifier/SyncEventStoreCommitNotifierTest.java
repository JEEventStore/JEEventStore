package org.jeeventstore.notifier;

import org.jeeventstore.EventStoreCommitNotifier;
import java.io.File;
import javax.ejb.EJB;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jeeventstore.ChangeSet;
import org.jeeventstore.store.TestChangeSet;
import org.jeeventstore.tests.DefaultDeployment;

/**
 *
 * @author Alexander Langer
 */
public class SyncEventStoreCommitNotifierTest extends AbstractEventStoreCommitNotifierTest {

    @Deployment
    public static Archive<?> deployment() {
        EnterpriseArchive ear = ShrinkWrap.create(EnterpriseArchive.class, "test.ear")
                .addAsModule(ShrinkWrap.create(JavaArchive.class, "ejb.jar")
                        .addAsManifestResource(new File("src/test/resources/META-INF/beans.xml"))
                        .addAsManifestResource(
                                new File("src/test/resources/META-INF/ejb-jar-SyncEventStoreCommitNotifierTest.xml"),
                                "ejb-jar.xml")
                        .addPackage(AsyncEventStoreCommitNotifier.class.getPackage())
                        .addPackage(ChangeSet.class.getPackage())
                        .addClass(TestChangeSet.class)
                );

        return ear;
    }

    @EJB(lookup = "java:global/test/ejb/EventStoreCommitNotifier")
    private EventStoreCommitNotifier notifier;

    @Override
    protected EventStoreCommitNotifier instance() {
        return this.notifier;
    }
    
}