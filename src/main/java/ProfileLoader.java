import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;

public class ProfileLoader {
    private static final Logger LOG = LoggerFactory.getLogger(ProfileLoader.class);
    private String ymlKey;
    private String expireDate;
    private String absOutFilePath;
    private String inputFilePath;

    public byte[] getYmlByte(){
        return constructNewYml();
    }

    public String getAbsFilePath() {
        return absFilePath;
    }

    public String getAbsOutFilePath() {
        return absOutFilePath;
    }

    private String absFilePath;

    public String getYmlKey() {
        return ymlKey;
    }

    public void loadProfile(String path) throws IOException, ParseException {
        File ymlFile = new File(path);
        if (ymlFile.exists()) {
            byte[] fileContent = Files.readAllBytes(ymlFile.toPath());
            String ymlJsonString = new String(fileContent,StandardCharsets.UTF_8);
            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = (JSONObject) jsonParser.parse(ymlJsonString);
            ymlKey = (String) jsonObject.get("key");
            expireDate = (String) jsonObject.get("expireDate");
            inputFilePath = ((String) jsonObject.get("filePath")).replace("\\","/");
            String outFilePath = ((String) jsonObject.get("outFilePath")).replace("\\", "/");
            absOutFilePath = Paths.get(outFilePath).toAbsolutePath().toString();
            absFilePath = Paths.get(inputFilePath).toAbsolutePath().toString();
            Constants.INPUT_ROOT = absFilePath;

            LOG.debug("=== input yml file debug ===");
            LOG.debug("expireDate: " + expireDate);
            LOG.debug("filePath: " + inputFilePath);
            LOG.debug("outFilePath: " + outFilePath);
            LOG.debug("absFilePath: " + absFilePath);
            LOG.debug("============================");

        } else
        {
            LOG.error("Input application.yml Profile Not Found");
            System.exit(1);
        }
    }

    private String expireDateConverter(String expireDate) {
        return expireDate + "T23:59:59.999";
    }

    private byte[] constructNewYml() {
        return ("{\n" +
                "   \"expireDate\":\""+ expireDate + "\"\n" +
                "}").getBytes(StandardCharsets.UTF_8);
    }


    public boolean verity() {
        boolean expired = LocalDateTime.parse(expireDateConverter(expireDate)).isBefore(LocalDateTime.now());
        boolean inputValid = new File(inputFilePath).exists();
        boolean outputValid = new File(String.valueOf(Paths.get(new File(absOutFilePath).getParent()))).isDirectory();
        if (expired) {
            LOG.error("Profile expired");
            return false;
        }
        else if (!inputValid)
        {
            LOG.error("Input file directory do not exist");
            return false;
        }
        else if (!outputValid)
        {
            LOG.error("Output directory do not exist");
            return false;
        }
        return true;
    }
}
