package com.barchart.udt;

import com.barchart.udt.lib.LibraryLoader;

public class AndroidLoaderUDT implements LibraryLoader {

	public static void load() {

		ResourceUDT.setLibraryLoaderClassName(AndroidLoaderUDT.class.getName());

	}

	@Override
	public void load(final String location) throws Exception {

		/** FIXME replace with /system/lib/libstlport.so */
		System.loadLibrary("stlport_shared");

		System.loadLibrary("barchart-udt-core-andriod");

	}

}
