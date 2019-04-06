package my.company;

import org.testng.annotations.Test;

import com.testing.common.ExcelReader;
import com.testing.common.ExcelWriter;

import org.testng.annotations.DataProvider;
import org.testng.annotations.BeforeSuite;
import static org.testng.Assert.assertEquals;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.testng.annotations.AfterSuite;

public class TestLogin {

	public KeyWord kw;
	public ExcelReader er;
	public ExcelWriter ew;

	@Test(dataProvider = "dp")
	public void f(String a, String b, String c, String d, String e, String f, String g, String h, String i, String j,
			String k, String l, String m) {
		kw.line = Integer.parseInt(m);
		String re = kw.PostRequest(e, f);
		kw.assertEqual(i, j);
		assertEquals(kw.returnValue(re, i), j);
	}

	@DataProvider
	public Object[][] dp() {
		Object[][] result = er.readAsMatrix();
		return result;
	}

	@BeforeSuite
	public void beforeSuite() {
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		String createtime = sdf.format(date);
		String filepath = System.getProperty("user.dir");
		String file = filepath + "\\cases\\";
		String fileres = filepath + "\\cases\\result-";
		file = file + "HttpLogin.xlsx";
		fileres = fileres + "HttpLogin" + createtime + ".xlsx";
		er = new ExcelReader(file);
		ew = new ExcelWriter(file, fileres);
		kw = new KeyWord(ew);
	}

	@AfterSuite
	public void afterSuite() {
		er.close();
		ew.save();
	}

}
