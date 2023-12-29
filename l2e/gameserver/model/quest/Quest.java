package l2e.gameserver.model.quest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.commons.util.Rnd;
import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.GameTimeController;
import l2e.gameserver.data.htm.HtmCache;
import l2e.gameserver.data.parser.ItemsParser;
import l2e.gameserver.data.parser.NpcsParser;
import l2e.gameserver.data.parser.QuestsParser;
import l2e.gameserver.data.parser.SpawnParser;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.geodata.GeoEngine;
import l2e.gameserver.idfactory.IdFactory;
import l2e.gameserver.instancemanager.QuestManager;
import l2e.gameserver.instancemanager.ReflectionManager;
import l2e.gameserver.instancemanager.ZoneManager;
import l2e.gameserver.listener.ManagedLoader;
import l2e.gameserver.listener.ScriptManagerLoader;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.Party;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.MonsterInstance;
import l2e.gameserver.model.actor.instance.TrapInstance;
import l2e.gameserver.model.actor.templates.items.Item;
import l2e.gameserver.model.actor.templates.npc.MinionData;
import l2e.gameserver.model.actor.templates.npc.MinionTemplate;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.actor.templates.quest.QuestExperience;
import l2e.gameserver.model.actor.templates.quest.QuestRewardItem;
import l2e.gameserver.model.actor.templates.quest.QuestTemplate;
import l2e.gameserver.model.base.AcquireSkillType;
import l2e.gameserver.model.entity.Reflection;
import l2e.gameserver.model.holders.ItemHolder;
import l2e.gameserver.model.interfaces.IIdentifiable;
import l2e.gameserver.model.interfaces.IL2Procedure;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.items.itemcontainer.PcInventory;
import l2e.gameserver.model.olympiad.CompetitionType;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.spawn.SpawnTemplate;
import l2e.gameserver.model.spawn.SpawnTerritory;
import l2e.gameserver.model.spawn.Spawner;
import l2e.gameserver.model.stats.Stats;
import l2e.gameserver.model.stats.StatsSet;
import l2e.gameserver.model.strings.server.ServerMessage;
import l2e.gameserver.model.zone.ZoneType;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ExNpcQuestHtmlMessage;
import l2e.gameserver.network.serverpackets.ExShowScreenMessage;
import l2e.gameserver.network.serverpackets.InventoryUpdate;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.gameserver.network.serverpackets.PlaySound;
import l2e.gameserver.network.serverpackets.SpecialCamera;
import l2e.gameserver.network.serverpackets.StatusUpdate;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class Quest extends ManagedLoader implements IIdentifiable {
   public static final Logger _log = Logger.getLogger(Quest.class.getName());
   private static Map<String, Quest> _allScripts = new ConcurrentHashMap<>();
   private volatile Map<String, List<QuestTimer>> _questTimers = null;
   private final Set<Integer> _questInvolvedNpcs = new HashSet<>();
   private final ReentrantReadWriteLock _rwLock = new ReentrantReadWriteLock();
   private final WriteLock _writeLock = this._rwLock.writeLock();
   private final ReadLock _readLock = this._rwLock.readLock();
   private final int _questId;
   private final String _name;
   private final String _descr;
   private final byte _initialState = 0;
   protected boolean _onEnterWorld = false;
   private boolean _isCustom = false;
   private boolean _isOlympiadUse = false;
   public int[] questItemIds = null;
   private static final String QUEST_DELETE_FROM_CHAR_QUERY = "DELETE FROM character_quests WHERE charId=? AND name=?";
   private static final String QUEST_DELETE_FROM_CHAR_QUERY_NON_REPEATABLE_QUERY = "DELETE FROM character_quests WHERE charId=? AND name=? AND var!=?";
   private static final int RESET_HOUR = 6;
   private static final int RESET_MINUTES = 30;

   public int getResetHour() {
      return 6;
   }

   public int getResetMinutes() {
      return 30;
   }

   public static Collection<Quest> findAllEvents() {
      return _allScripts.values();
   }

   public Quest(int questId, String name, String descr) {
      this._questId = questId;
      this._name = name;
      this._descr = descr;
      if (questId != 0) {
         QuestManager.getInstance().addQuest(this);
      } else {
         _allScripts.put(name, this);
      }
   }

   public void saveGlobalData() {
   }

   @Override
   public int getId() {
      return this._questId;
   }

   public QuestState newQuestState(Player player) {
      return new QuestState(this, player, this.getInitialState());
   }

   public QuestState getQuestState(Player player, boolean initIfNone) {
      QuestState qs = player.getQuestState(this._name);
      return qs == null && initIfNone ? this.newQuestState(player) : qs;
   }

   public byte getInitialState() {
      return 0;
   }

   public String getName() {
      return this._name;
   }

   public String getDescr(Player player) {
      if (this._descr.equals("")) {
         QuestTemplate template = QuestsParser.getInstance().getTemplate(this._questId);
         return template != null ? template.getName(player.getLang()) : new ServerMessage("quest." + this._questId, player.getLang()).toString();
      } else {
         return this._descr;
      }
   }

   public void startQuestTimer(String name, long time, Npc npc, Player player) {
      this.startQuestTimer(name, time, npc, player, false);
   }

   public void startQuestTimer(String name, long time, Npc npc, Player player, boolean repeating) {
      List<QuestTimer> timers = this.getQuestTimers().computeIfAbsent(name, k -> new ArrayList(1));
      if (this.getQuestTimer(name, npc, player) == null) {
         this._writeLock.lock();

         try {
            timers.add(new QuestTimer(this, name, time, npc, player, repeating));
         } finally {
            this._writeLock.unlock();
         }
      }
   }

   public QuestTimer getQuestTimer(String name, Npc npc, Player player) {
      if (this._questTimers == null) {
         return null;
      } else {
         List<QuestTimer> timers = this.getQuestTimers().get(name);
         if (timers != null) {
            this._readLock.lock();

            QuestTimer var7;
            try {
               Iterator var5 = timers.iterator();

               QuestTimer timer;
               do {
                  if (!var5.hasNext()) {
                     return null;
                  }

                  timer = (QuestTimer)var5.next();
               } while(timer == null || !timer.isMatch(this, name, npc, player));

               var7 = timer;
            } finally {
               this._readLock.unlock();
            }

            return var7;
         } else {
            return null;
         }
      }
   }

   public void cancelQuestTimers(String name) {
      if (this._questTimers != null) {
         List<QuestTimer> timers = this.getQuestTimers().get(name);
         if (timers != null) {
            this._writeLock.lock();

            try {
               for(QuestTimer timer : timers) {
                  if (timer != null) {
                     timer.cancel();
                  }
               }

               timers.clear();
            } finally {
               this._writeLock.unlock();
            }
         }
      }
   }

   public void cancelQuestTimers(Npc npc) {
      if (this._questTimers != null) {
         for(List<QuestTimer> timers : this.getQuestTimers().values()) {
            if (timers != null) {
               this._writeLock.lock();

               try {
                  for(QuestTimer timer : timers) {
                     if (timer != null && timer.getNpc() == npc) {
                        timer.cancel();
                     }
                  }

                  timers.clear();
               } finally {
                  this._writeLock.unlock();
               }
            }
         }
      }
   }

   public void cancelQuestTimers(String name, int instanceId) {
      if (this._questTimers != null) {
         List<QuestTimer> timers = this.getQuestTimers().get(name);
         if (timers != null) {
            this._writeLock.lock();

            try {
               for(QuestTimer timer : timers) {
                  if (timer != null && timer.getReflectionId() == instanceId) {
                     timer.cancel();
                  }
               }
            } finally {
               this._writeLock.unlock();
            }
         }
      }
   }

   public void cancelQuestTimer(String name, Npc npc, Player player) {
      QuestTimer timer = this.getQuestTimer(name, npc, player);
      if (timer != null) {
         timer.cancelAndRemove();
      }
   }

   public void removeQuestTimer(QuestTimer timer) {
      if (timer != null && this._questTimers != null) {
         List<QuestTimer> timers = this.getQuestTimers().get(timer.getName());
         if (timers != null) {
            this._writeLock.lock();

            try {
               timers.remove(timer);
            } finally {
               this._writeLock.unlock();
            }
         }
      }
   }

   public final Map<String, List<QuestTimer>> getQuestTimers() {
      if (this._questTimers == null) {
         synchronized(this) {
            if (this._questTimers == null) {
               this._questTimers = new ConcurrentHashMap<>(1);
            }
         }
      }

      return this._questTimers;
   }

   public final void notifyAttack(Npc npc, Player attacker, int damage, boolean isSummon, Skill skill) {
      String res = null;

      try {
         res = this.onAttack(npc, attacker, damage, isSummon, skill);
      } catch (Exception var8) {
         this.showError(attacker, var8);
         return;
      }

      this.showResult(attacker, res);
   }

   public final void notifyDeath(Creature killer, Creature victim, QuestState qs) {
      String res = null;

      try {
         res = this.onDeath(killer, victim, qs);
      } catch (Exception var6) {
         this.showError(qs.getPlayer(), var6);
      }

      this.showResult(qs.getPlayer(), res);
   }

   public final void notifyItemUse(Item item, Player player) {
      String res = null;

      try {
         res = this.onItemUse(item, player);
      } catch (Exception var5) {
         this.showError(player, var5);
      }

      this.showResult(player, res);
   }

   public final void notifySpellFinished(Npc instance, Player player, Skill skill) {
      String res = null;

      try {
         res = this.onSpellFinished(instance, player, skill);
      } catch (Exception var6) {
         this.showError(player, var6);
      }

      this.showResult(player, res);
   }

   public final void notifyTrapAction(TrapInstance trap, Creature trigger, Quest.TrapAction action) {
      String res = null;

      try {
         res = this.onTrapAction(trap, trigger, action);
      } catch (Exception var6) {
         if (trigger.getActingPlayer() != null) {
            this.showError(trigger.getActingPlayer(), var6);
         }

         _log.log(Level.WARNING, "Exception on onTrapAction() in notifyTrapAction(): " + var6.getMessage(), (Throwable)var6);
         return;
      }

      if (trigger.getActingPlayer() != null) {
         this.showResult(trigger.getActingPlayer(), res);
      }
   }

   public final void notifySpawn(Npc npc) {
      try {
         this.onSpawn(npc);
      } catch (Exception var3) {
         _log.log(Level.WARNING, "Exception on onSpawn() in notifySpawn(): " + var3.getMessage(), (Throwable)var3);
      }
   }

   public final boolean notifyEvent(String event, Npc npc, Player player) {
      String res = null;

      try {
         res = this.onAdvEvent(event, npc, player);
      } catch (Exception var6) {
         return this.showError(player, var6);
      }

      return this.showResult(player, res);
   }

   public final void notifyEventReceived(String eventName, Npc sender, Npc receiver, GameObject reference) {
      try {
         this.onEventReceived(eventName, sender, receiver, reference);
      } catch (Exception var6) {
         _log.log(Level.WARNING, "Exception on onEventReceived() in notifyEventReceived(): " + var6.getMessage(), (Throwable)var6);
      }
   }

   public final void notifyEnterWorld(Player player) {
      String res = null;

      try {
         res = this.onEnterWorld(player);
      } catch (Exception var4) {
         this.showError(player, var4);
      }

      this.showResult(player, res);
   }

   public final void notifyKill(Npc npc, Player killer, boolean isSummon) {
      String res = null;

      try {
         res = this.onKill(npc, killer, isSummon);
      } catch (Exception var6) {
         _log.warning("Error with notifyKill at npcId: " + npc.getId());
         this.showError(killer, var6);
      }

      this.showResult(killer, res);
   }

   public final boolean notifyKillByMob(Npc npc, Npc killer) {
      try {
         this.onKillByMob(npc, killer);
         return true;
      } catch (Exception var4) {
         System.out.println(var4);
         return false;
      }
   }

   public final boolean notifyTalk(Npc npc, QuestState qs) {
      String res = null;

      try {
         res = this.onTalk(npc, qs.getPlayer());
      } catch (Exception var5) {
         return this.showError(qs.getPlayer(), var5);
      }

      qs.getPlayer().setLastQuestNpcObject(npc.getObjectId());
      return this.showResult(qs.getPlayer(), res);
   }

   public final void notifyFirstTalk(Npc npc, Player player) {
      String res = null;

      try {
         res = this.onFirstTalk(npc, player);
      } catch (Exception var5) {
         this.showError(player, var5);
      }

      this.showResult(player, res);
   }

   public final void notifyAcquireSkillList(Npc npc, Player player) {
      String res = null;

      try {
         res = this.onAcquireSkillList(npc, player);
      } catch (Exception var5) {
         this.showError(player, var5);
      }

      this.showResult(player, res);
   }

   public final void notifyAcquireSkillInfo(Npc npc, Player player, Skill skill) {
      String res = null;

      try {
         res = this.onAcquireSkillInfo(npc, player, skill);
      } catch (Exception var6) {
         this.showError(player, var6);
      }

      this.showResult(player, res);
   }

   public final void notifyAcquireSkill(Npc npc, Player player, Skill skill, AcquireSkillType type) {
      String res = null;

      try {
         res = this.onAcquireSkill(npc, player, skill, type);
      } catch (Exception var7) {
         this.showError(player, var7);
      }

      this.showResult(player, res);
   }

   public final boolean notifyItemTalk(ItemInstance item, Player player) {
      String res = null;

      try {
         res = this.onItemTalk(item, player);
         if (res != null) {
            if (res.equalsIgnoreCase("true")) {
               return true;
            }

            if (res.equalsIgnoreCase("false")) {
               return false;
            }
         }
      } catch (Exception var5) {
         return this.showError(player, var5);
      }

      return this.showResult(player, res);
   }

   public String onItemTalk(ItemInstance item, Player player) {
      return null;
   }

   public final boolean notifyItemEvent(ItemInstance item, Player player, String event) {
      String res = null;

      try {
         res = this.onItemEvent(item, player, event);
         if (res != null) {
            if (res.equalsIgnoreCase("true")) {
               return true;
            }

            if (res.equalsIgnoreCase("false")) {
               return false;
            }
         }
      } catch (Exception var6) {
         return this.showError(player, var6);
      }

      return this.showResult(player, res);
   }

   public final void notifySkillSee(Npc npc, Player caster, Skill skill, GameObject[] targets, boolean isSummon) {
      String res = null;

      try {
         res = this.onSkillSee(npc, caster, skill, targets, isSummon);
      } catch (Exception var8) {
         this.showError(caster, var8);
         return;
      }

      this.showResult(caster, res);
   }

   public final void notifyFactionCall(Npc npc, Npc caller, Player attacker, boolean isSummon) {
      String res = null;

      try {
         res = this.onFactionCall(npc, caller, attacker, isSummon);
      } catch (Exception var7) {
         this.showError(attacker, var7);
      }

      this.showResult(attacker, res);
   }

   public final void notifyAggroRangeEnter(Npc npc, Player player, boolean isSummon) {
      String res = null;

      try {
         res = this.onAggroRangeEnter(npc, player, isSummon);
      } catch (Exception var6) {
         this.showError(player, var6);
         return;
      }

      this.showResult(player, res);
   }

   public final void notifySeeCreature(Npc npc, Creature creature, boolean isSummon) {
      Player player = null;
      if (isSummon || creature.isPlayer()) {
         player = creature.getActingPlayer();
      }

      String res = null;

      try {
         res = this.onSeeCreature(npc, creature, isSummon);
      } catch (Exception var7) {
         if (player != null) {
            this.showError(player, var7);
         }

         return;
      }

      if (player != null) {
         this.showResult(player, res);
      }
   }

   public final void notifyEnterZone(Creature character, ZoneType zone) {
      Player player = character.getActingPlayer();
      String res = null;

      try {
         res = this.onEnterZone(character, zone);
      } catch (Exception var6) {
         if (player != null) {
            this.showError(player, var6);
         }
      }

      if (player != null) {
         this.showResult(player, res);
      }
   }

   public final void notifyExitZone(Creature character, ZoneType zone) {
      Player player = character.getActingPlayer();
      String res = null;

      try {
         res = this.onExitZone(character, zone);
      } catch (Exception var6) {
         if (player != null) {
            this.showError(player, var6);
         }
      }

      if (player != null) {
         this.showResult(player, res);
      }
   }

   public final void notifyOlympiadWin(Player winner, CompetitionType type) {
      try {
         this.onOlympiadWin(winner, type);
      } catch (Exception var4) {
         this.showError(winner, var4);
      }
   }

   public final void notifyOlympiadLose(Player loser, CompetitionType type) {
      try {
         this.onOlympiadLose(loser, type);
      } catch (Exception var4) {
         this.showError(loser, var4);
      }
   }

   public boolean notifyMoveFinished(Npc npc) {
      try {
         return this.onMoveFinished(npc);
      } catch (Exception var3) {
         _log.log(Level.WARNING, "Exception on onMoveFinished() in notifyMoveFinished(): " + var3.getMessage(), (Throwable)var3);
         return false;
      }
   }

   public final void notifyRouteFinished(Npc npc) {
      try {
         this.onRouteFinished(npc);
      } catch (Exception var3) {
         _log.log(Level.WARNING, "Exception on onRouteFinished() in notifyRouteFinished(): " + var3.getMessage(), (Throwable)var3);
      }
   }

   public final boolean notifyOnCanSeeMe(Npc npc, Player player) {
      try {
         return this.onCanSeeMe(npc, player);
      } catch (Exception var4) {
         _log.log(Level.WARNING, "Exception on onCanSeeMe() in notifyOnCanSeeMe(): " + var4.getMessage(), (Throwable)var4);
         return false;
      }
   }

   public String onAttack(Npc npc, Player attacker, int damage, boolean isSummon) {
      return null;
   }

   public String onAttack(Npc npc, Player attacker, int damage, boolean isSummon, Skill skill) {
      return this.onAttack(npc, attacker, damage, isSummon);
   }

   public String onDeath(Creature killer, Creature victim, QuestState qs) {
      return this.onAdvEvent("", killer instanceof Npc ? (Npc)killer : null, qs.getPlayer());
   }

   public String onAdvEvent(String event, Npc npc, Player player) {
      if (player != null) {
         QuestState qs = player.getQuestState(this.getName());
         if (qs != null) {
            return this.onEvent(event, qs);
         }
      }

      return null;
   }

   public String onEvent(String event, QuestState qs) {
      return null;
   }

   public String onKill(Npc npc, QuestState qs) {
      return null;
   }

   public String onKill(Npc npc, Player killer, boolean isSummon) {
      return null;
   }

   public String onKillByMob(Npc npc, Npc killer) {
      return null;
   }

   public String onTalk(Npc npc, Player talker) {
      return null;
   }

   public String onFirstTalk(Npc npc, Player player) {
      return null;
   }

   public String onItemEvent(ItemInstance item, Player player, String event) {
      return null;
   }

   public String onAcquireSkillList(Npc npc, Player player) {
      return null;
   }

   public String onAcquireSkillInfo(Npc npc, Player player, Skill skill) {
      return null;
   }

   public String onAcquireSkill(Npc npc, Player player, Skill skill, AcquireSkillType type) {
      return null;
   }

   public String onItemUse(Item item, Player player) {
      return null;
   }

   public String onSkillSee(Npc npc, Player caster, Skill skill, GameObject[] targets, boolean isSummon) {
      return null;
   }

   public String onSpellFinished(Npc npc, Player player, Skill skill) {
      return null;
   }

   public String onTrapAction(TrapInstance trap, Creature trigger, Quest.TrapAction action) {
      return null;
   }

   public String onSpawn(Npc npc) {
      return null;
   }

   public String onFactionCall(Npc npc, Npc caller, Player attacker, boolean isSummon) {
      return null;
   }

   public String onAggroRangeEnter(Npc npc, Player player, boolean isSummon) {
      return null;
   }

   public String onSeeCreature(Npc npc, Creature creature, boolean isSummon) {
      return null;
   }

   public String onEnterWorld(Player player) {
      return null;
   }

   public String onEnterZone(Creature character, ZoneType zone) {
      return null;
   }

   public String onExitZone(Creature character, ZoneType zone) {
      return null;
   }

   public String onEventReceived(String eventName, Npc sender, Npc receiver, GameObject reference) {
      return null;
   }

   public void onOlympiadWin(Player winner, CompetitionType type) {
   }

   public void onOlympiadLose(Player loser, CompetitionType type) {
   }

   public boolean onCanSeeMe(Npc npc, Player player) {
      return false;
   }

   public boolean showError(Player player, Throwable t) {
      _log.log(Level.WARNING, this.getScriptFile().getAbsolutePath(), t);
      if (t.getMessage() == null) {
         _log.warning(this.getClass().getSimpleName() + ": " + t.getMessage());
      }

      if (player != null && player.getAccessLevel().isGm()) {
         String res = "<html><body><title>Script error</title>" + Util.getStackTrace(t) + "</body></html>";
         return this.showResult(player, res);
      } else {
         return false;
      }
   }

   public boolean showResult(Player player, String res) {
      if (res != null && !res.isEmpty() && player != null) {
         if (res.endsWith(".htm") || res.endsWith(".html")) {
            this.showHtmlFile(player, res);
         } else if (res.startsWith("<html>")) {
            NpcHtmlMessage npcReply = new NpcHtmlMessage(player, 0, res);
            npcReply.replace("%playername%", player.getName());
            player.sendPacket(npcReply);
            player.sendActionFailed();
         } else {
            player.sendMessage(res);
         }

         return false;
      } else {
         return true;
      }
   }

   public static final void playerEnter(Player player) {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement invalidQuestData = con.prepareStatement("DELETE FROM character_quests WHERE charId = ? AND name = ?");
         PreparedStatement invalidQuestDataVar = con.prepareStatement("DELETE FROM character_quests WHERE charId = ? AND name = ? AND var = ?");
         PreparedStatement ps1 = con.prepareStatement("SELECT name, value FROM character_quests WHERE charId = ? AND var = ?");
      ) {
         ps1.setInt(1, player.getObjectId());
         ps1.setString(2, "<state>");

         try (ResultSet rs = ps1.executeQuery()) {
            while(rs.next()) {
               String questId = rs.getString("name");
               String statename = rs.getString("value");
               Quest q = QuestManager.getInstance().getQuest(questId);
               if (q == null) {
                  _log.finer("Unknown quest " + questId + " for player " + player.getName());
                  if (Config.AUTODELETE_INVALID_QUEST_DATA) {
                     invalidQuestData.setInt(1, player.getObjectId());
                     invalidQuestData.setString(2, questId);
                     invalidQuestData.executeUpdate();
                  }
               } else {
                  new QuestState(q, player, State.getStateId(statename));
               }
            }
         }

         try (PreparedStatement ps2 = con.prepareStatement("SELECT name, var, value FROM character_quests WHERE charId = ? AND var <> ?")) {
            ps2.setInt(1, player.getObjectId());
            ps2.setString(2, "<state>");

            try (ResultSet rs = ps2.executeQuery()) {
               while(rs.next()) {
                  String questId = rs.getString("name");
                  String var = rs.getString("var");
                  String value = rs.getString("value");
                  QuestState qs = player.getQuestState(questId);
                  if (qs == null) {
                     _log.finer("Lost variable " + var + " in quest " + questId + " for player " + player.getName());
                     if (Config.AUTODELETE_INVALID_QUEST_DATA) {
                        invalidQuestDataVar.setInt(1, player.getObjectId());
                        invalidQuestDataVar.setString(2, questId);
                        invalidQuestDataVar.setString(3, var);
                        invalidQuestDataVar.executeUpdate();
                     }
                  } else {
                     qs.setInternal(var, value);
                  }
               }
            }
         }
      } catch (Exception var232) {
         _log.log(Level.WARNING, "could not insert char quest:", (Throwable)var232);
      }

      for(String name : _allScripts.keySet()) {
         player.processQuestEvent(name, "enter");
      }
   }

   public final void saveGlobalQuestVar(String var, String value) {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("REPLACE INTO quest_global_data (quest_name,var,value) VALUES (?,?,?)");
      ) {
         statement.setString(1, this.getName());
         statement.setString(2, var);
         statement.setString(3, value);
         statement.executeUpdate();
      } catch (Exception var35) {
         _log.log(Level.WARNING, "could not insert global quest variable:", (Throwable)var35);
      }
   }

   public final String loadGlobalQuestVar(String var) {
      String result = "";

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("SELECT value FROM quest_global_data WHERE quest_name = ? AND var = ?");
      ) {
         statement.setString(1, this.getName());
         statement.setString(2, var);

         try (ResultSet rs = statement.executeQuery()) {
            if (rs.first()) {
               result = rs.getString(1);
            }
         }
      } catch (Exception var61) {
         _log.log(Level.WARNING, "could not load global quest variable:", (Throwable)var61);
      }

      return result;
   }

   public final void deleteGlobalQuestVar(String var) {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("DELETE FROM quest_global_data WHERE quest_name = ? AND var = ?");
      ) {
         statement.setString(1, this.getName());
         statement.setString(2, var);
         statement.executeUpdate();
      } catch (Exception var34) {
         _log.log(Level.WARNING, "could not delete global quest variable:", (Throwable)var34);
      }
   }

   public final void deleteAllGlobalQuestVars() {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("DELETE FROM quest_global_data WHERE quest_name = ?");
      ) {
         statement.setString(1, this.getName());
         statement.executeUpdate();
      } catch (Exception var33) {
         _log.log(Level.WARNING, "could not delete global quest variables:", (Throwable)var33);
      }
   }

   public static void createQuestVarInDb(QuestState qs, String var, String value) {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement(
            "INSERT INTO character_quests (charId,name,var,value) VALUES (?,?,?,?) ON DUPLICATE KEY UPDATE value=?"
         );
      ) {
         statement.setInt(1, qs.getPlayer().getObjectId());
         statement.setString(2, qs.getQuestName());
         statement.setString(3, var);
         statement.setString(4, value);
         statement.setString(5, value);
         statement.executeUpdate();
      } catch (Exception var35) {
         _log.log(Level.WARNING, "could not insert char quest:", (Throwable)var35);
      }
   }

   public static void updateQuestVarInDb(QuestState qs, String var, String value) {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("UPDATE character_quests SET value=? WHERE charId=? AND name=? AND var = ?");
      ) {
         statement.setString(1, value);
         statement.setInt(2, qs.getPlayer().getObjectId());
         statement.setString(3, qs.getQuestName());
         statement.setString(4, var);
         statement.executeUpdate();
      } catch (Exception var35) {
         _log.log(Level.WARNING, "could not update char quest:", (Throwable)var35);
      }
   }

   public static void deleteQuestVarInDb(QuestState qs, String var) {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("DELETE FROM character_quests WHERE charId=? AND name=? AND var=?");
      ) {
         statement.setInt(1, qs.getPlayer().getObjectId());
         statement.setString(2, qs.getQuestName());
         statement.setString(3, var);
         statement.executeUpdate();
      } catch (Exception var34) {
         _log.log(Level.WARNING, "could not delete char quest:", (Throwable)var34);
      }
   }

   public static void deleteQuestInDb(QuestState qs, boolean repeatable) {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement ps = con.prepareStatement(
            repeatable ? "DELETE FROM character_quests WHERE charId=? AND name=?" : "DELETE FROM character_quests WHERE charId=? AND name=? AND var!=?"
         );
      ) {
         ps.setInt(1, qs.getPlayer().getObjectId());
         ps.setString(2, qs.getQuestName());
         if (!repeatable) {
            ps.setString(3, "<state>");
         }

         ps.executeUpdate();
      } catch (Exception var34) {
         _log.log(Level.WARNING, "could not delete char quest:", (Throwable)var34);
      }
   }

   public static void createQuestInDb(QuestState qs) {
      createQuestVarInDb(qs, "<state>", State.getStateName(qs.getState()));
   }

   public static void updateQuestInDb(QuestState qs) {
      updateQuestVarInDb(qs, "<state>", State.getStateName(qs.getState()));
   }

   public static String getNoQuestMsg(Player player) {
      return HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/noquest.htm");
   }

   public static String getAlreadyCompletedMsg(Player player) {
      return HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/alreadycompleted.htm");
   }

   public void addEventId(Quest.QuestEventType eventType, int... npcIds) {
      try {
         for(int npcId : npcIds) {
            NpcTemplate t = NpcsParser.getInstance().getTemplate(npcId);
            if (t != null) {
               t.addQuestEvent(eventType, this);
               this._questInvolvedNpcs.add(npcId);
            }
         }
      } catch (Exception var8) {
         _log.log(Level.WARNING, "Exception on addEventId(): " + var8.getMessage(), (Throwable)var8);
      }
   }

   public void addStartNpc(int npcId) {
      this.addEventId(Quest.QuestEventType.QUEST_START, npcId);
   }

   public void addFirstTalkId(int npcId) {
      this.addEventId(Quest.QuestEventType.ON_FIRST_TALK, npcId);
   }

   public void addTalkId(int npcId) {
      this.addEventId(Quest.QuestEventType.ON_TALK, npcId);
   }

   public void addKillId(int killId) {
      this.addEventId(Quest.QuestEventType.ON_KILL, killId);
   }

   public void addAttackId(int npcId) {
      this.addEventId(Quest.QuestEventType.ON_ATTACK, npcId);
   }

   public void addStartNpc(int... npcIds) {
      this.addEventId(Quest.QuestEventType.QUEST_START, npcIds);
   }

   public void addFirstTalkId(int... npcIds) {
      this.addEventId(Quest.QuestEventType.ON_FIRST_TALK, npcIds);
   }

   public void addAcquireSkillId(int... npcIds) {
      this.addEventId(Quest.QuestEventType.ON_SKILL_LEARN, npcIds);
   }

   public void addAttackId(int... npcIds) {
      this.addEventId(Quest.QuestEventType.ON_ATTACK, npcIds);
   }

   public void addKillId(int... killIds) {
      this.addEventId(Quest.QuestEventType.ON_KILL, killIds);
   }

   public void addKillId(Collection<Integer> killIds) {
      for(int killId : killIds) {
         this.addEventId(Quest.QuestEventType.ON_KILL, killId);
      }
   }

   public void addTalkId(int... npcIds) {
      this.addEventId(Quest.QuestEventType.ON_TALK, npcIds);
   }

   public void addSpawnId(int... npcIds) {
      this.addEventId(Quest.QuestEventType.ON_SPAWN, npcIds);
   }

   public void addSkillSeeId(int... npcIds) {
      this.addEventId(Quest.QuestEventType.ON_SKILL_SEE, npcIds);
   }

   public void addSpellFinishedId(int... npcIds) {
      this.addEventId(Quest.QuestEventType.ON_SPELL_FINISHED, npcIds);
   }

   public void addTrapActionId(int... npcIds) {
      this.addEventId(Quest.QuestEventType.ON_TRAP_ACTION, npcIds);
   }

   public void addFactionCallId(int... npcIds) {
      this.addEventId(Quest.QuestEventType.ON_FACTION_CALL, npcIds);
   }

   public void addAggroRangeEnterId(int... npcIds) {
      this.addEventId(Quest.QuestEventType.ON_AGGRO_RANGE_ENTER, npcIds);
   }

   public void addSeeCreatureId(int... npcIds) {
      this.addEventId(Quest.QuestEventType.ON_SEE_CREATURE, npcIds);
   }

   public ZoneType[] addEnterZoneId(int... zoneIds) {
      ZoneType[] value = new ZoneType[zoneIds.length];
      int i = 0;

      for(int zoneId : zoneIds) {
         try {
            ZoneType zone = ZoneManager.getInstance().getZoneById(zoneId);
            if (zone != null) {
               zone.addQuestEvent(Quest.QuestEventType.ON_ENTER_ZONE, this);
            }

            value[i++] = zone;
         } catch (Exception var9) {
            _log.log(Level.WARNING, "Exception on addEnterZoneId(): " + var9.getMessage(), (Throwable)var9);
         }
      }

      return value;
   }

   public ZoneType[] addExitZoneId(int... zoneIds) {
      ZoneType[] value = new ZoneType[zoneIds.length];
      int i = 0;

      for(int zoneId : zoneIds) {
         try {
            ZoneType zone = ZoneManager.getInstance().getZoneById(zoneId);
            if (zone != null) {
               zone.addQuestEvent(Quest.QuestEventType.ON_EXIT_ZONE, this);
            }

            value[i++] = zone;
         } catch (Exception var9) {
            _log.log(Level.WARNING, "Exception on addEnterZoneId(): " + var9.getMessage(), (Throwable)var9);
         }
      }

      return value;
   }

   public ZoneType addExitZoneId(int zoneId) {
      try {
         ZoneType zone = ZoneManager.getInstance().getZoneById(zoneId);
         if (zone != null) {
            zone.addQuestEvent(Quest.QuestEventType.ON_EXIT_ZONE, this);
         }

         return zone;
      } catch (Exception var3) {
         _log.log(Level.WARNING, "Exception on addExitZoneId(): " + var3.getMessage(), (Throwable)var3);
         return null;
      }
   }

   public void addEventReceivedId(int... npcIds) {
      this.addEventId(Quest.QuestEventType.ON_EVENT_RECEIVED, npcIds);
   }

   public void addMoveFinishedId(int... npcIds) {
      this.addEventId(Quest.QuestEventType.ON_MOVE_FINISHED, npcIds);
   }

   public void addRouteFinishedId(int... npcIds) {
      this.addEventId(Quest.QuestEventType.ON_ROUTE_FINISHED, npcIds);
   }

   public void addCanSeeMeId(int... npcIds) {
      this.addEventId(Quest.QuestEventType.ON_CAN_SEE_ME, npcIds);
   }

   public void addCanSeeMeId(Collection<Integer> npcIds) {
      for(int npcId : npcIds) {
         this.addEventId(Quest.QuestEventType.ON_CAN_SEE_ME, npcId);
      }
   }

   public Player getRandomPartyMember(Player player) {
      if (player == null) {
         return null;
      } else {
         Party party = player.getParty();
         return party != null && !party.getMembers().isEmpty() ? party.getMembers().get(Rnd.get(party.getMembers().size())) : player;
      }
   }

   public Player getRandomPartyMember(Player player, int cond) {
      return this.getRandomPartyMember(player, "cond", String.valueOf(cond));
   }

   public Player getRandomPartyMember(Player player, String var, String value) {
      if (player == null) {
         return null;
      } else if (var == null) {
         return this.getRandomPartyMember(player);
      } else {
         QuestState temp = null;
         Party party = player.getParty();
         if (party != null && !party.getMembers().isEmpty()) {
            List<Player> candidates = new ArrayList<>();
            GameObject target = player.getTarget();
            if (target == null) {
               target = player;
            }

            for(Player partyMember : party.getMembers()) {
               if (partyMember != null) {
                  temp = partyMember.getQuestState(this.getName());
                  if (temp != null && temp.get(var) != null && temp.get(var).equalsIgnoreCase(value) && partyMember.isInsideRadius(target, 1500, true, false)) {
                     candidates.add(partyMember);
                  }
               }
            }

            return candidates.isEmpty() ? null : candidates.get(Rnd.get(candidates.size()));
         } else {
            temp = player.getQuestState(this.getName());
            return temp != null && temp.isSet(var) && temp.get(var).equalsIgnoreCase(value) ? player : null;
         }
      }
   }

   public QuestState checkAllQuestCondition(Player player, Npc npc, String var) {
      if (player == null) {
         return null;
      } else {
         QuestState st = player.getQuestState(this.getName());
         if (st == null) {
            return null;
         } else if (st.get(var) == null) {
            return null;
         } else if (npc == null) {
            return null;
         } else {
            return !player.isInsideRadius(npc, Config.ALT_PARTY_RANGE, true, false) ? null : st;
         }
      }
   }

   public QuestState checkPlayerCondition(Player player, Npc npc, String var, String value) {
      if (player == null) {
         return null;
      } else {
         QuestState st = player.getQuestState(this.getName());
         if (st == null) {
            return null;
         } else if (st.get(var) == null || !value.equalsIgnoreCase(st.get(var))) {
            return null;
         } else if (npc == null) {
            return null;
         } else {
            return !player.isInsideRadius(npc, Config.ALT_PARTY_RANGE, true, false) ? null : st;
         }
      }
   }

   public List<Player> getPartyMembers(Player player, Npc npc, String var, String value) {
      ArrayList<Player> candidates = new ArrayList<>();
      if (player != null && player.isInParty()) {
         for(Player partyMember : player.getParty().getMembers()) {
            if (partyMember != null && this.checkPlayerCondition(partyMember, npc, var, value) != null) {
               candidates.add(partyMember);
            }
         }
      } else if (this.checkPlayerCondition(player, npc, var, value) != null) {
         candidates.add(player);
      }

      return candidates;
   }

   public List<Player> getMembersCond(Player player, Npc npc, String var) {
      ArrayList<Player> candidates = new ArrayList<>();
      if (player != null && player.isInParty()) {
         for(Player partyMember : player.getParty().getMembers()) {
            if (partyMember != null && this.checkAllQuestCondition(partyMember, npc, var) != null) {
               candidates.add(partyMember);
            }
         }
      } else if (this.checkAllQuestCondition(player, npc, var) != null) {
         candidates.add(player);
      }

      return candidates;
   }

   public Player getRandomPartyMemberState(Player player, byte state) {
      if (player == null) {
         return null;
      } else {
         QuestState temp = null;
         Party party = player.getParty();
         if (party != null && !party.getMembers().isEmpty()) {
            List<Player> candidates = new ArrayList<>();
            GameObject target = player.getTarget();
            if (target == null) {
               target = player;
            }

            for(Player partyMember : party.getMembers()) {
               if (partyMember != null) {
                  temp = partyMember.getQuestState(this.getName());
                  if (temp != null && temp.getState() == state && partyMember.isInsideRadius(target, 1500, true, false)) {
                     candidates.add(partyMember);
                  }
               }
            }

            return candidates.isEmpty() ? null : candidates.get(Rnd.get(candidates.size()));
         } else {
            temp = player.getQuestState(this.getName());
            return temp != null && temp.getState() == state ? player : null;
         }
      }
   }

   public QuestState getRandomPartyMemberState(Player player, int condition, int playerChance, Npc target) {
      if (player != null && playerChance >= 1) {
         QuestState st = player.getQuestState(this.getName());
         if (!player.isInParty()) {
            if (!this.checkPartyMemberConditions(st, condition, target)) {
               return null;
            } else {
               return !checkDistanceToTarget(player, target) ? null : st;
            }
         } else {
            List<QuestState> candidates = new ArrayList<>();
            if (this.checkPartyMemberConditions(st, condition, target) && playerChance > 0) {
               for(int i = 0; i < playerChance; ++i) {
                  candidates.add(st);
               }
            }

            for(Player member : player.getParty().getMembers()) {
               if (member != player) {
                  st = member.getQuestState(this.getName());
                  if (this.checkPartyMemberConditions(st, condition, target)) {
                     candidates.add(st);
                  }
               }
            }

            if (candidates.isEmpty()) {
               return null;
            } else {
               st = candidates.get(getRandom(candidates.size()));
               return !checkDistanceToTarget(st.getPlayer(), target) ? null : st;
            }
         }
      } else {
         return null;
      }
   }

   private boolean checkPartyMemberConditions(QuestState qs, int condition, Npc npc) {
      return qs != null && (condition == -1 ? qs.isStarted() : qs.isCond(condition)) && this.checkPartyMember(qs, npc);
   }

   private static boolean checkDistanceToTarget(Player player, Npc target) {
      return target == null || Util.checkIfInRange(1500, player, target, true);
   }

   public boolean checkPartyMember(QuestState qs, Npc npc) {
      return true;
   }

   public static void showOnScreenMsg(Player player, String text, int time) {
      player.sendPacket(new ExShowScreenMessage(text, time));
   }

   public static void showOnScreenMsg(Player player, NpcStringId npcString, int position, int time, String... params) {
      player.sendPacket(new ExShowScreenMessage(npcString, position, time, params));
   }

   public static void showOnScreenMsg(Player player, SystemMessageId systemMsg, int position, int time, String... params) {
      player.sendPacket(new ExShowScreenMessage(systemMsg, position, time, params));
   }

   public String showHtmlFile(Player player, String fileName) {
      String lang = player.getLang();
      String questName = this.getName();
      int questId = this.getId();
      String directory = this.getDescr(player).toLowerCase();
      String filepath = "data/scripts/" + directory + "/" + questName + "/" + lang + "/" + fileName;
      String content = HtmCache.getInstance().getHtm(player, filepath);
      if (content == null) {
         filepath = "data/scripts/" + directory + "/" + questName + "/en/" + fileName;
         content = HtmCache.getInstance().getHtm(player, filepath);
      }

      if (content == null) {
         filepath = "data/scripts/quests/" + questName + "/" + lang + "/" + fileName;
         content = HtmCache.getInstance().getHtm(player, filepath);
      }

      if (content == null) {
         filepath = "data/scripts/quests/" + questName + "/en/" + fileName;
         content = HtmCache.getInstance().getHtm(player, filepath);
      }

      if (content == null) {
         content = "<html><body>My text is missing:<br>" + filepath + "</body></html>";
         _log.info("Cache[HTML]: Missing HTML page: " + filepath);
      }

      if (player.getTarget() != null) {
         content = content.replaceAll("%objectId%", String.valueOf(player.getTarget().getObjectId()));
      }

      if (content != null) {
         if (questId > 0 && questId < 20000) {
            ExNpcQuestHtmlMessage npcReply = new ExNpcQuestHtmlMessage(5, questId);
            npcReply.setHtml(player, content);
            npcReply.replace("%playername%", player.getName());
            player.sendPacket(npcReply);
         } else {
            NpcHtmlMessage npcReply = new NpcHtmlMessage(5);
            npcReply.setHtml(player, content);
            npcReply.replace("%playername%", player.getName());
            player.sendPacket(npcReply);
         }

         player.sendActionFailed();
      }

      return content;
   }

   public String getHtm(Player player, String lang, String fileName) {
      HtmCache hc = HtmCache.getInstance();
      String content = hc.getHtm(
         player,
         lang,
         fileName.startsWith("data/")
            ? fileName
            : "data/scripts/" + this.getDescr(player).toLowerCase() + "/" + this.getName() + "/" + player.getLang() + "/" + fileName
      );
      if (content == null) {
         content = hc.getHtm(player, lang, "data/scripts/" + this.getDescr(player) + "/" + this.getName() + "/" + player.getLang() + "/" + fileName);
         if (content == null) {
            content = hc.getHtmForce(player, lang, "data/scripts/quests/" + this.getName() + "/" + player.getLang() + "/" + fileName);
         }
      }

      return content;
   }

   public static Npc addSpawn(int npcId, Location loc) {
      return addSpawn(npcId, loc.getX(), loc.getY(), loc.getZ(), loc.getHeading(), false, 0L, false, 0);
   }

   public static Npc addSpawn(int npcId, Location loc, int instanceId) {
      return addSpawn(npcId, loc.getX(), loc.getY(), loc.getZ(), loc.getHeading(), false, 0L, false, instanceId);
   }

   public static Npc addSpawn(int npcId, Location loc, int geoIndex, int instanceId, int randomOffset) {
      Location newLoc;
      if (randomOffset > 0) {
         newLoc = Location.findPointToStay(loc, 0, randomOffset, geoIndex, true).setH(loc.getHeading());
      } else {
         newLoc = loc;
      }

      return addSpawn(npcId, newLoc, instanceId);
   }

   public static Npc addSpawn(int npcId, Creature cha) {
      return addSpawn(npcId, cha.getX(), cha.getY(), cha.getZ(), cha.getHeading(), false, 0L, false, 0);
   }

   public static Npc addSpawn(int npcId, Creature cha, boolean isSummonSpawn) {
      return addSpawn(npcId, cha.getX(), cha.getY(), cha.getZ(), cha.getHeading(), false, 0L, isSummonSpawn, 0);
   }

   public static Npc addSpawn(int npcId, Location loc, boolean randomOffset, long despawnDelay, boolean isSummonSpawn, int instanceId) {
      return addSpawn(npcId, loc.getX(), loc.getY(), loc.getZ(), loc.getHeading(), randomOffset, despawnDelay, isSummonSpawn, instanceId);
   }

   public static Npc addSpawn(int npcId, int x, int y, int z, int heading, boolean randomOffSet, long despawnDelay) {
      return addSpawn(npcId, x, y, z, heading, randomOffSet, despawnDelay, false, 0);
   }

   public static Npc addSpawn(int npcId, Location loc, boolean randomOffSet, long despawnDelay) {
      return addSpawn(npcId, loc.getX(), loc.getY(), loc.getZ(), loc.getHeading(), randomOffSet, despawnDelay, false, 0);
   }

   public static Npc addSpawn(int npcId, int x, int y, int z, int heading, boolean randomOffset, long despawnDelay, boolean isSummonSpawn) {
      return addSpawn(npcId, x, y, z, heading, randomOffset, despawnDelay, isSummonSpawn, 0);
   }

   public static Npc addSpawn(int npcId, Location loc, boolean randomOffset, long despawnDelay, boolean isSummonSpawn) {
      return addSpawn(npcId, loc.getX(), loc.getY(), loc.getZ(), loc.getHeading(), randomOffset, despawnDelay, isSummonSpawn, 0);
   }

   public static Npc addSpawn(int npcId, int x, int y, int z, int heading, boolean randomOffset, long despawnDelay, boolean isSummonSpawn, int instanceId) {
      Npc result = null;

      try {
         NpcTemplate template = NpcsParser.getInstance().getTemplate(npcId);
         if (template != null) {
            if (x == 0 && y == 0) {
               _log.log(Level.SEVERE, "Failed to adjust bad locks for quest spawn!  Spawn aborted!");
               return null;
            }

            if (randomOffset) {
               int offset = Rnd.get(2);
               if (offset == 0) {
                  offset = -1;
               }

               offset *= Rnd.get(50, 100);
               x += offset;
               offset = Rnd.get(2);
               if (offset == 0) {
                  offset = -1;
               }

               offset *= Rnd.get(50, 100);
               y += offset;
            }

            Spawner spawn = new Spawner(template);
            spawn.setReflectionId(instanceId);
            spawn.setHeading(heading);
            spawn.setX(x);
            spawn.setY(y);
            if (!template.getType().startsWith("Fly") && !template.isType("Npc")) {
               spawn.setZ(GeoEngine.getHeight(x, y, z, spawn.getGeoIndex()));
            } else {
               spawn.setZ(z);
            }

            spawn.stopRespawn();
            result = spawn.spawnOne(isSummonSpawn);
            if (despawnDelay > 0L) {
               result.scheduleDespawn(despawnDelay);
            }

            return result;
         }
      } catch (Exception var13) {
         _log.warning("Could not spawn Npc " + npcId + " Error: " + var13.getMessage());
      }

      return null;
   }

   public static Npc addSpawn(int npcId, SpawnTerritory ter, long despawnDelay, boolean isSummonSpawn, int instanceId, int geoIndex) {
      Npc result = null;

      try {
         NpcTemplate template = NpcsParser.getInstance().getTemplate(npcId);
         if (template != null) {
            SpawnTemplate tpl = new SpawnTemplate("none", 1, 0, 0);
            tpl.addSpawnRange(ter);
            Spawner c = new Spawner(template);
            c.setAmount(1);
            c.setSpawnTemplate(tpl);
            c.setLocation(c.calcSpawnRangeLoc(geoIndex, template));
            c.setReflectionId(instanceId);
            c.stopRespawn();
            result = c.spawnOne(isSummonSpawn);
            if (despawnDelay > 0L) {
               result.scheduleDespawn(despawnDelay);
            }

            return result;
         }
      } catch (Exception var11) {
         _log.warning("Could not spawn Npc " + npcId + " Error: " + var11.getMessage());
      }

      return null;
   }

   public TrapInstance addTrap(int trapId, int x, int y, int z, int heading, Skill skill, int instanceId) {
      NpcTemplate npcTemplate = NpcsParser.getInstance().getTemplate(trapId);
      TrapInstance trap = new TrapInstance(IdFactory.getInstance().getNextId(), npcTemplate, instanceId, -1);
      trap.setCurrentHp(trap.getMaxHp());
      trap.setCurrentMp(trap.getMaxMp());
      trap.setIsInvul(true);
      trap.setHeading(heading);
      trap.spawnMe(x, y, z);
      return trap;
   }

   public void addMinion(MonsterInstance master, int minionId) {
      master.getMinionList().addMinion(new MinionData(new MinionTemplate(minionId, 1)), true);
   }

   public int[] getRegisteredItemIds() {
      return this.questItemIds;
   }

   public void registerQuestItems(int... items) {
      this.questItemIds = items;
   }

   @Override
   public String getScriptName() {
      return this.getName();
   }

   @Override
   public void setActive(boolean status) {
   }

   @Override
   public boolean reload() {
      this.unload();
      return super.reload();
   }

   @Override
   public boolean unload() {
      return this.unload(true);
   }

   protected void cancelAllQuestTimers() {
      if (this._questTimers != null) {
         for(List<QuestTimer> timers : this.getQuestTimers().values()) {
            this._readLock.lock();

            try {
               for(QuestTimer timer : timers) {
                  timer.cancel();
               }
            } finally {
               this._readLock.unlock();
            }

            timers.clear();
         }

         this._questTimers.clear();
      }
   }

   public boolean unload(boolean removeFromList) {
      this.saveGlobalData();
      if (this._questTimers != null) {
         for(List<QuestTimer> timers : this.getQuestTimers().values()) {
            this._readLock.lock();

            try {
               for(QuestTimer timer : timers) {
                  timer.cancel();
               }
            } finally {
               this._readLock.unlock();
            }

            timers.clear();
         }

         this.getQuestTimers().clear();
      }

      for(Integer npcId : this._questInvolvedNpcs) {
         NpcTemplate template = NpcsParser.getInstance().getTemplate(npcId);
         if (template != null) {
            template.removeQuest(this);
         }
      }

      this._questInvolvedNpcs.clear();
      return removeFromList ? QuestManager.getInstance().removeQuest(this) : true;
   }

   public Set<Integer> getQuestInvolvedNpcs() {
      return this._questInvolvedNpcs;
   }

   @Override
   public ScriptManagerLoader<?> getScriptManager() {
      return QuestManager.getInstance();
   }

   public void setOnEnterWorld(boolean val) {
      this._onEnterWorld = val;
   }

   public boolean getOnEnterWorld() {
      return this._onEnterWorld;
   }

   public void setIsCustom(boolean val) {
      this._isCustom = val;
   }

   public boolean isCustomQuest() {
      return this._isCustom;
   }

   public void setOlympiadUse(boolean val) {
      this._isOlympiadUse = val;
   }

   public boolean isOlympiadUse() {
      return this._isOlympiadUse;
   }

   public static long getQuestItemsCount(Player player, int itemId) {
      return player.getInventory().getInventoryItemCount(itemId, -1);
   }

   public long getQuestItemsCount(Player player, int... itemIds) {
      long count = 0L;

      for(ItemInstance item : player.getInventory().getItems()) {
         if (item != null) {
            for(int itemId : itemIds) {
               if (item.getId() == itemId) {
                  if (count + item.getCount() > Long.MAX_VALUE) {
                     return Long.MAX_VALUE;
                  }

                  count += item.getCount();
               }
            }
         }
      }

      return count;
   }

   public static boolean hasQuestItems(Player player, int itemId) {
      return player.getInventory().getItemByItemId(itemId) != null;
   }

   public static boolean hasQuestItems(Player player, int... itemIds) {
      PcInventory inv = player.getInventory();

      for(int itemId : itemIds) {
         if (inv.getItemByItemId(itemId) == null) {
            return false;
         }
      }

      return true;
   }

   public static int getEnchantLevel(Player player, int itemId) {
      ItemInstance enchantedItem = player.getInventory().getItemByItemId(itemId);
      return enchantedItem == null ? 0 : enchantedItem.getEnchantLevel();
   }

   public void giveAdena(Player player, long count, boolean applyRates) {
      if (applyRates) {
         rewardItems(player, 57, count);
      } else {
         giveItems(player, 57, count);
      }
   }

   public static void rewardItems(Player player, ItemHolder holder) {
      rewardItems(player, holder.getId(), holder.getCount());
   }

   public static void rewardItems(Player player, int itemId, long count) {
      if (count > 0L) {
         ItemInstance _tmpItem = ItemsParser.getInstance().createDummyItem(itemId);
         if (_tmpItem != null) {
            try {
               if (itemId == 57) {
                  count = (long)((float)count * Config.RATE_QUEST_REWARD_ADENA);
               } else if (Config.RATE_QUEST_REWARD_USE_MULTIPLIERS) {
                  if (_tmpItem.isEtcItem()) {
                     switch(_tmpItem.getEtcItem().getItemType()) {
                        case POTION:
                           count = (long)((float)count * Config.RATE_QUEST_REWARD_POTION);
                           break;
                        case SCRL_ENCHANT_WP:
                        case SCRL_ENCHANT_AM:
                        case SCROLL:
                           count = (long)((float)count * Config.RATE_QUEST_REWARD_SCROLL);
                           break;
                        case RECIPE:
                           count = (long)((float)count * Config.RATE_QUEST_REWARD_RECIPE);
                           break;
                        case MATERIAL:
                           count = (long)((float)count * Config.RATE_QUEST_REWARD_MATERIAL);
                           break;
                        default:
                           count = (long)((float)count * Config.RATE_QUEST_REWARD);
                     }
                  }
               } else {
                  count = (long)((float)count * Config.RATE_QUEST_REWARD);
               }
            } catch (Exception var6) {
               count = Long.MAX_VALUE;
            }

            ItemInstance item = player.getInventory().addItem("Quest", itemId, count, player, player.getTarget());
            if (item != null) {
               sendItemGetMessage(player, item, count);
            }
         }
      }
   }

   private static void sendItemGetMessage(Player player, ItemInstance item, long count) {
      if (item.getId() == 57) {
         SystemMessage smsg = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S1_ADENA);
         smsg.addItemNumber(count);
         player.sendPacket(smsg);
      } else if (count > 1L) {
         SystemMessage smsg = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S);
         smsg.addItemName(item);
         smsg.addItemNumber(count);
         player.sendPacket(smsg);
      } else {
         SystemMessage smsg = SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1);
         smsg.addItemName(item);
         player.sendPacket(smsg);
      }

      StatusUpdate su = new StatusUpdate(player);
      su.addAttribute(14, player.getCurrentLoad());
      player.sendPacket(su);
   }

   public static void giveItems(Player player, int itemId, long count) {
      giveItems(player, itemId, count, 0);
   }

   protected static void giveItems(Player player, ItemHolder holder) {
      giveItems(player, holder.getId(), holder.getCount());
   }

   public static void giveItems(Player player, int itemId, long count, int enchantlevel) {
      if (count > 0L) {
         ItemInstance item = player.getInventory().addItem("Quest", itemId, count, player, player.getTarget());
         if (item != null) {
            if (enchantlevel > 0 && itemId != 57) {
               item.setEnchantLevel(enchantlevel);
            }

            sendItemGetMessage(player, item, count);
         }
      }
   }

   public static void giveItems(Player player, int itemId, long count, byte attributeId, int attributeLevel) {
      if (count > 0L) {
         ItemInstance item = player.getInventory().addItem("Quest", itemId, count, player, player.getTarget());
         if (item != null) {
            if (attributeId >= 0 && attributeLevel > 0) {
               item.setElementAttr(attributeId, attributeLevel);
               if (item.isEquipped()) {
                  item.updateElementAttrBonus(player);
               }

               InventoryUpdate iu = new InventoryUpdate();
               iu.addModifiedItem(item);
               player.sendPacket(iu);
            }

            sendItemGetMessage(player, item, count);
         }
      }
   }

   public boolean dropQuestItems(Player player, int itemId, int count, long neededCount, int dropChance, boolean sound) {
      return dropQuestItems(player, itemId, count, count, neededCount, dropChance, sound);
   }

   public static boolean dropQuestItems(Player player, int itemId, int minCount, int maxCount, long neededCount, int dropChance, boolean sound) {
      dropChance = (int)((float)dropChance * (Config.RATE_QUEST_DROP / (float)(player.getParty() != null ? player.getParty().getMemberCount() : 1)));
      long currentCount = getQuestItemsCount(player, itemId);
      if (neededCount > 0L && currentCount >= neededCount) {
         return true;
      } else if (currentCount >= neededCount) {
         return true;
      } else {
         long itemCount = 0L;

         for(int random = Rnd.get(1000000); random < dropChance; dropChance -= 1000000) {
            if (minCount < maxCount) {
               itemCount += (long)Rnd.get(minCount, maxCount);
            } else if (minCount == maxCount) {
               itemCount += (long)minCount;
            } else {
               ++itemCount;
            }
         }

         if (itemCount > 0L) {
            if (neededCount > 0L && currentCount + itemCount > neededCount) {
               itemCount = neededCount - currentCount;
            }

            if (!player.getInventory().validateCapacityByItemId(itemId)) {
               return false;
            }

            player.addItem("Quest", itemId, itemCount, player.getTarget(), true);
            if (sound) {
               playSound(player, currentCount + itemCount < neededCount ? Quest.QuestSound.ITEMSOUND_QUEST_ITEMGET : Quest.QuestSound.ITEMSOUND_QUEST_MIDDLE);
            }
         }

         return neededCount > 0L && currentCount + itemCount >= neededCount;
      }
   }

   public static boolean takeItems(Player player, int itemId, long amount) {
      ItemInstance item = player.getInventory().getItemByItemId(itemId);
      if (item == null) {
         return false;
      } else {
         if (amount < 0L || amount > item.getCount()) {
            amount = item.getCount();
         }

         if (item.isEquipped()) {
            ItemInstance[] unequiped = player.getInventory().unEquipItemInBodySlotAndRecord(item.getItem().getBodyPart());
            InventoryUpdate iu = new InventoryUpdate();

            for(ItemInstance itm : unequiped) {
               iu.addModifiedItem(itm);
            }

            player.sendPacket(iu);
            player.broadcastCharInfo();
         }

         return player.destroyItemByItemId("Quest", itemId, amount, player, true);
      }
   }

   public static long takeAllItems(Player player, int itemId, long amount) {
      ItemInstance item = player.getInventory().getItemByItemId(itemId);
      if (item == null) {
         return 0L;
      } else {
         if (amount < 0L || amount > item.getCount()) {
            amount = item.getCount();
         }

         if (item.isEquipped()) {
            ItemInstance[] unequiped = player.getInventory().unEquipItemInBodySlotAndRecord(item.getItem().getBodyPart());
            InventoryUpdate iu = new InventoryUpdate();

            for(ItemInstance itm : unequiped) {
               iu.addModifiedItem(itm);
            }

            player.sendPacket(iu);
            player.broadcastCharInfo();
         }

         player.destroyItemByItemId("Quest", itemId, amount, player, true);
         return amount;
      }
   }

   protected static boolean takeItems(Player player, ItemHolder holder) {
      return takeItems(player, holder.getId(), holder.getCount());
   }

   public static boolean takeItems(Player player, int amount, int... itemIds) {
      boolean check = true;
      if (itemIds != null) {
         for(int item : itemIds) {
            check &= takeItems(player, item, (long)amount);
         }
      }

      return check;
   }

   public void removeRegisteredQuestItems(Player player) {
      takeItems(player, -1, this.questItemIds);
   }

   public static void playSound(Player player, String sound) {
      player.sendPacket(Quest.QuestSound.getSound(sound));
   }

   public static void playSound(Player player, Quest.QuestSound sound) {
      player.sendPacket(sound.getPacket());
   }

   public static void addExpAndSp(Player player, long exp, int sp) {
      player.addExpAndSp(
         (long)player.calcStat(Stats.EXPSP_RATE, (double)((float)exp * Config.RATE_QUEST_REWARD_XP), null, null),
         (int)player.calcStat(Stats.EXPSP_RATE, (double)((float)sp * Config.RATE_QUEST_REWARD_SP), null, null)
      );
   }

   public static int getRandom(int max) {
      return Rnd.get(max);
   }

   public static int getRandom(int min, int max) {
      return Rnd.get(min, max);
   }

   public static boolean getRandomBoolean() {
      return Rnd.nextBoolean();
   }

   public static int getItemEquipped(Player player, int slot) {
      return player.getInventory().getPaperdollItemId(slot);
   }

   public static int getGameTicks() {
      return GameTimeController.getInstance().getGameTicks();
   }

   public final void executeForEachPlayer(Player player, final Npc npc, final boolean isSummon, boolean includeParty, boolean includeCommandChannel) {
      if ((includeParty || includeCommandChannel) && player.isInParty()) {
         if (includeCommandChannel && player.getParty().isInCommandChannel()) {
            player.getParty().getCommandChannel().forEachMember(new IL2Procedure<Player>() {
               public boolean execute(Player member) {
                  Quest.this.actionForEachPlayer(member, npc, isSummon);
                  return true;
               }
            });
         } else if (includeParty) {
            player.getParty().forEachMember(new IL2Procedure<Player>() {
               public boolean execute(Player member) {
                  Quest.this.actionForEachPlayer(member, npc, isSummon);
                  return true;
               }
            });
         }
      } else {
         this.actionForEachPlayer(player, npc, isSummon);
      }
   }

   public void actionForEachPlayer(Player player, Npc npc, boolean isSummon) {
   }

   public void teleportPlayer(Player player, Location loc, int instanceId) {
      this.teleportPlayer(player, loc, instanceId, true);
   }

   public void teleportPlayer(Player player, Location loc, int instanceId, boolean allowRandomOffset) {
      player.setReflectionId(instanceId);
      player.teleToLocation(loc, allowRandomOffset);
   }

   protected static boolean isIntInArray(int i, int[] ia) {
      for(int v : ia) {
         if (i == v) {
            return true;
         }
      }

      return false;
   }

   public boolean isDigit(String digit) {
      try {
         Integer.parseInt(digit);
         return true;
      } catch (Exception var3) {
         return false;
      }
   }

   public void addMoveFinishedId(int npcId) {
      this.addEventId(Quest.QuestEventType.ON_MOVE_FINISHED, npcId);
   }

   public void addNodeArrivedId(int... npcIds) {
      this.addEventId(Quest.QuestEventType.ON_NODE_ARRIVED, npcIds);
   }

   public final void notifyNodeArrived(Npc npc) {
      try {
         this.onNodeArrived(npc);
      } catch (Exception var3) {
         _log.log(Level.WARNING, "Exception on onNodeArrived() in notifyNodeArrived(): " + var3.getMessage(), (Throwable)var3);
      }
   }

   public void onNodeArrived(Npc npc) {
   }

   public boolean onMoveFinished(Npc npc) {
      return false;
   }

   public void onRouteFinished(Npc npc) {
   }

   public Npc spawnNpc(int npcId, int x, int y, int z, int heading, int instanceId) {
      NpcTemplate npcTemplate = NpcsParser.getInstance().getTemplate(npcId);
      Reflection instance = ReflectionManager.getInstance().getReflection(instanceId);

      try {
         Spawner npcSpawn = new Spawner(npcTemplate);
         npcSpawn.setX(x);
         npcSpawn.setY(y);
         npcSpawn.setZ(z);
         npcSpawn.setHeading(heading);
         npcSpawn.setAmount(1);
         npcSpawn.setReflectionId(instanceId);
         SpawnParser.getInstance().addNewSpawn(npcSpawn);
         Npc npc = npcSpawn.spawnOne(false);
         if (instanceId > 0) {
            instance.addNpc(npc);
         }

         return npc;
      } catch (Exception var11) {
         return null;
      }
   }

   public Npc spawnNpc(int npcId, Location loc, int heading, int instanceId) {
      return this.spawnNpc(npcId, loc.getX(), loc.getY(), loc.getZ(), heading, instanceId);
   }

   public Npc spawnNpc(int npcId, Location loc, int heading) {
      return this.spawnNpc(npcId, loc.getX(), loc.getY(), loc.getZ(), heading, 0);
   }

   public boolean hasAtLeastOneQuestItem(Player player, int... itemIds) {
      PcInventory inv = player.getInventory();

      for(int itemId : itemIds) {
         if (inv.getItemByItemId(itemId) != null) {
            return true;
         }
      }

      return false;
   }

   public static final void specialCamera(
      Player player,
      Creature creature,
      int force,
      int angle1,
      int angle2,
      int time,
      int range,
      int duration,
      int relYaw,
      int relPitch,
      int isWide,
      int relAngle
   ) {
      player.sendPacket(new SpecialCamera(creature, force, angle1, angle2, time, range, duration, relYaw, relPitch, isWide, relAngle));
   }

   public static final void specialCameraEx(
      Player player, Creature creature, int force, int angle1, int angle2, int time, int duration, int relYaw, int relPitch, int isWide, int relAngle
   ) {
      player.sendPacket(new SpecialCamera(creature, player, force, angle1, angle2, time, duration, relYaw, relPitch, isWide, relAngle));
   }

   public static final void specialCamera3(
      Player player,
      Creature creature,
      int force,
      int angle1,
      int angle2,
      int time,
      int range,
      int duration,
      int relYaw,
      int relPitch,
      int isWide,
      int relAngle,
      int unk
   ) {
      player.sendPacket(new SpecialCamera(creature, force, angle1, angle2, time, range, duration, relYaw, relPitch, isWide, relAngle, unk));
   }

   public static boolean giveItemRandomly(Player player, int itemId, long amountToGive, long limit, double dropChance, boolean playSound) {
      return giveItemRandomly(player, null, itemId, amountToGive, amountToGive, limit, dropChance, playSound);
   }

   public static boolean giveItemRandomly(Player player, Npc npc, int itemId, long amountToGive, long limit, double dropChance, boolean playSound) {
      return giveItemRandomly(player, npc, itemId, amountToGive, amountToGive, limit, dropChance, playSound);
   }

   public static boolean giveItemRandomly(Player player, Npc npc, int itemId, long minAmount, long maxAmount, long limit, double dropChance, boolean playSound) {
      long currentCount = getQuestItemsCount(player, itemId);
      if (limit > 0L && currentCount >= limit) {
         return true;
      } else {
         minAmount = (long)((float)minAmount * Config.RATE_QUEST_DROP);
         maxAmount = (long)((float)maxAmount * Config.RATE_QUEST_DROP);
         dropChance *= (double)Config.RATE_QUEST_DROP;
         long amountToGive = minAmount == maxAmount ? minAmount : Rnd.get(minAmount, maxAmount);
         double random = Rnd.nextDouble();
         if (dropChance >= random && amountToGive > 0L && player.getInventory().validateCapacityByItemId(itemId)) {
            if (limit > 0L && currentCount + amountToGive > limit) {
               amountToGive = limit - currentCount;
            }

            ItemInstance item = player.addItem("Quest", itemId, amountToGive, npc, true);
            if (item != null) {
               if (currentCount + amountToGive == limit) {
                  if (playSound) {
                     playSound(player, Quest.QuestSound.ITEMSOUND_QUEST_MIDDLE);
                  }

                  return true;
               }

               if (playSound) {
                  playSound(player, Quest.QuestSound.ITEMSOUND_QUEST_ITEMGET);
               }

               if (limit <= 0L) {
                  return true;
               }
            }
         }

         return false;
      }
   }

   public static void calcExpAndSp(Player player, int questId) {
      QuestTemplate template = QuestsParser.getInstance().getTemplate(questId);
      if (template != null) {
         QuestExperience reward = template.getExperienceRewards();
         if (reward != null) {
            double exp = (double)reward.getExp() * player.getStat().getRExp();
            double sp = (double)reward.getSp() * player.getStat().getRSp();
            exp *= reward.isExpRateable() ? (double)Config.RATE_QUEST_REWARD_XP : reward.getExpRate();
            sp *= reward.isExpRateable() ? (double)Config.RATE_QUEST_REWARD_SP : reward.getSpRate();
            player.addExpAndSp((long)player.calcStat(Stats.EXPSP_RATE, exp, null, null), (int)player.calcStat(Stats.EXPSP_RATE, sp, null, null));
         }
      }
   }

   public static void calcReward(Player player, int questId) {
      QuestTemplate template = QuestsParser.getInstance().getTemplate(questId);
      if (template != null) {
         List<QuestRewardItem> rewards = template.getRewards();
         if (rewards != null && !rewards.isEmpty()) {
            for(QuestRewardItem reward : rewards) {
               if (reward != null) {
                  long amount = reward.getMaxCount() != 0L ? Rnd.get(reward.getMinCount(), reward.getMaxCount()) : reward.getMinCount();
                  amount = (long)(
                     (double)amount
                        * (
                           reward.isRateable()
                              ? (double)Config.RATE_QUEST_REWARD
                                 * (
                                    player.isInParty() && Config.PREMIUM_PARTY_RATE
                                       ? player.getParty().getQuestRewardRate()
                                       : player.getPremiumBonus().getQuestRewardRate()
                                 )
                              : reward.getRate()
                        )
                  );
                  ItemInstance item = player.getInventory().addItem("Quest", reward.getId(), amount, player, player.getTarget());
                  if (item == null) {
                     return;
                  }

                  sendItemGetMessage(player, item, amount);
               }
            }
         }
      }
   }

   public static void calcReward(Player player, int questId, int variant) {
      QuestTemplate template = QuestsParser.getInstance().getTemplate(questId);
      if (template != null) {
         List<QuestRewardItem> rewards = template.getVariantRewards().get(variant);
         if (rewards != null && !rewards.isEmpty()) {
            for(QuestRewardItem reward : rewards) {
               if (reward != null) {
                  long amount = reward.getMaxCount() != 0L ? Rnd.get(reward.getMinCount(), reward.getMaxCount()) : reward.getMinCount();
                  amount = (long)(
                     (double)amount
                        * (
                           reward.isRateable()
                              ? (double)Config.RATE_QUEST_REWARD
                                 * (
                                    player.isInParty() && Config.PREMIUM_PARTY_RATE
                                       ? player.getParty().getQuestRewardRate()
                                       : player.getPremiumBonus().getQuestRewardRate()
                                 )
                              : reward.getRate()
                        )
                  );
                  ItemInstance item = player.getInventory().addItem("Quest", reward.getId(), amount, player, player.getTarget());
                  if (item == null) {
                     return;
                  }

                  sendItemGetMessage(player, item, amount);
               }
            }
         }
      }
   }

   public static void calcRewardPerItem(Player player, int questId, int variant, int totalAmount) {
      QuestTemplate template = QuestsParser.getInstance().getTemplate(questId);
      if (template != null) {
         List<QuestRewardItem> rewards = template.getVariantRewards().get(variant);
         if (rewards != null && !rewards.isEmpty()) {
            for(QuestRewardItem reward : rewards) {
               if (reward != null) {
                  long amount = reward.getMaxCount() != 0L ? Rnd.get(reward.getMinCount(), reward.getMaxCount()) : reward.getMinCount();
                  amount = (long)(
                     (double)amount
                        * (
                           reward.isRateable()
                              ? (double)Config.RATE_QUEST_REWARD
                                 * (
                                    player.isInParty() && Config.PREMIUM_PARTY_RATE
                                       ? player.getParty().getQuestRewardRate()
                                       : player.getPremiumBonus().getQuestRewardRate()
                                 )
                              : reward.getRate()
                        )
                  );
                  amount *= (long)totalAmount;
                  ItemInstance item = player.getInventory().addItem("Quest", reward.getId(), amount, player, player.getTarget());
                  if (item == null) {
                     return;
                  }

                  sendItemGetMessage(player, item, amount);
               }
            }
         }
      }
   }

   public static void calcRewardPerItem(Player player, int questId, int variant, int totalAmount, boolean isRandom) {
      QuestTemplate template = QuestsParser.getInstance().getTemplate(questId);
      if (template != null) {
         List<QuestRewardItem> rewards = template.getVariantRewards().get(variant);
         if (rewards != null && !rewards.isEmpty()) {
            QuestRewardItem reward = rewards.get(Rnd.get(rewards.size()));
            if (reward != null) {
               long amount = reward.getMaxCount() != 0L ? Rnd.get(reward.getMinCount(), reward.getMaxCount()) : reward.getMinCount();
               amount = (long)(
                  (double)amount
                     * (
                        !reward.isRateable()
                           ? reward.getRate()
                           : (double)Config.RATE_QUEST_REWARD
                              * (
                                 player.isInParty() && Config.PREMIUM_PARTY_RATE
                                    ? player.getParty().getQuestRewardRate()
                                    : player.getPremiumBonus().getQuestRewardRate()
                              )
                     )
               );
               amount *= (long)totalAmount;
               ItemInstance item = player.getInventory().addItem("Quest", reward.getId(), amount, player, player.getTarget());
               if (item == null) {
                  return;
               }

               sendItemGetMessage(player, item, amount);
            }
         }
      }
   }

   public static void calcReward(Player player, int questId, int variant, boolean isRandom) {
      QuestTemplate template = QuestsParser.getInstance().getTemplate(questId);
      if (template != null) {
         List<QuestRewardItem> rewards = template.getVariantRewards().get(variant);
         if (rewards != null && !rewards.isEmpty()) {
            if (isRandom) {
               QuestRewardItem reward = rewards.get(Rnd.get(rewards.size()));
               if (reward != null) {
                  long amount = reward.getMaxCount() != 0L ? Rnd.get(reward.getMinCount(), reward.getMaxCount()) : reward.getMinCount();
                  amount = (long)(
                     (double)amount
                        * (
                           !reward.isRateable()
                              ? reward.getRate()
                              : (double)Config.RATE_QUEST_REWARD
                                 * (
                                    player.isInParty() && Config.PREMIUM_PARTY_RATE
                                       ? player.getParty().getQuestRewardRate()
                                       : player.getPremiumBonus().getQuestRewardRate()
                                 )
                        )
                  );
                  ItemInstance item = player.getInventory().addItem("Quest", reward.getId(), amount, player, player.getTarget());
                  if (item == null) {
                     return;
                  }

                  sendItemGetMessage(player, item, amount);
               }
            } else {
               for(QuestRewardItem reward : rewards) {
                  if (reward != null) {
                     long amount = reward.getMaxCount() != 0L ? Rnd.get(reward.getMinCount(), reward.getMaxCount()) : reward.getMinCount();
                     amount = (long)(
                        (double)amount
                           * (
                              reward.isRateable()
                                 ? (double)Config.RATE_QUEST_REWARD
                                    * (
                                       player.isInParty() && Config.PREMIUM_PARTY_RATE
                                          ? player.getParty().getQuestRewardRate()
                                          : player.getPremiumBonus().getQuestRewardRate()
                                    )
                                 : reward.getRate()
                           )
                     );
                     ItemInstance item = player.getInventory().addItem("Quest", reward.getId(), amount, player, player.getTarget());
                     if (item == null) {
                        return;
                     }

                     sendItemGetMessage(player, item, amount);
                  }
               }
            }
         }
      }
   }

   public static int getMinLvl(int questId) {
      QuestTemplate template = QuestsParser.getInstance().getTemplate(questId);
      return template != null ? template.getMinLvl() : 1;
   }

   public static int getMaxLvl(int questId) {
      QuestTemplate template = QuestsParser.getInstance().getTemplate(questId);
      return template != null ? template.getMaxLvl() : 85;
   }

   public StatsSet getQuestParams(int questId) {
      return QuestsParser.getInstance().getTemplate(questId).getParams();
   }

   public static enum QuestEventType {
      ON_FIRST_TALK(false),
      QUEST_START(true),
      ON_TALK(true),
      ON_ATTACK(true),
      ON_KILL(true),
      ON_SPAWN(true),
      ON_SKILL_SEE(true),
      ON_FACTION_CALL(true),
      ON_AGGRO_RANGE_ENTER(true),
      ON_SPELL_FINISHED(true),
      ON_SKILL_LEARN(false),
      ON_ENTER_ZONE(true),
      ON_EXIT_ZONE(true),
      ON_TRAP_ACTION(true),
      ON_ITEM_USE(true),
      ON_NODE_ARRIVED(true),
      ON_EVENT_RECEIVED(true),
      ON_MOVE_FINISHED(true),
      ON_SEE_CREATURE(true),
      ON_ROUTE_FINISHED(true),
      ON_CAN_SEE_ME(false);

      private boolean _allowMultipleRegistration;

      private QuestEventType(boolean allowMultipleRegistration) {
         this._allowMultipleRegistration = allowMultipleRegistration;
      }

      public boolean isMultipleRegistrationAllowed() {
         return this._allowMultipleRegistration;
      }
   }

   public static enum QuestSound {
      ITEMSOUND_QUEST_ACCEPT(new PlaySound("ItemSound.quest_accept")),
      ITEMSOUND_QUEST_MIDDLE(new PlaySound("ItemSound.quest_middle")),
      ITEMSOUND_QUEST_FINISH(new PlaySound("ItemSound.quest_finish")),
      ITEMSOUND_QUEST_ITEMGET(new PlaySound("ItemSound.quest_itemget")),
      ITEMSOUND_QUEST_TUTORIAL(new PlaySound("ItemSound.quest_tutorial")),
      ITEMSOUND_QUEST_GIVEUP(new PlaySound("ItemSound.quest_giveup")),
      ITEMSOUND_QUEST_BEFORE_BATTLE(new PlaySound("ItemSound.quest_before_battle")),
      ITEMSOUND_QUEST_JACKPOT(new PlaySound("ItemSound.quest_jackpot")),
      ITEMSOUND_QUEST_FANFARE_1(new PlaySound("ItemSound.quest_fanfare_1")),
      ITEMSOUND_QUEST_FANFARE_2(new PlaySound("ItemSound.quest_fanfare_2")),
      ITEMSOUND_QUEST_FANFARE_MIDDLE(new PlaySound("ItemSound.quest_fanfare_middle")),
      ITEMSOUND_ARMOR_WOOD(new PlaySound("ItemSound.armor_wood_3")),
      ITEMSOUND_ARMOR_CLOTH(new PlaySound("ItemSound.item_drop_equip_armor_cloth")),
      AMDSOUND_ED_CHIMES(new PlaySound("AmdSound.ed_chimes_05")),
      HORROR_01(new PlaySound("horror_01")),
      AMBSOUND_HORROR_01(new PlaySound("AmbSound.dd_horror_01")),
      AMBSOUND_HORROR_03(new PlaySound("AmbSound.d_horror_03")),
      AMBSOUND_HORROR_15(new PlaySound("AmbSound.d_horror_15")),
      ITEMSOUND_ARMOR_LEATHER(new PlaySound("ItemSound.itemdrop_armor_leather")),
      ITEMSOUND_WEAPON_SPEAR(new PlaySound("ItemSound.itemdrop_weapon_spear")),
      AMBSOUND_MT_CREAK(new PlaySound("AmbSound.mt_creak01")),
      AMBSOUND_EG_DRON(new PlaySound("AmbSound.eg_dron_02")),
      SKILLSOUND_HORROR_02(new PlaySound("SkillSound5.horror_02")),
      CHRSOUND_MHFIGHTER_CRY(new PlaySound("ChrSound.MHFighter_cry")),
      AMDSOUND_WIND_LOOT(new PlaySound("AmdSound.d_wind_loot_02")),
      INTERFACESOUND_CHARSTAT_OPEN(new PlaySound("InterfaceSound.charstat_open_01")),
      AMDSOUND_HORROR_02(new PlaySound("AmdSound.dd_horror_02")),
      CHRSOUND_FDELF_CRY(new PlaySound("ChrSound.FDElf_Cry")),
      AMBSOUND_WINGFLAP(new PlaySound("AmbSound.t_wingflap_04")),
      AMBSOUND_THUNDER(new PlaySound("AmbSound.thunder_02")),
      AMBSOUND_DRONE(new PlaySound("AmbSound.ed_drone_02")),
      AMBSOUND_CRYSTAL_LOOP(new PlaySound("AmbSound.cd_crystal_loop")),
      AMBSOUND_PERCUSSION_01(new PlaySound("AmbSound.dt_percussion_01")),
      AMBSOUND_PERCUSSION_02(new PlaySound("AmbSound.ac_percussion_02")),
      ITEMSOUND_BROKEN_KEY(new PlaySound("ItemSound2.broken_key")),
      ITEMSOUND_SIREN(new PlaySound("ItemSound3.sys_siren")),
      ITEMSOUND_ENCHANT_SUCCESS(new PlaySound("ItemSound3.sys_enchant_success")),
      ITEMSOUND_ENCHANT_FAILED(new PlaySound("ItemSound3.sys_enchant_failed")),
      ITEMSOUND_SOW_SUCCESS(new PlaySound("ItemSound3.sys_sow_success")),
      SKILLSOUND_HORROR_1(new PlaySound("SkillSound5.horror_01")),
      SKILLSOUND_HORROR_2(new PlaySound("SkillSound5.horror_02")),
      SKILLSOUND_ANTARAS_FEAR(new PlaySound("SkillSound3.antaras_fear")),
      SKILLSOUND_JEWEL_CELEBRATE(new PlaySound("SkillSound2.jewel.celebrate")),
      SKILLSOUND_LIQUID_MIX(new PlaySound("SkillSound5.liquid_mix_01")),
      SKILLSOUND_LIQUID_SUCCESS(new PlaySound("SkillSound5.liquid_success_01")),
      SKILLSOUND_LIQUID_FAIL(new PlaySound("SkillSound5.liquid_fail_01")),
      ETCSOUND_ELROKI_SONG_FULL(new PlaySound("EtcSound.elcroki_song_full")),
      ETCSOUND_ELROKI_SONG_1ST(new PlaySound("EtcSound.elcroki_song_1st")),
      ETCSOUND_ELROKI_SONG_2ND(new PlaySound("EtcSound.elcroki_song_2nd")),
      ETCSOUND_ELROKI_SONG_3RD(new PlaySound("EtcSound.elcroki_song_3rd")),
      BS01_A(new PlaySound("BS01_A")),
      BS02_A(new PlaySound("BS02_A")),
      BS03_A(new PlaySound("BS03_A")),
      BS04_A(new PlaySound("BS04_A")),
      BS06_A(new PlaySound("BS06_A")),
      BS07_A(new PlaySound("BS07_A")),
      BS08_A(new PlaySound("BS08_A")),
      BS01_D(new PlaySound("BS01_D")),
      BS02_D(new PlaySound("BS02_D")),
      BS05_D(new PlaySound("BS05_D")),
      BS07_D(new PlaySound("BS07_D"));

      private final PlaySound _playSound;
      private static Map<String, PlaySound> soundPackets = new HashMap<>();

      private QuestSound(PlaySound playSound) {
         this._playSound = playSound;
      }

      public static PlaySound getSound(String soundName) {
         if (soundPackets.containsKey(soundName)) {
            return soundPackets.get(soundName);
         } else {
            for(Quest.QuestSound qs : values()) {
               if (qs._playSound.getSoundName().equals(soundName)) {
                  soundPackets.put(soundName, qs._playSound);
                  return qs._playSound;
               }
            }

            Quest._log.info("Missing QuestSound enum for sound: " + soundName);
            soundPackets.put(soundName, new PlaySound(soundName));
            return soundPackets.get(soundName);
         }
      }

      public String getSoundName() {
         return this._playSound.getSoundName();
      }

      public PlaySound getPacket() {
         return this._playSound;
      }
   }

   public static enum TrapAction {
      TRAP_TRIGGERED,
      TRAP_DETECTED,
      TRAP_DISARMED;
   }
}
