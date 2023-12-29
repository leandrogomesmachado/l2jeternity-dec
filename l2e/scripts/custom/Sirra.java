package l2e.scripts.custom;

import l2e.gameserver.instancemanager.ReflectionManager;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.reflection.ReflectionWorld;
import l2e.scripts.ai.AbstractNpcAI;

public final class Sirra extends AbstractNpcAI {
   private Sirra() {
      super(Sirra.class.getSimpleName(), "custom");
      this.addFirstTalkId(32762);
   }

   @Override
   public String onFirstTalk(Npc npc, Player player) {
      ReflectionWorld world = ReflectionManager.getInstance().getWorld(npc.getReflectionId());
      if (world != null && world.getTemplateId() == 139) {
         return world.isStatus(0) ? "32762-easy.htm" : "32762-easyfight.htm";
      } else if (world != null && world.getTemplateId() == 144) {
         return world.isStatus(0) ? "32762-hard.htm" : "32762-hardfight.htm";
      } else {
         return "32762.htm";
      }
   }

   public static void main(String[] args) {
      new Sirra();
   }
}
