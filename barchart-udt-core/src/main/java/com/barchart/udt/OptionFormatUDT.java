/**
 * Copyright (C) 2009-2012 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.udt;

public enum OptionFormatUDT {

	DECIMAL() {
		@Override
		public String convert(Object value) {
			if (value instanceof Number) {
				long number = ((Number) value).longValue();
				return String.format("%,d", number);
			}
			return "invalid value";
		}
	}, //

	BINARY() {
		@Override
		public String convert(Object value) {
			if (value instanceof Number) {
				long number = ((Number) value).longValue();
				return String.format("%,d (%,d K)", number, number / 1024);
			}
			return "invalid value";
		}
	}, //

	BOOLEAN() {
		@Override
		public String convert(Object value) {
			if (value instanceof Boolean) {
				boolean bool = ((Boolean) value).booleanValue();
				return String.format("%b", bool);
			}
			return "invalid value";
		}
	}, //

	;

	public abstract String convert(Object value);

	OptionFormatUDT() {
	}

}
