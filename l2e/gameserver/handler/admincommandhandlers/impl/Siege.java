package l2e.gameserver.handler.admincommandhandlers.impl;

import java.util.Calendar;
import java.util.StringTokenizer;
import l2e.commons.util.StringUtil;
import l2e.commons.util.Util;
import l2e.gameserver.SevenSigns;
import l2e.gameserver.data.holder.ClanHolder;
import l2e.gameserver.handler.admincommandhandlers.IAdminCommandHandler;
import l2e.gameserver.instancemanager.AuctionManager;
import l2e.gameserver.instancemanager.CHSiegeManager;
import l2e.gameserver.instancemanager.CastleManager;
import l2e.gameserver.instancemanager.ClanHallManager;
import l2e.gameserver.model.Clan;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.Castle;
import l2e.gameserver.model.entity.ClanHall;
import l2e.gameserver.model.entity.clanhall.SiegableHall;
import l2e.gameserver.model.zone.type.ClanHallZone;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;

public class Siege implements IAdminCommandHandler {
   private static final String[] ADMIN_COMMANDS = new String[]{
      "admin_siege",
      "admin_castle",
      "admin_siegeclanhall",
      "admin_clanhall",
      "admin_add_attacker",
      "admin_add_defender",
      "admin_add_guard",
      "admin_list_siege_clans",
      "admin_clear_siege_list",
      "admin_move_defenders",
      "admin_spawn_doors",
      "admin_endsiege",
      "admin_startsiege",
      "admin_setsiegetime",
      "admin_setcastle",
      "admin_removecastle",
      "admin_clanhall",
      "admin_clanhallset",
      "admin_clanhalldel",
      "admin_clanhallopendoors",
      "admin_clanhallclosedoors",
      "admin_clanhallteleportself"
   };

   @Override
   public boolean useAdminCommand(String command, Player activeChar) {
      StringTokenizer st = new StringTokenizer(command, " ");
      command = st.nextToken();
      Castle castle = null;
      ClanHall clanhall = null;
      if (st.hasMoreTokens()) {
         Player player = null;
         if (activeChar.getTarget() != null && activeChar.getTarget().isPlayer()) {
            player = activeChar.getTarget().getActingPlayer();
         }

         String val = st.nextToken();
         if (command.startsWith("admin_clanhall")) {
            if (Util.isDigit(val)) {
               clanhall = ClanHallManager.getInstance().getClanHallById(Integer.parseInt(val));
               Clan clan = null;
               switch(command) {
                  case "admin_clanhallset":
                     if (player == null || player.getClan() == null) {
                        activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
                        return false;
                     }

                     if (clanhall.getOwnerId() > 0) {
                        activeChar.sendMessage("This Clan Hall is not free!");
                        return false;
                     }

                     clan = player.getClan();
                     if (clan.getHideoutId() > 0) {
                        activeChar.sendMessage("You have already a Clan Hall!");
                        return false;
                     }

                     if (!clanhall.isSiegableHall()) {
                        ClanHallManager.getInstance().setOwner(clanhall.getId(), clan);
                        if (AuctionManager.getInstance().getAuction(clanhall.getId()) != null) {
                           AuctionManager.getInstance().getAuction(clanhall.getId()).deleteAuctionFromDB();
                        }
                     } else {
                        clanhall.setOwner(clan);
                        clan.setHideoutId(clanhall.getId());
                     }
                     break;
                  case "admin_clanhalldel":
                     if (!clanhall.isSiegableHall()) {
                        if (!ClanHallManager.getInstance().isFree(clanhall.getId())) {
                           ClanHallManager.getInstance().setFree(clanhall.getId());
                           AuctionManager.getInstance().initNPC(clanhall.getId());
                        } else {
                           activeChar.sendMessage("This Clan Hall is already free!");
                        }
                     } else {
                        int oldOwner = clanhall.getOwnerId();
                        if (oldOwner > 0) {
                           clanhall.free();
                           clan = ClanHolder.getInstance().getClan(oldOwner);
                           if (clan != null) {
                              clan.setHideoutId(0);
                              clan.broadcastClanStatus();
                           }
                        }
                     }
                     break;
                  case "admin_clanhallopendoors":
                     clanhall.openCloseDoors(true);
                     break;
                  case "admin_clanhallclosedoors":
                     clanhall.openCloseDoors(false);
                     break;
                  case "admin_clanhallteleportself":
                     ClanHallZone zone = clanhall.getZone();
                     if (zone != null) {
                        activeChar.teleToLocation(zone.getSpawnLoc(), true);
                     }
                     break;
                  default:
                     if (!clanhall.isSiegableHall()) {
                        this.showClanHallPage(activeChar, clanhall);
                     } else {
                        this.showSiegableHallPage(activeChar, (SiegableHall)clanhall);
                     }
               }
            }
         } else {
            castle = CastleManager.getInstance().getCastle(val);
            switch(command) {
               case "admin_add_attacker":
                  if (player == null) {
                     activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
                  } else {
                     castle.getSiege().registerAttacker(player, true);
                  }
                  break;
               case "admin_add_defender":
                  if (player == null) {
                     activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
                  } else {
                     castle.getSiege().registerDefender(player, true);
                  }
                  break;
               case "admin_add_guard":
                  if (st.hasMoreTokens()) {
                     val = st.nextToken();
                     if (Util.isDigit(val)) {
                        castle.getSiege().getSiegeGuardManager().addSiegeGuard(activeChar, Integer.parseInt(val));
                        break;
                     }
                  }

                  activeChar.sendMessage("Usage: //add_guard castle npcId");
                  break;
               case "admin_clear_siege_list":
                  castle.getSiege().clearSiegeClan();
                  break;
               case "admin_endsiege":
                  castle.getSiege().endSiege();
                  break;
               case "admin_list_siege_clans":
                  castle.getSiege().listRegisterClan(activeChar);
                  break;
               case "admin_move_defenders":
                  activeChar.sendMessage("Not implemented yet.");
                  break;
               case "admin_setcastle":
                  if (player != null && player.getClan() != null) {
                     castle.setOwner(player.getClan());
                  } else {
                     activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
                  }
                  break;
               case "admin_removecastle":
                  Clan clan = ClanHolder.getInstance().getClan(castle.getOwnerId());
                  if (clan != null) {
                     castle.removeOwner(clan);
                  } else {
                     activeChar.sendMessage("Unable to remove castle.");
                  }
                  break;
               case "admin_setsiegetime":
                  if (st.hasMoreTokens()) {
                     val = st.nextToken();
                     Calendar newAdminSiegeDate = Calendar.getInstance();
                     newAdminSiegeDate.setTimeInMillis(castle.getSiegeDate().getTimeInMillis());
                     if (val.equalsIgnoreCase("day")) {
                        newAdminSiegeDate.set(6, Integer.parseInt(st.nextToken()));
                     } else if (val.equalsIgnoreCase("hour")) {
                        newAdminSiegeDate.set(11, Integer.parseInt(st.nextToken()));
                     } else if (val.equalsIgnoreCase("min")) {
                        newAdminSiegeDate.set(12, Integer.parseInt(st.nextToken()));
                     }

                     if (newAdminSiegeDate.getTimeInMillis() < Calendar.getInstance().getTimeInMillis()) {
                        activeChar.sendMessage("Unable to change siege date.");
                     } else if (newAdminSiegeDate.getTimeInMillis() != castle.getSiegeDate().getTimeInMillis()) {
                        castle.getSiegeDate().setTimeInMillis(newAdminSiegeDate.getTimeInMillis());
                        castle.getSiege().saveSiegeDate();
                        activeChar.sendMessage("Castle siege time for castle " + castle.getName() + " has been changed.");
                     }
                  }

                  this.showSiegeTimePage(activeChar, castle);
                  break;
               case "admin_spawn_doors":
                  castle.spawnDoor();
                  break;
               case "admin_startsiege":
                  castle.getSiege().startSiege();
                  break;
               default:
                  this.showSiegePage(activeChar, castle.getName());
            }
         }
      } else {
         if (command.equals("admin_castle")) {
            this.showCastleSelectPage(activeChar);
            return true;
         }

         if (command.equals("admin_siegeclanhall")) {
            this.showSiegeClanHallSelectPage(activeChar);
            return true;
         }

         if (command.equals("admin_clanhall")) {
            this.showClanHallSelectPage(activeChar);
            return true;
         }
      }

      return true;
   }

   private void showCastleSelectPage(Player activeChar) {
      int i = 0;
      NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
      adminReply.setFile(activeChar, activeChar.getLang(), "data/html/admin/castles.htm");
      StringBuilder cList = new StringBuilder(500);

      for(Castle castle : CastleManager.getInstance().getCastles()) {
         if (castle != null) {
            String name = castle.getName();
            StringUtil.append(
               cList,
               "<td fixwidth=100 align=center><button value=\"" + name + "\" action=\"bypass -h admin_siege ",
               name,
               "\" width=80 height=21 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>"
            );
            ++i;
         }

         if (i > 2) {
            cList.append("</tr><tr>");
            i = 0;
         }
      }

      adminReply.replace("%castles%", cList.toString());
      activeChar.sendPacket(adminReply);
   }

   private void showSiegeClanHallSelectPage(Player activeChar) {
      int i = 0;
      NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
      adminReply.setFile(activeChar, activeChar.getLang(), "data/html/admin/siegehallinfo.htm");
      StringBuilder cList = new StringBuilder(500);

      for(SiegableHall hall : CHSiegeManager.getInstance().getConquerableHalls().values()) {
         if (hall != null) {
            StringUtil.append(
               cList,
               "<td fixwidth=150 align=center><button value=\""
                  + Util.clanHallName(activeChar, hall.getId())
                  + "\" action=\"bypass -h admin_chsiege_siegablehall ",
               String.valueOf(hall.getId()),
               "\" width=140 height=21 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>"
            );
            ++i;
         }

         if (i > 1) {
            cList.append("</tr><tr>");
            i = 0;
         }
      }

      adminReply.replace("%siegableHalls%", cList.toString());
      activeChar.sendPacket(adminReply);
   }

   private void showClanHallSelectPage(Player activeChar) {
      int i = 0;
      NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
      adminReply.setFile(activeChar, activeChar.getLang(), "data/html/admin/hallinfo.htm");
      StringBuilder cList = new StringBuilder(500);

      for(ClanHall clanhall : ClanHallManager.getInstance().getClanHalls().values()) {
         if (clanhall != null) {
            StringUtil.append(
               cList,
               "<td fixwidth=150 align=center><a action=\"bypass -h admin_clanhall ",
               String.valueOf(clanhall.getId()),
               "\">",
               Util.clanHallName(activeChar, clanhall.getId()),
               "</a></td>"
            );
            ++i;
         }

         if (i > 1) {
            cList.append("</tr><tr>");
            i = 0;
         }
      }

      adminReply.replace("%clanhalls%", cList.toString());
      cList.setLength(0);
      i = 0;

      for(ClanHall clanhall : ClanHallManager.getInstance().getFreeClanHalls().values()) {
         if (clanhall != null) {
            StringUtil.append(
               cList,
               "<td fixwidth=150 align=center><a action=\"bypass -h admin_clanhall ",
               String.valueOf(clanhall.getId()),
               "\">",
               Util.clanHallName(activeChar, clanhall.getId()),
               "</a></td>"
            );
            ++i;
         }

         if (i > 1) {
            cList.append("</tr><tr>");
            i = 0;
         }
      }

      adminReply.replace("%freeclanhalls%", cList.toString());
      activeChar.sendPacket(adminReply);
   }

   private void showSiegePage(Player activeChar, String castleName) {
      NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
      adminReply.setFile(activeChar, activeChar.getLang(), "data/html/admin/castle.htm");
      adminReply.replace("%castleName%", castleName);
      activeChar.sendPacket(adminReply);
   }

   private void showSiegeTimePage(Player activeChar, Castle castle) {
      NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
      adminReply.setFile(activeChar, activeChar.getLang(), "data/html/admin/castlesiegetime.htm");
      adminReply.replace("%castleName%", castle.getName());
      adminReply.replace("%time%", castle.getSiegeDate().getTime().toString());
      Calendar newDay = Calendar.getInstance();
      boolean isSunday = false;
      if (newDay.get(7) == 1) {
         isSunday = true;
      } else {
         newDay.set(7, 7);
      }

      if (!SevenSigns.getInstance().isDateInSealValidPeriod(newDay)) {
         newDay.add(5, 7);
      }

      if (isSunday) {
         adminReply.replace("%sundaylink%", String.valueOf(newDay.get(6)));
         adminReply.replace("%sunday%", String.valueOf(newDay.get(2) + "/" + newDay.get(5)));
         newDay.add(5, 13);
         adminReply.replace("%saturdaylink%", String.valueOf(newDay.get(6)));
         adminReply.replace("%saturday%", String.valueOf(newDay.get(2) + "/" + newDay.get(5)));
      } else {
         adminReply.replace("%saturdaylink%", String.valueOf(newDay.get(6)));
         adminReply.replace("%saturday%", String.valueOf(newDay.get(2) + "/" + newDay.get(5)));
         newDay.add(5, 1);
         adminReply.replace("%sundaylink%", String.valueOf(newDay.get(6)));
         adminReply.replace("%sunday%", String.valueOf(newDay.get(2) + "/" + newDay.get(5)));
      }

      activeChar.sendPacket(adminReply);
   }

   private void showClanHallPage(Player activeChar, ClanHall clanhall) {
      NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
      adminReply.setFile(activeChar, activeChar.getLang(), "data/html/admin/clanhall.htm");
      adminReply.replace("%clanhallName%", Util.clanHallName(activeChar, clanhall.getId()));
      adminReply.replace("%clanhallId%", String.valueOf(clanhall.getId()));
      Clan owner = ClanHolder.getInstance().getClan(clanhall.getOwnerId());
      adminReply.replace("%clanhallOwner%", owner == null ? "None" : owner.getName());
      activeChar.sendPacket(adminReply);
   }

   private void showSiegableHallPage(Player activeChar, SiegableHall hall) {
      NpcHtmlMessage msg = new NpcHtmlMessage(5);
      msg.setFile(activeChar, activeChar.getLang(), "data/html/admin/siegablehall.htm");
      msg.replace("%clanhallId%", String.valueOf(hall.getId()));
      msg.replace("%clanhallName%", Util.clanHallName(activeChar, hall.getId()));
      if (hall.getOwnerId() > 0) {
         Clan owner = ClanHolder.getInstance().getClan(hall.getOwnerId());
         msg.replace("%clanhallOwner%", owner != null ? owner.getName() : "No Owner");
      } else {
         msg.replace("%clanhallOwner%", "No Owner");
      }

      activeChar.sendPacket(msg);
   }

   @Override
   public String[] getAdminCommandList() {
      return ADMIN_COMMANDS;
   }
}
