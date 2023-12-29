package l2e.scripts.quests;

import l2e.gameserver.network.NpcStringId;

public class _734_Piercethroughashield extends TerritoryWarSuperClass {
   public static String qn1 = "_734_Piercethroughashield";
   public static int qnu = 734;
   public static String qna = "";

   public _734_Piercethroughashield() {
      super(qnu, qn1, qna);
      this.CLASS_IDS = new int[]{6, 91, 5, 90, 20, 99, 33, 106};
      qn = qn1;
      this.RANDOM_MIN = 10;
      this.RANDOM_MAX = 15;
      this.npcString = new NpcStringId[]{NpcStringId.YOU_HAVE_DEFEATED_S2_OF_S1_KNIGHTS, NpcStringId.YOU_WEAKENED_THE_ENEMYS_DEFENSE};
   }
}
