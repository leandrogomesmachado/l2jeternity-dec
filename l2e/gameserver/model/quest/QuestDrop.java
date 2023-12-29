package l2e.gameserver.model.quest;

import org.apache.commons.lang.ArrayUtils;

public class QuestDrop {
   public final int condition;
   public final int maxcount;
   public final int chance;
   public int[] itemList = ArrayUtils.EMPTY_INT_ARRAY;

   public QuestDrop(int condition, int maxcount, int chance) {
      this.condition = condition;
      this.maxcount = maxcount;
      this.chance = chance;
   }

   public QuestDrop addItem(int item) {
      this.itemList = ArrayUtils.add(this.itemList, item);
      return this;
   }
}
