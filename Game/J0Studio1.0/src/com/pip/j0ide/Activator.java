package com.pip.j0ide;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.pip.gtleditor.GTLPartitionScanner;
import com.pip.gtleditor.java.GTLCodeScanner;
import com.pip.gtleditor.util.GTLColorProvider;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.pip.j0ide";

	public final static String GTL_PARTITIONING= "__gtl_partitioning";   //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
	private GTLPartitionScanner fPartitionScanner;
	private GTLColorProvider fColorProvider;

	/**
	 * The constructor
	 */
	public Activator() {
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
	public static Activator getDefault() {
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
	
	/**
	 * Return a scanner for creating Java partitions.
	 * 
	 * @return a scanner for creating Java partitions
	 */
	 public GTLPartitionScanner getJavaPartitionScanner() {
		if (fPartitionScanner == null)
			fPartitionScanner= new GTLPartitionScanner();
		return fPartitionScanner;
	}
	
	/**
	 * Returns the singleton Java code scanner.
	 * 
	 * @return the singleton Java code scanner
	 */
	 public RuleBasedScanner getJavaCodeScanner(ISourceViewer viewer) {
	     return new GTLCodeScanner(getJavaColorProvider(), viewer);
	}
	
	/**
	 * Returns the singleton Java color provider.
	 * 
	 * @return the singleton Java color provider
	 */
	 public GTLColorProvider getJavaColorProvider() {
	 	if (fColorProvider == null)
			fColorProvider= new GTLColorProvider();
		return fColorProvider;
	}

    // The image file need to be shared.
    private final static String[] imageNames = { 
        "model", "file", "folder", "gtls", "gtl", "compile",
        "empty", "thread", "callstackitem",
        "globalvariable", "localvariable", "member", "usedslot", "leakslot"
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
