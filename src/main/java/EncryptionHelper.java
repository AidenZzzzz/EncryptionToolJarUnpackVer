import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

public class EncryptionHelper {
    private static final Logger LOG = LoggerFactory.getLogger(EncryptionHelper.class);

    private String key;

    public void encrypt(String unencryptedFileDirectory,  String key) throws InvalidAlgorithmParameterException, IllegalBlockSizeException, NoSuchPaddingException, IOException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        this.key = key;
        File dir = new File(unencryptedFileDirectory);
        if(dir.isDirectory()) {
            handleFolder(dir);
        }
        else
        {
            handleFile(dir);
        }
    }

    private void handleFile(File file) throws IOException {
        if (file.exists()) {
            byte[] fileContent = Files.readAllBytes(file.toPath());
            byte[] encryptedFileContent = Aes.encrypt(fileContent, key);

            String tempOutputFileName =
                    Constants.TEMP_FILE_KEYWORD +
                            File.separator + Constants.OUTPUT_DIR_KEYWORD + file.getPath().replace(file.getParent(),"");
            LOG.info("writing: "+ tempOutputFileName);
            File currentFile = new File(tempOutputFileName);
            currentFile.getParentFile().mkdirs();
            assert encryptedFileContent != null;
            Files.write(Paths.get(tempOutputFileName).toAbsolutePath(), encryptedFileContent);

        } else
        {
            LOG.error("Unencrypted File: " + file.getPath() + " Not Found" );
        }
    }


    public void generateEncryptYml(byte[] ymlByte, String key) throws IOException {
        Files.createDirectories(Paths.get(Constants.TEMP_FILE_KEYWORD));
        String outputFileName =  Constants.TEMP_FILE_KEYWORD + File.separator + Constants.PROFILE_KEYWORD;
        byte[] encryptedYml = Aes.encrypt(ymlByte,key);
        assert encryptedYml != null;
        Files.write(Paths.get(outputFileName).toAbsolutePath(), encryptedYml);
        LOG.info("Encrypted yml written to directory: " + Paths.get(outputFileName).toAbsolutePath());
    }

    /*
    recursively handle all file in the folder
     */
    public void handleFolder(final File folder) throws IOException {
        if(folder.exists()) {
            for (final File fileEntry : Objects.requireNonNull(folder.listFiles())) {
                if (fileEntry.isDirectory()) {
                    handleFolder(fileEntry);
                } else {
                    writeEncryptedFile(fileEntry, key );
                }
            }
        }
        else
        {
            LOG.error("ERROR: Unencrypted file folder not found");
        }

    }


    /*
    write the encrypted file content to the temp folder
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void writeEncryptedFile(File file, String key) throws IOException {
        if (file.exists()) {
            byte[] fileContent = Files.readAllBytes(file.toPath());
            byte[] encryptedFileContent = Aes.encrypt(fileContent, key);

            String tempOutputFileName =
                    Constants.TEMP_FILE_KEYWORD +
                    File.separator + Constants.OUTPUT_DIR_KEYWORD + file.getPath().replace(Constants.INPUT_ROOT,"");
            LOG.info("writing: "+ tempOutputFileName);
            File currentFile = new File(tempOutputFileName);
            currentFile.getParentFile().mkdirs();
            assert encryptedFileContent != null;
            Files.write(Paths.get(tempOutputFileName).toAbsolutePath(), encryptedFileContent);

        } else
        {
            LOG.error("Unencrypted File: " + file.getPath() + " Not Found" );
        }
    }


}
