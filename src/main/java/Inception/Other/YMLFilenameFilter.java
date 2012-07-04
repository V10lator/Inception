/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Inception.Other;

import java.io.File;
import java.io.FilenameFilter;

/**
 *
 * @author Xaymar
 */
public class YMLFilenameFilter
        implements FilenameFilter {

    @Override
    public boolean accept(File dir, String name) {
        if ((name.substring(name.lastIndexOf(".") + 1)).toLowerCase().equals("yml")) {
            return true;
        }
        return false;
    }
}
