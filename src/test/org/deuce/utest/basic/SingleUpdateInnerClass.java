package org.deuce.utest.basic;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.deuce.Atomic;

public class SingleUpdateInnerClass extends TestCase {
    
    public static void main(String [] args){
        SingleUpdateInnerClass test = new SingleUpdateInnerClass();
        test.setUp();
        test.testSimpleAdd();
    }

	private static class Inner{
		private int intVar;
		private long longVar;
		private double doubleVar;
		private Object objectVar;
		private String stringVar;

		private int[] intArrVar;
		private long[] longArrVar;
		private double[] doubleArrVar;
		private Object[] objectArrvar;
		private String[] stringArrvar;
		
		@Atomic int intVar(){return intVar;}
		@Atomic long longVar(){return longVar;}
		@Atomic double doubleVar(){return doubleVar;}
		@Atomic Object objectVar(){return objectVar;}
		@Atomic String stringVar(){return stringVar;}

		@Atomic int intArrVar(int idx){return intArrVar[idx];}
		@Atomic long longArrVar(int idx){return longArrVar[idx];}
		@Atomic double doubleArrVar(int idx){return doubleArrVar[idx];}
		@Atomic Object objectArrvar(int idx){return objectArrvar[idx];}
		@Atomic String stringArrvar(int idx){return stringArrvar[idx];}

	}
	
	private Inner inner = new Inner();
	
    @Override
    public void setUp() { 
    	
    	inner.intVar = 0;
    	inner.longVar = 0;
    	inner.doubleVar = 0;
    	inner.objectVar = null;
    	inner.stringVar = null;
    	
    	inner.intArrVar = new int[1];
    	inner.longArrVar = new long[1];
    	inner.doubleArrVar = new double[1];
    	inner.objectArrvar = new Object[1];
    	inner.stringArrvar = new String[1];
	}
	
	public void testSimpleAdd() {
		atomicSingleUpdate();
		
		Assert.assertEquals(inner.intVar(), 1);
		Assert.assertEquals(inner.longVar(), 1);
		Assert.assertEquals(inner.doubleVar(), 1.0);
		Assert.assertNotNull(inner.objectVar());
		Assert.assertEquals(inner.stringVar(), "a");
		
		Assert.assertEquals(inner.intArrVar(0), 1);
		Assert.assertEquals(inner.longArrVar(0), 1);
		Assert.assertEquals(inner.doubleArrVar(0), 1.0);
		Assert.assertNotNull(inner.objectArrvar(0));
		Assert.assertEquals(inner.stringArrvar(0), "a");
	}
	
	@Atomic
	private void atomicSingleUpdate() {
		inner.intVar++;
		inner.longVar++;
		inner.doubleVar++;
		inner.objectVar = new Object();
		inner.stringVar = new String("a");
		
		inner.intArrVar[0]++;
		inner.longArrVar[0]++;
		inner.doubleArrVar[0]++;
		inner.objectArrvar[0] = new Object();
		inner.stringArrvar[0] = new String("a");
	}
	
	public void testSimpleAddAndDec() {
		atomicSingleUpdateAndDec();
		
		Assert.assertEquals(inner.intVar, 0);
		Assert.assertEquals(inner.longVar, 0);
		Assert.assertEquals(inner.doubleVar, 0.0);
		Assert.assertNull(inner.objectVar);
		Assert.assertNull(inner.stringVar);
		
		Assert.assertEquals(inner.intArrVar[0], 0);
		Assert.assertEquals(inner.longArrVar[0], 0);
		Assert.assertEquals(inner.doubleArrVar[0], 0.0);
		Assert.assertNull(inner.objectArrvar[0]);
		Assert.assertNull(inner.stringArrvar[0]);
		
	}
	
	@Atomic
	private void atomicSingleUpdateAndDec() {
		inner.intVar++;
		inner.intVar--;
		
		inner.longVar++;
		inner.longVar--;
		
		inner.doubleVar++;
		inner.doubleVar--;
		
		inner.objectVar = new Object();
		inner.objectVar = null;
		
		inner.stringVar = "a";
		inner.stringVar = null;
		
		inner.intArrVar[0]++;
		inner.intArrVar[0]--;
		
		inner.longArrVar[0]++;
		inner.longArrVar[0]--;
    	
		inner.doubleArrVar[0]++;
		inner.doubleArrVar[0]--;
    	
		inner.objectArrvar[0] = new Object();
		inner.objectArrvar[0] = null;
    	
		inner.stringArrvar[0] = new String("a");
		inner.stringArrvar[0] = null;
	}

}
