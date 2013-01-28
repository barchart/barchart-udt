package com.barchart.udt.test;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.barchart.udt.test.HelloActivity.MyActivity;
import com.uphyca.testing.ActivityUnitTestCase;
import com.uphyca.testing.AndroidJUnit4TestAdapter;

public class HelloActivity extends ActivityUnitTestCase<MyActivity> {

	/**
	 * For Eclipse with ADT
	 * <p>
	 * Should use AndroidJUnit4TestAdapter for to running AndroidDependent
	 * TestCases.
	 */
	public static junit.framework.Test suite() {
		return new AndroidJUnit4TestAdapter(HelloActivity.class);
	}

	public static class MyActivity extends Activity {
		@Override
		protected void onCreate(final Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			final TextView view = new TextView(this);
			view.setText("Hello, activity.");
			view.setId(android.R.id.text1);
			setContentView(view);
		}
	}

	private Intent startIntent;

	public HelloActivity() {
		super(MyActivity.class);
	}

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		startIntent = new Intent(Intent.ACTION_MAIN);
	}

	@Test
	public void assertPreconditions() {
		startActivity(startIntent, null, null);
		assertNotNull(getActivity());
	}

	@Test
	public void sayHello() {
		startActivity(startIntent, null, null);
		assertThat(((TextView) getActivity().findViewById(android.R.id.text1))
				.getText().toString(), equalTo("Hello, activity."));
	}

}
