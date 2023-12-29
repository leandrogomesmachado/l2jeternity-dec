package l2e.scripts.instances.KamalokaSolo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.Map;
import l2e.gameserver.Config;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.data.parser.ReflectionParser;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.instancemanager.ReflectionManager;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.reflection.ReflectionTemplate;
import l2e.gameserver.model.actor.templates.reflection.ReflectionWorld;
import l2e.gameserver.model.entity.Reflection;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.scripts.instances.AbstractReflection;

public class KamalokaSolo extends AbstractReflection {
   public KamalokaSolo(String name, String descr) {
      super(name, descr);
   }

   private final synchronized void enterInstance(Player player, Npc npc, KamalokaSolo.KamaParam param) {
      if (this.enterInstance(player, npc, new KamalokaSolo.KamaWorld(), param.instanceId)) {
         ReflectionWorld world = ReflectionManager.getInstance().getPlayerWorld(player);
         long instanceOver = 1800000L + System.currentTimeMillis();
         KamalokaSolo.KamaPlayer kp = new KamalokaSolo.KamaPlayer();
         kp.instance = world.getReflectionId();
         kp.timeStamp = instanceOver;
         ((KamalokaSolo.KamaWorld)world).param = param;
         ((KamalokaSolo.KamaWorld)world).KamalokaPlayers.put(player.getName(), kp);
         this.startQuestTimer("time", 1200000L, null, player);
      }
   }

   @Override
   protected void onTeleportEnter(Player player, ReflectionTemplate template, ReflectionWorld world, boolean firstEntrance) {
      if (firstEntrance) {
         world.addAllowed(player.getObjectId());
         player.getAI().setIntention(CtrlIntention.IDLE);
         player.setReflectionId(world.getReflectionId());
         Location teleLoc = template.getTeleportCoord();
         player.teleToLocation(teleLoc, true);
         if (player.hasSummon()) {
            player.getSummon().getAI().setIntention(CtrlIntention.IDLE);
            player.getSummon().setReflectionId(world.getReflectionId());
            player.getSummon().teleToLocation(teleLoc, true);
         }
      } else {
         player.getAI().setIntention(CtrlIntention.IDLE);
         player.setReflectionId(world.getReflectionId());
         Location teleLoc = template.getTeleportCoord();
         player.teleToLocation(teleLoc, true);
         if (player.hasSummon()) {
            player.getSummon().getAI().setIntention(CtrlIntention.IDLE);
            player.getSummon().setReflectionId(world.getReflectionId());
            player.getSummon().teleToLocation(teleLoc, true);
         }
      }
   }

   public String onAdvEventTo(String event, Npc npc, Player player, String qn, int[] REW1, int[] REW2) {
      QuestState st = player.getQuestState(qn);
      if (st == null) {
         return null;
      } else {
         int instanceId = player.getReflectionId();
         String playerName = player.getName();
         if (instanceId == 0) {
            instanceId = player.getKamalokaId();
         }

         Location rewPosition = null;
         if (ReflectionManager.getInstance().reflectionExist(instanceId)) {
            Reflection instanceObj = ReflectionManager.getInstance().getReflection(instanceId);
            KamalokaSolo.KamaWorld world = (KamalokaSolo.KamaWorld)ReflectionManager.getInstance().getWorld(instanceId);
            if (world == null) {
               return "";
            } else {
               rewPosition = world.param.rewPosition;
               if (event.equalsIgnoreCase("time")) {
                  if (!player.isOnline()) {
                     return null;
                  }

                  instanceObj.setDuration(600000);
                  instanceObj.cleanupNpcs();
                  addSpawn(32485, rewPosition.getX(), rewPosition.getY(), rewPosition.getZ(), 0, false, 0L, false, instanceId);
                  if (!world.KamalokaPlayers.containsKey(playerName)) {
                     return "";
                  }

                  KamalokaSolo.KamaPlayer kp = world.KamalokaPlayers.get(playerName);
                  if (kp == null) {
                     return null;
                  }

                  if (kp.count < 10) {
                     kp.reward = 1;
                  } else {
                     kp.reward = kp.points / kp.count + 1;
                     int reward = kp.reward;
                     int count = kp.count;
                     ReflectionTemplate template = ReflectionParser.getInstance().getReflectionId(world.param.instanceId);
                     if (template != null) {
                        try (Connection con = DatabaseFactory.getInstance().getConnection()) {
                           int code = template.getMinLevel() * 100 + template.getMaxLevel();
                           PreparedStatement statement = con.prepareStatement("INSERT INTO kamaloka_results (char_name,Level,Grade,Count) VALUES (?,?,?,?)");
                           statement.setString(1, playerName);
                           statement.setInt(2, code);
                           statement.setInt(3, reward);
                           statement.setInt(4, count);
                           statement.executeUpdate();
                           statement.close();
                        } catch (Exception var31) {
                           _log.warning("Error while inserting Kamaloka data: " + var31);
                        }
                     }
                  }
               } else if (event.equalsIgnoreCase("Reward")) {
                  KamalokaSolo.KamaPlayer kp = world.KamalokaPlayers.get(playerName);
                  if (kp != null && !kp.rewarded) {
                     kp.rewarded = true;
                     int r = kp.reward - 1;
                     st.giveItems(REW1[r * 2], (long)REW1[r * 2 + 1]);
                     st.giveItems(REW2[r * 2], (long)REW2[r * 2 + 1]);
                     NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
                     html.setFile(player, "data/scripts/instances/KamalokaSolo/" + player.getLang() + "/1.htm");
                     html.replace("%kamaloka%", qn);
                     player.sendPacket(html);
                  }
               } else if (event.equalsIgnoreCase("Exit")) {
                  instanceObj.cleanupPlayers();
                  ReflectionManager.getInstance().destroyReflection(instanceId);
               }

               return null;
            }
         } else {
            return "";
         }
      }
   }

   public String onEnterTo(Npc npc, Player player, KamalokaSolo.KamaParam param) {
      QuestState st = player.getQuestState(param.qn);
      if (st == null) {
         st = this.newQuestState(player);
      }

      if (Config.ALT_KAMALOKA_SOLO_PREMIUM_ONLY && !player.hasPremiumBonus()) {
         NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
         html.setFile(player, "data/scripts/instances/KamalokaSolo/" + player.getLang() + "/32484-no.htm");
         player.sendPacket(html);
         return null;
      } else {
         this.enterInstance(player, npc, param);
         return "";
      }
   }

   public String onTalkTo(Npc npc, Player player, String qn) {
      QuestState st = player.getQuestState(qn);
      if (st == null) {
         st = this.newQuestState(player);
      }

      ReflectionWorld tmpworld = ReflectionManager.getInstance().getWorld(npc.getReflectionId());
      if (!(tmpworld instanceof KamalokaSolo.KamaWorld)) {
         return "";
      } else {
         String playerName = player.getName();
         KamalokaSolo.KamaWorld world = (KamalokaSolo.KamaWorld)tmpworld;
         KamalokaSolo.KamaPlayer kp = world.KamalokaPlayers.get(playerName);
         if (kp == null) {
            return "";
         } else {
            if (npc.getId() == 32485) {
               if (!world.KamalokaPlayers.containsKey(playerName)) {
                  return "";
               }

               String msgReward = "0.htm";
               if (!kp.rewarded) {
                  switch(kp.reward) {
                     case 1:
                        msgReward = "D.htm";
                        break;
                     case 2:
                        msgReward = "C.htm";
                        break;
                     case 3:
                        msgReward = "B.htm";
                        break;
                     case 4:
                        msgReward = "A.htm";
                        break;
                     case 5:
                        msgReward = "S.htm";
                        break;
                     default:
                        msgReward = "1.htm";
                  }
               }

               NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
               html.setFile(player, "data/scripts/instances/KamalokaSolo/" + player.getLang() + "/" + msgReward);
               html.replace("%kamaloka%", qn);
               player.sendPacket(html);
            }

            return null;
         }
      }
   }

   public String onKillTo(Npc npc, Player player, boolean isPet, String qn, int KANABION, int[] APPEAR) {
      if (player == null) {
         return "";
      } else {
         QuestState st = player.getQuestState(qn);
         if (st == null) {
            st = this.newQuestState(player);
         }

         ReflectionWorld tmpworld = ReflectionManager.getInstance().getWorld(npc.getReflectionId());
         if (!(tmpworld instanceof KamalokaSolo.KamaWorld)) {
            return "";
         } else {
            String playerName = player.getName();
            KamalokaSolo.KamaWorld world = (KamalokaSolo.KamaWorld)tmpworld;
            if (!world.KamalokaPlayers.containsKey(playerName)) {
               return "";
            } else {
               KamalokaSolo.KamaPlayer kp = world.KamalokaPlayers.get(playerName);
               int npcId = npc.getId();
               if (npcId == KANABION) {
                  ++kp.count;
               } else if (npcId == APPEAR[0]) {
                  ++kp.points;
               } else if (npcId == APPEAR[1]) {
                  kp.points += 2;
               }

               return "";
            }
         }
      }
   }

   public static void main(String[] args) {
      new KamalokaSolo("qn", "instance");
   }

   public class KamaParam {
      public String qn = "KamalokaSolo";
      public int instanceId = 0;
      public Location rewPosition = null;
   }

   protected class KamaPlayer {
      public int instance = 0;
      public long timeStamp = 0L;
      public int points = 0;
      public int count = 0;
      public int reward = 0;
      public boolean rewarded = false;
   }

   protected class KamaWorld extends ReflectionWorld {
      public Map<String, KamalokaSolo.KamaPlayer> KamalokaPlayers = new HashMap<>();
      public KamalokaSolo.KamaParam param = KamalokaSolo.this.new KamaParam();

      public KamaWorld() {
      }
   }
}
