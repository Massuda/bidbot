package org.bid;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public class PropertiesLoader {
    public String user;
    public String url;
    public Properties prop = new Properties();

    public PropertiesLoader(){
        try {
            //        InputStream configStream = getClass().getResourceAsStream("/config.properties");
            String currentDirectory = System.getProperty("user.dir");
            String fileName = "config.properties";
            String filePath = currentDirectory + File.separator + fileName;
            prop.load(Files.newInputStream(Paths.get(filePath)));
            this.user = prop.getProperty("username");
            this.url = prop.getProperty("url");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
