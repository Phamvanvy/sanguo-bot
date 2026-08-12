# Sanguo Flow Controller

1. Chạy `start-extension-server.ps1` từ thư mục project.
2. Mở `brave://extensions`, bật **Developer mode**.
3. Chọn **Load unpacked** và trỏ tới thư mục `extension` này.
4. Mở `https://play.minhchauh5.com/` và reload tab. Panel được ghim trực tiếp trên game.

Panel có thể kéo thả và thu gọn bằng nút `−`; bấm icon extension để ẩn/hiện.
Flow **Tự động đánh** chỉ bấm nút **Đánh** và 7 nút kỹ năng trong cụm chiến đấu,
lặp liên tục cho đến khi bấm **Dừng flow đang chạy**.
Các flow hoạt động nhanh chạy bằng cặp `mousedown`/`mouseup` mà canvas
TeaVM của game đăng ký: không dùng Chrome Debugger Protocol và không chiếm
chuột/bàn phím hệ thống. Giữ tab game mở; trình
duyệt có thể giảm tốc JavaScript nếu cửa sổ bị thu nhỏ hoặc tab bị đóng băng.
Các flow cần computer vision bám vào cửa sổ game hiện tại và dùng
OS input. Không bật CDP/remote debugging cho cửa sổ game vì guard của trang sẽ
chủ động đóng WebSocket.
