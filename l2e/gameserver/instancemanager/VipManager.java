package l2e.gameserver.instancemanager;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.player.vip.VipNpcTemplate;
import l2e.gameserver.model.actor.templates.player.vip.VipTemplate;
import l2e.gameserver.model.holders.ItemHolder;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class VipManager {
   private static final Logger _log = Logger.getLogger(VipManager.class.getName());
   private final List<VipTemplate> _templates = new ArrayList<>();
   private final List<VipNpcTemplate> _npcList = new ArrayList<>();
   private ScheduledFuture<?> _refreshTask = null;
   private int _maxLevel = 0;
   private long _maxPoints = 0L;

   public VipManager() {
      this._templates.clear();
      this._npcList.clear();
      this.templaterParser();
      this.searchMaxLevel();
      this.checkTimeTask();
   }

   private void templaterParser() {
      try {
         File file = new File(Config.DATAPACK_ROOT + "/data/stats/services/vipTemplates.xml");
         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         factory.setValidating(false);
         factory.setIgnoringComments(true);
         Document doc1 = factory.newDocumentBuilder().parse(file);

         for(Node n1 = doc1.getFirstChild(); n1 != null; n1 = n1.getNextSibling()) {
            if ("list".equalsIgnoreCase(n1.getNodeName())) {
               for(Node d1 = n1.getFirstChild(); d1 != null; d1 = d1.getNextSibling()) {
                  if ("vip".equalsIgnoreCase(d1.getNodeName())) {
                     int id = Integer.parseInt(d1.getAttributes().getNamedItem("level").getNodeValue());
                     long points = Long.parseLong(d1.getAttributes().getNamedItem("needPoints").getNodeValue());
                     double expRate = 1.0;
                     double spRate = 1.0;
                     double adenaRate = 1.0;
                     double dropRate = 1.0;
                     double dropRaidRate = 1.0;
                     double spoilRate = 1.0;
                     double epRate = 1.0;
                     int enchantRate = 0;
                     List<ItemHolder> items = new ArrayList<>();
                     List<ItemHolder> requestItems = new ArrayList<>();

                     for(Node s1 = d1.getFirstChild(); s1 != null; s1 = s1.getNextSibling()) {
                        if ("set".equalsIgnoreCase(s1.getNodeName())) {
                           if (s1.getAttributes().getNamedItem("expRate") != null) {
                              expRate = Double.parseDouble(s1.getAttributes().getNamedItem("expRate").getNodeValue());
                           }

                           if (s1.getAttributes().getNamedItem("spRate") != null) {
                              spRate = Double.parseDouble(s1.getAttributes().getNamedItem("spRate").getNodeValue());
                           }

                           if (s1.getAttributes().getNamedItem("adenaRate") != null) {
                              adenaRate = Double.parseDouble(s1.getAttributes().getNamedItem("adenaRate").getNodeValue());
                           }

                           if (s1.getAttributes().getNamedItem("dropRate") != null) {
                              dropRate = Double.parseDouble(s1.getAttributes().getNamedItem("dropRate").getNodeValue());
                           }

                           if (s1.getAttributes().getNamedItem("dropRaidRate") != null) {
                              dropRaidRate = Double.parseDouble(s1.getAttributes().getNamedItem("dropRaidRate").getNodeValue());
                           }

                           if (s1.getAttributes().getNamedItem("spoilRate") != null) {
                              spoilRate = Double.parseDouble(s1.getAttributes().getNamedItem("spoilRate").getNodeValue());
                           }

                           if (s1.getAttributes().getNamedItem("epRate") != null) {
                              epRate = Double.parseDouble(s1.getAttributes().getNamedItem("epRate").getNodeValue());
                           }

                           if (s1.getAttributes().getNamedItem("enchantRate") != null) {
                              enchantRate = Integer.parseInt(s1.getAttributes().getNamedItem("enchantRate").getNodeValue());
                           }
                        } else if ("daily".equalsIgnoreCase(s1.getNodeName())) {
                           if (s1.getAttributes().getNamedItem("itemId") != null && s1.getAttributes().getNamedItem("amount") != null) {
                              int itemId = Integer.parseInt(s1.getAttributes().getNamedItem("itemId").getNodeValue());
                              long itemAmount = Long.parseLong(s1.getAttributes().getNamedItem("amount").getNodeValue());
                              items.add(new ItemHolder(itemId, itemAmount));
                           }
                        } else if ("requestItems".equalsIgnoreCase(s1.getNodeName())
                           && s1.getAttributes().getNamedItem("itemId") != null
                           && s1.getAttributes().getNamedItem("amount") != null) {
                           int itemId = Integer.parseInt(s1.getAttributes().getNamedItem("itemId").getNodeValue());
                           long itemAmount = Long.parseLong(s1.getAttributes().getNamedItem("amount").getNodeValue());
                           requestItems.add(new ItemHolder(itemId, itemAmount));
                        }
                     }

                     this._templates
                        .add(
                           new VipTemplate(id, points, expRate, spRate, adenaRate, dropRate, dropRaidRate, spoilRate, epRate, enchantRate, items, requestItems)
                        );
                  }

                  if ("npc".equalsIgnoreCase(d1.getNodeName())) {
                     int npcId = Integer.parseInt(d1.getAttributes().getNamedItem("id").getNodeValue());
                     long points = Long.parseLong(d1.getAttributes().getNamedItem("points").getNodeValue());
                     this._npcList.add(new VipNpcTemplate(npcId, points));
                  }
               }
            }
         }

         _log.info("VipManager: Loaded " + this._templates.size() + " vip templates.");
         _log.info("VipManager: Loaded " + this._npcList.size() + " npc templates.");
      } catch (DOMException | ParserConfigurationException | SAXException | NumberFormatException var30) {
         _log.log(Level.WARNING, "VipManager: contributions.xml could not be initialized.", (Throwable)var30);
      } catch (IllegalArgumentException | IOException var31) {
         _log.log(Level.WARNING, "VipManager: IOException or IllegalArgumentException.", (Throwable)var31);
      }
   }

   public void checkTimeTask() {
      Calendar currentTime = Calendar.getInstance();
      long lastUpdate = ServerVariables.getLong("RefreshVipTime", 0L);
      if (currentTime.getTimeInMillis() > lastUpdate) {
         this.cleanVip();
      } else {
         this._refreshTask = ThreadPoolManager.getInstance().schedule(new VipManager.RefreshTask(), lastUpdate - System.currentTimeMillis());
      }
   }

   public void cleanVip() {
      for(Player player : World.getInstance().getAllPlayers()) {
         if (player != null) {
            player.setVipLevel(0);
            player.setVipPoints(0L);
         }
      }

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("DELETE FROM character_variables WHERE name='vipLevel'");
      ) {
         statement.execute();
      } catch (Exception var93) {
         _log.log(Level.WARNING, "Failed update vip level.", (Throwable)var93);
      }

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("DELETE FROM character_variables WHERE name='vipPoints'");
      ) {
         statement.execute();
      } catch (Exception var89) {
         _log.log(Level.WARNING, "Failed update vip points.", (Throwable)var89);
      }

      Calendar cal = Calendar.getInstance();
      cal.add(2, 1);
      cal.set(5, 1);
      cal.set(11, 6);
      cal.set(12, 30);
      ServerVariables.set("RefreshVipTime", cal.getTimeInMillis());
      if (this._refreshTask != null) {
         this._refreshTask.cancel(false);
         this._refreshTask = null;
      }

      this._refreshTask = ThreadPoolManager.getInstance().schedule(new VipManager.RefreshTask(), cal.getTimeInMillis() - System.currentTimeMillis());
      _log.info("VipManager: Reshresh completed.");
      _log.info("VipManager: Next refresh throught: " + Util.formatTime((int)(cal.getTimeInMillis() - System.currentTimeMillis()) / 1000));
   }

   public VipTemplate getVipLevel(int level) {
      for(VipTemplate template : this._templates) {
         if (template != null && template.getId() == level) {
            return template;
         }
      }

      return null;
   }

   public VipNpcTemplate getVipNpcTemplate(int id) {
      for(VipNpcTemplate template : this._npcList) {
         if (template != null && template.getId() == id) {
            return template;
         }
      }

      return null;
   }

   public List<VipTemplate> getVipTemplates() {
      return this._templates;
   }

   public void searchMaxLevel() {
      for(VipTemplate template : this._templates) {
         if (template != null && template.getId() > this._maxLevel) {
            this._maxLevel = template.getId();
            this._maxPoints = template.getPoints();
         }
      }
   }

   public long getMaxPoints() {
      return this._maxPoints;
   }

   public int getMaxLevel() {
      return this._maxLevel;
   }

   public static final VipManager getInstance() {
      return VipManager.SingletonHolder._instance;
   }

   private class RefreshTask implements Runnable {
      public RefreshTask() {
      }

      @Override
      public void run() {
         VipManager.this.cleanVip();
      }
   }

   private static class SingletonHolder {
      protected static final VipManager _instance = new VipManager();
   }
}
