import nju.ics.Main.PathRestoration;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


@RunWith(Parameterized.class)
public class PathRestorationTest {


    static String basic_data_file_path = "src/test/resources/inputs/basic-data-20200319.xls";

    static String test_data_file_path_1  = "src/test/resources/inputs/test-data-with-oracle-20200327.txt";
    static String test_data_file_path_2  = "src/test/resources/inputs/test-data-with-oracle-2020032702.txt";
    static String test_data_file_path_3  = "src/test/resources/inputs/ori_6000.txt";
    static String test_data_file_path_4  = "src/test/resources/inputs/mid_6000.txt";
    static String test_data_single       = "src/test/resources/inputs/single-test-case.txt";

    static List<String> originDPResults = new ArrayList<>();
    static int count = 0;

//    @Parameterized.Parameters(name = "{index}: assertEquals(DPResult, ManualResult)")
    @Parameterized.Parameters(name = "{index}: assertEquals(from origin, from chen)")
    public static Collection<Object> data() throws IOException {
        Collection<Object> retList = new ArrayList<>();

//        readAFile(retList, test_data_file_path_1, false);
//        readAFile(retList, test_data_file_path_2, false);

//        readAFile(retList, test_data_file_path_3, true);
        readAFile(retList, test_data_file_path_4, true);

//        readAFile(retList, test_data_single, false);
        return retList;
    }

    private static void readAFile(Collection<Object> retList, String test_data_file, boolean broken) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(test_data_file);
        BufferedReader br = new BufferedReader(new InputStreamReader(fileInputStream));

        String strLine;
        while ((strLine = br.readLine()) != null) {
            JSONObject jsonObject = new JSONObject(strLine);
            jsonObject.put("basicDataPath", basic_data_file_path);
            jsonObject.put("index", count);

            //make up with missing attributes in xu and chen's data.
            if (broken) {
                jsonObject.put("enTime", "");
                jsonObject.put("exTime", "");
                jsonObject.put("modifyCost", 0.01);
                jsonObject.put("addCost", 0.1);
                jsonObject.put("deleteCost", 500);
                jsonObject.put("deleteCost2", 2);
                jsonObject.put("deleteEndCost", 100000);
            }

            retList.add(jsonObject);
            count++;
        }
        fileInputStream.close();
    }

    @Parameterized.Parameter
    public JSONObject testCase;

    @Test
    public void testPathRestorationWithNewCases()  {
//        System.out.println("index = " + testCase.getInt("index"));
        PathRestoration pathRestoration = new PathRestoration();
        pathRestoration.pathRestorationMethod(testCase.toString());
        try {
            String DPResult = pathRestoration.recoveredPath.getLiteralPath();
            System.out.println(DPResult);

//            String manualResult = testCase.getString("manualResult");
//            assertEquals(manualResult, DPResult);
        }
        catch (JSONException Exp) {
            //do nothing.
        }
    }

//    @Test
    public void testPathRestorationByChen()  {
        System.out.println("index = " + testCase.getInt("index"));
        PathRestoration pathRestoration = new PathRestoration();
        pathRestoration.pathRestorationMethod(testCase.toString());
        String manualResult = testCase.getString("manualResult");
        String DPResult = pathRestoration.recoveredPath.getLiteralPath();
//        System.out.println(DPResult);
//        System.out.println(manualResult);

        int cur = testCase.getInt("index");
        System.out.println("cur = " + cur);
        if (cur < 17) {
            System.out.println("cur = " + cur + " DPResult = " + DPResult);
            originDPResults.add(DPResult);
        }
        else {
            System.out.println("i = " + cur%17 + " j = " + cur);
            assertEquals(originDPResults.get(cur%17), DPResult);
        }
//        assertEquals(manualResult, DPResult);

    }
}
