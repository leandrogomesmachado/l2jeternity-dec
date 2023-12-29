package l2e.scripts.quests;

import l2e.gameserver.network.NpcStringId;

public class _717_FortheSakeoftheTerritoryGludio extends TerritoryWarSuperClass {
   public static String qn1 = "_717_FortheSakeoftheTerritoryGludio";
   public static int qnu = 717;
   public static String qna = "";

   public _717_FortheSakeoftheTerritoryGludio() {
      super(qnu, qn1, qna);
      this.CATAPULT_ID = 36499;
      this.TERRITORY_ID = 81;
      this.LEADER_IDS = new int[]{36508, 36510, 36513, 36591};
      this.GUARD_IDS = new int[]{36509, 36511, 36512};
      qn = qn1;
      this.npcString = new NpcStringId[]{NpcStringId.THE_CATAPULT_OF_GLUDIO_HAS_BEEN_DESTROYED};
      this.registerKillIds();
   }
}
