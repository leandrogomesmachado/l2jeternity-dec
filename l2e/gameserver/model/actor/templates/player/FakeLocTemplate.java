package l2e.gameserver.model.actor.templates.player;

import java.util.List;
import l2e.gameserver.model.Location;

public class FakeLocTemplate {
   protected int _id;
   protected int _amount;
   protected Location _loc;
   protected int _minLvl;
   protected int _maxLvl;
   protected List<Integer> _classes = null;
   protected int _distance;
   private int _currectAmount;

   public FakeLocTemplate(int id, int amount, Location loc, List<Integer> classes, int minLvl, int maxLvl, int distance) {
      this._id = id;
      this._amount = amount;
      this._loc = loc;
      this._classes = classes;
      this._minLvl = minLvl;
      this._maxLvl = maxLvl;
      this._distance = distance;
      this._currectAmount = 0;
   }

   public int getId() {
      return this._id;
   }

   public int getAmount() {
      return this._amount;
   }

   public Location getLocation() {
      return this._loc;
   }

   public List<Integer> getClasses() {
      return this._classes;
   }

   public int getMinLvl() {
      return this._minLvl;
   }

   public int getMaxLvl() {
      return this._maxLvl;
   }

   public void setCurrentAmount(int val) {
      this._currectAmount = val;
   }

   public int getCurrentAmount() {
      return this._currectAmount;
   }

   public int getDistance() {
      return this._distance;
   }
}
