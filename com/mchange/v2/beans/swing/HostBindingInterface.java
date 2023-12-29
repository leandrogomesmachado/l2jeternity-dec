package com.mchange.v2.beans.swing;

import java.beans.PropertyEditor;

interface HostBindingInterface {
   void syncToValue(PropertyEditor var1, Object var2);

   void addUserModificationListeners();

   Object fetchUserModification(PropertyEditor var1, Object var2);

   void alertErroneousInput();
}
