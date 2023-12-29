package l2e.scripts.quests;

import l2e.gameserver.network.NpcStringId;

public class _725_FortheSakeoftheTerritorySchuttgart extends TerritoryWarSuperClass {
   public static String qn1 = "_725_FortheSakeoftheTerritorySchuttgart";
   public static int qnu = 725;
   public static String qna = "";

   public _725_FortheSakeoftheTerritorySchuttgart() {
      super(qnu, qn1, qna);
      this.CATAPULT_ID = 36507;
      this.TERRITORY_ID = 89;
      this.LEADER_IDS = new int[]{36556, 36558, 36561, 36599};
      this.GUARD_IDS = new int[]{36557, 36559, 36560};
      qn = qn1;
      this.npcString = new NpcStringId[]{NpcStringId.THE_CATAPULT_OF_SCHUTTGART_HAS_BEEN_DESTROYED};
      this.registerKillIds();
   }
}
