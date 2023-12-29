package l2e.scripts.quests;

import l2e.gameserver.model.quest.Quest;

public class _635_IntoTheDimensionalRift extends Quest {
   private _635_IntoTheDimensionalRift(int questId, String name, String descr) {
      super(questId, name, descr);
   }

   public static void main(String[] args) {
      new _635_IntoTheDimensionalRift(635, _635_IntoTheDimensionalRift.class.getSimpleName(), "");
   }
}
