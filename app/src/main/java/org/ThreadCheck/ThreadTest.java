package org.ThreadCheck;

class Counter{
    static int count;
    public synchronized static void Counting(){
        count++;
    }
}

class A implements Runnable{
    int initializer;
    public A(int initializer){
        this.initializer = initializer;
    }

    @Override
    public void run() {
        Counter count = new Counter();
        int i = 0;
        while (i < initializer) {
            try{
                count.Counting();
                Thread.sleep(1);
                i++;
            }
            catch (InterruptedException e){
                System.out.println("Error: " + e.getMessage());
            }
        }
    }
}
class B implements Runnable{
    int initializer;
    public B(int initializer){
        this.initializer = initializer;
    }

    @Override
    public void run(){
        Counter count = new Counter();
        int i = 0;
        while (i < initializer) {
            try{
                count.Counting();
                Thread.sleep(1);
                i++;
            }
            catch (InterruptedException e){
                System.out.println("Error: " + e.getMessage());
            }
        }
    }
}

public class ThreadTest {
    int initializer;
    public ThreadTest(int initializer){
        this.initializer = initializer;
    }
    public int ThreadWork() {
        Runnable obj1 = new A(this.initializer);
        Runnable obj2 = new B(this.initializer);

        Thread t1 = new Thread(obj1);
        Thread t2 = new Thread(obj2);


        t1.start();
        t2.start();

        try {
            t1.join();
            t2.join();
        }
         catch (InterruptedException e){
            return -1;
        }
        return Counter.count;
    }
}
