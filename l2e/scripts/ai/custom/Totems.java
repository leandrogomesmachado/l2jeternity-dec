package l2e.scripts.ai.custom;

import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.holders.SkillHolder;
import l2e.gameserver.model.skills.Skill;
import l2e.scripts.ai.AbstractNpcAI;

public class Totems extends AbstractNpcAI {
   private static final int TOTEM_OF_BODY = 143;
   private static final int TOTEM_OF_SPIRIT = 144;
   private static final int TOTEM_OF_BRAVERY = 145;
   private static final int TOTEM_OF_FORTITUDE = 146;

   protected Totems(String name, String descr) {
      super(name, descr);
      this.addFirstTalkId(new int[]{143, 144, 145, 146});
      this.addSpawnId(new int[]{143, 144, 145, 146});
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
      SkillHolder holder;
      switch(npc.getId()) {
         case 143:
            holder = new SkillHolder(23308, 1);
            break;
         case 144:
            holder = new SkillHolder(23309, 1);
            break;
         case 145:
            holder = new SkillHolder(23310, 1);
            break;
         case 146:
            holder = new SkillHolder(23311, 1);
            break;
         default:
            return;
      }

      ThreadPoolManager.getInstance().schedule(new Totems.TotemAI(npc, holder), 1000L);
   }

   public static void main(String[] args) {
      new Totems(Totems.class.getSimpleName(), "ai/npc");
   }

   protected class TotemAI implements Runnable {
      private final Npc _npc;
      private final SkillHolder _holder;

      protected TotemAI(Npc npc, SkillHolder holder) {
         this._npc = npc;
         this._holder = holder;
      }

      @Override
      public void run() {
         if (this._npc != null && this._npc.isVisible() && this._holder != null && this._holder.getSkill() != null) {
            Skill skill = this._holder.getSkill();

            for(Player player : World.getInstance().getAroundPlayers(this._npc, skill.getAffectRange(), 200)) {
               if (player.getFirstEffect(skill.getId()) == null) {
                  skill.getEffects(player, player, false);
               }
            }

            ThreadPoolManager.getInstance().schedule(this, 1000L);
         }
      }
   }
}
