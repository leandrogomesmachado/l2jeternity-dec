package l2e.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;
import l2e.gameserver.model.actor.templates.SeedTemplate;

public final class BuyListSeed extends GameServerPacket {
   private final int _manorId;
   private final List<BuyListSeed.Seed> _list = new ArrayList<>();
   private final long _money;

   public BuyListSeed(long currentMoney, int castleId, List<SeedTemplate> seeds) {
      this._money = currentMoney;
      this._manorId = castleId;
      if (seeds != null && seeds.size() > 0) {
         for(SeedTemplate s : seeds) {
            if (s.getCanProduce() > 0L && s.getPrice() > 0L) {
               this._list.add(new BuyListSeed.Seed(s.getId(), s.getCanProduce(), s.getPrice()));
            }
         }
      }
   }

   @Override
   protected final void writeImpl() {
      this.writeQ(this._money);
      this.writeD(this._manorId);
      if (this._list != null && this._list.size() > 0) {
         this.writeH(this._list.size());

         for(BuyListSeed.Seed s : this._list) {
            this.writeD(s._itemId);
            this.writeD(s._itemId);
            this.writeD(0);
            this.writeQ(s._count);
            this.writeH(5);
            this.writeH(0);
            this.writeH(0);
            this.writeD(0);
            this.writeH(0);
            this.writeH(0);
            this.writeD(0);
            this.writeD(-1);
            this.writeD(-9999);
            this.writeH(0);
            this.writeH(0);

            for(byte i = 0; i < 6; ++i) {
               this.writeH(0);
            }

            this.writeH(0);
            this.writeH(0);
            this.writeH(0);
            this.writeQ(s._price);
         }

         this._list.clear();
      } else {
         this.writeH(0);
      }
   }

   private static class Seed {
      public final int _itemId;
      public final long _count;
      public final long _price;

      public Seed(int itemId, long count, long price) {
         this._itemId = itemId;
         this._count = count;
         this._price = price;
      }
   }
}
