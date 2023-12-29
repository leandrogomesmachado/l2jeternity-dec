package l2e.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;
import l2e.gameserver.Config;
import l2e.gameserver.model.SkillLearn;
import l2e.gameserver.model.base.AcquireSkillType;
import l2e.gameserver.model.holders.ItemHolder;

public class AcquireSkillInfo extends GameServerPacket {
   private final AcquireSkillType _type;
   private final int _id;
   private final int _level;
   private final int _spCost;
   private final List<AcquireSkillInfo.Req> _reqs;

   public AcquireSkillInfo(AcquireSkillType skillType, SkillLearn skillLearn) {
      this._id = skillLearn.getId();
      this._level = skillLearn.getLvl();
      this._spCost = skillLearn.getLevelUpSp();
      this._type = skillType;
      this._reqs = new ArrayList<>();
      if (skillType != AcquireSkillType.PLEDGE || Config.LIFE_CRYSTAL_NEEDED) {
         for(ItemHolder item : skillLearn.getRequiredItems()) {
            if (Config.DIVINE_SP_BOOK_NEEDED || this._id != 1405) {
               this._reqs.add(new AcquireSkillInfo.Req(99, item.getId(), item.getCount(), 50));
            }
         }
      }
   }

   public AcquireSkillInfo(AcquireSkillType skillType, SkillLearn skillLearn, int sp) {
      this._id = skillLearn.getId();
      this._level = skillLearn.getLvl();
      this._spCost = sp;
      this._type = skillType;
      this._reqs = new ArrayList<>();

      for(ItemHolder item : skillLearn.getRequiredItems()) {
         this._reqs.add(new AcquireSkillInfo.Req(99, item.getId(), item.getCount(), 50));
      }
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._id);
      this.writeD(this._level);
      this.writeD(this._spCost);
      this.writeD(this._type.ordinal());
      this.writeD(this._reqs.size());

      for(AcquireSkillInfo.Req temp : this._reqs) {
         this.writeD(temp.type);
         this.writeD(temp.itemId);
         this.writeQ(temp.count);
         this.writeD(temp.unk);
      }
   }

   private static class Req {
      public int itemId;
      public long count;
      public int type;
      public int unk;

      public Req(int pType, int pItemId, long itemCount, int pUnk) {
         this.itemId = pItemId;
         this.type = pType;
         this.count = itemCount;
         this.unk = pUnk;
      }
   }
}
