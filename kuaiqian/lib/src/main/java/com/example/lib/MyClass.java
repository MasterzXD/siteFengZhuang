package com.example.lib;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MyClass {

    public final String  targetPath =  "E:\\projects\\siteFengZhuang\\kuaiqian\\app\\src\\main\\res\\";
    public static void main(String[] args) {
        new MyClass().testAAA();
    }

    public void testAAA(){
        String ROOTPATH = "E:\\projects\\siteFengZhuang\\kuaiqian\\app\\src\\";
        copyFile(new File(ROOTPATH));
    }

    public void copyFile(File file){
        if(file.isDirectory() && !file.getName().equals("main")&& !file.getName().equals("assets")){
            for (File f :file.listFiles()) {
                copyFile(f);
            }
        }else if(file.isFile()){
            String fileName = file.getName();
            if(fileName.contains(".")){

                if(!fileName.startsWith("iconx") && !fileName.startsWith("loading") && !fileName.endsWith("xml")){
                    System.out.println("----"+fileName);
                    String prefix = fileName.split(".")[0];
                    String sub = fileName.split(".")[1];
                    int i=0;
                    File target = new File(targetPath+"\\"+file.getParentFile().getName()+"\\"+fileName);
                    while (target.exists()){
                        i++;
                        target = new File(targetPath+"\\"+file.getParentFile().getName()+"\\"+prefix+i+"."+sub);
                    }
                    try {
                        Files.copy(file.toPath(),target.toPath());
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    file.delete();
                }
            }
        }

    }


}
