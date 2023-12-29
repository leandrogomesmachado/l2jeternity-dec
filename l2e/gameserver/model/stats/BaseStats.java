package l2e.gameserver.model.stats;

import java.io.File;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilderFactory;
import l2e.gameserver.Config;
import l2e.gameserver.model.actor.Creature;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public enum BaseStats {
   STR(new BaseStats.STR()),
   INT(new BaseStats.INT()),
   DEX(new BaseStats.DEX()),
   WIT(new BaseStats.WIT()),
   CON(new BaseStats.CON()),
   MEN(new BaseStats.MEN()),
   NULL(new BaseStats.NULL());

   private static final Logger _log = Logger.getLogger(BaseStats.class.getName());
   protected static final double[] STRbonus = new double[Config.BASE_STR_LIMIT];
   protected static final double[] INTbonus = new double[Config.BASE_INT_LIMIT];
   protected static final double[] DEXbonus = new double[Config.BASE_DEX_LIMIT];
   protected static final double[] WITbonus = new double[Config.BASE_WIT_LIMIT];
   protected static final double[] CONbonus = new double[Config.BASE_CON_LIMIT];
   protected static final double[] MENbonus = new double[Config.BASE_MEN_LIMIT];
   private final BaseStats.BaseStat _stat;

   public final String getValue() {
      return this._stat.getClass().getSimpleName();
   }

   private BaseStats(BaseStats.BaseStat s) {
      this._stat = s;
   }

   public final double calcBonus(Creature actor) {
      if (actor != null) {
         return actor.isNpc() && !Config.CALC_NPCS_STATS ? 1.0 : this._stat.calcBonus(actor);
      } else {
         return 1.0;
      }
   }

   public double calcChanceMod(Creature actor) {
      return this._stat.calcBonus(actor);
   }

   public static final BaseStats valueOfXml(String name) {
      name = name.intern();

      for(BaseStats s : values()) {
         if (s.getValue().equalsIgnoreCase(name)) {
            return s;
         }
      }

      throw new NoSuchElementException("Unknown name '" + name + "' for enum BaseStats");
   }

   static {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setValidating(false);
      factory.setIgnoringComments(true);
      File file = new File(Config.DATAPACK_ROOT, "data/stats/chars/statBonus.xml");
      Document doc = null;
      if (file.exists()) {
         try {
            doc = factory.newDocumentBuilder().parse(file);
         } catch (Exception var12) {
            _log.log(Level.WARNING, "[BaseStats] Could not parse file: " + var12.getMessage(), (Throwable)var12);
         }

         if (doc != null) {
            for(Node list = doc.getFirstChild(); list != null; list = list.getNextSibling()) {
               if ("list".equalsIgnoreCase(list.getNodeName())) {
                  for(Node stat = list.getFirstChild(); stat != null; stat = stat.getNextSibling()) {
                     String statName = stat.getNodeName();

                     for(Node value = stat.getFirstChild(); value != null; value = value.getNextSibling()) {
                        if ("stat".equalsIgnoreCase(value.getNodeName())) {
                           NamedNodeMap attrs = value.getAttributes();

                           int val;
                           double bonus;
                           try {
                              val = Integer.parseInt(attrs.getNamedItem("value").getNodeValue());
                              bonus = Double.parseDouble(attrs.getNamedItem("bonus").getNodeValue());
                           } catch (Exception var13) {
                              _log.severe("[BaseStats] Invalid stats value: " + value.getNodeValue() + ", skipping");
                              continue;
                           }

                           if ("STR".equalsIgnoreCase(statName)) {
                              STRbonus[val] = bonus;
                           } else if ("INT".equalsIgnoreCase(statName)) {
                              INTbonus[val] = bonus;
                           } else if ("DEX".equalsIgnoreCase(statName)) {
                              DEXbonus[val] = bonus;
                           } else if ("WIT".equalsIgnoreCase(statName)) {
                              WITbonus[val] = bonus;
                           } else if ("CON".equalsIgnoreCase(statName)) {
                              CONbonus[val] = bonus;
                           } else if ("MEN".equalsIgnoreCase(statName)) {
                              MENbonus[val] = bonus;
                           } else {
                              _log.severe("[BaseStats] Invalid stats name: " + statName + ", skipping");
                           }
                        }
                     }
                  }
               }
            }
         }
      } else {
         throw new Error("[BaseStats] File not found: " + file.getName());
      }
   }

   private interface BaseStat {
      double calcBonus(Creature var1);
   }

   protected static final class CON implements BaseStats.BaseStat {
      @Override
      public final double calcBonus(Creature actor) {
         int stat = actor.getCON();
         if (stat >= Config.BASE_CON_LIMIT) {
            if (Config.DEBUG) {
               BaseStats._log.warning("BaseStats: " + actor.getName() + " try exceed CON limit: " + stat);
            }

            stat = Config.BASE_RESET_CON;
         }

         return BaseStats.CONbonus[actor.getCON()];
      }
   }

   protected static final class DEX implements BaseStats.BaseStat {
      @Override
      public final double calcBonus(Creature actor) {
         int stat = actor.getDEX();
         if (stat >= Config.BASE_DEX_LIMIT) {
            if (Config.DEBUG) {
               BaseStats._log.warning("BaseStats: " + actor.getName() + " try exceed DEX limit: " + stat);
            }

            stat = Config.BASE_RESET_DEX;
         }

         return BaseStats.DEXbonus[actor.getDEX()];
      }
   }

   protected static final class INT implements BaseStats.BaseStat {
      @Override
      public final double calcBonus(Creature actor) {
         int stat = actor.getINT();
         if (stat >= Config.BASE_INT_LIMIT) {
            if (Config.DEBUG) {
               BaseStats._log.warning("BaseStats: " + actor.getName() + " try exceed INT limit: " + stat);
            }

            stat = Config.BASE_RESET_INT;
         }

         return BaseStats.INTbonus[actor.getINT()];
      }
   }

   protected static final class MEN implements BaseStats.BaseStat {
      @Override
      public final double calcBonus(Creature actor) {
         int stat = actor.getMEN();
         if (stat >= Config.BASE_MEN_LIMIT) {
            if (Config.DEBUG) {
               BaseStats._log.warning("BaseStats: " + actor.getName() + " try exceed MEN limit: " + stat);
            }

            stat = Config.BASE_RESET_MEN;
         }

         return BaseStats.MENbonus[actor.getMEN()];
      }
   }

   protected static final class NULL implements BaseStats.BaseStat {
      @Override
      public final double calcBonus(Creature actor) {
         return 1.0;
      }
   }

   protected static final class STR implements BaseStats.BaseStat {
      @Override
      public final double calcBonus(Creature actor) {
         int stat = actor.getSTR();
         if (stat >= Config.BASE_STR_LIMIT) {
            if (Config.DEBUG) {
               BaseStats._log.warning("BaseStats: " + actor.getName() + " try exceed STR limit: " + stat);
            }

            stat = Config.BASE_RESET_STR;
         }

         return BaseStats.STRbonus[stat];
      }
   }

   protected static final class WIT implements BaseStats.BaseStat {
      @Override
      public final double calcBonus(Creature actor) {
         int stat = actor.getWIT();
         if (stat >= Config.BASE_WIT_LIMIT) {
            if (Config.DEBUG) {
               BaseStats._log.warning("BaseStats: " + actor.getName() + " try exceed WIT limit: " + stat);
            }

            stat = Config.BASE_RESET_WIT;
         }

         return BaseStats.WITbonus[actor.getWIT()];
      }
   }
}
