package Components;

import Basic_Structure.Memory;

import java.util.ArrayList;
import java.util.PriorityQueue;
//****************************************************
//作业类：Job
//****************************************************
public class Job implements Comparable{
    public Integer JobsID;
    public Integer Priority;
    public Integer InstrucNum;
    public Integer InTimes;
    public Integer TotalSum;
    public ArrayList<Instruction> instructions;


    public Job(Integer jobsID, Integer inTimes, Integer priority, Integer instrucNum, ArrayList<Instruction> instructions) {
        JobsID = jobsID;
        Priority = priority;
        InTimes = inTimes;
        InstrucNum = instrucNum;
        this.instructions = instructions;
        this.TotalSum=CountTotalSum(instructions);
    }

    @Override
    public String toString() {
        return "{" +
                "Job=" + JobsID +
                '}';
    }

    @Override
    public int compareTo(Object job) {
        if(this.Priority!=((Job)job).Priority) {
            return ((Job) job).Priority - this.Priority;
        }
        else{
            return this.InTimes - ((Job) job).InTimes;
        }
    }
    public int CountTotalSum(ArrayList<Instruction> instructions)
    {
        int TotalDataSize=0;
        int ThisDataSize=0;
        for (Instruction instruction : instructions) {
            if(instruction.Instruc_State==0||instruction.Instruc_State==1||instruction.Instruc_State==2)
                ThisDataSize=1;
            else if (instruction.Instruc_State==3||instruction.Instruc_State==4)
                ThisDataSize=1+instruction.Data_Size*2;
            else
                System.out.println("Error in Counting DataSize");

            TotalDataSize = TotalDataSize + ThisDataSize;
        }
        return TotalDataSize;
    }
}
