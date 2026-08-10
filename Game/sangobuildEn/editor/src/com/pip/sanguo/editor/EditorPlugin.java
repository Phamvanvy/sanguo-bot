package com.pip.sanguo.editor;

import java.util.Arrays;
import java.util.Collection;
import java.util.Hashtable;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class EditorPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.pip.sanguo.editor";

	// The shared instance
	private static EditorPlugin plugin;
	
	/**
	 * The constructor
	 */
	public EditorPlugin() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static EditorPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

    // The image file need to be shared.
    private final static String[] imageNames = { 
        "dataobj", "npcicon", "itemtype", "item", "flag", "rootnode", 
        "dropgroup", "dropitem", "newattribute", "empty", "delete"
    };

    /** 
     * Initializes an image registry with images which are frequently used by the 
     * plugin.
     */
    protected void initializeImageRegistry(ImageRegistry reg) {
        try {
            for (int i = 0; i < imageNames.length; i++) {
                ImageDescriptor desc = getImageDescriptor("icons/" + imageNames[i] + ".gif");
                getImageRegistry().put(imageNames[i], desc);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
