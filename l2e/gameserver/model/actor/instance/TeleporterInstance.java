package l2e.gameserver.model.actor.instance;

import java.util.Calendar;
import java.util.StringTokenizer;
import l2e.gameserver.Config;
import l2e.gameserver.data.htm.HtmCache;
import l2e.gameserver.data.parser.TeleLocationParser;
import l2e.gameserver.instancemanager.CastleManager;
import l2e.gameserver.instancemanager.TownManager;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.TeleportTemplate;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.service.BotFunctions;
import l2e.gameserver.model.zone.type.TownZone;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;

public final class TeleporterInstance extends Npc {
   private static final int COND_ALL_FALSE = 0;
   private static final int COND_BUSY_BECAUSE_OF_SIEGE = 1;
   private static final int COND_OWNER = 2;
   private static final int COND_REGULAR = 3;

   public TeleporterInstance(int objectId, NpcTemplate template) {
      super(objectId, template);
      this.setInstanceType(GameObject.InstanceType.TeleporterInstance);
   }

   @Override
   public void onBypassFeedback(Player player, String command) {
      player.sendActionFailed();
      int condition = this.validateCondition(player);
      StringTokenizer st = new StringTokenizer(command, " ");
      String actualCommand = st.nextToken();
      if (player.getFirstEffect(6201) == null && player.getFirstEffect(6202) == null && player.getFirstEffect(6203) == null) {
         if (actualCommand.equalsIgnoreCase("goto")) {
            int npcId = this.getId();
            switch(npcId) {
               case 32534:
               case 32539:
                  if (player.isFlyingMounted()) {
                     player.sendPacket(SystemMessageId.YOU_CANNOT_ENTER_SEED_IN_FLYING_TRANSFORM);
                     return;
                  }
               default:
                  if (st.countTokens() <= 0) {
                     return;
                  }

                  int whereTo = Integer.parseInt(st.nextToken());
                  if (condition == 3) {
                     this.doTeleport(player, whereTo);
                     return;
                  }

                  if (condition == 2) {
                     int minPrivilegeLevel = 0;
                     if (st.countTokens() >= 1) {
                        minPrivilegeLevel = Integer.parseInt(st.nextToken());
                     }

                     if (10 >= minPrivilegeLevel) {
                        this.doTeleport(player, whereTo);
                     } else {
                        player.sendMessage("You don't have the sufficient access level to teleport there.");
                     }

                     return;
                  }
            }
         } else if (command.startsWith("Chat")) {
            Calendar cal = Calendar.getInstance();
            int val = 0;

            try {
               val = Integer.parseInt(command.substring(5));
            } catch (IndexOutOfBoundsException var9) {
            } catch (NumberFormatException var10) {
            }

            if (val == 1 && player.getLevel() < 41) {
               this.showNewbieHtml(player);
               return;
            }

            if (val == 1 && cal.get(11) >= 20 && cal.get(11) <= 23 && (cal.get(7) == 1 || cal.get(7) == 7)) {
               this.showHalfPriceHtml(player);
               return;
            }

            this.showChatWindow(player, val);
         }

         super.onBypassFeedback(player, command);
      } else {
         NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
         String filename = "data/html/teleporter/epictransformed.htm";
         html.setFile(player, player.getLang(), "data/html/teleporter/epictransformed.htm");
         html.replace("%objectId%", String.valueOf(this.getObjectId()));
         html.replace("%npcname%", this.getName());
         player.sendPacket(html);
      }
   }

   @Override
   public String getHtmlPath(int npcId, int val) {
      String pom = "";
      if (val == 0) {
         pom = "" + npcId;
      } else {
         pom = npcId + "-" + val;
      }

      return "data/html/teleporter/" + pom + ".htm";
   }

   private void showNewbieHtml(Player player) {
      if (player != null) {
         NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
         String filename = "data/html/teleporter/free/" + this.getTemplate().getId() + ".htm";
         if (!HtmCache.getInstance().isLoadable(filename)) {
            filename = "data/html/teleporter/" + this.getTemplate().getId() + "-1.htm";
         }

         html.setFile(player, player.getLang(), filename);
         html.replace("%objectId%", String.valueOf(this.getObjectId()));
         html.replace("%npcname%", this.getName());
         player.sendPacket(html);
      }
   }

   private void showHalfPriceHtml(Player player) {
      if (player != null) {
         NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
         String filename = "data/html/teleporter/half/" + this.getId() + ".htm";
         if (!HtmCache.getInstance().isLoadable(filename)) {
            filename = "data/html/teleporter/" + this.getId() + "-1.htm";
         }

         html.setFile(player, player.getLang(), filename);
         html.replace("%objectId%", String.valueOf(this.getObjectId()));
         html.replace("%npcname%", this.getName());
         player.sendPacket(html);
      }
   }

   @Override
   public void showChatWindow(Player player) {
      String filename = "data/html/teleporter/castleteleporter-no.htm";
      int condition = this.validateCondition(player);
      if (condition == 3) {
         super.showChatWindow(player);
      } else {
         if (condition > 0) {
            if (condition == 1) {
               filename = "data/html/teleporter/castleteleporter-busy.htm";
            } else if (condition == 2) {
               filename = this.getHtmlPath(this.getId(), 0);
            }
         }

         NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
         html.setFile(player, player.getLang(), filename);
         html.replace("%objectId%", String.valueOf(this.getObjectId()));
         html.replace("%npcname%", this.getName());
         player.sendPacket(html);
      }
   }

   private void doTeleport(Player player, int val) {
      TeleportTemplate list = TeleLocationParser.getInstance().getTemplate(val);
      if (list != null) {
         boolean notNeedCheck = val == 122 || val == 123 || val == 200916;
         TownZone town = TownManager.getTown(list.getLocX(), list.getLocY(), list.getLocZ());
         if (town != null && TownManager.townHasCastleInSiege(list.getLocX(), list.getLocY()) && !notNeedCheck) {
            player.sendPacket(SystemMessageId.NO_PORT_THAT_IS_IN_SIGE);
            return;
         }

         if (!Config.ALT_GAME_KARMA_PLAYER_CAN_USE_GK && player.getKarma() > 0) {
            player.sendMessage("Go away, you're not welcome here.");
            return;
         }

         if (player.isCombatFlagEquipped()) {
            player.sendPacket(SystemMessageId.YOU_CANNOT_TELEPORT_WHILE_IN_POSSESSION_OF_A_WARD);
            return;
         }

         if (list.getIsForNoble() && !player.isNoble()) {
            String filename = "data/html/teleporter/nobleteleporter-no.htm";
            NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
            html.setFile(player, player.getLang(), "data/html/teleporter/nobleteleporter-no.htm");
            html.replace("%objectId%", String.valueOf(this.getObjectId()));
            html.replace("%npcname%", this.getName());
            player.sendPacket(html);
            return;
         }

         if (player.isAlikeDead()) {
            return;
         }

         Calendar cal = Calendar.getInstance();
         int price = list.getPrice();
         if (player.getLevel() < 41) {
            price = 0;
         } else if (!list.getIsForNoble() && cal.get(11) >= 20 && cal.get(11) <= 23 && (cal.get(7) == 1 || cal.get(7) == 7)) {
            price /= 2;
         }

         if (Config.ALT_GAME_FREE_TELEPORT
            || player.destroyItemByItemId("Teleport " + (list.getIsForNoble() ? " nobless" : ""), list.getId(), (long)price, this, true)) {
            if (Config.DEBUG) {
               _log.info("Teleporting player " + player.getName() + " to new location: " + list.getLocX() + ":" + list.getLocY() + ":" + list.getLocZ());
            }

            if (BotFunctions.getInstance().isAutoTpGotoEnable(player)) {
               BotFunctions.getInstance().getAutoGotoTeleport(player, player.getLocation(), new Location(list.getLocX(), list.getLocY(), list.getLocZ()));
               return;
            }

            player.teleToLocation(list.getLocX(), list.getLocY(), list.getLocZ(), true);
         }
      } else {
         _log.warning("No teleport destination with id:" + val);
      }

      player.sendActionFailed();
   }

   private int validateCondition(Player player) {
      if (CastleManager.getInstance().getCastleIndex(this) < 0) {
         return 3;
      } else if (this.getCastle().getSiege().getIsInProgress()) {
         return 1;
      } else {
         return player.getClan() != null && this.getCastle().getOwnerId() == player.getClanId() ? 2 : 0;
      }
   }
}
