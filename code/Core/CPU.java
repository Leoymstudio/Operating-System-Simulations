package Core;

import Basic_Structure.Kernel;
import Basic_Structure.Memory;
import Basic_Structure.PCB;
import Basic_Structure.Scheduler;
import Components.Instruction;
import Control_Thread.MYClock_thread;
//***************************
//CPU类
//***************************
public class CPU{
    public int PC; // 程序计数器
    public Instruction IR; // 指令寄存器
    public int PSW; // 状态寄存器

    public static Integer isCloseInterrupted;// 0-不阻塞 1-input 2-output 3-disk read

    public CPU(){
        this.PC=0;
        this.IR= null;
        this.PSW=0;
        isCloseInterrupted=0;
    }
    //***************************
    // CPU现场保护函数
    //***************************
    public void CPU_PRO(PCB pcb) {
        // 保存CPU现场到寄存器或内存中
        pcb.PC = this.PC;
        pcb.IR = this.IR;
    }
    //***************************
    // CPU现场恢复函数
    //***************************
    public void CPU_REC(PCB pcb) {
        // 从寄存器或内存中恢复CPU现场
        this.PC = pcb.PC;
        this.IR = pcb.instructions.get(pcb.PC);
    }

    public Integer ifInterrupted(){
        return isCloseInterrupted;
    }

    public void clearInterrupted() {
        isCloseInterrupted=0;
    }

    public void clearPSW(){
        this.PSW = 0;
    }

    public void setPSW(){
        this.PSW = 1;
    }

    public int getPC()
    {
        return PC;
    }

    public void execute() {  // cpu execute the current instruction
        System.out.println("运行CPUing--------------------------------");
        //当前有正在执行的进程，或者没有(cpu空闲)
        PCB pcb = null;
//        System.out.println("这儿总过了吧！！！！"+Kernel.curPro);
        if (Kernel.curPro == null) {
            if (PSW == 0) {
//                GUI.Output.print("CPU空闲");
                System.out.println("CPU空闲");
                return;
            }
        } else {
            pcb = Kernel.curPro;
        }
        Kernel.curPro = pcb;
        System.out.println("这儿总过了吧！！！！"+Kernel.curPro.PC);

        //当前有正在执行的进程，判断一下当前进程有没有执行完
        //这里进if说明执行完了，但是如果直接返回这个时间片什么也没有用，所以不仅要删除队列中的pcb还要
        if (Kernel.curPro.isOver()) {
            System.out.println("当前进程没有可以执行的指令,当前进程没有使用当前时间片");
            Kernel.curPro.flag_over_or_not = true;//这里是没有执行指令但是进程结束，需要返回重新调度一个进程来执行，填补当前时间片
            return;
        }

        CPU_REC(Kernel.curPro); //fetch the instruction

        /*
        0 表示用户态计算操作指令
        1 表示键盘输入变量指令
        2 表示屏幕显示输出指令
        3 表示读入磁盘数据指令
        4 表示打印指令
        */
//        if(Kernel.curPro.is_first_process)
//        {
//            Kernel.curPro.ExcuteTimes=MYClock_thread.COUNTTIME;
//            Kernel.curPro.is_first_process=false;
//        }
//        System.out.println("test if here");
        if(!Kernel.curPro.IR.flag_IO)
        {
//            Kernel.gui.showcurPro.setText("进程"+Kernel.curPro.ProID+"无法执行");
            System.out.println("进程"+Kernel.curPro.ProID+"无法执行");
            System.out.println("指令"+Kernel.curPro.IR+"无法执行");
        }
        if (Kernel.curPro.IR.flag_IO) {

            switch (IR.Instruc_State) {
                case 0: {
                    if (Kernel.curPro.cur_Instruc_rest_time == 0) {
                        Kernel.curPro.cur_Instruc_rest_time = 1;
                    }
//                    Kernel.gui.pstfield.append("时钟"+MYClock_thread.COUNTTIME+":"+Kernel.curPro.ProID + "的第" + PC + "条指令执行指令0\n");
                    Kernel.gui.pstfield.append(MYClock_thread.COUNTTIME+":[运行进程:"+Kernel.curPro.ProID + ":" + PC + ",0,"+Kernel.curPro.InstrMem()+",1"+"]\n");
                    IR.execute();
                    isCloseInterrupted = 0;
                    //计算时间
                    Kernel.curPro.cur_rest_time -= 1;
                    Kernel.curPro.cur_Instruc_rest_time -= 1;
//                    Kernel.curPro.ExcuteTimes += 1;
//                    Kernel.curPro.IR.flag_IO = false;
                    if (Kernel.curPro.cur_Instruc_rest_time == 0) {
                        PC += 1;//cpu的pc
                        //更新IR
                        if (PC < Kernel.curPro.instructions.size()) {
                            IR = Kernel.curPro.instructions.get(PC);
                        }
                    }
                    CPU_PRO(Kernel.curPro);
                    return;
                }
                case 1: {

                    IR.execute();
                    Kernel.gui.pstfield.append(MYClock_thread.COUNTTIME+":[运行进程:"+Kernel.curPro.ProID + ":" + PC + ",1,"+Kernel.curPro.InstrMem()+",1"+"]\n");
                    isCloseInterrupted = 1;//input
                    Kernel.curPro.IR.flag_IO = true;
                    //加入阻塞队列，进行中断
                    System.out.println(IR);
                    System.out.println(Kernel.clockThread.COUNTTIME + ":Input堵塞了");
//                    Kernel.gui.statusfield.append("时钟"+Kernel.clockThread.COUNTTIME +":" + ":Input堵塞了\n");
                    CPU_PRO(Kernel.curPro);

//                    Kernel.curPro.RunTimes.add(MYClock_thread.getTime());
                    return;
                }

                case 2: {

                    IR.execute();
//                    Kernel.gui.pstfield.append("时钟"+MYClock_thread.COUNTTIME+":"+Kernel.curPro.ProID + "的第" + PC + "条指令执行指令2\n");
                    Kernel.gui.pstfield.append(MYClock_thread.COUNTTIME+":[运行进程:"+Kernel.curPro.ProID + ":" + PC + ",2,"+Kernel.curPro.InstrMem()+",1"+"]\n");
                    isCloseInterrupted = 2;//output
                    Kernel.curPro.IR.flag_IO = true;
                    //加入阻塞队列，进行中断
                    System.out.println(IR);
                    System.out.println(Kernel.clockThread.COUNTTIME + ":Output堵塞了");
//                    Kernel.gui.statusfield.append("时钟"+Kernel.clockThread.COUNTTIME +":" + ":Output堵塞了\n");
                    CPU_PRO(Kernel.curPro);

//                    Kernel.curPro.RunTimes.add(MYClock_thread.getTime());
                    return;
                }

                case 3: {

                    IR.execute();
//                    Kernel.gui.pstfield.append("时钟"+MYClock_thread.COUNTTIME+":"+Kernel.curPro.ProID + "的第" + PC + "条指令执行指令3\n");
                    Kernel.gui.pstfield.append(MYClock_thread.COUNTTIME+":[运行进程:"+Kernel.curPro.ProID + ":" + PC + ",3,"+Kernel.curPro.InstrMem()+","+(1+2*IR.Data_Size)+"]\n");
                    isCloseInterrupted = 3;//output

                    //加入阻塞队列，进行中断
                    System.out.println(IR);
                    System.out.println(Kernel.clockThread.COUNTTIME + ":磁盘读堵塞了");
//                    Kernel.gui.statusfield.append("时钟"+Kernel.clockThread.COUNTTIME +":" + ":磁盘读堵塞了\n");
                    CPU_PRO(Kernel.curPro);

//                    Kernel.curPro.RunTimes.add(MYClock_thread.getTime());
                    return;
                }
            }
        }

    }
}

