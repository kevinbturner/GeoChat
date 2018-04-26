package edu.fsu.cs.mobile.geochat;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.text.format.DateFormat;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class ChatActivity extends AppCompatActivity {

    private TextView send;
    private EditText message;
    private FirebaseListAdapter<ChatMessage> fAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        send = (TextView) findViewById(R.id.send);
        message = (EditText) findViewById(R.id.chatbox);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseDatabase.getInstance().getReference().push().setValue(new ChatMessage(message.getText().toString(), FirebaseAuth.getInstance().getCurrentUser().getEmail()));
                message.setText("");
                displayChatMessage();
            }
        });

        displayChatMessage();
    }

    public void displayChatMessage(){
        ListView messageList = (ListView) findViewById(R.id.messageList);
        fAdapter = new FirebaseListAdapter<ChatMessage>(this, ChatMessage.class, R.layout.message_item, FirebaseDatabase.getInstance().getReference()) {
            @Override
            protected void populateView(View v, ChatMessage model, int position) {
                TextView message, user, date;
                message = (TextView) v.findViewById(R.id.message_text);
                user = (TextView) v.findViewById(R.id.message_user);
                date = (TextView) v.findViewById(R.id.message_time);

                message.setText(model.getText());
                user.setText(model.getUser());
                date.setText(DateFormat.format("dd-MM-yyyy (HH:mm:ss)", model.getTime()));
            }
        };

        messageList.setAdapter(fAdapter);
    }
}
