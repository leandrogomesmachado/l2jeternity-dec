package l2e.gameserver.model.actor.instance;

import java.util.Arrays;
import java.util.StringTokenizer;
import l2e.commons.util.Evolve;
import l2e.commons.util.Util;
import l2e.gameserver.data.holder.ClanHolder;
import l2e.gameserver.instancemanager.ClanHallManager;
import l2e.gameserver.model.Clan;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.entity.ClanHall;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;

public class ClanHallDoormenInstance extends DoormenInstance {
   private volatile boolean _init = false;
   private ClanHall _clanHall = null;
   private boolean _hasEvolve = false;
   private static final int[] CH_WITH_EVOLVE = new int[]{36, 37, 38, 39, 40, 41, 51, 52, 53, 54, 55, 56, 57};

   public ClanHallDoormenInstance(int objectID, NpcTemplate template) {
      super(objectID, template);
      this.setInstanceType(GameObject.InstanceType.ClanHallDoormenInstance);
   }

   @Override
   public void onBypassFeedback(Player player, String command) {
      if (this._hasEvolve && command.startsWith("evolve") && this.isOwnerClan(player)) {
         StringTokenizer st = new StringTokenizer(command, " ");
         if (st.countTokens() >= 2) {
            st.nextToken();
            boolean ok = false;
            switch(Integer.parseInt(st.nextToken())) {
               case 1:
                  ok = Evolve.doEvolve(player, this, 9882, 10307, 55);
                  break;
               case 2:
                  ok = Evolve.doEvolve(player, this, 4422, 10308, 55);
                  break;
               case 3:
                  ok = Evolve.doEvolve(player, this, 4423, 10309, 55);
                  break;
               case 4:
                  ok = Evolve.doEvolve(player, this, 4424, 10310, 55);
                  break;
               case 5:
                  ok = Evolve.doEvolve(player, this, 10426, 10611, 70);
            }

            NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
            if (ok) {
               html.setFile(player, player.getLang(), "data/html/clanHallDoormen/evolve-ok.htm");
            } else {
               html.setFile(player, player.getLang(), "data/html/clanHallDoormen/evolve-no.htm");
            }

            player.sendPacket(html);
         }
      } else {
         super.onBypassFeedback(player, command);
      }
   }

   @Override
   public void showChatWindow(Player player) {
      player.sendActionFailed();
      NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
      if (this.getClanHall() != null) {
         Clan owner = ClanHolder.getInstance().getClan(this.getClanHall().getOwnerId());
         if (this.isOwnerClan(player)) {
            if (this._hasEvolve) {
               html.setFile(player, player.getLang(), "data/html/clanHallDoormen/doormen2.htm");
               html.replace("%clanname%", owner.getName());
            } else {
               html.setFile(player, player.getLang(), "data/html/clanHallDoormen/doormen1.htm");
               html.replace("%clanname%", owner.getName());
            }
         } else if (owner != null && owner.getLeader() != null) {
            html.setFile(player, player.getLang(), "data/html/clanHallDoormen/doormen-no.htm");
            html.replace("%leadername%", owner.getLeaderName());
            html.replace("%clanname%", owner.getName());
         } else {
            html.setFile(player, player.getLang(), "data/html/clanHallDoormen/emptyowner.htm");
            html.replace("%hallname%", Util.clanHallName(player, this.getClanHall().getId()));
         }

         html.replace("%objectId%", String.valueOf(this.getObjectId()));
         player.sendPacket(html);
      }
   }

   @Override
   protected final void openDoors(Player player, String command) {
      this.getClanHall().openCloseDoors(true);
      NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
      html.setFile(player, player.getLang(), "data/html/clanHallDoormen/doormen-opened.htm");
      html.replace("%objectId%", String.valueOf(this.getObjectId()));
      player.sendPacket(html);
   }

   @Override
   protected final void closeDoors(Player player, String command) {
      this.getClanHall().openCloseDoors(false);
      NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
      html.setFile(player, player.getLang(), "data/html/clanHallDoormen/doormen-closed.htm");
      html.replace("%objectId%", String.valueOf(this.getObjectId()));
      player.sendPacket(html);
   }

   private final ClanHall getClanHall() {
      if (!this._init) {
         synchronized(this) {
            if (!this._init) {
               this._clanHall = ClanHallManager.getInstance().getNearbyClanHall(this.getX(), this.getY(), 500);
               if (this._clanHall != null) {
                  this._hasEvolve = Arrays.binarySearch(CH_WITH_EVOLVE, this._clanHall.getId()) >= 0;
               }

               this._init = true;
            }
         }
      }

      return this._clanHall;
   }

   @Override
   protected final boolean isOwnerClan(Player player) {
      return player.getClan() != null && this.getClanHall() != null && player.getClanId() == this.getClanHall().getOwnerId();
   }
}
