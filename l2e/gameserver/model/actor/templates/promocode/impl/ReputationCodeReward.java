package l2e.gameserver.model.actor.templates.promocode.impl;

import l2e.gameserver.model.actor.Player;
import org.w3c.dom.NamedNodeMap;

public class ReputationCodeReward extends AbstractCodeReward {
   private final int _value;
   private final String _icon;

   public ReputationCodeReward(NamedNodeMap attr) {
      this._value = Integer.parseInt(attr.getNamedItem("val").getNodeValue());
      this._icon = attr.getNamedItem("icon") != null ? attr.getNamedItem("icon").getNodeValue() : "icon.skill0390";
   }

   @Override
   public void giveReward(Player player) {
      if (player.getClan() != null) {
         player.getClan().addReputationScore(this._value, true);
      }
   }

   @Override
   public String getIcon() {
      return this._icon;
   }

   public int getReputation() {
      return this._value;
   }
}
