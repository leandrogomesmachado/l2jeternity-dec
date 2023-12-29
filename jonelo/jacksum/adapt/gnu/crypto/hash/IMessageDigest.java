package jonelo.jacksum.adapt.gnu.crypto.hash;

public interface IMessageDigest extends Cloneable {
   String name();

   int hashSize();

   int blockSize();

   void update(byte var1);

   void update(byte[] var1, int var2, int var3);

   byte[] digest();

   void reset();

   boolean selfTest();

   Object clone();
}
