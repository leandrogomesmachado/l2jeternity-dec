package l2e.gameserver.model.actor.templates.promocode.impl;

import l2e.gameserver.model.actor.Player;
import org.w3c.dom.NamedNodeMap;

public class ExpCodeReward extends AbstractCodeReward {
   private final long _value;
   private final String _icon;

   public ExpCodeReward(NamedNodeMap attr) {
      this._value = Long.parseLong(attr.getNamedItem("val").getNodeValue());
      this._icon = attr.getNamedItem("icon") != null ? attr.getNamedItem("icon").getNodeValue() : "";
   }

   @Override
   public void giveReward(Player player) {
      player.addExpAndSp(this._value, 0);
   }

   @Override
   public String getIcon() {
      return this._icon;
   }

   public long getExp() {
      return this._value;
   }
}
