package l2e.scripts.quests;

import l2e.gameserver.network.NpcStringId;

public class _736_Weakenmagic extends TerritoryWarSuperClass {
   public static String qn1 = "_736_Weakenmagic";
   public static int qnu = 736;
   public static String qna = "";

   public _736_Weakenmagic() {
      super(qnu, qn1, qna);
      this.CLASS_IDS = new int[]{40, 110, 27, 103, 13, 95, 12, 94, 41, 111, 28, 104, 14, 96};
      qn = qn1;
      this.RANDOM_MIN = 10;
      this.RANDOM_MAX = 15;
      this.npcString = new NpcStringId[]{NpcStringId.YOU_HAVE_DEFEATED_S2_OF_S1_ENEMIES, NpcStringId.YOU_WEAKENED_THE_ENEMYS_MAGIC};
   }
}
