package l2e.gameserver.model.actor.templates.promocode.impl;

import l2e.gameserver.data.parser.ExperienceParser;
import l2e.gameserver.model.actor.Player;
import org.w3c.dom.NamedNodeMap;

public class SetLevelCodeReward extends AbstractCodeReward {
   private final int _level;
   private final String _icon;

   public SetLevelCodeReward(NamedNodeMap attr) {
      this._level = Integer.parseInt(attr.getNamedItem("val").getNodeValue());
      this._icon = attr.getNamedItem("icon") != null ? attr.getNamedItem("icon").getNodeValue() : "";
   }

   @Override
   public void giveReward(Player player) {
      long pXp = player.getExp();
      long tXp = ExperienceParser.getInstance().getExpForLevel(this._level);
      boolean delevel = this._level < player.getLevel();
      if (delevel) {
         player.getStat()
            .removeExpAndSp(
               player.getExp() - ExperienceParser.getInstance().getExpForLevel(player.getStat().getLevel() - (player.getLevel() - this._level)), 0
            );
      } else {
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
