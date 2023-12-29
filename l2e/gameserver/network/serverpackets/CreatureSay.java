package l2e.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;
import l2e.commons.util.ArabicUtilities;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.SystemMessageId;

public final class CreatureSay extends GameServerPacket {
   private final int _objectId;
   private final int _textType;
   private String _charName = null;
   private int _charId = 0;
   private String _text = null;
   private int _npcString = -1;
   private List<String> _parameters;

   public CreatureSay(int objectId, int messageType, String charName, String text) {
      this._objectId = objectId;
      this._textType = messageType;
      this._charName = charName;
      this._text = text;
   }

   public CreatureSay(int objectId, int messageType, int charId, NpcStringId npcString) {
      this._objectId = objectId;
      this._textType = messageType;
      this._charId = charId;
      this._npcString = npcString.getId();
   }

   public CreatureSay(int objectId, int messageType, String charName, NpcStringId npcString) {
      this._objectId = objectId;
      this._textType = messageType;
      this._charName = charName;
      this._npcString = npcString.getId();
   }

   public CreatureSay(int objectId, int messageType, int charId, SystemMessageId sysString) {
      this._objectId = objectId;
      this._textType = messageType;
      this._charId = charId;
      this._npcString = sysString.getId();
   }

   public void addStringParameter(String text) {
      if (this._parameters == null) {
         this._parameters = new ArrayList<>();
      }

      this._parameters.add(text);
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._objectId);
      this.writeD(this._textType);
      if (this._charName != null) {
         this.writeS(this._charName);
      } else {
         this.writeD(this._charId);
      }

      this.writeD(this._npcString);
      if (this._text != null) {
         if (ArabicUtilities.hasArabicLetters(this._text)) {
            String ttext = ArabicUtilities.reshapeSentence(this._text);
            this.writeS(ttext);
         } else {
            this.writeS(this._text);
         }
      } else if (this._parameters != null) {
         for(String s : this._parameters) {
            this.writeS(s);
         }
      }

      Player player = this.getClient().getActiveChar();
      if (player != null) {
         player.broadcastSnoop(this._textType, this._charName, this._text);
      }
   }
}
