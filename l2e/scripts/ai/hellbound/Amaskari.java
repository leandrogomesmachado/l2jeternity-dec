package l2e.scripts.ai.hellbound;

import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.instancemanager.HellboundManager;
import l2e.gameserver.model.MinionList;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.MinionInstance;
import l2e.gameserver.model.actor.instance.MonsterInstance;
import l2e.gameserver.model.holders.SkillHolder;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.NpcSay;
import l2e.scripts.ai.AbstractNpcAI;

public class Amaskari extends AbstractNpcAI {
   private static final int AMASKARI = 22449;
   private static final int AMASKARI_PRISONER = 22450;
   private static final int BUFF_ID = 4632;
   private static SkillHolder[] BUFF = new SkillHolder[]{new SkillHolder(4632, 1), new SkillHolder(4632, 2), new SkillHolder(4632, 3)};
   private static final NpcStringId[] AMASKARI_NPCSTRING_ID = new NpcStringId[]{
      NpcStringId.ILL_MAKE_EVERYONE_FEEL_THE_SAME_SUFFERING_AS_ME,
      NpcStringId.HA_HA_YES_DIE_SLOWLY_WRITHING_IN_PAIN_AND_AGONY,
      NpcStringId.MORE_NEED_MORE_SEVERE_PAIN,
      NpcStringId.SOMETHING_IS_BURNING_INSIDE_MY_BODY
   };
   private static final NpcStringId[] MINIONS_NPCSTRING_ID = new NpcStringId[]{
      NpcStringId.AHH_MY_LIFE_IS_BEING_DRAINED_OUT, NpcStringId.THANK_YOU_FOR_SAVING_ME, NpcStringId.IT_WILL_KILL_EVERYONE, NpcStringId.EEEK_I_FEEL_SICKYOW
   };

   private Amaskari(String name, String descr) {
      super(name, descr);
      this.addKillId(new int[]{22449, 22450});
      this.addAttackId(22449);
      this.addSpawnId(new int[]{22450});
   }

   @Override
   public final String onAdvEvent(String event, Npc npc, Player player) {
      if (event.equalsIgnoreCase("stop_toggle")) {
         npc.broadcastPacket(new NpcSay(npc.getObjectId(), 22, npc.getId(), AMASKARI_NPCSTRING_ID[2]), 2000);
         ((MonsterInstance)npc).clearAggroList();
         ((MonsterInstance)npc).getAI().setIntention(CtrlIntention.ACTIVE);
         npc.setIsInvul(false);
      } else if (event.equalsIgnoreCase("onspawn_msg") && npc != null && !npc.isDead()) {
         if (getRandom(100) > 20) {
            npc.broadcastPacket(new NpcSay(npc.getObjectId(), 22, npc.getId(), MINIONS_NPCSTRING_ID[2]), 2000);
         } else if (getRandom(100) > 40) {
            npc.broadcastPacket(new NpcSay(npc.getObjectId(), 22, npc.getId(), MINIONS_NPCSTRING_ID[3]), 2000);
         }

         this.startQuestTimer("onspawn_msg", (long)((getRandom(8) + 1) * 30000), npc, null);
      }

      return null;
   }

   @Override
   public String onAttack(Npc npc, Player attacker, int damage, boolean isSummon, Skill skill) {
      if (npc.getId() == 22449 && getRandom(1000) < 25) {
         npc.broadcastPacket(new NpcSay(npc.getObjectId(), 22, npc.getId(), AMASKARI_NPCSTRING_ID[0]), 2000);
         MinionList ml = npc.getMinionList();
         if (ml != null && ml.hasAliveMinions()) {
            for(MinionInstance minion : ml.getAliveMinions()) {
               if (minion != null && !minion.isDead() && getRandom(10) == 0) {
                  minion.broadcastPacket(new NpcSay(minion.getObjectId(), 22, minion.getId(), MINIONS_NPCSTRING_ID[0]), 2000);
                  minion.setCurrentHp(minion.getCurrentHp() - minion.getCurrentHp() / 5.0);
               }
            }
         }
      }

      return super.onAttack(npc, attacker, damage, isSummon, skill);
   }

   @Override
   public String onKill(Npc npc, Player killer, boolean isSummon) {
      if (npc.getId() == 22450) {
         Attackable master = ((MonsterInstance)npc).getLeader();
         if (master != null && !master.isDead()) {
            master.broadcastPacket(new NpcSay(master.getObjectId(), 22, master.getId(), AMASKARI_NPCSTRING_ID[1]), 2000);
            Effect e = master.getFirstEffect(4632);
            if (e != null && e.getSkill().getAbnormalLvl() == 3 && master.isInvul()) {
               master.setCurrentHp(master.getCurrentHp() + master.getCurrentHp() / 5.0);
            } else {
               master.clearAggroList();
               master.getAI().setIntention(CtrlIntention.ACTIVE);
               if (e == null) {
                  master.doCast(BUFF[0].getSkill());
               } else if (e != null && e.getSkill().getAbnormalLvl() > 0 && e.getSkill().getAbnormalLvl() < 3) {
                  master.doCast(BUFF[e.getSkill().getAbnormalLvl() + 1].getSkill());
               } else {
                  master.broadcastPacket(new NpcSay(master.getObjectId(), 22, master.getId(), AMASKARI_NPCSTRING_ID[3]), 2000);
                  master.setIsInvul(true);
                  this.startQuestTimer("stop_toggle", 10000L, master, null);
               }
            }
         }
      } else if (npc.getId() == 22449) {
         MinionList ml = npc.getMinionList();
         if (ml != null && ml.hasAliveMinions()) {
            for(MinionInstance minion : ml.getAliveMinions()) {
               if (minion != null && !minion.isDead()) {
                  if (getRandom(1000) > 300) {
                     minion.broadcastPacket(new NpcSay(minion.getObjectId(), 22, minion.getId(), MINIONS_NPCSTRING_ID[1]), 2000);
                  }

                  HellboundManager.getInstance().updateTrust(30, true);
                  minion.deleteMe();
               }
            }
         }
      }

      return super.onKill(npc, killer, isSummon);
   }

   @Override
   public final String onSpawn(Npc npc) {
      if (!npc.isTeleporting()) {
         this.startQuestTimer("onspawn_msg", (long)((getRandom(3) + 1) * 30000), npc, null);
      }

      return super.onSpawn(npc);
   }

   public static void main(String[] args) {
      new Amaskari(Amaskari.class.getSimpleName(), "ai");
   }
}
