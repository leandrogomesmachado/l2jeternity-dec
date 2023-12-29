package l2e.gameserver.model.actor.instance;

import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;

public class WarehouseInstance extends NpcInstance {
   public WarehouseInstance(int objectId, NpcTemplate template) {
      super(objectId, template);
      this.setInstanceType(GameObject.InstanceType.WarehouseInstance);
   }

   @Override
   public boolean isWarehouse() {
      return true;
   }

   @Override
   public String getHtmlPath(int npcId, int val) {
      String pom = "";
      if (val == 0) {
         pom = "" + npcId;
      } else {
         pom = npcId + "-" + val;
      }

      return "data/html/warehouse/" + pom + ".htm";
   }
}
