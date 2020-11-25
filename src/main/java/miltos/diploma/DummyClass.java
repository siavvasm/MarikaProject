package miltos.diploma;

public class DummyClass {
	
	public DummyClass() {
		// Empty constructor ...
	}
	
	public void dummyMethod() {
		System.out.println("The dummy method is invoked!");
		
		String x = null;
		x = "";
		System.out.println(x);
		
		int ThisIsAwronglySpelledVariable = 1;
		
		if (true) {
			System.out.println("Inside if statement");
		} else {
			
		}
		
		if (true) {
			System.out.println("Inside if statement");
		} else {
			
		}
		
		if (true) {
			System.out.println("Inside if statement");
		} else {
			
		}
		
		// Cyclic Dependency
		UselessClass uC = new UselessClass();
		uC.doSomething(1, 0, 1, 0);
	}
	
    public void printSmth() {
    	System.out.println("Cyclic Dependency");
    }
}
