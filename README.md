# Đảo Ví cho Android

Ứng dụng tự đọc thông báo biến động số dư từ ngân hàng và ghi vào sổ, rồi vẽ chi tiêu của bạn thành một hòn đảo trên hải đồ cổ.

Mọi dữ liệu nằm trên điện thoại của bạn. Ứng dụng không gửi giao dịch đi đâu cả. Kết nối mạng chỉ dùng để tải phông chữ.

## Cách 1: Để GitHub build giúp (không cần cài gì lên máy tính)

1. Tạo tài khoản GitHub nếu chưa có, rồi tạo một repository mới, chọn **Private**.
2. Giải nén file zip. Trong repository, bấm **Add file → Upload files** và kéo toàn bộ nội dung thư mục `DaoVi` vào (cả các thư mục `app`, `gradle`, `.github`), rồi bấm **Commit changes**.
   - Thư mục `.github` là thư mục ẩn, máy tính có thể không cho kéo vào. Nếu sau khi tải lên mà không thấy nó, hãy bấm **Add file → Create new file**, gõ tên `.github/workflows/build-apk.yml`, rồi dán nội dung của file đó vào.
3. Mở tab **Actions**. Lượt chạy **Build APK** sẽ tự bắt đầu, mất khoảng 5 đến 10 phút. Nếu không thấy, chọn **Build APK** rồi bấm **Run workflow**.
4. Khi có dấu tích xanh, mở lượt chạy đó, kéo xuống mục **Artifacts**, tải **DaoVi-apk**. Giải nén ra được `app-debug.apk`.
5. Chép file APK sang điện thoại và mở để cài. Android sẽ hỏi cho phép cài ứng dụng từ nguồn này, hãy đồng ý.

## Cách 2: Android Studio

Mở Android Studio, chọn **Open**, trỏ tới thư mục `DaoVi`, đợi đồng bộ xong rồi bấm **Run** khi điện thoại đang cắm cáp và đã bật gỡ lỗi USB.

## Lần đầu mở ứng dụng

1. **Cấp quyền đọc thông báo**: bấm nút trong thẻ *Trạm điện tín*, tìm **Đảo Ví** trong danh sách và bật lên.
   - Trên Android 13 trở lên, ứng dụng cài ngoài cửa hàng có thể bị báo **Cài đặt bị hạn chế**. Khi đó vào *Cài đặt → Ứng dụng → Đảo Ví*, bấm menu ba chấm ở góc trên, chọn **Cho phép cài đặt bị hạn chế**, rồi quay lại bật quyền.
2. **Chọn ứng dụng để nghe**: tick ứng dụng ngân hàng, ví điện tử của bạn. Nếu ngân hàng báo biến động bằng SMS, tick thêm ứng dụng **Tin nhắn**. Trạm chỉ giữ những tin có số tiền dạng biến động số dư.
3. **Giữ trạm luôn thức**: trên Xiaomi, Oppo, Vivo, Samsung, hãy tắt tối ưu pin cho Đảo Ví và bật tự khởi động, nếu không hệ thống có thể tắt trạm nghe khi máy ngủ.
4. Bấm **Gửi điện tín thử** để xem toàn bộ luồng chạy.
5. Giữ ngón tay lên màn hình chính, chọn **Widget**, kéo **Hải đồ Đảo Ví** ra để xem ngân sách tháng ngay ngoài màn hình.

## Chuyển dữ liệu từ bản web sang ứng dụng

Trên bản web: *Tùy chỉnh đảo → Sao lưu → Sao chép dữ liệu*. Trên ứng dụng: dán vào ô ở cùng mục rồi bấm **Khôi phục từ ô bên dưới**.

## Cần biết

- Một số ngân hàng chỉ báo "Bạn có biến động số dư mới" mà không kèm số tiền. Với những ngân hàng này, hãy bật nhận SMS biến động số dư và cho trạm nghe ứng dụng Tin nhắn.
- Khoản nào đảo không đoán được vùng sẽ được neo tạm ở Bến Cảng và đánh dấu *chờ xếp vùng*. Bạn đổi vùng một lần, đảo nhớ nơi đó cho các lần sau.
- Repository có sẵn một khóa ký cố định (`app/daovi.keystore`) để các bản build sau cài đè lên bản cũ mà không mất dữ liệu. Hãy để repository ở chế độ riêng tư.
- Thư thuyền trưởng và két chung đồng bộ chỉ có ở bản mở trong claude.ai. Bản trên máy giữ sổ sách riêng tư hoàn toàn.

## Cấu trúc mã

- `BankListenerService.kt`: trạm nghe thông báo, lọc theo ứng dụng đã chọn, bỏ qua tin không phải biến động số dư.
- `InboxDb.kt`: hộp thư điện tín (SQLite), chống ghi trùng bằng mã băm.
- `Parser.kt`: bộ đọc nhanh số tiền và chiều tiền.
- `Notifier.kt`: thông báo "đã vào sổ".
- `IslandWidget.kt`: widget màn hình chính.
- `Bridge.kt`, `MainActivity.kt`: cầu nối giữa phần máy và giao diện đảo.
- `assets/www/index.html`: toàn bộ giao diện hải đồ.
