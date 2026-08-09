# Sanguo Flow Controller

1. Chạy `start-extension-server.ps1` từ thư mục project.
2. Mở `brave://extensions`, bật **Developer mode**.
3. Chọn **Load unpacked** và trỏ tới thư mục `extension` này.
4. Mở `https://play.minhchauh5.com/` và reload tab. Panel được ghim trực tiếp trên game.

Panel có thể kéo thả và thu gọn bằng nút `−`; bấm icon extension để ẩn/hiện.
`Cầu phúc` và `Đổi code` dùng Chrome Debugger Protocol nên không chiếm chuột hệ
thống và có thể chạy khi tab nằm nền. Các flow cần computer vision vẫn đi qua
Python OS-input. Đóng DevTools của tab game trước khi chạy flow không-chuột.
