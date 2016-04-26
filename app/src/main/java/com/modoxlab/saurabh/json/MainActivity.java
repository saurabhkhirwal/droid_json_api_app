package com.modoxlab.saurabh.json;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private ListView lvOutput;
    private EditText etName;
    private EditText etEmail;
    private EditText etField;
    private EditText etID;
    private ProgressBar pbLoad;
    private RadioButton rbAdd;
    private RadioButton rbSearch;
    private RadioButton rbRemove;
    private Button btnSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.modoxlab.dedsec.json.R.layout.activity_main);
        etName = (EditText) findViewById(com.modoxlab.dedsec.json.R.id.etName);
        etEmail = (EditText) findViewById(com.modoxlab.dedsec.json.R.id.etEmail);
        etField = (EditText) findViewById(com.modoxlab.dedsec.json.R.id.etField);
        lvOutput = (ListView) findViewById(com.modoxlab.dedsec.json.R.id.lvOutput);
        pbLoad = (ProgressBar) findViewById(com.modoxlab.dedsec.json.R.id.pbLoad);
        etID = (EditText) findViewById(com.modoxlab.dedsec.json.R.id.etID);
        rbAdd = (RadioButton) findViewById(com.modoxlab.dedsec.json.R.id.rbAdd);
        rbSearch = (RadioButton) findViewById(com.modoxlab.dedsec.json.R.id.rbSearch);
        rbRemove = (RadioButton) findViewById(com.modoxlab.dedsec.json.R.id.rbRemove);
        btnSubmit = (Button) findViewById(com.modoxlab.dedsec.json.R.id.btnSubmit);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(com.modoxlab.dedsec.json.R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == com.modoxlab.dedsec.json.R.id.asList) {
            new SyncRefresh().execute("http://madebymask.com/api/index.php");
        }
        return super.onOptionsItemSelected(item);
    }

    public void btnAddHandler(View view) {
        etID.setText("");
        etEmail.setText("");
        etField.setText("");
        etName.setText("");
        lvOutput.setAdapter(null);
        etID.setVisibility(View.GONE);
        etName.setVisibility(View.VISIBLE);
        etEmail.setVisibility(View.VISIBLE);
        etField.setVisibility(View.VISIBLE);
    }

    public void btnSearchHandler(View view) {
        etID.setText("");
        etEmail.setText("");
        etField.setText("");
        etName.setText("");
        lvOutput.setAdapter(null);
        etID.setVisibility(View.VISIBLE);
        etName.setVisibility(View.GONE);
        etEmail.setVisibility(View.GONE);
        etField.setVisibility(View.GONE);
    }

    public void btnRemoveHandler(View view) {
        etID.setText("");
        etEmail.setText("");
        etField.setText("");
        etName.setText("");
        lvOutput.setAdapter(null);
        etID.setVisibility(View.VISIBLE);
        etName.setVisibility(View.GONE);
        etEmail.setVisibility(View.GONE);
        etField.setVisibility(View.GONE);
    }

    public void btnSubmitHandler(View view) {
        if (rbAdd.isChecked()) {
            new SyncAdd().execute("http://madebymask.com/api/index.php?func=add_user", etName.getText().toString(), etEmail.getText().toString(), etField.getText().toString());
        } else if (rbSearch.isChecked()) {
            new SyncSearch().execute("http://madebymask.com/api/index.php?func=get_user_by_id", etID.getText().toString());
        } else {
            new SyncRemove().execute("http://madebymask.com/api/index.php?func=remove_user", etID.getText().toString());
        }
    }

    private class SyncRefresh extends AsyncTask<String, Void, List<JSONModel>> {

        private String jsonString;

        @Override
        protected List<JSONModel> doInBackground(String... params) {
            String temp;
            try {
                URL url = new URL(params[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream inputStream = connection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder stringBuilder = new StringBuilder();
                while ((temp = bufferedReader.readLine()) != null) {
                    stringBuilder.append(temp);
                }
                jsonString = stringBuilder.toString();
                JSONObject jsonObject = new JSONObject(jsonString);
                JSONArray jsonArray = jsonObject.getJSONArray("users");
                List<JSONModel> jsonModelList = new ArrayList<>();
                Gson gson = new Gson();
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject tempObj = jsonArray.getJSONObject(i);
                    jsonModelList.add(gson.fromJson(tempObj.toString(), JSONModel.class));
                }
                inputStream.close();
                bufferedReader.close();
                connection.disconnect();
                return jsonModelList;
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            btnSubmit.setVisibility(View.GONE);
            pbLoad.setVisibility(View.VISIBLE);
            lvOutput.setAdapter(null);
        }

        @Override
        protected void onPostExecute(List<JSONModel> jsonModels) {
            pbLoad.setVisibility(View.GONE);
            btnSubmit.setVisibility(View.VISIBLE);
            if (jsonModels == null) {
                Toast.makeText(getApplicationContext(), "Please Check Your Internet Connection!", Toast.LENGTH_SHORT).show();
            } else {
                Adapter adapter = new Adapter(getApplicationContext(), com.modoxlab.dedsec.json.R.layout.layout_holder, jsonModels);
                lvOutput.setAdapter(adapter);
            }
        }
    }

    private class SyncAdd extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL(params[0]);
                Map<String, Object> e = new LinkedHashMap<>();
                e.put("name", params[1]);
                e.put("email", params[2]);
                e.put("field", params[3]);
                StringBuilder postData = new StringBuilder();
                for (Map.Entry<String, Object> param : e.entrySet()) {
                    if (postData.length() != 0) postData.append('&');
                    postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                    postData.append('=');
                    postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
                }
                byte[] postDataBytes = postData.toString().getBytes("UTF-8");

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
                conn.setDoOutput(true);
                conn.getOutputStream().write(postDataBytes);
                Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                StringBuilder sb = new StringBuilder();
                for (int c; (c = in.read()) >= 0; )
                    sb.append((char) c);
                String jsonFile = sb.toString();
                JSONObject jsonObject = new JSONObject(jsonFile);
                String response = jsonObject.getString("message");
                in.close();
                conn.disconnect();
                return response;
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }


        @Override
        protected void onPreExecute() {
            pbLoad.setVisibility(View.VISIBLE);
            btnSubmit.setVisibility(View.GONE);
            lvOutput.setAdapter(null);
        }

        @Override
        protected void onPostExecute(String s) {
            pbLoad.setVisibility(View.GONE);
            btnSubmit.setVisibility(View.VISIBLE);
            if (s == null) {
                Toast.makeText(getApplicationContext(), "Please Check Your Internet Connection!", Toast.LENGTH_SHORT).show();
            } else {
                etID.setText("");
                etEmail.setText("");
                etField.setText("");
                etName.setText("");
                Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class SyncSearch extends AsyncTask<String, Void, List<JSONModel>> {
        String response;

        protected List<JSONModel> doInBackground(String... params) {
            try {
                URL url = new URL(params[0]);
                Map<String, Object> e = new LinkedHashMap<>();
                e.put("id", params[1]);
                StringBuilder postData = new StringBuilder();
                for (Map.Entry<String, Object> param : e.entrySet()) {
                    if (postData.length() != 0) postData.append('&');
                    postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                    postData.append('=');
                    postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
                }
                byte[] postDataBytes = postData.toString().getBytes("UTF-8");

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
                conn.setDoOutput(true);
                conn.getOutputStream().write(postDataBytes);
                Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                StringBuilder sb = new StringBuilder();
                for (int c; (c = in.read()) >= 0; )
                    sb.append((char) c);
                String jsonFile = sb.toString();
                JSONObject jsonObject = new JSONObject(jsonFile);
                response = jsonObject.getString("message");
                JSONObject jsonObject1 = jsonObject.getJSONObject("user_details");
                List<JSONModel> jsonModelList = new ArrayList<>();
                Gson gson = new Gson();
                jsonModelList.add(gson.fromJson(jsonObject1.toString(), JSONModel.class));
                in.close();
                conn.disconnect();
                return jsonModelList;
            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPreExecute() {
            pbLoad.setVisibility(View.VISIBLE);
            btnSubmit.setVisibility(View.GONE);
            lvOutput.setAdapter(null);
        }

        @Override
        protected void onPostExecute(List<JSONModel> jsonModels) {
            pbLoad.setVisibility(View.GONE);
            btnSubmit.setVisibility(View.VISIBLE);
            if (response == null) {
                Toast.makeText(getApplicationContext(), "Please Check Your Internet Connection!", Toast.LENGTH_SHORT).show();
            } else if (jsonModels == null) {
                Toast.makeText(getApplicationContext(), response, Toast.LENGTH_SHORT).show();
            } else {
                etID.setText("");
                etEmail.setText("");
                etField.setText("");
                etName.setText("");
                Adapter adapter = new Adapter(getApplicationContext(), com.modoxlab.dedsec.json.R.layout.layout_holder, jsonModels);
                lvOutput.setAdapter(adapter);
            }
        }
    }

    private class SyncRemove extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL(params[0]);
                Map<String, Object> e = new LinkedHashMap<>();
                e.put("id", params[1]);
                StringBuilder postData = new StringBuilder();
                for (Map.Entry<String, Object> param : e.entrySet()) {
                    if (postData.length() != 0) postData.append('&');
                    postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                    postData.append('=');
                    postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
                }
                byte[] postDataBytes = postData.toString().getBytes("UTF-8");

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
                conn.setDoOutput(true);
                conn.getOutputStream().write(postDataBytes);
                Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                StringBuilder sb = new StringBuilder();
                for (int c; (c = in.read()) >= 0; )
                    sb.append((char) c);
                String jsonFile = sb.toString();
                JSONObject jsonObject = new JSONObject(jsonFile);
                String response = jsonObject.getString("message");
                in.close();
                conn.disconnect();
                return response;
            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            btnSubmit.setVisibility(View.GONE);
            pbLoad.setVisibility(View.VISIBLE);
            lvOutput.setAdapter(null);
        }

        @Override
        protected void onPostExecute(String s) {
            pbLoad.setVisibility(View.GONE);
            btnSubmit.setVisibility(View.VISIBLE);
            if (s == null) {
                Toast.makeText(getApplicationContext(), "Please Check Your Internet Connection!", Toast.LENGTH_SHORT).show();
            } else {
                etID.setText("");
                etEmail.setText("");
                etField.setText("");
                etName.setText("");
                Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class Adapter extends ArrayAdapter {

        private List<JSONModel> jsonModelList;
        private int resource;
        private LayoutInflater layoutInflater;

        public Adapter(Context context, int resource, List<JSONModel> objects) {
            super(context, resource, objects);
            this.resource = resource;
            jsonModelList = objects;
            layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Holder holder;
            if (convertView == null) {
                holder = new Holder();
                convertView = layoutInflater.inflate(resource, null);
                holder.tvID = (TextView) convertView.findViewById(com.modoxlab.dedsec.json.R.id.tvID);
                holder.tvName = (TextView) convertView.findViewById(com.modoxlab.dedsec.json.R.id.tvName);
                holder.tvEmail = (TextView) convertView.findViewById(com.modoxlab.dedsec.json.R.id.tvEmail);
                holder.tvField = (TextView) convertView.findViewById(com.modoxlab.dedsec.json.R.id.tvField);
                convertView.setTag(holder);
            } else {
                holder = (Holder) convertView.getTag();
            }
            holder.tvID.setText("ID: " + jsonModelList.get(position).getId());
            holder.tvName.setText(jsonModelList.get(position).getName());
            holder.tvEmail.setText("Email: " + jsonModelList.get(position).getEmail());
            holder.tvField.setText("Field: " + jsonModelList.get(position).getField());
            return convertView;
        }
    }

    private class Holder {
        private TextView tvName;
        private TextView tvID;
        private TextView tvEmail;
        private TextView tvField;
    }
}