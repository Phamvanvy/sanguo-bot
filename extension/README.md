# Sanguo Flow Controller

1. Chạy `start-extension-server.ps1` từ thư mục project.
2. Mở `brave://extensions`, bật **Developer mode**.
3. Chọn **Load unpacked** và trỏ tới thư mục `extension` này.
4. Mở `https://play.minhchauh5.com/` và reload tab. Panel được ghim trực tiếp trên game.

Panel có thể kéo thả và thu gọn bằng nút `−`; bấm icon extension để ẩn/hiện.
Flow **Học kỹ năng thú cưỡi** dùng sách thứ 3 đang hiển thị, bấm **Lĩnh ngộ** và
xác nhận **OK** đúng một lần. Hãy mở sẵn tab **Thú cưỡi** trước khi chạy flow.
Flow **Tự động đánh** chỉ bấm nút **Đánh** và 7 nút kỹ năng trong cụm chiến đấu,
lặp liên tục cho đến khi bấm **Dừng flow đang chạy**.
Flow **Phó bản Cổ Mộ** ghi từng bước vào `logs/extension-network.log` (các dòng
`dungeon_start`, `dungeon_step`, `dungeon_step_done`, `flow_end`): kích thước màn
hình lúc bắt đầu, bước định làm, thời gian thực tế, số vòng đánh và phần tử mà
mỗi click trúng (`canvas#screen` là trúng game). Controller phải đang chạy thì log
mới được ghi xuống file.
Flow không đoán bằng hình mà đọc thẳng dữ liệu game: `network_probe.js` nghe (chỉ
đọc, không gửi gì) gói tin WebSocket của game theo giao thức "UA" của client gốc để
biết map hiện tại, vị trí nhân vật và quái còn sống quanh đó. Hết quái trong
`monster_radius` thì đi tiếp ngay; tới nơi hay qua cổng cũng nhận theo gói tin chứ
không chờ đủ giây. Cổng chưa qua được (còn quái) thì đánh rồi vào
lại (dòng `dungeon_ambush`); đang đi thì không bấm Đánh (nhân vật sẽ đứng lại đánh). Cần bấm F5 tab game sau khi cập nhật extension.
Lộ trình Cổ Mộ và **Phó bản Hà Đông** ghi bằng tọa độ game (`goto: [x, y]`, số hiện
cạnh tên map; 1 đơn vị = 8 pixel map) kèm `map_size`. Khung Map vẽ map 1:1 trên màn
hình logic 1280x640 và cuộn theo nhân vật, nên flow tự tính điểm bấm và nút X từ vị
trí hiện tại; flow tìm đường trước trên lưới chỗ đi được của map (`walk_grids.js`, tạo từ dữ liệu map
gốc bằng `node tools/assets/walk_grid.mjs`) rồi mỗi lần mở Map bấm điểm xa nhất trên
đường còn hiện trên Map (mũi tên Map không cuộn được); lệch thì bấm bù. Chỗ boss được
lệch ±1 ô, còn cửa phải đứng đúng ô (không vào thì thử ô cửa theo dữ liệu map cạnh đó).
Quái sau tường (phải đi vòng quá `monster_path_tiles` ô) hay game báo không đánh được
(gói ATTACK_FAIL, vd. "Mục tiêu không nằm trong tầm nhìn") thì bỏ, đi tiếp; hết quái thì
không bấm Đánh nữa. Hà Đông khó
bắt đầu khi đã đứng trong Hà Đông Thảo Tặc (Ngoài); Hà Đông dễ chỉ đi 66,22 rồi đánh.
**Phó bản Cổ Mộ (dễ/khó)** cũng là một nút: đứng trong bản dễ hay khó rồi bấm chạy
(bấm ở sảnh thì vào bản khó qua NPC).
**Thiên Long trận (dễ/khó)** là một nút (hai bản giống nhau): đứng trong trận rồi bấm
chạy, flow lần lượt đánh 5 boss ở 20,31 → 88,10 → 145,45 → 41,117 → 77,64.
Các flow hoạt động nhanh chạy bằng cặp `mousedown`/`mouseup` mà canvas
TeaVM của game đăng ký: không dùng Chrome Debugger Protocol và không chiếm
chuột/bàn phím hệ thống. Giữ tab game mở; trình
duyệt có thể giảm tốc JavaScript nếu cửa sổ bị thu nhỏ hoặc tab bị đóng băng.
Các flow cần computer vision bám vào cửa sổ game hiện tại và dùng
OS input. Không bật CDP/remote debugging cho cửa sổ game vì guard của trang sẽ
chủ động đóng WebSocket.
