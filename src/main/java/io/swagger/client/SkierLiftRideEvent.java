package io.swagger.client;


public class SkierLiftRideEvent {
    private final int skierID;
    private final int resortID;
    private final int liftID;
    private final int seasonID;
    private final int dayID;
    private final int time;

    public SkierLiftRideEvent(int skierID, int resortID, int liftID, int seasonID, int dayID, int time) {
        this.skierID = skierID;
        this.resortID = resortID;
        this.liftID = liftID;
        this.seasonID = seasonID;
        this.dayID = dayID;
        this.time = time;
    }

    public int getSkierID() {
        return skierID;
    }

    public int getResortID() {
        return resortID;
    }

    public int getLiftID() {
        return liftID;
    }

    public int getSeasonID() {
        return seasonID;
    }

    public int getDayID() {
        return dayID;
    }

    public int getTime() {
        return time;
    }

    @Override
    public String toString() {
        return "SkierLiftRideEvent{" +
                "skierID=" + skierID +
                ", resortID=" + resortID +
                ", liftID=" + liftID +
                ", seasonID=" + seasonID +
                ", dayID=" + dayID +
                ", time=" + time +
                '}';
    }
}
