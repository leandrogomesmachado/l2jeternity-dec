package l2e.scripts.quests;

import gnu.trove.map.hash.TIntIntHashMap;
import l2e.gameserver.Config;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _385_YokeOfThePast extends Quest {
   private static final String qn = "_385_YokeOfThePast";
   private static final int[] GATEKEEPER_ZIGGURAT = new int[]{
      31095,
      31096,
      31097,
      31098,
      31099,
      31100,
      31101,
      31102,
      31103,
      31104,
      31105,
      31106,
      31107,
      31108,
      31109,
      31110,
      31114,
      31115,
      31116,
      31117,
      31118,
      31119,
      31120,
      31121,
      31122,
      31123,
      31124,
      31125,
      31126
   };
   private static final int ANCIENT_SCROLL = 5902;
   private static final int BLANK_SCROLL = 5965;
   private final TIntIntHashMap Chance = new TIntIntHashMap();

   public _385_YokeOfThePast(int questId, String name, String descr) {
      super(questId, name, descr);
      this.Chance.put(21208, 7);
      this.Chance.put(21209, 8);
      this.Chance.put(21210, 11);
      this.Chance.put(21211, 11);
      this.Chance.put(21213, 14);
      this.Chance.put(21214, 19);
      this.Chance.put(21215, 19);
      this.Chance.put(21217, 24);
      this.Chance.put(21218, 30);
      this.Chance.put(21219, 30);
      this.Chance.put(21221, 37);
      this.Chance.put(21222, 46);
      this.Chance.put(21223, 45);
      this.Chance.put(21224, 50);
      this.Chance.put(21225, 54);
      this.Chance.put(21226, 66);
      this.Chance.put(21227, 64);
      this.Chance.put(21228, 70);
      this.Chance.put(21229, 75);
      this.Chance.put(21230, 91);
      this.Chance.put(21231, 86);
      this.Chance.put(21236, 12);
      this.Chance.put(21237, 14);
      this.Chance.put(21238, 19);
      this.Chance.put(21239, 19);
      this.Chance.put(21240, 22);
      this.Chance.put(21241, 24);
      this.Chance.put(21242, 30);
      this.Chance.put(21243, 30);
      this.Chance.put(21244, 34);
      this.Chance.put(21245, 37);
      this.Chance.put(21246, 46);
      this.Chance.put(21247, 45);
      this.Chance.put(21248, 50);
      this.Chance.put(21249, 54);
      this.Chance.put(21250, 99);
      this.Chance.put(21251, 64);
      this.Chance.put(21252, 70);
      this.Chance.put(21253, 75);
      this.Chance.put(21254, 91);
      this.Chance.put(21255, 86);

      for(int ziggurat : GATEKEEPER_ZIGGURAT) {
         this.addStartNpc(ziggurat);
         this.addTalkId(ziggurat);
      }

      this.addKillId(
         new int[]{
            21208,
            21209,
            21210,
            21211,
            21213,
            21214,
            21215,
            21217,
            21218,
            21219,
            21221,
            21223,
            21224,
            21225,
            21226,
            21227,
            21228,
            21229,
            21230,
            21231,
            21236,
            21237,
            21238,
            21239,
            21240,
            21241,
            21242,
            21243,
            21244,
            21245,
            21246,
            21247,
            21248,
            21249,
            21250,
            21251,
            21252,
            21253,
            21254,
            21255
         }
      );
      this.questItemIds = new int[]{5902};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_385_YokeOfThePast");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("05.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("10.htm")) {
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      QuestState st = player.getQuestState("_385_YokeOfThePast");
      String htmltext = getNoQuestMsg(player);
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 0:
               if (player.getLevel() >= 20 && player.getLevel() <= 75) {
                  htmltext = "01.htm";
               } else {
                  htmltext = "02.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               if (st.getQuestItemsCount(5902) == 0L) {
                  htmltext = "08.htm";
               } else {
                  htmltext = "09.htm";
                  long count = st.getQuestItemsCount(5902);
                  st.takeItems(5902, -1L);
                  st.rewardItems(5965, count);
               }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      Player partyMember = this.getRandomPartyMemberState(player, (byte)1);
      if (partyMember == null) {
         return null;
      } else {
         QuestState st = partyMember.getQuestState("_385_YokeOfThePast");
         int p = this.Chance.get(npc.getId());
         int chance = (int)((float)p * Config.RATE_QUEST_DROP);
         int numItems = chance / 100;
         chance %= 100;
         if (st.getRandom(100) < chance) {
            ++numItems;
         }

         if (numItems > 0) {
            st.giveItems(5902, (long)numItems);
            st.playSound("ItemSound.quest_itemget");
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _385_YokeOfThePast(385, "_385_YokeOfThePast", "");
   }
}
