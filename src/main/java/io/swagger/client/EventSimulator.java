package io.swagger.client;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadLocalRandom;

public class EventSimulator implements Runnable {
    private final BlockingQueue<SkierLiftRideEvent> eventQueue;
    private final int totalEvents;

    public EventSimulator(BlockingQueue<SkierLiftRideEvent> eventQueue, int totalEvents) {
        this.eventQueue = eventQueue;
        this.totalEvents = totalEvents;
    }

    @Override
    public void run() {
        try {
            for (int i = 0; i < totalEvents; i++) {
                SkierLiftRideEvent event = new SkierLiftRideEvent(
                        ThreadLocalRandom.current().nextInt(1, 100001),  
                        ThreadLocalRandom.current().nextInt(1, 11),      
                        ThreadLocalRandom.current().nextInt(1, 41),      
                        2024,                                            
                        1,                                              
                        ThreadLocalRandom.current().nextInt(1, 361)      
                );
                eventQueue.put(event); 
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("EventGenerator interrupted");
        }
    }
}
