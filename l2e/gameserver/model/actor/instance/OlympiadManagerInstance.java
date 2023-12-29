package l2e.gameserver.model.actor.instance;

import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;

public class OlympiadManagerInstance extends Npc {
   public OlympiadManagerInstance(int objectId, NpcTemplate template) {
      super(objectId, template);
      this.setInstanceType(GameObject.InstanceType.OlympiadManagerInstance);
   }

   public void showChatWindow(Player player, int val, String suffix) {
      String filename = "data/html/olympiad/";
      filename = filename + "noble_desc" + val;
      filename = filename + (suffix != null ? suffix + ".htm" : ".htm");
      if (filename.equals("data/html/olympiad/noble_desc0.htm")) {
         filename = "data/html/olympiad/noble_main.htm";
      }

      this.showChatWindow(player, filename);
   }
}
