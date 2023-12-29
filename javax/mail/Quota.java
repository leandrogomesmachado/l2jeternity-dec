package javax.mail;

public class Quota {
   public String quotaRoot;
   public Quota.Resource[] resources;

   public Quota(String quotaRoot) {
      this.quotaRoot = quotaRoot;
   }

   public void setResourceLimit(String name, long limit) {
      if (this.resources == null) {
         this.resources = new Quota.Resource[1];
         this.resources[0] = new Quota.Resource(name, 0L, limit);
      } else {
         for(int i = 0; i < this.resources.length; ++i) {
            if (this.resources[i].name.equalsIgnoreCase(name)) {
               this.resources[i].limit = limit;
               return;
            }
         }

         Quota.Resource[] ra = new Quota.Resource[this.resources.length + 1];
         System.arraycopy(this.resources, 0, ra, 0, this.resources.length);
         ra[ra.length - 1] = new Quota.Resource(name, 0L, limit);
         this.resources = ra;
      }
   }

   public static class Resource {
      public String name;
      public long usage;
      public long limit;

      public Resource(String name, long usage, long limit) {
         this.name = name;
         this.usage = usage;
         this.limit = limit;
      }
   }
}
