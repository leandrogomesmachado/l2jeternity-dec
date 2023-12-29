package l2e.gameserver.network.serverpackets;

import java.util.List;
import l2e.gameserver.data.parser.ManorParser;
import l2e.gameserver.model.actor.templates.SeedTemplate;

public class ExShowSeedInfo extends GameServerPacket {
   private final List<SeedTemplate> _seeds;
   private final int _manorId;

   public ExShowSeedInfo(int manorId, List<SeedTemplate> seeds) {
      this._manorId = manorId;
      this._seeds = seeds;
   }

   @Override
   protected void writeImpl() {
      this.writeC(0);
      this.writeD(this._manorId);
      this.writeD(0);
      if (this._seeds == null) {
         this.writeD(0);
      } else {
         this.writeD(this._seeds.size());

         for(SeedTemplate seed : this._seeds) {
            this.writeD(seed.getId());
            this.writeQ(seed.getCanProduce());
            this.writeQ(seed.getStartProduce());
            this.writeQ(seed.getPrice());
            this.writeD(ManorParser.getInstance().getSeedLevel(seed.getId()));
            this.writeC(1);
            this.writeD(ManorParser.getInstance().getRewardItemBySeed(seed.getId(), 1));
            this.writeC(1);
            this.writeD(ManorParser.getInstance().getRewardItemBySeed(seed.getId(), 2));
         }
      }
   }
}
