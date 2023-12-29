package l2e.gameserver.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import l2e.commons.util.Rnd;
import l2e.gameserver.data.parser.NpcsParser;
import l2e.gameserver.idfactory.IdFactory;
import l2e.gameserver.model.actor.instance.MinionInstance;
import l2e.gameserver.model.actor.instance.MonsterInstance;
import l2e.gameserver.model.actor.templates.npc.MinionData;
import l2e.gameserver.model.actor.templates.npc.MinionTemplate;

public class MinionList {
   private final List<MinionData> _minionData;
   private final List<MinionInstance> _minions;
   private final MonsterInstance _master;
   private boolean _isRandomMinons = false;

   public MinionList(MonsterInstance master) {
      this._master = master;
      this._minions = new ArrayList<>();
      this._minionData = new ArrayList<>();
      this._isRandomMinons = this._master.getTemplate().isRandomMinons();
      this._minionData.addAll(this._master.getTemplate().getMinionData());
   }

   public boolean isRandomMinons() {
      return this._isRandomMinons;
   }

   public boolean addMinion(MinionData m, boolean spawn) {
      if (this._minionData.add(m)) {
         if (spawn) {
            this.spawnMinion(m);
         }

         return true;
      } else {
         return false;
      }
   }

   public boolean addMinion(MinionInstance m) {
      return this._minions.add(m);
   }

   public boolean hasAliveMinions() {
      for(MinionInstance m : this._minions) {
         if (m.isVisible() && !m.isDead()) {
            return true;
         }
      }

      return false;
   }

   public boolean hasMinions() {
      return this._minionData.size() > 0;
   }

   public boolean isWithMinions() {
      return this._minions.size() > 0;
   }

   public List<MinionInstance> getAliveMinions() {
      if (this._minions != null && !this._minions.isEmpty()) {
         List<MinionInstance> result = new ArrayList<>(this._minions.size());

         for(MinionInstance m : this._minions) {
            if (m.isVisible() && !m.isDead()) {
               result.add(m);
            }
         }

         return result;
      } else {
         return Collections.emptyList();
      }
   }

   public void spawnMinion(MinionData minions) {
      for(MinionTemplate minion : minions.getMinions()) {
         int minionId = minion.getMinionId();
         int minionCount = minion.getAmount();

         for(int i = 0; i < minionCount; ++i) {
            MinionInstance m = new MinionInstance(IdFactory.getInstance().getNextId(), NpcsParser.getInstance().getTemplate(minionId));
            m.setLeader(this._master);
            m.setIsNoRndWalk(true);
            if (m.getId() == 22830 || m.getId() == 22824) {
               m.isCanSupportMinion(false);
            }

            m.setIsRaidMinion(this._master.isRaid());
            this._master.spawnMinion(m);
            this._minions.add(m);
         }
      }
   }

   public void spawnMinions() {
      for(MinionData minions : this._minionData) {
         for(MinionTemplate minion : minions.getMinions()) {
            int minionId = minion.getMinionId();
            int minionCount = minion.getAmount();

            for(MinionInstance m : this._minions) {
               if (m.getId() == minionId) {
                  --minionCount;
               }

               if (m.isDead() || !m.isVisible()) {
                  m.refreshID();
                  this._master.spawnMinion(m);
               }
            }

            for(int i = 0; i < minionCount; ++i) {
               MinionInstance m = new MinionInstance(IdFactory.getInstance().getNextId(), NpcsParser.getInstance().getTemplate(minionId));
               m.setLeader(this._master);
               m.setIsNoRndWalk(true);
               m.setIsRaidMinion(this._master.isRaid());
               this._master.spawnMinion(m);
               this._minions.add(m);
            }
         }
      }
   }

   public void spawnRndMinions() {
      MinionData data = this._minionData.size() > 1 ? this._minionData.get(Rnd.get(this._minionData.size())) : this._minionData.get(0);
      if (data != null) {
         for(MinionTemplate minions : data.getMinions()) {
            int minionId = minions.getMinionId();
            int minionCount = minions.getAmount();

            for(MinionInstance m : this._minions) {
               if (m.getId() == minionId) {
                  --minionCount;
               }

               if (m.isDead() || !m.isVisible()) {
                  m.refreshID();
                  this._master.spawnMinion(m);
               }
            }

            for(int i = 0; i < minionCount; ++i) {
               MinionInstance m = new MinionInstance(IdFactory.getInstance().getNextId(), NpcsParser.getInstance().getTemplate(minionId));
               m.setLeader(this._master);
               m.setIsNoRndWalk(true);
               m.setIsRaidMinion(this._master.isRaid());
               this._master.spawnMinion(m);
               this._minions.add(m);
            }
         }
      }
   }

   public void unspawnMinions() {
      if (this._minions != null && !this._minions.isEmpty()) {
         for(MinionInstance m : this._minions) {
            if (m != null) {
               m.decayMe();
            }
         }
      }
   }

   public void deleteMinions() {
      if (this._minions != null && !this._minions.isEmpty()) {
         for(MinionInstance m : this._minions) {
            if (m != null) {
               m.deleteMe();
            }
         }

         this._minions.clear();
      }
   }
}
