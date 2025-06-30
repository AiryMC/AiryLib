package dev.airyy.AiryLib.core;

/**
 * Interface representing a plugin for the Airy system.
 * <p>
 * This interface defines the necessary lifecycle methods that any plugin must implement:
 * {@link #onInit()} for initialization tasks and {@link #onDestroy()} for cleanup.
 * The methods are called during the plugin's lifecycle, typically at startup and shutdown.
 */
public interface IAiryPlugin {

    /**
     * Called when the plugin is initialized.
     * <p>
     * This method is typically invoked during the plugin's startup phase.
     * Use this to register event listeners, initialize configuration files,
     * or perform other setup tasks necessary for the component to function.
     */
    void onInit();

    /**
     * Called when the plugin is being unloaded or the server is shutting down.
     * <p>
     * Use this to clean up resources, save persistent data, or unregister listeners.
     * This method ensures that the component shuts down gracefully without leaving
     * behind dangling references or tasks.
     */
    void onDestroy();
}
