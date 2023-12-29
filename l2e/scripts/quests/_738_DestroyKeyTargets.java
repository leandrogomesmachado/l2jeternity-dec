package l2e.scripts.quests;

import l2e.gameserver.network.NpcStringId;

public class _738_DestroyKeyTargets extends TerritoryWarSuperClass {
   public static String qn1 = "_738_DestroyKeyTargets";
   public static int qnu = 738;
   public static String qna = "";

   public _738_DestroyKeyTargets() {
      super(qnu, qn1, qna);
      this.CLASS_IDS = new int[]{51, 115, 57, 118};
      qn = qn1;
      this.RANDOM_MIN = 3;
      this.RANDOM_MAX = 8;
      this.npcString = new NpcStringId[]{NpcStringId.YOU_HAVE_DEFEATED_S2_OF_S1_WARSMITHS_AND_OVERLORDS, NpcStringId.YOU_DESTROYED_THE_ENEMYS_PROFESSIONALS};
   }
}
