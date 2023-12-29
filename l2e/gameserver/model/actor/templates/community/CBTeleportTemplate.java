package l2e.gameserver.model.actor.templates.community;

import l2e.gameserver.model.Location;
import l2e.gameserver.model.holders.ItemHolder;

public class CBTeleportTemplate {
   private final int _id;
   private final int _minLvl;
   private final int _maxLvl;
   private final String _name;
   private final boolean _canPk;
   private final boolean _isForPremium;
   private final Location _loc;
   private final ItemHolder _price;

   public CBTeleportTemplate(int id, String name, int minLvl, int maxLvl, boolean canPk, boolean isForPremium, Location loc, ItemHolder price) {
      this._id = id;
      this._name = name;
      this._minLvl = minLvl;
      this._maxLvl = maxLvl;
      this._canPk = canPk;
      this._isForPremium = isForPremium;
      this._loc = loc;
      this._price = price;
   }

   public int getId() {
      return this._id;
   }

   public String getName() {
      return this._name;
   }

   public int getMinLvl() {
      return this._minLvl;
   }

   public int getMaxLvl() {
      return this._maxLvl;
   }

   public boolean canPk() {
      return this._canPk;
   }

   public Location getLocation() {
      return this._loc;
   }

   public ItemHolder getPrice() {
      return this._price;
   }

   public boolean isForPremium() {
      return this._isForPremium;
   }
}
