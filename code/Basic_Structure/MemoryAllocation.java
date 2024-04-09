package Basic_Structure;
//****************************************************
//内存映射（物理）
//****************************************************
class MemoryAllocation {
    public int startIndex;  //起始地址
    public int size;  //大小
    public MemoryAllocation(int startIndex, int size) {
        this.startIndex = startIndex;
        this.size = size;
    }
}
