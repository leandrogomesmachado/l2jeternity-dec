package l2e.scripts.instances;

import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.reflection.ReflectionTemplate;
import l2e.gameserver.model.actor.templates.reflection.ReflectionWorld;
import l2e.gameserver.model.quest.QuestState;

public final class JiniaGuildHideout1 extends AbstractReflection {
   private JiniaGuildHideout1() {
      super(JiniaGuildHideout1.class.getSimpleName(), "instances");
      this.addStartNpc(32020);
      this.addTalkId(32020);
   }

   private final synchronized void enterInstance(Player player, Npc npc) {
      this.enterInstance(player, npc, new JiniaGuildHideout1.JGH1World(), 140);
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
   public String onTalk(Npc npc, Player talker) {
      QuestState qs = talker.getQuestState("_10284_AcquisitionOfDivineSword");
      int cond;
      if (qs != null && ((cond = qs.getCond()) == 1 || cond == 2)) {
         this.enterInstance(talker, npc);
         if (qs.getCond() < 2) {
            qs.setCond(2, true);
         }
      }

      return super.onTalk(npc, talker);
   }

   public static void main(String[] args) {
      new JiniaGuildHideout1();
   }

   private class JGH1World extends ReflectionWorld {
      public JGH1World() {
      }
   }
}
