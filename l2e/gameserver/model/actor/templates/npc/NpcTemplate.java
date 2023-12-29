package l2e.gameserver.model.actor.templates.npc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import l2e.commons.collections.MultiValueSet;
import l2e.gameserver.model.actor.templates.character.CharTemplate;
import l2e.gameserver.model.base.ClassId;
import l2e.gameserver.model.interfaces.IIdentifiable;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.reward.RewardList;
import l2e.gameserver.model.reward.RewardType;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.SkillType;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.skills.targets.TargetType;
import l2e.gameserver.model.stats.StatsSet;
import org.apache.commons.lang3.ArrayUtils;

public final class NpcTemplate extends CharTemplate implements IIdentifiable {
   private static final Logger _log = Logger.getLogger(NpcTemplate.class.getName());
   private final int _npcId;
   private final int _displayId;
   private final String _type;
   private final String _name;
   private final String _nameRu;
   private String _titleEn;
   private String _titleRu;
   private byte _level;
   private final int _rewardExp;
   private final int _rewardSp;
   private final int _rewardRp;
   private int _aggroRange;
   private int _hideAggroRange;
   private final int _rHand;
   private final int _lHand;
   private final int _enchantEffect;
   private final int _castleId;
   private NpcTemplate.Race _race;
   private final String _clientClass;
   private final boolean _isCustom;
   private final boolean _isQuestMonster;
   private final double _baseVitalityDivider;
   private boolean _isRandomMinons = false;
   private String _classAI = null;
   private final NpcTemplate.ShotsType _shots;
   private Faction _faction = Faction.NONE;
   private boolean _isRaid = false;
   private boolean _isEpicRaid = false;
   private boolean _isSiegeGuard = false;
   private boolean _isFlying = false;
   private final boolean _isCommonChest;
   private final boolean _isLethalImmune;
   private Map<RewardType, RewardList> _rewards = Collections.emptyMap();
   private final List<MinionData> _minions = new ArrayList<>();
   private List<AbsorbInfo> _absorbInfo = Collections.emptyList();
   private final List<ClassId> _teachInfo = new ArrayList<>();
   private final Map<Integer, Skill> _skills = new HashMap<>();
   private Skill[] _damageSkills = new Skill[0];
   private Skill[] _dotSkills = new Skill[0];
   private Skill[] _debuffSkills = new Skill[0];
   private Skill[] _buffSkills = new Skill[0];
   private Skill[] _stunSkills = new Skill[0];
   private Skill[] _healSkills = new Skill[0];
   private Skill[] _suicideSkills = new Skill[0];
   private Skill[] _resSkills = new Skill[0];
   private final Map<Quest.QuestEventType, List<Quest>> _questEvents = new ConcurrentHashMap<>();
   private MultiValueSet<String> _parameters = StatsSet.EMPTY;

   public static boolean isAssignableTo(Class<?> sub, Class<?> clazz) {
      if (clazz.isInterface()) {
         Class<?>[] interfaces = sub.getInterfaces();

         for(Class<?> interface1 : interfaces) {
            if (clazz.getName().equals(interface1.getName())) {
               return true;
            }
         }
      } else {
         do {
            if (sub.getName().equals(clazz.getName())) {
               return true;
            }

            sub = sub.getSuperclass();
         } while(sub != null);
      }

      return false;
   }

   public static boolean isAssignableTo(Object obj, Class<?> clazz) {
      return isAssignableTo(obj.getClass(), clazz);
   }

   public NpcTemplate(StatsSet set) {
      super(set);
      this._npcId = set.getInteger("npcId");
      this._displayId = set.getInteger("displayId");
      this._type = set.getString("type");
      this._name = set.getString("nameEn");
      this._nameRu = set.getString("nameRu");
      this._titleEn = set.getString("titleEn");
      this._titleRu = set.getString("titleRu");
      this._isQuestMonster = this.getTitle().equalsIgnoreCase("Quest Monster");
      this._level = set.getByte("level");
      this._rewardExp = set.getInteger("rewardExp");
      this._rewardSp = set.getInteger("rewardSp");
      this._rewardRp = set.getInteger("rewardRp");
      this._aggroRange = set.getInteger("aggroRange");
      this._hideAggroRange = set.getInteger("hideAggroRange", 0);
      this._rHand = set.getInteger("rhand", 0);
      this._lHand = set.getInteger("lhand", 0);
      this._shots = set.getEnum("shots", NpcTemplate.ShotsType.class, NpcTemplate.ShotsType.NONE);
      this._enchantEffect = set.getInteger("enchant", 0);
      this._clientClass = set.getString("texture", "");
      this._castleId = set.getInteger("castle_id", 0);
      this._baseVitalityDivider = this.getLevel() > 0 && this.getRewardExp() > 0
         ? this.getBaseHpMax() * 9.0 * (double)this.getLevel() * (double)this.getLevel() / (double)(100 * this.getRewardExp())
         : 0.0;
      this._isCommonChest = set.getBool("isCommonChest", false);
      this._isLethalImmune = set.getBool("isLethalImmune", false);
      this._isCustom = this._npcId != this._displayId;
      this.setAI(set.getString("ai_type"));
   }

   public void setParameter(String str, Object val) {
      if (this._parameters == StatsSet.EMPTY) {
         this._parameters = new StatsSet();
      }

      this._parameters.set(str, val);
   }

   public boolean hasParameters() {
      return this._parameters != null || this._parameters != StatsSet.EMPTY;
   }

   public void setParameters(MultiValueSet<String> set) {
      if (!set.isEmpty()) {
         if (this._parameters == StatsSet.EMPTY) {
            this._parameters = new MultiValueSet<>(set.size());
         }

         this._parameters.putAll(set);
      }
   }

   public int getParameter(String str, int val) {
      return this._parameters.getInteger(str, val);
   }

   public long getParameter(String str, long val) {
      return this._parameters.getLong(str, val);
   }

   public boolean getParameter(String str, boolean val) {
      return this._parameters.getBool(str, val);
   }

   public String getParameter(String str, String val) {
      return this._parameters.getString(str, val);
   }

   public MultiValueSet<String> getParameters() {
      return this._parameters;
   }

   private void setAI(String ai) {
      this._classAI = ai;
      this._isEpicRaid = this.isType("GrandBoss");
      this._isRaid = this.isType("RaidBoss") || this.isType("FlyRaidBoss") || this.isType("LostCaptain");
      this._isSiegeGuard = this.isType("Defender") || this.isType("FortCommander");
      this._isFlying = this.isType("FlyRaidBoss") || this.isType("FlyMonster") || this.isType("FlyNpc");
   }

   public String getAI() {
      return this._classAI;
   }

   public boolean isRaid() {
      return this._isRaid;
   }

   public boolean isEpicRaid() {
      return this._isEpicRaid;
   }

   public boolean isSiegeGuard() {
      return this._isSiegeGuard;
   }

   public boolean isFlying() {
      return this._isFlying;
   }

   public void setFaction(Faction faction) {
      this._faction = faction;
   }

   public Faction getFaction() {
      return this._faction;
   }

   public NpcTemplate.ShotsType getShots() {
      return this._shots;
   }

   public void setAggroRange(int val) {
      this._aggroRange = val;
   }

   public int getAggroRange() {
      return this._aggroRange;
   }

   public void setHideAggroRange(int val) {
      this._hideAggroRange = val;
   }

   public int getHideAggroRange() {
      return this._hideAggroRange;
   }

   public void addQuestEvent(Quest.QuestEventType EventType, Quest q) {
      if (!this._questEvents.containsKey(EventType)) {
         List<Quest> quests = new ArrayList<>();
         quests.add(q);
         this._questEvents.put(EventType, quests);
      } else {
         List<Quest> quests = this._questEvents.get(EventType);
         if (!EventType.isMultipleRegistrationAllowed() && !quests.isEmpty()) {
            _log.fine(
               "Quest event not allowed in multiple quests.  Skipped addition of Event Type \""
                  + EventType
                  + "\" for NPC \""
                  + this._name
                  + "\" and quest \""
                  + q.getName()
                  + "\"."
            );
         } else {
            quests.add(q);
         }
      }
   }

   public void removeQuest(Quest q) {
      for(Entry<Quest.QuestEventType, List<Quest>> entry : this._questEvents.entrySet()) {
         if (entry.getValue().contains(q)) {
            Iterator<Quest> it = entry.getValue().iterator();

            while(it.hasNext()) {
               Quest q1 = it.next();
               if (q1 == q) {
                  it.remove();
               }
            }

            if (entry.getValue().isEmpty()) {
               this._questEvents.remove(entry.getKey());
            }
         }
      }
   }

   public void addRaidData(MinionData minion, boolean isRandomMinons) {
      this._minions.add(minion);
      this._isRandomMinons = isRandomMinons;
   }

   public boolean isRandomMinons() {
      return this._isRandomMinons;
   }

   public void addTeachInfo(List<ClassId> teachInfo) {
      this._teachInfo.addAll(teachInfo);
   }

   public boolean canTeach(ClassId classId) {
      return classId.level() == 3 ? this._teachInfo.contains(classId.getParent()) : this._teachInfo.contains(classId);
   }

   public double getBaseVitalityDivider() {
      return this._baseVitalityDivider;
   }

   public String getClientClass() {
      return this._clientClass;
   }

   public int getEnchantEffect() {
      return this._enchantEffect;
   }

   public Map<Quest.QuestEventType, List<Quest>> getEventQuests() {
      return this._questEvents;
   }

   public List<Quest> getEventQuests(Quest.QuestEventType EventType) {
      return this._questEvents.get(EventType);
   }

   public int getIdTemplate() {
      return this._displayId;
   }

   public int getLeftHand() {
      return this._lHand;
   }

   public byte getLevel() {
      return this._level;
   }

   public void setLevel(byte val) {
      this._level = val;
   }

   public List<MinionData> getMinionData() {
      return this._minions;
   }

   public String getName() {
      return this._name;
   }

   public String getNameRu() {
      return this._nameRu;
   }

   @Override
   public int getId() {
      return this._npcId;
   }

   public NpcTemplate.Race getRace() {
      if (this._race == null) {
         this._race = NpcTemplate.Race.NONE;
      }

      return this._race;
   }

   public int getRewardExp() {
      return this._rewardExp;
   }

   public int getRewardSp() {
      return this._rewardSp;
   }

   public int getRewardRp() {
      return this._rewardRp;
   }

   public int getRightHand() {
      return this._rHand;
   }

   public List<ClassId> getTeachInfo() {
      return this._teachInfo;
   }

   public String getTitle() {
      return this._titleEn;
   }

   public void setTitle(String title) {
      this._titleEn = title;
   }

   public String getTitleRu() {
      return this._titleRu;
   }

   public void setTitleRu(String title) {
      this._titleRu = title;
   }

   public String getType() {
      return this._type;
   }

   public boolean isCustom() {
      return this._isCustom;
   }

   public boolean isQuestMonster() {
      return this._isQuestMonster;
   }

   public boolean isType(String t) {
      return this._type.equalsIgnoreCase(t);
   }

   public boolean isUndead() {
      return this._race == NpcTemplate.Race.UNDEAD;
   }

   public void setRace(int raceId) {
      switch(raceId) {
         case 1:
            this._race = NpcTemplate.Race.UNDEAD;
            break;
         case 2:
            this._race = NpcTemplate.Race.MAGICCREATURE;
            break;
         case 3:
            this._race = NpcTemplate.Race.BEAST;
            break;
         case 4:
            this._race = NpcTemplate.Race.ANIMAL;
            break;
         case 5:
            this._race = NpcTemplate.Race.PLANT;
            break;
         case 6:
            this._race = NpcTemplate.Race.HUMANOID;
            break;
         case 7:
            this._race = NpcTemplate.Race.SPIRIT;
            break;
         case 8:
            this._race = NpcTemplate.Race.ANGEL;
            break;
         case 9:
            this._race = NpcTemplate.Race.DEMON;
            break;
         case 10:
            this._race = NpcTemplate.Race.DRAGON;
            break;
         case 11:
            this._race = NpcTemplate.Race.GIANT;
            break;
         case 12:
            this._race = NpcTemplate.Race.BUG;
            break;
         case 13:
            this._race = NpcTemplate.Race.FAIRIE;
            break;
         case 14:
            this._race = NpcTemplate.Race.HUMAN;
            break;
         case 15:
            this._race = NpcTemplate.Race.ELVE;
            break;
         case 16:
            this._race = NpcTemplate.Race.DARKELVE;
            break;
         case 17:
            this._race = NpcTemplate.Race.ORC;
            break;
         case 18:
            this._race = NpcTemplate.Race.DWARVE;
            break;
         case 19:
            this._race = NpcTemplate.Race.OTHER;
            break;
         case 20:
            this._race = NpcTemplate.Race.NONLIVING;
            break;
         case 21:
            this._race = NpcTemplate.Race.SIEGEWEAPON;
            break;
         case 22:
            this._race = NpcTemplate.Race.DEFENDINGARMY;
            break;
         case 23:
            this._race = NpcTemplate.Race.MERCENARIE;
            break;
         case 24:
            this._race = NpcTemplate.Race.UNKNOWN;
            break;
         case 25:
            this._race = NpcTemplate.Race.KAMAEL;
            break;
         default:
            this._race = NpcTemplate.Race.NONE;
      }
   }

   public void putRewardList(RewardType rewardType, RewardList list) {
      if (this._rewards.isEmpty()) {
         this._rewards = new HashMap<>(RewardType.values().length);
      }

      this._rewards.put(rewardType, list);
   }

   public RewardList getRewardList(RewardType t) {
      return this._rewards.get(t);
   }

   public Map<RewardType, RewardList> getRewards() {
      return this._rewards;
   }

   public void addAbsorbInfo(AbsorbInfo absorbInfo) {
      if (this._absorbInfo.isEmpty()) {
         this._absorbInfo = new ArrayList<>(1);
      }

      this._absorbInfo.add(absorbInfo);
   }

   public List<AbsorbInfo> getAbsorbInfo() {
      return this._absorbInfo;
   }

   public boolean isTargetable() {
      return this.getParameter("noTargetable", false);
   }

   public boolean isHasNoChatWindow() {
      return this.getParameter("noChatWindow", false);
   }

   public boolean isShowName() {
      return this.getParameter("noShowName", false);
   }

   public boolean getRandomAnimation() {
      return this.getParameter("noRandomAnimation", false);
   }

   public boolean getRandomWalk() {
      return this.getParameter("noRandomWalk", false);
   }

   public boolean isMovementDisabled() {
      return this.getParameter("isMovementDisabled", false);
   }

   public boolean isImmobilized() {
      return this.getParameter("isImmobilized", false);
   }

   public boolean getCanChampion() {
      return this.getParameter("noChampion", false);
   }

   public boolean getCanSeeInSilentMove() {
      return this.getParameter("canSeeInSilentMove", false);
   }

   @Override
   public Map<Integer, Skill> getSkills() {
      return this._skills;
   }

   public void addSkill(Skill skill) {
      if (!this._skills.containsKey(skill.getId())) {
         this._skills.put(skill.getId(), skill);
      }

      if (skill.getTargetType() != TargetType.NONE && skill.getSkillType() != SkillType.NOTDONE && skill.isActive()) {
         if (skill.isSuicideAttack()) {
            this._suicideSkills = ArrayUtils.add(this._suicideSkills, skill);
         } else {
            switch(skill.getSkillType()) {
               case PDAM:
               case MANADAM:
               case MDAM:
               case DRAIN:
               case CHARGEDAM:
               case FATAL:
               case DEATHLINK:
               case CPDAMPERCENT:
                  this._damageSkills = ArrayUtils.add(this._damageSkills, skill);
                  break;
               case DOT:
               case MDOT:
               case POISON:
               case BLEED:
                  this._dotSkills = ArrayUtils.add(this._dotSkills, skill);
                  break;
               case DEBUFF:
               case SLEEP:
               case ROOT:
               case PARALYZE:
               case MUTE:
                  this._debuffSkills = ArrayUtils.add(this._debuffSkills, skill);
                  break;
               case BUFF:
                  this._buffSkills = ArrayUtils.add(this._buffSkills, skill);
                  break;
               case RESURRECT:
                  this._resSkills = ArrayUtils.add(this._resSkills, skill);
                  break;
               case STUN:
                  this._stunSkills = ArrayUtils.add(this._stunSkills, skill);
                  break;
               default:
                  if (skill.hasEffectType(
                     EffectType.CANCEL,
                     EffectType.CANCEL_ALL,
                     EffectType.CANCEL_BY_SLOT,
                     EffectType.MUTE,
                     EffectType.FEAR,
                     EffectType.SLEEP,
                     EffectType.ROOT,
                     EffectType.PARALYZE,
                     EffectType.NEGATE
                  )) {
                     this._debuffSkills = ArrayUtils.add(this._debuffSkills, skill);
                  } else if (skill.hasEffectType(EffectType.HEAL, EffectType.HEAL_OVER_TIME, EffectType.HEAL_PERCENT)) {
                     this._healSkills = ArrayUtils.add(this._healSkills, skill);
                  } else if (skill.hasEffectType(EffectType.STUN)) {
                     this._stunSkills = ArrayUtils.add(this._stunSkills, skill);
                  } else if (skill.hasEffectType(EffectType.DMG_OVER_TIME, EffectType.DMG_OVER_TIME_PERCENT)) {
                     this._dotSkills = ArrayUtils.add(this._dotSkills, skill);
                  }
            }
         }
      }
   }

   public Skill[] getDamageSkills() {
      return this._damageSkills;
   }

   public Skill[] getDotSkills() {
      return this._dotSkills;
   }

   public Skill[] getDebuffSkills() {
      return this._debuffSkills;
   }

   public Skill[] getBuffSkills() {
      return this._buffSkills;
   }

   public Skill[] getStunSkills() {
      return this._stunSkills;
   }

   public Skill[] getSuicideSkills() {
      return this._suicideSkills;
   }

   public Skill[] getResSkills() {
      return this._resSkills;
   }

   public Skill[] getHealSkills() {
      return this._healSkills;
   }

   public int getCastleId() {
      return this._castleId;
   }

   public boolean isCommonChest() {
      return this._isCommonChest;
   }

   public boolean isLethalImmune() {
      return this._isLethalImmune;
   }

   public static enum Race {
      UNDEAD,
      MAGICCREATURE,
      BEAST,
      ANIMAL,
      PLANT,
      HUMANOID,
      SPIRIT,
      ANGEL,
      DEMON,
      DRAGON,
      GIANT,
      BUG,
      FAIRIE,
      HUMAN,
      ELVE,
      DARKELVE,
      ORC,
      DWARVE,
      OTHER,
      NONLIVING,
      SIEGEWEAPON,
      DEFENDINGARMY,
      MERCENARIE,
      UNKNOWN,
      KAMAEL,
      NONE;
   }

   public static enum ShotsType {
      NONE,
      SOUL,
      SPIRIT,
      BSPIRIT,
      SOUL_SPIRIT,
      SOUL_BSPIRIT;
   }
}
