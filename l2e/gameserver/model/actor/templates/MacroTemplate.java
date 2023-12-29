package l2e.gameserver.model.actor.templates;

import l2e.gameserver.model.base.MacroType;

public class MacroTemplate {
   private final int _entry;
   private final MacroType _type;
   private final int _d1;
   private final int _d2;
   private final String _cmd;

   public MacroTemplate(int entry, MacroType type, int d1, int d2, String cmd) {
      this._entry = entry;
      this._type = type;
      this._d1 = d1;
      this._d2 = d2;
      this._cmd = cmd;
   }

   public int getEntry() {
      return this._entry;
   }

   public MacroType getType() {
      return this._type;
   }

   public int getD1() {
      return this._d1;
   }

   public int getD2() {
      return this._d2;
   }

   public String getCmd() {
      return this._cmd;
   }
}
