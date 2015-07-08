package org.jacp.test.messaging;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.jacp.launcher.TestFXJacpFXSpringLauncher;
import org.jacp.test.NonUITests;
import org.jacp.test.main.ApplicationLauncherPerspectiveMessaginTest;
import org.jacp.test.perspectives.PerspectiveMessagingTestP1;
import org.jacp.test.perspectives.PerspectiveMessagingTestP2;
import org.jacp.test.perspectives.PerspectiveMessagingTestP3;
import org.jacp.test.workbench.WorkbenchPerspectiveMessageTesting;
import org.jacpfx.rcp.workbench.FXWorkbench;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: amo
 * Date: 10.09.13
 * Time: 21:48
 * To change this template use File | Settings | File Templates.
 */
public class PerspectiveMessagingTest extends TestFXJacpFXSpringLauncher {

    @Override
    public String getXmlConfig() {
        return "main.xml";
    }

    /**
     * @param args
     */
    public static void main(final String[] args) {
        Application.launch(args);
    }

    @Override
    protected Class<? extends FXWorkbench> getWorkbenchClass() {
        return WorkbenchPerspectiveMessageTesting.class;
    }

    @Override
    protected String[] getBasePackages() {
        return new String[]{"org.jacp.test"};
    }

    @Override
    public void postInit(final Stage stage) {

    }



    /**
     * @param args
     */



    private void executeMessaging() throws InterruptedException {
        PerspectiveMessagingTestP1.wait = new CountDownLatch(1);
        PerspectiveMessagingTestP2.wait = new CountDownLatch(1);
        PerspectiveMessagingTestP3.wait = new CountDownLatch(1);
        PerspectiveMessagingTestP1.counter = new AtomicInteger(1000);
        PerspectiveMessagingTestP2.counter = new AtomicInteger(1000);
        PerspectiveMessagingTestP3.counter = new AtomicInteger(1000);
        PerspectiveMessagingTestP1.fireMessage();
        PerspectiveMessagingTestP1.wait.await();
        PerspectiveMessagingTestP2.wait.await();
        PerspectiveMessagingTestP3.wait.await();
    }

    private void warmUp() throws InterruptedException {
        executeMessaging();
    }

    @Test
    // default execution time was 54312 ms (linux) , macos 17826ms  // macos with 3 persp. and 300000 messages Execution time was 28494 ms.
    // macbook with 300000 messages 21068ms,17321ms, 14394ms, 29476ms, 27745ms, 26380ms,23214 ms
    public void testPerspectiveMessaging() throws InterruptedException {
        warmUp();
        long start = System.currentTimeMillis();
        int i = 0;
        while (i < 100) {
            executeMessaging();
            assertTrue(true);
            i++;
        }

        long end = System.currentTimeMillis();

        System.out.println("Execution time was " + (end - start) + " ms.");
    }

    @Override
    protected  void cleanup(){

    }
}
