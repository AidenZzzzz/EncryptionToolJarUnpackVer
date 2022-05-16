import org.apache.log4j.BasicConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.simple.parser.ParseException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/*
This program take 1 CLI indicating the location of the yml profile
key: AES key
expireDate yyyy-mm-dd
filePath: path of the file directory or file to be encrypted
outFilePath: output location of the executable product jar
 */
public class EncryptionTool {

    private static final Logger LOG = LoggerFactory.getLogger(EncryptionTool.class);

    public static void main(String[] args) throws IOException, InterruptedException {
        BasicConfigurator.configure();

        if(args[0] == null)
        {
            LOG.error("Missing CLI argument application.yml profile path");
            System.exit(1);
        }

        ProfileLoader profileLoader = new ProfileLoader();
        EncryptionHelper encryptionHelper = new EncryptionHelper();
        PackageHelper packageHelper = new PackageHelper();

        String ymlPath = args[0];
        String ymlAbsPath = Paths.get(ymlPath).toAbsolutePath().toString();

        LOG.debug("arg path: " + ymlPath);
        LOG.debug("abs Path: " + ymlAbsPath);

        packageHelper.clean();
        try {
            profileLoader.loadProfile(ymlAbsPath);
            if (profileLoader.verity()) {
                if (packageHelper.extractTool()) {
                        packageHelper.unpack();
                } else {
                    LOG.error("Decryption jar extraction failed");
                    System.exit(1);
                }
                encryptionHelper.generateEncryptYml(profileLoader.getYmlByte(), profileLoader.getYmlKey());
                encryptionHelper.encrypt(profileLoader.getAbsFilePath(), profileLoader.getYmlKey());
            } else {
                LOG.error("yml verification failed");
                packageHelper.clean();
                System.exit(1);
            }

            packageHelper.repackage(profileLoader.getAbsOutFilePath());

        } catch (IOException | InvalidAlgorithmParameterException | IllegalBlockSizeException | NoSuchPaddingException | BadPaddingException | NoSuchAlgorithmException | InvalidKeyException | ParseException e) {
            LOG.error("Encryption failed: " + e);
            e.printStackTrace();
            packageHelper.clean();
            LOG.info("cleaning temp files");
        }
        packageHelper.clean();
        LOG.info("cleaning temp files");
    }
}
