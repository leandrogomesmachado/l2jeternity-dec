package l2e.gameserver.network.serverpackets;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import l2e.gameserver.model.interfaces.ILocational;

public class ExServerPrimitive extends GameServerPacket {
   private final String _name;
   private final int _x;
   private final int _y;
   private final int _z;
   private final List<ExServerPrimitive.Point> _points = new ArrayList<>();
   private final List<ExServerPrimitive.Line> _lines = new ArrayList<>();

   public ExServerPrimitive(String name, int x, int y, int z) {
      this._name = name;
      this._x = x;
      this._y = y;
      this._z = z;
   }

   public ExServerPrimitive(String name, ILocational locational) {
      this(name, locational.getX(), locational.getY(), locational.getZ());
   }

   public void addPoint(String name, int color, boolean isNameColored, int x, int y, int z) {
      this._points.add(new ExServerPrimitive.Point(name, color, isNameColored, x, y, z));
   }

   public void addPoint(String name, int color, boolean isNameColored, ILocational locational) {
      this.addPoint(name, color, isNameColored, locational.getX(), locational.getY(), locational.getZ());
   }

   public void addPoint(int color, int x, int y, int z) {
      this.addPoint("", color, false, x, y, z);
   }

   public void addPoint(int color, ILocational locational) {
      this.addPoint("", color, false, locational);
   }

   public void addPoint(String name, Color color, boolean isNameColored, int x, int y, int z) {
      this.addPoint(name, color.getRGB(), isNameColored, x, y, z);
   }

   public void addPoint(String name, Color color, boolean isNameColored, ILocational locational) {
      this.addPoint(name, color.getRGB(), isNameColored, locational);
   }

   public void addPoint(Color color, int x, int y, int z) {
      this.addPoint("", color, false, x, y, z);
   }

   public void addPoint(Color color, ILocational locational) {
      this.addPoint("", color, false, locational);
   }

   public void addLine(String name, int color, boolean isNameColored, int x, int y, int z, int x2, int y2, int z2) {
      this._lines.add(new ExServerPrimitive.Line(name, color, isNameColored, x, y, z, x2, y2, z2));
   }

   public void addLine(String name, int color, boolean isNameColored, ILocational locational, int x2, int y2, int z2) {
      this.addLine(name, color, isNameColored, locational.getX(), locational.getY(), locational.getZ(), x2, y2, z2);
   }

   public void addLine(String name, int color, boolean isNameColored, int x, int y, int z, ILocational locational2) {
      this.addLine(name, color, isNameColored, x, y, z, locational2.getX(), locational2.getY(), locational2.getZ());
   }

   public void addLine(String name, int color, boolean isNameColored, ILocational locational, ILocational locational2) {
      this.addLine(name, color, isNameColored, locational, locational2.getX(), locational2.getY(), locational2.getZ());
   }

   public void addLine(int color, int x, int y, int z, int x2, int y2, int z2) {
      this.addLine("", color, false, x, y, z, x2, y2, z2);
   }

   public void addLine(int color, ILocational locational, int x2, int y2, int z2) {
      this.addLine("", color, false, locational, x2, y2, z2);
   }

   public void addLine(int color, int x, int y, int z, ILocational locational2) {
      this.addLine("", color, false, x, y, z, locational2);
   }

   public void addLine(int color, ILocational locational, ILocational locational2) {
      this.addLine("", color, false, locational, locational2);
   }

   public void addLine(String name, Color color, boolean isNameColored, int x, int y, int z, int x2, int y2, int z2) {
      this.addLine(name, color.getRGB(), isNameColored, x, y, z, x2, y2, z2);
   }

   public void addLine(String name, Color color, boolean isNameColored, ILocational locational, int x2, int y2, int z2) {
      this.addLine(name, color.getRGB(), isNameColored, locational, x2, y2, z2);
   }

   public void addLine(String name, Color color, boolean isNameColored, int x, int y, int z, ILocational locational2) {
      this.addLine(name, color.getRGB(), isNameColored, x, y, z, locational2);
   }

   public void addLine(String name, Color color, boolean isNameColored, ILocational locational, ILocational locational2) {
      this.addLine(name, color.getRGB(), isNameColored, locational, locational2);
   }

   public void addLine(Color color, int x, int y, int z, int x2, int y2, int z2) {
      this.addLine("", color, false, x, y, z, x2, y2, z2);
   }

   public void addLine(Color color, ILocational locational, int x2, int y2, int z2) {
      this.addLine("", color, false, locational, x2, y2, z2);
   }

   public void addLine(Color color, int x, int y, int z, ILocational locational2) {
      this.addLine("", color, false, x, y, z, locational2);
   }

   public void addLine(Color color, ILocational locational, ILocational locational2) {
      this.addLine("", color, false, locational, locational2);
   }

   @Override
   protected final void writeImpl() {
      this.writeS(this._name);
      this.writeD(this._x);
      this.writeD(this._y);
      this.writeD(this._z);
      this.writeD(65535);
      this.writeD(65535);
      this.writeD(this._points.size() + this._lines.size());

      for(ExServerPrimitive.Point point : this._points) {
         this.writeC(1);
         this.writeS(point.getName());
         int color = point.getColor();
         this.writeD(color >> 16 & 0xFF);
         this.writeD(color >> 8 & 0xFF);
         this.writeD(color & 0xFF);
         this.writeD(point.isNameColored() ? 1 : 0);
         this.writeD(point.getX());
         this.writeD(point.getY());
         this.writeD(point.getZ());
      }

      for(ExServerPrimitive.Line line : this._lines) {
         this.writeC(2);
         this.writeS(line.getName());
         int color = line.getColor();
         this.writeD(color >> 16 & 0xFF);
         this.writeD(color >> 8 & 0xFF);
         this.writeD(color & 0xFF);
         this.writeD(line.isNameColored() ? 1 : 0);
         this.writeD(line.getX());
         this.writeD(line.getY());
         this.writeD(line.getZ());
         this.writeD(line.getX2());
         this.writeD(line.getY2());
         this.writeD(line.getZ2());
      }
   }

   private static class Line extends ExServerPrimitive.Point {
      private final int _x2;
      private final int _y2;
      private final int _z2;

      public Line(String name, int color, boolean isNameColored, int x, int y, int z, int x2, int y2, int z2) {
         super(name, color, isNameColored, x, y, z);
         this._x2 = x2;
         this._y2 = y2;
         this._z2 = z2;
      }

      public int getX2() {
         return this._x2;
      }

      public int getY2() {
         return this._y2;
      }

      public int getZ2() {
         return this._z2;
      }
   }

   private static class Point {
      private final String _name;
      private final int _color;
      private final boolean _isNameColored;
      private final int _x;
      private final int _y;
      private final int _z;

      public Point(String name, int color, boolean isNameColored, int x, int y, int z) {
         this._name = name;
         this._color = color;
         this._isNameColored = isNameColored;
         this._x = x;
         this._y = y;
         this._z = z;
      }

      public String getName() {
         return this._name;
      }

      public int getColor() {
         return this._color;
      }

      public boolean isNameColored() {
         return this._isNameColored;
      }

      public int getX() {
         return this._x;
      }

      public int getY() {
         return this._y;
      }

      public int getZ() {
         return this._z;
      }
   }
}
