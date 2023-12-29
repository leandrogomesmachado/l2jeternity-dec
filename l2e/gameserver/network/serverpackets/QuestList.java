package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class QuestList extends GameServerPacket {
   private final Quest[] _quests;
   private final Player _activeChar;

   public QuestList(Player player) {
      this._activeChar = player;
      this._quests = player.getAllActiveQuests();
   }

   @Override
   protected final void writeImpl() {
      if (this._quests != null) {
         this.writeH(this._quests.length);

         for(Quest q : this._quests) {
            this.writeD(q.getId());
            QuestState qs = this._activeChar.getQuestState(q.getName());
            if (qs == null) {
               this.writeD(0);
            } else {
               int states = qs.getInt("__compltdStateFlags");
               if (states != 0) {
                  this.writeD(states);
               } else {
                  this.writeD(qs.getInt("cond"));
               }
            }
         }
      } else {
         this.writeH(0);
      }

      this.writeB(new byte[128]);
   }
}
