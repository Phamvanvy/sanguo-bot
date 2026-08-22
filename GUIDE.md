# Hướng dẫn chạy — server 2014 + client web + UI VM (ETF)

Tài liệu nhanh để bật toàn bộ stack và thử nghiệm client trình duyệt với
giao diện script `.etf` thật (observer mode). Chi tiết kiến trúc xem
`selfhost/README.md` và `web/README.md`.

## 1. Yêu cầu

- Docker Desktop (hoặc docker engine + compose plugin)
- Node.js ≥ 20 (cho bridge và test)
- Workspace gốc `Game/` đã được giải nén từ archive
  (`8pKq80XlJPUpwj39明珠三国.zip`) — server chạy trên bin/jar/data của nó.

## 2. Lần đầu tiên

```sh
sh selfhost/build_runtime.sh        # stage selfhost/runtime/ (~25 s khi đã warm)
npm --prefix web/bridge ci          # cài dependency bridge (1 lần duy nhất)
```

## 3. Bật server

```sh
docker compose -f selfhost/docker-compose.yml up -d
```

5 container: `mysql`, `accountserver`, `gameaccountserver`, `world`, `bridge`.
World mất **~90 s** boot (biên dịch mọi quest script). Chờ tới khi sẵn sàng:

```sh
docker compose -f selfhost/docker-compose.yml logs -f world | grep -m1 "World Started"
```

(Thấy dòng rồi thì Ctrl+C — container vẫn chạy nền.)

## 4. Tạo tài khoản + test login bằng CLI

```sh
sh selfhost/create_account.sh --name test1 --password 123456
node web/bridge/test_ws_g2.js --name test1 --password 123456
```

`test_ws_g2.js` đi hết đường: login → vào world → move, qua đúng đường
WebSocket↔TCP mà trình duyệt dùng.

## 5. Chơi trên trình duyệt

Mở:

```
http://127.0.0.1:8090/game.html
```

- Đăng nhập → chọn nhân vật → vào world.
- UI ETF (observer mode) tự bật ngay sau khi WebSocket connected — TRƯỚC cả
  login, nên script thấy toàn bộ ACCOUNT_LOGIN / ACTOR_LIST / ACTOR_LOGIN /
  GOMAP_ALLOW và init flood.
- Cửa sổ do script tạo sẽ được vẽ đè lên renderer của map (art 240×320 fit-scale).

### Debug UI VM từ console

```js
__game.vmObserver.statsSnapshot()
// {
//   rawReceived, handledByVM, handledByWorld, unhandled, overflowed,
//   topOpcodes: { "102": 12, ... },
//   liveVMs, windowCount, windows: ["game_panel/main", ...],
//   lastWindowEvent: { added: [...], opcodeBefore: 235 },   // opcode nào mở window
//   firstMissingSyscall: "0x....",   // syscall đầu tiên còn thiếu → port tiếp
//   firstVmError: "...",
// }
```

- `lastWindowEvent.opcodeBefore` là **bằng chứng live** về packet khiến một
  GWindow xuất hiện.
- F1 = nhật ký gói tin; WASD/mũi tên/Enter/số được forward vào VM như phím
  máy handheld (UP=1 DOWN=2 LEFT=3 RIGHT=4 FIRE=5…).

## 6. Test tự động (không cần server)

```sh
node --test "web/client/src/vm/*.test.mjs" \
            "web/client/src/assets/*.test.mjs" \
            "web/client/src/app/*.test.mjs" \
            web/bridge/asset-index.test.mjs
```

Boot smoke chạy chính các script `.etf.gz` trong `selfhost/runtime/data`
(không cần server đang chạy, chỉ cần thư mục data tồn tại).

Công cụ chẩn đoán VM:

```sh
node tools/vm/boot_probe.mjs          # boot chuỗi script, in missing/errors
node tools/vm/boot_hist.mjs           # đếm widget/window sau N frame
python tools/vm/syscall_lookup.py 0x1301   # đọc source VM.java của 1 syscall
```

## 7. Dừng / dọn dẹp

```sh
docker compose -f selfhost/docker-compose.yml down       # giữ volume mysql
docker compose -f selfhost/docker-compose.yml down -v    # xoá cả DB
```

`selfhost/runtime/` là thư mục sinh ra — **đừng sửa tay** (sẽ bị mất khi
build lại). Config sửa trong `selfhost/overlay/`, code Java sửa trong
`Game/**/src/`.

## Sự cố thường gặp

| Triệu chứng | Nguyên nhân / fix |
|---|---|
| Bridge log `missing dependencies` | Chưa chạy `npm --prefix web/bridge ci` |
| Login timeout | World chưa xong boot — chờ `World Started` |
| Port 8080 bị chiếm | Bridge cố ý dùng **8090** trên host (xem docker-compose) |
| Trang mở nhưng không có asset | DATA_DIR mount thiếu — kiểm tra `selfhost/runtime/data` tồn tại |
| `statsSnapshot.firstMissingSyscall` có giá trị | Script cần syscall đó — tra bằng `tools/vm/syscall_lookup.py <id>` rồi port |