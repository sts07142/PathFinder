package com.example.getapi;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlPullParser;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    EditText et_start, et_End;
    Button bin_test;
    TextView text;
    XmlPullParser xpp;
    String data;
    String locationData;
    String[] locationStart;
    String[] locationEnd;
    String minX, minY, maxX, maxY, drcType;
    String SendToMapData;

    double startX, startY, endX, endY;

    /*roadName, linkId, startNodeId, endNodeId, speed, travelTime <-준엽's 파싱
     * linkId, startNodeId, endNodeId, speed(km/h), travelTime(s), cost(m) <-인재's array //cost = speed*travelTime*10/36
     * String getXmlData 함수 내부에서 배열을 채웁니다
     */
    //String[][] linkBox=new String[540502][6];
    String[][] linkBox;
    int cntLink=0,totalCount,cntNode=0,startNodeIndex,endNodeIndex;
    double graph[][],startNode[],endNode[],nodeBox[][],route[],answer[];
    List<String> set = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        et_start = findViewById(R.id.et_StartAddress);
        et_End = findViewById(R.id.et_EndAddress);
        bin_test = findViewById(R.id.btn_test);
        text = findViewById(R.id.result);

        //button click 했을떄 일어나는 행동 저장
        bin_test.setOnClickListener(new View.OnClickListener() {

            StringBuffer buffer = new StringBuffer();

            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.btn_test:
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                String startAddress = et_start.getText().toString();
                                String endAddress = et_End.getText().toString();
                                //시작 주소의 위경도 받아오기
                                locationData = getLocationXmlData(startAddress);
                                SendToMapData = startAddress + "," + locationData + ",";
                                locationStart = locationData.split(",");
                                //data 넘기기 여기다가 위도 경도 받아오는거 구현
                                locationData = getLocationXmlData(endAddress);
                                SendToMapData = SendToMapData + endAddress + "," + locationData;
                                locationEnd = locationData.split(",");

                                startX = Double.parseDouble(locationStart[1]);
                                startY = Double.parseDouble(locationStart[0]);
                                endX = Double.parseDouble(locationEnd[1]);
                                endY = Double.parseDouble(locationEnd[0]);
                                if (startX > endX) {
                                    minX = locationEnd[1];
                                    maxX = locationStart[1];
                                } else {
                                    minX = locationStart[1];
                                    maxX = locationEnd[1];
                                }
                                if (startY > endY) {
                                    minY = locationEnd[0];
                                    maxY = locationStart[0];
                                    drcType = "Down";
                                } else {
                                    minY = locationStart[0];
                                    maxY = locationEnd[0];
                                    drcType = "Up";
                                }
                                data = getXmlData(minX, maxX, minY, maxY, drcType);
                                Intent intent = new Intent(MainActivity.this, MapActicity.class);
                                intent.putExtra("SendToMapData", SendToMapData);
                                startActivity(intent);
                                fill();
                                try {
                                    nearNode();
                                } catch (IOException e) {
                                    Log.d("My tag","nearnode error");
                                    e.printStackTrace();
                                }
                                dijkstra();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        text.setText(data);
                                    }
                                });
                            }
                        }).start();
                        break;
                }
            }
        });
    }

    //입력받은 주소의 위도,경도를 반환 하는 함수
    String getLocationXmlData(String startAddress) {
        StringBuffer locationXY = new StringBuffer();
        boolean islat = false;
        boolean islng = false;
        try {
            StringBuilder urlBuilder = new StringBuilder("https://maps.googleapis.com/maps/api/geocode/xml");

            urlBuilder.append("?" + URLEncoder.encode("address", "UTF-8") + "=" + URLEncoder.encode(startAddress, "UTF-8")); /*검색할주소*/
            urlBuilder.append("&" + URLEncoder.encode("key", "UTF-8") + "=" + URLEncoder.encode("ENTER_API_KEY", "UTF-8")); /*APi키*/
            urlBuilder.append("&" + URLEncoder.encode("language", "UTF-8") + "=" + URLEncoder.encode("ko", "UTF-8")); /*언어*/

            URL url = new URL(urlBuilder.toString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-type", "text/xml;charset=UTF-8");
            //System.out.println("Response code: " + conn.getResponseCode());
            BufferedReader rd;

            InputStream is = url.openStream();

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();//xml파싱을 위한
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(new InputStreamReader(is, "UTF-8")); //inputstream 으로부터 xml 입력받기

            String tag;
            xpp.next();
            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        break;

                    case XmlPullParser.START_TAG:
                        tag = xpp.getName();//테그 이름 얻어오기

                        if (tag.equals("result")) ;// 첫번째 검색결과
                        else if (tag.equals("location")) ;
                        else if (tag.equals("lat") && !islat) {
                            xpp.next();
                            locationXY.append(xpp.getText() + ",");//title 요소의 TEXT 읽어와서 문자열버퍼에 추가
                            islat = true;
                        } else if (tag.equals("lng") && !islng) {
                            xpp.next();
                            locationXY.append(xpp.getText());//category 요소의 TEXT 읽어와서 문자열버퍼에 추가
                            islng = true;
                        }
                        break;

                    case XmlPullParser.TEXT:
                        break;

                    case XmlPullParser.END_TAG:
                        tag = xpp.getName(); //테그 이름 얻어오기

                        if (tag.equals("result")) ;// 첫번째 검색결과종료..줄바꿈
                        break;
                }

                eventType = xpp.next();
            }

        } catch (Exception e) {
            locationXY.append("error \n\n");
        }

        return locationXY.toString();//StringBuffer 문자열 객체 반환
    }

    //입력받은 범위내 모든 도로 반환 함수
    String getXmlData(String minX, String maxX, String minY, String maxY, String drcType) {
        StringBuffer buffer = new StringBuffer();
        try {
            StringBuilder urlBuilder = new StringBuilder("https://openapi.its.go.kr:9443/trafficInfo"); /*URL*/

            urlBuilder.append("?" + URLEncoder.encode("apiKey", "UTF-8") + "=" + URLEncoder.encode("ENTER_API_KEY", "UTF-8")); /*API 입력*/
            urlBuilder.append("&" + URLEncoder.encode("type", "UTF-8") + "=" + URLEncoder.encode("all", "UTF-8")); /*도로유형*/
            urlBuilder.append("&" + URLEncoder.encode("routeNo", "UTF-8") + "=" + URLEncoder.encode("1", "UTF-8")); /*노선번호*/
            urlBuilder.append("&" + URLEncoder.encode("drcType", "UTF-8") + "=" + URLEncoder.encode(drcType, "UTF-8")); /*도로방향*/
            urlBuilder.append("&" + URLEncoder.encode("minX", "UTF-8") + "=" + URLEncoder.encode(minX, "UTF-8")); /*최소경도영역*/
            urlBuilder.append("&" + URLEncoder.encode("maxX", "UTF-8") + "=" + URLEncoder.encode(maxX, "UTF-8")); /*최대경도영역*/
            urlBuilder.append("&" + URLEncoder.encode("minY", "UTF-8") + "=" + URLEncoder.encode(minY, "UTF-8")); /*최소위도영역*/
            urlBuilder.append("&" + URLEncoder.encode("maxY", "UTF-8") + "=" + URLEncoder.encode(maxY, "UTF-8")); /*최대위도영역*/
            urlBuilder.append("&" + URLEncoder.encode("getType", "UTF-8") + "=" + URLEncoder.encode("xml", "UTF-8")); /*출력타입*/

            URL url = new URL(urlBuilder.toString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-type", "text/xml;charset=UTF-8");
            //System.out.println("Response code: " + conn.getResponseCode());
            BufferedReader rd;

            InputStream is = url.openStream();

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();//xml파싱을 위한
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(new InputStreamReader(is, "UTF-8")); //inputstream 으로부터 xml 입력받기

            String tag;
            xpp.next();
            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                Node node = new Node("","","","","","");
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        buffer.append("파싱 시작...\n\n");
                        break;

                    case XmlPullParser.START_TAG:
                        tag = xpp.getName();//테그 이름 얻어오기

                        String tmp;
                        if (tag.equals("totalCount")){
                            xpp.next();
                            totalCount=Integer.parseInt(xpp.getText());
                            linkBox=new String[totalCount][7]; //영역 안의 link 개수만큼 array선언
                        }
                        else if (tag.equals("item")) ;// 첫번째 검색결과
                        /* 도로명 필요 없음
                        else if (tag.equals("roadName")) {
                            buffer.append("도로명 : ");
                            xpp.next();
                            buffer.append(xpp.getText());//title 요소의 TEXT 읽어와서 문자열버퍼에 추가
                            buffer.append("\n"); //줄바꿈 문자 추가

                        }
                        */else if (tag.equals("linkId")) {
                            buffer.append("linkId : ");
                            xpp.next();

                            tmp=xpp.getText();
                            linkBox[cntLink][0]=tmp;

                            buffer.append(tmp);//category 요소의 TEXT 읽어와서 문자열버퍼에 추가
                            buffer.append("\n");//줄바꿈 문자 추가
                        } else if (tag.equals("startNodeId")) {
                            buffer.append("startNodeId :");
                            xpp.next();

                            tmp=xpp.getText();
                            linkBox[cntLink][1]=tmp;
                            set.add(tmp);

                            buffer.append(tmp);//description 요소의 TEXT 읽어와서 문자열버퍼에 추가
                            buffer.append("\n");//줄바꿈 문자 추가
                        } else if (tag.equals("endNodeId")) {
                            buffer.append("endNodeId :");
                            xpp.next();

                            tmp=xpp.getText();
                            linkBox[cntLink][2]=tmp;
                            set.add(tmp);

                            buffer.append(tmp);//telephone 요소의 TEXT 읽어와서 문자열버퍼에 추가
                            buffer.append("\n");//줄바꿈 문자 추가
                        } else if (tag.equals("speed")) {
                            buffer.append("speed :");
                            xpp.next();

                            tmp=xpp.getText();
                            linkBox[cntLink][3]=tmp;

                            buffer.append(tmp);//address 요소의 TEXT 읽어와서 문자열버퍼에 추가
                            buffer.append("\n");//줄바꿈 문자 추가
                        } else if (tag.equals("travelTime")) {
                            buffer.append("travelTime :");
                            xpp.next();

                            tmp=xpp.getText();
                            linkBox[cntLink][4]=tmp;
                            linkBox[cntLink][5]= Double.toString(Double.parseDouble(linkBox[cntLink][3]) * Double.parseDouble(linkBox[cntLink][4]) * 10 / 36);
                            cntLink++;

                            buffer.append(tmp);//mapx 요소의 TEXT 읽어와서 문자열버퍼에 추가
                            buffer.append("  ,  "); //줄바꿈 문자 추가

                        }
                        break;

                    case XmlPullParser.TEXT:
                        break;

                    case XmlPullParser.END_TAG:
                        tag = xpp.getName(); //테그 이름 얻어오기

                        if (tag.equals("item")) buffer.append("\n");// 첫번째 검색결과종료..줄바꿈
                        break;
                }

                eventType = xpp.next();
            }
        } catch (Exception e) {
            buffer.append("error \n\n");
        }
        buffer.append("파싱 끝\n");
        System.out.println();
        return buffer.toString();//StringBuffer 문자열 객체 반환
    }



    //******가장 빠른 경로 찾기! //dijkstra 사용하면 자동으로 결과 출력! //최적의 링크 ID를 사용하고 싶다면 printSolution을 변경하면 됩니다
    //nearNode 부분의 코드 수정 필요!@
    //fill node to node cost
    void fill() {
        Set<String> set2=new HashSet<String>(set);
        set=new ArrayList<String>(set2);

        cntNode=set.size();
        graph=new double[cntNode][cntNode];
        nodeBox=new double[cntNode][3];
        route=new double[cntNode];
        answer=new double[cntNode];

        for(int i=0; i<cntNode; i++) {
            for(int j=0; j<cntNode; j++) {
                if(i==j) {
                    graph[i][j]=0;
                    continue;
                }
                String f,t;
                f=set.get(i);
                t=set.get(j);
                for(int k=0; k<cntLink; k++) {
                    if( (f.equals(linkBox[k][1])) && (t.equals(linkBox[k][2])) ) {
                        graph[i][j]=Double.parseDouble(linkBox[k][5]);
                        break;
                    }
                    else
                        graph[i][j]=9999999;
                }
            }
        }
    }
    void nearNode() throws IOException {
        //change to your route
        InputStreamReader is = new InputStreamReader(getResources().openRawResource(R.raw.xynode1));
        BufferedReader reader = new BufferedReader(is);
        CSVReader br = new CSVReader(reader);
        String[] line = null;
        if(is == null){
            Log.d("My tag","File not found");
        }
        else{
            Log.d("My tag","file found");
        }
//        BufferedReader br=new BufferedReader(new FileReader(node));
//        String tmp;
        Log.d("My tag","Check");
        int cnt=0;
        while (true) {
            try {
                if (!((line = br.readNext()) != null)) {
                    Log.d("My tag","br null");
                    break;
                }
            } catch (CsvValidationException e) {
                e.printStackTrace();
            }
            Log.d("My tag",String.join(",", line));
        }
//        while((tmp=br.readLine()) != null) {
//            //NODE_ID,lat,lng
//            String[] line=tmp.split(",");
//            if(set.contains(line[0])) {
//                nodeBox[cnt][0]=Double.parseDouble(line[0]);
//                nodeBox[cnt][1]=Double.parseDouble(line[2]);
//                nodeBox[cnt][2]=Double.parseDouble(line[1]);
//                cnt++;
//            }
//            Log.d("My tag","Check2");
//        }

        double xx,yy;
        double result,min=9999999;
        int minIndex=0;
        for(int i=0; i<cntNode; i++) {
            //************************************need to fix here //how to get startX,Y? I think minX,Y is not equal startX,Y(maybe..)
            xx=nodeBox[i][1]-startX;
            yy=nodeBox[i][2]-startY;
            //************************************
            result=Math.sqrt(Math.pow(xx, 2)+Math.pow(yy, 2));
            if(min>result) {
                minIndex=i;
                min=result;
            }
        }
        for(int i=0; i<3; i++)
            startNode[i]=nodeBox[minIndex][i];
        startNodeIndex=minIndex;
        minIndex=0;
        min=999999999;
        for(int i=0; i<cntNode; i++) {
            //************************************need to fix here //how to get endX,Y?
            xx=nodeBox[i][1]-endX;
            yy=nodeBox[i][2]-endY;
            //************************************
            result=Math.sqrt(Math.pow(xx, 2)+Math.pow(yy, 2));
            if(min>result) {
                minIndex=i;
                min=result;
            }
        }
        for(int i=0; i<3; i++)
            startNode[i]=nodeBox[minIndex][i];
        endNodeIndex=minIndex;
    }
    int minDistance(double dist[], Boolean sptSet[])
    {
        double min = 99999999;
        int min_index = 0;

        for (int v = 0; v < cntNode; v++)
        {
            if (!sptSet[v] && min > dist[v])
            {
                min_index = v;
                min = dist[v];
            }
        }

        return min_index;
    }
    //print start,end,distance
    void printSolution(double dist[], int n)
    {
        System.out.printf("\nDistance\n");
        System.out.printf("%.0f to %.0f : %.8lf(m)\n",startNode[0], endNode[0], dist[endNodeIndex]);
    }
    //dijkstra algorithm
    void dijkstra()
    {
        int src=startNodeIndex;
        double dist[]=new double[cntNode]; // 최단 거리를 파악하는 배열
        Boolean sptSet[]=new Boolean[cntNode]; // 방문 했는지 체크 하는 bool형 배열

        for (int i = 0; i<cntNode; i++) {
            dist[i] = 99999999;
            sptSet[i] = false;
        }

        // 초기 조건 설정.
        dist[src] = 0;

        // cntNode-1번 루프를 수행한다는 것은 첫 src노드를 제외한 모든 노드들에 접근을 해 계산을 한다는 의미.
        for (int count = 0; count < cntNode - 1; count++)
        {
            // 최단거리 정보를 알고 있는 노드들 중 가장 거리가 짧은 노드의 인덱스를 가져온다.
            int u = minDistance(dist, sptSet);

            // 그래프 상의 모든 노드들을 탐색하며 u 노드의 주변 정보를 갱신한다.
            for (int v = 0; v < cntNode; v++)
            {
                // 1. 아직 처리가 되지 않은 노드이어야 하며 (무한루프 방지)
                // 2. u-v 간에 edge가 존재하고
                // 3. src부터 u까지의 경로가 존재하고
                // 4. 기존의 v노드까지의 최단거리 값보다 새로 계산되는 최단거리가 더 짧을 경우
                if ( (!sptSet[v])  && (dist[u] != 99999999) && (dist[v] > dist[u] + graph[u][v]) )
                {
                    // 최단거리를 갱신해준다.
                    dist[v] = dist[u] + graph[u][v];
                    route[v]=u;
                }
            }

            // 이제 이 노드(u)는 접근할 일이 없다. 플래그를 true로 설정.
            sptSet[u] = true;

            // 현재까지의 최단 거리를 출력해준다.
            //printSolution(dist, cntNode);
        }
        //find visited node
        int idx=endNodeIndex;
        int i=0;
        while(idx!=startNodeIndex){
            answer[i]=idx;
            idx=(int)route[idx];
            i++;
        }
        answer[i]=idx;

        System.out.printf("\nvisited route(node's index in box)\n");
        for(int j=i; j>=0; j--){
            System.out.printf("%.0f ",answer[j]);
        }
        System.out.printf("\nvisited route(linkID)\n");
        for(int j=i; j>0; j--){
            double id = 0;
            for(int k=0; k<cntLink; k++){
                if((linkBox[k][1].equals(nodeBox[(int)answer[j]][0])) && (linkBox[k][2].equals(nodeBox[(int)answer[j-1]][0])))
                {
                    id=Double.parseDouble(linkBox[k][0]);
                    break;
                }
            }
            System.out.printf("%.0f\n",id);
        }

        printSolution(dist,cntNode);
    }
    //******
}