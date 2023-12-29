package l2e.gameserver.instancemanager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import l2e.commons.threading.RunnableImpl;
import l2e.commons.util.Rnd;
import l2e.gameserver.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.data.parser.SpawnParser;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.npc.LakfiNpcTemplate;
import l2e.gameserver.model.actor.templates.npc.LakfiRewardTemplate;
import l2e.gameserver.model.spawn.Spawner;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class LakfiManager {
   private static final Logger _log = Logger.getLogger(LakfiManager.class.getName());
   protected static Map<Integer, List<LakfiNpcTemplate>> _lakfiList = new HashMap<>();
   private final List<Npc> _npcs = new ArrayList<>();
   private ScheduledFuture<?> _despawnTask = null;

   public LakfiManager() {
      if (Config.LAKFI_ENABLED) {
         _lakfiList.clear();
         this._npcs.clear();
         this.loadRewards();
         this.initSpawns();
         _log.info(this.getClass().getSimpleName() + ": Activated " + this._npcs.size() + " lakfi-lakfi npcs.");
      }
   }

   private void loadRewards() {
      try {
         File file = new File(Config.DATAPACK_ROOT + "/data/stats/npcs/lakfiRewards.xml");
         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         factory.setValidating(false);
         factory.setIgnoringComments(true);
         Document doc1 = factory.newDocumentBuilder().parse(file);
         int counter = 0;

         for(Node n1 = doc1.getFirstChild(); n1 != null; n1 = n1.getNextSibling()) {
            if ("list".equalsIgnoreCase(n1.getNodeName())) {
               for(Node d1 = n1.getFirstChild(); d1 != null; d1 = d1.getNextSibling()) {
                  if ("lakfi".equalsIgnoreCase(d1.getNodeName())) {
                     int level = Integer.parseInt(d1.getAttributes().getNamedItem("level").getNodeValue());
                     List<LakfiNpcTemplate> npcList = new ArrayList<>();

                     for(Node s1 = d1.getFirstChild(); s1 != null; s1 = s1.getNextSibling()) {
                        if ("npc".equalsIgnoreCase(s1.getNodeName())) {
                           List<LakfiRewardTemplate> rewards = new ArrayList<>();
                           int npcId = Integer.parseInt(s1.getAttributes().getNamedItem("id").getNodeValue());

                           for(Node z1 = s1.getFirstChild(); z1 != null; z1 = z1.getNextSibling()) {
                              if ("item".equalsIgnoreCase(z1.getNodeName())) {
                                 int itemId = Integer.parseInt(z1.getAttributes().getNamedItem("id").getNodeValue());
                                 long min = Long.parseLong(z1.getAttributes().getNamedItem("min").getNodeValue());
                                 long max = z1.getAttributes().getNamedItem("max") != null
                                    ? Long.parseLong(z1.getAttributes().getNamedItem("max").getNodeValue())
                                    : min;
                                 double chance = z1.getAttributes().getNamedItem("chance") != null
                                    ? Double.parseDouble(z1.getAttributes().getNamedItem("chance").getNodeValue())
                                    : 100.0;
                                 rewards.add(new LakfiRewardTemplate(itemId, min, max, chance));
                                 ++counter;
                              }
                           }

                           npcList.add(new LakfiNpcTemplate(npcId, rewards));
                        }
                     }

                     _lakfiList.put(level, npcList);
                  }
               }
            }
         }

         _log.info("LakfiManager: Loaded " + counter + " lakfi reward templates.");
      } catch (DOMException | ParserConfigurationException | SAXException | NumberFormatException var20) {
         _log.log(Level.WARNING, "LakfiManager: lakfiRewards.xml could not be initialized.", (Throwable)var20);
      } catch (IllegalArgumentException | IOException var21) {
         _log.log(Level.WARNING, "LakfiManager: IOException or IllegalArgumentException.", (Throwable)var21);
      }
   }

   public void getLakfiRewards(int lakfiLvl, Attackable actor, Player killer, double chance) {
      List<LakfiNpcTemplate> npcs = _lakfiList.get(lakfiLvl);
      if (npcs != null) {
         for(LakfiNpcTemplate template : npcs) {
            if (actor.getId() == template.getId()) {
               List<LakfiRewardTemplate> rewards = template.getRewards();
               if (rewards != null) {
                  List<LakfiRewardTemplate> curRewards = new ArrayList<>();

                  for(LakfiRewardTemplate tp : rewards) {
                     if (tp != null && tp.getChance() >= chance) {
                        curRewards.add(tp);
                     }
                  }

                  if (!curRewards.isEmpty()) {
                     Comparator<LakfiRewardTemplate> statsComparator = new LakfiManager.LakfiRewardInfo();
                     Collections.sort(curRewards, statsComparator);
                     LakfiRewardTemplate tpl = curRewards.get(curRewards.size() - 1);
                     if (tpl != null) {
                        long amount = tpl.getMinCount() != tpl.getMaxCount() ? Rnd.get(tpl.getMinCount(), tpl.getMaxCount()) : tpl.getMinCount();
                        actor.dropItem(killer, tpl.getId(), (long)((int)amount));
                     }
                  }
               }
            }
         }
      }
   }

   private void initSpawns() {
      this._npcs.clear();
      List<Spawner> spawns = new ArrayList<>();

      for(int i = 1; i < 10; ++i) {
         Spawner lakfiSpawn = this.getRndSpawn(SpawnParser.getInstance().getSpawn("lakkfi_" + i + ""));
         if (lakfiSpawn != null) {
            spawns.add(lakfiSpawn);
         }
      }

      if (spawns != null && !spawns.isEmpty()) {
         this.initSpawnGroups(spawns);
      }

      this._despawnTask = ThreadPoolManager.getInstance().schedule(new LakfiManager.DespawnTask(), (long)(Config.TIME_CHANGE_SPAWN * 60000));
   }

   public void stopTimer() {
      if (this._despawnTask != null) {
         this._despawnTask.cancel(false);
         this._despawnTask = null;
      }

      this._npcs.clear();
   }

   private void initSpawnGroups(List<Spawner> spawns) {
      for(Spawner spawn : spawns) {
         Npc npc = spawn.doSpawn();
         if (npc != null) {
            this._npcs.add(npc);
         }
      }
   }

   private Spawner getRndSpawn(List<Spawner> list) {
      if (list != null && !list.isEmpty()) {
         int index = Rnd.get(0, list.size() - 1);
         return list.get(index);
      } else {
         return null;
      }
   }

   public static final LakfiManager getInstance() {
      return LakfiManager.SingletonHolder._instance;
   }

   private class DespawnTask extends RunnableImpl {
      private DespawnTask() {
      }

      @Override
      public void runImpl() {
         for(Npc npc : LakfiManager.this._npcs) {
            if (npc != null) {
               npc.deleteMe();
            }
         }

         LakfiManager.this.initSpawns();
      }
   }

   private static class LakfiRewardInfo implements Comparator<LakfiRewardTemplate> {
      private LakfiRewardInfo() {
      }

      public int compare(LakfiRewardTemplate o1, LakfiRewardTemplate o2) {
         return Double.compare(o2.getChance(), o1.getChance());
      }
   }

   private static class SingletonHolder {
      protected static final LakfiManager _instance = new LakfiManager();
   }
}
