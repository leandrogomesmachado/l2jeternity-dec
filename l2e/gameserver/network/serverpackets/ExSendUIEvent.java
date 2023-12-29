package l2e.gameserver.network.serverpackets;

import java.util.Arrays;
import java.util.List;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.NpcStringId;

public class ExSendUIEvent extends GameServerPacket {
   private final int _objectId;
   private final boolean _type;
   private final boolean _countUp;
   private final int _startTime;
   private final int _endTime;
   private final int _npcstringId;
   private List<String> _params = null;

   public ExSendUIEvent(Player player, boolean hide, boolean countUp, int startTime, int endTime, String text) {
      this(player, hide, countUp, startTime, endTime, -1, text);
   }

   public ExSendUIEvent(Player player, boolean hide, boolean countUp, int startTime, int endTime, NpcStringId npcString, String... params) {
      this(player, hide, countUp, startTime, endTime, npcString.getId(), params);
   }

   public ExSendUIEvent(Player player, boolean hide, boolean countUp, int startTime, int endTime, int npcstringId, String... params) {
      this._objectId = player.getObjectId();
      this._type = hide;
      this._countUp = countUp;
      this._startTime = startTime;
      this._endTime = endTime;
      this._npcstringId = npcstringId;
      this._params = Arrays.asList(params);
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._objectId);
      this.writeD(this._type ? 1 : 0);
      this.writeD(0);
      this.writeD(0);
      this.writeS(this._countUp ? "1" : "0");
      this.writeS(String.valueOf(this._startTime / 60));
      this.writeS(String.valueOf(this._startTime % 60));
      this.writeS(String.valueOf(this._endTime / 60));
      this.writeS(String.valueOf(this._endTime % 60));
      this.writeD(this._npcstringId);
      if (this._params != null) {
         for(String param : this._params) {
            this.writeS(param);
         }
      }
   }
}
