package l2e.commons.util;

public final class FloodProtectorConfig {
   public String FLOOD_PROTECTOR_TYPE;
   public int FLOOD_PROTECTION_INTERVAL;
   public boolean LOG_FLOODING;
   public int PUNISHMENT_LIMIT;
   public String PUNISHMENT_TYPE;
   public long PUNISHMENT_TIME;

   public FloodProtectorConfig(String floodProtectorType) {
      this.FLOOD_PROTECTOR_TYPE = floodProtectorType;
   }

   public static FloodProtectorConfig load(String type, GameSettings properties) {
      FloodProtectorConfig config = new FloodProtectorConfig(type.toUpperCase());
      config.FLOOD_PROTECTION_INTERVAL = Integer.parseInt(properties.getProperty(type + "_FLOOD_PROTECTION_INTERVAL", "1000"));
      config.LOG_FLOODING = Boolean.parseBoolean(properties.getProperty(type + "_LOG_FLOODING", "False"));
      config.PUNISHMENT_LIMIT = Integer.parseInt(properties.getProperty(type + "_PUNISHMENT_LIMIT", "1"));
      config.PUNISHMENT_TYPE = properties.getProperty(type + "_PUNISHMENT_TYPE", "none");
      config.PUNISHMENT_TIME = Long.parseLong(properties.getProperty(type + "_PUNISHMENT_TIME", "0")) * 60000L;
      return config;
   }
}
