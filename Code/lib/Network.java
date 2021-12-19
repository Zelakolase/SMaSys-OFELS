package lib;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.function.Function;
import java.util.zip.GZIPOutputStream;

public class Network {
    /*
     * GZIP Compression shit
     */
    public static byte[] compress(byte[] data) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length);
        GZIPOutputStream gzip = new GZIPOutputStream(bos);
        gzip.write(data);
        gzip.close();
        byte[] compressed = bos.toByteArray();
        bos.close();
        return compressed;
    }

    /*
     * HTTP Write Response Function
     */
    public static void write(DataOutputStream dOS, byte[] res, String content, String S,boolean gzip,String addedResHeaders) {
        try {
            if (gzip) res = compress(res);
            dOS.write((S + "\r\n").getBytes());
            dOS.write("Server: SimplyJServer 1.0\r\n".getBytes());
            dOS.write((addedResHeaders).getBytes());
            dOS.write(("Connection: Keep-Alive\r\n").getBytes());
            if (gzip)
                dOS.write("Content-Encoding: gzip\r\n".getBytes());
            if(content == null) content = "ERR";
            if(res == null) res = new byte[] {0}; 
            if (content.equals("text/html")) {
                dOS.write(("Content-Type: " + content + ";charset=UTF-8\r\n").getBytes());
            } else {
                dOS.write(("Content-Type: " + content + "\r\n").getBytes());
            }
            dOS.write(("Content-Length: " + res.length + "\r\n\r\n").getBytes());
            dOS.write(res);
            dOS.flush();
            dOS.close();
        } catch (Exception e) {
        	log.e(e,Network.class.getName(),"write");
        }
    }

    /*
     * Reads from socket into ArrayList
     */
    public static ArrayList<Byte> read(DataInputStream dIS,int MAX_REQ_SIZE) {
        ArrayList<Byte> result = new ArrayList<Byte>();
        int byteCounter = 0;
        try {
            do {
            	if(byteCounter < MAX_REQ_SIZE*1000) {
                result.add(dIS.readNBytes(1)[0]);
                byteCounter ++;
            	}else {
            		
            	}
            } while (dIS.available() > 0);

        } catch (IOException e) {
        	log.e(e,Network.class.getName(),"read");
        }
        return result;
    }
    public static byte[] ManRead(DataInputStream dIS, int bytestoread) {
    	try {
			return dIS.readNBytes(bytestoread);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return null;
    }
}
