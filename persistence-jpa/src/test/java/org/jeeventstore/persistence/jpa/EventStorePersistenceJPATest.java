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
