package edu.telemarketer.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Be careful!
 * Created by hason on 15/9/17.
 */
public class PropertiesHelper {

    private static Logger logger = Logger.getLogger("PropertiesHelper");
    private static Properties properties;

    static {
        properties = new Properties();
        String setting = ClassLoader.getSystemResource("setting.properties").getPath();
        try (FileInputStream fileStream = new FileInputStream(setting)) {
            properties.load(fileStream);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "配置文件无法读取 : " + setting);
            throw new IllegalStateException("配置文件无法读取" + setting);
        }
    }

    public static String getProperty(String name) {
        return properties.getProperty(name);
    }

    public static String getProperty(String name, String defaultValue) {
        String result = properties.getProperty(name);
        if (result == null) {
            return defaultValue;
        }
        return result;
    }
}
