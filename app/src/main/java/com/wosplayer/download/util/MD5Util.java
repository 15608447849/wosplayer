package com.wosplayer.download.util;

import com.wosplayer.app.Logs;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.security.MessageDigest;

/**
 * 
 *<p>Title:MD5Util </p>
 *<p>Description: </p>
 *<p>Company: </p> 
 * @author xieyg
 * @date 下午3:06:56
 */
public class MD5Util {

	public static char[] hexChar = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
    public static String toHexString(byte[] b) {
        StringBuilder sb = new StringBuilder(b.length * 2);
        for (int i = 0; i < b.length; i++) {
            sb.append(hexChar[(b[i] & 0xf0) >>> 4]);
            sb.append(hexChar[b[i] & 0x0f]);
        }
        return sb.toString();
    }
    
    public static String getFileMD5String(File file) {
    	BufferedWriter bw = null;
        String md5Code = null;
        try{
            if (!file.exists()){
                Logs.e("生成MD5 的 资源文件不存在 :" + file.toString());
                return null;
            }

            FileInputStream in = new FileInputStream(file);

            byte [] buffer =  new   byte [ 1024 ];
            int  numRead =  0 ;
            MessageDigest messagedigest = MessageDigest.getInstance("MD5");
            while  ((numRead = in.read(buffer)) >  0 ) {
                messagedigest.update(buffer, 0 , numRead);
            }
            in.close();
            md5Code = toHexString(messagedigest.digest());
            } catch (Exception e){
            Logs.e(e.getMessage());
        }finally{
        	try {
                if (bw != null){
                    bw.close();
                }
			} catch (IOException e) {
				Logs.e(e.getMessage());
			}
        }
        return md5Code;
    }








    public static String readFileByLines(String fileName) {
        StringBuffer sb = new StringBuffer();
        File file = new File(fileName);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            int line = 1;
            // 一次读入一行，直到读入null为文件结束
            while ((tempString = reader.readLine()) != null) {
                // 显示行号

                sb.append(tempString);
                line++;
            }
            reader.close();

        } catch (IOException e) {
//            e.printStackTrace();
            Logs.e("MD5 - " + e.getMessage());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }

        return sb.toString();
    }

    public static int FTPMD5(String sourcefile_md5code, String dirFile) {
//        String strs = readFileByLines(sp);
        String dstr = readFileByLines(dirFile);
        deleteFile(dirFile);
        if (sourcefile_md5code.equals(dstr)){
            return 0;
        }else{
            return 1;
        }
    }

    /**
     * 删除本地文件
     *
     * @param fileName
     * @return
     */
    public static void deleteFile(String fileName) {
        try {
//            Logs.e("MD5"," 删除文件 - "+fileName);
            File file = new File(fileName);
            if (file.exists()) {
                file.delete();
                Logs.e("MD5"," 已删除文件 - "+fileName );
            }
        } catch (Exception e) {
            Logs.e("util", "delete file error with exception" + e.toString()
                    + "on file:" + fileName);
        }
    }
}
