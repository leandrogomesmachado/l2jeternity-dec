package l2e.scripts.quests;

public class _730_ProtecttheSuppliesSafe extends TerritoryWarSuperClass {
   public static String qn1 = "_730_ProtecttheSuppliesSafe.";
   public static int qnu = 730;
   public static String qna = "";

   public _730_ProtecttheSuppliesSafe() {
      super(qnu, qn1, qna);
      this.NPC_IDS = new int[]{36591, 36592, 36593, 36594, 36595, 36596, 36597, 36598, 36599};
      qn = qn1;
      this.registerAttackIds();
   }

   @Override
   public int getTerritoryIdForThisNPCId(int npcid) {
      return npcid - 36510;
   }
}
