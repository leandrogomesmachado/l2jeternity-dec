package l2e.gameserver.geodata.utils;

public final class LinePointIterator3D {
   private int _srcX;
   private int _srcY;
   private int _srcZ;
   private final int _dstX;
   private final int _dstY;
   private final int _dstZ;
   private final long _dx;
   private final long _dy;
   private final long _dz;
   private final long _sx;
   private final long _sy;
   private final long _sz;
   private long _error;
   private long _error2;
   private boolean _first;

   public LinePointIterator3D(int srcX, int srcY, int srcZ, int dstX, int dstY, int dstZ) {
      this._srcX = srcX;
      this._srcY = srcY;
      this._srcZ = srcZ;
      this._dstX = dstX;
      this._dstY = dstY;
      this._dstZ = dstZ;
      this._dx = Math.abs((long)dstX - (long)srcX);
      this._dy = Math.abs((long)dstY - (long)srcY);
      this._dz = Math.abs((long)dstZ - (long)srcZ);
      this._sx = srcX < dstX ? 1L : -1L;
      this._sy = srcY < dstY ? 1L : -1L;
      this._sz = srcZ < dstZ ? 1L : -1L;
      if (this._dx >= this._dy && this._dx >= this._dz) {
         this._error = this._error2 = this._dx / 2L;
      } else if (this._dy >= this._dx && this._dy >= this._dz) {
         this._error = this._error2 = this._dy / 2L;
      } else {
         this._error = this._error2 = this._dz / 2L;
      }

      this._first = true;
   }

   public boolean next() {
      if (this._first) {
         this._first = false;
         return true;
      } else {
         if (this._dx >= this._dy && this._dx >= this._dz) {
            if (this._srcX != this._dstX) {
               this._srcX = (int)((long)this._srcX + this._sx);
               this._error += this._dy;
               if (this._error >= this._dx) {
                  this._srcY = (int)((long)this._srcY + this._sy);
                  this._error -= this._dx;
               }

               this._error2 += this._dz;
               if (this._error2 >= this._dx) {
                  this._srcZ = (int)((long)this._srcZ + this._sz);
                  this._error2 -= this._dx;
               }

               return true;
            }
         } else if (this._dy >= this._dx && this._dy >= this._dz) {
            if (this._srcY != this._dstY) {
               this._srcY = (int)((long)this._srcY + this._sy);
               this._error += this._dx;
               if (this._error >= this._dy) {
                  this._srcX = (int)((long)this._srcX + this._sx);
                  this._error -= this._dy;
               }

               this._error2 += this._dz;
               if (this._error2 >= this._dy) {
                  this._srcZ = (int)((long)this._srcZ + this._sz);
                  this._error2 -= this._dy;
               }

               return true;
            }
         } else if (this._srcZ != this._dstZ) {
            this._srcZ = (int)((long)this._srcZ + this._sz);
            this._error += this._dx;
            if (this._error >= this._dz) {
               this._srcX = (int)((long)this._srcX + this._sx);
               this._error -= this._dz;
            }

            this._error2 += this._dy;
            if (this._error2 >= this._dz) {
               this._srcY = (int)((long)this._srcY + this._sy);
               this._error2 -= this._dz;
            }

            return true;
         }

         return false;
      }
   }

   public int x() {
      return this._srcX;
   }

   public int y() {
      return this._srcY;
   }

   public int z() {
      return this._srcZ;
   }
}
