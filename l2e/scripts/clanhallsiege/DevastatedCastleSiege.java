package l2e.scripts.clanhallsiege;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.data.holder.ClanHolder;
import l2e.gameserver.data.parser.NpcsParser;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.model.Clan;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.clanhall.ClanHallSiegeEngine;
import l2e.gameserver.network.NpcStringId;

public final class DevastatedCastleSiege extends ClanHallSiegeEngine {
   private static final String qn = "DevastatedCastleSiege";
   private static final int GUSTAV = 35410;
   private static final int MIKHAIL = 35409;
   private static final int DIETRICH = 35408;
   private static final double GUSTAV_TRIGGER_HP = NpcsParser.getInstance().getTemplate(35410).getBaseHpMax() / 12.0;
   private static Map<Integer, Integer> _damageToGustav = new HashMap<>();

   public DevastatedCastleSiege(int questId, String name, String descr, int hallId) {
      super(questId, name, descr, hallId);
      this.addKillId(35410);
      this.addSpawnId(new int[]{35409});
      this.addSpawnId(new int[]{35408});
      this.addAttackId(35410);
   }

   @Override
   public String onSpawn(Npc npc) {
      if (npc.getId() == 35409) {
         this.broadcastNpcSay(npc, 23, NpcStringId.GLORY_TO_ADEN_THE_KINGDOM_OF_THE_LION_GLORY_TO_SIR_GUSTAV_OUR_IMMORTAL_LORD);
      } else if (npc.getId() == 35408) {
         this.broadcastNpcSay(npc, 23, NpcStringId.SOLDIERS_OF_GUSTAV_GO_FORTH_AND_DESTROY_THE_INVADERS);
      }

      return null;
   }

   @Override
   public String onAttack(Npc npc, Player attacker, int damage, boolean isSummon) {
      if (!this._hall.isInSiege()) {
         return null;
      } else {
         synchronized(this) {
            Clan clan = attacker.getClan();
            if (clan != null && this.checkIsAttacker(clan)) {
               int id = clan.getId();
               if (_damageToGustav.containsKey(id)) {
                  int newDamage = _damageToGustav.get(id);
                  newDamage += damage;
                  _damageToGustav.put(id, newDamage);
               } else {
                  _damageToGustav.put(id, damage);
               }
            }

            if (npc.getCurrentHp() < GUSTAV_TRIGGER_HP && npc.getAI().getIntention() != CtrlIntention.CAST) {
               this.broadcastNpcSay(npc, 22, NpcStringId.THIS_IS_UNBELIEVABLE_HAVE_I_REALLY_BEEN_DEFEATED_I_SHALL_RETURN_AND_TAKE_YOUR_HEAD);
               npc.getAI().setIntention(CtrlIntention.CAST, SkillsParser.getInstance().getInfo(4235, 1), npc);
            }
         }

         return super.onAttack(npc, attacker, damage, isSummon);
      }
   }

   @Override
   public String onKill(Npc npc, Player killer, boolean isSummon) {
      if (!this._hall.isInSiege()) {
         return null;
      } else {
         this._missionAccomplished = true;
         if (npc.getId() == 35410) {
            synchronized(this) {
               this.cancelSiegeTask();
               this.endSiege();
            }
         }

         return super.onKill(npc, killer, isSummon);
      }
   }

   @Override
   public Clan getWinner() {
      int counter = 0;
      int damagest = 0;

      for(Entry<Integer, Integer> e : _damageToGustav.entrySet()) {
         int damage = e.getValue();
         if (damage > counter) {
            counter = damage;
            damagest = e.getKey();
         }
      }

      return ClanHolder.getInstance().getClan(damagest);
   }

   public static void main(String[] args) {
      new DevastatedCastleSiege(-1, "DevastatedCastleSiege", "clanhallsiege", 34);
   }
}
