package l2e.scripts.village_master;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;

public class Alliance extends Quest {
   private static final int[] NPCS = new int[]{
      30026,
      30031,
      30037,
      30066,
      30070,
      30109,
      30115,
      30120,
      30154,
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
      30358,
      30373,
      30462,
      30474,
      30498,
      30499,
      30500,
      30503,
      30504,
      30505,
      30508,
      30511,
      30512,
      30513,
      30520,
      30525,
      30565,
      30594,
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
      31276,
      31279,
      31285,
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
      31755,
      31958,
      31961,
      31965,
      31968,
      31974,
      31977,
      31996,
      32092,
      32093,
      32094,
      32095,
      32096,
      32097,
      32098,
      32145,
      32146,
      32147,
      32150,
      32153,
      32154,
      32157,
      32158,
      32160,
      32171,
      32193,
      32196,
      32199,
      32202,
      32205,
      32206,
      32209,
      32210,
      32213,
      32214,
      32217,
      32218,
      32221,
      32222,
      32225,
      32226,
      32229,
      32230,
      32233,
      32234
   };

   public Alliance(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(NPCS);
      this.addTalkId(NPCS);
   }

   @Override
   public String onTalk(Npc npc, Player talker) {
      return "9001-01.htm";
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      return !"9001-01.htm".equals(event) && player.getClan() == null ? "9001-04.htm" : event;
   }

   public static void main(String[] args) {
      new Alliance(-1, Alliance.class.getSimpleName(), "village_master");
   }
}
