import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

public class PackageHelper {
    private static final Logger LOG = LoggerFactory.getLogger(PackageHelper.class);
    public boolean extractTool() throws IOException {
        File file = new File(Constants.DECRYPTION_JAR_NAME);
        if (!file.exists()) {
            InputStream inputStream = (getClass().getResourceAsStream("DecryptionTool-1.0-SNAPSHOT.jar"));
            if(inputStream != null) {
                Files.copy(inputStream, file.getAbsoluteFile().toPath());
                inputStream.close();
                return true;
            }
            return false;
        }
        LOG.warn(Constants.DECRYPTION_JAR_NAME + " already exist, removing old");
        if(file.delete()) {
            return extractTool();
        }
        else
        {
            return false;
        }
    }

    public void unpack() throws IOException, InterruptedException {

        java.util.jar.JarFile jarfile = new java.util.jar.JarFile(new java.io.File(Constants.DECRYPTION_JAR_NAME));
        java.util.Enumeration<java.util.jar.JarEntry> enu= jarfile.entries();
        LOG.info("Unpacking...");
        while(enu.hasMoreElements())
        {
            String destdir = Constants.TEMP_FILE_KEYWORD;
            java.util.jar.JarEntry je = enu.nextElement();



            java.io.File fl = new java.io.File(destdir, je.getName());
            if(!fl.exists())
            {
                fl.getParentFile().mkdirs();
                fl = new java.io.File(destdir, je.getName());
            }
            if(je.isDirectory())
            {
                continue;
            }
            java.io.InputStream is = jarfile.getInputStream(je);
            java.io.FileOutputStream fo = new java.io.FileOutputStream(fl);
            while(is.available()>0)
            {
                fo.write(is.read());
            }
            fo.close();
            is.close();
        }

    }


    public void repackage(String absOutFilePath) throws IOException {
        File result = new File(absOutFilePath);
        if (result.exists()){
            if( result.delete()) {
                LOG.warn("Result Jar already exist, removed old Jar");
            }
            else {
                clean();
                LOG.error("Result Jar already exist, failed removing old jar");
            }
        }
        Path p = Files.createFile(Paths.get(absOutFilePath));
        String sourceDirPath = Constants.TEMP_FILE_KEYWORD;
        try (JarOutputStream jarOutputStream = new JarOutputStream(Files.newOutputStream(p))) {
            Path pp = Paths.get(sourceDirPath).toAbsolutePath();
            Files.walk(pp)
                    .filter(path -> !Files.isDirectory(path))
                    .forEach(path -> {
                        JarEntry jarEntry = new JarEntry(pp.relativize(path).toString().replace("\\", "/"));
                        try {
                            jarOutputStream.putNextEntry(jarEntry);
                            Files.copy(path, jarOutputStream);
                            jarOutputStream.closeEntry();
                        } catch (IOException e) {
                            try {
                                LOG.error("Failed writing jar byte");
                                e.printStackTrace();
                                clean();
                            } catch (IOException ex) {
                                LOG.error("Failed cleaning temp files");
                                ex.printStackTrace();
                            }
                        }
                    });
        } catch (IOException e) {
            LOG.error("Failed creating output jar stream");
            e.printStackTrace();
            clean();
        }
    }

    public void clean() throws IOException {
        File decryptionTool = new File("DecryptionTool-1.0-SNAPSHOT.jar");
        if(decryptionTool.exists()) {
            if (decryptionTool.delete()) {
                LOG.info("removed decryption tool");
            }
        }

        File tempFiles = new File(Constants.TEMP_FILE_KEYWORD);
        if(tempFiles.exists()) {
            deleteDir(tempFiles);
            LOG.info("removed tempFiles");
        }
    }
    private void deleteDir(File file) throws IOException {
        if (file.isDirectory()) {
            File[] entries = file.listFiles();
            if (entries != null) {
                for (File entry : entries) {
                    deleteDir(entry);
                }
            }
        }
        if (!file.delete()) {
            throw new IOException("Failed to delete " + file);
        }
    }

}
