package com.example.backup.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.backup.MainActivity;
import com.example.backup.R;
import com.example.backup.adapter.ChatAdapter;
import com.example.backup.controller.AiAssistantController;
import com.example.backup.model.ChatMessage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AiChatFragment extends Fragment {

    private ImageButton btnBack, btnClear, btnSend;
    private EditText etInput;
    private RecyclerView rvChat;
    private View typingIndicator;

    private ChatAdapter chatAdapter;
    private final List<ChatMessage> messageList = new ArrayList<>();
    private boolean isWaitingReply = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ai_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).selectTab(R.id.nav_home);
                }
            });
        }

        if (btnClear != null) {
            btnClear.setOnClickListener(v -> {
                messageList.clear();
                addWelcomeMessage();
                chatAdapter.notifyDataSetChanged();
                Toast.makeText(requireContext(), "Đã làm mới cuộc hội thoại", Toast.LENGTH_SHORT).show();
            });
        }

        // Setup Chat RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        layoutManager.setStackFromEnd(true);
        rvChat.setLayoutManager(layoutManager);
        chatAdapter = new ChatAdapter(requireContext(), messageList);
        rvChat.setAdapter(chatAdapter);

        // Tin nhắn chào mừng ban đầu nếu chưa có
        if (messageList.isEmpty()) {
            addWelcomeMessage();
        }

        btnSend.setOnClickListener(v -> sendUserMessage());

        etInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendUserMessage();
                return true;
            }
            return false;
        });

        setupQuickChips(view);
    }

    private void initViews(View view) {
        typingIndicator = view.findViewById(R.id.typingIndicator);
        btnBack         = view.findViewById(R.id.btnChatBack);
        btnClear        = view.findViewById(R.id.btnClearChat);
        btnSend         = view.findViewById(R.id.btnSendMessage);
        etInput         = view.findViewById(R.id.etChatInput);
        rvChat          = view.findViewById(R.id.rvChatMessages);
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
            Toast.makeText(requireContext(), "Đang chờ AI trả lời...", Toast.LENGTH_SHORT).show();
            return;
        }

        String time = getCurrentTime();
        messageList.add(new ChatMessage(text, true, time));
        chatAdapter.notifyItemInserted(messageList.size() - 1);
        scrollToBottom();
        etInput.setText("");

        // Hiện typing indicator
        isWaitingReply = true;
        btnSend.setEnabled(false);
        if (typingIndicator != null) typingIndicator.setVisibility(View.VISIBLE);

        // Gọi AI xử lý
        AiAssistantController.askAi(text, (replyText, suggestedMovie) -> {
            if (!isAdded()) return;
            // Ẩn typing indicator
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
            if (!messageList.isEmpty() && rvChat != null) {
                rvChat.smoothScrollToPosition(messageList.size() - 1);
            }
        }, 100);
    }

    private void setupQuickChips(View view) {
        setChipQuery(view, R.id.chipHot,     "Phim nào đang hot và nhiều người xem nhất?");
        setChipQuery(view, R.id.chipRomance, "Gợi ý cho mình phim thể loại tình cảm lãng mạn");
        setChipQuery(view, R.id.chipAction,  "Gợi ý phim hành động hay nhất cho mình");
        setChipQuery(view, R.id.chipAnime,   "Có phim anime hoặc hoạt hình Nhật Bản nào hay không?");
        setChipQuery(view, R.id.chipSciFi,   "Gợi ý phim khoa học viễn tưởng vũ trụ");
        setChipQuery(view, R.id.chipMood,    "Hôm nay tôi thấy buồn, nên xem phim gì?");
        setChipQuery(view, R.id.chipTop,     "Phim có điểm đánh giá cao nhất là phim gì?");
        setChipQuery(view, R.id.chipActor,   "Cho tôi biết danh sách diễn viên nổi tiếng");
    }

    private void setChipQuery(View root, int chipId, String query) {
        View chip = root.findViewById(chipId);
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
