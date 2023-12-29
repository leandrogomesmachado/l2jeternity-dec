package l2e.scripts.custom;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.NpcStringId;
import l2e.scripts.ai.AbstractNpcAI;

public final class Seyo extends AbstractNpcAI {
   private static final int SEYO = 32737;
   private static final int STONE_FRAGMENT = 15486;
   private static final NpcStringId[] TEXT = new NpcStringId[]{
      NpcStringId.NO_ONE_ELSE_DONT_WORRY_I_DONT_BITE_HAHA,
      NpcStringId.OK_MASTER_OF_LUCK_THATS_YOU_HAHA_WELL_ANYONE_CAN_COME_AFTER_ALL,
      NpcStringId.SHEDDING_BLOOD_IS_A_GIVEN_ON_THE_BATTLEFIELD_AT_LEAST_ITS_SAFE_HERE,
      NpcStringId.OK_WHOS_NEXT_IT_ALL_DEPENDS_ON_YOUR_FATE_AND_LUCK_RIGHT_AT_LEAST_COME_AND_TAKE_A_LOOK,
      NpcStringId.THERE_WAS_SOMEONE_WHO_WON_10000_FROM_ME_A_WARRIOR_SHOULDNT_JUST_BE_GOOD_AT_FIGHTING_RIGHT_YOUVE_GOTTA_BE_GOOD_IN_EVERYTHING
   };

   private Seyo() {
      super(Seyo.class.getSimpleName(), "custom");
      this.addStartNpc(32737);
      this.addTalkId(32737);
      this.addFirstTalkId(32737);
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = null;
      if (npc == null) {
         return htmltext;
      } else {
         switch(event) {
            case "TRICKERY_TIMER":
               if (npc.isScriptValue(1)) {
                  npc.setScriptValue(0);
                  this.broadcastNpcSay(npc, 22, TEXT[getRandom(TEXT.length)]);
               }
               break;
            case "give1":
               if (npc.isScriptValue(1)) {
                  htmltext = "32737-04.htm";
               } else if (!hasQuestItems(player, 15486)) {
                  htmltext = "32737-01.htm";
               } else {
                  npc.setScriptValue(1);
                  takeItems(player, 15486, 1L);
                  if (getRandom(100) == 0) {
                     giveItems(player, 15486, 100L);
                     this.broadcastNpcSay(
                        npc, 22, NpcStringId.AMAZING_S1_TOOK_100_OF_THESE_SOUL_STONE_FRAGMENTS_WHAT_A_COMPLETE_SWINDLER, new String[]{player.getName()}
                     );
                  } else {
                     this.broadcastNpcSay(npc, 22, NpcStringId.HMM_HEY_DID_YOU_GIVE_S1_SOMETHING_BUT_IT_WAS_JUST_1_HAHA, new String[]{player.getName()});
                  }

                  this.startQuestTimer("TRICKERY_TIMER", 5000L, npc, null);
               }
               break;
            case "give5":
               if (npc.isScriptValue(1)) {
                  htmltext = "32737-04.htm";
               } else if (getQuestItemsCount(player, 15486) < 5L) {
                  htmltext = "32737-02.htm";
               } else {
                  npc.setScriptValue(1);
                  takeItems(player, 15486, 5L);
                  int chance = getRandom(100);
                  if (chance < 20) {
                     this.broadcastNpcSay(npc, 22, NpcStringId.AHEM_S1_HAS_NO_LUCK_AT_ALL_TRY_PRAYING, new String[]{player.getName()});
                  } else if (chance < 80) {
                     giveItems(player, 15486, 1L);
                     this.broadcastNpcSay(npc, 22, NpcStringId.ITS_BETTER_THAN_LOSING_IT_ALL_RIGHT_OR_DOES_THIS_FEEL_WORSE);
                  } else {
                     int itemCount = getRandom(10, 16);
                     giveItems(player, 15486, (long)itemCount);
                     this.broadcastNpcSay(
                        npc, 22, NpcStringId.S1_PULLED_ONE_WITH_S2_DIGITS_LUCKY_NOT_BAD, new String[]{player.getName(), String.valueOf(itemCount)}
                     );
                  }

                  this.startQuestTimer("TRICKERY_TIMER", 5000L, npc, null);
               }
               break;
            case "give20":
               if (npc.isScriptValue(1)) {
                  htmltext = "32737-04.htm";
               } else if (getQuestItemsCount(player, 15486) < 20L) {
                  htmltext = "32737-03.htm";
               } else {
                  npc.setScriptValue(1);
                  takeItems(player, 15486, 20L);
                  int chance = getRandom(10000);
                  if (chance == 0) {
                     giveItems(player, 15486, 10000L);
                     this.broadcastNpcSay(
                        npc, 22, NpcStringId.AH_ITS_OVER_WHAT_KIND_OF_GUY_IS_THAT_DAMN_FINE_YOU_S1_TAKE_IT_AND_GET_OUTTA_HERE, new String[]{player.getName()}
                     );
                  } else if (chance < 10) {
                     giveItems(player, 15486, 1L);
                     this.broadcastNpcSay(npc, 22, NpcStringId.YOU_DONT_FEEL_BAD_RIGHT_ARE_YOU_SAD_BUT_DONT_CRY);
                  } else {
                     giveItems(player, 15486, (long)getRandom(1, 100));
                     this.broadcastNpcSay(npc, 22, NpcStringId.A_BIG_PIECE_IS_MADE_UP_OF_LITTLE_PIECES_SO_HERES_A_LITTLE_PIECE);
                  }

                  this.startQuestTimer("TRICKERY_TIMER", 5000L, npc, null);
               }
         }

         return htmltext;
      }
   }

   public static void main(String[] args) {
      new Seyo();
   }
}
