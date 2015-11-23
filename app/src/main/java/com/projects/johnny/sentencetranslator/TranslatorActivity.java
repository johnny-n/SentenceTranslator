package com.projects.johnny.sentencetranslator;

import android.os.AsyncTask;
import android.renderscript.ScriptGroup;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.MalformedInputException;

public class TranslatorActivity extends AppCompatActivity {

    EditText translateEditText;
    Button translateButton;
    TextView translationTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translator);

        translateEditText = (EditText) findViewById(R.id.translate_edit_text);
        translateButton = (Button) findViewById(R.id.translate_button);
        translationTextView = (TextView) findViewById(R.id.translation_text_view);


        translateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Checks if sentence is empty
                boolean isEmpty = translateEditText.getText().toString().trim().isEmpty();
                String sentenceToTranslate = translateEditText.getText().toString();

                if (!isEmpty) {
                    Toast.makeText(v.getContext(), "Translating data...", Toast.LENGTH_LONG).show();

                    // calls doInBackground() method for the RunInBackground class
                    new RunInBackground(sentenceToTranslate).execute();
                } else {
                    Toast.makeText(v.getContext(), "Please enter a sentence to translate", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * To prevent the application from locking up, we will have the
     *  translation process run in the background rather than
     *  having to wait for it to finish.
     */
    class RunInBackground extends AsyncTask<Void, Void, Void> {
        String JSONString = "";
        String result = "";
        String sentenceToTranslate;
        URL url;
        HttpURLConnection urlConnection;

        // Pass sentence to translate as argument in the constructor
        //  to avoid "Method getText() must be called from UI thread" error
        RunInBackground(String sentence) {
            sentenceToTranslate = sentence.replace(" ", "+");
        }

        // Remember that we call onPostExecute after this method is finished processing
        // doInBackground() cannot modify anything in the interface
        @Override
        protected Void doInBackground(Void... params) {
            InputStream inputStream = null;
            BufferedReader reader = null;

            // try-catch blocks needed for httpurlconnection/url assignments
            try {
                url = new URL("http://newjustin.com/translateit.php?action=translations&english_words=" + sentenceToTranslate);
                urlConnection = (HttpURLConnection) url.openConnection();
                inputStream = urlConnection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));

                StringBuilder stringBuilder = new StringBuilder();
                String line = null;

                while ( (line = reader.readLine()) != null) {
                    stringBuilder.append(line + "\n");
                }

                JSONString = stringBuilder.toString();
                JSONObject jObject = new JSONObject(JSONString);
                JSONArray jArray = jObject.getJSONArray("translations");

                outputTranslations(jArray);

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        // To be called after asynchronous background
        //  This method is allowed to modify the interface
        @Override
        protected void onPostExecute(Void aVoid) {
            translationTextView.setText(result);
        }

        protected void outputTranslations(JSONArray jsonArray) {
            String[] languages = {
                    "arabic",
                    "chinese",
                    "danish",
                    "dutch",
                    "french",
                    "german",
                    "italian",
                    "portuguese",
                    "russian",
                    "spanish"
            };

            try {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject translationObject = jsonArray.getJSONObject(i);
                    result += languages[i] + " : " + translationObject.getString(languages[i]) + "\n";
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}
