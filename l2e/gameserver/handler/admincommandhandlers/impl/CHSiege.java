package l2e.gameserver.handler.admincommandhandlers.impl;

import java.util.Calendar;
import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.data.holder.ClanHolder;
import l2e.gameserver.handler.admincommandhandlers.IAdminCommandHandler;
import l2e.gameserver.instancemanager.CHSiegeManager;
import l2e.gameserver.model.Clan;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.clanhall.ClanHallSiegeEngine;
import l2e.gameserver.model.entity.clanhall.SiegableHall;
import l2e.gameserver.network.serverpackets.CastleSiegeInfo;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;

public final class CHSiege implements IAdminCommandHandler {
   private static final String[] COMMANDS = new String[]{
      "admin_chsiege_siegablehall",
      "admin_chsiege_startSiege",
      "admin_chsiege_endsSiege",
      "admin_chsiege_setSiegeDate",
      "admin_chsiege_addAttacker",
      "admin_chsiege_removeAttacker",
      "admin_chsiege_clearAttackers",
      "admin_chsiege_listAttackers",
      "admin_chsiege_forwardSiege"
   };

   @Override
   public String[] getAdminCommandList() {
      return COMMANDS;
   }

   @Override
   public boolean useAdminCommand(String command, Player activeChar) {
      String[] split = command.split(" ");
      SiegableHall hall = null;
      if (Config.ALT_DEV_NO_SCRIPTS) {
         activeChar.sendMessage("AltDevNoScripts = true; Clan Hall Sieges are disabled!");
         return false;
      } else if (split.length < 2) {
         activeChar.sendMessage("You have to specify the hall id at least");
         return false;
      } else if ((hall = this.getHall(split[1], activeChar)) == null) {
         activeChar.sendMessage("Couldnt find he desired siegable hall (" + split[1] + ")");
         return false;
      } else if (hall.getSiege() == null) {
         activeChar.sendMessage("The given hall dont have any attached siege!");
         return false;
      } else {
         if (split[0].equals(COMMANDS[1])) {
            if (hall.isInSiege()) {
               activeChar.sendMessage("The requested clan hall is alredy in siege!");
            } else {
               Clan owner = ClanHolder.getInstance().getClan(hall.getOwnerId());
               if (owner != null) {
                  hall.free();
                  owner.setHideoutId(0);
                  hall.addAttacker(owner);
               }

               hall.getSiege().startSiege();
            }
         } else if (split[0].equals(COMMANDS[2])) {
            if (!hall.isInSiege()) {
               activeChar.sendMessage("The requested clan hall isnt in siege!");
            } else {
               hall.getSiege().endSiege();
            }
         } else if (split[0].equals(COMMANDS[3])) {
            if (!hall.isRegistering()) {
               activeChar.sendMessage("Cannot change siege date while hall is in siege");
            } else if (split.length < 3) {
               activeChar.sendMessage("The date format is incorrect. Try again.");
            } else {
               String[] rawDate = split[2].split(";");
               if (rawDate.length < 2) {
                  activeChar.sendMessage("You have to specify this format DD-MM-YYYY;HH:MM");
               } else {
                  String[] day = rawDate[0].split("-");
                  String[] hour = rawDate[1].split(":");
                  if (day.length >= 3 && hour.length >= 2) {
                     int d = this.parseInt(day[0]);
                     int month = this.parseInt(day[1]) - 1;
                     int year = this.parseInt(day[2]);
                     int h = this.parseInt(hour[0]);
                     int min = this.parseInt(hour[1]);
                     if ((month != 2 || d <= 28) && d <= 31 && d > 0 && month > 0 && month <= 12 && year >= Calendar.getInstance().get(1)) {
                        if (h > 0 && h <= 24 && min >= 0 && min < 60) {
                           Calendar c = Calendar.getInstance();
                           c.set(1, year);
                           c.set(2, month);
                           c.set(5, d);
                           c.set(11, h);
                           c.set(12, min);
                           c.set(13, 0);
                           if (c.getTimeInMillis() > System.currentTimeMillis()) {
                              activeChar.sendMessage(Util.clanHallName(activeChar, hall.getId()) + " siege: " + c.getTime().toString());
                              hall.setNextSiegeDate(c.getTimeInMillis());
                              hall.getSiege().updateSiege();
                              hall.updateDb();
                           } else {
                              activeChar.sendMessage("The given time is in the past!");
                           }
                        } else {
                           activeChar.sendMessage("Wrong hour/minutes gave!");
                        }
                     } else {
                        activeChar.sendMessage("Wrong day/month/year gave!");
                     }
                  } else {
                     activeChar.sendMessage("Incomplete day, hour or both!");
                  }
               }
            }
         } else if (split[0].equals(COMMANDS[4])) {
            if (hall.isInSiege()) {
               activeChar.sendMessage("The clan hall is in siege, cannot add attackers now.");
               return false;
            }

            Clan attacker = null;
            if (split.length < 3) {
               GameObject rawTarget = activeChar.getTarget();
               Player target = null;
               if (rawTarget == null) {
                  activeChar.sendMessage("You must target a clan member of the attacker!");
               } else if (!(rawTarget instanceof Player)) {
                  activeChar.sendMessage("You must target a player with clan!");
               } else if ((target = (Player)rawTarget).getClan() == null) {
                  activeChar.sendMessage("Your target does not have any clan!");
               } else if (hall.getSiege().checkIsAttacker(target.getClan())) {
                  activeChar.sendMessage("Your target's clan is alredy participating!");
               } else {
                  attacker = target.getClan();
               }
            } else {
               Clan rawClan = ClanHolder.getInstance().getClanByName(split[2]);
               if (rawClan == null) {
                  activeChar.sendMessage("The given clan does not exist!");
               } else if (hall.getSiege().checkIsAttacker(rawClan)) {
                  activeChar.sendMessage("The given clan is alredy participating!");
               } else {
                  attacker = rawClan;
               }
            }

            if (attacker != null) {
               hall.addAttacker(attacker);
            }
         } else if (split[0].equals(COMMANDS[5])) {
            if (hall.isInSiege()) {
               activeChar.sendMessage("The clan hall is in siege, cannot remove attackers now.");
               return false;
            }

            if (split.length < 3) {
               GameObject rawTarget = activeChar.getTarget();
               Player target = null;
               if (rawTarget == null) {
                  activeChar.sendMessage("You must target a clan member of the attacker!");
               } else if (!(rawTarget instanceof Player)) {
                  activeChar.sendMessage("You must target a player with clan!");
               } else if ((target = (Player)rawTarget).getClan() == null) {
                  activeChar.sendMessage("Your target does not have any clan!");
               } else if (!hall.getSiege().checkIsAttacker(target.getClan())) {
                  activeChar.sendMessage("Your target's clan is not participating!");
               } else {
                  hall.removeAttacker(target.getClan());
               }
            } else {
               Clan rawClan = ClanHolder.getInstance().getClanByName(split[2]);
               if (rawClan == null) {
                  activeChar.sendMessage("The given clan does not exist!");
               } else if (!hall.getSiege().checkIsAttacker(rawClan)) {
                  activeChar.sendMessage("The given clan is not participating!");
               } else {
                  hall.removeAttacker(rawClan);
               }
            }
         } else if (split[0].equals(COMMANDS[6])) {
            if (hall.isInSiege()) {
               activeChar.sendMessage("The requested hall is in siege right now, cannot clear attacker list!");
            } else {
               ClanHallSiegeEngine siegable = hall.getSiege();
               siegable.getAttackers().clear();
            }
         } else if (split[0].equals(COMMANDS[7])) {
            activeChar.sendPacket(new CastleSiegeInfo(hall));
         } else if (split[0].equals(COMMANDS[8])) {
            ClanHallSiegeEngine siegable = hall.getSiege();
            siegable.cancelSiegeTask();
            switch(hall.getSiegeStatus()) {
               case REGISTERING:
                  siegable.prepareOwner();
                  break;
               case WAITING_BATTLE:
                  siegable.startSiege();
                  break;
               case RUNNING:
                  siegable.endSiege();
            }
         }

         this.sendSiegableHallPage(activeChar, split[1], hall);
         return false;
      }
   }

   private SiegableHall getHall(String id, Player gm) {
      int ch = this.parseInt(id);
      if (ch == 0) {
         gm.sendMessage("Wrong clan hall id, unparseable id!");
         return null;
      } else {
         SiegableHall hall = CHSiegeManager.getInstance().getSiegableHall(ch);
         if (hall == null) {
            gm.sendMessage("Couldnt find the clan hall.");
         }

         return hall;
      }
   }

   private int parseInt(String st) {
      int val = 0;

      try {
         val = Integer.parseInt(st);
      } catch (NumberFormatException var4) {
         var4.printStackTrace();
      }

      return val;
   }

   private void sendSiegableHallPage(Player activeChar, String hallId, SiegableHall hall) {
      NpcHtmlMessage msg = new NpcHtmlMessage(5);
      msg.setFile(activeChar, activeChar.getLang(), "data/html/admin/siegablehall.htm");
      msg.replace("%clanhallId%", hallId);
      msg.replace("%clanhallName%", Util.clanHallName(activeChar, this.parseInt(hallId)));
      if (hall.getOwnerId() > 0) {
         Clan owner = ClanHolder.getInstance().getClan(hall.getOwnerId());
         if (owner != null) {
            msg.replace("%clanhallOwner%", owner.getName());
         } else {
            msg.replace("%clanhallOwner%", "No Owner");
         }
      } else {
         msg.replace("%clanhallOwner%", "No Owner");
      }

      activeChar.sendPacket(msg);
   }
}
