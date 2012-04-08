package ag.AlteredGaming;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public static String[] arraySplit(String split, String delimiter) {
        return split.split(delimiter);
    }

    public static String arrayCombine(String[] array, String delimiter) {
        String output = "";
        for (String word : array) {
            output += (output.isEmpty() ? "" : delimiter) + word;
        }
        return output;
    }

    public static String[] smartSplit(String[] args) {
        return smartSplit(arrayCombine(args, " "));
    }

    public static String[] smartSplit(String text) {
        ArrayList<String> list = new ArrayList<String>();
        Matcher match = Pattern.compile("[^\\s\"']+|\"([^\"]*)\"|'([^']*)'").matcher(text);
        while (match.find()) {
            list.add(match.group(1) != null ? match.group(1) : match.group(2) != null ? match.group(2) : match.group());
        }
        return list.toArray(new String[list.size()]);
    }

    public static String[] reparseArgs(String[] args) {
        return smartSplit(args);
    }
}
