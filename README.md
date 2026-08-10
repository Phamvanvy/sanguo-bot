# Sanguo Bot + Flow Extension

Bot tự động thao tác game qua Playwright/CDP nên không chiếm chuột và bàn phím
Windows. Extension Brave/Chrome cung cấp popup để chạy từng flow mà không cần gõ
lệnh Python; backend OS Input cũ vẫn có thể bật lại khi cần.

> Extension chỉ là giao diện điều khiển. Controller Python phải được chạy trên
> máy và cửa sổ game Brave phải đang mở.

## 1. Chuẩn bị môi trường

Yêu cầu:

- Windows 10/11.
- Python 3.12.
- Brave Browser.
- Đăng nhập game một lần trong profile `.brave-cdp-profile` do bot tự mở.

Mặc định `game.control_mode` là `cdp_attach`. Controller tự mở Brave bằng profile
riêng và cổng CDP 9222 nếu chưa có phiên đang chạy. Không cần giữ cửa sổ game ở
foreground; chuột vật lý vẫn dùng bình thường. Nếu game chặn CDP, đổi lại:

```yaml
game:
  control_mode: "os_input"
```

Mở PowerShell tại thư mục project:

```powershell
cd E:\repos\sanguo-bot
```

Nếu chưa có môi trường `.venv`:

```powershell
python -m venv .venv
.\.venv\Scripts\python.exe -m pip install -r requirements.txt
```

## 2. Chạy extension controller

Trong PowerShell tại thư mục project, chạy:

```powershell
powershell -ExecutionPolicy Bypass -File .\start-extension-server.ps1
```

Hoặc chạy từ bất kỳ thư mục nào:

```powershell
powershell -ExecutionPolicy Bypass -File "E:\repos\sanguo-bot\start-extension-server.ps1"
```

Controller đã sẵn sàng khi terminal hiện:

```text
Sanguo extension controller: http://127.0.0.1:8765
```

Giữ cửa sổ PowerShell này mở trong lúc sử dụng extension. Nhấn `Ctrl+C` để
dừng controller.

## 3. Cài extension vào Brave

1. Mở `brave://extensions`.
2. Bật **Developer mode** ở góc trên bên phải.
3. Chọn **Load unpacked**.
4. Chọn thư mục `E:\repos\sanguo-bot\extension`.
5. Ghim **Sanguo Flow Controller** lên thanh công cụ.
6. Bấm icon extension để mở danh sách flow.

Chấm xanh trên popup nghĩa là extension đã kết nối controller. Nếu chấm đỏ,
kiểm tra lại bước 2.

## 4. Các flow hiện có

### Tự động đánh

Bấm **Đánh** và 7 nút kỹ năng trong cụm chiến đấu theo vòng lặp. Flow không tự di chuyển
hay chọn tọa độ quái; game tự chọn mục tiêu khi nút **Đánh** được bấm. Bấm **Dừng flow đang
chạy** trên panel để dừng.

### Full auto

Gom nhiều nhiệm vụ từ các dấu `!`, sau đó tự làm, trả nhiệm vụ, bấm
`Hoàn thành` và tiếp tục nhận nhiệm vụ mới.

### Gom nhiệm vụ

Mở Map, tìm dấu `!`, tự chạy tới NPC và bấm `Nhận`. Bot gom nhiều nhiệm vụ
trước khi bắt đầu làm.

### Làm nhiệm vụ

Làm các nhiệm vụ đã nhận:

- Bấm nhiệm vụ để tự chạy tới mục tiêu.
- Đánh quái hoặc thu thập.
- Tự chạy về NPC trả nhiệm vụ.
- Bấm `Hoàn thành`.

### Cầu phúc

Flow cố định, không dùng AI:

1. Mở `C.Phúc`.
2. Bấm `Cầu phúc 10 lần`.
3. Bấm `OK`.
4. Tắt thông báo kết quả và lặp lại.

Mặc định flow lặp không giới hạn cho tới khi bấm **Dừng flow đang chạy**.
Flow này tiêu Xu liên tục. Có thể đặt giới hạn bằng `max_cycles` trong
`config.yaml` (`0` nghĩa là không giới hạn).

### Đổi code

Trước khi chạy, nhân vật phải đứng cạnh NPC **Nhân viên đổi mã kích hoạt** như
vị trí đã calibrate.

Flow nhập lần lượt:

- `MCH5EXPH1-100`.
- `MCH5VIP1` đến `MCH5VIP12`.
- `MCH5TEST1` đến `MCH5TEST100`.

Sau mỗi mã, bot đóng thông báo, mở lại NPC và nhập mã tiếp theo. Code đã dùng
hoặc không hợp lệ không làm dừng toàn bộ danh sách. Nhóm `MCH5TEST` có thể yêu
cầu dùng hết số Xu đã nhận trước khi mã tiếp theo thành công.

## 5. Dừng flow

Bấm **Dừng flow đang chạy** trong popup. Nếu popup không phản hồi, quay lại cửa
sổ PowerShell và nhấn `Ctrl+C`.

## 6. Chạy bot không qua extension

Chạy thật:

```powershell
.\.venv\Scripts\python.exe -m src.bot --live
```

Giới hạn số nhiệm vụ:

```powershell
.\.venv\Scripts\python.exe -m src.bot --live --max-quests 5
```

## 7. Cấu hình và chỉnh tọa độ

Tất cả tọa độ là tỷ lệ `0..1` của canvas game và nằm trong `config.yaml`:

- `quest_actions`: đi đường, đánh, thu thập, nhận/trả nhiệm vụ.
- `activity_macros.blessing`: flow Cầu phúc.
- `activity_macros.code_redeem`: NPC, ô nhập và danh sách code.
- `game.os_input.canvas_insets_px`: mép canvas so với cửa sổ Brave.

Controller extension tự chuyển inset pixel sang tỷ lệ theo kích thước cửa sổ,
nên hỗ trợ cả cửa sổ 1366x900 và Brave maximized.

## 8. Xử lý lỗi

### Popup báo controller chưa chạy

Chạy lại:

```powershell
powershell -ExecutionPolicy Bypass -File .\start-extension-server.ps1
```

Nếu cổng `8765` đang được dùng, đóng controller cũ hoặc tiến trình Python cũ
rồi chạy lại.

### PowerShell chặn script

Dùng đúng lệnh có `-ExecutionPolicy Bypass` ở trên. Lệnh này chỉ áp dụng cho
lần chạy hiện tại, không thay đổi policy toàn hệ thống.

### Bot click lệch khi dùng OS Input

- Đảm bảo game nằm trong cửa sổ Brave đã được bot nhận diện.
- Không thu nhỏ cửa sổ trong khi flow đang chạy.
- Kiểm tra `canvas_insets_px` và các tọa độ flow trong `config.yaml`.
- Dùng `tools/calibrate.py` nếu giao diện game thay đổi.

Với `cdp_attach`, tọa độ được lấy trực tiếp từ bounding box của `#screen`, không
dùng `canvas_insets_px` và không phụ thuộc vị trí cửa sổ trên màn hình.

### Flow bị kẹt

1. Bấm **Dừng flow đang chạy**.
2. Đóng các popup còn mở trong game.
3. Đưa nhân vật về màn hình game chính.
4. Chạy lại flow cần thiết.

Log của extension được lưu tại:

```text
logs/extension-flow.log
```

## 9. Cấu trúc chính

```text
extension/                 Popup Brave/Chrome
src/extension_server.py    Local controller và flow worker
src/game/actions.py        Click, di chuyển, đánh và nhận nhiệm vụ
src/game/quests.py         Vòng lặp thực thi nhiệm vụ
config.yaml                Tọa độ và cấu hình flow
start-extension-server.ps1 Script khởi động controller
```
