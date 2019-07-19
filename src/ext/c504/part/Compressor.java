package ext.c504.part;

import java.io.File;   
import java.io.FileInputStream;   
import java.io.FileOutputStream;   
  
import org.apache.tools.zip.ZipOutputStream;   
  
public class Compressor {   
       

    public void zip(String inputFileName, String zipFileName) throws Exception {   
        System.out.println(zipFileName);   
        zip(zipFileName, new File(inputFileName));   
    }   
  
    private void zip(String zipFileName, File inputFile) throws Exception {   
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(   
                zipFileName));   
        zip(out, inputFile, "");   
        System.out.println("zip done");   
        out.close();   
    }   
  
    private void zip(ZipOutputStream out, File f, String base) throws Exception {   
        if (f.isDirectory()) {  //�ж��Ƿ�ΪĿ¼   
            File[] fl = f.listFiles();   
            base = base.length() == 0 ? f.getName() : base;   
            out.putNextEntry(new org.apache.tools.zip.ZipEntry(base + "/"));
            System.out.println((base + "/"));
            for (int i = 0; i < fl.length; i++) {   
                zip(out, fl[i], base +"/"+ fl[i].getName());   
            }   
        } else {                //ѹ��Ŀ¼�е������ļ�   
            out.putNextEntry(new org.apache.tools.zip.ZipEntry(base));   
            FileInputStream in = new FileInputStream(f);   
            int b;   
            System.out.println(base);   
            while ((b = in.read()) != -1) {   
                out.write(b);   
            }   
            in.close();   
        }   
    }   
  
    public static void main(String[] temp) {   
        String inputFileName = "C:\\Users\\Administrator\\Desktop\\���տ�ƬExcelģ��";
        String zipFileName = "C:\\Users\\Administrator\\Desktop\\test.zip";
  
        Compressor book = new Compressor();   
        try {   
            book.zip(inputFileName, zipFileName);   
        } catch (Exception ex) {   
            ex.printStackTrace();   
        }   
    }   
  
}  



