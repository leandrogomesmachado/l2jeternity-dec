package l2e.gameserver.network.serverpackets;

import java.util.Map;
import l2e.gameserver.model.entity.Hero;
import l2e.gameserver.model.stats.StatsSet;

public class ExHeroList extends GameServerPacket {
   private final Map<Integer, StatsSet> _heroList = Hero.getInstance().getHeroes();

   @Override
   protected void writeImpl() {
      this.writeD(this._heroList.size());

      for(Integer heroId : this._heroList.keySet()) {
         StatsSet hero = this._heroList.get(heroId);
         this.writeS(hero.getString("char_name"));
         this.writeD(hero.getInteger("class_id"));
         this.writeS(hero.getString("clan_name", ""));
         this.writeD(hero.getInteger("clan_crest", 0));
         this.writeS(hero.getString("ally_name", ""));
         this.writeD(hero.getInteger("ally_crest", 0));
         this.writeD(hero.getInteger("count"));
      }
   }
}
