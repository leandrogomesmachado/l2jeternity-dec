package l2e.gameserver.model.actor.instance.player;

public class CharacterVariable {
   private final String _name;
   private final String _value;
   private final long _expireTime;

   public CharacterVariable(String name, String value, long expireTime) {
      this._name = name;
      this._value = value;
      this._expireTime = expireTime;
   }

   public String getName() {
      return this._name;
   }

   public String getValue() {
      return this._value;
   }

   public long getExpireTime() {
      return this._expireTime;
   }

   public boolean isExpired() {
      return this._expireTime > 0L && this._expireTime < System.currentTimeMillis();
   }
}
