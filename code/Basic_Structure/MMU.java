package Basic_Structure;
/*
***************************
地址变换过程（MMU）需要在界面上显示并详细记录过程，保存到
ProcessResults-？？？-算法名称代号.txt 文件中。
？？？表示数字，为每次运行完成所有进程的总分钟数。
***************************
*/
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


public class MMU {
    public StringBuilder sb;
    MMU(){
        sb = new StringBuilder();
    }
    public void outmmu(String log) {
        sb.append(log);
    }
    public void outtxt(String log) throws IOException {


    }
}
