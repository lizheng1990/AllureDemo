package my.company;

import org.testng.annotations.Test;

import org.testng.annotations.DataProvider;
import org.testng.annotations.BeforeSuite;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.AfterSuite;

public class TestSum {

	@Test(dataProvider = "dp")
	public void f(int a, int b, int c) {
		int su = a + b;
		assertEquals(su, c);
	}

	@DataProvider
	public Object[][] dp() {
		return new Object[][] { { -2147483648, -1,2147483647 }, { -2147483648, 0,-2147483647 },
				{ -2147483648, 1,-2147483647 }, { 2147483647, -1,2147483647 }, { 2147483647, 0,2147483647 },
				{ 2147483647, 1,-2147483648 } };
	}
}
