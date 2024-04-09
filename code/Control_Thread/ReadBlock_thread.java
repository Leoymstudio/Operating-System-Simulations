package Control_Thread;
import Basic_Structure.*;

import java.util.Objects;
//****************************************************
//读磁盘线程：ReadBlock_thread( )
//****************************************************
public class ReadBlock_thread implements Runnable {
    @Override
    public void run() {
        System.out.println("========================readblockthread线程启动=============================");
        while (true) {
            Kernel.lock.lock();
            try {
                Kernel.readCondition.await();
                System.out.println("======================rbt线程执行===========================");
                if(Kernel.curPro!=null)
                    Kernel.gui.showcurPro.setText(String.valueOf(Kernel.curPro.ProID));
                else
                    Kernel.gui.showcurPro.setText("null");
                Read();
                //阻塞到就绪
                if(Kernel.readBlockQueue.peek()!=null&&Kernel.readBlockQueue.peek().readBlockQueue_time+3<=MYClock_thread.COUNTTIME){
                    PCB outpcb=Kernel.readBlockQueue.poll();
                    System.out.println("ReadBlock此时时钟"+MYClock_thread.COUNTTIME);
                    System.out.println(outpcb.ProID+"的第"+outpcb.PC+"条指令取出Read阻塞队列");
//                    Kernel.gui.pstfield.append("时钟"+MYClock_thread.COUNTTIME+":"+outpcb.ProID+"的第"+outpcb.PC+"条指令取出Read阻塞队列\n");
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
//                    if(Kernel.curPro==null)
//                    {
//                        Kernel.curPro=Scheduler.DJFK();
//                        if(Kernel.curPro!=null)
//                            Kernel.gui.showcurPro.setText(String.valueOf(Kernel.curPro.ProID));
//                        else
//                            Kernel.gui.showcurPro.setText("null");
//                        if(Kernel.curPro!=null&&!Kernel.curPro.isOver())
//                            Kernel.cpu.CPU_REC(Kernel.curPro);
//                    }
                }
                System.out.println("======================打印readblock阻塞队列===========================");
                Kernel.show_readBlockQueue();
                Kernel.Printstatus();
                // 唤醒pst线程，并询jit线程等待
                Kernel.pstCondition.signal();
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
    //输出处理函数：Read（）
    //***********************************
    public void Read(){
        if(Kernel.curPro!=null&&!Kernel.curPro.isOver()){
//            System.out.println("flagio"+Kernel.curPro.IR.flag_IO);
            if(Kernel.curPro.instructions.get(Kernel.curPro.PC).Instruc_State ==3&&Kernel.curPro.IR.flag_IO){
                // 保存当前CPU现场状态
//                Kernel.cpu.CPU_PRO(Kernel.curPro);
                //==========================
                System.out.println(Kernel.curPro.ProID+"的第"+Kernel.curPro.PC+"条指令进入readblock阻塞队列");
                System.out.println("read此时时钟"+MYClock_thread.COUNTTIME);
//                Kernel.gui.pstfield.append("时钟"+MYClock_thread.COUNTTIME+":"+Kernel.curPro.ProID+"的第"+Kernel.curPro.PC+"条指令进入readblock阻塞队列\n");
                Kernel.gui.pstfield.append(MYClock_thread.COUNTTIME+":[阻塞进程:readblock阻塞队列,"+Kernel.curPro.ProID+"]\n");
                Kernel.curPro.BqTimes3=MYClock_thread.COUNTTIME;
                // 写入
                Kernel.memory.printMemoryMap();
                Cache("P",null);
                Kernel.memory.printMemoryMap();
                Scheduler.remove_PCB(Kernel.curPro);
                System.out.println("就绪队列中移除了"+Kernel.curPro.ProID);
                Kernel.curPro.readBlockQueue_time=MYClock_thread.COUNTTIME;
                Kernel.curPro.IR.flag_IO=false;
                Kernel.readBlockQueue.add(Kernel.curPro);  // 当前进程阻塞
                Kernel.curPro=null;
            }
        }

    }
}
