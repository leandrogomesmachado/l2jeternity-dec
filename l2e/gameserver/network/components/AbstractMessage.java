package l2e.gameserver.network.components;

import java.io.PrintStream;
import java.util.Arrays;
import l2e.gameserver.data.parser.ItemsParser;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.Summon;
import l2e.gameserver.model.actor.instance.DoorInstance;
import l2e.gameserver.model.actor.templates.items.Item;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.GameServerPacket;

public abstract class AbstractMessage<T extends AbstractMessage<?>> extends GameServerPacket {
   private static final AbstractMessage.SMParam[] EMPTY_PARAM_ARRAY = new AbstractMessage.SMParam[0];
   private static final byte TYPE_SYSTEM_STRING = 13;
   private static final byte TYPE_PLAYER_NAME = 12;
   private static final byte TYPE_DOOR_NAME = 11;
   private static final byte TYPE_INSTANCE_NAME = 10;
   private static final byte TYPE_ELEMENT_NAME = 9;
   private static final byte TYPE_ZONE_NAME = 7;
   private static final byte TYPE_LONG_NUMBER = 6;
   private static final byte TYPE_CASTLE_NAME = 5;
   private static final byte TYPE_SKILL_NAME = 4;
   private static final byte TYPE_ITEM_NAME = 3;
   private static final byte TYPE_NPC_NAME = 2;
   private static final byte TYPE_INT_NUMBER = 1;
   private static final byte TYPE_TEXT = 0;
   private AbstractMessage.SMParam[] _params;
   private final SystemMessageId _smId;
   private int _paramIndex;

   public AbstractMessage(SystemMessageId smId) {
      if (smId == null) {
         throw new NullPointerException("SystemMessageId cannot be null!");
      } else {
         this._smId = smId;
         this._params = smId.getParamCount() > 0 ? new AbstractMessage.SMParam[smId.getParamCount()] : EMPTY_PARAM_ARRAY;
      }
   }

   public final int getId() {
      return this._smId.getId();
   }

   public final SystemMessageId getSystemMessageId() {
      return this._smId;
   }

   private final void append(AbstractMessage.SMParam param) {
      if (this._paramIndex >= this._params.length) {
         this._params = Arrays.copyOf(this._params, this._paramIndex + 1);
         this._smId.setParamCount(this._paramIndex + 1);
         _log.info("Wrong parameter count '" + (this._paramIndex + 1) + "' for SystemMessageId: " + this._smId);
      }

      this._params[this._paramIndex++] = param;
   }

   public final T addString(String text) {
      this.append(new AbstractMessage.SMParam((byte)0, text));
      return (T)this;
   }

   public final T addCastleId(int number) {
      this.append(new AbstractMessage.SMParam((byte)5, number));
      return (T)this;
   }

   public final T addInt(int number) {
      this.append(new AbstractMessage.SMParam((byte)1, number));
      return (T)this;
   }

   public final T addLong(long number) {
      this.append(new AbstractMessage.SMParam((byte)6, number));
      return (T)this;
   }

   public final T addNumber(int number) {
      this.append(new AbstractMessage.SMParam((byte)1, number));
      return (T)this;
   }

   public final T addItemNumber(long number) {
      this.append(new AbstractMessage.SMParam((byte)6, number));
      return (T)this;
   }

   public final T addCharName(Creature cha) {
      if (cha.isNpc()) {
         Npc npc = (Npc)cha;
         return this.addNpcName(npc);
      } else if (cha.isPlayer()) {
         return this.addPcName(cha.getActingPlayer());
      } else if (cha.isSummon()) {
         Summon summon = (Summon)cha;
         return this.addNpcName(summon);
      } else if (cha.isDoor()) {
         DoorInstance door = (DoorInstance)cha;
         return this.addDoorName(door.getId());
      } else {
         return this.addString(cha.getName());
      }
   }

   public final T addPcName(Player pc) {
      this.append(new AbstractMessage.SMParam((byte)12, pc.getAppearance().getVisibleName()));
      return (T)this;
   }

   public final T addDoorName(int doorId) {
      this.append(new AbstractMessage.SMParam((byte)11, doorId));
      return (T)this;
   }

   public final T addNpcName(Npc npc) {
      return this.addNpcName(npc.getTemplate());
   }

   public final T addNpcName(Summon npc) {
      return this.addNpcName(npc.getId());
   }

   public final T addNpcName(NpcTemplate template) {
      return (T)(template.isCustom() ? this.addString(template.getName()) : this.addNpcName(template.getId()));
   }

   public final T addNpcName(int id) {
      this.append(new AbstractMessage.SMParam((byte)2, 1000000 + id));
      return (T)this;
   }

   public T addItemName(ItemInstance item) {
      return this.addItemName(item.getId());
   }

   public T addItemName(Item item) {
      return this.addItemName(item.getId());
   }

   public final T addItemName(int id) {
      Item item = ItemsParser.getInstance().getTemplate(id);
      if (item != null && item.getDisplayId() != id) {
         return this.addString(item.getNameEn());
      } else {
         this.append(new AbstractMessage.SMParam((byte)3, id));
         return (T)this;
      }
   }

   public final T addZoneName(int x, int y, int z) {
      this.append(new AbstractMessage.SMParam((byte)7, new int[]{x, y, z}));
      return (T)this;
   }

   public final T addSkillName(Effect effect) {
      return this.addSkillName(effect.getSkill());
   }

   public final T addSkillName(Skill skill) {
      return (T)(skill.getId() != skill.getDisplayId() ? this.addString(skill.getNameEn()) : this.addSkillName(skill.getId(), skill.getLevel()));
   }

   public final T addSkillName(int id) {
      return this.addSkillName(id, 1);
   }

   public final T addSkillName(int id, int lvl) {
      this.append(new AbstractMessage.SMParam((byte)4, new int[]{id, lvl}));
      return (T)this;
   }

   public final T addElemental(int type) {
      this.append(new AbstractMessage.SMParam((byte)9, type));
      return (T)this;
   }

   public final T addSystemString(int type) {
      this.append(new AbstractMessage.SMParam((byte)13, type));
      return (T)this;
   }

   public final T addInstanceName(int type) {
      this.append(new AbstractMessage.SMParam((byte)10, type));
      return (T)this;
   }

   protected final void writeInfo() {
      this.writeD(this.getId());
      this.writeD(this._params.length);

      for(int i = 0; i < this._paramIndex; ++i) {
         AbstractMessage.SMParam param = this._params[i];
         this.writeD(param.getType());
         switch(param.getType()) {
            case 0:
            case 12:
               this.writeS(param.getStringValue());
               break;
            case 1:
            case 2:
            case 3:
            case 5:
            case 9:
            case 10:
            case 11:
            case 13:
               this.writeD(param.getIntValue());
               break;
            case 4: {
               int[] array = param.getIntArrayValue();
               this.writeD(array[0]);
               this.writeD(array[1]);
               break;
            }
            case 6:
               this.writeQ(param.getLongValue());
               break;
            case 7: {
               int[] array = param.getIntArrayValue();
               this.writeD(array[0]);
               this.writeD(array[1]);
               this.writeD(array[2]);
            }
            case 8:
         }
      }
   }

   public final void printMe(PrintStream out) {
      out.println(98);
      out.println(this.getId());
      out.println(this._params.length);

      for(AbstractMessage.SMParam param : this._params) {
         switch(param.getType()) {
            case 0:
            case 12:
               out.println(param.getStringValue());
               break;
            case 1:
            case 2:
            case 3:
            case 5:
            case 9:
            case 10:
            case 11:
            case 13:
               out.println(param.getIntValue());
               break;
            case 4: {
               int[] array = param.getIntArrayValue();
               out.println(array[0]);
               out.println(array[1]);
               break;
            }
            case 6:
               out.println(param.getLongValue());
               break;
            case 7: {
               int[] array = param.getIntArrayValue();
               out.println(array[0]);
               out.println(array[1]);
               out.println(array[2]);
            }
            case 8:
         }
      }
   }

   private static final class SMParam {
      private final byte _type;
      private final Object _value;

      public SMParam(byte type, Object value) {
         this._type = type;
         this._value = value;
      }

      public final byte getType() {
         return this._type;
      }

      public final String getStringValue() {
         return (String)this._value;
      }

      public final int getIntValue() {
         return this._value;
      }

      public final long getLongValue() {
         return this._value;
      }

      public final int[] getIntArrayValue() {
         return (int[])this._value;
      }
   }
}
