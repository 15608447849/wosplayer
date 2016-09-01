package installUtils;

import com.wosplayer.app.log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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
    
    public static File getFileMD5String(File file) {
    	FileWriter fw = null;
    	BufferedWriter bw = null;
    	File md5File = null;
        try{
            FileInputStream in = new FileInputStream(file);
            FileChannel ch = in.getChannel();
            MappedByteBuffer byteBuffer = ch.map(FileChannel.MapMode.READ_ONLY, 0,file.length());
            MessageDigest messagedigest = MessageDigest.getInstance("MD5");
            messagedigest.update(byteBuffer);
            String md5Code = toHexString(messagedigest.digest());
            md5File = new File(file.getPath()+".md5");
            fw = new FileWriter(md5File);
            bw = new BufferedWriter(fw);
            bw.write(md5Code);
        } catch (NoSuchAlgorithmException e) {
        	
        } catch (IOException e) {
        	
        }finally{
        	try {
				bw.close();
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
        return md5File;
    }


    public static String readFileByLines(String fileName) {
        StringBuffer sb = new StringBuffer();
        File file = new File(fileName);
        BufferedReader reader = null;
        try {
            System.out.println("以行为单位读取文件内容，一次读一整行：");
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            int line = 1;
            // 一次读入一行，直到读入null为文件结束
            while ((tempString = reader.readLine()) != null) {
                // 显示行号
                System.out.println("line " + line + ": " + tempString);
                sb.append(tempString);
                line++;
            }
            reader.close();

        } catch (IOException e) {
//            e.printStackTrace();
            log.e("MD5 - " + e.getMessage());
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

    public static int FTPMD5(String sp, String dp) {

        String strs = readFileByLines(sp);
        String dstr = readFileByLines(dp);

        if (strs.equals(dstr)){
            deleteFile(dstr);
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
    public static boolean deleteFile(String fileName) {
        try {
            File file = new File(fileName);
            if (file.exists()) {
                return file.delete();
            }
            return true;
        } catch (Exception e) {
            log.e("util", "delete file error with exception" + e.toString()
                    + "on file:" + fileName);
            return false;
        }
    }
}
