package l2e.gameserver.model.interfaces;

public interface ILocational {
   int getX();

   int getY();

   int getZ();

   int getHeading();

   ILocational getLocation();
}
