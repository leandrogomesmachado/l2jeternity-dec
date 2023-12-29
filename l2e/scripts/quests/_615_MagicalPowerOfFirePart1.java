package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.holders.SkillHolder;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.NpcSay;

public class _615_MagicalPowerOfFirePart1 extends Quest {
   public _615_MagicalPowerOfFirePart1(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(31378);
      this.addTalkId(new int[]{31379, 31378, 31559});
      this.addAttackId(
         new int[]{
            21324, 21325, 21327, 21328, 21329, 21331, 21332, 21334, 21335, 21336, 21338, 21339, 21340, 21342, 21343, 21344, 21345, 21346, 21347, 21348, 21349
         }
      );
      this.questItemIds = new int[]{7242};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return null;
      } else {
         String htmltext = null;
         switch(event) {
            case "31378-02.htm":
               st.startQuest();
               htmltext = event;
               break;
            case "open_box":
               if (!st.hasQuestItems(1661)) {
                  htmltext = "31559-02.htm";
               } else if (st.isCond(2)) {
                  if (st.isSet("spawned")) {
                     st.takeItems(1661, 1L);
                     htmltext = "31559-04.htm";
                  } else {
                     st.giveItems(7242, 1L);
                     st.takeItems(1661, 1L);
                     st.setCond(3, true);
                     htmltext = "31559-03.htm";
                  }
               }
               break;
            case "eye_despawn":
               npc.broadcastPacket(new NpcSay(npc, 22, NpcStringId.ASEFA_HAS_ALREADY_SEEN_YOUR_FACE), 2000);
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
            eye.broadcastPacket(new NpcSay(eye, 22, NpcStringId.YOU_CANT_AVOID_THE_EYES_OF_ASEFA), 2000);
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
            case 31378:
               switch(st.getState()) {
                  case 0:
                     return player.getLevel() >= 74
                        ? (this.hasAtLeastOneQuestItem(player, new int[]{7221, 7222, 7223, 7224, 7225}) ? "31378-01.htm" : "31378-00.htm")
                        : "31378-00a.htm";
                  case 1:
                     if (st.isCond(1)) {
                        htmltext = "31378-03.htm";
                     }

                     return htmltext;
                  default:
                     return htmltext;
               }
            case 31379:
               if (st.isStarted()) {
                  switch(st.getCond()) {
                     case 1:
                        htmltext = "31379-01.htm";
                        st.setCond(2, true);
                        break;
                     case 2:
                        if (st.isSet("spawned")) {
                           st.unset("spawned");
                           npc.setTarget(player);
                           npc.doCast(new SkillHolder(4548, 1).getSkill());
                           htmltext = "31379-03.htm";
                        } else {
                           htmltext = "31379-02.htm";
                        }
                        break;
                     case 3:
                        st.calcReward(this.getId());
                        st.exitQuest(true, true);
                        htmltext = "31379-04.htm";
                  }
               }
               break;
            case 31559:
               if (st.isCond(2)) {
                  htmltext = "31559-01.htm";
               }
         }

         return htmltext;
      }
   }

   public static void main(String[] args) {
      new _615_MagicalPowerOfFirePart1(615, _615_MagicalPowerOfFirePart1.class.getSimpleName(), "");
   }
}
