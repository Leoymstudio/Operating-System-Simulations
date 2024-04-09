package Control_Thread;
import Basic_Structure.Kernel;
import Basic_Structure.PCB;
import Components.Instruction;
import Components.Job;
import Core.CPU;
//import java.awt.image.Kernel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.locks.ReentrantLock;

import Basic_Structure.Scheduler;

import static Basic_Structure.Scheduler.queues;

//*********************************
//进程调度算法线程类，名称：ProcessScheduling_thread
//***************************************
public class ProcessScheduling_thread implements Runnable {
    public void run() {
        // 初始化三级队列
        queues = new ArrayList[3];
        for (int i = 0; i < 3; i++) {
            queues[i] = new ArrayList<>();
        }
        Scheduler.curTimeSlice=1;// 初始化当前时间片
        System.out.println("======================ProcessScheduling_thread启动=============================");


        while (true) {
            Kernel.lock.lock();
            try {
                Kernel.pstCondition.await();
                if(Kernel.curPro!=null)
                    Kernel.gui.showcurPro.setText(String.valueOf(Kernel.curPro.ProID));
                else
                    Kernel.gui.showcurPro.setText("null");
                //检查进程调度
                System.out.println("调度入口1");
                SetSchedulingTime();

                Kernel.inputCondition.signal();
                Kernel.pstCondition.await();
                if(Kernel.curPro != null )
                    System.out.println("Kernel.curPro"+Kernel.curPro+"Kernel.curPro.IR"+Kernel.curPro.IR+"Kernel.curPro.IR.flag_IO"+Kernel.curPro.IR.flag_IO);

                while(Kernel.curPro != null &&Kernel.curPro.IR.flag_IO){
                    if(Kernel.curPro.IR.Instruc_State==0&&Kernel.curPro.cur_Instruc_rest_time != 0|| CPU.isCloseInterrupted == 0)
                        break;
                    Kernel.cpu.clearInterrupted();//isCloseInterrupted=0;
//                    Kernel.curPro=null;
                    System.out.println("调度入口2");
                    //检查进程调度
                    SetSchedulingTime();

                    Kernel.inputCondition.signal();
                    Kernel.pstCondition.await();
                }

                //死锁判断===============================================================


                //===============================================================================

                System.out.println("====================processschedule执行结束，去到下一个时钟周期=====================");
                if(Kernel.buffer.isBufferEmpty())
                    Kernel.gui.bufferfield.append(MYClock_thread.COUNTTIME+":[缓冲区无进程]\n");

                //时钟线程启动
                Kernel.clkCondition.signal();

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                Kernel.lock.unlock();
            }
        }
    }

    //******************************************
    //调度时长设置函数，名称：SetSchedulingTime（）
    //******************************************
    public static void SetSchedulingTime( ) {
        System.out.println("Kernel.curPro"+Kernel.curPro);
        if (Kernel.curPro == null) {
            PCB pcb=Scheduler.DJFK();
            System.out.println("DJFK.curPro"+pcb);
            if(pcb==null){
                Kernel.cpu.clearPSW();
                System.out.println("cpu空闲");
                Kernel.gui.pstfield.append(MYClock_thread.COUNTTIME+":[CPU空闲]\n");
            }
            else{
                Kernel.curPro=pcb;
                System.out.println("pcb.PC "+pcb.PC);
                pcb.IR.flag_IO=true;
                if(!pcb.isOver())
                    Kernel.cpu.CPU_REC(pcb);
//                Kernel.curPro.RunTimes.add(MYClock_thread.getTime());
            }
        }
        if(Kernel.curPro!=null)
            Kernel.gui.showcurPro.setText(String.valueOf(Kernel.curPro.ProID));
        else
            Kernel.gui.showcurPro.setText("null");
        // 打印就绪队列信息
        Scheduler.readyQueuePrint();

        // 打印当前进程信息
        if(Kernel.curPro!=null){
            System.out.println("当前进程："+ Kernel.curPro.ProID+" 当前所在的时间片："+Kernel.curPro.curTimeSlice+"当前指令"+Kernel.curPro.IR);
            Scheduler.curTimeSlice = Kernel.curPro.curTimeSlice;
            System.out.println("cpu1");
            //CPU执行指令
            Kernel.cpu.execute();

            if((Kernel.cpu.ifInterrupted() == 1||Kernel.cpu.ifInterrupted() == 2 || Kernel.cpu.ifInterrupted()==3)&&Kernel.curPro.IR.flag_IO){
                System.out.println("不是0号指令");
            }
            else{
                //如果时间片用完,并且当前进程没有结束
                if(Kernel.curPro.cur_rest_time == 0 && !Kernel.curPro.isOver()&& CPU.isCloseInterrupted !=0){
                    //正常调度
                    System.out.println("进程"+Kernel.curPro.ProID);
                    System.out.println("时间片用完,并且当前进程没有结束，正常调度新进程");

//                    Kernel.curPro.RunTimes.add(MYClock_thread.getTime()+1);//调度新进程，记录上一个进程结束时间,+1是因为这个时候time没有加


                    Processscheduling();
                } else if (Kernel.curPro.curTimeSlice!=1&&Kernel.curPro.cur_rest_time != 0&& CPU.isCloseInterrupted ==0&&!Kernel.curPro.isOver()) {
                    //0型指令在二级和三级队列里没执行完，跳到下一个队列
                    System.out.println("进这了-------对我新写的-----------");
                    //此处应该执行指令调度,但调度完的指令不马上运行
                    Scheduler.add_index(Kernel.curPro,Kernel.curPro.curTimeSlice);
                    Kernel.gui.pstfield.append(MYClock_thread.COUNTTIME+":[重新进入就绪队列:"+Kernel.curPro.ProID+","+(Kernel.curPro.InstrucNum-Kernel.curPro.PC)+"]\n");
                    System.out.println(Kernel.curPro.ProID+"在clock="+MYClock_thread.getTime()+"重新加入就绪队列\n"+"当前Kernel.curPro.PC:"+Kernel.curPro.PC);
                    Kernel.curPro=null;

                } else if (CPU.isCloseInterrupted ==0&&Kernel.curPro.curTimeSlice==1&&Kernel.curPro.cur_rest_time == 0&&!Kernel.curPro.isOver()) {
                    //0型指令在一级队列，刚好用完时间片，等待调度
                    System.out.println("进这里吧好好好-------对对对-----------");
                    Scheduler.add_index(Kernel.curPro,Kernel.curPro.curTimeSlice);
                    Kernel.gui.pstfield.append(MYClock_thread.COUNTTIME+":[重新进入就绪队列:"+Kernel.curPro.ProID+","+(Kernel.curPro.InstrucNum-Kernel.curPro.PC)+"]\n");
                    System.out.println(Kernel.curPro.ProID+"在clock="+MYClock_thread.getTime()+"重新加入就绪队列\n"+"当前Kernel.curPro.PC:"+Kernel.curPro.PC);
                    Kernel.curPro=null;
                }

                //如果进程指令执行完,没有使用时间片,while怕新调的进程还是执行完了并且没有使用时间片
                while(Kernel.curPro != null && Kernel.curPro.isOver() && Kernel.curPro.flag_over_or_not){
                    //移除当前进程，并且重新调度一个并执行
                    System.out.println("进程指令执行完,没有使用时间片");

//                    Kernel.curPro.RunTimes.add(MYClock_thread.getTime()+1);//记录结束时间

                    Kernel.curPro.EndTimes=MYClock_thread.getTime()-1;
//                    Kernel.curPro.TurnTimes=Kernel.curPro.EndTimes-Kernel.curPro.ArriveTime;
//                    Kernel.curPro.ExcuteTimes=Kernel.curPro.EndTimes-Kernel.curPro.InTimes;

                    Kernel.Job_count-=1;
                    if(Kernel.Job_count==0){
                        Kernel.all_job_time=MYClock_thread.getTime()+1;
                        Kernel.gui.pstfield.append(MYClock_thread.COUNTTIME+":[终止:"+Kernel.curPro.ProID+"]\n");
                    }

                    //-----------------------------------------
                    System.out.println("进程"+Kernel.curPro.ProID+"结束了,时间序列是：");
//                    Kernel.gui.fQfield.append("进程"+Kernel.curPro.ProID+"结束了");
//                    Kernel.gui.fQfield.append(Kernel.curPro.InTimes+","+Kernel.curPro.EndTimes+","+Kernel.curPro.PCBTimes+","+(Kernel.curPro.EndTimes-Kernel.curPro.InTimes)+","+(Kernel.curPro.EndTimes-Kernel.curPro.ExcuteTimes)+"\n");
                    Kernel.gui.fQfield.append(MYClock_thread.COUNTTIME+":["+Kernel.curPro.ProID+":"+Kernel.curPro.InTimes+"+"+Kernel.curPro.PCBTimes+"+"+(Kernel.curPro.EndTimes-Kernel.curPro.PCBTimes)+"]\n");
//                    for(int i:Kernel.curPro.RunTimes){
//                        System.out.println(i+" ");
//                    }
                    System.out.println("\n");
                    System.out.println("进程"+Kernel.curPro.ProID+"的到达时间是："+Kernel.curPro.InTimes);
                    System.out.println("进程"+Kernel.curPro.ProID+"的结束时间是："+Kernel.curPro.EndTimes);
                    System.out.println("进程"+Kernel.curPro.ProID+"的创建PCB时间是："+Kernel.curPro.PCBTimes);
                    System.out.println("进程"+Kernel.curPro.ProID+"的周转时间是："+(Kernel.curPro.EndTimes-Kernel.curPro.InTimes));
                    System.out.println("进程"+Kernel.curPro.ProID+"的执行时间是："+(Kernel.curPro.EndTimes-Kernel.curPro.PCBTimes));

                    //-----------------------------------------

                    Kernel.memory.deallocateMemory(Kernel.curPro.ProID);
                    Kernel.memory.printMemoryMap();
                    Scheduler.remove_PCB(Kernel.curPro);
                    System.out.print(Kernel.curPro.ProID+"is over\n");
                    Kernel.curPro=null;

                    Processscheduling();
                    System.out.println("cpu2");

                    Kernel.cpu.execute();
                }

                //如果进程指令执行完，使用了时间片
                if(Kernel.curPro != null && Kernel.curPro.isOver()&& !Kernel.curPro.flag_over_or_not){
                    //移除当前进程，正常调度
                    System.out.println("进程指令执行完，使用了时间片");

//                    Kernel.curPro.RunTimes.add(MYClock_thread.getTime()+1);//记录结束时间

                    Kernel.curPro.EndTimes=MYClock_thread.getTime()-1;

//                    Kernel.curPro.TurnTimes=Kernel.curPro.EndTimes-Kernel.curPro.ArriveTime;
//                    Kernel.curPro.ExcuteTimes=Kernel.curPro.EndTimes-Kernel.curPro.InTimes;

                    Kernel.Job_count-=1;
                    if(Kernel.Job_count==0){
                        Kernel.all_job_time=MYClock_thread.getTime()+1;
                    }

                    //-----------------------------------------
                    System.out.println("进程"+Kernel.curPro.ProID+"结束了,时间序列是：");
//                    Kernel.gui.fQfield.append("进程"+Kernel.curPro.ProID+"结束了");
//                    Kernel.gui.fQfield.append(Kernel.curPro.InTimes+","+Kernel.curPro.EndTimes+","+Kernel.curPro.PCBTimes+","+(Kernel.curPro.EndTimes-Kernel.curPro.InTimes)+","+(Kernel.curPro.EndTimes-Kernel.curPro.ExcuteTimes)+"\n");
                    Kernel.gui.fQfield.append(MYClock_thread.COUNTTIME+":["+Kernel.curPro.ProID+":"+Kernel.curPro.InTimes+"+"+Kernel.curPro.PCBTimes+"+"+(Kernel.curPro.EndTimes-Kernel.curPro.PCBTimes)+"]\n");
//                    for(int i:Kernel.curPro.RunTimes){
//                        System.out.print(i+" ");
//                    }
                    System.out.println("\n");
                    System.out.println("进程"+Kernel.curPro.ProID+"的到达时间是："+Kernel.curPro.InTimes);
                    System.out.println("进程"+Kernel.curPro.ProID+"的结束时间是："+Kernel.curPro.EndTimes);
                    System.out.println("进程"+Kernel.curPro.ProID+"的创建PCB时间是："+Kernel.curPro.PCBTimes);
                    System.out.println("进程"+Kernel.curPro.ProID+"的周转时间是："+(Kernel.curPro.EndTimes-Kernel.curPro.InTimes));
                    System.out.println("进程"+Kernel.curPro.ProID+"的执行时间是："+(Kernel.curPro.EndTimes-Kernel.curPro.PCBTimes));
                    //-----------------------------------------

                    Kernel.memory.deallocateMemory(Kernel.curPro.ProID);
                    Kernel.memory.printMemoryMap();
                    Scheduler.remove_PCB(Kernel.curPro);
                    System.out.println(Kernel.curPro.ProID+"is over\n");
                    Kernel.curPro=null;

                    Processscheduling();
                }
                if(Kernel.curPro!=null)
                    Kernel.gui.showcurPro.setText(String.valueOf(Kernel.curPro.ProID));
                else
                    Kernel.gui.showcurPro.setText("null");

            }
        }
    }
    //********************************************
    //进程调度函数，名称：ProcessScheduling（）
    //********************************************
    public static void Processscheduling() {
        // 处理机上内容非空
        if (Kernel.curPro != null){
            //保护现场
//            Kernel.cpu.CPU_PRO(Kernel.curPro);
            //下处理机，重新加入就绪队列
            Scheduler.add_index(Kernel.curPro,Kernel.curPro.curTimeSlice);
            Kernel.gui.pstfield.append(MYClock_thread.COUNTTIME+":[进入就绪队列:"+Kernel.curPro.ProID+","+(Kernel.curPro.InstrucNum-Kernel.curPro.PC)+"]\n");
            System.out.println(Kernel.curPro.ProID+"在clock="+MYClock_thread.getTime()+"重新加入就绪队列\n"+"当前Kernel.curPro.PC:"+Kernel.curPro.PC);
        }
        // 处理机上内容为空, 或者不为空并且重新加入就绪队列
        Kernel.curPro=Scheduler.DJFK();
        System.out.println("DJFK了这样的一个叫："+Kernel.curPro+"的进程");

        if(Kernel.curPro!=null&&!Kernel.curPro.isOver()){
            Kernel.cpu.CPU_REC(Kernel.curPro);

//            Kernel.curPro.RunTimes.add(MYClock_thread.getTime()+1);//被调度记录开始时间

            Scheduler.curTimeSlice=Kernel.curPro.curTimeSlice;
            Kernel.curPro.cur_rest_time=Kernel.curPro.curTimeSlice;
        }
    }

}




