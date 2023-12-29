package l2e.scripts.quests;

import l2e.gameserver.network.NpcStringId;

public class _724_FortheSakeoftheTerritoryRune extends TerritoryWarSuperClass {
   public static String qn1 = "_724_FortheSakeoftheTerritoryRune";
   public static int qnu = 724;
   public static String qna = "";

   public _724_FortheSakeoftheTerritoryRune() {
      super(qnu, qn1, qna);
      this.CATAPULT_ID = 36506;
      this.TERRITORY_ID = 88;
      this.LEADER_IDS = new int[]{36550, 36552, 36555, 36598};
      this.GUARD_IDS = new int[]{36551, 36553, 36554};
      qn = qn1;
      this.npcString = new NpcStringId[]{NpcStringId.THE_CATAPULT_OF_RUNE_HAS_BEEN_DESTROYED};
      this.registerKillIds();
   }
}
