package l2e.gameserver.model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import l2e.gameserver.model.actor.instance.ControllableMobInstance;

public class MobGroupData {
   private final Map<Integer, MobGroup> _groupMap = new ConcurrentHashMap<>();
   public static final int FOLLOW_RANGE = 300;
   public static final int RANDOM_RANGE = 300;

   protected MobGroupData() {
   }

   public static MobGroupData getInstance() {
      return MobGroupData.SingletonHolder._instance;
   }

   public void addGroup(int groupKey, MobGroup group) {
      this._groupMap.put(groupKey, group);
   }

   public MobGroup getGroup(int groupKey) {
      return this._groupMap.get(groupKey);
   }

   public int getGroupCount() {
      return this._groupMap.size();
   }

   public MobGroup getGroupForMob(ControllableMobInstance mobInst) {
      for(MobGroup mobGroup : this._groupMap.values()) {
         if (mobGroup.isGroupMember(mobInst)) {
            return mobGroup;
         }
      }

      return null;
   }

   public MobGroup[] getGroups() {
      return this._groupMap.values().toArray(new MobGroup[this.getGroupCount()]);
   }

   public boolean removeGroup(int groupKey) {
      return this._groupMap.remove(groupKey) != null;
   }

   private static class SingletonHolder {
      protected static final MobGroupData _instance = new MobGroupData();
   }
}
