package com.barchart.udt.lib;

import static com.barchart.udt.lib.LibraryUDT.*;
import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.barchart.udt.ResourceUDT;
import com.barchart.udt.SocketUDT;
import com.barchart.udt.TypeUDT;
import com.barchart.udt.util.HelperUtils;

public class TestLibraryUDT {

	static {

		HelperUtils.logOsArch();
		HelperUtils.logClassPath();
		HelperUtils.logLibraryPath();

	}

	@Test
	public void testLoadWithProps() throws Exception {

		ResourceUDT.setLibraryLoaderClassName(LibraryLoaderDefaultUDT.class.getName());

		ResourceUDT.setLibraryExtractLocation("./target/test-testLoadWithProps-" + HelperUtils.getRandomString());

		SocketUDT socket = new SocketUDT(TypeUDT.DATAGRAM);

		assertTrue(socket.isOpen());

	}

	@Test
	public void testLoadString() throws Exception {

		String targetFolder = "./target/testLoadString-" + HelperUtils.getRandomString();

		LibraryUDT.load(targetFolder);

		assertTrue(true);

	}

	@Test
	public void testPath() {

		assertEquals(I386_LINUX_GPP.sourceLibRealNAR(),//
				"/lib/i386-Linux-gpp/jni/" + coreName());

		assertEquals(I386_LINUX_GPP.targetResPath("./lib", coreName()),
				"./lib/i386-Linux-gpp/" + coreName());

		//

		assertEquals(X86_WINDOWS_GPP.sourceDepTestNAR("nar.dll"),
				"/aol/x86-Windows-gpp/lib/nar.dll");

	}


}
