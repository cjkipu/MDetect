package com.jobchumo.mddetect;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.NetworkOnMainThreadException;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import io.github.redouane59.twitter.TwitterClient;
import io.github.redouane59.twitter.dto.endpoints.AdditionalParameters;
import io.github.redouane59.twitter.dto.tweet.Tweet;
import io.github.redouane59.twitter.dto.tweet.TweetList;
import io.github.redouane59.twitter.dto.tweet.TweetV2;
import io.github.redouane59.twitter.dto.user.User;
import io.github.redouane59.twitter.signature.TwitterCredentials;

public class TwitterActivity extends AppCompatActivity {

    protected  static TwitterClient twitterClient;
    protected EditText username;
    protected FirebaseUser firebaseUser;
    protected FirebaseAuth firebaseAuth;
    protected FirebaseDatabase firebaseDatabase;
    protected DatabaseReference databaseReference;
    protected DatabaseReference mUser;
    protected ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_twitter);

        twitterClient = new TwitterClient(TwitterCredentials.builder()
                .accessToken("717751924129472512-wyxzUq8Os3IgMprko2pAkkFLSEQ59q2")
                .accessTokenSecret("4Tq0uQXt95eKxrmr6RA7hQP40SPTzpG7Hujek5ftZT3ZY")
                .apiKey("24xvUJF9KUdjseqCY2FTkVCc5")
                .apiSecretKey("RzPVUxgeXYzgUfR8vrAsdRC0yEZqCa4KJkK9DXaNtOlXB0GV24")
                .bearerToken("AAAAAAAAAAAAAAAAAAAAAKzISwEAAAAArZZ4ThC0joFGnlENHNZPVHZfrdo%3DJc8MadyRgarKojzrhcaH0pJS2w8oBXT6u1IDvAcaDfjW0ILqG4")
                .build());

        username = findViewById(R.id.twitterUsername);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        mUser = FirebaseDatabase.getInstance().getReference("Timeline");
        progressDialog = new ProgressDialog(this);


    }

    public void backAction(View view) {
        startActivity(new Intent(TwitterActivity.this, MainActivity.class));
    }


    private class MyTask extends AsyncTask<Void, Void, Void>{
        String usernam = username.getText().toString();
        String id;
        List<String> tweets = new ArrayList<>();
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                User user = twitterClient.getUserFromUserName(usernam);
                id = user.getId();
                Log.d("UserID", id);

                TweetList userTimeline = twitterClient.getUserTimeline(id, AdditionalParameters.builder().maxResults(10).build());
                List<TweetV2.TweetData> tweetList = userTimeline.getData();
                for (int i = 0; i<tweetList.size(); i++) {
                    String nu = tweetList.get(i).getText();
                    tweets.add(nu);
                    Log.d("tweet Content", nu);
                }
                for (int i = 0; i<tweets.size(); i++) {
                    String nu = tweets.get(i);
                    Log.d("tweet Content", nu);
                }

            } catch (NetworkOnMainThreadException e){
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            String usernam = username.getText().toString();

            twitterTweets(id, tweets);
            super.onPostExecute(aVoid);
        }
    }

    private void twitterTweets(String Id, List<String> tweets) {

        progressDialog.setMessage("Getting your timeline");
        progressDialog.show();

        String userName = username.getText().toString();
        TimelineInfo info = new TimelineInfo(Id, tweets);

        Log.d("Username", userName);

        databaseReference.child("Timeline").child(firebaseUser.getUid()).setValue(info)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(TwitterActivity.this,"Timeline Saved",Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                    else {
                        FirebaseAuthException e = (FirebaseAuthException )task.getException();
                        Toast.makeText(TwitterActivity.this, "Failed To Save Information: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                });


    }

    public void userTimeline(View view) {
        String userName = username.getText().toString().trim();

        if (TextUtils.isEmpty(userName)) {
            Toast.makeText(TwitterActivity.this, "Empty Field! \nPlease Enter Your Username", Toast.LENGTH_SHORT).show();
        }
        else {
            progressDialog.setMessage("Getting your timeline");
            progressDialog.show();
            new MyTask().execute();
        }

    }

    public void mood(View view) {
        //add code for getting the ml result here then pass it to the mood activity class
        //or pass the username to the mood activity class then do the url string request code there
        startActivity(new Intent(TwitterActivity.this, MoodActivity.class));
    }

}