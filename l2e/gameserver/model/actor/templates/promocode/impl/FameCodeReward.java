package l2e.gameserver.model.actor.templates.promocode.impl;

import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;
import org.w3c.dom.NamedNodeMap;

public class FameCodeReward extends AbstractCodeReward {
   private final int _value;
   private final String _icon;

   public FameCodeReward(NamedNodeMap attr) {
      this._value = Integer.parseInt(attr.getNamedItem("val").getNodeValue());
      this._icon = attr.getNamedItem("icon") != null ? attr.getNamedItem("icon").getNodeValue() : "icon.pvp_point_i00";
   }

   @Override
   public void giveReward(Player player) {
      player.setFame(player.getFame() + this._value);
      SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.ACQUIRED_S1_REPUTATION_SCORE);
      sm.addNumber(this._value);
      player.sendPacket(sm);
      player.sendUserInfo();
   }

   @Override
   public String getIcon() {
      return this._icon;
   }

   public int getFame() {
      return this._value;
   }
}
