package Control_Thread;

import Basic_Structure.Kernel;
//import GUI.GUI.GUI.GUI;

//***************************
//MYClock_thread
//***************************
public class MYClock_thread implements Runnable {
    //***************************
    //该类中设计一个共享属性变量，名称：COUNTTIME，整型，单位：秒（s）
    //***************************
    public static int COUNTTIME = 0; // 每1秒激活该线程计时，COUNTTIME+1操作
    public static int simulationSecond = 1000; // 仿真1s 时间,方便后期在前端加速
    public static boolean stop;

    public void run() {
        stop=false;
        System.out.println("=====================Clock线程启动=========================");
//        Kernel.memory.printMemoryMap();
        //线程休眠?
        try {
            Thread.sleep(1000);
        }catch (InterruptedException e) {
            e.printStackTrace();
        }

        while (true) {
            //当前线程获得锁
            Kernel.lock.lock();
            try {
                if(!stop)
                {
                    //打印系统时钟---------------------------------------------------
                    System.out.println("**************************clock is "+COUNTTIME+"************************************************************\n");
                    Kernel.gui.timelable.setText(String.valueOf(COUNTTIME));
                    if(Kernel.curPro!=null)
                        Kernel.gui.showcurPro.setText(String.valueOf(Kernel.curPro.ProID));
                    else
                        Kernel.gui.showcurPro.setText("null");
                    System.out.println("current PCB"+Kernel.curPro);
//                GUI.GUI.Label103.setText(String.valueOf(COUNTTIME));
                    //唤醒JIT线程
                    Kernel.jitCondition.signal();
                    //clk线程在此等待被唤醒
                    Kernel.clkCondition.await();
                    //模拟时钟中断
                    TIMECOUNT();
                }
//                if (GUI.GUI.suspendflag) {
//                    Kernel.clkCondition.await();
//                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                Kernel.lock.unlock();
            }
        }
    }

    //获取系统时钟
    public static int getTime() {
        return COUNTTIME;
    }

    //模拟时钟中断,可以通过simulationSecond加速时间

    //***************************
    //TIME-COUNT（）：通过该函数对 COUNTTIME 变量计时操作。此变量为临界资
    //源，需要互斥访问；因此，操作该变量时可以用 JAVA 加锁操作；
    //***************************
    public void TIMECOUNT() {
        try {
            COUNTTIME++;
//            System.out. println("clock"+COUNTTIME);
            Thread.sleep(simulationSecond);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void clockstop()
    {
        stop=true;
    }
    public static void clockawake()
    {
        stop=false;
    }

}