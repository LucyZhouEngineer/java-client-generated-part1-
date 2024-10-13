package io.swagger.client;

import java.util.concurrent.*;


public class Client {
    private static final int NUM_THREADS = 32;       // Thread pool size
    private static final int REQUESTS_PER_THREAD = 1000;  // Requests per worker
    private static final int TOTAL_EVENTS = 200000;  // Total requests to send
    public static final String BASE_URL = "http://54.213.146.151:8080";

    public static void main(String[] args) throws InterruptedException {
        BlockingQueue<SkierLiftRideEvent> eventQueue = new LinkedBlockingQueue<>();

        // Start the event generator thread
        EventSimulator generator = new EventSimulator(eventQueue, TOTAL_EVENTS);
        Thread generatorThread = new Thread(generator);
        generatorThread.start();

        // Start time measurement
        long startTime = System.currentTimeMillis();

        // Create a fixed thread pool
        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);

        int remainingEvents = TOTAL_EVENTS;
        while (remainingEvents > 0) {
            // Calculate how many events this batch will handle
            int eventsToSend = Math.min(remainingEvents, REQUESTS_PER_THREAD);

            // Submit a new PostWorker task for each batch of events
            PostWorker worker = new PostWorker(eventQueue, eventsToSend);
            executor.execute(worker);

            // Update the remaining events to be processed
            remainingEvents -= eventsToSend;
        }

        // Shutdown and await termination
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.HOURS);
        generatorThread.join();

        // End time measurement
        long endTime = System.currentTimeMillis();

        // Print statistics
        printStatistics(endTime - startTime);
    }

    private static void printStatistics(long wallTime) {
        int totalCompleted = PostWorker.getTotalCompleted();
        int totalFailed = PostWorker.getTotalFailed();

        System.out.println("Number of successful requests: " + totalCompleted);
        System.out.println("Number of failed requests: " + totalFailed);
        System.out.println("Total run time (ms): " + wallTime);
        System.out.println("Throughput (req/sec): " + (totalCompleted / (wallTime / 1000.0)));
    }
}





















//public class Client {
//
//    public static final String BASE_URL = "http://54.213.146.151:8080";
//    private static final int NUM_THREADS = 32;
//    private static final int REQUESTS_PER_THREAD = 1000;
//    private static final int TOTAL_EVENTS = 200000;
//
//    public static void main(String[] args) throws InterruptedException {
//        BlockingQueue<SkierLiftRideEvent> eventQueue = new LinkedBlockingQueue<>();
//
//        // Start the event generator thread
//        EventSimulator generator = new EventSimulator(eventQueue, TOTAL_EVENTS);
//        Thread generatorThread = new Thread(generator);
//        generatorThread.start();
//
//        // Measure the start time for wall time calculation
//        long startTime = System.currentTimeMillis();
//
//        // Use a thread pool to manage PostWorker threads
//        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
//        PostWorker[] workers = new PostWorker[NUM_THREADS];
//
//        // Initialize and submit PostWorker threads to the executor
//        for (int i = 0; i < NUM_THREADS; i++) {
//            workers[i] = new PostWorker(eventQueue, REQUESTS_PER_THREAD);
//            executor.execute(workers[i]);
//        }
//
//        // Wait for all threads to finish execution
//        executor.shutdown();
//        executor.awaitTermination(1, TimeUnit.HOURS);  // Set a timeout to avoid deadlock
//
//        // Ensure the event generator thread is done
//        generatorThread.join();
//
//        // Measure the end time for wall time calculation
//        long endTime = System.currentTimeMillis();
//
//        // Print out the results
//        printStatistics(workers, endTime - startTime);
//    }
//
//    // Method to print statistics after all threads are complete
//    private static void printStatistics(PostWorker[] workers, long wallTime) {
//        int totalCompleted = 0;
//        int totalFailed = 0;
//
//        // Aggregate the results from all workers
//        for (PostWorker worker : workers) {
//            totalCompleted += worker.getCompletedRequests();
//            totalFailed += worker.getFailedRequests();
//        }
//
//        System.out.println("Number of successful requests: " + totalCompleted);
//        System.out.println("Number of failed requests: " + totalFailed);
//        System.out.println("Total run time (ms): " + wallTime);
//        System.out.println("Throughput (req/sec): " + (totalCompleted / (wallTime / 1000.0)));
//    }
//}
