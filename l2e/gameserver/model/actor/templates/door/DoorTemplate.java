package l2e.gameserver.model.actor.templates.door;

import l2e.commons.geometry.Polygon;
import l2e.gameserver.model.actor.templates.character.CharTemplate;
import l2e.gameserver.model.interfaces.IIdentifiable;
import l2e.gameserver.model.stats.StatsSet;

public class DoorTemplate extends CharTemplate implements IIdentifiable {
   public final int doorId;
   public final int[] nodeX;
   public final int[] nodeY;
   public final int nodeZ;
   public final int height;
   public final int posX;
   public final int posY;
   public final int posZ;
   public final int emmiter;
   public final int childDoorId;
   public final String name;
   public final String groupName;
   public final boolean showHp;
   public final boolean isWall;
   public final byte masterDoorClose;
   public final byte masterDoorOpen;
   private final Polygon _polygon;

   public DoorTemplate(StatsSet set) {
      super(set);
      this.doorId = set.getInteger("id");
      this.name = set.getString("name");
      String[] pos = set.getString("pos").split(";");
      this.posX = Integer.parseInt(pos[0]);
      this.posY = Integer.parseInt(pos[1]);
      this.posZ = Integer.parseInt(pos[2]);
      this.height = set.getInteger("height");
      this.nodeZ = set.getInteger("nodeZ");
      this.nodeX = new int[4];
      this.nodeY = new int[4];

      for(int i = 0; i < 4; ++i) {
         String[] split = set.getString("node" + (i + 1)).split(",");
         this.nodeX[i] = Integer.parseInt(split[0]);
         this.nodeY[i] = Integer.parseInt(split[1]);
      }

      this.emmiter = set.getInteger("emitter_id", 0);
      this.showHp = set.getBool("hp_showable", true);
      this.isWall = set.getBool("is_wall", false);
      this.groupName = set.getString("group", null);
      this.childDoorId = set.getInteger("child_id_event", -1);
      String masterevent = set.getString("master_close_event", "act_nothing");
      if (masterevent.equals("act_open")) {
         this.masterDoorClose = 1;
      } else if (masterevent.equals("act_close")) {
         this.masterDoorClose = -1;
      } else {
         this.masterDoorClose = 0;
      }

      masterevent = set.getString("master_open_event", "act_nothing");
      if (masterevent.equals("act_open")) {
         this.masterDoorOpen = 1;
      } else if (masterevent.equals("act_close")) {
         this.masterDoorOpen = -1;
      } else {
         this.masterDoorOpen = 0;
      }

      String[] pos1 = set.getString("node1").split(",");
      String[] pos2 = set.getString("node2").split(",");
      String[] pos3 = set.getString("node3").split(",");
      String[] pos4 = set.getString("node4").split(",");
      Polygon shape = new Polygon();
      if (pos1 != null) {
         shape.add(Integer.parseInt(pos1[0]), Integer.parseInt(pos1[1]));
         shape.add(Integer.parseInt(pos2[0]), Integer.parseInt(pos2[1]));
         shape.add(Integer.parseInt(pos3[0]), Integer.parseInt(pos3[1]));
         shape.add(Integer.parseInt(pos4[0]), Integer.parseInt(pos4[1]));
      }

      shape.setZmin(set.getInteger("nodeZ"));
      shape.setZmax(set.getInteger("nodeZ") + this.height);
      this._polygon = shape;
   }

   @Override
   public int getId() {
      return this.doorId;
   }

   public Polygon getPolygon() {
      return this._polygon;
   }
}
