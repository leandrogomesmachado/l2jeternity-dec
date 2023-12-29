package l2e.scripts.quests;

import l2e.gameserver.Config;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _10501_ZakenEmbroideredSoulCloak extends Quest {
   public _10501_ZakenEmbroideredSoulCloak(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(32612);
      this.addTalkId(32612);
      this.addKillId(29181);
      this.questItemIds = new int[]{21722};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("32612-01.htm")) {
            st.startQuest();
         } else if (event.equalsIgnoreCase("32612-03.htm")) {
            st.takeItems(21722, -1L);
            st.calcReward(this.getId());
            st.exitQuest(false, true);
            htmltext = "32612-reward.htm";
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 0:
               if (player.getLevel() < 78) {
                  htmltext = "32612-level_error.htm";
               } else {
                  htmltext = "32612-00.htm";
               }
               break;
            case 1:
               switch(st.getCond()) {
                  case 1:
                     return "32612-error.htm";
                  case 2:
                     htmltext = "32612-02.htm";
                     return htmltext;
                  default:
                     return htmltext;
               }
            case 2:
               htmltext = getAlreadyCompletedMsg(player);
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      if (npc.getId() == 29181) {
         if (player.getParty() != null) {
            if (player.getParty().getCommandChannel() != null) {
               for(Player ccMember : player.getParty().getCommandChannel()) {
                  if (ccMember != null && ccMember.isInRangeZ(npc, (long)Config.ALT_PARTY_RANGE2) && ccMember.getReflectionId() == player.getReflectionId()) {
                     this.rewardPlayer(ccMember, npc.getId());
                  }
               }
            } else {
               for(Player partyMember : player.getParty().getMembers()) {
                  if (partyMember != null
                     && partyMember.isInRangeZ(npc, (long)Config.ALT_PARTY_RANGE2)
                     && partyMember.getReflectionId() == player.getReflectionId()) {
                     this.rewardPlayer(partyMember, npc.getId());
                  }
               }
            }
         } else {
            this.rewardPlayer(player, npc.getId());
         }
      }

      return null;
   }

   private void rewardPlayer(Player player, int npcId) {
      QuestState st = player.getQuestState(this.getName());
      if (st != null && st.isCond(1) && st.calcDropItems(this.getId(), 21722, npcId, 20)) {
         st.setCond(2, true);
      }
   }

   public static void main(String[] args) {
      new _10501_ZakenEmbroideredSoulCloak(10501, _10501_ZakenEmbroideredSoulCloak.class.getSimpleName(), "");
   }
}
