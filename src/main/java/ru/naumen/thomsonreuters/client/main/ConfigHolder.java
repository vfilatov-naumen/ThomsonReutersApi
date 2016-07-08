package ru.naumen.thomsonreuters.client.main;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.io.File;

public class ConfigHolder {
    public static final Config cfg = ConfigFactory.parseFile(new File("custom.conf")).withFallback(ConfigFactory.load());
}
