package Control_Thread;
import Basic_Structure.*;

import java.util.Objects;
//****************************************************
//屏幕输出：OutputBlock_thread( )
//****************************************************
public class OutputBlock_thread implements Runnable {
    @Override
    public void run() {
        System.out.println("========================outputblockthread线程启动=============================");
        while (true) {
            Kernel.lock.lock();
            try {
                Kernel.outputCondition.await();
                System.out.println("======================obt线程执行===========================");
                if(Kernel.curPro!=null)
                    Kernel.gui.showcurPro.setText(String.valueOf(Kernel.curPro.ProID));
                else
                    Kernel.gui.showcurPro.setText("null");
                OUTPUT();
                //阻塞到就绪
                if(Kernel.outputBlockQueue.peek()!=null&&Kernel.outputBlockQueue.peek().outputBlockQueue_time+2<=MYClock_thread.COUNTTIME){
                    PCB outpcb=Kernel.outputBlockQueue.poll();
                    System.out.println("OUTPUT此时时钟"+MYClock_thread.COUNTTIME);
                    System.out.println(outpcb.ProID+"的第"+outpcb.PC+"条指令取出output阻塞队列");
//                    Kernel.gui.pstfield.append(MYClock_thread.COUNTTIME+":["+outpcb.ProID+"的第"+outpcb.PC+"条指令取出output阻塞队列]\n");
                    Kernel.memory.printMemoryMap();
                    Cache("V",outpcb);
                    Kernel.memory.printMemoryMap();
                    Kernel.cpu.CPU_REC(outpcb);
                    Kernel.cpu.PC+=1;
//                    Kernel.curPro=outpcb;
                    System.out.println("Kernel.cpu");
                    System.out.println(Kernel.cpu.PC);
                    System.out.println("Kernel.curPro");
//                    System.out.println(Kernel.curPro);
                    //更新IR
                    if(Kernel.cpu.PC<outpcb.instructions.size()) {
//                        System.out.println("没到这？？？？？？？？？？？？？？？？？？？？？？？？？");
                        Kernel.cpu.IR = outpcb.instructions.get(Kernel.cpu.PC);
                    }
                    Kernel.cpu.CPU_PRO(outpcb);
                    if(!outpcb.isOver())
                    {
                        outpcb.IR.flag_IO=true;
                    }
                    Scheduler.add(outpcb);
                    Kernel.gui.pstfield.append(MYClock_thread.COUNTTIME+":[重新进入就绪队列:"+outpcb.ProID+","+(outpcb.InstrucNum-outpcb.PC)+"]\n");
//                    if(Kernel.curPro==null){
//                        Kernel.curPro=Scheduler.DJFK();
//                        if(Kernel.curPro!=null)
//                            Kernel.gui.showcurPro.setText(String.valueOf(Kernel.curPro.ProID));
//                        else
//                            Kernel.gui.showcurPro.setText("null");
//                        if(Kernel.curPro!=null&&!Kernel.curPro.isOver())
//                            Kernel.cpu.CPU_REC(Kernel.curPro);
//                    }
                }

                System.out.println("======================打印outputblock阻塞队列===========================");
                Kernel.show_outputBlockQueue();
                Kernel.Printstatus();
                // 唤醒read线程
                Kernel.readCondition.signal();
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
    //输出处理函数：OUTPUT（）
    //***********************************
    public void OUTPUT(){
        if(Kernel.curPro!=null&&!Kernel.curPro.isOver()) {
            System.out.println("当前CPU的状态:"+Kernel.cpu.ifInterrupted());
//            System.out.println("当前Kernel.curPro的状态:"+Kernel.curPro.instructions.get(Kernel.curPro.PC).Instruc_State);
//            if (Kernel.cpu.ifInterrupted() == 2 && Kernel.curPro.IR.flag_IO) {
            if (Kernel.curPro.instructions.get(Kernel.curPro.PC).Instruc_State == 2 && Kernel.curPro.IR.flag_IO) {
                // 保存当前CPU现场状态
//                Kernel.cpu.CPU_PRO(Kernel.curPro);
                //==========================
                System.out.println(Kernel.curPro.ProID + "的第" + Kernel.curPro.PC + "条指令进入output阻塞队列");
                System.out.println("OUTPUT此时时钟" + MYClock_thread.COUNTTIME);
//                Kernel.gui.pstfield.append("时钟" + MYClock_thread.COUNTTIME+":"+Kernel.curPro.ProID + "的第" + Kernel.curPro.PC + "条指令进入output阻塞队列\n");
                Kernel.gui.pstfield.append(MYClock_thread.COUNTTIME+":[阻塞进程:output阻塞队列,"+Kernel.curPro.ProID+"]\n");
                Kernel.curPro.BqTimes2=MYClock_thread.COUNTTIME;

                // 写入
                Kernel.memory.printMemoryMap();
                Cache("P", null);
                Kernel.memory.printMemoryMap();
                Scheduler.remove_PCB(Kernel.curPro);
                Kernel.curPro.outputBlockQueue_time = MYClock_thread.COUNTTIME;
                Kernel.curPro.IR.flag_IO = false;
                Kernel.outputBlockQueue.add(Kernel.curPro);  // 当前进程阻塞
                Kernel.curPro=null;

            }
        }

    }
}