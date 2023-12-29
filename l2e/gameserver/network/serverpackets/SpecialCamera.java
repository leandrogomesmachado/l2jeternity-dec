package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.actor.Creature;

public class SpecialCamera extends GameServerPacket {
   private final int _id;
   private final int _force;
   private final int _angle1;
   private final int _angle2;
   private final int _time;
   private final int _duration;
   private final int _relYaw;
   private final int _relPitch;
   private final int _isWide;
   private final int _relAngle;
   private final int _unk;

   public SpecialCamera(
      Creature creature, int force, int angle1, int angle2, int time, int range, int duration, int relYaw, int relPitch, int isWide, int relAngle
   ) {
      this(creature, force, angle1, angle2, time, duration, range, relYaw, relPitch, isWide, relAngle, 0);
   }

   public SpecialCamera(
      Creature creature, Creature talker, int force, int angle1, int angle2, int time, int duration, int relYaw, int relPitch, int isWide, int relAngle
   ) {
      this(creature, force, angle1, angle2, time, duration, 0, relYaw, relPitch, isWide, relAngle, 0);
   }

   public SpecialCamera(int id, int dist, int yaw, int pitch, int time, int duration, int turn, int rise, int widescreen, int heading) {
      this(id, dist, yaw, pitch, time, duration, turn, rise, widescreen, heading, 0);
   }

   public SpecialCamera(int id, int dist, int yaw, int pitch, int time, int duration, int turn, int rise, int widescreen, int heading, int unkonown) {
      this._id = id;
      this._force = dist;
      this._angle1 = yaw;
      this._angle2 = pitch;
      this._time = time;
      this._duration = duration;
      this._relYaw = turn;
      this._relPitch = rise;
      this._isWide = widescreen;
      this._relAngle = heading;
      this._unk = unkonown;
   }

   public SpecialCamera(int id, int force, int angle1, int angle2, int time, int duration) {
      this._id = id;
      this._force = force;
      this._angle1 = angle1;
      this._angle2 = angle2;
      this._time = time;
      this._duration = duration;
      this._relYaw = 0;
      this._relPitch = 0;
      this._isWide = 0;
      this._relAngle = 0;
      this._unk = 0;
   }

   public SpecialCamera(Creature creature, int force, int angle1, int angle2, int time, int duration) {
      this._id = creature.getObjectId();
      this._force = force;
      this._angle1 = angle1;
      this._angle2 = angle2;
      this._time = time;
      this._duration = duration;
      this._relYaw = 0;
      this._relPitch = 0;
      this._isWide = 0;
      this._relAngle = 0;
      this._unk = 0;
   }

   public SpecialCamera(
      Creature creature, int force, int angle1, int angle2, int time, int range, int duration, int relYaw, int relPitch, int isWide, int relAngle, int unk
   ) {
      this._id = creature.getObjectId();
      this._force = force;
      this._angle1 = angle1;
      this._angle2 = angle2;
      this._time = time;
      this._duration = duration;
      this._relYaw = relYaw;
      this._relPitch = relPitch;
      this._isWide = isWide;
      this._relAngle = relAngle;
      this._unk = unk;
   }

   @Override
   public void writeImpl() {
      this.writeD(this._id);
      this.writeD(this._force);
      this.writeD(this._angle1);
      this.writeD(this._angle2);
      this.writeD(this._time);
      this.writeD(this._duration);
      this.writeD(this._relYaw);
      this.writeD(this._relPitch);
      this.writeD(this._isWide);
      this.writeD(this._relAngle);
      this.writeD(this._unk);
   }
}
