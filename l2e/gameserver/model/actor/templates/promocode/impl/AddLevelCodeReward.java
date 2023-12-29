package l2e.gameserver.model.actor.templates.promocode.impl;

import l2e.gameserver.data.parser.ExperienceParser;
import l2e.gameserver.model.actor.Player;
import org.w3c.dom.NamedNodeMap;

public class AddLevelCodeReward extends AbstractCodeReward {
   private final int _level;
   private final String _icon;

   public AddLevelCodeReward(NamedNodeMap attr) {
      this._level = Integer.parseInt(attr.getNamedItem("val").getNodeValue());
      this._icon = attr.getNamedItem("icon") != null ? attr.getNamedItem("icon").getNodeValue() : "";
   }

   @Override
   public void giveReward(Player player) {
      if (player.getLevel() < 85) {
         int nextLevel = player.getLevel() + this._level;
         if (nextLevel > 85) {
            nextLevel = 85;
         }

         long pXp = player.getExp();
         long tXp = ExperienceParser.getInstance().getExpForLevel(nextLevel);
         player.addExpAndSp(tXp - pXp, 0);
      }
   }

   @Override
   public String getIcon() {
      return this._icon;
   }

   public int getLevel() {
      return this._level;
   }
}
