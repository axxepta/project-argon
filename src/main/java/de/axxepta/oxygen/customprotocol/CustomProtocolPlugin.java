package de.axxepta.oxygen.customprotocol;

import ro.sync.exml.plugin.Plugin;
import ro.sync.exml.plugin.PluginDescriptor;

/**
 * Sample plugin for customs protocols.
 */
public class CustomProtocolPlugin extends Plugin {
  /**
   * The static plugin instance.
   */
  private static CustomProtocolPlugin instance = null;

  /**
   * Constructs the plugin.
   *
   * @param descriptor The plugin descriptor
   */
  public CustomProtocolPlugin(PluginDescriptor descriptor) {
    super(descriptor);

    if (instance != null) {
      throw new IllegalStateException("Already instantiated!");
    }
    instance = this;
  }

  /**
   * Get the plugin instance.
   *
   * @return the shared plugin instance.
   */
  public static CustomProtocolPlugin getInstance() {
    return instance;
  }
}
