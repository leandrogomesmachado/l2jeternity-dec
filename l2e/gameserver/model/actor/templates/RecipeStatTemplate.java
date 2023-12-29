package l2e.gameserver.model.actor.templates;

public class RecipeStatTemplate {
   private RecipeStatTemplate.StatType _type;
   private int _value;

   public RecipeStatTemplate(String type, int value) {
      this._type = Enum.valueOf(RecipeStatTemplate.StatType.class, type);
      this._value = value;
   }

   public RecipeStatTemplate.StatType getType() {
      return this._type;
   }

   public int getValue() {
      return this._value;
   }

   public static enum StatType {
      HP,
      MP,
      XP,
      SP,
      GIM;
   }
}
