import java.util.*;
import java.io.IOException;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

class Router implements Runnable {

    File file = new File("output.txt");
    FileWriter fw = new FileWriter(file);
    PrintWriter pw=new PrintWriter(fw);
    public static List<Device> Wqueue=new ArrayList<Device>();
    public static List<Device> Rqueue=new ArrayList<Device>() ;
    public static int rConnectNum=0;
    public static int index=0;
    public static int check=0;
    public static int fileCount=0;
    public Router() throws IOException {
    }

    public void Occupy(Device d) throws InterruptedException {
        //for 3la devices l rqueue
        Thread t1 = null;

        t1=new Thread(this,d.stringetName());
        index++;

        if(check!= Network.N) {
            for (int i = 0; i < Rqueue.size(); i++) {
                if (Objects.equals(Rqueue.get(i).stringetName(), d.stringetName())) {
                    rConnectNum = i + 1;

                }
            }
            check++;
        }

        t1.start();
    }
    public void Release(String s) throws InterruptedException {

        for (int i=0;i<Rqueue.size();i++){
            if(Objects.equals(s, Rqueue.get(i).stringetName())){
                Rqueue.remove(Rqueue.get(i));
                rConnectNum=i+1;
                index--;
                Semaphore.value++;
                try {
                    if(Wqueue.size()!=0) {
                        Semaphore.Signal(i);
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }

        fileCount++;
    }
    public  void check(){//check close output file
        if (fileCount== Network.D){
            pw.close();
        }
    }
    public void run(){
        Random random=new Random();
        for (int i = 0; i < Rqueue.size(); i++) {
            if(Objects.equals(Rqueue.get(i).stringetName(), Thread.currentThread().getName())) {
                try {
                    Thread.sleep(random.nextInt(5) * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                pw.println("connection " + (i+1) + ": " + Thread.currentThread().getName() + " " + "Login");

                try {
                    Thread.sleep(random.nextInt(5) * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                pw.println("connection " +(i+1) + ": " + Thread.currentThread().getName() + " " + "performs online activity");
                try {
                    Thread.sleep(random.nextInt(5) * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                pw.println("connection " + (i+1) + ": " + Thread.currentThread().getName() + " " + "Logged out");
                try {
                    Release(Thread.currentThread().getName());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        check();//check to close output file
        Thread.currentThread().stop();
    }
}

class Semaphore {


    public static int value=0;
    private static Router r;

    static {
        try {
            r = new Router();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static int ind=0;
    public static int count=0;

    public Semaphore(int val){
        value= Network.N;
    }

    public Semaphore() {

    }
    public void Wait()
    {

        for(int i = Network.N; i< Network.D; i++){

            if (value <= 0)
            {
                Router.Wqueue.add(Network.ar.get(i));
                r.pw.println(Network.ar.get(i).stringetName()+" ("+ Network.ar.get(i).getType()+") "+ " arrived and waiting");

            }
        }
    }


    public static void Signal() throws InterruptedException { //for the first N devices
        for(int i = count; i< Network.ar.size(); i++) {
            if (value > 0) {
                value--;
                count++;
                Notify(Network.ar.get(i));
                if(ind!= Network.N){//arrived check
                    ind++;
                    r.pw.println("("+ Network.ar.get(i).stringetName()+") "+"("+ Network.ar.get(i).getType()+") " +" arrived");
                    r.pw.println("connection "+(Router.rConnectNum +1)+": "+ Network.ar.get(i).stringetName()+ " Occupied");


                }
                r.Occupy(Network.ar.get(i));

            }
        }

    }
    public static void Signal(int i) throws InterruptedException { //for waiting devices
        for(int j = count; j< Network.ar.size(); j++) {
            if (value > 0) {
                value--;
                count++;
                Router.Rqueue.add(i, Network.ar.get(j)) ;

                Router.Wqueue.remove(Network.ar.get(j));

                r.pw.println("connection "+(i+1)+": "+ Network.ar.get(j).stringetName()+ " Occupied");
                r.Occupy(Network.ar.get(j));

            }
        }
    }

    public static void Notify(Device e) {

        Router.Rqueue.add(e);
        Router.Wqueue.remove(e);
    }


}

class Device {

    String name;
    String type;
    public String stringetName() {
        return name;
    }

    @Override
    public String toString() {
        return "Device{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
    public Device(String name, String type)
    {
        this.name=name;
        this.type=type;
    }
    public String getType() {
        return type;
    }

}

class Network  {

    public static int N;
    public static int D;
    public static ArrayList<Device> ar=new ArrayList<Device>();

    public static void main(String[] args) throws InterruptedException, IOException {

        Scanner input=new Scanner(System.in);

        boolean validInput=false;
        System.out.println("What is the number of WI-FI Connections?");

        while(!validInput) {
            try {
                N = input.nextInt();
                validInput = true;
            } catch(InputMismatchException e) {
                System.out.println("Please enter an integer!");
                input.next();
            }
        }
        validInput=false;
        System.out.println("What is the number of devices Clients want to connect?");

        while(!validInput) {
            try {
                D=input.nextInt();
                validInput = true;
            } catch(InputMismatchException e) {
                System.out.println("Please enter an integer!");
                input.next();
            }
        }

        Device d;
        Semaphore S=new Semaphore(N);
        System.out.println("Please Enter Name and Type for each device ");

        for (int i=0;i<D;i++){
            String name, type;
            name=input.next();
            type=input.next();
            d=new Device(name,type);
            ar.add(d);
        }
        Semaphore.Signal();
        S.Wait();
    }

}