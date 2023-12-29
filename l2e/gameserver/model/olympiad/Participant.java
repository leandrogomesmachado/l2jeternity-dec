package l2e.gameserver.model.olympiad;

import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.stats.StatsSet;

public final class Participant {
   private final int objectId;
   private Player player;
   private final String name;
   private final int side;
   private final int baseClass;
   private boolean disconnected = false;
   private boolean defaulted = false;
   private final StatsSet stats;
   public String clanName;
   public int clanId;

   public Participant(Player plr, int olympiadSide) {
      this.objectId = plr.getObjectId();
      this.player = plr;
      this.name = plr.getName();
      this.side = olympiadSide;
      this.baseClass = plr.getBaseClass();
      this.stats = Olympiad.getNobleStats(this.getObjectId());
      this.clanName = plr.getClan() != null ? plr.getClan().getName() : "";
      this.clanId = plr.getClanId();
   }

   public Participant(int objId, int olympiadSide) {
      this.objectId = objId;
      this.player = null;
      this.name = "-";
      this.side = olympiadSide;
      this.baseClass = 0;
      this.stats = null;
      this.clanName = "";
      this.clanId = 0;
   }

   public final boolean updatePlayer() {
      if (this.player == null || !this.player.isOnline()) {
         this.player = World.getInstance().getPlayer(this.getObjectId());
      }

      return this.player != null;
   }

   public final void updateStat(String statName, int increment) {
      this.stats.set(statName, Math.max(this.stats.getInteger(statName) + increment, 0));
   }

   public String getName() {
      return this.name;
   }

   public Player getPlayer() {
      return this.player;
   }

   public int getObjectId() {
      return this.objectId;
   }

   public StatsSet getStats() {
      return this.stats;
   }

   public void setPlayer(Player noble) {
      this.player = noble;
   }

   public int getSide() {
      return this.side;
   }

   public int getBaseClass() {
      return this.baseClass;
   }

   public boolean isDisconnected() {
      return this.disconnected;
   }

   public void setDisconnected(boolean val) {
      this.disconnected = val;
   }

   public boolean isDefaulted() {
      return this.defaulted;
   }

   public void setDefaulted(boolean val) {
      this.defaulted = val;
   }

   public String getClanName() {
      return this.clanName;
   }

   public int getClanId() {
      return this.clanId;
   }
}
