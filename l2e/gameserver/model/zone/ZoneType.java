package l2e.gameserver.model.zone;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import l2e.commons.listener.Listener;
import l2e.commons.listener.ListenerList;
import l2e.gameserver.data.parser.ReflectionParser;
import l2e.gameserver.instancemanager.ReflectionManager;
import l2e.gameserver.listener.events.OnZoneEnterLeaveListener;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.reflection.ReflectionTemplate;
import l2e.gameserver.model.interfaces.ILocational;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.network.serverpackets.GameServerPacket;

public abstract class ZoneType {
   protected static final Logger _log = Logger.getLogger(ZoneType.class.getName());
   private final int _id;
   protected ZoneForm _zone;
   protected Map<Integer, Creature> _characterList;
   private boolean _checkAffected = false;
   private String _name = null;
   private String _type = null;
   private final List<ZoneId> _zoneId = new ArrayList<>();
   private int _reflectionId = 0;
   private int _reflectionTemplateId = 0;
   private int _minLvl;
   private int _maxLvl;
   private int[] _race;
   private int[] _class;
   private char _classType;
   private Map<Quest.QuestEventType, List<Quest>> _questEvents;
   private GameObject.InstanceType _target = GameObject.InstanceType.Creature;
   private boolean _allowStore;
   private AbstractZoneSettings _settings;
   private final ZoneType.ZoneListenerList _listeners = new ZoneType.ZoneListenerList();
   private double _hpLimit = -1.0;
   private double _mpLimit = -1.0;
   private double _cpLimit = -1.0;
   private double _pAtkLimit = -1.0;
   private double _pDefLimit = -1.0;
   private double _pAtkSpeedLimit = -1.0;
   private double _mAtkLimit = -1.0;
   private double _mDefLimit = -1.0;
   private double _mAtkSpeedLimit = -1.0;
   private double _critDmgLimit = -1.0;
   private double _runSpeedLimit = -1.0;
   private double _walkSpeedLimit = -1.0;
   private int _accuracyLimit = -1;
   private double _critHitLimit = -1.0;
   private double _mCritHitLimit = -1.0;
   private int _evasionLimit = -1;
   private double _pvpPhysSkillDmgLimit = -1.0;
   private double _pvpPhysSkillDefLimit = -1.0;
   private double _pvpPhysDefLimit = -1.0;
   private double _pvpPhysDmgLimit = -1.0;
   private double _pvpMagicDmgLimit = -1.0;
   private double _pvpMagicDefLimit = -1.0;

   protected ZoneType(int id) {
      this._id = id;
      this._characterList = new ConcurrentHashMap<>();
      this._minLvl = 0;
      this._maxLvl = 255;
      this._classType = 0;
      this._race = null;
      this._class = null;
      this._allowStore = true;
   }

   public int getId() {
      return this._id;
   }

   public void setParameter(String name, String value) {
      this._checkAffected = true;
      if (name.equals("name")) {
         this._name = value;
      } else if (name.equals("reflectionId")) {
         this._reflectionId = Integer.parseInt(value);
      } else if (name.equals("reflectionTemplateId")) {
         this._reflectionTemplateId = Integer.parseInt(value);
      } else if (name.equals("affectedLvlMin")) {
         this._minLvl = Integer.parseInt(value);
      } else if (name.equals("affectedLvlMax")) {
         this._maxLvl = Integer.parseInt(value);
      } else if (name.equals("affectedRace")) {
         if (this._race == null) {
            this._race = new int[1];
            this._race[0] = Integer.parseInt(value);
         } else {
            int[] temp = new int[this._race.length + 1];

            int i;
            for(i = 0; i < this._race.length; ++i) {
               temp[i] = this._race[i];
            }

            temp[i] = Integer.parseInt(value);
            this._race = temp;
         }
      } else if (name.equals("affectedClassId")) {
         if (this._class == null) {
            this._class = new int[1];
            this._class[0] = Integer.parseInt(value);
         } else {
            int[] temp = new int[this._class.length + 1];

            int i;
            for(i = 0; i < this._class.length; ++i) {
               temp[i] = this._class[i];
            }

            temp[i] = Integer.parseInt(value);
            this._class = temp;
         }
      } else if (name.equals("affectedClassType")) {
         if (value.equals("Fighter")) {
            this._classType = 1;
         } else {
            this._classType = 2;
         }
      } else if (name.equals("targetClass")) {
         this._target = Enum.valueOf(GameObject.InstanceType.class, value);
      } else if (name.equals("allowStore")) {
         this._allowStore = Boolean.parseBoolean(value);
      } else if (name.equals("allowStore")) {
         this._allowStore = Boolean.parseBoolean(value);
      } else if (name.equals("hpLimit")) {
         this._hpLimit = Double.parseDouble(value);
         if (this._hpLimit > 0.0) {
            this.addZoneId(ZoneId.HP_LIMIT);
         }
      } else if (name.equals("mpLimit")) {
         this._mpLimit = Double.parseDouble(value);
         if (this._mpLimit > 0.0) {
            this.addZoneId(ZoneId.MP_LIMIT);
         }
      } else if (name.equals("cpLimit")) {
         this._cpLimit = Double.parseDouble(value);
         if (this._cpLimit > 0.0) {
            this.addZoneId(ZoneId.CP_LIMIT);
         }
      } else if (name.equals("pAtkLimit")) {
         this._pAtkLimit = Double.parseDouble(value);
         if (this._pAtkLimit > 0.0) {
            this.addZoneId(ZoneId.P_ATK_LIMIT);
         }
      } else if (name.equals("pDefLimit")) {
         this._pDefLimit = Double.parseDouble(value);
         if (this._pDefLimit > 0.0) {
            this.addZoneId(ZoneId.P_DEF_LIMIT);
         }
      } else if (name.equals("pAtkSpeedLimit")) {
         this._pAtkSpeedLimit = Double.parseDouble(value);
         if (this._pAtkSpeedLimit > 0.0) {
            this.addZoneId(ZoneId.ATK_SPEED_LIMIT);
         }
      } else if (name.equals("mAtkLimit")) {
         this._mAtkLimit = Double.parseDouble(value);
         if (this._mAtkLimit > 0.0) {
            this.addZoneId(ZoneId.M_ATK_LIMIT);
         }
      } else if (name.equals("mDefLimit")) {
         this._mDefLimit = Double.parseDouble(value);
         if (this._mDefLimit > 0.0) {
            this.addZoneId(ZoneId.M_DEF_LIMIT);
         }
      } else if (name.equals("mAtkSpeedLimit")) {
         this._mAtkSpeedLimit = Double.parseDouble(value);
         if (this._mAtkSpeedLimit > 0.0) {
            this.addZoneId(ZoneId.M_ATK_SPEED_LIMIT);
         }
      } else if (name.equals("critDmgLimit")) {
         this._critDmgLimit = Double.parseDouble(value);
         if (this._critDmgLimit > 0.0) {
            this.addZoneId(ZoneId.CRIT_DMG_LIMIT);
         }
      } else if (name.equals("runSpeedLimit")) {
         this._runSpeedLimit = Double.parseDouble(value);
         if (this._runSpeedLimit > 0.0) {
            this.addZoneId(ZoneId.RUN_SPEED_LIMIT);
         }
      } else if (name.equals("walkSpeedLimit")) {
         this._walkSpeedLimit = Double.parseDouble(value);
         if (this._walkSpeedLimit > 0.0) {
            this.addZoneId(ZoneId.WALK_SPEED_LIMIT);
         }
      } else if (name.equals("accuracyLimit")) {
         this._accuracyLimit = Integer.parseInt(value);
         if (this._accuracyLimit > 0) {
            this.addZoneId(ZoneId.ACCURACY_LIMIT);
         }
      } else if (name.equals("critHitLimit")) {
         this._critHitLimit = Double.parseDouble(value);
         if (this._critHitLimit > 0.0) {
            this.addZoneId(ZoneId.CRIT_HIT_LIMIT);
         }
      } else if (name.equals("mCritHitLimit")) {
         this._mCritHitLimit = Double.parseDouble(value);
         if (this._mCritHitLimit > 0.0) {
            this.addZoneId(ZoneId.MCRIT_HIT_LIMIT);
         }
      } else if (name.equals("evasionLimit")) {
         this._evasionLimit = Integer.parseInt(value);
         if (this._evasionLimit > 0) {
            this.addZoneId(ZoneId.EVASION_LIMIT);
         }
      } else if (name.equals("pvpPhysSkillDmgLimit")) {
         this._pvpPhysSkillDmgLimit = Double.parseDouble(value);
         if (this._pvpPhysSkillDmgLimit > 0.0) {
            this.addZoneId(ZoneId.PVP_PHYS_SKILL_DMG_LIMIT);
         }
      } else if (name.equals("pvpPhysSkillDefLimit")) {
         this._pvpPhysSkillDefLimit = Double.parseDouble(value);
         if (this._pvpPhysSkillDefLimit > 0.0) {
            this.addZoneId(ZoneId.PVP_PHYS_SKILL_DEF_LIMIT);
         }
      } else if (name.equals("pvpPhysDefLimit")) {
         this._pvpPhysDefLimit = Double.parseDouble(value);
         if (this._pvpPhysDefLimit > 0.0) {
            this.addZoneId(ZoneId.PVP_PHYS_DEF_LIMIT);
         }
      } else if (name.equals("pvpPhysDmgLimit")) {
         this._pvpPhysDmgLimit = Double.parseDouble(value);
         if (this._pvpPhysDmgLimit > 0.0) {
            this.addZoneId(ZoneId.PVP_PHYS_DMG_LIMIT);
         }
      } else if (name.equals("pvpMagicDmgLimit")) {
         this._pvpMagicDmgLimit = Double.parseDouble(value);
         if (this._pvpMagicDmgLimit > 0.0) {
            this.addZoneId(ZoneId.PVP_MAGIC_DMG_LIMIT);
         }
      } else if (name.equals("pvpMagicDefLimit")) {
         this._pvpMagicDefLimit = Double.parseDouble(value);
         if (this._pvpMagicDefLimit > 0.0) {
            this.addZoneId(ZoneId.PVP_MAGIC_DEF_LIMIT);
         }
      } else {
         _log.info(this.getClass().getSimpleName() + ": Unknown parameter - " + name + " in zone: " + this.getId());
      }
   }

   private boolean isAffected(Creature character) {
      if (character.getLevel() < this._minLvl || character.getLevel() > this._maxLvl) {
         return false;
      } else if (!character.isInstanceType(this._target)) {
         return false;
      } else {
         if (character.isPlayer()) {
            if (this._classType != 0) {
               if (((Player)character).isMageClass()) {
                  if (this._classType == 1) {
                     return false;
                  }
               } else if (this._classType == 2) {
                  return false;
               }
            }

            if (this._race != null) {
               boolean ok = false;

               for(int element : this._race) {
                  if (((Player)character).getRace().ordinal() == element) {
                     ok = true;
                     break;
                  }
               }

               if (!ok) {
                  return false;
               }
            }

            if (this._class != null) {
               boolean ok = false;

               for(int _clas : this._class) {
                  if (((Player)character).getClassId().ordinal() == _clas) {
                     ok = true;
                     break;
                  }
               }

               if (!ok) {
                  return false;
               }
            }
         }

         return true;
      }
   }

   public void setZone(ZoneForm zone) {
      if (this._zone != null) {
         throw new IllegalStateException("Zone already set");
      } else {
         this._zone = zone;
      }
   }

   public ZoneForm getZone() {
      return this._zone;
   }

   public void setName(String name) {
      this._name = name;
   }

   public String getName() {
      return this._name;
   }

   public void setReflectionId(int instanceId) {
      this._reflectionId = instanceId;
   }

   public int getReflectionId() {
      return this._reflectionId;
   }

   public int getReflectionTemplateId() {
      return this._reflectionTemplateId;
   }

   public boolean isInsideZone(ILocational loc) {
      return this._zone.isInsideZone(loc.getX(), loc.getY(), loc.getZ());
   }

   public boolean isInsideZone(int x, int y) {
      return this._zone.isInsideZone(x, y, this._zone.getHighZ());
   }

   public boolean isInsideZone(int x, int y, int z) {
      return this._zone.isInsideZone(x, y, z);
   }

   public boolean isInsideZone(int x, int y, int z, int instanceId) {
      return this._reflectionId != 0 && instanceId != 0 && this._reflectionId != instanceId ? false : this._zone.isInsideZone(x, y, z);
   }

   public boolean isInsideZone(GameObject object) {
      return this.isInsideZone(object.getX(), object.getY(), object.getZ(), object.getReflectionId());
   }

   public double getDistanceToZone(int x, int y) {
      return this.getZone().getDistanceToZone(x, y);
   }

   public double getDistanceToZone(GameObject object) {
      return this.getZone().getDistanceToZone(object.getX(), object.getY());
   }

   public void revalidateInZone(Creature character) {
      if (!this._checkAffected || this.isAffected(character)) {
         if (this.isInsideZone((GameObject)character)) {
            if (!this._characterList.containsKey(character.getObjectId())) {
               List<Quest> quests = this.getQuestByEvent(Quest.QuestEventType.ON_ENTER_ZONE);
               if (quests != null) {
                  for(Quest quest : quests) {
                     quest.notifyEnterZone(character, this);
                  }
               }

               this._characterList.put(character.getObjectId(), character);
               this.onEnter(character);
               this._listeners.onEnter(character);
               if (character != null && character.isPlayer() && character.getActingPlayer().isGM()) {
                  character.sendMessage("Entered the zone " + this.getName());
               }
            }
         } else {
            this.removeCharacter(character);
         }
      }
   }

   public void removeCharacter(Creature character) {
      if (this._characterList.containsKey(character.getObjectId())) {
         List<Quest> quests = this.getQuestByEvent(Quest.QuestEventType.ON_EXIT_ZONE);
         if (quests != null) {
            for(Quest quest : quests) {
               quest.notifyExitZone(character, this);
            }
         }

         this._characterList.remove(character.getObjectId());
         this.onExit(character);
         this._listeners.onLeave(character);
         if (character != null && character.isPlayer() && character.getActingPlayer().isGM()) {
            character.sendMessage("Left the area " + this.getName());
         }
      }
   }

   public boolean isCharacterInZone(Creature character) {
      return this._characterList.containsKey(character.getObjectId());
   }

   public AbstractZoneSettings getSettings() {
      return this._settings;
   }

   public void setSettings(AbstractZoneSettings settings) {
      if (this._settings != null) {
         this._settings.clear();
      }

      this._settings = settings;
   }

   protected abstract void onEnter(Creature var1);

   protected abstract void onExit(Creature var1);

   public void onDieInside(Creature character) {
   }

   public void onReviveInside(Creature character) {
   }

   public void onPlayerLoginInside(Player player) {
   }

   public void onPlayerLogoutInside(Player player) {
   }

   public Map<Integer, Creature> getCharacters() {
      return this._characterList;
   }

   public Collection<Creature> getCharactersInside() {
      return this._characterList.values();
   }

   public List<Player> getPlayersInside() {
      List<Player> players = new ArrayList<>();

      for(Creature ch : this._characterList.values()) {
         if (ch != null && ch.isPlayer()) {
            players.add(ch.getActingPlayer());
         }
      }

      return players;
   }

   public void addQuestEvent(Quest.QuestEventType EventType, Quest q) {
      if (this._questEvents == null) {
         this._questEvents = new HashMap<>();
      }

      List<Quest> questByEvents = this._questEvents.get(EventType);
      if (questByEvents == null) {
         questByEvents = new ArrayList<>();
      }

      if (!questByEvents.contains(q)) {
         questByEvents.add(q);
      }

      this._questEvents.put(EventType, questByEvents);
   }

   public List<Quest> getQuestByEvent(Quest.QuestEventType EventType) {
      return this._questEvents == null ? null : this._questEvents.get(EventType);
   }

   public void broadcastPacket(GameServerPacket packet) {
      if (!this._characterList.isEmpty()) {
         for(Creature character : this._characterList.values()) {
            if (character != null && character.isPlayer()) {
               character.sendPacket(packet);
            }
         }
      }
   }

   public GameObject.InstanceType getTargetType() {
      return this._target;
   }

   public void setTargetType(GameObject.InstanceType type) {
      this._target = type;
      this._checkAffected = true;
   }

   public boolean getAllowStore() {
      return this._allowStore;
   }

   @Override
   public String toString() {
      return this.getClass().getSimpleName() + "[" + this._id + "]";
   }

   public void visualizeZone(int z) {
      this.getZone().visualizeZone(z);
   }

   public <T extends Listener<ZoneType>> boolean addListener(T listener) {
      return this._listeners.add(listener);
   }

   public <T extends Listener<ZoneType>> boolean removeListener(T listener) {
      return this._listeners.remove(listener);
   }

   public void generateReflection() {
      ReflectionTemplate template = ReflectionParser.getInstance().getReflectionId(this._reflectionTemplateId);
      if (template != null) {
         this._reflectionId = ReflectionManager.getInstance().createDynamicReflection(template).getId();
      }
   }

   public void setType(String type) {
      this._type = type;
   }

   public String getType() {
      return this._type;
   }

   public void addZoneId(ZoneId id) {
      this._zoneId.add(id);
   }

   public List<ZoneId> getZoneId() {
      return this._zoneId;
   }

   public double getHpLimit() {
      return this._hpLimit;
   }

   public double getMpLimit() {
      return this._mpLimit;
   }

   public double getCpLimit() {
      return this._cpLimit;
   }

   public double getPAtkLimit() {
      return this._pAtkLimit;
   }

   public double getPDefLimit() {
      return this._pDefLimit;
   }

   public double getAtkSpeedLimit() {
      return this._pAtkSpeedLimit;
   }

   public double getMAtkLimit() {
      return this._mAtkLimit;
   }

   public double getMDefLimit() {
      return this._mDefLimit;
   }

   public double getMAtkSpeedLimit() {
      return this._mAtkSpeedLimit;
   }

   public double getCritDmgLimit() {
      return this._critDmgLimit;
   }

   public double getRunSpeedLimit() {
      return this._runSpeedLimit;
   }

   public double getWalkSpeedLimit() {
      return this._walkSpeedLimit;
   }

   public int getAccuracyLimit() {
      return this._accuracyLimit;
   }

   public double getCritHitLimit() {
      return this._critHitLimit;
   }

   public double getMCritHitLimit() {
      return this._mCritHitLimit;
   }

   public int getEvasionLimit() {
      return this._evasionLimit;
   }

   public double getPvpPhysSkillDmgLimit() {
      return this._pvpPhysSkillDmgLimit;
   }

   public double getPvpPhysSkillDefLimit() {
      return this._pvpPhysSkillDefLimit;
   }

   public double getPvpPhysDefLimit() {
      return this._pvpPhysDefLimit;
   }

   public double getPvpPhysDmgLimit() {
      return this._pvpPhysDmgLimit;
   }

   public double getPvpMagicDmgLimit() {
      return this._pvpMagicDmgLimit;
   }

   public double getPvpMagicDefLimit() {
      return this._pvpMagicDefLimit;
   }

   public class ZoneListenerList extends ListenerList<ZoneType> {
      public void onEnter(Creature actor) {
         if (!this.getListeners().isEmpty()) {
            for(Listener<ZoneType> listener : this.getListeners()) {
               ((OnZoneEnterLeaveListener)listener).onZoneEnter(ZoneType.this, actor);
            }
         }
      }

      public void onLeave(Creature actor) {
         if (!this.getListeners().isEmpty()) {
            for(Listener<ZoneType> listener : this.getListeners()) {
               ((OnZoneEnterLeaveListener)listener).onZoneLeave(ZoneType.this, actor);
            }
         }
      }
   }
}
