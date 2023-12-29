package l2e.scripts.ai.dragonvalley;

import java.util.Map;
import l2e.commons.util.NpcUtils;
import l2e.gameserver.Config;
import l2e.gameserver.ai.npc.Fighter;
import l2e.gameserver.model.PlayerGroup;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.QuestState;

public class DrakeBosses extends Fighter {
   public DrakeBosses(Attackable actor) {
      super(actor);
   }

   @Override
   protected void onEvtDead(Creature killer) {
      Npc corpse = null;
      switch(this.getActiveChar().getId()) {
         case 25725:
            corpse = NpcUtils.spawnSingleNpc(32884, this.getActiveChar().getLocation(), 300000L);
            break;
         case 25726:
            corpse = NpcUtils.spawnSingleNpc(32885, this.getActiveChar().getLocation(), 300000L);
            break;
         case 25727:
            corpse = NpcUtils.spawnSingleNpc(32886, this.getActiveChar().getLocation(), 300000L);
      }

      if (killer != null && corpse != null) {
         boolean isForAll = this.getActiveChar().getTemplate().getParameter("456QuestbyAggroList", false);
         Player player = killer.getActingPlayer();
         if (player != null) {
            if (isForAll) {
               for(Creature creature : this.getActiveChar().getAggroList().keySet()) {
                  if (creature != null && creature.isPlayer()) {
                     Player pl = creature.getActingPlayer();
                     if (pl != null
                        && !pl.isDead()
                        && (
                           this.getActiveChar().isInRangeZ(pl, (long)Config.ALT_PARTY_RANGE)
                              || this.getActiveChar().isInRangeZ(killer, (long)Config.ALT_PARTY_RANGE)
                        )) {
                        QuestState st = pl.getQuestState("_456_DontKnowDontCare");
                        if (st != null && st.isCond(1)) {
                           st.set("RaidKilled", corpse.getObjectId());
                        }
                     }
                  }
               }
            } else {
               PlayerGroup group = player.getPlayerGroup();
               if (group != null) {
                  Map<Creature, Attackable.AggroInfo> aggro = this.getActiveChar().getAggroList();

                  for(Player pl : group) {
                     if (pl != null
                        && !pl.isDead()
                        && aggro.containsKey(pl)
                        && (
                           this.getActiveChar().isInRangeZ(pl, (long)Config.ALT_PARTY_RANGE)
                              || this.getActiveChar().isInRangeZ(killer, (long)Config.ALT_PARTY_RANGE)
                        )) {
                        QuestState st = pl.getQuestState("_456_DontKnowDontCare");
                        if (st != null && st.isCond(1)) {
                           st.set("RaidKilled", corpse.getObjectId());
                        }
                     }
                  }
               }
            }
         }
      }

      super.onEvtDead(killer);
      this.getActiveChar().endDecayTask();
   }
}
