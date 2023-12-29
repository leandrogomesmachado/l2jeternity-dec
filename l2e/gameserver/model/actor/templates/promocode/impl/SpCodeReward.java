package l2e.gameserver.model.actor.templates.promocode.impl;

import l2e.gameserver.model.actor.Player;
import org.w3c.dom.NamedNodeMap;

public class SpCodeReward extends AbstractCodeReward {
   private final int _value;
   private final String _icon;

   public SpCodeReward(NamedNodeMap attr) {
      this._value = Integer.parseInt(attr.getNamedItem("val").getNodeValue());
      this._icon = attr.getNamedItem("icon") != null ? attr.getNamedItem("icon").getNodeValue() : "";
   }

   @Override
   public void giveReward(Player player) {
      player.addExpAndSp(0L, this._value);
   }

   @Override
   public String getIcon() {
      return this._icon;
   }

   public int getSp() {
      return this._value;
   }
}
