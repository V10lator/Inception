package ag.AlteredGaming;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 *
 * @author Xaymar
 */
public class EasyZipFile {

    private ZipFile objZipFile;

    public EasyZipFile(File objTargetZipFile)
            throws ZipException, IOException, NullPointerException, FileNotFoundException {
        if (objTargetZipFile != null && objTargetZipFile.exists() && !objTargetZipFile.isDirectory()) {
            objZipFile = new ZipFile(objTargetZipFile);
        } else {
            //Catch this!
            if (objTargetZipFile == null) {
                throw (new NullPointerException());
            }
            if (!objTargetZipFile.exists()) {
                throw (new FileNotFoundException());
            }
            if (objTargetZipFile.isDirectory()) {
                throw (new FileNotFoundException());
            }
        }
    }

    private ZipEntry findFile(String path) {
        Enumeration enmZipEntries = objZipFile.entries();
        File objFoundFile = null;
        while (enmZipEntries.hasMoreElements()) {
            ZipEntry objZipEntry = (ZipEntry) enmZipEntries.nextElement();
            if (objZipEntry.getName().equals(path)) {
                return objZipEntry;
            }
        }
        return null;
    }

    public File unzipFile(String path, String toPath) {
        ZipEntry objTheFile = findFile(path);
        File objExtractedFile = null;
        if (objTheFile != null) {
            objExtractedFile = new File(toPath + objTheFile.getName());
            if (objExtractedFile.exists()) {
                objExtractedFile.delete();
            }

            objExtractedFile.mkdirs();
            if (!objTheFile.isDirectory()) {
                try {
                    objExtractedFile.createNewFile();
                    InputStream objZipInput = objZipFile.getInputStream(objTheFile);
                    OutputStream objZipOutput = new BufferedOutputStream(new FileOutputStream(objExtractedFile));
                    util.copyInputStream(objZipInput, objZipOutput);
                    objZipInput.close();
                    objZipOutput.close();
                    return objExtractedFile;
                } catch (IOException ex) {
                    Logger.getLogger(EasyZipFile.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return null;
    }
}
