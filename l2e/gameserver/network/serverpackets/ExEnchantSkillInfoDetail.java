package l2e.gameserver.network.serverpackets;

import l2e.gameserver.Config;
import l2e.gameserver.data.parser.EnchantSkillGroupsParser;
import l2e.gameserver.model.EnchantSkillGroup;
import l2e.gameserver.model.EnchantSkillLearn;
import l2e.gameserver.model.actor.Player;

public class ExEnchantSkillInfoDetail extends GameServerPacket {
   private static final int TYPE_NORMAL_ENCHANT = 0;
   private static final int TYPE_SAFE_ENCHANT = 1;
   private static final int TYPE_UNTRAIN_ENCHANT = 2;
   private static final int TYPE_CHANGE_ENCHANT = 3;
   private int bookId = 0;
   private int reqCount = 0;
   private int multi = 1;
   private final int _type;
   private final int _skillid;
   private final int _skilllvl;
   private final int _chance;
   private int _sp;
   private final int _adenacount;

   public ExEnchantSkillInfoDetail(int type, int skillid, int skilllvl, Player ply) {
      EnchantSkillLearn enchantLearn = EnchantSkillGroupsParser.getInstance().getSkillEnchantmentBySkillId(skillid);
      EnchantSkillGroup.EnchantSkillsHolder esd = null;
      if (enchantLearn != null) {
         if (skilllvl > 100) {
            esd = enchantLearn.getEnchantSkillsHolder(skilllvl);
         } else {
            esd = enchantLearn.getFirstRouteGroup().getEnchantGroupDetails().get(0);
         }
      }

      if (esd == null) {
         throw new IllegalArgumentException("Skill " + skillid + " dont have enchant data for level " + skilllvl);
      } else {
         if (type == 0) {
            this.multi = EnchantSkillGroupsParser.NORMAL_ENCHANT_COST_MULTIPLIER;
         } else if (type == 1) {
            this.multi = EnchantSkillGroupsParser.SAFE_ENCHANT_COST_MULTIPLIER;
         }

         this._chance = esd.getRate(ply);
         this._sp = esd.getSpCost();
         if (type == 2) {
            this._sp = (int)(0.8 * (double)this._sp);
         }

         this._adenacount = esd.getAdenaCost() * this.multi;
         this._type = type;
         this._skillid = skillid;
         this._skilllvl = skilllvl;
         switch(type) {
            case 0:
               this.bookId = 6622;
               this.reqCount = this._skilllvl % 100 > 1 ? 0 : 1;
               break;
            case 1:
               this.bookId = 9627;
               this.reqCount = 1;
               break;
            case 2:
               this.bookId = 9625;
               this.reqCount = 1;
               break;
            case 3:
               this.bookId = 9626;
               this.reqCount = 1;
               break;
            default:
               return;
         }

         if (type != 1 && !Config.ES_SP_BOOK_NEEDED) {
            this.reqCount = 0;
         }
      }
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._type);
      this.writeD(this._skillid);
      this.writeD(this._skilllvl);
      this.writeD(this._sp * this.multi);
      this.writeD(this._chance);
      this.writeD(2);
      this.writeD(57);
      this.writeD(this._adenacount);
      this.writeD(this.bookId);
      this.writeD(this.reqCount);
   }
}
