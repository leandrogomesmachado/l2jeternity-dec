package l2e.scripts.custom;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class ShadowWeapons extends Quest {
   private static final String qn = "ShadowWeapons";
   private static final int[] NPC = new int[]{
      30037,
      30066,
      30070,
      30109,
      30115,
      30120,
      30174,
      30175,
      30176,
      30187,
      30191,
      30195,
      30288,
      30289,
      30290,
      30297,
      30373,
      30462,
      30474,
      30498,
      30499,
      30500,
      30503,
      30504,
      30505,
      30511,
      30512,
      30513,
      30595,
      30676,
      30677,
      30681,
      30685,
      30687,
      30689,
      30694,
      30699,
      30704,
      30845,
      30847,
      30849,
      30854,
      30857,
      30862,
      30865,
      30894,
      30897,
      30900,
      30905,
      30910,
      30913,
      31269,
      31272,
      31288,
      31314,
      31317,
      31321,
      31324,
      31326,
      31328,
      31331,
      31334,
      31336,
      31965,
      31974,
      31276,
      31285,
      31958,
      31961,
      31996,
      31968,
      31977,
      32092,
      32093,
      32094,
      32095,
      32096,
      32097,
      32098,
      32193,
      32196,
      32199,
      32202,
      32205,
      32206,
      32213,
      32214,
      32221,
      32222,
      32229,
      32230,
      32233,
      32234
   };
   private static final int D_COUPON = 8869;
   private static final int C_COUPON = 8870;

   public ShadowWeapons(int id, String name, String descr) {
      super(id, name, descr);

      for(int item : NPC) {
         this.addStartNpc(item);
         this.addTalkId(item);
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      QuestState st = player.getQuestState("ShadowWeapons");
      String htmltext = getNoQuestMsg(player);
      if (st == null) {
         return htmltext;
      } else {
         boolean has_d = st.hasQuestItems(8869);
         boolean has_c = st.hasQuestItems(8870);
         if (!has_d && !has_c) {
            htmltext = "exchange-no.htm";
         } else {
            if (!has_d) {
               htmltext = "exchange-d.htm";
            } else if (!has_c) {
               htmltext = "exchange-c.htm";
            }

            htmltext = "exchange-b.htm";
         }

         st.exitQuest(true);
         return htmltext;
      }
   }

   public static void main(String[] args) {
      new ShadowWeapons(-1, "ShadowWeapons", "custom");
   }
}
