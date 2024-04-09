package Basic_Structure;


import Components.Instruction;
import Components.Job;
import Control_Thread.MYClock_thread;

import java.util.ArrayList;
import java.util.Objects;
//****************************************************
//进程/pcb类PCB
//****************************************************
public class PCB implements Comparable{
    public Integer ProID; // 进程ID
    public Integer Priority;
    public Integer ArriveTime; //没用了
    public Integer InTimes; //作业到达时间
    public Integer PCBTimes;
    public Integer EndTimes;// 结束时间
    public Integer PSW; // 进程状态
//    public ArrayList<Integer> RunTimes; // 进程运行时间序列（开始，结束）
    public Integer TurnTimes;  //进程周转时间统计
    public Integer ExcuteTimes; // 实际进程运行时间统计
    public Integer InstrucNum; // 总指令数
    public Integer curTimeSlice; // 当前所在时间片
    public Integer cur_rest_time; // 当前时间片剩余时间
    public Integer cur_Instruc_rest_time;//当前指令剩余时间
    public Integer inputBlockQueue_time;
    public Integer outputBlockQueue_time;
    public Integer readBlockQueue_time;
    public Integer Buffer_flag;
    public Boolean is_first_process;

    public boolean is_IOblock_or_not;

    public ArrayList<Instruction> instructions;

    public Integer PC;
    public Instruction IR;
    public boolean flag_over_or_not;//判断进程的指令执行完了没有

    public Job job;

    public String data;
    public Integer BqTimes1;
    public Integer BqTimes2;
    public Integer BqTimes3;
    public Integer TotalSum;
    public Integer startMem;

    public PCB(Job job) {
        this.job = job;
        this.ProID = job.JobsID;
        this.Priority = job.Priority;
        this.InTimes = MYClock_thread.COUNTTIME;
        this.ArriveTime=job.InTimes;
        this.EndTimes = null;
        this.PSW = 0; //0 代表就绪
//        this.RunTimes = new ArrayList<>();
        this.TurnTimes=0;
        this.ExcuteTimes=0;
        this.InstrucNum = job.InstrucNum;
        this.PC = 0;
        this.IR = job.instructions.get(PC);
        this.curTimeSlice=1;
        this.cur_rest_time=this.curTimeSlice;
        this.flag_over_or_not=false;
        this.cur_Instruc_rest_time=0;
        this.inputBlockQueue_time=0;
        this.outputBlockQueue_time=0;
        this.instructions=job.instructions;
        this.Buffer_flag=0;
        this.is_IOblock_or_not=false;
        this.BqTimes1=0;
        this.BqTimes2=0;
        this.BqTimes3=0;
        this.PCBTimes=job.InTimes;
        this.is_first_process=true;
        this.TotalSum= job.TotalSum;
    }

    public boolean isOver() {
        if(PC>=InstrucNum)
            return true;
        else
            return false;
    }

    @Override
    public int compareTo(Object o) {
        return this.Priority - ((PCB)o).Priority;
    }

    @Override
    public String toString() {
        return "{" +
                "ID=" + ProID +
                '}';
    }
    public Integer InstrMem(){
        return startMem+CountMem(instructions,PC);
    }  //计算当前指令在物理快中的位置（这里其实就是MMU）
    public int CountMem(ArrayList<Instruction> instructions,Integer PC)  //计算当前指令在进程中的相对位置
    {
        int TotalDataSize=0;
        int ThisDataSize=0;
        for (int i = 0; i < PC; i++) {
            Instruction instruction = instructions.get(i);
            if(instruction.Instruc_State==0 || instruction.Instruc_State==1 || instruction.Instruc_State==2)
                ThisDataSize=1;
            else if (instruction.Instruc_State==3 || instruction.Instruc_State==4)
                ThisDataSize=1 + instruction.Data_Size*2;
            else
                System.out.println("Error in Counting DataSize");
            TotalDataSize = TotalDataSize + ThisDataSize;
        }
        return TotalDataSize;
    }
}
