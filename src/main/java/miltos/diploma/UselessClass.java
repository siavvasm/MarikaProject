package miltos.diploma;

public class UselessClass {

	public void doSomething(int x, int y, int z, int k) {
		
		int var = x + y + z + k;
		

		
		if (true) {
			if (var > 3) {
				System.out.println("The sum is greater than 3!");
			} else if (var > 1) {
				System.out.println("The sum is equal to 2!");
			} else {
				
			}
		}
		
		String s = "";
		
		if (s.equals("")) {
			System.out.println("The string is empty!");
		} else {
			
		}
		
		// Cyclic Dependency
		DummyClass dummyClass = new DummyClass();
		dummyClass.printSmth();
	}
}
