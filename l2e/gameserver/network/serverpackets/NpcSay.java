package l2e.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.network.NpcStringId;

public final class NpcSay extends GameServerPacket {
   private final int _objectId;
   private final int _textType;
   private final int _npcId;
   private String _text;
   private final int _npcString;
   private List<String> _parameters;

   public NpcSay(int objectId, int messageType, int npcId, String text) {
      this._objectId = objectId;
      this._textType = messageType;
      this._npcId = 1000000 + npcId;
      this._npcString = -1;
      this._text = text;
   }

   public NpcSay(Npc npc, int messageType, String text) {
      this._objectId = npc.getObjectId();
      this._textType = messageType;
      this._npcId = 1000000 + npc.getId();
      this._npcString = -1;
      this._text = text;
   }

   public NpcSay(int objectId, int messageType, int npcId, NpcStringId npcString) {
      this._objectId = objectId;
      this._textType = messageType;
      this._npcId = 1000000 + npcId;
      this._npcString = npcString.getId();
   }

   public NpcSay(Npc npc, int messageType, NpcStringId npcString) {
      this._objectId = npc.getObjectId();
      this._textType = messageType;
      this._npcId = 1000000 + npc.getId();
      this._npcString = npcString.getId();
   }

   public NpcSay addStringParameter(String text) {
      if (this._parameters == null) {
         this._parameters = new ArrayList<>();
      }

      this._parameters.add(text);
      return this;
   }

   public NpcSay addStringParameters(String... params) {
      if (params != null && params.length > 0) {
         if (this._parameters == null) {
            this._parameters = new ArrayList<>();
         }

         for(String item : params) {
            if (item != null && item.length() > 0) {
               this._parameters.add(item);
            }
         }
      }

      return this;
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._objectId);
      this.writeD(this._textType);
      this.writeD(this._npcId);
      this.writeD(this._npcString);
      if (this._npcString == -1) {
         this.writeS(this._text);
      } else if (this._parameters != null) {
         for(String s : this._parameters) {
            this.writeS(s);
         }
      }
   }
}
