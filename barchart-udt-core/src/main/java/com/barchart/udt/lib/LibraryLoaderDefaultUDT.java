package com.barchart.udt.lib;

public class LibraryLoaderDefaultUDT implements LibraryLoaderUDT {

	@Override
	public void load(final String location) throws Exception {

		LibraryUDT.load(location);

	}

}
