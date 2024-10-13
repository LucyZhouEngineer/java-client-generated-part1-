package io.swagger.client;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;




public class SkiersApiMultithreadedClient {
    public static final String BASE_PATH = "http://54.185.11.219:8080";
    private static final int TOTAL_EVENTS = 200_000;
    private static final int INITIAL_THREADS = 32;

    public static void main(String[] args) throws InterruptedException {
        BlockingQueue<SkierLiftRideEvent> eventQueue = new ArrayBlockingQueue<>(1000);
        AtomicInteger completedRequests = new AtomicInteger(0);
        AtomicInteger failedRequests = new AtomicInteger(0);

        long startTime = System.currentTimeMillis();

        Thread generatorThread = new Thread(new EventGenerator(eventQueue, TOTAL_EVENTS));
        generatorThread.start();

        ExecutorService executor = Executors.newFixedThreadPool(INITIAL_THREADS);
        for (int i = 0; i < INITIAL_THREADS; i++) {
            executor.execute(new PostWorker(eventQueue, completedRequests, failedRequests));
        }

        generatorThread.join();
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.HOURS);

        long endTime = System.currentTimeMillis();
        long wallTime = endTime - startTime;

        System.out.println("Main method executed!");
        System.out.println("Completed Requests: " + completedRequests.get());
        System.out.println("Failed Requests: " + failedRequests.get());
        System.out.println("Total Run Time (ms): " + wallTime);
        System.out.println("Throughput (requests/sec): " + (TOTAL_EVENTS * 1000) / wallTime);
    }
}
