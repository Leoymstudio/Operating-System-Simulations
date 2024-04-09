package Basic_Structure;

import Components.Job;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.Iterator;
import Control_Thread.*;
import Core.CPU;
import GUI.GUI;
import Basic_Structure.Buffer;
import Basic_Structure.HardDisk;

import javax.swing.*;
//****************************************************
//内核Kernel
//****************************************************
public class Kernel {
    // 当前并发度
    public static int currency=0;
    //最高并发度
    public static final int MAX_CURRENCY=12;
    //正在执行线程
    public static PCB curPro;

    public static ReentrantLock lock =new ReentrantLock();

    // 某个条件唤醒
    public static Condition clkCondition = lock.newCondition();
    public static Condition jitCondition = lock.newCondition();
    public static Condition pstCondition = lock.newCondition();
    public static Condition inputCondition = lock.newCondition();
//    public static Condition bufCondition = lock.newCondition();
    public static Condition outputCondition = lock.newCondition();
    public static Condition readCondition = lock.newCondition();

    public static PriorityQueue<Job> HB=new PriorityQueue<>();

    public static ConcurrentLinkedQueue<PCB> inputBlockQueue = new ConcurrentLinkedQueue<>();  //阻塞队列1
    public static ConcurrentLinkedQueue<PCB> outputBlockQueue = new ConcurrentLinkedQueue<>();  //阻塞队列2
    public static ConcurrentLinkedQueue<PCB> readBlockQueue = new ConcurrentLinkedQueue<>();  //阻塞队列3

    // 声明需要的对象
    public static Memory memory;
    public static MMU mmu;
    public static HardDisk hardDisk;
    public static CPU cpu;
    public static Buffer buffer;
    public static MYClock_thread clockThread;
    public static JobIn_thread jobinThread;
    public static ProcessScheduling_thread processSchedulingThread;

    public static Integer Job_count;

    public static Integer all_job_time;





    // Kernel构造函数，进行对象的初始化
    public Kernel(){
        memory = new Memory();
        mmu=new MMU();
        buffer = new Buffer();
        cpu = new CPU();
        clockThread = new MYClock_thread();
        hardDisk = new HardDisk();
        jobinThread = new JobIn_thread();
        processSchedulingThread = new ProcessScheduling_thread();
    }

    // 用来启动线程的方法
    public void startSystem(){
        new Thread(new MYClock_thread()).start();
        new Thread(new JobIn_thread()).start();
        new Thread(new ProcessScheduling_thread()).start();
        new Thread(new InputBlock_thread()).start();
        new Thread(new OutputBlock_thread()).start();
        new Thread(new ReadBlock_thread()).start();
    }
    //****************************
    //阻塞队列信息列表 1（包括：位置编号（BqNum1）、进程进入键盘输入阻塞队列时间（ BqTimes1））；
    //阻塞队列信息列表 2（包括：位置编号（BqNum2）、进程进入显示器输出阻塞队列时间（ BqTimes2））；
    //在就绪队列信息列表（包括：位置编号（RqNum）、进入就绪队列时间（RqTimes））
    //****************************
    public static void show_inputBlockQueue()
    {
        StringBuilder sb = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();
        int count = 1;
        Iterator<PCB> iterator = inputBlockQueue.iterator();
        while (iterator.hasNext())
        {
            PCB pcb = iterator.next();
            System.out.println("BqNum1: " + count + ", BqTimes1: " + pcb.BqTimes1+", ProID: "+pcb.ProID+"; ");
            sb.append("BqNum1: ").append(count).append(", BqTimes1: ").append(pcb.BqTimes1).append(", ProID: ").append(pcb.ProID).append(",ST").append(pcb.IR.Instruc_State).append("\n");
            sb2.append(pcb.BqTimes1).append(",").append(pcb.ProID).append("/");

            count++;
        }
        Kernel.gui.bQ1field.setText(sb.toString());
        Kernel.gui.BB1.append(sb2);
    }
    public static void show_outputBlockQueue()
    {
        StringBuilder sb = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();
        int count = 1;
        Iterator<PCB> iterator = outputBlockQueue.iterator();
        while (iterator.hasNext())
        {
            PCB pcb = iterator.next();
            System.out.println("BqNum2: " + count + ", BqTimes2: " + pcb.BqTimes2+", ProID: "+pcb.ProID+"; ");
            sb.append("BqNum2: ").append(count).append(", BqTimes2: ").append(pcb.BqTimes2).append(", ProID: ").append(pcb.ProID).append(",ST").append(pcb.IR.Instruc_State).append("\n");
            sb2.append(pcb.BqTimes2).append(",").append(pcb.ProID).append("/");
            count++;
        }
        Kernel.gui.bQ2field.setText(sb.toString());
        Kernel.gui.BB2.append(sb2);
    }
    public static void show_readBlockQueue()
    {
        StringBuilder sb = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();
        int count = 1;
        Iterator<PCB> iterator = readBlockQueue.iterator();
        while (iterator.hasNext())
        {
            PCB pcb = iterator.next();
            System.out.println("BqNum3: " + count + ", BqTimes3: " + pcb.BqTimes3+", ProID: "+pcb.ProID+"; ");
            sb.append("BqNum3: ").append(count).append(", BqTimes3: ").append(pcb.BqTimes3).append(", ProID: ").append(pcb.ProID).append(",ST").append(pcb.IR.Instruc_State).append("\n");
            sb2.append(pcb.BqTimes3).append(",").append(pcb.ProID).append("/");
            count++;
        }
//        System.out.println("大哥能不能出来啊?");
//        System.out.println(sb);
        Kernel.gui.bQ3field.setText(sb.toString());
        Kernel.gui.BB3.append(sb2);
    }
    public static String getBB(String sb, StringBuilder BB) {
        return sb + BB.toString() + "]";
    }
    public static void Printstatus() {
        StringBuilder sta = new StringBuilder();

        sta.append(getBB("BB1:[阻塞队列1,键盘输入:", Kernel.gui.BB1)).append("\n");
        sta.append(getBB("BB2:[阻塞队列2,屏幕显示:", Kernel.gui.BB2)).append("\n");
        sta.append(getBB("BB4:[阻塞队列3,读磁盘数据:", Kernel.gui.BB3)).append("\n");
        Kernel.gui.statusfield.setText(sta.toString());
    }
    public static GUI gui = new GUI();
    public static void startGUI()
    {
        JFrame frame = new JFrame("操作系统");
        frame.setContentPane(gui.panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 720);
        frame.setVisible(true);
    }
}






















