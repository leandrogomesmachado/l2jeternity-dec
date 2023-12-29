package l2e.gameserver.network.clientpackets;

import java.util.ArrayList;
import java.util.List;
import l2e.gameserver.instancemanager.CursedWeaponsManager;
import l2e.gameserver.model.CursedWeapon;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.network.serverpackets.ExCursedWeaponLocation;

public final class RequestCursedWeaponLocation extends GameClientPacket {
   @Override
   protected void readImpl() {
   }

   @Override
   protected void runImpl() {
      Creature activeChar = this.getClient().getActiveChar();
      if (activeChar != null) {
         List<ExCursedWeaponLocation.CursedWeaponInfo> list = new ArrayList<>();

         for(CursedWeapon cw : CursedWeaponsManager.getInstance().getCursedWeapons()) {
            if (cw.isActive()) {
               Location pos = cw.getWorldPosition();
               if (pos != null) {
                  list.add(new ExCursedWeaponLocation.CursedWeaponInfo(pos, cw.getItemId(), cw.isActivated() ? 1 : 0));
               }
            }
         }

         if (!list.isEmpty()) {
            activeChar.sendPacket(new ExCursedWeaponLocation(list));
         }
      }
   }

   @Override
   protected boolean triggersOnActionRequest() {
      return false;
   }
}
