/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package os_project2;

import java.util.ArrayList;

/**
 *
 * @author UpToTech
 */
public class Process {

    String processName;
    double startTime = -1;
    double finishTime = -1;
    double arrivalTime;
    double burstTime;
    int size;
    double remainingTime;
    int numberOfFaults = 0;
    int pageLocation;   // where we r at each process file ( in it's page file ) 
    double waitTime;
    double turnaround;
    ArrayList<Page> pages = new ArrayList<Page>();

    public Process(String processName, double arrivalTime, double burstTime, int size) {
        this.processName = processName;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.size = size;
        this.remainingTime = burstTime;
    }

    public Process() {

    }
}
