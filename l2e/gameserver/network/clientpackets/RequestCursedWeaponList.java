package l2e.gameserver.network.clientpackets;

import java.util.ArrayList;
import java.util.List;
import l2e.gameserver.instancemanager.CursedWeaponsManager;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.network.serverpackets.ExCursedWeaponList;

public class RequestCursedWeaponList extends GameClientPacket {
   @Override
   protected void readImpl() {
   }

   @Override
   protected void runImpl() {
      Creature activeChar = this.getClient().getActiveChar();
      if (activeChar != null) {
         List<Integer> list = new ArrayList<>();

         for(int id : CursedWeaponsManager.getInstance().getCursedWeaponsIds()) {
            list.add(id);
         }

         activeChar.sendPacket(new ExCursedWeaponList(list));
      }
   }

   @Override
   protected boolean triggersOnActionRequest() {
      return false;
   }
}
