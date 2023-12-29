package l2e.scripts.ai.custom;

import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.holders.SkillHolder;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.zone.ZoneId;
import l2e.scripts.ai.AbstractNpcAI;

public class ChristmasTree extends AbstractNpcAI {
   private static final int CHRISTMAS_TREE = 13007;

   protected ChristmasTree(String name, String descr) {
      super(name, descr);
      this.addFirstTalkId(13007);
      this.addSpawnId(new int[]{13007});
   }

   @Override
   public String onFirstTalk(Npc npc, Player player) {
      return null;
   }

   @Override
   public String onSpawn(Npc npc) {
      this.addTask(npc);
      return super.onSpawn(npc);
   }

   private void addTask(Npc npc) {
      SkillHolder holder = new SkillHolder(2139, 1);
      ThreadPoolManager.getInstance().schedule(new ChristmasTree.ChristmasTreeAI(npc, holder), 1000L);
   }

   public static void main(String[] args) {
      new ChristmasTree(ChristmasTree.class.getSimpleName(), "ai");
   }

   protected class ChristmasTreeAI implements Runnable {
      private final Npc _npc;
      private final SkillHolder _holder;

      protected ChristmasTreeAI(Npc npc, SkillHolder holder) {
         this._npc = npc;
         this._holder = holder;
      }

      @Override
      public void run() {
         if (this._npc != null && this._npc.isVisible() && this._holder != null && this._holder.getSkill() != null) {
            if (!this._npc.isInsideZone(ZoneId.PEACE)) {
               Skill skill = this._holder.getSkill();
               if (this._npc.getSummoner() == null || !this._npc.getSummoner().isPlayer()) {
                  ThreadPoolManager.getInstance().schedule(this, 1000L);
                  return;
               }

               Player player = this._npc.getSummoner().getActingPlayer();
               if (!player.isInParty()) {
                  if (player.isInsideRadius(this._npc, skill.getAffectRange(), true, true)) {
                     skill.getEffects(this._npc, player, false);
                  }
               } else {
                  for(Player member : player.getParty().getMembers()) {
                     if (member != null && member.isInsideRadius(this._npc, skill.getAffectRange(), true, true)) {
                        skill.getEffects(this._npc, member, false);
                     }
                  }
               }
            }

            ThreadPoolManager.getInstance().schedule(this, 1000L);
         }
      }
   }
}
