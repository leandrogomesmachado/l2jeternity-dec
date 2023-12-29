package l2e.scripts.quests;

import l2e.gameserver.network.NpcStringId;

public class _720_FortheSakeoftheTerritoryOren extends TerritoryWarSuperClass {
   public static String qn1 = "_720_FortheSakeoftheTerritoryOren";
   public static int qnu = 720;
   public static String qna = "";

   public _720_FortheSakeoftheTerritoryOren() {
      super(qnu, qn1, qna);
      this.CATAPULT_ID = 36502;
      this.TERRITORY_ID = 84;
      this.LEADER_IDS = new int[]{36526, 36528, 36531, 36594};
      this.GUARD_IDS = new int[]{36527, 36529, 36530};
      qn = qn1;
      this.npcString = new NpcStringId[]{NpcStringId.THE_CATAPULT_OF_OREN_HAS_BEEN_DESTROYED};
      this.registerKillIds();
   }
}
