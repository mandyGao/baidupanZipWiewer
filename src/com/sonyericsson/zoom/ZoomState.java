/*
 * Copyright (c) 2010, Sony Ericsson Mobile Communication AB. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, 
 * are permitted provided that the following conditions are met:
 *
 *    * Redistributions of source code must retain the above copyright notice, this 
 *      list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright notice,
 *      this list of conditions and the following disclaimer in the documentation
 *      and/or other materials provided with the distribution.
 *    * Neither the name of the Sony Ericsson Mobile Communication AB nor the names
 *      of its contributors may be used to endorse or promote products derived from
 *      this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.sonyericsson.zoom;

import java.util.Observable;

/**
 * A ZoomState holds zoom and pan values and allows the user to read and listen
 * to changes. Clients that modify ZoomState should call notifyObservers()
 */
public class ZoomState extends Observable {

	/**
	 * @author FalconIA Methods of horizontal alignment.
	 */
	public enum AlignX {
		Left, Center, Right
	}

	/**
	 * @author FalconIA Methods of vertical alignment.
	 */
	public enum AlignY {
		Top, Center, Bottom
	}

	/**
	 * Method of horizontal alignment.
	 */
	private AlignX mAlignX = AlignX.Center;

	/**
	 * Method of vertical alignment.
	 */
	private AlignY mAlignY = AlignY.Center;

	/**
	 * Default value of Zoom level.
	 */
	private float mZoomDefault;

	/**
	 * Zoom level A value of 1.0 means the content fits the view.
	 */
	private float mZoom;

	/**
	 * Pan position x-coordinate X-coordinate of zoom window center position,
	 * relative to the width of the content.
	 */
	private float mPanX;

	/**
	 * Pan position y-coordinate Y-coordinate of zoom window center position,
	 * relative to the height of the content.
	 */
	private float mPanY;

	// Public methods

	/**
	 * Get current horizontal alignment method
	 * 
	 * @return current horizontal alignment method
	 */
	public AlignX getAlignX() {
		return mAlignX;
	}

	/**
	 * Get current vertical alignment method
	 * 
	 * @return current vertical alignment method
	 */
	public AlignY getAlignY() {
		return mAlignY;
	}

	/**
	 * Get current x-pan
	 * 
	 * @return current x-pan
	 */
	public float getPanX() {
		return mPanX;
	}

	/**
	 * Get current y-pan
	 * 
	 * @return Current y-pan
	 */
	public float getPanY() {
		return mPanY;
	}

	/**
	 * Get current zoom value
	 * 
	 * @return Current zoom value
	 */
	public float getZoom() {
		return mZoom;
	}

	/**
	 * Get default zoom value
	 * 
	 * @return Default zoom value
	 */
	public float getDefaultZoom() {
		return Math.max(1, mZoomDefault);
	}

	/**
	 * Help function for calculating current zoom value in x-dimension
	 * 
	 * @param aspectQuotient
	 *            (Aspect ratio content) / (Aspect ratio view)
	 * @return Current zoom value in x-dimension
	 */
	public float getZoomX(float aspectQuotient) {
		return Math.min(mZoom, mZoom * aspectQuotient);
	}

	/**
	 * Help function for calculating current zoom value in y-dimension
	 * 
	 * @param aspectQuotient
	 *            (Aspect ratio content) / (Aspect ratio view)
	 * @return Current zoom value in y-dimension
	 */
	public float getZoomY(float aspectQuotient) {
		return Math.min(mZoom, mZoom / aspectQuotient);
	}

	/**
	 * Set horizontal alignment method
	 * 
	 * @param alignX
	 *            Horizontal alignment method to set
	 */
	public void setAlignX(AlignX alignX) {
		if (alignX != mAlignX) {
			mAlignX = alignX;
			setChanged();
		}
	}

	/**
	 * Set vertical alignment method
	 * 
	 * @param alignY
	 *            Vertical alignment method to set
	 */
	public void setAlignY(AlignY alignY) {
		if (alignY != mAlignY) {
			mAlignY = alignY;
			setChanged();
		}
	}

	/**
	 * Set pan-x
	 * 
	 * @param panX
	 *            Pan-x value to set
	 */
	public void setPanX(float panX) {
		if (panX != mPanX) {
			mPanX = panX;
			setChanged();
		}
	}

	/**
	 * Set pan-y
	 * 
	 * @param panY
	 *            Pan-y value to set
	 */
	public void setPanY(float panY) {
		if (panY != mPanY) {
			mPanY = panY;
			setChanged();
		}
	}

	/**
	 * Set zoom
	 * 
	 * @param zoom
	 *            Zoom value to set
	 */
	public void setZoom(float zoom) {
		if (zoom != mZoom) {
			mZoom = zoom;
			setChanged();
		}
	}

	/**
	 * Set default zoom
	 * 
	 * @param zoom
	 *            Default zoom value to set
	 */
	public void setDefaultZoom(float zoom) {
		if (zoom != mZoomDefault || zoom != mZoom) {
			mZoomDefault = zoom;
			mZoom = zoom;
			setChanged();
		}
	}

}
