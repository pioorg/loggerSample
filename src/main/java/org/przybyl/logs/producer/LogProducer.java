package org.przybyl.logs.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.security.SecureRandom;

import static org.przybyl.logs.producer.LogProducer.logger;

/**
 * Created by Piotr Przybył (piotr@przybyl.org).
 */
public class LogProducer {

    static final Logger logger = LoggerFactory.getLogger(LogProducer.class);

    public static void main(String[] args) {
        MDC.put("foo", "bar");
        System.out.println("Starting the app.");
        addShutdownHook();

        new ActualWorker().doHeavyStuffWithLotsOfLogs();


    }

    private static void addShutdownHook() {
        final Thread mainProducerThread = Thread.currentThread();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Stopping the app.");
            try {
                mainProducerThread.interrupt();
                mainProducerThread.join(1000);
            } catch (InterruptedException e) {
                logger.error("Interrupted during join.", e);
            }
        }));
    }
}

class ActualWorker {
    private static SecureRandom random = new SecureRandom();

    void doHeavyStuffWithLotsOfLogs() {
        while (!Thread.interrupted()) {
            try {
                doAndLogSomeStuff();
                Thread.sleep(500);
            } catch (InterruptedException e) {
                logger.info("Interrupted during sleep, going to quit.");
                break;
            } catch (Throwable t) {
                logger.error("Should not happen, but my stupid leader insists on this log. Anyway.", t);
                System.exit(42);
            }
        }
    }

    private void doAndLogSomeStuff() {
        int nextActionIndex = random.nextInt(51) / 10;
        switch (nextActionIndex) {
            case 0:
                logger.error("Things went out of control.");
                break;
            case 1:
                logger.warn("Didn't I warn you?");
                break;
            case 2:
                logger.info("For your information: do it as ASAP as possible!");
                break;
            case 3:
                logger.debug("Let's see what's going on...");
                break;
            case 4:
                logger.trace("This is micro management.");
                break;
            default:
                throw new IllegalArgumentException("Action code outside range!");

        }
    }
}
