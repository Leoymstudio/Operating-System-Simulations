package Basic_Structure;
import java.util.ArrayList;
import java.util.PriorityQueue;
import Components.Instruction;
import Components.Job;
import Control_Thread.MYClock_thread;
import java.io.*;
import java.util.Random;
// HardDisk类表示一个硬盘，其中包含了临时作业和后备队列等信息
public class HardDisk {
    // 存储临时作业的集合
    public static ArrayList<Job> tempJobs;
    // 存储后备作业的优先级队列
    public static PriorityQueue<Job> jobFallbackQueue;

    // 记录了log中已处理的最后一个作业
    public static Integer last;
    public static Integer createtimes;

    public HardDisk()
    {
        createtimes=8;
        String root;
        root="input3";  //路径
        try {
            submit(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(jobFallbackQueue);
    }

    // 初始化为硬盘
    static {
        tempJobs = new ArrayList<>();
        jobFallbackQueue = new PriorityQueue<>();
        last = 0;
    }

    private static ArrayList<Instruction> getInstructionsById(String str, Integer jobsID) throws IOException {
        // 创建一个指令集合
        ArrayList<Instruction> instructions = new ArrayList<>();
        // 拼接文件名
        String filename = str + File.separator + jobsID.toString() + ".txt";
        // 创建一个BufferedReader对象
        BufferedReader bfr = new BufferedReader(new FileReader(filename));
        // 循环读取文件
        while(true) {
            // 读取一行
            String line = bfr.readLine();
            // 如果读取完毕或者这一行是空，则退出循环
            if(line == null || line.length() == 0) {
                break;
            }
            // 将这一行按照逗号分割成数组
            String[] instructionData = line.split(",");

            // 初始化为默认值或 null，根据你的具体需求调整
            Integer Instruc_ID = null, Instruc_State = null, Data_Size = 0;

            // 检查并解析数据
            if (instructionData.length > 0 && !instructionData[0].trim().isEmpty()) {
                Instruc_ID = Integer.parseInt(instructionData[0].trim());
            }
            if (instructionData.length > 1 && !instructionData[1].trim().isEmpty()) {
                Instruc_State = Integer.parseInt(instructionData[1].trim());
            }
            if (instructionData.length > 2 && !instructionData[2].trim().isEmpty()) {
                Data_Size = Integer.parseInt(instructionData[2].trim());
            }

            // 将指令添加到集合中
            instructions.add(new Instruction(Instruc_ID, Instruc_State, Data_Size));
//            System.out.println(instructions);
        }
        // 返回指令集合
        return instructions;
    }

    /*
     * 这个方法从给定的root路径下的文件中读取作业，并将每个作业添加到作业后备队列中。
     */
    public static void submit(String root) throws IOException {
        // 指定输入文件的文件名
        Kernel.Job_count=0;
        String filename = root + File.separator + "jobs-input.txt";
        // 以读取模式打开文件
        BufferedReader bfr = new BufferedReader(new FileReader(filename));
        while(true) {
            // 读取一行数据
            String line = bfr.readLine();
//            System.out.println(line);
            // 如果读取到数据，则继续；如果读取到EOF，则退出循环
            if(line == null || line.length() == 0) {
                break;
            }
            // 将一行数据按逗号分割成数组
            String[] jobData = line.split(",");
            // 提取作业ID、优先级、指令数量等数据
            Integer JobsID = Integer.parseInt(jobData[0].trim());
            Integer InTimes = Integer.parseInt(jobData[1].trim());
            Integer Priority = Integer.parseInt(jobData[2].trim());
            Integer InstrucNum = Integer.parseInt(jobData[3].trim());
            Job job = new Job(JobsID, InTimes, Priority, InstrucNum, getInstructionsById(root, JobsID));
            // 输出一条新增作业的日志信息
            String s = "新增作业:" + job.JobsID + "," + job.InTimes + "," + job.InstrucNum;
            Kernel.Job_count+=1;
//            System.out.println(s);

            // 将新建的作业添加到作业后备队列
            jobFallbackQueue.add(job);
        }
    }
    public static ArrayList<Instruction> getrandomInstructions()
    {
        int i=1;
        // 创建一个指令集合
        ArrayList<Instruction> instructions = new ArrayList<>();
        // 循环读取文件
        while(i<=10) {
            Integer Instruc_ID = null, Instruc_State = null, Data_Size = 0;
            Instruc_ID=i;
            // 生成 0-3 之间的随机数
            Random random = new Random();
            Instruc_State = random.nextInt(4);

            // 根据 Instruc_State 的值设置 Data_Size
            if (Instruc_State == 3) {
                Data_Size = random.nextInt(2) + 1; // 1 或 2
            }

            // 将指令添加到集合中
            instructions.add(new Instruction(Instruc_ID, Instruc_State, Data_Size));
            i++;
        }
        // 返回指令集合
        return instructions;
    }
    public static void CreateTask(){
        Job job = new Job(createtimes, MYClock_thread.COUNTTIME, 0, 10, getrandomInstructions());
        createtimes+=1;
        // 将新建的作业添加到作业后备队列
        jobFallbackQueue.add(job);
        System.out.println("加入新作业:"+job.JobsID+"\n");
        Kernel.Job_count+=1;
    }
    public static ArrayList<Instruction> getrandomInstructions2()
    {
        int i=1;
        // 创建一个指令集合
        ArrayList<Instruction> instructions = new ArrayList<>();
        // 循环读取文件
        while(i<=10) {
            Integer Instruc_ID = null, Instruc_State = null, Data_Size = 0;
            Instruc_ID=i;
            Instruc_State = 3;

            // 根据 Instruc_State 的值设置 Data_Size
            Data_Size = 6;

            // 将指令添加到集合中
            instructions.add(new Instruction(Instruc_ID, Instruc_State, Data_Size));
            i++;
        }
        // 返回指令集合
        return instructions;
    }
    public static void Createdeathlock(){
        Job job = new Job(createtimes, MYClock_thread.COUNTTIME, 0, 10, getrandomInstructions2());
        createtimes+=1;
        // 将新建的作业添加到作业后备队列
        jobFallbackQueue.add(job);
        System.out.println("加入新作业:"+job.JobsID+"\n");
        Kernel.Job_count+=1;
    }
}
