package l2e.gameserver.model.actor.instance;

import java.util.List;
import java.util.Locale;
import l2e.commons.util.StringUtil;
import l2e.gameserver.idfactory.IdFactory;
import l2e.gameserver.instancemanager.games.MonsterRaceManager;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.HistoryInfoTemplate;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.DeleteObject;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class RaceManagerInstance extends Npc {
   protected static final int[] TICKET_PRICES = new int[]{100, 500, 1000, 5000, 10000, 20000, 50000, 100000};

   public RaceManagerInstance(int objectId, NpcTemplate template) {
      super(objectId, template);
      this.setInstanceType(GameObject.InstanceType.RaceManagerInstance);
   }

   @Override
   public void onBypassFeedback(Player player, String command) {
      if (command.startsWith("BuyTicket")) {
         if (MonsterRaceManager.getInstance().getCurrentRaceState() != MonsterRaceManager.RaceState.ACCEPTING_BETS) {
            player.sendPacket(SystemMessageId.MONSRACE_TICKETS_NOT_AVAILABLE);
            super.onBypassFeedback(player, "Chat 0");
            return;
         }

         int val = Integer.parseInt(command.substring(10));
         if (val == 0) {
            player.setRace(0, 0);
            player.setRace(1, 0);
         }

         if (val == 10 && player.getRace(0) == 0 || val == 20 && player.getRace(0) == 0 && player.getRace(1) == 0) {
            val = 0;
         }

         int npcId = this.getTemplate().getId();
         NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
         if (val < 10) {
            html.setFile(player, player.getLang(), this.getHtmlPath(npcId, 2));

            for(int i = 0; i < 8; ++i) {
               int n = i + 1;
               String search = "Mob" + n;
               html.replace(search, player.getNpcName(MonsterRaceManager.getInstance().getMonsters()[i].getTemplate()));
            }

            String search = "No1";
            if (val == 0) {
               html.replace(search, "");
            } else {
               html.replace(search, (long)val);
               player.setRace(0, val);
            }
         } else if (val < 20) {
            if (player.getRace(0) == 0) {
               return;
            }

            html.setFile(player, player.getLang(), this.getHtmlPath(npcId, 3));
            html.replace("0place", (long)player.getRace(0));
            String search = "Mob1";
            String replace = player.getNpcName(MonsterRaceManager.getInstance().getMonsters()[player.getRace(0) - 1].getTemplate());
            html.replace(search, replace);
            search = "0adena";
            if (val == 10) {
               html.replace(search, "");
            } else {
               html.replace(search, (long)TICKET_PRICES[val - 11]);
               player.setRace(1, val - 10);
            }
         } else {
            if (val != 20) {
               if (player.getRace(0) != 0 && player.getRace(1) != 0) {
                  int ticket = player.getRace(0);
                  int priceId = player.getRace(1);
                  if (!player.reduceAdena("Race", (long)TICKET_PRICES[priceId - 1], this, true)) {
                     return;
                  }

                  player.setRace(0, 0);
                  player.setRace(1, 0);
                  ItemInstance item = new ItemInstance(IdFactory.getInstance().getNextId(), 4443);
                  item.setCount(1L);
                  item.setEnchantLevel(MonsterRaceManager.getInstance().getRaceNumber());
                  item.setCustomType1(ticket);
                  item.setCustomType2(TICKET_PRICES[priceId - 1] / 100);
                  player.addItem("Race", item, player, false);
                  player.sendPacket(
                     SystemMessage.getSystemMessage(SystemMessageId.ACQUIRED_S1_S2)
                        .addNumber(MonsterRaceManager.getInstance().getRaceNumber())
                        .addItemName(4443)
                  );
                  MonsterRaceManager.getInstance().setBetOnLane(ticket, (long)TICKET_PRICES[priceId - 1], true);
                  super.onBypassFeedback(player, "Chat 0");
                  return;
               }

               return;
            }

            if (player.getRace(0) == 0 || player.getRace(1) == 0) {
               return;
            }

            html.setFile(player, player.getLang(), this.getHtmlPath(npcId, 4));
            html.replace("0place", (long)player.getRace(0));
            String search = "Mob1";
            String replace = player.getNpcName(MonsterRaceManager.getInstance().getMonsters()[player.getRace(0) - 1].getTemplate());
            html.replace(search, replace);
            search = "0adena";
            int price = TICKET_PRICES[player.getRace(1) - 1];
            html.replace(search, (long)price);
            search = "0tax";
            int tax = 0;
            html.replace(search, 0L);
            search = "0total";
            int total = price + 0;
            html.replace(search, (long)total);
         }

         html.replace("1race", (long)MonsterRaceManager.getInstance().getRaceNumber());
         html.replace("%objectId%", (long)this.getObjectId());
         player.sendPacket(html);
      } else if (command.equals("ShowOdds")) {
         if (MonsterRaceManager.getInstance().getCurrentRaceState() == MonsterRaceManager.RaceState.ACCEPTING_BETS) {
            player.sendPacket(SystemMessageId.MONSRACE_NO_PAYOUT_INFO);
            super.onBypassFeedback(player, "Chat 0");
            return;
         }

         NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
         html.setFile(player, player.getLang(), this.getHtmlPath(this.getTemplate().getId(), 5));

         for(int i = 0; i < 8; ++i) {
            int n = i + 1;
            html.replace("Mob" + n, player.getNpcName(MonsterRaceManager.getInstance().getMonsters()[i].getTemplate()));
            double odd = MonsterRaceManager.getInstance().getOdds().get(i);
            html.replace("Odd" + n, odd > 0.0 ? String.format(Locale.ENGLISH, "%.1f", odd) : "&$804;");
         }

         html.replace("1race", (long)MonsterRaceManager.getInstance().getRaceNumber());
         html.replace("%objectId%", (long)this.getObjectId());
         player.sendPacket(html);
      } else if (command.equals("ShowInfo")) {
         NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
         html.setFile(player, player.getLang(), this.getHtmlPath(this.getTemplate().getId(), 6));

         for(int i = 0; i < 8; ++i) {
            int n = i + 1;
            String search = "Mob" + n;
            html.replace(search, player.getNpcName(MonsterRaceManager.getInstance().getMonsters()[i].getTemplate()));
         }

         html.replace("%objectId%", (long)this.getObjectId());
         player.sendPacket(html);
      } else if (command.equals("ShowTickets")) {
         StringBuilder sb = new StringBuilder();

         for(ItemInstance ticket : player.getInventory().getAllItemsByItemId(4443)) {
            if (ticket.getEnchantLevel() != MonsterRaceManager.getInstance().getRaceNumber()) {
               StringUtil.append(
                  sb,
                  "<tr><td><a action=\"bypass -h npc_%objectId%_ShowTicket ",
                  ticket.getObjectId(),
                  "\">",
                  ticket.getEnchantLevel(),
                  " Race Number</a></td><td align=right><font color=\"LEVEL\">",
                  ticket.getCustomType1(),
                  "</font> Number</td><td align=right><font color=\"LEVEL\">",
                  ticket.getCustomType2() * 100,
                  "</font> Adena</td></tr>"
               );
            }
         }

         NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
         html.setFile(player, player.getLang(), this.getHtmlPath(this.getTemplate().getId(), 7));
         html.replace("%tickets%", sb.toString());
         html.replace("%objectId%", (long)this.getObjectId());
         player.sendPacket(html);
      } else if (command.startsWith("ShowTicket")) {
         int val = Integer.parseInt(command.substring(11));
         if (val == 0) {
            super.onBypassFeedback(player, "Chat 0");
            return;
         }

         ItemInstance ticket = player.getInventory().getItemByObjectId(val);
         if (ticket == null) {
            super.onBypassFeedback(player, "Chat 0");
            return;
         }

         int raceId = ticket.getEnchantLevel();
         int lane = ticket.getCustomType1();
         int bet = ticket.getCustomType2() * 100;
         HistoryInfoTemplate info = MonsterRaceManager.getInstance().getHistory().get(raceId - 1);
         if (info == null) {
            super.onBypassFeedback(player, "Chat 0");
            return;
         }

         NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
         html.setFile(player, player.getLang(), this.getHtmlPath(this.getTemplate().getId(), 8));
         html.replace("%raceId%", (long)raceId);
         html.replace("%lane%", (long)lane);
         html.replace("%bet%", (long)bet);
         html.replace("%firstLane%", (long)info.getFirst());
         html.replace("%odd%", lane == info.getFirst() ? String.format(Locale.ENGLISH, "%.2f", info.getOddRate()) : "0.01");
         html.replace("%objectId%", (long)this.getObjectId());
         html.replace("%ticketObjectId%", (long)val);
         player.sendPacket(html);
      } else {
         if (command.startsWith("CalculateWin")) {
            int val = Integer.parseInt(command.substring(13));
            if (val == 0) {
               super.onBypassFeedback(player, "Chat 0");
               return;
            }

            ItemInstance ticket = player.getInventory().getItemByObjectId(val);
            if (ticket == null) {
               super.onBypassFeedback(player, "Chat 0");
               return;
            }

            int raceId = ticket.getEnchantLevel();
            int lane = ticket.getCustomType1();
            int bet = ticket.getCustomType2() * 100;
            HistoryInfoTemplate info = MonsterRaceManager.getInstance().getHistory().get(raceId - 1);
            if (info == null) {
               super.onBypassFeedback(player, "Chat 0");
               return;
            }

            if (player.destroyItem("MonsterTrack", ticket, this, true)) {
               player.addAdena("MonsterTrack", (long)((int)((double)bet * (lane == info.getFirst() ? info.getOddRate() : 0.01))), this, true);
            }

            super.onBypassFeedback(player, "Chat 0");
            return;
         }

         if (command.equals("ViewHistory")) {
            StringBuilder sb = new StringBuilder();
            List<HistoryInfoTemplate> history = MonsterRaceManager.getInstance().getHistory();

            for(int i = history.size() - 1; i >= Math.max(0, history.size() - 7); --i) {
               HistoryInfoTemplate info = history.get(i);
               StringUtil.append(
                  sb,
                  "<tr><td><font color=\"LEVEL\">",
                  info.getRaceId(),
                  "</font> th</td><td><font color=\"LEVEL\">",
                  info.getFirst(),
                  "</font> Lane </td><td><font color=\"LEVEL\">",
                  info.getSecond(),
                  "</font> Lane</td><td align=right><font color=00ffff>",
                  String.format(Locale.ENGLISH, "%.2f", info.getOddRate()),
                  "</font> Times</td></tr>"
               );
            }

            NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
            html.setFile(player, player.getLang(), this.getHtmlPath(this.getTemplate().getId(), 9));
            html.replace("%infos%", sb.toString());
            html.replace("%objectId%", (long)this.getObjectId());
            player.sendPacket(html);
         } else {
            super.onBypassFeedback(player, command);
         }
      }
   }

   @Override
   public void addInfoObject(GameObject object) {
      if (object.isPlayer()) {
         object.sendPacket(MonsterRaceManager.getInstance().getRacePacket());
      }
   }

   @Override
   public void removeInfoObject(GameObject object) {
      super.removeInfoObject(object);
      if (object.isPlayer()) {
         for(Npc npc : MonsterRaceManager.getInstance().getMonsters()) {
            object.sendPacket(new DeleteObject(npc));
         }
      }
   }
}
