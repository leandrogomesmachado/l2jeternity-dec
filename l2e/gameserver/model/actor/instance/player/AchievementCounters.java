package l2e.gameserver.model.actor.instance.player;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import l2e.commons.util.Util;
import l2e.gameserver.model.CommandChannel;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.player.AchiveTemplate;
import l2e.gameserver.model.entity.events.custom.achievements.AchievementManager;
import l2e.gameserver.network.serverpackets.ShowTutorialMark;

public class AchievementCounters {
   public final Map<Integer, Long> _achievements = new ConcurrentHashMap<>();
   private Player _player = null;
   protected int _objId = 0;

   public AchievementCounters(Player activeChar) {
      this._player = activeChar;
      this._objId = activeChar == null ? 0 : activeChar.getObjectId();
      this._achievements.clear();
   }

   public Map<Integer, Long> getAchievements() {
      return this._achievements;
   }

   public long getAchievementInfo(int id) {
      if (!this.hasAchievementInfo(id)) {
         this._achievements.put(id, 0L);
      }

      return this._achievements.get(id);
   }

   public void setAchievementInfo(int id, long points, boolean addSum) {
      if (this._player.getAchievements() != null && !this._player.getAchievements().isEmpty()) {
         if (this.hasAchievementInfo(id)) {
            long nextPoints = addSum ? points : this.getAchievementInfo(id) + points;
            this._achievements.put(id, nextPoints);
         } else {
            this._achievements.put(id, points);
         }

         this.checkProgress(id);
      }
   }

   public void refreshAchievementInfo(int id) {
      this._achievements.put(id, 0L);
      this.checkProgress(id);
   }

   public boolean hasAchievementInfo(int id) {
      return this._achievements.containsKey(id);
   }

   public void delAchievementInfo(int id) {
      this._achievements.remove(id);
   }

   protected Player getPlayer() {
      return this._player;
   }

   public void checkProgress(int id) {
      if (this._player != null && AchievementManager.getInstance().isActive()) {
         if (AchievementManager.getInstance().isActive()) {
            AchiveTemplate arc = AchievementManager.getInstance().getAchievement(id);
            if (arc != null) {
               int achievementId = arc.getId();
               int achievementLevel = this._player.getAchievements().get(achievementId);
               if (AchievementManager.getInstance().getMaxLevel(achievementId) <= achievementLevel) {
                  return;
               }

               AchiveTemplate nextLevelAchievement = AchievementManager.getInstance().getAchievement(achievementId, ++achievementLevel);
               if (nextLevelAchievement != null && nextLevelAchievement.isDone(this.getAchievementInfo(nextLevelAchievement.getId()))) {
                  this._player.sendPacket(new ShowTutorialMark(false, this._player.getObjectId()));
               }
            }
         }
      }
   }

   public void addAchivementInfo(String type, int select, long points, boolean addSum, boolean isForParty, boolean isForClan) {
      if (AchievementManager.getInstance().isActive()) {
         long addPoints = points > 0L ? points : 1L;
         AchiveTemplate arc = null;
         if (type.equals("killbyId")) {
            arc = AchievementManager.getInstance().getAchievementKillById(select);
         } else if (type.equals("reflectionById")) {
            arc = AchievementManager.getInstance().getAchievementRefById(select);
         } else if (type.equals("questById")) {
            arc = AchievementManager.getInstance().getAchievementQuestById(select);
         } else if (type.equals("enchantWeaponByLvl")) {
            arc = AchievementManager.getInstance().getAchievementWeaponEnchantByLvl(select);
         } else if (type.equals("enchantArmorByLvl")) {
            arc = AchievementManager.getInstance().getAchievementArmorEnchantByLvl(select);
         } else if (type.equals("enchantJewerlyByLvl")) {
            arc = AchievementManager.getInstance().getAchievementJewerlyEnchantByLvl(select);
         } else {
            arc = AchievementManager.getInstance().getAchievementType(type);
         }

         if (arc != null) {
            if (isForParty) {
               if (this._player.getParty() != null) {
                  CommandChannel channel = this._player.getParty().getCommandChannel();
                  if (channel != null) {
                     for(Player ccMember : channel.getMembers()) {
                        if (ccMember != null && Util.checkIfInRange(3000, this._player, ccMember, false)) {
                           ccMember.getCounters().setAchievementInfo(arc.getId(), addPoints, addSum);
                        }
                     }
                  } else {
                     for(Player pMember : this._player.getParty().getMembers()) {
                        if (pMember != null && Util.checkIfInRange(3000, this._player, pMember, false)) {
                           pMember.getCounters().setAchievementInfo(arc.getId(), addPoints, addSum);
                        }
                     }
                  }
               } else {
                  this.setAchievementInfo(arc.getId(), addPoints, addSum);
               }
            } else if (isForClan && this._player.getClan() != null) {
               for(Player member : this._player.getClan().getOnlineMembers()) {
                  if (member != null) {
                     member.getCounters().setAchievementInfo(arc.getId(), addPoints, addSum);
                  }
               }
            } else {
               this.setAchievementInfo(arc.getId(), addPoints, addSum);
            }
         }
      }
   }
}
