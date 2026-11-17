package com.example.backup.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.telephony.SmsManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.backup.R;

import java.security.SecureRandom;
import java.util.Locale;

public class OtpService {

    private static final String TAG = "OtpService";
    public static final String OTP_CHANNEL_ID = "nitflex_otp_channel";
    public static final String OTP_CHANNEL_NAME = "Mã Xác Thực OTP Nit Flex";

    public static String generateOtp() {
        SecureRandom random = new SecureRandom();
        int num = 100000 + random.nextInt(900000);
        return String.format(Locale.US, "%06d", num);
    }

    public static void createOtpNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    OTP_CHANNEL_ID,
                    OTP_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Thông báo gửi mã xác thực OTP đăng ký tài khoản");
            channel.enableLights(true);
            channel.enableVibration(true);

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    public static void sendOtp(Context context, String phoneNumber, String otpCode) {
        if (context == null || phoneNumber == null || otpCode == null) return;

        createOtpNotificationChannel(context);

        // 1. Gửi SMS qua SmsManager (nếu máy có SIM / modem)
        try {
            SmsManager smsManager;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                smsManager = context.getSystemService(SmsManager.class);
            } else {
                smsManager = SmsManager.getDefault();
            }
            if (smsManager != null) {
                String message = "[NIT FLEX] Ma xac thuc OTP cua ban la: " + otpCode + ". Ma co hieu luc trong 60 giay.";
                smsManager.sendTextMessage(phoneNumber, null, message, null, null);
                Log.d(TAG, "Đã gửi SMS OTP tới: " + phoneNumber);
            }
        } catch (Exception e) {
            Log.w(TAG, "Không thể gửi SMS (máy ảo hoặc chưa cấp quyền SMS): " + e.getMessage());
        }

        // 2. Hiển thị Push Notification nổi bật lên thanh thông báo
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
            }

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, OTP_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_play_arrow)
                    .setContentTitle("📲 MÃ XÁC THỰC OTP: " + otpCode)
                    .setContentText("Mã OTP đăng ký cho SĐT " + phoneNumber + " là: " + otpCode)
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .setBigContentTitle("📲 NIT FLEX - MÃ XÁC THỰC OTP")
                            .bigText("Mã OTP đăng ký tài khoản cho số điện thoại " + phoneNumber + " là: " + otpCode
                                    + "\n\nMã có hiệu lực trong 60 giây. Vui lòng không chia sẻ mã này cho bất kỳ ai!"))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true);

            NotificationManagerCompat.from(context).notify(9988, builder.build());
            Log.d(TAG, "Đã đẩy Notification OTP: " + otpCode);
        } catch (Exception e) {
            Log.w(TAG, "Không thể đẩy Notification OTP: " + e.getMessage());
        }
    }
}
