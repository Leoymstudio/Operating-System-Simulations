package Control_Thread;
//import Basic_structure.Jobs;
import Basic_Structure.Kernel;
import Basic_Structure.PCB;
import Basic_Structure.Scheduler;
import Components.Job;
import Basic_Structure.HardDisk;
import java.util.Iterator;
import Basic_Structure.MMU;
//****************************************************
//作业请求查询线程：JobIn_thread( )
//****************************************************
public class JobIn_thread implements Runnable {
    public static int jobsID = 0;

    public void run() {
        System.out.println("=========================JobIn线程启动===========================");
        while (true) {
            Kernel.lock.lock();
            try {
                Kernel.jitCondition.await();//执行作业请求判读
                System.out.println("===========================JobIn线程=============================");
                if(Kernel.curPro!=null)
                    Kernel.gui.showcurPro.setText(String.valueOf(Kernel.curPro.ProID));
                else
                    Kernel.gui.showcurPro.setText("null");
                if (MYClock_thread.getTime() % 5 == 0) {
                    CheckJob();
                    System.out.println("check job结束");
                }

                // 作业后备队列打印信息
                System.out.println("作业后备队列打印信息");
                job_Print();

                //创建进程
                CreatePro();

                // 唤醒pst线程，并询jit线程等待
                Kernel.pstCondition.signal();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                Kernel.lock.unlock();
            }
        }
    }

    public static void job_Print() {
//      Kernel.gui.HBfield.setText("");
        StringBuilder sb = new StringBuilder();
        System.out.println("id+time+priority+instructnum+sum\n");
        for (Job jobs : Kernel.HB) {  //遍历后备队列
//            System.out.println(jobs.JobsID+ "\t " + + jobs.InTimes+ "\t "+jobs.Priority+"\t "+ jobs.InstrucNum+"\n"+jobs.TotalSum+"\t");
//            String jobInfo=jobs.JobsID+ "\t " + + jobs.InTimes+ "\t "+jobs.Priority+"\t "+ jobs.InstrucNum+"\n"+jobs.TotalSum+"\t";
            String jobInfo = MYClock_thread.getTime() + ":[新增作业:" +
                    jobs.JobsID + ","
                    + jobs.InTimes + ","
                    + jobs.InstrucNum + "]";
            sb.append(jobInfo).append("\n");
        }
        Kernel.gui.HBfield.setText(sb.toString());

    }

//    // 作业请求判读函数
//    // 功能:利用COUNTTINE变量判断并发作业请求文件是否有新进程请求，如果有，进入后备队列，如果没有，空操作;
//    public void CheckJob() {
//        // 当作业暂存列表非空，队首元素的进入时间小于当前线程时间
//        while (!Input.tmpJobsList.isEmpty() && Input.tmpJobsList.peek().jcb.InTimes <= Clock_thread.getTime()) {
//            // 0:[新增作业:作业编号,请求时间,指令数量]
//            GUI.GUI.text1_print(clock_thread.getTime() + ":[新建作业:" +
//                    Input.tmpJobsList.peek().jcb.JobsID + ","
//                    + Input.tmpjobslist.peek().jcb.InTimes + ","
//                    + Input.tmpJobsList.peek().jcb.InsNum + "]");
//            //加入作业后备队列(外存)
//            kernel.JobQueue.add(Input.tmpJobslist.poLL());
//        }
//    }

    //*********************************
    //作业请求判读函数，名称：CheckJob（）
    //*********************************
    public void CheckJob() {
//        Kernel.gui.HBfield.setText("");
        StringBuilder sb = new StringBuilder();
        while (!HardDisk.jobFallbackQueue.isEmpty() && HardDisk.jobFallbackQueue.peek().InTimes <= MYClock_thread.getTime()) {

            // 0:[新增作业:作业编号,请求时间,指令数量]
            String jobInfo = MYClock_thread.getTime() + ":[新建作业:" +
                    HardDisk.jobFallbackQueue.peek().JobsID + ","
                    + HardDisk.jobFallbackQueue.peek().InTimes + ","
                    + HardDisk.jobFallbackQueue.peek().InstrucNum + "]";
            System.out.println(jobInfo);
            sb.append(jobInfo).append("\n"); // 将作业信息累加到StringBuilder中
            // 从HardDisk类的jobFallbackQueue取出作业，加入Kernel类的JobQueue后备队列（外存）
            JTYXJ();
        }
//        Kernel.gui.HBfield.setText(sb.toString());
//        Kernel.gui.HBarea.append(sb.toString());
//        job_Print();
    }

    //检查是否允许作业创建进程
    public void CreatePro() {
        StringBuilder sb = new StringBuilder();
        //当前系统并发度小于最大并发度,作业后备队列非空/就绪队列中的进程数目一定小于最大并发度
        while (Kernel.currency < Kernel.MAX_CURRENCY && !Kernel.HB.isEmpty()) {
            CheckDeathlock();
            if (Kernel.HB.peek() != null && Kernel.memory.allocateMemory(Kernel.HB.peek().JobsID, Kernel.HB.peek().TotalSum)) {
                Kernel.memory.printMemoryMap();
                System.out.println("创建新进程--------------");
                PCB pcb = new PCB(Kernel.HB.poll());

                pcb.PCBTimes = MYClock_thread.COUNTTIME;
                Kernel.gui.pstfield.append(MYClock_thread.COUNTTIME + ":[创建进程:" + pcb.ProID + "," + Kernel.memory.showstartIndex + "," + pcb.TotalSum + "]\n");
                Kernel.mmu.outmmu(MYClock_thread.COUNTTIME + ":[创建进程:" + pcb.ProID + "," + Kernel.memory.showstartIndex + "," + pcb.TotalSum + "]\n");
                pcb.startMem = Kernel.memory.showstartIndex;
                //加入就绪队列
                Scheduler.add(pcb);
                Kernel.gui.pstfield.append(MYClock_thread.COUNTTIME + ":[进入就绪队列:" + pcb.ProID + "," + (pcb.InstrucNum - pcb.PC) + "]\n");
                System.out.println("创建新进程结束--------------打印就绪队列");
                Scheduler.readyQueuePrint();
            }
            else
                break;
        }
    }
    //********************
    //用 JTYXJ（静态优先级）算法完成机器调度所有作业
    //********************
    private void JTYXJ(){
        Kernel.HB.add(HardDisk.jobFallbackQueue.poll());
    }
    public void CheckDeathlock(){  //
        if (Kernel.HB.peek() != null && Kernel.HB.peek().TotalSum > 120) {
            Kernel.gui.pstfield.append(MYClock_thread.COUNTTIME+":[出现死锁:"+Kernel.HB.peek().JobsID+"+"+Kernel.HB.peek().TotalSum+"]\n");
            Kernel.gui.pstfield.append(MYClock_thread.COUNTTIME+":[解除死锁:"+Kernel.HB.peek().JobsID+"+"+Kernel.HB.peek().TotalSum+"]\n");
            Kernel.HB.poll();  //检测死锁
        }
    }
}
