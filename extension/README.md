# Sanguo Flow Controller

1. Chạy `start-extension-server.ps1` từ thư mục project.
2. Mở `brave://extensions`, bật **Developer mode**.
3. Chọn **Load unpacked** và trỏ tới thư mục `extension` này.
4. Mở `https://play.minhchauh5.com/`, ghim extension, bấm icon ngay trên tab game và chọn flow.

Popup gửi title của tab đang chọn cho controller tại `127.0.0.1:8765`; controller
chỉ bám vào cửa sổ chứa tab đó và không mở tab mới. Mọi click thật vẫn đi qua
Python OS-input; extension không inject script vào game và không dùng CDP.
