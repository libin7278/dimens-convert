package com.libin;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.libin.utils.FileIOUtils;

import javax.swing.*;
import java.io.File;
import java.util.List;

public class DimendsConvert extends AnAction {
    private static final String LINE_SEP = "\n";

    @Override
    public void actionPerformed(AnActionEvent event) {
        Project project = event.getData(PlatformDataKeys.PROJECT);
        String resolution = askForResolution(project);
        String fileName = askForFileName(project);
        try{
            if(resolution.isEmpty() || fileName.isEmpty()){
                Messages.showMessageDialog(project,"请输入有效信息","失败",Messages.getQuestionIcon());
            }else {
                selectFile(project, resolution,fileName);
            }
        }catch (Exception e){
            System.out.println(e);
        }
    }

    private String askForResolution(Project project) {
        return Messages.showInputDialog(project,
                "请输入需要转换倍数", "Input Your Resolution",
                Messages.getQuestionIcon());
    }

    private String askForFileName(Project project) {
        return Messages.showInputDialog(project,
                "请输入新的文件夹名称?", "Input Your FileName",
                Messages.getQuestionIcon());

    }

    private void selectFile(Project project, String resolution, String fileName) {
        System.out.println("resolution:" +resolution +"      fileName" +fileName);
        JFileChooser jfc = new JFileChooser();
        //只支持文件
        jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        jfc.showDialog(new JLabel(), "选择");
        File file = jfc.getSelectedFile();
        if (file.isFile()) {
            //获取文件名
            String getFileName = jfc.getSelectedFile().getName();
            String fileType = getFileName.substring(getFileName.lastIndexOf("."));
            if(fileType.equals(".xml")){
                //file.getAbsolutePath() 获取文件路径
                convert(project,file.getAbsolutePath(),resolution,fileName);
            }else {
                Messages.showMessageDialog(project,"选择的文件类型有误","失败",Messages.getQuestionIcon());
            }
        }
        System.out.println(jfc.getSelectedFile().getName());
    }

    /**
     * 转换成输入对应比率的dimens文件
     * @param project
     * @param absolutePath
     * @param resolution
     * @param fileName
     */
    private void convert(Project project, String absolutePath, String resolution, String fileName) {
        float scale = 1;
        try{
           scale = Float.parseFloat(resolution);
        }catch(Exception e){
            Messages.showMessageDialog(project,"输入的转换倍数有误，保持原有数值","失败",Messages.getQuestionIcon());
        }
        //输入
        File xml = new File(absolutePath);
        //输出 原name+分辨率.xml
        String newFilePath = absolutePath.substring(0, absolutePath.indexOf("/res/"));
        newFilePath = newFilePath + "/res/" + fileName + "/dimens.xml";
        System.out.println(newFilePath+"=====");
        File xmlOutput = new File(newFilePath);
        List<String> list = FileIOUtils.readFile2List(xml, "UTF-8");
        StringBuilder sb = new StringBuilder();
        for (int i = 0, len = list.size(); i < len; ++i) {
            String line = list.get(i);
            if (!line.contains("<dimen name=")) {
                sb.append(line);
            } else {
                int indexOfStart = line.indexOf('>');
                sb.append(line.substring(0, indexOfStart + 1));
                int indexOfEnd = line.lastIndexOf('<');
                int num = Integer.parseInt(line.substring(indexOfStart + 1, indexOfEnd - 2));
                String unit = line.substring(indexOfEnd - 2, indexOfEnd);
                String scaleString = String.valueOf((int) ((scale * num) + 0.5));
                sb.append(scaleString)
                        .append(line.substring(indexOfEnd - 2));
            }
            sb.append(LINE_SEP);
        }
        FileIOUtils.writeFileFromString(xmlOutput, sb.toString(), false);

        Messages.showMessageDialog(project,"成功生成对应分辨率dimens文件","成功",Messages.getQuestionIcon());

    }

}
