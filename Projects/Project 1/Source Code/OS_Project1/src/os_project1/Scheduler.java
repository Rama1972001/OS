package os_project1;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Scanner;
import javax.swing.JOptionPane;
import org.omg.CORBA.Current;

public class Scheduler {
    
    ArrayList<Process> processes;
    static ArrayList<Process> readyQueue;
    
    final int cancelButtonPressed = -1;
    final int nonPositiveTimeQuantum = -2;
    final int invalidInput = -3;
    
    public static void main(String[] args) {
        new Interface().main(args);
    }
   
    public ArrayList<Integer> SJF(){
        int time = 0;                               // current time
        Process currentlyRunning = null;            // the process that is running currently, initially null
        readyQueue = new ArrayList<Process>();      // ready queue to add arrived processes that are ready to run
        ArrayList<Integer> ganttChart = new ArrayList<Integer>();   // Gantt Chart to show processes run-time
        
        while(!allProcessesFinished()){     // while there are still unfinished processes
            
            checkProcessesArrival(time);    // check if any process has arriced at current moment
            
            if(currentlyRunning == null){               // if no process is running currently 
                Process leastBurstTimeProcess = findLeastBurstTimeProcess();            // find the process with minimum burt time
                int leastBursTimetProcessIdx = readyQueue.indexOf(leastBurstTimeProcess);   // find its index in the readyQueue
                if (leastBursTimetProcessIdx != -1)                                     // and if it is in the queue
                    currentlyRunning = readyQueue.remove(leastBursTimetProcessIdx);     // remove it and run it
                
                if(currentlyRunning != null && currentlyRunning.startTime == -1)    // if there is a process has just started to run
                    currentlyRunning.startTime = time;                              // set its start time to current time
            }
            
            if(currentlyRunning != null && time == currentlyRunning.burstTime + currentlyRunning.startTime){    // if current time = burst time + start time for a currently running process,
                                                                                                                // then this means that the process has finished
                currentlyRunning.finishTime = time;         // set finish time to current time
                currentlyRunning.turnaround = currentlyRunning.finishTime - currentlyRunning.arrivalTime;   // compute turnaround time
                currentlyRunning.waitingTime = currentlyRunning.turnaround - currentlyRunning.burstTime;    // compute waiting time
                currentlyRunning = null;
                
                /* starting another process, just at the same time the previous process has finished */
                Process leastBurstTimeProcess = findLeastBurstTimeProcess();            // find the process with minimum burst time
                int leastBursTimetProcessIdx = readyQueue.indexOf(leastBurstTimeProcess);   // find its index in the readyQueue
                if (leastBursTimetProcessIdx != -1)                                     // and if it is in the queue
                    currentlyRunning = readyQueue.remove(leastBursTimetProcessIdx);     // remove it and run it
                
                if(currentlyRunning != null && currentlyRunning.startTime == -1)    // if there is a process has just started to run,
                    currentlyRunning.startTime = time;                              // then set its start time to current time
            }
            
            addToGanttChart(ganttChart, currentlyRunning);          // add currently running process at the current moment to Gantt Chart
            time++;         // increment time
        }
        
        return ganttChart;
    }  
  
    public ArrayList<Integer> SRTF(){
        int time = 0;                               // current time
        Process currentlyRunning = null;            // the process that is running currently, initially null
        readyQueue = new ArrayList<Process>();      // ready queue to add arrived processes that are ready to run
        ArrayList<Integer> ganttChart = new ArrayList<Integer>();   // Gantt Chart to show processes run-time
        
        while(!allProcessesFinished()){     // while there are still unfinished processes
            checkProcessesArrival(time);    // check if any process has arriced at current moment

            if(currentlyRunning != null)            // if a process is running currently
                currentlyRunning.remainingTime--;   // decrement its remaining running time
            
            Process leastRemainingTimeProcess = findLeastRemainingTimeProcess();    // find the process that has minimum remaining running time
            currentlyRunning = leastRemainingTimeProcess;                           // and then run it, preemiting the previous running process, if any
            
            if(currentlyRunning != null && currentlyRunning.startTime == -1)    // if a process has just started to run,
                currentlyRunning.startTime = time;                              // then set its start time to current time
 
            if(currentlyRunning != null && currentlyRunning.remainingTime == 0){    // if remaining running time for a currently running process is 0,
                                                                                    // then this means that the process has finished  
                currentlyRunning.finishTime = time;         // set finish time to current time
                currentlyRunning.turnaround = currentlyRunning.finishTime - currentlyRunning.arrivalTime;   // compute turnaround time
                currentlyRunning.waitingTime = currentlyRunning.turnaround - currentlyRunning.burstTime;    // compute waiting time
                
                int currentlyRunningIdx = readyQueue.indexOf(currentlyRunning); // find the index of the finished process
                readyQueue.remove(currentlyRunningIdx);                         // remove the finished process from the readyQueue
                currentlyRunning = null;

                /* starting another process, just at the same time the previous process has finished */
                leastRemainingTimeProcess = findLeastRemainingTimeProcess();    // find the process that has minimum remaining running time
                currentlyRunning = leastRemainingTimeProcess;                   // and then run it

                if(currentlyRunning != null && currentlyRunning.startTime == -1)    // if a process has just started to run,
                    currentlyRunning.startTime = time;                              // then set its start time to current time
            }
            
            addToGanttChart(ganttChart, currentlyRunning);          // add currently running process at the current moment to Gantt Chart
            time++;         // increment time
        }
        
        return ganttChart;
    }
    
    public ArrayList<Integer> RR(){
        int time = 0;                          // current time
        Process currentlyRunning = null;       // the process that is running currently, initially null
        int timeQuantum, remainingTimeQuantum; 
        int i = 0;                             // index for readyQueue
        
        readyQueue = new ArrayList<Process>();                      // ready queue to add arrived processes that are ready to run
        ArrayList<Integer> ganttChart = new ArrayList<Integer>();   // Gantt Chart to show processes run-time

        timeQuantum = readTimeQuantum();            // read timeQuantum from user
        
        if (timeQuantum == invalidInput){                          // if invalid input was entered
            JOptionPane.showMessageDialog(null, "Invalid Input!");
            return null;
        }
        else if (timeQuantum == nonPositiveTimeQuantum){                     // if value less than 1 was entered
            JOptionPane.showMessageDialog(null, "Time Quantum Cannot be less than 1!");
            return null;
        }        
        else if (timeQuantum == cancelButtonPressed){                     // if user pressed cancel button
            return null;
        }
        else
            remainingTimeQuantum = timeQuantum;     // initialize remainingTimeQuantum
     
        while(!allProcessesFinished()){     // while there are still unfinished processes
            checkProcessesArrival(time);    // check if any process has arriced at current moment
            
            if (currentlyRunning != null){
                
                remainingTimeQuantum--;             // decrement remainingTimeQuantum every second
                currentlyRunning.remainingTime--;   // decrement remainingTime for currently running process every second
            }
                       
            if(currentlyRunning != null && currentlyRunning.remainingTime == 0){    // if remaining running time for a currently running process is 0,
                                                                                    // then this means that the process has finished  
                currentlyRunning.finishTime = time;         // set finish time to current time
                currentlyRunning.turnaround = currentlyRunning.finishTime - currentlyRunning.arrivalTime;   // compute turnaround time
                currentlyRunning.waitingTime = currentlyRunning.turnaround - currentlyRunning.burstTime;    // compute waiting time

                int currentlyRunningIdx = readyQueue.indexOf(currentlyRunning);  // get the index of currently running process
                readyQueue.remove(currentlyRunningIdx);                         // remove this process from the readyQueue
                currentlyRunning = null;

                if (readyQueue.size() != 0){                                // if there still some processes in readyQueue to run
                    currentlyRunning = readyQueue.get( currentlyRunningIdx % readyQueue.size() );   /* get the process that is just after the last process finished (FCFS).
                                                                                                       note that, this process's index = currentlyRunningIdx, because we 
                                                                                                       removed an element from the arrayList, and indices have been shifted left */
                    remainingTimeQuantum = timeQuantum;         // reset remainingTimeQuantum  

                    if(currentlyRunning != null && currentlyRunning.startTime == -1) // if the process has just started to run,
                        currentlyRunning.startTime = time;                           // then set its start time to current time  
                }  
            }
            
            if((currentlyRunning == null || remainingTimeQuantum == 0) && readyQueue.size() != 0){          // if there is no process running currently or a process has finished its timeQuantum
                i = (i + 1) % readyQueue.size(); 
                currentlyRunning = readyQueue.get(i); // get the next process in readyQueue
                /*
                note: this line instead of the previous two made a very big mistake
                    currentlyRunning = readyQueue.get( (i++) % readyQueue.size() );
                */
                remainingTimeQuantum = timeQuantum;                             // reset remainingTimeQuantum
                
                if(currentlyRunning != null && currentlyRunning.startTime == -1)// if the process has just started to run,
                    currentlyRunning.startTime = time;                          // then set its start time to current time
            }
            
            addToGanttChart(ganttChart, currentlyRunning);          // add currently running process at the current moment to Gantt Chart
            time++;         // increment time
        }
        
        return ganttChart;
    }    
    
    public ArrayList<Integer> RMS(){
        
        int time = 0;                           // current time
        Process currentlyRunning = null;        // the process that is running currently, initially null
        readyQueue = new ArrayList<Process>();                      // ready queue to add arrived processes that are ready to run
        ArrayList<Integer> ganttChart = new ArrayList<Integer>();   // Gantt Chart to show processes run-time
        Process leastPeriodProcess = null;                          // the process that has minimum period
        
        while(!allProcessesFinished()){     // while there are still unfinished processes

            checkProcessesArrivalAndPeriod(time);   // check if any process has arrived or repeated at cuurent time
             
            if(currentlyRunning != null)  // if a process is currently running
                currentlyRunning.remainingTime--;   // decrement its remaining running time
            
            if(currentlyRunning != null && currentlyRunning.remainingTime == 0){    /* if remaining running time for a process = 0, then either it finished all of its periods
                                                                                       or it finished one period */      
                currentlyRunning.remainingPeriods--;            // decrement number of periods for the process
                
                if (currentlyRunning.remainingPeriods == 0){    // if number of periods for the process = 0, then it finished
                   
                    currentlyRunning.finishTime = time;         // set finish time to current time
                    currentlyRunning.turnaround = currentlyRunning.finishTime - currentlyRunning.arrivalTime;   // compute turnaround time
                    currentlyRunning.waitingTime = currentlyRunning.turnaround - currentlyRunning.burstTime;    // compute waiting time

                    int currentlyRunningIdx = readyQueue.indexOf(currentlyRunning);  // get the index of currently running process
                    readyQueue.remove(currentlyRunningIdx);                         // remove this process from the readyQueue
                    currentlyRunning = null;   
                }
                
                /* starting another process, just at the same time the previous process has finished */
                leastPeriodProcess = findLeastPeriodProcess();      // find the process that has minimum period
                currentlyRunning = leastPeriodProcess;              // and then run it

                if(currentlyRunning != null && currentlyRunning.startTime == -1)    // if the process has just started, don't decrement its remaining running time value
                    currentlyRunning.startTime = time;
            }
             
            leastPeriodProcess = findLeastPeriodProcess();  // find the process that has minimum period
            currentlyRunning = leastPeriodProcess;                  // and then run it, preemiting the previous running process, if any

            addToGanttChart(ganttChart, currentlyRunning);          // add currently running process at the current moment to Gantt Chart
            
            if (deadlineMissed(currentlyRunning, time, ganttChart)){ // checking if the process missed its deadline
                JOptionPane.showMessageDialog(null, "Time = " + time + " ! Deadline for Process " + currentlyRunning.pid + " has been Missed! Terminating all Processes");
                return ganttChart;
            }
            
            if(currentlyRunning != null && currentlyRunning.startTime == -1) // if the process has just started to run,
                currentlyRunning.startTime = time;                           // then set its start time to current time 
            
            time++;         // increment time
        }
        
        return ganttChart;
    }

    public ArrayList<Integer> FCFS(){
        int time = 0;                          // current time
        Process currentlyRunning = null;       // the process that is running currently, initially null
        
        readyQueue = new ArrayList<Process>();                      // ready queue to add arrived processes that are ready to run
        ArrayList<Integer> ganttChart = new ArrayList<Integer>();   // Gantt Chart to show processes run-time                                                   // initialize remainingTimeQuantum
     
        while(!allProcessesFinished()){     // while there are still unfinished processes
            checkProcessesArrival(time);    // check if any process has arriced at current moment
            
            if(currentlyRunning == null){          // if there is no process running currently
                currentlyRunning = readyQueue.get(0);   // get the first process in readyQueue
                
                if(currentlyRunning != null)                // if the process has just started to run,
                    currentlyRunning.startTime = time;      // then set its start time to current time
            }
               
            if (currentlyRunning != null){
                
                if (time != currentlyRunning.startTime) // decrement remainingTime for currently running process every second
                    currentlyRunning.remainingTime--;   // except at the start second

                if(currentlyRunning != null && currentlyRunning.remainingTime == 0){    // if remaining running time for a currently running process is 0,
                                                                                        // then this means that the process has finished  
                    currentlyRunning.finishTime = time;         // set finish time to current time
                    currentlyRunning.turnaround = currentlyRunning.finishTime - currentlyRunning.arrivalTime;   // compute turnaround time
                    currentlyRunning.waitingTime = currentlyRunning.turnaround - currentlyRunning.burstTime;    // compute waiting time
                
                    readyQueue.remove(0);          // remove this process from the readyQueue
                    currentlyRunning = null;
                    
                    if (readyQueue.size() != 0)                 // if there still some processes in the readyQueue
                        currentlyRunning = readyQueue.get(0);   // get the first process in readyQueue
                
                    if(currentlyRunning != null)                // if the process has just started to run,
                        currentlyRunning.startTime = time;      // then set its start time to current time
                }
            }
            
            addToGanttChart(ganttChart, currentlyRunning);          // add currently running process at the current moment to Gantt Chart
            time++;         // increment time
        }
        
        return ganttChart;
    }
    
    public static Process findLeastBurstTimeProcess(){   // function to find the process that has minimum burst time
        int minBurstTime = Integer.MAX_VALUE;            // initilize minBurstTime to a very large number
        Process leastBurstTimeProcess = null;
        for (Process p : readyQueue) {
            if(p.burstTime < minBurstTime){         // if process p has less burst time than leastBurstTimeProcess
                leastBurstTimeProcess = p;          // make p the leastBurstTimeProcess           
                minBurstTime = p.burstTime;
            }
        }
        return leastBurstTimeProcess;       // return the leastBurstTimeProcess
    }
    
    public static Process findLeastRemainingTimeProcess(){  // function to find the process that has minimum remaining running time
        int minRemainingTime = Integer.MAX_VALUE;           // initilize minBurstTime to a very large number
        Process leastRemainingTimeProcess = null;
        for (Process p : readyQueue) {  
            if(p.remainingTime < minRemainingTime){     // if process p has less remaining running time than leastRemainingTimeProcess
                leastRemainingTimeProcess = p;          // make p the leastRemainingTimeProcess    
                minRemainingTime = p.remainingTime;
            }
        }
        return leastRemainingTimeProcess;       // return the leastRemainingTimeProcess
    }
    
    public static Process findLeastPeriodProcess(){     // function to find the process that has minimum period
        int minPeriod = Integer.MAX_VALUE;              // initilize minBurstTime to a very large number
        Process leastPeriodProcess = null;
        for (Process p : readyQueue) {
            if(p.remainingTime != 0 && p.interval < minPeriod){ // if process p has less period than leastPeriodProcess
                leastPeriodProcess = p;                         // make p the leastPeriodProcess 
                minPeriod = p.interval;
            }
        }
        return leastPeriodProcess;              // return the leastPeriodProcess
    }
    
    public boolean allProcessesFinished(){  // function to check if all process has finished or not
        for (Process p : processes) 
            if(p.finishTime == -1)          // if any process has finish time = -1, then it hasn't finished yet
                return false;
        return true;
    }
    
    public void checkProcessesArrival(int time){    // check if any process has arrived at the given time
        for (Process p : processes) 
            if(p.arrivalTime == time)   // if a process arrived at the given time
                readyQueue.add(p);      // then add it to the readyQueue
    }
    
    public void checkProcessesArrivalAndPeriod(int time){   // check if any process has arrived or repeated at the given time
        for (Process p : processes){ 
            if(p.arrivalTime == time)   // if a process arrived at the given time
                readyQueue.add(p);      // then add it to the readyQueue
            
            // if current time = the period of any process, then re-run this process
            if (time > p.arrivalTime && (time - p.arrivalTime) % p.interval == 0)
                p.remainingTime = p.burstTime;      // re-run the process
        } 
    }
    
    public boolean deadlineMissed (Process currentlyRunning, int time, ArrayList<Integer> ganttChart){
        
        int currentPeriod = -1;
        if (currentlyRunning != null)           // computing the period number that the currently running process is running in
            currentPeriod = currentlyRunning.repeat - currentlyRunning.remainingPeriods + 1;

        /* if the currently running process missed its deadline */
        if (currentlyRunning != null && time > currentlyRunning.deadline && time / currentlyRunning.deadline == currentPeriod)
            return true;         // deadline has been missed
        else
            return false;   // deadline has been missed yet
    }
    
    public int readTimeQuantum(){               // function to read timeQuantum from user and return it
        String timeQuantumString = JOptionPane.showInputDialog(null,"Enter Time Quantum:");
        int timeQuantum;
        
        if (timeQuantumString == null)          // if user pressed cancel button
            return cancelButtonPressed;
        else{
            try {
                timeQuantum = Integer.parseInt(timeQuantumString); // getting timeQuantum from user
                if (timeQuantum < 1)
                    return nonPositiveTimeQuantum;
            } catch (Exception e) {
                return invalidInput;
            }   
        }
        
        return timeQuantum;
    }
    
    public void addToGanttChart(ArrayList<Integer> ganttChart, Process currentlyRunning){   // function to add a process's pid to Gantt Chart
        if (currentlyRunning != null)               // if there is a process running at the time the function has called
            ganttChart.add(currentlyRunning.pid);   // add this process's pid to Gantt Chart
        else                                        
            ganttChart.add(-1);                     // add -1, indicating no process was running at this time
    }
    
    public void displayGanttChart(ArrayList<Integer> ganttChart) {  // function to display Gantt Chart
        
        for (int i = 0; i < ganttChart.size(); i++) 
            System.out.printf("%3d ",i);
 
        System.out.println();
        
        for (Integer integer : ganttChart)
            System.out.printf("%3d ", integer);
        
        System.out.println();   
        System.out.println(); 
    }
    
    public void printProcessesInfo(){   // function to display all processes info
        for (int i = 0; i < processes.size(); i++)
            System.out.println(processes.get(i).toString());
        
        System.out.println(); 
//        System.out.println("Average Turnaround Time = " + avgTurnaround() + " , Average Waiting Time = " + avgWaitingTime() + " , CPU Usage = " + cpuUsage() + "\n");
    }
    
    public void printAveragesAndCpuUsage(){
        System.out.println("Average Turnaround Time = " + avgTurnaround() + " , Average Waiting Time = " + avgWaitingTime() + " ,Overall CPU Usage = " + cpuUsage() + "\n");
    }
    
    public void resetAllProcesses(){    // function to reset all ran processes and re-initialize them    
        for (Process p : processes) 
            p.resetProcess();      
    }
    
    public double avgTurnaround(){      // function to compute the average turnaround time
        
        double avgTurnaround = 0;
        
        for (Process p : processes)
            avgTurnaround += p.turnaround;
        
        avgTurnaround /= processes.size();
        
        return avgTurnaround;
    }   
    
    public double avgWaitingTime(){     // function to compute the average waiting time
        
        double avgWaitingTime = 0;
        
        for (Process p : processes)
            avgWaitingTime += p.waitingTime;
        
        avgWaitingTime /= processes.size();
        
        return avgWaitingTime;
    }
    
    public int cpuUsage(){              // function to compute the total CPU Usage time
        
        int cpuUsage = 0;
        
        for (Process p : processes)
            cpuUsage += p.burstTime;
        
        return cpuUsage;        
    }
       
    public boolean readFile(String fileName){      // function to read processes data from the given input file, and save it in the arrayList processes
                                                   // returns true if data has read successfully, otherwise it returns false
        processes = new ArrayList<Process>();   
        try{
            File file = new File(fileName);
            Scanner input = new Scanner(file); 
            input.nextLine();                   // skipping first 2 lines in the input file
            input.nextLine();                   // which are table headers, and an horizontal line
            
            while(input.hasNextLine()){         // read file line by line until its end
                
                String line = input.nextLine();         // get a line
                line = line.replaceAll("[ \t]", "");    // remove spaces and tabs
                String tokens[] = line.split("[|]");    // split it using the de-limitor "|"
                
                /* convert data from String to int and save it */
                int pid = Integer.parseInt(tokens[0]);          
                int arrivalTime = Integer.parseInt(tokens[1]);  
                int burstTime = Integer.parseInt(tokens[2]);
                int repeat = Integer.parseInt(tokens[3]);
                int interval = Integer.parseInt(tokens[4]);
                int deadline = Integer.parseInt(tokens[5]);
                
                /* create a new process from the read data and add it immediately to the processes arrayList */
                processes.add(new Process(pid, arrivalTime, burstTime, repeat, interval, deadline));
              }
        }
        catch(Exception ex){                // if any problem occured while oppening or reading the file
            JOptionPane.showMessageDialog(null, "Error openning file: " + ex.getMessage()); // show an error message
            return false;
        }
        
        return true;
    }   
}