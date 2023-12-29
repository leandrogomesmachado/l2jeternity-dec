package l2e.gameserver.model.items.multisell;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ListContainer {
   protected int _listId;
   protected boolean _applyTaxes = false;
   protected boolean _maintainEnchantment = false;
   protected double _useRate = 1.0;
   protected List<Entry> _entries = new ArrayList<>();
   protected Set<Integer> _npcsAllowed = null;

   public ListContainer(int listId) {
      this._listId = listId;
   }

   public final List<Entry> getEntries() {
      return this._entries;
   }

   public final int getListId() {
      return this._listId;
   }

   public final void setApplyTaxes(boolean applyTaxes) {
      this._applyTaxes = applyTaxes;
   }

   public final boolean getApplyTaxes() {
      return this._applyTaxes;
   }

   public final void setMaintainEnchantment(boolean maintainEnchantment) {
      this._maintainEnchantment = maintainEnchantment;
   }

   public double getUseRate() {
      return this._useRate;
   }

   public void setUseRate(double rate) {
      this._useRate = rate;
   }

   public final boolean getMaintainEnchantment() {
      return this._maintainEnchantment;
   }

   public void allowNpc(int npcId) {
      if (this._npcsAllowed == null) {
         this._npcsAllowed = new HashSet<>();
      }

      this._npcsAllowed.add(npcId);
   }

   public boolean isNpcAllowed(int npcId) {
      return this._npcsAllowed == null || this._npcsAllowed.contains(npcId);
   }

   public boolean isNpcOnly() {
      return this._npcsAllowed != null;
   }
}
