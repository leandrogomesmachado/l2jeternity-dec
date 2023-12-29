package l2e.scripts.instances;

import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.instancemanager.ReflectionManager;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.reflection.ReflectionTemplate;
import l2e.gameserver.model.actor.templates.reflection.ReflectionWorld;
import l2e.gameserver.model.quest.QuestState;

public class HideoutoftheDawn extends AbstractReflection {
   public HideoutoftheDawn(String name, String descr) {
      super(name, descr);
      this.addStartNpc(new int[]{32593, 32617});
      this.addTalkId(new int[]{32593, 32617});
   }

   private final synchronized void enterInstance(Player player, Npc npc) {
      this.enterInstance(player, npc, new HideoutoftheDawn.HoDWorld(), 113);
   }

   @Override
   protected void onTeleportEnter(Player player, ReflectionTemplate template, ReflectionWorld world, boolean firstEntrance) {
      if (firstEntrance) {
         world.addAllowed(player.getObjectId());
         player.getAI().setIntention(CtrlIntention.IDLE);
         player.setReflectionId(world.getReflectionId());
         Location teleLoc = template.getTeleportCoord();
         player.teleToLocation(teleLoc, true);
         if (player.hasSummon()) {
            player.getSummon().getAI().setIntention(CtrlIntention.IDLE);
            player.getSummon().setReflectionId(world.getReflectionId());
            player.getSummon().teleToLocation(teleLoc, true);
         }
      } else {
         player.getAI().setIntention(CtrlIntention.IDLE);
         player.setReflectionId(world.getReflectionId());
         Location teleLoc = template.getTeleportCoord();
         player.teleToLocation(teleLoc, true);
         if (player.hasSummon()) {
            player.getSummon().getAI().setIntention(CtrlIntention.IDLE);
            player.getSummon().setReflectionId(world.getReflectionId());
            player.getSummon().teleToLocation(teleLoc, true);
         }
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         st = this.newQuestState(player);
      }

      if (npc.getId() == 32593) {
         this.enterInstance(player, npc);
         return null;
      } else if (npc.getId() == 32617) {
         ReflectionWorld world = ReflectionManager.getInstance().getPlayerWorld(player);
         if (world != null) {
            world.removeAllowed(player.getObjectId());
         }

         player.setReflectionId(0);
         player.teleToLocation(new Location(147072, 23743, -1984, 0), true);
         return null;
      } else {
         return htmltext;
      }
   }

   public static void main(String[] args) {
      new HideoutoftheDawn(HideoutoftheDawn.class.getSimpleName(), "instances");
   }

   private class HoDWorld extends ReflectionWorld {
      public HoDWorld() {
      }
   }
}
