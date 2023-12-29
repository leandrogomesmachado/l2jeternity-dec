package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class GmViewQuestInfo extends GameServerPacket {
   private final Player _activeChar;

   public GmViewQuestInfo(Player cha) {
      this._activeChar = cha;
   }

   @Override
   protected final void writeImpl() {
      this.writeS(this._activeChar.getName());
      Quest[] questList = this._activeChar.getAllActiveQuests();
      if (questList.length == 0) {
         this.writeC(0);
         this.writeH(0);
         this.writeH(0);
      } else {
         this.writeH(questList.length);

         for(Quest q : questList) {
            this.writeD(q.getId());
            QuestState qs = this._activeChar.getQuestState(q.getName());
            if (qs == null) {
               this.writeD(0);
            } else {
               this.writeD(qs.getInt("cond"));
            }
         }
      }
   }
}
