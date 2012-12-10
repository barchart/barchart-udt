package com.barchart.udt.nio;

public class MainSelectorUDT {

	public static void main(String[] args) throws Exception {

		TestSelectorUDT test = new TestSelectorUDT();

		test.setUp();

		test.testSelect();

		test.tearDown();

	}

}
