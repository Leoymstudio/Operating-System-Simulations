package Components;
import Basic_Structure.Kernel;
import Control_Thread.MYClock_thread;
import Control_Thread.ProcessScheduling_thread;
import Control_Thread.InputBlock_thread;
import Control_Thread.OutputBlock_thread;
//****************************************************
//指令类Instruction
//****************************************************
public class Instruction {
    public Integer Instruc_ID; // 指令标识
    public Integer Instruc_State; // 指令类型
    public Integer Data_Size; // 逻辑地址指令访问
    public boolean flag_IO;//防止反复进入队列


    @Override
    public String toString() {  //指令字符串形式--便于输出
        return "Instruction{" +
                "Instruc_ID=" + Instruc_ID +
                ", Instruc_State=" + Instruc_State +
                ", Data_Size=" + Data_Size +
                '}';
    }

    public Instruction(Integer instruc_ID, Integer instruc_State, Integer data_Size) {  //指令构造函数
        Instruc_ID = instruc_ID;
        Instruc_State = instruc_State;
        Data_Size = data_Size;
        flag_IO=true;
    }
    public void execute() {

        if(Instruc_State == 0) {
            Instruc0();
        } else if(Instruc_State == 1) {
            Instruc1();
        }
        else if(Instruc_State == 2) {
            Instruc2();
        }
        else if(Instruc_State == 3) {
            Instruc3();
        }
        else {
            System.out.println("Error");
        }
    }

    /*
        0 表示用户态计算操作指令。执行该指令需要运行时间 InRunTimes=1s。
    当剩余时间片 < InRunTimes，相当于时间片到；如果 CPU 执行该计算操作函数时，
    有优先级高的抢占进程请求，则执行完该指令以后被抢占；
    */
    /*
        在这个代码块中，首先通过Clock_thread.class获取锁。
    获取锁后，可以将lastTime变量的值更新为Clock_thread.COUNTTIME的值。
    这样可以保证在多个线程中共享这个计数器。更新完成后，释放锁。
    */
    private void Instruc0() {
        System.out.println("这里是似乎啥也没干的--------------------------------------Instruc0在第"+Instruc_ID+"条");
        // 获取当前指令执行时间
    }
    /*
        1 表示键盘输入变量指令。发生系统调用，CPU 进行模式切换，运行进程进入阻塞态；
    值守的键盘操作输入模块接收到输入变量或输出变量内容，InRunTimes=2s 后完成输入，产
    生硬件终端信息号，阻塞队列 1 的队头节点出队，进入就绪队列；
    InputBlock_thread 类在 2s 以后自动唤醒该进程；
    */
    private void Instruc1() {
        System.out.println("Instruc1");
    }

    /*
    2 表示屏幕显示输出指令。发生系统调用，CPU 进行模式切换，运行进程进入阻塞态；
    值守的屏幕显示模块输出变量内容，InRunTimes=2s 后完成显示，产生硬件终端信息号，阻
    塞队列 2 的队头节点出队，进入就绪队列；OutputBlock_thread 类在 2s 以后自动唤醒该进程；
    */
    private void Instruc2() {
        System.out.println("Instruc2");
    }
    /*
    3 表示读入磁盘数据指令。发生系统调用，CPU 进行模式切换，运行进程进入阻塞态；
    */
    private void Instruc3() {
        System.out.println("Instruc3");
    }


}
