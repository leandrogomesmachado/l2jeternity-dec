package l2e.scripts.quests;

public class _729_Protecttheterritorycatapult extends TerritoryWarSuperClass {
   public static String qn1 = "_729_Protecttheterritorycatapult";
   public static int qnu = 729;
   public static String qna = "";

   public _729_Protecttheterritorycatapult() {
      super(qnu, qn1, qna);
      this.NPC_IDS = new int[]{36499, 36500, 36501, 36502, 36503, 36504, 36505, 36506, 36507};
      qn = qn1;
      this.registerAttackIds();
   }

   @Override
   public int getTerritoryIdForThisNPCId(int npcid) {
      return npcid - 36418;
   }
}
