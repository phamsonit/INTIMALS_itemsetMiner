package be.intimals.seqMiner.util;

import java.io.FileWriter;

public class TimeOut implements Runnable {

    private boolean taskComplete = false;
    private long times;
    private FileWriter report;

    public void setTaskComplete(boolean value){
        this.taskComplete = value;
    }

    public void setTimes(long t){
        this.times = t;
    }

    public void setReport(FileWriter report){
        this.report = report;
    }

    public void closeReport(){
        try {
            this.report.close();
        }catch (Exception e){}
    }

    public void stop(){
        System.exit(3);
    }

    public void run() {
        try {
            Thread.sleep(times);
            if (taskComplete == false) {
                //System.out.println("Timed Out : "+times/(60*1000)+" minutes");
                report.write("Timed Out : "+times/(60*1000)+" minutes");
                closeReport();
                System.exit(2);
            }
        }catch (Exception e){
            System.out.println("Timeout:" + e);
            e.printStackTrace();
        }
    }

}


