package io.swagger.client;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import io.swagger.client.api.SkiersApi;
import io.swagger.client.model.LiftRide;

public class PostWorker implements Runnable {
    private final BlockingQueue<SkierLiftRideEvent> eventQueue;
    private final int requestsToSend;
    private static final int MAX_RETRIES = 5;

    // Use AtomicInteger for thread-safe updates to completed and failed counts
    private static final AtomicInteger totalCompleted = new AtomicInteger(0);
    private static final AtomicInteger totalFailed = new AtomicInteger(0);

    public PostWorker(BlockingQueue<SkierLiftRideEvent> eventQueue, int requestsToSend) {
        this.eventQueue = eventQueue;
        this.requestsToSend = requestsToSend;
    }

    @Override
    public void run() {
        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath(Client.BASE_URL);
        SkiersApi skiersApi = new SkiersApi(apiClient);

        try {
            for (int i = 0; i < requestsToSend; i++) {
                SkierLiftRideEvent event = eventQueue.take();
                LiftRide liftRide = new LiftRide();
                liftRide.setLiftID(event.getLiftID());
                liftRide.setTime(event.getTime());

                int attempt = 0;
                boolean success = false;

                while (attempt < MAX_RETRIES && !success) {
                    try {
                        skiersApi.writeNewLiftRide(
                            liftRide, event.getResortID(),
                            String.valueOf(event.getSeasonID()),
                            String.valueOf(event.getDayID()),
                            event.getSkierID()
                        );
                        totalCompleted.incrementAndGet();
                        success = true;
                    } catch (ApiException e) {
                        attempt++;
                        if (attempt >= MAX_RETRIES) {
                            totalFailed.incrementAndGet();
                        }
                        Thread.sleep(1000L * attempt);  // Exponential backoff
                    }
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static int getTotalCompleted() {
        return totalCompleted.get();
    }

    public static int getTotalFailed() {
        return totalFailed.get();
    }
}




//
//public class PostWorker implements Runnable {
//    private final BlockingQueue<SkierLiftRideEvent> eventQueue;
//    private final int requestsPerThread;
//    private int completedRequests = 0;
//    private int failedRequests = 0;
//    private static final int MAX_RETRIES = 5;
//
//    public PostWorker(BlockingQueue<SkierLiftRideEvent> eventQueue, int requestsPerThread) {
//        this.eventQueue = eventQueue;
//        this.requestsPerThread = requestsPerThread;
//    }
//
//    @Override
//    public void run() {
//        ApiClient apiClient = new ApiClient();
//        apiClient.setBasePath(Client.BASE_URL);  // Base URL only
//        SkiersApi skiersApi = new SkiersApi(apiClient);
//
//        try {
//            for (int i = 0; i < requestsPerThread; i++) {
//                SkierLiftRideEvent event = eventQueue.take();  // Blocking until an event is available
//
//                LiftRide liftRide = new LiftRide();
//                liftRide.setLiftID(event.getLiftID());
//                liftRide.setTime(event.getTime());
//
//                int attempt = 0;
//                boolean success = false;
//
//                // Retry logic with MAX_RETRIES
//                while (attempt < MAX_RETRIES && !success) {
//                    try {
//                        // POST request using Swagger-generated API client
//                        skiersApi.writeNewLiftRide(
//                            liftRide, event.getResortID(),
//                            String.valueOf(event.getSeasonID()),
//                            String.valueOf(event.getDayID()),
//                            event.getSkierID()
//                        );
//                        completedRequests++;
//                        success = true;
//                    } catch (ApiException e) {
//                        attempt++;
//                        System.err.println("Request failed: " + e.getMessage());
//                        if (attempt >= MAX_RETRIES) {
//                            failedRequests++;
//                        }
//                        // Exponential backoff on retries
//                        Thread.sleep(1000L * attempt);
//                    }
//                }
//            }
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//        }
//    }
//
//    public int getCompletedRequests() {
//        return completedRequests;
//    }
//
//    public int getFailedRequests() {
//        return failedRequests;
//    }
//}
