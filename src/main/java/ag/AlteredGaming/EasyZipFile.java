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

    private File objRealZipFile;
    private ZipFile objZipFile;

    public EasyZipFile(File objTargetZipFile)
            throws ZipException, IOException, NullPointerException, FileNotFoundException {
        if (objTargetZipFile != null && objTargetZipFile.exists() && !objTargetZipFile.isDirectory()) {
            objRealZipFile = objTargetZipFile;
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

    private ZipEntry findPath(String path) {
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

    public File unzipPath(String path, File toPath) {
        return unzipPathAs(path, toPath.getAbsolutePath().concat(path.substring(path.lastIndexOf("/") + 1)));
    }

    public File unzipPath(String path, String toPath) {
        return unzipPathAs(path, toPath.concat(path.substring(path.lastIndexOf("/") + 1)));
    }

    public File unzipPathAs(String path, String toPath) {
        return unzipPathAs(path, new File(toPath));
    }

    public File unzipPathAs(String path, File toPath) {
        ZipEntry objThePath = findPath(path);
        if (objThePath != null) {
            File objExtractedPath = toPath;
            Logger.getLogger(Inception.class.getName()).fine(objExtractedPath.getPath());
            if (!objThePath.isDirectory()) {
                if (objExtractedPath.isFile()) {
                    if (objExtractedPath.exists()) {
                        objExtractedPath.delete();
                    }
                }
                try {
                    objExtractedPath.createNewFile();
                    InputStream objZipInput = objZipFile.getInputStream(objThePath);
                    OutputStream objZipOutput = new BufferedOutputStream(new FileOutputStream(objExtractedPath));
                    util.copyInputStream(objZipInput, objZipOutput);
                    objZipInput.close();
                    objZipOutput.close();
                    return objExtractedPath;
                } catch (IOException ex) {
                    Logger.getLogger(EasyZipFile.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                if (objExtractedPath.isDirectory()) {
                    if (objExtractedPath.exists()) {
                        objExtractedPath.delete();
                    }
                }
                objExtractedPath.mkdirs();
                return objExtractedPath;
            }
        }
        return null;
    }

    public void open()
            throws ZipException, IOException {
        if (objZipFile != null) {
            close();
        }
        if (objRealZipFile != null && objRealZipFile.exists() && !objRealZipFile.isDirectory()) {
            objZipFile = new ZipFile(objRealZipFile);
        } else {
            //Catch this!
            if (objRealZipFile == null) {
                throw (new NullPointerException());
            }
            if (!objRealZipFile.exists()) {
                throw (new FileNotFoundException());
            }
            if (objRealZipFile.isDirectory()) {
                throw (new FileNotFoundException());
            }
        }
    }

    public void close()
            throws IOException {
        objZipFile.close();
        objZipFile = null;
    }

    public void reload()
            throws IOException {
        close();
        open();
    }
}
