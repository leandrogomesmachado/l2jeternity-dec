package l2e.gameserver.network.clientpackets;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.data.holder.CharNameHolder;
import l2e.gameserver.data.parser.CharTemplateParser;
import l2e.gameserver.data.parser.InitialShortcutParser;
import l2e.gameserver.data.parser.SkillTreesParser;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.instancemanager.QuestManager;
import l2e.gameserver.listener.events.PlayerEvent;
import l2e.gameserver.listener.player.PlayerListener;
import l2e.gameserver.model.Augmentation;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.SkillLearn;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.appearance.PcAppearance;
import l2e.gameserver.model.actor.stat.PcStat;
import l2e.gameserver.model.actor.templates.player.PcTemplate;
import l2e.gameserver.model.base.ClassId;
import l2e.gameserver.model.items.PcItemTemplate;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.olympiad.Olympiad;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.GameClient;
import l2e.gameserver.network.serverpackets.CharacterCreateFail;
import l2e.gameserver.network.serverpackets.CharacterCreateSuccess;
import l2e.gameserver.network.serverpackets.CharacterSelectionInfo;
import l2e.gameserver.network.serverpackets.NewCharacterFail;

public final class RequestCharacterCreate extends GameClientPacket {
   protected static final Logger _logAccounting = Logger.getLogger("accounting");
   private static final List<PlayerListener> _listeners = new LinkedList<>();
   private String _name;
   protected int _race;
   private byte _sex;
   private int _classId;
   protected int _int;
   protected int _str;
   protected int _con;
   protected int _men;
   protected int _dex;
   protected int _wit;
   private byte _hairStyle;
   private byte _hairColor;
   private byte _face;

   @Override
   protected void readImpl() {
      this._name = this.readS();
      this._race = this.readD();
      this._sex = (byte)this.readD();
      this._classId = this.readD();
      this._int = this.readD();
      this._str = this.readD();
      this._con = this.readD();
      this._men = this.readD();
      this._dex = this.readD();
      this._wit = this.readD();
      this._hairStyle = (byte)this.readD();
      this._hairColor = (byte)this.readD();
      this._face = (byte)this.readD();
   }

   @Override
   protected void runImpl() {
      if (this._name.length() >= 1 && this._name.length() <= 16) {
         if (Config.FORBIDDEN_NAMES.length > 1) {
            for(String st : Config.FORBIDDEN_NAMES) {
               if (this._name.toLowerCase().contains(st.toLowerCase())) {
                  this.sendPacket(new CharacterCreateFail(4));
                  return;
               }
            }
         }

         if (Util.isAlphaNumeric(this._name) && this.isValidName(this._name)) {
            if (this._face > 2 || this._face < 0) {
               _log.warning("Character Creation Failure: Character face " + this._face + " is invalid. Possible client hack. " + this.getClient());
               this.sendPacket(new CharacterCreateFail(0));
            } else if (this._hairStyle >= 0 && (this._sex != 0 || this._hairStyle <= 4) && (this._sex == 0 || this._hairStyle <= 6)) {
               if (this._hairColor <= 3 && this._hairColor >= 0) {
                  Player newChar = null;
                  PcTemplate template = null;
                  synchronized(CharNameHolder.getInstance()) {
                     if (CharNameHolder.getInstance().accountCharNumber(this.getClient().getLogin()) >= Config.MAX_CHARACTERS_NUMBER_PER_ACCOUNT
                        && Config.MAX_CHARACTERS_NUMBER_PER_ACCOUNT != 0) {
                        if (Config.DEBUG) {
                           _log.fine("Max number of characters reached. Creation failed.");
                        }

                        this.sendPacket(new CharacterCreateFail(1));
                        return;
                     }

                     if (CharNameHolder.getInstance().doesCharNameExist(this._name)) {
                        if (Config.DEBUG) {
                           _log.fine(
                              "Character Creation Failure: Message generated: You cannot create another character. Please delete the existing character and try again."
                           );
                        }

                        this.sendPacket(new CharacterCreateFail(2));
                        return;
                     }

                     template = CharTemplateParser.getInstance().getTemplate(this._classId);
                     if (template == null || ClassId.getClassId(this._classId).level() > 0) {
                        if (Config.DEBUG) {
                           _log.fine(
                              "Character Creation Failure: "
                                 + this._name
                                 + " classId: "
                                 + this._classId
                                 + " Template: "
                                 + template
                                 + " Message generated: Your character creation has failed."
                           );
                        }

                        this.sendPacket(new CharacterCreateFail(0));
                        return;
                     }

                     PcAppearance app = new PcAppearance(this._face, this._hairColor, this._hairStyle, this._sex != 0);
                     newChar = Player.create(template, this.getClient().getLogin(), this._name, app);
                  }

                  try {
                     newChar.setCurrentHp(newChar.getMaxHp());
                     newChar.setCurrentMp(newChar.getMaxMp());
                     this.sendPacket(new CharacterCreateSuccess());
                     this.initNewChar(this.getClient(), newChar);
                     LogRecord record = new LogRecord(Level.INFO, "Created new character");
                     record.setParameters(new Object[]{newChar, this.getClient()});
                     _logAccounting.log(record);
                  } catch (Exception var6) {
                     this.sendPacket(new NewCharacterFail());
                     _log.log(Level.WARNING, "Exception on create new char(): " + var6.getMessage(), (Throwable)var6);
                  }
               } else {
                  _log.warning(
                     "Character Creation Failure: Character hair color " + this._hairColor + " is invalid. Possible client hack. " + this.getClient()
                  );
                  this.sendPacket(new CharacterCreateFail(0));
               }
            } else {
               _log.warning("Character Creation Failure: Character hair style " + this._hairStyle + " is invalid. Possible client hack. " + this.getClient());
               this.sendPacket(new CharacterCreateFail(0));
            }
         } else {
            if (Config.DEBUG) {
               _log.fine("Character Creation Failure: Character name " + this._name + " is invalid. Message generated: Incorrect name. Please try again.");
            }

            this.sendPacket(new CharacterCreateFail(4));
         }
      } else {
         if (Config.DEBUG) {
            _log.fine(
               "Character Creation Failure: Character name "
                  + this._name
                  + " is invalid. Message generated: Your title cannot exceed 16 characters in length. Please try again."
            );
         }

         this.sendPacket(new CharacterCreateFail(3));
      }
   }

   private boolean isValidName(String text) {
      boolean result = true;

      Pattern pattern;
      try {
         pattern = Pattern.compile(Config.CNAME_TEMPLATE);
      } catch (PatternSyntaxException var6) {
         _log.warning("ERROR : Character name pattern of config is wrong!");
         pattern = Pattern.compile(".*");
      }

      Matcher regexp = pattern.matcher(text);
      if (!regexp.matches()) {
         result = false;
      }

      return result;
   }

   private void initNewChar(GameClient client, Player newChar) {
      if (Config.DEBUG) {
         _log.fine("Character init start");
      }

      World.getInstance().addObject(newChar);
      if (Config.STARTING_ADENA > 0L) {
         newChar.addAdena("Init", Config.STARTING_ADENA, null, false);
      }

      PcTemplate template = newChar.getTemplate();
      if (Config.ALLOW_NEW_CHAR_CUSTOM_POSITION) {
         newChar.setXYZInvisible(Config.NEW_CHAR_POSITION_X, Config.NEW_CHAR_POSITION_Y, Config.NEW_CHAR_POSITION_Z);
      } else {
         Location createLoc = template.getCreationPoint();
         newChar.setXYZInvisible(createLoc.getX(), createLoc.getY(), createLoc.getZ());
      }

      if (Config.ALLOW_NEW_CHARACTER_TITLE) {
         newChar.setTitle(Config.NEW_CHARACTER_TITLE);
      } else {
         newChar.setTitle("");
      }

      if (Config.NEW_CHAR_IS_NOBLE) {
         Olympiad.addNoble(newChar);
         newChar.setNoble(true);
      }

      if (Config.ENABLE_VITALITY) {
         newChar.setVitalityPoints(Math.min(Config.STARTING_VITALITY_POINTS, PcStat.MAX_VITALITY_POINTS), true);
      }

      if (Config.STARTING_LEVEL > 1) {
         newChar.getStat().addLevel((byte)(Config.STARTING_LEVEL - 1), true);
      }

      if (Config.STARTING_SP > 0) {
         newChar.getStat().addSp(Config.STARTING_SP);
      }

      if (template.hasInitialEquipment()) {
         for(PcItemTemplate ie : template.getInitialEquipment()) {
            ItemInstance item = newChar.getInventory().addItem("Multisell", ie.getId(), ie.getCount(), newChar, null);
            if (item != null && !item.isStackable()) {
               if (ie.getAugmentId() > 0) {
                  item.setAugmentation(new Augmentation(ie.getAugmentId()));
               }

               if (ie.getElementals() != null && !ie.getElementals().isEmpty()) {
                  String[] elements = ie.getElementals().split(";");

                  for(String el : elements) {
                     String[] element = el.split(":");
                     if (element != null) {
                        item.setElementAttr(Byte.parseByte(element[0]), Integer.parseInt(element[1]));
                     }
                  }
               }

               if (ie.getDurability() > 0) {
                  item.setTime((long)ie.getDurability());
               }

               item.setEnchantLevel(ie.getEnchant());
               item.updateDatabase();
               if (item.isEquipable() && ie.isEquipped()) {
                  newChar.getInventory().equipItem(item);
               }
            }
         }
      }

      for(SkillLearn skill : SkillTreesParser.getInstance().getAvailableSkills(newChar, newChar.getClassId(), false, true)) {
         newChar.addSkill(SkillsParser.getInstance().getInfo(skill.getId(), skill.getLvl()), true);
         if (Config.DEBUG) {
            _log.fine("Adding starter skill:" + skill.getId() + " / " + skill.getLvl());
         }
      }

      InitialShortcutParser.getInstance().registerAllShortcuts(newChar);
      if (!Config.DISABLE_TUTORIAL) {
         this.startTutorialQuest(newChar);
      }

      PlayerEvent event = new PlayerEvent();
      event.setObjectId(newChar.getObjectId());
      event.setName(newChar.getName());
      event.setClient(client);
      this.firePlayerListener(event);
      newChar.setOnlineStatus(true, false);
      newChar.deleteMe();
      CharacterSelectionInfo cl = new CharacterSelectionInfo(client.getLogin(), client.getSessionId().playOkID1);
      client.sendPacket(cl);
      client.setCharSelection(cl.getCharInfo());
      if (Config.DEBUG) {
         _log.fine("Character init end");
      }
   }

   public void startTutorialQuest(Player player) {
      QuestState qs = player.getQuestState("_255_Tutorial");
      Quest q = null;
      if (qs == null) {
         q = QuestManager.getInstance().getQuest("_255_Tutorial");
      }

      if (q != null) {
         q.newQuestState(player).setState((byte)1);
      }
   }

   private void firePlayerListener(PlayerEvent event) {
      for(PlayerListener listener : _listeners) {
         listener.onCharCreate(event);
      }
   }

   public static void addPlayerListener(PlayerListener listener) {
      if (!_listeners.contains(listener)) {
         _listeners.add(listener);
      }
   }

   public static void removePlayerListener(PlayerListener listener) {
      _listeners.remove(listener);
   }
}
