package l2e.gameserver.model;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import l2e.gameserver.model.actor.Npc;

public class SiegeClan {
   private int _clanId = 0;
   private final List<Npc> _flag = new CopyOnWriteArrayList<>();
   private int _numFlagsAdded = 0;
   private SiegeClan.SiegeClanType _type;

   public SiegeClan(int clanId, SiegeClan.SiegeClanType type) {
      this._clanId = clanId;
      this._type = type;
   }

   public int getNumFlags() {
      return this._numFlagsAdded;
   }

   public void addFlag(Npc flag) {
      ++this._numFlagsAdded;
      this.getFlag().add(flag);
   }

   public boolean removeFlag(Npc flag) {
      if (flag == null) {
         return false;
      } else {
         boolean ret = this.getFlag().remove(flag);
         if (ret) {
            while(this.getFlag().remove(flag)) {
            }
         }

         flag.deleteMe();
         --this._numFlagsAdded;
         return ret;
      }
   }

   public void removeFlags() {
      for(Npc flag : this.getFlag()) {
         this.removeFlag(flag);
      }
   }

   public final int getClanId() {
      return this._clanId;
   }

   public final List<Npc> getFlag() {
      return this._flag;
   }

   public SiegeClan.SiegeClanType getType() {
      return this._type;
   }

   public void setType(SiegeClan.SiegeClanType setType) {
      this._type = setType;
   }

   public static enum SiegeClanType {
      OWNER,
      DEFENDER,
      ATTACKER,
      DEFENDER_PENDING;
   }
}
