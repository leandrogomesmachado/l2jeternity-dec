package l2e.gameserver.model.actor.instance;

import java.util.StringTokenizer;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.entity.clanhall.SiegableHall;

public class CastleDoormenInstance extends DoormenInstance {
   public CastleDoormenInstance(int objectID, NpcTemplate template) {
      super(objectID, template);
      this.setInstanceType(GameObject.InstanceType.CastleDoormenInstance);
   }

   @Override
   protected final void openDoors(Player player, String command) {
      StringTokenizer st = new StringTokenizer(command.substring(10), ", ");
      st.nextToken();

      while(st.hasMoreTokens()) {
         if (this.getConquerableHall() != null) {
            this.getConquerableHall().openCloseDoor(Integer.parseInt(st.nextToken()), true);
         } else {
            this.getCastle().openDoor(player, Integer.parseInt(st.nextToken()));
         }
      }
   }

   @Override
   protected final void closeDoors(Player player, String command) {
      StringTokenizer st = new StringTokenizer(command.substring(11), ", ");
      st.nextToken();

      while(st.hasMoreTokens()) {
         if (this.getConquerableHall() != null) {
            this.getConquerableHall().openCloseDoor(Integer.parseInt(st.nextToken()), false);
         } else {
            this.getCastle().closeDoor(player, Integer.parseInt(st.nextToken()));
         }
      }
   }

   @Override
   protected final boolean isOwnerClan(Player player) {
      if (player.getClan() != null) {
         if (this.getConquerableHall() != null) {
            if (player.getClanId() == this.getConquerableHall().getOwnerId() && (player.getClanPrivileges() & 65536) == 65536) {
               return true;
            }
         } else if (this.getCastle() != null && player.getClanId() == this.getCastle().getOwnerId() && (player.getClanPrivileges() & 65536) == 65536) {
            return true;
         }
      }

      return false;
   }

   @Override
   protected final boolean isUnderSiege() {
      SiegableHall hall = this.getConquerableHall();
      return hall != null ? hall.isInSiege() : this.getCastle().getZone().isActive();
   }
}
