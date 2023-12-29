package l2e.gameserver.model.actor.instance;

import l2e.gameserver.Config;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;

public class AdventurerInstance extends NpcInstance {
   public AdventurerInstance(int objectId, NpcTemplate template) {
      super(objectId, template);
      this.setInstanceType(GameObject.InstanceType.AdventurerInstance);
   }

   @Override
   public String getHtmlPath(int npcId, int val) {
      String pom = "";
      if (val == 0) {
         pom = "" + npcId;
      } else {
         pom = npcId + "-" + val;
      }

      return Config.PC_BANG_ENABLED ? "data/html/adventurer_guildsman/" + pom + "-pcbangpoint.htm" : "data/html/adventurer_guildsman/" + pom + ".htm";
   }
}
