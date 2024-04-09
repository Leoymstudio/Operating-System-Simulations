package Basic_Structure;

import java.util.ArrayList;
import java.util.TreeMap;
import Components.Job;
//****************************************************
//就绪队列类Scheduler
//****************************************************
public class Scheduler {
    // 三级队列,直接当就绪队列
    public static ArrayList<PCB>[] queues;
    public static int curTimeSlice;// 当前时间片

    // 将进程添加到就绪队列中的第一级队列
    public static void add(PCB pcb) {
        pcb.curTimeSlice=1;
        pcb.cur_rest_time=1;
        queues[0].add(pcb); // 总是加入到第一级队列
    }

    // 0, 1, 2
    public static void add_index(PCB pcb,int timeslice){
        int x=0;
        int y=0;
        if(timeslice==1){
            x=1;
            y=2;
        }else if(timeslice==2){
            x=2;
            y=4;
        }else{
            x=2;
            y=4;
        }
        pcb.curTimeSlice=y;
        pcb.cur_rest_time=y;
        queues[x].add(pcb);
    }


    // 从就绪队列中取出并移除一个进程
    public static PCB DJFK() {
        // 从第一级队列开始检查是否有进程
        for (int i = 0; i < 3; i++) {
            if (!queues[i].isEmpty()) {
                return queues[i].remove(0); // 从队列头部移除并返回进程
            }
        }
        return null; // 如果所有队列都为空，则返回null
    }

    public static boolean isEmpty() {
        // 检查所有队列是否为空
        for (int i = 0; i < 3; i++) {
            if (!queues[i].isEmpty()) {
                return false;
            }
        }
        return true; // 如果所有队列都为空，则返回true
    }

    //进程执行完成后移除指定线程
    public static void remove_PCB(PCB pcb){
        for(int i=0;i<3;i++){
            for(PCB p : queues[i]){
                if(pcb.ProID==p.ProID){
                    queues[i].remove(p);
                    return;
                }
            }
        }
    }

    // 打印就绪队列中的进程信息
    public static void readyQueuePrint() {
        int i=1;
        for (ArrayList<PCB> queue : queues) {
            System.out.println("queue"+i+"\n");
            for (PCB pcb : queue) {
                System.out.println("Process ID: " + pcb.ProID + ", Priority: " + pcb.Priority + ", PC: " + pcb.PC + ", PCStatue: " + pcb.IR.Instruc_State+", FlagIO: "+pcb.IR.flag_IO +"\n");
                if(!pcb.IR.flag_IO)
                    pcb.IR.flag_IO=true;
            }
            showreadyQueue(i,queue);
            i++;
        }
    }

    public static void showreadyQueue(int i,ArrayList<PCB> queue){
        if(i==1)
        {
            Kernel.gui.rQ1field.setText("");
            for (PCB pcb : queue) {
                if(pcb.PC<pcb.instructions.size())
                    Kernel.gui.rQ1field.append("Process ID: " + pcb.ProID + ", Priority: " + pcb.Priority + ", PC: " + pcb.PC + ", PCStatue: " + pcb.instructions.get(pcb.PC).Instruc_State+", FlagIO: "+pcb.IR.flag_IO +"\n");
            }
        }
        if (i==2) {
            Kernel.gui.rQ2field.setText("");
            for (PCB pcb : queue) {
                if(pcb.PC<pcb.instructions.size())
                    Kernel.gui.rQ2field.append("Process ID: " + pcb.ProID + ", Priority: " + pcb.Priority + ", PC: " + pcb.PC + ", PCStatue: " + pcb.instructions.get(pcb.PC).Instruc_State+", FlagIO: "+pcb.IR.flag_IO+"\n");
            }
        }
        if(i==3){
            Kernel.gui.rQ3field.setText("");
            for (PCB pcb : queue) {
                if(pcb.PC<pcb.instructions.size())
                    Kernel.gui.rQ3field.append("Process ID: " + pcb.ProID + ", Priority: " + pcb.Priority + ", PC: " + pcb.PC + ", PCStatue: " + pcb.instructions.get(pcb.PC).Instruc_State+", FlagIO: "+pcb.IR.flag_IO+"\n");
            }
        }

    }

}


