package io.swagger.client;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import io.swagger.client.api.SkiersApi;
import io.swagger.client.model.LiftRide;

public class PostWorker implements Runnable {
    private final BlockingQueue<SkierLiftRideEvent> eventQueue;
    private final AtomicInteger completedRequests;
    private final AtomicInteger failedRequests;
    private static final int MAX_RETRIES = 5;

    public PostWorker(BlockingQueue<SkierLiftRideEvent> eventQueue, AtomicInteger completedRequests, AtomicInteger failedRequests) {
        this.eventQueue = eventQueue;
        this.completedRequests = completedRequests;
        this.failedRequests = failedRequests;
    }

    @Override
    public void run() {
        ApiClient apiClient = new ApiClient();
        // We'll set the base path for each request individually
        SkiersApi skiersApi = new SkiersApi(apiClient);

        while (true) {
            try {
                SkierLiftRideEvent event = eventQueue.take(); // Blocking until an event is available
                LiftRide liftRide = new LiftRide();
                liftRide.setLiftID(event.getLiftID());
                liftRide.setTime(event.getTime());

                // Construct the full path for this specific request
                String fullPath = SkiersApiMultithreadedClient.BASE_PATH + String.format("/skiers/%d/seasons/%d/days/%d/skiers/%d",
                    
                    event.getResortID(),
                    event.getSeasonID(),
                    event.getDayID(),
                    event.getSkierID());
                
                // Set the base path for this specific request
                apiClient.setBasePath(fullPath);

                int attempt = 0;
                boolean success = false;

                while (attempt < MAX_RETRIES && !success) {
                    try {
                        // Now we only need to pass the LiftRide object
                         skiersApi.writeNewLiftRide(
                                liftRide, event.getResortID(), String.valueOf(event.getSeasonID()),
                                String.valueOf(event.getDayID()), event.getSkierID()
                        );
                        completedRequests.incrementAndGet();
                        success = true;
                    } catch (ApiException e) {
                        attempt++;
                        if (attempt >= MAX_RETRIES) {
                            failedRequests.incrementAndGet();
                        }
                        Thread.sleep(1000 * attempt); 
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}




// package io.swagger.client;
// import java.util.concurrent.BlockingQueue;
// import java.util.concurrent.atomic.AtomicInteger;
// import io.swagger.client.api.SkiersApi;
// import io.swagger.client.model.LiftRide;


// public class PostWorker implements Runnable {
//     private final BlockingQueue<SkierLiftRideEvent> eventQueue;
//     private final AtomicInteger completedRequests;
//     private final AtomicInteger failedRequests;
//     private static final int MAX_RETRIES = 5;

//     public PostWorker(BlockingQueue<SkierLiftRideEvent> eventQueue, AtomicInteger completedRequests, AtomicInteger failedRequests) {
//         this.eventQueue = eventQueue;
//         this.completedRequests = completedRequests;
//         this.failedRequests = failedRequests;
//     }

//     @Override
//     public void run() {
//         ApiClient apiClient = new ApiClient();
//         apiClient.setBasePath(SkiersApiMultithreadedClient.BASE_PATH);
//         SkiersApi skiersApi = new SkiersApi(apiClient);

//         while (true) {
//             try {
//                 SkierLiftRideEvent event = eventQueue.take(); // Blocking until an event is available
//                 LiftRide liftRide = new LiftRide();
//                 liftRide.setLiftID(event.getLiftID());
//                 liftRide.setTime(event.getTime());

//                 int attempt = 0;
//                 boolean success = false;

//                 while (attempt < MAX_RETRIES && !success) {
//                     try {
//                         skiersApi.writeNewLiftRide(
//                                 liftRide, event.getResortID(), String.valueOf(event.getSeasonID()),
//                                 String.valueOf(event.getDayID()), event.getSkierID()
//                         );
//                         completedRequests.incrementAndGet();
//                         success = true;
//                     } catch (ApiException e) {
//                         attempt++;
//                         if (attempt >= MAX_RETRIES) {
//                             failedRequests.incrementAndGet();
//                         }
//                         Thread.sleep(1000 * attempt); 
//                     }
//                 }
//             } catch (InterruptedException e) {
//                 Thread.currentThread().interrupt();
//                 break;
//             }
//         }
//     }
// }
