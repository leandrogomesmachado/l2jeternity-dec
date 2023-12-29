package l2e.gameserver.handler.communityhandlers.impl;

import java.util.StringTokenizer;
import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.data.dao.CharacterCBTeleportDAO;
import l2e.gameserver.data.parser.CommunityTeleportsParser;
import l2e.gameserver.handler.communityhandlers.ICommunityBoardHandler;
import l2e.gameserver.instancemanager.SiegeManager;
import l2e.gameserver.instancemanager.TerritoryWarManager;
import l2e.gameserver.instancemanager.ZoneManager;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.community.CBTeleportTemplate;
import l2e.gameserver.model.actor.templates.player.PcTeleportTemplate;
import l2e.gameserver.model.entity.Siege;
import l2e.gameserver.model.entity.events.cleft.AerialCleftEvent;
import l2e.gameserver.model.service.BotFunctions;
import l2e.gameserver.model.strings.server.ServerMessage;
import l2e.gameserver.model.strings.server.ServerStorage;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.model.zone.type.NoRestartZone;
import l2e.gameserver.model.zone.type.SiegeZone;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.gameserver.network.serverpackets.ShowBoard;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class CommunityTeleport extends AbstractCommunity implements ICommunityBoardHandler {
   public CommunityTeleport() {
      if (Config.DEBUG) {
         _log.info(this.getClass().getSimpleName() + ": Loading all functions.");
      }
   }

   @Override
   public String[] getBypassCommands() {
      return new String[]{"_bbsteleport;"};
   }

   @Override
   public void onBypassCommand(String command, Player player) {
      if (checkCondition(player, false, true)) {
         if ((player.isInSiege() || player.getSiegeState() != 0) && !Config.ALLOW_COMMUNITY_TELEPORT_IN_SIEGE) {
            player.sendMessage(new ServerMessage("Community.ALL_DISABLE", player.getLang()).toString());
         } else if (player.getReflectionId() > 0) {
            player.sendMessage(new ServerMessage("CommunityTeleport.MSG_1", player.getLang()).toString());
         } else {
            if (command.equals("_bbsteleport;")) {
               this.showInfo(player, "index");
            } else if (command.startsWith("_bbsteleport;delete;")) {
               String id = null;
               StringTokenizer st = new StringTokenizer(command, ";");
               st.nextToken();
               st.nextToken();

               try {
                  id = st.nextToken();
               } catch (Exception var18) {
               }

               if (id != null) {
                  int tpId = Integer.parseInt(id);
                  if (player.getCBTeleport(tpId) != null) {
                     player.removeCBTeleport(tpId);
                     CharacterCBTeleportDAO.getInstance().delete(player, tpId);
                     this.showInfo(player, "index");
                  }
               }
            } else if (command.startsWith("_bbsteleport;save;")) {
               String name = null;
               StringTokenizer st = new StringTokenizer(command, ";");
               st.nextToken();
               st.nextToken();

               try {
                  name = st.nextToken();
               } catch (Exception var17) {
               }

               if (name != null) {
                  this.addNewPosition(player, name);
               } else {
                  player.sendMessage(new ServerMessage("CommunityTeleport.MSG_5", player.getLang()).toString());
               }

               this.showInfo(player, "index");
            } else if (command.startsWith("_bbsteleport;tpl;")) {
               String id = null;
               String price = null;
               StringTokenizer st = new StringTokenizer(command, " ");
               st.nextToken();

               try {
                  id = st.nextToken();
               } catch (Exception var16) {
               }

               try {
                  price = st.nextToken();
               } catch (Exception var15) {
               }

               if (Config.BLOCK_TP_AT_SIEGES_FOR_ALL && this.isSiegesIsAcvite()) {
                  player.sendMessage(new ServerMessage("Community.ALL_DISABLE", player.getLang()).toString());
                  return;
               }

               if (id != null && price != null) {
                  PcTeleportTemplate tpl = player.getCBTeleport(Integer.parseInt(id));
                  if (tpl != null) {
                     this.doTeleport(player, tpl.getX(), tpl.getY(), tpl.getZ(), Integer.parseInt(price));
                  }
               }
            } else if (command.startsWith("_bbsteleport;teleport;")) {
               String x = null;
               String y = null;
               String z = null;
               String price = null;
               StringTokenizer st = new StringTokenizer(command, " ");
               st.nextToken();

               try {
                  x = st.nextToken();
               } catch (Exception var14) {
               }

               try {
                  y = st.nextToken();
               } catch (Exception var13) {
               }

               try {
                  z = st.nextToken();
               } catch (Exception var12) {
               }

               try {
                  price = st.nextToken();
               } catch (Exception var11) {
               }

               if (!Config.ALLOW_COMMUNITY_COORDS_TP) {
                  player.sendMessage(new ServerMessage("Community.ALL_DISABLE", player.getLang()).toString());
                  return;
               }

               if (x != null && y != null && z != null && price != null) {
                  if (BotFunctions.getInstance().isAutoTpByCoordsEnable(player)) {
                     BotFunctions.getInstance()
                        .getAutoTeleportByCoords(player, player.getLocation(), new Location(Integer.parseInt(x), Integer.parseInt(y), Integer.parseInt(z)));
                     return;
                  }

                  this.doTeleport(player, Integer.parseInt(x), Integer.parseInt(y), Integer.parseInt(z), Integer.parseInt(price));
               }
            } else if (command.startsWith("_bbsteleport;id;")) {
               StringTokenizer st = new StringTokenizer(command, ";");
               st.nextToken();
               st.nextToken();
               String id = null;

               try {
                  id = st.nextToken();
               } catch (Exception var10) {
               }

               if (id != null) {
                  CBTeleportTemplate template = CommunityTeleportsParser.getInstance().getTemplate(Integer.parseInt(id));
                  if (template != null) {
                     if (player.isCombatFlagEquipped()
                        || player.isDead()
                        || player.isAlikeDead()
                        || player.isCastingNow()
                        || player.isInCombat()
                        || player.isAttackingNow()
                        || player.isInOlympiadMode()
                        || player.isJailed()
                        || player.isFlying()
                        || player.getKarma() > 0 && !template.canPk()
                        || player.isInDuel()
                        || player.getLevel() < template.getMinLvl()
                        || player.getLevel() > template.getMaxLvl()) {
                        player.sendMessage(new ServerMessage("CommunityTeleport.MSG_1", player.getLang()).toString());
                        return;
                     }

                     if (template.isForPremium() && !player.hasPremiumBonus()) {
                        player.sendMessage(new ServerMessage("ServiceBBS.ONLY_FOR_PREMIUM", player.getLang()).toString());
                        return;
                     }

                     if (player.getUCState() > 0
                        || player.isInFightEvent()
                        || (AerialCleftEvent.getInstance().isStarted() || AerialCleftEvent.getInstance().isRewarding())
                           && AerialCleftEvent.getInstance().isPlayerParticipant(player.getObjectId())
                        || player.getReflectionId() != 0) {
                        player.sendMessage(new ServerMessage("CommunityTeleport.MSG_1", player.getLang()).toString());
                        return;
                     }

                     if (player.isInsideZone(ZoneId.NO_RESTART) && !Config.ALLOW_COMMUNITY_TP_NO_RESTART_ZONES) {
                        player.sendMessage(new ServerMessage("CommunityTeleport.MSG_1", player.getLang()).toString());
                        return;
                     }

                     if (player.isInsideZone(ZoneId.SIEGE) && !Config.ALLOW_COMMUNITY_TP_SIEGE_ZONES) {
                        player.sendMessage(new ServerMessage("CommunityTeleport.MSG_1", player.getLang()).toString());
                        return;
                     }

                     NoRestartZone zone = ZoneManager.getInstance()
                        .getZone(template.getLocation().getX(), template.getLocation().getY(), template.getLocation().getZ(), NoRestartZone.class);
                     if (zone != null && !Config.ALLOW_COMMUNITY_TP_NO_RESTART_ZONES) {
                        player.sendMessage(new ServerMessage("CommunityTeleport.MSG_1", player.getLang()).toString());
                        return;
                     }

                     SiegeZone siegeZone = ZoneManager.getInstance()
                        .getZone(template.getLocation().getX(), template.getLocation().getY(), template.getLocation().getZ(), SiegeZone.class);
                     if (siegeZone != null && siegeZone.isActive() && !Config.ALLOW_COMMUNITY_TP_SIEGE_ZONES) {
                        player.sendMessage(new ServerMessage("CommunityTeleport.MSG_1", player.getLang()).toString());
                        return;
                     }

                     if (template.getPrice() != null && player.getLevel() > Config.COMMUNITY_FREE_TP_LVL) {
                        if (player.getInventory().getItemByItemId(template.getPrice().getId()) == null) {
                           player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
                           return;
                        }

                        if (player.getInventory().getItemByItemId(template.getPrice().getId()).getCount() < template.getPrice().getCount()) {
                           player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
                           return;
                        }

                        player.destroyItemByItemId("Teleport", template.getPrice().getId(), template.getPrice().getCount(), player, true);
                     }

                     player.sendPacket(new ShowBoard());
                     if (BotFunctions.getInstance().isAutoTpByIdEnable(player)) {
                        BotFunctions.getInstance()
                           .getAutoTeleportById(
                              player,
                              player.getLocation(),
                              new Location(template.getLocation().getX(), template.getLocation().getY(), template.getLocation().getZ()),
                              0
                           );
                        return;
                     }

                     player.teleToLocation(template.getLocation().getX(), template.getLocation().getY(), template.getLocation().getZ(), true);
                  }
               }
            } else if (command.startsWith("_bbsteleport;page;")) {
               String page = null;
               StringTokenizer st = new StringTokenizer(command, ";");
               st.nextToken();
               st.nextToken();

               try {
                  page = st.nextToken();
               } catch (Exception var9) {
               }

               if (page != null) {
                  this.showInfo(player, page);
               }
            }
         }
      }
   }

   private void doTeleport(Player player, int x, int y, int z, int price) {
      if (player.getUCState() <= 0
         && !player.isCombatFlagEquipped()
         && !player.isDead()
         && !player.isAlikeDead()
         && !player.isCastingNow()
         && !player.isInCombat()
         && !player.isAttackingNow()
         && !player.isInOlympiadMode()
         && !player.isJailed()
         && !player.isFlying()
         && !player.isInDuel()) {
         if (player.isInFightEvent()
            || (AerialCleftEvent.getInstance().isStarted() || AerialCleftEvent.getInstance().isRewarding())
               && AerialCleftEvent.getInstance().isPlayerParticipant(player.getObjectId())
            || player.getReflectionId() != 0) {
            player.sendMessage(new ServerMessage("CommunityTeleport.MSG_1", player.getLang()).toString());
         } else if (player.isInsideZone(ZoneId.NO_RESTART) && !Config.ALLOW_COMMUNITY_TP_NO_RESTART_ZONES) {
            player.sendMessage(new ServerMessage("CommunityTeleport.MSG_1", player.getLang()).toString());
         } else if (player.isInsideZone(ZoneId.SIEGE) && !Config.ALLOW_COMMUNITY_TP_SIEGE_ZONES) {
            player.sendMessage(new ServerMessage("CommunityTeleport.MSG_1", player.getLang()).toString());
         } else {
            NoRestartZone zone = ZoneManager.getInstance().getZone(x, y, z, NoRestartZone.class);
            if (zone != null && !Config.ALLOW_COMMUNITY_TP_NO_RESTART_ZONES) {
               player.sendMessage(new ServerMessage("CommunityTeleport.MSG_1", player.getLang()).toString());
            } else {
               SiegeZone siegeZone = ZoneManager.getInstance().getZone(x, y, z, SiegeZone.class);
               if (siegeZone != null
                  && (siegeZone.getFortId() > 0 || siegeZone.getCastleId() > 0)
                  && siegeZone.isActive()
                  && !Config.ALLOW_COMMUNITY_TP_SIEGE_ZONES) {
                  player.sendMessage(new ServerMessage("CommunityTeleport.MSG_1", player.getLang()).toString());
               } else {
                  if (price > 0 && player.getLevel() > Config.COMMUNITY_FREE_TP_LVL) {
                     if (player.getAdena() < (long)price) {
                        player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_NOT_ENOUGH_ADENA));
                        return;
                     }

                     player.reduceAdena("Teleport", (long)price, player, true);
                  }

                  player.sendPacket(new ShowBoard());
                  player.teleToLocation(x, y, z, true);
               }
            }
         }
      } else {
         player.sendMessage(new ServerMessage("CommunityTeleport.MSG_1", player.getLang()).toString());
      }
   }

   private void showInfo(Player player, String htm) {
      StringBuilder points = new StringBuilder();
      if (player.getCBTeleports() != null && player.getCBTeleports().size() != 0) {
         for(PcTeleportTemplate tpl : player.getCBTeleports()) {
            if (tpl != null) {
               points.append("<table width=200>");
               points.append("<tr>");
               points.append("<td>");
               points.append(
                  "<button value=\""
                     + tpl.getName()
                     + "\" action=\"bypass -h _bbsteleport;tpl; "
                     + tpl.getId()
                     + " "
                     + 100000
                     + "\" width=120 height=26 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">"
               );
               points.append("</td>");
               points.append("<td>");
               points.append(
                  "<button value=\"\" action=\"bypass -h _bbsteleport;delete;"
                     + tpl.getId()
                     + "\" width=32 height=32 back=\"L2UI_CT1.MiniMap_DF_MinusBtn_Red_Down\" fore=\"L2UI_CT1.MiniMap_DF_MinusBtn_Red\">"
               );
               points.append("</td>");
               points.append("</tr>");
               points.append("</table>");
            }
         }
      } else {
         points.append("<table width=200>");
         points.append("<tr>");
         points.append("<td align=center>");
         points.append("<br>" + ServerStorage.getInstance().getString(player.getLang(), "CommunityTeleport.EMPTY_POINTS") + "");
         points.append("</td>");
         points.append("</tr>");
         points.append("</table>");
      }

      NpcHtmlMessage html = new NpcHtmlMessage(5);
      html.setFile(player, player.getLang(), "data/html/community/teleports/" + htm + ".htm");
      html.replace("%tp%", points.toString());
      separateAndSend(html.getHtm(), player);
   }

   private void addNewPosition(Player player, String name) {
      if (player.isDead() || player.isAlikeDead() || player.isCastingNow() || player.isAttackingNow()) {
         player.sendMessage(new ServerMessage("CommunityTeleport.MSG_2", player.getLang()).toString());
      } else if (Config.BLOCK_TP_AT_SIEGES_FOR_ALL && this.isSiegesIsAcvite()) {
         player.sendMessage(new ServerMessage("Community.ALL_DISABLE", player.getLang()).toString());
      } else if (player.isInCombat()) {
         player.sendMessage(new ServerMessage("CommunityTeleport.MSG_3", player.getLang()).toString());
      } else if (player.getUCState() > 0
         || player.isInFightEvent()
         || (AerialCleftEvent.getInstance().isStarted() || AerialCleftEvent.getInstance().isRewarding())
            && AerialCleftEvent.getInstance().isPlayerParticipant(player.getObjectId())
         || player.getReflectionId() > 0
         || player.isInsideZone(ZoneId.NO_RESTART)
         || player.isInsideZone(ZoneId.SWAMP)
         || player.isInsideZone(ZoneId.LANDING)
         || player.isInsideZone(ZoneId.NO_RESTART)
         || player.isInsideZone(ZoneId.SIEGE)
         || player.isInsideZone(ZoneId.MONSTER_TRACK)
         || player.isInsideZone(ZoneId.CASTLE)
         || player.isInsideZone(ZoneId.MOTHER_TREE)
         || player.isInsideZone(ZoneId.SCRIPT)
         || player.isInsideZone(ZoneId.JAIL)
         || player.isFlying()) {
         player.sendMessage(new ServerMessage("CommunityTeleport.MSG_4", player.getLang()).toString());
      } else if (!Util.isMatchingRegexp(name, Config.CNAME_TEMPLATE)) {
         player.sendMessage(new ServerMessage("CommunityTeleport.MSG_7", player.getLang()).toString());
      } else {
         SiegeZone zone = ZoneManager.getInstance().getZone(player, SiegeZone.class);
         if (zone == null || zone.getFortId() <= 0 && zone.getCastleId() <= 0) {
            if (player.isInsideZone(ZoneId.NO_RESTART)) {
               player.sendMessage(new ServerMessage("CommunityTeleport.MSG_4", player.getLang()).toString());
            } else {
               if (!CharacterCBTeleportDAO.getInstance().add(player, name)) {
                  ServerMessage msg = new ServerMessage("CommunityTeleport.MSG_6", player.getLang());
                  msg.add(Config.COMMUNITY_TELEPORT_TABS);
                  player.sendMessage(msg.toString());
               }
            }
         } else {
            player.sendMessage(new ServerMessage("CommunityTeleport.MSG_4", player.getLang()).toString());
         }
      }
   }

   private boolean isSiegesIsAcvite() {
      if (TerritoryWarManager.getInstance().isTWInProgress()) {
         return true;
      } else {
         for(Siege siege : SiegeManager.getInstance().getSieges()) {
            if (siege.getIsInProgress()) {
               return true;
            }
         }

         return false;
      }
   }

   @Override
   public void onWriteCommand(String command, String ar1, String ar2, String ar3, String ar4, String ar5, Player activeChar) {
   }

   public static CommunityTeleport getInstance() {
      return CommunityTeleport.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final CommunityTeleport _instance = new CommunityTeleport();
   }
}
