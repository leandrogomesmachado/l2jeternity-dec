package l2e.gameserver.model;

import l2e.gameserver.Config;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.interfaces.IIdentifiable;
import l2e.gameserver.network.serverpackets.AllianceCrest;
import l2e.gameserver.network.serverpackets.ExPledgeEmblem;
import l2e.gameserver.network.serverpackets.PledgeCrest;

public final class Crest implements IIdentifiable {
   private final int _id;
   private final byte[] _data;
   private final CrestType _type;

   public Crest(int id, byte[] data, CrestType type) {
      this._id = id;
      this._data = data;
      this._type = type;
   }

   @Override
   public int getId() {
      return this._id;
   }

   public byte[] getData() {
      return this._data;
   }

   public CrestType getType() {
      return this._type;
   }

   public String getClientPath(Player activeChar) {
      String path = null;
      switch(this.getType()) {
         case PLEDGE:
            activeChar.sendPacket(new PledgeCrest(this.getId(), this.getData()));
            path = "Crest.crest_" + Config.REQUEST_ID + "_" + this.getId();
            break;
         case PLEDGE_LARGE:
            activeChar.sendPacket(new ExPledgeEmblem(this.getId(), this.getData()));
            path = "Crest.crest_" + Config.REQUEST_ID + "_" + this.getId() + "_l";
            break;
         case ALLY:
            activeChar.sendPacket(new AllianceCrest(this.getId(), this.getData()));
            path = "Crest.crest_" + Config.REQUEST_ID + "_" + this.getId();
      }

      return path;
   }
}
