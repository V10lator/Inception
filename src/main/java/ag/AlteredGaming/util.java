/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ag.AlteredGaming;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author Xaymar
 */
public class util {

    public static void copyInputStream(InputStream in, OutputStream out)
            throws IOException {
        copyInputStream(in, out, 1024);
    }

    public static void copyInputStream(InputStream in, OutputStream out, int BufferLength)
            throws IOException {
        byte[] buffer = new byte[BufferLength];
        int len;
        while ((len = in.read(buffer)) >= 0) {
            out.write(buffer, 0, len);
        }
    }
}
