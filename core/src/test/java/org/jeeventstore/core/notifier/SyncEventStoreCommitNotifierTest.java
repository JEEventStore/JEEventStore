package org.jeeventstore.core.notifier;

import java.io.File;
import javax.ejb.EJB;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.testng.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jeeventstore.core.DefaultDeployment;
import org.jeeventstore.core.store.TestChangeSet;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 *
 * @author Alexander Langer
 */
public class SyncEventStoreCommitNotifierTest extends AbstractEventStoreCommitNotifierTest {

    @Deployment
    public static EnterpriseArchive deployment() {
        EnterpriseArchive ear = DefaultDeployment.ear("org.jeeventstore:jeeventstore-core");
        ear.addAsModule(ShrinkWrap.create(JavaArchive.class)
                .addAsManifestResource(new File("src/test/resources/META-INF/beans.xml"))
                .addAsManifestResource(
                    new File("src/test/resources/META-INF/ejb-jar-SyncEventStoreCommitNotifierTest.xml"),
                             "ejb-jar.xml")
                .addClass(AbstractEventStoreCommitNotifierTest.class)
                .addClass(SyncEventStoreCommitNotifierTest.class)
                .addClass(TestChangeSet.class)
                );
        return ear;
    }

    @EJB
    private EventStoreCommitNotifier notifier;

    @Override
    protected EventStoreCommitNotifier instance() {
        return this.notifier;
    }
    
}