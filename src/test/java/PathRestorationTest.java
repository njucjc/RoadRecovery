import nju.ics.Main.PathRestoration;
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
    static String test_data_file_path  = "src/test/resources/inputs/test-data-with-oracle-20200327.txt";


    @Parameterized.Parameters(name = "{index}: assertEquals(DPResult, ManualResult)")
    public static Collection<Object> data() throws IOException {
        Collection<Object> retList = new ArrayList<>();
        FileInputStream fileInputStream = new FileInputStream(test_data_file_path);
        BufferedReader br = new BufferedReader(new InputStreamReader(fileInputStream));

        String strLine; int count = 0;
        while ((strLine = br.readLine()) != null) {
//            System.out.print( count + ": ");
            count++;
            JSONObject jsonObject = new JSONObject(strLine);
            jsonObject.put("basicDataPath", basic_data_file_path);
            retList.add(jsonObject);
//            if (count > 10) break;
        }
        fileInputStream.close();
        return retList;
    }

    @Parameterized.Parameter
    public JSONObject testCase;

    @Test
    public void testPathRestorationWithNewCases()  {
        PathRestoration pathRestoration = new PathRestoration();
        String returnedJSONString = pathRestoration.pathRestorationMethod(testCase.toString());
        JSONObject jsonObject = new JSONObject(returnedJSONString);
        String manualResult = testCase.getString("manualResult");
        String DPResult = pathRestoration.recoveredPath.getLiteralPath();
//        System.out.println(DPResult);
//        System.out.println(manualResult);

        assertEquals(manualResult, DPResult);
    }



    JSONObject successJsonObject = new JSONObject();
    JSONObject failureJsonObject = new JSONObject();

//    @Test
    public void testPathRestorationMethod() {
        getInput();

        PathRestoration pathRestoration = new PathRestoration();
        String returnString;

        System.out.println(successJsonObject);
        returnString = pathRestoration.pathRestorationMethod(successJsonObject.toString());
        System.out.println(returnString);
        System.out.println();

//        System.out.println(failureJsonObject);
//        returnString = pathRestoration.pathRestorationMethod(failureJsonObject.toString());
//        System.out.println(returnString);

        //assert some properties
    }

    private void getInput() {
        //manually curate a successful JSON data
        successJsonObject.put("basicDataPath", basic_data_file_path);

        successJsonObject.put("modifyCost", 0.01);
        successJsonObject.put("addCost", 0.1);
        successJsonObject.put("deleteCost", 4000);
        successJsonObject.put("deleteCost2", 2);
        successJsonObject.put("deleteEndCost", 1000000);

        List<JSONObject> gantryIDList = new ArrayList<>();
        successJsonObject.put("enStationId", "");
        successJsonObject.put("exStationId", "");
        successJsonObject.put("enTime", "2020-01-23 16:30:31");
        successJsonObject.put("exTime", "2020-01-23 18:40:20");
        addToList(gantryIDList, "3F5A0A");
        addToList(gantryIDList, "3D5A0C");
        addToList(gantryIDList, "3D5A0E");
        addToList(gantryIDList, "3D5A0F");
        addToList(gantryIDList, "3D5A10");
        addToList(gantryIDList, "3D5A11");
        addToList(gantryIDList, "3D5A12");
        addToList(gantryIDList, "3D5F06");
        addToList(gantryIDList, "3D5F07");
        addToList(gantryIDList, "3D5F08");
        addToList(gantryIDList, "3C4A04");
        addToList(gantryIDList, "3E4A05");

        successJsonObject.put("gantryIdList", gantryIDList);


        //manually curate a failure JSON data
        failureJsonObject.put("enStationId", "");
        failureJsonObject.put("exStationId", "");
        failureJsonObject.put("enTime",      "2020-01-22 11:39:03");
        failureJsonObject.put("exTime",      "2020-01-22 12:06:05");

        failureJsonObject.put("basicDataPath", basic_data_file_path);

        failureJsonObject.put("modifyCost", 0.01);
        failureJsonObject.put("addCost", 0.1);
        failureJsonObject.put("deleteCost", 4000);
        failureJsonObject.put("deleteCost2", 2);
        failureJsonObject.put("deleteEndCost", 1000000);

        List<JSONObject> failList = new ArrayList<>();
        failureJsonObject.put("gantryIdList", failList);
    }

    private void addToList(List<JSONObject> list, String gantryHex) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("gantryHex", gantryHex);
        jsonObject.put("transTime", "");
        list.add(jsonObject);
    }
}
