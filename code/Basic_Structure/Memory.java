package Basic_Structure;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
//****************************************************
//内存类Memory
//****************************************************
public class Memory {

    private static final int MEMORY_SIZE = 120; // Total number of basic units

    private BitSet memoryMap;
    private Map<Integer, MemoryAllocation> allocations;
    public int showstartIndex;

    public Memory() {
        memoryMap = new BitSet(MEMORY_SIZE);
        allocations = new HashMap<>();
        showstartIndex=0;
    }
    public boolean allocateMemory(int jobId, int size) {
        int unitsNeeded = size;  // 计算需要分配的内存单元数
        int startIndex = findBestFit(unitsNeeded);  // 找到最佳适应的内存索引
        if (startIndex != -1) {  // 如果找到了最佳适应的内存索引
            memoryMap.set(startIndex, startIndex + unitsNeeded);  // 将内存映射中的起始索引设置为起始索引和单元数
            allocations.put(jobId, new MemoryAllocation(startIndex, size));  // 将分配的内存添加到分配映射中
            System.out.println("Memory allocated for job " + jobId + " from index " + startIndex +
                    " to " + (startIndex + unitsNeeded - 1));
            showstartIndex=startIndex; // 设置showstartIndex
            return true;
        } else {
            System.out.println("Memory allocation failed for job " + jobId + ": Not enough memory available.");
            return false;
        }
    }
    // 释放内存
    public void deallocateMemory(int jobId) {
        MemoryAllocation allocation = allocations.get(jobId);  // 获取指定jobId的内存分配
        if (allocation != null) {  // 如果存在内存分配
            memoryMap.clear(allocation.startIndex, allocation.startIndex + allocation.size);  // 从内存中清除指定的分配
            System.out.println("Memory deallocated for job " + jobId + " from index " + allocation.startIndex +
                    " to " + (allocation.startIndex + allocation.size - 1));
            allocations.remove(jobId);  // 移除指定的内存分配
        } else {
            System.out.println("Memory deallocation failed for job " + jobId + ": No such allocation.");
        }
    }
    private int findBestFit(int unitsNeeded) {  // 遍历内存映射，找到第一个空闲块，其大小大于等于unitsNeeded
        int minStartIndex = -1;
        int minBlockSize = Integer.MAX_VALUE;

        // 当前正在查找的空闲块的起始地址
        int startIndex = 0;
        while (startIndex + unitsNeeded <= MEMORY_SIZE) {
            startIndex = memoryMap.nextClearBit(startIndex);  // 找到下一个空闲块的起始地址
            if (startIndex + unitsNeeded > MEMORY_SIZE) {
                break;
            }
            int endIndex = memoryMap.nextSetBit(startIndex);  // 找到下一个占用块的起始地址
            if (endIndex == -1) {
                endIndex = MEMORY_SIZE;
            }
            int blockSize = endIndex - startIndex;  // 计算空闲块的大小

            if (blockSize >= unitsNeeded && blockSize < minBlockSize) {  // 如果空闲块的大小大于等于unitsNeeded，并且小于最小块大小，则更新最小块大小和起始地址
                minBlockSize = blockSize;
                minStartIndex = startIndex;
            }
            startIndex = endIndex + 1; // 更新startIndex为下一个占用块的起始地址+1
        }
        return minStartIndex;
    }

    public void printMemoryMap() {  //打印输出Memory存储空间
        System.out.println("Memory Map:");
        Kernel.gui.memfield.setText("");
        for (int i = 0; i < MEMORY_SIZE-40; i++) {
            if(i%10==0)
                Kernel.gui.memfield.append("物理块"+(i/10+1)+"：");
            System.out.print(memoryMap.get(i) ? "1 " : "0 ");
            Kernel.gui.memfield.append(memoryMap.get(i) ? "1 " : "0 ");
            if((i+1)%10 ==0)  //换行操作
            {
                System.out.println("\n");
                Kernel.gui.memfield.append("\n");

            }
        }
        Kernel.buffer.printBufferMap();  //同时打印缓冲区的存储


    }
}

