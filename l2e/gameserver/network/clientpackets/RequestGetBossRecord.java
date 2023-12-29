package l2e.gameserver.network.clientpackets;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import l2e.gameserver.instancemanager.RaidBossSpawnManager;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.serverpackets.ExGetBossRecord;

public class RequestGetBossRecord extends GameClientPacket {
   protected int _bossId;

   @Override
   protected void readImpl() {
      this._bossId = this.readD();
   }

   @Override
   protected void runImpl() {
      Player activeChar = this.getClient().getActiveChar();
      if (activeChar != null) {
         int totalPoints = 0;
         int ranking = 0;
         List<ExGetBossRecord.BossRecordInfo> list = new ArrayList<>();
         Map<Integer, Integer> points = RaidBossSpawnManager.getInstance().getPointsForOwnerId(activeChar.getObjectId());
         if (points != null && !points.isEmpty()) {
            for(Entry<Integer, Integer> e : points.entrySet()) {
               switch(e.getKey()) {
                  case -1:
                     ranking = e.getValue();
                     break;
                  case 0:
                     totalPoints = e.getValue();
                     break;
                  default:
                     list.add(new ExGetBossRecord.BossRecordInfo(e.getKey(), e.getValue(), 0));
               }
            }
         }

         activeChar.sendPacket(new ExGetBossRecord(ranking, totalPoints, list));
      }
   }

   @Override
   protected boolean triggersOnActionRequest() {
      return false;
   }
}
