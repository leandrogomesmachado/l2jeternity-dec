package l2e.gameserver.network.serverpackets;

import java.util.Calendar;
import java.util.List;
import l2e.gameserver.instancemanager.TerritoryWarManager;
import l2e.gameserver.model.Clan;
import l2e.gameserver.model.actor.Player;

public class ExShowDominionRegistry extends GameServerPacket {
   private static final int MINID = 80;
   private final int _castleId;
   private int _clanReq = 0;
   private int _mercReq = 0;
   private int _isMercRegistered = 0;
   private int _isClanRegistered = 0;
   private int _warTime = (int)(Calendar.getInstance().getTimeInMillis() / 1000L);
   private final int _currentTime = (int)(Calendar.getInstance().getTimeInMillis() / 1000L);

   public ExShowDominionRegistry(int castleId, Player player) {
      this._castleId = castleId;
      if (TerritoryWarManager.getInstance().getRegisteredClans(castleId) != null) {
         this._clanReq = TerritoryWarManager.getInstance().getRegisteredClans(castleId).size();
         if (player.getClan() != null) {
            this._isClanRegistered = TerritoryWarManager.getInstance().getRegisteredClans(castleId).contains(player.getClan()) ? 1 : 0;
         }
      }

      if (TerritoryWarManager.getInstance().getRegisteredMercenaries(castleId) != null) {
         this._mercReq = TerritoryWarManager.getInstance().getRegisteredMercenaries(castleId).size();
         this._isMercRegistered = TerritoryWarManager.getInstance().getRegisteredMercenaries(castleId).contains(player.getObjectId()) ? 1 : 0;
      }

      this._warTime = (int)(TerritoryWarManager.getInstance().getTWStartTimeInMillis() / 1000L);
   }

   @Override
   protected void writeImpl() {
      this.writeD(80 + this._castleId);
      if (TerritoryWarManager.getInstance().getTerritory(this._castleId) == null) {
         this.writeS("No Owner");
         this.writeS("No Owner");
         this.writeS("No Ally");
      } else {
         Clan clan = TerritoryWarManager.getInstance().getTerritory(this._castleId).getOwnerClan();
         if (clan == null) {
            this.writeS("No Owner");
            this.writeS("No Owner");
            this.writeS("No Ally");
         } else {
            this.writeS(clan.getName());
            this.writeS(clan.getLeaderName());
            this.writeS(clan.getAllyName());
         }
      }

      this.writeD(this._clanReq);
      this.writeD(this._mercReq);
      this.writeD(this._warTime);
      this.writeD(this._currentTime);
      this.writeD(this._isClanRegistered);
      this.writeD(this._isMercRegistered);
      this.writeD(1);
      List<TerritoryWarManager.Territory> territoryList = TerritoryWarManager.getInstance().getAllTerritories();
      this.writeD(territoryList.size());

      for(TerritoryWarManager.Territory t : territoryList) {
         this.writeD(t.getTerritoryId());
         this.writeD(t.getOwnedWardIds().size());

         for(int i : t.getOwnedWardIds()) {
            this.writeD(i);
         }
      }
   }
}
