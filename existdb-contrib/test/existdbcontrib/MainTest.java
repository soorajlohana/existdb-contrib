/*
 * MainTest.java
 * JUnit based test
 *
 * Created on January 14, 2007, 5:12 PM
 */

package existdbcontrib;

import junit.framework.*;

/**
 *
 * @author wessels
 */
public class MainTest extends TestCase {
    
    public MainTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    /**
     * Test of main method, of class existdbcontrib.Main.
     */
    public void testMain() {
        System.out.println("main");
        
        String[] args = null;
        
        Main.main(args);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
