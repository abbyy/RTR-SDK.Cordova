package com.abbyy.mobile.rtr.cordova.utils;

import android.graphics.Bitmap;
import android.util.LruCache;

public class MemoryCappedCache extends LruCache<Integer, Bitmap> {
	public MemoryCappedCache( int maxSize )
	{
		super( maxSize );
	}

	protected int sizeOf( Integer key, Bitmap value )
	{
		return value.getByteCount();
	}
}
