package util;

import static java.lang.System.*;

import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * base for all tests
 */
public class TestAny {

	protected static final Logger log = LoggerFactory.getLogger(TestAny.class);

	@BeforeClass
	public static void initClass() throws Exception {

		log.info("arch/os : {}/{}", //
				getProperty("os.arch"), getProperty("os.name"));

	}

}
