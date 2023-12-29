package l2e.gameserver.model.entity.events;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.xml.parsers.DocumentBuilderFactory;
import l2e.commons.geometry.Polygon;
import l2e.commons.time.cron.SchedulingPattern;
import l2e.commons.util.Rnd;
import l2e.gameserver.data.parser.NpcsParser;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.instancemanager.WorldEventManager;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.MonsterInstance;
import l2e.gameserver.model.entity.events.model.template.WorldEventDrop;
import l2e.gameserver.model.entity.events.model.template.WorldEventLocation;
import l2e.gameserver.model.entity.events.model.template.WorldEventReward;
import l2e.gameserver.model.entity.events.model.template.WorldEventSpawn;
import l2e.gameserver.model.entity.events.model.template.WorldEventTemplate;
import l2e.gameserver.model.entity.events.model.template.WorldEventTerritory;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.spawn.SpawnTerritory;
import l2e.gameserver.model.stats.StatsSet;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public abstract class AbstractWorldEvent extends Quest {
   public AbstractWorldEvent(String name, String descr) {
      super(-1, name, descr);
   }

   public abstract boolean eventStart(long var1);

   public abstract boolean eventStop();

   public abstract boolean isEventActive();

   public abstract WorldEventTemplate getEventTemplate();

   public abstract boolean isReloaded();

   public abstract void startTimerTask(long var1, boolean var3);

   protected void updateStatus(String event, long endTime, boolean newEvent) {
      WorldEventManager.getInstance().updateEventExpireTime(event, endTime);

      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement stmt = con.prepareStatement(
            newEvent
               ? "REPLACE INTO events_custom_data (event_name, expire_time) VALUES (?,?)"
               : "UPDATE events_custom_data SET expire_time = ? WHERE event_name = ?"
         );
         if (newEvent) {
            stmt.setString(1, event);
            stmt.setLong(2, endTime);
            stmt.execute();
            stmt.close();
         } else {
            stmt.setLong(1, endTime);
            stmt.setString(2, event);
            stmt.execute();
            stmt.close();
         }
      } catch (Exception var20) {
         _log.warning("Warning: could not update " + event + " database!");
      }
   }

   protected long restoreStatus(String event) {
      return WorldEventManager.getInstance().getEventExpireTime(event);
   }

   protected WorldEventTemplate parseSettings(String event) {
      WorldEventTemplate template = null;

      try {
         File file = new File("data/scripts/events/" + event + "/settings.xml");
         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         factory.setValidating(false);
         factory.setIgnoringComments(true);
         Document doc = factory.newDocumentBuilder().parse(file);
         boolean activate = false;
         String eventName = "";
         String timePattern = null;
         int period = 0;
         Map<Integer, List<WorldEventReward>> variantRequests = new HashMap<>();
         Map<Integer, List<WorldEventReward>> variantRewards = new HashMap<>();
         Map<Integer, List<WorldEventReward>> variantRandomRewards = new HashMap<>();
         List<WorldEventDrop> dropList = new ArrayList<>();
         List<WorldEventSpawn> spawnList = new ArrayList<>();
         List<WorldEventLocation> locations = new ArrayList<>();
         List<WorldEventTerritory> territories = new ArrayList<>();
         StatsSet params = new StatsSet();

         for(Node n1 = doc.getFirstChild(); n1 != null; n1 = n1.getNextSibling()) {
            if ("event".equalsIgnoreCase(n1.getNodeName())) {
               int id = Integer.parseInt(n1.getAttributes().getNamedItem("id").getNodeValue());
               eventName = n1.getAttributes().getNamedItem("name").getNodeValue();
               activate = Boolean.parseBoolean(n1.getAttributes().getNamedItem("activate").getNodeValue());

               for(Node n = n1.getFirstChild(); n != null; n = n.getNextSibling()) {
                  if (n.getNodeName().equalsIgnoreCase("time")) {
                     timePattern = n.getAttributes().getNamedItem("pattern") == null ? null : n.getAttributes().getNamedItem("pattern").getNodeValue();
                     period = Integer.parseInt(n.getAttributes().getNamedItem("period").getNodeValue());
                  } else if (n.getNodeName().equalsIgnoreCase("spawnlist")) {
                     for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
                        if (d.getNodeName().equalsIgnoreCase("npc")) {
                           try {
                              int npcId = Integer.parseInt(d.getAttributes().getNamedItem("id").getNodeValue());
                              int x = Integer.parseInt(d.getAttributes().getNamedItem("x").getNodeValue());
                              int y = Integer.parseInt(d.getAttributes().getNamedItem("y").getNodeValue());
                              int z = Integer.parseInt(d.getAttributes().getNamedItem("z").getNodeValue());
                              int h = d.getAttributes().getNamedItem("heading").getNodeValue() != null
                                 ? Integer.parseInt(d.getAttributes().getNamedItem("heading").getNodeValue())
                                 : 0;
                              if (NpcsParser.getInstance().getTemplate(npcId) == null) {
                                 _log.warning(event + ": " + npcId + " is wrong NPC id, NPC was not added in spawnlist");
                              } else {
                                 spawnList.add(new WorldEventSpawn(npcId, new Location(x, y, z, h)));
                              }
                           } catch (NumberFormatException var35) {
                              _log.warning("Wrong number format in settings.xml block for " + event);
                           }
                        } else if (d.getNodeName().equalsIgnoreCase("location")) {
                           String name = d.getAttributes().getNamedItem("name").getNodeValue();
                           int x = Integer.parseInt(d.getAttributes().getNamedItem("x").getNodeValue());
                           int y = Integer.parseInt(d.getAttributes().getNamedItem("y").getNodeValue());
                           int z = Integer.parseInt(d.getAttributes().getNamedItem("z").getNodeValue());
                           int h = d.getAttributes().getNamedItem("h").getNodeValue() != null
                              ? Integer.parseInt(d.getAttributes().getNamedItem("h").getNodeValue())
                              : 0;
                           locations.add(new WorldEventLocation(name, new Location(x, y, z, h)));
                        } else if (d.getNodeName().equalsIgnoreCase("territory")) {
                           String name = d.getAttributes().getNamedItem("name").getNodeValue();
                           SpawnTerritory t = new SpawnTerritory();
                           t.add(this.parsePolygon0(name, d, d.getAttributes()));
                           territories.add(new WorldEventTerritory(name, t));
                        }
                     }
                  } else if (n.getNodeName().equalsIgnoreCase("droplist")) {
                     for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
                        if (d.getNodeName().equalsIgnoreCase("item")) {
                           int itemId = Integer.parseInt(d.getAttributes().getNamedItem("id").getNodeValue());
                           long min = Long.parseLong(d.getAttributes().getNamedItem("min").getNodeValue());
                           long max = d.getAttributes().getNamedItem("max") != null
                              ? Long.parseLong(d.getAttributes().getNamedItem("max").getNodeValue())
                              : 0L;
                           double chance = Double.parseDouble(d.getAttributes().getNamedItem("chance").getNodeValue());
                           int minLvl = d.getAttributes().getNamedItem("minLvl") != null
                              ? Integer.parseInt(d.getAttributes().getNamedItem("minLvl").getNodeValue())
                              : 1;
                           int maxLvl = d.getAttributes().getNamedItem("maxLvl") != null
                              ? Integer.parseInt(d.getAttributes().getNamedItem("maxLvl").getNodeValue())
                              : 85;
                           dropList.add(new WorldEventDrop(itemId, min, max, chance, minLvl, maxLvl));
                        }
                     }
                  } else if ("add_parameters".equalsIgnoreCase(n.getNodeName())) {
                     for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
                        if ("set".equalsIgnoreCase(d.getNodeName())) {
                           params.set(d.getAttributes().getNamedItem("name").getNodeValue(), d.getAttributes().getNamedItem("value").getNodeValue());
                        }
                     }
                  } else if (n.getNodeName().equalsIgnoreCase("rewardlist")) {
                     for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
                        if (d.getNodeName().equalsIgnoreCase("variant")) {
                           List<WorldEventReward> requestList = new ArrayList<>();
                           List<WorldEventReward> rewardList = new ArrayList<>();
                           List<WorldEventReward> randomRewardList = new ArrayList<>();
                           int variantId = Integer.parseInt(d.getAttributes().getNamedItem("id").getNodeValue());

                           for(Node e = d.getFirstChild(); e != null; e = e.getNextSibling()) {
                              if (e.getNodeName().equalsIgnoreCase("reward")) {
                                 int itemId = Integer.parseInt(e.getAttributes().getNamedItem("id").getNodeValue());
                                 long min = (long)Integer.parseInt(e.getAttributes().getNamedItem("min").getNodeValue());
                                 long max = e.getAttributes().getNamedItem("max") != null
                                    ? (long)Integer.parseInt(e.getAttributes().getNamedItem("max").getNodeValue())
                                    : 0L;
                                 double chance = e.getAttributes().getNamedItem("chance") != null
                                    ? Double.parseDouble(e.getAttributes().getNamedItem("chance").getNodeValue())
                                    : 0.0;
                                 rewardList.add(new WorldEventReward(itemId, min, max, chance));
                              } else if (e.getNodeName().equalsIgnoreCase("request")) {
                                 int itemId = Integer.parseInt(e.getAttributes().getNamedItem("id").getNodeValue());
                                 long min = (long)Integer.parseInt(e.getAttributes().getNamedItem("min").getNodeValue());
                                 long max = e.getAttributes().getNamedItem("max") != null
                                    ? (long)Integer.parseInt(e.getAttributes().getNamedItem("max").getNodeValue())
                                    : 0L;
                                 requestList.add(new WorldEventReward(itemId, min, max, 0.0));
                              } else if (e.getNodeName().equalsIgnoreCase("random")) {
                                 for(Node g = e.getFirstChild(); g != null; g = g.getNextSibling()) {
                                    if (g.getNodeName().equalsIgnoreCase("reward")) {
                                       int itemId = Integer.parseInt(g.getAttributes().getNamedItem("id").getNodeValue());
                                       long min = (long)Integer.parseInt(g.getAttributes().getNamedItem("min").getNodeValue());
                                       long max = g.getAttributes().getNamedItem("max") != null
                                          ? (long)Integer.parseInt(g.getAttributes().getNamedItem("max").getNodeValue())
                                          : 0L;
                                       double chance = g.getAttributes().getNamedItem("chance") != null
                                          ? Double.parseDouble(g.getAttributes().getNamedItem("chance").getNodeValue())
                                          : 0.0;
                                       randomRewardList.add(new WorldEventReward(itemId, min, max, chance));
                                    }
                                 }
                              }
                           }

                           variantRequests.put(variantId, requestList);
                           variantRewards.put(variantId, rewardList);
                           variantRandomRewards.put(variantId, randomRewardList);
                        }
                     }
                  }
               }

               template = new WorldEventTemplate(
                  id,
                  eventName,
                  activate,
                  new SchedulingPattern(timePattern),
                  period,
                  dropList,
                  variantRequests,
                  variantRewards,
                  variantRandomRewards,
                  spawnList,
                  locations,
                  territories,
                  params
               );
            }
         }
      } catch (Exception var36) {
         _log.log(Level.WARNING, event + ": error reading data/scripts/events/" + event + "/settings.xml ! " + var36.getMessage(), (Throwable)var36);
      }

      return template;
   }

   private Polygon parsePolygon0(String name, Node n, NamedNodeMap attrs) {
      Polygon temp = new Polygon();

      for(Node cd = n.getFirstChild(); cd != null; cd = cd.getNextSibling()) {
         if ("add".equalsIgnoreCase(cd.getNodeName())) {
            attrs = cd.getAttributes();
            int x = Integer.parseInt(attrs.getNamedItem("x").getNodeValue());
            int y = Integer.parseInt(attrs.getNamedItem("y").getNodeValue());
            int zmin = Integer.parseInt(attrs.getNamedItem("zmin").getNodeValue());
            int zmax = Integer.parseInt(attrs.getNamedItem("zmax").getNodeValue());
            temp.add(x, y).setZmin(zmin).setZmax(zmax);
         }
      }

      if (!temp.validate()) {
         _log.warning("Invalid polygon: " + name + "{" + temp + "}. File: " + this.getClass().getSimpleName());
      }

      return temp;
   }

   protected long calcEventTime(WorldEventTemplate template) {
      return template != null && template.isActivated() && template.getTimePattern() != null ? template.getTimePattern().next(System.currentTimeMillis()) : 0L;
   }

   protected void checkTimerTask(long time, boolean checkZero) {
      if (time != 0L) {
         long startTime = 0L;
         if (time == -1L && checkZero) {
            startTime = System.currentTimeMillis() + 1000L;
         } else {
            startTime = time;
         }

         this.startTimerTask(startTime, time == -1L);
      }
   }

   public static boolean isTakeRequestItems(Player player, WorldEventTemplate template, int variant) {
      if (template != null) {
         List<WorldEventReward> requestItems = template.getVariantRequests().get(variant);
         if (requestItems != null && !requestItems.isEmpty()) {
            for(WorldEventReward request : requestItems) {
               if (player.getInventory().getItemByItemId(request.getId()) == null
                  || player.getInventory().getItemByItemId(request.getId()).getCount() < request.getMinCount()) {
                  return false;
               }
            }

            for(WorldEventReward request : requestItems) {
               player.destroyItemByItemId("takeItems", request.getId(), request.getMinCount(), player, true);
            }

            return true;
         }
      }

      return false;
   }

   public static void calcReward(Player player, WorldEventTemplate template, int variant) {
      if (template != null) {
         List<WorldEventReward> rewards = template.getVariantRewards().get(variant);
         if (rewards != null && !rewards.isEmpty()) {
            for(WorldEventReward reward : rewards) {
               if (reward != null) {
                  long amount = reward.getMaxCount() != 0L ? Rnd.get(reward.getMinCount(), reward.getMaxCount()) : reward.getMinCount();
                  player.addItem("Event-" + template.getId() + "", reward.getId(), amount, player, true);
               }
            }
         }

         List<WorldEventReward> rndRewards = template.getVariantRandomRewards().get(variant);
         if (rndRewards != null && !rndRewards.isEmpty()) {
            WorldEventReward reward = rndRewards.get(Rnd.get(rndRewards.size()));
            if (reward != null) {
               long amount = reward.getMaxCount() != 0L ? Rnd.get(reward.getMinCount(), reward.getMaxCount()) : reward.getMinCount();
               player.addItem("Event-" + template.getId() + "", reward.getId(), amount, player, true);
            }
         }
      }
   }

   public static void calcRandomReward(Player player, WorldEventTemplate template, int variant, double chance) {
      if (template != null) {
         List<WorldEventReward> rewardList = new ArrayList<>();
         List<WorldEventReward> rndRewards = template.getVariantRandomRewards().get(variant);
         if (rndRewards != null && !rndRewards.isEmpty()) {
            for(WorldEventReward reward : rndRewards) {
               if (reward != null && chance <= reward.getChance()) {
                  rewardList.add(reward);
               }
            }
         }

         if (rewardList != null && !rewardList.isEmpty()) {
            WorldEventReward reward = rewardList.get(Rnd.get(rewardList.size()));
            if (reward != null) {
               long amount = reward.getMaxCount() != 0L ? Rnd.get(reward.getMinCount(), reward.getMaxCount()) : reward.getMinCount();
               player.addItem("Event-" + template.getId() + "", reward.getId(), amount, player, true);
            }
         }
      }
   }

   public static void calcRandomGroupReward(Npc npc, Player player, WorldEventTemplate template, int variant) {
      if (template != null) {
         List<WorldEventReward> rndRewards = template.getVariantRewards().get(variant);
         if (rndRewards != null && !rndRewards.isEmpty()) {
            double chance = 0.0;

            for(WorldEventReward reward : rndRewards) {
               if (reward != null && chance != reward.getChance() && Rnd.chance(reward.getChance())) {
                  chance = reward.getChance();
                  long amount = reward.getMaxCount() != 0L ? Rnd.get(reward.getMinCount(), reward.getMaxCount()) : reward.getMinCount();
                  ((MonsterInstance)npc).dropItem(player, reward.getId(), (long)((int)amount));
               }
            }
         }
      }
   }
}
