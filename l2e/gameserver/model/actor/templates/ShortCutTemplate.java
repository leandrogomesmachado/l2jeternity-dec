package l2e.gameserver.model.actor.templates;

import l2e.gameserver.model.base.ShortcutType;

public class ShortCutTemplate {
   private final int _slot;
   private final int _page;
   private final ShortcutType _type;
   private final int _id;
   private final int _level;
   private final int _characterType;
   private int _sharedReuseGroup = -1;
   private int _currentReuse = 0;
   private int _reuse = 0;
   private int _augmentationId = 0;

   public ShortCutTemplate(int slotId, int pageId, ShortcutType type, int shortcutId, int shortcutLevel, int characterType) {
      this._slot = slotId;
      this._page = pageId;
      this._type = type;
      this._id = shortcutId;
      this._level = shortcutLevel;
      this._characterType = characterType;
   }

   public int getId() {
      return this._id;
   }

   public int getLevel() {
      return this._level;
   }

   public int getPage() {
      return this._page;
   }

   public int getSlot() {
      return this._slot;
   }

   public ShortcutType getType() {
      return this._type;
   }

   public int getCharacterType() {
      return this._characterType;
   }

   public int getSharedReuseGroup() {
      return this._sharedReuseGroup;
   }

   public void setSharedReuseGroup(int g) {
      this._sharedReuseGroup = g;
   }

   public void setCurrenReuse(int reuse) {
      this._currentReuse = reuse;
   }

   public int getCurrenReuse() {
      return this._currentReuse;
   }

   public void setReuse(int reuse) {
      this._reuse = reuse;
   }

   public int getReuse() {
      return this._reuse;
   }

   public void setAugmentationId(int augmentationId) {
      this._augmentationId = augmentationId;
   }

   public int getAugmentationId() {
      return this._augmentationId;
   }
}
