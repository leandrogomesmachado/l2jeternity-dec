package l2e.gameserver.model.service.buffer;

import java.util.ArrayList;
import java.util.List;

public class PlayerScheme {
   private final int _schemeId;
   private String _schemeName;
   private int _iconId;
   private final List<SchemeBuff> _schemeBuffs;

   public PlayerScheme(int schemeId, String schemeName, int iconId) {
      this._schemeId = schemeId;
      this._schemeName = schemeName;
      this._iconId = iconId;
      this._schemeBuffs = new ArrayList<>();
   }

   public int getSchemeId() {
      return this._schemeId;
   }

   public void setName(String name) {
      this._schemeName = name;
   }

   public String getName() {
      return this._schemeName;
   }

   public int getIconId() {
      return this._iconId;
   }

   public void setIcon(int iconId) {
      this._iconId = iconId;
   }

   public List<SchemeBuff> getBuffs() {
      return this._schemeBuffs;
   }

   public void addBuff(SchemeBuff buff) {
      this._schemeBuffs.add(buff);
   }
}
