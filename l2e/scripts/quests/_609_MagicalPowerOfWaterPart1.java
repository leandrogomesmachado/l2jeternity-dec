package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.holders.SkillHolder;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.NpcSay;

public class _609_MagicalPowerOfWaterPart1 extends Quest {
   public _609_MagicalPowerOfWaterPart1(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(31371);
      this.addTalkId(new int[]{31372, 31371, 31561});
      this.addAttackId(
         new int[]{
            21350, 21351, 21353, 21354, 21355, 21357, 21358, 21360, 21361, 21362, 21364, 21365, 21366, 21368, 21369, 21370, 21371, 21372, 21373, 21374, 21375
         }
      );
      this.questItemIds = new int[]{7237};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return null;
      } else {
         String htmltext = null;
         switch(event) {
            case "31371-02.htm":
               st.startQuest();
               htmltext = event;
               break;
            case "open_box":
               if (!st.hasQuestItems(1661)) {
                  htmltext = "31561-02.htm";
               } else if (st.isCond(2)) {
                  if (st.isSet("spawned")) {
                     st.takeItems(1661, 1L);
                     htmltext = "31561-04.htm";
                  } else {
                     st.giveItems(7237, 1L);
                     st.takeItems(1661, 1L);
                     st.setCond(3, true);
                     htmltext = "31561-03.htm";
                  }
               }
               break;
            case "eye_despawn":
               npc.broadcastPacket(new NpcSay(npc, 22, NpcStringId.UDAN_HAS_ALREADY_SEEN_YOUR_FACE), 2000);
               npc.deleteMe();
         }

         return htmltext;
      }
   }

   @Override
   public String onAttack(Npc npc, Player attacker, int damage, boolean isSummon) {
      QuestState st = attacker.getQuestState(this.getName());
      if (st != null && st.isCond(2) && !st.isSet("spawned")) {
         st.set("spawned", "1");
         npc.setTarget(attacker);
         npc.doCast(new SkillHolder(4547, 1).getSkill());
         Npc eye = addSpawn(31684, npc);
         if (eye != null) {
            eye.broadcastPacket(new NpcSay(eye, 22, NpcStringId.YOU_CANT_AVOID_THE_EYES_OF_UDAN), 2000);
            this.startQuestTimer("eye_despawn", 10000L, eye, attacker);
         }
      }

      return super.onAttack(npc, attacker, damage, isSummon);
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = this.getQuestState(player, true);
      if (st == null) {
         return htmltext;
      } else {
         switch(npc.getId()) {
            case 31371:
               switch(st.getState()) {
                  case 0:
                     return player.getLevel() >= 74
                        ? (this.hasAtLeastOneQuestItem(player, new int[]{7211, 7212, 7213, 7214, 7215}) ? "31371-01.htm" : "31371-00.htm")
                        : "31371-00a.htm";
                  case 1:
                     if (st.isCond(1)) {
                        htmltext = "31371-03.htm";
                     }

                     return htmltext;
                  default:
                     return htmltext;
               }
            case 31372:
               if (st.isStarted()) {
                  switch(st.getCond()) {
                     case 1:
                        htmltext = "31372-01.htm";
                        st.setCond(2, true);
                        break;
                     case 2:
                        if (st.isSet("spawned")) {
                           st.unset("spawned");
                           npc.setTarget(player);
                           npc.doCast(new SkillHolder(4548, 1).getSkill());
                           htmltext = "31372-03.htm";
                        } else {
                           htmltext = "31372-02.htm";
                        }
                        break;
                     case 3:
                        st.calcReward(this.getId());
                        st.exitQuest(true, true);
                        htmltext = "31372-04.htm";
                  }
               }
               break;
            case 31561:
               if (st.isCond(2)) {
                  htmltext = "31561-01.htm";
               }
         }

         return htmltext;
      }
   }

   public static void main(String[] args) {
      new _609_MagicalPowerOfWaterPart1(609, _609_MagicalPowerOfWaterPart1.class.getSimpleName(), "");
   }
}
