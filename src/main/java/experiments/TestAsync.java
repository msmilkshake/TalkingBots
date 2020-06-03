package experiments;

import java.util.Scanner;

public class TestAsync {
    
    private Scanner scanner;
    private Object lock;
    private volatile String string;
    
    public TestAsync() {
        scanner = new Scanner(System.in);
        lock = new Object();
        string = "";
    }
    
    public void start() {
        
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    synchronized (lock) {
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    System.out.println("String changed.");
                }
            }
        });
        t1.start();
        
        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (!string.isEmpty()) {
                        synchronized (lock) {
                            lock.notify();
                        }
                        string = "";
                    }
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        t2.start();
        
        Thread t3 = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    string = scanner.nextLine();
                }
            }
        });
        t3.start();
    }
    
    public static void main(String[] args) {
        new TestAsync().start();
    }
    
}