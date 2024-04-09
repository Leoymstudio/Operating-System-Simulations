package GUI;
import Control_Thread.*;
import Basic_Structure.*;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class GUI {
    public JPanel panel1;
    public JPanel topPanel;
    public JPanel buttonPanel;
    public JPanel readyQ;
    public JPanel show;
    public JPanel blockQ;
    public JPanel readyQ1;
    public JPanel readyQ3;
    public JPanel readyQ2;
    public JPanel blockQ1;
    public JPanel blockQ3;
    public JPanel blockQ2;
    public JLabel readyQ1label;
    public JTextArea rQ1field;
    public JLabel readyQ2label;
    public JLabel readyQ3label;
    public JTextArea rQ2field;
    public JTextArea rQ3field;
    public JPanel HBQ;
    public JLabel HBQlabel;
    public JTextArea HBfield;
    public JLabel blockQ1label;
    public JTextArea bQ1field;
    public JPanel FinishQ;
    public JLabel FinishQlabel;
    public JLabel blockQ2label;
    public JLabel blockQ3label;
    public JTextArea bQ2field;
    public JTextArea bQ3field;
    public JTextArea fQfield;
    public JLabel showpstlabel;
    public JPanel showpst;
    public JLabel showStatus;
    public JLabel showbuffer;
    public JPanel showMemory;
    public JPanel Button;
    public JLabel MemoryLabel;
    public JButton StartButton;
    public JButton EndButton;
    public JButton StopButton;
    public JButton AwakeButton;
    public JPanel ShowTime;
    public JLabel showtimelabel;
    public JLabel timelable;
    public JTextArea pstfield;
    public JTextArea statusfield;
    public JTextArea bufferfield;
    public JPanel curPro;
    public JLabel ProLabel;
    public JLabel showcurPro;
    public JScrollPane Jscroll;
    public JScrollPane jscr2;
    public JScrollPane jscr3;
    public JTextArea memfield;
    public JButton autotaskButton;
    private JButton 生成死锁Button;


    public boolean First_start;
    public StringBuilder BB1;
    public StringBuilder BB2;
    public StringBuilder BB3;

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }

    public GUI() {
        rQ1field.setLineWrap(true);
        rQ2field.setLineWrap(true);
        rQ3field.setLineWrap(true);
        HBfield.setLineWrap(true);
        bQ1field.setLineWrap(true);
        bQ2field.setLineWrap(true);
        bQ3field.setLineWrap(true);
        fQfield.setLineWrap(true);
        pstfield.setLineWrap(true);
        statusfield.setLineWrap(true);
        bufferfield.setLineWrap(true);
        memfield.setLineWrap(true);
        this.First_start=true;
        BB1= new StringBuilder("");
        BB2= new StringBuilder("");
        BB3= new StringBuilder("");
        StartButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(First_start)
                {
                    First_start=false;
                    Kernel kernel=new Kernel();
                    kernel.startSystem();
                }
            }
        });
        StopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MYClock_thread.clockstop();
            }
        });
        AwakeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MYClock_thread.clockawake();
            }
        });
        EndButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MYClock_thread.clockstop();
                String mainText = Kernel.gui.pstfield.getText();
                String mainBuffer = Kernel.gui.bufferfield.getText();
                String mainStatus = Kernel.gui.statusfield.getText();
                String mainStatus2 = Kernel.gui.fQfield.getText();
                StringBuilder sb = new StringBuilder();
                sb.append("作业/进程调度事件：").append("\n");
                sb.append(mainText).append("\n\n");
                sb.append("缓冲区处理事件：").append("\n");
                sb.append(mainBuffer).append("\n\n");
                sb.append("状态统计信息：").append("\n");
                sb.append(mainStatus2);
                sb.append(mainStatus);
                String fileName = "ProcessResults-" + Kernel.all_job_time + "-JTYXJ.txt";
                String dir = "./output3";
                File file = new File(dir + File.separator + fileName);
                if(file.exists()) {
                    file.delete();
                }
                try {
                    file.createNewFile();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                FileWriter fw = null;
                try {
                    fw = new FileWriter(file);
                    fw.write(sb.toString());
                    fw.close();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                System.exit(0);
            }
        });
        autotaskButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Kernel.hardDisk.CreateTask();
            }
        });
        生成死锁Button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Kernel.hardDisk.Createdeathlock();
            }
        });
    }
}
