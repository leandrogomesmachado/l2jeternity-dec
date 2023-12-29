package l2e.scripts.quests;

import l2e.gameserver.network.NpcStringId;

public class _735_Makespearsdull extends TerritoryWarSuperClass {
   public static String qn1 = "_735_Makespearsdull";
   public static int qnu = 735;
   public static String qna = "";

   public _735_Makespearsdull() {
      super(qnu, qn1, qna);
      this.CLASS_IDS = new int[]{
         23,
         101,
         36,
         108,
         8,
         93,
         2,
         88,
         3,
         89,
         48,
         114,
         46,
         113,
         55,
         117,
         9,
         92,
         24,
         102,
         37,
         109,
         34,
         107,
         21,
         100,
         127,
         131,
         128,
         132,
         129,
         133,
         130,
         134,
         135,
         136
      };
      qn = qn1;
      this.RANDOM_MIN = 15;
      this.RANDOM_MAX = 20;
      this.npcString = new NpcStringId[]{NpcStringId.YOU_HAVE_DEFEATED_S2_OF_S1_WARRIORS_AND_ROGUES, NpcStringId.YOU_WEAKENED_THE_ENEMYS_ATTACK};
   }
}
