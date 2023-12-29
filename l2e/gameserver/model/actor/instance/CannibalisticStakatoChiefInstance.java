package l2e.gameserver.model.actor.instance;

import l2e.commons.util.Rnd;
import l2e.gameserver.Config;
import l2e.gameserver.model.Party;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;

public class CannibalisticStakatoChiefInstance extends RaidBossInstance {
   private static final int[] ITEMS = new int[]{14833, 14834};

   public CannibalisticStakatoChiefInstance(int objectId, NpcTemplate template) {
      super(objectId, template);
   }

   @Override
   protected void onDeath(Creature killer) {
      if (killer == null) {
         super.onDeath(killer);
      } else {
         Creature topdam = this.getMostHated();
         if (topdam == null) {
            topdam = killer;
         }

         Player player = topdam.getActingPlayer();
         if (player == null) {
            super.onDeath(killer);
         } else {
            Party party = player.getParty();
            if (party != null) {
               for(Player member : party.getMembers()) {
                  if (member != null && player.isInRange(member, (long)Config.ALT_PARTY_RANGE)) {
                     int itemId = ITEMS[Rnd.get(ITEMS.length)];
                     member.addItem("Reward", itemId, 1L, member, true);
                  }
               }
            } else {
               int itemId = ITEMS[Rnd.get(ITEMS.length)];
               player.addItem("Reward", itemId, 1L, player, true);
            }

            super.onDeath(killer);
         }
      }
   }
}
