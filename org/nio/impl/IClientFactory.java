package org.nio.impl;

public interface IClientFactory<T extends MMOClient> {
   T create(MMOConnection<T> var1);
}
