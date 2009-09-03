package org.rubypeople.rdt.internal.debug.ui;

import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;

public class RDTImageDescriptor extends CompositeImageDescriptor {

	/** Flag to render the is out of synch adornment */
	public final static int IS_OUT_OF_SYNCH= 			0x0001;
	
	private Point fSize;
	private ImageDescriptor fBaseImage;
	private int fFlags;
	
	public RDTImageDescriptor(ImageDescriptor baseImage, int flags) {
		setBaseImage(baseImage);
		setFlags(flags);
	}
	
	@Override
	protected void drawCompositeImage(int width, int height) {
		ImageData bg= getBaseImage().getImageData();
		if (bg == null) {
			bg= DEFAULT_IMAGE_DATA;
		}
		drawImage(bg, 0, 0);
		drawOverlays();
	}

	@Override
	protected Point getSize() {
		if (fSize == null) {
			ImageData data= getBaseImage().getImageData();
			setSize(new Point(data.width, data.height));
		}
		return fSize;
	}
	
	protected void setSize(Point size) {
		fSize = size;
	}
	
	protected ImageDescriptor getBaseImage() {
		return fBaseImage;
	}
	
	protected void setBaseImage(ImageDescriptor baseImage) {
		fBaseImage = baseImage;
	}
	
	protected int getFlags() {
		return fFlags;
	}
	
	protected void setFlags(int flags) {
		fFlags = flags;
	}
	
	/**
	 * Add any overlays to the image as specified in the flags.
	 */
	protected void drawOverlays() {
		int flags= getFlags();
		int x= 0;
		int y= 0;
		ImageData data= null;
		if ((flags & IS_OUT_OF_SYNCH) != 0) {
			x= getSize().x;
			y= 0;
			data= getImageData(RubyDebugImages.IMG_OVR_OUT_OF_SYNCH);
			x -= data.width;
			drawImage(data, x, y);
		}
	}
	
	private ImageData getImageData(String imageDescriptorKey) {
		return RubyDebugImages.getImageDescriptor(imageDescriptorKey).getImageData();
	}

}
