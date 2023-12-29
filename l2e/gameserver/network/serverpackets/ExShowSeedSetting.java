package l2e.gameserver.network.serverpackets;

import java.util.List;
import l2e.gameserver.data.parser.ManorParser;
import l2e.gameserver.instancemanager.CastleManager;
import l2e.gameserver.model.actor.templates.SeedTemplate;
import l2e.gameserver.model.entity.Castle;

public class ExShowSeedSetting extends GameServerPacket {
   private final int _manorId;
   private final int _count;
   private final long[] _seedData;

   public ExShowSeedSetting(int manorId) {
      this._manorId = manorId;
      Castle c = CastleManager.getInstance().getCastleById(this._manorId);
      List<Integer> seeds = ManorParser.getInstance().getSeedsForCastle(this._manorId);
      this._count = seeds.size();
      this._seedData = new long[this._count * 12];
      int i = 0;

      for(int s : seeds) {
         this._seedData[i * 12 + 0] = (long)s;
         this._seedData[i * 12 + 1] = (long)ManorParser.getInstance().getSeedLevel(s);
         this._seedData[i * 12 + 2] = (long)ManorParser.getInstance().getRewardItemBySeed(s, 1);
         this._seedData[i * 12 + 3] = (long)ManorParser.getInstance().getRewardItemBySeed(s, 2);
         this._seedData[i * 12 + 4] = (long)ManorParser.getInstance().getSeedSaleLimit(s);
         this._seedData[i * 12 + 5] = ManorParser.getInstance().getSeedBuyPrice(s);
         this._seedData[i * 12 + 6] = (long)(ManorParser.getInstance().getSeedBasicPrice(s) * 60 / 100);
         this._seedData[i * 12 + 7] = (long)(ManorParser.getInstance().getSeedBasicPrice(s) * 10);
         SeedTemplate seedPr = c.getSeed(s, 0);
         if (seedPr != null) {
            this._seedData[i * 12 + 8] = seedPr.getStartProduce();
            this._seedData[i * 12 + 9] = seedPr.getPrice();
         } else {
            this._seedData[i * 12 + 8] = 0L;
            this._seedData[i * 12 + 9] = 0L;
         }

         seedPr = c.getSeed(s, 1);
         if (seedPr != null) {
            this._seedData[i * 12 + 10] = seedPr.getStartProduce();
            this._seedData[i * 12 + 11] = seedPr.getPrice();
         } else {
            this._seedData[i * 12 + 10] = 0L;
            this._seedData[i * 12 + 11] = 0L;
         }

         ++i;
      }
   }

   @Override
   public void writeImpl() {
      this.writeD(this._manorId);
      this.writeD(this._count);

      for(int i = 0; i < this._count; ++i) {
         this.writeD((int)this._seedData[i * 12 + 0]);
         this.writeD((int)this._seedData[i * 12 + 1]);
         this.writeC(1);
         this.writeD((int)this._seedData[i * 12 + 2]);
         this.writeC(1);
         this.writeD((int)this._seedData[i * 12 + 3]);
         this.writeD((int)this._seedData[i * 12 + 4]);
         this.writeD((int)this._seedData[i * 12 + 5]);
         this.writeD((int)this._seedData[i * 12 + 6]);
         this.writeD((int)this._seedData[i * 12 + 7]);
         this.writeQ(this._seedData[i * 12 + 8]);
         this.writeQ(this._seedData[i * 12 + 9]);
         this.writeQ(this._seedData[i * 12 + 10]);
         this.writeQ(this._seedData[i * 12 + 11]);
      }
   }
}
