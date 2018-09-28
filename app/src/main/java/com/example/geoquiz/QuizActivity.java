package com.example.geoquiz;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.Question;            //page 97

public class QuizActivity extends AppCompatActivity {                   //страница 126

    private Button mTrueButton;
    private Button mFalseButton;
    private Button mNextButton;
    private Button mPrevButton;
    private Button mCheatButton;
    private TextView mQuestionTextView;

    private static boolean isButtonPressed = false;
    private static boolean isQuizEnded = false;

    private static final String TAG  = "QuizActivity";
    private static final String KEY_INDEX = "index";
    private static final String IS_PRESSED = "index2";
    private static final String ANSWERED_QUESTIONS = "answered";
    private static final String RESULTS = "results";
    private static final String END = "end";


    private static final int REQUEST_CODE_CHEAT = 0;

    private Question[] mQuestionBank = new Question[]{
            new Question(R.string.question_australia,true),
            new Question(R.string.question_oceans,true),
            new Question(R.string.question_mideast,false),
            new Question(R.string.question_africa,false),
            new Question(R.string.question_americas,true),
            new Question(R.string.question_asia,true),
    };

    private boolean[] mIsQuestionAnswered = new boolean[mQuestionBank.length];      //почему нельзя создать массив не в куче?
    private int[] mResults = new int[mQuestionBank.length];

    private int mCurrentIndex = 0;
    private boolean mIsCheater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG,"onCreate(Bundle) called");
        setContentView(R.layout.activity_quiz);

        if (savedInstanceState != null) {
            mCurrentIndex = savedInstanceState.getInt(KEY_INDEX, 0);
            isButtonPressed = savedInstanceState.getBoolean(IS_PRESSED,false);
            mIsQuestionAnswered = savedInstanceState.getBooleanArray(ANSWERED_QUESTIONS);       // не указано значение по умолчанию, надеюсь прокатит
            mResults = savedInstanceState.getIntArray(RESULTS);
            isQuizEnded = savedInstanceState.getBoolean(END,false);
        }else {
            for (int i = 0; i < mIsQuestionAnswered.length; i++){
                mIsQuestionAnswered[i] = false;
            }
            for (int i = 0; i < mResults.length; i++){
                mResults[i] = 0;
            }
        }


        mQuestionTextView = (TextView) findViewById(R.id.question_text_view);

        mQuestionTextView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                mCurrentIndex = (mCurrentIndex + 1) % mQuestionBank.length;
                if (mIsQuestionAnswered[mCurrentIndex]){
                    disableButtons();
                }
                updateQuestion();
            }
        });
        updateQuestion();

        mTrueButton = (Button) findViewById(R.id.true_button);
        if (isButtonPressed == true){
            mTrueButton.setEnabled(false);
        }else {
            mTrueButton.setEnabled(true);
        }
        mFalseButton = (Button) findViewById(R.id.false_button);
        if (isButtonPressed == true){
            mFalseButton.setEnabled(false);
        }else {
            mFalseButton.setEnabled(true);
        }

        mTrueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isButtonPressed = true;
                mIsQuestionAnswered[mCurrentIndex] = true;
                disableButtons();
                checkAnswer(true);
            }
        });
        mFalseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isButtonPressed = true;
                mIsQuestionAnswered[mCurrentIndex] = true;
                disableButtons();
                checkAnswer(false);
            }
        });

        mPrevButton = (Button) findViewById(R.id.prev_button);
        mPrevButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                mCurrentIndex = (mCurrentIndex - 1) % mQuestionBank.length;
                if (mCurrentIndex<0){
                    mCurrentIndex=mCurrentIndex+mQuestionBank.length;
                }
                enableButtons();
                if (mIsQuestionAnswered[mCurrentIndex]){
                    disableButtons();
                }
                updateQuestion();
            }
        });

        mNextButton = (Button) findViewById(R.id.next_button);
        mNextButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                mCurrentIndex = (mCurrentIndex + 1) % mQuestionBank.length;
                enableButtons();
                if (mIsQuestionAnswered[mCurrentIndex]){
                    disableButtons();
                }
                updateQuestion();
            }
        });
        mCheatButton = (Button)findViewById(R.id.cheat_button);
        mCheatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //start cheat Activity
                boolean answerIsTrue = mQuestionBank[mCurrentIndex].isAnswerTrue();
                Intent intent = CheatActivity.newIntent(QuizActivity.this, answerIsTrue);
                startActivityForResult(intent,REQUEST_CODE_CHEAT);
            }
        });

        updateQuestion();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        Log.i(TAG, "onSaveInstanceState");
        savedInstanceState.putInt(KEY_INDEX, mCurrentIndex);
        savedInstanceState.putBoolean(IS_PRESSED,isButtonPressed);
        savedInstanceState.putBooleanArray(ANSWERED_QUESTIONS,mIsQuestionAnswered);
        savedInstanceState.putIntArray(RESULTS,mResults);
        savedInstanceState.putBoolean(END,isQuizEnded);
    }

    private void disableButtons(){
        isButtonPressed = true;
        mTrueButton = (Button) findViewById(R.id.true_button);
        mFalseButton = (Button) findViewById(R.id.false_button);
        mFalseButton.setEnabled(false);
        mTrueButton.setEnabled(false);
    }

    private void enableButtons(){
        isButtonPressed = false;
        mTrueButton = (Button) findViewById(R.id.true_button);
        mFalseButton = (Button) findViewById(R.id.false_button);
        mFalseButton.setEnabled(true);
        mTrueButton.setEnabled(true);
    }

    private void updateQuestion(){
        int question = mQuestionBank[mCurrentIndex].getTextResId();
        mQuestionTextView.setText(question);
        if (mIsQuestionAnswered[mCurrentIndex]){
            disableButtons();
        }
        if(checkAreAllQuestionsAnswered()){
            int result = 0;
            for (int i = 0; i < mResults.length; i++){
                result+=mResults[i];
            }
            int messageResId = 0;
            messageResId = R.string.results;
            if (!isQuizEnded){
                Toast.makeText(this,"Your results" + " " + result + "/" + mResults.length,Toast.LENGTH_SHORT).show();  // почему по id нельзя?
                isQuizEnded = true;
            }
        }
    }

    private boolean checkAreAllQuestionsAnswered(){
        boolean flag = true;
        for (int i = 0; i < mIsQuestionAnswered.length; i++){
            if (!mIsQuestionAnswered[i]){
                flag = false;
            }
        }
        return flag;
    }


    private void checkAnswer(boolean userPressedTrue){
        boolean answerIsTrue = mQuestionBank[mCurrentIndex].isAnswerTrue();

        int messageResId = 0;

        if (userPressedTrue == answerIsTrue){
            messageResId = R.string.correct_toast;
            mResults[mCurrentIndex]=1;
        }else {
            messageResId = R.string.incorrect_toast;
            mResults[mCurrentIndex]=0;
        }
        Toast.makeText(this, messageResId,Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == REQUEST_CODE_CHEAT) {
            if (data == null) {
                return;
            }
            mIsCheater = CheatActivity.wasAnswerShown(data);//// ге понимаю, пора спатеньки страница 130
        }
    }
}
