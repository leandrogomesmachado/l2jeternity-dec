package l2e.gameserver.instancemanager;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.commons.util.GameSettings;
import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.data.holder.CharNameHolder;
import l2e.gameserver.data.htm.HtmCache;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.events.cleft.AerialCleftEvent;
import l2e.gameserver.model.holders.SkillHolder;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.network.serverpackets.RadarControl;
import l2e.gameserver.network.serverpackets.ShowMiniMap;
import l2e.gameserver.network.serverpackets.TutorialCloseHtml;
import l2e.gameserver.network.serverpackets.TutorialShowHtml;

public class RevengeManager {
   protected static final Logger _log = Logger.getLogger(RevengeManager.class.getName());
   public static int[] TELEPORT_PRICE = new int[2];

   public RevengeManager() {
      if (Config.ALLOW_REVENGE_SYSTEM) {
         this.load();
         this.cleanUpDatas();
      }
   }

   private final void load() {
      GameSettings revSettings = new GameSettings();
      File file = new File("./config/mods/revenge.ini");

      try (InputStream is = new FileInputStream(file)) {
         revSettings.load(is);
      } catch (Exception var18) {
      }

      String[] price = revSettings.getProperty("TeleportPrice", "4037,10").split(",");

      try {
         TELEPORT_PRICE[0] = Integer.parseInt(price[0]);
         TELEPORT_PRICE[1] = Integer.parseInt(price[1]);
      } catch (NumberFormatException var14) {
      }
   }

   public void checkKiller(Player player, Player killer) {
      if (killer != null && Config.ALLOW_REVENGE_SYSTEM) {
         if (!AerialCleftEvent.getInstance().isStarted() && !AerialCleftEvent.getInstance().isRewarding()
            || !AerialCleftEvent.getInstance().isPlayerParticipant(killer.getObjectId())) {
            if ((killer.getParty() == null || killer.getParty().getUCState() == null) && killer.getUCState() <= 0) {
               if (!killer.isJailed()
                  && !killer.isFlying()
                  && !killer.isInOlympiadMode()
                  && !killer.inObserverMode()
                  && !killer.isInDuel()
                  && !killer.isInsideZone(ZoneId.NO_RESTART)
                  && !killer.isInsideZone(ZoneId.PEACE)
                  && !killer.isInsideZone(ZoneId.PVP)
                  && !killer.isInSiege()
                  && killer.getReflectionId() <= 0
                  && (!killer.isInFightEvent() || !killer.isInsideZone(ZoneId.FUN_PVP))) {
                  player.addRevengeId(killer.getObjectId());
                  killer.removeRevengeId(player.getObjectId());
               }
            }
         }
      }
   }

   public void getRevengeList(Player player) {
      if (Config.ALLOW_REVENGE_SYSTEM) {
         if (player.getRevengeList() != null && !player.getRevengeList().isEmpty()) {
            player.setRevengeActive(true);
            String html = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/mods/revenge/revengeList.htm");
            String template = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/mods/revenge/template.htm");
            String block = "";
            String list = "";

            for(int charId : player.getRevengeList()) {
               String charName = null;
               String className = null;
               boolean isOnline = false;
               Player target = World.getInstance().getPlayer(charId);
               if (target != null) {
                  charName = target.getName();
                  className = Util.className(player, target.getClassId().getId());
                  isOnline = true;
               } else {
                  charName = CharNameHolder.getInstance().getNameById(charId);
                  className = getClassName(player, charId);
                  isOnline = false;
               }

               block = template.replace("%nick%", charName != null ? charName : "");
               block = block.replace("%class%", className != null ? className : "");
               if (isOnline) {
                  block = block.replace(
                     "%button%",
                     "<table><tr><td><button action=\"bypass -h _revenge search "
                        + charId
                        + "\" width=16 height=16 back=\"L2UI_CH3.ShortCut_TooltipMax_Down\" fore=\"L2UI_CH3.ShortCut_TooltipMax\"></td><td><button action=\"bypass -h _revenge kill "
                        + charId
                        + "\" width=16 height=16 back=\"L2UI_CH3.shortcut_next_down\" fore=\"L2UI_CH3.shortcut_next\"></td></tr></table>"
                  );
               } else {
                  block = block.replace("%button%", "<font color=\"FF0000\">OFFLINE</font>");
               }

               list = list + block;
            }

            html = html.replace("%list%", list);
            player.sendPacket(new TutorialShowHtml(html));
         } else {
            player.sendMessage("Your list is empty...");
         }
      }
   }

   public void requestPlayerMenuBypass(final Player player, String bypass) {
      if (bypass.startsWith("_revenge")) {
         StringTokenizer st = new StringTokenizer(bypass, " ");
         st.nextToken();
         String action = st.nextToken();
         String charId = null;

         try {
            charId = st.nextToken();
         } catch (Exception var11) {
         }

         switch(action) {
            case "search":
               if (charId != null) {
                  final Player target = World.getInstance().getPlayer(Integer.parseInt(charId));
                  if (target != null) {
                     new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                           player.sendPacket(new RadarControl(2, 2, target.getX(), target.getY(), target.getZ()));
                           player.sendPacket(new RadarControl(0, 1, target.getX(), target.getY(), target.getZ()));
                        }
                     }, 500L);
                     player.sendPacket(new ShowMiniMap(0));
                  } else {
                     player.sendMessage("Your target offline...");
                  }
               }
               break;
            case "kill":
               if (charId == null) {
                  break;
               }

               Player target = World.getInstance().getPlayer(Integer.parseInt(charId));
               if (target == null) {
                  player.sendMessage("Your target offline...");
               } else {
                  if ((AerialCleftEvent.getInstance().isStarted() || AerialCleftEvent.getInstance().isRewarding())
                     && AerialCleftEvent.getInstance().isPlayerParticipant(target.getObjectId())) {
                     player.sendMessage("Not suitable conditions!");
                     return;
                  }

                  if ((AerialCleftEvent.getInstance().isStarted() || AerialCleftEvent.getInstance().isRewarding())
                     && AerialCleftEvent.getInstance().isPlayerParticipant(player.getObjectId())) {
                     player.sendMessage("Not suitable conditions!");
                     return;
                  }

                  if (player.isDead()
                     || player.isJailed()
                     || player.isFlying()
                     || player.isInOlympiadMode()
                     || player.inObserverMode()
                     || player.isInDuel()
                     || player.isInsideZone(ZoneId.NO_RESTART)
                     || player.getReflectionId() > 0
                     || player.isInFightEvent()) {
                     player.sendMessage("Not suitable conditions!");
                     return;
                  }

                  if (target.isDead()
                     || target.isJailed()
                     || target.isFlying()
                     || target.isInOlympiadMode()
                     || target.inObserverMode()
                     || target.isInDuel()
                     || target.isInsideZone(ZoneId.NO_RESTART)
                     || target.isInsideZone(ZoneId.PEACE)
                     || target.isInsideZone(ZoneId.PVP)
                     || target.isInSiege()
                     || target.getReflectionId() > 0
                     || target.isInFightEvent()
                     || target.isInsideZone(ZoneId.FUN_PVP)) {
                     player.sendMessage("Not suitable conditions!");
                     return;
                  }

                  Location loc = Location.findPointToStay(target.getLocation(), 80, 120, target.getGeoIndex(), false);
                  if (loc != null) {
                     if (TELEPORT_PRICE[0] > 0) {
                        if (player.getInventory().getItemByItemId(TELEPORT_PRICE[0]) == null
                           || player.getInventory().getItemByItemId(TELEPORT_PRICE[0]).getCount() < (long)TELEPORT_PRICE[1]) {
                           player.sendMessage("You need " + TELEPORT_PRICE[1] + " " + Util.getItemName(player, TELEPORT_PRICE[0]) + " to use function!");
                           return;
                        }

                        player.destroyItemByItemId("Rebirth", TELEPORT_PRICE[0], (long)TELEPORT_PRICE[1], player, true);
                     }

                     Skill hide = new SkillHolder(922, 1).getSkill();
                     if (hide != null) {
                        hide.getEffects(player, player, false);
                     }

                     player.teleToLocation(loc.getX(), loc.getY(), loc.getZ(), true);
                     player.setRevengeActive(false);
                     player.sendPacket(TutorialCloseHtml.STATIC_PACKET);
                  }
               }
               break;
            case "close":
               player.setRevengeActive(false);
               player.sendPacket(TutorialCloseHtml.STATIC_PACKET);
               player.getRevengeMark();
         }
      }
   }

   private static String getClassName(Player player, int charId) {
      int classId = -1;

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("SELECT classid FROM characters WHERE charId=?");
      ) {
         statement.setInt(1, charId);

         try (ResultSet rset = statement.executeQuery()) {
            if (rset.next()) {
               classId = rset.getInt(1);
            }
         }
      } catch (Exception var61) {
         _log.log(Level.SEVERE, "Error select classId:", (Throwable)var61);
      }

      return classId >= 0 ? Util.className(player, classId) : null;
   }

   public void cleanUpDatas() {
      Calendar currentTime = Calendar.getInstance();
      long lastUpdate = ServerVariables.getLong("Revenge_Task", 0L);
      if (currentTime.getTimeInMillis() > lastUpdate) {
         Calendar newTime = Calendar.getInstance();
         newTime.setLenient(true);
         newTime.set(11, 6);
         newTime.set(12, 30);
         if (newTime.getTimeInMillis() < currentTime.getTimeInMillis()) {
            newTime.add(5, 1);
         }

         ServerVariables.set("Revenge_Task", newTime.getTimeInMillis());

         try (
            Connection con = DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("DELETE FROM character_variables WHERE name = ?");
         ) {
            statement.setString(1, "revengeList");
            statement.execute();
         } catch (Exception var37) {
            _log.log(Level.SEVERE, "Failed to clean up revenge datas.", (Throwable)var37);
         }

         for(Player player : World.getInstance().getAllPlayers()) {
            if (player != null) {
               player.clenUpRevengeList();
            }
         }

         _log.info("RevengeManager: Info reshresh completed.");
         _log.info("RevengeManager: Next refresh throught: " + Util.formatTime((int)(newTime.getTimeInMillis() - System.currentTimeMillis()) / 1000));
      }
   }

   public static final RevengeManager getInstance() {
      return RevengeManager.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final RevengeManager _instance = new RevengeManager();
   }
}
