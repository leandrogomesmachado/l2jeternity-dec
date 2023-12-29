package l2e.scripts.clanhallsiege;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import l2e.gameserver.GameTimeController;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.data.holder.ClanHolder;
import l2e.gameserver.model.Clan;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.clanhall.ClanHallSiegeEngine;
import l2e.gameserver.network.NpcStringId;

public final class FortressOfTheDeadSiege extends ClanHallSiegeEngine {
   private static final String qn = "FortressOfTheDeadSiege";
   private static final int LIDIA = 35629;
   private static final int ALFRED = 35630;
   private static final int GISELLE = 35631;
   private static Map<Integer, Integer> _damageToLidia = new HashMap<>();

   public FortressOfTheDeadSiege(int questId, String name, String descr, int hallId) {
      super(questId, name, descr, hallId);
      this.addKillId(35629);
      this.addKillId(35630);
      this.addKillId(35631);
      this.addSpawnId(new int[]{35629});
      this.addSpawnId(new int[]{35630});
      this.addSpawnId(new int[]{35631});
      this.addAttackId(35629);
   }

   @Override
   public String onSpawn(Npc npc) {
      if (npc.getId() == 35629) {
         this.broadcastNpcSay(
            npc,
            23,
            NpcStringId.HMM_THOSE_WHO_ARE_NOT_OF_THE_BLOODLINE_ARE_COMING_THIS_WAY_TO_TAKE_OVER_THE_CASTLE_HUMPH_THE_BITTER_GRUDGES_OF_THE_DEAD_YOU_MUST_NOT_MAKE_LIGHT_OF_THEIR_POWER
         );
      } else if (npc.getId() == 35630) {
         this.broadcastNpcSay(npc, 23, NpcStringId.HEH_HEH_I_SEE_THAT_THE_FEAST_HAS_BEGUN_BE_WARY_THE_CURSE_OF_THE_HELLMANN_FAMILY_HAS_POISONED_THIS_LAND);
      } else if (npc.getId() == 35631) {
         this.broadcastNpcSay(
            npc,
            23,
            NpcStringId.ARISE_MY_FAITHFUL_SERVANTS_YOU_MY_PEOPLE_WHO_HAVE_INHERITED_THE_BLOOD_IT_IS_THE_CALLING_OF_MY_DAUGHTER_THE_FEAST_OF_BLOOD_WILL_NOW_BEGIN
         );
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
               if (id > 0 && _damageToLidia.containsKey(id)) {
                  int newDamage = _damageToLidia.get(id);
                  newDamage += damage;
                  _damageToLidia.put(id, newDamage);
               } else {
                  _damageToLidia.put(id, damage);
               }
            }

            return null;
         }
      }
   }

   @Override
   public String onKill(Npc npc, Player killer, boolean isSummon) {
      if (!this._hall.isInSiege()) {
         return null;
      } else {
         int npcId = npc.getId();
         if (npcId == 35630 || npcId == 35631) {
            this.broadcastNpcSay(npc, 23, NpcStringId.AARGH_IF_I_DIE_THEN_THE_MAGIC_FORCE_FIELD_OF_BLOOD_WILL);
         }

         if (npcId == 35629) {
            this.broadcastNpcSay(
               npc, 23, NpcStringId.GRARR_FOR_THE_NEXT_2_MINUTES_OR_SO_THE_GAME_ARENA_ARE_WILL_BE_CLEANED_THROW_ANY_ITEMS_YOU_DONT_NEED_TO_THE_FLOOR_NOW
            );
            this._missionAccomplished = true;
            synchronized(this) {
               this.cancelSiegeTask();
               this.endSiege();
            }
         }

         return null;
      }
   }

   @Override
   public Clan getWinner() {
      int counter = 0;
      int damagest = 0;

      for(Entry<Integer, Integer> e : _damageToLidia.entrySet()) {
         int damage = e.getValue();
         if (damage > counter) {
            counter = damage;
            damagest = e.getKey();
         }
      }

      return ClanHolder.getInstance().getClan(damagest);
   }

   @Override
   public void startSiege() {
      int hoursLeft = GameTimeController.getInstance().getGameTime() / 60 % 24;
      if (hoursLeft >= 0 && hoursLeft <= 6) {
         super.startSiege();
      } else {
         this.cancelSiegeTask();
         long scheduleTime = (long)((24 - hoursLeft) * 10 * 60000);
         this._siegeTask = ThreadPoolManager.getInstance().schedule(new ClanHallSiegeEngine.SiegeStarts(), scheduleTime);
      }
   }

   public static void main(String[] args) {
      new FortressOfTheDeadSiege(-1, "FortressOfTheDeadSiege", "clanhallsiege", 64);
   }
}
