package Control_Thread;
import Basic_Structure.Kernel;
import Basic_Structure.Scheduler;
import  Core.CPU;
import Basic_Structure.Memory;
import Basic_Structure.PCB;
import Components.Instruction;
import Basic_Structure.Buffer;

import java.util.Objects;
//****************************************************
//输入中断线程：InputBlock_thread( )
//****************************************************

public class InputBlock_thread implements Runnable {

    private int timer = 2;

    @Override
    public void run() {
        System.out.println("==========================inputblockthread线程启动=========================");
        while (true) {
            Kernel.lock.lock();
            try {
                Kernel.inputCondition.await();
                System.out.println("====================ibt线程执行=======================");
                if(Kernel.curPro!=null)
                    Kernel.gui.showcurPro.setText(String.valueOf(Kernel.curPro.ProID));
                else
                    Kernel.gui.showcurPro.setText("null");
                INPUT();
                //阻塞到就绪==================================================
                if (Kernel.inputBlockQueue.peek() != null && Kernel.inputBlockQueue.peek().inputBlockQueue_time + 2 <= MYClock_thread.COUNTTIME) {
                    PCB outpcb = Kernel.inputBlockQueue.poll();
                    System.out.println("INPUT此时时钟" + MYClock_thread.COUNTTIME);
                    System.out.println(outpcb.ProID + "的第" + outpcb.PC + "条指令取出input阻塞队列");
//                    Kernel.gui.pstfield.append("时钟" + MYClock_thread.COUNTTIME+":"+outpcb.ProID + "的第" + outpcb.PC + "条指令取出input阻塞队列\n");
                    Kernel.memory.printMemoryMap();
                    Cache("V",outpcb);
                    Kernel.memory.printMemoryMap();
                    Kernel.cpu.CPU_REC(outpcb);
                    Kernel.cpu.PC+=1;
                    //更新IR
//                    Kernel.curPro=outpcb;
                    System.out.println("Kernel.cpu");
                    System.out.println(Kernel.cpu.PC);
                    System.out.println("Kernel.curPro");
//                    System.out.println(Kernel.curPro);
                    //更新IR
                    if(Kernel.cpu.PC<outpcb.instructions.size()) {
//                        System.out.println("没到这？？？？？？？？？？？？？？？？？？？？？？？？？");
                        Kernel.cpu.IR = outpcb.instructions.get(Kernel.cpu.PC);
                        System.out.println("更新后的指令"+Kernel.cpu.IR);
                    }
                    Kernel.cpu.CPU_PRO(outpcb);
                    if(!outpcb.isOver())
                    {
                        System.out.println("回去的进程Process ID: " + outpcb.ProID + ", Priority: " + outpcb.Priority + ", PC: " + outpcb.PC + ", PCStatue: " + outpcb.instructions.get(outpcb.PC).Instruc_State+", FlagIO: "+outpcb.IR.flag_IO+"\n");
                        outpcb.IR.flag_IO=true;
                    }
                    Scheduler.add(outpcb);
                    Kernel.gui.pstfield.append(MYClock_thread.COUNTTIME+":[重新进入就绪队列:"+outpcb.ProID+","+(outpcb.InstrucNum-outpcb.PC)+"]\n");
//                    if(Kernel.curPro==null)
//                    {
//                        Kernel.curPro=Scheduler.DJFK();
//                        if(Kernel.curPro!=null){
//                            Kernel.gui.showcurPro.setText(String.valueOf(Kernel.curPro.ProID));
//                        }
//                        else
//                            Kernel.gui.showcurPro.setText("null");
//                        System.out.println("DFJK了一个"+Kernel.curPro+"的进程到第"+Kernel.curPro.PC+"条了");
//                        if(Kernel.curPro!=null&&!Kernel.curPro.isOver()){
//                            System.out.println(Kernel.curPro.PC+" ");
//                            System.out.println(Kernel.curPro.instructions.get(Kernel.curPro.PC));
//                            Kernel.cpu.CPU_REC(Kernel.curPro);
//                        }
//                    }
                }
                System.out.println("======================打印inputblock阻塞队列===========================");
                Kernel.show_inputBlockQueue();
                Kernel.Printstatus();
                Kernel.outputCondition.signal();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                Kernel.lock.unlock();
            }
        }
    }
    //****************************************************
    //缓冲区操作函数：Cache( ),函数的输入输出变量可以自定义
    //****************************************************
    public void Cache(String PV,PCB outpcb)  //这玩意
    {
        if (Objects.equals(PV, "P"))
        {
            Kernel.buffer.P(Kernel.curPro);
            return;
        }
        else {
            Kernel.buffer.V(outpcb);
            return;
        }

    }
    //***********************************
    //输入处理函数：INPUT（）
    //***********************************
    public void INPUT()
    {
        if(Kernel.curPro!=null&&!Kernel.curPro.isOver()) {  //非空没结束
            if (Kernel.curPro.instructions.get(Kernel.curPro.PC).Instruc_State == 1 && Kernel.curPro.IR.flag_IO) {  //可以进来
                // 保存当前CPU现场状态
//                Kernel.cpu.CPU_PRO(Kernel.curPro);
                //进入阻塞队列==========================
                System.out.println(Kernel.curPro.ProID + "的第" + Kernel.curPro.PC + "条指令进入input阻塞队列");
                System.out.println("INPUT此时时钟" + MYClock_thread.COUNTTIME);
//                Kernel.gui.pstfield.append("时钟" + MYClock_thread.COUNTTIME+":"+Kernel.curPro.ProID + "的第" + Kernel.curPro.PC + "条指令进入input阻塞队列\n");
                Kernel.gui.pstfield.append(MYClock_thread.COUNTTIME+":[阻塞进程:input阻塞队列,"+Kernel.curPro.ProID+"]\n");
                Kernel.curPro.BqTimes1=MYClock_thread.COUNTTIME;

                // 写入
                Kernel.memory.printMemoryMap();
                Cache("P", null);
                Kernel.memory.printMemoryMap();
                Scheduler.remove_PCB(Kernel.curPro);
                Kernel.curPro.inputBlockQueue_time = MYClock_thread.COUNTTIME;  //记录当前时间
                Kernel.curPro.IR.flag_IO = false;  //不能再进来
                Kernel.inputBlockQueue.add(Kernel.curPro);  // 当前进程阻塞
                Kernel.curPro=null;
            }
        }
    }
}



