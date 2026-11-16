package com.example.backup.view;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.backup.R;
import com.example.backup.adapter.ChatAdapter;
import com.example.backup.controller.AiAssistantController;
import com.example.backup.model.ChatMessage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatAiActivity extends AppCompatActivity {

    private ImageButton btnBack, btnClear, btnSend;
    private EditText etInput;
    private RecyclerView rvChat;
    private View chatHeader, chatInputBar;
    private View typingIndicator;

    private ChatAdapter chatAdapter;
    private final List<ChatMessage> messageList = new ArrayList<>();
    private boolean isWaitingReply = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_ai);

        chatHeader      = findViewById(R.id.chatHeader);
        chatInputBar    = findViewById(R.id.chatInputBar);
        typingIndicator = findViewById(R.id.typingIndicator);
        btnBack         = findViewById(R.id.btnChatBack);
        btnClear        = findViewById(R.id.btnClearChat);
        btnSend         = findViewById(R.id.btnSendMessage);
        etInput         = findViewById(R.id.etChatInput);
        rvChat          = findViewById(R.id.rvChatMessages);

        handleWindowInsets();

        btnBack.setOnClickListener(v -> finish());

        btnClear.setOnClickListener(v -> {
            messageList.clear();
            addWelcomeMessage();
            chatAdapter.notifyDataSetChanged();
            Toast.makeText(this, "Đã làm mới cuộc hội thoại", Toast.LENGTH_SHORT).show();
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        rvChat.setLayoutManager(layoutManager);
        chatAdapter = new ChatAdapter(this, messageList);
        rvChat.setAdapter(chatAdapter);

        addWelcomeMessage();

        btnSend.setOnClickListener(v -> sendUserMessage());

        etInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendUserMessage();
                return true;
            }
            return false;
        });

        setupQuickChips();
    }

    private void handleWindowInsets() {
        View root = findViewById(R.id.chatRoot);
        int padLeft   = chatHeader.getPaddingLeft();
        int padRight  = chatHeader.getPaddingRight();
        int padBottom = chatHeader.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            int extraTop = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 6, getResources().getDisplayMetrics());
            chatHeader.setPadding(padLeft, insets.top + extraTop, padRight, padBottom);
            chatInputBar.setPadding(
                    chatInputBar.getPaddingLeft(), chatInputBar.getPaddingTop(),
                    chatInputBar.getPaddingRight(),
                    insets.bottom + (int) TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics()));
            return windowInsets;
        });
    }

    private void addWelcomeMessage() {
        String time    = getCurrentTime();
        String welcome = "Xin chào! Mình là Trợ Lý AI Nit Flex 🎬✨\n\n"
                + "Bạn có thể hỏi mình:\n"
                + "• Gợi ý phim theo thể loại hoặc tâm trạng\n"
                + "• Tìm phim có điểm đánh giá cao nhất\n"
                + "• Hỏi về diễn viên, nội dung phim cụ thể\n"
                + "• Hoặc gõ tên phim bất kỳ để tìm kiếm!\n\n"
                + "Bạn muốn xem gì hôm nay? 😊";
        messageList.add(new ChatMessage(welcome, false, time));
    }

    private void sendUserMessage() {
        String text = etInput.getText().toString().trim();
        if (text.isEmpty()) return;
        if (isWaitingReply) {
            Toast.makeText(this, "Đang chờ AI trả lời...", Toast.LENGTH_SHORT).show();
            return;
        }

        String time = getCurrentTime();
        messageList.add(new ChatMessage(text, true, time));
        chatAdapter.notifyItemInserted(messageList.size() - 1);
        scrollToBottom();
        etInput.setText("");

        isWaitingReply = true;
        btnSend.setEnabled(false);
        if (typingIndicator != null) typingIndicator.setVisibility(View.VISIBLE);

        AiAssistantController.askAi(text, (replyText, suggestedMovie) -> {

            if (typingIndicator != null) typingIndicator.setVisibility(View.GONE);
            isWaitingReply = false;
            btnSend.setEnabled(true);

            String finalReply = (replyText != null && !replyText.isEmpty())
                    ? replyText : "🤖 Xin lỗi, hãy thử lại nhé!";

            String aiTime = getCurrentTime();
            messageList.add(new ChatMessage(finalReply, false, aiTime, suggestedMovie));
            chatAdapter.notifyItemInserted(messageList.size() - 1);
            scrollToBottom();
        });
    }

    private void scrollToBottom() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (!messageList.isEmpty()) {
                rvChat.smoothScrollToPosition(messageList.size() - 1);
            }
        }, 100);
    }

    private void setupQuickChips() {

        setChipQuery(R.id.chipHot,    "Phim nào đang hot và nhiều người xem nhất?");
        setChipQuery(R.id.chipAction, "Gợi ý phim hành động hay nhất cho mình");
        setChipQuery(R.id.chipAnime,  "Có phim anime hoặc hoạt hình Nhật Bản nào hay không?");
        setChipQuery(R.id.chipSciFi,  "Gợi ý phim khoa học viễn tưởng vũ trụ");
        setChipQuery(R.id.chipMood,   "Hôm nay tôi thấy buồn, nên xem phim gì?");
        setChipQuery(R.id.chipTop,    "Phim có điểm đánh giá cao nhất là phim gì?");
        setChipQuery(R.id.chipActor,  "Cho tôi biết danh sách diễn viên nổi tiếng");
    }

    private void setChipQuery(int chipId, String query) {
        View chip = findViewById(chipId);
        if (chip != null) {
            chip.setOnClickListener(v -> {
                etInput.setText(query);
                sendUserMessage();
            });
        }
    }

    private String getCurrentTime() {
        return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
    }
}
