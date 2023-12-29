package l2e.scripts.quests;

import l2e.gameserver.network.NpcStringId;

public class _737_DenyBlessings extends TerritoryWarSuperClass {
   public static String qn1 = "_737_DenyBlessings";
   public static int qnu = 737;
   public static String qna = "";

   public _737_DenyBlessings() {
      super(qnu, qn1, qna);
      this.CLASS_IDS = new int[]{43, 112, 30, 105, 16, 97, 17, 98, 52, 116};
      qn = qn1;
      this.RANDOM_MIN = 3;
      this.RANDOM_MAX = 8;
      this.npcString = new NpcStringId[]{NpcStringId.YOU_HAVE_DEFEATED_S2_OF_S1_HEALERS_AND_BUFFERS, NpcStringId.YOU_WEAKENED_THE_ENEMYS_ATTACK};
   }
}
