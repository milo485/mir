package mir.log;

import mir.log.Log;


public class TestFramework {

    public static void main(String[] args) {
        TestFramework t = new TestFramework();

        Log.info( TestFramework.class, "class");
        Log.info( t, "object" );
        Log.info( null, "lalala" );
    }
}
