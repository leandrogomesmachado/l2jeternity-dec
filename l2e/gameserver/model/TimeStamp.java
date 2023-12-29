package l2e.gameserver.model;

import l2e.commons.time.cron.SchedulingPattern;
import l2e.gameserver.Config;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.skills.Skill;

public class TimeStamp {
   private final int _id1;
   private final int _id2;
   private final long _reuse;
   private final long _stamp;
   private final int _group;
   private final boolean _decreaseReuse;

   public TimeStamp(Skill skill, long reuse) {
      this._id1 = skill.getId();
      this._id2 = skill.getLevel();
      this._reuse = reuse;
      this._stamp = System.currentTimeMillis() + reuse;
      this._group = -1;
      this._decreaseReuse = !skill.isHandler() && !skill.isItemSkill();
   }

   public TimeStamp(Skill skill, long reuse, long systime) {
      this._id1 = skill.getId();
      this._id2 = skill.getLevel();
      this._reuse = reuse;
      this._stamp = systime;
      this._group = -1;
      this._decreaseReuse = !skill.isHandler() && !skill.isItemSkill();
   }

   public TimeStamp(ItemInstance item, long reuse, boolean byCron) {
      this._id1 = item.getId();
      this._id2 = item.getObjectId();
      this._reuse = reuse;
      this._stamp = byCron ? new SchedulingPattern("30 6 * * *").next(System.currentTimeMillis()) : System.currentTimeMillis() + reuse;
      this._group = item.getSharedReuseGroup();
      this._decreaseReuse = false;
   }

   public TimeStamp(ItemInstance item, long reuse, long systime) {
      this._id1 = item.getId();
      this._id2 = item.getObjectId();
      this._reuse = reuse;
      this._stamp = systime;
      this._group = item.getSharedReuseGroup();
      this._decreaseReuse = false;
   }

   public long getStamp() {
      return this._stamp;
   }

   public int getItemId() {
      return this._id1;
   }

   public int getItemObjectId() {
      return this._id2;
   }

   public int getSkillId() {
      return this._id1;
   }

   public int getSkillLvl() {
      return this._id2;
   }

   public long getReuse() {
      return this._reuse;
   }

   public int getSharedReuseGroup() {
      return this._group;
   }

   public long getReuseBasic() {
      return this._reuse == 0L ? this.getRemaining() : this._reuse;
   }

   public long getRemaining() {
      return Math.max(this._stamp - System.currentTimeMillis(), 0L);
   }

   public boolean hasNotPassed() {
      return System.currentTimeMillis() < this._stamp - (this._decreaseReuse ? Config.ALT_REUSE_CORRECTION : 0L);
   }
}
