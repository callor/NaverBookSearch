package com.callor.naverbook;

import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.callor.naverbook.databinding.ActivityMainBinding;
import com.callor.naverbook.databinding.ContentMainBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding mainBinding;
    ContentMainBinding contentBinding;
    List<NaverBookVO> books;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mainBinding = DataBindingUtil.setContentView(this,R.layout.activity_main);
        contentBinding = mainBinding.contentMain;
        setSupportActionBar(mainBinding.toolbar);

        contentBinding.btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Async 클래스를 객체로 생성
                NaverSearch naverSearch = new NaverSearch();

                // Async 작동 시작!!
                naverSearch.execute();
            }
        });

        mainBinding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    // Naver에 비동기 방식으로 데이터를 가져오는 주체
    class NaverSearch extends AsyncTask<Integer, Integer, Void> {

        // 비동기로 실제 작업이 이루어지는 부분
        @Override
        protected Void doInBackground(Integer... integers) {
            naverSearch(); // 비동방식으로 method 처리
            return null;
        }

        // 비동기 실행이 정상적으로 종료(완료)되면 자동 호출되는 method
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            // Adapter와 RecylerView 연결
            BookAdapter adapter = new BookAdapter(MainActivity.this,books);
            contentBinding.booksList.setAdapter(adapter);

            StaggeredGridLayoutManager layoutManager
                    = new StaggeredGridLayoutManager(1,StaggeredGridLayoutManager.VERTICAL);
            contentBinding.booksList.setLayoutManager(layoutManager);

        }
    }

    public void naverSearch() {

        try {

            // 검색어를 encoding
            String searchString = contentBinding.txtBookSearch.getText().toString();
            String text = URLEncoder.encode(searchString,"UTF-8");
            String apiURL = "https://openapi.naver.com/v1/search/book.json";
            apiURL += "?query=" + text;
            apiURL += "&display=20" ;

            Log.d("NAVER:",apiURL);
            URL url = new URL(apiURL);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();

            // Header 만들기
            Log.d("NAVER:","02");
            connection.setRequestMethod("GET");
            connection.setRequestProperty("X-Naver-Client-Id",NaverSecret.NAVER_CLIENT_ID);
            connection.setRequestProperty("X-Naver-Client-Secret",NaverSecret.NAVER_CLIENT_SECRET);

            // 네이버에 GET으로 조회하는 주체
            Log.d("NAVER:","03");
            int responseCode = connection.getResponseCode();
            BufferedReader buffer ;
            if(responseCode == 200) { // 요청이 정상적이어서 데이터가 정상으로 도착할 것이다.
                buffer = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            } else {
                return ;
            }
            Log.d("NAVER:","04");
            String inputLine ;
            String jsonString = new String();

            // 데이터가 정상적으로 읽고 있으면
            while((inputLine = buffer.readLine()) != null) {
                jsonString += inputLine;
            }
            buffer.close(); //  네트워크 종료
            Log.d("NAVER:","05");

            // 네이버로 부터 전송되는 data는 json 구조를 가진 String이므로
            // json Object로 변경해줘야 내부에서 원활하게 사용할 수 있다.
            JSONObject resJSON = new JSONObject(jsonString);

            // 도서목록(정보가 담겨있는)
            //      네이버는 items라는 이름으로 Array형태의 데이터를 보낸다
            //          items key를 참조하여 하위 Data를 JSONArray 형태로 추출한다.
            JSONArray items = resJSON.getJSONArray("items");

            books = new ArrayList<NaverBookVO>();
            for(int i = 0 ; i < items.length() ; i++) {

                // item을 하나씩 뽑아내기
                JSONObject item = items.getJSONObject(i);
                Log.d("NAVER:",item.getString("title")); // 디버깅창에서 데이터 확인하기

                NaverBookVO vo = new NaverBookVO();
                vo.setTitle(item.getString("title"));
                vo.setLink(item.getString("link"));
                vo.setImage(item.getString("image"));
                vo.setDescription(item.getString("description"));

                vo.setAuthor(item.getString("author"));
                vo.setPrice(item.getString("price"));

                books.add(vo);

            }

        } catch (UnsupportedEncodingException e) {
            // URLEncoder
            e.printStackTrace();
        } catch (MalformedURLException e) {
            // URL 생성할때
            e.printStackTrace();
        } catch (IOException e) {
            // 읽을때
            e.printStackTrace();
        } catch (JSONException e) {
            // JSONObject로 변경할때
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
