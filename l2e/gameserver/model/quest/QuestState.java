package l2e.gameserver.model.quest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.commons.util.Rnd;
import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.data.htm.HtmCache;
import l2e.gameserver.data.parser.QuestsParser;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.handler.voicedcommandhandlers.IVoicedCommandHandler;
import l2e.gameserver.handler.voicedcommandhandlers.VoicedCommandHandler;
import l2e.gameserver.instancemanager.DailyTaskManager;
import l2e.gameserver.instancemanager.QuestManager;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.MonsterInstance;
import l2e.gameserver.model.actor.templates.daily.DailyTaskTemplate;
import l2e.gameserver.model.actor.templates.player.PlayerTaskTemplate;
import l2e.gameserver.model.actor.templates.quest.QuestDropItem;
import l2e.gameserver.model.actor.templates.quest.QuestTemplate;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.stats.StatsSet;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ExShowQuestMark;
import l2e.gameserver.network.serverpackets.InventoryUpdate;
import l2e.gameserver.network.serverpackets.PlaySound;
import l2e.gameserver.network.serverpackets.QuestList;
import l2e.gameserver.network.serverpackets.ShowTutorialMark;
import l2e.gameserver.network.serverpackets.StatusUpdate;
import l2e.gameserver.network.serverpackets.SystemMessage;
import l2e.gameserver.network.serverpackets.TutorialCloseHtml;
import l2e.gameserver.network.serverpackets.TutorialEnableClientEvent;
import l2e.gameserver.network.serverpackets.TutorialShowHtml;

public final class QuestState {
   protected static final Logger _log = Logger.getLogger(QuestState.class.getName());
   private final String _questName;
   private final Player _player;
   private byte _state;
   private Map<String, String> _vars;
   private boolean _isExitQuestOnCleanUp = false;

   public QuestState(Quest quest, Player player, byte state) {
      this._questName = quest.getName();
      this._player = player;
      this._state = state;
      player.setQuestState(this);
   }

   public String getQuestName() {
      return this._questName;
   }

   public Quest getQuest() {
      return QuestManager.getInstance().getQuest(this._questName);
   }

   public Player getPlayer() {
      return this._player;
   }

   public byte getState() {
      return this._state;
   }

   public boolean isCreated() {
      return this._state == 0;
   }

   public boolean isStarted() {
      return this._state == 1;
   }

   public boolean isCompleted() {
      return this._state == 2;
   }

   public boolean setState(byte state) {
      return this.setState(state, true);
   }

   public boolean setState(byte state, boolean saveInDb) {
      if (this._state == state) {
         return false;
      } else {
         boolean newQuest = this.isCreated();
         this._state = state;
         if (saveInDb) {
            if (newQuest) {
               Quest.createQuestInDb(this);
            } else {
               Quest.updateQuestInDb(this);
            }
         }

         this._player.sendPacket(new QuestList(this._player));
         return true;
      }
   }

   public String setInternal(String var, String val) {
      if (this._vars == null) {
         this._vars = new HashMap<>();
      }

      if (val == null) {
         val = "";
      }

      this._vars.put(var, val);
      return val;
   }

   public String set(String var, int val) {
      return this.set(var, Integer.toString(val));
   }

   public String set(String var, String val) {
      if (this._vars == null) {
         this._vars = new HashMap<>();
      }

      if (val == null) {
         val = "";
      }

      String old = this._vars.put(var, val);
      if (old != null) {
         Quest.updateQuestVarInDb(this, var, val);
      } else {
         Quest.createQuestVarInDb(this, var, val);
      }

      if ("cond".equals(var)) {
         try {
            int previousVal = 0;

            try {
               previousVal = Integer.parseInt(old);
            } catch (Exception var6) {
               previousVal = 0;
            }

            this.setCond(Integer.parseInt(val), previousVal);
         } catch (Exception var7) {
            _log.log(
               Level.WARNING,
               this._player.getName()
                  + ", "
                  + this.getQuestName()
                  + " cond ["
                  + val
                  + "] is not an integer.  Value stored, but no packet was sent: "
                  + var7.getMessage(),
               (Throwable)var7
            );
         }
      }

      return val;
   }

   private void setCond(int cond, int old) {
      if (cond != old) {
         int completedStateFlags = 0;
         if (cond >= 3 && cond <= 31) {
            completedStateFlags = this.getInt("__compltdStateFlags");
         } else {
            this.unset("__compltdStateFlags");
         }

         if (completedStateFlags == 0) {
            if (cond > old + 1) {
               completedStateFlags = -2147483647;
               completedStateFlags |= (1 << old) - 1;
               completedStateFlags |= 1 << cond - 1;
               this.set("__compltdStateFlags", String.valueOf(completedStateFlags));
            }
         } else if (cond < old) {
            completedStateFlags &= (1 << cond) - 1;
            if (completedStateFlags == (1 << cond) - 1) {
               this.unset("__compltdStateFlags");
            } else {
               completedStateFlags |= -2147483647;
               this.set("__compltdStateFlags", String.valueOf(completedStateFlags));
            }
         } else {
            completedStateFlags |= 1 << cond - 1;
            this.set("__compltdStateFlags", String.valueOf(completedStateFlags));
         }

         this._player.sendPacket(new QuestList(this._player));
         Quest q = this.getQuest();
         if (!q.isCustomQuest() && cond > 0) {
            this._player.sendPacket(new ExShowQuestMark(q.getId(), this.getCond()));
         }
      }
   }

   public String unset(String var) {
      if (this._vars == null) {
         return null;
      } else {
         String old = this._vars.remove(var);
         if (old != null) {
            Quest.deleteQuestVarInDb(this, var);
         }

         return old;
      }
   }

   public final void saveGlobalQuestVar(String var, String value) {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("REPLACE INTO character_quest_global_data (charId, var, value) VALUES (?, ?, ?)");
      ) {
         statement.setInt(1, this._player.getObjectId());
         statement.setString(2, var);
         statement.setString(3, value);
         statement.executeUpdate();
      } catch (Exception var35) {
         _log.log(Level.WARNING, "Could not insert player's global quest variable: " + var35.getMessage(), (Throwable)var35);
      }
   }

   public final String getGlobalQuestVar(String var) {
      String result = "";

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement ps = con.prepareStatement("SELECT value FROM character_quest_global_data WHERE charId = ? AND var = ?");
      ) {
         ps.setInt(1, this._player.getObjectId());
         ps.setString(2, var);

         try (ResultSet rs = ps.executeQuery()) {
            if (rs.first()) {
               result = rs.getString(1);
            }
         }
      } catch (Exception var61) {
         _log.log(Level.WARNING, "Could not load player's global quest variable: " + var61.getMessage(), (Throwable)var61);
      }

      return result;
   }

   public final void deleteGlobalQuestVar(String var) {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("DELETE FROM character_quest_global_data WHERE charId = ? AND var = ?");
      ) {
         statement.setInt(1, this._player.getObjectId());
         statement.setString(2, var);
         statement.executeUpdate();
      } catch (Exception var34) {
         _log.log(
            Level.WARNING,
            "could not delete player's global quest variable; charId = "
               + this._player.getObjectId()
               + ", variable name = "
               + var
               + ". Exception: "
               + var34.getMessage(),
            (Throwable)var34
         );
      }
   }

   public String get(String var) {
      return this._vars == null ? null : this._vars.get(var);
   }

   public int getInt(String var) {
      if (this._vars == null) {
         return 0;
      } else {
         String variable = this._vars.get(var);
         if (variable != null && !variable.isEmpty()) {
            int varint = 0;

            try {
               varint = Integer.parseInt(variable);
            } catch (NumberFormatException var5) {
               _log.log(
                  Level.INFO,
                  "Quest "
                     + this.getQuestName()
                     + ", method getInt("
                     + var
                     + "), tried to parse a non-integer value ("
                     + variable
                     + "). Char ID: "
                     + this._player.getObjectId(),
                  (Throwable)var5
               );
            }

            return varint;
         } else {
            return 0;
         }
      }
   }

   public boolean isCond(int condition) {
      return this.getInt("cond") == condition;
   }

   public QuestState setCond(int value) {
      if (this.isStarted()) {
         this.set("cond", Integer.toString(value));
      }

      return this;
   }

   public int getCond() {
      return this.isStarted() ? this.getInt("cond") : 0;
   }

   public boolean isSet(String variable) {
      return this.get(variable) != null;
   }

   public QuestState setCond(int value, boolean playQuestMiddle) {
      if (!this.isStarted()) {
         return this;
      } else {
         this.set("cond", String.valueOf(value));
         if (playQuestMiddle) {
            Quest.playSound(this._player, Quest.QuestSound.ITEMSOUND_QUEST_MIDDLE);
         }

         return this;
      }
   }

   public QuestState setMemoState(int value) {
      this.set("memoState", String.valueOf(value));
      return this;
   }

   public int getMemoState() {
      return this.isStarted() ? this.getInt("memoState") : 0;
   }

   public boolean isMemoState(int memoState) {
      return this.getInt("memoState") == memoState;
   }

   public int getMemoStateEx(int slot) {
      return this.isStarted() ? this.getInt("memoStateEx" + slot) : 0;
   }

   public QuestState setMemoStateEx(int slot, int value) {
      this.set("memoStateEx" + slot, String.valueOf(value));
      return this;
   }

   public boolean isMemoStateEx(int memoStateEx) {
      return this.getInt("memoStateEx") == memoStateEx;
   }

   public void addNotifyOfDeath(Creature character) {
      if (character.isPlayer()) {
         ((Player)character).addNotifyQuestOfDeath(this);
      }
   }

   public long getQuestItemsCount(int itemId) {
      return Quest.getQuestItemsCount(this._player, itemId);
   }

   public long getQuestItemsCount(int... itemsIds) {
      long result = 0L;

      for(int id : itemsIds) {
         result += this.getQuestItemsCount(id);
      }

      return result;
   }

   public boolean hasQuestItems(int itemId) {
      return Quest.hasQuestItems(this._player, itemId);
   }

   public boolean hasQuestItems(int... itemIds) {
      return Quest.hasQuestItems(this._player, itemIds);
   }

   public int getEnchantLevel(int itemId) {
      return Quest.getEnchantLevel(this._player, itemId);
   }

   public void rewardItems(int itemId, long count) {
      Quest.rewardItems(this._player, itemId, count);
   }

   public void giveItems(int itemId, long count) {
      Quest.giveItems(this._player, itemId, count, 0);
   }

   public void giveItems(int itemId, long count, int enchantlevel) {
      Quest.giveItems(this._player, itemId, count, enchantlevel);
   }

   public void giveItems(int itemId, long count, byte attributeId, int attributeLevel) {
      Quest.giveItems(this._player, itemId, count, attributeId, attributeLevel);
   }

   public boolean dropQuestItems(int itemId, int count, int dropChance) {
      return this.dropQuestItems(itemId, count, count, -1L, dropChance, true);
   }

   public boolean dropQuestItems(int itemId, int count, long neededCount, int dropChance, boolean sound) {
      return Quest.dropQuestItems(this._player, itemId, count, count, neededCount, dropChance, sound);
   }

   public boolean dropQuestItems(int itemId, int minCount, int maxCount, long neededCount, int dropChance, boolean sound) {
      return Quest.dropQuestItems(this._player, itemId, minCount, maxCount, neededCount, dropChance, sound);
   }

   public boolean dropQuestItems(int itemId, int minCount, int maxCount, long neededCount, boolean infiniteCount, float dropChance, boolean sound) {
      long currentCount = this.getQuestItemsCount(itemId);
      if (!infiniteCount && neededCount > 0L && currentCount >= neededCount) {
         return true;
      } else {
         int MAX_CHANCE = 1000;
         int adjDropChance = (int)(dropChance * 10.0F * Config.RATE_QUEST_DROP);
         int curDropChance = adjDropChance;
         int adjMaxCount = (int)((float)maxCount * Config.RATE_QUEST_DROP);
         long itemCount = 0L;
         if (adjDropChance > 1000 && !Config.PRECISE_DROP_CALCULATION) {
            int multiplier = adjDropChance / 1000;
            if (minCount < maxCount) {
               itemCount += (long)Rnd.get(minCount * multiplier, maxCount * multiplier);
            } else if (minCount == maxCount) {
               itemCount += (long)(minCount * multiplier);
            } else {
               itemCount += (long)multiplier;
            }

            curDropChance = adjDropChance % 1000;
         }

         for(int random = Rnd.get(1000); random < curDropChance; curDropChance -= 1000) {
            if (minCount < maxCount) {
               itemCount += (long)Rnd.get(minCount, maxCount);
            } else if (minCount == maxCount) {
               itemCount += (long)minCount;
            } else {
               ++itemCount;
            }
         }

         if (itemCount > 0L) {
            if (itemCount > (long)adjMaxCount) {
               itemCount = (long)adjMaxCount;
            }

            itemCount *= 1L;
            if (!infiniteCount && neededCount > 0L && currentCount + itemCount > neededCount) {
               itemCount = neededCount - currentCount;
            }

            if (!this.getPlayer().getInventory().validateCapacityByItemId(itemId)) {
               return false;
            }

            this.getPlayer().addItem("Quest", itemId, itemCount, this.getPlayer().getTarget(), true);
            if (sound) {
               if (neededCount == 0L) {
                  this.playSound(Quest.QuestSound.ITEMSOUND_QUEST_ITEMGET);
               } else {
                  this.playSound(
                     currentCount % neededCount + itemCount < neededCount ? Quest.QuestSound.ITEMSOUND_QUEST_ITEMGET : Quest.QuestSound.ITEMSOUND_QUEST_MIDDLE
                  );
               }
            }
         }

         return !infiniteCount && neededCount > 0L && currentCount + itemCount >= neededCount;
      }
   }

   public boolean dropItemsAlways(int itemId, int count, long neededCount) {
      return this.dropItems(itemId, count, neededCount, 1000000, (byte)1);
   }

   public synchronized long dropItems(int itemId, long count, long limit) {
      boolean have = false;
      long qic = this.getQuestItemsCount(itemId);
      if (qic > 0L) {
         have = true;
      }

      if (count <= 0L) {
         return qic;
      } else {
         count = (long)((int)((float)count * Config.RATE_QUEST_DROP));
         if (limit > 0L && qic + count > limit) {
            count = limit - qic;
         }

         ItemInstance item = this.getPlayer().getInventory().addItem("QuestItemDrop", itemId, count, this.getPlayer(), this.getPlayer().getTarget());
         if (item == null) {
            return qic;
         } else {
            if (count > 1L) {
               SystemMessage smsg = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S);
               smsg.addItemName(item);
               smsg.addItemNumber(count);
               this.getPlayer().sendPacket(smsg);
            } else {
               SystemMessage smsg = SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1);
               smsg.addItemName(item);
               this.getPlayer().sendPacket(smsg);
            }

            InventoryUpdate iu = new InventoryUpdate();
            if (item.isStackable() && have) {
               iu.addModifiedItem(item);
            } else {
               iu.addNewItem(item);
            }

            this.getPlayer().sendPacket(iu);
            StatusUpdate su = new StatusUpdate(this.getPlayer().getObjectId());
            su.addAttribute(14, this.getPlayer().getCurrentLoad());
            this.getPlayer().sendPacket(su);
            return qic + count;
         }
      }
   }

   public boolean dropItems(int itemId, int count, long neededCount, int dropChance) {
      return this.dropItems(itemId, count, neededCount, dropChance, (byte)0);
   }

   public boolean dropItems(int itemId, int count, long neededCount, int dropChance, byte type) {
      long currentCount = this.getQuestItemsCount(itemId);
      if (neededCount > 0L && currentCount >= neededCount) {
         return true;
      } else {
         int amount = 0;
         switch(type) {
            case 0:
               dropChance = (int)((float)dropChance * Config.RATE_QUEST_DROP);
               amount = count * (dropChance / 1000000);
               if (Rnd.get(1000000) < dropChance % 1000000) {
                  amount += count;
               }
               break;
            case 1:
               if (Rnd.get(1000000) < dropChance) {
                  amount = (int)((float)count * Config.RATE_QUEST_DROP);
               }
               break;
            case 2:
               if ((float)Rnd.get(1000000) < (float)dropChance * Config.RATE_QUEST_DROP) {
                  amount = count;
               }
               break;
            case 3:
               if (Rnd.get(1000000) < dropChance) {
                  amount = count;
               }
         }

         boolean reached = false;
         if (amount > 0) {
            if (neededCount > 0L) {
               reached = currentCount + (long)amount >= neededCount;
               amount = (int)(reached ? neededCount - currentCount : (long)amount);
            }

            if (!this._player.getInventory().validateCapacityByItemId(itemId)) {
               return false;
            }

            this.giveItems(itemId, (long)amount, 0);
            this.playSound(reached ? "ItemSound.quest_middle" : "ItemSound.quest_itemget");
         }

         return neededCount > 0L && reached;
      }
   }

   public int rollDrop(int count, double calcChance) {
      return !(calcChance <= 0.0) && count > 0 ? this.rollDrop(count, count, calcChance) : 0;
   }

   public int rollDrop(int min, int max, double calcChance) {
      if (!(calcChance <= 0.0) && min > 0 && max > 0) {
         int dropmult = 1;
         calcChance *= this.getRateQuestsDrop();
         if (calcChance > 100.0) {
            if ((double)((int)Math.ceil(calcChance / 100.0)) <= calcChance / 100.0) {
               calcChance = Math.nextUp(calcChance);
            }

            dropmult = (int)Math.ceil(calcChance / 100.0);
            calcChance /= (double)dropmult;
         }

         return Rnd.chance(calcChance) ? Rnd.get(min * dropmult, max * dropmult) : 0;
      } else {
         return 0;
      }
   }

   public boolean rollAndGive(int itemId, int count, double calcChance) {
      if (!(calcChance <= 0.0) && count > 0 && itemId > 0) {
         int countToDrop = this.rollDrop(count, calcChance);
         if (countToDrop > 0) {
            this.giveItems(itemId, (long)countToDrop);
            this.playSound("ItemSound.quest_itemget");
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public boolean rollAndGive(int itemId, int min, int max, int limit, double calcChance) {
      if (!(calcChance <= 0.0) && min > 0 && max > 0 && limit > 0 && itemId > 0) {
         long count = (long)this.rollDrop(min, max, calcChance);
         if (count > 0L) {
            long alreadyCount = this.getQuestItemsCount(itemId);
            if (alreadyCount + count > (long)limit) {
               count = (long)limit - alreadyCount;
            }

            if (count > 0L) {
               this.giveItems(itemId, count);
               if (count + alreadyCount >= (long)limit) {
                  this.playSound("ItemSound.quest_middle");
                  return true;
               }

               this.playSound("ItemSound.quest_itemget");
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public void rollAndGive(int itemId, int min, int max, double calcChance) {
      if (!(calcChance <= 0.0) && min > 0 && max > 0 && itemId > 0) {
         int count = this.rollDrop(min, max, calcChance);
         if (count > 0) {
            this.giveItems(itemId, (long)count);
            this.playSound("ItemSound.quest_itemget");
         }
      }
   }

   public void addRadar(int x, int y, int z) {
      this._player.getRadar().addMarker(x, y, z);
   }

   public void removeRadar(int x, int y, int z) {
      this._player.getRadar().removeMarker(x, y, z);
   }

   public void clearRadar() {
      this._player.getRadar().removeAllMarkers();
   }

   public void takeItems(int itemId, long count) {
      Quest.takeItems(this._player, itemId, count);
   }

   public long takeAllItems(int itemId) {
      return Quest.takeAllItems(this.getPlayer(), itemId, -1L);
   }

   public long takeAllItems(int... itemsIds) {
      long result = 0L;

      for(int id : itemsIds) {
         result += this.takeAllItems(id);
      }

      return result;
   }

   public void playSound(String sound) {
      Quest.playSound(this._player, sound);
   }

   public void playSound(Quest.QuestSound sound) {
      Quest.playSound(this._player, sound);
   }

   public void addExpAndSp(int exp, int sp) {
      exp = (int)((double)exp * this.getPlayer().getStat().getRExp());
      sp = (int)((double)sp * this.getPlayer().getStat().getRSp());
      Quest.addExpAndSp(this._player, (long)exp, sp);
   }

   public int getItemEquipped(int loc) {
      return Quest.getItemEquipped(this._player, loc);
   }

   public final boolean isExitQuestOnCleanUp() {
      return this._isExitQuestOnCleanUp;
   }

   public void setIsExitQuestOnCleanUp(boolean isExitQuestOnCleanUp) {
      this._isExitQuestOnCleanUp = isExitQuestOnCleanUp;
   }

   public void startQuestTimer(String name, long time) {
      this.getQuest().startQuestTimer(name, time, null, this.getPlayer(), false);
   }

   public void startQuestTimer(String name, long time, Npc npc) {
      this.getQuest().startQuestTimer(name, time, npc, this.getPlayer(), false);
   }

   public void startRepeatingQuestTimer(String name, long time) {
      this.getQuest().startQuestTimer(name, time, null, this.getPlayer(), true);
   }

   public void startRepeatingQuestTimer(String name, long time, Npc npc) {
      this.getQuest().startQuestTimer(name, time, npc, this.getPlayer(), true);
   }

   public final QuestTimer getQuestTimer(String name) {
      return this.getQuest().getQuestTimer(name, null, this.getPlayer());
   }

   public Npc addSpawn(int npcId) {
      return this.addSpawn(npcId, this._player.getX(), this._player.getY(), this._player.getZ(), 0, false, 0, false);
   }

   public Npc addSpawn(int npcId, int despawnDelay) {
      return this.addSpawn(npcId, this._player.getX(), this._player.getY(), this._player.getZ(), 0, false, despawnDelay, false);
   }

   public Npc addSpawn(int npcId, int x, int y, int z) {
      return this.addSpawn(npcId, x, y, z, 0, false, 0, false);
   }

   public Npc addSpawn(int npcId, int x, int y, int z, int despawnDelay) {
      return this.addSpawn(npcId, x, y, z, 0, false, despawnDelay, false);
   }

   public Npc addSpawn(int npcId, Creature cha) {
      return this.addSpawn(npcId, cha.getX(), cha.getY(), cha.getZ(), cha.getHeading(), true, 0, false);
   }

   public Npc addSpawn(int npcId, Creature cha, int despawnDelay) {
      return this.addSpawn(npcId, cha.getX(), cha.getY(), cha.getZ(), cha.getHeading(), true, despawnDelay, false);
   }

   public Npc addSpawn(int npcId, Creature cha, boolean randomOffset, int despawnDelay) {
      return this.addSpawn(npcId, cha.getX(), cha.getY(), cha.getZ(), cha.getHeading(), randomOffset, despawnDelay, false);
   }

   public Npc addSpawn(int npcId, int x, int y, int z, int heading, boolean randomOffset, int despawnDelay) {
      return this.addSpawn(npcId, x, y, z, heading, randomOffset, despawnDelay, false);
   }

   public Npc addSpawn(int npcId, int x, int y, int z, int heading, boolean randomOffset, int despawnDelay, boolean isSummonSpawn) {
      return Quest.addSpawn(npcId, x, y, z, heading, randomOffset, (long)despawnDelay, isSummonSpawn);
   }

   public String showHtmlFile(String fileName) {
      return this.getQuest().showHtmlFile(this.getPlayer(), fileName);
   }

   public QuestState startQuest() {
      if (this.isCreated() && !this.getQuest().isCustomQuest()) {
         this.set("cond", "1");
         this.setState((byte)1);
         this.playSound(Quest.QuestSound.ITEMSOUND_QUEST_ACCEPT);
      }

      return this;
   }

   public QuestState exitQuest(QuestState.QuestType type) {
      switch(type) {
         case DAILY:
            this.exitQuest(false);
            this.setRestartTime();
            break;
         default:
            this.exitQuest(type == QuestState.QuestType.REPEATABLE);
      }

      return this;
   }

   public QuestState exitWithCheckHwid(QuestState.QuestType type, boolean playExitQuest) {
      switch(type) {
         case DAILY:
            this.exitQuest(false);
            this.setRestartTime();
            QuestManager.getInstance().insert(this._player.getHWID(), this.getQuest().getId());
            break;
         default:
            this.exitQuest(type == QuestState.QuestType.REPEATABLE);
      }

      if (playExitQuest) {
         this.playSound(Quest.QuestSound.ITEMSOUND_QUEST_FINISH);
      }

      return this;
   }

   public boolean isHwidAvailable() {
      return QuestManager.getInstance().isHwidAvailable(this._player, this.getQuest().getId());
   }

   public QuestState exitQuest(QuestState.QuestType type, boolean playExitQuest) {
      this.exitQuest(type);
      if (playExitQuest) {
         this.playSound(Quest.QuestSound.ITEMSOUND_QUEST_FINISH);
      }

      return this;
   }

   public QuestState exitQuest(boolean repeatable) {
      this._player.removeNotifyQuestOfDeath(this);
      if (!this.isStarted()) {
         return this;
      } else {
         if (Config.ALLOW_DAILY_TASKS && this.getQuest().getId() > 0 && this._player != null && this._player.getActiveDailyTasks() != null) {
            for(PlayerTaskTemplate taskTemplate : this._player.getActiveDailyTasks()) {
               if (taskTemplate.getType().equalsIgnoreCase("Quest") && !taskTemplate.isComplete()) {
                  DailyTaskTemplate task = DailyTaskManager.getInstance().getDailyTask(taskTemplate.getId());
                  if (task.getQuestId() == this.getQuest().getId()) {
                     taskTemplate.setIsComplete(true);
                     this._player.updateDailyStatus(taskTemplate);
                     IVoicedCommandHandler vch = VoicedCommandHandler.getInstance().getHandler("missions");
                     if (vch != null) {
                        vch.useVoicedCommand("missions", this._player, null);
                     }
                  }
               }
            }
         }

         this.getQuest().removeRegisteredQuestItems(this._player);
         Quest.deleteQuestInDb(this, repeatable);
         if (repeatable) {
            this._player.getCounters().addAchivementInfo("repeatableQuest", 0, -1L, false, false, false);
            this._player.delQuestState(this.getQuestName());
            this._player.sendPacket(new QuestList(this._player));
         } else {
            this._player.getCounters().addAchivementInfo("notRepeatableQuest", 0, -1L, false, false, false);
            this.setState((byte)2);
         }

         this._player.getCounters().addAchivementInfo("questById", this.getQuest().getId(), -1L, false, false, false);
         this._vars = null;
         return this;
      }
   }

   public QuestState exitQuest(boolean repeatable, boolean playExitQuest) {
      this.exitQuest(repeatable);
      if (playExitQuest) {
         this.playSound(Quest.QuestSound.ITEMSOUND_QUEST_FINISH);
      }

      return this;
   }

   public void showQuestionMark(boolean quest, int number) {
      this._player.sendPacket(new ShowTutorialMark(quest, number));
   }

   public void playTutorialVoice(String voice) {
      this._player.sendPacket(new PlaySound(2, voice, 0, 0, this._player.getX(), this._player.getY(), this._player.getZ()));
   }

   public void showTutorialHTML(String html) {
      Player player = this.getPlayer();
      String lang = player.getLang();
      String filepath = "data/scripts/quests/_255_Tutorial/" + lang + "/" + html;
      String content = HtmCache.getInstance().getHtm(player, filepath);
      if (content == null) {
         filepath = "data/scripts/quests/_255_Tutorial/en/" + html;
         content = HtmCache.getInstance().getHtm(player, filepath);
      }

      if (content == null) {
         _log.warning("Cache[HTML]: Missing HTML page: " + filepath);
         content = "<html><body>File data/scripts/quests/_255_Tutorial/" + html + " not found or file is empty.</body></html>";
      }

      player.sendPacket(new TutorialShowHtml(content));
   }

   public void closeTutorialHtml() {
      this._player.sendPacket(TutorialCloseHtml.STATIC_PACKET);
   }

   public void onTutorialClientEvent(int number) {
      this._player.sendPacket(new TutorialEnableClientEvent(number));
   }

   public void dropItem(MonsterInstance npc, Player player, int itemId, int count) {
      npc.dropItem(player, itemId, (long)count);
   }

   public void setRestartTime() {
      Calendar reDo = Calendar.getInstance();
      if (reDo.get(11) >= this.getQuest().getResetHour()) {
         reDo.add(5, 1);
      }

      reDo.set(11, this.getQuest().getResetHour());
      reDo.set(12, this.getQuest().getResetMinutes());
      this.set("restartTime", String.valueOf(reDo.getTimeInMillis()));
   }

   public boolean isNowAvailable() {
      String val = this.get("restartTime");
      return val == null || !Util.isDigit(val) || Long.parseLong(val) <= System.currentTimeMillis();
   }

   public int getRandom(int max) {
      return Rnd.get(max);
   }

   public double getRateQuestsDrop() {
      return (double)Config.RATE_QUEST_DROP
         * (
            this._player.isInParty() && Config.PREMIUM_PARTY_RATE
               ? this._player.getParty().getQuestDropRate()
               : this._player.getPremiumBonus().getQuestDropRate()
         );
   }

   public void calcExpAndSp(int questId) {
      Quest.calcExpAndSp(this._player, questId);
   }

   public void calcReward(int questId) {
      Quest.calcReward(this._player, questId);
   }

   public void calcReward(int questId, int variant) {
      Quest.calcReward(this._player, questId, variant);
   }

   public void calcRewardPerItem(int questId, int variant, int totalAmount) {
      Quest.calcRewardPerItem(this._player, questId, variant, totalAmount);
   }

   public void calcRewardPerItem(int questId, int variant, int totalAmount, boolean isRandom) {
      Quest.calcRewardPerItem(this._player, questId, variant, totalAmount, isRandom);
   }

   public void calcReward(int questId, int variant, boolean isRandom) {
      Quest.calcReward(this._player, questId, variant, isRandom);
   }

   public boolean calcDropItems(int questId, int itemId, int npcId, int limit) {
      if (limit > 0 && itemId > 0 && npcId > 0) {
         long count = 0L;
         QuestTemplate template = QuestsParser.getInstance().getTemplate(questId);
         if (template != null) {
            List<QuestDropItem> dropList = template.getDropList().get(npcId);
            if (dropList != null && !dropList.isEmpty()) {
               for(QuestDropItem drop : dropList) {
                  if (drop != null && drop.getId() == itemId) {
                     if (Rnd.chance(drop.getChance())) {
                        count = drop.getMaxCount() != 0L ? Rnd.get(drop.getMinCount(), drop.getMaxCount()) : drop.getMinCount();
                        count = (long)(
                           (double)count
                              * (
                                 !drop.isRateable()
                                    ? drop.getRate()
                                    : (double)Config.RATE_QUEST_DROP
                                       * (
                                          this._player.isInParty() && Config.PREMIUM_PARTY_RATE
                                             ? this._player.getParty().getQuestDropRate()
                                             : this._player.getPremiumBonus().getQuestDropRate()
                                       )
                              )
                        );
                     }
                     break;
                  }
               }
            }
         }

         if (count > 0L) {
            long alreadyCount = this.getQuestItemsCount(itemId);
            if (alreadyCount + count >= (long)limit) {
               count = (long)limit - alreadyCount;
            }

            if (count > 0L) {
               this.giveItems(itemId, count);
               if (count + alreadyCount >= (long)limit) {
                  this.playSound(Quest.QuestSound.ITEMSOUND_QUEST_MIDDLE);
                  return true;
               }

               this.playSound(Quest.QuestSound.ITEMSOUND_QUEST_ITEMGET);
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public void calcDoDropItems(int questId, int itemId, int npcId, int limit) {
      if (limit > 0 && itemId > 0 && npcId > 0) {
         long count = 0L;
         QuestTemplate template = QuestsParser.getInstance().getTemplate(questId);
         if (template != null) {
            List<QuestDropItem> dropList = template.getDropList().get(npcId);
            if (dropList != null && !dropList.isEmpty()) {
               for(QuestDropItem drop : dropList) {
                  if (drop != null && drop.getId() == itemId) {
                     if (Rnd.chance(drop.getChance())) {
                        count = drop.getMaxCount() != 0L ? Rnd.get(drop.getMinCount(), drop.getMaxCount()) : drop.getMinCount();
                        count = (long)(
                           (double)count
                              * (
                                 !drop.isRateable()
                                    ? drop.getRate()
                                    : (double)Config.RATE_QUEST_DROP
                                       * (
                                          this._player.isInParty() && Config.PREMIUM_PARTY_RATE
                                             ? this._player.getParty().getQuestDropRate()
                                             : this._player.getPremiumBonus().getQuestDropRate()
                                       )
                              )
                        );
                     }
                     break;
                  }
               }
            }
         }

         if (count > 0L) {
            long alreadyCount = this.getQuestItemsCount(itemId);
            if (alreadyCount + count >= (long)limit) {
               count = (long)limit - alreadyCount;
            }

            if (count > 0L) {
               this.giveItems(itemId, count);
               if (count + alreadyCount <= (long)limit) {
                  this.playSound(Quest.QuestSound.ITEMSOUND_QUEST_ITEMGET);
               }
            }
         }
      }
   }

   public StatsSet getQuestParams(int questId) {
      return QuestsParser.getInstance().getTemplate(questId).getParams();
   }

   public static enum QuestType {
      REPEATABLE,
      ONE_TIME,
      DAILY;
   }
}
