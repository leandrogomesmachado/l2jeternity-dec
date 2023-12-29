package l2e.scripts.ai.custom;

import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.holders.SkillHolder;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.zone.ZoneId;
import l2e.scripts.ai.AbstractNpcAI;

public class BirthdayCake extends AbstractNpcAI {
   private static final int BIRTHDAY_CAKE_24 = 106;
   private static final int BIRTHDAY_CAKE = 139;

   protected BirthdayCake(String name, String descr) {
      super(name, descr);
      this.addFirstTalkId(new int[]{139, 106});
      this.addSpawnId(new int[]{139, 106});
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
         case 106:
            holder = new SkillHolder(22250, 1);
            break;
         case 139:
            holder = new SkillHolder(22035, 1);
            break;
         default:
            return;
      }

      ThreadPoolManager.getInstance().schedule(new BirthdayCake.BirthdayCakeAI(npc, holder), 1000L);
   }

   public static void main(String[] args) {
      new BirthdayCake(BirthdayCake.class.getSimpleName(), "ai/npc");
   }

   protected class BirthdayCakeAI implements Runnable {
      private final Npc _npc;
      private final SkillHolder _holder;

      protected BirthdayCakeAI(Npc npc, SkillHolder holder) {
         this._npc = npc;
         this._holder = holder;
      }

      @Override
      public void run() {
         if (this._npc != null && this._npc.isVisible() && this._holder != null && this._holder.getSkill() != null) {
            if (!this._npc.isInsideZone(ZoneId.PEACE)) {
               Skill skill = this._holder.getSkill();
               switch(this._npc.getId()) {
                  case 106:
                     Player player = this._npc.getSummoner().getActingPlayer();
                     if (player == null) {
                        ThreadPoolManager.getInstance().schedule(this, 1000L);
                        return;
                     }

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
                     break;
                  case 139:
                     for(Player player : World.getInstance().getAroundPlayers(this._npc, skill.getAffectRange(), 200)) {
                        skill.getEffects(this._npc, player, false);
                     }
               }
            }

            ThreadPoolManager.getInstance().schedule(this, 1000L);
         }
      }
   }
}
