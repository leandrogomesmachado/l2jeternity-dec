package l2e.gameserver.model.actor.templates;

import java.util.List;

public class ExtractableSkillTemplate {
   private final int _hash;
   private final List<ExtractableProductItemTemplate> _product;

   public ExtractableSkillTemplate(int hash, List<ExtractableProductItemTemplate> products) {
      this._hash = hash;
      this._product = products;
   }

   public int getSkillHash() {
      return this._hash;
   }

   public List<ExtractableProductItemTemplate> getProductItems() {
      return this._product;
   }
}
