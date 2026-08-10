package com.pip.engine;


import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.lcdui.Canvas;
//#if ModelID == AndroidAuto
//#if opengl == true
//# import com.pip.android.opengl.GLTextureManager;
//#endif
//#endif
import com.pip.image.ImageSet;
import com.pip.image.PipAnimateSet;
import com.pip.sanguo.GameMain;


public class AnimateCache{
    private static final Hashtable animateCache = new Hashtable(); //Animate Name, PipAnimateSet or String(when requesting)
    private static final Hashtable imageCache = new Hashtable(); //Image Name, ImageSet or String(when requesting)

    private static final Hashtable requestAnimate2Owner = new Hashtable(); //Animate Name, Vector IAnimateOwner
    private static final Hashtable requestImage2Animate = new Hashtable(); //Image Name Vector String(Animate Name)

    private static final Hashtable animate2Owner = new Hashtable(); //Animate Name, Vector IAnimateOwner
    private static final Hashtable image2Animate = new Hashtable(); //Image Name Vector String(Animate Name)

    private static final Vector aysnAnimateReady = new Vector();
    private static final Vector pendingReleaseAnimate = new Vector();
    
    public static void clearPendingReleaseAnimate(){
        synchronized(pendingReleaseAnimate){
            int count = pendingReleaseAnimate.size();
            
            for(int i = 0; i < count; i++){
                releaseAnimate(null, (String)pendingReleaseAnimate.elementAt(i), true);
            }
            
            pendingReleaseAnimate.removeAllElements();
        }
    }
    
    //#if opengl == true
    //# public static boolean isNpcAnimateFormat(String name){
    	//# if(name.indexOf("body") != -1 || name.indexOf("horse") != -1){
    		//# return true;
    	//# }
    	//# boolean r = true;
    	//# char ch;
    	//# int dotPos = name.indexOf('.');
    	//# String fileName;
    	//# if(dotPos != -1){
    		//# fileName = name.substring(0, dotPos);
    	//# } else {
    		//# fileName = name;
    	//# }
    	//# String sample = "0123456789_";
    	//# for (int i = 0; i < fileName.length(); i++) {
			//# ch = fileName.charAt(i);
			//# if(sample.indexOf(ch) == -1){
				//# r = false;
				//# break;
			//# }
		//# }
//#     	
    	//# return r;
    //# }
    //# public static boolean isBody(PipAnimateSet pas){
    	//# boolean r = false;
    	//# String[] pipNames = pas.getAllImageName();
		//# for (int i = 0; i < pipNames.length; i++) {
			//# if(isNpcAnimateFormat(pipNames[i])){
				//# r = true;
				//# break;
			//# }
		//# }
    	//# return r;
    //# }
    //# /*
     //# * OpenGL模式下，图片对象需要向材质管理器注册才能使用。
     //# */
    /*
     * OpenGL模式下，图片对象需要向材质管理器注册才能使用。
     */
  //#      private static void registerTexture(String name, Object obj) {
    	//#     	 if (obj instanceof ImageSet) {
    		//#     		((ImageSet)obj).bindTexture(Canvas.GL_POOL_MISC, name);
    		//# 		 } 
    	//#      }
//#     
    /*
     * OpenGL模式下，图片对象不使用了要从材质管理器注销。
     */
   //#      private static void unregisterTexture(String name, Object obj) {
    	//#     	 if (obj instanceof ImageSet) {
    		//#     		 ((ImageSet) obj).unbind();
    		//# 		 } else if (obj instanceof PipAnimateSet) {
			//# 			 PipAnimateSet pas = (PipAnimateSet)obj;
			//# 			 for (int i = 0; i < pas.getImageCount(); i++) {
				//# 				 pas.getSourceImage(i).unbind();
				//# 			 }
			//# 		 }
    	//#      }
//#endif
    
    public static void clear(){
    	//#if opengl == true
    	//# 	 if (GameMain.openglMode) {
    	//#   	// OPENGL模式下，需要清除所有图片材质
    	//#   		 Enumeration enum1 = imageCache.keys();
    	//#    		 while (enum1.hasMoreElements()) {
    	//#    			 String name = (String)enum1.nextElement();
    	//#     			 Object obj = imageCache.get(name);
    	//#     			 if(obj instanceof ImageSet){
    	//#     				 ImageSet img = (ImageSet)obj;
    	//#     				 img.unbind();
    	//#    			 }
    	//#    		 }
    				 
    	//#    		 Enumeration enum2 = animateCache.keys();
    	//#     		 while (enum2.hasMoreElements()) {
    	//#     			 String name = (String)enum2.nextElement();
    	//#     			 Object obj = animateCache.get(name);
    	//#     			 if(obj instanceof PipAnimateSet){
    	//#     				 PipAnimateSet pa = (PipAnimateSet)obj;
    	//#     				 for (int i = 0; i < pa.getImageCount(); i++) {
    	//#     					ImageSet img = pa.getSourceImage(i);
    	//#    					img.unbind();
    	//#     					System.out.println("unbind:"+name);
    	//#    				}
    	//#     			 } else if(obj instanceof ImageSet){
    	//#    				 ImageSet img = (ImageSet)obj;
    	//#     				 img.unbind();
    	//#     			 }
    	//#     		 }
    	//#    	 }
    	//#endif
    	        animateCache.clear();
    	        imageCache.clear();
    	        requestAnimate2Owner.clear();
    	        requestImage2Animate.clear();
    	        animate2Owner.clear();
    	        image2Animate.clear();
    	        aysnAnimateReady.removeAllElements();
    	        pendingReleaseAnimate.removeAllElements();
    	    }
    
    public static void processAnimateReadyQueue(){
        Object[][] items;

        synchronized(aysnAnimateReady){
            items = new Object[aysnAnimateReady.size()][];
            aysnAnimateReady.copyInto(items);
            aysnAnimateReady.removeAllElements();
        }

        for(int i = 0; i < items.length; i++){
            Object[] tmp = items[i];
            IAnimateOwner owner = (IAnimateOwner)tmp[0];
            String animateName = (String)tmp[1];
            PipAnimateSet animate = (PipAnimateSet)tmp[2];
            owner.animateReady(animateName, animate);
        }
    }

    public static void requestAnimate(IAnimateOwner owner, String animateName){
        Object cacheAnimate = animateCache.get(animateName);

        if(cacheAnimate == null){
            addAnimateRequest(owner, animateName);
            animateCache.put(animateName, animateName);
            GameMain.resourceManager.requestResource(animateName);
            
          //#ifdef buildtest
            System.out.println("Request animate : " + animateName);
          //#endif
        }else if(cacheAnimate instanceof String){
            addAnimateRequest(owner, animateName);
        }else if(cacheAnimate instanceof PipAnimateSet){
            PipAnimateSet animate = (PipAnimateSet)cacheAnimate;

            if(animate.ready()){
                addAnimateOwner(owner, animateName);
                addAnimateReadyQueue(owner, animateName, animate);
            }else{
                addAnimateRequest(owner, animateName);
            }
        }
    }

    public static void recvAnimate(String animateName, byte[] ctnData){
    	//#ifdef buildtest
        System.out.println("Recv Animate: " + animateName);
      //#endif
        
        PipAnimateSet animate = new PipAnimateSet(null, ctnData);
        animate.fileName = animateName;
        
        animateCache.put(animateName, animate);

        String[] missingImage = animate.getMissingImage();

        for(int i = 0; i < missingImage.length; i++){
            Object image = imageCache.get(missingImage[i]);

            if(image == null){
                addImageReuest(missingImage[i], animateName);
                imageCache.put(missingImage[i], missingImage[i]);
                GameMain.resourceManager.requestResource(missingImage[i]);

              //#ifdef buildtest
                System.out.println("Request Image : " + missingImage[i]);
              //#endif
            }else if(image instanceof String){
                addImageReuest(missingImage[i], animateName);
            }else if(image instanceof ImageSet){
                addImageOwner(missingImage[i], animateName);
                //#if opengl == true
                //# if(Canvas.openglMode){
                	//# registerTexture(missingImage[i], image);
                //# }
                //#endif
                animate.setImage(missingImage[i], (ImageSet)image);

                if(animate.ready()){
                    animateReady(animateName, animate);
                }
            }
        }
    }

    public static void recvImage(String imageName, ImageSet image){
    	//#ifdef buildtest
        System.out.println("Recv Image: " + imageName);
      //#endif
        
        imageReady(imageName, image);
//#if opengl == true
   	 //# if (GameMain.openglMode) {
   		 //# image.fileName = imageName;
       	 //# registerTexture(imageName, image);
        //# }
//#endif

        Vector animates = (Vector)image2Animate.get(imageName);
        
        if(animates == null){
            return;
        }

        
        for(int i = 0; i < animates.size(); i++){
            String animateName = (String)animates.elementAt(i);
            PipAnimateSet animate = (PipAnimateSet)animateCache.get(animateName);
            animate.setImage(imageName, image);

            if(animate.ready()){
                animateReady(animateName, animate);
            }
        }

    }

    public static void releaseAnimate(IAnimateOwner owner, String animateName, boolean force){
    	Object obj = animateCache.get(animateName);
    	if (obj == null) {
    		return;
    	}
    	if (obj instanceof String) {
    		// 考虑还没有载入的情况
            Vector owners = (Vector)requestAnimate2Owner.get(animateName);
            if (owners != null) {
            	owners.removeElement(owner);
            }
            return;
    	}
    	
        PipAnimateSet animate = (PipAnimateSet)obj;
        
        if(animate == null) {
        	return;
        }
        
        String[] imageNames = animate.getAllImageName();

        Vector owners = (Vector)animate2Owner.get(animateName);
        
        if(owners == null){
            owners = new Vector();
        }
        
        Vector restOwners = new Vector();

        for(int i = 0; i < owners.size(); i++){
            IAnimateOwner tmpOwner = (IAnimateOwner)owners.elementAt(i);

            if(owner == null || !(tmpOwner.getType() == owner.getType() && tmpOwner.getId() == owner.getId())){
                restOwners.addElement(tmpOwner);
            }
        }

        owners.removeAllElements();

        for(int i = 0; i < restOwners.size(); i++){
            owners.addElement(restOwners.elementAt(i));
        }

        //no owner left, remove animateCache and remove all image2Animate
        if(owners.size() == 0){
            animate2Owner.remove(animateName);
            
            if(!force && GameMain.animateCacheType != 2){
            	//#ifdef buildtest
                System.out.println("Pending Release Animate: " + animateName);
              //#endif
                
                synchronized(pendingReleaseAnimate){
                    pendingReleaseAnimate.addElement(animateName);
                }
                
                return;
            }else{
//#if opengl == true	        	
//	             //# if (GameMain.openglMode) {
//	            	 //# unregisterTexture(animateName, animate);
//	             //# }
//#endif
            	animateCache.remove(animateName);
            }

          //#ifdef buildtest
            System.out.println("Release Animate: " + animateName);
          //#endif
            
            //remove all image2Animate
            for(int i = 0; i < imageNames.length; i++){
                Vector animates = (Vector)image2Animate.get(imageNames[i]);

                if(animates == null){
                    continue;
                }

                Vector restAnimates = new Vector();

                for(int j = 0; j < animates.size(); j++){
                    String tmpAnimateName = (String)animates.elementAt(j);

                    if(!tmpAnimateName.equals(animateName)){
                        restAnimates.addElement(tmpAnimateName);
                    }
                }

                animates.removeAllElements();

                for(int j = 0; j < restAnimates.size(); j++){
                    animates.addElement(restAnimates.elementAt(j));
                }

                //no animate left remove imageCache
                if(animates.size() == 0){
                	//#ifdef buildtest
                    System.out.println("Release Image: " + imageNames[i]);
                  //#endif
                    Object set = imageCache.get(imageNames[i]);
//#if opengl == true	        	
	   	            //# if (GameMain.openglMode) {
	   	            	//# unregisterTexture(imageNames[i], set);
	   	            //# }
//#endif
                    imageCache.remove(imageNames[i]);
                    image2Animate.remove(imageNames[i]);

                }
            }
        }
    }

    private static void addAnimateReadyQueue(IAnimateOwner owner, String animateName, PipAnimateSet animate){
        synchronized(aysnAnimateReady){
            Object[] tmp = new Object[]{
                            owner, animateName, animate
            };
            aysnAnimateReady.addElement(tmp);
        }
    }

    private static void animateReady(String animateName, PipAnimateSet animate){
        Vector requestOwners = (Vector)requestAnimate2Owner.get(animateName);
        
        if(requestOwners == null){
            return;
        }
        
        Vector owners = (Vector)animate2Owner.get(animateName);
        requestAnimate2Owner.remove(animateName);

        if(owners == null){
            owners = new Vector();
        }

        for(int i = 0; i < requestOwners.size(); i++){
            owners.addElement(requestOwners.elementAt(i));
        }

        animate2Owner.put(animateName, owners);

        for(int i = 0; i < owners.size(); i++){
            IAnimateOwner owner = (IAnimateOwner)owners.elementAt(i);
            addAnimateReadyQueue(owner, animateName, animate);
        }
    }

    private static void imageReady(String imageName, ImageSet image){
        Vector requestAnimates = (Vector)requestImage2Animate.get(imageName);
        
        if(requestAnimates == null){
            return;
        }
        
        imageCache.put(imageName, image);
//#if opengl == true
         //# if (GameMain.openglMode) {
        	 //# registerTexture(imageName, image);
         //# }
//#endif
        Vector animates = (Vector)image2Animate.get(imageName);
        requestImage2Animate.remove(imageName);

        if(animates == null){
            animates = new Vector();
        }

        for(int i = 0; i < requestAnimates.size(); i++){
            animates.addElement(requestAnimates.elementAt(i));
        }

        image2Animate.put(imageName, animates);
    }

    private static void addAnimateOwner(IAnimateOwner owner, String animateName){
        Vector owners = (Vector)animate2Owner.get(animateName);
        
        if(owners == null){
            return;
        }
        
        owners.addElement(owner);
    }

    private static void addImageOwner(String imageName, String animateName){
        Vector animates = (Vector)image2Animate.get(imageName);
        animates.addElement(animateName);
    }

    private static void addAnimateRequest(IAnimateOwner owner, String animateName){
        Vector owners = (Vector)requestAnimate2Owner.get(animateName);

        if(owners == null){
            owners = new Vector();
        }

        owners.addElement(owner);
        requestAnimate2Owner.put(animateName, owners);
    }

    private static void addImageReuest(String imageName, String animateName){
        Vector animates = (Vector)requestImage2Animate.get(imageName);

        if(animates == null){
            animates = new Vector();
        }

        animates.addElement(animateName);
        requestImage2Animate.put(imageName, animates);
    }
}
