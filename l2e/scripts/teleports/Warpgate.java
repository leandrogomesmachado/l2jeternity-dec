package l2e.scripts.teleports;

import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.instancemanager.HellboundManager;
import l2e.gameserver.model.PcCondOverride;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.service.BotFunctions;
import l2e.gameserver.model.zone.ZoneType;

public class Warpgate extends Quest {
   public Warpgate(int var1, String var2, String var3) {
      super(var1, var2, var3);
      this.addStartNpc(new int[]{32314, 32315, 32316, 32317, 32318, 32319});
      this.addFirstTalkId(new int[]{32314, 32315, 32316, 32317, 32318, 32319});
      this.addTalkId(new int[]{32314, 32315, 32316, 32317, 32318, 32319});
      this.addEnterZoneId(new int[]{40101});
   }

   private static final boolean canEnter(Player var0) {
      if (var0.isFlying()) {
         return false;
      } else if (!Config.ENTER_HELLBOUND_WITHOUT_QUEST && !var0.isInFightEvent() && !var0.canOverrideCond(PcCondOverride.ZONE_CONDITIONS)) {
         if (!HellboundManager.getInstance().isLocked()) {
            QuestState var1 = var0.getQuestState("_130_PathToHellbound");
            if (var1 != null && var1.isCompleted()) {
               return true;
            }
         }

         QuestState var2 = var0.getQuestState("_133_ThatsBloodyHot");
         return var2 != null && var2.isCompleted();
      } else {
         return true;
      }
   }

   @Override
   public final String onFirstTalk(Npc var1, Player var2) {
      return !canEnter(var2) && HellboundManager.getInstance().isLocked() ? "warpgate-locked.htm" : var1.getId() + ".htm";
   }

   @Override
   public final String onTalk(Npc var1, Player var2) {
      if (!canEnter(var2)) {
         return "warpgate-no.htm";
      } else {
         if (var2.isInParty() && var2.getParty().isLeader(var2) && var2.getVarB("autoTeleport@", false)) {
            for(Player var4 : var2.getParty().getMembers()) {
               if (var4 != null
                  && var4.getObjectId() != var2.getObjectId()
                  && Util.checkIfInRange(1000, var2, var4, true)
                  && canEnter(var4)
                  && BotFunctions.checkCondition(var4, false)
                  && var4.getIPAddress().equalsIgnoreCase(var2.getIPAddress())) {
                  var4.teleToLocation(-11272, 236464, -3248, true);
               }
            }
         }

         var2.teleToLocation(-11272, 236464, -3248, true);
         HellboundManager.getInstance().unlock();
         return null;
      }
   }

   @Override
   public final String onEnterZone(Creature var1, ZoneType var2) {
      if (var1.isPlayer()) {
         if (!canEnter(var1.getActingPlayer())) {
            ThreadPoolManager.getInstance().schedule(new Warpgate.Teleport(var1), 1000L);
         } else if (!((Player)var1).isMinimapAllowed() && var1.getInventory().getItemByItemId(9994) != null) {
            ((Player)var1).setMinimapAllowed(true);
         }
      }

      return null;
   }

   public static void main(String[] var0) {
      new Warpgate(-1, Warpgate.class.getSimpleName(), "teleports");
   }

   private static final class Teleport implements Runnable {
      private final Creature _char;

      public Teleport(Creature var1) {
         this._char = var1;
      }

      @Override
      public void run() {
         try {
            this._char.teleToLocation(-16555, 209375, -3670, true);
         } catch (Exception var2) {
            var2.printStackTrace();
         }
      }
   }
}
