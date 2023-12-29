package l2e.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;
import l2e.gameserver.data.parser.EnchantSkillGroupsParser;
import l2e.gameserver.model.EnchantSkillGroup;
import l2e.gameserver.model.EnchantSkillLearn;

public final class ExEnchantSkillInfo extends GameServerPacket {
   private final List<Integer> _routes = new ArrayList<>();
   private final int _id;
   private final int _lvl;
   private boolean _maxEnchanted = false;

   public ExEnchantSkillInfo(int id, int lvl) {
      this._id = id;
      this._lvl = lvl;
      EnchantSkillLearn enchantLearn = EnchantSkillGroupsParser.getInstance().getSkillEnchantmentBySkillId(this._id);
      if (enchantLearn != null) {
         if (this._lvl > 100) {
            this._maxEnchanted = enchantLearn.isMaxEnchant(this._lvl);
            EnchantSkillGroup.EnchantSkillsHolder esd = enchantLearn.getEnchantSkillsHolder(this._lvl);
            if (esd != null) {
               this._routes.add(this._lvl);
            }

            int skillLvL = this._lvl % 100;

            for(int route : enchantLearn.getAllRoutes()) {
               if (route * 100 + skillLvL != this._lvl) {
                  this._routes.add(route * 100 + skillLvL);
               }
            }
         } else {
            for(int route : enchantLearn.getAllRoutes()) {
               this._routes.add(route * 100 + 1);
            }
         }
      }
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._id);
      this.writeD(this._lvl);
      this.writeD(this._maxEnchanted ? 0 : 1);
      this.writeD(this._lvl > 100 ? 1 : 0);
      this.writeD(this._routes.size());

      for(int level : this._routes) {
         this.writeD(level);
      }
   }
}
