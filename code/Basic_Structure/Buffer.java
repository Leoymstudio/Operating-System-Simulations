package Basic_Structure;


import Control_Thread.MYClock_thread;

import java.sql.SQLOutput;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
//****************************************************
//缓存区Buffer
//****************************************************
public class Buffer {

    private static final int MEMORY_SIZE = 40; // Total number of basic units

    private BitSet memoryMap;
    private Map<Integer, MemoryAllocation> allocations;
    public static Integer Buffer_full;
    public static int showbufferIndex;
//    public static PCB currentProcess;
//    public static ConcurrentLinkedQueue<PCB> inputBlockQueue = new ConcurrentLinkedQueue<>();  //阻塞队列
//    public static ConcurrentLinkedQueue<PCB> outputBlockQueue = new ConcurrentLinkedQueue<>();  //阻塞队列
    public Buffer() {
        memoryMap = new BitSet(MEMORY_SIZE);
        allocations = new HashMap<>();
        Buffer_full = MEMORY_SIZE/4;
        showbufferIndex=0;
    }

    public boolean allocateMemory(int jobId) {
        // 分配4个单位内存
        int size=4;
        // 需要多少内存
        int unitsNeeded = size;
        // 找到最佳适应
        int startIndex = findBestFit(unitsNeeded);
        // 如果找到最佳适应
        if (startIndex != -1) {
            memoryMap.set(startIndex, startIndex + unitsNeeded);  // 将内存映射到startIndex
            allocations.put(jobId, new MemoryAllocation(startIndex, size));  // 存储分配
            System.out.println("Buffer allocated for job " + jobId + " from index " + startIndex +
                    " to " + (startIndex + unitsNeeded - 1));
            showbufferIndex=startIndex;
            return true;
        } else {
            System.out.println("Buffer allocation failed for job " + jobId + ": Not enough memory available.");
            return false;
        }
    }

    public void deallocateMemory(int jobId) {
        // 获取分配
        MemoryAllocation allocation = allocations.get(jobId);
        // 如果分配不为空
        if (allocation != null) {
            // 清除内存映射
            memoryMap.clear(allocation.startIndex, allocation.startIndex + allocation.size);
            System.out.println("Buffer deallocated for job " + jobId + " from index " + allocation.startIndex +
                    " to " + (allocation.startIndex + allocation.size - 1));
            // 移除分配
            allocations.remove(jobId);
        } else {
            System.out.println("Buffer deallocation failed for job " + jobId + ": No such allocation.");
        }
    }

    private int findBestFit(int unitsNeeded) {
        int minStartIndex = -1;
        int minBlockSize = Integer.MAX_VALUE;

        int startIndex = 0;
        while (startIndex + unitsNeeded <= MEMORY_SIZE) {
            startIndex = memoryMap.nextClearBit(startIndex);
            if (startIndex + unitsNeeded > MEMORY_SIZE) {
                break;
            }
            int endIndex = memoryMap.nextSetBit(startIndex);
            if (endIndex == -1) {
                endIndex = MEMORY_SIZE;
            }
            int blockSize = endIndex - startIndex;
            if (blockSize >= unitsNeeded && blockSize < minBlockSize) {
                minBlockSize = blockSize;
                minStartIndex = startIndex;
            }
            startIndex = endIndex + 1; // Move startIndex to the next set bit
        }
        return minStartIndex;
    }

    public void P(PCB pcb) { // write into the buffer
        System.out.println("Buffer is writing!"); // 写入缓冲区
        Kernel.gui.bufferfield.append(MYClock_thread.COUNTTIME+":[P操作:"+pcb.ProID+"]\n");

        Buffer_full=Buffer_full-1;

        if(Buffer_full>=0) {
            //flag通过，能读
            pcb.Buffer_flag=1;
            //申请缓冲区空间
            Kernel.buffer.allocateMemory(pcb.ProID);
            Kernel.gui.bufferfield.append(MYClock_thread.COUNTTIME+":[拷贝入缓冲区:"+pcb.ProID+":"+pcb.IR.Instruc_State+":"+showbufferIndex/4+"]\n");
        }
        else
        {
            //flag不通过---不能读
            pcb.Buffer_flag=0;
            System.out.println("Buffer已满");
            Kernel.gui.bufferfield.append(MYClock_thread.COUNTTIME+":[Buffer满:"+pcb.ProID+"]\n");
        }
    }
    public void V(PCB pcb) {
        System.out.println("Buffer is reading!");
//        Kernel.gui.bufferfield.append("时钟"+ MYClock_thread.COUNTTIME+"进程"+pcb.ProID+"V\n");
        Kernel.gui.bufferfield.append(MYClock_thread.COUNTTIME+":[V操作:"+pcb.ProID+"]\n");
        //释放缓冲区空间
        Kernel.buffer.deallocateMemory(pcb.ProID);
        Kernel.gui.bufferfield.append(MYClock_thread.COUNTTIME+":[拷贝出缓冲区:"+pcb.ProID+":"+pcb.IR.Instruc_State+":"+showbufferIndex/4+"]\n");
    }
    public void printBufferMap() {
        System.out.println("BufferMap:");
        for (int i = 0; i < MEMORY_SIZE; i++) {
            if(i%4==0)
                Kernel.gui.memfield.append("物理块(Buffer)"+i/4+"：");
            System.out.print(memoryMap.get(i) ? "1 " : "0 ");
            Kernel.gui.memfield.append(memoryMap.get(i) ? "1 " : "0 ");
            if((i+1)%4 ==0)
            {
                System.out.println("\n");
                Kernel.gui.memfield.append("\n");

            }
        }


    }
    public boolean isBufferEmpty() {
        if(memoryMap.isEmpty())
            return true;
        else
            return false;
    }

}

