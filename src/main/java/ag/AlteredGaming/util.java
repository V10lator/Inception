package ag.AlteredGaming;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.bind.TypeConstraintException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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

    public static String substitude(String On, String[] What, String[] With) {
        if (What.length != With.length) {
            throw new java.lang.ArrayIndexOutOfBoundsException();
        }

        for (int count = 0; count < What.length; count++) {
            if (What[count].contains(",")) {
                String[] WhatArgs = What[count].split(",");
                for (String arg : WhatArgs) {
                    On = On.replace(arg, With[count]);
                }
            } else {
                On = On.replace(What[count], With[count]);
            }
        }

        return On;
    }

    public static String colorize(String On) {
        return ChatColor.translateAlternateColorCodes("&".charAt(0), On);
    }

    public static <T> CommandSender senderFromName(T player) {
        if (player instanceof CommandSender) {
            return (CommandSender) player;
        } else if (player instanceof Player) {
            return (Player) player;
        } else if (player instanceof String) {
            return Bukkit.getPlayerExact((String) player);
        } else {
            throw new TypeConstraintException("'player' must be CommandSender, Player or String");
        }
    }
}
