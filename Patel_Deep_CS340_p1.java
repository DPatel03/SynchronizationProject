import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicBoolean;

public class Patel_Deep_CS340_p1{
    public static int groupSize = 4;
    public static final int hTime = 4000;

    public static void main(String[] args) {
        int visitors = 18;
        int drivers = 4;
        // Creating controller thread
        Controller con = new Controller();

        // Create and start visitor threads
        for (int i = 0; i < visitors; i++) {
            Visitor vis = new Visitor(i, con);
            vis.start();
        }

        // Create and start driver threads
        for (int i = 0; i < drivers; i++) {
            Driver dri = new Driver(i, con);
            dri.start();
        }

        // Start controller thread
        con.start();
    }
}

class Visitor extends Thread {
    public static long time = System.currentTimeMillis();
    int id;
    boolean Handicapped;
    Controller controller; // constructor has id and controller thread as argument b/c need to add the visitor to Queue

    public Visitor(int id, Controller controller) {
        this.id = id;
        setName("Visitor - " + id);
        this.Handicapped = Math.random() <= .25; // Around 25% Visitor will be Handicapped Visitor
        this.controller = controller;
    }

    public void msg(String m) {
        System.out.println("[" + (System.currentTimeMillis() - time) + "] " + getName() + ": " + m);
    }
    // If boolean for Handicapped is true then thread with sleep for twice longer and i am checking thorugh if statement then add them to queue which is in  controller thread.
    public void run() {
        if(Handicapped){
            msg("Handicapped visitor arrived at museum ");
            msg("Wandering around the museum ");
            try{
                Thread.sleep(4000);
            } catch (InterruptedException e) {
                msg("Interrupted");
            }
            controller.Queue(this);
            
        }else{
            msg("Visitor arrives at museum ");
            msg("Wandering around the museum ");
            try{
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                msg("Interrupted");
            }
            controller.Queue(this);
        }
    } 
}

class Driver extends Thread {
    public static long time = System.currentTimeMillis();
    int id;
    Controller controller;

    public Driver(int id, Controller controller) {
        this.id = id;
        setName("Driver - " + id);
        this.controller = controller;
    }

    public void msg(String m) {
        System.out.println("[" + (System.currentTimeMillis() - time) + "] " + getName() + ": " + m);
    }
    // it will first print car arrived and acces the function which is Group Visitor with car. 
    public void run() {
        try {
            msg("Arrives at the depot.");
            controller.Group(this);
            msg("Driving around the park.");
            Thread.sleep(2000); 
            msg("Letting visitors off.");
            Thread.sleep(2000); 
            } catch (InterruptedException e) {
                msg("Interrupted.");
        }
    }
}

class Controller extends Thread {
    public static long time = System.currentTimeMillis();
    Queue<Visitor> visitorQueue = new LinkedList<>(); // To store all the visitors when they come to the depot
    AtomicInteger handicappedCounter = new AtomicInteger(0); // Count how many handicapped visitors arrived
    boolean groupReady = false;
    AtomicBoolean loadingCar = new AtomicBoolean(false);

    public void msg(String m) {
        System.out.println("[" + (System.currentTimeMillis() - time) + "] " + getName() + ": " + m);
    }

    public Controller() {
        setName("Controller");
    }
    // visitor thread come here and it self to queue
    public void Queue(Visitor vis) {
        visitorQueue.add(vis);
        if (vis.Handicapped) {
            handicappedCounter.incrementAndGet();
        }
    }
    // drive come and there and use this fucntion. 
    public void Group(Driver dri) {
        while (visitorQueue.size() < Patel_Deep_CS340_p1.groupSize) {
            // Busy wait
            Thread.yield();
        }
        // group four visitor in this part of the function  
        for (int i = 0; i < Patel_Deep_CS340_p1.groupSize; i++) {
            Visitor vis = visitorQueue.poll();
            if (vis != null) {
                vis.msg("Boarding driver " + dri.getName());
            }
        }
        msg("Group is ready for driver " + dri.getName());
        groupReady = true;
    }

    public void run() {
        msg("Controller started");
        try {
            // Busy wait for handicapped visitor to arrive and if no visitor thread at queue. 
            while (handicappedCounter.get() == 0 && visitorQueue.isEmpty()) {
                Thread.sleep(Patel_Deep_CS340_p1.hTime);
            }
            if (handicappedCounter.get() > 0) {
                handicappedCounter.decrementAndGet();
            }
        } catch (InterruptedException e) {
            msg("Interrupted");
        }
    }
}
