package l2e.gameserver.model.actor.templates.promocode.impl;

import l2e.gameserver.Config;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ExPCCafePointInfo;
import l2e.gameserver.network.serverpackets.SystemMessage;
import org.w3c.dom.NamedNodeMap;

public class PcPointCodeReward extends AbstractCodeReward {
   private int _value;
   private final String _icon;

   public PcPointCodeReward(NamedNodeMap attr) {
      this._value = Integer.parseInt(attr.getNamedItem("val").getNodeValue());
      this._icon = attr.getNamedItem("icon") != null ? attr.getNamedItem("icon").getNodeValue() : "icon.etc_pccafe_point_i00";
   }

   @Override
   public void giveReward(Player player) {
      if (player.getPcBangPoints() + this._value > Config.MAX_PC_BANG_POINTS) {
         this._value = Config.MAX_PC_BANG_POINTS - player.getPcBangPoints();
      }

      SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_ACQUIRED_S1_PC_CAFE_POINTS);
      sm.addNumber(this._value);
      player.sendPacket(sm);
      player.setPcBangPoints(player.getPcBangPoints() + this._value);
      player.sendPacket(new ExPCCafePointInfo(player.getPcBangPoints(), this._value, true, false, 1));
   }

   @Override
   public String getIcon() {
      return this._icon;
   }

   public int getPcPoints() {
      return this._value;
   }
}
