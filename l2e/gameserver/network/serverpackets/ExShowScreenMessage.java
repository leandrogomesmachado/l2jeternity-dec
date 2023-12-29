package l2e.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.SystemMessageId;

public class ExShowScreenMessage extends GameServerPacket {
   private final int _type;
   private final int _sysMessageId;
   private final int _unk1;
   private final int _unk2;
   private final int _unk3;
   private final boolean _fade;
   private final int _size;
   private final int _position;
   private boolean _effect;
   private final String _text;
   private final int _time;
   private final int _npcString;
   private List<String> _parameters = null;
   public static final byte TOP_LEFT = 1;
   public static final byte TOP_CENTER = 2;
   public static final byte TOP_RIGHT = 3;
   public static final byte MIDDLE_LEFT = 4;
   public static final byte MIDDLE_CENTER = 5;
   public static final byte MIDDLE_RIGHT = 6;
   public static final byte BOTTOM_CENTER = 7;
   public static final byte BOTTOM_RIGHT = 8;

   public ExShowScreenMessage(String text, int time) {
      this._type = 2;
      this._sysMessageId = -1;
      this._unk1 = 0;
      this._unk2 = 0;
      this._unk3 = 0;
      this._fade = false;
      this._position = 2;
      this._text = text;
      this._time = time;
      this._size = 0;
      this._effect = false;
      this._npcString = -1;
   }

   public ExShowScreenMessage(String text, int time, byte text_align, boolean big_font) {
      this._type = 2;
      this._sysMessageId = -1;
      this._unk1 = 0;
      this._unk2 = 0;
      this._unk3 = 0;
      this._fade = false;
      this._position = text_align;
      this._text = text;
      this._time = time;
      this._size = big_font ? 0 : 1;
      this._effect = false;
      this._npcString = -1;
   }

   public ExShowScreenMessage(NpcStringId npcString, int position, int time, String... params) {
      this._type = 2;
      this._sysMessageId = -1;
      this._unk1 = 0;
      this._unk2 = 0;
      this._unk3 = 0;
      this._fade = false;
      this._position = position;
      this._text = null;
      this._time = time;
      this._size = 0;
      this._effect = false;
      this._npcString = npcString.getId();
      if (params != null) {
         this.addStringParameter(params);
      }
   }

   public ExShowScreenMessage(NpcStringId npcString, int position, int size, int time, String... params) {
      this._type = 2;
      this._sysMessageId = -1;
      this._unk1 = 0;
      this._unk2 = 0;
      this._unk3 = 0;
      this._fade = false;
      this._position = position;
      this._text = null;
      this._time = time;
      this._size = size;
      this._effect = false;
      this._npcString = npcString.getId();
      if (params != null) {
         this.addStringParameter(params);
      }
   }

   public ExShowScreenMessage(SystemMessageId systemMsg, int position, int time, String... params) {
      this._type = 2;
      this._sysMessageId = systemMsg.getId();
      this._unk1 = 0;
      this._unk2 = 0;
      this._unk3 = 0;
      this._fade = false;
      this._position = position;
      this._text = null;
      this._time = time;
      this._size = 0;
      this._effect = false;
      this._npcString = -1;
      if (params != null) {
         this.addStringParameter(params);
      }
   }

   public ExShowScreenMessage(
      int type,
      int messageId,
      int position,
      int unk1,
      int size,
      int unk2,
      int unk3,
      boolean showEffect,
      int time,
      boolean fade,
      String text,
      NpcStringId npcString
   ) {
      this._type = type;
      this._sysMessageId = messageId;
      this._unk1 = unk1;
      this._unk2 = unk2;
      this._unk3 = unk3;
      this._fade = fade;
      this._position = position;
      this._text = text;
      this._time = time;
      this._size = size;
      this._effect = showEffect;
      this._npcString = npcString.getId();
   }

   public ExShowScreenMessage(
      int type,
      int messageId,
      int position,
      int unk1,
      int size,
      int unk2,
      int unk3,
      boolean showEffect,
      int time,
      boolean fade,
      String text,
      NpcStringId npcString,
      String params
   ) {
      this._type = type;
      this._sysMessageId = messageId;
      this._unk1 = unk1;
      this._unk2 = unk2;
      this._unk3 = unk3;
      this._fade = fade;
      this._position = position;
      this._text = text;
      this._time = time;
      this._size = size;
      this._effect = showEffect;
      this._npcString = npcString.getId();
   }

   public ExShowScreenMessage(
      int type, int messageId, int position, int unk1, int size, int unk2, int unk3, boolean showEffect, int time, boolean fade, String text
   ) {
      this._type = type;
      this._sysMessageId = messageId;
      this._unk1 = unk1;
      this._unk2 = unk2;
      this._unk3 = unk3;
      this._fade = fade;
      this._position = position;
      this._text = text;
      this._time = time;
      this._size = size;
      this._effect = showEffect;
      this._npcString = -1;
   }

   public void addStringParameter(String... params) {
      if (this._parameters == null) {
         this._parameters = new ArrayList<>();
      }

      for(String param : params) {
         this._parameters.add(param);
      }
   }

   public ExShowScreenMessage(
      int type,
      int messageId,
      int position,
      int unk1,
      int size,
      int unk2,
      int unk3,
      boolean showEffect,
      int time,
      boolean fade,
      String text,
      int npcString,
      String... params
   ) {
      this(type, messageId, position, unk1, size, unk2, unk3, showEffect, time, fade, text, npcString);
      this._parameters = Arrays.asList(params);
   }

   public ExShowScreenMessage(
      int type, int messageId, int position, int unk1, int size, int unk2, int unk3, boolean showEffect, int time, boolean fade, String text, int npcString
   ) {
      this._type = type;
      this._sysMessageId = messageId;
      this._unk1 = unk1;
      this._unk2 = unk2;
      this._unk3 = unk3;
      this._fade = fade;
      this._position = position;
      this._text = text;
      this._time = time;
      this._size = size;
      this._effect = showEffect;
      this._npcString = npcString;
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._type);
      this.writeD(this._sysMessageId);
      this.writeD(this._position);
      this.writeD(this._unk1);
      this.writeD(this._size);
      this.writeD(this._unk2);
      this.writeD(this._unk3);
      this.writeD(this._effect ? 1 : 0);
      this.writeD(this._time);
      this.writeD(this._fade ? 1 : 0);
      this.writeD(this._npcString);
      if (this._npcString == -1) {
         this.writeS(this._text);
      } else if (this._parameters != null) {
         for(String s : this._parameters) {
            this.writeS(s);
         }
      }
   }

   public ExShowScreenMessage setUpperEffect(boolean value) {
      this._effect = value;
      return this;
   }
}
