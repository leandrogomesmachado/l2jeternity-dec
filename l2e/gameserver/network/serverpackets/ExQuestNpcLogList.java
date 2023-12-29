package l2e.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;

public class ExQuestNpcLogList extends GameServerPacket {
   private final int _questId;
   private final List<ExQuestNpcLogList.NpcHolder> _npcs = new ArrayList<>();

   public ExQuestNpcLogList(int questId) {
      this._questId = questId;
   }

   public void addNpc(int npcId, int count) {
      this._npcs.add(new ExQuestNpcLogList.NpcHolder(npcId, 0, count));
   }

   public void addNpc(int npcId, int unknown, int count) {
      this._npcs.add(new ExQuestNpcLogList.NpcHolder(npcId, unknown, count));
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._questId);
      this.writeC(this._npcs.size());

      for(ExQuestNpcLogList.NpcHolder holder : this._npcs) {
         this.writeD(holder.getNpcId() + 1000000);
         this.writeC(holder.getUnknown());
         this.writeD(holder.getCount());
      }
   }

   private class NpcHolder {
      private final int _npcId;
      private final int _unknown;
      private final int _count;

      public NpcHolder(int npcId, int unknown, int count) {
         this._npcId = npcId;
         this._unknown = unknown;
         this._count = count;
      }

      public int getNpcId() {
         return this._npcId;
      }

      public int getUnknown() {
         return this._unknown;
      }

      public int getCount() {
         return this._count;
      }
   }
}
