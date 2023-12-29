package l2e.gameserver.model.actor.appearance;

import l2e.gameserver.model.actor.Player;

public class PcAppearance {
   private Player _owner;
   private byte _face;
   private byte _hairColor;
   private byte _hairStyle;
   private boolean _sex;
   private boolean _displayName;
   private boolean _ghostmode = false;
   private String _visibleName;
   private String _visibleTitle;
   private int _nameColor = 16777215;
   private int _titleColor = 16777079;

   public PcAppearance(byte face, byte hColor, byte hStyle, boolean sex) {
      this._face = face;
      this._hairColor = hColor;
      this._hairStyle = hStyle;
      this._sex = sex;
   }

   public final void setVisibleName(String visibleName) {
      this._visibleName = visibleName;
      this._displayName = this._visibleName != null;
   }

   public final String getVisibleName() {
      if (this._visibleName == null) {
         return this.getOwner().getName();
      } else {
         return this._displayName ? this._visibleName : "";
      }
   }

   public final void setVisibleTitle(String visibleTitle) {
      this._visibleTitle = visibleTitle;
   }

   public final String getVisibleTitle() {
      if (this._visibleTitle == null) {
         return this.getOwner().getTitle();
      } else {
         return this._displayName ? this._visibleTitle : "";
      }
   }

   public final byte getFace() {
      return this._face;
   }

   public final void setFace(int value) {
      this._face = (byte)value;
   }

   public final byte getHairColor() {
      return this._hairColor;
   }

   public final void setHairColor(int value) {
      this._hairColor = (byte)value;
   }

   public final byte getHairStyle() {
      return this._hairStyle;
   }

   public final void setHairStyle(int value) {
      this._hairStyle = (byte)value;
   }

   public final boolean getSex() {
      return this._sex;
   }

   public final void setSex(boolean isfemale) {
      this._sex = isfemale;
   }

   public void setGhostMode(boolean b) {
      this._ghostmode = b;
   }

   public boolean isGhost() {
      return this._ghostmode;
   }

   public int getNameColor() {
      return this._nameColor;
   }

   public void setNameColor(int nameColor) {
      if (nameColor >= 0) {
         this._nameColor = nameColor;
      }
   }

   public void setNameColor(int red, int green, int blue) {
      this._nameColor = (red & 0xFF) + ((green & 0xFF) << 8) + ((blue & 0xFF) << 16);
   }

   public int getTitleColor() {
      return this._titleColor;
   }

   public void setTitleColor(int titleColor) {
      if (titleColor >= 0) {
         this._titleColor = titleColor;
      }
   }

   public void setTitleColor(int red, int green, int blue) {
      this._titleColor = (red & 0xFF) + ((green & 0xFF) << 8) + ((blue & 0xFF) << 16);
   }

   public void setOwner(Player owner) {
      this._owner = owner;
   }

   public Player getOwner() {
      return this._owner;
   }

   public boolean getDisplayName() {
      return this._displayName;
   }

   public void setDisplayName(boolean b) {
      this._displayName = b;
   }
}
